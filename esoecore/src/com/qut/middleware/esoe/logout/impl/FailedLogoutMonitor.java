/* 
 * Copyright 2006, Queensland University of Technology
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy of 
 * the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations under 
 * the License.
 * 
 * Author: Andre Zitelli
 * Creation Date: 24/1/2007
 * 
 * Purpose: A thread used to monitor the cache of Failed LogoutRequests. Such failures occur when
 * an SPEP fails to respond in an acceptable way to a logout request from the ESOE. This thread
 * will continually poll the cache of failures at regular intervals attempting to resend them until
 * they are successfull, or they expire.
 */
package com.qut.middleware.esoe.logout.impl;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.qut.middleware.crypto.KeystoreResolver;
import com.qut.middleware.esoe.MonitorThread;
import com.qut.middleware.esoe.logout.LogoutMechanism;
import com.qut.middleware.esoe.logout.bean.FailedLogout;
import com.qut.middleware.esoe.logout.bean.FailedLogoutRepository;
import com.qut.middleware.saml2.SchemaConstants;
import com.qut.middleware.saml2.exception.MarshallerException;
import com.qut.middleware.saml2.exception.ReferenceValueException;
import com.qut.middleware.saml2.exception.SignatureValueException;
import com.qut.middleware.saml2.exception.UnmarshallerException;
import com.qut.middleware.saml2.handler.Unmarshaller;
import com.qut.middleware.saml2.handler.impl.UnmarshallerImpl;
import com.qut.middleware.saml2.schemas.protocol.LogoutRequest;

/** A thread used to monitor the cache of Failed LogoutRequests. Such failures occur when
 * an SPEP fails to respond in an acceptable way to a logout request from the ESOE. This thread
 * will continually poll the cache of failures at regular intervals attempting to resend them until
 * they are successful, or they expire.
 */
public class FailedLogoutMonitor extends Thread implements MonitorThread
{
	private FailedLogoutRepository logoutFailures;

	private volatile boolean running;
	
	private int retryInterval;
	private int maxFailureAge;

	private KeystoreResolver keyStoreResolver;
	private LogoutMechanism logoutMechanism; 
		
	private String[] schemas = new String[] { SchemaConstants.samlProtocol };
	private final String PKGNAMES = LogoutRequest.class.getPackage().getName();
	private Unmarshaller<LogoutRequest> logoutRequestUnmarshaller;
	
	
	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(FailedLogoutMonitor.class.getName());

	/**
	 * @param failureRep
	 *            The logout failure repository instance to be monitored.
	 * @param metadata
	 *            The metadata object to be used when resolving SPEP information
	 * @param wsClient
	 *            The ESOE web service object used to send LogoutRequests to SPEPs.
	 * @param retryInterval
	 *            The retry interval between attempting logout requests to SPEPs, seconds.
	 * @param maxFailureAge
	 *            The time in minutes that a logout failure will be held in the repository, in seconds.
	 * @throws UnmarshallerException if the LogoutREquest unmarshaller cannot be created.
	 */
	public FailedLogoutMonitor(FailedLogoutRepository failureRep, KeystoreResolver keyStoreResolver, 
			LogoutMechanism logoutMechanism, int retryInterval, int maxFailureAge) throws UnmarshallerException, MarshallerException
	{
		
		if (failureRep == null)
			throw new IllegalArgumentException(Messages.getString("FailedLogoutMonitor.0"));  //$NON-NLS-1$

		if (keyStoreResolver == null)
			throw new IllegalArgumentException(Messages.getString("FailedLogoutMonitor.15")); //$NON-NLS-1$
		
		if(logoutMechanism == null)
			throw new IllegalArgumentException("Param logoutMechanism MUST NOT be null !");
		
		if (retryInterval <= 0)
			throw new IllegalArgumentException(Messages.getString("FailedLogoutMonitor.3"));  //$NON-NLS-1$

		if (maxFailureAge < 0 || (maxFailureAge > Integer.MAX_VALUE / 1000))
			throw new IllegalArgumentException(Messages.getString("FailedLogoutMonitor.4")); //$NON-NLS-1$
	
		this.logoutFailures = failureRep;
		this.retryInterval = retryInterval * 1000 ;
		this.maxFailureAge = maxFailureAge * 1000 ;
		this.keyStoreResolver = keyStoreResolver;
		this.logoutMechanism = logoutMechanism;
		
		this.logoutRequestUnmarshaller = new UnmarshallerImpl<LogoutRequest>(this.PKGNAMES, this.schemas, this.keyStoreResolver);
				
		this.logger.info(MessageFormat.format(Messages.getString("FailedLogoutMonitor.5"),  maxFailureAge) ); //$NON-NLS-1$
	
		this.setName("FailedLogout Monitor Thread"); //$NON-NLS-1$ 
		
		// start the thread (calls the run method)
		this.setDaemon(true);
		this.start();
	}

	
	/*
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		this.setRunning(true);
		
		while (this.isRunning())
		{
			// retry failed updates until cleared [ thread ]
			try
			{
				sleep(this.retryInterval);
				
				this.flushRepository();
			}
			catch(InterruptedException e)
			{
				if(!this.running)
					break;
			}
			// ignore interrupts and other non-runtime exceptions
			catch (Exception e)
			{
				this.logger.trace(e.getLocalizedMessage(), e);
			}
		}
		
		this.logger.info(this.getName() + Messages.getString("FailedLogoutMonitor.10")); //$NON-NLS-1$
		
		return;
	}

	
	
	
	/*
	 * Iterate through the list of failure objects in the repository and attempt to resend the SPEP notification. If a
	 * request cannot be sent, it remains in the repository, else it is removed. If a failure cannot be sent and it is
	 * older than this.maxFailureAge, it is also removed.
	 * 
	 */
	private void flushRepository()
	{		
		LogoutMechanism.result result = LogoutMechanism.result.LogoutRequestFailed;
		
		this.logger.debug(MessageFormat.format(Messages.getString("FailedLogoutMonitor.18"), this.logoutFailures.getSize()) );  //$NON-NLS-1$
		
		Iterator<FailedLogout> iter = this.logoutFailures.getFailures().iterator();

		// while failures in repository, send update request to associated SPEP and delete
		while (iter.hasNext())
		{
			FailedLogout failure = iter.next();
			
			// call logout mechanism, but do not store logout again if failed so we can determine if it should be removed.
			result = this.sendLogoutRequest(failure.getRequestDocument(), failure.getEndPoint(), false);
				
			if (result == LogoutMechanism.result.LogoutSuccessful)
			{
				this.logger.info(MessageFormat.format(Messages.getString("FailedLogoutMonitor.11"),  failure.getEndPoint()) ); //$NON-NLS-1$
				this.logoutFailures.remove(failure);
			}
			else
			// see if it's time to expire the record
			{
				this.logger.debug(MessageFormat.format(Messages.getString("FailedLogoutMonitor.12"),  failure.getEndPoint()) ); //$NON-NLS-1$
				
				int age = (int) (System.currentTimeMillis() - failure.getTimeStamp().getTime() );
				
				this.logger.debug(MessageFormat.format("Comparing failure age of {0} milliseconds to maxFailureAge: {1} milliseconds.",age ,this.maxFailureAge) ); //$NON-NLS-1$ 
				
				if (age > this.maxFailureAge)
				{
					this.logger.info(MessageFormat.format(Messages.getString("FailedLogoutMonitor.20"), failure.getEndPoint()) );  //$NON-NLS-1$
					this.logoutFailures.remove(failure);
				}
			}
		}
	}
	
	
	/*
	 * Send an Logout Request Request to the specified SPEP endpoint.
	 * 
	 * @param logoutRequest The LogoutRequest to send to the specified endpoint
	 * @param endPoint The endpoint to send the logout request to.
	 * @return The result of the operation. Either LogoutSuccessfull or FailedLogout
	 */
	private LogoutMechanism.result sendLogoutRequest(Element logoutRequest, String endpoint, boolean storeFailedLogout)
	{
		LogoutRequest request = null;
		
		this.logger.trace(Messages.getString("FailedLogoutMonitor.13")); //$NON-NLS-1$
			
		// unmarshall the original failure request to obtain information needed by logout mechanism
		try
		{
			request = this.logoutRequestUnmarshaller.unMarshallSigned(logoutRequest);
		}
		catch(UnmarshallerException e)
		{
			this.logger.error(Messages.getString("FailedLogoutMonitor.22"));  //$NON-NLS-1$
			this.logger.trace(e.getLocalizedMessage(), e);
		}
		catch(ReferenceValueException e)
		{
			this.logger.error(Messages.getString("FailedLogoutMonitor.23")); //$NON-NLS-1$
			this.logger.trace(e.getLocalizedMessage(), e);
		}
		catch(SignatureValueException e)
		{
			this.logger.error(Messages.getString("FailedLogoutMonitor.24")); //$NON-NLS-1$
			this.logger.trace(e.getLocalizedMessage(), e);
		}
				
		if(request != null)
		{
			String samlAuthnID = request.getNameID().getValue();
			List<String> sessionIDs = request.getSessionIndices();
					
			// Attempt to send logout request
			return this.logoutMechanism.performSingleLogout(samlAuthnID, sessionIDs, endpoint, storeFailedLogout);
		}
		else
		{
			this.logger.warn("Invalid LogoutRequest in failure repository. Unable to send.");
			return LogoutMechanism.result.LogoutRequestFailed;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoe.MonitorThread#shutdown()
	 */
	public void shutdown()
	{
		this.setRunning(false);
		
		this.interrupt();
	}
	
	/* Determine if the thread should continue running.
	 * 
	 */
	protected synchronized boolean isRunning()
	{
		return this.running;
	}
	
	/* Set whether the thread should continue running.
	 * 
	 */
	protected synchronized void setRunning(boolean running)
	{
		this.running = running;
	}

}

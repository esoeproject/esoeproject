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
package com.qut.middleware.esoe.sso.impl;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.Iterator;

import javax.xml.bind.JAXBElement;

import org.apache.log4j.Logger;

import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.MonitorThread;
import com.qut.middleware.esoe.crypto.KeyStoreResolver;
import com.qut.middleware.esoe.metadata.Metadata;
import com.qut.middleware.esoe.sso.SSOProcessor;
import com.qut.middleware.esoe.sso.SSOProcessor.result;
import com.qut.middleware.esoe.sso.bean.FailedLogout;
import com.qut.middleware.esoe.sso.bean.FailedLogoutRepository;
import com.qut.middleware.esoe.util.CalendarUtils;
import com.qut.middleware.esoe.ws.WSClient;
import com.qut.middleware.esoe.ws.exception.WSClientException;
import com.qut.middleware.saml2.exception.InvalidSAMLResponseException;
import com.qut.middleware.saml2.exception.MarshallerException;
import com.qut.middleware.saml2.exception.ReferenceValueException;
import com.qut.middleware.saml2.exception.SignatureValueException;
import com.qut.middleware.saml2.exception.UnmarshallerException;
import com.qut.middleware.saml2.handler.Marshaller;
import com.qut.middleware.saml2.handler.Unmarshaller;
import com.qut.middleware.saml2.handler.impl.MarshallerImpl;
import com.qut.middleware.saml2.handler.impl.UnmarshallerImpl;
import com.qut.middleware.saml2.identifier.IdentifierGenerator;
import com.qut.middleware.saml2.schemas.protocol.LogoutRequest;
import com.qut.middleware.saml2.schemas.protocol.Response;
import com.qut.middleware.saml2.schemas.protocol.StatusResponseType;
import com.qut.middleware.saml2.validator.SAMLValidator;

/** A thread used to monitor the cache of Failed LogoutRequests. Such failures occur when
 * an SPEP fails to respond in an acceptable way to a logout request from the ESOE. This thread
 * will continually poll the cache of failures at regular intervals attempting to resend them until
 * they are successfull, or they expire.
 */
public class FailedLogoutMonitor extends Thread implements MonitorThread
{
	private FailedLogoutRepository updateFailures;

	private volatile boolean running;
	
	private SAMLValidator samlValidator;
	private IdentifierGenerator identifierGenerator;
	
	private int retryInterval;
	private int maxFailureAge;
	private final WSClient wsClient;
	private Metadata metadata;

	private KeyStoreResolver keyStoreResolver;
	
	private Marshaller<LogoutRequest> logoutRequestMarshaller;
	private Unmarshaller<LogoutRequest> logoutRequestUnmarshaller;
	private Unmarshaller<JAXBElement<StatusResponseType>> logoutResponseUnmarshaller;
		
	private String[] schemas = new String[] { ConfigurationConstants.samlProtocol };
		
	private final String UNMAR_PKGNAMES = Response.class.getPackage().getName();
	private final String MAR_PKGNAMES = LogoutRequest.class.getPackage().getName();
	
	/* Local logging instance */
	private Logger logger = Logger.getLogger(FailedLogoutMonitor.class.getName());

	/**
	 * @param failureRep
	 *            The logout failure repository instance
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
	public FailedLogoutMonitor(FailedLogoutRepository failureRep, SAMLValidator samlValidator, Metadata metadata,
			KeyStoreResolver keyStoreResolver, IdentifierGenerator identifierGenerator, WSClient wsClient, int retryInterval, int maxFailureAge) throws UnmarshallerException, MarshallerException
	{
		this.wsClient = wsClient;

		if (failureRep == null)
			throw new IllegalArgumentException(Messages.getString("FailedLogoutMonitor.0"));  //$NON-NLS-1$

		if(samlValidator == null)
			throw new IllegalArgumentException(Messages.getString("FailedLogoutMonitor.14")); //$NON-NLS-1$
			
		if (metadata == null)
			throw new IllegalArgumentException(Messages.getString("FailedLogoutMonitor.1")); //$NON-NLS-1$

		if (keyStoreResolver == null)
			throw new IllegalArgumentException(Messages.getString("FailedLogoutMonitor.15")); //$NON-NLS-1$
		
		if (identifierGenerator == null)
			throw new IllegalArgumentException(Messages.getString("FailedLogoutMonitor.16")); //$NON-NLS-1$
		
		if (wsClient == null)
			throw new IllegalArgumentException(Messages.getString("FailedLogoutMonitor.2"));  //$NON-NLS-1$

		if (retryInterval <= 0)
			throw new IllegalArgumentException(Messages.getString("FailedLogoutMonitor.3"));  //$NON-NLS-1$

		if (maxFailureAge < 0 || (maxFailureAge > Integer.MAX_VALUE / 1000))
			throw new IllegalArgumentException(Messages.getString("FailedLogoutMonitor.4")); //$NON-NLS-1$
	
		this.metadata = metadata;
		this.updateFailures = failureRep;
		this.retryInterval = retryInterval * 1000 ;
		this.maxFailureAge = maxFailureAge * 1000 ;
		this.keyStoreResolver = keyStoreResolver;
		this.identifierGenerator = identifierGenerator;
		this.samlValidator = samlValidator;
		
		this.logoutResponseUnmarshaller = new UnmarshallerImpl<JAXBElement<StatusResponseType>>(this.UNMAR_PKGNAMES, new String[]{ConfigurationConstants.samlProtocol}, this.metadata);
		this.logoutRequestMarshaller = new MarshallerImpl<LogoutRequest>(this.MAR_PKGNAMES, this.schemas, this.keyStoreResolver.getKeyAlias(), this.keyStoreResolver.getPrivateKey());
		this.logoutRequestUnmarshaller = new UnmarshallerImpl<LogoutRequest>(this.UNMAR_PKGNAMES, this.schemas, this.metadata);
		
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
	 * Send an Logout Request Request to the specified SPEP endpoint.
	 * 
	 * @param logoutRequest The LogoutRequest to send to the specified endpoint
	 * @param endPoint The endpoint to send the logout request to.
	 * @return The result of the operation. Either LogoutSuccessfull or FailedLogout
	 */
	private result sendLogoutRequest(byte[] logoutRequest, String endPoint)
	{
		byte[] responseDocument;

		try
		{
			this.logger.trace(MessageFormat.format( Messages.getString("FailedLogoutMonitor.13"), logoutRequest) ); //$NON-NLS-1$
			
			// attempt to send logout request
			responseDocument = this.wsClient.singleLogout(logoutRequest, endPoint);
			
			// validate the response from the attempted target
			StatusResponseType logoutResponse = (this.logoutResponseUnmarshaller.unMarshallSigned(responseDocument)).getValue();

			this.samlValidator.getResponseValidator().validate(logoutResponse);
			
			// no need to do any further checking. If the response is not success it can only
			// mean the SPEP knows nothing about the principal, in which case there's no point
			// resending it. So we assume success if the SPEP sends a valid Response.
			return result.LogoutSuccessful;
		}
		catch (SignatureValueException e)
		{
			this.logger.error(Messages.getString("FailedLogoutMonitor.6"));  //$NON-NLS-1$
			this.logger.trace (e);

			return result.LogoutRequestFailed;
		}
		catch (ReferenceValueException e)
		{
			this.logger.error(Messages.getString("FailedLogoutMonitor.7"));  //$NON-NLS-1$
			this.logger.trace(e.getLocalizedMessage(), e);

			return result.LogoutRequestFailed;
		}
		catch (UnmarshallerException e)
		{
			this.logger.error(Messages.getString("FailedLogoutMonitor.8") + endPoint); //$NON-NLS-1$
			this.logger.trace(e.getLocalizedMessage(), e);

			return result.LogoutRequestFailed;
		}
		catch(InvalidSAMLResponseException e)
		{
			this.logger.error(Messages.getString("FailedLogoutMonitor.17")); //$NON-NLS-1$
			this.logger.trace(e.getLocalizedMessage(), e);			
		
			return result.LogoutRequestFailed;
		}
		catch (WSClientException e)
		{
			this.logger.error(Messages.getString("FailedLogoutMonitor.9") + endPoint);  //$NON-NLS-1$
			this.logger.trace(e.getLocalizedMessage(), e);

			return result.LogoutRequestFailed;
		}		
		catch (UnsupportedOperationException e)
		{
			this.logger.error(Messages.getString("FailedLogoutMonitor.9") + endPoint);  //$NON-NLS-1$
			this.logger.trace(e.getLocalizedMessage(), e);

			return result.LogoutRequestFailed;
		}		
	}

	
	/*
	 * Iterate through the list of failure objects in the repository and attempt to resend the SPEP notification. If a
	 * request cannot be sent, it remains in the repository, else it is removed. If a failure cannot be sent and it is
	 * older than this.maxFailureAge, it is also removed.
	 * 
	 */
	private void flushRepository()
	{		
		SSOProcessor.result result = SSOProcessor.result.LogoutRequestFailed;
		
		this.logger.debug(MessageFormat.format(Messages.getString("FailedLogoutMonitor.18"), this.updateFailures.getSize()) );  //$NON-NLS-1$
		
		Iterator<FailedLogout> iter = this.updateFailures.getFailures().iterator();

		// while failures in repository, send update request to associated SPEP and delete
		while (iter.hasNext())
		{
			FailedLogout failure = iter.next();

			// we have to regenerate the original request with updated timestamps and sigs before sending
			// because the allowed time skew will more than likely have expired.
			
			byte[] newDocument = this.regenerateDocument(failure.getRequestDocument());
			
			if(newDocument != null)
			{
				failure.setRequestDocument(newDocument);

				result = this.sendLogoutRequest(failure.getRequestDocument(), failure.getEndPoint());
			}
			else
			{
				// skip the send because it won't succeed
				this.logger.error(Messages.getString("FailedLogoutMonitor.19")); //$NON-NLS-1$
			}			
			
			if (result == SSOProcessor.result.LogoutSuccessful)
			{
				this.logger.info(MessageFormat.format(Messages.getString("FailedLogoutMonitor.11"),  failure.getEndPoint()) ); //$NON-NLS-1$
				this.updateFailures.remove(failure);
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
					this.updateFailures.remove(failure);
				}
			}
		}
	}
	
	
	/* Modifies the given string representation of the ClearAuthzCacheRequest with updated timestamps
	 * SAML ID's and signatures. If an error occurs diring the process, null is returned.
	 * 
	 */
	private byte[] regenerateDocument(byte[] oldDocument)
	{
		this.logger.debug(Messages.getString("FailedLogoutMonitor.21")); //$NON-NLS-1$
		
		byte[] newDoc = null;
		LogoutRequest request = null;
		
		// unmarshall the original failure request for modification
		try
		{
			request = this.logoutRequestUnmarshaller.unMarshallSigned(oldDocument);
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
			try
			{				
				// reset timestamp
				request.setIssueInstant(CalendarUtils.generateXMLCalendar());
				
				// reset request ID
				request.setID(this.identifierGenerator.generateSAMLID());
				
				// re marshall mofified document
				newDoc = this.logoutRequestMarshaller.marshallSigned(request);		
			}
			catch(MarshallerException e)
			{
				this.logger.error(Messages.getString("FailedLogoutMonitor.25")); //$NON-NLS-1$
				this.logger.trace(e.getLocalizedMessage(), e);
			}
		}
		
		try
		{
			this.logger.trace("Regenerated new LogoutRequest: \n" + new String(newDoc,  "UTF-16") );
		}
		catch(UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		
		return newDoc;
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

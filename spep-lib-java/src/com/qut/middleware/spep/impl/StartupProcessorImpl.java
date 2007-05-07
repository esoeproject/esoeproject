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
 * Author: Shaun Mangelsdorf
 * Creation Date: 18/12/2006
 * 
 * Purpose: Implements the StartupProcessor interface
 */
package com.qut.middleware.spep.impl;

import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.w3._2000._09.xmldsig_.Signature;

import com.qut.middleware.saml2.StatusCodeConstants;
import com.qut.middleware.saml2.VersionConstants;
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
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.esoe.protocol.ValidateInitializationRequest;
import com.qut.middleware.saml2.schemas.esoe.protocol.ValidateInitializationResponse;
import com.qut.middleware.saml2.validator.SAMLValidator;
import com.qut.middleware.spep.ConfigurationConstants;
import com.qut.middleware.spep.Messages;
import com.qut.middleware.spep.StartupProcessor;
import com.qut.middleware.spep.exception.SPEPInitializationException;
import com.qut.middleware.spep.metadata.KeyStoreResolver;
import com.qut.middleware.spep.metadata.Metadata;
import com.qut.middleware.spep.util.CalendarUtils;
import com.qut.middleware.spep.ws.WSClient;
import com.qut.middleware.spep.ws.exception.WSClientException;

/** */
public class StartupProcessorImpl implements StartupProcessor
{
	private result startupResult;
	private String spepIdentifier;
	private IdentifierGenerator identifierGenerator;
	private String compileSystem;
	private String compileDate;
	private String swVersion;
	private String environment;
	private int nodeID;
	private int startupRetryInterval = 20000;
	private List<String> ipAddressList;
	private WSClient wsClient;
	private Marshaller<ValidateInitializationRequest> validateInitializationRequestMarshaller;
	private Unmarshaller<ValidateInitializationResponse> validateInitializationResponseUnmarshaller;
	private SAMLValidator samlValidator;
	private final Metadata metadata;
	
	private final String UNMAR_PKGNAMES = ValidateInitializationRequest.class.getPackage().getName();
	private final String MAR_PKGNAMES = ValidateInitializationRequest.class.getPackage().getName();
	
	/* Local logging instance */
	private Logger logger = Logger.getLogger(StartupProcessorImpl.class.getName());
	
	/**
	 * constructor
	 * 
	 * @param metadata 
	 * @param spepIdentifier 
	 * @param identifierGenerator 
	 * @param wsClient 
	 * @param samlValidator 
	 * @param keyStoreResolver 
	 * @param ipAddressList 
	 * @param serverInfo 
	 * @param nodeID The node indentifier of this SPEP, used to btaon index locations.
	 * @throws MarshallerException 
	 * @throws UnmarshallerException 
	 */
	public StartupProcessorImpl(Metadata metadata, String spepIdentifier, IdentifierGenerator identifierGenerator, WSClient wsClient, SAMLValidator samlValidator, KeyStoreResolver keyStoreResolver, List<String> ipAddressList, String serverInfo, int nodeID) throws MarshallerException, UnmarshallerException
	{	
		if(metadata == null)
		{
			throw new IllegalArgumentException(Messages.getString("StartupProcessorImpl.19"));  //$NON-NLS-1$
		}
		if(spepIdentifier == null)
		{
			throw new IllegalArgumentException(Messages.getString("StartupProcessorImpl.20"));  //$NON-NLS-1$
		}
		if(identifierGenerator == null)
		{
			throw new IllegalArgumentException(Messages.getString("StartupProcessorImpl.21"));  //$NON-NLS-1$
		}
		if(wsClient == null)
		{
			throw new IllegalArgumentException(Messages.getString("StartupProcessorImpl.22"));  //$NON-NLS-1$
		}
		if(samlValidator == null)
		{
			throw new IllegalArgumentException(Messages.getString("StartupProcessorImpl.23"));  //$NON-NLS-1$
		}
		if(keyStoreResolver == null)
		{
			throw new IllegalArgumentException(Messages.getString("StartupProcessorImpl.24"));  //$NON-NLS-1$
		}
		if(ipAddressList == null)
		{
			throw new IllegalArgumentException(Messages.getString("StartupProcessorImpl.25"));  //$NON-NLS-1$
		}
		if(serverInfo == null)
		{
			throw new IllegalArgumentException(Messages.getString("StartupProcessorImpl.26"));  //$NON-NLS-1$
		}
		if(nodeID < 0 || nodeID > Integer.MAX_VALUE)
			throw new IllegalArgumentException(Messages.getString("StartupProcessorImpl.34") + Integer.MAX_VALUE); //$NON-NLS-1$
		
		this.metadata = metadata;
		this.spepIdentifier = spepIdentifier;
		this.identifierGenerator = identifierGenerator;
		this.samlValidator = samlValidator;
		this.wsClient = wsClient;
		this.nodeID = nodeID;
		
		this.startupResult = result.wait;
		
		ResourceBundle bundle = ResourceBundle.getBundle(ConfigurationConstants.SPEP_COMPILE_TIME);
		
		this.compileDate = bundle.getString("spep.compileTime.compileDate"); //$NON-NLS-1$
		this.compileSystem = bundle.getString("spep.compileTime.compileSystem"); //$NON-NLS-1$
		this.swVersion = bundle.getString("spep.compileTime.swVersion"); //$NON-NLS-1$
		String osName = System.getProperty("os.name"); //$NON-NLS-1$
		String osVersion = System.getProperty("os.version"); //$NON-NLS-1$
		String osArch = System.getProperty("os.arch"); //$NON-NLS-1$
		String javaRuntimeName = System.getProperty("java.runtime.name"); //$NON-NLS-1$
		String javaRuntimeVersion = System.getProperty("java.runtime.version"); //$NON-NLS-1$
		String javaVendor = System.getProperty("java.vendor"); //$NON-NLS-1$
		this.environment = MessageFormat.format("{0} {1} {2} - {3} {4} {5} - {6}", new Object[]{osName, osVersion, osArch, javaRuntimeName, javaRuntimeVersion, javaVendor, serverInfo}); //$NON-NLS-1$
		
		this.ipAddressList = new Vector<String>();
		this.ipAddressList.addAll(ipAddressList);
		
		String[] validateInitializationSchemas = new String[]{ConfigurationConstants.esoeProtocol};
		this.validateInitializationRequestMarshaller = new MarshallerImpl<ValidateInitializationRequest>(this.MAR_PKGNAMES, validateInitializationSchemas, keyStoreResolver.getKeyAlias(), keyStoreResolver.getPrivateKey());
		this.validateInitializationResponseUnmarshaller = new UnmarshallerImpl<ValidateInitializationResponse>(this.UNMAR_PKGNAMES, validateInitializationSchemas, this.metadata);
		
		this.logger.info(Messages.getString("StartupProcessorImpl.0")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.spep.StartupProcessor#allowProcessing()
	 */
	public synchronized result allowProcessing()
	{
		return this.startupResult;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.spep.StartupProcessor#beginSPEPStartup()
	 */
	public synchronized void beginSPEPStartup()
	{
		this.logger.debug(Messages.getString("StartupProcessorImpl.1")); //$NON-NLS-1$
		
		Thread thread = new StartupProcessorThread();
		thread.start();
	}
	
	/*
	 * 
	 */
	private synchronized void setStartupResult(result startupResult)
	{
		this.startupResult = startupResult;
	}
	
	/* Send an SPEP startup message to the ESOE and request startup. If the request is accepted the
	 * SPEP will begin processing, if not it will wait until it has clearance to start from the
	 * ESOE.
	 * 
	 */
	protected void doStartup()
	{
		this.logger.info(Messages.getString("StartupProcessorImpl.2")); //$NON-NLS-1$

		setStartupResult(result.wait);
		
		while (!result.allow.equals(allowProcessing()))
		{
			try
			{
				String samlID = this.identifierGenerator.generateSAMLID();
				
				String requestDocument = buildRequest(samlID);
				String endpoint = this.metadata.getSPEPStartupServiceEndpoint();
				
				this.logger.debug(MessageFormat.format(Messages.getString("StartupProcessorImpl.3"), endpoint) ); //$NON-NLS-1$
				
				String responseDocument = this.wsClient.spepStartup(requestDocument, endpoint);
				
				this.logger.debug(Messages.getString("StartupProcessorImpl.4")); //$NON-NLS-1$

				processResponse(responseDocument, samlID);
				
				setStartupResult(result.allow);
				
				break;
			}
			catch (WSClientException e)
			{
				setStartupResult(result.fail);
				this.logger.error(MessageFormat.format(Messages.getString("StartupProcessorImpl.5"), e.getMessage())); //$NON-NLS-1$
			}
			catch (MarshallerException e)
			{
				setStartupResult(result.fail);
				this.logger.error(MessageFormat.format(Messages.getString("StartupProcessorImpl.6"), e.getMessage())); //$NON-NLS-1$
			}
			catch (SignatureValueException e)
			{
				setStartupResult(result.fail);
				this.logger.error(MessageFormat.format(Messages.getString("StartupProcessorImpl.7"), e.getMessage())); //$NON-NLS-1$
			}
			catch (ReferenceValueException e)
			{
				setStartupResult(result.fail);
				this.logger.error(MessageFormat.format(Messages.getString("StartupProcessorImpl.8"), e.getMessage())); //$NON-NLS-1$
			}
			catch (UnmarshallerException e)
			{
				setStartupResult(result.fail);
				this.logger.error(MessageFormat.format(Messages.getString("StartupProcessorImpl.9"), e.getMessage())); //$NON-NLS-1$
			}
			catch (SPEPInitializationException e)
			{
				setStartupResult(result.fail);
				this.logger.error(MessageFormat.format(Messages.getString("StartupProcessorImpl.10"), e.getMessage())); //$NON-NLS-1$
			}
			catch (Exception e)
			{
				e.printStackTrace();
				setStartupResult(result.fail);
				this.logger.fatal(MessageFormat.format(Messages.getString("StartupProcessorImpl.11"), e.getMessage())); //$NON-NLS-1$
			}
			
			try
			{
				Thread.sleep(this.startupRetryInterval);
				this.logger.fatal(MessageFormat.format(Messages.getString("StartupProcessorImpl.30"), this.startupRetryInterval/1000) ); //$NON-NLS-1$
			}
			catch (InterruptedException e)
			{
				// Ignore
			}
		}
		
		this.logger.debug(Messages.getString("StartupProcessorImpl.33")); //$NON-NLS-1$
		
	}
	
	/* Builds a string representation of ValidateInitializationRequest using the given SAMLID.
	 * 
	 */
	private String buildRequest(String samlID) throws MarshallerException
	{
		NameIDType issuer = new NameIDType();
		issuer.setValue(this.spepIdentifier);
		
		ValidateInitializationRequest validateInitializationRequest = new ValidateInitializationRequest();
		validateInitializationRequest.setID(samlID);
		validateInitializationRequest.setVersion(VersionConstants.saml20);
		validateInitializationRequest.setIssueInstant(CalendarUtils.generateXMLCalendar());
		validateInitializationRequest.setIssuer(issuer);
		validateInitializationRequest.setSignature(new Signature());
		validateInitializationRequest.setAuthzCacheIndex(this.nodeID);

		validateInitializationRequest.setCompileDate(this.compileDate);
		validateInitializationRequest.setCompileSystem(this.compileSystem);
		validateInitializationRequest.setSwVersion(this.swVersion);
		validateInitializationRequest.setEnvironment(this.environment);
		validateInitializationRequest.getIpAddress().addAll(this.ipAddressList);
		validateInitializationRequest.setNodeId(new Integer(this.nodeID).toString()); 
		
		this.logger.debug(MessageFormat.format(Messages.getString("StartupProcessorImpl.12"), samlID)); //$NON-NLS-1$
		
		return this.validateInitializationRequestMarshaller.marshallSigned(validateInitializationRequest);
	}
	
	/* Process the string representation of the given ValidateInitliazationResponse object.
	 * 
	 */
	private void processResponse(String responseDocument, String expectedSAMLID) throws SignatureValueException, ReferenceValueException, UnmarshallerException, SPEPInitializationException
	{
		ValidateInitializationResponse validateInitializationResponse = this.validateInitializationResponseUnmarshaller.unMarshallSigned(responseDocument);

		this.logger.debug(MessageFormat.format(Messages.getString("StartupProcessorImpl.13"), validateInitializationResponse.getID())); //$NON-NLS-1$
		
		// validate the response standard fields
		try
		{
			this.samlValidator.getResponseValidator().validate(validateInitializationResponse);
		}
		catch (InvalidSAMLResponseException e)
		{
			this.logger.error(MessageFormat.format(Messages.getString("StartupProcessorImpl.27"), validateInitializationResponse.getID(), expectedSAMLID, e.getMessage())); //$NON-NLS-1$
			
			throw new SPEPInitializationException(Messages.getString("StartupProcessorImpl.28")); //$NON-NLS-1$
		}

		// check that issuer was ESOE
		if(!this.metadata.getESOEIdentifier().equals(validateInitializationResponse.getIssuer().getValue()))
		{
			this.logger.error(MessageFormat.format(Messages.getString("StartupProcessorImpl.31"), this.metadata.getESOEIdentifier(), validateInitializationResponse.getIssuer().getValue())); //$NON-NLS-1$

			throw new SPEPInitializationException(MessageFormat.format(Messages.getString("StartupProcessorImpl.32"), this.metadata.getESOEIdentifier(), validateInitializationResponse.getIssuer().getValue())); //$NON-NLS-1$
		
		}
				
		// check status code == success
		if (!validateInitializationResponse.getStatus().getStatusCode().getValue().equals(StatusCodeConstants.success))
		{
			this.logger.error(MessageFormat.format(Messages.getString("StartupProcessorImpl.14"), validateInitializationResponse.getStatus().getStatusCode().getValue(), validateInitializationResponse.getStatus().getStatusMessage())); //$NON-NLS-1$

			throw new SPEPInitializationException(Messages.getString("StartupProcessorImpl.15")); //$NON-NLS-1$
		}
			
		this.logger.debug(Messages.getString("StartupProcessorImpl.17")); //$NON-NLS-1$
		return;
	}

	
	private class StartupProcessorThread extends Thread
	{
		protected StartupProcessorThread() 
		{
			super("SPEP Startup Processor Thread"); //$NON-NLS-1$
		}
		
		@Override
		public void run()
		{
			StartupProcessorImpl.this.doStartup();
		}
	}
}

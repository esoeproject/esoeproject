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
 * Author: Bradley Beddoes
 * Creation Date: 04/12/2006
 * 
 * Purpose: Implements the Web Services Processor interface
 */
package com.qut.middleware.esoe.ws.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.qut.middleware.esoe.aa.AttributeAuthorityProcessor;
import com.qut.middleware.esoe.aa.bean.AAProcessorData;
import com.qut.middleware.esoe.aa.bean.impl.AAProcessorDataImpl;
import com.qut.middleware.esoe.aa.exception.InvalidPrincipalException;
import com.qut.middleware.esoe.aa.exception.InvalidRequestException;
import com.qut.middleware.esoe.authz.AuthorizationProcessor;
import com.qut.middleware.esoe.authz.bean.AuthorizationProcessorData;
import com.qut.middleware.esoe.authz.bean.impl.AuthorizationProcessorDataImpl;
import com.qut.middleware.esoe.delegauthn.DelegatedAuthenticationProcessor;
import com.qut.middleware.esoe.delegauthn.bean.DelegatedAuthenticationData;
import com.qut.middleware.esoe.delegauthn.bean.impl.DelegatedAuthenticationDataImpl;
import com.qut.middleware.esoe.spep.SPEPProcessor;
import com.qut.middleware.esoe.spep.bean.SPEPProcessorData;
import com.qut.middleware.esoe.spep.bean.impl.SPEPProcessorDataImpl;
import com.qut.middleware.esoe.spep.exception.DatabaseFailureException;
import com.qut.middleware.esoe.spep.exception.DatabaseFailureNoSuchSPEPException;
import com.qut.middleware.esoe.spep.exception.SPEPCacheUpdateException;
import com.qut.middleware.esoe.ws.WSProcessor;
import com.qut.middleware.esoe.ws.exception.WSProcessorException;
import com.qut.middleware.saml2.exception.SOAPException;
import com.qut.middleware.saml2.handler.SOAPHandler;

public class WSProcessorImpl implements WSProcessor
{
	private AttributeAuthorityProcessor attributeAuthorityProcessor;
	private SPEPProcessor spepProcessor;
	private AuthorizationProcessor authorizationProcessor;
	private DelegatedAuthenticationProcessor delegAuthnProcessor;

	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(WSProcessorImpl.class.getName());
	private List<SOAPHandler> soapHandlers;

	/**
	 * Constructor
	 * 
	 * @param attributeAuthorityProcessor
	 *            Created instance of this processor
	 * @param authorizationProcessor
	 *            Created instance of this processor
	 * @param spepProcessor
	 *            Created instance of this processor
	 */
	public WSProcessorImpl(AttributeAuthorityProcessor attributeAuthorityProcessor,
			AuthorizationProcessor authorizationProcessor, SPEPProcessor spepProcessor,
			DelegatedAuthenticationProcessor delegAuthnProcessor, List<SOAPHandler> soapHandlers)
	{
		if (attributeAuthorityProcessor == null)
		{
			this.logger.error(Messages.getString("WSProcessorImpl.36")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("WSProcessorImpl.37")); //$NON-NLS-1$
		}
		if (authorizationProcessor == null)
		{
			this.logger.error(Messages.getString("WSProcessorImpl.38")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("WSProcessorImpl.39")); //$NON-NLS-1$
		}
		if (spepProcessor == null)
		{
			this.logger.error(Messages.getString("WSProcessorImpl.40")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("WSProcessorImpl.41")); //$NON-NLS-1$
		}
		if (delegAuthnProcessor == null)
		{
			this.logger.error(Messages.getString("WSProcessorImpl.42")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("WSProcessorImpl.43")); //$NON-NLS-1$
		}
		if (soapHandlers == null || soapHandlers.size() == 0)
		{
			this.logger.error("No SOAP handlers provided. Cannot create the web service processor without any SOAP handlers.");
			throw new IllegalArgumentException("No SOAP handlers provided. Cannot create the web service processor without any SOAP handlers.");
		}

		this.attributeAuthorityProcessor = attributeAuthorityProcessor;
		this.spepProcessor = spepProcessor;
		this.authorizationProcessor = authorizationProcessor;
		this.delegAuthnProcessor = delegAuthnProcessor;
		this.soapHandlers = soapHandlers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.ws.WSProcessor#attributeAuthority(org.apache.axiom.om.OMElement)
	 */
	public byte[] attributeAuthority(byte[] attributeQuery, String contentType) throws WSProcessorException
	{
		AAProcessorData data = new AAProcessorDataImpl();

		this.logger.debug(Messages.getString("WSProcessorImpl.3")); //$NON-NLS-1$

		SOAPHandler handler = this.getHandler(contentType);
		this.logger.debug("Got SOAP handler of class {} for content type {}", handler.getClass(), contentType);
		try
		{
			Element request = readRequest(attributeQuery, handler);
			
			data.setRequestDocument(request);
			this.attributeAuthorityProcessor.execute(data);

			if (data.getResponseDocument() != null)
			{
				this.logger.debug(Messages.getString("WSProcessorImpl.4")); //$NON-NLS-1$
				String responseEncoding = this.getEncoding(handler, attributeQuery);
				return generateResponse(data.getResponseDocument(), handler, responseEncoding);
			}

			this.logger.warn(Messages.getString("WSProcessorImpl.5")); //$NON-NLS-1$
			throw new WSProcessorException(Messages.getString("WSProcessorImpl.52") + Messages.getString("WSProcessorImpl.0")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch (InvalidPrincipalException e)
		{
			this.logger.warn(Messages.getString("WSProcessorImpl.6")); //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);
			if (data.getResponseDocument() != null)
			{
				this.logger.debug(Messages.getString("WSProcessorImpl.7")); //$NON-NLS-1$
				String responseEncoding = this.getEncoding(handler, attributeQuery);
				return generateResponse(data.getResponseDocument(), handler, responseEncoding);
			}

			this.logger.warn(Messages.getString("WSProcessorImpl.8")); //$NON-NLS-1$
			throw new WSProcessorException(Messages.getString("WSProcessorImpl.52") + e.getMessage(), e); //$NON-NLS-1$
		}
		catch (InvalidRequestException e)
		{
			this.logger.warn(Messages.getString("WSProcessorImpl.9")); //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);
			if (data.getResponseDocument() != null)
			{
				this.logger.debug(Messages.getString("WSProcessorImpl.10")); //$NON-NLS-1$
				String responseEncoding = this.getEncoding(handler, attributeQuery);
				return generateResponse(data.getResponseDocument(), handler, responseEncoding);
			}

			this.logger.warn(Messages.getString("WSProcessorImpl.11")); //$NON-NLS-1$
			throw new WSProcessorException(Messages.getString("WSProcessorImpl.52") + e.getMessage(), e); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.ws.WSProcessor#policyDecisionPoint(org.apache.axiom.om.OMElement)
	 */
	public byte[] policyDecisionPoint(byte[] decisionRequest, String contentType) throws WSProcessorException
	{
		AuthorizationProcessorData data = new AuthorizationProcessorDataImpl();

		this.logger.debug(Messages.getString("WSProcessorImpl.12")); //$NON-NLS-1$
		SOAPHandler handler = this.getHandler(contentType);
		this.logger.debug("Got SOAP handler of class {} for content type {}", handler.getClass(), contentType);
		try
		{
			Element request = readRequest(decisionRequest, handler);
			data.setRequestDocument(request);
			this.authorizationProcessor.execute(data);

			if (data.getResponseDocument() != null)
			{
				this.logger.debug(Messages.getString("WSProcessorImpl.13")); //$NON-NLS-1$
				String responseEncoding = this.getEncoding(handler, decisionRequest);
				return generateResponse(data.getResponseDocument(), handler, responseEncoding);
			}

			this.logger.warn(Messages.getString("WSProcessorImpl.14")); //$NON-NLS-1$
			throw new WSProcessorException(Messages.getString("WSProcessorImpl.1")); //$NON-NLS-1$
		}
		catch (com.qut.middleware.esoe.authz.exception.InvalidRequestException e)
		{
			this.logger.warn(Messages.getString("WSProcessorImpl.15")); //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);
			if (data.getResponseDocument() != null)
			{
				this.logger.debug(Messages.getString("WSProcessorImpl.16")); //$NON-NLS-1$
				String responseEncoding = this.getEncoding(handler, decisionRequest);
				return generateResponse(data.getResponseDocument(), handler, responseEncoding);
			}

			this.logger.warn(Messages.getString("WSProcessorImpl.17")); //$NON-NLS-1$
			throw new WSProcessorException(Messages.getString("WSProcessorImpl.52") + e.getMessage()); //$NON-NLS-1$
		}
	
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.ws.WSProcessor#spepStartup(org.apache.axiom.om.OMElement)
	 */
	public byte[] spepStartup(byte[] spepStartup, String contentType) throws WSProcessorException
	{

		SPEPProcessorData data = new SPEPProcessorDataImpl();

		this.logger.debug(Messages.getString("WSProcessorImpl.18")); //$NON-NLS-1$
		SOAPHandler handler = this.getHandler(contentType);
		this.logger.debug("Got SOAP handler of class {} for content type {}", handler.getClass(), contentType);
		try
		{
			Element request = readRequest(spepStartup, handler);
			data.setRequestDocument(request);

			this.spepProcessor.getStartup().registerSPEPStartup(data);

			if (data.getResponseDocument() != null)
			{
				this.logger.debug(Messages.getString("WSProcessorImpl.19")); //$NON-NLS-1$
				String responseEncoding = this.getEncoding(handler, spepStartup);
				return generateResponse(data.getResponseDocument(), handler, responseEncoding);
			}

			this.logger.warn(Messages.getString("WSProcessorImpl.20")); //$NON-NLS-1$
			throw new WSProcessorException(Messages.getString("WSProcessorImpl.2")); //$NON-NLS-1$
		}
		catch (com.qut.middleware.esoe.spep.exception.InvalidRequestException e)
		{
			this.logger.warn(Messages.getString("WSProcessorImpl.21")); //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);
			if (data.getResponseDocument() != null)
			{
				this.logger.debug(Messages.getString("WSProcessorImpl.22")); //$NON-NLS-1$
				String responseEncoding = this.getEncoding(handler, spepStartup);
				return generateResponse(data.getResponseDocument(), handler, responseEncoding);
			}

			this.logger.warn(Messages.getString("WSProcessorImpl.23")); //$NON-NLS-1$
			throw new WSProcessorException(Messages.getString("WSProcessorImpl.52") + e.getMessage()); //$NON-NLS-1$
		}
		catch (DatabaseFailureNoSuchSPEPException e)
		{
			this.logger.warn(Messages.getString("WSProcessorImpl.24")); //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);
			if (data.getResponseDocument() != null)
			{
				this.logger.debug(Messages.getString("WSProcessorImpl.25")); //$NON-NLS-1$
				String responseEncoding = this.getEncoding(handler, spepStartup);
				return generateResponse(data.getResponseDocument(), handler, responseEncoding);
			}

			this.logger.warn(Messages.getString("WSProcessorImpl.26")); //$NON-NLS-1$
			throw new WSProcessorException(Messages.getString("WSProcessorImpl.52") + e.getMessage()); //$NON-NLS-1$
		}
		catch (SPEPCacheUpdateException e)
		{
			this.logger.warn(Messages.getString("WSProcessorImpl.27")); //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);
			if (data.getResponseDocument() != null)
			{
				this.logger.debug(Messages.getString("WSProcessorImpl.28")); //$NON-NLS-1$
				String responseEncoding = this.getEncoding(handler, spepStartup);
				return generateResponse(data.getResponseDocument(), handler, responseEncoding);
			}

			throw new WSProcessorException(Messages.getString("WSProcessorImpl.52") + e.getMessage()); //$NON-NLS-1$
		}
		catch (DatabaseFailureException e)
		{
			this.logger.warn(Messages.getString("WSProcessorImpl.29")); //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);

			if (data.getResponseDocument() != null)
			{
				this.logger.debug(Messages.getString("WSProcessorImpl.30")); //$NON-NLS-1$
				String responseEncoding = this.getEncoding(handler, spepStartup);
				return generateResponse(data.getResponseDocument(), handler, responseEncoding);
			}

			this.logger.warn(Messages.getString("WSProcessorImpl.31")); //$NON-NLS-1$
			throw new WSProcessorException(Messages.getString("WSProcessorImpl.52") + e.getMessage()); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.ws.WSProcessor#registerPrincipal(org.apache.axiom.om.OMElement)
	 */
	public byte[] registerPrincipal(byte[] registerPrincipal, String contentType) throws WSProcessorException
	{
		SOAPHandler handler = this.getHandler(contentType);
		this.logger.debug("Got SOAP handler of class {} for content type {}", handler.getClass(), contentType);

		DelegatedAuthenticationData data = new DelegatedAuthenticationDataImpl();
		Element delegAuthnRequest;

		this.logger.debug(Messages.getString("WSProcessorImpl.47")); //$NON-NLS-1$

		delegAuthnRequest = readRequest(registerPrincipal, handler);
		this.logger.debug(Messages.getString("WSProcessorImpl.48") + delegAuthnRequest); //$NON-NLS-1$

		data.setRequestDocument(delegAuthnRequest);
		this.delegAuthnProcessor.execute(data);

		if (data.getResponseDocument() != null)
		{
			this.logger.debug(Messages.getString("WSProcessorImpl.49")); //$NON-NLS-1$
			
			String responseEncoding = this.getEncoding(handler, registerPrincipal);
			return generateResponse(data.getResponseDocument(), handler, responseEncoding);
		}

		this.logger.warn(Messages.getString("WSProcessorImpl.50")); //$NON-NLS-1$
		throw new WSProcessorException(Messages.getString("WSProcessorImpl.51")); //$NON-NLS-1$
	}
	
	private SOAPHandler getHandler(String contentType) throws WSProcessorException
	{
		for (SOAPHandler handler : this.soapHandlers)
		{
			if (handler.canHandle(contentType))
				return handler;
		}
		
		throw new WSProcessorException("No registered SOAPHandler available to handle Content-Type: " + contentType);
	}
	
	private String getEncoding(SOAPHandler handler, byte[] document)
	{
		CharsetDetector detector = new CharsetDetector();
		detector.setText(document);
		CharsetMatch match = detector.detect();
		
		if (match != null)
		{
			return match.getName();
		}
		else
		{
			return handler.getDefaultEncoding();
		}
	}

	/**
	 * Reads Axis2 web requests and gets the raw Soap body as a String
	 * 
	 * @param requestDocument Raw SOAP document containing the request.
	 * @return DOM Element representing the request document.
	 */
	private Element readRequest(byte[] requestDocument, SOAPHandler handler) throws WSProcessorException
	{
		try
		{
			CharsetDetector detector = new CharsetDetector();
			this.logger.trace(detector.getString(requestDocument, null));
			
			return handler.unwrapDocument(requestDocument);
		}
		catch (SOAPException e)
		{
			this.logger.debug("SOAP Exception while trying to unwrap request document.", e);
			throw new WSProcessorException("SOAP Exception while trying to unwrap request document.");
		}
	}

	/**
	 * Generates an Axis 2 response object
	 * 
	 * @param responseDocument DOM Element representing the SAML document to respond with.
	 * @return Raw SOAP document containing the response.
	 */
	private byte[] generateResponse(Element responseDocument, SOAPHandler handler, String encoding) throws WSProcessorException
	{
		try
		{
			byte[] document = handler.wrapDocument(responseDocument, encoding);
			
			CharsetDetector detector = new CharsetDetector();
			this.logger.trace(detector.getString(document, null));
			
			return document;
		}
		catch (SOAPException e)
		{
			this.logger.debug("SOAP Exception while trying to wrap response document.", e);
			throw new WSProcessorException("SOAP Exception while trying to wrap response document.");
		}
	}
}

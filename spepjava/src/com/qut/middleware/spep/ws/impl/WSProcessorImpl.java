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
 * Creation Date: 15/12/2006
 * 
 * Purpose: Implements the WSProcessor interface.
 */
package com.qut.middleware.spep.ws.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.qut.middleware.saml2.exception.MarshallerException;
import com.qut.middleware.saml2.exception.SOAPException;
import com.qut.middleware.saml2.handler.SOAPHandler;
import com.qut.middleware.spep.authn.AuthnProcessor;
import com.qut.middleware.spep.authn.AuthnProcessorData;
import com.qut.middleware.spep.authn.impl.AuthnProcessorDataImpl;
import com.qut.middleware.spep.exception.LogoutException;
import com.qut.middleware.spep.pep.PolicyEnforcementProcessor;
import com.qut.middleware.spep.ws.WSProcessor;
import com.qut.middleware.spep.ws.exception.WSProcessorException;

/** */
public class WSProcessorImpl implements WSProcessor
{
	private PolicyEnforcementProcessor policyEnforcementProcessor;
	private AuthnProcessor authnProcessor;
	
	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(WSProcessorImpl.class.getName());
	private List<SOAPHandler> soapHandlers;

	/**
	 * Default constructor
	 */
	public WSProcessorImpl(PolicyEnforcementProcessor policyEnforcementProcessor, AuthnProcessor authnProcessor, List<SOAPHandler> soapHandlers)
	{
		this.policyEnforcementProcessor = policyEnforcementProcessor;
		this.authnProcessor = authnProcessor;
		this.soapHandlers = soapHandlers;
		
		this.logger.debug("Initialized WSProcessorImpl.");
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.spep.ws.WSProcessor#authzCacheClear(org.apache.axiom.om.OMElement)
	 */
	public byte[] authzCacheClear(byte[] authzCacheClear, String contentType) throws WSProcessorException
	{
		try
		{
			SOAPHandler handler = this.getHandler(contentType);
			
			Element request = readRequest(authzCacheClear, handler);
			
			Element response = this.policyEnforcementProcessor.authzCacheClear(request);
			if (response != null)
			{
				this.logger.debug("Authz cache clear processed. Responding to ESOE with response document");
				return generateResponse(response, handler);
			}

			this.logger.warn("Authz cache clear resulted in null response document.");
			throw new WSProcessorException("Authz cache clear resulted in null response document.");
		}
		catch (MarshallerException e)
		{
			this.logger.error("Marshaller exception occurred while processing authz cache clear request. Message was: " + e.getMessage());
			throw new WSProcessorException("Marshaller exception occurred while processing authz cache clear request. Fail.");
		}
	}
	
	/* (non-Javadoc)
	 * @see com.qut.middleware.spep.ws.WSProcessor#singleLogout(org.apache.axiom.om.OMElement)
	 */
	public byte[] singleLogout(byte[] logoutRequest, String contentType) throws WSProcessorException
	{
		try
		{
			SOAPHandler handler = this.getHandler(contentType);
			
			Element request = readRequest(logoutRequest, handler);
			
			Element response = this.authnProcessor.logoutPrincipal(request);
			if (response != null)
			{
				this.logger.debug("Authz cache clear processed. Responding to ESOE with response document");
				return generateResponse(response, handler);
			}

			this.logger.warn("Authz cache clear resulted in null response document.");
			throw new WSProcessorException("Authz cache clear resulted in null response document.");
		}
		catch (LogoutException e)
		{
			this.logger.error("Exception occurred while processing logout request. Message was: " + e.getMessage());
			throw new WSProcessorException("Exception occurred while processing logout request.");
		}
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
	private byte[] generateResponse(Element responseDocument, SOAPHandler handler) throws WSProcessorException
	{
		try
		{
			byte[] document = handler.wrapDocument(responseDocument);
			
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

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
 * Creation Date: 14/12/2006
 * 
 * Purpose: Implements the WSClient interface.
 */
package com.qut.middleware.spep.ws.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.ibm.icu.text.CharsetDetector;
import com.qut.middleware.saml2.exception.SOAPException;
import com.qut.middleware.saml2.handler.SOAPHandler;
import com.qut.middleware.spep.ws.Messages;
import com.qut.middleware.spep.ws.WSClient;
import com.qut.middleware.spep.ws.exception.WSClientException;

/** */
public class WSClientImpl implements WSClient
{
	private static final String CONTENT_TYPE = "Content-Type";
	// SOAP Action is specified for backward compatibility with Axis-based SPEP web services.
	private static final String SOAP_ACTION = "SOAPAction";
	private static final int BUF_SIZE = 1024;
	private SOAPHandler soapHandler;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * Constructor
	 */
	public WSClientImpl(SOAPHandler soapHandler)
	{
		this.soapHandler = soapHandler;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.spep.ws.WSClient#attributeAuthority(java.lang.String, java.lang.String)
	 */
	public Element attributeAuthority(Element attributeQuery, String endpoint) throws WSClientException
	{
		final String action = "attributeAuthority";

		if (endpoint == null || endpoint.length() <= 0)
		{
			this.logger.error(Messages.getString("WSClientImpl.8")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("WSClientImpl.1")); //$NON-NLS-1$
		}
		
		if (attributeQuery == null)
		{
			this.logger.error(MessageFormat.format(Messages.getString("WSClientImpl.9"), endpoint)); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("WSClientImpl.0")); //$NON-NLS-1$
		}
		
		this.logger.debug(MessageFormat.format(Messages.getString("WSClientImpl.10"), endpoint)); //$NON-NLS-1$
		
		return invokeWSCall(attributeQuery, endpoint, action);
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.spep.ws.WSClient#policyDecisionPoint(java.lang.String, java.lang.String)
	 */
	public Element policyDecisionPoint(Element decisionRequest, String endpoint) throws WSClientException
	{
		final String action = "policyDecisionPoint";

		if (endpoint == null || endpoint.length() <= 0)
		{
			this.logger.error(Messages.getString("WSClientImpl.11")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("WSClientImpl.3")); //$NON-NLS-1$
		}
		
		if (decisionRequest == null)
		{
			this.logger.error(MessageFormat.format(Messages.getString("WSClientImpl.12"), endpoint)); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("WSClientImpl.2")); //$NON-NLS-1$
		}
		
		this.logger.debug(MessageFormat.format(Messages.getString("WSClientImpl.13"), endpoint)); //$NON-NLS-1$
		
		return invokeWSCall(decisionRequest, endpoint, action);
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.spep.ws.WSClient#spepStartup(java.lang.String, java.lang.String)
	 */
	public Element spepStartup(Element spepStartup, String endpoint) throws WSClientException
	{
		final String action = "spepStartup";

		if (endpoint == null || endpoint.length() <= 0)
		{
			this.logger.error(Messages.getString("WSClientImpl.14")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("WSClientImpl.5")); //$NON-NLS-1$
		}
		
		if (spepStartup == null)
		{
			this.logger.error(MessageFormat.format(Messages.getString("WSClientImpl.15"), endpoint)); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("WSClientImpl.4")); //$NON-NLS-1$
		}
		
		this.logger.debug(MessageFormat.format(Messages.getString("WSClientImpl.16"), endpoint)); //$NON-NLS-1$
		
		return invokeWSCall(spepStartup, endpoint, action);
	}
	
	public Element artifactResolve(Element artifactResolve, String endpoint) throws WSClientException
	{
		final String action = "artifactResolve";

		if (endpoint == null || endpoint.length() <= 0)
		{
			this.logger.error("WS target endpoint cannot be null");
			throw new IllegalArgumentException("WS target endpoint cannot be null");
		}
		
		if (artifactResolve == null)
		{
			this.logger.error("Artifact resolve request to endpoint {} failed, request was null", endpoint);
			throw new IllegalArgumentException("Artifact resolve request cannot be null");
		}
		
		this.logger.debug("Sending artifact resolve request to {}", endpoint);
		
		return invokeWSCall(artifactResolve, endpoint, action);
	}
	

	/*
	 * Responsible for actual logic in converting incoming string to Axis format and translating Axis response format to
	 * string
	 * 
	 * @param request
	 *            The SAML document to send to remote soap server
	 * @param endpoint
	 *            The endpoint of the the remote server to communicate with
	 * @return The SAML response document from remote soap server
	 * @throws WSClientException
	 */
	private Element invokeWSCall(Element request, String endpoint, String soapAction) throws WSClientException
	{
		byte[] requestBytes;
		try
		{
			requestBytes = this.soapHandler.wrapDocument(request);
		}
		catch (SOAPException e)
		{
			this.logger.debug("SOAP exception occurred while trying to wrap the request document.", e);
			throw new WSClientException("SOAP exception occurred while trying to wrap the request document. Error was: " + e.getMessage());
		}
		
		CharsetDetector detector = new CharsetDetector();
		this.logger.trace(detector.getString(requestBytes, null));

		ByteArrayOutputStream responseStream;
		try
		{
			URL url = new URL(endpoint);
			URLConnection connection = url.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(true);
			
			this.setContentType(connection);
			connection.setRequestProperty(SOAP_ACTION, soapAction);
			
			OutputStream out = connection.getOutputStream();
			out.write(requestBytes);
			
			InputStream in = connection.getInputStream();
			responseStream = new ByteArrayOutputStream();
			byte[] buf = new byte[BUF_SIZE];
			int count;
			while ((count = in.read(buf)) > 0)
			{
				responseStream.write(buf, 0, count);
			}
		}
		catch (MalformedURLException e)
		{
			this.logger.debug("WS endpoint URL {} was malformed. Unable to perform query", new Object[]{endpoint}, e);
			throw new WSClientException("SOAP exception occurred while trying to wrap the request document. Error was: " + e.getMessage());
		}
		catch (IOException e)
		{
			this.logger.debug("SOAP exception occurred while trying to wrap the request document.", e);
			throw new WSClientException("SOAP exception occurred while trying to wrap the request document. Error was: " + e.getMessage());
		}
		
		try
		{
			byte[] responseDocument = responseStream.toByteArray();
			this.logger.trace(detector.getString(responseDocument, null));

			return this.soapHandler.unwrapDocument(responseDocument);
		}
		catch (SOAPException e)
		{
			this.logger.debug("SOAP exception occurred while trying to unwrap the response document.", e);
			throw new WSClientException("SOAP exception occurred while trying to unwrap the response document. Error was: " + e.getMessage());
		}
	}
	
	private void setContentType(URLConnection connection)
	{
		String encoding = this.soapHandler.getDefaultEncoding();
		connection.setRequestProperty(CONTENT_TYPE, this.soapHandler.getContentType(encoding));
	}
}

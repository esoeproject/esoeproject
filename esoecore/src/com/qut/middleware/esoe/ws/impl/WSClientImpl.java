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
 * Creation Date: 05/12/2006
 * 
 * Purpose: Implements web services client logic
 */
package com.qut.middleware.esoe.ws.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.ibm.icu.text.CharsetDetector;
import com.qut.middleware.esoe.ws.WSClient;
import com.qut.middleware.esoe.ws.exception.WSClientException;
import com.qut.middleware.saml2.exception.SOAPException;
import com.qut.middleware.saml2.handler.SOAPHandler;

/** Implements web services client logic. */
public class WSClientImpl implements WSClient
{
	private static final String CONTENT_TYPE = "Content-Type";
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.ws.WSClient#authzCacheClear(java.lang.String, java.lang.String)
	 */
	public Element authzCacheClear(Element request, String endpoint) throws WSClientException
	{
		if (request == null)
			throw new IllegalArgumentException(Messages.getString("WSClientImpl.0")); //$NON-NLS-1$

		if (endpoint == null)
			throw new IllegalArgumentException(Messages.getString("WSClientImpl.1")); //$NON-NLS-1$

		return invokeWSCall(request, endpoint);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.ws.WSClient#singleLogout(java.lang.String, java.lang.String)
	 */
	public Element singleLogout(Element request, String endpoint) throws WSClientException
	{
		if (request == null)
			throw new IllegalArgumentException(Messages.getString("WSClientImpl.2")); //$NON-NLS-1$

		if (endpoint == null)
			throw new IllegalArgumentException(Messages.getString("WSClientImpl.3")); //$NON-NLS-1$

		return invokeWSCall(request, endpoint);
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
	private Element invokeWSCall(Element request, String endpoint) throws WSClientException
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
			return this.soapHandler.unwrapDocument(responseStream.toByteArray());
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

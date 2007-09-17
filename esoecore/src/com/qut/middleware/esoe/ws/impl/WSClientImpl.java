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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;

import com.qut.middleware.esoe.ws.WSClient;
import com.qut.middleware.esoe.ws.exception.WSClientException;

/** Implements web services client logic. */
public class WSClientImpl implements WSClient
{
	private XMLInputFactory xmlInputFactory;
	
	
	/**
	 * Constructor
	 */
	public WSClientImpl()
	{
		this.xmlInputFactory = XMLInputFactory.newInstance();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.ws.WSClient#authzCacheClear(java.lang.String, java.lang.String)
	 */
	public byte[] authzCacheClear(byte[] request, String endpoint) throws WSClientException
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
	public byte[] singleLogout(byte[] request, String endpoint) throws WSClientException
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
	private byte[] invokeWSCall(byte[] request, String endpoint) throws WSClientException
	{
		ByteArrayOutputStream response;
		XMLStreamReader xmlreader;
		StAXOMBuilder builder;
		OMElement requestElement, resultElement;
		ServiceClient serviceClient;
		Options options;
		EndpointReference targetEPR;

		try
		{
			targetEPR = new EndpointReference(endpoint);
			xmlreader = this.xmlInputFactory.createXMLStreamReader(new ByteArrayInputStream(request));
			builder = new StAXOMBuilder(xmlreader);
			requestElement = builder.getDocumentElement();

			serviceClient = new ServiceClient();
			options = new Options();
			serviceClient.setOptions(options);
			options.setTo(targetEPR);
			options.setProperty(org.apache.axis2.Constants.Configuration.CHARACTER_SET_ENCODING, "UTF-16");

			resultElement = serviceClient.sendReceive(requestElement);

			response = new ByteArrayOutputStream();
			if (resultElement != null)
			{
				resultElement.serialize(XMLOutputFactory.newInstance().createXMLStreamWriter(response));
			}
			else
			{
				throw new WSClientException(com.qut.middleware.esoe.ws.impl.Messages.getString("WSClientImpl.4")); //$NON-NLS-1$
			}

			return response.toByteArray();
		}
		catch (AxisFault e)
		{
			throw new WSClientException(e.getMessage(), e);
		}
		catch (XMLStreamException e)
		{
			throw new WSClientException(e.getMessage(), e);
		}
		catch (FactoryConfigurationError e)
		{
			throw new WSClientException(e.getMessage());
		}
	}
}

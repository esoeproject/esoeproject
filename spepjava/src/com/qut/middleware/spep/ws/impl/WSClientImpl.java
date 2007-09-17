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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.MessageFormat;

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
import org.apache.log4j.Logger;

import com.qut.middleware.spep.authn.impl.AuthnProcessorImpl;
import com.qut.middleware.spep.ws.Messages;
import com.qut.middleware.spep.ws.WSClient;
import com.qut.middleware.spep.ws.exception.WSClientException;

/** */
public class WSClientImpl implements WSClient
{
	private XMLInputFactory xmlInputFactory;

	/* Local logging instance */
	private Logger logger = Logger.getLogger(WSClientImpl.class.getName());

	/**
	 * Constructor
	 * @param reportingProcessor 
	 */
	public WSClientImpl()
	{
		this.xmlInputFactory = XMLInputFactory.newInstance();
		
		this.logger.info(Messages.getString("WSClientImpl.7")); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see com.qut.middleware.spep.ws.WSClient#attributeAuthority(java.lang.String, java.lang.String)
	 */
	public byte[] attributeAuthority(byte[] attributeQuery, String endpoint) throws WSClientException
	{
		if (endpoint == null || endpoint.length() <= 0)
		{
			this.logger.error(Messages.getString("WSClientImpl.8")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("WSClientImpl.1")); //$NON-NLS-1$
		}
		
		if (attributeQuery == null || attributeQuery.length <= 0)
		{
			this.logger.error(MessageFormat.format(Messages.getString("WSClientImpl.9"), endpoint)); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("WSClientImpl.0")); //$NON-NLS-1$
		}
		
		this.logger.debug(MessageFormat.format(Messages.getString("WSClientImpl.10"), endpoint)); //$NON-NLS-1$
		
		return invokeWSCall(attributeQuery, endpoint);
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.spep.ws.WSClient#policyDecisionPoint(java.lang.String, java.lang.String)
	 */
	public byte[] policyDecisionPoint(byte[] decisionRequest, String endpoint) throws WSClientException
	{
		if (endpoint == null || endpoint.length() <= 0)
		{
			this.logger.error(Messages.getString("WSClientImpl.11")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("WSClientImpl.3")); //$NON-NLS-1$
		}
		
		if (decisionRequest == null || decisionRequest.length <= 0)
		{
			this.logger.error(MessageFormat.format(Messages.getString("WSClientImpl.12"), endpoint)); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("WSClientImpl.2")); //$NON-NLS-1$
		}
		
		this.logger.debug(MessageFormat.format(Messages.getString("WSClientImpl.13"), endpoint)); //$NON-NLS-1$
		
		return invokeWSCall(decisionRequest, endpoint);
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.spep.ws.WSClient#spepStartup(java.lang.String, java.lang.String)
	 */
	public byte[] spepStartup(byte[] spepStartup, String endpoint) throws WSClientException
	{
		if (endpoint == null || endpoint.length() <= 0)
		{
			this.logger.error(Messages.getString("WSClientImpl.14")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("WSClientImpl.5")); //$NON-NLS-1$
		}
		
		if (spepStartup == null || spepStartup.length <= 0)
		{
			this.logger.error(MessageFormat.format(Messages.getString("WSClientImpl.15"), endpoint)); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("WSClientImpl.4")); //$NON-NLS-1$
		}
		
		this.logger.debug(MessageFormat.format(Messages.getString("WSClientImpl.16"), endpoint)); //$NON-NLS-1$
		
		return invokeWSCall(spepStartup, endpoint);
	}
	
	/**
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
		ByteArrayInputStream reader;
		ByteArrayOutputStream writer;
		XMLStreamReader xmlreader;
		StAXOMBuilder builder;
		OMElement requestElement, resultElement;
		ServiceClient serviceClient;
		Options options;
		EndpointReference targetEPR;

		try
		{
			targetEPR = new EndpointReference(endpoint);
			reader = new ByteArrayInputStream(request);
			xmlreader = this.xmlInputFactory.createXMLStreamReader(reader);
			builder = new StAXOMBuilder(xmlreader);
			requestElement = builder.getDocumentElement();

			serviceClient = new ServiceClient();
			options = new Options();
			options.setTo(targetEPR);
			options.setProperty(org.apache.axis2.Constants.Configuration.CHARACTER_SET_ENCODING, "UTF-16");
			serviceClient.setOptions(options);
			
			this.logger.debug(Messages.getString("WSClientImpl.17")); //$NON-NLS-1$

			resultElement = serviceClient.sendReceive(requestElement);

			writer = new ByteArrayOutputStream();
			if (resultElement != null)
			{
				resultElement.serialize(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));
			}
			else
			{
				this.logger.error(Messages.getString("WSClientImpl.18")); //$NON-NLS-1$
				throw new WSClientException(Messages.getString("WSClientImpl.6")); //$NON-NLS-1$
			}

			this.logger.debug(MessageFormat.format(Messages.getString("WSClientImpl.19"), endpoint)); //$NON-NLS-1$
			return writer.toByteArray();
		}
		catch (AxisFault e)
		{
			this.logger.error("AxisFault occured while invoking WS call - trace level has full output");
			this.logger.trace(MessageFormat.format(Messages.getString("WSClientImpl.20"), e.getMessage())); //$NON-NLS-1$
			throw new WSClientException(e.getMessage(), e);
		}
		catch (XMLStreamException e)
		{
			this.logger.error("XML Stream exception occured while invoking WS call");
			this.logger.trace(MessageFormat.format(Messages.getString("WSClientImpl.21"), e.getMessage())); //$NON-NLS-1$
			throw new WSClientException(e.getMessage(), e);
		}
		catch (FactoryConfigurationError e)
		{
			e.getException().printStackTrace();
			this.logger.error(MessageFormat.format(Messages.getString("WSClientImpl.22"), e.getMessage())); //$NON-NLS-1$
			throw new WSClientException(e.getMessage());
		}
	}
}

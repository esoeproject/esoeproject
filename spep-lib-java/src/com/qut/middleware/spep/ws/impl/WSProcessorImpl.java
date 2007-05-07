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

import java.io.StringReader;
import java.io.StringWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.log4j.Logger;

import com.qut.middleware.spep.Initializer;
import com.qut.middleware.spep.SPEP;
import com.qut.middleware.spep.authn.AuthnProcessorData;
import com.qut.middleware.spep.authn.impl.AuthnProcessorDataImpl;
import com.qut.middleware.spep.exception.SPEPInitializationException;
import com.qut.middleware.spep.ws.Messages;
import com.qut.middleware.spep.ws.WSProcessor;

/** */
public class WSProcessorImpl implements WSProcessor
{
	private XMLInputFactory xmlInputFactory;

	private Logger logger = Logger.getLogger(WSProcessorImpl.class.getName());

	/**
	 * Default constructor
	 */
	public WSProcessorImpl()
	{
		this.xmlInputFactory = XMLInputFactory.newInstance();
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.spep.ws.WSProcessor#authzCacheClear(org.apache.axiom.om.OMElement)
	 */
	public OMElement authzCacheClear(OMElement request) throws AxisFault
	{
		MessageContext.getCurrentMessageContext().getAxisService();
		String requestDocument = null, responseDocument = null;
		try
		{
			requestDocument = readRequest(request);
			this.logger.debug(Messages.getString("WSProcessorImpl.6") + requestDocument); //$NON-NLS-1$

			SPEP spep = initSPEP();
			responseDocument = spep.getPolicyEnforcementProcessor().authzCacheClear(requestDocument);

			if (responseDocument != null)
			{
				this.logger.debug(Messages.getString("WSProcessorImpl.7") + responseDocument); //$NON-NLS-1$
				return generateResponse(responseDocument);
			}

			throw new AxisFault(Messages.getString("WSProcessorImpl.0")); //$NON-NLS-1$
			
		}
		catch (Exception e)
		{
			if (responseDocument != null)
			{
				this.logger.debug(Messages.getString("WSProcessorImpl.7") + responseDocument); //$NON-NLS-1$
				return generateResponse(responseDocument);
			}

			throw new AxisFault(e.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see com.qut.middleware.spep.ws.WSProcessor#singleLogout(org.apache.axiom.om.OMElement)
	 */
	public OMElement singleLogout(OMElement request) throws AxisFault
	{
		String requestDocument = null, responseDocument = null;
		try
		{
			requestDocument = readRequest(request);
			this.logger.debug(Messages.getString("WSProcessorImpl.8") + requestDocument); //$NON-NLS-1$

			SPEP spep = initSPEP();
			AuthnProcessorData data = new AuthnProcessorDataImpl();
			data.setRequestDocument(requestDocument);
			spep.getAuthnProcessor().logoutPrincipal(data);
			
			responseDocument = data.getResponseDocument();

			if (responseDocument != null)
			{
				this.logger.debug(Messages.getString("WSProcessorImpl.9") + requestDocument); //$NON-NLS-1$
				return generateResponse(responseDocument);
			}

			throw new AxisFault(Messages.getString("WSProcessorImpl.1")); //$NON-NLS-1$
			
		}
		catch (Exception e)
		{
			if (responseDocument != null)
			{
				this.logger.debug(Messages.getString("WSProcessorImpl.7") + responseDocument); //$NON-NLS-1$
				return generateResponse(responseDocument);
			}

			throw new AxisFault(e.getMessage());
		}
	}
	
	private SPEP initSPEP() throws SPEPInitializationException
	{
		MessageContext messageContext = MessageContext.getCurrentMessageContext();
		if (messageContext == null)
		{
			throw new IllegalArgumentException(Messages.getString("WSProcessorImpl.2")); //$NON-NLS-1$
		}
		
		Parameter servletConfigParameter = messageContext.getAxisService().getParameter(HTTPConstants.HTTP_SERVLETCONFIG);
		if (servletConfigParameter == null)
		{
			throw new IllegalArgumentException(Messages.getString("WSProcessorImpl.3")); //$NON-NLS-1$
		}
		
		Object servletConfigObject = servletConfigParameter.getValue();
		if (servletConfigObject == null || !(servletConfigObject instanceof ServletConfig))
		{
			throw new IllegalArgumentException(Messages.getString("WSProcessorImpl.4")); //$NON-NLS-1$
		}
		
		ServletContext servletContext = ((ServletConfig)servletConfigObject).getServletContext();
		if (servletContext == null)
		{
			throw new IllegalArgumentException(Messages.getString("WSProcessorImpl.5")); //$NON-NLS-1$
		}
		
		return Initializer.init(servletContext);
	}

	/**
	 * Reads Axis2 web requests and gets the raw Soap body as a String
	 * 
	 * @param requestDocument Axis 2 Axiom representation of the request
	 * @return String representation of the request document
	 */
	private String readRequest(OMElement requestDocument) throws AxisFault
	{
		StringWriter writer;
		XMLStreamWriter xmlWriter;

		try
		{
			writer = new StringWriter();
			xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(writer);
			requestDocument.serialize(xmlWriter);
			writer.flush();

			return writer.toString();
		}
		catch (XMLStreamException e)
		{
			throw new AxisFault(e.getMessage());
		}
	}

	/**
	 * Generates an Axis 2 response object
	 * 
	 * @param responseDocument
	 *            String representation of the SAML document to respond with
	 * @return The Axis2 representation of the document
	 */
	private OMElement generateResponse(String responseDocument) throws AxisFault
	{
		XMLStreamReader xmlreader;
		StAXOMBuilder builder;
		OMElement response;
		StringReader reader;

		try
		{
			reader = new StringReader(responseDocument);
			xmlreader = this.xmlInputFactory.createXMLStreamReader(reader);
			builder = new StAXOMBuilder(xmlreader);
			response = builder.getDocumentElement();

			return response;
		}
		catch (XMLStreamException e)
		{
			throw new AxisFault(e.getMessage());
		}
	}
}

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.log4j.Logger;

import com.qut.middleware.esoe.aa.AttributeAuthorityProcessor;
import com.qut.middleware.esoe.aa.bean.AAProcessorData;
import com.qut.middleware.esoe.aa.bean.impl.AAProcessorDataImpl;
import com.qut.middleware.esoe.aa.exception.InvalidPrincipalException;
import com.qut.middleware.esoe.aa.exception.InvalidRequestException;
import com.qut.middleware.esoe.delegauthn.DelegatedAuthenticationProcessor;
import com.qut.middleware.esoe.delegauthn.bean.DelegatedAuthenticationData;
import com.qut.middleware.esoe.delegauthn.bean.impl.DelegatedAuthenticationDataImpl;
import com.qut.middleware.esoe.pdp.AuthorizationProcessor;
import com.qut.middleware.esoe.pdp.bean.AuthorizationProcessorData;
import com.qut.middleware.esoe.pdp.bean.impl.AuthorizationProcessorDataImpl;
import com.qut.middleware.esoe.spep.SPEPProcessor;
import com.qut.middleware.esoe.spep.bean.SPEPProcessorData;
import com.qut.middleware.esoe.spep.bean.impl.SPEPProcessorDataImpl;
import com.qut.middleware.esoe.spep.exception.DatabaseFailureException;
import com.qut.middleware.esoe.spep.exception.DatabaseFailureNoSuchSPEPException;
import com.qut.middleware.esoe.spep.exception.SPEPCacheUpdateException;
import com.qut.middleware.esoe.ws.WSProcessor;

public class WSProcessorImpl implements WSProcessor
{
	private AttributeAuthorityProcessor attributeAuthorityProcessor;
	private SPEPProcessor spepProcessor;
	private AuthorizationProcessor authorizationProcessor;
	private DelegatedAuthenticationProcessor delegAuthnProcessor;

	private XMLInputFactory xmlInputFactory;

	/* Local logging instance */
	private Logger logger = Logger.getLogger(WSProcessorImpl.class.getName());

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
			DelegatedAuthenticationProcessor delegAuthnProcessor)
	{
		if (attributeAuthorityProcessor == null)
		{
			this.logger.fatal(Messages.getString("WSProcessorImpl.36")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("WSProcessorImpl.37")); //$NON-NLS-1$
		}
		if (authorizationProcessor == null)
		{
			this.logger.fatal(Messages.getString("WSProcessorImpl.38")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("WSProcessorImpl.39")); //$NON-NLS-1$
		}
		if (spepProcessor == null)
		{
			this.logger.fatal(Messages.getString("WSProcessorImpl.40")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("WSProcessorImpl.41")); //$NON-NLS-1$
		}
		if (delegAuthnProcessor == null)
		{
			this.logger.fatal(Messages.getString("WSProcessorImpl.42")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("WSProcessorImpl.43")); //$NON-NLS-1$
		}

		this.attributeAuthorityProcessor = attributeAuthorityProcessor;
		this.spepProcessor = spepProcessor;
		this.authorizationProcessor = authorizationProcessor;
		this.delegAuthnProcessor = delegAuthnProcessor;

		this.xmlInputFactory = XMLInputFactory.newInstance();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.ws.WSProcessor#attributeAuthority(org.apache.axiom.om.OMElement)
	 */
	public OMElement attributeAuthority(OMElement attributeQuery) throws AxisFault
	{
		AAProcessorData data = new AAProcessorDataImpl();

		this.logger.debug(Messages.getString("WSProcessorImpl.3")); //$NON-NLS-1$
		try
		{
			data.setRequestDocument(readRequest(attributeQuery));
			this.attributeAuthorityProcessor.execute(data);

			if (data.getResponseDocument() != null)
			{
				this.logger.debug(Messages.getString("WSProcessorImpl.4")); //$NON-NLS-1$
				return generateResponse(data.getResponseDocument());
			}

			this.logger.warn(Messages.getString("WSProcessorImpl.5")); //$NON-NLS-1$
			throw new AxisFault(Messages.getString("WSProcessorImpl.52") + Messages.getString("WSProcessorImpl.0")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch (InvalidPrincipalException e)
		{
			this.logger.warn(Messages.getString("WSProcessorImpl.6")); //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);
			if (data.getResponseDocument() != null)
			{
				this.logger.debug(Messages.getString("WSProcessorImpl.7")); //$NON-NLS-1$
				return generateResponse(data.getResponseDocument());
			}

			this.logger.warn(Messages.getString("WSProcessorImpl.8")); //$NON-NLS-1$
			throw new AxisFault(Messages.getString("WSProcessorImpl.52") + e.getMessage(), e); //$NON-NLS-1$
		}
		catch (InvalidRequestException e)
		{
			this.logger.warn(Messages.getString("WSProcessorImpl.9")); //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);
			if (data.getResponseDocument() != null)
			{
				this.logger.debug(Messages.getString("WSProcessorImpl.10")); //$NON-NLS-1$
				return generateResponse(data.getResponseDocument());
			}

			this.logger.warn(Messages.getString("WSProcessorImpl.11")); //$NON-NLS-1$
			throw new AxisFault(Messages.getString("WSProcessorImpl.52") + e.getMessage(), e); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.ws.WSProcessor#policyDecisionPoint(org.apache.axiom.om.OMElement)
	 */
	public OMElement policyDecisionPoint(OMElement decisionRequest) throws AxisFault
	{
		synchronized( this )
		{
		AuthorizationProcessorData data = new AuthorizationProcessorDataImpl();
		String decisionRequestString;

		this.logger.debug(Messages.getString("WSProcessorImpl.12")); //$NON-NLS-1$
		try
		{

			data.setRequestDocument(readRequest(decisionRequest));
			this.authorizationProcessor.execute(data);

			if (data.getResponseDocument() != null)
			{
				this.logger.debug(Messages.getString("WSProcessorImpl.13")); //$NON-NLS-1$
				return generateResponse(data.getResponseDocument());
			}

			this.logger.warn(Messages.getString("WSProcessorImpl.14")); //$NON-NLS-1$
			throw new AxisFault(Messages.getString("WSProcessorImpl.1")); //$NON-NLS-1$
		}
		catch (com.qut.middleware.esoe.pdp.exception.InvalidRequestException e)
		{
			this.logger.warn(Messages.getString("WSProcessorImpl.15")); //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);
			if (data.getResponseDocument() != null)
			{
				this.logger.debug(Messages.getString("WSProcessorImpl.16")); //$NON-NLS-1$
				return generateResponse(data.getResponseDocument());
			}

			this.logger.warn(Messages.getString("WSProcessorImpl.17")); //$NON-NLS-1$
			throw new AxisFault(Messages.getString("WSProcessorImpl.52") + e.getMessage()); //$NON-NLS-1$
		}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.ws.WSProcessor#spepStartup(org.apache.axiom.om.OMElement)
	 */
	public OMElement spepStartup(OMElement spepStartup) throws AxisFault
	{

		SPEPProcessorData data = new SPEPProcessorDataImpl();

		this.logger.debug(Messages.getString("WSProcessorImpl.18")); //$NON-NLS-1$
		try
		{
			data.setRequestDocument(readRequest(spepStartup));

			this.spepProcessor.getStartup().registerSPEPStartup(data);

			if (data.getResponseDocument() != null)
			{
				this.logger.debug(Messages.getString("WSProcessorImpl.19")); //$NON-NLS-1$
				return generateResponse(data.getResponseDocument());
			}

			this.logger.warn(Messages.getString("WSProcessorImpl.20")); //$NON-NLS-1$
			throw new AxisFault(Messages.getString("WSProcessorImpl.2")); //$NON-NLS-1$
		}
		catch (com.qut.middleware.esoe.spep.exception.InvalidRequestException e)
		{
			this.logger.warn(Messages.getString("WSProcessorImpl.21")); //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);
			if (data.getResponseDocument() != null)
			{
				this.logger.debug(Messages.getString("WSProcessorImpl.22")); //$NON-NLS-1$
				return generateResponse(data.getResponseDocument());
			}

			this.logger.warn(Messages.getString("WSProcessorImpl.23")); //$NON-NLS-1$
			throw new AxisFault(Messages.getString("WSProcessorImpl.52") + e.getMessage()); //$NON-NLS-1$
		}
		catch (DatabaseFailureNoSuchSPEPException e)
		{
			this.logger.warn(Messages.getString("WSProcessorImpl.24")); //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);
			if (data.getResponseDocument() != null)
			{
				this.logger.debug(Messages.getString("WSProcessorImpl.25")); //$NON-NLS-1$
				return generateResponse(data.getResponseDocument());
			}

			this.logger.warn(Messages.getString("WSProcessorImpl.26")); //$NON-NLS-1$
			throw new AxisFault(Messages.getString("WSProcessorImpl.52") + e.getMessage()); //$NON-NLS-1$
		}
		catch (SPEPCacheUpdateException e)
		{
			this.logger.warn(Messages.getString("WSProcessorImpl.27")); //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);
			if (data.getResponseDocument() != null)
			{
				this.logger.debug(Messages.getString("WSProcessorImpl.28")); //$NON-NLS-1$
				return generateResponse(data.getResponseDocument());
			}

			throw new AxisFault(Messages.getString("WSProcessorImpl.52") + e.getMessage()); //$NON-NLS-1$
		}
		catch (DatabaseFailureException e)
		{
			this.logger.warn(Messages.getString("WSProcessorImpl.29")); //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);

			if (data.getResponseDocument() != null)
			{
				this.logger.debug(Messages.getString("WSProcessorImpl.30")); //$NON-NLS-1$
				return generateResponse(data.getResponseDocument());
			}

			this.logger.warn(Messages.getString("WSProcessorImpl.31")); //$NON-NLS-1$
			throw new AxisFault(Messages.getString("WSProcessorImpl.52") + e.getMessage()); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.ws.WSProcessor#registerPrincipal(org.apache.axiom.om.OMElement)
	 */
	public OMElement registerPrincipal(OMElement registerPrincipal) throws AxisFault
	{
		DelegatedAuthenticationData data = new DelegatedAuthenticationDataImpl();
		byte[] decisionRequestString;

		this.logger.debug(Messages.getString("WSProcessorImpl.47")); //$NON-NLS-1$

		decisionRequestString = readRequest(registerPrincipal);
		this.logger.debug(Messages.getString("WSProcessorImpl.48") + decisionRequestString); //$NON-NLS-1$

		data.setRequestDocument(decisionRequestString);
		this.delegAuthnProcessor.execute(data);

		if (data.getResponseDocument() != null)
		{
			this.logger.debug(Messages.getString("WSProcessorImpl.49")); //$NON-NLS-1$
			return generateResponse(data.getResponseDocument());
		}

		this.logger.warn(Messages.getString("WSProcessorImpl.50")); //$NON-NLS-1$
		throw new AxisFault(Messages.getString("WSProcessorImpl.51")); //$NON-NLS-1$
	}

	/**
	 * Reads Axis2 web requests and gets the raw Soap body as a String
	 * 
	 * @param requestDocument
	 *            Axis 2 Axiom representation of the request
	 * @return String representation of the request document
	 */
	private byte[] readRequest(OMElement requestDocument) throws AxisFault
	{
		ByteArrayOutputStream request;
		XMLStreamWriter xmlWriter;

		this.logger.debug(Messages.getString("WSProcessorImpl.32")); //$NON-NLS-1$
		try
		{
			request = new ByteArrayOutputStream();
			xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(request);
			requestDocument.serialize(xmlWriter);

			return request.toByteArray();
		}
		catch (XMLStreamException e)
		{
			this.logger.warn(Messages.getString("WSProcessorImpl.33")); //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new AxisFault(Messages.getString("WSProcessorImpl.52") + e.getMessage()); //$NON-NLS-1$
		}
	}

	/**
	 * Generates an Axis 2 response object
	 * 
	 * @param responseDocument
	 *            String representation of the SAML document to respond with
	 * @return
	 */
	private OMElement generateResponse(byte[] responseDocument) throws AxisFault
	{
		XMLStreamReader xmlreader;
		StAXOMBuilder builder;
		OMElement response;
		ByteArrayInputStream responseStream;

		this.logger.debug(Messages.getString("WSProcessorImpl.34")); //$NON-NLS-1$
		try
		{

			responseStream = new ByteArrayInputStream(responseDocument);
			xmlreader = this.xmlInputFactory.createXMLStreamReader(responseStream);
			builder = new StAXOMBuilder(xmlreader);
			response = builder.getDocumentElement();

			return response;
		}
		catch (XMLStreamException e)
		{
			this.logger.warn(Messages.getString("WSProcessorImpl.35")); //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new AxisFault(Messages.getString("WSProcessorImpl.52") + e.getMessage()); //$NON-NLS-1$
		}
	}
}

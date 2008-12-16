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
 * Creation Date: 07/03/2007
 * 
 * Purpose: Implementation of AuthnProcessor for shibboleth 1.3 integration
 */
package com.qut.middleware.delegator.shib.authn.impl;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3._2000._09.xmldsig_.Signature;

import com.qut.middleware.crypto.KeystoreResolver;
import com.qut.middleware.delegator.shib.ConfigurationConstants;
import com.qut.middleware.delegator.shib.authn.AuthnProcessor;
import com.qut.middleware.delegator.shib.authn.bean.AuthnProcessorData;
import com.qut.middleware.delegator.shib.authn.bean.ShibAttribute;
import com.qut.middleware.delegator.shib.ws.WSClient;
import com.qut.middleware.delegator.shib.ws.exception.WSClientException;
import com.qut.middleware.saml2.AttributeFormatConstants;
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
import com.qut.middleware.saml2.schemas.assertion.AttributeType;
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.esoe.delegated.RegisterPrincipalRequest;
import com.qut.middleware.saml2.schemas.esoe.delegated.RegisterPrincipalResponse;
import com.qut.middleware.saml2.validator.SAMLValidator;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import com.sun.org.apache.xml.internal.security.keys.storage.implementations.KeyStoreResolver;

public class AuthnProcessorImpl implements AuthnProcessor
{
	private final String SAML_AUTHN_DELEG_IDENTIFIER = "saml-delegated-authn-handler-v0.01";

	private SAMLValidator validator;
	private WSClient wsClient;
	
	private IdentifierGenerator identiferGenerator;
	private KeystoreResolver keyStoreResolver;
	private String issuerID;
	private String principalRegistrationEndpoint;
	private String userIdentifier;
	
	private List<ShibAttribute> defaultSiteAttributes;
	private List<ShibAttribute> requestedAttributes;

	private Unmarshaller<RegisterPrincipalResponse> unmarshaller;
	private Marshaller<RegisterPrincipalRequest> marshaller;

	private final String[] schemas = new String[] { ConfigurationConstants.delegatedAuthn };
	private final String UNMAR_PKGNAMES = RegisterPrincipalResponse.class.getPackage().getName();
	private final String MAR_PKGNAMES = RegisterPrincipalRequest.class.getPackage().getName();

	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(AuthnProcessorImpl.class.getName());

	public AuthnProcessorImpl(SAMLValidator validator, WSClient wsClient, IdentifierGenerator identiferGenerator, KeyStoreResolver keytoreResolver, String issuerID, String principalRegistrationEndpoint, List<ShibAttribute> defaultSiteAttributes, List<ShibAttribute> requestedAttributes, String userIdentifier) throws UnmarshallerException, MarshallerException
	{
		this.validator = validator;
		this.wsClient = wsClient;
		this.identiferGenerator = identiferGenerator;
		this.keyStoreResolver = keyStoreResolver;
		this.issuerID = issuerID;
		this.principalRegistrationEndpoint = principalRegistrationEndpoint;
		this.defaultSiteAttributes = defaultSiteAttributes;
		this.requestedAttributes = requestedAttributes;
		this.userIdentifier = userIdentifier;

		this.unmarshaller = new UnmarshallerImpl<RegisterPrincipalResponse>(this.UNMAR_PKGNAMES, schemas, keyStoreResolver);
		this.marshaller = new MarshallerImpl<RegisterPrincipalRequest>(this.MAR_PKGNAMES, schemas, this.keyStoreResolver);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.delegator.shib.authn.AuthnProcessor#execute(com.qut.middleware.delegator.shib.authn.bean.AuthnProcessorData)
	 */
	public result execute(AuthnProcessorData processorData)
	{
		byte[] response;
		String remoteUser;
		RegisterPrincipalResponse responseObj;
		List<AttributeType> esoeAttributes = new ArrayList<AttributeType>();

		Map<String, List<Object>> sessionData = new HashMap<String, List<Object>>();

		Enumeration<String> headerNames = processorData.getHttpRequest().getHeaderNames();
		this.logger.debug("Retrieved header details from shibboleth authn, processing..");
		
		remoteUser = processorData.getHttpRequest().getRemoteUser();
		if(remoteUser != null && remoteUser.length() > 0)
		{
			this.logger.debug("REMOTE_USER is set in this shibboleth session as " + remoteUser);
			AttributeType esoeIdentifierAttribute = new AttributeType();
			esoeIdentifierAttribute.setNameFormat(AttributeFormatConstants.basic);
			esoeIdentifierAttribute.setName(this.userIdentifier);
			esoeIdentifierAttribute.getAttributeValues().add(remoteUser);
			esoeAttributes.add(esoeIdentifierAttribute);
		}
		else
			this.logger.debug("REMOTE_USER is not set in this shibboleth session");

		while (headerNames.hasMoreElements())
		{
			String headerName = headerNames.nextElement();
			this.logger.debug("Processing header " + headerName + " from shibboleth authentication response");

			for (ShibAttribute shibAttribute : this.requestedAttributes)
			{
				/*
				 * If this is a shibboleth attribute that has been configured then add to attribute data to present to
				 * ESOE
				 */
				if (shibAttribute.getLabel().equals(headerName))
				{
					this.logger.debug("Matched shibboleth attribute " + headerName + " to esoeAttribute " + shibAttribute.getEsoeAttributeName() + " processing supplied values");
					AttributeType esoeAttribute = new AttributeType();

					esoeAttribute.setNameFormat(AttributeFormatConstants.basic);
					esoeAttribute.setName(shibAttribute.getEsoeAttributeName());

					Enumeration<String> headerValues = processorData.getHttpRequest().getHeaders(headerName);
					while (headerValues.hasMoreElements())
					{
						String value = headerValues.nextElement();
						if(value != null && value.length() > 0)
						{
							this.logger.debug("Adding value of:" + value + " to attribute " + shibAttribute.getEsoeAttributeName());
							esoeAttribute.getAttributeValues().add(value);
						}
					}
					if (esoeAttribute.getAttributeValues().size() > 0)
					{
						esoeAttributes.add(esoeAttribute);
					}
				}
			}
		}

		/* Ensure we set default attributes */
		for (ShibAttribute attribute : this.defaultSiteAttributes)
		{
			AttributeType esoeDefaultAttribute = new AttributeType();
			esoeDefaultAttribute.setNameFormat(AttributeFormatConstants.basic);
			esoeDefaultAttribute.setName(attribute.getEsoeAttributeName());
			esoeDefaultAttribute.getAttributeValues().add(attribute.getValuePrepend() + attribute.getValue());
			esoeAttributes.add(esoeDefaultAttribute);
		}

		byte[] request = generateRegisterPrincipalRequest(esoeAttributes);
		if (request == null)
		{
			this.logger.warn("Failed attempting to marshall register principal request");
			return result.Failure;
		}

		this.logger.debug("Sending register principal request of: \n" + request);
		try
		{
			response = wsClient.registerPrincipal(request, this.principalRegistrationEndpoint);
		}
		catch (WSClientException e)
		{
			this.logger.error("Failed to successfully send WS call to registerPrincipal endpoint of " + this.principalRegistrationEndpoint);
			this.logger.debug(e.getMessage(), e);
			return result.Failure;
		}

		this.logger.debug("Got a SAML response from RegisterPrincipal WS call of: \n" + response);
		responseObj = this.validateResponse(response);

		if (responseObj == null)
		{
			this.logger.debug("Unable to correctly unmarshall response document from ESOE, failing principal authn attempt");
			return result.Failure;
		}

		/* Set the session identifier to whatever the ESOE says it should be */
		this.logger.debug("Setting session identifier as: " + responseObj.getSessionIdentifier() + " as advised by ESOE");
		processorData.setSessionID(responseObj.getSessionIdentifier());

		/* Insert attributes into the session for web teir to display to user for acceptance */
		processorData.getHttpRequest().getSession().setAttribute(ConfigurationConstants.RELEASED_ATTRIBUTES_SESSION_IDENTIFIER, esoeAttributes);

		return result.Completed;
	}

	private byte[] generateRegisterPrincipalRequest(List<AttributeType> attributes)
	{
		RegisterPrincipalRequest request = new RegisterPrincipalRequest();
		byte[] document;

		NameIDType issuer = new NameIDType();
		issuer.setValue(this.issuerID);
		request.setIssuer(issuer);
		request.setVersion(VersionConstants.saml20);
		request.getAttributesAndEncryptedAttributes().addAll(attributes);
		request.setID(this.identiferGenerator.generateSAMLID());
		request.setSource(this.SAML_AUTHN_DELEG_IDENTIFIER);
		request.setPrincipalAuthnIdentifier("unknown");
		request.setIssueInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));
		request.setSignature(new Signature());

		try
		{
			document = this.marshaller.marshallSigned(request);
		}
		catch (MarshallerException e)
		{
			this.logger.debug("Exception thrown when attempting to marshall the document", e);
			return null;
		}

		this.logger.debug("Successfully marshalled RegisterPrincipalRequest \n" + document);
		return document;
	}

	private RegisterPrincipalResponse validateResponse(byte[] response)
	{
		try
		{
			RegisterPrincipalResponse responseObj;

			responseObj = this.unmarshaller.unMarshallSigned(response);
			this.validator.getResponseValidator().validate(responseObj);

			return responseObj;
		}
		catch (SignatureValueException e)
		{
			this.logger.error("Fatal error validating response from ESOE - " + e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			return null;
		}
		catch (ReferenceValueException e)
		{
			this.logger.error("Fatal error validating response from ESOE - " + e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			return null;
		}
		catch (UnmarshallerException e)
		{
			this.logger.error("Fatal error validating response from ESOE - " + e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			return null;
		}
		catch (InvalidSAMLResponseException e)
		{
			this.logger.error("Fatal error validating response from ESOE - " + e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			return null;
		}
	}
}

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
 * Purpose: Implementation of AuthnProcessor
 */
package com.qut.middleware.delegator.openid.authn.impl;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.SimpleTimeZone;

import javax.servlet.ServletException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.codec.binary.Hex;
import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3._2000._09.xmldsig_.Signature;
import org.w3c.dom.Element;

import com.qut.middleware.crypto.KeystoreResolver;
import com.qut.middleware.delegator.openid.ConfigurationConstants;
import com.qut.middleware.delegator.openid.authn.AuthnProcessor;
import com.qut.middleware.delegator.openid.authn.bean.AuthnProcessorData;
import com.qut.middleware.delegator.openid.authn.bean.OpenIDAttribute;
import com.qut.middleware.esoe.ws.WSClient;
import com.qut.middleware.esoe.ws.exception.WSClientException;
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

public class AuthnProcessorImpl implements AuthnProcessor
{
	private final String OPENID_AUTHN_DELEG_IDENTIFIER = "openid-delegated-authn-handler";

	private SAMLValidator validator;
	private WSClient wsClient;
	private IdentifierGenerator identiferGenerator;
	private KeystoreResolver keyStoreResolver;
	private String issuerID;
	private String userIdentifier;
	private String principalRegistrationEndpoint;
	List<OpenIDAttribute> defaultSiteAttributes;
	List<OpenIDAttribute> requestedAttributes;
	private String responseEndpoint;
	private boolean httpsOffload;

	private Unmarshaller<RegisterPrincipalResponse> unmarshaller;
	private Marshaller<RegisterPrincipalRequest> marshaller;

	private final String[] schemas = new String[] { ConfigurationConstants.delegatedAuthn };
	private final String UNMAR_PKGNAMES = RegisterPrincipalResponse.class.getPackage().getName();
	private final String MAR_PKGNAMES = RegisterPrincipalRequest.class.getPackage().getName();

	private ConsumerManager manager;

	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(AuthnProcessorImpl.class.getName());

	/**
	 * Creates OpenID AuthnProcessorImpl
	 * @param validator SAML Validator
	 * @param wsClient Implementation of WSClient to invoke ws queries against ESOE with
	 * @param identiferGenerator Implementation of Identifier Generator
	 * @param keyStoreResolver Keystore resolver to resolve local and ESOE public keys
	 * @param responseEndpoint Endpoint to advise OpenID IDP to respond to on success.
	 * @param defaultSiteAttributes Default attributes to be populated by OpenID delegator
	 * @param requestedAttributes Attributes which the openID delegator should request from compatible openID IDP's
	 * @param issuerID ID to assign as issuer element
	 * @param principalRegistrationEndpoint URL for ESOE to direct WS connections to
	 * @param userIdentifier User identifier in use at site, generally uid
	 * @param httpsOffload Determines if the delegator is working behind a Layer 7 load balancer or not which is offloading SSL.
	 * @throws ConsumerException
	 * @throws UnmarshallerException
	 * @throws MarshallerException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public AuthnProcessorImpl(SAMLValidator validator, WSClient wsClient, IdentifierGenerator identiferGenerator, KeystoreResolver keyStoreResolver, String responseEndpoint, List<OpenIDAttribute> defaultSiteAttributes, List<OpenIDAttribute> requestedAttributes, String issuerID, String principalRegistrationEndpoint, String userIdentifier, boolean httpsOffload) throws ConsumerException, UnmarshallerException, MarshallerException, ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		this.validator = validator;
		this.wsClient = wsClient;
		this.identiferGenerator = identiferGenerator;
		this.keyStoreResolver = keyStoreResolver;
		this.issuerID = issuerID;
		this.principalRegistrationEndpoint = principalRegistrationEndpoint;
		this.responseEndpoint = responseEndpoint;
		this.defaultSiteAttributes = defaultSiteAttributes;
		this.requestedAttributes = requestedAttributes;
		this.userIdentifier = userIdentifier;
		this.httpsOffload = httpsOffload;

		this.unmarshaller = new UnmarshallerImpl<RegisterPrincipalResponse>(this.UNMAR_PKGNAMES, schemas, this.keyStoreResolver);
		this.marshaller = new MarshallerImpl<RegisterPrincipalRequest>(this.MAR_PKGNAMES, schemas, this.keyStoreResolver);

		manager = new ConsumerManager();
		//manager.setAllowNoEncHttp(false);
	}

	private void invokeOpenIDAuthnRequest(AuthnProcessorData processorData) throws OpenIDException, ServletException, IOException
	{
		List discoveries;
		DiscoveryInformation discovered;
		AuthRequest authReq;

		/* Perform discovery and association on users provider */
		discoveries = manager.discover(processorData.getHttpRequest().getParameter(ConfigurationConstants.OPENID_USER_IDENTIFIER));
		discovered = manager.associate(discoveries);

		processorData.getHttpRequest().getSession().setAttribute(ConfigurationConstants.OPENID_USER_SESSION_IDENTIFIER, discovered);

		/* Create AuthRequest message to be sent to the OpenID provider */
		authReq = manager.authenticate(discovered, this.responseEndpoint);

		FetchRequest request = FetchRequest.createFetchRequest();
		for (OpenIDAttribute attribute : this.requestedAttributes)
			request.addAttribute(attribute.getLabel(), attribute.getSchema(), attribute.isRequired());

		authReq.addExtension(request);

		/* Send request to provider dependent on protocol version supported */
		if (!discovered.isVersion2())
		{
			/* Perform GET request to OpenID version 1.0 provider */
			processorData.getHttpResponse().sendRedirect(authReq.getDestinationUrl(true));
		}
		else
		{
			/* Perform POST request to OpenID version 2.0 provider */
			processorData.getHttpRequest().getSession().setAttribute(ConfigurationConstants.OPENID_AUTH_REQUEST, authReq);
			processorData.getHttpResponse().sendRedirect("openidauthenticator.htm");
		}
	}

	private void verifyOpenIDAuthnResponse(AuthnProcessorData processorData, List<AttributeType> esoeAttributes) throws OpenIDException, NoSuchAlgorithmException
	{
		ParameterList response;
		DiscoveryInformation discovered;
		StringBuffer receivingURL;
		String queryString;
		VerificationResult verification;
		Identifier verified;
		AuthSuccess authSuccess;

		response = new ParameterList(processorData.getHttpRequest().getParameterMap());

		/* Retrieve the stored discovery information */
		discovered = (DiscoveryInformation) processorData.getHttpRequest().getSession().getAttribute(ConfigurationConstants.OPENID_USER_SESSION_IDENTIFIER);

		/* Extract the receiving URL from the HTTP request */
		receivingURL = processorData.getHttpRequest().getRequestURL();

		/*
		 * If a Layer 7 type device is offloading https change the recievingURL accordingly to ensure
		 * correct verification
		 */
		if (httpsOffload)
		{
			receivingURL.delete(0, 4);
			receivingURL.insert(0, "https");
		}

		queryString = processorData.getHttpRequest().getQueryString();
		if (queryString != null && queryString.length() > 0)
			receivingURL.append("?").append(processorData.getHttpRequest().getQueryString());

		/* Verify the response */
		this.logger.debug("About to verify response, accepted at receivingURL of " + receivingURL + " server set return to as " + response.toString());

		verification = manager.verify(receivingURL.toString(), response, discovered);
		verified = verification.getVerifiedId();
		if (verified != null)
		{
			AttributeType esoeAttribute;
			MessageDigest algorithm;
			byte messageDigest[];

			authSuccess = (AuthSuccess) verification.getAuthResponse();

			/*
			 * Merge verified ID to ESOE view, OpenID identifiers aren't really compatible with most applications as an
			 * identifier, so we'll md5 hash them for presentation as uid
			 */
			algorithm = MessageDigest.getInstance("MD5");
			algorithm.reset();
			algorithm.update(verification.getVerifiedId().getIdentifier().getBytes());
			messageDigest = algorithm.digest();

			esoeAttribute = new AttributeType();
			esoeAttribute.setNameFormat(AttributeFormatConstants.basic);
			esoeAttribute.setName(this.userIdentifier);
			esoeAttribute.getAttributeValues().add(new String(Hex.encodeHex(messageDigest)) + ConfigurationConstants.OPENID_NAMESPACE);
			esoeAttributes.add(esoeAttribute);

			/*
			 * Store openID identifier in attributes for use by applications
			 */
			esoeAttribute = new AttributeType();
			esoeAttribute.setNameFormat(AttributeFormatConstants.basic);
			esoeAttribute.setName(ConfigurationConstants.OPENID_IDENTIFIER_ATTRIBUTE);
			esoeAttribute.getAttributeValues().add(verification.getVerifiedId().getIdentifier());
			esoeAttributes.add(esoeAttribute);

			/*
			 * Retrieve requested attributes (if provided, given low deployments of attribute exchange currently we
			 * don't fail when this isn't presented)
			 */
			if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX))
			{
				FetchResponse fetchResp = (FetchResponse) authSuccess.getExtension(AxMessage.OPENID_NS_AX);

				for (OpenIDAttribute attribute : this.requestedAttributes)
				{
					List<String> values = fetchResp.getAttributeValues(attribute.getLabel());

					/* Merge to ESOE view */
					esoeAttribute = new AttributeType();
					esoeAttribute.setNameFormat(AttributeFormatConstants.basic);
					esoeAttribute.setName(attribute.getEsoeAttributeName());
					for (String value : values)
					{
						esoeAttribute.getAttributeValues().add(attribute.getValuePrepend() + value);
					}
					esoeAttributes.add(esoeAttribute);
				}
			}
		}
		else
		{
			throw new OpenIDException("Attempt by manager to verify result returned null");
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.qut.middleware.delegator.openid.authn.AuthnProcessor#execute(com.qut.middleware.delegator.openid.authn.bean.AuthnProcessorData)
	 */
	public result execute(AuthnProcessorData processorData)
	{
		Element response;
		RegisterPrincipalResponse responseObj;
		List<AttributeType> esoeAttributes = new ArrayList<AttributeType>();

		/* Determine operation to invoke */
		if (processorData.getHttpRequest().getParameter(ConfigurationConstants.OPENID_USER_IDENTIFIER) != null)
		{
			try
			{
				this.invokeOpenIDAuthnRequest(processorData);
				return result.NoOp;
			}
			catch (IOException e)
			{
				this.logger.error(e.getLocalizedMessage());
				this.logger.debug(e.toString());
				return result.Failure;
			}
			catch (OpenIDException e)
			{
				this.logger.error(e.getLocalizedMessage());
				this.logger.debug(e.toString());
				return result.Failure;
			}
			catch (ServletException e)
			{
				this.logger.error(e.getLocalizedMessage());
				this.logger.debug(e.toString());
				return result.Failure;
			}
		}
		else
		{
			try
			{
				this.verifyOpenIDAuthnResponse(processorData, esoeAttributes);
			}
			catch (OpenIDException e)
			{
				this.logger.error(e.getLocalizedMessage());
				this.logger.debug(e.toString());
				return result.Failure;
			}
			catch (NoSuchAlgorithmException e)
			{
				this.logger.error(e.getLocalizedMessage());
				this.logger.debug(e.toString());
				return result.Failure;
			}
		}

		/* Ensure we set default attributes */
		for (OpenIDAttribute attribute : this.defaultSiteAttributes)
		{
			AttributeType esoeDefaultAttribute = new AttributeType();
			esoeDefaultAttribute.setNameFormat(AttributeFormatConstants.basic);
			esoeDefaultAttribute.setName(attribute.getEsoeAttributeName());
			esoeDefaultAttribute.getAttributeValues().add(attribute.getValuePrepend() + attribute.getValue());
			esoeAttributes.add(esoeDefaultAttribute);
		}

		Element request = generateRegisterPrincipalRequest(esoeAttributes);
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

	private Element generateRegisterPrincipalRequest(List<AttributeType> attributes)
	{
		RegisterPrincipalRequest request = new RegisterPrincipalRequest();
		Element document;

		NameIDType issuer = new NameIDType();
		issuer.setValue(this.issuerID);
		request.setIssuer(issuer);
		request.setVersion(VersionConstants.saml20);
		request.getAttributesAndEncryptedAttributes().addAll(attributes);
		request.setID(this.identiferGenerator.generateSAMLID());
		request.setSource(this.OPENID_AUTHN_DELEG_IDENTIFIER);
		request.setPrincipalAuthnIdentifier("unknown");
		request.setIssueInstant(generateXMLCalendar());
		request.setSignature(new Signature());

		try
		{
			document = this.marshaller.marshallSignedElement(request);
		}
		catch (MarshallerException e)
		{
			this.logger.debug("Exception thrown when attempting to marshall the document", e);
			return null;
		}

		this.logger.debug("Successfully marshalled RegisterPrincipalRequest \n" + document);
		return document;
	}

	private RegisterPrincipalResponse validateResponse(Element response)
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

	/**
	 * Generates an XML gregorian calendar instance based on 0 offset UTC current time.
	 *
	 * @return The created calendar for the current UTC time, else null if an error
	 * occurs creating the calendar.
	 */
	private XMLGregorianCalendar generateXMLCalendar()
	{
		GregorianCalendar calendar;

		SimpleTimeZone tz = new SimpleTimeZone(0, ConfigurationConstants.timeZone);
		calendar = new GregorianCalendar(tz);

		try
		{
			DatatypeFactory factory = DatatypeFactory.newInstance();
			return factory.newXMLGregorianCalendar(calendar);
		}
		catch(DatatypeConfigurationException e)
		{
			return null;
		}
	}
}

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
 * Author: Shaun Mangelsdorf / Bradley Beddoes
 * Creation Date: 11/12/2006 / 01/03/2007
 * 
 * Purpose: Implements the PolicyEnforcementProcessor interface.
 */
package com.qut.middleware.spep.pep.impl;

import java.text.MessageFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
import org.w3._2000._09.xmldsig_.Signature;
import org.w3c.dom.Element;

import com.qut.middleware.saml2.StatusCodeConstants;
import com.qut.middleware.saml2.VersionConstants;
import com.qut.middleware.saml2.exception.InvalidSAMLRequestException;
import com.qut.middleware.saml2.exception.MarshallerException;
import com.qut.middleware.saml2.exception.ReferenceValueException;
import com.qut.middleware.saml2.exception.SignatureValueException;
import com.qut.middleware.saml2.exception.UnmarshallerException;
import com.qut.middleware.saml2.handler.Marshaller;
import com.qut.middleware.saml2.handler.Unmarshaller;
import com.qut.middleware.saml2.handler.impl.MarshallerImpl;
import com.qut.middleware.saml2.handler.impl.UnmarshallerImpl;
import com.qut.middleware.saml2.identifier.IdentifierGenerator;
import com.qut.middleware.saml2.schemas.assertion.Assertion;
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.assertion.StatementAbstractType;
import com.qut.middleware.saml2.schemas.assertion.SubjectConfirmation;
import com.qut.middleware.saml2.schemas.assertion.SubjectConfirmationDataType;
import com.qut.middleware.saml2.schemas.esoe.lxacml.AttributeAssignment;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Obligation;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Obligations;
import com.qut.middleware.saml2.schemas.esoe.lxacml.assertion.LXACMLAuthzDecisionStatement;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.Attribute;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.AttributeValue;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.DecisionType;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.Request;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.Resource;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.Result;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.Subject;
import com.qut.middleware.saml2.schemas.esoe.lxacml.grouptarget.GroupTarget;
import com.qut.middleware.saml2.schemas.esoe.lxacml.protocol.LXACMLAuthzDecisionQuery;
import com.qut.middleware.saml2.schemas.esoe.protocol.ClearAuthzCacheRequest;
import com.qut.middleware.saml2.schemas.esoe.protocol.ClearAuthzCacheResponse;
import com.qut.middleware.saml2.schemas.protocol.Extensions;
import com.qut.middleware.saml2.schemas.protocol.Response;
import com.qut.middleware.saml2.schemas.protocol.Status;
import com.qut.middleware.saml2.schemas.protocol.StatusCode;
import com.qut.middleware.saml2.validator.SAMLValidator;
import com.qut.middleware.spep.ConfigurationConstants;
import com.qut.middleware.spep.metadata.KeyStoreResolver;
import com.qut.middleware.spep.metadata.Metadata;
import com.qut.middleware.spep.pep.Messages;
import com.qut.middleware.spep.pep.PolicyEnforcementProcessor;
import com.qut.middleware.spep.pep.SessionGroupCache;
import com.qut.middleware.spep.sessions.PrincipalSession;
import com.qut.middleware.spep.sessions.SessionCache;
import com.qut.middleware.spep.util.CalendarUtils;
import com.qut.middleware.spep.ws.WSClient;
import com.qut.middleware.spep.ws.exception.WSClientException;

/** Implements the PolicyEnforcementProcessor interface. */
public class PolicyEnforcementProcessorImpl implements PolicyEnforcementProcessor
{
	private static String ATTRIBUTE_ID = "lxacmlpdp:obligation:cachetargets:updateusercache"; //$NON-NLS-1$
	private static String OBLIGATION_ID = "lxacmlpdp:obligation:cachetargets"; //$NON-NLS-1$

	private IdentifierGenerator identifierGenerator;
	private Metadata metadata;
	private Marshaller<LXACMLAuthzDecisionQuery> lxacmlAuthzDecisionQueryMarshaller;
	private Unmarshaller<Response> responseUnmarshaller;
	private WSClient wsClient;
	private SessionGroupCache sessionGroupCache;
	private Unmarshaller<ClearAuthzCacheRequest> clearAuthzCacheRequestUnmarshaller;
	private Marshaller<ClearAuthzCacheResponse> clearAuthzCacheResponseMarshaller;
	private SAMLValidator samlValidator;
	private Unmarshaller<GroupTarget> groupTargetUnmarshaller;
	private SessionCache sessionCache;

	private final String UNMAR_PKGNAMES = Response.class.getPackage().getName() + ":" + GroupTarget.class.getPackage().getName() + ":" + LXACMLAuthzDecisionStatement.class.getPackage().getName(); //$NON-NLS-1$
	private final String UNMAR_PKGNAMES2 = ClearAuthzCacheRequest.class.getPackage().getName();
	private final String UNMAR_PKGNAMES3 = GroupTarget.class.getPackage().getName();
	private final String MAR_PKGNAMES = LXACMLAuthzDecisionQuery.class.getPackage().getName();
	private final String MAR_PKGNAMES2 = ClearAuthzCacheRequest.class.getPackage().getName() + ":" + Request.class.getPackage().getName(); //$NON-NLS-1$

	/* Local logging instance */
	private Logger logger = Logger.getLogger(PolicyEnforcementProcessorImpl.class.getName());
	private Logger authzLogger = Logger.getLogger(ConfigurationConstants.authzLogger);

	/**
	 * @param reportingProcessor
	 *            to be used for logging.
	 * @param sessionCache
	 *            to obtain principal session information.
	 * @param sessionGroupCache
	 *            used to obtain policy group target and authz target information when making auth decisions.
	 * @param wsClient
	 *            used for making web service calls.
	 * @param identifierGenerator
	 *            used to generate unique identifiers for use in SAML requests.
	 * @param metadata
	 *            used to obtain SPEP service information.
	 * @param keyStoreResolver
	 *            for resolving public/private keys.
	 * @param samlValidator
	 *            used to validate SAML Requests/Responses.
	 * @throws MarshallerException
	 *             if the marshaller canot be created.
	 * @throws UnmarshallerException
	 *             if the unmarshaller canot be created.
	 */
	public PolicyEnforcementProcessorImpl(SessionCache sessionCache, SessionGroupCache sessionGroupCache, WSClient wsClient, IdentifierGenerator identifierGenerator, Metadata metadata, KeyStoreResolver keyStoreResolver, SAMLValidator samlValidator) throws MarshallerException, UnmarshallerException
	{
		if (sessionCache == null)
		{
			throw new IllegalArgumentException(Messages.getString("PolicyEnforcementProcessorImpl.33")); //$NON-NLS-1$
		}
		if (sessionGroupCache == null)
		{
			throw new IllegalArgumentException(Messages.getString("PolicyEnforcementProcessorImpl.34")); //$NON-NLS-1$
		}
		if (wsClient == null)
		{
			throw new IllegalArgumentException(Messages.getString("PolicyEnforcementProcessorImpl.35")); //$NON-NLS-1$
		}
		if (identifierGenerator == null)
		{
			throw new IllegalArgumentException(Messages.getString("PolicyEnforcementProcessorImpl.36")); //$NON-NLS-1$
		}
		if (metadata == null)
		{
			throw new IllegalArgumentException(Messages.getString("PolicyEnforcementProcessorImpl.37")); //$NON-NLS-1$
		}
		if (keyStoreResolver == null)
		{
			throw new IllegalArgumentException(Messages.getString("PolicyEnforcementProcessorImpl.38")); //$NON-NLS-1$
		}
		if (samlValidator == null)
		{
			throw new IllegalArgumentException(Messages.getString("PolicyEnforcementProcessorImpl.39")); //$NON-NLS-1$
		}

		this.sessionCache = sessionCache;
		this.sessionGroupCache = sessionGroupCache;
		this.wsClient = wsClient;
		this.identifierGenerator = identifierGenerator;
		this.metadata = metadata;
		this.samlValidator = samlValidator;

		String[] authzDecisionSchemas = new String[] { ConfigurationConstants.lxacmlSAMLAssertion, ConfigurationConstants.lxacmlSAMLProtocol, ConfigurationConstants.samlProtocol };
		this.lxacmlAuthzDecisionQueryMarshaller = new MarshallerImpl<LXACMLAuthzDecisionQuery>(this.MAR_PKGNAMES, authzDecisionSchemas, keyStoreResolver.getKeyAlias(), keyStoreResolver.getPrivateKey());
		this.responseUnmarshaller = new UnmarshallerImpl<Response>(this.UNMAR_PKGNAMES, authzDecisionSchemas, this.metadata);

		String[] clearAuthzCacheSchemas = new String[] { ConfigurationConstants.esoeProtocol, ConfigurationConstants.samlAssertion, ConfigurationConstants.samlProtocol };

		// create marshallers/unmarshallers
		this.clearAuthzCacheRequestUnmarshaller = new UnmarshallerImpl<ClearAuthzCacheRequest>(this.UNMAR_PKGNAMES2, clearAuthzCacheSchemas, this.metadata);
		this.clearAuthzCacheResponseMarshaller = new MarshallerImpl<ClearAuthzCacheResponse>(this.MAR_PKGNAMES2, clearAuthzCacheSchemas, keyStoreResolver.getKeyAlias(), keyStoreResolver.getPrivateKey());

		String[] groupTargetSchemas = new String[] { ConfigurationConstants.lxacmlGroupTarget };
		this.groupTargetUnmarshaller = new UnmarshallerImpl<GroupTarget>(this.UNMAR_PKGNAMES3, groupTargetSchemas);

		this.logger.info(Messages.getString("PolicyEnforcementProcessorImpl.40")); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.spep.pep.PolicyEnforcementProcessor#authzCacheClear(java.lang.String)
	 */
	public byte[] authzCacheClear(byte[] requestDocument) throws MarshallerException
	{
		String id = null, statusCodeValue = null, statusMessage = null;
		PrincipalSession principal;

		try
		{
			this.logger.debug(Messages.getString("PolicyEnforcementProcessorImpl.0")); //$NON-NLS-1$
			ClearAuthzCacheRequest clearAuthzCacheRequest = this.clearAuthzCacheRequestUnmarshaller.unMarshallSigned(requestDocument);
			id = clearAuthzCacheRequest.getID();

			this.samlValidator.getRequestValidator().validate(clearAuthzCacheRequest);

			/*
			 * Determine if the ESOE has identifier the principal whose session we are to terminate, if this does not
			 * exist proceed to complete cache clear of all authz decisions
			 */
			if (clearAuthzCacheRequest.getSubject() != null)
			{
				if (clearAuthzCacheRequest.getSubject().getNameID() == null)
				{
					statusCodeValue = StatusCodeConstants.requester;
					statusMessage = Messages.getString("PolicyEnforcementProcessorImpl.48"); //$NON-NLS-1$
					return buildResponse(id, statusMessage, statusCodeValue);
				}

				principal = this.sessionCache.getPrincipalSessionByEsoeSessionID(clearAuthzCacheRequest.getSubject().getNameID().getValue());
				this.sessionGroupCache.clearPrincipalSession(principal);

				this.logger.info(Messages.getString("PolicyEnforcementProcessorImpl.49") + principal.getEsoeSessionID() + Messages.getString("PolicyEnforcementProcessorImpl.50")); //$NON-NLS-1$ //$NON-NLS-2$
				statusCodeValue = StatusCodeConstants.success;

				return buildResponse(id, statusMessage, statusCodeValue);
			}

			this.logger.debug(Messages.getString("PolicyEnforcementProcessorImpl.1")); //$NON-NLS-1$
			Extensions extensions = clearAuthzCacheRequest.getExtensions();

			// GroupTargets are stored in extensions, therefore no extensions, or empty extensions means that the policy
			// set has no policies defined (because the GroupTarget is required in a Policy so it would not be valid
			// according to schema if a Policy existed and had no Targets). Clear the cache and continue. The PEP
			// should not send an Authz query in the case of an empty PolicySet, but fall through to default policy
			// decision.
			Map<String, List<String>> groupTargets = new HashMap<String, List<String>>();

			if (extensions == null || extensions.getAnies() == null)
			{
				this.logger.warn(Messages.getString("PolicyEnforcementProcessorImpl.2"));
			}
			else
			{
				for (Element element : extensions.getAnies())
				{
					if (element.getLocalName().equals(Messages.getString("PolicyEnforcementProcessorImpl.3"))) //$NON-NLS-1$
					{
						GroupTarget groupTarget = this.groupTargetUnmarshaller.unMarshallUnSigned(element);

						String groupTargetID = groupTarget.getGroupTargetID();
						List<String> authzTargets = groupTarget.getAuthzTargets();

						this.logger.debug(MessageFormat.format(Messages.getString("PolicyEnforcementProcessorImpl.4"), new Object[] { groupTargetID, Integer.toString(authzTargets.size()) })); //$NON-NLS-1$

						groupTargets.put(groupTargetID, authzTargets);
					}
				}
			}

			// success response
			this.sessionGroupCache.clearCache(groupTargets);

			this.logger.info(MessageFormat.format(Messages.getString("PolicyEnforcementProcessorImpl.5"), new Object[] { Integer.toString(groupTargets.size()) })); //$NON-NLS-1$
			statusCodeValue = StatusCodeConstants.success;

			return buildResponse(id, statusMessage, statusCodeValue);

		}
		catch (SignatureValueException e)
		{
			statusCodeValue = StatusCodeConstants.requester;
			statusMessage = MessageFormat.format(Messages.getString("PolicyEnforcementProcessorImpl.6"), new Object[] { e.getMessage() }); //$NON-NLS-1$
			this.logger.error(statusMessage);

			return buildResponse(id, statusMessage, statusCodeValue);
		}
		catch (ReferenceValueException e)
		{
			statusCodeValue = StatusCodeConstants.requester;
			statusMessage = MessageFormat.format(Messages.getString("PolicyEnforcementProcessorImpl.7"), new Object[] { e.getMessage() }); //$NON-NLS-1$
			this.logger.error(statusMessage);

			return buildResponse(id, statusMessage, statusCodeValue);
		}
		catch (UnmarshallerException e)
		{
			statusCodeValue = StatusCodeConstants.requester;
			statusMessage = MessageFormat.format(Messages.getString("PolicyEnforcementProcessorImpl.8"), new Object[] { e.getMessage() }); //$NON-NLS-1$
			this.logger.error(statusMessage);

			return buildResponse(id, statusMessage, statusCodeValue);
		}
		catch (InvalidSAMLRequestException e)
		{
			statusCodeValue = StatusCodeConstants.requester;
			statusMessage = MessageFormat.format(Messages.getString("PolicyEnforcementProcessorImpl.9"), new Object[] { e.getMessage() }); //$NON-NLS-1$
			this.logger.error(statusMessage);

			return buildResponse(id, statusMessage, statusCodeValue);
		}
	}

	/*
	 * Builds a ClearAuthzCacheResponse to be sent back to ESOE when a cacheClear request has been recieved.
	 * 
	 */
	private byte[] buildResponse(String inResponseTo, String statusMessage, String statusCodeValue) throws MarshallerException
	{
		byte[] responseDocument = null;
		ClearAuthzCacheResponse clearAuthzCacheResponse = null;

		NameIDType issuer = new NameIDType();
		issuer.setValue(this.metadata.getSPEPIdentifier());

		Status status = new Status();
		StatusCode statusCode = new StatusCode();
		statusCode.setValue(statusCodeValue);
		status.setStatusCode(statusCode);
		status.setStatusMessage(statusMessage);

		clearAuthzCacheResponse = new ClearAuthzCacheResponse();
		clearAuthzCacheResponse.setInResponseTo(inResponseTo);
		clearAuthzCacheResponse.setID(this.identifierGenerator.generateSAMLID());
		clearAuthzCacheResponse.setIssueInstant(CalendarUtils.generateXMLCalendar());
		clearAuthzCacheResponse.setIssuer(issuer);
		clearAuthzCacheResponse.setSignature(new Signature());
		clearAuthzCacheResponse.setVersion(VersionConstants.saml20);
		clearAuthzCacheResponse.setStatus(status);

		this.logger.debug(Messages.getString("PolicyEnforcementProcessorImpl.10")); //$NON-NLS-1$

		responseDocument = this.clearAuthzCacheResponseMarshaller.marshallSigned(clearAuthzCacheResponse);

		return responseDocument;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.spep.pep.PolicyEnforcementProcessor#makeAuthzDecision(java.lang.String, java.lang.String)
	 */
	public decision makeAuthzDecision(String sessionID, String resource)
	{
		return makeAuthzDecision(sessionID, resource, null);
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.spep.pep.PolicyEnforcementProcessor#makeAuthzDecision(java.lang.String, java.lang.String)
	 */
	public decision makeAuthzDecision(String sessionID, String resource, String action)
	{
		PrincipalSession principalSession = this.sessionCache.getPrincipalSession(sessionID);
		if (principalSession == null)
			return decision.error;

		return makeAuthzDecision(principalSession, resource, action);
	}

	/*
	 * Make an authorization decision for the requested resource based on cached authz group targets.
	 * 
	 */
	private decision makeAuthzDecision(PrincipalSession principalSession, String resource, String action)
	{
		// Evaluate from cache
		decision policyDecision = this.sessionGroupCache.makeCachedAuthzDecision(principalSession, resource, action);

		// If the cache gave an authoritative answer return it.
		if (policyDecision.equals(decision.permit) || policyDecision.equals(decision.deny))
		{
			return policyDecision;
		}

		// Need more information. Query the PDP.
		if (policyDecision.equals(decision.notcached))
		{
			byte[] decisionRequest;
			try
			{
				// Generate a query based on the session and resource being requested.
				decisionRequest = generateAuthzDecisionQuery(principalSession, resource);
			}
			catch (MarshallerException e)
			{
				this.logger.error(MessageFormat.format(Messages.getString("PolicyEnforcementProcessorImpl.11"), new Object[] { e.getMessage() })); //$NON-NLS-1$
				return decision.error;
			}

			// Make the web service call.. could be a lengthy process
			String endpoint = this.metadata.getAuthzServiceEndpoint();
			byte[] responseDocument;
			try
			{
				responseDocument = this.wsClient.policyDecisionPoint(decisionRequest, endpoint);
			}
			catch (WSClientException e)
			{
				this.logger.error(MessageFormat.format(Messages.getString("PolicyEnforcementProcessorImpl.12"), new Object[] { e.getMessage() })); //$NON-NLS-1$
				return decision.error;
			}

			try
			{
				policyDecision = processAuthzDecisionStatement(principalSession, responseDocument, resource, action);
			}
			catch (SignatureValueException e)
			{
				this.logger.error(MessageFormat.format(Messages.getString("PolicyEnforcementProcessorImpl.13"), new Object[] { e.getMessage() })); //$NON-NLS-1$
				return decision.error;
			}
			catch (ReferenceValueException e)
			{
				this.logger.error(MessageFormat.format(Messages.getString("PolicyEnforcementProcessorImpl.14"), new Object[] { e.getMessage() })); //$NON-NLS-1$
				return decision.error;
			}
			catch (UnmarshallerException e)
			{
				this.logger.error(MessageFormat.format(Messages.getString("PolicyEnforcementProcessorImpl.15"), new Object[] { e.getMessage() })); //$NON-NLS-1$
				return decision.error;
			}

			if (policyDecision.equals(decision.permit) || policyDecision.equals(decision.deny))
			{
				return policyDecision;
			}
		}

		// If policyDecision is "error" then we have already logged the error, otherwise log a generic message.
		if (!policyDecision.equals(decision.error))
		{
			this.logger.error(Messages.getString("PolicyEnforcementProcessorImpl.16")); //$NON-NLS-1$
		}

		return decision.error;
	}

	private decision processAuthzDecisionStatement(PrincipalSession principalSession, byte[] responseDocument, String resource, String action) throws SignatureValueException, ReferenceValueException, UnmarshallerException
	{
		decision policyDecision = null;

		this.logger.debug(Messages.getString("PolicyEnforcementProcessorImpl.18")); //$NON-NLS-1$
		this.logger.debug(responseDocument);

		Response response = this.responseUnmarshaller.unMarshallSigned(responseDocument);

		// if authz failed then the likely scenario is that the principals session has been deleted from
		// the ESOE. In any case we want to purge the client session to force them back to authn
		if (response.getStatus().getStatusCode().getValue().equals(StatusCodeConstants.authnFailed))
		{
			this.authzLogger.error(MessageFormat.format(Messages.getString("PolicyEnforcementProcessorImpl.46"), principalSession.getEsoeSessionID())); //$NON-NLS-1$
			this.sessionCache.terminatePrincipalSession(principalSession);
			return decision.deny;
		}

		// Find all assertions in the response.
		for (Object encryptedAssertionOrAssertion : response.getEncryptedAssertionsAndAssertions())
		{
			if (encryptedAssertionOrAssertion instanceof Assertion)
			{
				this.logger.debug(Messages.getString("PolicyEnforcementProcessorImpl.19")); //$NON-NLS-1$

				Assertion assertion = (Assertion) encryptedAssertionOrAssertion;

				// ensure parameters are present. not the prettiest way to do it but it avoids
				// namespace clashes with Subject
				if (assertion.getSubject() == null)
				{
					this.logger.error(Messages.getString("PolicyEnforcementProcessorImpl.41")); //$NON-NLS-1$
					return decision.deny;
				}

				if (assertion.getSubject().getSubjectConfirmationNonID() == null)
				{
					this.logger.error(Messages.getString("PolicyEnforcementProcessorImpl.42")); //$NON-NLS-1$
					return decision.deny;
				}

				// verify SubjectConfirmationData fields
				List<SubjectConfirmation> subjectConfirmations = assertion.getSubject().getSubjectConfirmationNonID();
				if (subjectConfirmations.size() == 0)
				{
					this.logger.error(Messages.getString("PolicyEnforcementProcessorImpl.43")); //$NON-NLS-1$
					policyDecision = decision.deny;
				}

				for (SubjectConfirmation confirmation : subjectConfirmations)
				{
					SubjectConfirmationDataType confirmationData = confirmation.getSubjectConfirmationData();

					if (confirmationData == null)
					{
						this.logger.error(Messages.getString("PolicyEnforcementProcessorImpl.44")); //$NON-NLS-1$
						policyDecision = decision.deny;
					}

					// validate data has not expired
					XMLGregorianCalendar xmlCalendar = confirmationData.getNotOnOrAfter();
					GregorianCalendar notOnOrAfterCal = xmlCalendar.toGregorianCalendar();

					XMLGregorianCalendar thisXmlCal = CalendarUtils.generateXMLCalendar();
					GregorianCalendar thisCal = thisXmlCal.toGregorianCalendar();

					if (thisCal.after(notOnOrAfterCal))
					{
						this.logger.debug(MessageFormat.format(Messages.getString("PolicyEnforcementProcessorImpl.47"), thisCal.getTimeInMillis(), notOnOrAfterCal.getTimeInMillis())); //$NON-NLS-1$
						// request is out of date
						this.logger.error(Messages.getString("PolicyEnforcementProcessorImpl.45")); //$NON-NLS-1$
						policyDecision = decision.deny;
					}

				}

				// Check all statements in the assertion, and find LXACMLAuthzDecisionStatements
				for (StatementAbstractType statement : assertion.getAuthnStatementsAndAuthzDecisionStatementsAndAttributeStatements())
				{
					if (statement instanceof LXACMLAuthzDecisionStatement)
					{
						LXACMLAuthzDecisionStatement lxacmlAuthzDecisionStatement = (LXACMLAuthzDecisionStatement) statement;

						this.logger.debug(Messages.getString("PolicyEnforcementProcessorImpl.20")); //$NON-NLS-1$

						com.qut.middleware.saml2.schemas.esoe.lxacml.context.Response lxacmlResponse = lxacmlAuthzDecisionStatement.getResponse();
						Result result = lxacmlResponse.getResult();

						// Find out if the decision was a permit or a deny and set the policy decision appropriately
						DecisionType lxacmlDecision = result.getDecision();
						if (lxacmlDecision.equals(DecisionType.DENY))
						{
							this.authzLogger.info(MessageFormat.format(Messages.getString("PolicyEnforcementProcessorImpl.21"), new Object[] { principalSession.getEsoeSessionID(), resource })); //$NON-NLS-1$
							policyDecision = decision.deny;
						}
						else
							if (lxacmlDecision.equals(DecisionType.PERMIT))
							{
								this.authzLogger.info(MessageFormat.format(Messages.getString("PolicyEnforcementProcessorImpl.22"), new Object[] { principalSession.getEsoeSessionID(), resource })); //$NON-NLS-1$
								policyDecision = decision.permit;
							}
							else
							{
								this.authzLogger.info(MessageFormat.format(Messages.getString("PolicyEnforcementProcessorImpl.23"), new Object[] { principalSession.getEsoeSessionID(), resource })); //$NON-NLS-1$
								policyDecision = decision.error;
							}

						processObligations(principalSession, result.getObligations(), policyDecision, resource, action);

						return policyDecision;
					}
				}
			}
		}

		return decision.error;
	}

	private void processObligations(PrincipalSession principalSession, Obligations obligations, decision decision, String resource, String action)
	{
		this.logger.info(MessageFormat.format(Messages.getString("PolicyEnforcementProcessorImpl.24"), new Object[] { decision.value(), principalSession.getEsoeSessionID(), resource, action })); //$NON-NLS-1$

		for (Obligation obligation : obligations.getObligations())
		{
			// Make sure that the <Obligation> is the kind that we expect
			if (OBLIGATION_ID.equals(obligation.getObligationId()))
			{
				// We're working here based on the fact that decision.value() is defined the same as EffectType.value()
				if (decision.value().equalsIgnoreCase(obligation.getFulfillOn().value()))
				{
					for (AttributeAssignment attributeAssignment : obligation.getAttributeAssignments())
					{
						// Make sure we have the right <AttributeAssignment>
						if (ATTRIBUTE_ID.equals(attributeAssignment.getAttributeId()))
						{
							for (Object content : attributeAssignment.getContent())
							{
								if (content instanceof GroupTarget)
								{
									GroupTarget groupTarget = (GroupTarget) content;

									String groupTargetID = groupTarget.getGroupTargetID();
									this.logger.debug(MessageFormat.format(Messages.getString("PolicyEnforcementProcessorImpl.25"), new Object[] { Integer.toString(groupTarget.getAuthzTargets().size()), principalSession.getEsoeSessionID(), groupTargetID })); //$NON-NLS-1$
									this.sessionGroupCache.updateCache(principalSession, groupTargetID, groupTarget.getAuthzTargets(), action, decision);
								}
								else
									if (content instanceof String)
									{
										this.logger.debug(MessageFormat.format(Messages.getString("PolicyEnforcementProcessorImpl.26"), new Object[] { content })); //$NON-NLS-1$
									}
									else
									{
										this.logger.debug(MessageFormat.format(Messages.getString("PolicyEnforcementProcessorImpl.27"), new Object[] { content.getClass().getName() })); //$NON-NLS-1$
									}
							}
						}
						else
						{
							this.logger.debug(MessageFormat.format(Messages.getString("PolicyEnforcementProcessorImpl.28"), new Object[] { attributeAssignment.getAttributeId() })); //$NON-NLS-1$
						}
					}
				}
			}
			else
			{
				this.logger.debug(MessageFormat.format(Messages.getString("PolicyEnforcementProcessorImpl.29"), new Object[] { obligation.getObligationId() })); //$NON-NLS-1$
			}
		}
	}

	private byte[] generateAuthzDecisionQuery(PrincipalSession principalSession, String resourceString) throws MarshallerException
	{
		this.logger.debug(Messages.getString("PolicyEnforcementProcessorImpl.30")); //$NON-NLS-1$
		byte[] requestDocument = null;
		String esoeSessionIndex = principalSession.getEsoeSessionID();

		// The resource being accessed by the client
		Resource resource = new Resource();
		Attribute resourceAttribute = new Attribute();
		AttributeValue resourceAttributeValue = new AttributeValue();
		resourceAttributeValue.getContent().add(resourceString);
		resourceAttribute.setAttributeValue(resourceAttributeValue);
		resource.setAttribute(resourceAttribute);

		// Set the subject of the query..
		Subject subject = new Subject();
		Attribute subjectAttribute = new Attribute();
		AttributeValue subjectAttributeValue = new AttributeValue();
		subjectAttributeValue.getContent().add(esoeSessionIndex); // .. to the session
		subjectAttribute.setAttributeValue(subjectAttributeValue);
		subject.setAttribute(subjectAttribute);

		Request request = new Request();
		request.setResource(resource);
		request.setSubject(subject);

		// SPEP <Issuer> tag
		NameIDType issuer = new NameIDType();
		issuer.setValue(this.metadata.getSPEPIdentifier());

		// The actual authz query.
		LXACMLAuthzDecisionQuery lxacmlAuthzDecisionQuery = new LXACMLAuthzDecisionQuery();
		lxacmlAuthzDecisionQuery.setRequest(request);
		lxacmlAuthzDecisionQuery.setID(this.identifierGenerator.generateSAMLID());
		lxacmlAuthzDecisionQuery.setIssueInstant(CalendarUtils.generateXMLCalendar());
		lxacmlAuthzDecisionQuery.setVersion(VersionConstants.saml20);
		lxacmlAuthzDecisionQuery.setIssuer(issuer);
		lxacmlAuthzDecisionQuery.setSignature(new Signature());

		this.logger.debug(Messages.getString("PolicyEnforcementProcessorImpl.31")); //$NON-NLS-1$
		requestDocument = this.lxacmlAuthzDecisionQueryMarshaller.marshallSigned(lxacmlAuthzDecisionQuery);

		return requestDocument;
	}
}

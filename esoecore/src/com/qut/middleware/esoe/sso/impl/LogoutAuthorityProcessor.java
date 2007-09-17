/* Copyright 2006, Queensland University of Technology
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
 * Author: Andre Zitelli
 * Creation Date: 14/12/2006
 * 
 * Purpose: Performs the logic to Logout SSO sessions.
 */
package com.qut.middleware.esoe.sso.impl;

import java.text.MessageFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.xml.bind.JAXBElement;

import org.apache.log4j.Logger;
import org.w3._2000._09.xmldsig_.Signature;

import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.crypto.KeyStoreResolver;
import com.qut.middleware.esoe.metadata.Metadata;
import com.qut.middleware.esoe.metadata.exception.InvalidMetadataEndpointException;
import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.SessionsProcessor;
import com.qut.middleware.esoe.sessions.Terminate;
import com.qut.middleware.esoe.sessions.exception.InvalidDescriptorIdentifierException;
import com.qut.middleware.esoe.sso.SSOProcessor;
import com.qut.middleware.esoe.sso.bean.FailedLogout;
import com.qut.middleware.esoe.sso.bean.FailedLogoutRepository;
import com.qut.middleware.esoe.sso.bean.SSOLogoutState;
import com.qut.middleware.esoe.sso.bean.SSOProcessorData;
import com.qut.middleware.esoe.sso.bean.impl.FailedLogoutImpl;
import com.qut.middleware.esoe.sso.bean.impl.SSOLogoutStateImpl;
import com.qut.middleware.esoe.sso.exception.InvalidRequestException;
import com.qut.middleware.esoe.sso.exception.InvalidSessionIdentifierException;
import com.qut.middleware.esoe.util.CalendarUtils;
import com.qut.middleware.esoe.ws.WSClient;
import com.qut.middleware.esoe.ws.exception.WSClientException;
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
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.protocol.LogoutRequest;
import com.qut.middleware.saml2.schemas.protocol.StatusResponseType;
import com.qut.middleware.saml2.validator.SAMLValidator;

/** Performs the logic to Logout SSO sessions. */

public class LogoutAuthorityProcessor implements SSOProcessor 
{	
	private SAMLValidator samlValidator;
	private SessionsProcessor sessionsProcessor;
	private Metadata metadata;	
	private WSClient wsClient;
	private KeyStoreResolver keyStoreResolver;
	private IdentifierGenerator identifierGenerator;
	
	private Marshaller<LogoutRequest> marshaller;
	private Unmarshaller<JAXBElement<StatusResponseType>> unmarshaller;
	
	private String charset = "UTF-16";
	
	private FailedLogoutRepository logoutFailures;
	
	private final static String LOGOUT_REASON = com.qut.middleware.esoe.sso.impl.Messages.getString("LogoutAuthorityProcessor.0"); //$NON-NLS-1$
	
	private String[] schemas = new String[] { ConfigurationConstants.samlProtocol };
	
	private final String UNMAR_PKGNAMES = StatusResponseType.class.getPackage().getName();
	private final String MAR_PKGNAMES = LogoutRequest.class.getPackage().getName();
	
	/* Local logging instance */
	private Logger logger = Logger.getLogger(LogoutAuthorityProcessor.class.getName());

	/** 
	 * 
	 * @param logoutFailures The repository to be used for recording FailedLogouts
	 * @param samlValidator Used to validate SAML responses
	 * @param sessionsProcessor Used to manipulate user SSO Sessions
	 * @param wsClient For sending LogoutRequests to associated SPEP endpoints.
	 * @param metadata For resolving SPEP endpoints.
	 */
	public LogoutAuthorityProcessor(FailedLogoutRepository logoutFailures, SAMLValidator samlValidator,
			SessionsProcessor sessionsProcessor, Metadata metadata, IdentifierGenerator identifierGenerator,  KeyStoreResolver keyStoreResolver, WSClient wsClient) 
	throws MarshallerException, UnmarshallerException
	{
		if(logoutFailures == null)
			throw new IllegalArgumentException(com.qut.middleware.esoe.sso.impl.Messages.getString("LogoutAuthorityProcessor.1")); //$NON-NLS-1$
		
		if(samlValidator == null)
			throw new IllegalArgumentException(com.qut.middleware.esoe.sso.impl.Messages.getString("LogoutAuthorityProcessor.2")); //$NON-NLS-1$
		
		if (sessionsProcessor == null)
			throw new IllegalArgumentException(com.qut.middleware.esoe.sso.impl.Messages.getString("LogoutAuthorityProcessor.3")); //$NON-NLS-1$

		if (metadata == null)
			throw new IllegalArgumentException(com.qut.middleware.esoe.sso.impl.Messages.getString("LogoutAuthorityProcessor.4"));  //$NON-NLS-1$
		
		if (identifierGenerator == null)
			throw new IllegalArgumentException(Messages.getString("LogoutAuthorityProcessor.28"));  //$NON-NLS-1$
		
		if (wsClient == null)
			throw new IllegalArgumentException(com.qut.middleware.esoe.sso.impl.Messages.getString("LogoutAuthorityProcessor.5")); //$NON-NLS-1$
		
		if (keyStoreResolver == null)
			throw new IllegalArgumentException(com.qut.middleware.esoe.sso.impl.Messages.getString("LogoutAuthorityProcessor.6")); //$NON-NLS-1$
		
		this.logoutFailures = logoutFailures;
		this.samlValidator = samlValidator;
		this.sessionsProcessor = sessionsProcessor;
		this.metadata = metadata;	
		this.wsClient = wsClient;
		this.keyStoreResolver = keyStoreResolver;
		this.identifierGenerator = identifierGenerator;
		
		this.marshaller = new MarshallerImpl<LogoutRequest>(this.MAR_PKGNAMES, this.schemas, this.keyStoreResolver.getKeyAlias(), this.keyStoreResolver.getPrivateKey());
		this.unmarshaller = new UnmarshallerImpl<JAXBElement<StatusResponseType>>(this.UNMAR_PKGNAMES, this.schemas, this.metadata);
		
		this.logger.info(com.qut.middleware.esoe.sso.impl.Messages.getString("LogoutAuthorityProcessor.7")); //$NON-NLS-1$		
	}
	
	
	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.sso.SSOProcessor#execute(com.qut.middleware.esoe.sso.bean.SSOProcessorData)
	 */
	public result execute(SSOProcessorData data) throws InvalidSessionIdentifierException, InvalidRequestException
	{
		if(data == null)
			throw new IllegalArgumentException(com.qut.middleware.esoe.sso.impl.Messages.getString("LogoutAuthorityProcessor.8"));  //$NON-NLS-1$
		
		Principal principal = null;
		List<SSOLogoutState> logoutStates = new Vector<SSOLogoutState>();
		
		try
		{
			principal = this.sessionsProcessor.getQuery().queryAuthnSession(data.getSessionID());	
			
			if( data.getSessionID() == null || !data.getSessionID().equals(principal.getSessionID()) )
			{
				throw new InvalidSessionIdentifierException(com.qut.middleware.esoe.sso.impl.Messages.getString("LogoutAuthorityProcessor.9")); //$NON-NLS-1$
			}			
				
			this.logger.debug(Messages.getString("LogoutAuthorityProcessor.25")); //$NON-NLS-1$
			
			// obtain active entities (SPEPS logged into) for user, iterate through and send logout request to each SPEP
			List<String> activeDescriptors = principal.getActiveDescriptors();
			if(activeDescriptors != null)
			{
				Iterator<String> entitiesIterator = activeDescriptors.iterator();
				while(entitiesIterator.hasNext())
				{
					String entity = entitiesIterator.next();
					
					// resolve all enpoints for the given entity and send logout request
					List<String> endPoints = new Vector<String>();
					
					endPoints = this.metadata.resolveSingleLogoutService(entity);
					
					Iterator<String> endpointIter = endPoints.iterator();
					while (endpointIter.hasNext())
					{
						String endPoint = endpointIter.next();
						
						List<String> indicies = null;
						try
						{
							indicies = principal.getDescriptorSessionIdentifiers(entity);
						}
						catch(InvalidDescriptorIdentifierException e)
						{
							this.logger.warn(com.qut.middleware.esoe.sso.impl.Messages.getString("LogoutAuthorityProcessor.10")); //$NON-NLS-1$
						}
						
						// generate logout request string and send it
						byte[] request = this.generateLogoutRequest(principal.getSAMLAuthnIdentifier(), LOGOUT_REASON, indicies);
						
						result result = this.sendLogoutRequest(request, endPoint);
						
						// store the state of the logout request for reporting if required
						SSOLogoutState logoutState = new SSOLogoutStateImpl();
						logoutState.setSPEPURL(entity);
						
						if(result == SSOProcessor.result.LogoutSuccessful)
						{
							logoutState.setLogoutState(true);
							logoutState.setLogoutStateDescription(com.qut.middleware.esoe.sso.impl.Messages.getString("LogoutAuthorityProcessor.11")); //$NON-NLS-1$
						}
						else
						{
							logoutState.setLogoutState(false);
							logoutState.setLogoutStateDescription(com.qut.middleware.esoe.sso.impl.Messages.getString("LogoutAuthorityProcessor.12"));						 //$NON-NLS-1$
						}
						
						logoutStates.add(logoutState);
					}
				}
			}		
			else
				this.logger.debug(Messages.getString("LogoutAuthorityProcessor.26")); //$NON-NLS-1$
		}
		catch (com.qut.middleware.esoe.sessions.exception.InvalidSessionIdentifierException e)
		{
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new InvalidSessionIdentifierException(com.qut.middleware.esoe.sso.impl.Messages.getString("LogoutAuthorityProcessor.13")); //$NON-NLS-1$
		}
		catch(InvalidMetadataEndpointException e)
		{
			this.logger.debug(e.getLocalizedMessage(), e);
		}
		catch(MarshallerException e)
		{
			this.logger.error(com.qut.middleware.esoe.sso.impl.Messages.getString("LogoutAuthorityProcessor.14") + data.getSessionID() ); //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);			
		}
		

		// set the state of logout attempts for all user active SSO entities
		data.setLogoutStates(logoutStates);
		
		// no matter the outcome of sent LogoutRequests, we will terminate the users SSO session
		this.logger.info(com.qut.middleware.esoe.sso.impl.Messages.getString("LogoutAuthorityProcessor.15") + principal.getPrincipalAuthnIdentifier()); //$NON-NLS-1$
		
		Terminate terminate = this.sessionsProcessor.getTerminate();
		
		try
		{
			terminate.terminateSession(data.getSessionID());
		}
		// this should never happen as we validated the sessionID above
		catch(com.qut.middleware.esoe.sessions.exception.InvalidSessionIdentifierException e)
		{
			this.logger.error(com.qut.middleware.esoe.sso.impl.Messages.getString("LogoutAuthorityProcessor.16") + data.getSessionID()); //$NON-NLS-1$
		}
		
		return result.LogoutSuccessful;
		
	}
	
	
	/*
	 * Send an LogoutRequest request to the specified SPEP endpoint. If the request can not be delivered
	 * for any reason, a LogoutFailure is added to the respository of failures for later delivery.
	 * 
	 * @param logoutRequest The xml logout request.
	 * @param endPoint The endpoint to send to.
	 * @return the result of the operation.
	 */
	private result sendLogoutRequest(byte[] logoutRequest, String endPoint)
	{
		byte[] responseDocument;

		try
		{			
			this.logger.debug(MessageFormat.format(Messages.getString("LogoutAuthorityProcessor.29"),  endPoint));  //$NON-NLS-1$
			
			responseDocument = this.wsClient.singleLogout(logoutRequest, endPoint);
			
			StatusResponseType logoutResponse = (this.unmarshaller.unMarshallSigned(responseDocument)).getValue();

			this.samlValidator.getResponseValidator().validate(logoutResponse);
			
			this.logger.debug(MessageFormat.format(Messages.getString("LogoutAuthorityProcessor.27"), endPoint) ); //$NON-NLS-1$
			
			// no need to do any further checking. If the response is not success it can only
			// mean the SPEP knows nothing about the principal, in which case there's no point
			// resending it. So we assume success if the SPEP sends a valid Response.
			return result.LogoutSuccessful;
			
		}		
		// any failures in validating the Logout Response must result in failure and we add them
		// to the failure repository so we can attempt to resend them
		catch (UnmarshallerException e)
		{
			this.logger.error(com.qut.middleware.esoe.sso.impl.Messages.getString("LogoutAuthorityProcessor.18"));  //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);
			
			this.recordFailure(logoutRequest, endPoint);
			
			return result.LogoutRequestFailed;
		}
		catch (SignatureValueException e)
		{
			this.logger.error(com.qut.middleware.esoe.sso.impl.Messages.getString("LogoutAuthorityProcessor.19") + endPoint);  //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);

			this.recordFailure(logoutRequest, endPoint);
			
			return result.LogoutRequestFailed;
		}
		catch (ReferenceValueException e)
		{
			this.logger.error(com.qut.middleware.esoe.sso.impl.Messages.getString("LogoutAuthorityProcessor.20") + endPoint);  //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);

			this.recordFailure(logoutRequest, endPoint);
			
			return result.LogoutRequestFailed;
		}
		catch(InvalidSAMLResponseException e)
		{
			this.logger.error(com.qut.middleware.esoe.sso.impl.Messages.getString("LogoutAuthorityProcessor.21"));  //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);			
		
			this.recordFailure(logoutRequest, endPoint);
		
			return result.LogoutRequestFailed;
		}
		catch (WSClientException e)
		{
			this.logger.error(com.qut.middleware.esoe.sso.impl.Messages.getString("LogoutAuthorityProcessor.22") + endPoint);  //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);
			
			this.recordFailure(logoutRequest, endPoint);

			return result.LogoutRequestFailed;
		}
	}
	
	
	/*
	 * Creates the logout request to send to an endpoint. 
	 * 
	 * @param samlAuthnID The ID of the user making the logout request.
	 * @param reason The reason for the logout.
	 * @param sessionIndicies A list of session Indexes associated with the principal.
	 * @return The string representation of the xml request object generated by this method.
	 */
	private byte[] generateLogoutRequest(String samlAuthnID, String reason, List<String> sessionIndicies) throws MarshallerException
	{
		byte[] requestDocument = null;
		LogoutRequest request = new LogoutRequest();
		
		NameIDType subject = new NameIDType();
		NameIDType issuer = new NameIDType();
		subject.setValue(samlAuthnID);
		request.setNameID(subject);
		request.setID(this.identifierGenerator.generateSAMLID());
		request.setReason(reason);
		request.setVersion(VersionConstants.saml20);
		issuer.setValue(this.metadata.getEsoeEntityID());
		request.setIssuer(issuer);
		
		// Timestamps MUST be set to UTC, no offset
		request.setIssueInstant(CalendarUtils.generateXMLCalendar());
		
		request.setSignature(new Signature());
		
		if(sessionIndicies != null)
		{
			Iterator<String> iterator = sessionIndicies.iterator();
			while(iterator.hasNext())
			{
				String sessionIndex = iterator.next();
				if(sessionIndex != null)
					request.getSessionIndices().add(sessionIndex);
			}
			
		}
		
		requestDocument = this.marshaller.marshallSigned(request);
	
		return requestDocument;
	}
	
	
	/*
	 * Add the failure record to the logout failure repository.
	 *  
	 * @param request The request that failed to deliver. @param endPoint The end point the the request failed to
	 * deliver to.
	 */
	private synchronized void recordFailure(byte[] request, String endPoint)
	{		
		// create an UpdateFailure object
		FailedLogout failure = new FailedLogoutImpl();
		
		failure.setEndPoint(endPoint);
		failure.setRequestDocument(request);
		failure.setTimeStamp(new Date());
		
		// add it to failure repository
		this.logoutFailures.add(failure);

		this.logger.info(com.qut.middleware.esoe.sso.impl.Messages.getString("LogoutAuthorityProcessor.23") + endPoint ); //$NON-NLS-1$ 
		
		this.logger.trace(request);	
	}
}

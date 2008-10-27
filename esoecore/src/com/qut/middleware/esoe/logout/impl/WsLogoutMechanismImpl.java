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
 * Creation Date: 20/03/2008
 */

package com.qut.middleware.esoe.logout.impl;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.xml.bind.JAXBElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3._2000._09.xmldsig_.Signature;
import org.w3c.dom.Element;

import com.qut.middleware.crypto.KeystoreResolver;
import com.qut.middleware.esoe.logout.LogoutMechanism;
import com.qut.middleware.esoe.logout.bean.FailedLogout;
import com.qut.middleware.esoe.logout.bean.FailedLogoutRepository;
import com.qut.middleware.esoe.logout.bean.impl.FailedLogoutImpl;
import com.qut.middleware.esoe.util.CalendarUtils;
import com.qut.middleware.esoe.ws.WSClient;
import com.qut.middleware.esoe.ws.exception.WSClientException;
import com.qut.middleware.metadata.bean.EntityData;
import com.qut.middleware.metadata.bean.saml.SPEPRole;
import com.qut.middleware.metadata.bean.saml.endpoint.Endpoint;
import com.qut.middleware.metadata.exception.MetadataStateException;
import com.qut.middleware.metadata.processor.MetadataProcessor;
import com.qut.middleware.saml2.BindingConstants;
import com.qut.middleware.saml2.SchemaConstants;
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
import com.qut.middleware.saml2.schemas.protocol.Response;
import com.qut.middleware.saml2.schemas.protocol.StatusResponseType;
import com.qut.middleware.saml2.validator.SAMLValidator;

/**
 *  An implementation of LogoutMechanism with a web service client as the underlying transport mechanism.
 */
public class WsLogoutMechanismImpl implements LogoutMechanism 
{
	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	private KeystoreResolver keyStoreResolver;
	private WSClient wsClient;
	private SAMLValidator samlValidator;
	private IdentifierGenerator identifierGenerator;
	private MetadataProcessor metadata;	
	private FailedLogoutRepository logoutFailures;
	
	private Marshaller<LogoutRequest> logoutRequestMarshaller;
	private Unmarshaller<JAXBElement<StatusResponseType>> logoutResponseUnmarshaller;
		
	private String[] schemas = new String[] { SchemaConstants.samlProtocol };
		
	private final String UNMAR_PKGNAMES = Response.class.getPackage().getName();
	private final String MAR_PKGNAMES = LogoutRequest.class.getPackage().getName();
	private final String IMPLEMENTED_BINDING = BindingConstants.soap;

	private String esoeIdentifier;
	
	private final static String LOGOUT_REASON = Messages.getString("LogoutProcessor.0"); //$NON-NLS-1$
		
	public WsLogoutMechanismImpl(WSClient wsClient, MetadataProcessor metadata, IdentifierGenerator identifierGenerator, 
			KeystoreResolver keystoreResolver, SAMLValidator samlValidator, FailedLogoutRepository logoutFailures, String esoeIdentifier)
	throws MarshallerException, UnmarshallerException
	{
		if (wsClient == null)
			throw new IllegalArgumentException("Supplied wsClient implementation MUST NOT be null.");  //$NON-NLS-1$

		if (metadata == null)
			throw new IllegalArgumentException("Supplied samlValidator implementation MUST NOT be null.");  //$NON-NLS-1$
		
		if (identifierGenerator == null)
			throw new IllegalArgumentException("Supplied identifierGenerator implementation MUST NOT be null.");  //$NON-NLS-1$
		
		if (keystoreResolver == null)
			throw new IllegalArgumentException("Supplied keystoreResolver implementation MUST NOT be null.");	
		
		if(samlValidator == null)
			throw new IllegalArgumentException("Supplied SAML validator  MUST NOT be null !");
		
		if(logoutFailures == null)
			throw new IllegalArgumentException("Param logoutFailures MUST NOT be null.");
		
		if(esoeIdentifier == null)
			throw new IllegalArgumentException("Supplied ESOE identifier MUST NOT be null");
		
		this.metadata = metadata;		
		this.wsClient = wsClient;
		this.identifierGenerator = identifierGenerator;
		this.keyStoreResolver = keystoreResolver;
		this.samlValidator = samlValidator;
		this.logoutFailures = logoutFailures;
		this.esoeIdentifier = esoeIdentifier;
		
		this.logoutRequestMarshaller = new MarshallerImpl<LogoutRequest>(this.MAR_PKGNAMES, this.schemas, this.keyStoreResolver);
		this.logoutResponseUnmarshaller = new UnmarshallerImpl<JAXBElement<StatusResponseType>>(this.UNMAR_PKGNAMES, this.schemas, this.metadata);
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoe.logout.LogoutMechanism#performSingleLogout(java.lang.String[], java.lang.String)
	 */
	public result performSingleLogout(String samlAuthnID, List<String> sessionIDs, String endpoint, boolean storeFailedLogout) 
	{
		LogoutMechanism.result result = null;
		
		try
		{
			Element logoutRequest = this.generateLogoutRequest(samlAuthnID, sessionIDs);
			
			return this.sendLogoutRequest(logoutRequest, endpoint, samlAuthnID, storeFailedLogout);
		}		
		catch(MarshallerException e)
		{
			result = LogoutMechanism.result.LogoutRequestFailed;
		}
		
		return result;
	}

		
	/* Send an Logout Request Request to the specified SPEP endpoint.
	 * 
	 * @param logoutRequest The LogoutRequest to send to the specified endpoint.
	 * @param endPoint The endpoint to send the logout request to.
	 * @param The SAML session identifier of the user being logged out. For recording of failures in this context.
	 * @return The result of the operation. Either LogoutSuccessfull or FailedLogout
	 */
	private LogoutMechanism.result sendLogoutRequest(Element logoutRequest, String endPoint, String samlAuthnID, boolean storeFailedLogout)
	{
		Element responseDocument;

		try
		{
			// attempt to send logout request
			responseDocument = this.wsClient.singleLogout(logoutRequest, endPoint);
			
			// validate the response from the attempted target
			StatusResponseType logoutResponse = (this.logoutResponseUnmarshaller.unMarshallSigned(responseDocument)).getValue();

			this.samlValidator.getResponseValidator().validate(logoutResponse);
			
			this.logger.debug("Recieved Response from endpoint. Status is: " + logoutResponse.getStatus().getStatusMessage());
			
			// TODO check response for errors
			return LogoutMechanism.result.LogoutSuccessful;
		}
		catch (SignatureValueException e)
		{
			this.logger.error(Messages.getString("FailedLogoutMonitor.6"));  //$NON-NLS-1$
			this.logger.trace(e.toString());

			if(storeFailedLogout)
			{
				this.logger.debug("Param storeFailedLogout set to true. Adding failed logout to repository.");
				this.recordFailure(logoutRequest, endPoint, samlAuthnID);
			}
			else
				this.logger.debug("Param storeFailedLogout set to false. NOT adding failed logout to repository.");
			
			return LogoutMechanism.result.LogoutRequestFailed;
		}
		catch (ReferenceValueException e)
		{
			this.logger.error(Messages.getString("FailedLogoutMonitor.7"));  //$NON-NLS-1$
			this.logger.trace(e.getLocalizedMessage(), e);

			if(storeFailedLogout)
			{
				this.logger.debug("Param storeFailedLogout set to true. Adding failed logout to repository.");
				this.recordFailure(logoutRequest, endPoint, samlAuthnID);
			}
			else
				this.logger.debug("Param storeFailedLogout set to false. NOT adding failed logout to repository.");
			
			return LogoutMechanism.result.LogoutRequestFailed;
		}
		catch (UnmarshallerException e)
		{
			this.logger.error(Messages.getString("FailedLogoutMonitor.8") + endPoint); //$NON-NLS-1$
			this.logger.trace(e.getLocalizedMessage(), e);

			if(storeFailedLogout)
			{
				this.logger.debug("Param storeFailedLogout set to true. Adding failed logout to repository.");
				this.recordFailure(logoutRequest, endPoint, samlAuthnID);
			}
			else
				this.logger.debug("Param storeFailedLogout set to false. NOT adding failed logout to repository.");
			
			return LogoutMechanism.result.LogoutRequestFailed;
		}
		catch(InvalidSAMLResponseException e)
		{
			this.logger.error(Messages.getString("FailedLogoutMonitor.17")); //$NON-NLS-1$
			this.logger.trace(e.getLocalizedMessage(), e);			
		
			if(storeFailedLogout)
			{
				this.logger.debug("Param storeFailedLogout set to true. Adding failed logout to repository.");
				this.recordFailure(logoutRequest, endPoint, samlAuthnID);
			}
			else
				this.logger.debug("Param storeFailedLogout set to false. NOT adding failed logout to repository.");
			
			return LogoutMechanism.result.LogoutRequestFailed;
		}
		catch (WSClientException e)
		{
			this.logger.error(Messages.getString("FailedLogoutMonitor.9") + endPoint);  //$NON-NLS-1$
			this.logger.trace(e.getLocalizedMessage(), e);

			this.logger.debug("Param storeFailedLogout set to " + storeFailedLogout);
			
			if(storeFailedLogout)
				this.recordFailure(logoutRequest, endPoint, samlAuthnID);
					
			return LogoutMechanism.result.LogoutRequestFailed;
		}		
		catch (UnsupportedOperationException e)
		{
			this.logger.error(Messages.getString("FailedLogoutMonitor.9") + endPoint);  //$NON-NLS-1$
			this.logger.trace(e.getLocalizedMessage(), e);
	
			if(storeFailedLogout)
			{
				this.logger.debug("Param storeFailedLogout set to true. Adding failed logout to repository.");
				this.recordFailure(logoutRequest, endPoint, samlAuthnID);
			}
			else
				this.logger.debug("Param storeFailedLogout set to false. NOT adding failed logout to repository.");
			
			return LogoutMechanism.result.LogoutRequestFailed;
		}		
	}
	 
	
	/* Creates the logout request to send to an endpoint. 
	 * 
	 * @param samlAuthnID The ID of the user making the logout request.
	 * @param reason The reason for the logout.
	 * @param sessionIndicies A list of session Indexes associated with the principal.
	 * @return The string representation of the xml request object generated by this method.
	 */
	private Element generateLogoutRequest(String samlAuthnID,  List<String> sessionIndicies) throws MarshallerException
	{
		Element requestDocument = null;
		LogoutRequest request = new LogoutRequest();
		
		NameIDType subject = new NameIDType();
		NameIDType issuer = new NameIDType();
		subject.setValue(samlAuthnID);
		request.setNameID(subject);
		request.setID(this.identifierGenerator.generateSAMLID());
		request.setReason(LOGOUT_REASON);
		request.setVersion(VersionConstants.saml20);
		issuer.setValue(this.esoeIdentifier);
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
		
		requestDocument = this.logoutRequestMarshaller.marshallSignedElement(request);
	
		return requestDocument;
	}

	
	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoe.logout.LogoutMechanism#getEndPoints(java.lang.String)
	 */
		public List<String> getEndPoints(String entityID)
		{
			List<String> endpointLocations = new Vector<String>();
			
			EntityData entityData;
			try
			{
				entityData = this.metadata.getEntityData(entityID);
			}
			catch (MetadataStateException e)
			{
				// Metadata not loaded correctly. Just return an empty list.
				return endpointLocations;
			}
			
			if (entityData == null)
			{
				this.logger.error("No SPEP entity could be found. Unable to process logouts. Entity ID: " + entityID);
				return endpointLocations;
			}
			SPEPRole spepRole = entityData.getRoleData(SPEPRole.class);
			
			if (spepRole == null)
			{
				this.logger.error("Entity did not contain SPEP role. Unable to process logouts. Entity ID: " + entityID);
				return endpointLocations;
			}
			
			for (Endpoint endpoint : spepRole.getSingleLogoutServiceEndpointList())
			{
				endpointLocations.add(endpoint.getLocation());
			}
			
			return endpointLocations;
		}
	
	
		/*
		 * Add the failure record to the logout failure repository. If the given failure is determined to already exist in the failure
		 * repository, the old failure is replaced with the given one.
		 *  
		 * @param request The request that failed to deliver. 
		 * @param endPoint The end point the the request failed to deliver to.
		 * @param authnSessionId The session Id of the user for whom the LogoutRequest has failed. Used in comparisons with
		 * currently held failures to determine if a failure already exists for that user on that endpoint.
		 */
		private void recordFailure(Element request, String endPoint, String authnSessionId)
		{		
			// create an UpdateFailure object
			FailedLogout failure = new FailedLogoutImpl();
			
			failure.setEndPoint(endPoint);
			failure.setRequestDocument(request);
			failure.setTimeStamp(new Date());
			failure.setAuthnId(authnSessionId);
			
			// Add it to failure repository if it doesn't already exist
			if(this.logoutFailures.containsFailure(failure))
			{
				this.logger.debug(MessageFormat.format("Failed logout destined for {0} already exists, ignoring new logout request.", failure.getEndPoint()) ); //$NON-NLS-1$
			}
			else
			{
				this.logoutFailures.add(failure);
				this.logger.info(MessageFormat.format("Adding failed logout destined for {0} to repository.", failure.getEndPoint()) ); //$NON-NLS-1$
			}
					
		}
}

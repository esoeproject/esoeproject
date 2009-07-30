/* Copyright 2008, Queensland University of Technology
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
 * Creation Date: 28/11/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.esoe.sso.plugins.artifact.impl;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.w3._2000._09.xmldsig_.Signature;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.qut.middleware.crypto.KeystoreResolver;
import com.qut.middleware.esoe.sso.plugins.artifact.ArtifactProcessor;
import com.qut.middleware.esoe.sso.plugins.artifact.bean.Artifact;
import com.qut.middleware.esoe.sso.plugins.artifact.data.ArtifactDao;
import com.qut.middleware.esoe.sso.plugins.artifact.exception.ArtifactBindingException;
import com.qut.middleware.esoe.util.CalendarUtils;
import com.qut.middleware.esoe.ws.WSClient;
import com.qut.middleware.esoe.ws.exception.WSClientException;
import com.qut.middleware.metadata.bean.saml.SPEPRole;
import com.qut.middleware.metadata.exception.MetadataStateException;
import com.qut.middleware.metadata.processor.MetadataProcessor;
import com.qut.middleware.saml2.BindingConstants;
import com.qut.middleware.saml2.NameIDFormatConstants;
import com.qut.middleware.saml2.SchemaConstants;
import com.qut.middleware.saml2.StatusCodeConstants;
import com.qut.middleware.saml2.VersionConstants;
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
import com.qut.middleware.saml2.schemas.protocol.ArtifactResolve;
import com.qut.middleware.saml2.schemas.protocol.ArtifactResponse;
import com.qut.middleware.saml2.schemas.protocol.Status;
import com.qut.middleware.saml2.schemas.protocol.StatusCode;

public class ArtifactProcessorImpl implements ArtifactProcessor, InitializingBean
{
	private static final String ARTIFACT_BINDING = BindingConstants.soap;
	private String entityIdentifier;
	private byte[] sourceID;
	private int nodeIndex = -1;
	private IdentifierGenerator identifierGenerator;

	private final String RNG = "SHA1PRNG";
	private SecureRandom random;
	
	private ArtifactDao artifactDao;
	
	private Marshaller<ArtifactResponse> artifactResponseMarshaller;
	private Unmarshaller<ArtifactResponse> artifactResponseUnmarshaller;
	private Marshaller<ArtifactResolve> artifactResolveMarshaller;
	private Unmarshaller<ArtifactResolve> artifactResolveUnmarshaller;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private WSClient client;
	private MetadataProcessor metadataProcessor;
	private KeystoreResolver keystoreResolver;
	
	public void setKeystoreResolver(KeystoreResolver keystoreResolver)
	{
		this.keystoreResolver = keystoreResolver;
	}

	public void setIdentifierGenerator(IdentifierGenerator generator)
	{
		this.identifierGenerator = generator;
	}
	
	public void setEntityIdentifier(String entityIdentifier)
	{
		try
		{
			// Digesting the (local) entity identifier is a one-time operation, so we do it here.
			this.sourceID = digestEntityIdentifier(entityIdentifier);
			this.entityIdentifier = entityIdentifier;
			
			if (this.sourceID.length != 20)
			{
				throw new IllegalArgumentException("SHA1 hash of ESOE identifier resulted in an invalid result. Length should be 20 but was " + this.sourceID.length);
			}
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new IllegalArgumentException("Unable to digest ESOE identifier for source ID. The hash algorithm does not exist. " + e.getMessage(), e);
		}
		catch (UnsupportedEncodingException e)
		{
			throw new IllegalArgumentException("Unable to digest ESOE identifier for source ID. The encoding is not supported. " + e.getMessage(), e);
		}
	}

	private byte[] digestEntityIdentifier(String entityIdentifier) throws UnsupportedEncodingException, NoSuchAlgorithmException
	{
		byte[] entityIdentifierBytes = entityIdentifier.getBytes("UTF-8");
		MessageDigest digest = MessageDigest.getInstance("SHA1");
		digest.update(entityIdentifierBytes);
		return digest.digest();
	}
	
	public void setWSClient(WSClient client)
	{
		this.client = client;
	}
	
	public void setMetadataProcessor(MetadataProcessor metadataProcessor)
	{
		this.metadataProcessor = metadataProcessor;
	}
	
	public void setNodeIndex(int nodeIndex)
	{
		this.nodeIndex = nodeIndex;
	}
	
	public void afterPropertiesSet()
	{
		if (this.entityIdentifier == null || this.sourceID == null)
		{
			throw new IllegalArgumentException("ESOE identifier has not been specified correctly.");
		}
		
		if (this.identifierGenerator == null)
		{
			throw new IllegalArgumentException("Identifier generator has not been specified correctly.");
		}
		
		if (this.nodeIndex == -1)
		{
			throw new IllegalArgumentException("Node index has not been specified correctly.");
		}

		try
		{
			this.random = SecureRandom.getInstance(this.RNG);
			this.random.setSeed(System.currentTimeMillis());
			
			String packages = ArtifactResolve.class.getPackage().getName();
			String[] schema = new String[]{SchemaConstants.samlProtocol};
			this.artifactResponseMarshaller = new MarshallerImpl<ArtifactResponse>(packages, schema, this.keystoreResolver);
			this.artifactResponseUnmarshaller = new UnmarshallerImpl<ArtifactResponse>(packages, schema, this.metadataProcessor);
			this.artifactResolveMarshaller = new MarshallerImpl<ArtifactResolve>(packages, schema, this.keystoreResolver);
			this.artifactResolveUnmarshaller = new UnmarshallerImpl<ArtifactResolve>(packages, schema, this.metadataProcessor);
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new IllegalArgumentException("No such algorithm exists for the PRNG. Unable to continue. Error: " + e.getMessage(), e);
		}
		catch (MarshallerException e)
		{
			throw new IllegalArgumentException("Error initializing marshaller. Unable to continue. Error: " + e.getMessage(), e);
		}
		catch (UnmarshallerException e)
		{
			throw new IllegalArgumentException("Error intitializing unmarshaller. Unable to continue. Error: " + e.getMessage(), e);
		}
	}

	public Element execute(Element artifactRequest)
	{
		ArtifactResolve artifactResolve = null;
		try
		{
			this.logger.debug("Going to unmarshal ArtifactResolve document");
			artifactResolve = this.artifactResolveUnmarshaller.unMarshallSigned(artifactRequest);
			String requestIssuer = artifactResolve.getIssuer().getValue();
			
			String artifactToken = artifactResolve.getArtifact();
			this.logger.info("Unmarshalled ArtifactResolve document from issuer {} - requested artifact token is {}", new Object[]{requestIssuer, artifactToken});
			Artifact artifact = new Artifact(artifactToken);
			byte[] sourceID = artifact.getSourceID();
			
			boolean validSourceID = (sourceID.length == this.sourceID.length);
			if (validSourceID)
			{
				for (int i=0; i<sourceID.length; ++i)
				{
					// Compare each byte in sequence
					if (sourceID[i] != this.sourceID[i])
					{
						validSourceID = false;
						break;
					}
				}
			}
			
			if (!validSourceID)
			{
				// Convert source IDs to hex for logging.
				String sourceIDString = new String(Hex.encodeHex(sourceID));
				String expectedSourceIDString = new String(Hex.encodeHex(this.sourceID));
				String time = String.valueOf(System.currentTimeMillis());
				this.logger.error("{} Source ID for artifact token was invalid. Was {} but should have been {}", new Object[]{time, sourceIDString, expectedSourceIDString});
				return this.errorResponse(artifactResolve, StatusCodeConstants.requester, time + " Source ID for artifact token was invalid. Unable to respond");
			}
			
			// Resolve the artifact from the underlying data source.
			this.artifactDao.resolveArtifact(artifact);
			
			String audience = artifact.getAudience();
			byte[] document = artifact.getDocument();
			if (!requestIssuer.equals(audience) || document == null)
			{
				String time = String.valueOf(System.currentTimeMillis());
				this.logger.error("{} Audience entity ID for artifact token was invalid. Will respond with no document content. Request issuer was {} but document audience was {}", new Object[]{time, requestIssuer, audience});
				return this.statusResponse(artifactResolve, StatusCodeConstants.success, time + " The artifact request was successful but no artifact document to respond with.", null);
			}
			
			Document artifactDocument = this.artifactResolveUnmarshaller.generateDocument(document, false);
			return this.statusResponse(artifactResolve, StatusCodeConstants.success, "The artifact request was successful.", artifactDocument.getDocumentElement());
		}
		catch (SignatureValueException e)
		{
			artifactResolve = (ArtifactResolve)e.getJAXBObject();
			String issuer;
			if (artifactResolve != null)
			{
				issuer = artifactResolve.getIssuer().getValue();
			}
			else
			{
				issuer = "unknown issuer";
			}
			String time = String.valueOf(System.currentTimeMillis());
			this.logger.error("{} Signature validation failed on ArtifactResolve request. Generating error response. Request issuer was {}. Error was: {}", new Object[]{time, issuer, e.getMessage()});
			return this.errorResponse(artifactResolve, StatusCodeConstants.requester, time + "Signature validation failed on ArtifactResolve request. Unwilling to respond.");
		}
		catch (ReferenceValueException e)
		{
			artifactResolve = (ArtifactResolve)e.getJAXBObject();
			String issuer;
			if (artifactResolve != null)
			{
				issuer = artifactResolve.getIssuer().getValue();
			}
			else
			{
				issuer = "unknown issuer";
			}
			String time = String.valueOf(System.currentTimeMillis());
			this.logger.error("{} Reference value failure on ArtifactResolve request. Generating error response. Request issuer was {}. Error was: {}", new Object[]{time, issuer, e.getMessage()});
			return this.errorResponse(artifactResolve, StatusCodeConstants.requester, time + " Reference value failure on ArtifactResolve request. Unwilling to respond.");
		}
		catch (UnmarshallerException e)
		{
			artifactResolve = (ArtifactResolve)e.getJAXBObject();
			String issuer;
			if (artifactResolve != null)
			{
				issuer = artifactResolve.getIssuer().getValue();
			}
			else
			{
				issuer = "unknown issuer";
			}
			String time = String.valueOf(System.currentTimeMillis());
			this.logger.error("{} Unmarshalling failed on ArtifactResolve request. Generating error response. Request issuer was {}. Error was: {}", new Object[]{time, issuer, e.getMessage()});
			return this.errorResponse(artifactResolve, StatusCodeConstants.requester, time + " Unmarshalling failed on ArtifactResolve request. Unwilling to respond.");
		}
		catch (ArtifactBindingException e)
		{
			String issuer;
			if (artifactResolve != null)
			{
				issuer = artifactResolve.getIssuer().getValue();
			}
			else
			{
				issuer = "unknown issuer";
			}
			String time = String.valueOf(System.currentTimeMillis());
			this.logger.error("{} Artifact binding failed to process the request. Generating error response. Request issuer was {}. Error was: {}", new Object[]{time, issuer, e.getMessage()});
			return this.errorResponse(artifactResolve, StatusCodeConstants.requester, time + " Artifact binding failed to process the request. Unwilling to respond.");
		}
	}

	public String registerArtifact(Element artifactDocument, String audience) throws ArtifactBindingException
	{
		try
		{
			byte[] messageHandle = new byte[20];
			this.random.nextBytes(messageHandle);
			
			byte[] document = this.artifactResponseMarshaller.generateOutput(artifactDocument.getOwnerDocument(), null);
			
			Artifact artifact = new Artifact(this.nodeIndex, this.sourceID, messageHandle, audience, document);
			this.artifactDao.storeArtifact(artifact);
			
			return artifact.toBase64Artifact();
		}
		catch (MarshallerException e)
		{
			throw new ArtifactBindingException("Marshalling failed while registering artifact. Unable to continue. Error: " + e.getMessage(), e);
		}
	}
	
	private Element errorResponse(ArtifactResolve request, String statusCodeValue, String statusMessage)
	{
		return this.statusResponse(request, statusCodeValue, statusMessage, null);
	}
	
	private Element statusResponse(ArtifactResolve request, String statusCodeValue, String statusMessage, Element content)
	{
		ArtifactResponse artifactResponse = new ArtifactResponse();
		artifactResponse.setID(this.identifierGenerator.generateSAMLID());
		artifactResponse.setIssueInstant(CalendarUtils.generateXMLCalendar());
		artifactResponse.setVersion(VersionConstants.saml20);
		artifactResponse.setSignature(new Signature());
		
		NameIDType issuer = new NameIDType();
		issuer.setValue(this.entityIdentifier);
		issuer.setFormat(NameIDFormatConstants.entity);
		artifactResponse.setIssuer(issuer);
		
		Status status = new Status();
		StatusCode statusCode = new StatusCode();
		statusCode.setValue(statusCodeValue);
		status.setStatusCode(statusCode);
		status.setStatusMessage(statusMessage);
		artifactResponse.setStatus(status);
		
		String requestIssuer = "unknown issuer";
		if (request != null)
		{
			requestIssuer = request.getIssuer().getValue();
			artifactResponse.setInResponseTo(request.getID());
		}
		
		if (content != null)
		{
			artifactResponse.setAny(content);
		}
		
		try
		{
			Element response = this.artifactResponseMarshaller.marshallSignedElement(artifactResponse);
			this.logger.info("Marshalled status response. Request issuer = {}  Status = {}  Message = {}  Has content = {}", new Object[]{requestIssuer, statusCodeValue, statusMessage, Boolean.toString(content != null)});
			return response;
		}
		catch (MarshallerException e)
		{
			this.logger.error("Error marshalling status response. Unable to respond. Error was: {}, Caused by: {}", e.getMessage(), (e.getCause() != null) ? e.getCause().getMessage() : null);
			return null;
		}
	}

	public Element getRemoteArtifact(String artifactToken) throws ArtifactBindingException
	{
		try
		{
			// Generate a request.
			ArtifactResolve artifactResolve = new ArtifactResolve();
			artifactResolve.setID(this.identifierGenerator.generateSAMLID());
			artifactResolve.setIssueInstant(CalendarUtils.generateXMLCalendar());
			artifactResolve.setVersion(VersionConstants.saml20);
			artifactResolve.setSignature(new Signature());
			
			NameIDType issuer = new NameIDType();
			issuer.setValue(this.entityIdentifier);
			issuer.setFormat(NameIDFormatConstants.entity);
			artifactResolve.setIssuer(issuer);
			
			artifactResolve.setArtifact(artifactToken);
			
			// Marshal to a DOM element for the WS query
			Element artifactResolveDocument = this.artifactResolveMarshaller.marshallSignedElement(artifactResolve);
			
			// Find the destination.
			Artifact artifact = new Artifact(artifactToken);
			String entityID = findEntityID(artifact);
			int index = artifact.getIndex();
			
			// Get the endpoint for the request from metadata
			SPEPRole spepRole = this.metadataProcessor.getEntityRoleData(entityID, SPEPRole.class);
			String endpoint = null;
			if (spepRole != null)
			{
				endpoint = spepRole.getArtifactResolutionEndpoint(ARTIFACT_BINDING, index);
			}
			if (endpoint == null)
			{
				throw new ArtifactBindingException("Endpoint for artifact resolution service was null. Unable to resolve artifacts for entity: " + entityID);
			}
			
			// Make the WS call
			Element response = this.client.artifactResolve(artifactResolveDocument, endpoint);
			
			// Unmarshal and validate the response.
			ArtifactResponse artifactResponse = this.artifactResponseUnmarshaller.unMarshallSigned(response);
			if (!artifactResponse.getInResponseTo().equals(artifactResolve.getID()))
			{
				throw new ArtifactBindingException("Artifact response InResponseTo element does not match the ID of the request document. Expected: " + artifactResolve.getID() + " but the ID was: " + artifactResponse.getInResponseTo());
			}
			
			Status status = artifactResponse.getStatus();
			String responseIssuer = artifactResponse.getIssuer().getValue();
			if (status == null)
			{
				throw new ArtifactBindingException("Artifact response had null Status, unable to validate. Issuer was: " + responseIssuer);
			}
			StatusCode statusCode = status.getStatusCode();
			if (statusCode == null)
			{
				throw new ArtifactBindingException("Artifact response status did not have a status code. Issuer was: " + responseIssuer);
			}
			String statusCodeValue = statusCode.getValue();
			if (statusCodeValue == null)
			{
				throw new ArtifactBindingException("Artifact response status code did not have a value. Issuer was: " + responseIssuer);
			}
			String statusMessage = status.getStatusMessage();
			if (statusMessage == null)
			{
				throw new ArtifactBindingException("Artifact response status did not have a message. Issuer was: " + responseIssuer);
			}
			
			if (!statusCodeValue.equals(StatusCodeConstants.success))
			{
				throw new ArtifactBindingException("Artifact response status did not indicate success. Status was: " + statusCodeValue + "  Issuer: " + responseIssuer + "  Status message: " + statusMessage);
			}
			
			Element artifactDocument = artifactResponse.getAny();
			if (artifactDocument == null)
			{
				throw new ArtifactBindingException("Artifact response did not have a document. Issuer was: " + responseIssuer);
			}
			
			this.logger.info("ArtifactResponse from {} had status {}, status message: {}", new Object[]{responseIssuer, statusCodeValue, statusMessage});
			return artifactDocument;
		}
		catch (MarshallerException e)
		{
			this.logger.error("Artifact resolution failed for token {}, marshalling exception occurred while trying to create an ArtifactResolve request. Message was: {}", new Object[]{artifactToken, e.getMessage()});
			throw new ArtifactBindingException("Marshalling exception occurred while trying to create an ArtifactResolve request. Unable to continue processing.", e);
		}
		catch (MetadataStateException e)
		{
			this.logger.error("Artifact resolution failed for token {}, metadata was in an invalid state. Message was: {}", new Object[]{artifactToken, e.getMessage()});
			throw new ArtifactBindingException("Metadata was in an invalid state, unable to perform an ArtifactResolve request.", e);
		}
		catch (SignatureValueException e)
		{
			ArtifactResponse artifactResponse = (ArtifactResponse)e.getJAXBObject();
			String issuer = "unknown";
			if (artifactResponse != null) {
				issuer = artifactResponse.getIssuer().getValue();
			}
			this.logger.error("Artifact resolution failed for token {}, the signature on the response was invalid. Message was: {}", new Object[]{artifactToken, e.getMessage()});
			throw new ArtifactBindingException("Artifact resolution failed, the signature on the response was invalid. Issuer: " + issuer, e);
		}
		catch (ReferenceValueException e)
		{
			ArtifactResponse artifactResponse = (ArtifactResponse)e.getJAXBObject();
			String issuer = "unknown";
			if (artifactResponse != null) {
				issuer = artifactResponse.getIssuer().getValue();
			}
			this.logger.error("Artifact resolution failed for token {} due to a reference value error while validating signatures. Message was: {}", new Object[]{artifactToken, e.getMessage()});
			throw new ArtifactBindingException("Artifact resolution failed due to a reference value error while validating signatures. Issuer: " + issuer, e);
		}
		catch (UnmarshallerException e)
		{
			ArtifactResponse artifactResponse = (ArtifactResponse)e.getJAXBObject();
			String issuer = "unknown";
			if (artifactResponse != null) {
				issuer = artifactResponse.getIssuer().getValue();
			}
			this.logger.error("Artifact resolution failed for token {}, the unmarshaller failed to validate the document. Message was: {}", new Object[]{artifactToken, e.getMessage()});
			throw new ArtifactBindingException("Artifact resolution failed, the unmarshaller failed to validate the document. Issuer: " + issuer, e);
		}
		catch (WSClientException e)
		{
			this.logger.error("Artifact resolution failed for token {}, the web service call was not made successfully. Message was: {}", new Object[]{artifactToken, e.getMessage()});
			throw new ArtifactBindingException("Artifact resolution failed, the web service call was not made successfully.", e);
		}
	}

	private String findEntityID(Artifact artifact) throws ArtifactBindingException
	{
		try
		{
			byte[] sourceID = artifact.getSourceID();
			
			for (String entity : this.metadataProcessor.getEntityList())
			{
				byte[] entitySourceID = this.digestEntityIdentifier(entity);
				
				// Check lengths then compare byte by byte
				if (sourceID.length != entitySourceID.length) continue;
				for (int i=0; i<sourceID.length; ++i)
				{
					if (sourceID[i] != entitySourceID[i]) continue;
				}
				
				return entity;
			}
			
			return null;
		}
		catch (UnsupportedEncodingException e)
		{
			throw new ArtifactBindingException("Encoding was not supported. Unable to continue processing.", e);
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new ArtifactBindingException("No such algorithm exists while trying to digest entity identifier. Unable to continue processing.", e);
		}
	}
}

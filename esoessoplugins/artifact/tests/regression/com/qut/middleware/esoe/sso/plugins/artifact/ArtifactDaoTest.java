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
 * Creation Date: 17/12/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.esoe.sso.plugins.artifact;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.w3._2000._09.xmldsig_.Signature;
import org.w3c.dom.Element;

import com.qut.middleware.crypto.KeystoreResolver;
import com.qut.middleware.crypto.impl.KeystoreResolverImpl;
import com.qut.middleware.esoe.sso.plugins.artifact.bean.Artifact;
import com.qut.middleware.esoe.sso.plugins.artifact.data.ArtifactDao;
import com.qut.middleware.esoe.sso.plugins.artifact.data.impl.ArtifactDaoMemoryImpl;
import com.qut.middleware.esoe.util.CalendarUtils;
import com.qut.middleware.saml2.SchemaConstants;
import com.qut.middleware.saml2.StatusCodeConstants;
import com.qut.middleware.saml2.VersionConstants;
import com.qut.middleware.saml2.handler.Marshaller;
import com.qut.middleware.saml2.handler.Unmarshaller;
import com.qut.middleware.saml2.handler.impl.MarshallerImpl;
import com.qut.middleware.saml2.handler.impl.UnmarshallerImpl;
import com.qut.middleware.saml2.schemas.protocol.ArtifactResponse;
import com.qut.middleware.saml2.schemas.protocol.AuthnRequest;
import com.qut.middleware.saml2.schemas.protocol.Response;
import com.qut.middleware.saml2.schemas.protocol.Status;
import com.qut.middleware.saml2.schemas.protocol.StatusCode;
import com.qut.middleware.saml2.schemas.soap.v12.Body;
import com.qut.middleware.saml2.schemas.soap.v12.Envelope;

public class ArtifactDaoTest
{
	
	private String keystorePath = "tests/testdata/testKeystore.ks";
	private String keystorePassword = "kspass";
	private String keyPassword = "keypass";
	private String keyAlias = "testcert";

	private KeystoreResolver keystoreResolver;
	
	byte[] sourceID = new byte[]{
			0, 0, 0, 0, 0,
			0, 0, 0, 0, 0,
			0, 0, 0, 0, 0,
			0, 0, 0, 0, 0,
	};
	byte[] messageHandle = new byte[]{
			1, 2, 3, 4, 5,
			1, 2, 3, 4, 5,
			1, 2, 3, 4, 5,
			1, 2, 3, 4, 5,
	};
	
	public ArtifactDaoTest() throws Exception
	{
		this.keystoreResolver = new KeystoreResolverImpl(new File(keystorePath), keystorePassword, "testpriv", keyPassword);
	}

	@Test
	public void testStoreRetrieve() throws Exception
	{
		String audience = "audience";
		byte[] document = new byte[0];
		Artifact artifact = new Artifact(0, sourceID, messageHandle, audience, document);
		ArtifactDao artifactDao = new ArtifactDaoMemoryImpl();
		artifactDao.storeArtifact(artifact);
		
		String artifactToken = artifact.toBase64Artifact();
		
		Artifact artifactRetrieved = new Artifact(artifactToken);
		artifactDao.resolveArtifact(artifactRetrieved);
		
		assertEquals(audience, artifactRetrieved.getAudience());
		assertEquals(document, artifactRetrieved.getDocument());
	}
	
	@Test
	public void testSignature() throws Exception
	{
		String packages = Response.class.getPackage().getName();
		String[] schemas = new String[]{SchemaConstants.samlProtocol};
		String soapPackage = Envelope.class.getPackage().getName();
		String[] soapSchemas = new String[]{SchemaConstants.soapv12};
		
		Marshaller<Response> marshaller = new MarshallerImpl<Response>(packages, schemas, this.keystoreResolver);
		Marshaller<ArtifactResponse> artifactMarshaller = (Marshaller)marshaller;
		Unmarshaller<Response> unmarshaller = new UnmarshallerImpl<Response>(packages, schemas, this.keystoreResolver);
		Unmarshaller<ArtifactResponse> artifactUnmarshaller = (Unmarshaller)unmarshaller;
		Marshaller<Envelope> soapMarshaller = new MarshallerImpl<Envelope>(soapPackage, soapSchemas, this.keystoreResolver);
		Unmarshaller<Envelope> soapUnmarshaller = new UnmarshallerImpl<Envelope>(soapPackage, soapSchemas, this.keystoreResolver);
		
		byte[] doc = null;
		
		{
			Response response = new Response();
			response.setSignature(new Signature());
			response.setID("Abcd");
			response.setIssueInstant(CalendarUtils.generateXMLCalendar());
			response.setVersion(VersionConstants.saml20);
			response.setStatus(new Status());
			response.getStatus().setStatusMessage("message");
			response.getStatus().setStatusCode(new StatusCode());
			response.getStatus().getStatusCode().setValue(StatusCodeConstants.success);
			
			Element element1 = marshaller.marshallSignedElement(response);
			
			ArtifactResponse artifactResponse = new ArtifactResponse();
			artifactResponse.setSignature(new Signature());
			artifactResponse.setID("Efgh");
			artifactResponse.setIssueInstant(CalendarUtils.generateXMLCalendar());
			artifactResponse.setVersion(VersionConstants.saml20);
			artifactResponse.setStatus(new Status());
			artifactResponse.getStatus().setStatusMessage("message");
			artifactResponse.getStatus().setStatusCode(new StatusCode());
			artifactResponse.getStatus().getStatusCode().setValue(StatusCodeConstants.success);
			
			artifactResponse.setAny(element1);
	
			Element artifactResponseElement = artifactMarshaller.marshallSignedElement(artifactResponse);
			
			Envelope envelope = new Envelope();
			envelope.setBody(new Body());
			envelope.getBody().getAnies().add(artifactResponseElement);
			
			doc = soapMarshaller.marshallUnSigned(envelope);
		}
		
		Envelope envelope = soapUnmarshaller.unMarshallUnSigned(doc);
		ArtifactResponse artifactResponse = artifactUnmarshaller.unMarshallUnSigned(envelope.getBody().getAnies().get(0));
		Response response = unmarshaller.unMarshallSigned(artifactResponse.getAny());
	}
}

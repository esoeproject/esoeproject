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

	private String keystorePath = "build/testdata/testKeystore.ks";
	private String keystorePassword = "kspass";
	private String keyPassword = "keypass";

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
}

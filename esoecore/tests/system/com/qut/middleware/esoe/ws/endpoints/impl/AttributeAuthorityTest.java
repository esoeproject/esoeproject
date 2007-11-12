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
 * Author:
 * Creation Date:
 * 
 * Purpose:
 */
package com.qut.middleware.esoe.ws.endpoints.impl;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringWriter;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.GregorianCalendar;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.junit.Before;
import org.junit.Test;
import org.w3._2000._09.xmldsig_.Signature;

import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.crypto.KeyStoreResolver;
import com.qut.middleware.esoe.crypto.impl.KeyStoreResolverImpl;
import com.qut.middleware.saml2.exception.MarshallerException;
import com.qut.middleware.saml2.handler.Marshaller;
import com.qut.middleware.saml2.handler.impl.MarshallerImpl;
import com.qut.middleware.saml2.identifier.IdentifierGenerator;
import com.qut.middleware.saml2.identifier.impl.IdentifierCacheImpl;
import com.qut.middleware.saml2.identifier.impl.IdentifierGeneratorImpl;
import com.qut.middleware.saml2.schemas.assertion.AttributeType;
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.assertion.Subject;
import com.qut.middleware.saml2.schemas.protocol.AttributeQuery;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

/**
 * @author beddoes
 * 
 */
public class AttributeAuthorityTest
{
	private Marshaller<AttributeQuery> marshaller;
	private IdentifierGenerator identifierGenerator;
	
	@Before
	public void setup() throws Exception
	{
		String keyStorePath = "secure" + File.separator + "esoekeystore.ks";
		String keyStorePassword = "Es0EKs54P4SSPK";
		String esoeKeyAlias = "esoeprimary";
		String esoeKeyPassword = "Es0EKs54P4SSPK";
		
		KeyStoreResolver keyStoreResolver = new KeyStoreResolverImpl(new File(keyStorePath), keyStorePassword, esoeKeyAlias, esoeKeyPassword);
		
		PublicKey publicKey = keyStoreResolver.resolveKey("esoeprimary");
		PrivateKey privateKey = keyStoreResolver.getPrivateKey();
		
		String[] schemas = new String[]{ConfigurationConstants.samlProtocol};
		
		this.marshaller = new MarshallerImpl<AttributeQuery>(AttributeQuery.class.getPackage().getName(), schemas, esoeKeyAlias, privateKey);
		this.identifierGenerator = new IdentifierGeneratorImpl(new IdentifierCacheImpl());
	}

	@Test
	public void testExecuteAttributeAuthority()
	{
		EndpointReference targetEPR = new EndpointReference(
		"http://esoe-dev.qut.edu.au:8080/ws/services/esoe/attributeAuthority");
		
		try
		{
			ByteArrayInputStream reader = new ByteArrayInputStream(createAttributeRequest());

			XMLInputFactory xif = XMLInputFactory.newInstance();
			XMLStreamReader xmlreader = xif.createXMLStreamReader(reader);

			StAXOMBuilder builder = new StAXOMBuilder(xmlreader);
			OMElement request = builder.getDocumentElement();

			ServiceClient serviceClient = new ServiceClient();
			Options options = new Options();
			serviceClient.setOptions(options);
			options.setTo(targetEPR);

			OMElement result = serviceClient.sendReceive(request);

			StringWriter writer = new StringWriter();
			result.serialize(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));
			writer.flush();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private byte[] createAttributeRequest()
	{
		String destination = "https://site.url/roar"; // AttributeQuery attribute
		String consent = "https://site.url/roar"; // AttributeQuery attribute
		String issuer = "7602897340987209385709283750-kjgashldjfhlasdjhflaksjhdfasdf-235908273"; // AttributeQuery /
																									// Issuer / NameID
																									// value

		AttributeQuery attributeQuery = new AttributeQuery();
		attributeQuery.setID(this.identifierGenerator.generateSAMLID());
		attributeQuery.setIssueInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));
		attributeQuery.setDestination(destination);
		attributeQuery.setConsent(consent);
		attributeQuery.setVersion("2.0");

		Subject subject = new Subject();
		NameIDType subjectNameID = new NameIDType();
		subjectNameID.setValue("_ba7f2c8d7ca6b1d20d8254b0f825f74d16f73e9f-b7542692abeb5a6237a23e6cdeee443b");

		subject.setNameID(subjectNameID);
		attributeQuery.setSubject(subject);

		NameIDType issuerNameID = new NameIDType();
		issuerNameID.setValue(issuer);
		attributeQuery.setIssuer(issuerNameID);

		AttributeType attributeUsername = new AttributeType();
		attributeUsername.setName("username");
		AttributeType attributeMail = new AttributeType();
		attributeMail.setName("mail");
		AttributeType attributeSurname = new AttributeType();
		attributeSurname.setName("surname");
		attributeQuery.getAttributes().add(attributeUsername);
		attributeQuery.getAttributes().add(attributeMail);
		attributeQuery.getAttributes().add(attributeSurname);

		Signature signature = new Signature();
		attributeQuery.setSignature(signature);

		byte[] request = null;
		try
		{
			request = this.marshaller.marshallSigned(attributeQuery);
		}
		catch (MarshallerException e)
		{
			fail("Marshaller error: " + e.getMessage() + "\n" + e.getCause().getMessage());
		}
		
		return request;
	}
}
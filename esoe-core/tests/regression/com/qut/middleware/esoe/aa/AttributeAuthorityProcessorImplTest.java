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
package com.qut.middleware.esoe.aa;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.fail;

import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;
import org.w3._2000._09.xmldsig_.Signature;

import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.aa.bean.AAProcessorData;
import com.qut.middleware.esoe.aa.bean.impl.AAProcessorDataImpl;
import com.qut.middleware.esoe.aa.exception.InvalidPrincipalException;
import com.qut.middleware.esoe.aa.exception.InvalidRequestException;
import com.qut.middleware.esoe.aa.impl.AttributeAuthorityProcessorImpl;
import com.qut.middleware.esoe.crypto.KeyStoreResolver;
import com.qut.middleware.esoe.crypto.impl.KeyStoreResolverImpl;
import com.qut.middleware.esoe.metadata.Metadata;
import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.Query;
import com.qut.middleware.esoe.sessions.SessionsProcessor;
import com.qut.middleware.esoe.sessions.bean.IdentityAttribute;
import com.qut.middleware.esoe.spep.SPEPProcessor;
import com.qut.middleware.saml2.exception.MarshallerException;
import com.qut.middleware.saml2.exception.ReferenceValueException;
import com.qut.middleware.saml2.exception.SignatureValueException;
import com.qut.middleware.saml2.exception.UnmarshallerException;
import com.qut.middleware.saml2.handler.Marshaller;
import com.qut.middleware.saml2.handler.Unmarshaller;
import com.qut.middleware.saml2.handler.impl.MarshallerImpl;
import com.qut.middleware.saml2.handler.impl.UnmarshallerImpl;
import com.qut.middleware.saml2.identifier.IdentifierCache;
import com.qut.middleware.saml2.identifier.IdentifierGenerator;
import com.qut.middleware.saml2.identifier.impl.IdentifierCacheImpl;
import com.qut.middleware.saml2.identifier.impl.IdentifierGeneratorImpl;
import com.qut.middleware.saml2.schemas.assertion.AttributeType;
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.assertion.Subject;
import com.qut.middleware.saml2.schemas.protocol.AttributeQuery;
import com.qut.middleware.saml2.schemas.protocol.Response;
import com.qut.middleware.saml2.validator.SAMLValidator;
import com.qut.middleware.saml2.validator.impl.SAMLValidatorImpl;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

/**
 * @author shaun
 *
 */
@SuppressWarnings({"boxing","nls","unused"})
public class AttributeAuthorityProcessorImplTest
{
	private SPEPProcessor spepProcessor;
	private SessionsProcessor sessionsProcessor;
	private Query query;
	private Principal principal;
	private String sessionID = "857929385702837501-75921837954781293847987-79182374987123";
	private String samlID = "kfahsdkjfhwqoieo-hklajshogiwjopeipqoripqow-jgoijoakjoa";
	private SAMLValidator samlValidator;
	private AttributeAuthorityProcessor attributeAuthorityProcessor;
	private Metadata metadata;
	private Unmarshaller<Response> responseUnmarshaller;
	private Marshaller<AttributeQuery> attributeQueryMarshaller;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		this.query = createMock(Query.class);
		
		this.sessionsProcessor = createMock(SessionsProcessor.class);
		this.spepProcessor = createMock(SPEPProcessor.class);
		
		this.principal = createMock(Principal.class);
		
		Map<String,IdentityAttribute> attributeMap = new HashMap<String,IdentityAttribute>();
		
		expect(this.sessionsProcessor.getQuery()).andReturn(this.query).anyTimes();
		expect(this.query.querySAMLSession(this.samlID)).andReturn(this.principal).anyTimes();
		expect(this.principal.getPrincipalAuthnIdentifier()).andReturn("beddoes").anyTimes();
		expect(this.principal.getSessionID()).andReturn(this.sessionID).anyTimes();
		expect(this.principal.getAttributes()).andReturn(attributeMap).anyTimes();
		
		List<Object> usernameValues = new Vector<Object>();
		usernameValues.add("beddoes");
		
		IdentityAttribute usernameAttribute = createMock(IdentityAttribute.class);
		expect(usernameAttribute.getValues()).andReturn(usernameValues).anyTimes();
		attributeMap.put("username", usernameAttribute);
		
		List<Object> mailValues = new Vector<Object>();
		mailValues.add("beddoes@qut.edu.au");
		mailValues.add("b.beddoes@qut.edu.au");
		
		IdentityAttribute mailAttribute = createMock(IdentityAttribute.class);
	//	expect(usernameAttribute.getValues()).andReturn(mailValues).anyTimes();
		
		String keyStorePath =  "../esoe/secure/esoekeystore.ks";
		String keyStorePassword = "Es0EKs54P4SSPK";
		String esoeKeyAlias = "esoeprimary";
		String esoeKeyPassword = "Es0EKs54P4SSPK";
		
		KeyStoreResolver keyStoreResolver = new KeyStoreResolverImpl(new File(keyStorePath), keyStorePassword, esoeKeyAlias, esoeKeyPassword);
		
		PublicKey publicKey = keyStoreResolver.resolveKey("esoeprimary");
		PrivateKey privateKey = keyStoreResolver.getPrivateKey();
		
		this.metadata = createMock(Metadata.class);
		expect(this.metadata.resolveKey("esoeprimary")).andReturn(publicKey).anyTimes();
		
		this.spepProcessor = createMock(SPEPProcessor.class);
		expect(this.spepProcessor.getMetadata()).andReturn(this.metadata).anyTimes();
		expect(this.metadata.getESOEIdentifier()).andReturn("ESOE-TEST").anyTimes();
		
		IdentifierCache identifierCache = new IdentifierCacheImpl();
		IdentifierGenerator identifierGenerator = new IdentifierGeneratorImpl(new IdentifierCacheImpl());
		
		this.samlValidator = new SAMLValidatorImpl(identifierCache, 180);
		
		replay(this.metadata);
		replay(this.query);
		replay(this.sessionsProcessor);
		replay(this.principal);
		replay(this.spepProcessor);
		replay(usernameAttribute);
		replay(mailAttribute);
		
		this.attributeAuthorityProcessor = new AttributeAuthorityProcessorImpl(this.metadata, this.sessionsProcessor, this.samlValidator, identifierGenerator, keyStoreResolver, 60);
		
		String[] schemas = new String[]{ConfigurationConstants.samlProtocol};
		this.responseUnmarshaller = new UnmarshallerImpl<Response>(Response.class.getPackage().getName(), schemas, this.metadata);
		
		this.attributeQueryMarshaller = new MarshallerImpl<AttributeQuery>(AttributeQuery.class.getPackage().getName(), schemas, esoeKeyAlias, privateKey);

	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.aa.impl.AttributeAuthorityProcessorImpl#execute(com.qut.middleware.esoe.aa.bean.AAProcessorData)}.
	 */
	@Test
	public final void testProcessRequest()
	{
		AAProcessorData data = new AAProcessorDataImpl();
		String id = "abc"; // AttributeQuery attribute
		String destination = "https://site.url/roar"; // AttributeQuery attribute
		String consent = "https://site.url/roar"; // AttributeQuery attribute
		String issuer = "7602897340987209385709283750-kjgashldjfhlasdjhflaksjhdfasdf-235908273"; // AttributeQuery / Issuer / NameID value
		
		AttributeQuery attributeQuery = new AttributeQuery();
		attributeQuery.setID(id);
		attributeQuery.setIssueInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));
		attributeQuery.setDestination(destination);
		attributeQuery.setConsent(consent);
		attributeQuery.setVersion("2.0");
		
		Subject subject = new Subject();
		NameIDType subjectNameID = new NameIDType();
		subjectNameID.setValue(this.samlID);
		
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
		
		String request = null;
		try
		{
			request = marshalRequest(attributeQuery);
		}
		catch (MarshallerException e)
		{
			fail("Marshaller error: " + e.getMessage() + "\n" + e.getCause().getMessage());
		}
		
		data.setRequestDocument(request);
		
		
		try
		{
			this.attributeAuthorityProcessor.execute(data);
		}
		catch (InvalidPrincipalException e)
		{
			fail("Invalid principal: " + e.getMessage() + "\n" + e.getCause().getMessage());
		}
		catch (InvalidRequestException e)
		{
			e.printStackTrace();
			fail("Invalid request: " + e.getMessage() + "\n" + e.getCause().getMessage());
		}
		
		//System.out.println(data.getResponseDocument());
	}

	private String marshalRequest(AttributeQuery request) throws MarshallerException
	{
		return this.attributeQueryMarshaller.marshallSigned(request);
	}
	
	private Response unmarshalResponse(String response) throws UnmarshallerException, ReferenceValueException, SignatureValueException
	{
		return this.responseUnmarshaller.unMarshallSigned(response);
	}
}

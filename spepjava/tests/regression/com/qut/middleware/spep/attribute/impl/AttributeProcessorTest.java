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
 * Author: Shaun Mangelsdorf
 * Creation Date: 22/11/2006
 *
 * Purpose: Tests the attribute processor
 */
package com.qut.middleware.spep.attribute.impl;

import static com.qut.middleware.test.regression.Capture.capture;
import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.math.BigInteger;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.SimpleTimeZone;

import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Before;
import org.junit.Test;
import org.w3._2000._09.xmldsig_.Signature;
import org.w3c.dom.Element;

import com.qut.middleware.crypto.KeystoreResolver;
import com.qut.middleware.crypto.impl.KeystoreResolverImpl;
import com.qut.middleware.metadata.bean.EntityData;
import com.qut.middleware.metadata.bean.saml.SPEPRole;
import com.qut.middleware.metadata.bean.saml.TrustedESOERole;
import com.qut.middleware.metadata.processor.MetadataProcessor;
import com.qut.middleware.saml2.ConfirmationMethodConstants;
import com.qut.middleware.saml2.NameIDFormatConstants;
import com.qut.middleware.saml2.SchemaConstants;
import com.qut.middleware.saml2.StatusCodeConstants;
import com.qut.middleware.saml2.VersionConstants;
import com.qut.middleware.saml2.handler.Marshaller;
import com.qut.middleware.saml2.handler.Unmarshaller;
import com.qut.middleware.saml2.handler.impl.MarshallerImpl;
import com.qut.middleware.saml2.handler.impl.UnmarshallerImpl;
import com.qut.middleware.saml2.identifier.IdentifierCache;
import com.qut.middleware.saml2.identifier.IdentifierGenerator;
import com.qut.middleware.saml2.identifier.impl.IdentifierCacheImpl;
import com.qut.middleware.saml2.schemas.assertion.Assertion;
import com.qut.middleware.saml2.schemas.assertion.AttributeStatement;
import com.qut.middleware.saml2.schemas.assertion.AttributeType;
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.assertion.Subject;
import com.qut.middleware.saml2.schemas.assertion.SubjectConfirmation;
import com.qut.middleware.saml2.schemas.assertion.SubjectConfirmationDataType;
import com.qut.middleware.saml2.schemas.protocol.AttributeQuery;
import com.qut.middleware.saml2.schemas.protocol.Response;
import com.qut.middleware.saml2.schemas.protocol.Status;
import com.qut.middleware.saml2.schemas.protocol.StatusCode;
import com.qut.middleware.saml2.validator.SAMLValidator;
import com.qut.middleware.saml2.validator.impl.SAMLValidatorImpl;
import com.qut.middleware.spep.ConfigurationConstants;
import com.qut.middleware.spep.exception.AttributeProcessingException;
import com.qut.middleware.spep.sessions.PrincipalSession;
import com.qut.middleware.spep.sessions.impl.PrincipalSessionImpl;
import com.qut.middleware.spep.ws.WSClient;
import com.qut.middleware.test.regression.Capture;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

/** */

@SuppressWarnings({"nls"})
public class AttributeProcessorTest
{

	private String samlID1;
	private String keyName;
	private MetadataProcessor metadata;
	private IdentifierGenerator identifierGenerator;
	private IdentifierCache identifierCache;
	private SAMLValidator samlValidator;
	private AttributeProcessorImpl attributeProcessor;
	private String[] schemas;
	private Marshaller<Response> responseMarshaller;
	private WSClient wsClient;
	private String emailAddress;
	private String uid;
	private PublicKey publicKey;
	private SAMLValidator internalSAMLValidator;
	private String spepIdentifier;
	private String esoeID = "89548958904543563";
	private String assertionConsumerServiceLocation = "some.place/someservice";

	private List<Object> mocked;
	private EntityData spepEntityData;
	private SPEPRole spepRole;
	private EntityData esoeEntityData;
	private TrustedESOERole esoeRole;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		this.mocked = new ArrayList<Object>();
		File in = new File( "tests" + File.separator + "testdata" + File.separator + "testkeystore.ks");
		KeystoreResolver keyStoreResolver = new KeystoreResolverImpl(in, "Es0EKs54P4SSPK", "esoeprimary", "Es0EKs54P4SSPK");
		this.publicKey = keyStoreResolver.getLocalPublicKey();
		this.keyName = keyStoreResolver.getLocalKeyAlias();

		this.samlID1 = "jfaosjdofiqjwoerjqoweijr-hjosadijroqwiejroijo";
		this.spepIdentifier = "_JAFIOSJEOIFJQWEIOJFQPWOEJPQOWREPOQWERPOIQWEPORIQWPOEKPAOSDGPOJAKGJWQLEKGJ";

		this.metadata = createMock(MetadataProcessor.class);
		this.mocked.add(this.metadata);
		this.spepEntityData = createMock(EntityData.class);
		this.mocked.add(this.spepEntityData);
		this.spepRole = createMock(SPEPRole.class);
		this.mocked.add(this.spepRole);
		expect(this.metadata.getEntityData(spepIdentifier)).andReturn(this.spepEntityData).anyTimes();
		expect(this.metadata.getEntityRoleData(spepIdentifier, SPEPRole.class)).andReturn(this.spepRole).anyTimes();
		expect(this.spepEntityData.getRoleData(SPEPRole.class)).andReturn(this.spepRole).anyTimes();
		expect(this.spepRole.getAssertionConsumerServiceEndpoint((String)notNull(), anyInt())).andReturn(this.assertionConsumerServiceLocation).anyTimes();

		this.esoeEntityData = createMock(EntityData.class);
		this.mocked.add(esoeEntityData);
		this.esoeRole = createMock(TrustedESOERole.class);
		this.mocked.add(esoeRole);
		expect(this.metadata.getEntityData(esoeID)).andReturn(this.esoeEntityData).anyTimes();
		expect(this.metadata.getEntityRoleData(esoeID, TrustedESOERole.class)).andReturn(this.esoeRole).anyTimes();
		expect(this.esoeEntityData.getRoleData(TrustedESOERole.class)).andReturn(this.esoeRole).anyTimes();

		this.identifierGenerator = createMock(IdentifierGenerator.class);
		this.mocked.add(this.identifierGenerator);

		this.identifierCache = createMock(IdentifierCache.class);
		this.mocked.add(this.identifierCache);
		expect(this.identifierCache.containsIdentifier(this.samlID1)).andReturn(Boolean.TRUE).anyTimes();

		this.samlValidator = new SAMLValidatorImpl(this.identifierCache, 180);

		this.identifierCache.registerIdentifier(this.samlID1);

		this.internalSAMLValidator = new SAMLValidatorImpl(new IdentifierCacheImpl(), 180);

		this.wsClient = createMock(WSClient.class);
		this.mocked.add(this.wsClient);

		this.attributeProcessor = new AttributeProcessorImpl(this.metadata, this.wsClient, this.identifierGenerator, this.samlValidator, keyStoreResolver, esoeID, spepIdentifier, false, false);

		this.schemas = new String[]{SchemaConstants.samlProtocol, SchemaConstants.samlAssertion};
		this.responseMarshaller = new MarshallerImpl<Response>(Response.class.getPackage().getName(), this.schemas, keyStoreResolver);
	}

	private void startMock()
	{
		for (Object o : this.mocked) replay(o);
	}

	private void endMock()
	{
		for (Object o : this.mocked) verify(o);
	}

	/**
	 * Test method for {@link com.qut.middleware.spep.attribute.AttributeProcessor#doAttributeProcessing(com.qut.middleware.spep.sessions.PrincipalSession)}.
	 * @throws Exception
	 */
	@Test
	public void testBeginAttributeProcessing1a() throws Exception
	{
		Response response = buildResponse();

		Element responseDocument = this.responseMarshaller.marshallSignedElement(response);

		Capture<Element> captureRequest = new Capture<Element>();

		expect(this.wsClient.attributeAuthority(capture(captureRequest),(String)notNull())).andReturn(responseDocument);
		expect(this.identifierGenerator.generateSAMLID()).andReturn(this.samlID1).once();
		expect(this.metadata.resolveKey(this.keyName)).andReturn(this.publicKey).anyTimes();
		expect(this.metadata.resolveKey((String)notNull(), (BigInteger)notNull())).andReturn(this.publicKey).anyTimes();
		expect(this.esoeRole.getAttributeServiceEndpoint((String)notNull())).andReturn("").anyTimes();

		startMock();

		GregorianCalendar expiry = new GregorianCalendar();
		expiry.add(Calendar.DAY_OF_MONTH, 1);
		Date sessionNotOnOrAfter = expiry.getTime();

		PrincipalSession principalSession = new PrincipalSessionImpl();
		principalSession.setEsoeSessionID("sdofajoijroqiwjeorijqweorijqwoeirjqwerioqwjeroiqwjer");
		principalSession.setSessionNotOnOrAfter(sessionNotOnOrAfter);

		this.attributeProcessor.doAttributeProcessing(principalSession);

		endMock();

		assertTrue(captureRequest.getCaptured().size() == 1);

		Unmarshaller<AttributeQuery> attributeQueryUnmarshaller = new UnmarshallerImpl<AttributeQuery>(AttributeQuery.class.getPackage().getName(), new String[]{SchemaConstants.samlAssertion, SchemaConstants.samlProtocol});
		AttributeQuery query = attributeQueryUnmarshaller.unMarshallUnSigned(captureRequest.getCaptured().get(0));
		this.internalSAMLValidator.getRequestValidator().validate(query);

		assertTrue(principalSession.getAttributes().get("mail").contains(this.emailAddress));
		assertTrue(principalSession.getAttributes().get("uid").contains(this.uid));
	}

	/**
	 * Test method for {@link com.qut.middleware.spep.attribute.AttributeProcessor#doAttributeProcessing(com.qut.middleware.spep.sessions.PrincipalSession)}.
	 * @throws Exception
	 */
	@Test(expected = AttributeProcessingException.class)
	public void testBeginAttributeProcessing1b() throws Exception
	{
		Response response = buildResponse();
		response.setInResponseTo("non-existent-session");

		Element responseDocument = this.responseMarshaller.marshallSignedElement(response);

		expect(this.wsClient.attributeAuthority((Element)notNull(),(String)notNull())).andReturn(responseDocument);
		expect(this.identifierGenerator.generateSAMLID()).andReturn(this.samlID1).once();
		expect(this.metadata.resolveKey(this.keyName)).andReturn(this.publicKey).anyTimes();
		expect(this.metadata.resolveKey((String)notNull(), (BigInteger)notNull())).andReturn(this.publicKey).anyTimes();
		expect(this.esoeRole.getAttributeServiceEndpoint((String)notNull())).andReturn("").anyTimes();

		startMock();

		GregorianCalendar expiry = new GregorianCalendar();
		expiry.add(Calendar.DAY_OF_MONTH, 1);
		Date sessionNotOnOrAfter = expiry.getTime();

		PrincipalSession principalSession = new PrincipalSessionImpl();
		principalSession.setEsoeSessionID("sdofajoijroqiwjeorijqweorijqwoeirjqwerioqwjeroiqwjer");
		principalSession.setSessionNotOnOrAfter(sessionNotOnOrAfter);

		this.attributeProcessor.doAttributeProcessing(principalSession);

		endMock();

		assertTrue(principalSession.getAttributes().get("mail").contains(this.emailAddress));
		assertTrue(principalSession.getAttributes().get("uid").contains(this.uid));
	}

	/**
	 * Test method for {@link com.qut.middleware.spep.attribute.AttributeProcessor#doAttributeProcessing(com.qut.middleware.spep.sessions.PrincipalSession)}.
	 * @throws Exception
	 */
	@Test(expected = AttributeProcessingException.class)
	public void testBeginAttributeProcessing1c() throws Exception
	{
		Response response = buildResponse();
		response.getEncryptedAssertionsAndAssertions().clear();

		Element responseDocument = this.responseMarshaller.marshallSignedElement(response);

		expect(this.wsClient.attributeAuthority((Element)notNull(),(String)notNull())).andReturn(responseDocument);
		expect(this.identifierGenerator.generateSAMLID()).andReturn(this.samlID1).once();
		expect(this.metadata.resolveKey(this.keyName)).andReturn(this.publicKey).anyTimes();
		expect(this.metadata.resolveKey((String)notNull(), (BigInteger)notNull())).andReturn(this.publicKey).anyTimes();
		expect(this.esoeRole.getAttributeServiceEndpoint((String)notNull())).andReturn("").anyTimes();

		startMock();

		GregorianCalendar expiry = new GregorianCalendar();
		expiry.add(Calendar.DAY_OF_MONTH, 1);
		Date sessionNotOnOrAfter = expiry.getTime();

		PrincipalSession principalSession = new PrincipalSessionImpl();
		principalSession.setEsoeSessionID("sdofajoijroqiwjeorijqweorijqwoeirjqwerioqwjeroiqwjer");
		principalSession.setSessionNotOnOrAfter(sessionNotOnOrAfter);

		this.attributeProcessor.doAttributeProcessing(principalSession);

		endMock();

		assertTrue(principalSession.getAttributes().get("mail").contains(this.emailAddress));
		assertTrue(principalSession.getAttributes().get("uid").contains(this.uid));
	}

	/**
	 * @throws Exception
	 */
	@Test(expected = AttributeProcessingException.class)
	public void testBeginAttributeProcessing2a() throws Exception
	{
		this.attributeProcessor.doAttributeProcessing(null);
	}

	/**
	 * @throws Exception
	 */
	@Test(expected = AttributeProcessingException.class)
	public void testBeginAttributeProcessing2b() throws Exception
	{
		GregorianCalendar expiry = new GregorianCalendar();
		expiry.add(Calendar.DAY_OF_MONTH, 1);
		Date sessionNotOnOrAfter = expiry.getTime();

		PrincipalSession principalSession = new PrincipalSessionImpl();
		principalSession.setSessionNotOnOrAfter(sessionNotOnOrAfter);

		this.attributeProcessor.doAttributeProcessing(principalSession);
	}

	/**
	 * @throws Exception
	 */
	@Test(expected = AttributeProcessingException.class)
	public void testBeginAttributeProcessing2c() throws Exception
	{
		GregorianCalendar expiry = new GregorianCalendar();
		expiry.add(Calendar.DAY_OF_MONTH, 1);
		Date sessionNotOnOrAfter = expiry.getTime();

		PrincipalSession principalSession = new PrincipalSessionImpl();
		principalSession.setEsoeSessionID("sdofajoijroqiwjeorijqweorijqwoeirjqwerioqwjeroiqwjer");
		principalSession.setSessionNotOnOrAfter(sessionNotOnOrAfter);

		this.attributeProcessor.doAttributeProcessing(principalSession);
	}

	/**
	 * @throws Exception
	 */
	@Test(expected = AttributeProcessingException.class)
	public void testBeginAttributeProcessing2d() throws Exception
	{
		PrincipalSession principalSession = new PrincipalSessionImpl();
		principalSession.setEsoeSessionID("sdofajoijroqiwjeorijqweorijqwoeirjqwerioqwjeroiqwjer");

		this.attributeProcessor.doAttributeProcessing(principalSession);
	}

	private Response buildResponse() throws Exception
	{
		String responseID = "faoiwehroiqjweorijqwoeirj-oasdjogijqwoeijroqwiejroqwe";
		String subjectNameIDValue = "subject";
		String issuerNameIDValue = this.esoeID;
		this.emailAddress = "email@lol.com";
		this.uid = "subject";

		Response response = new Response();

		NameIDType issuer = new NameIDType();
		issuer.setValue(issuerNameIDValue);

		Status status = new Status();
		StatusCode statusCode = new StatusCode();
		statusCode.setValue(StatusCodeConstants.success);
		status.setStatusCode(statusCode);

		Subject subject = new Subject();
		NameIDType subjectNameID = new NameIDType();
		subjectNameID.setValue(subjectNameIDValue);
		subject.setNameID(subjectNameID);

		AttributeType uidAttribute = new AttributeType();
		uidAttribute.setFriendlyName("uid");
		uidAttribute.setName("uid");
		uidAttribute.setNameFormat(NameIDFormatConstants.unspecified);
		uidAttribute.getAttributeValues().add(this.uid);

		AttributeType mailAttribute = new AttributeType();
		mailAttribute.setFriendlyName("mail");
		mailAttribute.setName("mail");
		mailAttribute.setNameFormat(NameIDFormatConstants.unspecified);
		mailAttribute.getAttributeValues().add(this.emailAddress);

		AttributeStatement attributeStatement = new AttributeStatement();
		attributeStatement.getEncryptedAttributesAndAttributes().add(uidAttribute);
		attributeStatement.getEncryptedAttributesAndAttributes().add(mailAttribute);

		Assertion assertion = new Assertion();
		assertion.setID("a8197235981723947192837491283749812734-9782305987120893490182340981203948");
		assertion.setIssueInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));
		assertion.setIssuer(issuer);
		assertion.setSubject(subject);
		assertion.setVersion(VersionConstants.saml20);
		assertion.getAuthnStatementsAndAuthzDecisionStatementsAndAttributeStatements().add(attributeStatement);

		/* subject MUST contain a SubjectConfirmation */
		SubjectConfirmation confirmation = new SubjectConfirmation();
		confirmation.setMethod(ConfirmationMethodConstants.bearer);
		SubjectConfirmationDataType confirmationData = new SubjectConfirmationDataType();
		confirmationData.setRecipient(this.assertionConsumerServiceLocation);
		confirmationData.setInResponseTo(responseID);
		confirmationData.setNotOnOrAfter(this.generateXMLCalendar(100));
		confirmation.setSubjectConfirmationData(confirmationData);
		subject.getSubjectConfirmationNonID().add(confirmation);

		response.setID(responseID);
		response.setInResponseTo(this.samlID1);
		response.setIssueInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));
		response.setIssuer(issuer);
		response.setSignature(new Signature());
		response.setStatus(status);
		response.setVersion(VersionConstants.saml20);
		response.getEncryptedAssertionsAndAssertions().add(assertion);

		return response;
	}

	private XMLGregorianCalendar generateXMLCalendar(int offset)
	{
		GregorianCalendar calendar;
		XMLGregorianCalendar xmlCalendar;

		SimpleTimeZone tz = new SimpleTimeZone(0, ConfigurationConstants.timeZone);
		calendar = new GregorianCalendar(tz);
		calendar.add(Calendar.SECOND, offset);
		xmlCalendar = new XMLGregorianCalendarImpl(calendar);

		return xmlCalendar;
	}
}

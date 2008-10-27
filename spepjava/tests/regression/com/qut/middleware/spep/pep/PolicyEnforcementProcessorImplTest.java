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
 * Creation Date: 11/12/2006
 * 
 * Purpose:
 */
package com.qut.middleware.spep.pep;

import static com.qut.middleware.test.regression.Capture.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.not;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.Vector;

import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Before;
import org.junit.Test;
import org.w3._2000._09.xmldsig_.Signature;
import org.w3c.dom.Element;

import com.qut.middleware.crypto.KeystoreResolver;
import com.qut.middleware.crypto.impl.KeystoreResolverImpl;
import com.qut.middleware.metadata.bean.EntityData;
import com.qut.middleware.metadata.bean.saml.TrustedESOERole;
import com.qut.middleware.metadata.processor.MetadataProcessor;
import com.qut.middleware.saml2.ConfirmationMethodConstants;
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
import com.qut.middleware.saml2.identifier.IdentifierCache;
import com.qut.middleware.saml2.identifier.IdentifierGenerator;
import com.qut.middleware.saml2.schemas.assertion.Assertion;
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.assertion.StatementAbstractType;
import com.qut.middleware.saml2.schemas.assertion.Subject;
import com.qut.middleware.saml2.schemas.assertion.SubjectConfirmation;
import com.qut.middleware.saml2.schemas.assertion.SubjectConfirmationDataType;
import com.qut.middleware.saml2.schemas.esoe.lxacml.AttributeAssignment;
import com.qut.middleware.saml2.schemas.esoe.lxacml.EffectType;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Obligation;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Obligations;
import com.qut.middleware.saml2.schemas.esoe.lxacml.assertion.LXACMLAuthzDecisionStatement;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.DecisionType;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.Result;
import com.qut.middleware.saml2.schemas.esoe.lxacml.grouptarget.GroupTarget;
import com.qut.middleware.saml2.schemas.esoe.lxacml.protocol.LXACMLAuthzDecisionQuery;
import com.qut.middleware.saml2.schemas.esoe.protocol.ClearAuthzCacheRequest;
import com.qut.middleware.saml2.schemas.esoe.protocol.ClearAuthzCacheResponse;
import com.qut.middleware.saml2.schemas.protocol.Extensions;
import com.qut.middleware.saml2.schemas.protocol.RequestAbstractType;
import com.qut.middleware.saml2.schemas.protocol.Response;
import com.qut.middleware.saml2.schemas.protocol.Status;
import com.qut.middleware.saml2.schemas.protocol.StatusCode;
import com.qut.middleware.saml2.schemas.protocol.StatusResponseType;
import com.qut.middleware.saml2.validator.impl.SAMLValidatorImpl;
import com.qut.middleware.spep.ConfigurationConstants;
import com.qut.middleware.spep.pep.PolicyEnforcementProcessor.decision;
import com.qut.middleware.spep.pep.impl.PolicyEnforcementProcessorImpl;
import com.qut.middleware.spep.sessions.PrincipalSession;
import com.qut.middleware.spep.sessions.SessionCache;
import com.qut.middleware.spep.ws.WSClient;
import com.qut.middleware.test.regression.Capture;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

/** */
@SuppressWarnings("nls")
public class PolicyEnforcementProcessorImplTest
{
	private static String authzQueryPackages;
	private static String cacheClearPackages;
	private static String groupTargetPackages;
	private static Marshaller<Response> responseMarshaller;
	private static KeystoreResolver keyStoreResolver;
	private static Marshaller<ClearAuthzCacheRequest> clearAuthzCacheRequestMarshaller;
	private static Unmarshaller<ClearAuthzCacheResponse> clearAuthzCacheResponseUnmarshaller;
	private static Marshaller<GroupTarget> groupTargetElementMarshaller;

	private static String ATTRIBUTE_ID = "lxacmlpdp:obligation:cachetargets:updateusercache"; //$NON-NLS-1$
	private static String OBLIGATION_ID = "lxacmlpdp:obligation:cachetargets"; //$NON-NLS-1$
	private SessionGroupCache sessionGroupCache;
	private WSClient wsClient;
	private IdentifierGenerator identifierGenerator;
	private MetadataProcessor metadata;
	private PolicyEnforcementProcessor processor;
	private String spepIdentifier;
	private String documentID;
	private String authzServiceEndpoint;
	private String resource;
	private String samlID;
	private String sessionID;
	private SAMLValidatorImpl samlValidator;
	private PrincipalSession principalSession;
	private SessionCache sessionCache;
	private String esoeIdentifier;
	private List<Object> mocked;
	private EntityData esoeEntityData;
	private TrustedESOERole esoeRole;
	
	public PolicyEnforcementProcessorImplTest() throws Exception
	{
		keyStoreResolver = new KeystoreResolverImpl(new File( "tests" + File.separator + "testdata" + File.separator + "testkeystore.ks"), "Es0EKs54P4SSPK", "esoeprimary", "Es0EKs54P4SSPK");

		authzQueryPackages = LXACMLAuthzDecisionQuery.class.getPackage().getName() + ":" + //$NON-NLS-1$
			GroupTarget.class.getPackage().getName() + ":" + //$NON-NLS-1$
			StatementAbstractType.class.getPackage().getName() + ":" + //$NON-NLS-1$
			LXACMLAuthzDecisionStatement.class.getPackage().getName() + ":" + //$NON-NLS-1$
			Response.class.getPackage().getName();

		String[] schemas = new String[]{SchemaConstants.samlProtocol, SchemaConstants.lxacml,
			SchemaConstants.lxacmlSAMLProtocol, SchemaConstants.lxacmlGroupTarget,
			SchemaConstants.lxacmlSAMLAssertion, SchemaConstants.samlAssertion};

		responseMarshaller = new MarshallerImpl<Response>(authzQueryPackages,  schemas, keyStoreResolver);
		
		cacheClearPackages = ClearAuthzCacheRequest.class.getPackage().getName() + ":" +
			StatusResponseType.class.getPackage().getName() + ":" +
			RequestAbstractType.class.getPackage().getName();
		
		String[] cacheClearSchemas = new String[]{SchemaConstants.samlProtocol, SchemaConstants.samlAssertion, SchemaConstants.esoeProtocol};
		
		clearAuthzCacheRequestMarshaller = new MarshallerImpl<ClearAuthzCacheRequest>(cacheClearPackages, cacheClearSchemas, keyStoreResolver);
		clearAuthzCacheResponseUnmarshaller = new UnmarshallerImpl<ClearAuthzCacheResponse>(cacheClearPackages, cacheClearSchemas, keyStoreResolver);
		
		groupTargetPackages = GroupTarget.class.getPackage().getName();
		
		String[] groupTargetSchemas = new String[]{SchemaConstants.lxacmlGroupTarget};
		
		groupTargetElementMarshaller = new MarshallerImpl<GroupTarget>(groupTargetPackages, groupTargetSchemas);
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		this.mocked = new ArrayList<Object>();
		
		this.spepIdentifier = "_joqijoiqfjoimaslkjflaksjdflkasjdlfasdf-awjoertjq908jr9182j30r91j203r9";
		this.esoeIdentifier = "esoe.url";
		this.documentID = "_21830958712983749-12538719283749182734987-1oasodifjoqiwjfoiajsdf";
		this.authzServiceEndpoint = "https://esoe.url/authz";
		this.sessionID = "_uu9t8u98q3u032u509qwui095i0923i-4iq02395u0q9wuetoijsdlkgjlksda";
		
		this.sessionGroupCache = createMock(SessionGroupCache.class);
		this.mocked.add(this.sessionGroupCache);
		this.wsClient = createMock(WSClient.class);
		this.mocked.add(this.wsClient);
		
		this.identifierGenerator = createMock(IdentifierGenerator.class);
		this.mocked.add(this.identifierGenerator);
		expect(this.identifierGenerator.generateSAMLID()).andReturn(this.documentID).anyTimes();
		

		this.metadata = createMock(MetadataProcessor.class);
		this.mocked.add(this.metadata);
		this.esoeEntityData = createMock(EntityData.class);
		this.mocked.add(this.esoeEntityData);
		this.esoeRole = createMock(TrustedESOERole.class);
		this.mocked.add(this.esoeRole);
		expect(this.metadata.getEntityData(this.esoeIdentifier)).andReturn(this.esoeEntityData).anyTimes();
		expect(this.metadata.getEntityRoleData(this.esoeIdentifier, TrustedESOERole.class)).andReturn(this.esoeRole).anyTimes();
		expect(this.esoeEntityData.getRoleData(TrustedESOERole.class)).andReturn(this.esoeRole).anyTimes();
		expect(this.esoeRole.getLXACMLAuthzServiceEndpoint((String)notNull())).andReturn(this.authzServiceEndpoint).anyTimes();
		expect(this.metadata.resolveKey("esoeprimary")).andReturn(keyStoreResolver.getLocalPublicKey()).anyTimes();
		
		IdentifierCache identifierCache = createMock(IdentifierCache.class);
		this.mocked.add(identifierCache);
		identifierCache.registerIdentifier((String)notNull());
		expectLastCall().anyTimes();
		
		this.samlValidator = new SAMLValidatorImpl(identifierCache, 180);
		
		this.principalSession = createMock(PrincipalSession.class);
		this.mocked.add(this.principalSession);
		this.sessionCache = createMock(SessionCache.class);
		this.mocked.add(this.sessionCache);
		expect(this.sessionCache.getPrincipalSession((String)notNull())).andReturn(this.principalSession).anyTimes();
		
		this.processor = new PolicyEnforcementProcessorImpl(this.sessionCache, this.sessionGroupCache, this.wsClient, this.identifierGenerator, this.metadata, keyStoreResolver, this.samlValidator, this.esoeIdentifier, this.spepIdentifier, false, false);
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
	 * Test method for {@link com.qut.middleware.spep.pep.PolicyEnforcementProcessor#makeAuthzDecision(com.qut.middleware.spep.sessions.PrincipalSession, java.lang.String)}.
	 * @throws Exception 
	 */
	@Test
	public void testMakeAuthzDecision1a() throws Exception
	{
		
		
		this.resource = "/secure/securedocument.html";

		expect(this.sessionGroupCache.makeCachedAuthzDecision(this.principalSession, this.resource)).andReturn(decision.deny).anyTimes();
		expect(this.sessionGroupCache.makeCachedAuthzDecision(this.principalSession, this.resource, null)).andReturn(decision.deny).anyTimes();
		expect(this.sessionCache.getPrincipalSession( this.sessionID )).andReturn( this.principalSession ).anyTimes();
		
		startMock();
		
		assertEquals(decision.deny, this.processor.makeAuthzDecision(sessionID, this.resource));
		
		endMock();
	}

	/**
	 * Test method for {@link com.qut.middleware.spep.pep.PolicyEnforcementProcessor#makeAuthzDecision(com.qut.middleware.spep.sessions.PrincipalSession, java.lang.String)}.
	 */
	/**
	 * @throws Exception
	 */
	@Test
	public void testMakeAuthzDecision1b() throws Exception
	{
		
		
		this.resource = "/secure/securedocument.html";

		expect(this.sessionGroupCache.makeCachedAuthzDecision(this.principalSession, this.resource)).andReturn(decision.permit).anyTimes();
		expect(this.sessionGroupCache.makeCachedAuthzDecision(this.principalSession, this.resource, null)).andReturn(decision.permit).anyTimes();
		expect(this.sessionCache.getPrincipalSession( this.sessionID )).andReturn( this.principalSession ).anyTimes();
		
		startMock();
		
		assertEquals(decision.permit, this.processor.makeAuthzDecision(sessionID, this.resource));
		
		endMock();
	}

	/**
	 * Test method for {@link com.qut.middleware.spep.pep.PolicyEnforcementProcessor#makeAuthzDecision(com.qut.middleware.spep.sessions.PrincipalSession, java.lang.String)}.
	 */
	@Test
	public void testMakeAuthzDecision1c() throws Exception
	{
		
		
		this.resource = "/secure/securedocument.html";

		expect(this.sessionGroupCache.makeCachedAuthzDecision(principalSession, resource)).andReturn(decision.error).anyTimes();
		expect(this.sessionGroupCache.makeCachedAuthzDecision(this.principalSession, this.resource, null)).andReturn(decision.error).anyTimes();
		expect(this.sessionCache.getPrincipalSession( this.sessionID )).andReturn( this.principalSession ).anyTimes();
		
		startMock();
		
		assertEquals(decision.error, this.processor.makeAuthzDecision(sessionID, resource));
		
		endMock();
	}
	
	/**
	 * Test method for {@link com.qut.middleware.spep.pep.PolicyEnforcementProcessor#makeAuthzDecision(com.qut.middleware.spep.sessions.PrincipalSession, java.lang.String)}.
	 */
	@Test
	public void testMakeAuthzDecision2a() throws Exception
	{
		
		
		this.resource = "/secure/securedocument.html";
		this.samlID = "_29387123948719283749182374981723498712934871923874-972130587190238409128304";
		this.sessionID = "_890981059810239480129348-123048-123580192835491283049182-305812359182039";
		
		Obligations obligations = new Obligations();
		
		String groupTargetID = "/secure/.*";
		String authzTarget1 = "/secure/.*.html";
		String authzTarget2 = "/secure/secure.*";
		
		List<String> authzTargets = new Vector<String>();
		authzTargets.add(authzTarget1);
		authzTargets.add(authzTarget2);

		GroupTarget groupTarget = new GroupTarget();
		groupTarget.setGroupTargetID(groupTargetID);
		
		groupTarget.getAuthzTargets().addAll(authzTargets);
		
		AttributeAssignment attributeAssignment = new AttributeAssignment();
		attributeAssignment.setAttributeId(ATTRIBUTE_ID);
		attributeAssignment.getContent().add(groupTarget);
		
		Obligation obligation1 = new Obligation();
		obligation1.setFulfillOn(EffectType.PERMIT);
		obligation1.setObligationId(OBLIGATION_ID);
		obligation1.getAttributeAssignments().add(attributeAssignment);
		obligations.getObligations().add(obligation1);
		
		Element responseDocument = generateResponse(decision.permit, obligations);
		
		Capture<String> captureRequest = new Capture<String>();
		Capture<List<String>> captureAuthzTargets = new Capture<List<String>>();
		
		expect(this.sessionGroupCache.makeCachedAuthzDecision(this.principalSession, resource)).andReturn(decision.notcached).anyTimes();
		expect(this.sessionGroupCache.makeCachedAuthzDecision(this.principalSession, resource, null)).andReturn(decision.notcached).anyTimes();
		this.sessionGroupCache.updateCache(eq(this.principalSession), eq(groupTargetID), capture(captureAuthzTargets), (eq((String)null)), not(eq(decision.notcached)));
		expectLastCall().anyTimes();
		expect(this.principalSession.getEsoeSessionID()).andReturn(samlID).anyTimes();
		expect(this.wsClient.policyDecisionPoint((Element)notNull(), (String)notNull())).andReturn(responseDocument).anyTimes();
		
		startMock();
		
		assertEquals(decision.permit, this.processor.makeAuthzDecision(sessionID, resource));
		
		endMock();
		
		assertTrue(captureAuthzTargets.getCaptured().size() > 0);
		assertTrue(captureAuthzTargets.getCaptured().get(0).containsAll(authzTargets));
		assertTrue(authzTargets.containsAll(captureAuthzTargets.getCaptured().get(0)));
	}
	
	/**
	 * Test method for {@link com.qut.middleware.spep.pep.PolicyEnforcementProcessor#makeAuthzDecision(com.qut.middleware.spep.sessions.PrincipalSession, java.lang.String)}.
	 */
	@Test
	public void testMakeAuthzDecision2b() throws Exception
	{
		
		
		this.resource = "/secure/securedocument.html";
		this.samlID = "_29387123948719283749182374981723498712934871923874-972130587190238409128304";
		this.sessionID = "_890981059810239480129348-123048-123580192835491283049182-305812359182039";
		
		Obligations obligations = new Obligations();
		
		String groupTargetID = "/secure/.*";

		GroupTarget groupTarget = new GroupTarget();
		groupTarget.setGroupTargetID(groupTargetID);
		groupTarget.getAuthzTargets().add(groupTargetID);
		
		AttributeAssignment attributeAssignment = new AttributeAssignment();
		attributeAssignment.setAttributeId(ATTRIBUTE_ID);
		attributeAssignment.getContent().add(groupTarget);
		
		Obligation obligation1 = new Obligation();
		obligation1.setFulfillOn(EffectType.DENY);
		obligation1.setObligationId(OBLIGATION_ID);
		obligation1.getAttributeAssignments().add(attributeAssignment);
		obligations.getObligations().add(obligation1);
		
		Element responseDocument = generateResponse(decision.deny, obligations);
		
		Capture<String> captureRequest = new Capture<String>();
		Capture<List<String>> captureAuthzTargets = new Capture<List<String>>();
		
		expect(this.sessionGroupCache.makeCachedAuthzDecision(this.principalSession, resource)).andReturn(decision.notcached).anyTimes();
		expect(this.sessionGroupCache.makeCachedAuthzDecision(this.principalSession, resource, null)).andReturn(decision.notcached).anyTimes();
		this.sessionGroupCache.updateCache(eq(this.principalSession), eq(groupTargetID), capture(captureAuthzTargets), (eq((String)null)), not(eq(decision.notcached)));
		expectLastCall().anyTimes();
		expect(this.principalSession.getEsoeSessionID()).andReturn(samlID).anyTimes();
		expect(this.wsClient.policyDecisionPoint((Element)notNull(), (String)notNull())).andReturn(responseDocument).anyTimes();
		
		this.sessionCache.terminatePrincipalSession(this.principalSession);
		expectLastCall().anyTimes();
		
		startMock();
		
		assertEquals(decision.deny, this.processor.makeAuthzDecision(sessionID, resource));
		
		endMock();
	}
	
	/**
	 * Test method for {@link com.qut.middleware.spep.pep.PolicyEnforcementProcessor#makeAuthzDecision(com.qut.middleware.spep.sessions.PrincipalSession, java.lang.String)}.
	 */
	@Test
	public void testMakeAuthzDecision2c() throws Exception
	{
		
		
		this.resource = "/secure/securedocument.html";
		this.samlID = "_29387123948719283749182374981723498712934871923874-972130587190238409128304";
		this.sessionID = "_890981059810239480129348-123048-123580192835491283049182-305812359182039";
		
		Obligations obligations = new Obligations();
		
		String groupTargetID = "/secure/.*";
		String authzTarget1 = "/secure/.*.html";
		String authzTarget2 = "/secure/secure.*";
		String groupTargetID2 = "/secure/.*ent.html";
		String authz2Target1 = "/secure/.*.htm.*";
		String authz2Target2 = "/secure/securedoc.*";
		
		List<String> authzTargets = new Vector<String>();
		authzTargets.add(authzTarget1);
		authzTargets.add(authzTarget2);
		List<String> authz2Targets = new Vector<String>();
		authz2Targets.add(authz2Target1);
		authz2Targets.add(authz2Target2);

		GroupTarget groupTarget = new GroupTarget();
		groupTarget.setGroupTargetID(groupTargetID);
		
		groupTarget.getAuthzTargets().addAll(authzTargets);
		
		GroupTarget groupTarget2 = new GroupTarget();
		groupTarget2.setGroupTargetID(groupTargetID2);
		
		groupTarget2.getAuthzTargets().addAll(authz2Targets);
		
		AttributeAssignment attributeAssignment = new AttributeAssignment();
		attributeAssignment.setAttributeId(ATTRIBUTE_ID);
		attributeAssignment.getContent().add(groupTarget);
		attributeAssignment.getContent().add(groupTarget2);
		
		Obligation obligation1 = new Obligation();
		obligation1.setFulfillOn(EffectType.PERMIT);
		obligation1.setObligationId(OBLIGATION_ID);
		obligation1.getAttributeAssignments().add(attributeAssignment);
		obligations.getObligations().add(obligation1);
		
		Element responseDocument = generateResponse(decision.permit, obligations);
		
		Capture<String> captureRequest = new Capture<String>();
		Capture<List<String>> captureAuthzTargets = new Capture<List<String>>();
		
		expect(this.sessionGroupCache.makeCachedAuthzDecision(this.principalSession, this.resource)).andReturn(decision.notcached).anyTimes();
		expect(this.sessionGroupCache.makeCachedAuthzDecision(this.principalSession, this.resource, null)).andReturn(decision.notcached).anyTimes();
		this.sessionGroupCache.updateCache(eq(this.principalSession), eq(groupTargetID), capture(captureAuthzTargets), (eq((String)null)), not(eq(decision.notcached)));
		expectLastCall().anyTimes();
		this.sessionGroupCache.updateCache(eq(this.principalSession), eq(groupTargetID2), capture(captureAuthzTargets), (eq((String)null)), not(eq(decision.notcached)));
		expectLastCall().anyTimes();
		expect(this.principalSession.getEsoeSessionID()).andReturn(samlID).anyTimes();
		expect(this.wsClient.policyDecisionPoint((Element)notNull(), (String)notNull())).andReturn(responseDocument).anyTimes();
		
		startMock();
		
		assertEquals(decision.permit, this.processor.makeAuthzDecision(sessionID, resource));
		
		endMock();
		
		assertTrue(captureAuthzTargets.getCaptured().size() > 1);
		assertTrue(captureAuthzTargets.getCaptured().get(0).containsAll(authzTargets));
		assertTrue(authzTargets.containsAll(captureAuthzTargets.getCaptured().get(0)));
		assertTrue(captureAuthzTargets.getCaptured().get(1).containsAll(authz2Targets));
		assertTrue(authz2Targets.containsAll(captureAuthzTargets.getCaptured().get(1)));
	}
	
	@Test
	public void testAuthzCacheClear1() throws Exception
	{
		ClearAuthzCacheRequest request = new ClearAuthzCacheRequest();
		NameIDType issuer = new NameIDType();
		issuer.setValue(this.esoeIdentifier);
		String requestID = "_asfjopiwejoiqjweorijqwoejroqwiejtroiqwjetoiqwjetpoiqjweporijqwpeorjipqwer";
		request.setID(requestID);
		request.setIssueInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));
		request.setIssuer(issuer);
		request.setReason("Lol");
		request.setSignature(new Signature());
		request.setVersion(VersionConstants.saml20);
		
		Element requestDocument = clearAuthzCacheRequestMarshaller.marshallSignedElement(request);
		
		Capture<Map<String,List<String>>> mapCapture = new Capture<Map<String,List<String>>>();
		this.sessionGroupCache.clearCache( capture( mapCapture ) );
		expectLastCall().once();


		startMock();
		
		Element responseDocument = this.processor.authzCacheClear(requestDocument);
		
		endMock();
		
		// There will only be 1 element in the "captured" list if endMock() didn't throw.
		Map<String,List<String>> cacheClearMap = mapCapture.getCaptured().get(0);
		
		assertEquals( cacheClearMap.size(), 0 );

		ClearAuthzCacheResponse response = clearAuthzCacheResponseUnmarshaller.unMarshallSigned(responseDocument);
		
		assertEquals(StatusCodeConstants.success, response.getStatus().getStatusCode().getValue());
		assertEquals(requestID, response.getInResponseTo());
	}
	
	@Test
	public void testAuthzCacheClear2() throws Exception
	{
		ClearAuthzCacheRequest request = new ClearAuthzCacheRequest();
		NameIDType issuer = new NameIDType();
		issuer.setValue(this.esoeIdentifier);
		String requestID = "_asfjopiwejoiqjweorijqwoejroqwiejtroiqwjetoiqwjetpoiqjweporijqwpeorjipqwer";
		request.setID(requestID);
		request.setIssueInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));
		request.setIssuer(issuer);
		request.setReason("Lol");
		request.setSignature(new Signature());
		request.setVersion(VersionConstants.saml20);
		
		List<String> authzTargets1 = new Vector<String>();
		authzTargets1.add("/group/target/1/authz1");
		authzTargets1.add("/group/target/1/authz2");
		
		String groupTargetID1 = "/group/target/1";
		GroupTarget groupTarget1 = new GroupTarget();
		groupTarget1.setGroupTargetID(groupTargetID1);
		groupTarget1.getAuthzTargets().addAll(authzTargets1);
		
		Element groupTargetElement1 = groupTargetElementMarshaller.marshallUnSignedElement(groupTarget1);
		
		Extensions extensions = new Extensions();
		request.setExtensions(extensions);
		extensions.getAnies().add(groupTargetElement1);
		
		Element requestDocument = clearAuthzCacheRequestMarshaller.marshallSignedElement(request);
		
		Capture<Map<String,List<String>>> captureGroupTargetMap = new Capture<Map<String,List<String>>>();
		
		this.sessionGroupCache.clearCache(capture(captureGroupTargetMap));
		expectLastCall().once();


		startMock();
		
		Element responseDocument = this.processor.authzCacheClear(requestDocument);
		
		endMock();
		

		ClearAuthzCacheResponse response = clearAuthzCacheResponseUnmarshaller.unMarshallSigned(responseDocument);
		
		assertEquals(StatusCodeConstants.success, response.getStatus().getStatusCode().getValue());
		assertEquals(requestID, response.getInResponseTo());
		
		List<Map<String,List<String>>> captured = captureGroupTargetMap.getCaptured();
		assertEquals(1, captured.size());
		Map<String,List<String>> groupTargetMap = captured.get(0);
		assertNotNull("Group target was not processed", groupTargetMap.get(groupTargetID1));
		assertTrue("All authz targets were not processed", groupTargetMap.get(groupTargetID1).containsAll(authzTargets1));
		assertTrue("Unexpected authz targets were processed", authzTargets1.containsAll(groupTargetMap.get(groupTargetID1)));
	}
	
	@Test
	public void testAuthzCacheClear2a() throws Exception
	{
		ClearAuthzCacheRequest request = new ClearAuthzCacheRequest();
		NameIDType issuer = new NameIDType();
		issuer.setValue(this.esoeIdentifier);
		String requestID = "_asfjopiwejoiqjweorijqwoejroqwiejtroiqwjetoiqwjetpoiqjweporijqwpeorjipqwer";
		request.setID(requestID);
		request.setIssueInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));
		request.setIssuer(issuer);
		request.setReason("Lol");
		request.setSignature(new Signature());
		request.setVersion(VersionConstants.saml20);
		
		List<String> authzTargets1 = new Vector<String>();
		authzTargets1.add("/group/target/1/authz1");
		authzTargets1.add("/group/target/1/authz2");
		
		String groupTargetID1 = "/group/target/1";
		GroupTarget groupTarget1 = new GroupTarget();
		groupTarget1.setGroupTargetID(groupTargetID1);
		groupTarget1.getAuthzTargets().addAll(authzTargets1);
		
		List<String> authzTargets2 = new Vector<String>();
		authzTargets2.add("/group/target/2/authz1");
		authzTargets2.add("/group/target/2/authz2");
		authzTargets2.add("/group/target/2/authz3");
		
		String groupTargetID2 = "/group/target/2";
		GroupTarget groupTarget2 = new GroupTarget();
		groupTarget2.setGroupTargetID(groupTargetID2);
		groupTarget2.getAuthzTargets().addAll(authzTargets2);
		
		Element groupTargetElement1 = groupTargetElementMarshaller.marshallUnSignedElement(groupTarget1);
		Element groupTargetElement2 = groupTargetElementMarshaller.marshallUnSignedElement(groupTarget2);
		
		Extensions extensions = new Extensions();
		request.setExtensions(extensions);
		extensions.getAnies().add(groupTargetElement1);
		extensions.getAnies().add(groupTargetElement2);
		
		Element requestDocument = clearAuthzCacheRequestMarshaller.marshallSignedElement(request);
		
		Capture<Map<String,List<String>>> captureGroupTargetMap = new Capture<Map<String,List<String>>>();
		
		this.sessionGroupCache.clearCache(capture(captureGroupTargetMap));
		expectLastCall().once();


		startMock();
		
		Element responseDocument = this.processor.authzCacheClear(requestDocument);
		
		endMock();
		

		ClearAuthzCacheResponse response = clearAuthzCacheResponseUnmarshaller.unMarshallSigned(responseDocument);
		
		assertEquals(StatusCodeConstants.success, response.getStatus().getStatusCode().getValue());
		assertEquals(requestID, response.getInResponseTo());
		
		List<Map<String,List<String>>> captured = captureGroupTargetMap.getCaptured();
		assertEquals(1, captured.size());
		Map<String,List<String>> groupTargetMap = captured.get(0);
		assertNotNull("(1) Group target was not processed", groupTargetMap.get(groupTargetID1));
		assertTrue("(1) All authz targets were not processed", groupTargetMap.get(groupTargetID1).containsAll(authzTargets1));
		assertTrue("(1) Unexpected authz targets were processed", authzTargets1.containsAll(groupTargetMap.get(groupTargetID1)));
		assertNotNull("(2) Group target was not processed", groupTargetMap.get(groupTargetID2));
		assertTrue("(2) All authz targets were not processed", groupTargetMap.get(groupTargetID2).containsAll(authzTargets2));
		assertTrue("(2) Unexpected authz targets were processed", authzTargets2.containsAll(groupTargetMap.get(groupTargetID2)));
	}
	
	@Test
	public void testAuthzCacheClear3() throws Exception
	{
		ClearAuthzCacheRequest request = new ClearAuthzCacheRequest();
		NameIDType issuer = new NameIDType();
		issuer.setValue(this.esoeIdentifier);
		String requestID = "_asfjopiwejoiqjweorijqwoejroqwiejtroiqwjetoiqwjetpoiqjweporijqwpeorjipqwer";
		Subject subject = new Subject();
		NameIDType identifier = new NameIDType();
		identifier.setValue("10234-123");
		subject.setNameID(identifier);
		request.setSubject(subject);
		request.setID(requestID);
		request.setIssueInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));
		request.setIssuer(issuer);
		request.setReason("Lol");
		request.setSignature(new Signature());
		request.setVersion(VersionConstants.saml20);
		
		Element requestDocument = clearAuthzCacheRequestMarshaller.marshallSignedElement(request);

		expect(sessionCache.getPrincipalSessionByEsoeSessionID("10234-123")).andReturn(principalSession);
		sessionGroupCache.clearPrincipalSession(principalSession);
		
		startMock();
		
		Element responseDocument = this.processor.authzCacheClear(requestDocument);
		
		endMock();
		
		ClearAuthzCacheResponse response = clearAuthzCacheResponseUnmarshaller.unMarshallSigned(responseDocument);
		
		assertEquals(StatusCodeConstants.success, response.getStatus().getStatusCode().getValue());
		assertEquals(requestID, response.getInResponseTo());
	}

	protected Element generateResponse(decision desiredDecision, Obligations obligations) throws SignatureValueException, ReferenceValueException, UnmarshallerException, MarshallerException
	{
		NameIDType issuer = new NameIDType();
		issuer.setValue(this.esoeIdentifier);
		
		DecisionType decision = null;
		String statusMessage = null;
		String statusCodeString = null;
		
		switch(desiredDecision)
		{
			case permit:
				decision = DecisionType.PERMIT;
				statusMessage = "Permitted";
				statusCodeString = StatusCodeConstants.success;
				break;
			case deny:
				decision = DecisionType.DENY;
				statusMessage = "Denied.";
				statusCodeString = StatusCodeConstants.authnFailed;
				break;
			case error:
				statusMessage = "Error.";
				statusCodeString = StatusCodeConstants.requestUnsupported;
				break;
			case notcached:
				throw new UnsupportedOperationException();
		}
		
		Status status = new Status();
		StatusCode statusCode = new StatusCode();
		statusCode.setValue(statusCodeString);
		status.setStatusCode(statusCode);
		status.setStatusMessage(statusMessage);
		
		Response response = new Response();
		response.setID("_918275987192387409182304981234-01923598712398709128304981203498");
		response.setInResponseTo(this.samlID);
		response.setIssueInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));
		response.setSignature(new Signature());
		response.setStatus(status);
		response.setVersion(VersionConstants.saml20);
		
		if (!desiredDecision.equals(com.qut.middleware.spep.pep.PolicyEnforcementProcessor.decision.error))
		{
			com.qut.middleware.saml2.schemas.esoe.lxacml.context.Status lxacmlStatus = new com.qut.middleware.saml2.schemas.esoe.lxacml.context.Status();
			lxacmlStatus.setStatusMessage(statusMessage);
			
			Result result = new Result();
			result.setDecision(decision);
			result.setStatus(lxacmlStatus);
			result.setObligations(obligations);
			
			com.qut.middleware.saml2.schemas.esoe.lxacml.context.Response lxacmlResponse = new com.qut.middleware.saml2.schemas.esoe.lxacml.context.Response();
			lxacmlResponse.setResult(result);
			
			LXACMLAuthzDecisionStatement lxacmlAuthzDecisionStatement = new LXACMLAuthzDecisionStatement();
			lxacmlAuthzDecisionStatement.setResponse(lxacmlResponse);
			
			Assertion assertion = new Assertion();
			
			Subject subject = new Subject();
			NameIDType subjectNameID = new NameIDType();
			subjectNameID.setValue("whatever");
			subject.setNameID(subjectNameID);
			
			/* subject MUST contain a SubjectConfirmation */
			SubjectConfirmation confirmation = new SubjectConfirmation();
			confirmation.setMethod(ConfirmationMethodConstants.bearer);
			SubjectConfirmationDataType confirmationData = new SubjectConfirmationDataType();
			confirmationData.setInResponseTo(this.samlID);
			confirmationData.setNotOnOrAfter(this.generateXMLCalendar(100));
			confirmation.setSubjectConfirmationData(confirmationData);
			subject.getSubjectConfirmationNonID().add(confirmation);
			
			assertion.setID("_59182739487129384791823749817-1239084719023850912830498");
			assertion.setIssueInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));
			assertion.setIssuer(issuer);
			assertion.setVersion(VersionConstants.saml20);
			assertion.getAuthnStatementsAndAuthzDecisionStatementsAndAttributeStatements().add(lxacmlAuthzDecisionStatement);
			assertion.setSubject(subject);
			
			response.getEncryptedAssertionsAndAssertions().add(assertion);
		}
				
		Element responseDocument = this.responseMarshaller.marshallSignedElement(response);
		
		return responseDocument;
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

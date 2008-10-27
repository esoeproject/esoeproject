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
 * Creation Date: 15/12/2006
 * 
 * Purpose: Functional test for the PEP component of the SPEP
 */
package com.qut.middleware.spep.pep;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.Vector;
import java.util.Map.Entry;

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
import com.qut.middleware.saml2.exception.UnmarshallerException;
import com.qut.middleware.saml2.handler.Marshaller;
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
import com.qut.middleware.spep.pep.impl.SessionGroupCacheImpl;
import com.qut.middleware.spep.sessions.PrincipalSession;
import com.qut.middleware.spep.sessions.SessionCache;
import com.qut.middleware.spep.ws.WSClient;
import com.qut.middleware.spep.ws.exception.WSClientException;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

/** */
@SuppressWarnings("nls")
public class PolicyEnforcementProcessorTest
{
	private static String ATTRIBUTE_ID = "lxacmlpdp:obligation:cachetargets:updateusercache"; //$NON-NLS-1$
	private static String OBLIGATION_ID = "lxacmlpdp:obligation:cachetargets"; //$NON-NLS-1$
	private SessionGroupCache sessionGroupCache;
	private WSClient wsClient;
	private IdentifierGenerator identifierGenerator;
	private KeystoreResolver keyStoreResolver;
	private MetadataProcessor metadata;
	private PolicyEnforcementProcessor processor;
	private String spepIdentifier;
	private String documentID;
	private String authzServiceEndpoint;
	private String samlID;
	private Marshaller<Response> responseMarshaller;
	private String sessionID;
	private String marshallPackages;
	private PrincipalSession principalSession;
	private String esoeSessionIndex;
	private SAMLValidatorImpl samlValidator;
	private SessionCache sessionCache;
	private String esoeIdentifier;
	private Marshaller<GroupTarget> groupTargetMarshaller;
	private Marshaller<ClearAuthzCacheRequest> clearAuthzCacheRequestMarshaller;
	private String clearAuthzCachePackages;
	private UnmarshallerImpl<ClearAuthzCacheResponse> clearAuthzCacheResponseUnmarshaller;
	private String spepKeyAlias = "54f748a6c6b8a4f8";
	private List<Object> mocked;
	private EntityData esoeEntityData;
	private TrustedESOERole esoeRole;
	
	public PolicyEnforcementProcessorTest() {}
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		this.mocked = new ArrayList<Object>();
		
		this.spepIdentifier = "_joqijoiqfjoimaslkjflaksjdflkasjdlfasdf-awjoertjq908jr9182j30r91j203r9";
		this.esoeIdentifier = "_5thqweroqir82u39r8juq9238jrt0q29j3r09q0r9jq-t0iq0-2jtiopqwjeotijowijt";
		this.esoeSessionIndex = "_jtlaksjdoriqwjeoriuqwoeruiqwoeijroqwijf-q095801293u092u13059u120935u0";
		this.documentID = "_21830958712983749-12538719283749182734987-1oasodifjoqiwjfoiajsdf";
		this.sessionID = "_098140598120398401293840129850912385-0182509178029385091283049182057938679";
		this.authzServiceEndpoint = "https://esoe.url/authz";
		
		this.wsClient = createMock(WSClient.class);
		this.mocked.add(this.wsClient);
		
		this.identifierGenerator = createMock(IdentifierGenerator.class);
		this.mocked.add(this.identifierGenerator);
		expect(this.identifierGenerator.generateSAMLID()).andReturn(this.documentID).anyTimes();
		
		this.keyStoreResolver = new KeystoreResolverImpl(new File( "tests" + File.separator + "testdata" + File.separator + "testspkeystore.ks"), "esoekspass", "54f748a6c6b8a4f8", "9d600hGZQV7591nWVtNcwAtU");

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
		expect(this.metadata.resolveKey(this.spepKeyAlias)).andReturn(this.keyStoreResolver.getLocalPublicKey()).anyTimes();
		
		IdentifierCache identifierCache = createMock(IdentifierCache.class);
		this.mocked.add(identifierCache);
		identifierCache.registerIdentifier((String)notNull());
		expectLastCall().anyTimes();
		
		this.samlValidator = new SAMLValidatorImpl(identifierCache, 180);
		
		this.marshallPackages = LXACMLAuthzDecisionQuery.class.getPackage().getName() + ":" + //$NON-NLS-1$
			GroupTarget.class.getPackage().getName() + ":" + //$NON-NLS-1$
			StatementAbstractType.class.getPackage().getName() + ":" + //$NON-NLS-1$
			LXACMLAuthzDecisionStatement.class.getPackage().getName() + ":" + //$NON-NLS-1$
			Response.class.getPackage().getName();
		String[] schemas = new String[]{SchemaConstants.samlProtocol, SchemaConstants.lxacml,
				SchemaConstants.lxacmlSAMLProtocol, SchemaConstants.lxacmlGroupTarget,
				SchemaConstants.lxacmlSAMLAssertion, SchemaConstants.samlAssertion};
		
		this.responseMarshaller = new MarshallerImpl<Response>(this.marshallPackages, schemas, this.keyStoreResolver);

		this.principalSession = createMock(PrincipalSession.class);
		this.mocked.add(this.principalSession);
		expect(this.principalSession.getEsoeSessionID()).andReturn(this.esoeSessionIndex).anyTimes();
		
		this.sessionCache = createMock(SessionCache.class);
		this.mocked.add(this.sessionCache);
		expect(this.sessionCache.getPrincipalSession((String)notNull())).andReturn(this.principalSession).anyTimes();
		
		String[] groupTargetSchemas = new String[]{SchemaConstants.lxacmlGroupTarget};
		this.groupTargetMarshaller = new MarshallerImpl<GroupTarget>(GroupTarget.class.getPackage().getName(), groupTargetSchemas);
		
		this.clearAuthzCachePackages = ClearAuthzCacheRequest.class.getPackage().getName() + ":" +
			StatusResponseType.class.getPackage().getName() + ":" +
			RequestAbstractType.class.getPackage().getName();
		
		String[] clearAuthzCacheSchemas = new String[]{SchemaConstants.esoeProtocol, SchemaConstants.samlAssertion, SchemaConstants.samlProtocol};
		this.clearAuthzCacheRequestMarshaller = new MarshallerImpl<ClearAuthzCacheRequest>(this.clearAuthzCachePackages, clearAuthzCacheSchemas, this.keyStoreResolver);
		this.clearAuthzCacheResponseUnmarshaller = new UnmarshallerImpl<ClearAuthzCacheResponse>(this.clearAuthzCachePackages, clearAuthzCacheSchemas, this.keyStoreResolver);
	}
	
	private void createPEP(decision defaultDecision)
	{
		this.sessionGroupCache = new SessionGroupCacheImpl(defaultDecision);
		try
		{
			this.processor = new PolicyEnforcementProcessorImpl(this.sessionCache, this.sessionGroupCache, this.wsClient, this.identifierGenerator, this.metadata, this.keyStoreResolver, this.samlValidator, this.esoeIdentifier, this.spepIdentifier, false, false);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
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
	public final void testMakeAuthzDecision1() throws Exception
	{
		createPEP(decision.deny);
		
		String groupTarget1 = "/.*.jsp";
		List<String> authzTargets1 = new Vector<String>();
		authzTargets1.add("/admin/.*.jsp");
		
		String groupTarget2 = "/admin/.*";
		List<String> authzTargets2 = new Vector<String>();
		authzTargets2.add("/admin/secure/.*");
		
		String groupTarget3 = "/admin/secure/.*";
		List<String> authzTargets3 = new Vector<String>();
		authzTargets3.add(".*/secure/.*.gif");
		
		Map<String,List<String>> groupTargetMap = new HashMap<String, List<String>>();
		groupTargetMap.put(groupTarget1, authzTargets1);
		groupTargetMap.put(groupTarget2, authzTargets2);
		groupTargetMap.put(groupTarget3, authzTargets3);
		
		String resource1 = "/somepage.jsp";
		decision decision1 = decision.deny;
		String resource2 = "/admin/somepage.jsp";
		decision decision2 = decision.permit;
		String resource3 = "/admin/secure/somepage.jsp";
		decision decision3 = decision.permit;
		String resource4 = "/admin/secure/icon.gif";
		decision decision4 = decision.permit;

		Obligations obligations = new Obligations();
		
		AttributeAssignment attributeAssignment = new AttributeAssignment();
		attributeAssignment.setAttributeId(ATTRIBUTE_ID);
		
		GroupTarget groupTarget = new GroupTarget();
		groupTarget.setGroupTargetID(groupTarget1);
		groupTarget.getAuthzTargets().addAll(authzTargets1);
		attributeAssignment.getContent().add(groupTarget);
		
		groupTarget = new GroupTarget();
		groupTarget.setGroupTargetID(groupTarget2);
		groupTarget.getAuthzTargets().addAll(authzTargets2);
		attributeAssignment.getContent().add(groupTarget);

		groupTarget = new GroupTarget();
		groupTarget.setGroupTargetID(groupTarget3);
		groupTarget.getAuthzTargets().addAll(authzTargets3);
		attributeAssignment.getContent().add(groupTarget);
		
		Obligation obligation1 = new Obligation();
		obligation1.setFulfillOn(EffectType.PERMIT);
		obligation1.setObligationId(OBLIGATION_ID);
		obligation1.getAttributeAssignments().add(attributeAssignment);
		obligations.getObligations().add(obligation1);
		
		Element responseDocument = generateResponse(decision.permit, obligations);

		//expect(this.principalSession.getSessionID()).andReturn(this.sessionID).anyTimes();
		expect(this.wsClient.policyDecisionPoint((Element)notNull(), (String)notNull())).andReturn(responseDocument).anyTimes();
		
		startMock();
		
		Element authzCacheClearResponseDocument = this.processor.authzCacheClear(generateClearAuthzCacheRequest(groupTargetMap));
		validateClearAuthzCacheResponse(authzCacheClearResponseDocument);

		// Need to run decision 4 first, as running 1 first would cause incorrect behaviour, since the LXACMLAuthzDecisionStatement indicates
		// a permit when we are expecting a deny.
		assertEquals("Decision 4 was incorrect", decision4, this.processor.makeAuthzDecision(this.sessionID, resource4));
		assertEquals("Decision 1 was incorrect", decision1, this.processor.makeAuthzDecision(this.sessionID, resource1));
		assertEquals("Decision 2 was incorrect", decision2, this.processor.makeAuthzDecision(this.sessionID, resource2));
		assertEquals("Decision 3 was incorrect", decision3, this.processor.makeAuthzDecision(this.sessionID, resource3));
		
		endMock();
	}
	
	/**
	 * Test method for {@link com.qut.middleware.spep.pep.SessionGroupCache#makeCachedAuthzDecision(com.qut.middleware.spep.sessions.PrincipalSession, java.lang.String)}.
	 * @throws WSClientException 
	 */
	@Test
	public void testMakeCachedAuthzDecision1a() throws Exception
	{
		createPEP(decision.deny);

		String groupTarget1 = "/.*.jsp";
		List<String> authzTargets1 = new Vector<String>();
		authzTargets1.add("/admin/.*.jsp");
		
		String groupTarget2 = "/admin/.*";
		List<String> authzTargets2 = new Vector<String>();
		authzTargets2.add("/admin/secure/.*");
		
		String groupTarget3 = "/admin/secure/.*";
		List<String> authzTargets3 = new Vector<String>();
		authzTargets3.add(".*/secure/.*.gif");
		
		Map<String,List<String>> groupTargetMap = new HashMap<String, List<String>>();
		groupTargetMap.put(groupTarget1, authzTargets1);
		groupTargetMap.put(groupTarget2, authzTargets2);
		groupTargetMap.put(groupTarget3, authzTargets3);

		String resource1 = "/somepage.jsp";
		decision decision1 = decision.deny;
		String resource2 = "/admin/somepage.jsp";
		decision decision2 = decision.permit;
		String resource3 = "/admin/secure/somepage.jsp";
		decision decision3 = decision.permit;
		String resource4 = "/admin/secure/icon.gif";
		decision decision4 = decision.error;
		
		
		expect(this.wsClient.policyDecisionPoint((Element)notNull(), (String)notNull())).andReturn(createMock(Element.class)).anyTimes();
		
		startMock();
		
		Element responseDocument = this.processor.authzCacheClear(generateClearAuthzCacheRequest(groupTargetMap));
		validateClearAuthzCacheResponse(responseDocument);

		this.sessionGroupCache.updateCache(this.principalSession, groupTarget1, authzTargets1, null, decision.permit);
		this.sessionGroupCache.updateCache(this.principalSession, groupTarget2, authzTargets2, null, decision.permit);

		assertEquals("Decision 1 was incorrect", decision1, this.processor.makeAuthzDecision(this.sessionID, resource1));
		assertEquals("Decision 2 was incorrect", decision2, this.processor.makeAuthzDecision(this.sessionID, resource2));
		assertEquals("Decision 3 was incorrect", decision3, this.processor.makeAuthzDecision(this.sessionID, resource3));
		assertEquals("Decision 4 was incorrect", decision4, this.processor.makeAuthzDecision(this.sessionID, resource4));
		
		endMock();
	}

	/**
	 * Test method for {@link com.qut.middleware.spep.pep.SessionGroupCache#makeCachedAuthzDecision(com.qut.middleware.spep.sessions.PrincipalSession, java.lang.String)}.
	 * @throws Exception 
	 */
	@Test
	public void testMakeCachedAuthzDecision1b() throws Exception
	{
		createPEP(decision.permit);

		String groupTarget1 = "/.*.jsp";
		List<String> authzTargets1 = new Vector<String>();
		authzTargets1.add("/admin/.*.jsp");
		
		String groupTarget2 = "/admin/.*";
		List<String> authzTargets2 = new Vector<String>();
		authzTargets2.add("/admin/secure/.*");
		
		String groupTarget3 = "/admin/secure/.*";
		List<String> authzTargets3 = new Vector<String>();
		authzTargets3.add(".*/secure/.*.gif");
		
		
		Map<String,List<String>> groupTargetMap = new HashMap<String, List<String>>();
		groupTargetMap.put(groupTarget1, authzTargets1);
		groupTargetMap.put(groupTarget2, authzTargets2);
		groupTargetMap.put(groupTarget3, authzTargets3);

		String resource1 = "/somepage.jsp";
		decision decision1 = decision.permit;
		String resource2 = "/admin/somepage.jsp";
		decision decision2 = decision.permit;
		String resource3 = "/admin/secure/somepage.jsp";
		decision decision3 = decision.permit;
		String resource4 = "/admin/secure/icon.gif";
		decision decision4 = decision.error;
		
		
		expect(this.wsClient.policyDecisionPoint((Element)notNull(), (String)notNull())).andReturn(createMock(Element.class)).anyTimes();

		startMock();
		
		Element responseDocument = this.processor.authzCacheClear(generateClearAuthzCacheRequest(groupTargetMap));
		validateClearAuthzCacheResponse(responseDocument);

		this.sessionGroupCache.updateCache(this.principalSession, groupTarget1, authzTargets1, null, decision.permit);
		this.sessionGroupCache.updateCache(this.principalSession, groupTarget2, authzTargets2, null, decision.permit);

		assertEquals("Decision 1 was incorrect", decision1, this.processor.makeAuthzDecision(this.sessionID, resource1));
		assertEquals("Decision 2 was incorrect", decision2, this.processor.makeAuthzDecision(this.sessionID, resource2));
		assertEquals("Decision 3 was incorrect", decision3, this.processor.makeAuthzDecision(this.sessionID, resource3));
		assertEquals("Decision 4 was incorrect", decision4, this.processor.makeAuthzDecision(this.sessionID, resource4));
		
		endMock();
	}

	/**
	 * Test method for {@link com.qut.middleware.spep.pep.SessionGroupCache#makeCachedAuthzDecision(com.qut.middleware.spep.sessions.PrincipalSession, java.lang.String)}.
	 */
	@Test
	public void testMakeCachedAuthzDecision2a() throws Exception
	{
		createPEP(decision.deny);

		String groupTarget1 = "/.*.jsp";
		List<String> authzTargets1 = new Vector<String>();
		authzTargets1.add("/admin/.*.jsp");
		
		String groupTarget2 = "/admin/.*";
		List<String> authzTargets2 = new Vector<String>();
		authzTargets2.add("/admin/secure/.*");
		
		String groupTarget3 = "/admin/secure/.*";
		List<String> authzTargets3 = new Vector<String>();
		authzTargets3.add(".*/secure/.*.gif");
		
		
		Map<String,List<String>> groupTargetMap = new HashMap<String, List<String>>();
		groupTargetMap.put(groupTarget1, authzTargets1);
		groupTargetMap.put(groupTarget2, authzTargets2);
		groupTargetMap.put(groupTarget3, authzTargets3);

		String resource1 = "/somepage.jsp";
		decision decision1 = decision.deny;
		String resource2 = "/admin/somepage.jsp";
		decision decision2 = decision.deny;
		String resource3 = "/admin/secure/somepage.jsp";
		decision decision3 = decision.deny;
		String resource4 = "/admin/secure/icon.gif";
		decision decision4 = decision.permit;
		
		
		startMock();
		
		Element responseDocument = this.processor.authzCacheClear(generateClearAuthzCacheRequest(groupTargetMap));
		validateClearAuthzCacheResponse(responseDocument);

		this.sessionGroupCache.updateCache(this.principalSession, groupTarget1, authzTargets1, null, decision.deny);
		this.sessionGroupCache.updateCache(this.principalSession, groupTarget2, authzTargets2, null, decision.permit);
		this.sessionGroupCache.updateCache(this.principalSession, groupTarget3, authzTargets3, null, decision.permit);

		assertEquals("Decision 1 was incorrect", decision1, this.processor.makeAuthzDecision(this.sessionID, resource1));
		assertEquals("Decision 2 was incorrect", decision2, this.processor.makeAuthzDecision(this.sessionID, resource2));
		assertEquals("Decision 3 was incorrect", decision3, this.processor.makeAuthzDecision(this.sessionID, resource3));
		assertEquals("Decision 4 was incorrect", decision4, this.processor.makeAuthzDecision(this.sessionID, resource4));
		
		endMock();
	}

	/**
	 * Test method for {@link com.qut.middleware.spep.pep.SessionGroupCache#makeCachedAuthzDecision(com.qut.middleware.spep.sessions.PrincipalSession, java.lang.String)}.
	 */
	@Test
	public void testMakeCachedAuthzDecision2b() throws Exception
	{
		createPEP(decision.permit);

		String groupTarget1 = "/.*.jsp";
		List<String> authzTargets1 = new Vector<String>();
		authzTargets1.add("/admin/.*.jsp");
		
		String groupTarget2 = "/admin/.*";
		List<String> authzTargets2 = new Vector<String>();
		authzTargets2.add("/admin/secure/.*");
		
		String groupTarget3 = "/admin/secure/.*";
		List<String> authzTargets3 = new Vector<String>();
		authzTargets3.add(".*/secure/.*.gif");
		
		
		Map<String,List<String>> groupTargetMap = new HashMap<String, List<String>>();
		groupTargetMap.put(groupTarget1, authzTargets1);
		groupTargetMap.put(groupTarget2, authzTargets2);
		groupTargetMap.put(groupTarget3, authzTargets3);

		String resource1 = "/somepage.jsp";
		decision decision1 = decision.permit;
		String resource2 = "/admin/somepage.jsp";
		decision decision2 = decision.deny;
		String resource3 = "/admin/secure/somepage.jsp";
		decision decision3 = decision.deny;
		String resource4 = "/admin/secure/icon.gif";
		decision decision4 = decision.permit;
		
		
		startMock();
		
		Element responseDocument = this.processor.authzCacheClear(generateClearAuthzCacheRequest(groupTargetMap));
		validateClearAuthzCacheResponse(responseDocument);

		this.sessionGroupCache.updateCache(this.principalSession, groupTarget1, authzTargets1, null, decision.deny);
		this.sessionGroupCache.updateCache(this.principalSession, groupTarget2, authzTargets2, null, decision.permit);
		this.sessionGroupCache.updateCache(this.principalSession, groupTarget3, authzTargets3, null, decision.permit);

		assertEquals("Decision 1 was incorrect", decision1, this.processor.makeAuthzDecision(this.sessionID, resource1));
		assertEquals("Decision 2 was incorrect", decision2, this.processor.makeAuthzDecision(this.sessionID, resource2));
		assertEquals("Decision 3 was incorrect", decision3, this.processor.makeAuthzDecision(this.sessionID, resource3));
		assertEquals("Decision 4 was incorrect", decision4, this.processor.makeAuthzDecision(this.sessionID, resource4));
		
		endMock();
	}


	/**
	 * Test method for {@link com.qut.middleware.spep.pep.SessionGroupCache#makeCachedAuthzDecision(com.qut.middleware.spep.sessions.PrincipalSession, java.lang.String)}.
	 */
	@Test
	public void testMakeCachedAuthzDecision3a() throws Exception
	{
		createPEP(decision.deny);

		String groupTarget1 = "/.*.jsp";
		List<String> authzTargets1 = new Vector<String>();
		authzTargets1.add("/admin/.*.jsp");
		
		String groupTarget2 = "/admin/.*";
		List<String> authzTargets2 = new Vector<String>();
		authzTargets2.add("/admin/secure/.*");
		
		String groupTarget3 = "/admin/secure/.*";
		List<String> authzTargets3 = new Vector<String>();
		authzTargets3.add(".*/secure/.*.gif");
		
		
		Map<String,List<String>> groupTargetMap = new HashMap<String, List<String>>();
		groupTargetMap.put(groupTarget1, authzTargets1);
		groupTargetMap.put(groupTarget2, authzTargets2);
		groupTargetMap.put(groupTarget3, authzTargets3);

		String resource1 = "/somepage.jsp";
		decision decision1 = decision.deny;
		String resource2 = "/admin/somepage.jsp";
		decision decision2 = decision.permit;
		String resource3 = "/admin/secure/somepage.jsp";
		decision decision3 = decision.deny;
		String resource4 = "/admin/secure/icon.gif";
		decision decision4 = decision.deny;
		
		
		startMock();
		
		Element responseDocument = this.processor.authzCacheClear(generateClearAuthzCacheRequest(groupTargetMap));
		validateClearAuthzCacheResponse(responseDocument);

		this.sessionGroupCache.updateCache(this.principalSession, groupTarget1, authzTargets1, null, decision.permit);
		this.sessionGroupCache.updateCache(this.principalSession, groupTarget2, authzTargets2, null, decision.deny);
		this.sessionGroupCache.updateCache(this.principalSession, groupTarget3, authzTargets3, null, decision.permit);

		assertEquals("Decision 1 was incorrect", decision1, this.processor.makeAuthzDecision(this.sessionID, resource1));
		assertEquals("Decision 2 was incorrect", decision2, this.processor.makeAuthzDecision(this.sessionID, resource2));
		assertEquals("Decision 3 was incorrect", decision3, this.processor.makeAuthzDecision(this.sessionID, resource3));
		assertEquals("Decision 4 was incorrect", decision4, this.processor.makeAuthzDecision(this.sessionID, resource4));
		
		endMock();
	}


	/**
	 * Test method for {@link com.qut.middleware.spep.pep.SessionGroupCache#makeCachedAuthzDecision(com.qut.middleware.spep.sessions.PrincipalSession, java.lang.String)}.
	 */
	@Test
	public void testMakeCachedAuthzDecision3b() throws Exception
	{
		createPEP(decision.permit);

		String groupTarget1 = "/.*.jsp";
		List<String> authzTargets1 = new Vector<String>();
		authzTargets1.add("/admin/.*.jsp");
		
		String groupTarget2 = "/admin/.*";
		List<String> authzTargets2 = new Vector<String>();
		authzTargets2.add("/admin/secure/.*");
		
		String groupTarget3 = "/admin/secure/.*";
		List<String> authzTargets3 = new Vector<String>();
		authzTargets3.add(".*/secure/.*.gif");
		
		
		Map<String,List<String>> groupTargetMap = new HashMap<String, List<String>>();
		groupTargetMap.put(groupTarget1, authzTargets1);
		groupTargetMap.put(groupTarget2, authzTargets2);
		groupTargetMap.put(groupTarget3, authzTargets3);
		
		String resource1 = "/somepage.jsp";
		decision decision1 = decision.permit;
		String resource2 = "/admin/somepage.jsp";
		decision decision2 = decision.permit;
		String resource3 = "/admin/secure/somepage.jsp";
		decision decision3 = decision.deny;
		String resource4 = "/admin/secure/icon.gif";
		decision decision4 = decision.deny;
		
		
		startMock();
		
		Element responseDocument = this.processor.authzCacheClear(generateClearAuthzCacheRequest(groupTargetMap));
		validateClearAuthzCacheResponse(responseDocument);

		this.sessionGroupCache.updateCache(this.principalSession, groupTarget1, authzTargets1, null, decision.permit);
		this.sessionGroupCache.updateCache(this.principalSession, groupTarget2, authzTargets2, null, decision.deny);
		this.sessionGroupCache.updateCache(this.principalSession, groupTarget3, authzTargets3, null, decision.permit);

		assertEquals("Decision 1 was incorrect", decision1, this.processor.makeAuthzDecision(this.sessionID, resource1));
		assertEquals("Decision 2 was incorrect", decision2, this.processor.makeAuthzDecision(this.sessionID, resource2));
		assertEquals("Decision 3 was incorrect", decision3, this.processor.makeAuthzDecision(this.sessionID, resource3));
		assertEquals("Decision 4 was incorrect", decision4, this.processor.makeAuthzDecision(this.sessionID, resource4));
		
		endMock();
	}


	/**
	 * Test method for {@link com.qut.middleware.spep.pep.SessionGroupCache#makeCachedAuthzDecision(com.qut.middleware.spep.sessions.PrincipalSession, java.lang.String)}.
	 * @throws Exception 
	 */
	@Test
	public void testMakeCachedAuthzDecision4a() throws Exception
	{
		createPEP(decision.deny);

		String groupTarget1 = "/.*.jsp";
		List<String> authzTargets1 = new Vector<String>();
		authzTargets1.add("/admin/.*.jsp");
		
		String groupTarget2 = "/admin/.*";
		List<String> authzTargets2 = new Vector<String>();
		authzTargets2.add("/admin/secure/.*");
		
		String groupTarget3 = "/admin/secure/.*";
		List<String> authzTargets3 = new Vector<String>();
		authzTargets3.add(".*/secure/.*.gif");
		
		
		Map<String,List<String>> groupTargetMap = new HashMap<String, List<String>>();
		groupTargetMap.put(groupTarget1, authzTargets1);
		groupTargetMap.put(groupTarget2, authzTargets2);
		groupTargetMap.put(groupTarget3, authzTargets3);

		String resource1 = "/somepage.jsp";
		decision decision1 = decision.deny;
		String resource2 = "/admin/somepage.jsp";
		decision decision2 = decision.error;
		String resource3 = "/admin/secure/somepage.jsp";
		decision decision3 = decision.error;
		String resource4 = "/admin/secure/icon.gif";
		decision decision4 = decision.permit;
		
		expect(this.wsClient.policyDecisionPoint((Element)notNull(), (String)notNull())).andReturn(createMock(Element.class)).anyTimes();

		startMock();
		
		Element responseDocument = this.processor.authzCacheClear(generateClearAuthzCacheRequest(groupTargetMap));
		validateClearAuthzCacheResponse(responseDocument);

		this.sessionGroupCache.updateCache(this.principalSession, groupTarget2, authzTargets2, null, decision.permit);
		this.sessionGroupCache.updateCache(this.principalSession, groupTarget3, authzTargets3, null, decision.permit);

		assertEquals("Decision 1 was incorrect", decision1, this.processor.makeAuthzDecision(this.sessionID, resource1));
		assertEquals("Decision 2 was incorrect", decision2, this.processor.makeAuthzDecision(this.sessionID, resource2));
		assertEquals("Decision 3 was incorrect", decision3, this.processor.makeAuthzDecision(this.sessionID, resource3));
		assertEquals("Decision 4 was incorrect", decision4, this.processor.makeAuthzDecision(this.sessionID, resource4));
		
		endMock();
	}


	/**
	 * Test method for {@link com.qut.middleware.spep.pep.SessionGroupCache#makeCachedAuthzDecision(com.qut.middleware.spep.sessions.PrincipalSession, java.lang.String)}.
	 * @throws Exception 
	 */
	@Test
	public void testMakeCachedAuthzDecision4b() throws Exception
	{
		createPEP(decision.permit);

		String groupTarget1 = "/.*.jsp";
		List<String> authzTargets1 = new Vector<String>();
		authzTargets1.add("/admin/.*.jsp");
		
		String groupTarget2 = "/admin/.*";
		List<String> authzTargets2 = new Vector<String>();
		authzTargets2.add("/admin/secure/.*");
		
		String groupTarget3 = "/admin/secure/.*";
		List<String> authzTargets3 = new Vector<String>();
		authzTargets3.add(".*/secure/.*.gif");
		
		
		Map<String,List<String>> groupTargetMap = new HashMap<String, List<String>>();
		groupTargetMap.put(groupTarget1, authzTargets1);
		groupTargetMap.put(groupTarget2, authzTargets2);
		groupTargetMap.put(groupTarget3, authzTargets3);

		String resource1 = "/somepage.jsp";
		decision decision1 = decision.permit;
		String resource2 = "/admin/somepage.jsp";
		decision decision2 = decision.error;
		String resource3 = "/admin/secure/somepage.jsp";
		decision decision3 = decision.error;
		String resource4 = "/admin/secure/icon.gif";
		decision decision4 = decision.permit;
		
		
		expect(this.wsClient.policyDecisionPoint((Element)notNull(), (String)notNull())).andReturn(createMock(Element.class)).anyTimes();

		startMock();
		
		Element responseDocument = this.processor.authzCacheClear(generateClearAuthzCacheRequest(groupTargetMap));
		validateClearAuthzCacheResponse(responseDocument);

		this.sessionGroupCache.updateCache(this.principalSession, groupTarget2, authzTargets2, null, decision.permit);
		this.sessionGroupCache.updateCache(this.principalSession, groupTarget3, authzTargets3, null, decision.permit);

		assertEquals("Decision 1 was incorrect", decision1, this.processor.makeAuthzDecision(this.sessionID, resource1));
		assertEquals("Decision 2 was incorrect", decision2, this.processor.makeAuthzDecision(this.sessionID, resource2));
		assertEquals("Decision 3 was incorrect", decision3, this.processor.makeAuthzDecision(this.sessionID, resource3));
		assertEquals("Decision 4 was incorrect", decision4, this.processor.makeAuthzDecision(this.sessionID, resource4));
		
		endMock();
	}

	/**
	 * Test method for {@link com.qut.middleware.spep.pep.SessionGroupCache#makeCachedAuthzDecision(com.qut.middleware.spep.sessions.PrincipalSession, java.lang.String)}.
	 */
	@Test
	public void testMakeCachedAuthzDecision5a() throws Exception
	{
		createPEP(decision.deny);

		String groupTarget1 = "/.*.jsp";
		List<String> authzTargets1 = new Vector<String>();
		authzTargets1.add(".*/secure/.*");
		
		String groupTarget2 = "/admin/.*";
		List<String> authzTargets2 = new Vector<String>();
		authzTargets2.add("/admin/secure/.*");
		authzTargets2.add("/admin/.*\\.jsp");
		
		String groupTarget3 = "/admin/secure/.*";
		List<String> authzTargets3 = new Vector<String>();
		authzTargets3.add(".*/secure/.*\\.gif");
		
		
		Map<String,List<String>> groupTargetMap = new HashMap<String, List<String>>();
		groupTargetMap.put(groupTarget1, authzTargets1);
		groupTargetMap.put(groupTarget2, authzTargets2);
		groupTargetMap.put(groupTarget3, authzTargets3);

		String resource1 = "/somepage.jsp";
		decision decision1 = decision.deny;
		String resource2 = "/admin/somepage.jsp";
		decision decision2 = decision.permit;
		String resource3 = "/admin/secure/somepage.jsp";
		decision decision3 = decision.deny;
		String resource4 = "/admin/secure/icon.gif";
		decision decision4 = decision.permit;
		
		
		startMock();
		
		Element responseDocument = this.processor.authzCacheClear(generateClearAuthzCacheRequest(groupTargetMap));
		validateClearAuthzCacheResponse(responseDocument);

		this.sessionGroupCache.updateCache(this.principalSession, groupTarget1, authzTargets1, null, decision.deny);
		this.sessionGroupCache.updateCache(this.principalSession, groupTarget2, authzTargets2, null, decision.permit);
		this.sessionGroupCache.updateCache(this.principalSession, groupTarget3, authzTargets3, null, decision.permit);

		assertEquals("Decision 1 was incorrect", decision1, this.processor.makeAuthzDecision(this.sessionID, resource1));
		assertEquals("Decision 2 was incorrect", decision2, this.processor.makeAuthzDecision(this.sessionID, resource2));
		assertEquals("Decision 3 was incorrect", decision3, this.processor.makeAuthzDecision(this.sessionID, resource3));
		assertEquals("Decision 4 was incorrect", decision4, this.processor.makeAuthzDecision(this.sessionID, resource4));
		
		endMock();
	}

	/**
	 * Test method for {@link com.qut.middleware.spep.pep.SessionGroupCache#makeCachedAuthzDecision(com.qut.middleware.spep.sessions.PrincipalSession, java.lang.String)}.
	 */
	@Test
	public void testMakeCachedAuthzDecision5b() throws Exception
	{
		createPEP(decision.permit);

		String groupTarget1 = "/.*.jsp";
		List<String> authzTargets1 = new Vector<String>();
		authzTargets1.add(".*/secure/.*");
		
		String groupTarget2 = "/admin/.*";
		List<String> authzTargets2 = new Vector<String>();
		authzTargets2.add("/admin/secure/.*");
		authzTargets2.add("/admin/.*\\.jsp");
		
		String groupTarget3 = "/admin/secure/.*";
		List<String> authzTargets3 = new Vector<String>();
		authzTargets3.add(".*/secure/.*\\.gif");
		
		
		Map<String,List<String>> groupTargetMap = new HashMap<String, List<String>>();
		groupTargetMap.put(groupTarget1, authzTargets1);
		groupTargetMap.put(groupTarget2, authzTargets2);
		groupTargetMap.put(groupTarget3, authzTargets3);
		
		String resource1 = "/somepage.jsp";
		decision decision1 = decision.permit;
		String resource2 = "/admin/somepage.jsp";
		decision decision2 = decision.permit;
		String resource3 = "/admin/secure/somepage.jsp";
		decision decision3 = decision.deny;
		String resource4 = "/admin/secure/icon.gif";
		decision decision4 = decision.permit;
		
		
		startMock();
		
		Element responseDocument = this.processor.authzCacheClear(generateClearAuthzCacheRequest(groupTargetMap));
		validateClearAuthzCacheResponse(responseDocument);

		this.sessionGroupCache.updateCache(this.principalSession, groupTarget1, authzTargets1, null, decision.deny);
		this.sessionGroupCache.updateCache(this.principalSession, groupTarget2, authzTargets2, null, decision.permit);
		this.sessionGroupCache.updateCache(this.principalSession, groupTarget3, authzTargets3, null, decision.permit);

		assertEquals("Decision 1 was incorrect", decision1, this.processor.makeAuthzDecision(this.sessionID, resource1));
		assertEquals("Decision 2 was incorrect", decision2, this.processor.makeAuthzDecision(this.sessionID, resource2));
		assertEquals("Decision 3 was incorrect", decision3, this.processor.makeAuthzDecision(this.sessionID, resource3));
		assertEquals("Decision 4 was incorrect", decision4, this.processor.makeAuthzDecision(this.sessionID, resource4));
		
		endMock();
	}
	
	protected Element generateClearAuthzCacheRequest(Map<String,List<String>> groupTargetMap) throws MarshallerException
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
		
		Extensions extensions = new Extensions();
		request.setExtensions(extensions);

		List<Element> groupTargetList = new Vector<Element>();
		for (Entry<String, List<String>> groupTargetMapEntry : groupTargetMap.entrySet())
		{
			GroupTarget groupTarget = new GroupTarget();
			groupTarget.setGroupTargetID(groupTargetMapEntry.getKey());
			groupTarget.getAuthzTargets().addAll(groupTargetMapEntry.getValue());
			
			Element element = this.groupTargetMarshaller.marshallUnSignedElement(groupTarget);
			groupTargetList.add(element);
		}
		
		extensions.getAnies().addAll(groupTargetList);
		
		Element requestXml = this.clearAuthzCacheRequestMarshaller.marshallSignedElement(request);
		
		return requestXml;
	}
	
	protected void validateClearAuthzCacheResponse(Element responseDocument) throws UnmarshallerException
	{
		ClearAuthzCacheResponse response = this.clearAuthzCacheResponseUnmarshaller.unMarshallUnSigned(responseDocument);
		
		assertEquals("Clear authz cache response was invalid", StatusCodeConstants.success, response.getStatus().getStatusCode().getValue());
	}
	
	protected Element generateResponse(decision desiredDecision, Obligations obligations) throws MarshallerException
	{
		NameIDType issuer = new NameIDType();
		issuer.setValue("issuer");
		
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

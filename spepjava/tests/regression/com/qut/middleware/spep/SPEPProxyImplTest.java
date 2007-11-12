package com.qut.middleware.spep;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.spep.authn.AuthnProcessor;
import com.qut.middleware.spep.impl.SPEPProxyImpl;
import com.qut.middleware.spep.pep.PolicyEnforcementProcessor;
import com.qut.middleware.spep.sessions.PrincipalSession;

public class SPEPProxyImplTest
{
	private SPEP spep;
	private SPEPProxyImpl proxy;

	@Before
	public void setUp() throws Exception
	{
		
	}

	@After
	public void tearDown() throws Exception
	{
	}

	@Test
	public void testSPEPProxyImpl()
	{
		spep = createMock(SPEP.class);
		proxy = new SPEPProxyImpl(spep);
		
		assertEquals(spep, proxy.getSpep());
	}

	@Test
	public void testSetSpep()
	{
		spep = createMock(SPEP.class);
		proxy = new SPEPProxyImpl(spep);
		
		spep = createMock(SPEP.class);
		proxy.setSpep(spep);
		assertEquals(spep, proxy.getSpep());
		
	}

	@Test
	public void testGetDefaultUrl()
	{
		String defaultURL = "testurl.com";
		spep = createMock(SPEP.class);
		expect(spep.getDefaultUrl()).andReturn(defaultURL);
		
		replay(spep);
		
		proxy = new SPEPProxyImpl(spep);
		assertEquals(defaultURL, proxy.getDefaultUrl());
		
		verify(spep);
	}

	@Test
	public void testGetEsoeGlobalTokenName()
	{
		String token = "esoecookie";
		spep = createMock(SPEP.class);
		expect(spep.getEsoeGlobalTokenName()).andReturn(token);
		
		replay(spep);
		
		proxy = new SPEPProxyImpl(spep);
		assertEquals(token, proxy.getEsoeGlobalTokenName());
		
		verify(spep);
	}

	@Test
	public void testGetLazyInitDefaultAction()
	{
		SPEPProxy.defaultAction action;
		
		spep = createMock(SPEP.class);
		expect(spep.getLazyInitDefaultAction()).andReturn(SPEP.defaultAction.Permit);
		expect(spep.getLazyInitDefaultAction()).andReturn(SPEP.defaultAction.Deny);
		
		replay(spep);
		
		proxy = new SPEPProxyImpl(spep);
		
		assertEquals(SPEPProxy.defaultAction.permit, proxy.getLazyInitDefaultAction());
		assertEquals(SPEPProxy.defaultAction.deny, proxy.getLazyInitDefaultAction());
		
		verify(spep);
	}

	@Test
	public void testGetServiceHost()
	{
		String host = "test.host";
		spep = createMock(SPEP.class);
		expect(spep.getServiceHost()).andReturn(host);
		
		replay(spep);
		
		proxy = new SPEPProxyImpl(spep);
		assertEquals(host, proxy.getServiceHost());
		
		verify(spep);
	}

	@Test
	public void testGetSsoRedirect()
	{
		String sso = "test.host";
		spep = createMock(SPEP.class);
		expect(spep.getSsoRedirect()).andReturn(sso);
		
		replay(spep);
		
		proxy = new SPEPProxyImpl(spep);
		assertEquals(sso, proxy.getSsoRedirect());
		
		verify(spep);
	}

	@Test
	public void testGetTokenName()
	{
		String token = "token";
		spep = createMock(SPEP.class);
		expect(spep.getTokenName()).andReturn(token);
		
		replay(spep);
		
		proxy = new SPEPProxyImpl(spep);
		assertEquals(token, proxy.getTokenName());
		
		verify(spep);
	}

	@Test
	public void testIsLazyInit()
	{
		spep = createMock(SPEP.class);
		expect(spep.isLazyInit()).andReturn(false);
		expect(spep.isLazyInit()).andReturn(true);
		
		
		replay(spep);
		
		proxy = new SPEPProxyImpl(spep);
		assertEquals(false, proxy.isLazyInit());
		assertEquals(true, proxy.isLazyInit());
		
		verify(spep);
	}

	@Test
	public void testIsStarted()
	{
		spep = createMock(SPEP.class);
		expect(spep.isStarted()).andReturn(false);
		expect(spep.isStarted()).andReturn(true);
		
		
		replay(spep);
		
		proxy = new SPEPProxyImpl(spep);
		assertEquals(false, proxy.isStarted());
		assertEquals(true, proxy.isStarted());
		
		verify(spep);
	}

	@Test
	public void testMakeAuthzDecision1()
	{
		String sessionID = "123";
		String resource = "/index.jsp";
		
		spep = createMock(SPEP.class);
		PolicyEnforcementProcessor pep = createMock(PolicyEnforcementProcessor.class);
		
		expect(spep.getPolicyEnforcementProcessor()).andReturn(pep).times(4);
		
		expect(pep.makeAuthzDecision(sessionID, resource)).andReturn(PolicyEnforcementProcessor.decision.deny);
		expect(pep.makeAuthzDecision(sessionID, resource)).andReturn(PolicyEnforcementProcessor.decision.error);
		expect(pep.makeAuthzDecision(sessionID, resource)).andReturn(PolicyEnforcementProcessor.decision.notcached);
		expect(pep.makeAuthzDecision(sessionID, resource)).andReturn(PolicyEnforcementProcessor.decision.permit);
		
		replay(pep);
		replay(spep);
		
		
		proxy = new SPEPProxyImpl(spep);
		
		assertEquals(SPEPProxy.decision.deny, proxy.makeAuthzDecision(sessionID, resource));
		assertEquals(SPEPProxy.decision.error, proxy.makeAuthzDecision(sessionID, resource));
		assertEquals(SPEPProxy.decision.notcached, proxy.makeAuthzDecision(sessionID, resource));
		assertEquals(SPEPProxy.decision.permit, proxy.makeAuthzDecision(sessionID, resource));
		
		verify(pep);
		verify(spep);		
	}

	@Test
	public void testMakeAuthzDecision2()
	{
		String sessionID = "123";
		String resource = "/index.jsp";
		String action = "write";
		
		spep = createMock(SPEP.class);
		PolicyEnforcementProcessor pep = createMock(PolicyEnforcementProcessor.class);
		
		expect(spep.getPolicyEnforcementProcessor()).andReturn(pep).times(4);
		
		expect(pep.makeAuthzDecision(sessionID, resource, action)).andReturn(PolicyEnforcementProcessor.decision.deny);
		expect(pep.makeAuthzDecision(sessionID, resource, action)).andReturn(PolicyEnforcementProcessor.decision.error);
		expect(pep.makeAuthzDecision(sessionID, resource, action)).andReturn(PolicyEnforcementProcessor.decision.notcached);
		expect(pep.makeAuthzDecision(sessionID, resource, action)).andReturn(PolicyEnforcementProcessor.decision.permit);
		
		replay(pep);
		replay(spep);
		
		
		proxy = new SPEPProxyImpl(spep);
		
		assertEquals(SPEPProxy.decision.deny, proxy.makeAuthzDecision(sessionID, resource, action));
		assertEquals(SPEPProxy.decision.error, proxy.makeAuthzDecision(sessionID, resource, action));
		assertEquals(SPEPProxy.decision.notcached, proxy.makeAuthzDecision(sessionID, resource, action));
		assertEquals(SPEPProxy.decision.permit, proxy.makeAuthzDecision(sessionID, resource, action));
		
		verify(pep);
		verify(spep);	
	}

	@Test
	public void testVerifySession()
	{
		String sessionID = "123";
		spep = createMock(SPEP.class);
		AuthnProcessor authn = createMock(AuthnProcessor.class);
		PrincipalSession principal = createMock(PrincipalSession.class);
		
		expect(spep.getAuthnProcessor()).andReturn(authn).times(2);
		expect(authn.verifySession(sessionID)).andReturn(principal);
		expect(authn.verifySession(sessionID)).andReturn(null);
		
		replay(principal);
		replay(authn);
		replay(spep);
		
		
		proxy = new SPEPProxyImpl(spep);
		assertEquals(principal, proxy.verifySession(sessionID));
		assertEquals(null, proxy.verifySession(sessionID));
		
		verify(principal);
		verify(authn);
		verify(spep);
	}

	@Test
	public void testGetLazyInitResources()
	{
		String res1 = "test.html";
		String res2 = "test2.html";
		List<String> res = new ArrayList<String>();
		res.add(res1);
		res.add(res2);
		
		spep = createMock(SPEP.class);
		expect(spep.getLazyInitResources()).andReturn(res);
		
		replay(spep);
		
		proxy = new SPEPProxyImpl(spep);
		assertTrue(proxy.getLazyInitResources().contains("test2.html"));
		
		verify(spep);
	}

	@Test
	public void testGetLogoutClearCookies()
	{
		List<Cookie> cookies = new ArrayList<Cookie>();
		
		spep = createMock(SPEP.class);
		expect(spep.getLogoutClearCookies()).andReturn(cookies);
		
		replay(spep);
		
		proxy = new SPEPProxyImpl(spep);
		assertEquals(cookies, proxy.getLogoutClearCookies());
		
		verify(spep);
	}

}

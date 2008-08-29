package com.qut.middleware.spep;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Vector;

import javax.servlet.http.Cookie;

import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.metadata.processor.MetadataProcessor;
import com.qut.middleware.spep.attribute.AttributeProcessor;
import com.qut.middleware.spep.authn.AuthnProcessor;
import com.qut.middleware.spep.impl.SPEPImpl;
import com.qut.middleware.spep.pep.PolicyEnforcementProcessor;
import com.qut.middleware.spep.pep.SessionGroupCache;

@SuppressWarnings("nls")
public class SPEPImplTest {

	private SPEPImpl spep;
	
	private String tokenName;
	private String tokenDomain;
	private String loginRedirect;
	private String defaultUrl;
	private AttributeProcessor attributeProcessor;
	private AuthnProcessor authnProcessor;
	private MetadataProcessor metadata;
	private PolicyEnforcementProcessor policyEnforcementProcessor;
	private SessionGroupCache sessionGroupCache;
	private boolean started;
	private StartupProcessor startupProcessor;
	
	@Before
	public void setUp() throws Exception 
	{
		this.spep = new SPEPImpl();
		
		this.tokenDomain = "my.test.domain";
		this.tokenName = "spepSession";
		this.loginRedirect = "https://test.spep/login";
		this.defaultUrl = "https//test.spep/index.jsp";
		this.attributeProcessor = createMock(AttributeProcessor.class);
		this.authnProcessor = createMock(AuthnProcessor.class);
		this.metadata = createMock(MetadataProcessor.class);
		this.policyEnforcementProcessor = createMock(PolicyEnforcementProcessor.class);
		this.sessionGroupCache = createMock(SessionGroupCache.class);
		this.started = false;
		this.startupProcessor = createMock(StartupProcessor.class);
	}

	
	@Test
	public void testGetAttributeProcessor() 
	{
		this.spep.setAttributeProcessor(this.attributeProcessor);
		
		assertEquals(this.attributeProcessor, this.spep.getAttributeProcessor());
	}

	
	@Test
	public void testGetAuthnProcessor()
	{
		this.spep.setAuthnProcessor(this.authnProcessor);
		
		assertEquals(this.authnProcessor, this.spep.getAuthnProcessor());
	}

	
	@Test
	public void testGetMetadata()
	{
		this.spep.setMetadataProcessor(this.metadata);
		
		assertEquals(this.metadata, this.spep.getMetadataProcessor());
	}

	
	@Test
	public void testGetPolicyEnforcementProcessor()
	{
		this.spep.setPolicyEnforcementProcessor(this.policyEnforcementProcessor);
		
		assertEquals(this.policyEnforcementProcessor, this.spep.getPolicyEnforcementProcessor());
	}

	
	@Test
	public void testGetTokenName()
	{
		this.spep.setTokenName(this.tokenName);
		
		assertEquals(this.tokenName, this.spep.getTokenName());
	}

	
	@Test
	public void testGetDefaultUrl() 
	{
		this.spep.setDefaultURL(this.defaultUrl);
		
		assertEquals(this.defaultUrl, this.spep.getDefaultUrl());
	}

	
	@Test
	public void testGetSessionGroupCache()
	{
		this.spep.setSessionGroupCache(this.sessionGroupCache);
		
		assertEquals(this.sessionGroupCache, this.spep.getSessionGroupCache());
	}

	@Test
	public void testGetStartupProcessor()
	{
		this.spep.setStartupProcessor(this.startupProcessor);
		
		assertEquals(this.startupProcessor, this.spep.getStartupProcessor());
	}

	
	@Test
	public void testIsStarted1()
	{
		this.spep.setStartupProcessor(this.startupProcessor);
		
		expect(this.startupProcessor.allowProcessing()).andReturn(StartupProcessor.result.allow).once();
		
		replay(this.startupProcessor);
		
		assertTrue(this.spep.isStarted());
	}

	
	@Test
	public void testIsStarted2()
	{
		this.spep.setStartupProcessor(this.startupProcessor);
		
		expect(this.startupProcessor.allowProcessing()).andReturn(StartupProcessor.result.fail).once();
		
		replay(this.startupProcessor);
		
		assertTrue(!this.spep.isStarted());
	}
	
	
	@Test
	public void testGetLogoutClearCookies()
	{
		List<Cookie> cookies = new Vector<Cookie>();
		Cookie cookie = new Cookie("cookie1", "testValue");
		cookies.add(cookie);
		
		this.spep.setLogoutClearCookies(cookies);
		
		
		assertEquals(cookies, this.spep.getLogoutClearCookies());
	}

}

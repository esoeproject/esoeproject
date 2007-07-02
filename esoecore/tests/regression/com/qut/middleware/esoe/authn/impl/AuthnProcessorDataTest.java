package com.qut.middleware.esoe.authn.impl;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.esoe.authn.bean.AuthnProcessorData;
import com.qut.middleware.esoe.authn.bean.impl.AuthnProcessorDataImpl;

@SuppressWarnings("nls")
public class AuthnProcessorDataTest {

	private AuthnProcessorData data;
	
	@Before
	public void setUp() throws Exception
	{
		this.data = new AuthnProcessorDataImpl();
	}

	@Test
	public void testGetAutomatedSSO() 
	{
		this.data.setAutomatedSSO(false);
		
		assertEquals(this.data.getAutomatedSSO(), false);		
	}

	@Test
	public void testGetCurrentHandler()
	{
		this.data.setCurrentHandler("myHandler");
		
		assertEquals(this.data.getCurrentHandler(), "myHandler");
	
	}

	@Test
	public void testGetInvalidURL()
	{
		this.data.setInvalidURL("my.url.com");
		
		assertEquals(this.data.getInvalidURL(), "my.url.com");
	
	}

	@Test
	public void testGetHttpRequest()
	{
		HttpServletRequest myRequest = createMock(HttpServletRequest.class);
		
		this.data.setHttpRequest(myRequest);
		
		assertEquals(myRequest, this.data.getHttpRequest());
	}

	@Test
	public void testGetHttpResponse() 
	{
		HttpServletResponse myResponse = createMock(HttpServletResponse.class);
		
		this.data.setHttpResponse(myResponse);
		
		assertEquals(myResponse, this.data.getHttpResponse());
	}

	@Test
	public void testGetRedirectTarget()
	{
		this.data.setRedirectTarget("my.url.com");
		
		assertEquals("my.url.com", this.data.getRedirectTarget());
	
	}

	@Test
	public void testGetSessionID()
	{
		this.data.setSessionID("_&*493gf70eewft4r43");
		
		assertEquals("_&*493gf70eewft4r43", this.data.getSessionID());
	
	}

	@Test
	public void testGetSuccessfulAuthn()
	{
		this.data.setSuccessfulAuthn(true);
	
		assertEquals(true, this.data.getSuccessfulAuthn());	
	}

	@Test
	public void testSetAutomatedSSO()
	{
		this.data.setAutomatedSSO(true);
		
		assertEquals(this.data.getAutomatedSSO(), true);
	}

	@Test
	public void testSetCurrentHandler()
	{
		this.data.setCurrentHandler("myHandler");
		
		assertEquals(this.data.getCurrentHandler(), "myHandler");
	}

	@Test
	public void testSetInvalidURL()
	{
		this.data.setInvalidURL("my.url.org.au");
		
		assertEquals(this.data.getInvalidURL(), "my.url.org.au");
	}
		
	@Test
	public void testSetHttpRequest()
	{
		HttpServletRequest myRequest = createMock(HttpServletRequest.class);
		
		this.data.setHttpRequest(myRequest);
		
		assertEquals(myRequest, this.data.getHttpRequest());
	}

	@Test
	public void testSetHttpResponse()
	{
		HttpServletResponse myResponse = createMock(HttpServletResponse.class);
		
		this.data.setHttpResponse(myResponse);
		
		assertEquals(myResponse, this.data.getHttpResponse());
	}

	@Test
	public void testSetRedirectTarget()
	{
		this.data.setRedirectTarget("my.url.com");
		
		assertEquals("my.url.com", this.data.getRedirectTarget());
	}

	@Test
	public void testSetSessionID()
	{
		this.data.setSessionID("_&*493gf70eewft4r43");
		
		assertEquals("_&*493gf70eewft4r43", this.data.getSessionID());	
	}

	@Test
	public void testSetSuccessfulAuthn()
	{
		this.data.setSuccessfulAuthn(false);
		
		assertEquals(false, this.data.getSuccessfulAuthn());	
		
	}

	@Test
	public void testGetPrincipalName()
	{
		this.data.setPrincipalName("fred");
		
		assertEquals("fred", this.data.getPrincipalName());
	}

	@Test
	public void testSetPrincipalName()
	{
		this.data.setPrincipalName("fred");
		
		assertEquals("fred", this.data.getPrincipalName());
	}

	@Test
	public void testGetErrorCode()
	{
		this.data.setError(501, "test error message");
		
		assertEquals(501, this.data.getErrorCode());
		
		assertEquals("test error message", this.data.getErrorMessage());
	}

	@Test
	public void testGetErrorMessage() 
	{
		this.data.setError(501, "test error message");
		
		assertEquals("test error message", this.data.getErrorMessage());
		
	}

	@Test
	public void testSetError()
	{
		this.data.setError(401, null);
		
		assertEquals(401, this.data.getErrorCode());
		
	}

}

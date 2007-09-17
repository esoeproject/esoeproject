package com.qut.middleware.esoe.deledauthn;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.esoe.delegauthn.bean.DelegatedAuthenticationData;
import com.qut.middleware.esoe.delegauthn.bean.impl.DelegatedAuthenticationDataImpl;
import com.qut.middleware.saml2.schemas.esoe.delegated.RegisterPrincipalRequest;

public class DelegAuthenticationDataTest {

	private DelegatedAuthenticationData data;
	
	@Before
	public void setUp() throws Exception
	{
		this.data = new DelegatedAuthenticationDataImpl();
	}

	@Test
	public void testGetRequestDocument()
	{
		byte[] doc = new String("<request></request>").getBytes();
		// tests get and set
		this.data.setRequestDocument(doc);
		
		assertEquals(doc, this.data.getRequestDocument());
	}

	@Test
	public void testGetResponseDocument()
	{
		byte[] doc = new String("<request></request>").getBytes();
		// tests get and set
		this.data.setResponseDocument(doc);
		
		assertEquals(doc, this.data.getResponseDocument());
	}
	
	@Test
	public void testGetRegisterPrincipalRequest()
	{
		RegisterPrincipalRequest princRequest = new RegisterPrincipalRequest();
		
		this.data.setRegisterPrincipalRequest(princRequest);
		
		assertEquals(princRequest, this.data.getRegisterPrincipalRequest());
	}

	
}

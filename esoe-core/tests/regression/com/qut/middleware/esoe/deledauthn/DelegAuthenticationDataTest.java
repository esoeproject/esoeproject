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
		// tests get and set
		this.data.setRequestDocument("<request></request>");
		
		assertEquals("<request></request>", this.data.getRequestDocument());
	}

	@Test
	public void testGetResponseDocument()
	{
		// tests get and set
		this.data.setResponseDocument("<request></request>");
		
		assertEquals("<request></request>", this.data.getResponseDocument());
	}
	
	@Test
	public void testGetRegisterPrincipalRequest()
	{
		RegisterPrincipalRequest princRequest = new RegisterPrincipalRequest();
		
		this.data.setRegisterPrincipalRequest(princRequest);
		
		assertEquals(princRequest, this.data.getRegisterPrincipalRequest());
	}

	
}

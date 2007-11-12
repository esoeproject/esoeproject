package com.qut.middleware.delegator.openid.authn.bean;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.delegator.openid.authn.bean.impl.AuthnProcessorDataImpl;

public class AuthnProcessorDataImplTest {

	private AuthnProcessorDataImpl authnProcessorDataImpl;
	
	@Before
	public void setUp() throws Exception {
		this.authnProcessorDataImpl = new AuthnProcessorDataImpl();
	}

	@After
	public void tearDown() throws Exception {
		
	}

	@Test
	public void testGetHttpRequest() {
		HttpServletRequest request = createMock(HttpServletRequest.class);
		this.authnProcessorDataImpl.setHttpRequest(request);
		assertEquals(request, this.authnProcessorDataImpl.getHttpRequest());
		
	}

	@Test
	public void testGetHttpResponse() {
		HttpServletResponse response = createMock(HttpServletResponse.class);
		
	}

	@Test
	public void testGetSessionID() {
		String sessionID = "1234567890";
		this.authnProcessorDataImpl.setSessionID(sessionID);
		assertEquals(sessionID, this.authnProcessorDataImpl.getSessionID());
	}

}

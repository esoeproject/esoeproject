package com.qut.middleware.esoe.sso.impl;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.esoe.sso.bean.SSOLogoutState;
import com.qut.middleware.esoe.sso.bean.impl.SSOLogoutStateImpl;

public class SSOLogoutStateTest {

	private SSOLogoutState logoutState;
	
	private boolean state = false;
	private String desc = "You failed to logout";
	private String spepUrl = "some.dodgy.spep";
	
	@Before
	public void setUp() throws Exception 
	{
		this.logoutState = new SSOLogoutStateImpl();
	}

	@Test
	public void testGetLogoutState()
	{
		this.logoutState.setLogoutState(this.state);
		
		assertEquals(this.state, this.logoutState.getLogoutState());
	}

	@Test
	public void testGetLogoutStateDescription()
	{
		this.logoutState.setLogoutStateDescription(this.desc);
		
		assertEquals(this.desc, this.logoutState.getLogoutStateDescription());
	}

	@Test
	public void testGetSPEPURL() 
	{
		this.logoutState.setSPEPURL(this.spepUrl);
		
		assertEquals(this.spepUrl, this.logoutState.getSPEPURL());
	}

}

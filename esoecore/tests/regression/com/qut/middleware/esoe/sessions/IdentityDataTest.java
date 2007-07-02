package com.qut.middleware.esoe.sessions;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.esoe.sessions.bean.IdentityAttribute;
import com.qut.middleware.esoe.sessions.bean.IdentityData;
import com.qut.middleware.esoe.sessions.bean.impl.IdentityAttributeImpl;
import com.qut.middleware.esoe.sessions.bean.impl.IdentityDataImpl;
import com.qut.middleware.saml2.schemas.esoe.sessions.IdentityType;

@SuppressWarnings("nls")
public class IdentityDataTest {

	private IdentityData data;
	
	private Map<String, IdentityAttribute> attributes;
	private String handler;
	private String sessionID;
	private String principalAuthnID;
	private List<IdentityType> identity;
	
	
		@Before
	public void setUp() throws Exception 
	{
		this.handler = "Test Handler";
		this.sessionID = "_GHD6843GYDAS8023EFG7DSA68";
		this.principalAuthnID = "testPrincipal-038903";
		
		this.attributes = new HashMap<String, IdentityAttribute>();
		IdentityAttribute identityAttribute =  new IdentityAttributeImpl();
		identityAttribute.addValue("testuser");
		this.attributes.put("uid", identityAttribute);
		
		this.identity = new Vector<IdentityType>();
		this.identity.add(new IdentityType());
		
		this.data = new IdentityDataImpl();
	}

	
	@Test
	public void testGetAttributes()
	{
		this.data.getAttributes().putAll(this.attributes);
		
		assertEquals(this.attributes, this.data.getAttributes());
	}

	@Test
	public void testGetIdentity() 
	{
		this.data.setIdentity(this.identity);
		
		assertEquals(this.identity, this.data.getIdentity());
	}

	@Test
	public void testGetPrincipalAuthnIdentifier()
	{
		this.data.setPrincipalAuthnIdentifier(this.principalAuthnID);
		
		assertEquals(this.principalAuthnID, this.data.getPrincipalAuthnIdentifier());
	}

	@Test
	public void testGetSessionID()
	{
		this.data.setSessionID(this.sessionID);
		
		assertEquals(this.sessionID, this.data.getSessionID());
	}
		
	@Test
	public void testGetCurrentHandler()
	{
		this.data.setCurrentHandler(this.handler);
		
		assertEquals(this.handler, this.data.getCurrentHandler());
	}	

}

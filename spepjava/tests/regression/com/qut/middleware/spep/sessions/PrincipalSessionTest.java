package com.qut.middleware.spep.sessions;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.spep.sessions.impl.PrincipalSessionImpl;

@SuppressWarnings("nls")
public class PrincipalSessionTest 
{

	private PrincipalSession principalSession;
	
	private String esoeSessionID;
	private Date sessionNotOnOrAfter;
	private List<String> sessionID;
	private Map<String, List<Object>> attributes;
	
	@Before
	public void setUp() throws Exception 
	{
		this.principalSession = new PrincipalSessionImpl();
		
		this.esoeSessionID = "_784327943274";
		this.sessionNotOnOrAfter = new Date();
		
		this.sessionID = new Vector<String>();
		this.sessionID.add("sessionID-1004");
		this.sessionID.add("sessionID-1039");
		
		this.attributes = new HashMap<String, List<Object>>();
		this.attributes.put("uid", new Vector<Object>());
		
	}

	
	@Test
	public void testHashCode()
	{
		this.principalSession.setEsoeSessionID(this.esoeSessionID);
		
		assertEquals(this.esoeSessionID.hashCode(), this.principalSession.hashCode());
	}

	
	@Test
	public void testGetEsoeSessionID()
	{
		this.principalSession.setEsoeSessionID(this.esoeSessionID);
		
		assertEquals(this.esoeSessionID, this.principalSession.getEsoeSessionID());
	}


	@Test
	public void testAddESOESessionIndexAndLocalSessionID()
	{
		String newEsoeSessionIndex = "789434";
		String localSessionID = "74837492242";
		
		this.principalSession.setEsoeSessionID(this.esoeSessionID);
		
		this.principalSession.addESOESessionIndexAndLocalSessionID(newEsoeSessionIndex, localSessionID);
		
		System.out.println(this.principalSession.getEsoeSessionIndex().size());
		
		assertTrue(this.principalSession.getEsoeSessionIndex().get(newEsoeSessionIndex) != null);
	}

	
	@Test
	public void testGetSessionIDList()
	{		
		String newEsoeSessionIndex = "789434";
		String localSessionID = "74837492242";
		
		this.principalSession.addESOESessionIndexAndLocalSessionID(newEsoeSessionIndex, localSessionID);
	
		assertTrue(this.principalSession.getSessionIDList().contains(localSessionID));
	}

	@Test
	public void testGetSessionNotOnOrAfter()
	{
		this.principalSession.setSessionNotOnOrAfter(this.sessionNotOnOrAfter);
		
		assertEquals(this.sessionNotOnOrAfter, this.principalSession.getSessionNotOnOrAfter());
	}

	
	@Test
	public void testGetAttributes()
	{
		this.principalSession.getAttributes().putAll(this.attributes);
		
		assertEquals(this.attributes, this.principalSession.getAttributes());
	}

	@Test
	public void testEqualsObject() 
	{
		this.principalSession.setEsoeSessionID(this.esoeSessionID);
		
		assertTrue(this.principalSession.equals(this.principalSession));
		
		assertTrue(!this.principalSession.equals(new Object()));
	}

}

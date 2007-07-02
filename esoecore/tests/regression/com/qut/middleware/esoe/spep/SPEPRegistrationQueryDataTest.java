package com.qut.middleware.esoe.spep;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.esoe.spep.sqlmap.impl.SPEPRegistrationQueryData;

public class SPEPRegistrationQueryDataTest {

	private SPEPRegistrationQueryData data;
	
	private String descID = "784789434275945";
	private String nodeID = "0";
	
	@Before
	public void setUp() throws Exception 
	{
		this.data = new SPEPRegistrationQueryData();
	}

	@Test
	public void testGetDescriptorID()
	{
		this.data.setDescriptorID(this.descID);
		
		assertEquals(this.descID, this.data.getDescriptorID());
	}

	
	@Test
	public void testGetNodeID()
	{
		this.data.setNodeID(this.nodeID);
		
		assertEquals(this.nodeID, this.data.getNodeID());
	}
	
}

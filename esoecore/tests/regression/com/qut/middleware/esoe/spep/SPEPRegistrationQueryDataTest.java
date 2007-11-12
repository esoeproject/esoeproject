package com.qut.middleware.esoe.spep;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.esoe.spep.sqlmap.impl.SPEPRegistrationQueryData;

public class SPEPRegistrationQueryDataTest {

	private SPEPRegistrationQueryData data;
	
	private Integer entID = new Integer("11");
	private String nodeID = "1";
	
	@Before
	public void setUp() throws Exception 
	{
		this.data = new SPEPRegistrationQueryData();
	}

	@Test
	public void testGetDescriptorID()
	{
		this.data.setEntID(this.entID);
		
		assertEquals(this.entID, this.data.getEntID());
	}

	
	@Test
	public void testGetNodeID()
	{
		this.data.setNodeID(this.nodeID);
		
		assertEquals(this.nodeID, this.data.getNodeID());
	}
	
}

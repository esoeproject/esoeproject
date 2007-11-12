package com.qut.middleware.esoe.spep;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.esoe.spep.sqlmap.impl.SPEPRegistrationData;

public class SPEPRegistrationDataTest {

	private SPEPRegistrationData data;
	
	private String compileDate = "01/08/3323";
	private String compileSystem = "My BSD";
	private Date dateAdded = new Date();
	private Date dateUpdated = new Date();
	private String ipAddress = "1.1.1.4";
	private String nodeID = "0";
	private Integer entID = new Integer("11");
	private String environment = "Env";
	private String version = "1.0";
	
	@Before
	public void setUp() throws Exception
	{
		this.data = new SPEPRegistrationData();		
	}

	@Test
	public void testGetCompileDate()
	{
		this.data.setCompileDate(this.compileDate);
		
		assertEquals(this.compileDate, this.data.getCompileDate());
	}
	

	@Test
	public void testGetCompileSystem()
	{
		this.data.setCompileSystem(this.compileSystem);
		
		assertEquals(this.compileSystem, this.data.getCompileSystem());
	}

		@Test
	public void testGetDateAdded()
	{
		this.data.setDateAdded(this.dateAdded);
		
		assertEquals(this.dateAdded, this.data.getDateAdded());
	}

	
	@Test
	public void testGetDateUpdated() 
	{
		this.data.setDateUpdated(this.dateUpdated);
		
		assertEquals(this.dateUpdated, this.data.getDateUpdated());
	}

	
	@Test
	public void testGetDescriptorID()
	{
		this.data.setEntID(this.entID);
		
		assertEquals(this.entID, this.data.getEntID());
	}

	@Test
	public void testGetEnvironment()
	{
		this.data.setEnvironment(this.environment);
		
		assertEquals(this.environment, this.data.getEnvironment());
	}

	
	@Test
	public void testGetIpAddress()
	{
		this.data.setIpAddress(this.ipAddress);
		
		assertEquals(this.ipAddress, this.data.getIpAddress());
	}

	
	@Test
	public void testGetNodeID()
	{
		this.data.setNodeID(this.nodeID);
		
		assertEquals(this.nodeID, this.data.getNodeID());
	}


	@Test
	public void testGetVersion()
	{
		this.data.setVersion(this.version);
		
		assertEquals(this.version, this.data.getVersion());
	}
	
}

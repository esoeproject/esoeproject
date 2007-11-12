package com.qut.middleware.esoe.pdp;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.esoe.pdp.cache.sqlmap.impl.PolicyCacheQueryData;

public class PolicyCacheQueryDataTest
{
	private PolicyCacheQueryData queryData;	
	
	@Before
	public void setUp() throws Exception
	{
		this.queryData = new PolicyCacheQueryData();
	}

	@Test
	public void testGetLastSequenceId() 
	{
		long id = 436256875345l;
		
		this.queryData.setSequenceId(new BigDecimal(id) );
		
		assertEquals(this.queryData.getSequenceId().longValue(), id);
	}

	@Test
	public void testGetDescriptorID() 
	{
		String id = "784362gsdaf6g326gr8or";
		
		this.queryData.setEntityID(id);
		
		assertEquals(this.queryData.getEntityID(), id);
		
	}

	@Test
	public void testGetPolicyId() 
	{
		String id = "y7y7fdfdgr8or";
		
		this.queryData.setPolicyId(id);
		
		assertEquals(this.queryData.getPolicyId(), id);
		
	}
	
}

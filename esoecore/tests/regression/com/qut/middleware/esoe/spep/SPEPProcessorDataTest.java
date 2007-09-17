package com.qut.middleware.esoe.spep;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.esoe.spep.bean.SPEPProcessorData;
import com.qut.middleware.esoe.spep.bean.impl.SPEPProcessorDataImpl;

@SuppressWarnings("nls")
public class SPEPProcessorDataTest {
	
	private SPEPProcessorData data;
	
	private String descriptorID = "*847893ryfde76ft80247tgrfG*bo";
	private byte[] requestDoc = new String("<hello></hello>").getBytes();
	private byte[] responseDoc = new String("<goodbye></goodbye>").getBytes();
	private int index = 1;
	
	@Before
	public void setUp() throws Exception
	{
		this.data = new SPEPProcessorDataImpl();
	}
	
	@Test
	public void testGetAuthzCacheIndex()
	{
		// tests get and set
		this.data.setAuthzCacheIndex(this.index);
		
		assertEquals(this.index, this.data.getAuthzCacheIndex());
	}

	@Test
	public void testGetRequestDescriptorID()
	{
		this.data.setIssuerID(this.descriptorID);
		
		assertEquals(this.descriptorID, this.data.getIssuerID());
	}
		
	@Test
	public void testGetRequestDocument()
	{
		this.data.setRequestDocument(this.requestDoc);
		
		assertEquals(this.requestDoc, this.data.getRequestDocument());
	}

	@Test
	public void testGetResponseDocument()
	{
		this.data.setResponseDocument(this.responseDoc);
		
		assertEquals(this.responseDoc, this.data.getResponseDocument());
	}
		
}

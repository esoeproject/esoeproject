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
	private String requestDoc = "<hello></hello>";
	private String responseDoc = "<goodbye></goodbye>";
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
		this.data.setRequestDescriptorID(this.descriptorID);
		
		assertEquals(this.descriptorID, this.data.getRequestDescriptorID());
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

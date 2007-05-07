package com.qut.middleware.esoe.aa;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.esoe.aa.bean.AAProcessorData;
import com.qut.middleware.esoe.aa.bean.impl.AAProcessorDataImpl;


@SuppressWarnings("nls")
public class AAProcessorDataTest {

	private AAProcessorData data;
	
	@Before
	public void setUp() throws Exception
	{
		this.data = new AAProcessorDataImpl();
	}

	@Test
	public void testGetRequestDocument()
	{
		// covers get and set
		this.data.setRequestDocument("<test></test>");
	
		assertEquals("<test></test>", this.data.getRequestDocument());
	}
	
	@Test
	public void testGetDescriptorID()
	{
		// covers get and set
		this.data.setDescriptorID("_823478475834");
	
		assertEquals("_823478475834", this.data.getDescriptorID());	
	}

	
	@Test
	public void testGetSubjectID()
	{
		// covers get and set
		this.data.setSubjectID("_823478475834");
	
		assertEquals("_823478475834", this.data.getSubjectID());	

	}

	@Test
	public void testSetResponseDocument()
	{
		// covers get and set
		this.data.setResponseDocument("<something></something>");
	
		assertEquals("<something></something>", this.data.getResponseDocument());
	
	}

	
}

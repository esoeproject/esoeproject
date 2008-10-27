package com.qut.middleware.esoe.aa;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;

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
		DOMImplementation dom = null;
		try 
		{
			dom = DOMImplementationRegistry.newInstance().getDOMImplementation("XML 1.0");
		}
		catch (Exception e)
		{
			fail("Failed to instantiate DomImplementation.");
			//e.printStackTrace();
		} 
		
		Element elem = null;
		Document doc = null;
		try
		{
			doc =  dom.createDocument("http://www.w3.org/2001/XMLSchema", "xs:string", null);
			elem = doc.createElement("TEST");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail("Error occured created Document or Element");
		}
		
		assertNotNull(elem);
		
		// covers get and set
		this.data.setRequestDocument(elem);
	
		assertEquals(elem, this.data.getRequestDocument());
	}
	
	@Test
	public void testGetDescriptorID()
	{
		// covers get and set
		this.data.setIssuerID("_823478475834");
	
		assertEquals("_823478475834", this.data.getIssuerID());	
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
		DOMImplementation dom = null;
		try 
		{
			dom = DOMImplementationRegistry.newInstance().getDOMImplementation("XML 1.0");
		}
		catch (Exception e)
		{
			fail("Failed to instantiate DomImplementation.");
			//e.printStackTrace();
		} 
		
		Element elem = null;
		Document doc = null;
		try
		{
			doc =  dom.createDocument("http://www.w3.org/2001/XMLSchema", "xs:string", null);
			elem = doc.createElement("TEST");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail("Error occured created Document or Element");
		}
		
		assertNotNull(elem);
		
		// covers get and set
		this.data.setResponseDocument(elem);
	
		assertEquals(elem, this.data.getResponseDocument());
	
	}

	
}

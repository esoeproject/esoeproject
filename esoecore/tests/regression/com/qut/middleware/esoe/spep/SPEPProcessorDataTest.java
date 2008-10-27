package com.qut.middleware.esoe.spep;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;

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
		this.data.setRequestDocument(this.getDodgyElement("Request"));
		
		assertEquals("Request", this.data.getRequestDocument().getNodeName());
	}

	@Test
	public void testGetResponseDocument()
	{
		this.data.setResponseDocument(this.getDodgyElement("Response"));
		
		assertEquals("Response", this.data.getResponseDocument().getNodeName());
	}
		
	private Element getDodgyElement(String nodename)
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
			elem = doc.createElement(nodename);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail("Error occured created Document or Element");
		}
		
		return elem;
	}
}

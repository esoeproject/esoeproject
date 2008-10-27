package com.qut.middleware.esoe.deledauthn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;

import com.qut.middleware.esoe.delegauthn.bean.DelegatedAuthenticationData;
import com.qut.middleware.esoe.delegauthn.bean.impl.DelegatedAuthenticationDataImpl;
import com.qut.middleware.saml2.schemas.esoe.delegated.RegisterPrincipalRequest;

public class DelegAuthenticationDataTest {

	private DelegatedAuthenticationData data;
	
	@Before
	public void setUp() throws Exception
	{
		this.data = new DelegatedAuthenticationDataImpl();
	}

	@Test
	public void testGetRequestDocument()
	{
		Element elem = this.getDodgyElement();
		
		assertNotNull(elem);
		
		// tests get and set
		this.data.setRequestDocument(elem);
		
		assertEquals(elem, this.data.getRequestDocument());
	}

	@Test
	public void testGetResponseDocument()
	{
		Element elem = this.getDodgyElement();
		
		assertNotNull(elem);
		
		// tests get and set
		this.data.setResponseDocument(elem);
		
		assertEquals(elem, this.data.getResponseDocument());
	}
	
	@Test
	public void testGetRegisterPrincipalRequest()
	{
		RegisterPrincipalRequest princRequest = new RegisterPrincipalRequest();
		
		this.data.setRegisterPrincipalRequest(princRequest);
		
		assertEquals(princRequest, this.data.getRegisterPrincipalRequest());
	}

	private Element getDodgyElement()
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
		
		return elem;
	}
}

/**
 * 
 */
package com.qut.middleware.esoe.pdp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;

import com.qut.middleware.esoe.authz.bean.AuthorizationProcessorData;
import com.qut.middleware.esoe.authz.bean.impl.AuthorizationProcessorDataImpl;

public class AuthorizationProcessorDataTest {

	private AuthorizationProcessorData testData;
	
	private String descriptorID = "546348723834786438"; //$NON-NLS-1$
	private String subjectID = "746187672gfefga68fgtaifhg7tfe7087geusiahdg78etf6e7wg9fe9d6sartsdf7e9"; //$NON-NLS-1$
	private String request = "Request";
	private String response = "Response";
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		this.testData = new AuthorizationProcessorDataImpl();
	}


	/**
	 * Test method for {@link com.qut.middleware.esoe.authz.bean.impl.AuthorizationProcessorDataImpl#setIssuerID(java.lang.String)}.
	 */
	@Test
	public void testSetDescriptorID()
	{		
		this.testData.setIssuerID(this.descriptorID);
		
		assertTrue(this.testData.getIssuerID().equals(this.descriptorID)); 
	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.authz.bean.impl.AuthorizationProcessorDataImpl#setRequestDocument(org.opensaml.saml2.core.Request)}.
	 */
	@Test
	public void testSetRequestDocument() 
	{
		this.testData.setRequestDocument(this.getDodgyElement(this.request));
		
		assertEquals("Set LXACML request document comparison.", this.request, this.testData.getRequestDocument().getNodeName());  //$NON-NLS-1$
		
	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.authz.bean.impl.AuthorizationProcessorDataImpl#setResponseDocument(org.opensaml.saml2.core.Response)}.
	 */
	@Test
	public void testSetResponseDocument()
	{
		this.testData.setResponseDocument(this.getDodgyElement(this.response));
		
		assertNotSame("Request document has not been set.", null, this.testData.getResponseDocument()); //$NON-NLS-1$
		
		assertEquals("Set LXACML response document comparison.", this.response, this.testData.getResponseDocument().getNodeName());  //$NON-NLS-1$
		
	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.authz.bean.impl.AuthorizationProcessorDataImpl#setSubjectID(java.lang.String)}.
	 */
	@Test
	public void testSetSubjectID()
	{
		// set value should not match value 
		this.testData.setIssuerID(this.subjectID + "783742343729"); //$NON-NLS-1$
		
		assertNotSame("Set subject ID comparison.", this.subjectID, this.testData.getIssuerID());  //$NON-NLS-1$
		
		this.testData.setIssuerID(this.subjectID);
		
		assertSame("Set subject ID comparison.", this.subjectID, this.testData.getIssuerID()); //$NON-NLS-1$
	}


	/**
	 * Test method for {@link com.qut.middleware.esoe.authz.bean.impl.AuthorizationProcessorDataImpl#getIssuerID()}.
	 */
	@Test
	public void testGetDescriptorID() 
	{		
		this.testData.setIssuerID(this.descriptorID);
		
		assertSame("Set Descriptor ID comparison.", this.descriptorID, this.testData.getIssuerID()); //$NON-NLS-1$
	}

	
	/**
	 * Test method for {@link com.qut.middleware.esoe.authz.bean.impl.AuthorizationProcessorDataImpl#getRequestDocument()}.
	 */
	@Test
	public void testGetRequestDocument()
	{		
		this.testData.setRequestDocument(this.getDodgyElement(this.request));
		
		assertEquals("Get LAXCML request document comparison.", this.request, this.testData.getRequestDocument().getNodeName());  //$NON-NLS-1$
		
	}

	
	/**
	 * Test method for {@link com.qut.middleware.esoe.authz.bean.impl.AuthorizationProcessorDataImpl#getResponseDocument()}.
	 */
	@Test
	public void testGetResponseDocument()
	{
		this.testData.setResponseDocument(this.getDodgyElement(this.response));
				
		assertEquals("Get LAXCML response document comparison.", this.response, this.testData.getResponseDocument().getNodeName());  //$NON-NLS-1$
	}

	
	/**
	 * Test method for {@link com.qut.middleware.esoe.authz.bean.impl.AuthorizationProcessorDataImpl#getSubjectID()}.
	 */
	@Test
	public void testGetSubjectID()
	{		
		// set value should not match value 
		this.testData.setIssuerID(this.subjectID + "7837429"); //$NON-NLS-1$
		
		assertNotSame("Subject ID comparison.", this.subjectID, this.testData.getIssuerID());  //$NON-NLS-1$
		
		this.testData.setIssuerID(this.subjectID);
		
		assertSame("Subject ID comparison.", this.subjectID, this.testData.getIssuerID()); //$NON-NLS-1$
	}

	private Element getDodgyElement(String value)
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
			elem = doc.createElement(value);
			elem.setAttribute("test", "hi there");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail("Error occured created Document or Element");
		}
		
		return elem;
	}
}

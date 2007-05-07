/**
 * 
 */
package com.qut.middleware.esoe.pdp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.esoe.pdp.bean.AuthorizationProcessorData;
import com.qut.middleware.esoe.pdp.bean.impl.AuthorizationProcessorDataImpl;

/**
 * @author zedly
 *
 */
public class AuthorizationProcessorDataTest {

	private AuthorizationProcessorData testData;
	
	private String descriptorID = "546348723834786438"; //$NON-NLS-1$
	private String subjectID = "746187672gfefga68fgtaifhg7tfe7087geusiahdg78etf6e7wg9fe9d6sartsdf7e9"; //$NON-NLS-1$
	private String request = "<question><mytest value=\"blah\" /></question>";
	private String response = "<answer><mytest value=\"blah\" /></answer>";
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		this.testData = new AuthorizationProcessorDataImpl();
	}


	/**
	 * Test method for {@link com.qut.middleware.esoe.pdp.bean.impl.AuthorizationProcessorDataImpl#setDescriptorID(java.lang.String)}.
	 */
	@Test
	public void testSetDescriptorID()
	{		
		this.testData.setDescriptorID(this.descriptorID);
		
		assertTrue(this.testData.getDescriptorID().equals(this.descriptorID)); 
	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.pdp.bean.impl.AuthorizationProcessorDataImpl#setRequestDocument(org.opensaml.saml2.core.Request)}.
	 */
	@Test
	public void testSetRequestDocument() 
	{
		this.testData.setRequestDocument(this.request);
		
		assertEquals("Set LXACML request document comparison.", this.request, this.testData.getRequestDocument());  //$NON-NLS-1$
		
	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.pdp.bean.impl.AuthorizationProcessorDataImpl#setResponseDocument(org.opensaml.saml2.core.Response)}.
	 */
	@Test
	public void testSetResponseDocument()
	{
		this.testData.setResponseDocument(this.response);
		
		assertNotSame("Request document has not been set.", null, this.testData.getResponseDocument()); //$NON-NLS-1$
		
		assertEquals("Set LXACML response document comparison.", this.response, this.testData.getResponseDocument());  //$NON-NLS-1$
		
	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.pdp.bean.impl.AuthorizationProcessorDataImpl#setSubjectID(java.lang.String)}.
	 */
	@Test
	public void testSetSubjectID()
	{
		// set value should not match value 
		this.testData.setDescriptorID(this.subjectID + "783742343729"); //$NON-NLS-1$
		
		assertNotSame("Set subject ID comparison.", this.subjectID, this.testData.getDescriptorID());  //$NON-NLS-1$
		
		this.testData.setDescriptorID(this.subjectID);
		
		assertSame("Set subject ID comparison.", this.subjectID, this.testData.getDescriptorID()); //$NON-NLS-1$
	}


	/**
	 * Test method for {@link com.qut.middleware.esoe.pdp.bean.impl.AuthorizationProcessorDataImpl#getDescriptorID()}.
	 */
	@Test
	public void testGetDescriptorID() 
	{		
		this.testData.setDescriptorID(this.descriptorID);
		
		assertSame("Set Descriptor ID comparison.", this.descriptorID, this.testData.getDescriptorID()); //$NON-NLS-1$
	}

	
	/**
	 * Test method for {@link com.qut.middleware.esoe.pdp.bean.impl.AuthorizationProcessorDataImpl#getRequestDocument()}.
	 */
	@Test
	public void testGetRequestDocument()
	{		
		this.testData.setRequestDocument(this.request);
		
		assertEquals("Get LAXCML request document comparison.", this.request, this.testData.getRequestDocument());  //$NON-NLS-1$
		
	}

	
	/**
	 * Test method for {@link com.qut.middleware.esoe.pdp.bean.impl.AuthorizationProcessorDataImpl#getResponseDocument()}.
	 */
	@Test
	public void testGetResponseDocument()
	{
		this.testData.setResponseDocument(this.response);
				
		assertEquals("Get LAXCML response document comparison.", this.response, this.testData.getResponseDocument());  //$NON-NLS-1$
	}

	
	/**
	 * Test method for {@link com.qut.middleware.esoe.pdp.bean.impl.AuthorizationProcessorDataImpl#getSubjectID()}.
	 */
	@Test
	public void testGetSubjectID()
	{		
		// set value should not match value 
		this.testData.setDescriptorID(this.subjectID + "7837429"); //$NON-NLS-1$
		
		assertNotSame("Subject ID comparison.", this.subjectID, this.testData.getDescriptorID());  //$NON-NLS-1$
		
		this.testData.setDescriptorID(this.subjectID);
		
		assertSame("Subject ID comparison.", this.subjectID, this.testData.getDescriptorID()); //$NON-NLS-1$
	}

}

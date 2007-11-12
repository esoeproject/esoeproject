/**
 * 
 */
package com.qut.middleware.delegator.openid.authn.bean;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.delegator.openid.authn.bean.impl.OpenIDAttributeImpl;


public class OpenIDAttributeImplTest {

	private OpenIDAttributeImpl openIDAttribute;
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		openIDAttribute = new OpenIDAttributeImpl();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link com.qut.middleware.delegator.openid.authn.bean.impl.OpenIDAttributeImpl#getLabel()}.
	 */
	@Test
	public void testGetLabel() {
		String label = "testLabel";
		this.openIDAttribute.setLabel(label);
		assertEquals(label, this.openIDAttribute.getLabel());
	}

	/**
	 * Test method for {@link com.qut.middleware.delegator.openid.authn.bean.impl.OpenIDAttributeImpl#getSchema()}.
	 */
	@Test
	public void testGetSchema() {
		String schema = "testschema.xsd";
		this.openIDAttribute.setSchema(schema);
		assertEquals(schema, this.openIDAttribute.getSchema());
	}

	/**
	 * Test method for {@link com.qut.middleware.delegator.openid.authn.bean.impl.OpenIDAttributeImpl#isRequired()}.
	 */
	@Test
	public void testIsRequired() {
		boolean required = true;
		this.openIDAttribute.setRequired(required);
		assertEquals(required, this.openIDAttribute.isRequired());
		required = false;
		this.openIDAttribute.setRequired(required);
		assertEquals(required, this.openIDAttribute.isRequired());
	}

	/**
	 * Test method for {@link com.qut.middleware.delegator.openid.authn.bean.impl.OpenIDAttributeImpl#getEsoeAttributeName()}.
	 */
	@Test
	public void testGetEsoeAttributeName() {
		String esoeAttributeName = "esoeAttribute";
		this.openIDAttribute.setEsoeAttributeName(esoeAttributeName);
		assertEquals(esoeAttributeName, this.openIDAttribute.getEsoeAttributeName());
	}

	/**
	 * Test method for {@link com.qut.middleware.delegator.openid.authn.bean.impl.OpenIDAttributeImpl#getValue()}.
	 */
	@Test
	public void testGetValue() {
		String value = "value";
		this.openIDAttribute.setValue(value);
		assertEquals(value, this.openIDAttribute.getValue());
	}

	/**
	 * Test method for {@link com.qut.middleware.delegator.openid.authn.bean.impl.OpenIDAttributeImpl#getValuePrepend()}.
	 */
	@Test
	public void testGetValuePrepend() {
		String valuePrepend = "val-";
		this.openIDAttribute.setValuePrepend(valuePrepend);
		assertEquals(valuePrepend, this.openIDAttribute.getValuePrepend());
	}

}

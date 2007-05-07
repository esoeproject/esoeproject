package com.qut.middleware.esoe.authn.impl;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.esoe.authn.bean.AuthnIdentityAttribute;
import com.qut.middleware.esoe.authn.bean.impl.AuthnIdentityAttributeImpl;


@SuppressWarnings("nls")
public class AuthnIdentityAttributeTest {

	private AuthnIdentityAttribute idAttr;

	private String name;
	private List<String> values; 
	
	@Before
	public void setUp() throws Exception
	{
		this.idAttr = new AuthnIdentityAttributeImpl();
		
		this.name = "fred";
		this.values =  new Vector<String>();
		this.values.add("red");
		this.values.add("puce");
	}

	@Test
	public void testSetName()
	{
		this.idAttr.setName(this.name);
		
		assertNotNull(this.idAttr.getName());
		
		assertEquals("getName() returned incorrect value.", this.name, this.idAttr.getName());
		
	}

	@Test
	public void testSetValues()
	{
		this.idAttr.setValues(this.values);

		assertTrue(this.values.size() == 2);
		
		assertEquals("values not set as expected.", this.values.size(), this.idAttr.getValues().size());
	}

	@Test
	public void testGetName()
	{		
		this.idAttr.setName(this.name);
		
		assertNotNull(this.idAttr.getName());
		
		assertEquals("getName() returned incorrect value.", this.name, this.idAttr.getName());
	
	}

	@Test
	public void testGetValues() 
	{
		for(String value: this.idAttr.getValues())
		{
			assertTrue(this.values.contains(value));
		}
	}

}

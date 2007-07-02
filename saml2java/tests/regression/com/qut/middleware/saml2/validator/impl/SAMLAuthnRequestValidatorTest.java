package com.qut.middleware.saml2.validator.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.saml2.exception.InvalidSAMLRequestException;
import com.qut.middleware.saml2.schemas.protocol.AuthnRequest;
import com.qut.middleware.saml2.schemas.protocol.NameIDPolicy;

public class SAMLAuthnRequestValidatorTest
{

	@Before
	public void setUp() throws Exception
	{
	}

	@After
	public void tearDown() throws Exception
	{
	}
	
	/* Tests to ensure null NameIDPolicy throws exception */
	@Test(expected=InvalidSAMLRequestException.class)
	public void testValidate1() throws InvalidSAMLRequestException
	{
		SAMLAuthnRequestValidatorImpl validator = new SAMLAuthnRequestValidatorImpl();
		AuthnRequest request = new AuthnRequest();
		request.setNameIDPolicy(null);
		
		validator.validate(request);
	}
	
	/* Tests to ensure null consumer assertion index throws exception */
	@Test(expected=InvalidSAMLRequestException.class)
	public void testValidate1a() throws InvalidSAMLRequestException
	{
		SAMLAuthnRequestValidatorImpl validator = new SAMLAuthnRequestValidatorImpl();
		AuthnRequest request = new AuthnRequest();
		request.setNameIDPolicy(new NameIDPolicy());
		request.setAssertionConsumerServiceIndex(null);
		validator.validate(request);
	}

}

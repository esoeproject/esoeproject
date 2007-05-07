package com.qut.middleware.saml2.validator.impl;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.fail;

import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;

import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.saml2.VersionConstants;
import com.qut.middleware.saml2.exception.InvalidSAMLRequestException;
import com.qut.middleware.saml2.identifier.IdentifierCache;
import com.qut.middleware.saml2.identifier.exception.IdentifierCollisionException;
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.protocol.AuthnRequest;
import com.qut.middleware.saml2.schemas.protocol.RequestAbstractType;
import com.qut.middleware.saml2.schemas.protocol.Status;
import com.qut.middleware.saml2.schemas.protocol.StatusCode;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

public class SAMLRequestValidatorTest
{

	@Before
	public void setUp() throws Exception
	{
	}

	@After
	public void tearDown() throws Exception
	{
	}

	/* Tests to ensure that null identifier cache throws the correct exception */
	@Test(expected = IllegalArgumentException.class)
	public void testSAMLRequestValidatorImpl1()
	{
		SAMLRequestValidatorImpl validator = new SAMLRequestValidatorImpl(null, 100);
	}

	/* Tests to ensure that out of range time interval throws the correct exception */
	@Test(expected = IllegalArgumentException.class)
	public void testSAMLRequestValidatorImpl1a()
	{
		IdentifierCache cache = createMock(IdentifierCache.class);
		replay(cache);
		SAMLRequestValidatorImpl validator = new SAMLRequestValidatorImpl(cache, (Integer.MAX_VALUE / 1000) + 1);
		verify(cache);
	}

	/* Tests to ensure that null request throws the correct exception */
	@Test(expected = IllegalArgumentException.class)
	public void testValidate1() throws InvalidSAMLRequestException
	{
		IdentifierCache cache = createMock(IdentifierCache.class);
		replay(cache);
		SAMLRequestValidatorImpl validator = new SAMLRequestValidatorImpl(cache, 100);
		
		validator.validate(null);
		verify(cache);
	}
	
	/* Tests to ensure that null request ID throws the correct exception */
	@Test(expected = InvalidSAMLRequestException.class)
	public void testValidate2() throws InvalidSAMLRequestException
	{
		IdentifierCache cache = createMock(IdentifierCache.class);
		replay(cache);
		SAMLRequestValidatorImpl validator = new SAMLRequestValidatorImpl(cache, 100);
		
		RequestAbstractType request = new AuthnRequest();
		request.setID(null);
		
		validator.validate(request);
		verify(cache);
	}
	
	/* Tests to ensure that null request issue instant throws the correct exception */
	@Test(expected = InvalidSAMLRequestException.class)
	public void testValidate2a() throws InvalidSAMLRequestException
	{
		IdentifierCache cache = createMock(IdentifierCache.class);
		replay(cache);
		SAMLRequestValidatorImpl validator = new SAMLRequestValidatorImpl(cache, 100);
		
		RequestAbstractType request = new AuthnRequest();
		request.setID("1234");
		request.setIssueInstant(null);
		
		validator.validate(request);
		verify(cache);
	}
	
	/* Tests to ensure that request issue instant thats out of scope throws the correct exception */
	@Test(expected = InvalidSAMLRequestException.class)
	public void testValidate2b() throws InvalidSAMLRequestException
	{
		IdentifierCache cache = createMock(IdentifierCache.class);
		replay(cache);
		SAMLRequestValidatorImpl validator = new SAMLRequestValidatorImpl(cache, 100);
		
		RequestAbstractType request = new AuthnRequest();
		request.setID("1234");
		
		/* GMT timezone */
		SimpleTimeZone gmt = new SimpleTimeZone(0, "UTC");

		/* GregorianCalendar with the GMT time zone */
		GregorianCalendar calendar = new GregorianCalendar(gmt);
		calendar.add(GregorianCalendar.MINUTE, 10);
		XMLGregorianCalendar xmlCalendar = new XMLGregorianCalendarImpl(calendar);
		
		request.setIssueInstant(xmlCalendar);
		
		validator.validate(request);
		verify(cache);
	}
	
	/* Tests to ensure that 0 length request issuer length throws the correct exception */
	@Test(expected = InvalidSAMLRequestException.class)
	public void testValidate2c() throws InvalidSAMLRequestException
	{
		IdentifierCache cache = createMock(IdentifierCache.class);
		replay(cache);
		SAMLRequestValidatorImpl validator = new SAMLRequestValidatorImpl(cache, 100);
		
		RequestAbstractType request = new AuthnRequest();
		request.setID("1234");
		
		/* GMT timezone */
		SimpleTimeZone gmt = new SimpleTimeZone(0, "UTC");

		/* GregorianCalendar with the GMT time zone */
		GregorianCalendar calendar = new GregorianCalendar(gmt);
		XMLGregorianCalendar xmlCalendar = new XMLGregorianCalendarImpl(calendar);
		
		request.setIssueInstant(xmlCalendar);
		
		NameIDType issuer = new NameIDType();
		issuer.setValue("");
		request.setIssuer(issuer);
		
		validator.validate(request);
		verify(cache);
	}
	
	/* Tests to ensure that invalid SAML version throws the correct exception */
	@Test(expected = InvalidSAMLRequestException.class)
	public void testValidate2d() throws InvalidSAMLRequestException
	{
		IdentifierCache cache = createMock(IdentifierCache.class);
		replay(cache);
		SAMLRequestValidatorImpl validator = new SAMLRequestValidatorImpl(cache, 100);
		
		RequestAbstractType request = new AuthnRequest();
		request.setID("1234");
		
		/* GMT timezone */
		SimpleTimeZone gmt = new SimpleTimeZone(0, "UTC");

		/* GregorianCalendar with the GMT time zone */
		GregorianCalendar calendar = new GregorianCalendar(gmt);
		XMLGregorianCalendar xmlCalendar = new XMLGregorianCalendarImpl(calendar);
		
		request.setIssueInstant(xmlCalendar);
		
		NameIDType issuer = new NameIDType();
		issuer.setValue("testcase");
		request.setIssuer(issuer);
		request.setVersion("SAML-TC");
		
		validator.validate(request);
		verify(cache);
	}
	
	/* Tests to ensure that replay attack generates the correct exception */
	@Test(expected = InvalidSAMLRequestException.class)
	public void testValidate2g() throws InvalidSAMLRequestException
	{
		IdentifierCache cache = createMock(IdentifierCache.class);
		try
		{
			cache.registerIdentifier("1234");
		}
		catch (IdentifierCollisionException e)
		{
			e.printStackTrace();
			fail("IdentifierCollisionException not expected in this test");
		}
		expectLastCall().andThrow(new IdentifierCollisionException("testcase"));
		replay(cache);
		
		SAMLRequestValidatorImpl validator = new SAMLRequestValidatorImpl(cache, 100);
		
		RequestAbstractType request = new AuthnRequest();
		request.setID("1234");
		
		/* GMT timezone */
		SimpleTimeZone gmt = new SimpleTimeZone(0, "UTC");

		/* GregorianCalendar with the GMT time zone */
		GregorianCalendar calendar = new GregorianCalendar(gmt);
		XMLGregorianCalendar xmlCalendar = new XMLGregorianCalendarImpl(calendar);
		
		request.setIssueInstant(xmlCalendar);
		
		NameIDType issuer = new NameIDType();
		issuer.setValue("testcase");
		request.setIssuer(issuer);
		request.setVersion(VersionConstants.saml20);
		
		Status status = new Status();
		StatusCode code = new StatusCode();
		code.setValue("success");
		status.setStatusCode(code);
		
		validator.validate(request);
		verify(cache);
	}
	
	/* Tests to ensure no exceptions with valid request */
	@Test
	public void testValidate3() throws InvalidSAMLRequestException
	{
		IdentifierCache cache = createMock(IdentifierCache.class);
		try
		{
			cache.registerIdentifier("1234");
		}
		catch (IdentifierCollisionException e)
		{
			e.printStackTrace();
			fail("IdentifierCollisionException not expected in this test");
		}
		replay(cache);
		SAMLRequestValidatorImpl validator = new SAMLRequestValidatorImpl(cache, 100);
		
		RequestAbstractType request = new AuthnRequest();
		request.setID("1234");
		
		/* GMT timezone */
		SimpleTimeZone gmt = new SimpleTimeZone(0, "UTC");

		/* GregorianCalendar with the GMT time zone */
		GregorianCalendar calendar = new GregorianCalendar(gmt);
		XMLGregorianCalendar xmlCalendar = new XMLGregorianCalendarImpl(calendar);
		
		request.setIssueInstant(xmlCalendar);
		
		NameIDType issuer = new NameIDType();
		issuer.setValue("testcase");
		request.setIssuer(issuer);
		request.setVersion(VersionConstants.saml20);
		
		Status status = new Status();
		StatusCode code = new StatusCode();
		code.setValue("success");
		status.setStatusCode(code);
		
		validator.validate(request);
		verify(cache);
	}

}

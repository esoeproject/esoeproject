package com.qut.middleware.saml2.validator.impl;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
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
import com.qut.middleware.saml2.exception.InvalidSAMLResponseException;
import com.qut.middleware.saml2.identifier.IdentifierCache;
import com.qut.middleware.saml2.identifier.exception.IdentifierCollisionException;
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.protocol.Response;
import com.qut.middleware.saml2.schemas.protocol.Status;
import com.qut.middleware.saml2.schemas.protocol.StatusCode;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

public class SAMLResponseValidatorTest
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
	public void testSAMLResponseValidatorImpl1()
	{
		SAMLResponseValidatorImpl validator = new SAMLResponseValidatorImpl(null, 100);
	}

	/* Tests to ensure that out of range time interval throws the correct exception */
	@Test(expected = IllegalArgumentException.class)
	public void testSAMLResponseValidatorImpl1a()
	{
		IdentifierCache cache = createMock(IdentifierCache.class);
		replay(cache);
		SAMLResponseValidatorImpl validator = new SAMLResponseValidatorImpl(cache, (Integer.MAX_VALUE / 1000) + 1);
		verify(cache);
	}

	/* Tests to ensure that null response throws the correct exception */
	@Test(expected = IllegalArgumentException.class)
	public void testValidate1() throws InvalidSAMLResponseException
	{
		IdentifierCache cache = createMock(IdentifierCache.class);
		replay(cache);
		SAMLResponseValidatorImpl validator = new SAMLResponseValidatorImpl(cache, 100);
		
		validator.validate(null);
		verify(cache);
	}
	
	/* Tests to ensure that null response ID throws the correct exception */
	@Test(expected = InvalidSAMLResponseException.class)
	public void testValidate2() throws InvalidSAMLResponseException
	{
		IdentifierCache cache = createMock(IdentifierCache.class);
		replay(cache);
		SAMLResponseValidatorImpl validator = new SAMLResponseValidatorImpl(cache, 100);
		
		Response response = new Response();
		response.setID(null);
		
		validator.validate(response);
		verify(cache);
	}
	
	/* Tests to ensure that null response issue instant throws the correct exception */
	@Test(expected = InvalidSAMLResponseException.class)
	public void testValidate2a() throws InvalidSAMLResponseException
	{
		IdentifierCache cache = createMock(IdentifierCache.class);
		replay(cache);
		SAMLResponseValidatorImpl validator = new SAMLResponseValidatorImpl(cache, 100);
		
		Response response = new Response();
		response.setID("1234");
		response.setIssueInstant(null);
		
		validator.validate(response);
		verify(cache);
	}
	
	/* Tests to ensure that response issue instant thats out of scope throws the correct exception */
	@Test(expected = InvalidSAMLResponseException.class)
	public void testValidate2b() throws InvalidSAMLResponseException
	{
		IdentifierCache cache = createMock(IdentifierCache.class);
		replay(cache);
		SAMLResponseValidatorImpl validator = new SAMLResponseValidatorImpl(cache, 100);
		
		Response response = new Response();
		response.setID("1234");
		
		/* GMT timezone */
		SimpleTimeZone gmt = new SimpleTimeZone(0, "UTC");

		/* GregorianCalendar with the GMT time zone */
		GregorianCalendar calendar = new GregorianCalendar(gmt);
		calendar.add(GregorianCalendar.MINUTE, 10);
		XMLGregorianCalendar xmlCalendar = new XMLGregorianCalendarImpl(calendar);
		
		response.setIssueInstant(xmlCalendar);
		
		validator.validate(response);
		verify(cache);
	}
	
	/* Tests to ensure that 0 length response issuer length throws the correct exception */
	@Test(expected = InvalidSAMLResponseException.class)
	public void testValidate2c() throws InvalidSAMLResponseException
	{
		IdentifierCache cache = createMock(IdentifierCache.class);
		replay(cache);
		SAMLResponseValidatorImpl validator = new SAMLResponseValidatorImpl(cache, 100);
		
		Response response = new Response();
		response.setID("1234");
		
		/* GMT timezone */
		SimpleTimeZone gmt = new SimpleTimeZone(0, "UTC");

		/* GregorianCalendar with the GMT time zone */
		GregorianCalendar calendar = new GregorianCalendar(gmt);
		XMLGregorianCalendar xmlCalendar = new XMLGregorianCalendarImpl(calendar);
		
		response.setIssueInstant(xmlCalendar);
		
		NameIDType issuer = new NameIDType();
		issuer.setValue("");
		response.setIssuer(issuer);
		
		validator.validate(response);
		verify(cache);
	}
	
	/* Tests to ensure that invalid SAML version throws the correct exception */
	@Test(expected = InvalidSAMLResponseException.class)
	public void testValidate2d() throws InvalidSAMLResponseException
	{
		IdentifierCache cache = createMock(IdentifierCache.class);
		replay(cache);
		SAMLResponseValidatorImpl validator = new SAMLResponseValidatorImpl(cache, 100);
		
		Response response = new Response();
		response.setID("1234");
		
		/* GMT timezone */
		SimpleTimeZone gmt = new SimpleTimeZone(0, "UTC");

		/* GregorianCalendar with the GMT time zone */
		GregorianCalendar calendar = new GregorianCalendar(gmt);
		XMLGregorianCalendar xmlCalendar = new XMLGregorianCalendarImpl(calendar);
		
		response.setIssueInstant(xmlCalendar);
		
		NameIDType issuer = new NameIDType();
		issuer.setValue("testcase");
		response.setIssuer(issuer);
		response.setVersion("SAML-TC");
		
		validator.validate(response);
		verify(cache);
	}
	
	/* Tests to ensure that invalid SAML response status throws the correct exception */
	@Test(expected = InvalidSAMLResponseException.class)
	public void testValidate2e() throws InvalidSAMLResponseException
	{
		IdentifierCache cache = createMock(IdentifierCache.class);
		replay(cache);
		SAMLResponseValidatorImpl validator = new SAMLResponseValidatorImpl(cache, 100);
		
		Response response = new Response();
		response.setID("1234");
		
		/* GMT timezone */
		SimpleTimeZone gmt = new SimpleTimeZone(0, "UTC");

		/* GregorianCalendar with the GMT time zone */
		GregorianCalendar calendar = new GregorianCalendar(gmt);
		XMLGregorianCalendar xmlCalendar = new XMLGregorianCalendarImpl(calendar);
		
		response.setIssueInstant(xmlCalendar);
		
		NameIDType issuer = new NameIDType();
		issuer.setValue("testcase");
		response.setIssuer(issuer);
		response.setVersion(VersionConstants.saml20);
		
		Status status = new Status();
		status.setStatusCode(null);
		response.setStatus(status);
		
		validator.validate(response);
		verify(cache);
	}
	
	/* Tests to ensure that invalid SAML in response to field gives the correct exception */
	@Test(expected = InvalidSAMLResponseException.class)
	public void testValidate2f() throws InvalidSAMLResponseException
	{
		IdentifierCache cache = createMock(IdentifierCache.class);
		expect(cache.containsIdentifier("5678")).andReturn(false);
		replay(cache);
		SAMLResponseValidatorImpl validator = new SAMLResponseValidatorImpl(cache, 100);
		
		Response response = new Response();
		response.setID("1234");
		
		/* GMT timezone */
		SimpleTimeZone gmt = new SimpleTimeZone(0, "UTC");

		/* GregorianCalendar with the GMT time zone */
		GregorianCalendar calendar = new GregorianCalendar(gmt);
		XMLGregorianCalendar xmlCalendar = new XMLGregorianCalendarImpl(calendar);
		
		response.setIssueInstant(xmlCalendar);
		
		NameIDType issuer = new NameIDType();
		issuer.setValue("testcase");
		response.setIssuer(issuer);
		response.setVersion(VersionConstants.saml20);
		
		Status status = new Status();
		StatusCode code = new StatusCode();
		code.setValue("success");
		status.setStatusCode(code);
		response.setStatus(status);
		response.setInResponseTo("5678");
		
		validator.validate(response);
		verify(cache);
	}
	
	/* Tests to ensure that replay attack generates the correct exception */
	@Test(expected = InvalidSAMLResponseException.class)
	public void testValidate2g() throws InvalidSAMLResponseException
	{
		IdentifierCache cache = createMock(IdentifierCache.class);
		expect(cache.containsIdentifier("5678")).andReturn(true);
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
		SAMLResponseValidatorImpl validator = new SAMLResponseValidatorImpl(cache, 100);
		
		Response response = new Response();
		response.setID("1234");
		
		/* GMT timezone */
		SimpleTimeZone gmt = new SimpleTimeZone(0, "UTC");

		/* GregorianCalendar with the GMT time zone */
		GregorianCalendar calendar = new GregorianCalendar(gmt);
		XMLGregorianCalendar xmlCalendar = new XMLGregorianCalendarImpl(calendar);
		
		response.setIssueInstant(xmlCalendar);
		
		NameIDType issuer = new NameIDType();
		issuer.setValue("testcase");
		response.setIssuer(issuer);
		response.setVersion(VersionConstants.saml20);
		
		Status status = new Status();
		StatusCode code = new StatusCode();
		code.setValue("success");
		status.setStatusCode(code);
		response.setStatus(status);
		response.setInResponseTo("5678");
		
		validator.validate(response);
		verify(cache);
	}
	
	/* Tests to ensure no exceptions with valid request */
	@Test
	public void testValidate3() throws InvalidSAMLResponseException
	{
		IdentifierCache cache = createMock(IdentifierCache.class);
		expect(cache.containsIdentifier("5678")).andReturn(true);
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
		SAMLResponseValidatorImpl validator = new SAMLResponseValidatorImpl(cache, 100);
		
		Response response = new Response();
		response.setID("1234");
		
		/* GMT timezone */
		SimpleTimeZone gmt = new SimpleTimeZone(0, "UTC");

		/* GregorianCalendar with the GMT time zone */
		GregorianCalendar calendar = new GregorianCalendar(gmt);
		XMLGregorianCalendar xmlCalendar = new XMLGregorianCalendarImpl(calendar);
		
		response.setIssueInstant(xmlCalendar);
		
		NameIDType issuer = new NameIDType();
		issuer.setValue("testcase");
		response.setIssuer(issuer);
		response.setVersion(VersionConstants.saml20);
		
		Status status = new Status();
		StatusCode code = new StatusCode();
		code.setValue("success");
		status.setStatusCode(code);
		response.setStatus(status);
		response.setInResponseTo("5678");
		
		validator.validate(response);
		verify(cache);
	}

}

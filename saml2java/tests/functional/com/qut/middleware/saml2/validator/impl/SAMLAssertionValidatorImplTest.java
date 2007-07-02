/* 
 * Copyright 2006, Queensland University of Technology
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy of 
 * the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations under 
 * the License.
 * 
 * Author: Bradley Beddoes
 * Creation Date: 21/03/2007
 * 
 * Purpose: Tests to ensure the SAMLAssertionValidator validates correctly
 */

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
import com.qut.middleware.saml2.exception.InvalidSAMLAssertionException;
import com.qut.middleware.saml2.identifier.IdentifierCache;
import com.qut.middleware.saml2.identifier.exception.IdentifierCollisionException;
import com.qut.middleware.saml2.schemas.assertion.Assertion;
import com.qut.middleware.saml2.schemas.assertion.AudienceRestriction;
import com.qut.middleware.saml2.schemas.assertion.Conditions;
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.assertion.OneTimeUse;
import com.qut.middleware.saml2.schemas.assertion.Subject;
import com.qut.middleware.saml2.schemas.assertion.SubjectConfirmation;
import com.qut.middleware.saml2.schemas.assertion.SubjectConfirmationDataType;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

public class SAMLAssertionValidatorImplTest
{

	@Before
	public void setUp() throws Exception
	{
	}

	@After
	public void tearDown() throws Exception
	{
	}

	/* Ensure exception when null passed for identifier cache */
	@Test(expected = IllegalArgumentException.class)
	public void testSAMLAssertionValidatorImpl1()
	{
		SAMLAssertionValidatorImpl validator = new SAMLAssertionValidatorImpl(null, 100);
	}

	/* Ensure exception when to large value passed for timeout */
	@Test(expected = IllegalArgumentException.class)
	public void testSAMLAssertionValidatorImpl2()
	{
		IdentifierCache cache = createMock(IdentifierCache.class);
		replay(cache);
		SAMLAssertionValidatorImpl validator = new SAMLAssertionValidatorImpl(cache, (Integer.MAX_VALUE / 1000) + 1);
		verify(cache);
	}

	/* Ensure exception when null assertion object passed */
	@Test(expected = IllegalArgumentException.class)
	public void testValidate1()
	{
		IdentifierCache cache = createMock(IdentifierCache.class);
		replay(cache);
		SAMLAssertionValidatorImpl validator = new SAMLAssertionValidatorImpl(cache, 100);
		try
		{
			validator.validate(null);
		}
		catch (InvalidSAMLAssertionException e)
		{
			e.printStackTrace();
			fail("InvalidSAMLAssertionException not expected");
		}
		verify(cache);
	}

	/* Ensure null id throws exception */
	@Test(expected = InvalidSAMLAssertionException.class)
	public void testValidate2() throws InvalidSAMLAssertionException
	{
		IdentifierCache cache = createMock(IdentifierCache.class);
		replay(cache);
		SAMLAssertionValidatorImpl validator = new SAMLAssertionValidatorImpl(cache, 100);

		Assertion assertion = new Assertion();
		assertion.setID(null);
		validator.validate(assertion);

		verify(cache);
	}
	
	/* Ensure invalid SAML version throws exception */
	@Test(expected = InvalidSAMLAssertionException.class)
	public void testValidate2a() throws InvalidSAMLAssertionException
	{
		IdentifierCache cache = createMock(IdentifierCache.class);
		replay(cache);
		SAMLAssertionValidatorImpl validator = new SAMLAssertionValidatorImpl(cache, 100);

		Assertion assertion = new Assertion();
		assertion.setID("1234");
		assertion.setVersion("SAML1");
		validator.validate(assertion);

		verify(cache);
	}
	
	/* Ensure invalid SAML version throws exception */
	@Test(expected = InvalidSAMLAssertionException.class)
	public void testValidate2b() throws InvalidSAMLAssertionException
	{
		IdentifierCache cache = createMock(IdentifierCache.class);
		replay(cache);
		SAMLAssertionValidatorImpl validator = new SAMLAssertionValidatorImpl(cache, 100);

		Assertion assertion = new Assertion();
		assertion.setID("1234");
		assertion.setVersion("SAML1");
		validator.validate(assertion);

		verify(cache);
	}
	
	/* Ensure invalid SAML issuer throws exception */
	@Test(expected = InvalidSAMLAssertionException.class)
	public void testValidate2c() throws InvalidSAMLAssertionException
	{
		IdentifierCache cache = createMock(IdentifierCache.class);
		replay(cache);
		SAMLAssertionValidatorImpl validator = new SAMLAssertionValidatorImpl(cache, 100);

		Assertion assertion = new Assertion();
		assertion.setID("1234");
		assertion.setVersion(VersionConstants.saml20);
		
		NameIDType issuer = new NameIDType();
		issuer.setValue(null);
		assertion.setIssuer(issuer);
		
		validator.validate(assertion);

		verify(cache);
	}
	
	/* Ensure invalid SAML subject NameID throws exception */
	@Test(expected = InvalidSAMLAssertionException.class)
	public void testValidate2d() throws InvalidSAMLAssertionException
	{
		IdentifierCache cache = createMock(IdentifierCache.class);
		replay(cache);
		SAMLAssertionValidatorImpl validator = new SAMLAssertionValidatorImpl(cache, 100);

		Assertion assertion = new Assertion();
		assertion.setID("1234");
		assertion.setVersion(VersionConstants.saml20);
		
		NameIDType issuer = new NameIDType();
		issuer.setValue("com.testcase");
		assertion.setIssuer(issuer);
		
		Subject subject = new Subject();
		subject.setNameID(null);
		assertion.setSubject(subject);
		
		validator.validate(assertion);

		verify(cache);
	}

	/* Ensure invalid SAML subject EncryptedID throws exception */
	@Test(expected = InvalidSAMLAssertionException.class)
	public void testValidate2e() throws InvalidSAMLAssertionException
	{
		IdentifierCache cache = createMock(IdentifierCache.class);
		replay(cache);
		SAMLAssertionValidatorImpl validator = new SAMLAssertionValidatorImpl(cache, 100);

		Assertion assertion = new Assertion();
		assertion.setID("1234");
		assertion.setVersion(VersionConstants.saml20);
		
		NameIDType issuer = new NameIDType();
		issuer.setValue("com.testcase");
		assertion.setIssuer(issuer);
		
		Subject subject = new Subject();
		subject.setEncryptedID(null);
		assertion.setSubject(subject);
		
		assertion.setSubject(subject);
		
		validator.validate(assertion);

		verify(cache);
	}
	
	/* Ensure invalid SAML confirmation data throws exception */
	@Test(expected = InvalidSAMLAssertionException.class)
	public void testValidate2f() throws InvalidSAMLAssertionException
	{
		IdentifierCache cache = createMock(IdentifierCache.class);
		replay(cache);
		SAMLAssertionValidatorImpl validator = new SAMLAssertionValidatorImpl(cache, 100);

		Assertion assertion = new Assertion();
		assertion.setID("1234");
		assertion.setVersion(VersionConstants.saml20);
		
		NameIDType issuer = new NameIDType();
		issuer.setValue("com.testcase");
		assertion.setIssuer(issuer);
		
		Subject subject = new Subject();
		NameIDType subjectName = new NameIDType();
		subjectName.setValue("testcase");
		subject.setNameID(subjectName);
		
		SubjectConfirmation subjectConfirmation = new SubjectConfirmation();
		
		SubjectConfirmationDataType confirmationData = new SubjectConfirmationDataType();
		
		/* GMT timezone */
		SimpleTimeZone gmt = new SimpleTimeZone(0, "UTC");

		/* GregorianCalendar with the GMT time zone */
		GregorianCalendar calendar = new GregorianCalendar(gmt);
		calendar.add(GregorianCalendar.MINUTE, 10);
		XMLGregorianCalendar xmlCalendar = new XMLGregorianCalendarImpl(calendar);
		
		confirmationData.setNotOnOrAfter(xmlCalendar);
		
		subjectConfirmation.setSubjectConfirmationData(confirmationData);
		subject.getSubjectConfirmationNonID().add(subjectConfirmation);
		
		assertion.setSubject(subject);
		
		validator.validate(assertion);

		verify(cache);
	}
	
	/* Ensure invalid SAML confirmation data throws exception */
	@Test(expected = InvalidSAMLAssertionException.class)
	public void testValidate2g() throws InvalidSAMLAssertionException
	{
		IdentifierCache cache = createMock(IdentifierCache.class);
		expect(cache.containsIdentifier("5678")).andReturn(false);
		replay(cache);
		SAMLAssertionValidatorImpl validator = new SAMLAssertionValidatorImpl(cache, 100);

		Assertion assertion = new Assertion();
		assertion.setID("1234");
		assertion.setVersion(VersionConstants.saml20);
		
		NameIDType issuer = new NameIDType();
		issuer.setValue("com.testcase");
		assertion.setIssuer(issuer);
		
		Subject subject = new Subject();
		NameIDType subjectName = new NameIDType();
		subjectName.setValue("testcase");
		subject.setNameID(subjectName);
		
		SubjectConfirmation subjectConfirmation = new SubjectConfirmation();
		
		SubjectConfirmationDataType confirmationData = new SubjectConfirmationDataType();
		
		/* GMT timezone */
		SimpleTimeZone gmt = new SimpleTimeZone(0, "UTC");

		/* GregorianCalendar with the GMT time zone */
		GregorianCalendar calendar = new GregorianCalendar(gmt);
		XMLGregorianCalendar xmlCalendar = new XMLGregorianCalendarImpl(calendar);
		
		confirmationData.setNotOnOrAfter(xmlCalendar);
		confirmationData.setInResponseTo("5678");
		
		subjectConfirmation.setSubjectConfirmationData(confirmationData);
		subject.getSubjectConfirmationNonID().add(subjectConfirmation);
		
		assertion.setSubject(subject);
		
		validator.validate(assertion);

		verify(cache);
	}	
	
	/* Ensure invalid SAML confirmation throws exception */
	@Test(expected = InvalidSAMLAssertionException.class)
	public void testValidate2h() throws InvalidSAMLAssertionException
	{
		IdentifierCache cache = createMock(IdentifierCache.class);
		expect(cache.containsIdentifier("5678")).andReturn(true);
		replay(cache);
		SAMLAssertionValidatorImpl validator = new SAMLAssertionValidatorImpl(cache, 100);

		Assertion assertion = new Assertion();
		assertion.setID("1234");
		assertion.setVersion(VersionConstants.saml20);
		
		NameIDType issuer = new NameIDType();
		issuer.setValue("com.testcase");
		assertion.setIssuer(issuer);
		
		Subject subject = new Subject();
		NameIDType subjectName = new NameIDType();
		subjectName.setValue("testcase");
		subject.setNameID(subjectName);
		
		SubjectConfirmation subjectConfirmation = new SubjectConfirmation();
		
		SubjectConfirmationDataType confirmationData = new SubjectConfirmationDataType();
		
		/* GMT timezone */
		SimpleTimeZone gmt = new SimpleTimeZone(0, "UTC");

		/* GregorianCalendar with the GMT time zone */
		GregorianCalendar calendar = new GregorianCalendar(gmt);
		XMLGregorianCalendar xmlCalendar = new XMLGregorianCalendarImpl(calendar);
		
		confirmationData.setNotOnOrAfter(xmlCalendar);
		confirmationData.setInResponseTo("5678");
		
		subjectConfirmation.setSubjectConfirmationData(confirmationData);
		subject.getSubjectConfirmationNonID().add(subjectConfirmation);
		
		assertion.setSubject(subject);
		assertion.setConditions(null);
		
		validator.validate(assertion);

		verify(cache);
	}
	
	/* Ensure invalid SAML confirmation (missing AudienceRestriction) throws exception */
	@Test(expected = InvalidSAMLAssertionException.class)
	public void testValidate2i() throws InvalidSAMLAssertionException
	{
		IdentifierCache cache = createMock(IdentifierCache.class);
		expect(cache.containsIdentifier("5678")).andReturn(true);
		replay(cache);
		SAMLAssertionValidatorImpl validator = new SAMLAssertionValidatorImpl(cache, 100);

		Assertion assertion = new Assertion();
		assertion.setID("1234");
		assertion.setVersion(VersionConstants.saml20);
		
		NameIDType issuer = new NameIDType();
		issuer.setValue("com.testcase");
		assertion.setIssuer(issuer);
		
		Subject subject = new Subject();
		NameIDType subjectName = new NameIDType();
		subjectName.setValue("testcase");
		subject.setNameID(subjectName);
		
		SubjectConfirmation subjectConfirmation = new SubjectConfirmation();
		
		SubjectConfirmationDataType confirmationData = new SubjectConfirmationDataType();
		
		/* GMT timezone */
		SimpleTimeZone gmt = new SimpleTimeZone(0, "UTC");

		/* GregorianCalendar with the GMT time zone */
		GregorianCalendar calendar = new GregorianCalendar(gmt);
		XMLGregorianCalendar xmlCalendar = new XMLGregorianCalendarImpl(calendar);
		
		confirmationData.setNotOnOrAfter(xmlCalendar);
		confirmationData.setInResponseTo("5678");
		
		subjectConfirmation.setSubjectConfirmationData(confirmationData);
		subject.getSubjectConfirmationNonID().add(subjectConfirmation);
		
		assertion.setSubject(subject);
		OneTimeUse condition = new OneTimeUse();
		Conditions conditions = new Conditions();
		conditions.getConditionsAndOneTimeUsesAndAudienceRestrictions().add(condition);
	
		assertion.setConditions(conditions);
		
		validator.validate(assertion);

		verify(cache);
	}
	
	/* Ensure invalid SAML assertion ID throws exception */
	@Test(expected = InvalidSAMLAssertionException.class)
	public void testValidate2j() throws InvalidSAMLAssertionException
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
		SAMLAssertionValidatorImpl validator = new SAMLAssertionValidatorImpl(cache, 100);

		Assertion assertion = new Assertion();
		assertion.setID("1234");
		assertion.setVersion(VersionConstants.saml20);
		
		NameIDType issuer = new NameIDType();
		issuer.setValue("com.testcase");
		assertion.setIssuer(issuer);
		
		Subject subject = new Subject();
		NameIDType subjectName = new NameIDType();
		subjectName.setValue("testcase");
		subject.setNameID(subjectName);
		
		SubjectConfirmation subjectConfirmation = new SubjectConfirmation();
		
		SubjectConfirmationDataType confirmationData = new SubjectConfirmationDataType();
		
		/* GMT timezone */
		SimpleTimeZone gmt = new SimpleTimeZone(0, "UTC");

		/* GregorianCalendar with the GMT time zone */
		GregorianCalendar calendar = new GregorianCalendar(gmt);
		XMLGregorianCalendar xmlCalendar = new XMLGregorianCalendarImpl(calendar);
		
		confirmationData.setNotOnOrAfter(xmlCalendar);
		confirmationData.setInResponseTo("5678");
		
		subjectConfirmation.setSubjectConfirmationData(confirmationData);
		subject.getSubjectConfirmationNonID().add(subjectConfirmation);
		
		assertion.setSubject(subject);
		AudienceRestriction condition = new AudienceRestriction();
		Conditions conditions = new Conditions();
		conditions.getConditionsAndOneTimeUsesAndAudienceRestrictions().add(condition);
	
		assertion.setConditions(conditions);
		
		validator.validate(assertion);

		verify(cache);
	}
	
	/* Ensure valid assertion throws no exception */
	@Test
	public void testValidate3() throws InvalidSAMLAssertionException
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
		SAMLAssertionValidatorImpl validator = new SAMLAssertionValidatorImpl(cache, 100);

		Assertion assertion = new Assertion();
		assertion.setID("1234");
		assertion.setVersion(VersionConstants.saml20);
		
		NameIDType issuer = new NameIDType();
		issuer.setValue("com.testcase");
		assertion.setIssuer(issuer);
		
		Subject subject = new Subject();
		NameIDType subjectName = new NameIDType();
		subjectName.setValue("testcase");
		subject.setNameID(subjectName);
		
		SubjectConfirmation subjectConfirmation = new SubjectConfirmation();
		
		SubjectConfirmationDataType confirmationData = new SubjectConfirmationDataType();
		
		/* GMT timezone */
		SimpleTimeZone gmt = new SimpleTimeZone(0, "UTC");

		/* GregorianCalendar with the GMT time zone */
		GregorianCalendar calendar = new GregorianCalendar(gmt);
		XMLGregorianCalendar xmlCalendar = new XMLGregorianCalendarImpl(calendar);
		
		confirmationData.setNotOnOrAfter(xmlCalendar);
		confirmationData.setInResponseTo("5678");
		
		subjectConfirmation.setSubjectConfirmationData(confirmationData);
		subject.getSubjectConfirmationNonID().add(subjectConfirmation);
		
		assertion.setSubject(subject);
		AudienceRestriction condition = new AudienceRestriction();
		Conditions conditions = new Conditions();
		conditions.getConditionsAndOneTimeUsesAndAudienceRestrictions().add(condition);
	
		assertion.setConditions(conditions);
		
		validator.validate(assertion);

		verify(cache);
	}
	
}

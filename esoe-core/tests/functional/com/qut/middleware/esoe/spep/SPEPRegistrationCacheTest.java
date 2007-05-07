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
 * Author:
 * Creation Date:
 * 
 * Purpose:
 */
package com.qut.middleware.esoe.spep;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;

import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.esoe.spep.exception.DatabaseFailureException;
import com.qut.middleware.esoe.spep.exception.DatabaseFailureNoSuchSPEPException;
import com.qut.middleware.esoe.spep.exception.InvalidRequestException;
import com.qut.middleware.esoe.spep.exception.SPEPCacheUpdateException;
import com.qut.middleware.esoe.spep.impl.SPEPRegistrationCacheImpl;
import com.qut.middleware.esoe.spep.sqlmap.SPEPRegistrationDao;
import com.qut.middleware.esoe.spep.sqlmap.impl.SPEPRegistrationData;
import com.qut.middleware.esoe.spep.sqlmap.impl.SPEPRegistrationQueryData;
import com.qut.middleware.saml2.VersionConstants;
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.esoe.protocol.ValidateInitializationRequest;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

/**
 * @author Shaun
 *
 * NOTE: the file SqlMapConfig.xml
 */
@SuppressWarnings("nls")
public class SPEPRegistrationCacheTest
{

	private SPEPRegistrationCache spepRegistrationCache;
	private SPEPRegistrationDao sqlConfig;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		// first test invalid constructor param
		try
		{
			//this.sqlConfig = createMock(SPEPRegistrationDao.class);
			this.spepRegistrationCache = new SPEPRegistrationCacheImpl(this.sqlConfig);
		}
		catch(IllegalArgumentException e)
		{
			// good
		}
		catch(Exception e)
		{
			// bad
			e.printStackTrace();
		}
		
		this.sqlConfig = createMock(SPEPRegistrationDao.class);
		this.spepRegistrationCache = new SPEPRegistrationCacheImpl(this.sqlConfig);
	
	}
	
	
	/**
	 * Test method for {@link com.qut.middleware.esoe.spep.SPEPRegistrationCache#registerSPEP(com.qut.middleware.saml2.schemas.esoe.protocol.ValidateInitializationRequest)}.
	 *
	 * Test registration behaviour for a registration request for an spep that DOES exist in
	 * mocked datasource and does already contain rego data. The registration process should succeed
	 * and update the record.
	 */
	@Test 
	public void testRegisterSPEP1() throws InvalidRequestException, DatabaseFailureNoSuchSPEPException, DatabaseFailureException, SPEPCacheUpdateException
	{
		// setup a data type to be returned by mocked object
		SPEPRegistrationData data = new SPEPRegistrationData();
		data.setIpAddress("1.1.1.1");
		data.setCompileDate("1/1/2");
		data.setCompileSystem("My OS");
		data.setEnvironment("TEST");
		data.setVersion("1.0");		
		
		expect(this.sqlConfig.querySPEPExists((SPEPRegistrationQueryData)notNull())).andReturn(new Integer(1)).atLeastOnce();
		expect(this.sqlConfig.getSPEPRegistration((SPEPRegistrationQueryData)notNull())).andReturn(data).once();

		// because the record exists, we expect the process will update the record
		this.sqlConfig.updateSPEPRegistration((SPEPRegistrationData)notNull());
		expectLastCall().once();
		
		replay(this.sqlConfig);		
		
		ValidateInitializationRequest request = new ValidateInitializationRequest();
		int authzCacheIndex = 0;
		String date = "date";
		String system = "system";
		String environment = "environment";
		String version = "version";
		String issuerNameID = "nameID";
		String ipAddress = "ipaddr.xxx";

		request.setID("spep.test.url");
		request.getIpAddress().add(ipAddress);
		request.setAuthzCacheIndex(authzCacheIndex);
		request.setCompileDate(date);
		request.setCompileSystem(system);
		request.setEnvironment(environment);
		request.setSwVersion(version);
		request.setVersion(VersionConstants.saml20);
		NameIDType issuer = new NameIDType();
		issuer.setValue(issuerNameID);
		request.setIssuer(issuer);
		request.setNodeId("001");
		request.setIssueInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));
		
		this.spepRegistrationCache.registerSPEP(request);
		
	}
	
	/**
	 * Test method for {@link com.qut.middleware.esoe.spep.SPEPRegistrationCache#registerSPEP(com.qut.middleware.saml2.schemas.esoe.protocol.ValidateInitializationRequest)}.
	 *
	 * Test registration behaviour for a registration request for an spep that DOES exist in
	 * mocked datasource and does NOT contain rego data. The registration process should succeed
	 * and insert the new record.
	 */
	@Test 
	public void testRegisterSPEP2() throws InvalidRequestException, DatabaseFailureNoSuchSPEPException, DatabaseFailureException, SPEPCacheUpdateException
	{		
		expect(this.sqlConfig.querySPEPExists((SPEPRegistrationQueryData)notNull())).andReturn(new Integer(1)).atLeastOnce();
		expect(this.sqlConfig.getSPEPRegistration((SPEPRegistrationQueryData)notNull())).andReturn(null).once();

		// because the record does not exist, we expect the process will insert the record
		this.sqlConfig.insertSPEPRegistration((SPEPRegistrationData)notNull());
		expectLastCall().once();
		
		replay(this.sqlConfig);		
		
		ValidateInitializationRequest request = new ValidateInitializationRequest();
		int authzCacheIndex = 0;
		String date = "date";
		String system = "system";
		String environment = "environment";
		String version = "version";
		String issuerNameID = "nameID";
		String ipAddress = "ipaddr.xxx";

		request.setID("spep.test.url");
		request.getIpAddress().add(ipAddress);
		request.setAuthzCacheIndex(authzCacheIndex);
		request.setCompileDate(date);
		request.setCompileSystem(system);
		request.setEnvironment(environment);
		request.setSwVersion(version);
		request.setVersion(VersionConstants.saml20);
		NameIDType issuer = new NameIDType();
		issuer.setValue(issuerNameID);
		request.setIssuer(issuer);
		request.setNodeId("001");
		request.setIssueInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));
		
		this.spepRegistrationCache.registerSPEP(request);
		
	}
	
	
	/**
	 * Test method for {@link com.qut.middleware.esoe.spep.SPEPRegistrationCache#registerSPEP(com.qut.middleware.saml2.schemas.esoe.protocol.ValidateInitializationRequest)}.
	 *
	 * Test registration behaviour for a registration request for an spep that doesnt exist in
	 * mocked datasource. The registration process should throw an exception.
	 */
	@Test (expected = DatabaseFailureNoSuchSPEPException.class)
	public void testRegisterSPEP3() throws InvalidRequestException, DatabaseFailureNoSuchSPEPException, DatabaseFailureException, SPEPCacheUpdateException
	{
			//expect(this.sqlConfig.getSPEPRegistration((SPEPRegistrationQueryData)notNull())).andReturn(arg0);
		expect(this.sqlConfig.querySPEPExists((SPEPRegistrationQueryData)notNull())).andReturn(new Integer(0)).atLeastOnce();
		replay(this.sqlConfig);
		
		
		ValidateInitializationRequest request = new ValidateInitializationRequest();
		int authzCacheIndex = 0;
		String date = "date";
		String system = "system";
		String environment = "environment";
		String version = "version";
		String issuerNameID = "nameID";
		String ipAddress = "ipaddr.xxx";

		request.setID("spep.test.url");
		request.getIpAddress().add(ipAddress);
		request.setAuthzCacheIndex(authzCacheIndex);
		request.setCompileDate(date);
		request.setCompileSystem(system);
		request.setEnvironment(environment);
		request.setSwVersion(version);
		request.setVersion(VersionConstants.saml20);
		NameIDType issuer = new NameIDType();
		issuer.setValue(issuerNameID);
		request.setIssuer(issuer);
		request.setNodeId("001");
		request.setIssueInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));
		
		this.spepRegistrationCache.registerSPEP(request);
		
	}

	
	/**
	 * Test method for {@link com.qut.middleware.esoe.spep.SPEPRegistrationCache#registerSPEP(com.qut.middleware.saml2.schemas.esoe.protocol.ValidateInitializationRequest)}.
	 *
	 * Test registration behaviour for a registration request that is missing required information.
	 */
	@Test (expected = InvalidRequestException.class)
	public void testRegisterSPEP4() throws Exception
	{
		expect(this.sqlConfig.querySPEPExists((SPEPRegistrationQueryData)notNull())).andReturn(new Integer(1)).atLeastOnce();
		replay(this.sqlConfig);
				
		ValidateInitializationRequest request = new ValidateInitializationRequest();
		int authzCacheIndex = 0;
		String date = "date";
		String system = "system";
		String environment = "environment";
		String version = "version";
		String issuerNameID = "nameID";
		String ipAddress = "ipaddr.xxx";

		request.setID("spep.test.url");
		//request.getIpAddress().add(ipAddress);
		request.setAuthzCacheIndex(authzCacheIndex);
		request.setCompileDate(date);
		request.setCompileSystem(system);
		request.setEnvironment(environment);
		request.setSwVersion(version);
		request.setVersion(VersionConstants.saml20);
		NameIDType issuer = new NameIDType();
		issuer.setValue(issuerNameID);
		request.setIssuer(issuer);
		request.setNodeId("001");
		request.setIssueInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));
		
		this.spepRegistrationCache.registerSPEP(request);
		
	}
	
	/**
	 * Test method for {@link com.qut.middleware.esoe.spep.SPEPRegistrationCache#registerSPEP(com.qut.middleware.saml2.schemas.esoe.protocol.ValidateInitializationRequest)}.
	 *
	 * Test registration behaviour for a registration request that is missing required information.
	 */
	@Test (expected = InvalidRequestException.class)
	public void testRegisterSPEP5() throws Exception
	{
		expect(this.sqlConfig.querySPEPExists((SPEPRegistrationQueryData)notNull())).andReturn(new Integer(1)).atLeastOnce();
		replay(this.sqlConfig);
				
		ValidateInitializationRequest request = new ValidateInitializationRequest();
		int authzCacheIndex = 0;
		String date = "date";
		String system = "system";
		String environment = "environment";
		String version = "version";
		String issuerNameID = "nameID";
		String ipAddress = "ipaddr.xxx";

		request.setID("spep.test.url");
		request.getIpAddress().add(ipAddress);
		request.setAuthzCacheIndex(authzCacheIndex);
		//request.setCompileDate(date);
		request.setCompileSystem(system);
		request.setEnvironment(environment);
		request.setSwVersion(version);
		request.setVersion(VersionConstants.saml20);
		NameIDType issuer = new NameIDType();
		issuer.setValue(issuerNameID);
		request.setIssuer(issuer);
		request.setNodeId("001");
		request.setIssueInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));
		
		this.spepRegistrationCache.registerSPEP(request);
		
	}
	
	/**
	 * Test method for {@link com.qut.middleware.esoe.spep.SPEPRegistrationCache#registerSPEP(com.qut.middleware.saml2.schemas.esoe.protocol.ValidateInitializationRequest)}.
	 *
	 * Test registration behaviour for a registration request that is missing required information.
	 */
	@Test (expected = InvalidRequestException.class)
	public void testRegisterSPEP6() throws Exception
	{
		expect(this.sqlConfig.querySPEPExists((SPEPRegistrationQueryData)notNull())).andReturn(new Integer(1)).atLeastOnce();
		replay(this.sqlConfig);
				
		ValidateInitializationRequest request = new ValidateInitializationRequest();
		int authzCacheIndex = 0;
		String date = "date";
		String system = "system";
		String environment = "environment";
		String version = "version";
		String issuerNameID = "nameID";
		String ipAddress = "ipaddr.xxx";

		request.setID("spep.test.url");
		request.getIpAddress().add(ipAddress);
		request.setCompileDate(date);
	//	request.setCompileSystem(system);
		request.setEnvironment(environment);
		request.setSwVersion(version);
		request.setVersion(VersionConstants.saml20);
		NameIDType issuer = new NameIDType();
		issuer.setValue(issuerNameID);
		request.setIssuer(issuer);
		request.setNodeId("001");
		request.setIssueInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));
		
		this.spepRegistrationCache.registerSPEP(request);
		
	}
	
	/**
	 * Test method for {@link com.qut.middleware.esoe.spep.SPEPRegistrationCache#registerSPEP(com.qut.middleware.saml2.schemas.esoe.protocol.ValidateInitializationRequest)}.
	 *
	 * Test registration behaviour for a registration request that is missing required information.
	 */
	@Test (expected = InvalidRequestException.class)
	public void testRegisterSPEP8() throws Exception
	{
		expect(this.sqlConfig.querySPEPExists((SPEPRegistrationQueryData)notNull())).andReturn(new Integer(1)).atLeastOnce();
		replay(this.sqlConfig);
				
		ValidateInitializationRequest request = new ValidateInitializationRequest();
		int authzCacheIndex = 0;
		String date = "date";
		String system = "system";
		String version = "version";		
		String environment = "environment";
		String issuerNameID = "nameID";
		String ipAddress = "ipaddr.xxx";

		request.setID("spep.test.url");
		request.getIpAddress().add(ipAddress);
		request.setAuthzCacheIndex(authzCacheIndex);
		request.setCompileDate(date);
		request.setCompileSystem(system);
		request.setEnvironment(environment);
		request.setSwVersion(version);
		request.setVersion(VersionConstants.saml20);
		NameIDType issuer = new NameIDType();
		//issuer.setValue(issuerNameID);
		request.setIssuer(issuer);
		request.setNodeId("001");
		request.setIssueInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));
		
		this.spepRegistrationCache.registerSPEP(request);
		
	}
	
	/**
	 * Test method for {@link com.qut.middleware.esoe.spep.SPEPRegistrationCache#registerSPEP(com.qut.middleware.saml2.schemas.esoe.protocol.ValidateInitializationRequest)}.
	 *
	 * Test registration behaviour for a registration request that is missing required information.
	 */
	@Test (expected = InvalidRequestException.class)
	public void testRegisterSPEP7() throws Exception
	{
		expect(this.sqlConfig.querySPEPExists((SPEPRegistrationQueryData)notNull())).andReturn(new Integer(1)).atLeastOnce();
		replay(this.sqlConfig);
				
		ValidateInitializationRequest request = new ValidateInitializationRequest();
		int authzCacheIndex = 0;
		String date = "date";
		String system = "system";
		String environment = "environment";
		String issuerNameID = "nameID";
		String ipAddress = "ipaddr.xxx";

		request.setID("spep.test.url");
		request.getIpAddress().add(ipAddress);
		request.setAuthzCacheIndex(authzCacheIndex);
		request.setCompileDate(date);
		request.setCompileSystem(system);
		request.setEnvironment(environment);
	//	request.setSwVersion(version);
		request.setVersion(VersionConstants.saml20);
		NameIDType issuer = new NameIDType();
		issuer.setValue(issuerNameID);
		request.setIssuer(issuer);
		request.setNodeId("001");
		request.setIssueInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));
		
		this.spepRegistrationCache.registerSPEP(request);
		
	}
	
	
	/**
	 * Test method for {@link com.qut.middleware.esoe.spep.SPEPRegistrationCache#registerSPEP(com.qut.middleware.saml2.schemas.esoe.protocol.ValidateInitializationRequest)}.
	 *
	 * Test registration behaviour for a registration request that is missing required information.
	 */
	@Test (expected = InvalidRequestException.class)
	public void testRegisterSPEP9() throws Exception
	{
		expect(this.sqlConfig.querySPEPExists((SPEPRegistrationQueryData)notNull())).andReturn(new Integer(1)).atLeastOnce();
		replay(this.sqlConfig);
				
		ValidateInitializationRequest request = new ValidateInitializationRequest();
		int authzCacheIndex = 0;
		String date = "date";
		String system = "system";
		String version = "version";
		String environment = "environment";
		String issuerNameID = "nameID";
		String ipAddress = "ipaddr.xxx";

		request.setID("spep.test.url");
		request.getIpAddress().add(ipAddress);
		request.setAuthzCacheIndex(authzCacheIndex);
		request.setCompileDate(date);
		request.setCompileSystem(system);
		request.setEnvironment(environment);
		request.setSwVersion(version);
		request.setVersion(VersionConstants.saml20);
		NameIDType issuer = new NameIDType();
		issuer.setValue(issuerNameID);
		request.setIssuer(issuer);
	//	request.setNodeId("001");
		request.setIssueInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));
		
		this.spepRegistrationCache.registerSPEP(request);
		
	}
}


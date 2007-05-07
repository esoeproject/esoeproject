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
package com.qut.middleware.esoe.authn.pipeline.authenticator;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.esoe.authn.pipeline.Authenticator;

@SuppressWarnings(value={"unqualified-field-access", "nls"})
public class LdapBasicAuthenticatorTest
{
	private LdapBasicAuthenticator authenticator;

	private String LDAP_SERVER = "auth-ldap.qut.edu.au"; 
	private int LDAP_SERVER_CLEAR_PORT = 389;
	private int LDAP_SERVER_SSL_PORT = 636;
	private String BASE_DN = "ou=people,dc=qut,dc=edu,dc=au"; 
	private String IDENTIFIER = "uid"; 

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		/* TODO: Need to test recursive authn */
		this.authenticator = new LdapBasicAuthenticator(this.LDAP_SERVER, this.LDAP_SERVER_CLEAR_PORT, this.BASE_DN, this.IDENTIFIER, false, false);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception
	{
		// Not Implemented
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.pipeline.authenticator.LdapBasicAuthenticator#authenticate(java.lang.String, java.lang.String)}.
	 */
	@Test (timeout=3000)
	public void testAuthenticateValid()
	{
		Authenticator.result result;
		this.authenticator.disableSSL();
		
		// use the credentials of a user on the target server
		result = this.authenticator.authenticate("accesstest", "testing123");  
		
		assertEquals("Result should be successful authentication", Authenticator.result.Successful, result); 
	}
	
	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.pipeline.authenticator.LdapBasicAuthenticator#authenticate(java.lang.String, java.lang.String)}.
	 */
	@Test (timeout=5000)
//	@Ignore
	public void testAuthenticateValidSSL()
	{
		this.authenticator = new LdapBasicAuthenticator(this.LDAP_SERVER, this.LDAP_SERVER_SSL_PORT, this.BASE_DN, this.IDENTIFIER, false, false);

		Authenticator.result result;

		// use the credentials of a user on the target server
		result = this.authenticator.authenticate("accesstest", "testing123");  
		
		assertEquals("Result should be successful authentication", Authenticator.result.Successful, result); 
	}
	
	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.pipeline.authenticator.LdapBasicAuthenticator#authenticate(java.lang.String, java.lang.String)}.
	 */
	@Test (timeout=3000)
	public void testAuthenticateInValid()
	{
		Authenticator.result result;
		this.authenticator.disableSSL();
		
		// use the credentials of a user on the target server (incorrect password to fail auth)
		result = this.authenticator.authenticate("accesstest", "testing12343532");  
		
		assertEquals("Result should be failed authentication", Authenticator.result.Failure, result);
	}
	
	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.pipeline.authenticator.LdapBasicAuthenticator#authenticate(java.lang.String, java.lang.String)}.
	 *
	 * Test the recursive searching feature od internal dtermineDN function in authenticator. This test 
	 * will search for a valid existing user in the directory.
	 */
	@Test //(timeout=5000)
	public void testAuthenticateRecursive1()
	{
		// test admin bind version of authenticator. Turning recursive searches on will require this.
		String adminuser = "cn=admin,ou=admins,o=qut";
		String adminpass = "data0tab";
		
		this.authenticator = new LdapBasicAuthenticator(this.LDAP_SERVER, this.LDAP_SERVER_SSL_PORT, this.BASE_DN, this.IDENTIFIER, true, false, adminuser, adminpass);
		
		// test to make sure its set up correctly
		assertEquals(true, this.authenticator.isRecursive());		
		assertEquals(this.BASE_DN, this.authenticator.getBaseDN());
		assertEquals(this.IDENTIFIER, this.authenticator.getIdentifier());
		assertEquals(this.LDAP_SERVER_SSL_PORT, this.authenticator.getLdapServerPort());		
		assertEquals(this.LDAP_SERVER, this.authenticator.getLdapServer());
		
		Authenticator.result result;
		
		// use the credentials of a user on the target server
		result = this.authenticator.authenticate("accesstest", "testing123");  
		
		assertEquals("Result should be successful authentication", Authenticator.result.Successful, result); 
	}
	
	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.pipeline.authenticator.LdapBasicAuthenticator#authenticate(java.lang.String, java.lang.String)}.
	 *
	 * Test the recursive searching feature od internal dtermineDN function in authenticator. This test 
	 * will search for a NON existing user in the directory.
	 */
	@Test 
	public void testAuthenticateRecursive2()
	{
		// test admin bind version of authenticator. Turning recursive searches on will require this.
		String adminuser = "cn=admin,ou=admins,o=qut";
		String adminpass = "data0tab";
		
		this.authenticator = new LdapBasicAuthenticator(this.LDAP_SERVER, this.LDAP_SERVER_SSL_PORT, this.BASE_DN, this.IDENTIFIER, true, false, adminuser, adminpass);
		
		// test to make sure its set up correctly
		assertEquals(true, this.authenticator.isRecursive());	
		assertEquals(this.BASE_DN, this.authenticator.getBaseDN());
		assertEquals(this.IDENTIFIER, this.authenticator.getIdentifier());
		assertEquals(this.LDAP_SERVER_SSL_PORT, this.authenticator.getLdapServerPort());		
		assertEquals(this.LDAP_SERVER, this.authenticator.getLdapServer());
		
		Authenticator.result result;
		
		// use the credentials of a user on the target server
		result = this.authenticator.authenticate("accesstestnotexists", "testing123");  
		
		assertEquals("Result should be Unsuccessful authentication", Authenticator.result.Failure, result); 
	}
	
	
	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.pipeline.authenticator.LdapBasicAuthenticator#authenticate(java.lang.String, java.lang.String)}.
	 *
	 * Test the recursive searching feature od internal dtermineDN function in authenticator. Test for
	 * invalid bind redentials.
	 */
	@Test 
	public void testAuthenticateRecursive3()
	{
		// test admin bind version of authenticator. Turning recursive searches on will require this.
		String adminuser = "cn=admin,ou=admins,o=qut";
		String adminpass = "incorrectpassword";
		
		this.authenticator = new LdapBasicAuthenticator(this.LDAP_SERVER, this.LDAP_SERVER_SSL_PORT, this.BASE_DN, this.IDENTIFIER, true, false, adminuser, adminpass);
		
		// test to make sure its set up correctly
		assertEquals(true, this.authenticator.isRecursive());	
		assertEquals(this.BASE_DN, this.authenticator.getBaseDN());
		assertEquals(this.IDENTIFIER, this.authenticator.getIdentifier());
		assertEquals(this.LDAP_SERVER_SSL_PORT, this.authenticator.getLdapServerPort());		
		assertEquals(this.LDAP_SERVER, this.authenticator.getLdapServer());
		
		Authenticator.result result;
		
		// use the credentials of a user on the target server
		result = this.authenticator.authenticate("accesstestnotexists", "testing123");  
		
		assertEquals("Result should be Unsuccessful authentication", Authenticator.result.Failure, result); 
	}
	
	
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction1()
	{
		this.authenticator = new LdapBasicAuthenticator(null, this.LDAP_SERVER_SSL_PORT, this.BASE_DN, this.IDENTIFIER, true, false, "a", "b");
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction2()
	{
		this.authenticator = new LdapBasicAuthenticator(this.LDAP_SERVER, -1, this.BASE_DN, this.IDENTIFIER, true, false, "a", "b");
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction3()
	{
		this.authenticator = new LdapBasicAuthenticator(this.LDAP_SERVER, this.LDAP_SERVER_SSL_PORT, null, this.IDENTIFIER, true, false, "a", "b");
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction4()
	{
		this.authenticator = new LdapBasicAuthenticator(this.LDAP_SERVER, this.LDAP_SERVER_SSL_PORT, this.BASE_DN, null, true, false, "a", "b");
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction5()
	{
		this.authenticator = new LdapBasicAuthenticator(this.LDAP_SERVER, this.LDAP_SERVER_SSL_PORT, this.BASE_DN, this.IDENTIFIER, true, false, null, "b");
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction6()
	{
		this.authenticator = new LdapBasicAuthenticator(this.LDAP_SERVER, this.LDAP_SERVER_SSL_PORT, this.BASE_DN, this.IDENTIFIER, true, false, "a", null);
	}
}

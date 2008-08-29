package com.qut.middleware.esoe.authn.plugins.usernamepassword;


import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.esoe.authn.pipeline.Authenticator;
import com.qut.middleware.esoe.authn.plugins.usernamepassword.authenticator.LdapBasicAuthenticator;

@SuppressWarnings(value={"unqualified-field-access", "nls"})
public class LdapBasicAuthenticatorTest
{
	private LdapBasicAuthenticator authenticator;

	private Properties props;
	private String LDAP_SERVER; 
	private int LDAP_SERVER_CLEAR_PORT = 389;
	private int LDAP_SERVER_SSL_PORT = 636;
	private String BASE_DN; 
	private String IDENTIFIER = "uid"; 
	private String LDAP_USER;
	private String LDAP_USER_PASSWORD;
	private String LDAP_ADMIN_USER;
	private String LDAP_ADMIN_USER_PASSWORD;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		props = new Properties();
		FileInputStream reader = new FileInputStream(new File("tests/testdata/functional.properties"));
		props.load(reader);
		
		this.LDAP_SERVER = props.getProperty("ldapServer");
		this.BASE_DN = props.getProperty("ldapBaseDN");
		this.LDAP_USER = props.getProperty("ldapUser");
		this.LDAP_USER_PASSWORD = props.getProperty("ldapUserPass");
		this.LDAP_ADMIN_USER = props.getProperty("ldapAdminUser");
		this.LDAP_ADMIN_USER_PASSWORD = props.getProperty("ldapAdminUserPass");
		
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
	 * {@link com.qut.middleware.esoe.authn.plugins.usernamepassword.authenticator.LdapBasicAuthenticator#authenticate(java.lang.String, java.lang.String)}.
	 */
	@Test 
	public void testAuthenticateValid()
	{
		Authenticator.result result;
		this.authenticator.disableSSL();
		
		// use the credentials of a user on the target server
		result = this.authenticator.authenticate(LDAP_USER, LDAP_USER_PASSWORD);  
		
		assertEquals("Result should be successful authentication", Authenticator.result.Successful, result); 
	}
	
	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.plugins.usernamepassword.authenticator.LdapBasicAuthenticator#authenticate(java.lang.String, java.lang.String)}.
	 */
	@Test 
	public void testAuthenticateValidSSL()
	{
		this.authenticator = new LdapBasicAuthenticator(this.LDAP_SERVER, this.LDAP_SERVER_SSL_PORT, this.BASE_DN, this.IDENTIFIER, false, false);

		Authenticator.result result;

		// use the credentials of a user on the target server
		result = this.authenticator.authenticate(LDAP_USER, LDAP_USER_PASSWORD);  
		
		assertEquals("Result should be successful authentication", Authenticator.result.Successful, result); 
	}
	
	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.plugins.usernamepassword.authenticator.LdapBasicAuthenticator#authenticate(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testAuthenticateInValid()
	{
		Authenticator.result result;
		this.authenticator.disableSSL();
		
		// use the credentials of a user on the target server (incorrect password to fail auth)
		result = this.authenticator.authenticate(LDAP_USER, "testing12343532");  
		
		assertEquals("Result should be failed authentication", Authenticator.result.Failure, result);
	}
	
	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.plugins.usernamepassword.authenticator.LdapBasicAuthenticator#authenticate(java.lang.String, java.lang.String)}.
	 *
	 * Test the recursive searching feature od internal dtermineDN function in authenticator. This test 
	 * will search for a valid existing user in the directory.
	 */
	@Test 
	public void testAuthenticateRecursive1()
	{	
		this.authenticator = new LdapBasicAuthenticator(this.LDAP_SERVER, this.LDAP_SERVER_CLEAR_PORT, this.BASE_DN, this.IDENTIFIER, true, true, LDAP_ADMIN_USER, LDAP_ADMIN_USER_PASSWORD);
		
		// test to make sure its set up correctly
		assertEquals(true, this.authenticator.isRecursive());		
		assertEquals(this.BASE_DN, this.authenticator.getBaseDN());
		assertEquals(this.IDENTIFIER, this.authenticator.getIdentifier());
		assertEquals(this.LDAP_SERVER_CLEAR_PORT, this.authenticator.getLdapServerPort());		
		assertEquals(this.LDAP_SERVER, this.authenticator.getLdapServer());
		
		Authenticator.result result;
		
		// use the credentials of a user on the target server
		result = this.authenticator.authenticate(LDAP_USER, LDAP_USER_PASSWORD);  
		
		assertEquals("Result should be successful authentication", Authenticator.result.Successful, result); 
	}
	
	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.plugins.usernamepassword.authenticator.LdapBasicAuthenticator#authenticate(java.lang.String, java.lang.String)}.
	 *
	 * Test the recursive searching feature od internal dtermineDN function in authenticator. This test 
	 * will search for a NON existing user in the directory.
	 */
	@Test 
	public void testAuthenticateRecursive2()
	{		
		this.authenticator = new LdapBasicAuthenticator(this.LDAP_SERVER, this.LDAP_SERVER_CLEAR_PORT, this.BASE_DN, this.IDENTIFIER, true, true, LDAP_ADMIN_USER, LDAP_ADMIN_USER_PASSWORD);
		
		// test to make sure its set up correctly
		assertEquals(true, this.authenticator.isRecursive());	
		assertEquals(this.BASE_DN, this.authenticator.getBaseDN());
		assertEquals(this.IDENTIFIER, this.authenticator.getIdentifier());
		assertEquals(this.LDAP_SERVER_CLEAR_PORT, this.authenticator.getLdapServerPort());		
		assertEquals(this.LDAP_SERVER, this.authenticator.getLdapServer());
		
		Authenticator.result result;
		
		// use the credentials of a user on the target server
		result = this.authenticator.authenticate(LDAP_USER + "_nonexist", LDAP_USER_PASSWORD);  
		
		assertEquals("Result should be Unsuccessful authentication", Authenticator.result.Failure, result); 
	}
	
	
	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.plugins.usernamepassword.authenticator.LdapBasicAuthenticator#authenticate(java.lang.String, java.lang.String)}.
	 *
	 * Test the recursive searching feature od internal dtermineDN function in authenticator. Test for
	 * invalid bind redentials.
	 */
	@Test 
	public void testAuthenticateRecursive3()
	{	
		this.authenticator = new LdapBasicAuthenticator(this.LDAP_SERVER, this.LDAP_SERVER_CLEAR_PORT, this.BASE_DN, this.IDENTIFIER, true, true, LDAP_ADMIN_USER, LDAP_ADMIN_USER_PASSWORD);
		
		// test to make sure its set up correctly
		assertEquals(true, this.authenticator.isRecursive());	
		assertEquals(this.BASE_DN, this.authenticator.getBaseDN());
		assertEquals(this.IDENTIFIER, this.authenticator.getIdentifier());
		assertEquals(this.LDAP_SERVER_CLEAR_PORT, this.authenticator.getLdapServerPort());		
		assertEquals(this.LDAP_SERVER, this.authenticator.getLdapServer());
		
		Authenticator.result result;
		
		// use the credentials of a user on the target server
		result = this.authenticator.authenticate(LDAP_USER + "_nonexist", LDAP_USER_PASSWORD);  
		
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

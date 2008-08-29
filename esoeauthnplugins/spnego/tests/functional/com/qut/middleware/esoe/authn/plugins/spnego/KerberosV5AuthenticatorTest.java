package com.qut.middleware.esoe.authn.plugins.spnego;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;

import org.apache.commons.codec.binary.Base64;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.esoe.authn.pipeline.Authenticator;
import com.qut.middleware.esoe.authn.plugins.spnego.authenticator.KerberosV5Authenticator;
import com.qut.middleware.esoe.authn.plugins.spnego.authenticator.KerberosV5Configuration;
@SuppressWarnings({"nls", "synthetic-access"})
public class KerberosV5AuthenticatorTest {

	private KerberosV5Authenticator authenticator;
	private final String SPNEGO_OID = "1.3.6.1.5.5.2"; // "1.2.840.113554.1.2.2" = kerberos V5
	private String testdataPath;
	
	@Before
	public void setUp() throws Exception
	{
		this.testdataPath = System.getProperty("user.dir") + File.separator +"tests" + File.separator+ "testdata"+  File.separator ;
		String kerberosFile = this.testdataPath + "krb5.conf";
		
		//this.authenticator = new KerberosV5Authenticator(loginFile, kerberosFile);
		
		this.authenticator = new KerberosV5Authenticator(this.getServerLoginConfiguration());
		
		// For unit testing we have to set this system property. During runtime the java
		// security libs will look for the file /META-INF/krb5.conf 
		System.setProperty("java.security.krb5.conf", kerberosFile);
		
	}

	
	/** Test a valid authentication request. the set serverPrincipalName must exist in 
	 * the configured kerberos database for the test to succeed. Note .. a TGT ticket cache must 
	 * be used for user authentication as there is no console input for the tests.
	 *
	 */
	@Test
	public void testAuthenticate1()
	{
		//String spnegoToken = "";
		byte[] spnegoToken = new byte[0];
		LoginContext context = null;
		
		try
		{			
			// customized login object
			Configuration config = this.getClientLoginConfiguration();
			
			context = new LoginContext("clientAuth", null, null, config);
			
			context.login();			
		
			Subject subject = context.getSubject();
			//System.out.println("Authenticated CLIENT principal: " + subject.getPrincipals());
					
			// perform kerberos context init
			ClientAuthenticationAction action = new ClientAuthenticationAction();
			action.setPrincipalName("HTTPS@esoe-dev.qut.edu.au:8443");
			
			spnegoToken = (byte[])Subject.doAs(subject, action);
			
			//System.out.println("Client generated SPNEGO Token: " + getHexBytes(spnegoToken, 0, spnegoToken.length));
			
		}
		catch(Exception e)
		{
			//e.printStackTrace();
			fail("Unable to create SPNEGO Token.");
		}
		
		String result = this.authenticator.authenticate(new String(Base64.encodeBase64(spnegoToken)));
		
		assertEquals("Unexpected return code recieved. " , "beddoes@ADTST.QUT.EDU.AU", result);
	}

	
	/** Test an invalid authentication request. The request is for a servicePrincipal that does
	 * not match the target. Authenticator Should return auth failed because the checksum of the
	 * GSS token will fail. 
	 *
	 */
	@Test
	public void testAuthenticate2()
	{
		//String spnegoToken = "";
		byte[] spnegoToken = new byte[0];
		
		try
		{		
			LoginContext context = null;
			
			//	customized login object
			Configuration config = this.getClientLoginConfiguration();
			
			context = new LoginContext("clientAuth", null, null, config);

			// Perform client authentication
			context.login();
			
			Subject subject = context.getSubject();
			//System.out.println("Authenticated CLIENT principal: " + subject.getPrincipals());
					
			// perform kerberos context init
			ClientAuthenticationAction action = new ClientAuthenticationAction();
			
			// For this test to succeed, this must be set to a valid SPN in the
			// configured kerberos DB, but it MUST be different to the target service
			// IE: the server configured in krb5-login.conf
			action.setPrincipalName("HOST@xecute.adtst.qut.edu.au");
			
			spnegoToken = (byte[])Subject.doAs(subject, action);
			
			//System.out.println("Client generated SPNEGO Token: " + getHexBytes(spnegoToken, 0, spnegoToken.length));
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail("Unable to create SPNEGO Token. ");
		}
		
		String result = this.authenticator.authenticate(new String(Base64.encodeBase64(spnegoToken)));
		
		assertEquals("Unexpected return code recieved. " , null, result);
	}
	
		
	private final String getHexBytes(byte[] bytes, int pos, int len) {

		StringBuffer sb = new StringBuffer();
		for (int i = pos; i < (pos + len); i++) {

			int b1 = (bytes[i] >> 4) & 0x0f;
			int b2 = bytes[i] & 0x0f;

			sb.append(Integer.toHexString(b1));
			sb.append(Integer.toHexString(b2));
			sb.append(' ');
		}
		return sb.toString();
	}	
	
	
	
	/****************************************************************************************/
	/** Class to perform the actual authentication logic. Passed to login method.
	 * 
	 ****************************************************************************************/
	private class ClientAuthenticationAction implements PrivilegedExceptionAction
	{
		private String principalName;
		
		public Object run() throws Exception 
		{			
				return this.generateSPNEGOToken(this.principalName);				
		}
		

		/* Generates the SPNEGO token. This is effectively the same token that would be generated
		 * by a web browser and placed into the Authorization header for a HTTP Negotiate
		 * challenge during SPNEGO enabled SSO to a web server. In order to achieve this we must
		 * obtain the TGT from the KDC and establish a GSS context, then we can convert the context
		 * data to the spnego token. 
		 * 
		 * @param serverPrincipalName The name of the KERBEROS service that this client will
		 * connect to. The service name must match the the requested service, else authentication
		 * willfail. EG connecting with a service name of HTTP/esoe@QUT.EDU.AU@QUT.EDU.AU, the
		 * endpouint MUST be esoe.qut.edu.au AND it must have the above SPN registered in the 
		 * Kerberos TGS.
		 */
		private byte[] generateSPNEGOToken(String serverPrincipalName) throws Exception
		{
			    /*
			     * This Oid is used to represent the SPNEGO GSS-API
			     * mechanism. It is defined in RFC 2478. We will use this Oid
			     * whenever we need to indicate to the GSS-API that it must
			     * use SPNEGO for some purpose.
			     */
			    Oid spnegoOid = new Oid(SPNEGO_OID);

			    GSSManager manager = GSSManager.getInstance();

			    //System.out.println("Obtaining TGT for server principal: " + this.principalName);
			    /*
			     * Create a GSSName out of the server's name. 
			     */
			    GSSName serverName = manager.createName(serverPrincipalName, GSSName.NT_HOSTBASED_SERVICE, spnegoOid);

			    /*
			     * Create a GSSContext for mutual authentication with the
			     * server.
			     *    - serverName is the GSSName that represents the server.
			     *    - krb5Oid is the Oid that represents the mechanism to
			     *      use. The client chooses the mechanism to use.
			     *    - null is passed in for client credentials
			     *    - DEFAULT_LIFETIME lets the mechanism decide how long the
			     *      context can remain valid.
			     * Note: Passing in null for the credentials asks GSS-API to
			     * use the default credentials. This means that the mechanism
			     * will look among the credentials stored in the current Subject
			     * to find the right kind of credentials that it needs.
			     */
			    GSSContext context = manager.createContext(serverName,
				spnegoOid,
				null,
				GSSContext.DEFAULT_LIFETIME);

			    // Set the desired optional features on the context. The client
			    // chooses these options.

			    context.requestMutualAuth(true);  // Mutual authentication
			    context.requestConf(true);  // Will use confidentiality later
			    context.requestInteg(true); // Will use integrity later
			    context.requestCredDeleg(true);
			    
			    // Do the context eastablishment loop

			    byte[] token = new byte[0];

			
			    // token is ignored on the first call
				token = context.initSecContext(token, 0, token.length);
		
				// Send a token to the server if one was generated by initSecContext
				if (token != null) 
					System.out.println("Will send token of size " + token.length + " from initSecContext.");
				
				
			    return token;
			
		}
				
		/** MUST be set before call to subject.doAs()
		 * 
		 * @param name
		 */
		public void setPrincipalName(String name)
		{
			this.principalName = name;
		}		
		
	}
	
	/** SET the details for user login. NOTE: Pre-req setup for this test is as follows:
	 * 
	 *  1. run kinit -c /path/to/cache/file user@TEST.KERBEROS.REALM
	 *  where user@TEST.KERBEROS.REALM is a valid principal located in the 
	 *  KDC for the realm setup in krb5.conf
	 *  2. Ensure the ticketCache option below is set to the generated cache file
	 *  3. ensure the user option below is set to the user for whom the cache file 
	 *  was generated.
	 * 
	 * @return Configuration object to be comsumed by kerberos auth module.
	 */
	private Configuration getClientLoginConfiguration()
	{
		Map<String, String> options = new HashMap<String, String>();
		options.put("useTicketCache", "true");
		options.put("principal", "beddoes@ADTST.QUT.EDU.AU");
		options.put("debug", "true");
		
		Map<String, File> fileOptions = new HashMap<String, File>();
		File keytab = new File(this.testdataPath + "ticketcache");
		fileOptions.put("ticketCache", keytab);		
		
		KerberosV5Configuration config = new KerberosV5Configuration(options, fileOptions);
		
		return config;
	}
	
	/** SET the details for server login.
	 * 
	 * @return Configuration object to be comsumed by kerberos auth module.
	 */
	private Configuration getServerLoginConfiguration()
	{
		Map<String, String> options = new HashMap<String, String>();
		options.put("useKeyTab", "true");
		options.put("storeKey", "true");
		options.put("doNotPrompt", "true");
		options.put("debug", "true");
		options.put("principal", "HTTPS/esoe-dev.qut.edu.au:8443");
		
		
		Map<String, File> fileOptions = new HashMap<String, File>();
		File keytab = new File(this.testdataPath + "esoe-dev-http.keytab");
		fileOptions.put("keyTab", keytab);		
		
		KerberosV5Configuration config = new KerberosV5Configuration(options, fileOptions);
		
		return config;
	}	
	
}

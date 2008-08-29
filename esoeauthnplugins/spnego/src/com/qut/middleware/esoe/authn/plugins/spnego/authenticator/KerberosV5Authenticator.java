/* Copyright 2006, Queensland University of Technology
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
 * Author: Andre Zitelli
 * Creation Date: 17/1/2007
 * 
 * Purpose: Kerberos V5 implementation of an SPNEGOAuthenticator.
 */

package com.qut.middleware.esoe.authn.plugins.spnego.authenticator;

import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.security.auth.Subject;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.commons.codec.binary.Base64;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.Oid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.esoe.authn.plugins.spnego.SPNEGOAuthenticator;

public class KerberosV5Authenticator implements SPNEGOAuthenticator
{
	private String kerberosLoginModuleName = Messages.getString("KerberosV5Authenticator.0"); //$NON-NLS-1$
	private Configuration config;
	
	final static  String SPNEGO_OID = Messages.getString("KerberosV5Authenticator.1");  //$NON-NLS-1$
	
	/* Local logging instance */
	Logger logger = LoggerFactory.getLogger(this.getClass().getName());

		
	/** Create a KerberosV5Authenticator with the given login configuration.
	 * 
	 * @param config The Configuration object used to pass login information that will
	 * be used by the kerberos login module to authenticate the server.
	 * 
	 */
	public KerberosV5Authenticator(Configuration config)
	{
		if (config == null)
		{
			this.logger.error(Messages.getString("KerberosV5Authenticator.2")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("KerberosV5Authenticator.3"));  //$NON-NLS-1$
		}
		
		this.config = config;
		
		this.logger.info(Messages.getString("KerberosV5Authenticator.5") + this.kerberosLoginModuleName); //$NON-NLS-1$
		
	}
	
	/* NOT IMPLEMENTED
	 */
	public result authenticate(String userIdentifier, String password)
	{		
		return null;
	}


	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoe.authn.pipeline.SPNEGOAuthenticator#authenticate(java.lang.String)
	 */
	public String authenticate(String spnegoToken)
	{			
		try
		{								
			return this.authenticate(spnegoToken.getBytes());			
		}
		catch(Exception e)
		{
			this.logger.trace(e.toString());
			// auth failure = null return
			return null;
		}
	}	
	
	
	
	/* Authenticate the given GSS token. Current implementation only supports kerberos V5 mechanism.
	 * 
	 * @param spnegoData The base 64 encoded byte array that contains the GSS token to be evaluated for
	 * user authentication.
	 */
	@SuppressWarnings("synthetic-access")
	private String authenticate(byte[] spnegoData)
	{
		// Create action to perform
		KerberosAuthenticationAction action = new KerberosAuthenticationAction();

		byte[] bytes = Base64.decodeBase64(spnegoData);
	
		action.setSpNegoToken(bytes);
		
		return this.loginAndAction(Messages.getString("KerberosV5Authenticator.6"), action);		 //$NON-NLS-1$
	}	
		
	
	/* Performs server kerberos authentication (Ie obtaining TGT) and validates the user Kerberos
	 * ticket via a call the given PrivilegedExceptionAction param.
	 * 
	 * @param loginContextName The context for this server login as configured in the appropriate
	 * Jaas.conf file.
	 * @param The action to perform upon successfull server authentication.
	 */
	@SuppressWarnings("unchecked")
	private String loginAndAction(String loginContextName, KerberosAuthenticationAction actionToPerform) 
	{
		LoginContext context = null;
		
		try 
		{
			// Create a LoginContext 
			context = new LoginContext(loginContextName, null, null, this.config);

			this.logger.trace(Messages.getString("KerberosV5Authenticator.7") + loginContextName); //$NON-NLS-1$
			
			// Perform server authentication
			context.login();
			
			Subject subject = context.getSubject();
			this.logger.trace(subject.toString());
			this.logger.trace(Messages.getString("KerberosV5Authenticator.8") + subject.getPrincipals()); //$NON-NLS-1$
					
			// perform kerberos validation
			return (String)(Subject.doAs(subject, actionToPerform));
			
		} 
		catch (LoginException e)
		{		
			this.logger.warn(Messages.getString("KerberosV5Authenticator.9")); //$NON-NLS-1$
			this.logger.trace(e.getLocalizedMessage(), e);

			return null;
		}
		catch(PrivilegedActionException e)
		{	
			this.logger.trace(e.getLocalizedMessage(), e);
			this.logger.trace(Messages.getString("KerberosV5Authenticator.10") + e.getCause().getMessage()); //$NON-NLS-1$
		
			return null;
		}
		catch(Exception e)
		{
			this.logger.debug(Messages.getString("KerberosV5Authenticator.11") + e.getCause().getMessage()); //$NON-NLS-1$
			this.logger.trace(e.getLocalizedMessage(), e);
			
			return null;
		}		
		
	}
		
	
	/** Class to perform the actual authentication logic. Passed to login method.
	 * 
	 */
	private class KerberosAuthenticationAction implements PrivilegedExceptionAction
	{
		private byte[] spnegoToken = null;
		
		public Object run() throws GSSException 
		{		
			// Get own Kerberos credentials for accepting connection
			GSSManager manager = GSSManager.getInstance();
			Oid spnegoOid = new Oid(SPNEGO_OID);
					
			GSSCredential serverCreds = manager.createCredential(null,
			GSSCredential.INDEFINITE_LIFETIME, spnegoOid, GSSCredential.ACCEPT_ONLY);
						
			/*
			 * Create a GSSContext to receive the incoming request from the
			 * client. Use null for the server credentials passed in. This
			 * tells the underlying mechanism to use whatever credentials it
			 * has available that can be used to accept this connection.
			 */
			GSSContext context = manager.createContext((GSSCredential) serverCreds);
			
			KerberosV5Authenticator.this.logger.trace(Messages.getString("KerberosV5Authenticator.12") + this.spnegoToken.length +Messages.getString("KerberosV5Authenticator.13") + getHexBytes(this.spnegoToken, 0, this.spnegoToken.length)); //$NON-NLS-1$ //$NON-NLS-2$
						
			// we wont return the token at this stage
			@SuppressWarnings("unused")
			byte[] token = context.acceptSecContext(this.spnegoToken, 0, this.spnegoToken.length);
			
			KerberosV5Authenticator.this.logger.trace("Context established."); //$NON-NLS-1$
			KerberosV5Authenticator.this.logger.trace(Messages.getString("KerberosV5Authenticator.14") + context.getCredDelegState()); //$NON-NLS-1$
			KerberosV5Authenticator.this.logger.trace(Messages.getString("KerberosV5Authenticator.15") + context.getSrcName()); //$NON-NLS-1$
			KerberosV5Authenticator.this.logger.trace(Messages.getString("KerberosV5Authenticator.16") + context.getTargName()); //$NON-NLS-1$
			
			String authenticatedPrincipal = null;
			
			if(context.isEstablished())
			{
				authenticatedPrincipal = context.getSrcName().toString();
				KerberosV5Authenticator.this.logger.info(Messages.getString("KerberosV5Authenticator.17") + authenticatedPrincipal); //$NON-NLS-1$
			}
			
			return authenticatedPrincipal;
		
		}
		
		public void setSpNegoToken(byte[] token)
		{
			this.spnegoToken = token;
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
	}
}

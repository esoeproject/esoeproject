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
 * Creation Date: 09/10/2006
 * 
 * Purpose: Given a user identifier and password authenticates the user against the configured LDAP server.
 * Based on the IETF LDAP classes for Java. For more details on API see:
 * http://developer.novell.com/documentation/jldap/jldapenu/ietfapi/index.html
 */
package com.qut.middleware.esoe.authn.pipeline.authenticator;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;

import org.apache.log4j.Logger;
import org.ietf.ldap.LDAPConnection;
import org.ietf.ldap.LDAPException;
import org.ietf.ldap.LDAPSearchResults;

import com.novell.ldap.LDAPJSSESecureSocketFactory;
import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.authn.pipeline.UserPassAuthenticator;

public class LdapBasicAuthenticator implements UserPassAuthenticator
{
	private String LDAP_EQUALS = "="; //$NON-NLS-1$
	private String LDAP_SEPERATOR = ","; //$NON-NLS-1$
	private String LDAP_UTF = "UTF8"; //$NON-NLS-1$
	private String LDAP_ATTRIBUTES[] = { "dn" }; //$NON-NLS-1$

	private int LDAP_VERSION = 3;

	private String ldapServer;
	private String baseDN;
	private String identifier;
	private String adminUser;
	private String adminPassword;

	private int ldapServerPort;

	private boolean recursive;
	private boolean disableSSL;

	/* Local logging instance */
	private Logger logger = Logger.getLogger(LdapBasicAuthenticator.class.getName());
	private Logger authnLogger = Logger.getLogger(ConfigurationConstants.authnLogger);

	/**
	 * Constructor for LdapBasicAuthenticator where admin binds are not required
	 * 
	 * @param ldapServer
	 * @param ldapServerPort
	 * @param baseDN
	 * @param identifier
	 * @param recursive
	 * @param disableSSL
	 */
	public LdapBasicAuthenticator(String ldapServer, int ldapServerPort, String baseDN, String identifier,
			boolean recursive, boolean disableSSL)
	{
		this.logger.debug(Messages.getString("LdapBasicAuthenticator.3")); //$NON-NLS-1$
		this.setupAuthenticator(ldapServer, ldapServerPort, baseDN, identifier, recursive, disableSSL);
	}

	/**
	 * Constructor for LdapBasicAuthenticator where admin binds are required
	 * 
	 * @param ldapServer
	 * @param ldapServerPort
	 * @param baseDN
	 * @param identifier
	 * @param recursive
	 * @param disableSSL
	 * @param adminUser
	 * @param adminPassword
	 */
	public LdapBasicAuthenticator(String ldapServer, int ldapServerPort, String baseDN, String identifier,
			boolean recursive, boolean disableSSL, String adminUser, String adminPassword)
	{
		if(adminUser == null)
			throw new IllegalArgumentException(Messages.getString("LdapBasicAuthenticator.25")); //$NON-NLS-1$
		
		if(adminPassword == null)
			throw new IllegalArgumentException(Messages.getString("LdapBasicAuthenticator.26")); //$NON-NLS-1$
		
		this.logger.debug(Messages.getString("LdapBasicAuthenticator.4")); //$NON-NLS-1$
		this.adminUser = adminUser;
		this.adminPassword = adminPassword;
		this.setupAuthenticator(ldapServer, ldapServerPort, baseDN, identifier, recursive, disableSSL);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.authn.pipeline.Authenticator#authenticate(java.lang.String, java.lang.String)
	 */
	public result authenticate(String userIdentifier, String password)
	{
		LDAPConnection conn;
		LDAPJSSESecureSocketFactory ssf;
		String DN = determineDN(userIdentifier);

		if (DN == null)
		{
			this.logger.debug(Messages.getString("LdapBasicAuthenticator.5") + userIdentifier); //$NON-NLS-1$
			return result.Failure;
		}

		if (!this.disableSSL)
		{
			/* Authenticate the user by binding to the configured ldap server */
			ssf = new LDAPJSSESecureSocketFactory();
			conn = new LDAPConnection(ssf);
		}
		else
		{
			this.logger.info(Messages.getString("LdapBasicAuthenticator.6")); //$NON-NLS-1$
			conn = new LDAPConnection();
		}

		try
		{
			conn.connect(this.ldapServer, this.ldapServerPort);

			try
			{
				this.logger.trace(Messages.getString("LdapBasicAuthenticator.27") + DN); //$NON-NLS-1$
				
				conn.bind(this.LDAP_VERSION, DN, password.getBytes(this.LDAP_UTF));
			}
			catch (UnsupportedEncodingException uee)
			{
				this.logger.error(Messages.getString("LdapBasicAuthenticator.7")); //$NON-NLS-1$
				throw new LDAPException("UTF8 Invalid Encoding", LDAPException.LOCAL_ERROR, (String) null, uee); //$NON-NLS-1$
			}

			if (conn.isBound())
			{
				this.authnLogger.info(Messages.getString("LdapBasicAuthenticator.8") + DN); //$NON-NLS-1$
				return result.Successful;
			}

			this.authnLogger.error(Messages.getString("LdapBasicAuthenticator.9") + DN); //$NON-NLS-1$
			return result.Failure;
		}
		catch (LDAPException e)
		{
			this.logger.warn(MessageFormat.format(Messages.getString("LdapBasicAuthenticator.10"), DN, e.getLocalizedMessage()) ); //$NON-NLS-1$
			return result.Failure;
		}
		finally
		{
			try
			{
				conn.disconnect();
			}
			catch (LDAPException le)
			{
				this.logger
						.warn(Messages.getString("LdapBasicAuthenticator.11") //$NON-NLS-1$
								+ le.getLocalizedMessage());
			}
		}
	}

	private String determineDN(String userIdentifier)
	{
		LDAPConnection conn;
		LDAPJSSESecureSocketFactory ssf;

		this.logger.trace(MessageFormat.format(Messages.getString("LdapBasicAuthenticator.28"), userIdentifier) ); //$NON-NLS-1$
				
		if (!this.recursive)
		{
			String DN = (this.identifier.concat(userIdentifier)).concat(this.LDAP_SEPERATOR).concat(this.baseDN);
			this.logger.debug(Messages.getString("LdapBasicAuthenticator.12") + DN); //$NON-NLS-1$
			/* Simply generates complete DN based on configuration alone, all users in the same container */
			return DN;
		}

		/* Users in multiple subcontainers we must resolve DN using the directory */
		if (!this.disableSSL)
		{
			/* Authenticate the user by binding to the configured ldap server */
			ssf = new LDAPJSSESecureSocketFactory();
			conn = new LDAPConnection(ssf);
		}
		else
		{
			this.logger.info(Messages.getString("LdapBasicAuthenticator.13")); //$NON-NLS-1$
			conn = new LDAPConnection();
		}

		try
		{
			conn.connect(this.ldapServer, this.ldapServerPort);
			try
			{
				this.logger.trace(Messages.getString("LdapBasicAuthenticator.29") + this.adminUser); //$NON-NLS-1$
				
				conn.bind(this.LDAP_VERSION, this.adminUser, this.adminPassword.getBytes(this.LDAP_UTF));
			}
			catch (UnsupportedEncodingException uee)
			{
				this.logger.error(Messages.getString("LdapBasicAuthenticator.14")); //$NON-NLS-1$
				throw new LDAPException("UTF8 Invalid Encoding", LDAPException.LOCAL_ERROR, uee.getMessage(), uee); //$NON-NLS-1$
			}
			
			String searchFilter = this.identifier.concat(userIdentifier);
			
			this.logger.debug(MessageFormat.format(Messages.getString("LdapBasicAuthenticator.24"), searchFilter, this.baseDN) ); //$NON-NLS-1$
			
			LDAPSearchResults searchResults = conn.search(this.baseDN, LDAPConnection.SCOPE_SUB, searchFilter, this.LDAP_ATTRIBUTES, true);

			if(searchResults.hasMore())
				return searchResults.next().getDN();
			else
				return null;
		}
		catch (LDAPException e)
		{
			this.logger.warn(MessageFormat.format(Messages.getString("LdapBasicAuthenticator.16"), e.getLocalizedMessage()) ); //$NON-NLS-1$
			this.logger.trace(e);
			return null;
		}
		finally
		{
			try
			{
				conn.disconnect();
			}
			catch (LDAPException le)
			{
				this.logger
						.warn(Messages.getString("LdapBasicAuthenticator.17") //$NON-NLS-1$
								+ le.getLocalizedMessage());
				return null;
			}
		}
	}

	/**
	 * This allows administrators to disable SSL functionality, it is not reccomended in PRD
	 */
	public void disableSSL()
	{
		this.disableSSL = true;
	}

	/**
	 * @return The base DN
	 */
	public String getBaseDN()
	{
		return this.baseDN;
	}

	/**
	 * @return The identifier used to located a user in the directory. This will be prepended to
	 * the base DN and user ID when performing a search. For example uid, cn etc. Will become
	 * uid=username,this.baseDN
	 */
	public String getIdentifier()
	{
		return this.identifier.substring(0, this.identifier.indexOf('='));
	}

	/**
	 * @return The LDAP server
	 */
	public String getLdapServer()
	{
		return this.ldapServer;
	}

	/**
	 * @return The LDAP server port
	 */
	public int getLdapServerPort()
	{
		return this.ldapServerPort;
	}

	/**
	 * @return Boolean value indicating if the search will be recursive
	 */
	public boolean isRecursive()
	{
		return this.recursive;
	}

	/**
	 * Sets up initial LdapBasicAuthenticator state
	 * @param ldapServer LDAP server to point to (IP or DNS entry)
	 * @param ldapServerPort Port to connect to
	 * @param baseDN BaseDN to start searching from if recursive or container holding all objects if non recursive
	 * @param identifier Identifier of objects int he directory commonly uid or cn
	 * @param recursive Is recursive searching required or not
	 * @param disableSSL Are SSL connections supported
	 */
	private void setupAuthenticator(String ldapServer, int ldapServerPort, String baseDN, String identifier,
			boolean recursive, boolean disableSSL)
	{
		/* Ensure that a stable base is created when this authenticator is setup */
		if (ldapServer == null)
		{
			this.logger.fatal(Messages.getString("LdapBasicAuthenticator.0")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("LdapBasicAuthenticator.0")); //$NON-NLS-1$
		}

		if (baseDN == null)
		{
			this.logger.fatal(Messages.getString("LdapBasicAuthenticator.1")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("LdapBasicAuthenticator.1")); //$NON-NLS-1$
		}

		if (identifier == null)
		{
			this.logger.fatal(Messages.getString("LdapBasicAuthenticator.2")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("LdapBasicAuthenticator.2")); //$NON-NLS-1$
		}

		if(ldapServerPort < 1 || ldapServerPort > Integer.MAX_VALUE)
			throw new IllegalArgumentException(Messages.getString("LdapBasicAuthenticator.30") + Integer.MAX_VALUE); //$NON-NLS-1$
		
		this.ldapServer = ldapServer;
		this.baseDN = baseDN;
		this.identifier = identifier.concat(this.LDAP_EQUALS);

		this.ldapServerPort = ldapServerPort;
		this.recursive = recursive;

		this.disableSSL = disableSSL;

		this.logger.info(Messages.getString("LdapBasicAuthenticator.18") + this.ldapServer //$NON-NLS-1$
				+ Messages.getString("LdapBasicAuthenticator.19") + this.ldapServerPort + Messages.getString("LdapBasicAuthenticator.20") + this.baseDN + Messages.getString("LdapBasicAuthenticator.21") + this.identifier //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ Messages.getString("LdapBasicAuthenticator.22") + this.recursive + Messages.getString("LdapBasicAuthenticator.23") + this.disableSSL); //$NON-NLS-1$ //$NON-NLS-2$
	}
}

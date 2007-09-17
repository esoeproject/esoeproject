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
 * Author: Andre Zitelli
 * Creation Date: 02/01/2007
 * 
 * Purpose: Allows users to authenticate themselves to the system using a GSS-API Negotiation Mechanism.
 * See RFC 2478.
 */
package com.qut.middleware.esoe.authn.pipeline.handlers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.authn.bean.AuthnIdentityAttribute;
import com.qut.middleware.esoe.authn.bean.AuthnProcessorData;
import com.qut.middleware.esoe.authn.exception.SessionCreationException;
import com.qut.middleware.esoe.authn.pipeline.Authenticator;
import com.qut.middleware.esoe.authn.pipeline.Handler;
import com.qut.middleware.esoe.authn.pipeline.SPNEGOAuthenticator;
import com.qut.middleware.esoe.sessions.Create;
import com.qut.middleware.esoe.sessions.SessionsProcessor;
import com.qut.middleware.esoe.sessions.exception.DataSourceException;
import com.qut.middleware.esoe.sessions.exception.DuplicateSessionException;
import com.qut.middleware.saml2.AuthenticationContextConstants;
import com.qut.middleware.saml2.identifier.IdentifierGenerator;

public class SPNEGOHandler implements Handler
{
	private SessionsProcessor sessionsProcessor;
	private SPNEGOAuthenticator authenticator;
	private IdentifierGenerator identifierGenerator;
	private List<AuthnIdentityAttribute> identityAttributes;
	private String handlerName = "SPNEGOHandler"; //$NON-NLS-1$
	private String redirectTarget;
	private String userAgentID;

	private Logger logger = Logger.getLogger(this.getClass().getName());
	private Logger authnLogger = Logger.getLogger(ConfigurationConstants.authnLogger);

	// Required headers for SPNEGO processing.
	private final static String HEADER_WWW_AUTHENTICATE = Messages.getString("SPNEGOHandler.0"); //$NON-NLS-1$
	private final static String HEADER_AUTHORIZATION = Messages.getString("SPNEGOHandler.1"); //$NON-NLS-1$
	private final static String HEADER_USER_AGENT = Messages.getString("SPNEGOHandler.2"); //$NON-NLS-1$

	// The prefix used by SPNEGO before the Base64 encoded GSS-API token (Negotiate).
	private final static String SPNEGO_NEGOTIATE = Messages.getString("SPNEGOHandler.3"); //$NON-NLS-1$

	/**
	 * 
	 * @param authenticator
	 *            The authenticator to use with this handler. As this handler processes SPNEGO tickets, the
	 *            authenticator must implement Kerberos V5 authentication.
	 * 
	 * @param sessionsProcessor
	 *            A concrete sessionsProcessor implementation
	 * @param identifierGenerator
	 *            A concrete identifierGenerator implementation
	 * @param identityAttributes
	 *            A list of attributes for the handler to inject into the users identity information if successfully
	 *            authenticated
	 * @param redirectURL
	 *            The URL to redirect the user to on successfull authentication.
	 * @param spnegoUserAgentID
	 *            The tag added to the user agent header by a browser that supports SSO (as defined by the
	 *            intstitution)- see esoe.config: spnegoHandler.spnegoUserAgentID
	 * 
	 */
	public SPNEGOHandler(SPNEGOAuthenticator authenticator, SessionsProcessor sessionsProcessor,
			IdentifierGenerator identifierGenerator, List<AuthnIdentityAttribute> identityAttributes,
			String redirectURL, String spnegoUserAgentID)
	{
		if (authenticator == null)
		{
			this.logger.fatal(Messages.getString("SPNEGOHandler.4")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("SPNEGOHandler.5")); //$NON-NLS-1$
		}

		if (sessionsProcessor == null)
		{
			this.logger.fatal(Messages.getString("SPNEGOHandler.6")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("SPNEGOHandler.7")); //$NON-NLS-1$
		}

		if (identifierGenerator == null)
		{
			this.logger.fatal(Messages.getString("SPNEGOHandler.8")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("SPNEGOHandler.8")); //$NON-NLS-1$
		}

		if (identityAttributes == null)
		{
			this.logger.fatal(Messages.getString("SPNEGOHandler.9")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("SPNEGOHandler.9")); //$NON-NLS-1$
		}
		
		if (redirectURL == null)
		{
			this.logger.fatal(Messages.getString("SPNEGOHandler.10")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("SPNEGOHandler.11")); //$NON-NLS-1$
		}

		if (spnegoUserAgentID == null)
		{
			this.logger.fatal(Messages.getString("SPNEGOHandler.12")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("SPNEGOHandler.13")); //$NON-NLS-1$
		}

		this.authenticator = authenticator;
		this.sessionsProcessor = sessionsProcessor;
		this.identifierGenerator = identifierGenerator;
		this.identityAttributes = identityAttributes;
		this.redirectTarget = redirectURL;
		this.userAgentID = spnegoUserAgentID;

		this.logger
				.info(Messages.getString("SPNEGOHandler.14") + this.getHandlerName() + Messages.getString("SPNEGOHandler.15") + spnegoUserAgentID + Messages.getString("SPNEGOHandler.16") + this.redirectTarget); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.authn.pipeline.Handler#execute(com.qut.middleware.esoe.authn.bean.AuthnProcessorData)
	 */
	public result execute(AuthnProcessorData data) throws SessionCreationException
	{
		HttpServletRequest request = data.getHttpRequest();

		// check if auto SSO is disabled. If so take no action
		if (!data.getAutomatedSSO())
		{
			this.logger.debug(Messages.getString("SPNEGOHandler.17")); //$NON-NLS-1$
			return Handler.result.NoAction;
		}

		// Check browser for supportedness. We can only authenticate the user if they are logged
		// on to the AD domain. In this environment we expect the browser to send a custom header
		// to identify that is is in fact joined to a domain.

		String userAgentValue = request.getHeader(HEADER_USER_AGENT);

		this.logger.debug(Messages.getString("SPNEGOHandler.18") + userAgentValue); //$NON-NLS-1$

		if (userAgentValue != null && !userAgentValue.contains(this.userAgentID))
		{
			this.logger.debug(Messages.getString("SPNEGOHandler.19")); //$NON-NLS-1$
		}
		else
		{
			// check for presence of SPNEGO negotiate
			String spnegoNegotiateData = request.getHeader(HEADER_AUTHORIZATION);

			// If the browser hasn't sent a 'Negotiate' authorization header, challenge them
			if (spnegoNegotiateData == null)
			{
				this.logger
						.debug(Messages.getString("SPNEGOHandler.20") + data.getHttpRequest().getRemoteAddr() + Messages.getString("SPNEGOHandler.21")); //$NON-NLS-1$ //$NON-NLS-2$

				data.getHttpResponse().addHeader(HEADER_WWW_AUTHENTICATE, SPNEGO_NEGOTIATE);
				data.setError(HttpServletResponse.SC_UNAUTHORIZED, Messages.getString("SPNEGOHandler.22")); //$NON-NLS-1$

				return Handler.result.UserAgent;
			}

			// browser has sent Negotiate header, parse and process
			this.logger.debug(Messages.getString("SPNEGOHandler.23")); //$NON-NLS-1$

			// strip off "Negotiate" part to leave the actual token
			spnegoNegotiateData = spnegoNegotiateData.substring(SPNEGO_NEGOTIATE.length() + 1);

			if (spnegoNegotiateData != null)
			{
				this.logger.trace(Messages.getString("SPNEGOHandler.24") + spnegoNegotiateData); //$NON-NLS-1$ 

				String authenticatedPrincipal = this.authenticator.authenticate(spnegoNegotiateData);

				if (authenticatedPrincipal != null)
				{
					this.logger.debug(Messages.getString("SPNEGOHandler.26") + this.redirectTarget); //$NON-NLS-1$

					data.setRedirectTarget(this.redirectTarget);
					data.setPrincipalName(this.extractUid(authenticatedPrincipal));
				}
				else
				{
					// browser did not send a supported SPNEGO token, or authentication failed.
					// Take no action, let next handler attempt auth
					this.logger
							.debug(Messages.getString("SPNEGOHandler.27") + request.getRemoteAddr() + Messages.getString("SPNEGOHandler.28") + userAgentValue); //$NON-NLS-1$ //$NON-NLS-2$

					return Handler.result.NoAction;
				}

				// create the session for the authenticated principal
				Create.result result;

				data.setSessionID(this.identifierGenerator.generateSessionID());
				this.authnLogger.info("Successfully authenticated principal " + this.extractUid(authenticatedPrincipal) + " to underlying authentication mechanism identified by external ESOE ID of: " + data.getSessionID() + " using SPNEGO handler");
				
				try
				{
					// Create the session in the local session cache
					result = this.sessionsProcessor.getCreate().createLocalSession(data.getSessionID(),
							data.getPrincipalName(), AuthenticationContextConstants.passwordProtectedTransport, this.identityAttributes);
				}
				catch (DuplicateSessionException dse)
				{
					this.logger
							.error("Duplicate Session detected: " + data.getSessionID() + Messages.getString("UsernamePasswordHandler.17") + data.getPrincipalName()); //$NON-NLS-1$ //$NON-NLS-2$
					return Handler.result.Invalid;
				}
				catch (DataSourceException dse)
				{
					this.logger
							.error("Session processor was unable to setup principal session correctly due to underlying data source problems."); //$NON-NLS-1$
					return Handler.result.Invalid;
				}

				this.logger.debug("Result of establishing session with sessions processor was: " + result); //$NON-NLS-1$

				// Interpret the return value from the sessions processor
				switch (result)
				{
					case SessionCreated:
						return Handler.result.Successful;
					default:
						return Handler.result.Invalid;
				}
			}
		}

		return Handler.result.NoAction;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.authn.pipeline.Handler#getAuthenticator()
	 */
	public Authenticator getAuthenticator()
	{
		return this.authenticator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.authn.pipeline.Handler#getHandlerName()
	 */
	public String getHandlerName()
	{
		return this.handlerName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.authn.pipeline.Handler#getIdentifierGenerator()
	 */
	public IdentifierGenerator getIdentifierGenerator()
	{
		return this.identifierGenerator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.authn.pipeline.Handler#getSessionsProcessor()
	 */
	public SessionsProcessor getSessionsProcessor()
	{
		return this.sessionsProcessor;
	}

	/*
	 * Extract the uid that the sessions processor expects from the given kerberos principal. Ie: the
	 * part before the @ symbol. If for some reason the principal parameter string does not contain
	 * the @ symbol, null is returned.
	 * 
	 */
	private String extractUid(String principalName)
	{
		String uid = null;

		if (principalName != null)
		{
			int end = principalName.indexOf("@"); //$NON-NLS-1$
			if(end != -1)
				uid = principalName.substring(0, end); 
		}

		return uid;
	}
}

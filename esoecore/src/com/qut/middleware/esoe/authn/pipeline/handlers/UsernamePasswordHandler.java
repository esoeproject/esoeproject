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
 * Creation Date: 06/10/2006
 * 
 * Purpose: Allows users to authenticate themselves to the system using a username and password
 * combination supplied via html web form via http post
 */
package com.qut.middleware.esoe.authn.pipeline.handlers;

import java.util.List;

import org.apache.log4j.Logger;

import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.authn.bean.AuthnIdentityAttribute;
import com.qut.middleware.esoe.authn.bean.AuthnProcessorData;
import com.qut.middleware.esoe.authn.exception.SessionCreationException;
import com.qut.middleware.esoe.authn.pipeline.Authenticator;
import com.qut.middleware.esoe.authn.pipeline.Handler;
import com.qut.middleware.esoe.authn.pipeline.UserPassAuthenticator;
import com.qut.middleware.esoe.sessions.Create;
import com.qut.middleware.esoe.sessions.SessionsProcessor;
import com.qut.middleware.esoe.sessions.exception.DataSourceException;
import com.qut.middleware.esoe.sessions.exception.DuplicateSessionException;
import com.qut.middleware.saml2.AuthenticationContextConstants;
import com.qut.middleware.saml2.identifier.IdentifierGenerator;

public class UsernamePasswordHandler implements Handler
{
	private final String METHOD_POST = "POST"; //$NON-NLS-1$
	private final String FORM_USER_IDENTIFIER = "esoeauthn_user"; //$NON-NLS-1$
	private final String FORM_PASSWORD_IDENTIFIER = "esoeauthn_pw"; //$NON-NLS-1$
	private final String HANDLER_VERSION = "esoehandler=uph-1.0"; ///$NON-NLS-1$

	private SessionsProcessor sessionsProcessor;
	private UserPassAuthenticator authenticator;
	private IdentifierGenerator identifierGenerator;
	private List<AuthnIdentityAttribute> identityAttributes;

	private String handlerName = "UsernamePasswordHandler v-1.0"; //$NON-NLS-1$
	private String redirectTarget;
	private String invalidURL, requireCredentialsURL, failedAuthnNameValue;

	/* Local logging instance */
	private Logger logger = Logger.getLogger(UsernamePasswordHandler.class.getName());
	private Logger authnLogger = Logger.getLogger(ConfigurationConstants.authnLogger);

	/**
	 * Sets up the UsernamePasswordHandler, if a default redirectTarget is not provided (element 0) then the failURL
	 * will be substituted
	 * 
	 * @param authenticator
	 *            A concrete userpassauthenticator implementation
	 * @param sessionsProcessor
	 *            A concrete sessionsProcessor implementation
	 * @param identifierGenerator
	 *            A concrete identifierGenerator implementation
	 * @param identityAttributes
	 * 			  A list of attributes for the handler to inject into the users identity information if successfully authenticated
	 * @param requireCredentialsURL
	 *            URL to direct principal to when we need to acquire their username and password
	 * @param failedAuthnNameValue
	 *            Name Value pair to append to requireCredentialsURL when user fails authentication eg: rc=authnfail
	 * @param redirectTarget
	 *            Redirect target to send principal to on successful authentication
	 * @param invalidURL
	 *            URL to redirect to for all failures
	 */
	public UsernamePasswordHandler(UserPassAuthenticator authenticator, SessionsProcessor sessionsProcessor,
			IdentifierGenerator identifierGenerator, List<AuthnIdentityAttribute> identityAttributes, String requireCredentialsURL, String failedAuthnNameValue,
			String redirectTarget, String invalidURL)
	{
		/* Ensure that a stable base is created when this Handler is setup */
		if (redirectTarget == null)
		{
			this.logger.fatal(Messages.getString("UsernamePasswordHandler.6")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("UsernamePasswordHandler.6")); //$NON-NLS-1$
		}

		if (authenticator == null)
		{
			this.logger.fatal(Messages.getString("UsernamePasswordHandler.7")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("UsernamePasswordHandler.7")); //$NON-NLS-1$
		}

		if (sessionsProcessor == null)
		{
			this.logger.fatal(Messages.getString("UsernamePasswordHandler.8")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("UsernamePasswordHandler.8")); //$NON-NLS-1$
		}

		if (identifierGenerator == null)
		{
			this.logger.fatal(Messages.getString("UsernamePasswordHandler.9")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("UsernamePasswordHandler.9")); //$NON-NLS-1$
		}

		if (invalidURL == null)
		{
			this.logger.fatal(Messages.getString("UsernamePasswordHandler.10")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("UsernamePasswordHandler.10")); //$NON-NLS-1$
		}

		if (requireCredentialsURL == null)
		{
			this.logger.fatal(Messages.getString("UsernamePasswordHandler.11")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("UsernamePasswordHandler.11")); //$NON-NLS-1$
		}

		if (failedAuthnNameValue == null)
		{
			this.logger.fatal(Messages.getString("UsernamePasswordHandler.5")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("UsernamePasswordHandler.5")); //$NON-NLS-1$
		}

		this.authenticator = authenticator;
		this.sessionsProcessor = sessionsProcessor;
		this.identifierGenerator = identifierGenerator;
		this.identityAttributes = identityAttributes;
		this.requireCredentialsURL = requireCredentialsURL;
		this.failedAuthnNameValue = failedAuthnNameValue;
		this.redirectTarget = redirectTarget;
		this.invalidURL = invalidURL;

		this.logger.info(Messages.getString("UsernamePasswordHandler.4") + this.getHandlerName() + Messages.getString("UsernamePasswordHandler.3") //$NON-NLS-1$ //$NON-NLS-2$
				+ this.requireCredentialsURL + Messages.getString("UsernamePasswordHandler.2") + this.failedAuthnNameValue //$NON-NLS-1$
				+ Messages.getString("UsernamePasswordHandler.1") + this.redirectTarget + Messages.getString("UsernamePasswordHandler.0") + this.invalidURL); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.authn.pipeline.Handler#execute(com.qut.middleware.esoe.authn.bean.AuthnProcessorData)
	 */
	public result execute(AuthnProcessorData data) throws SessionCreationException
	{
		Create.result result;

		String principalName;
		String password;

		if (data.getSuccessfulAuthn())
		{
			this.logger.debug(Messages.getString("UsernamePasswordHandler.12")); //$NON-NLS-1$
			return Handler.result.NoAction;
		}

		/* If a GET request is recieved by this catch all Handler send the user to the login environment */
		if (!data.getHttpRequest().getMethod().equals(this.METHOD_POST))
		{
			this.logger.debug(Messages.getString("UsernamePasswordHandler.13")); //$NON-NLS-1$
			failedAuthentication(data, false);
			return Handler.result.UserAgent;
		}

		principalName = data.getHttpRequest().getParameter(this.FORM_USER_IDENTIFIER);
		password = data.getHttpRequest().getParameter(this.FORM_PASSWORD_IDENTIFIER);

		if (principalName == null || password == null)
		{
			this.logger.debug("Did not find either username or password, ensure form submitted contains " + this.FORM_USER_IDENTIFIER + " AND " + this.FORM_PASSWORD_IDENTIFIER);
			failedAuthentication(data, true);
			return Handler.result.Failure;
		}

		if (Authenticator.result.Failure == this.authenticator.authenticate(principalName, password))
		{
			this.authnLogger.error(Messages.getString("UsernamePasswordHandler.14") + principalName + Messages.getString("UsernamePasswordHandler.15")); //$NON-NLS-1$ //$NON-NLS-2$
			failedAuthentication(data, true);
			return Handler.result.Failure;
		}

		data.setSessionID(this.identifierGenerator.generateSessionID());
		this.authnLogger.info("Successfully authenticated principal " + principalName + Messages.getString("UsernamePasswordHandler.15") + data.getSessionID() + " using username/password handler");

		try
		{
			// Create the session in the local session cache
			result = this.sessionsProcessor.getCreate().createLocalSession(data.getSessionID(), principalName,
					AuthenticationContextConstants.passwordProtectedTransport, this.identityAttributes);
		}
		catch (DuplicateSessionException dse)
		{
			this.logger.error(Messages.getString("UsernamePasswordHandler.16") + data.getSessionID() + Messages.getString("UsernamePasswordHandler.17") + principalName); //$NON-NLS-1$ //$NON-NLS-2$
			invalidAuthentication(data);
			return Handler.result.Invalid;
		}
		catch (DataSourceException dse)
		{
			this.logger.error(Messages.getString("UsernamePasswordHandler.18")); //$NON-NLS-1$
			invalidAuthentication(data);
			return Handler.result.Invalid;
		}

		this.authnLogger.info(Messages.getString("UsernamePasswordHandler.19") + result); //$NON-NLS-1$
		
		// Interpret the return value from the sessions processor
		switch (result)
		{
			case SessionCreated:
				successfulAuthentication(data);
				return Handler.result.Successful;
			default:
				invalidAuthentication(data);
				return Handler.result.Invalid;
		}
	}

	/**
	 * Undertakes logic when the user successfully authenticates.
	 * 
	 * @param data
	 *            The current AuthnProcessor data for the principals session
	 * @param redirID
	 *            The determined value of the response URL to reply to
	 */
	private void successfulAuthentication(AuthnProcessorData data)
	{
		this.logger.debug(Messages.getString("UsernamePasswordHandler.20")); //$NON-NLS-1$

		data.setRedirectTarget(this.redirectTarget);
	}

	/**
	 * Undertakes logic when the user fails authentication, will occur when no userIdentifier or password is presented
	 * or when the underlying authentication repository indicates the combination is invalid.
	 * 
	 * @param data
	 *            The current AuthnProcessor data for the principals session
	 */
	private void failedAuthentication(AuthnProcessorData data, boolean appendErrorCode)
	{
		this.logger.debug(Messages.getString("UsernamePasswordHandler.21")); //$NON-NLS-1$
		if (appendErrorCode)
			data.setRedirectTarget(this.requireCredentialsURL + "?" + this.failedAuthnNameValue); //$NON-NLS-1$
		else
			data.setRedirectTarget(this.requireCredentialsURL);
	}

	/**
	 * Undertakes logic when the system fails to complete authentication, possibly due to invalid input, duplicated
	 * sessions etc.
	 * 
	 * @param data
	 *            The current AuthnProcessor data for the principals session
	 */
	private void invalidAuthentication(AuthnProcessorData data)
	{
		data.setInvalidURL(this.invalidURL + "?" + this.HANDLER_VERSION); //$NON-NLS-1$
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
}

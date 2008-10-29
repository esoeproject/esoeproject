package com.qut.middleware.esoe.authn.plugins.usernamepassword.handler;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.authn.bean.AuthnIdentityAttribute;
import com.qut.middleware.esoe.authn.bean.AuthnProcessorData;
import com.qut.middleware.esoe.authn.exception.SessionCreationException;
import com.qut.middleware.esoe.authn.pipeline.Authenticator;
import com.qut.middleware.esoe.authn.pipeline.Handler;
import com.qut.middleware.esoe.sessions.SessionsProcessor;
import com.qut.middleware.esoe.sessions.exception.SessionCacheUpdateException;
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
	private Logger logger = LoggerFactory.getLogger(UsernamePasswordHandler.class.getName());
	private Logger authnLogger = LoggerFactory.getLogger(ConfigurationConstants.authnLogger);

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
			this.logger.error(Messages.getString("UsernamePasswordHandler.6")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("UsernamePasswordHandler.6")); //$NON-NLS-1$
		}

		if (authenticator == null)
		{
			this.logger.error(Messages.getString("UsernamePasswordHandler.7")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("UsernamePasswordHandler.7")); //$NON-NLS-1$
		}

		if (sessionsProcessor == null)
		{
			this.logger.error(Messages.getString("UsernamePasswordHandler.8")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("UsernamePasswordHandler.8")); //$NON-NLS-1$
		}

		if (identifierGenerator == null)
		{
			this.logger.error(Messages.getString("UsernamePasswordHandler.9")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("UsernamePasswordHandler.9")); //$NON-NLS-1$
		}

		if (invalidURL == null)
		{
			this.logger.error(Messages.getString("UsernamePasswordHandler.10")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("UsernamePasswordHandler.10")); //$NON-NLS-1$
		}

		if (requireCredentialsURL == null)
		{
			this.logger.error(Messages.getString("UsernamePasswordHandler.11")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("UsernamePasswordHandler.11")); //$NON-NLS-1$
		}

		if (failedAuthnNameValue == null)
		{
			this.logger.error(Messages.getString("UsernamePasswordHandler.5")); //$NON-NLS-1$
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
			return Handler.result.UserAgent;
		}

		if (Authenticator.result.Failure == this.authenticator.authenticate(principalName, password))
		{
			this.authnLogger.error(Messages.getString("UsernamePasswordHandler.14") + principalName + Messages.getString("UsernamePasswordHandler.15")); //$NON-NLS-1$ //$NON-NLS-2$
			failedAuthentication(data, true);
			return Handler.result.Failure;
		}

		data.setPrincipalName(principalName);
		data.setSessionID(this.identifierGenerator.generateSessionID());
		this.authnLogger.info("Successfully authenticated principal " + principalName + Messages.getString("UsernamePasswordHandler.15") + data.getSessionID() + " using username/password handler");

		try
		{
			// Create the session in the local session cache
			this.sessionsProcessor.getCreate().createLocalSession(data.getSessionID(), principalName,
			AuthenticationContextConstants.passwordProtectedTransport, this.identityAttributes);
			
			successfulAuthentication(data);
			return Handler.result.Successful;
		}
		catch (SessionCacheUpdateException e)
		{
			this.logger.error("Error adding Principal {} to Session cache for session ID {}.", data.getPrincipalName(), data.getSessionID());
			this.logger.debug(e.fillInStackTrace().toString());
			
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

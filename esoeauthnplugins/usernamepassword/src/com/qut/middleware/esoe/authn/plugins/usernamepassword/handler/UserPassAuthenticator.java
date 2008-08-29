package com.qut.middleware.esoe.authn.plugins.usernamepassword.handler;

import com.qut.middleware.esoe.authn.pipeline.Authenticator;


public interface UserPassAuthenticator extends Authenticator
{
	
	/**
	 * Attempts to authenticate the principal based on userIdentifier and provided password.
	 * 
	 * @param userIdentifier Representation of the user which for which they are known on the backend authentication system
	 * @param password Users password value
	 * @return Result of the authentication call to backend auth provider
	 */
	public result authenticate(String userIdentifier, String password);
}

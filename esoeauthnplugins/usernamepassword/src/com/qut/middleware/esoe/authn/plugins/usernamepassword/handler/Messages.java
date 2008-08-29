package com.qut.middleware.esoe.authn.plugins.usernamepassword.handler;


import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages
{
	private static final String BUNDLE_NAME = "com.qut.middleware.esoe.authn.plugins.usernamepassword.handler.messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private Messages()
	{
		// Not implemented
	}

	/*
	 * @param key The key to use for locating the String
	 * @return The externalized String value
	 */
	public static String getString(String key)
	{
		try
		{
			return RESOURCE_BUNDLE.getString(key);
		}
		catch (MissingResourceException e)
		{
			return '!' + key + '!';
		}
	}
}

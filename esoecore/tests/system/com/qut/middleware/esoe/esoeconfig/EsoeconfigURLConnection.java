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
package com.qut.middleware.esoe.esoeconfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URL;
import java.net.URLConnection;

import com.qut.middleware.esoe.CompleteESOETest;
import com.qut.middleware.esoe.ConfigurationConstants;

/**
 * @author Shaun
 *
 */
public class EsoeconfigURLConnection extends URLConnection
{
	private String content;
	
	protected EsoeconfigURLConnection(URL url)
	{
		super(url);
		
		StringBuilder contentBuilder = new StringBuilder();
		
		contentBuilder.append(ConfigurationConstants.AUTHN_DYNAMIC_URL_PARAM).append('=').append(CompleteESOETest.AUTHN_DYNAMIC_URL_PARAM).append('\n');
		contentBuilder.append(ConfigurationConstants.SESSION_TOKEN_NAME).append('=').append(CompleteESOETest.SESSION_TOKEN_NAME).append('\n');
		contentBuilder.append(ConfigurationConstants.COOKIE_SESSION_DOMAIN).append('=').append(CompleteESOETest.COOKIE_SESSION_DOMAIN).append('\n');
		contentBuilder.append(ConfigurationConstants.DISABLE_SSO_TOKEN_NAME).append('=').append(CompleteESOETest.DISABLE_SSO_TOKEN_NAME).append('\n');
		contentBuilder.append(ConfigurationConstants.AUTHN_REDIRECT_URL).append('=').append(CompleteESOETest.AUTHN_REDIRECT_URL).append('\n');
		contentBuilder.append(ConfigurationConstants.SSO_URL).append('=').append(CompleteESOETest.SSO_URL).append('\n');
		contentBuilder.append(ConfigurationConstants.LOGOUT_REDIRECT_URL).append('=').append(CompleteESOETest.LOGOUT_REDIRECT_URL).append('\n');
		contentBuilder.append(ConfigurationConstants.LOGOUT_RESPONSE_REDIRECT_URL).append('=').append(CompleteESOETest.LOGOUT_RESPONSE_REDIRECT_URL).append('\n');
		
		this.content = contentBuilder.toString();
	}
	
	

	@Override
	public void connect() throws IOException
	{
	}
	
	@Override
	public InputStream getInputStream() throws IOException
	{
		byte[] contentBytes = this.content.getBytes();
		PipedOutputStream out = new PipedOutputStream();
		PipedInputStream in = new PipedInputStream( out );
		
		out.write( contentBytes );
		out.close();
		
		return in;
	}

}

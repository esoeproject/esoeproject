/*
 * Copyright 2006, Queensland University of Technology Licensed under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Author: Shaun Mangelsdorf 
 * Creation Date: Sep 11, 2007
 * 
 * Purpose:
 */

package com.qut.middleware.spep.integrators.oracle;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import oracle.security.sso.ias904.toolkit.IPASAuthException;
import oracle.security.sso.ias904.toolkit.IPASAuthInterface;
import oracle.security.sso.ias904.toolkit.IPASInsufficientCredException;
import oracle.security.sso.ias904.toolkit.IPASUserInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integrator to allow Oracle SSO to authenticate using an SPEP.
 */
public class OracleSSOIntegrator implements IPASAuthInterface
{
	private static final String PROPERTIES_RESOURCE = "spep.oraclesso.properties";
	private static final String PROPERTY_SPEP_ATTRIBUTES_NAME = "spep.oraclesso.spepAttributesName";
	private static final String PROPERTY_SPEP_USER_IDENTIFIER_ATTRIBUTE = "spep.oraclesso.userIdentifierAttribute";
	
	private static final String DEFAULT_SPEP_ATTRIBUTES_NAME = "attributes";
	private static final String DEFAULT_USER_IDENTIFIER_ATTRIBUTE = "uid";
	
	private Logger logger = LoggerFactory.getLogger( this.getClass() );
	private String spepAttributesName;
	private String userIdentifierAttribute;
	
	public OracleSSOIntegrator()
	{
		Properties properties = new Properties();
		try {
			URL propertiesURL = this.getClass().getResource( PROPERTIES_RESOURCE );
			InputStream in = propertiesURL.openStream();
			
			properties.load( in );
			
			this.logger.debug( "Loaded properties file. Beginning configuration." );
		} catch (IOException e) {
			this.logger.error( "No resource named " + PROPERTIES_RESOURCE + " could be found. Falling back to default values." );
		}
		
		this.spepAttributesName = properties.getProperty( PROPERTY_SPEP_ATTRIBUTES_NAME, DEFAULT_SPEP_ATTRIBUTES_NAME );
		this.logger.debug( "Configured SPEP attributes name: " + this.spepAttributesName );
		this.userIdentifierAttribute = properties.getProperty( PROPERTY_SPEP_USER_IDENTIFIER_ATTRIBUTE, DEFAULT_USER_IDENTIFIER_ATTRIBUTE );
		this.logger.debug( "Configured User identifier attribute: " + this.userIdentifierAttribute );
	}

	/**
	 * This method handles the passing of the username from the session to Oracle SSO.
	 * Overrides:
	 * 
	 * @see oracle.security.sso.ias904.toolkit.IPASAuthInterface#authenticate(javax.servlet.http.HttpServletRequest)
	 * @param request The HTTP request made to the servlet
	 * @return The username in an IPASUserInfo object
	 */
	public IPASUserInfo authenticate( HttpServletRequest request ) throws IPASAuthException, IPASInsufficientCredException
	{
		this.logger.debug( "About to grab SPEP attributes object from session." );
		
		// Get the attribute map out of the session.
		HttpSession session = request.getSession();
		Object attributeMapObject = session.getAttribute( spepAttributesName );
		
		// Ensure we have a map before casting it.
		if( attributeMapObject instanceof HashMap )
		{
			this.logger.debug( "Got attributes hashmap. Grabbing username attribute values." );
			HashMap attributeMap = (HashMap)session.getAttribute( spepAttributesName );
			
			if( attributeMap != null )
			{
				// Get the value of the username attribute
				Object uidListObject = attributeMap.get( userIdentifierAttribute );
				
				if( uidListObject != null && uidListObject instanceof List )
				{
					List uidList = (List)uidListObject;
					
					Object uidObject = uidList.get(0);
					if( uidObject != null && uidObject instanceof String )
					{
						String uid = (String)uidObject;
						
						this.logger.debug( "Got uid for session: " + uid );
						
						// Success, we have a uid. Return it to Oracle SSO.
						return new IPASUserInfo( uid );
					}
				}
			}
		}
		else
		{
			this.logger.error( "No attributes found in session. Investigate potential problem with SPEP filter." );
		}

		// If something fails we will fall through to here
		throw new IPASInsufficientCredException( "No SPEP attribute map was found, or the attribute called '" + userIdentifierAttribute + "' could not be found in the map." );
	}

	/**
	 * Returns an error/login page Overrides:
	 * 
	 * @see oracle.security.sso.ias904.toolkit.IPASAuthInterface#getUserCredentialPage(javax.servlet.http.HttpServletRequest,
	 *      java.lang.String)
	 * @param request
	 * @param msg
	 * @return
	 */
	public URL getUserCredentialPage( HttpServletRequest request, String msg )
	{
		return null;
	}
}

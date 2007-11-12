/*
 * Copyright 2007, Queensland University of Technology Licensed under the Apache
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
 * Creation Date: Oct 4, 2007
 * 
 * Purpose:
 */
package com.qut.middleware.spep.filter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

import com.qut.middleware.spep.ConfigurationConstants;
import com.qut.middleware.spep.SPEPProxy;
import com.qut.middleware.spep.filter.exception.SPEPInitializationException;
import com.qut.middleware.spep.filter.proxy.GenericObjectInvocationHandler;

/**
 * @class Initializer
 */
public class Initializer {
	
	/* Local logging instance */
	static private Logger logger = Logger.getLogger(Initializer.class.getName());
	
	/**
	 * @param context The servlet context in which to initialize a SPEP
	 * @return The SPEP for the given servlet context.
	 * @throws SPEPInitializationException 
	 */
	public static synchronized SPEPProxy init(ServletContext context) throws SPEPInitializationException
	{
		if( context == null )
		{
			Initializer.logger.error( "Null servlet context passed to filter Initializer. Couldn't get SPEP instance." );
			throw new SPEPInitializationException( "SPEP couldn't be initialized. No servlet context was given." );
		}
		
		Object spepObject = context.getAttribute(ConfigurationConstants.SPEP_PROXY);
		
		if(spepObject == null)
		{
			Initializer.logger.error( "No SPEP has been instantiated in the given servlet context." );
			throw new SPEPInitializationException( "SPEP couldn't be initialized. No SPEP in this servlet context (yet?)." );
		}
				
		Initializer.logger.debug( "Got SPEP object. Class is: " + spepObject.getClass().getName() + ". Creating proxy." );
		
		Class<?>[] spepInterfaces = { SPEPProxy.class };
		InvocationHandler spepInvocationHandler = new GenericObjectInvocationHandler( spepObject );
		SPEPProxy spep = (SPEPProxy)Proxy.newProxyInstance( Initializer.class.getClassLoader(), spepInterfaces, spepInvocationHandler );
		
		return spep;
	}
	
}

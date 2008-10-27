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
 * Creation Date: 30/10/2006
 * 
 * Purpose: Holds constant values for the various modules in the SPEP that won't 
 * 		reasonably need to be changed at any client site, all fields must have 
 * 		associated comments indicating where they are used
 */
package com.qut.middleware.spep;

import java.io.File;

/** Holds constant values for the various modules in the SPEP that won't 
 * 		reasonably need to be changed at any client site, all fields must have 
 * 		associated comments indicating where they are used. */
public class ConfigurationConstants
{
	/* Loggers */
	public static final String authnLogger = "spep.authn";
	public static final String authzLogger = "spep.authz";
	
	/** Timezone in use for the SPEP */
	public static final String timeZone = "UTC"; //$NON-NLS-1$
	
	/** SPEP Path Java Property */
	public static final String SPEP_PATH_PROP = "spep.data";
	
	/** SPEP local config filename */
	public static final String SPEP_CONFIG_LOCAL = File.separatorChar + "WEB-INF" + File.separatorChar + "spepvars.config"; //$NON-NLS-1$
	/** SPEP config filename */
	public static final String SPEP_CONFIG = File.separatorChar + "config" + File.separatorChar + "spep.config"; //$NON-NLS-1$
	/** SPEP compile time resource bundle */
	public static final String SPEP_COMPILE_TIME = "com.qut.middleware.spep.compile"; //$NON-NLS-1$
	/** SPEP web service name as defined in services.xml for axis2 */
	public static final String SPEP_WEBSERVICE_NAME = "spep"; //$NON-NLS-1$
	/** Name of the attribute in the servlet context that contains the SPEP object */
	public static final String SERVLET_CONTEXT_NAME = "com.qut.middleware.spep.instance"; //$NON-NLS-1$
	/** Name of the attribute in the servlet context that contains the SPEPProxy object */
	public static final String SPEP_PROXY = "com.qut.middleware.spep.proxy.instance"; //$NON-NLS-1$
}

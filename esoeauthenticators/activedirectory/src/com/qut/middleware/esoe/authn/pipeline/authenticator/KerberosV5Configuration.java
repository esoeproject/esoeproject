/* Copyright 2006, Queensland University of Technology
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
 * Creation Date: 18/1/2007
 * 
 * Purpose: Configuration object for use with the Kerberos Login Module @see 
 * com.sun.security.auth.module.Krb5LoginModule. This object must be passed to 
 * the KerberosV5Authenticator with appropriate options in order to authenticate
 * the server principal to the kerberos AS.
 */

package com.qut.middleware.esoe.authn.pipeline.authenticator;


import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

import org.apache.log4j.Logger;


public class KerberosV5Configuration extends Configuration
{
	/* Local logging instance */
	Logger logger = Logger.getLogger(this.getClass().getName());
	
	private Vector<AppConfigurationEntry> appConfigs;
	private String loginModuleName = "com.sun.security.auth.module.Krb5LoginModule"; //$NON-NLS-1$
	
	AppConfigurationEntry.LoginModuleControlFlag flag = AppConfigurationEntry.LoginModuleControlFlag.REQUIRED;
	
	/** Default constructor. Creates the authentication configuration entry with the given options.
	 * 
	 * @param options A map of string options to provide to the authenticator. These are general options as 
	 * specified in java security login modules. Do not include filename options with these, use the files
	 * param instead. 
	 * @param files A map of optionKey = File parameters. The filepath will be extracted from the file object
	 * and added to the configuration options under the given key. For example <keyTab, KeytabFile>. This 
	 * method is used so that local file paths can be used in the file object, rather than providing full
	 * path strings in options.
	 * 
	 */
	public KerberosV5Configuration(Map<String, String> options, Map<String, File> files)
	{		
		this.appConfigs = new Vector<AppConfigurationEntry>();
		
		if (options == null)
		{
			this.logger.fatal(Messages.getString("KerberosV5Configuration.0")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("KerberosV5Configuration.0"));  //$NON-NLS-1$
		}
			
		if(files == null)
			throw new IllegalArgumentException(Messages.getString("KerberosV5Configuration.1")); //$NON-NLS-1$
		
		// process and add file options to option map		
		Set<String> fileKeys = files.keySet();
		Iterator<String> filesIter = fileKeys.iterator();
		while(filesIter.hasNext())
		{
			String key = filesIter.next();
			options.put(key, files.get(key).getAbsolutePath() );
		}
		
		this.appConfigs.add(new AppConfigurationEntry(this.loginModuleName, this.flag, options));
	}
	
			
	/* (non-Javadoc)
	 * @see javax.security.auth.login.Configuration#getAppConfigurationEntry(java.lang.String)
	 * 
	 * NOTE: Our implementation requirements are only for one kerberos configuration, therefore
	 * calling this method will only ever return at most one Entry.
	 */
	@Override
	public AppConfigurationEntry[] getAppConfigurationEntry(String name)
	{
		return this.appConfigs.toArray(new AppConfigurationEntry[]{});
	}

		
	}
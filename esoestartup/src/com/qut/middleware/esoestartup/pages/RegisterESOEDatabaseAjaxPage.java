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
 * Creation Date: 1/5/07
 */
package com.qut.middleware.esoestartup.pages;

import net.sf.click.Page;

public class RegisterESOEDatabaseAjaxPage extends Page
{
	public String driver;
	public String driverTemplate;
	
	private String defaultMySQL;
	private String defaultOracle;
	
	public void onInit()
	{
		
	}
	
	public void onGet()
	{
		if (driver == null)
			return;
		
		if(driver.equals(PageConstants.DATA_REPOSITORY_DRIVER_MYSQL))
			driverTemplate = defaultMySQL;
		
		if(driver.equals(PageConstants.DATA_REPOSITORY_DRIVER_ORACLE))
			driverTemplate = defaultOracle;
	}
	
	@Override
	public String getContentType()
	{
		return "text/xml";
	}
	
	@Override
	public String getTemplate()
	{
		return "ajax/ajax-template.htm";
	}

	public String getDefaultMySQL()
	{
		return defaultMySQL;
	}

	public void setDefaultMySQL(String defaultMySQL)
	{
		this.defaultMySQL = defaultMySQL;
	}

	public String getDefaultOracle()
	{
		return defaultOracle;
	}

	public void setDefaultOracle(String defaultOracle)
	{
		this.defaultOracle = defaultOracle;
	}
}

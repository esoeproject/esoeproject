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
package com.qut.middleware.delegator.shib.pages;

import java.util.List;

import org.apache.log4j.Logger;

import com.qut.middleware.delegator.shib.ConfigurationConstants;
import com.qut.middleware.saml2.schemas.assertion.AttributeType;


public class AcceptPage extends BorderPage
{
	public List<AttributeType> attributes;
	
	/* Local logging instance */
	private Logger logger = Logger.getLogger(AcceptPage.class.getName());
	
	public AcceptPage()
	{
	}
	
	@Override
	public void onInit()
	{
	}

	@Override
	public void onGet()
	{
		super.onGet();
		
		attributes = (List<AttributeType>)this.retrieveSession(ConfigurationConstants.RELEASED_ATTRIBUTES_SESSION_IDENTIFIER);
	}
	
}

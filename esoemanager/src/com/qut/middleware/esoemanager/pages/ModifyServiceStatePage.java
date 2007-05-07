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
package com.qut.middleware.esoemanager.pages;

import org.apache.log4j.Logger;

import com.qut.middleware.esoemanager.exception.ModifyServiceStateException;
import com.qut.middleware.esoemanager.logic.ModifyServiceStateLogic;

public class ModifyServiceStatePage extends BorderPage
{
	ModifyServiceStateLogic logic;

	public String serviceID;
	public String state;
	public String confirm;

	/* Local logging instance */
	private Logger logger = Logger.getLogger(ModifyServiceStatePage.class.getName());

	public ModifyServiceStatePage()
	{
		this.serviceID = null;
		this.state = null;
		this.confirm = null;
	}
	
	public ModifyServiceStateLogic getModifyServiceStateLogic()
	{
		return this.logic;
	}
	
	public void setModifyServiceStateLogic(ModifyServiceStateLogic logic)
	{
		this.logic = logic;
	}
	
	@Override
	public void onGet()
	{
		if(this.serviceID != null && this.state != null && ( this.state.equals(PageConstants.SERVICE_ACTIVE) || this.state.equals(PageConstants.SERVICE_INACTIVE)))
		{
			/* Determine if the user has confirmed this action */
			if(this.confirm != null && this.confirm.equals(PageConstants.CONFIRMED))
			{
				if(this.state.equals(PageConstants.SERVICE_ACTIVE))
				{
					try
					{
						this.logic.setActive(this.serviceID);
					}
					catch (ModifyServiceStateException e)
					{
						this.logger.error("Unable to make service active" + e.getLocalizedMessage());
						this.logger.debug(e);
						
						// TODO: Redirect to error page
					}
				}
				else
				{
					try
					{
						this.logic.setInActive(serviceID);
					}
					catch (ModifyServiceStateException e)
					{
						this.logger.error("Unable to make service active" + e.getLocalizedMessage());
						this.logger.debug(e);
						
						// TODO: Redirect to error page
					}
				}
			}
		}
		else
		{
			// TODO: Redirect to error page
		}
	}
}

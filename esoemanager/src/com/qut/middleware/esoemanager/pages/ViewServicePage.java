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

import net.sf.click.control.TextArea;
import net.sf.click.util.ClickUtils;

import com.qut.middleware.esoemanager.bean.ServiceBean;
import com.qut.middleware.esoemanager.exception.ViewServiceException;
import com.qut.middleware.esoemanager.logic.ViewServiceLogic;

public class ViewServicePage extends BorderPage
{
	/* entityID is populated from name/value pair in the request */
	public String entityID;
	
	public ServiceBean serviceDetails;
	public String samlDescriptor;
	public TextArea saml;
	
	private ViewServiceLogic logic;
	private ClickUtils util;
	
	public ViewServicePage()
	{
		this.util = new ClickUtils();
	}
	
	@Override
	public void onInit()
	{
		
		this.saml = new TextArea(PageConstants.SAML_DESCRIPTOR_XML);
		
		if(this.entityID != null)
		{
			try
			{
				this.serviceDetails = this.logic.execute(entityID);
				this.samlDescriptor = this.util.escapeHtml(this.serviceDetails.getDescriptorXML());
			}
			catch (ViewServiceException e)
			{
				this.serviceDetails = null;
				this.samlDescriptor = null;
			}
		}
		else
		{
			//TODO: Redirect to error page
		}
	}
	
	public ViewServiceLogic getViewServiceLogic()
	{
		return this.logic;
	}
	
	public void setViewServiceLogic(ViewServiceLogic logic)
	{
		this.logic = logic;
	}
}

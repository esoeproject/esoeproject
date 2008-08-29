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

import java.io.UnsupportedEncodingException;

import net.sf.click.control.Submit;

import com.qut.middleware.esoemanager.exception.AttributePolicyException;
import com.qut.middleware.esoemanager.exception.ServiceAuthorizationPolicyException;
import com.qut.middleware.esoemanager.logic.ConfigureAttributePolicyLogic;
import com.qut.middleware.esoemanager.pages.forms.impl.AttributeForm;

public class ConfigureAttributePolicyPage extends BorderPage
{
	ConfigureAttributePolicyLogic logic;

	public AttributeForm attributeForm;
	public boolean validPolicy;
	public boolean editComplete;
	public String errorMessage;

	public ConfigureAttributePolicyPage()
	{
		this.validPolicy = true;
		this.editComplete = false;
		this.attributeForm = new AttributeForm();
	}

	@Override
	public void onInit()
	{
		this.attributeForm.init();
		Submit submitPolicy = new Submit(PageConstants.NAV_COMPLETE_LABEL, this, PageConstants.NAV_COMPLETE_FUNC);
		this.attributeForm.add(submitPolicy);
	}

	@Override
	public void onGet()
	{
		try
		{
			byte[] rawData = this.logic.getActiveAttributePolicy();
			if (rawData != null)
			{
				String activePolicy = new String(rawData, "UTF-16");
				this.attributeForm.getField(PageConstants.ATTRIBUTE_POLICY).setValue(activePolicy);
			}
		}
		catch (AttributePolicyException e)
		{
			// TODO: Error page
		}
		catch (UnsupportedEncodingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean completeClick()
	{
		if (this.attributeForm.isValid())
		{
			try
			{
				this.logic.updateAttributePolicy(this.attributeForm.getFieldValue(PageConstants.ATTRIBUTE_POLICY).getBytes("UTF-16"));
			}
			catch (AttributePolicyException e)
			{
				this.validPolicy = false;
				this.attributeForm.setError(e.getLocalizedMessage());
				return true;
			}
			catch (UnsupportedEncodingException e)
			{
				this.validPolicy = false;
				this.attributeForm.setError(e.getLocalizedMessage());
				return true;
			}

			this.editComplete = true;
			return false;
		}

		return true;
	}

	public ConfigureAttributePolicyLogic getConfigureAttributePolicyLogic()
	{
		return this.logic;
	}

	public void setConfigureAttributePolicyLogic(ConfigureAttributePolicyLogic logic)
	{
		this.logic = logic;
	}
}

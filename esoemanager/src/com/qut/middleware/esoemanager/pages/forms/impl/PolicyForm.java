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
package com.qut.middleware.esoemanager.pages.forms.impl;

import net.sf.click.control.Form;
import net.sf.click.control.HiddenField;
import net.sf.click.control.TextArea;

import com.qut.middleware.esoemanager.pages.PageConstants;
import com.qut.middleware.esoemanager.pages.forms.BaseForm;

public class PolicyForm extends Form implements BaseForm
{
	public void init()
	{
		TextArea lxacmlPolicy = new TextArea(PageConstants.LXACML_POLICY, true);
		lxacmlPolicy.setCols(125);
		lxacmlPolicy.setRows(40);
		lxacmlPolicy.setLabel("");
		
		HiddenField serviceID = new HiddenField(PageConstants.ENTITYID, String.class);
		HiddenField descriptorID = new HiddenField(PageConstants.DESCRIPTORID, String.class);

		this.add(serviceID);
		this.add(descriptorID);
		this.add(lxacmlPolicy);
	}
}

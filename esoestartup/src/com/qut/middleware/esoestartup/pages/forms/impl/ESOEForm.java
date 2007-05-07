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
package com.qut.middleware.esoestartup.pages.forms.impl;

import net.sf.click.control.Form;
import net.sf.click.control.Label;
import net.sf.click.control.TextField;

import com.qut.middleware.esoemanager.pages.forms.BaseForm;
import com.qut.middleware.esoestartup.pages.PageConstants;

public class ESOEForm extends Form implements BaseForm
{
	private static final long serialVersionUID = 684245328592342498L;

	public void init()
	{
		this.setValidate(true);
		this.setErrorsPosition(Form.POSITION_TOP);

		TextField esoeNodeURL = new TextField(PageConstants.ESOE_NODE_URL, true);
		esoeNodeURL.setSize(PageConstants.URL_FIELD_WIDTH);
		esoeNodeURL.setValue(PageConstants.DEFAULT_PROTOCOL);
		esoeNodeURL.setLabel("ESOE Service URL <a href=\"help.htm#esoe\" target=\"_blank\">?</a>");
		
		TextField esoeSingleSignOnService = new TextField(PageConstants.ESOE_SINGLE_SIGN_ON_SERVICE, true);
		esoeSingleSignOnService.setSize(PageConstants.URL_FIELD_WIDTH);
		esoeSingleSignOnService.setLabel("Single Sign On Service <a href=\"help.htm#esoe\" target=\"_blank\">?</a>");
		
		TextField esoeAttributeService = new TextField(PageConstants.ESOE_ATTRIBUTE_SERVICE, true);
		esoeAttributeService.setSize(PageConstants.URL_FIELD_WIDTH);
		esoeAttributeService.setLabel("Attribute Service <a href=\"help.htm#esoe\" target=\"_blank\">?</a>");
		
		TextField esoeLXACMLService = new TextField(PageConstants.ESOE_LXACML_SERVICE, true);
		esoeLXACMLService.setSize(PageConstants.URL_FIELD_WIDTH);
		esoeLXACMLService.setLabel("LXACML Service <a href=\"help.htm#esoe\" target=\"_blank\">?</a>");
		
		TextField esoeSPEPStartupService = new TextField(PageConstants.ESOE_SPEP_STARTUP_SERVICE, true);
		esoeSPEPStartupService.setLabel("Startup Service <a href=\"help.htm#esoe\" target=\"_blank\">?</a>");
		esoeSPEPStartupService.setSize(PageConstants.URL_FIELD_WIDTH);
		
		TextField organizationDisplayName = new TextField(PageConstants.ESOE_ORGANIZATION_DISPLAY_NAME, true);
		organizationDisplayName.setLabel("Organization Display Name");
		TextField organizationName = new TextField(PageConstants.ESOE_ORGANIZATION_NAME, true);
		organizationName.setLabel("Organization Name");
		TextField organizationURL = new TextField(PageConstants.ESOE_ORGANIZATION_URL, true);
		organizationURL.setLabel("Organization URL");

		this.add(new Label("org", "Organization details for ESOE deployer"));
		this.add(organizationName);
		this.add(organizationDisplayName);
		this.add(organizationURL);
		this.add(new Label("empty", "<br/>"));
		this.add(new Label("deployment", "Server details for ESOE deployment"));
		this.add(esoeNodeURL);
		this.add(esoeSingleSignOnService);
		this.add(esoeAttributeService);
		this.add(esoeLXACMLService);
		this.add(esoeSPEPStartupService);
	}

}

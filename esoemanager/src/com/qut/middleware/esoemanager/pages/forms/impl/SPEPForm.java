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
import net.sf.click.control.Radio;
import net.sf.click.control.RadioGroup;
import net.sf.click.control.Submit;
import net.sf.click.control.TextField;

import com.qut.middleware.esoemanager.pages.PageConstants;
import com.qut.middleware.esoemanager.pages.forms.BaseForm;

public class SPEPForm extends Form implements BaseForm
{
	private static final long serialVersionUID = 8283314595038519364L;

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoemanager.pages.forms.BaseForm#init()
	 */
	public void init()
	{
		/* Setup SPEP Node details form */
		this.setColumns(1);
		this.setValidate(true);
		this.setErrorsPosition(Form.POSITION_TOP);
		
		RadioGroup radioGroup = new RadioGroup(PageConstants.SERVICE_TYPE);
		Radio java = new Radio(PageConstants.SERVICE_TYPE_JAVA, PageConstants.SERVICE_TYPE_JAVA);
		java.setAttribute("onfocus", "changeServiceEnds('Java');");
		
		Radio apache = new Radio(PageConstants.SERVICE_TYPE_APACHE, PageConstants.SERVICE_TYPE_APACHE);
		apache.setAttribute("onfocus", "changeServiceEnds('Apache');");
		
		Radio iis = new Radio(PageConstants.SERVICE_TYPE_IIS, PageConstants.SERVICE_TYPE_IIS);
		iis.setAttribute("onfocus", "changeServiceEnds('IIS');");
		
		radioGroup.add(java);
        radioGroup.add(apache);
        radioGroup.add(iis);
        radioGroup.setValue(PageConstants.SERVICE_TYPE_JAVA);
        radioGroup.setVerticalLayout(false);
		
		TextField spepNodeURL = new TextField(PageConstants.SPEP_NODE_URL, true);
		spepNodeURL.setMinLength(8);
		spepNodeURL.setSize(PageConstants.URL_FIELD_WIDTH);
		spepNodeURL.setLabel("SPEP Node URL <a href=\"help.htm#servicenodes\" target=\"_blank\">?</a>");
		
		
		TextField singleLogoutService = new TextField(PageConstants.SINGLE_LOGOUT_SERVICE, true);
		singleLogoutService.setLabel("Single Logout Service <a href=\"help.htm#servicenodes\" target=\"_blank\">?</a>");
		singleLogoutService.setSize(PageConstants.URL_FIELD_WIDTH);
		
		TextField assertionConsumerService = new TextField(PageConstants.ASSERTION_CONSUMER_SERVICE, true);
		assertionConsumerService.setSize(PageConstants.URL_FIELD_WIDTH);
		assertionConsumerService.setLabel("Assertion Consumer Service <a href=\"help.htm#servicenodes\" target=\"_blank\">?</a>");
				
		TextField cacheClearService = new TextField(PageConstants.CACHE_CLEAR_SERVICE, true);
		cacheClearService.setSize(PageConstants.URL_FIELD_WIDTH);
		cacheClearService.setLabel("Cache Clear Service <a href=\"help.htm#servicenodes\" target=\"_blank\">?</a>");
				
		Submit submitNode = new Submit(PageConstants.SAVE_NODE, PageConstants.SAVE_NODE);
		
		this.add(radioGroup);
		this.add(spepNodeURL);
		this.add(assertionConsumerService);
		this.add(singleLogoutService);
		this.add(cacheClearService);
		this.add(submitNode);
	}

}

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
import net.sf.click.control.TextArea;
import net.sf.click.control.TextField;

import com.qut.middleware.esoemanager.pages.PageConstants;
import com.qut.middleware.esoemanager.pages.forms.BaseForm;

public class ServiceForm extends Form implements BaseForm
{
	private static final long serialVersionUID = 6316798124524412315L;

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoemanager.pages.forms.BaseForm#init()
	 */
	public void init()
	{
		/* Setup service details form */
		this.setColumns(1);
		this.setValidate(true);
		this.setErrorsPosition(Form.POSITION_TOP);
	
		TextField serviceName = new TextField(PageConstants.SERVICE_NAME, true);
		TextField serviceURL = new TextField(PageConstants.SERVICE_URL, true);
		serviceURL.setLabel("Service URL <a href=\"help.htm#servicedetails\" target=\"_blank\">?</a>");
		serviceURL.setSize(PageConstants.URL_FIELD_WIDTH);
		
		TextArea serviceDescription = new TextArea(PageConstants.SERVICE_DESCRIPTION, true);
		serviceDescription.setCols(100);
		serviceDescription.setRows(6);
		
		TextArea serviceAuthzFailureMessage = new TextArea(PageConstants.SERVICE_AUTHZ_FAILURE_MESSAGE, true);
		serviceAuthzFailureMessage.setCols(100);
		serviceAuthzFailureMessage.setRows(6);
		serviceAuthzFailureMessage.setLabel("Service Authorization Failure <a href=\"help.htm#servicedetails\" target=\"_blank\">?</a>");
		
		serviceURL.setValue(PageConstants.DEFAULT_PROTOCOL);
		
		this.add(serviceName);
		this.add(serviceURL);
		this.add(serviceDescription);
		this.add(serviceAuthzFailureMessage);
	}

}

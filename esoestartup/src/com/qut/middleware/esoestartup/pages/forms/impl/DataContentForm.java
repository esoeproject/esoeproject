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
import net.sf.click.control.PasswordField;
import net.sf.click.control.Select;
import net.sf.click.control.TextField;

import com.qut.middleware.esoemanager.pages.forms.BaseForm;
import com.qut.middleware.esoestartup.pages.PageConstants;

public class DataContentForm extends Form implements BaseForm
{
	private static final long serialVersionUID = -624708254886157937L;

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoemanager.pages.forms.BaseForm#init()
	 */
	public void init()
	{
		this.setValidate(true);
		this.setErrorsPosition(Form.POSITION_TOP);
		
		TextField esoeData = new TextField(PageConstants.ESOE_CONTENT_DATA, true);
		esoeData.setSize(PageConstants.URL_FIELD_WIDTH);
		esoeData.setLabel("ESOE Data Directory <a href=\"help.htm#esoedata\" target=\"_blank\">?</a>");
		
		TextField esoemanagerData = new TextField(PageConstants.ESOEMANAGER_CONTENT_DATA, true);
		esoemanagerData.setSize(PageConstants.URL_FIELD_WIDTH);
		esoemanagerData.setLabel("ESOE Manager Data Directory <a href=\"help.htm#esoedata\" target=\"_blank\">?</a>");
		
		TextField spepData = new TextField(PageConstants.SPEP_CONTENT_DATA, true);
		spepData.setSize(PageConstants.URL_FIELD_WIDTH);
		spepData.setLabel("ESOE Data Directory <a href=\"help.htm#esoedata\" target=\"_blank\">?</a>");
	
		
		this.add(esoeData);
		this.add(esoemanagerData);
		this.add(spepData);
	}

}

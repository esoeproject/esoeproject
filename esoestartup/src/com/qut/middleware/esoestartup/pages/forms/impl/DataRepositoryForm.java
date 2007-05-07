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

public class DataRepositoryForm extends Form implements BaseForm
{
	private static final long serialVersionUID = 6419948319761863630L;

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoemanager.pages.forms.BaseForm#init()
	 */
	public void init()
	{
		this.setValidate(true);
		this.setErrorsPosition(Form.POSITION_TOP);
		
		Select driverType = new Select();
		driverType.add(PageConstants.DATA_REPOSITORY_DRIVER_MYSQL);
		driverType.add(PageConstants.DATA_REPOSITORY_DRIVER_ORACLE);
		driverType.setName(PageConstants.DATA_REPOSITORY_DRIVER);
		
		TextField repositoryURL = new TextField(PageConstants.DATA_REPOSITORY_URL, true);
		repositoryURL.setSize(PageConstants.URL_FIELD_WIDTH);
		repositoryURL.setLabel("Database Connection String <a href=\"help.htm#database\" target=\"_blank\">?</a>");
		
		TextField repositoryUser = new TextField(PageConstants.DATA_REPOSITORY_USERNAME, true);
		repositoryUser.setLabel("Database Username <a href=\"help.htm#database\" target=\"_blank\">?</a>");
		
		PasswordField repositoryPassword = new PasswordField(PageConstants.DATA_REPOSITORY_PASSWORD, true);
		repositoryPassword.setLabel("Database Password <a href=\"help.htm#database\" target=\"_blank\">?</a>");
		
		this.add(driverType);
		this.add(repositoryURL);
		this.add(repositoryUser);
		this.add(repositoryPassword);
	}

}

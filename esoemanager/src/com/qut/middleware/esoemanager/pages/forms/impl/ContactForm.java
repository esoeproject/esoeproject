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
import net.sf.click.control.Select;
import net.sf.click.control.Submit;
import net.sf.click.control.TextField;
import net.sf.click.extras.control.EmailField;
import net.sf.click.extras.control.TelephoneField;

import com.qut.middleware.esoemanager.pages.PageConstants;
import com.qut.middleware.esoemanager.pages.forms.BaseForm;

public class ContactForm extends Form implements BaseForm
{
	private static final long serialVersionUID = 6210530994393929256L;

	public void init()
	{
		/* Setup contact details form */
		this.setColumns(3);
		this.setValidate(true);
		this.setErrorsPosition(Form.POSITION_TOP);
		
		Select contactType = new Select();
		contactType.add(PageConstants.CONTACTTYPE_TECH);
		contactType.add(PageConstants.CONTACTTYPE_SUP);
		contactType.add(PageConstants.CONTACTTYPE_ADMIN);
		contactType.add(PageConstants.CONTACTTYPE_BILL);
		contactType.setName(PageConstants.CONTACTTYPE);
		
		HiddenField contactID = new HiddenField(PageConstants.CONTACTID, String.class);
		TextField company = new TextField(PageConstants.COMPANY, true);
		TextField givenName = new TextField(PageConstants.GIVENNAME, true);
		TextField surname = new TextField(PageConstants.SURNAME, true);
		surname.setLabel(PageConstants.SURNAME_LABEL);
		EmailField email = new EmailField(PageConstants.EMAILADDRESS, true);
		TextField telephone = new TelephoneField(PageConstants.TELEPHONENUMBER, true);
		telephone.setMinLength(4);
		
		Submit submitContact = new Submit(PageConstants.SAVE_CONTACT, PageConstants.SAVE_CONTACT);
		
		this.add(contactID);
		this.add(contactType);
		this.add(givenName);
		this.add(surname);
		this.add(email);
		this.add(telephone);
		this.add(company);
		this.add(submitContact);
	}
}

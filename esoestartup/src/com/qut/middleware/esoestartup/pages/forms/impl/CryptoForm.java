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
import net.sf.click.control.TextField;
import net.sf.click.extras.control.EmailField;

import com.qut.middleware.esoestartup.pages.PageConstants;

public class CryptoForm extends Form
{
	private static final long serialVersionUID = -499557841006446445L;
	
	public void init()
	{
		this.setValidate(true);
		this.setErrorsPosition(Form.POSITION_TOP);
		
		TextField issuerDN = new TextField(PageConstants.CRYPTO_ISSUER_DN, true);
		issuerDN.setSize(PageConstants.URL_FIELD_WIDTH);
		issuerDN.setLabel("Issuer DN <a href=\"help.htm#crypto\" target=\"_blank\">?</a>");

		EmailField issuerEmailAddress = new EmailField(PageConstants.CRYPTO_ISSUER_EMAIL, true);
		
		this.add(issuerDN);
		this.add(issuerEmailAddress);
	}
}

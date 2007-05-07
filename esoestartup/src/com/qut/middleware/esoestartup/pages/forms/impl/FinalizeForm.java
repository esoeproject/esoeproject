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

import com.qut.middleware.esoemanager.pages.forms.BaseForm;
import com.qut.middleware.esoestartup.pages.PageConstants;

public class FinalizeForm extends Form implements BaseForm
{

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoemanager.pages.forms.BaseForm#init()
	 */
	public void init()
	{
		TextField tomcatLocation = new TextField(PageConstants.TOMCAT_WEBAPPS_DIRECTORY, true);
		tomcatLocation.setSize(PageConstants.URL_FIELD_WIDTH);
		
		TextField diskLocation = new TextField(PageConstants.WRITEABLE_DIRECTORY, true);
		diskLocation.setSize(PageConstants.URL_FIELD_WIDTH);
		
		this.add(tomcatLocation);
		this.add(diskLocation);
	}
	
}

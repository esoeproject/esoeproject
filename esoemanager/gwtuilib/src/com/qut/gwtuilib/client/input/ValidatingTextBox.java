/* Copyright 2008, Queensland University of Technology
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
 */
package com.qut.gwtuilib.client.input;

import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.qut.gwtuilib.client.CSSConstants;

public class ValidatingTextBox extends ValidatingTextBoxBase
{
	private TextBox textBox;
	
	public ValidatingTextBox(int min, String regex, String errMsg, String areaId)
	{
		this.textBox = new TextBox();
		this.textBox.addStyleName(CSSConstants.validatingTextBox);
		super.init(min, regex, errMsg, areaId);
	}
	
	public ValidatingTextBox(int min, String regex, String errMsg, String areaId, String helpLink)
	{
		this.textBox = new TextBox();
		this.textBox.addStyleName(CSSConstants.validatingTextBox);
		super.init(min, regex, errMsg, areaId, helpLink);
	}
	
	@Override
	protected TextBoxBase getTextBoxBase()
	{
		return this.textBox;
	}

}

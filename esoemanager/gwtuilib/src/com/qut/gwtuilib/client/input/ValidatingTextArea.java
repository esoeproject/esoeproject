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

import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBoxBase;

public class ValidatingTextArea extends ValidatingTextBoxBase
{
	private TextArea textArea;
	
	public ValidatingTextArea(int min, String errMsg, String areaId, String helpLink)
	{
		this.textArea = new TextArea();
		super.init(min, null, errMsg, areaId, helpLink);
	}
	
	public ValidatingTextArea(int min, String errMsg, String areaId)
	{
		this.textArea = new TextArea();
		super.init(min, null, errMsg, areaId);
	}
	
	@Override
	protected TextBoxBase getTextBoxBase()
	{
		return this.textArea;
	}

}

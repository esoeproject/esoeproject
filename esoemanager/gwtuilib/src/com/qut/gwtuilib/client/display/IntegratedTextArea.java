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
package com.qut.gwtuilib.client.display;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.qut.gwtuilib.client.CSSConstants;
import com.qut.gwtuilib.client.exceptions.InvalidContentException;
import com.qut.gwtuilib.client.input.ValidatingTextArea;

public class IntegratedTextArea 
{	
	protected Label titleLbl;
	protected HorizontalPanel data;
	protected ValidatingTextArea validatingTextArea;
	
	private String areaID;
	private Object parent;

	public IntegratedTextArea(Object parent, int min, String errMsg, String title, String areaID)
	{
		this.parent = parent;
		this.areaID = areaID;
		createInterface(min, errMsg, title, null);
	}
	
	public IntegratedTextArea(Object parent, int min, String errMsg, String title, String areaID, String helpLink)
	{
		this.parent = parent;
		this.areaID = areaID;
		createInterface(min, errMsg, title, helpLink);
	}
	
	protected void createInterface(int min, String errMsg, String title, String helpLink)
	{
		this.data = new HorizontalPanel();
		
		this.titleLbl = new Label(title);
		this.titleLbl.addStyleName(CSSConstants.integratedTextBoxTitle);

		if(helpLink != null && helpLink.length() > 0)
			this.validatingTextArea = new ValidatingTextArea(min, errMsg, this.areaID, helpLink);
		else
			this.validatingTextArea = new ValidatingTextArea(min, errMsg, this.areaID);
		
		this.validatingTextArea.addStyleName(CSSConstants.integratedTextBoxValue);
		
		
		this.data.add(this.validatingTextArea);
	}

	public Label getTitle()
	{
		return this.titleLbl;
	}

	public Panel getContent()
	{
		return this.data;
	}

	public void setText(String text) throws InvalidContentException
	{
		this.validatingTextArea.setText(text);
	}

	public String getText() throws InvalidContentException
	{
		return this.validatingTextArea.getText();
	}

	public boolean isValid()
	{
		return validatingTextArea.isValid();
	}
	
	public void addTableRow(FlexibleTable table)
	{
		table.insertWidget(this.getTitle());
		table.insertWidget(this.getContent());
		table.nextRow();
	}

}

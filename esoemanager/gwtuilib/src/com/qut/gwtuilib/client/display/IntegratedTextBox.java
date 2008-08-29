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

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FocusListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.qut.gwtuilib.client.CSSConstants;
import com.qut.gwtuilib.client.exceptions.InvalidContentException;
import com.qut.gwtuilib.client.input.ValidatingTextBox;

public class IntegratedTextBox 
{	
	protected Label titleLbl;
	protected HorizontalPanel data;
	protected ValidatingTextBox validatingTextBox;
	
	private String areaID;
	Object parent;

	public IntegratedTextBox(Object parent, int min, String regex, String errMsg, String title, String areaID)
	{
		this.parent = parent;
		this.areaID = areaID;
		createInterface(min, regex, errMsg, title, null);
	}
	
	public IntegratedTextBox(Object parent, int min, String regex, String errMsg, String title, String areaID, String helpLink)
	{
		this.parent = parent;
		this.areaID = areaID;
		createInterface(min, regex, errMsg, title, helpLink);
	}
	
	protected void createInterface(int min, String regex, String errMsg, String title, String helpLink)
	{
		this.data = new HorizontalPanel();

		this.titleLbl = new Label(title);
		this.titleLbl.addStyleName(CSSConstants.integratedTextBoxTitle);

		if(helpLink != null && helpLink.length() > 0)
			this.validatingTextBox = new ValidatingTextBox(min, regex, errMsg, this.areaID, helpLink);
		else
			this.validatingTextBox = new ValidatingTextBox(min, regex, errMsg, this.areaID);
			
		this.validatingTextBox.addStyleName(CSSConstants.integratedTextBoxValue);
			
		this.data.add(this.validatingTextBox);
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
		this.validatingTextBox.setText(text);
	}

	public String getText() throws InvalidContentException
	{
		return this.validatingTextBox.getText();
	}

	public boolean isValid()
	{
		return validatingTextBox.isValid();
	}
	
	public void addTableRow(FlexibleTable table)
	{
		table.insertWidget(this.getTitle());
		table.insertWidget(this.getContent());
		table.nextRow();
	}

	/**
	 * @param listener
	 * @see com.qut.gwtuilib.client.input.ValidatingTextBoxBase#addChangeListener(com.google.gwt.user.client.ui.ChangeListener)
	 */
	public void addChangeListener(ChangeListener listener)
	{
		validatingTextBox.addChangeListener(listener);
	}

	/**
	 * @param listener
	 * @see com.qut.gwtuilib.client.input.ValidatingTextBoxBase#addClickListener(com.google.gwt.user.client.ui.ClickListener)
	 */
	public void addClickListener(ClickListener listener)
	{
		validatingTextBox.addClickListener(listener);
	}

	/**
	 * @param listener
	 * @see com.qut.gwtuilib.client.input.ValidatingTextBoxBase#addFocusListener(com.google.gwt.user.client.ui.FocusListener)
	 */
	public void addFocusListener(FocusListener listener)
	{
		validatingTextBox.addFocusListener(listener);
	}

	/**
	 * @param listener
	 * @see com.qut.gwtuilib.client.input.ValidatingTextBoxBase#addKeyboardListener(com.google.gwt.user.client.ui.KeyboardListener)
	 */
	public void addKeyboardListener(KeyboardListener listener)
	{
		validatingTextBox.addKeyboardListener(listener);
	}
	
	
}

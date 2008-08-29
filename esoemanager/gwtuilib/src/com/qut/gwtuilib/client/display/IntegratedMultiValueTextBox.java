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

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.qut.gwtuilib.client.CSSConstants;
import com.qut.gwtuilib.client.eventdriven.eventmgr.EventController;
import com.qut.gwtuilib.client.eventdriven.events.EventConstants;
import com.qut.gwtuilib.client.eventdriven.events.IntegratedMultiValueTextBoxEvent;
import com.qut.gwtuilib.client.exceptions.InvalidContentException;
import com.qut.gwtuilib.client.input.StyledButton;
import com.qut.gwtuilib.client.input.ValidatingTextBox;

public class IntegratedMultiValueTextBox
{
	protected Label titleLbl;
	protected VerticalPanel data;
	protected List<Value> values;
	
	private String areaID;
	private String title;
	private String helpLink;
	private String regex;
	private String errMsg;
	int min;
	
	Object parent;

	public IntegratedMultiValueTextBox(Object parent, int min, String regex, String errMsg, String title, String areaID)
	{
		this.parent = parent;
		this.min = min;
		this.regex = regex;
		this.errMsg = errMsg;
		this.title = title;
		this.areaID = areaID;
		createInterface();
	}
	
	public IntegratedMultiValueTextBox(Object parent, int min, String regex, String errMsg, String title, String areaID, String helpLink)
	{
		this.parent = parent;
		this.min = min;
		this.regex = regex;
		this.errMsg = errMsg;
		this.title = title;
		this.areaID = areaID;
		this.helpLink = helpLink;
		createInterface();
	}
	
	protected void createInterface()
	{
		this.values = new ArrayList<Value>();

		this.titleLbl = new Label(this.title);
		this.titleLbl.addStyleName(CSSConstants.integratedTextBoxTitle);
		this.data = new VerticalPanel();
		this.data.addStyleName(CSSConstants.multiValueTextBoxValues);
	}
	
	protected void refreshInterface()
	{
		this.data.clear();
		for(Value value : this.values)
		{
			if(this.values.size() == 1)
				value.disableRemove();
			else
				value.enableRemove();
			
			this.data.add(value);
		}
	}
	
	public int attributeCount()
	{
		return this.data.getWidgetCount();
	}
	
	public void addValue()
	{
		Value value = new Value();		
		refreshInterface();
		
		EventController.executeEvent(new IntegratedMultiValueTextBoxEvent(EventConstants.integratedMultiValueTextBoxValueAdded,
				IntegratedMultiValueTextBox.this.areaID, IntegratedMultiValueTextBox.this.parent));
	}
	
	public void addValue(String text) throws InvalidContentException
	{
		Value value = new Value();
		value.setText(text);
		refreshInterface();
	}

	public Label getTitle()
	{
		return this.titleLbl;
	}

	public Panel getContent()
	{
		return this.data;
	}

	public List<String> getValues() throws InvalidContentException
	{
		List<String> result = new ArrayList<String>();
		for(Value value : this.values)
		{
			String currentValue = value.getText();
			if(currentValue != null && currentValue.trim().length() > 0)
				result.add(currentValue);
		}
		
		return result;
	}

		
	public void addTableRow(FlexibleTable table)
	{
		table.insertWidget(this.getTitle());
		table.insertWidget(this.getContent());
		table.nextRow();
	}
	
	public void clear()
	{
		this.values.clear();
	}
	
	private class Value extends HorizontalPanel
	{
		StyledButton addValue;
		StyledButton removeValue;
		ValidatingTextBox validatingTextBox;
		
		public Value()
		{
			if(IntegratedMultiValueTextBox.this.helpLink != null && IntegratedMultiValueTextBox.this.helpLink.length() > 0)
				this.validatingTextBox = new ValidatingTextBox(IntegratedMultiValueTextBox.this.min, IntegratedMultiValueTextBox.this.regex, IntegratedMultiValueTextBox.this.errMsg, IntegratedMultiValueTextBox.this.areaID, IntegratedMultiValueTextBox.this.helpLink);
			else
				this.validatingTextBox = new ValidatingTextBox(IntegratedMultiValueTextBox.this.min, IntegratedMultiValueTextBox.this.regex, IntegratedMultiValueTextBox.this.errMsg, IntegratedMultiValueTextBox.this.areaID);
				
			this.validatingTextBox.addStyleName(CSSConstants.integratedTextBoxValue);
			
			this.addValue = new StyledButton("add", "");
			this.addValue.addClickListener(new ClickListener()
			{
				public void onClick(Widget sender)
				{
					addValue();					
				}
			});
			
			this.removeValue = new StyledButton("delete", "");
			this.removeValue.addClickListener(new ClickListener()
			{
				public void onClick(Widget sender)
				{
					IntegratedMultiValueTextBox.this.values.remove(Value.this);					
					IntegratedMultiValueTextBox.this.refreshInterface();
					
					EventController.executeEvent(new IntegratedMultiValueTextBoxEvent(EventConstants.integratedMultiValueTextBoxValueRemoved,
							IntegratedMultiValueTextBox.this.areaID, IntegratedMultiValueTextBox.this.parent));
				}
			});
			
			this.add(this.validatingTextBox);
			this.add(this.addValue);
			this.add(this.removeValue);
			
			IntegratedMultiValueTextBox.this.values.add(this);
		}
		
		public void disableRemove()
		{
			this.removeValue.setVisible(false);
		}
		
		public void enableRemove()
		{
			this.removeValue.setVisible(true);
		}
		
		public void setText(String text) throws InvalidContentException
		{
			this.validatingTextBox.setText(text);
		}
		
		public String getText() throws InvalidContentException
		{
			return this.validatingTextBox.getText();
		}
	}
}

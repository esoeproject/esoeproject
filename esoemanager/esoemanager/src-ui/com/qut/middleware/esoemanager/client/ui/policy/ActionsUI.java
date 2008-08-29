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
package com.qut.middleware.esoemanager.client.ui.policy;

import java.util.List;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.qut.gwtuilib.client.RegexConstants;
import com.qut.gwtuilib.client.display.FlexibleTable;
import com.qut.gwtuilib.client.display.IntegratedMultiValueTextBox;
import com.qut.gwtuilib.client.exceptions.InvalidContentException;
import com.qut.gwtuilib.client.input.StyledButton;
import com.qut.middleware.esoemanager.client.EsoeManagerConstants;

public class ActionsUI
{
	VerticalPanel content;
	FlexibleTable contentLayout;
	IntegratedMultiValueTextBox actions;
	
	StyledButton toggle;
	
	boolean actionSet;
	
	Label noActions; 
	Label setActions;
	
	public ActionsUI()
	{
		this.actionSet = false;
		this.content = new VerticalPanel();
		this.contentLayout = new FlexibleTable(2,2);
		
		this.toggle = new StyledButton("toggle", "");
		this.toggle.addClickListener(new ClickListener()
		{
			public void onClick(Widget sender)
			{
				actionSet = !actionSet;
				refreshInterface();
			}
		});
		
		this.actions = new IntegratedMultiValueTextBox(this, 1, RegexConstants.matchAll, "Action must be completed", "Action", EsoeManagerConstants.areaID);
		this.actions.getContent().setVisible(false);
		
		HorizontalPanel labels = new HorizontalPanel();
		this.noActions = new Label("This rule is invoked regardless of action being performed");
		this.setActions = new Label("This rule is invoked when one of the following actions are matched");
		this.setActions.setVisible(false);
		
		labels.add(this.noActions);
		labels.add(this.setActions);
		
		this.contentLayout.insertWidget(this.toggle);
		this.contentLayout.insertWidget(labels);
		this.contentLayout.nextRow();
		this.contentLayout.insertWidget(null);
		this.contentLayout.insertWidget(this.actions.getContent());
		
		this.content.add(contentLayout);
	}
	
	private void refreshInterface()
	{
		if(actionSet)
		{
			this.noActions.setVisible(false);
			this.setActions.setVisible(true);
			this.actions.getContent().setVisible(true);
			try
			{
				if(this.actions.getValues() == null || this.actions.getValues().size() == 0)
					this.actions.addValue();
			}
			catch (InvalidContentException e)
			{
			}
		}
		else
		{
			this.noActions.setVisible(true);
			this.setActions.setVisible(false);
			this.actions.getContent().setVisible(false);
		}
	}
	
	public VerticalPanel getContent()
	{
		return this.content;
	}
	
	public boolean actionsSet()
	{
		return this.actionSet;
	}
	
	public void enableActions()
	{
		this.actionSet = true;
		this.refreshInterface();
	}
	
	public void disableActions()
	{
		this.actionSet = false;
		this.refreshInterface();
	}

	public void addValue()
	{
		actions.addValue();
	}

	public void addValue(String text) throws InvalidContentException
	{
		actions.addValue(text);
	}

	public List<String> getValues() throws InvalidContentException
	{
		return actions.getValues();
	}		
}

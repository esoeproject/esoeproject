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

public class ResourcesUI
{
	VerticalPanel content;
	FlexibleTable contentLayout;
	IntegratedMultiValueTextBox resources;
	
	StyledButton toggle;
	
	boolean resourcesSet;
	
	Label noResources; 
	Label setResources;
	
	public ResourcesUI()
	{
		this.resourcesSet = false;
		this.content = new VerticalPanel();
		this.contentLayout = new FlexibleTable(2,2);
		
		this.toggle = new StyledButton("toggle", "");
		this.toggle.addClickListener(new ClickListener()
		{
			public void onClick(Widget sender)
			{
				ResourcesUI.this.resourcesSet = !ResourcesUI.this.resourcesSet;
				refreshInterface();
			}
		});
		
		this.resources = new IntegratedMultiValueTextBox(this, 1, RegexConstants.matchAll, "Resource must be completed", "Action", EsoeManagerConstants.areaID);
		this.resources.getContent().setVisible(false);
		
		HorizontalPanel labels = new HorizontalPanel();
		this.noResources = new Label("This rule is always evaluated");
		this.setResources = new Label("This rule is evaluated when the policy target is matched and one of the following resources are matched");
		this.setResources.setVisible(false);
		
		labels.add(this.noResources);
		labels.add(this.setResources);
		
		this.contentLayout.insertWidget(this.toggle);
		this.contentLayout.insertWidget(labels);
		this.contentLayout.nextRow();
		this.contentLayout.insertWidget(null);
		this.contentLayout.insertWidget(this.resources.getContent());
		
		this.content.add(contentLayout);
	}
	
	void refreshInterface()
	{
		if(resourcesSet)
		{
			this.noResources.setVisible(false);
			this.setResources.setVisible(true);
			this.resources.getContent().setVisible(true);
			try
			{
				if(this.resources.getValues() == null || this.resources.getValues().size() == 0)
					this.resources.addValue();
			}
			catch (InvalidContentException e)
			{
			}
		}
		else
		{
			this.noResources.setVisible(true);
			this.setResources.setVisible(false);
			this.resources.getContent().setVisible(false);
		}
	}
	
	public boolean resourcesSet()
	{
		return this.resourcesSet;
	}
	
	public VerticalPanel getContent()
	{
		return this.content;
	}
	
	public void enableResources()
	{
		this.resourcesSet = true;
		this.refreshInterface();
	}
	
	public void disableResources()
	{
		this.resourcesSet = false;
		this.refreshInterface();
	}

	public void addValue()
	{
		resources.addValue();
	}

	public void addValue(String text) throws InvalidContentException
	{
		resources.addValue(text);
	}

	public List<String> getValues() throws InvalidContentException
	{
		return resources.getValues();
	}		
}

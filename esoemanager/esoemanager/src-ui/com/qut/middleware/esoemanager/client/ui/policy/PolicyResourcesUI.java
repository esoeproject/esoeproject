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

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.qut.gwtuilib.client.RegexConstants;
import com.qut.gwtuilib.client.display.FlexibleTable;
import com.qut.gwtuilib.client.display.IntegratedMultiValueTextBox;
import com.qut.gwtuilib.client.exceptions.InvalidContentException;
import com.qut.middleware.esoemanager.client.EsoeManagerConstants;

public class PolicyResourcesUI
{
	VerticalPanel content;
	FlexibleTable contentLayout;
	IntegratedMultiValueTextBox resources;
	
	Label targetLbl;
	
	public PolicyResourcesUI()
	{
		this.content = new VerticalPanel();
		this.contentLayout = new FlexibleTable(2,2);
		
		this.resources = new IntegratedMultiValueTextBox(this, 1, RegexConstants.matchAll, "Target must be completed", "Action", EsoeManagerConstants.areaID);
		this.targetLbl = new Label("This policy is evaluated when one of the following resources are matched");
		
		this.contentLayout.insertWidget(this.targetLbl);
		this.contentLayout.nextRow();
		this.contentLayout.insertWidget(this.resources.getContent());
		
		this.content.add(this.contentLayout);
	}
	
	public VerticalPanel getContent()
	{
		return this.content;
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
	
	public void clear()
	{
		this.resources.clear();
	}
}

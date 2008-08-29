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
package com.qut.middleware.esoemanager.client.ui.policy.operators;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.qut.gwtuilib.client.eventdriven.eventmgr.EventController;
import com.qut.gwtuilib.client.eventdriven.events.MessageEvent;
import com.qut.gwtuilib.client.input.StyledButton;
import com.qut.middleware.esoemanager.client.CSSConstants;
import com.qut.middleware.esoemanager.client.EsoeManagerConstants;
import com.qut.middleware.esoemanager.client.events.EventConstants;

public class StringNormLowerCaseUI extends OperatorUI
{	
	private Label operatorTitleLbl;
	
	public StringNormLowerCaseUI(OperatorUI parent, boolean editMode)
	{
		super(parent, editMode, false);
	}
	
	public void createLocalInterface()
	{
		this.localContent.clear();
		
		this.addStyleName(CSSConstants.esoeManagerPolicyOperatorStringNormLowerCase);
		
		HorizontalPanel content = new HorizontalPanel();
		
		this.operatorTitleLbl = new Label("convert all characters to lower case before matching");
		this.operatorTitleLbl.addStyleName(CSSConstants.esoeManagerPolicyOperatorTitle);
		
		StyledButton delete = new StyledButton("delete", "");
		delete.addClickListener(new ClickListener()
		{
			public void onClick(Widget sender)
			{
				parent.deleteChild(StringNormLowerCaseUI.this);
			}
		});
		
		content.add(this.operatorTitleLbl);
		content.add(delete);
		
		this.localContent.add(content);
	}

	public void addChild(OperatorUI child, String childType)
	{
		EventController.executeEvent(new MessageEvent(EventConstants.userMessage, EsoeManagerConstants.areaID,
				MessageEvent.error, "The string lower case normalization operator does not have any children"));
	}

	public void deleteChild(OperatorUI child)
	{		
	}
}

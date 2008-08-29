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

import java.util.List;

import com.google.gwt.user.client.ui.Label;
import com.qut.gwtuilib.client.eventdriven.eventmgr.EventController;
import com.qut.gwtuilib.client.eventdriven.events.MessageEvent;
import com.qut.middleware.esoemanager.client.CSSConstants;
import com.qut.middleware.esoemanager.client.EsoeManagerConstants;
import com.qut.middleware.esoemanager.client.PolicyConstants;
import com.qut.middleware.esoemanager.client.events.EventConstants;
import com.qut.middleware.esoemanager.client.exceptions.PolicyComponentValidationException;

public class OrUI extends OperatorUI
{
	public OrUI(OperatorUI parent, boolean editMode)
	{
		super(parent, editMode, true);
		init();
	}

	private void init()
	{
		this.validChildOperators.add(PolicyConstants.OR);
		this.validChildOperators.add(PolicyConstants.AND);
		this.validChildOperators.add(PolicyConstants.NOT);
		this.validChildOperators.add(PolicyConstants.STRINGEQUAL);
		this.validChildOperators.add(PolicyConstants.STRINGREGEXMATCH);
	}
	
	@Override
	public void createLocalInterface()
	{
		this.addStyleName(CSSConstants.esoeManagerPolicyOperatorOr);
	
		this.localContent.clear();
		if (this.children != null && this.children.size() > 0)
		{
			for (int i = 0; i < this.children.size(); i++)
			{
				OperatorUI child = this.children.get(i);
				this.localContent.add(child);

				if (this.children.size() == 1 || (i + 1) != this.children.size())
				{
					Label orLbl = new Label("OR");
					orLbl.addStyleName(CSSConstants.esoeManagerPolicyOperatorTitleBoolean);
					this.localContent.add(orLbl);
				}
			}
		}
		else
		{
			Label orLbl = new Label("OR");
			orLbl.addStyleName(CSSConstants.esoeManagerPolicyOperatorTitleBoolean);
			this.localContent.add(orLbl);
		}		
	}
		
	/* (non-Javadoc)
	 * @see com.qut.middleware.esoemanager.client.ui.data.policy.Operator#validateOperator()
	 */
	@Override
	public void validate(List<String> idList) throws PolicyComponentValidationException
	{
		if(this.children == null || this.children.size() < 2)
		{
			this.localMessages.errorMsg("OR operators must have at least two children");
			throw new PolicyComponentValidationException("OR operators must have at least two children");
		}
		super.validate(idList);
	}

	@Override
	public void addChild(OperatorUI child, String childType)
	{
		if (this.validChildOperators.contains(childType))
		{
			this.children.add(child);
			this.createLocalInterface();
			return;
		}

		EventController.executeEvent(new MessageEvent(EventConstants.userMessage, EsoeManagerConstants.areaID,
				MessageEvent.error, "Unable to add " + childType + " to an OR operator"));
	}

	
	@Override
	public void deleteChild(OperatorUI child)
	{
		if (this.children.contains(child))
		{
			this.children.remove(child);
			this.createLocalInterface();
			return;
		}

		EventController.executeEvent(new MessageEvent(EventConstants.userMessage, EsoeManagerConstants.areaID,
				MessageEvent.error, "No such child found"));
	}

}

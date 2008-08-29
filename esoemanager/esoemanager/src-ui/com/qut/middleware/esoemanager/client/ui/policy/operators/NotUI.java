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

public class NotUI extends OperatorUI
{
	public NotUI(OperatorUI parent, boolean editMode)
	{
		super(parent, editMode, true);
		init();
	}

	private void init()
	{
		this.validChildOperators.add(PolicyConstants.STRINGEQUAL);
		this.validChildOperators.add(PolicyConstants.STRINGREGEXMATCH);
	}

	@Override
	public void createLocalInterface()
	{
		this.addStyleName(CSSConstants.esoeManagerPolicyOperatorNot);

		this.localContent.clear();
		
		Label notLbl = new Label("NOT");
		notLbl.addStyleName(CSSConstants.esoeManagerPolicyOperatorTitleBoolean);
		localContent.add(notLbl);
		
		if (children != null && children.size() > 0)
		{
			for (int i = 0; i < children.size(); i++)
			{
				OperatorUI child = children.get(i);
				localContent.add(child);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoemanager.client.ui.data.policy.Operator#validateOperator()
	 */
	@Override
	public void validate(List<String> idList) throws PolicyComponentValidationException
	{
		if (this.children == null || this.children.size() < 1)
		{
			this.localMessages.errorMsg("NOT operators must have a child");
			throw new PolicyComponentValidationException("NOT operators must have a child");
		}
		super.validate(idList);
	}

	@Override
	public void addChild(OperatorUI child, String childType)
	{
		if (this.validChildOperators.contains(childType))
		{
			if (this.children.size() == 0)
			{
				this.children.add(child);
				this.createLocalInterface();

			}
			else
			{
				EventController.executeEvent(new MessageEvent(EventConstants.userMessage, EsoeManagerConstants.areaID,
						MessageEvent.error, "NOT operators can only have a single child"));
			}
		}
		else
		{
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, EsoeManagerConstants.areaID,
					MessageEvent.error, "Unable to add " + childType + " to a NOT operator"));
		}
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

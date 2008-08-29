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
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.qut.gwtuilib.client.display.FlexibleTable;
import com.qut.gwtuilib.client.display.IntegratedListBox;
import com.qut.gwtuilib.client.input.StyledButton;
import com.qut.middleware.esoemanager.client.CSSConstants;
import com.qut.middleware.esoemanager.client.PolicyConstants;
import com.qut.middleware.esoemanager.client.ui.policy.operators.AndUI;
import com.qut.middleware.esoemanager.client.ui.policy.operators.ConditionUI;
import com.qut.middleware.esoemanager.client.ui.policy.operators.NotUI;
import com.qut.middleware.esoemanager.client.ui.policy.operators.OperatorUI;
import com.qut.middleware.esoemanager.client.ui.policy.operators.OrUI;
import com.qut.middleware.esoemanager.client.ui.policy.operators.StringEqualsUI;
import com.qut.middleware.esoemanager.client.ui.policy.operators.StringNormLowerCaseUI;
import com.qut.middleware.esoemanager.client.ui.policy.operators.StringNormSpaceUI;
import com.qut.middleware.esoemanager.client.ui.policy.operators.StringRegexMatchUI;

public class OperatorChooserUI extends VerticalPanel
{
	final protected List<String> validChildOperators;
	final protected OperatorUI operatorParent;

	public OperatorChooserUI(OperatorUI operatorParent, List<String> validChildOperators)
	{
		this.operatorParent = operatorParent;
		this.validChildOperators = validChildOperators;
	}
	
	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.UIObject#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible)
	{
		/* Creates interface at time of display to allow updates to children records etc in previous logic */
		if(visible)
			this.createInterface();
		
		super.setVisible(visible);
	}

	private void createInterface()
	{
		this.clear();
		this.addStyleName(CSSConstants.esoeManagerPolicyRuleOperatorChooser);
		
		FlexibleTable content = new FlexibleTable(2, 2);

		final IntegratedListBox operators = new IntegratedListBox("operators", "Add operation");

		if (this.validChildOperators.contains(PolicyConstants.OR))
			operators.addItem(PolicyConstants.OR);

		if (this.validChildOperators.contains(PolicyConstants.AND))
			operators.addItem(PolicyConstants.AND);

		if (this.validChildOperators.contains(PolicyConstants.NOT))
			operators.addItem(PolicyConstants.NOT);

		if (this.validChildOperators.contains(PolicyConstants.STRINGEQUAL))
			operators.addItem(PolicyConstants.STRINGEQUAL);

		if (this.validChildOperators.contains(PolicyConstants.STRINGREGEXMATCH))
			operators.addItem(PolicyConstants.STRINGREGEXMATCH);

		if (this.validChildOperators.contains(PolicyConstants.STRINGNORMLOWERCASE))
			operators.addItem(PolicyConstants.STRINGNORMLOWERCASE);

		if (this.validChildOperators.contains(PolicyConstants.STRINGNORMSPACE))
			operators.addItem(PolicyConstants.STRINGNORMSPACE);

		content.insertWidget(operators.getTitle());
		content.insertWidget(operators.getContent());

		StyledButton save = new StyledButton("confirm", "");
		save.addClickListener(new ClickListener()
		{
			public void onClick(Widget sender)
			{
				String operatorName = operators.getItemText(operators.getSelectedIndex());
				if (OperatorChooserUI.this.validChildOperators.contains(operatorName))
				{
					if (operatorName.equals(PolicyConstants.OR))
					{
						OrUI or = new OrUI(OperatorChooserUI.this.operatorParent, true);
						OperatorChooserUI.this.operatorParent.addChild(or, PolicyConstants.OR);
					}

					if (operatorName.equals(PolicyConstants.AND))
					{
						AndUI and = new AndUI(OperatorChooserUI.this.operatorParent, true);
						OperatorChooserUI.this.operatorParent.addChild(and, PolicyConstants.AND);

					}

					if (operatorName.equals(PolicyConstants.NOT))
					{
						NotUI not = new NotUI(OperatorChooserUI.this.operatorParent, true);
						OperatorChooserUI.this.operatorParent.addChild(not, PolicyConstants.NOT);

					}

					if (operatorName.equals(PolicyConstants.STRINGEQUAL))
					{
						StringEqualsUI stringEqual = new StringEqualsUI(OperatorChooserUI.this.operatorParent, true);
						OperatorChooserUI.this.operatorParent.addChild(stringEqual, PolicyConstants.STRINGEQUAL);

					}

					if (operatorName.equals(PolicyConstants.STRINGREGEXMATCH))
					{
						StringRegexMatchUI stringRegexMatch = new StringRegexMatchUI(OperatorChooserUI.this.operatorParent, true);
						OperatorChooserUI.this.operatorParent.addChild(stringRegexMatch, PolicyConstants.STRINGREGEXMATCH);

					}

					if (operatorName.equals(PolicyConstants.STRINGNORMLOWERCASE))
					{
						StringNormLowerCaseUI stringNormLower = new StringNormLowerCaseUI(OperatorChooserUI.this.operatorParent, true);
						OperatorChooserUI.this.operatorParent.addChild(stringNormLower, PolicyConstants.STRINGNORMLOWERCASE);

					}
					if (operatorName.equals(PolicyConstants.STRINGNORMSPACE))
					{
						StringNormSpaceUI stringNormSpace = new StringNormSpaceUI(OperatorChooserUI.this.operatorParent, true);
						OperatorChooserUI.this.operatorParent.addChild(stringNormSpace, PolicyConstants.STRINGNORMSPACE);
					}
					
					OperatorChooserUI.this.setVisible(false);
				}
			}
		});
		
		StyledButton cancel = new StyledButton("cancel", "");
		cancel.addClickListener(new ClickListener()
		{
			public void onClick(Widget sender)
			{
				OperatorChooserUI.this.setVisible(false);				
			}
		});

		content.insertWidget(save);
		
		if(!(this.operatorParent instanceof ConditionUI))
			content.insertWidget(cancel);
		
		this.add(content);
	}
}

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

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.qut.gwtuilib.client.display.FlexibleTable;
import com.qut.gwtuilib.client.display.IntegratedListBox;
import com.qut.gwtuilib.client.exceptions.InvalidContentException;
import com.qut.middleware.esoemanager.client.EsoeManagerConstants;

public class EffectUI extends HorizontalPanel
{
	VerticalPanel content;
	IntegratedListBox effects;
	
	public EffectUI()
	{
		FlexibleTable content = new FlexibleTable(2,2);
		this.effects = new IntegratedListBox(this, "Effect applied by this rule", EsoeManagerConstants.areaID);
		this.effects.getBackingListBox().addItem("Permit");
		this.effects.getBackingListBox().addItem("Deny");
		
		content.insertWidget(this.effects.getTitle());
		content.insertWidget(this.effects.getContent());
		
		this.add(content);
	}
	
	public VerticalPanel getContent()
	{
		return this.content;
	}
	
	public void setEffect(String effect) throws InvalidContentException
	{
		this.effects.selectItem(effect);
	}
	
	public String getEffect()
	{
		return this.effects.getCurrentValue();
	}
}

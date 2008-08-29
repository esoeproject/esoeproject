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
package com.qut.gwtuilib.client.input;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.PushButton;
import com.qut.gwtuilib.client.CSSConstants;
import com.qut.gwtuilib.client.eventdriven.eventmgr.BaseEvent;
import com.qut.gwtuilib.client.eventdriven.eventmgr.EventListener;

public class StyledButton extends PushButton implements EventListener
{
	private final String name;
	
	public StyledButton(String name, AbstractImagePrototype img)
	{
		super(img.createImage());
		
		this.name = name;		
		this.addStyleName(CSSConstants.button);
		this.addStyleName(CSSConstants.button + "-" + this.name);
	}
	
	public StyledButton(String name, String label)
	{
		super(label);
		
		if(label != null && label.length() > 0)
		{
			this.addStyleName(CSSConstants.buttonText);
		}
		else
		{
			this.addStyleName(CSSConstants.buttonNoText);
		}
		
		this.name = name;		
		this.addStyleName(CSSConstants.button);
		this.addStyleName(CSSConstants.button + "-" + this.name);
	}
	
	@Override
	public void onClick()
	{
		super.onClick();
	}

	public void executeEvent(BaseEvent event)
	{
			
	}

	public String[] getRegisteredEvents()
	{
		return null;
	}
}

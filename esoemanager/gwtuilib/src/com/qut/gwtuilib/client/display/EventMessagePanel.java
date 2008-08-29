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

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.qut.gwtuilib.client.eventdriven.eventmgr.BaseEvent;
import com.qut.gwtuilib.client.eventdriven.eventmgr.EventController;
import com.qut.gwtuilib.client.eventdriven.eventmgr.EventListener;
import com.qut.gwtuilib.client.eventdriven.events.EventConstants;
import com.qut.gwtuilib.client.eventdriven.events.MessageEvent;

public class EventMessagePanel extends HorizontalPanel implements EventListener
{
	/* All events this editor instance will respond to */
	private String[] registeredEvents = { EventConstants.userMessage };

	private String areaID;
	
	MessagePanel messagePanel;

	public EventMessagePanel(String areaID)
	{
		this.areaID = areaID;
		this.messagePanel = new MessagePanel();
		this.add(this.messagePanel);

		EventController.registerListener(this);
	}

	public void executeEvent(BaseEvent event)
	{
		if (event instanceof MessageEvent)
		{			
			MessageEvent messageEvent = (MessageEvent) event;
			if (messageEvent.getAreaID().matches(this.areaID))
			{
				if (messageEvent.getType() == MessageEvent.error)
				{
					this.messagePanel.errorMsg(messageEvent.getMessage());
				}
				
				if (messageEvent.getType() == MessageEvent.information)
				{
					this.messagePanel.informationMsg(messageEvent.getMessage());
				}

				if (messageEvent.getType() == MessageEvent.ok)
				{
					this.messagePanel.okMsg(messageEvent.getMessage());
				}				
			}
		}
	}

	public String[] getRegisteredEvents()
	{
		return this.registeredEvents;
	}

}

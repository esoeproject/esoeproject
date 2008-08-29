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

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.qut.gwtuilib.client.CSSConstants;
import com.qut.gwtuilib.client.eventdriven.eventmgr.BaseEvent;
import com.qut.gwtuilib.client.eventdriven.eventmgr.EventController;
import com.qut.gwtuilib.client.eventdriven.eventmgr.EventListener;
import com.qut.gwtuilib.client.eventdriven.events.EventConstants;
import com.qut.gwtuilib.client.eventdriven.events.MessageEvent;
import com.qut.gwtuilib.client.input.StyledButton;

public class EventMessageLoggingPanel extends HorizontalPanel implements EventListener
{
	/* All events this editor instance will respond to */
	private String[] registeredEvents = { EventConstants.userMessage };

	private String areaID;
	
	ScrollPanel container;
	VerticalPanel loggingPanel;

	public EventMessageLoggingPanel(String areaID)
	{
		this.areaID = areaID;
		this.createInterface();
		
		EventController.registerListener(this);
	}
	
	private void createInterface()
	{
		StyledButton toggle = new StyledButton("application", "");
		
		toggle.addClickListener(new ClickListener(){
			
			public void onClick(Widget sender) {
				container.setVisible(!container.isVisible());				
			}
			
		});
		
		this.loggingPanel = new VerticalPanel();	
		
		this.container = new ScrollPanel(this.loggingPanel);
		this.container.setHeight("100px");
		this.container.setVisible(false);
		
		this.add(toggle);
		this.add(this.container);
		this.addStyleName(CSSConstants.eventMessageLoggingPanel);
	}

	public void executeEvent(BaseEvent event)
	{
		if (event instanceof MessageEvent)
		{			
			MessageEvent messageEvent = (MessageEvent) event;
			LogStatement logStatement = new LogStatement(messageEvent.getMessage(), messageEvent.getType());
					
			if(loggingPanel.getWidgetCount() > 25)
			{
				loggingPanel.remove(25);
			}
			
			loggingPanel.insert(logStatement, 0);
		}
	}

	public String[] getRegisteredEvents()
	{
		return this.registeredEvents;
	}

	private class LogStatement extends HorizontalPanel
	{		
		public LogStatement(String statement, int type)
		{
			this.setSpacing(5);
			
			Label log = new Label(statement);
			log.addStyleName(CSSConstants.logMessage);
			
			if(type == MessageEvent.error)
			{
				log.addStyleName(CSSConstants.errorText);
			}
				
			if(type == MessageEvent.information)
			{
				log.addStyleName(CSSConstants.infoText);
			}
			
			if(type == MessageEvent.ok)
			{
				log.addStyleName(CSSConstants.okText);
			}
			
			this.add(log);
		}
	}
}

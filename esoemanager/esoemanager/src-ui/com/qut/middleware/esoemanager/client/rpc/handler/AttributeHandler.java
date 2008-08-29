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
package com.qut.middleware.esoemanager.client.rpc.handler;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ListBox;
import com.qut.gwtuilib.client.display.Loader;
import com.qut.gwtuilib.client.eventdriven.eventmgr.EventController;
import com.qut.gwtuilib.client.eventdriven.events.MessageEvent;
import com.qut.middleware.esoemanager.client.events.EventConstants;

public class AttributeHandler implements AsyncCallback
{
	Loader loader;
	String areaID;
	
	ListBox listBox;
	List<String> attribContainer;
	
	public AttributeHandler(Loader loader, List<String> attribContainer, ListBox listBox, String areaID)
	{
		super();
		this.loader = loader;
		this.attribContainer = attribContainer;
		this.listBox = listBox;
		this.areaID = areaID;
	}

	public void onFailure(Throwable caught)
	{
		this.loader.setVisible(false);
		EventController.executeEvent(new MessageEvent(EventConstants.userMessage, AttributeHandler.this.areaID,
				MessageEvent.error,
				"Unable to obtain attribute list from the server, please contact a system administrator. Server response: "
						+ caught.getLocalizedMessage()));
	}

	public void onSuccess(Object result)
	{
		this.loader.setVisible(false);
		if (result != null)
		{
			List<String> attributes = (List<String>) result;
			for (String attribute : attributes)
			{
				attribContainer.add(attribute);
				this.listBox.addItem(attribute);
			}
			return;
		}
		EventController.executeEvent(new MessageEvent(EventConstants.userMessage, AttributeHandler.this.areaID,
				MessageEvent.error,
				"Obtained invalid attribute list from the server, please contact a system administrator."));
	}

}

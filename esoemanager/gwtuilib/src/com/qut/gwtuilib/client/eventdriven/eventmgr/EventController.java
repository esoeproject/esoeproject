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
package com.qut.gwtuilib.client.eventdriven.eventmgr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventController
{
	// Map<String, List<OriaEditorListener>>
	private static Map eventListeners = new HashMap();

	public static void registerListener(EventListener listener)
	{
		String[] events = listener.getRegisteredEvents();
		if (events != null)
		{
			for (int i = 0; i < events.length; i++)
			{
				if (eventListeners.containsKey(events[i]))
				{
					List listeners = (List) eventListeners.get(events[i]);
					listeners.add(listener);
				}
				else
				{
					List listeners = new ArrayList();
					listeners.add(listener);
					eventListeners.put(events[i], listeners);
				}
			}
		}
	}

	public static void executeEvent(BaseEvent event)
	{
		List listeners = (List) eventListeners.get(event.getName());

		if (listeners != null)
		{
			for (int i = 0; i < listeners.size(); i++)
			{
				EventListener listener = ((EventListener) listeners.get(i));
				listener.executeEvent(event);
			}
		}
	}
}

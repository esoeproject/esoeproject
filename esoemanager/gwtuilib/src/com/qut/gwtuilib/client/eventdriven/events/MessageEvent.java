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
package com.qut.gwtuilib.client.eventdriven.events;

import com.qut.gwtuilib.client.eventdriven.eventmgr.BaseEvent;

public class MessageEvent extends BaseEvent
{
	static final public int error = 1;
	static final public int information = 2;
	static final public int ok = 3;
	
	int type;
	String message;
	String areaID;
	
	public MessageEvent(String name, String areaID, int type, String message)
	{
		super(name);
		
		this.areaID = areaID;
		this.type = type;
		this.message = message;
	}
	
	public int getType()
	{
		return this.type;
	}
	
	public String getMessage()
	{
		return this.message;
	}
	
	public String getAreaID()
	{
		return this.areaID;
	}
	
}

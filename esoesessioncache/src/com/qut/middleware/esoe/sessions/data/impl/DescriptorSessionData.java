/* 
 * Copyright 2008, Queensland University of Technology
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
 * 
 * Author: Shaun Mangelsdorf
 * Creation Date: 09/09/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.esoe.sessions.data.impl;

public class DescriptorSessionData
{
	private String sessionID;
	private String entityID;
	private String entitySessionID;
	
	public String getSessionID()
	{
		return sessionID;
	}
	public void setSessionID(String sessionID)
	{
		this.sessionID = sessionID;
	}
	public String getEntityID()
	{
		return entityID;
	}
	public void setEntityID(String entityID)
	{
		this.entityID = entityID;
	}
	public String getEntitySessionID()
	{
		return entitySessionID;
	}
	public void setEntitySessionID(String entitySessionID)
	{
		this.entitySessionID = entitySessionID;
	}
}

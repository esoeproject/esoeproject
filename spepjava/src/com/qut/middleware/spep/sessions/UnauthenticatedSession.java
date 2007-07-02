/* 
 * Copyright 2006, Queensland University of Technology
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
 * Creation Date: 24/11/2006
 * 
 * Purpose: Represents a new session that has not yet been authenticated by the ESOE
 */
package com.qut.middleware.spep.sessions;

/** */
public interface UnauthenticatedSession
{
	/**
	 * @return the authnRequestSAMLID
	 */
	public String getAuthnRequestSAMLID();
	
	/**
	 * @param authnRequestSAMLID the authnRequestSAMLID to set
	 */
	public void setAuthnRequestSAMLID(String authnRequestSAMLID);
	
	/**
	 * @return the requestURL
	 */
	public String getRequestURL();
	
	/**
	 * @param requestURL the requestURL to set
	 */
	public void setRequestURL(String requestURL);
	
	/**
	 * @return The length of time in seconds since this session was last inserted/retrieved from the cache
	 */
	public long getIdleTime();
	
	/**
	 * Updates the time stamp used by getIdleTime() to determine the time.
	 */
	public void updateTime();
}

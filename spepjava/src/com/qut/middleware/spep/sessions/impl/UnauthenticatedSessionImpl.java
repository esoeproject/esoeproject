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
 * Purpose: Implements the UnauthenticatedSession interface.
 */
package com.qut.middleware.spep.sessions.impl;

import com.qut.middleware.spep.sessions.UnauthenticatedSession;

/** */
public class UnauthenticatedSessionImpl implements UnauthenticatedSession
{
	private String authnRequestSAMLID;
	private String requestURL;
	private long time;
	
	/* (non-Javadoc)
	 * @see com.qut.middleware.spep.sessions.PrincipalSession#getAuthnRequestSAMLID()
	 */
	public String getAuthnRequestSAMLID()
	{
		return this.authnRequestSAMLID;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.spep.sessions.PrincipalSession#setAuthnRequestSAMLID(java.lang.String)
	 */
	public void setAuthnRequestSAMLID(String authnRequestSAMLID)
	{
		this.authnRequestSAMLID = authnRequestSAMLID;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.spep.sessions.PrincipalSession#getRequestURL()
	 */
	public String getRequestURL()
	{
		return this.requestURL;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.spep.sessions.PrincipalSession#setRequestURL(java.lang.String)
	 */
	public void setRequestURL(String requestURL)
	{
		this.requestURL = requestURL;
	}

	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.spep.sessions.UnauthenticatedSession#getIdleTime()
	 */
	public long getIdleTime()
	{
		return (System.currentTimeMillis() - this.time) / 1000;
	}

	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.spep.sessions.UnauthenticatedSession#updateTime()
	 */
	public void updateTime()
	{
		this.time = System.currentTimeMillis();
	}
}

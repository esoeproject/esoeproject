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
 * Author: Andre Zitelli
 * Creation Date: 14/12/2006
 * 
 * Purpose: Implementation to store the state of LogoutRequests.
 */
package com.qut.middleware.esoe.sso.bean.impl;

import com.qut.middleware.esoe.sso.bean.SSOLogoutState;

/**
 * Implementation to store the state of LogoutRequests.
 */
public class SSOLogoutStateImpl implements SSOLogoutState {

	private String logoutDescription;
	private boolean logoutState;
	private String spepURL;
		
	/** Initializes fields to empty values.
	*
	*/
	@SuppressWarnings("nls")
	public SSOLogoutStateImpl()
	{
		this.logoutDescription = "";
		this.logoutState = false;
		this.spepURL = "";
	}
	
	
	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.sso.bean.SSOLogoutState#getLogoutState()
	 */
	public boolean getLogoutState()
	{
		return this.logoutState;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.sso.bean.SSOLogoutState#getLogoutStateDescription()
	 */
	public String getLogoutStateDescription()
	{
		return this.logoutDescription;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.sso.bean.SSOLogoutState#getSPEPURL()
	 */
	public String getSPEPURL() 
	{
		return this.spepURL;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.sso.bean.SSOLogoutState#setLogoutState(boolean)
	 */
	public void setLogoutState(boolean state)
	{
		this.logoutState = state;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.sso.bean.SSOLogoutState#setLogoutStateDescription(java.lang.String)
	 */
	public void setLogoutStateDescription(String logoutDescription)
	{
		this.logoutDescription = logoutDescription;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.sso.bean.SSOLogoutState#setSPEPURL(java.lang.String)
	 */
	public void setSPEPURL(String url)
	{
		this.spepURL = url;
	}

}

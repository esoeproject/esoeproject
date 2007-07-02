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
 * Author: Bradley Beddoes
 * Creation Date: 24/10/2006
 * 
 * Purpose: Implementation of bean that carries information about failed logout attempts to SPEP's to logout monitor process
 */

package com.qut.middleware.esoe.sso.bean.impl;

import java.util.Date;

import com.qut.middleware.esoe.sso.bean.FailedLogout;

/** Implementation of bean that carries information about failed logout attempts to SPEP's to logout monitor process. */
public class FailedLogoutImpl implements FailedLogout
{

	private String endPoint;
	private String requestDocument;
	private Date timeStamp;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sso.bean.FailedLogout#getEndPoint()
	 */
	public String getEndPoint()
	{
		return this.endPoint;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sso.bean.FailedLogout#getRequestDocument()
	 */
	public String getRequestDocument()
	{
		return this.requestDocument;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sso.bean.FailedLogout#setEndPoint(java.lang.String)
	 */
	public void setEndPoint(String endPoint)
	{
		this.endPoint = endPoint;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sso.bean.FailedLogout#setRequestDocument(java.lang.String)
	 */
	public void setRequestDocument(String requestDocument)
	{
		this.requestDocument = requestDocument;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.sso.bean.FailedLogout#getTimeStamp()
	 */
	public Date getTimeStamp() 
	{
		return this.timeStamp;
	}
	

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.sso.bean.FailedLogout#setTimeStamp(java.util.Date)
	 */
	public void setTimeStamp(Date when)
	{
		this.timeStamp = when;
	}

	

}

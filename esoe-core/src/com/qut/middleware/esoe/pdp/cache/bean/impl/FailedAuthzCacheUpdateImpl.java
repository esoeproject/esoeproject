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
 * Creation Date: 12/10/2006
 * 
 * Purpose: Implements the FailedAuthzCacheUpdate interface
 */
package com.qut.middleware.esoe.pdp.cache.bean.impl;

import java.util.Date;

import com.qut.middleware.esoe.pdp.cache.bean.FailedAuthzCacheUpdate;

/** */
public class FailedAuthzCacheUpdateImpl implements FailedAuthzCacheUpdate
{

	private String endPoint;
	
	private String request;
	
	private Date timeAttempted;

	
	/* 
	 * @see com.qut.middleware.esoe.pdp.cache.bean.FailedAuthzCacheUpdate#getEndPoint()
	 */
	public String getEndPoint()
	{
		return this.endPoint;
	}

	/* 
	 * @see com.qut.middleware.esoe.pdp.cache.bean.FailedAuthzCacheUpdate#getRequestDocument()
	 */
	public String getRequestDocument()
	{
		return this.request;
	}

	/* 
	 * @see com.qut.middleware.esoe.pdp.cache.bean.FailedAuthzCacheUpdate#getTimeStamp()
	 */
	public Date getTimeStamp()
	{
		return this.timeAttempted;
	}

	/* 
	 * @see com.qut.middleware.esoe.pdp.cache.bean.FailedAuthzCacheUpdate#setEndPoint(java.lang.String)
	 */
	public void setEndPoint(String endPointURL)
	{
		this.endPoint = endPointURL;
	}

	/* 
	 * @see com.qut.middleware.esoe.pdp.cache.bean.FailedAuthzCacheUpdate#setRequestDocument(com.qut.middleware.esoe.xml.lxacml.protocol.LXACMLAuthzDecisionQueryType)
	 */
	public void setRequestDocument(String request)
	{
		this.request = request;
	}

	/* 
	 * @see com.qut.middleware.esoe.pdp.cache.bean.FailedAuthzCacheUpdate#setTimeStamp(java.util.Date)
	 */
	public void setTimeStamp(Date when)
	{
		this.timeAttempted = when;
	}

}

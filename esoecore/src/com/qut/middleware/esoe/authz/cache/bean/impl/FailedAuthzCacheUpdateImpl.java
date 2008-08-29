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
package com.qut.middleware.esoe.authz.cache.bean.impl;

import java.util.Date;

import com.qut.middleware.esoe.authz.cache.bean.FailedAuthzCacheUpdate;
import com.qut.middleware.esoe.logout.bean.FailedLogout;

/** */
public class FailedAuthzCacheUpdateImpl implements FailedAuthzCacheUpdate
{

	private String endPoint;
	
	private byte[] request;
	
	private Date timeAttempted;

	
	/* 
	 * @see com.qut.middleware.esoe.authz.cache.bean.FailedAuthzCacheUpdate#getEndPoint()
	 */
	public String getEndPoint()
	{
		return this.endPoint;
	}

	/* 
	 * @see com.qut.middleware.esoe.authz.cache.bean.FailedAuthzCacheUpdate#getRequestDocument()
	 */
	public byte[] getRequestDocument()
	{
		return this.request;
	}

	/* 
	 * @see com.qut.middleware.esoe.authz.cache.bean.FailedAuthzCacheUpdate#getTimeStamp()
	 */
	public Date getTimeStamp()
	{
		return this.timeAttempted;
	}

	/* 
	 * @see com.qut.middleware.esoe.authz.cache.bean.FailedAuthzCacheUpdate#setEndPoint(java.lang.String)
	 */
	public void setEndPoint(String endPointURL)
	{
		this.endPoint = endPointURL;
	}

	/* 
	 * @see com.qut.middleware.esoe.authz.cache.bean.FailedAuthzCacheUpdate#setRequestDocument(com.qut.middleware.esoe.xml.lxacml.protocol.LXACMLAuthzDecisionQueryType)
	 */
	public void setRequestDocument(byte[] request)
	{
		this.request = request;
	}

	/* 
	 * @see com.qut.middleware.esoe.authz.cache.bean.FailedAuthzCacheUpdate#setTimeStamp(java.util.Date)
	 */
	public void setTimeStamp(Date when)
	{
		this.timeAttempted = when;
	}

	/** Returns true IFF o represents an instance of FailedAuthzCacheUpdate AND the string returned by the accessor getEndpoint()
	 *  is equals for both objects.
	 * 
	 */
	@Override
	public boolean equals(Object o)
	{
		// It should be sufficient to say two failures are equal if the endpoint id the same, as each endpoint should 
		// only ever recieve one cache clear at a time.
		if (o == null || this.endPoint == null )
			return false;
		
		if(o instanceof FailedAuthzCacheUpdate)
		{			
			FailedAuthzCacheUpdate comparison = (FailedAuthzCacheUpdate)o;
			
			// compare endpoint string field
			return (comparison.getEndPoint() != null && comparison.getEndPoint().equals(this.endPoint));
		}
		
		return false;
	}
}

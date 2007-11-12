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
	private byte[] requestDocument;
	private Date timeStamp;
	private String authnId;
	
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
	public byte[] getRequestDocument()
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
	public void setRequestDocument(byte[] requestDocument)
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

	
	public String getAuthnId() 
	{
		return this.authnId;
	}

	
	public void setAuthnId(String id) 
	{
		this.authnId = id;		
	}

	/** Returns true IFF o represents an instance of FailedLogout AND the string values returned from
	 * getEndpoint() and get authnId() are equal for both objects.
	 * 
	 */
	@Override
	public boolean equals(Object o)
	{
		// Failed logouts are more difficult to determine if one equals another. As each endpoint can recieve 
		// many logout requests, each with different documents for different user sessions, it is not sifficient
		// to use an endpoint as a unique identifier. The only way to uniquely identify a particular logout request
		// is to include both endpoint and authn session id as the identifier.
		if (o == null || this.endPoint == null || this.authnId == null)
			return false;
		
		if(o instanceof FailedLogout)
		{			
			boolean equal = false;
			FailedLogout comparison = (FailedLogout)o;
			
			// compare endpoint string field
			equal = comparison.getEndPoint() != null && comparison.getEndPoint().equals(this.endPoint);
			
			// compare set authn id to distinguish between user sessions for logout
			equal = (equal && (this.authnId.equals(comparison.getAuthnId()))  );
					
			return equal;
		}
		
		return false;
	}

}

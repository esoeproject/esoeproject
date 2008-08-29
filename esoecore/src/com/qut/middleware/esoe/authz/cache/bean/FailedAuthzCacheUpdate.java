/* Copyright 2006, Queensland University of Technology
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
 * Creation Date: 28/09/2006
 * 
 * Purpose: This interface defines a bean that stores information that can be used to
 * inform SPEP's of cache updates. It MUST be used to record state whenever a failure 
 * occurs in sending a clearCacheState directive to an SPEP.
 */
package com.qut.middleware.esoe.authz.cache.bean;

import java.util.Date;

/** */
public interface FailedAuthzCacheUpdate 
{
	
	/**
	 * Set the opensaml 2.0 compliant request document associated with the update failure. This 
	 * is the message that was attempted to send to the SPEP.
	 * 
	 * @param request A string representing a valid, opensaml 2.0 compliant request.
	 */
	public void setRequestDocument(byte[] request);
	

	/**
	 * Get the opensaml 2.0 compliant request document associated with the update failure. This 
	 * is the message that was attempted to send to the SPEP.
	 * 
	 * @return The opensaml 2.0 request document as set by setRequestDocument, else null if not exists.
	 *        
	 */
	public byte[] getRequestDocument();	
	

	/** Set the string representation of the end point URL that the update was attempted against. 
	 * 
	 * @param endPointURL The end point URL string.
	 */
	public void setEndPoint(String endPointURL);
	
	
	/** Retrieve the string representation of the end point URL that the update was attempted against. 
	 * 
	 * @return The end point URL string.
	 */
	public String getEndPoint();
	
	
	/** Set the time that the update attempt was made.
	 * 
	 * @param when The timestamp of the update attempt.
	 */
	public void setTimeStamp(Date when);
	
	
	/** Get the time that the update attempt was made.
	 * 
	 * @return The timestamp of the update attempt.
	 */
	public Date getTimeStamp();
	
	/** Implementing classes must override java.lang.Object.equals(Object o) to include field comparison so that
	 * calls to .equals for this object is correct.
	 * 
	 */
	public boolean equals(Object o);
	
	
}

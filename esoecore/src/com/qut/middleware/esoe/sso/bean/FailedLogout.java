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
 * Purpose: Carries information about failed logout attempts to logout monitor process
 */
package com.qut.middleware.esoe.sso.bean;

import java.util.Date;

/** Carries information about failed logout attempts to logout monitor process. */
public interface FailedLogout
{
	/**
	 * @return The endpoint URL to communicate with
	 */
	public String getEndPoint();

	/**
	 * @return The original request document that was issued
	 */
	public byte[] getRequestDocument();

	/**
	 * Sets the endPoint
	 * @param endPoint
	 */
	public void setEndPoint(String endPoint);
	
	/**
	 * Sets the Request Document
	 * @param requestDocument the Request Document
	 */
	public void setRequestDocument(byte[] requestDocument);
	
	
	/** Set the timestamp when the failure occured
	 * 
	 */
	public void setTimeStamp(Date when);
	
	
	/** Get the timestamp of when the failure occured
	 * 
	 */
	public Date getTimeStamp();
	
	
}

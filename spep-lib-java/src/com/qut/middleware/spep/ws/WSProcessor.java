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
 * Creation Date: 14/12/2006
 * 
 * Purpose: Provides an interface for the web service processor, which defines endpoints
 * 		that will be served as SOAP based web services.
 */
package com.qut.middleware.spep.ws;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;

/** Provides an interface for the web service processor, which defines endpoints
 * 		that will be served as SOAP based web services.*/
public interface WSProcessor
{
	/** Performs a clear of the authorization cache.
	 * 
	 * @param request The AuthzCacheClearRequest document.
	 * @return The AuthzCacheClearResponse document.
	 * @throws AxisFault if the web service client cannot send the request.
	 */
	public OMElement authzCacheClear(OMElement request) throws AxisFault;
	
	/** Performs a single logout operation.
	 * 
	 * @param request The LogoutRequest document
	 * @return The LogoutResponse document
	 * @throws AxisFault if the web service client cannot send the request.
	 */
	public OMElement singleLogout(OMElement request) throws AxisFault;
}

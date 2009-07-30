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
 * Purpose: Provides an interface for web service calls to be made.
 */
package com.qut.middleware.spep.ws;

import org.w3c.dom.Element;

import com.qut.middleware.spep.ws.exception.WSClientException;

/** Provides an interface for web service calls to be made.*/
public interface WSClient
{
	/** Performs a web service attribute query and returns the response.
	 * 
	 * @param attributeQuery The attribute query to make.
	 * @param endpoint The endpoint to send the query to.
	 * @return The response to the query.
	 * @throws WSClientException if the Web service client cannot establish the connection.
	 */
	public Element attributeAuthority(Element attributeQuery, String endpoint) throws WSClientException;
	
	/** Performs a web service policy decision query to determine whether access to a resource
	 * should be allowed, and returns the response. 
	 * 
	 * @param decisionRequest The string representation of a LXACLMAuthzDecisionQuery request to send.
	 * @param endpoint The endpoint to send the query to.
	 * @return The response to the query
	 * @throws WSClientException if the Web service client cannot establish the connection
	 */
	public Element policyDecisionPoint(Element decisionRequest, String endpoint) throws WSClientException;
	
	/** Performs a web service spep startup request and returns the response.
	 * 
	 * @param spepStartup The string representation of the ValidateInitializationRequest to send.
	 * @param endpoint The endpoint to send the request to
	 * @return The response to the request.
	 * @throws WSClientException if the Web service client cannot establish the connection
	 */
	public Element spepStartup(Element spepStartup, String endpoint) throws WSClientException;
	
	/** Performs a web service artifact resolve request and returns the response.
	 * 
	 * @param artifactResolve The ArtifactResolve request to send
	 * @param endpoint The endpoint to send the request to
	 * @return The response to the request
	 * @throws WSClientException If the web service client cannot establish the connection
	 */
	public Element artifactResolve(Element artifactResolve, String endpoint) throws WSClientException;
}

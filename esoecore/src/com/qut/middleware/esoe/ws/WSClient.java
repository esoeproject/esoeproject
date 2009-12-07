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
 * Creation Date: 05/12/2006
 *
 * Purpose: Defines all operations the ESOE may invoke over remote WS links
 */
package com.qut.middleware.esoe.ws;

import org.w3c.dom.Element;

import com.qut.middleware.esoe.ws.exception.WSClientException;

/** Defines all operations the ESOE may invoke over remote WS links. */

public interface WSClient
{
	/** Send an authzCacheClear Request to the specified endpoint.
	 *
	 * @param request The byte representation of the request to send.
	 * @param endpoint The string representation of the endpoint URL to send to.
	 *
	 * @return The string representation of the Response sent back from the endpoint.
	 *
	 * @throws WSClientException if the client cannot send the request.
	 */
	public Element authzCacheClear(Element request, String endpoint) throws WSClientException;

	/** Send an SingleLogoutRequest Request to the specified endpoint.
	 *
	 * @param request The byte representation of the request to send.
	 * @param endpoint The string representation of the endpoint URL to send to.
	 *
	 * @return The string representation of the SingleLogoutResponse sent back from the endpoint.
	 *
	 * @throws WSClientException if the client cannot send the request.
	 */
	public Element singleLogout(Element request, String endpoint) throws WSClientException;

	/**
	 * Sends an ArtifactResolve request to the specified endpoint.
	 * @param request The request document element.
	 * @param endpoint The endpoint location to make the request to.
	 * @return The response document element.
	 * @throws WSClientException If an error occurs while processing the request.
	 */
	public Element artifactResolve(Element request, String endpoint) throws WSClientException;

	/**
	 * Sends a RegisterPrincipalRequest to the specified endpoint
	 * @param request The request document element
	 * @param endpoint The endpoint location to make the request to
	 * @return The response document element
	 * @throws WSClientException If an error occurs while processing the request.
	 */
	public Element registerPrincipal(Element request, String endpoint) throws WSClientException;
}

/* Copyright 2008, Queensland University of Technology
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
 * Creation Date: 15/12/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.spep.authn.bindings;

import org.w3c.dom.Element;

import com.qut.middleware.spep.exception.AuthenticationException;

public interface ArtifactProcessor
{
	/**
	 * Registers an artifact document with the artifact resolver.
	 * @param artifactDocument The DOM element of the root of the document. It is assumed that artifactDocument.getOwnerDocument() refers to the document as a whole.
	 * @param audience The entity ID of the intended audience of the document. The document will only be returned to that entity.
	 * @return The Base64 encoded artifact token for use with the user agent.
	 * @throws ArtifactBindingException If an error occurs while processing the artifact.
	 */
	public String registerArtifact(Element artifactDocument, String audience) throws AuthenticationException;
	
	/**
	 * Retrieves an artifact document from a remote source.
	 * @param artifactToken The Base64 encoded artifact token.
	 * @return The root document element of the artifact.
	 * @throws ArtifactBindingException If an error occurs while processing the artifact.
	 */
	public Element getRemoteArtifact(String artifactToken) throws AuthenticationException;
	
	/**
	 * Handles a web service query for an artifact document.
	 * @param artifactRequest
	 * @return
	 * @throws AuthenticationException
	 */
	public Element execute(Element artifactRequest) throws AuthenticationException;
}

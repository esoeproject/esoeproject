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
 * Creation Date: 03/12/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.esoe.sso.plugins.artifact.data;

import com.qut.middleware.esoe.sso.plugins.artifact.bean.Artifact;
import com.qut.middleware.esoe.sso.plugins.artifact.exception.ArtifactBindingException;

public interface ArtifactDao
{
	/**
	 * Stores the given artifact to be retrieved later. All properties in the Artifact must be set.
	 * @param artifact The message handle of the artifact.
	 * @throws ArtifactBindingException If the artifact could not be stored due to an error.
	 */
	public void storeArtifact(Artifact artifact) throws ArtifactBindingException;
	
	/**
	 * Retrieves the given artifact. The messageHandle must be present in the request object.
	 * 
	 * It is guaranteed that 'audience' and 'document' will be set in the Artifact object before returning.
	 * 
	 * @param artifact The Artifact object containing the message handle of the artifact.
	 * @throws ArtifactBindingException If the artifact resolution failed due to an error.
	 */
	public void resolveArtifact(Artifact artifact) throws ArtifactBindingException;
}

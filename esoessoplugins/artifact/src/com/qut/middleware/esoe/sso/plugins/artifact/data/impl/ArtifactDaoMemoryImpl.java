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

package com.qut.middleware.esoe.sso.plugins.artifact.data.impl;

import java.util.HashMap;
import java.util.Map;

import com.qut.middleware.esoe.sso.plugins.artifact.bean.Artifact;

public class ArtifactDaoMemoryImpl extends ArtifactDaoBase
{
	private Map<String, Artifact> artifacts;
	
	public ArtifactDaoMemoryImpl()
	{
		this.artifacts = new HashMap<String, Artifact>();
	}

	@Override
	protected Artifact resolveArtifact(String messageHandle)
	{
		return this.artifacts.get(messageHandle);
	}

	@Override
	protected void storeArtifact(Artifact artifact, String messageHandle)
	{
		this.artifacts.put(messageHandle, artifact);
	}
}

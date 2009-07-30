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
 * Creation Date: 09/12/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.esoe.sso.plugins.artifact.data.impl;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;

import com.qut.middleware.esoe.sso.plugins.artifact.bean.Artifact;
import com.qut.middleware.esoe.sso.plugins.artifact.data.ArtifactDao;
import com.qut.middleware.esoe.sso.plugins.artifact.exception.ArtifactBindingException;

public abstract class ArtifactDaoBase implements ArtifactDao
{
	/**
	 * Stores the artifact to be referenced by the given message handle.
	 * @param artifact The artifact to be stored
	 * @param messageHandle A String representation of the message handle
	 */
	protected abstract void storeArtifact(Artifact artifact, String messageHandle);

	/**
	 * Resolves a previously stored artifact.
	 * 
	 * It must be guaranteed that 'audience' and 'document' be set in the Artifact object returned. All other fields are optional.
	 * 
	 * @param messageHandle A String representation of the message handle
	 * @return The artifact corresponding to that message handle
	 */
	protected abstract Artifact resolveArtifact(String messageHandle);

	public void resolveArtifact(Artifact artifact) throws ArtifactBindingException
	{
		try
		{
			byte[] base64MessageHandle = Base64.encodeBase64(artifact.getMessageHandle());
			String messageHandle = new String(base64MessageHandle, "UTF-8");
			
			Artifact retrieved = this.resolveArtifact(messageHandle);
			artifact.setDocument(retrieved.getDocument());
			artifact.setAudience(retrieved.getAudience());
		}
		catch (UnsupportedEncodingException e)
		{
			throw new ArtifactBindingException("Unable to create message handle base64 string, the required encoding is not supported. Error: " + e.getMessage(), e);
		}
	}

	public void storeArtifact(Artifact artifact) throws ArtifactBindingException
	{
		try
		{
			byte[] base64Bytes = Base64.encodeBase64(artifact.getMessageHandle());
			String messageHandle = new String(base64Bytes, "UTF-8");
			
			storeArtifact(artifact, messageHandle);
		}
		catch (UnsupportedEncodingException e)
		{
			throw new ArtifactBindingException("Unable to create message handle base64 string, the required encoding is not supported. Error: " + e.getMessage(), e);
		}
	}

}
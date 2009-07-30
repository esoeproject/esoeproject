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
package com.qut.middleware.esoe.sso.plugins.artifact.bean;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;

import com.qut.middleware.esoe.sso.plugins.artifact.exception.ArtifactBindingException;

public class Artifact
{
	private static final int TYPE_ID = 4;
	
	private int type;
	private int index;
	private byte[] sourceID;
	private byte[] messageHandle;
	private String audience;
	private byte[] document;
	
	public Artifact(int index, byte[] sourceID, byte[] messageHandle, String audience, byte[] document) throws ArtifactBindingException
	{
		this.type = TYPE_ID;
		this.index = index;
		
		if (sourceID.length != 20)
		{
			throw new ArtifactBindingException("Source ID length must be 20");
		}
		this.sourceID = sourceID;
		
		if (messageHandle.length != 20)
		{
			throw new ArtifactBindingException("Message handle length must be 20");
		}
		this.messageHandle = messageHandle;
		
		if (audience == null)
		{
			throw new ArtifactBindingException("Audience for SAML artifact cannot be null");
		}
		this.audience = audience;
		
		if (document == null)
		{
			throw new ArtifactBindingException("SAML artifact cannot have a null document element");
		}
		this.document = document;
	}
	
	public Artifact(String artifact) throws ArtifactBindingException
	{
		byte[] rawArtifact;
		try
		{
			rawArtifact = Base64.decodeBase64(artifact.getBytes("UTF-8"));
		}
		catch (UnsupportedEncodingException e)
		{
			throw new IllegalArgumentException("Unable to parse the Base64 SAML artifact token because the required encoding is not supported.", e);
		}
		
		if (rawArtifact.length < 20)
		{
			throw new ArtifactBindingException("Unable to parse the SAML artifact token, it is not the correct length required by the SAML spec (20 bytes).");
		}
		
		type = ((int)rawArtifact[0] * 0x100) + (int)rawArtifact[1];
		index = ((int)rawArtifact[2] * 0x100) + (int)rawArtifact[3];
		
		if (type != 0x0004)
		{
			throw new ArtifactBindingException("Unable to parse the SAML artifact token, unrecognized type ID: " + type);
		}

		if (rawArtifact.length != 44)
		{
			throw new ArtifactBindingException("Unable to parse the SAML artifact token, it is not the correct length required by the SAML spec for type ID 0x0004 (44 bytes).");
		}
		
		this.sourceID = new byte[20];
		for (int i=0; i<20; ++i)
		{
			// Read bytes 4 - 23 inclusive.
			this.sourceID[i] = rawArtifact[4 + i];
		}
		
		this.messageHandle = new byte[20];
		for (int i=0; i<20; ++i)
		{
			// Read bytes 24 - 43 inclusive.
			this.messageHandle[i] = rawArtifact[24 + i];
		}
	}
	
	public String toBase64Artifact()
	{
		/* 44 bytes:
		 *   2 for type ID
		 *   2 for endpoint index
		 *   40 for 'remaining artifact'
		 *   
		 * We're using type ID 0x0004 defined by SAML 2.0 spec so
		 * Remaining artifact (40 bytes):
		 *   20 for source ID
		 *   20 for message handle
		 */
		byte[] rawArtifact = new byte[44];
		
		rawArtifact[0] = (byte)((type & (0xFF00)) / 0x100);
		rawArtifact[1] = (byte)(type & (0xFF));
		rawArtifact[2] = (byte)((index & (0xFF00)) / 0x100);
		rawArtifact[3] = (byte)(index & (0xFF));
		
		for (int i=0; i<20; ++i)
		{
			// Write bytes 4 - 23 inclusive.
			rawArtifact[4 + i] = this.sourceID[i];
		}
		
		for (int i=0; i<20; ++i)
		{
			// Write bytes 24 - 43 inclusive
			rawArtifact[24 + i] = this.messageHandle[i];
		}
		
		byte[] base64 = Base64.encodeBase64(rawArtifact);
		try
		{
			return new String(base64, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new IllegalArgumentException("Unable to create the Base64 SAML artifact token because the required encoding is not supported.", e);
		}
	}

	public int getType()
	{
		return type;
	}

	public int getIndex()
	{
		return index;
	}

	public byte[] getSourceID()
	{
		return sourceID;
	}

	public byte[] getMessageHandle()
	{
		return messageHandle;
	}

	public String getAudience()
	{
		return audience;
	}
	
	public void setAudience(String audience)
	{
		this.audience = audience;
	}

	public byte[] getDocument()
	{
		return document;
	}

	public void setDocument(byte[] document)
	{
		this.document = document;
	}
}
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
 * Creation Date: 17/11/2006
 * 
 * Purpose: Implementation of LSInput interface, while we could resolve from various DOM objects it was easier for our purposes to recreate.
 */
package com.qut.middleware.saml2.resolver;

import java.io.InputStream;
import java.io.Reader;

import org.w3c.dom.ls.LSInput;

/** Implementation of LSInput interface, while we could resolve from various DOM objects it was easier for our purposes to recreate. */
public class LSInputImpl implements LSInput
{
	String baseURI;
	InputStream byteStream;
	boolean certifiedText;
	Reader characterStream;
	String encoding;
	String publicId;
	String stringData;
	String systemId;
	
	/* (non-Javadoc)
	 * @see org.w3c.dom.ls.LSInput#getBaseURI()
	 */
	public String getBaseURI()
	{
		return this.baseURI;
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.ls.LSInput#getByteStream()
	 */
	public InputStream getByteStream()
	{
		return this.byteStream;
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.ls.LSInput#getCertifiedText()
	 */
	public boolean getCertifiedText()
	{
		return this.certifiedText;
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.ls.LSInput#getCharacterStream()
	 */
	public Reader getCharacterStream()
	{
		return this.characterStream;
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.ls.LSInput#getEncoding()
	 */
	public String getEncoding()
	{
		return this.encoding;
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.ls.LSInput#getPublicId()
	 */
	public String getPublicId()
	{
		return this.publicId;
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.ls.LSInput#getStringData()
	 */
	public String getStringData()
	{
		return this.stringData;
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.ls.LSInput#getSystemId()
	 */
	public String getSystemId()
	{
		return this.systemId;
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.ls.LSInput#setBaseURI(java.lang.String)
	 */
	public void setBaseURI(String baseURI)
	{
		this.baseURI = baseURI;
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.ls.LSInput#setByteStream(java.io.InputStream)
	 */
	public void setByteStream(InputStream byteStream)
	{
		this.byteStream = byteStream;
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.ls.LSInput#setCertifiedText(boolean)
	 */
	public void setCertifiedText(boolean certifiedText)
	{
		this.certifiedText = certifiedText;
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.ls.LSInput#setCharacterStream(java.io.Reader)
	 */
	public void setCharacterStream(Reader characterStream)
	{
		this.characterStream = characterStream;
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.ls.LSInput#setEncoding(java.lang.String)
	 */
	public void setEncoding(String encoding)
	{
		this.encoding = encoding;
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.ls.LSInput#setPublicId(java.lang.String)
	 */
	public void setPublicId(String publicId)
	{
		this.publicId = publicId;
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.ls.LSInput#setStringData(java.lang.String)
	 */
	public void setStringData(String stringData)
	{
		this.stringData = stringData;
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.ls.LSInput#setSystemId(java.lang.String)
	 */
	public void setSystemId(String systemId)
	{
		this.systemId = systemId;
	}

}

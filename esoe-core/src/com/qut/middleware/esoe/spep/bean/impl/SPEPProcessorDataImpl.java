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
 * Creation Date: 09/11/2006
 * 
 * Purpose: Implements the SPEPProcessorData interface.
 */
package com.qut.middleware.esoe.spep.bean.impl;

import com.qut.middleware.esoe.spep.bean.SPEPProcessorData;


public class SPEPProcessorDataImpl implements SPEPProcessorData
{

	private int authzCacheIndex;
	private String requestEntityID;
	private String requestDocument;
	private String responseDocument;

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.spep.bean.SPEPProcessorData#getAuthzEndpointID()
	 */
	public int getAuthzCacheIndex()
	{
		return this.authzCacheIndex;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.spep.bean.SPEPProcessorData#getRequestEntityID()
	 */
	public String getRequestDescriptorID()
	{
		return this.requestEntityID;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.spep.bean.SPEPProcessorData#setAuthzEndpointID(java.lang.String)
	 */
	public void setAuthzCacheIndex(int authzEndpointID)
	{
		this.authzCacheIndex = authzEndpointID;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.spep.bean.SPEPProcessorData#setRequestEntityID(java.lang.String)
	 */
	public void setRequestDescriptorID(String requestDescriptorID)
	{
		this.requestEntityID = requestDescriptorID;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.bean.SAMLProcessorData#getRequestDocument()
	 */
	public String getRequestDocument()
	{
		return this.requestDocument;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.bean.SAMLProcessorData#getResponseDocument()
	 */
	public String getResponseDocument()
	{
		return this.responseDocument;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.bean.SAMLProcessorData#setRequestDocument(java.lang.String)
	 */
	public void setRequestDocument(String requestDocument)
	{
		this.requestDocument = requestDocument;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.bean.SAMLProcessorData#setResponseDocument(java.lang.String)
	 */
	public void setResponseDocument(String responseDocument)
	{
		this.responseDocument = responseDocument;
	}

}

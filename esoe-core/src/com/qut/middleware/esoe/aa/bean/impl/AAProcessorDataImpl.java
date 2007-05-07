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
 * Creation Date: 19/10/2006
 * 
 * Purpose: Implements the AAProcessorData interface.
 */
package com.qut.middleware.esoe.aa.bean.impl;

import com.qut.middleware.esoe.aa.bean.AAProcessorData;

public class AAProcessorDataImpl implements AAProcessorData
{
	private String requestDocument;
	private String descriptorID;
	private String subjectID;
	private String responseDocument;

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.aa.bean.AAProcessorData#getRequestDocument()
	 */
	public String getRequestDocument()
	{
		return this.requestDocument;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.aa.bean.AAProcessorData#setRequestDocument(org.opensaml.saml2.core.Request)
	 */
	public void setRequestDocument(String requestDocument)
	{
		this.requestDocument = requestDocument;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.aa.bean.AAProcessorData#getDescriptorID()
	 */
	public String getDescriptorID()
	{
		return this.descriptorID;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.aa.bean.AAProcessorData#getResponseDocument()
	 */
	public String getResponseDocument()
	{
		return this.responseDocument;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.aa.bean.AAProcessorData#getSubjectID()
	 */
	public String getSubjectID()
	{
		return this.subjectID;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.aa.bean.AAProcessorData#setDescriptorID(java.lang.String)
	 */
	public void setDescriptorID(String descriptorID)
	{
		this.descriptorID = descriptorID;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.aa.bean.AAProcessorData#setResponseDocument(org.opensaml.saml2.core.Response)
	 */
	public void setResponseDocument(String responseDocument)
	{
		this.responseDocument = responseDocument;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.aa.bean.AAProcessorData#setSubjectID(java.lang.String)
	 */
	public void setSubjectID(String subjectID)
	{
		this.subjectID = subjectID;
	}

}

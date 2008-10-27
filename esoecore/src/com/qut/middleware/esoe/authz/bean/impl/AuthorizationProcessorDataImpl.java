/* Copyright 2006, Queensland University of Technology
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
 * Author: Andre Zitelli
 * Creation Date: 9/10/2006
 * 
 * Purpose: @see com.qut.middleware.esoe.authz.bean.AuthorizationProcessorData
 * 
 */


package com.qut.middleware.esoe.authz.bean.impl;

import org.w3c.dom.Element;

import com.qut.middleware.esoe.authz.bean.AuthorizationProcessorData;

/** */
public class AuthorizationProcessorDataImpl implements AuthorizationProcessorData
{

	private String issuerID;
	
	private String subjectID;
	
	private Element request;
	
	private Element response;
	
	/**
	 * Default constructor. Sets all values to null.
	 */
	public AuthorizationProcessorDataImpl()
	{
		this.issuerID = null;
		this.subjectID =  null;
		this.request =  null;
		this.response =  null;
	}
	
	/* 
	 * @see com.qut.middleware.esoe.authz.bean.AuthorizationProcessorData#getDescriptorID()
	 */
	public String getIssuerID()
	{		
		return this.issuerID;
	}

		
	/* 
	 * @see com.qut.middleware.esoe.authz.bean.AuthorizationProcessorData#getRequest()
	 */
	public Element getRequestDocument()
	{
		return this.request;
	}

	
	/* 
	 * @see com.qut.middleware.esoe.authz.bean.AuthorizationProcessorData#getResponse()
	 */
	public Element getResponseDocument()
	{
		return this.response;
	}

	
	/* 
	 * @see com.qut.middleware.esoe.authz.bean.AuthorizationProcessorData#getSubjectID()
	 */
	public String getSubjectID()
	{
		return this.subjectID;
	}

	
	/* 
	 * @see com.qut.middleware.esoe.authz.bean.AuthorizationProcessorData#setDescriptorID(java.lang.String)
	 */
	public void setIssuerID(String issuerID)
	{
		this.issuerID = issuerID;
	}

	
	/* 
	 * @see com.qut.middleware.esoe.authz.bean.AuthorizationProcessorData#setRequest(java.lang.String)
	 */
	public void setRequestDocument(Element request)
	{
		this.request = request;
	}

	
	/* 
	 * @see com.qut.middleware.esoe.authz.bean.AuthorizationProcessorData#setResponse(java.lang.String)
	 */
	public void setResponseDocument(Element response)
	{
		this.response = response;
	}

	
	/* 
	 * @see com.qut.middleware.esoe.authz.bean.AuthorizationProcessorData#setSubjectID(java.lang.String)
	 */
	public void setSubjectID(String ID)
	{
		this.subjectID = ID;
	}	
}

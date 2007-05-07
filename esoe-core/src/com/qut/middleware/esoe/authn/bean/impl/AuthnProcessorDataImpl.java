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
 * Creation Date: 05/10/2006
 * 
 * Purpose: Interface for describing databean sent between client facing servlet and authn processor
 */
package com.qut.middleware.esoe.authn.bean.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.qut.middleware.esoe.authn.bean.AuthnProcessorData;

/** */
public class AuthnProcessorDataImpl implements AuthnProcessorData
{	
	private HttpServletRequest request;
	private HttpServletResponse response;

	private String redirectTarget;
	private String currentHandler;
	private String invalidURL;
	private String sessionID;
	private String principalName;
	private int errorCode;
	private String errorMsg;
	
	private boolean successfulAuthn;
	private boolean automatedSSO = true; // enabled by default

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.authn.bean.AuthnProcessorData#getAutomatedSOO()
	 */
	public boolean getAutomatedSSO()
	{
		return this.automatedSSO;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.authn.bean.AuthnProcessorData#getCurrentHandler()
	 */
	public String getCurrentHandler()
	{
		return this.currentHandler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.authn.bean.AuthnProcessorData#getFailURL()
	 */
	public String getInvalidURL()
	{
		return this.invalidURL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.authn.bean.AuthnProcessorData#getHttpRequest()
	 */
	public HttpServletRequest getHttpRequest()
	{
		return this.request;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.authn.bean.AuthnProcessorData#getHttpServletResponse()
	 */
	public HttpServletResponse getHttpResponse()
	{
		return this.response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.authn.bean.AuthnProcessorData#getRedirctTarget()
	 */
	public String getRedirectTarget()
	{
		return this.redirectTarget;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.authn.bean.AuthnProcessorData#getSessionIdentifier()
	 */
	public String getSessionID()
	{
		return this.sessionID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.authn.bean.AuthnProcessorData#getSuccessfulAuthn()
	 */
	public boolean getSuccessfulAuthn()
	{
		return this.successfulAuthn;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.authn.bean.AuthnProcessorData#setAutomatedSSO(boolean)
	 */
	public void setAutomatedSSO(boolean automatedSSO)
	{
		this.automatedSSO = automatedSSO;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.authn.bean.AuthnProcessorData#setCurrentHandler(java.lang.String)
	 */
	public void setCurrentHandler(String currentHandler)
	{
		this.currentHandler = currentHandler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.authn.bean.AuthnProcessorData#setFailURL(java.lang.String)
	 */
	public void setInvalidURL(String failURL)
	{
		this.invalidURL = failURL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.authn.bean.AuthnProcessorData#setHttpRequest(javax.servlet.http.HttpServletRequest)
	 */
	public void setHttpRequest(HttpServletRequest request)
	{
		this.request = request;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.authn.bean.AuthnProcessorData#setHttpServletResponse(javax.servlet.http.HttpServletResponse)
	 */
	public void setHttpResponse(HttpServletResponse response)
	{
		this.response = response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.authn.bean.AuthnProcessorData#setRedirectTarget(java.lang.String)
	 */
	public void setRedirectTarget(String redirectTarget)
	{
		this.redirectTarget = redirectTarget;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.authn.bean.AuthnProcessorData#setSessionIdentifier(java.lang.String)
	 */
	public void setSessionID(String sessionIdentifier)
	{
		this.sessionID = sessionIdentifier;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.authn.bean.AuthnProcessorData#getSuccesfulAuthn(boolean)
	 */
	public void setSuccessfulAuthn(boolean successfulAuthn)
	{
		this.successfulAuthn = successfulAuthn;
	}

	
	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.authn.bean.AuthnProcessorData#getPrincipalName()
	 */
	public String getPrincipalName()
	{		
		return this.principalName;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.authn.bean.AuthnProcessorData#setPrincipalName(java.lang.String)
	 */
	public void setPrincipalName(String principalName)
	{
		this.principalName = principalName;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.authn.bean.AuthnProcessorData#getErrorCode()
	 */
	public int getErrorCode()
	{
		return this.errorCode;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.authn.bean.AuthnProcessorData#getErrorMessage()
	 */
	public String getErrorMessage() 
	{
		return this.errorMsg;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.authn.bean.AuthnProcessorData#setError(int, java.lang.String)
	 */
	public void setError(int errorCode, String errorMsg)
	{
		this.errorCode = errorCode;
		this.errorMsg = errorMsg;
	}
}

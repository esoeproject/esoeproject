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
 * Creation Date: 24/10/2006
 * 
 * Purpose: Implementation of bean that transfers data between components of the SSO system
 */

package com.qut.middleware.esoe.sso.bean.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sso.SSOProcessor;
import com.qut.middleware.esoe.sso.bean.SSOProcessorData;
import com.qut.middleware.saml2.schemas.protocol.AuthnRequest;

/** Implementation of bean that transfers data between components of the SSO system. */
public class SSOProcessorDataImpl implements SSOProcessorData
{
	private HttpServletRequest httpRequest;
	private HttpServletResponse httpResponse;
	private RequestMethod requestMethod;

	private AuthnRequest authnRequest;
	private byte[] requestDocument;
	private byte[] responseDocument;
	private String samlBinding;
	private Object bindingData;
	private String responseEndpoint;
	private String requestEncoding;
	private String requestCharsetName;
	private String remoteAddress;
	private String commonCookieValue;
	private SSOAction currentAction;
	private String currentHandler;
	private String issuerID;
	private String sessionID;
	private Principal principal;
	private String spepEntityID;
	private boolean responded;
	private List<String> validIdentifiers;
	private SSOProcessor ssoProcessor;
	private String sessionIndex;

	Stack<String> handlerResetStack;

	public String getSessionIndex()
	{
		return sessionIndex;
	}

	public void setSessionIndex(String sessionIndex)
	{
		this.sessionIndex = sessionIndex;
	}

	public SSOProcessor getSSOProcessor()
	{
		return ssoProcessor;
	}

	public void setSSOProcessor(SSOProcessor processor)
	{
		ssoProcessor = processor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sso.bean.SSOProcessorData#getAuthnRequest()
	 */
	public AuthnRequest getAuthnRequest()
	{
		return this.authnRequest;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sso.bean.SSOProcessorData#getHttpRequest()
	 */
	public HttpServletRequest getHttpRequest()
	{
		return this.httpRequest;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sso.bean.SSOProcessorData#getHttpResponse()
	 */
	public HttpServletResponse getHttpResponse()
	{
		return this.httpResponse;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sso.bean.SSOProcessorData#getRequestDocument()
	 */
	public byte[] getRequestDocument()
	{
		return this.requestDocument;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sso.bean.SSOProcessorData#getResponseDocument()
	 */
	public byte[] getResponseDocument()
	{
		return this.responseDocument;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.qut.middleware.esoe.sso.bean.SSOProcessorData#setAuthnRequest(com.qut.middleware.saml2.schemas.protocol.
	 * AuthnRequest)
	 */
	public void setAuthnRequest(AuthnRequest authnRequest)
	{
		this.authnRequest = authnRequest;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sso.bean.SSOProcessorData#setHttpRequest(javax.servlet.http.HttpServletRequest)
	 */
	public void setHttpRequest(HttpServletRequest httpRequest)
	{
		this.httpRequest = httpRequest;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sso.bean.SSOProcessorData#setHttpResponse(javax.servlet.http.HttpServletResponse)
	 */
	public void setHttpResponse(HttpServletResponse httpResponse)
	{
		this.httpResponse = httpResponse;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sso.bean.SSOProcessorData#setRequestDocument(java.lang.String)
	 */
	public void setRequestDocument(byte[] requestDocument)
	{
		this.requestDocument = requestDocument;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sso.bean.SSOProcessorData#setResponseDocument(java.lang.String)
	 */
	public void setResponseDocument(byte[] responseDocument)
	{
		this.responseDocument = responseDocument;

	}

	public String getSamlBinding()
	{
		return this.samlBinding;
	}

	public void setSamlBinding(String samlBinding)
	{
		this.samlBinding = samlBinding;
	}
	
	public <T> T getBindingData(Class<T> clazz)
	{
		if (this.bindingData == null) return null;
		
		if (clazz.isAssignableFrom(this.bindingData.getClass()))
		{
			return clazz.cast(this.bindingData);
		}
		
		return null;
	}
	
	public <T> void setBindingData(T obj)
	{
		this.bindingData = obj;
	}
	
	public RequestMethod getRequestMethod()
	{
		return this.requestMethod;
	}
	
	public void setRequestMethod(RequestMethod requestMethod)
	{
		this.requestMethod = requestMethod;
	}

	public String getResponseEndpoint()
	{
		return responseEndpoint;
	}

	public void setResponseEndpoint(String responseEndpoint)
	{
		this.responseEndpoint = responseEndpoint;
	}

	public String getRequestEncoding()
	{
		return requestEncoding;
	}

	public void setRequestEncoding(String requestEncoding)
	{
		this.requestEncoding = requestEncoding;
	}

	public String getRequestCharsetName()
	{
		return requestCharsetName;
	}

	public void setRequestCharsetName(String requestCharsetName)
	{
		this.requestCharsetName = requestCharsetName;
	}

	public void setRemoteAddress(String remoteAddress)
	{
		this.remoteAddress = remoteAddress;
	}

	public String getRemoteAddress()
	{
		return remoteAddress;
	}

	public String getCommonCookieValue()
	{
		return commonCookieValue;
	}

	public void setCommonCookieValue(String commonCookieValue)
	{
		this.commonCookieValue = commonCookieValue;
	}

	public SSOAction getCurrentAction()
	{
		return currentAction;
	}

	public void setCurrentAction(SSOAction currentAction)
	{
		this.currentAction = currentAction;
	}

	public String getCurrentHandler()
	{
		return currentHandler;
	}

	public void setCurrentHandler(String currentHandler)
	{
		this.currentHandler = currentHandler;
	}

	public String getIssuerID()
	{
		return issuerID;
	}

	public void setIssuerID(String issuerID)
	{
		this.issuerID = issuerID;
	}

	public String getSessionID()
	{
		return sessionID;
	}

	public void setSessionID(String sessionID)
	{
		this.sessionID = sessionID;
	}

	public Principal getPrincipal()
	{
		return principal;
	}

	public void setPrincipal(Principal principal)
	{
		this.principal = principal;
	}

	public String getSPEPEntityID()
	{
		return spepEntityID;
	}

	public void setSPEPEntityID(String spepEntityID)
	{
		this.spepEntityID = spepEntityID;
	}

	public boolean isResponded()
	{
		return responded;
	}

	public void setResponded(boolean responded)
	{
		this.responded = responded;
	}

	public List<String> getValidIdentifiers()
	{
		return validIdentifiers;
	}

	public void setValidIdentifiers(List<String> validIdentifiers)
	{
		this.validIdentifiers = validIdentifiers;
	}

	public List<String> getHandlerResetHistory() {
		return new ArrayList<String>(this.handlerResetStack);
	}

	public boolean handlerReset(String handlerName, int limit) {
		this.handlerResetStack.push(handlerName);
		return this.handlerResetStack.size() <= limit;
	}

	public void resetHandlerResets() {
		this.handlerResetStack.clear();
	}

}

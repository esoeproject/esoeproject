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

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.qut.middleware.esoe.sso.bean.SSOProcessorData;
import com.qut.middleware.saml2.schemas.protocol.AuthnRequest;

/** Implementation of bean that transfers data between components of the SSO system. */
public class SSOProcessorDataImpl implements SSOProcessorData
{
	private HttpServletRequest httpRequest;
	private HttpServletResponse httpResponse;
	
	private String samlBinding;
	private AuthnRequest authnRequest;
	private String relayState;
	private String samlEncoding;
	private String sigAlg;
	private String signature;
	private String issuerID;
	private String responseEndpoint;
	private int responseEndpointID;
	private String responseURL;
	private byte[] requestDocument;
	private byte[] responseDocument;
	private String sessionID;
	private String requestCharsetName;
	private String samlDomainCookieData;
	private String commonCookieValue;
	private List<String> validIdentifiers;
	
	private boolean returningRequest;

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.sso.bean.SSOProcessorData#getAuthnRequest()
	 */
	public AuthnRequest getAuthnRequest()
	{
		return this.authnRequest;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sso.bean.SSOProcessorData#getDescriptorID()
	 */
	public String getIssuerID()
	{
		return this.issuerID;
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
	 * @see com.qut.middleware.esoe.sso.bean.SSOProcessorData#getResponseEndpoint()
	 */
	public String getResponseEndpoint()
	{
		return this.responseEndpoint;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sso.bean.SSOProcessorData#getResponseEndpointID()
	 */
	public int getResponseEndpointID()
	{
		return this.responseEndpointID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sso.bean.SSOProcessorData#getResponseURL()
	 */
	public String getResponseURL()
	{
		return this.responseURL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sso.bean.SSOProcessorData#getSessionID()
	 */
	public String getSessionID()
	{
		return this.sessionID;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.sso.bean.SSOProcessorData#isReturningRequest()
	 */
	public boolean isReturningRequest()
	{
		return this.returningRequest;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.sso.bean.SSOProcessorData#setAuthnRequest(com.qut.middleware.saml2.schemas.protocol.AuthnRequest)
	 */
	public void setAuthnRequest(AuthnRequest authnRequest)
	{
		this.authnRequest = authnRequest;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sso.bean.SSOProcessorData#setDescriptorID(java.lang.String)
	 */
	public void setIssuerID(String issuerID)
	{
		this.issuerID = issuerID;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sso.bean.SSOProcessorData#setResponseEndpoint(java.lang.String)
	 */
	public void setResponseEndpoint(String responseEndpoint)
	{
		this.responseEndpoint = responseEndpoint;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sso.bean.SSOProcessorData#setResponseEndpointID(java.lang.String)
	 */
	public void setResponseEndpointID(int responseEndpointID)
	{
		this.responseEndpointID = responseEndpointID;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sso.bean.SSOProcessorData#setResponseURL(java.lang.String)
	 */
	public void setResponseURL(String responseURL)
	{
		this.responseURL = responseURL;

	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.sso.bean.SSOProcessorData#setReturningRequest(boolean)
	 */
	public void setReturningRequest(boolean returningRequest)
	{
		this.returningRequest = returningRequest;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sso.bean.SSOProcessorData#setSessionID(java.lang.String)
	 */
	public void setSessionID(String sessionID)
	{
		this.sessionID = sessionID;

	}

	public String getRequestCharsetName()
	{
		return requestCharsetName;
	}

	public void setRequestCharsetName(String requestCharsetName)
	{
		this.requestCharsetName = requestCharsetName;
	}

	public String getRelayState()
	{
		return relayState;
	}

	public void setRelayState(String relayState)
	{
		this.relayState = relayState;
	}

	public String getSamlEncoding()
	{
		return samlEncoding;
	}

	public void setSamlEncoding(String samlEncoding)
	{
		this.samlEncoding = samlEncoding;
	}

	public String getSigAlg()
	{
		return sigAlg;
	}

	public void setSigAlg(String sigAlg)
	{
		this.sigAlg = sigAlg;
	}

	public String getSamlBinding()
	{
		return samlBinding;
	}

	public void setSamlBinding(String samlBinding)
	{
		this.samlBinding = samlBinding;
	}

	public String getSignature()
	{
		return signature;
	}

	public void setSignature(String signature)
	{
		this.signature = signature;
	}

	public String getSamlDomainCookieData()
	{
		return samlDomainCookieData;
	}

	public void setSamlDomainCookieData(String samlDomainCookieData)
	{
		this.samlDomainCookieData = samlDomainCookieData;
	}

	public String getCommonCookieValue()
	{
		return commonCookieValue;
	}

	public void setCommonCookieValue(String commonCookieValue)
	{
		this.commonCookieValue = commonCookieValue;
	}

	public List<String> getValidIdentifiers()
	{
		return validIdentifiers;
	}

	public void setValidIdentifiers(List<String> validIdentifiers)
	{
		this.validIdentifiers = validIdentifiers;
	}

}

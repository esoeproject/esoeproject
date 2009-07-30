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
 * Author: Bradley Beddoes
 * Creation Date: 24/10/2006
 * 
 * Purpose: Transfers data between components of the SSO system
 */
package com.qut.middleware.esoe.sso.bean;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.qut.middleware.esoe.bean.SAMLProcessorData;
import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sso.SSOProcessor;
import com.qut.middleware.saml2.schemas.protocol.AuthnRequest;

/** Transfers data between components of the SSO system.  */
public interface SSOProcessorData extends SAMLProcessorData
{
	/** Session identifier for this data bean */
	public static String SESSION_NAME = "com.qut.middleware.esoe.sso.bean"; //$NON-NLS-1$
	
	public enum RequestMethod {
		/** HTTP standard GET request method */
		HTTP_GET,
		/** HTTP standard POST request method */
		HTTP_POST
	}
	
	public enum SSOAction {
		/** Request processing step - processing the AuthnRequest */
		REQUEST_PROCESSING,
		/** SSO processing step - session validation, entity & session registration, etc. */
		SSO_PROCESSING,
		/** Response processing step - generating the AuthnResponse */
		RESPONSE_PROCESSING
	}
	
	/**
	 * @return The HTTP request method that initiated this request.
	 */
	public RequestMethod getRequestMethod();
	
	/**
	 * @param httpRequestMethod The HTTP request method that initiated this request.
	 */
	public void setRequestMethod(RequestMethod httpRequestMethod);
	
	/**
	 * @return The binding used in this request
	 */
	public String getSamlBinding();
	
	/**
	 * @param samlBinding
	 */
	public void setSamlBinding(String samlBinding);
	
	/**
	 * @return the current unmarshalled AuthnRequest
	 */
	public AuthnRequest getAuthnRequest();
	
	/**
	 * @param authnRequest
	 */
	public void setAuthnRequest(AuthnRequest authnRequest);
	
	/**
	 * @return the current http request object
	 */
	public HttpServletRequest getHttpRequest();

	/**
	 * Sets the httpRequest
	 * @param httpRequest The HTTP request object
	 */
	public void setHttpRequest(HttpServletRequest httpRequest);

	/**
	 * @return the current http response object
	 */
	public HttpServletResponse getHttpResponse();

	/**
	 * Sets the httpResponse
	 * @param httpResponse The HTTP response
	 */
	public void setHttpResponse(HttpServletResponse httpResponse);
	
	/**
	 * @param <T> Type expected to be returned
	 * @param clazz Class object of type expected
	 * @return The binding data bean
	 */
	public <T> T getBindingData(Class<T> clazz);
	
	/**
	 * @param <T> Type of binding data bean
	 * @param obj The binding data bean
	 */
	public <T> void setBindingData(T obj);

	public String getResponseEndpoint();
	
	public void setResponseEndpoint(String responseEndpoint);
	
	public String getRequestCharsetName();

	public void setRequestCharsetName(String detectCharSet);
	
	public String getRemoteAddress();

	public void setRemoteAddress(String remoteAddress);
	
	public SSOAction getCurrentAction();
	
	public void setCurrentAction(SSOAction action);
	
	public String getCurrentHandler();
	
	public void setCurrentHandler(String handler);
	
	public String getCommonCookieValue();
	
	public void setCommonCookieValue(String value);
	
	public String getSessionID();
	
	public void setSessionID(String sessionID);
	
	public Principal getPrincipal();
	
	public void setPrincipal(Principal principal);
	
	public String getIssuerID();
	
	public void setIssuerID(String issuerID);
	
	public List<String> getValidIdentifiers();
	
	public void setValidIdentifiers(List<String> validIdentifiers);
	
	public boolean isResponded();
	
	public void setResponded(boolean responded);
	
	public SSOProcessor getSSOProcessor();
	
	public void setSSOProcessor(SSOProcessor processor);
	
	public String getSessionIndex();

	public void setSessionIndex(String sessionIndex);

	/**
	 * Signals a reset. Used to prevent an overflow of resets if one or more handlers are behaving poorly.
	 * @param handlerName The name of the handler requesting the reset.
	 * @param limit The limit of resets before returning false
	 * @return True if the reset should succeed, or false if the limit has been exceeded.
	 */
	public boolean handlerReset(String handlerName, int limit);

	/**
	 * @return The list of handlers that have issued a reset
	 */
	public List<String> getHandlerResetHistory();

	/**
	 * Resets the "handler resets" tracker.
	 */
	public void resetHandlerResets();
}

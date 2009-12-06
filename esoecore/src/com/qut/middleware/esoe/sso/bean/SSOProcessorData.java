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
import com.qut.middleware.saml2.schemas.protocol.AuthnRequest;

/** Transfers data between components of the SSO system.  */
public interface SSOProcessorData extends SAMLProcessorData
{
	/** Session identifier for this data bean */
	public static String SESSION_NAME = "com.qut.middleware.esoe.sso.bean"; //$NON-NLS-1$
	
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
	 * @return the relay stae of the request
	 */
	public String getRelayState();
	
	/**
	 * @param relayState
	 */
	public void setRelayState(String relayState);
	
	/**
	 * @return the SAML Encoding in use
	 */
	public String getSamlEncoding();
	
	/**
	 * @param samlEncoding
	 */
	public void setSamlEncoding(String samlEncoding);
	
	/**
	 * @return The value to set in the domain cookie
	 */
	public String getSamlDomainCookieData();
	
	/**
	 * @param samlDomainCookieData
	 */
	public void setSamlDomainCookieData(String samlDomainCookieData);
	
	/**
	 * @return The algorithm used to create the signature for HTTP Redirect binding
	 */
	public String getSigAlg();
	
	/**
	 * @param sigAlg
	 */
	public void setSigAlg(String sigAlg);
	
	/*
	 * @return The signature of the request for HTTP Redirect binding
	 */
	public String getSignature();
	
	/**
	 * @param sigAlg
	 */
	public void setSignature(String signature);
	
	/**
	 * @return the issuer ID of the communicating SPEP
	 */
	public String getIssuerID();

	/**
	 * @return the current http request object
	 */
	public HttpServletRequest getHttpRequest();

	/**
	 * @return the current http response object
	 */
	public HttpServletResponse getHttpResponse();

	/**
	 * @return the resolved endpoint of the spep instance it wishes us to respond to
	 */
	public String getResponseEndpoint();

	/**
	 * @return the endpoint id supplied in the request
	 */
	public int getResponseEndpointID();

	/**
	 * @return the url we should send the principal back to on response
	 */
	public String getResponseURL();

	/**
	 * @return the principals authn generated sessionID
	 */
	public String getSessionID();
	
	/**
	 * @return representation as to if this sso request has previously had processing started or not
	 */
	public boolean isReturningRequest();

	/**
	 * Sets the issuerID
	 * @param issuerID The issuer ID
	 */
	public void setIssuerID(String issuerID);

	/**
	 * Sets the httpRequest
	 * @param httpRequest The HTTP request object
	 */
	public void setHttpRequest(HttpServletRequest httpRequest);

	/**
	 * Sets the httpResponse
	 * @param httpResponse The HTTP response
	 */
	public void setHttpResponse(HttpServletResponse httpResponse);

	/**
	 * Sets the responseEndpoint
	 * @param responseEndpoint The response endpoint
	 */
	public void setResponseEndpoint(String responseEndpoint);

	/**
	 * Sets the responseEndpointID
	 * @param responseEndpointID The response endpoint ID
	 */
	public void setResponseEndpointID(int responseEndpointID);

	/**
	 * Sets the responseURL
	 * @param responseURL The response URL
	 */
	public void setResponseURL(String responseURL);

	/**
	 * Sets the sessionID
	 * @param sessionID The session ID
	 */
	public void setSessionID(String sessionID);
	
	/**
	 * Sets the returningRequest
	 * @param returningRequest the returningRequest value
	 */
	public void setReturningRequest(boolean returningRequest);
			
	/**
	 * @return The charset used by the caller
	 */
	public String getRequestCharsetName();
	
	/**
	 * @param name
	 */
	public void setRequestCharsetName(String name);
	
	public String getCommonCookieValue();
	
	public void setCommonCookieValue(String commonCookieValue);
	
	public List<String> getValidIdentifiers();
	
	public void setValidIdentifiers(List<String> validIdentifiers);
	
}

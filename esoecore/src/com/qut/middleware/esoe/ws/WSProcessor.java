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
 * Creation Date: 04/12/2006
 * 
 * Purpose: Defines requirements for all web service interfaces on the ESOE
 */
package com.qut.middleware.esoe.ws;

import com.qut.middleware.esoe.ws.exception.WSProcessorException;

public interface WSProcessor
{
	
	/**
	 * Function which Axis invokes on the esoe service for the operation attributeAuthority
	 * 
	 * @param attributeQuery SOAP document containing envelope with attribute query
	 * @return SOAP document containing some form of response
	 */
	public byte[] attributeAuthority(byte[] attributeQuery, String contentType) throws WSProcessorException;
	
	/**
	 * Function which Axis invokes on the esoe service for the operation policyDecisionPoint
	 * 
	 * @param decisionRequest SOAP document containing envelope with policy query
	 * @return An Axiom OMElement representation of the response contents for Soap:Body
	 */
	public byte[] policyDecisionPoint(byte[] decisionRequest, String contentType) throws WSProcessorException;
	
	/**
	 * Function which Axis invokes on the esoe service for the operation spepStartup
	 * 
	 * @param spepStartup SOAP document containing envelope with SPEP startup query
	 * @return An Axiom OMElement representation of the response contents for Soap:Body
	 */
	public byte[] spepStartup(byte[] spepStartup, String contentType) throws WSProcessorException;
	
	/**
	 * Function which Axis invokes on the esoe service for the operation registerPrincipal
	 * 
	 * @param spepStartup SOAP document containing envelope with register principal query
	 * @return An Axiom OMElement representation of the response contents for Soap:Body
	 */
	public byte[] registerPrincipal(byte[] registerPrincipal, String contentType) throws WSProcessorException;
}

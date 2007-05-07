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

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;

public interface WSProcessor
{
	
	/**
	 * Function which Axis invokes on the esoe service for the operation attributeAuthority
	 * 
	 * @param attributeQuery Axiom OMElement representation of the contents of Soap:Body
	 * @return An Axiom OMElement representation of the response contents for Soap:Body
	 */
	public OMElement attributeAuthority(OMElement attributeQuery) throws AxisFault;
	
	/**
	 * Function which Axis invokes on the esoe service for the operation policyDecisionPoint
	 * 
	 * @param decisionRequest Axiom OMElement representation of the contents of Soap:Body
	 * @return An Axiom OMElement representation of the response contents for Soap:Body
	 */
	public OMElement policyDecisionPoint(OMElement decisionRequest) throws AxisFault;
	
	/**
	 * Function which Axis invokes on the esoe service for the operation spepStartup
	 * 
	 * @param spepStartup Axiom OMElement representation of the contents of Soap:Body
	 * @return An Axiom OMElement representation of the response contents for Soap:Body
	 */
	public OMElement spepStartup(OMElement spepStartup) throws AxisFault;
	
	/**
	 * Function which Axis invokes on the esoe service for the operation registerPrincipal
	 * 
	 * @param spepStartup Axiom OMElement representation of the contents of Soap:Body
	 * @return An Axiom OMElement representation of the response contents for Soap:Body
	 */
	public OMElement registerPrincipal(OMElement registerPrincipal) throws AxisFault;
}

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
 * Creation Date: 28/09/2006
 * 
 * Purpose: A data type to hold information relating to an authorization request as handled
 * by the <code>AuthorizationProcessor</code>.
 * 
 */
package com.qut.middleware.esoe.authz.bean;

import com.qut.middleware.esoe.ws.bean.WSProcessorData;

/** */
public interface AuthorizationProcessorData extends WSProcessorData
{

	/**
	 * Sets the issuer identifier of the authorization request. The issuer ID is used to identify
	 * a service authorization policy. See ESOE specification <a href=https://wiki.qut.
	 * edu.au/display/ICC/ESOE+Design#ESOEDesign-AuthorizationProcessor> LXACMLSchema </a>.
	 * 
	 * @param issuerID The issuer identifier.
	 */
	public void setIssuerID(String issuerID);
	

	/**
	 * Gets the issuer identifier of the authorization request. The issuer ID is used to identify a 
	 * lxacml authorization policy.
	 * @return The descriptor identifier
	 */
	public String getIssuerID();
	

	/** Set the subject ID string for the authorization data. The subjectID is used to identify
	 * a SAML 2.0 <code>Principal</code>.
	 * 
	 * @param ID The subject identifier of the SAML 2.0 principal.
	 */
	public void setSubjectID(String ID);
	

	/** Get the subject ID string for the authorization data. The subjectID is used to identify
	 * a SAML 2.0 <code> Principal </code>.
	 * 
	 * @return The subject identifier associated with this object.
	 */
	public String getSubjectID();
	
}

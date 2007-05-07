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
 * Creation Date: 30/10/2006
 * 
 * Purpose: Base requirements interface for all processor data beans in the system that interact with SAML documents
 */
package com.qut.middleware.esoe.bean;

/** */
public interface SAMLProcessorData 
{
	/** Accessor for SAML 2.0 request document.
	 * 
	 * @return request document for this processor data bean
	 */
	public String getRequestDocument();
	
	/**
	 * Mutator for SAML 2.0 request document.
	 * 
	 * @param requestDocument The SAML request to store in this processor data bean
	 */
	public void setRequestDocument(String requestDocument);
	
	/** Accessor for SAML 2.0 response document.
	 *  
	 * @return response document for this processor data bean
	 */
	public String getResponseDocument();
	
	/** Mutator for SAML 2.0 response document.
	 * 
	 * @param responseDocument The SAML response to store in this processor data bean
	 */
	public void setResponseDocument(String responseDocument);
}

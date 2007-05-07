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
 * Creation Date: 06/03/2007
 * 
 * Purpose: Implementation of DelegatedAuthentcationData interface
 */

package com.qut.middleware.esoe.delegauthn.bean.impl;

import com.qut.middleware.esoe.delegauthn.bean.DelegatedAuthenticationData;
import com.qut.middleware.saml2.schemas.esoe.delegated.RegisterPrincipalRequest;

public class DelegatedAuthenticationDataImpl implements DelegatedAuthenticationData
{
	private String requestDocument;
	private String responseDocument;
	private RegisterPrincipalRequest registerPrincipalRequest;
	
	public String getRequestDocument()
	{
		return this.requestDocument;
	}

	public String getResponseDocument()
	{
		return this.responseDocument;
	}

	public void setRequestDocument(String requestDocument)
	{
		this.requestDocument = requestDocument;
	}

	public void setResponseDocument(String responseDocument)
	{
		this.responseDocument = responseDocument;
	}

	public RegisterPrincipalRequest getRegisterPrincipalRequest()
	{
		return this.registerPrincipalRequest;
	}

	public void setRegisterPrincipalRequest(RegisterPrincipalRequest registerPrincipalRequest)
	{
		this.registerPrincipalRequest = registerPrincipalRequest;
	}

}

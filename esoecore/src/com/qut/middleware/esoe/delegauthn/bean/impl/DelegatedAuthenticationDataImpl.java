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
	private byte[] requestDocument;
	private byte[] responseDocument;
	private RegisterPrincipalRequest registerPrincipalRequest;
	
	public byte[] getRequestDocument()
	{
		return this.requestDocument;
	}

	public byte[] getResponseDocument()
	{
		return this.responseDocument;
	}

	public void setRequestDocument(byte[] requestDocument)
	{
		this.requestDocument = requestDocument;
	}

	public void setResponseDocument(byte[] responseDocument)
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

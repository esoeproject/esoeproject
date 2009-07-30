/* Copyright 2006-2008, Queensland University of Technology
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
 * Creation Date: 28/09/2006
 * 
 * Purpose: Interface specifying operations that must be present on implemented logic to handle SAML SSO interactions for authn and single logout
 */
package com.qut.middleware.esoe.sso;

import org.w3c.dom.Element;

import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sso.bean.SSOProcessorData;
import com.qut.middleware.esoe.sso.exception.InvalidRequestException;
import com.qut.middleware.esoe.sso.exception.InvalidSessionIdentifierException;
import com.qut.middleware.esoe.sso.exception.SSOException;
import com.qut.middleware.saml2.exception.MarshallerException;
import com.qut.middleware.saml2.exception.ReferenceValueException;
import com.qut.middleware.saml2.exception.SignatureValueException;
import com.qut.middleware.saml2.exception.UnmarshallerException;
import com.qut.middleware.saml2.schemas.protocol.AuthnRequest;
import com.qut.middleware.saml2.schemas.protocol.Response;

/** Interface specifying operations that must be present on implemented logic to handle SAML SSO interactions for authn and single logout. */
public interface SSOProcessor
{
	/** Results that this interface will return from its publicly facing methods */
	public static enum result
	{
		/** Indicates that the SSO Generation was successful */
		SSOGenerationSuccessful, 
		/** SSO generation failed */
		SSOGenerationFailed,
		/** Force authentication using a passive handler */
		ForcePassiveAuthn,
		/** Force authentication using a non-passive handler */
		ForceAuthn
	};
	
	/** Perform required logic.
	 * 
	 * @param data Instantiated and partially populated data bean to operate on
	 * @return A SSOProcessor.result value indicating the result of the execute function on the supplied request
	 * @throws SSOException
	 */
	public result execute(SSOProcessorData data) throws SSOException;
	
	public AuthnRequest unmarshallRequest(byte[] requestDocument, boolean signed) throws SignatureValueException, ReferenceValueException, UnmarshallerException;
	public AuthnRequest unmarshallRequest(Element requestDocument, boolean signed) throws SignatureValueException, ReferenceValueException, UnmarshallerException;
	public byte[] marshallResponse(Response responseObject, boolean signed, String charset) throws MarshallerException;
	
	public void processAuthnRequest(SSOProcessorData data) throws SSOException;
	
	public byte[] createSuccessfulAuthnResponse(SSOProcessorData data, String sessionIndex, String charset, boolean signed) throws SSOException;
	public Element createSuccessfulAuthnResponseElement(SSOProcessorData data, String sessionIndex, String charset, boolean signed) throws SSOException;
	public byte[] createStatusAuthnResponse(SSOProcessorData data, String statusValue, String detailedStatusValue, String statusMessage, boolean signed) throws SSOException;
}

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
 * Creation Date: 17/1/2007
 * 
 * Purpose: Interface to an object capable of authenticating from an SPNEGO token.
 */
package com.qut.middleware.esoe.authn.pipeline;

public interface SPNEGOAuthenticator extends Authenticator
{
	
	/** Authenticate the principal based on data contained in the SPNEGO token.
	 * 
	 * @param spnegoData The String that contains the GSS token to be evaluated for
	 * user authentication. The string MUST be a valid SPNEGO wrapped GSS token encoded
	 * in base64 for authentication to succeed. See RFC 1964 && 4121 for details.
	 * 
	 * @return A string representation of the authenticated principal if auth succeeds,
	 * else null. The value of context.getSrcName() for an established GSS context.
	 *
	 */
	public String authenticate(String spnegoData);
}

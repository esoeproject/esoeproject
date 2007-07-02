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

public interface UserPassAuthenticator extends Authenticator
{
	
	/**
	 * Attempts to authenticate the principal based on userIdentifier and provided password.
	 * 
	 * @param userIdentifier Representation of the user which for which they are known on the backend authentication system
	 * @param password Users password value
	 * @return Result of the authentication call to backend auth provider
	 */
	public result authenticate(String userIdentifier, String password);
}

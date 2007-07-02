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
 * Creation Date: 06/03/2007
 * 
 * Purpose: Provides authentication and identity setup services for principals via openID
 */
package com.qut.middleware.delegator.openid.authn;

import com.qut.middleware.delegator.openid.authn.bean.AuthnProcessorData;

public interface AuthnProcessor
{
	/** Results that this interface will return from its publicly facing methods */
	public static enum result
	{
		/** Indicates that the authentication operation was completed successfully */
		Completed,
		/** Indicates a failure (but client interaction expected failure) occurred while processing the authentication operation */
		Failure,
		NoOp
	}
	
	public result execute(AuthnProcessorData processorData); 
}

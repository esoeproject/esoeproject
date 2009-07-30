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
 * Author: Shaun Mangelsdorf
 * Creation Date: 25/08/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.esoe.sso.pipeline;

import com.qut.middleware.esoe.sso.bean.SSOProcessorData;

public interface Handler
{
	public static enum result
	{
		/** Processed successfully */
		Successful,
		/** The binding was determined but the request was deemed invalid */
		InvalidRequest,
		/** The binding handler was unwilling to respond */
		UnwillingToRespond,
		/** No action was taken, and no error occurred */
		NoAction,
		/** Some state has changed that requires the current line of processing to be reset */
		Reset
	};

	public result executeRequest(SSOProcessorData data);
	
	public result executeResponse(SSOProcessorData data);
	
	public String getHandlerName();
}

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
 * Creation Date: 25/10/2006
 * 
 * Purpose: Provides an central place for other packages to interface with the SPEP Processor
 */
package com.qut.middleware.esoe.spep;

import com.qut.middleware.esoe.sessions.Principal;

/** Provides an central place for other packages to interface with the SPEP Processor. */

public interface SPEPProcessor
{
	/**
	 * @return Metadata instance.
	 * @deprecated Use the shared Metadata object directly.
	 * 
	 */
	//@Deprecated
	//public Metadata getMetadata();
	
	/**
	 * @return SPEP startup instance.
	 */
	public Startup getStartup();
	
	/**
	 * @param principal The principal object for which session data is to be cleared
	 * @param descriptors
	 */
	public void clearPrincipalSPEPCaches(Principal principal);
}

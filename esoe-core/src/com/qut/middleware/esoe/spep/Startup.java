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
 * Creation Date: 02/11/2006
 * 
 * Purpose: Provides an interface to register the startup of SPEPs with the ESOE
 */
package com.qut.middleware.esoe.spep;

import com.qut.middleware.esoe.spep.bean.SPEPProcessorData;
import com.qut.middleware.esoe.spep.exception.DatabaseFailureException;
import com.qut.middleware.esoe.spep.exception.DatabaseFailureNoSuchSPEPException;
import com.qut.middleware.esoe.spep.exception.InvalidRequestException;
import com.qut.middleware.esoe.spep.exception.SPEPCacheUpdateException;

/** Provides an interface to register the startup of SPEPs with the ESOE. */
public interface Startup
{
	/**
	 * Registers an SPEP upon starting up.
	 * @param data The data containing the request document for the SPEP startup.
	 * @throws InvalidRequestException
	 * @throws DatabaseFailureNoSuchSPEPException
	 * @throws SPEPCacheUpdateException
	 * @throws DatabaseFailureException 
	 */
	public void registerSPEPStartup(SPEPProcessorData data) throws InvalidRequestException, DatabaseFailureNoSuchSPEPException, SPEPCacheUpdateException, DatabaseFailureException;
}

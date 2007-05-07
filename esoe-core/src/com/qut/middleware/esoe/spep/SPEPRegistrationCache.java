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
 * Author: Shaun Mangelsdorf
 * Creation Date: 03/11/2006
 * 
 * Purpose: Provides an interface for the SPEP Processor to register SPEPs as they
 * 		start up.
 */
package com.qut.middleware.esoe.spep;

import com.qut.middleware.esoe.spep.exception.DatabaseFailureException;
import com.qut.middleware.esoe.spep.exception.DatabaseFailureNoSuchSPEPException;
import com.qut.middleware.esoe.spep.exception.InvalidRequestException;
import com.qut.middleware.esoe.spep.exception.SPEPCacheUpdateException;
import com.qut.middleware.saml2.schemas.esoe.protocol.ValidateInitializationRequest;

/** Provides an interface for the SPEP Processor to register SPEPs as they
 * 		start up.*/
public interface SPEPRegistrationCache
{
	/**
	 * Registers that an SPEP is starting up.
	 * @param data The data in the validation request from the SPEP
	 * @throws InvalidRequestException if the ValidateInitializationRequest fails xml validation.
	 * @throws DatabaseFailureNoSuchSPEPException if the SPEP configuration data cannot be found.
	 * @throws DatabaseFailureException if there is an error connected to underlying data source.
	 * @throws SPEPCacheUpdateException if the ESOE cannot send a clearCache Request to the SPEP 
	 * requesting registration.
	 */
	public void registerSPEP(ValidateInitializationRequest data) throws InvalidRequestException, DatabaseFailureNoSuchSPEPException, DatabaseFailureException, SPEPCacheUpdateException;
}

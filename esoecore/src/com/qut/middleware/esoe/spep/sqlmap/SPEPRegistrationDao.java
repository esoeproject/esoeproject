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
 * Creation Date: 17/11/2006
 * 
 * Purpose: Interface for the Data Access Object that manipulates SPEP registration data.
 */
package com.qut.middleware.esoe.spep.sqlmap;

import com.qut.middleware.esoe.spep.exception.DatabaseFailureException;
import com.qut.middleware.esoe.spep.exception.DatabaseFailureNoSuchSPEPException;
import com.qut.middleware.esoe.spep.exception.SPEPCacheUpdateException;
import com.qut.middleware.esoe.spep.sqlmap.impl.SPEPRegistrationData;
import com.qut.middleware.esoe.spep.sqlmap.impl.SPEPRegistrationQueryData;

/** Interface for the Data Access Object that manipulates SPEP registration data. */

public interface SPEPRegistrationDao
{
	/**
	 * Get data repository mapping for the supplied entityID
	 * @param entityID Service EntityID from metadata
	 * @return entID to use when communicating with data repository
	 * @throws SPEPRegistrationDAOException
	 */
	public Integer getEntID(String entityID) throws DatabaseFailureNoSuchSPEPException;
	
	
	/** Retrieve the data for an SPEP and map into returned data type.
	 * 
	 * @param queryData The query data to be used to find the SPEP registration data
	 * @return The SPEP registration data
	 * @throws DatabaseFailureException
	 */
	public SPEPRegistrationData getSPEPRegistration(SPEPRegistrationQueryData queryData) throws DatabaseFailureException;
	
	
	/** Insert an SPEP registration record.
	 * 
	 * @param record The record to insert in the database
	 * @throws SPEPCacheUpdateException
	 */
	public void insertSPEPRegistration(SPEPRegistrationData record) throws SPEPCacheUpdateException;
	
	
	/** Update an  exisiting SPEP registration record.
	 * 
	 * @param record The record to update in the database
	 * @throws SPEPCacheUpdateException
	 */
	public void updateSPEPRegistration(SPEPRegistrationData record) throws SPEPCacheUpdateException;	
}
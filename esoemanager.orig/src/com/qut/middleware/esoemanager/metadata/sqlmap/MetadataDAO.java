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
 * Creation Date: 1/5/07
 * 
 * Purpose: Metadata repository data access object
 */
package com.qut.middleware.esoemanager.metadata.sqlmap;

import java.util.List;
import java.util.Map;

import com.qut.middleware.esoemanager.exception.MetadataDAOException;
import com.qut.middleware.esoemanager.exception.SPEPDAOException;

public interface MetadataDAO
{
	/**
	 * Returns the entityID mapping from the data repository for a given entID
	 * @param entID stored in data repository
	 * @return The value for entityID for use in metadata generation
	 * @throws SPEPDAOException
	 */
	public String getEntityID(Integer entID) throws MetadataDAOException;
	
	public List<Integer> queryActiveEntities()  throws MetadataDAOException;
	
	public List<Map<String, String>> queryContacts(Integer entID) throws MetadataDAOException;
	
	public List<Map<String, Object>> queryIDPDescriptor(Integer entID) throws MetadataDAOException;
	
	public List<Map<String, Object>> querySPDescriptors(Integer entID) throws MetadataDAOException;
	
	public List<Map<String, Object>> queryAttributeAuthorityDescriptor(Integer entID) throws MetadataDAOException;
	
	public List<Map<String, Object>> queryLXACMLPDPDescriptor(Integer entID) throws MetadataDAOException;
	
	public List<Map<String, Object>> queryDescriptorActivePublicKeys(Integer descID) throws MetadataDAOException;
	
}

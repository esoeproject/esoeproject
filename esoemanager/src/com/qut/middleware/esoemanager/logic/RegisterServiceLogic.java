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
 * Purpose:  Registers a new service and all associated details in backend data store
 */
package com.qut.middleware.esoemanager.logic;

import com.qut.middleware.esoemanager.bean.ServiceBean;
import com.qut.middleware.esoemanager.exception.RegisterServiceException;

public interface RegisterServiceLogic
{
	/**
	 * Creates a new service in the authentication system
	 * @param bean Populated Service bean with all details of new service
	 * @return The serviceID (entityID) of the service that has been created
	 * @throws RegisterServiceException
	 */
	public String execute(ServiceBean bean)  throws RegisterServiceException;
}

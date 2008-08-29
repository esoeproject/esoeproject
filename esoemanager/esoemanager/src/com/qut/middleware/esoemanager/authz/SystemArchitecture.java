/* Copyright 2008, Queensland University of Technology
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
 */
package com.qut.middleware.esoemanager.authz;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class SystemArchitecture
{
	/**
	 * A listing is any attempt to retrieve details about all possible services using
	 * RPC or any future supported method at the business logic layer.
	 */
	@Pointcut("execution(* com.qut.middleware.esoemanager.manager.logic.Service.retrieveSimpleServiceListing(..))")
	public void verifyListing() {}
	
	/**
	 * Any method which specifies a serviceID as it's parameter will be matched by this pointcut
	 */
	@Pointcut("execution(* com.qut.middleware.esoemanager.manager.logic..*.*(..))")
	public void verifyServiceAccess() {}
	
	/**
	 * Determine if the user is a super user or not for UI manipulation
	 */
	@Pointcut("execution(* com.qut.middleware.esoemanager.EsoeManager.isSuperUser())")
	public void determineSuperUser() {}
}

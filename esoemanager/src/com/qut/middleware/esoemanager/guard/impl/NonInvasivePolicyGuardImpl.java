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
 * Purpose: very basic policy guard implementation, imply allows edit to continue
 */
package com.qut.middleware.esoemanager.guard.impl;

import com.qut.middleware.esoemanager.exception.PolicyGuardException;
import com.qut.middleware.esoemanager.guard.PolicyGuard;

public class NonInvasivePolicyGuardImpl implements PolicyGuard
{

	public void validatePolicy(String submittedPolicy) throws PolicyGuardException
	{
		/* This policy guard just accepts everything, we'll demonstrate more advanced 
		 * policy guards over the coming weeks
		 */
	}

}

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
package com.qut.middleware.esoemanager.manager.logic;

import com.qut.middleware.esoemanager.exception.ToggleException;

public interface ToggleState
{
	public void toggleServiceState(String serviceID, boolean active) throws ToggleException;
	
	public void toggleNodeState(String serviceID, String nodeIdentifier, boolean active) throws ToggleException;
	
	public void toggleServicePolicyState(String serviceID, String policyID, boolean active) throws ToggleException;
}

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

import java.util.List;

import com.qut.middleware.esoemanager.client.rpc.bean.Policy;
import com.qut.middleware.esoemanager.exception.CreateServicePolicyException;
import com.qut.middleware.esoemanager.exception.DeleteServicePolicyException;
import com.qut.middleware.esoemanager.exception.RetrieveServicePolicyException;
import com.qut.middleware.esoemanager.exception.SaveServicePolicyException;

public interface ServicePolicies
{
	public Policy retrievePolicy(String serviceID, String policyID) throws RetrieveServicePolicyException;
	public String retrievePolicyXML(String serviceID, String policyID) throws RetrieveServicePolicyException;
	
	public List<Policy> retrievePolicies(String serviceID) throws RetrieveServicePolicyException;
	
	public void savePolicy(String serviceID, Policy policy) throws SaveServicePolicyException;
	public void savePolicyXML(String serviceID, String policy) throws SaveServicePolicyException;
	
	public String createPolicy(String serviceID, Policy policy) throws CreateServicePolicyException;
	public String createPolicyXML(String serviceID, String policy) throws CreateServicePolicyException;
	
	public void deletePolicy(String serviceID, String policyID) throws DeleteServicePolicyException;
}

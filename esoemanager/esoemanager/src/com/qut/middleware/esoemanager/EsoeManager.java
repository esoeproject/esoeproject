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
package com.qut.middleware.esoemanager;

import com.qut.middleware.esoemanager.manager.logic.Attributes;
import com.qut.middleware.esoemanager.manager.logic.Dashboard;
import com.qut.middleware.esoemanager.manager.logic.Service;
import com.qut.middleware.esoemanager.manager.logic.ServiceContacts;
import com.qut.middleware.esoemanager.manager.logic.ServiceCrypto;
import com.qut.middleware.esoemanager.manager.logic.ServiceNodes;
import com.qut.middleware.esoemanager.manager.logic.ServicePolicies;
import com.qut.middleware.esoemanager.manager.logic.ToggleState;
import com.qut.middleware.esoemanager.metadata.logic.MetadataCache;
import com.qut.middleware.esoemanager.util.UtilFunctions;

public interface EsoeManager
{

	/**
	 * @see Security pointcut associated with this, return false by default in case of problems 
	 */
	public Boolean isSuperUser();

	public String getCompleteMD();

	public ToggleState getToggleState();

	public void setToggleState(ToggleState toggleState);

	public Service getService();

	public void setService(Service service);

	public ServiceContacts getServiceContacts();

	public void setServiceContacts(ServiceContacts serviceContacts);

	public ServiceNodes getServiceNodes();

	public void setServiceNodes(ServiceNodes serviceNodes);

	public ServicePolicies getServicePolicies();

	public void setServicePolicies(ServicePolicies servicePolicies);

	public ServiceCrypto getServiceCrypto();

	public void setServiceCrypto(ServiceCrypto serviceCrypto);

	public Attributes getAttributes();

	public void setAttributes(Attributes attributes);

	public Dashboard getDashboard();

	public void setDashboard(Dashboard dashboard);

	public MetadataCache getMetadataCache();

	public void setMetadataCache(MetadataCache metadataCache);

	public UtilFunctions getUtils();

	public void setUtils(UtilFunctions utils);

}
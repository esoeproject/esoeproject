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
 * Purpose: Bean to store service  node details
 */
package com.qut.middleware.esoemanager.bean;

public interface ServiceNodeBean
{
	public String getNodeID();
	
	public void setNodeID(String nodeID);
	
	public String getAssertionConsumerService();

	public void setAssertionConsumerService(String assertionConsumerService);

	public String getCacheClearService();

	public void setCacheClearService(String cacheClearService);

	public String getNodeURL();

	public void setNodeURL(String nodeURL);

	public String getSingleLogoutService();

	public void setSingleLogoutService(String singleLogoutService);

}
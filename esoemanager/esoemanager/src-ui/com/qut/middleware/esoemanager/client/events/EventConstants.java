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
package com.qut.middleware.esoemanager.client.events;

public class EventConstants extends com.qut.gwtuilib.client.eventdriven.events.EventConstants
{
	public static String showService = "showService";
	public static String showServiceList = "showServiceList";
	public static String showServiceDetails = "showServiceDetails";

	public static String createPolicy = "createPolicy";
	public static String cancelPolicyCreation = "cancelPolicyCreation";
	public static String successfulPolicyCreation = "successfulPolicyCreation";
	
	public static String updatePolicy = "updatePolicy";
	
	public static String savedPolicy = "savedPolicy";
	public static String savePolicyFailure = "savePolicyFailure";
	
	public static String deletedPolicy = "deletedPolicy";
	public static String deletePolicyFailure = "deletedPolicyFailure";
	
	public static String configurationChange = "configurationChange";
}

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
package com.qut.gwtuilib.client.eventdriven.events;

public class EventConstants
{	
	/* Used to send message to UI message display component */
	public static String userMessage = "userMessage";
	
	/* Content save events */
	public static String beginSaveContent = "beginSaveContent";
	public static String saveContent = "saveContent";
	public static String saveContentVersion = "saveContentVersion";
	public static String saveContentLocation = "saveContentLocation";
	public static String saveSuccessful = "saveSuccessful";
	public static String saveContentVersionSuccessful = "saveContentVersionSuccessful";
	public static String saveContentLocationSuccessful = "saveContentVersionSuccessful";
	public static String saveFailed = "saveFailed";
	public static String saveContentVersionFailed = "saveContentVersionFailed";
	public static String saveContentLocationFailed = "saveContentLocationFailed";
	
	public static String integratedTextBoxUpdated = "integratedTextBoxUpdated";
	public static String integratedTextBoxInvalid = "integratedTextBoxInvalid";
	public static String integratedTextBoxCancel = "integratedTextBoxCancel";
	
	public static String integratedMultiValueTextBoxValueAdded = "integratedMultiValueTextBoxValueAdded";
	public static String integratedMultiValueTextBoxValueRemoved = "integratedMultiValueTextBoxValueRemoved";
	
	public static String integratedTextAreaUpdated = "integratedTextAreaUpdated";
	public static String integratedTextAreaInvalid = "integratedTextAreaInvalid";
	public static String integratedTextAreaCancel = "integratedTextAreaCancel";
	
	public static String integratedListBoxUpdated = "integratedListBoxUpdated";
	public static String integratedListBoxInvalid = "integratedListBoxInvalid";
	public static String integratedListBoxCancel = "integratedListBoxCancel";
	
	public static String repositoryRenderererSelectionMade = "repositoryRenderereeSelectionMade";
	
	public static String themeSelectedEvent = "themeSelectedEvent";
}

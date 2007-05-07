/* Copyright 2006, Queensland University of Technology
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
 * Author: Shaun Mangelsdorf
 * Creation Date: 19/10/2006
 * 
 * Purpose: Interface for the data bean that contains data to be used by the
 * 		attribute authority.
 */
package com.qut.middleware.esoe.aa.bean;

import com.qut.middleware.esoe.bean.SAMLProcessorData;

/** */
public interface AAProcessorData extends SAMLProcessorData
{
	/** Accessor for descriptor ID. A descriptorID is used to identify SPEP configuration.
	 * 
	 * @return String descriptor ID
	 */
	public String getDescriptorID();
	
	
	/** Mutator for descriptor ID
	 * 
	 * @param descriptorID String descriptor ID. A descriptorID is used to identify SPEP configuration.
	 */
	public void setDescriptorID(String descriptorID);
	/**
	 * Accessor for subject ID
	 * @return String subject ID
	 */
	
	/** A subjectID is used to identify the principal for which attributes will be retrieved.
	 * 
	 * @return String subject ID
	 */
	public String getSubjectID();
	
	
	/** A subjectID is used to identify the principal for which attributes will be retrieved.
	 * 
	 * @param subjectID String subject ID
	 */
	public void setSubjectID(String subjectID);
}

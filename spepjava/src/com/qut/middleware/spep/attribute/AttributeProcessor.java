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
 * Author: Shaun Mangelsdorf
 * Creation Date: 09/11/2006
 * 
 * Purpose: Interface for the attribute processor component of the SPEP.
 */
package com.qut.middleware.spep.attribute;

import com.qut.middleware.spep.exception.AttributeProcessingException;
import com.qut.middleware.spep.sessions.PrincipalSession;

/** Interface for the attribute processor component of the SPEP. */
public interface AttributeProcessor 
{
	/** Resolves the attributes for the client session. Uses the subject ID in the given
	 * PrincipalSession to resolve attributes and populates it with retrieved values.
	 * 
	 * @param principalSession The client session to resolve attributes for.
	 * @throws AttributeProcessingException if an error occurs retrieveing client attributes.
	 */
	public void doAttributeProcessing(PrincipalSession principalSession) throws AttributeProcessingException;
}

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
 * Purpose: Allows client to modify state between active and inactive (removing and inserting to active metadata)
 */
package com.qut.middleware.esoemanager.logic;

import com.qut.middleware.esoemanager.exception.ModifyServiceStateException;

public interface ModifyServiceStateLogic
{

	public abstract void setActive(Integer entID) throws ModifyServiceStateException;

	public abstract void setInActive(Integer entID) throws ModifyServiceStateException;

}
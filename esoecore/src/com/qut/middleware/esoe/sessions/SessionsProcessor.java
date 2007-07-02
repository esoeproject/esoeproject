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
 * Creation Date: 28/09/2006
 * 
 * Purpose: Front end to the Sessions Processor component of the ESOE.
 */
package com.qut.middleware.esoe.sessions;

/** */
public interface SessionsProcessor
{
	/**
	 * Accessor method for the concrete Create object.
	 * 
	 * @return Concrete Create implementation.
	 */
	public Create getCreate();

	/**
	 * Accessor method for the concrete Query object.
	 * 
	 * @return Concrete Query implementation.
	 */
	public Query getQuery();

	/**
	 * Accessor method for the concrete Terminate object.
	 * 
	 * @return Concrete Terminate implementation.
	 */
	public Terminate getTerminate();

	/**
	 * Accessor method for the concrete Update object.
	 * 
	 * @return Concrete Update implementation.
	 */
	public Update getUpdate();
}

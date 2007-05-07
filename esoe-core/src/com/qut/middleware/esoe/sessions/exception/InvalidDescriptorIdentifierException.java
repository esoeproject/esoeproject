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
 * Purpose: Thrown when a descriptor identifier is given that does not refer to an
 * active identity for a principal.
 */
package com.qut.middleware.esoe.sessions.exception;

/**
 * Thrown when a descriptor identifier is given that does not refer to an
 * active identity for a principal.
 *  */
public class InvalidDescriptorIdentifierException extends Exception
{
	private static final long serialVersionUID = 5305305865602847214L;
}

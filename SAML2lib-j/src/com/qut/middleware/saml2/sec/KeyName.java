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
 * Creation Date: 17/11/2006
 * 
 * Purpose: Ensures creation of a KeyName is simplified for library programmers, abstracts away JAXBElement requirements
 */
package com.qut.middleware.saml2.sec;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

/** Ensures creation of a KeyName is simplified for library programmers, abstracts away JAXBElement requirements. */
public class KeyName extends JAXBElement<String>
{
	private static final long serialVersionUID = 6032597376102630268L;
	
	private static final String qName = "ds:KeyName"; //$NON-NLS-1$

	/**
	 * @param value The name of this key to be stored in XML
	 */
	public KeyName(String value)
	{
		super(new QName(qName), String.class, value);
	}
}

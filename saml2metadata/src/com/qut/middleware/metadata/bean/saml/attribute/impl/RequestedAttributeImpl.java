/*
 * Copyright 2008, Queensland University of Technology
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
 * Creation Date: 04/06/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.metadata.bean.saml.attribute.impl;

import com.qut.middleware.metadata.bean.saml.attribute.RequestedAttribute;

public class RequestedAttributeImpl implements RequestedAttribute
{
	private String name;
	private String nameFormat;
	private String friendlyName;
	private boolean required;

	public RequestedAttributeImpl(String name, String nameFormat, String friendlyName, boolean required)
	{
		this.name = name;
		this.nameFormat = nameFormat;
		this.friendlyName = friendlyName;
		this.required = required;
	}

	public String getFriendlyName()
	{
		return this.friendlyName;
	}

	public String getName()
	{
		return this.name;
	}

	public String getNameFormat()
	{
		return this.nameFormat;
	}

	public boolean isRequired()
	{
		return this.required;
	}
}

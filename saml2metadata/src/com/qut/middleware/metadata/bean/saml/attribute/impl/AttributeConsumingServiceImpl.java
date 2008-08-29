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

import java.util.List;

import com.qut.middleware.metadata.bean.saml.attribute.AttributeConsumingService;
import com.qut.middleware.metadata.bean.saml.attribute.RequestedAttribute;

public class AttributeConsumingServiceImpl implements AttributeConsumingService
{
	private int index;
	// Spelling mistake intentional. "default" is a reserved word
	private boolean defalt;
	private List<RequestedAttribute> requestedAttribute;
	private List<String> serviceNames;
	private List<String> serviceDescriptions;

	public AttributeConsumingServiceImpl(int index, boolean defalt, List<RequestedAttribute> requestedAttributes, List<String> serviceDescriptions, List<String> serviceNames)
	{
		this.index = index;
		this.defalt = defalt;
		this.requestedAttribute = requestedAttributes;
		this.serviceDescriptions = serviceDescriptions;
		this.serviceNames = serviceNames;
	}
	
	public int getIndex()
	{
		return this.index;
	}

	public List<RequestedAttribute> getRequestedAttributes()
	{
		return this.requestedAttribute;
	}

	public List<String> getServiceDescriptions()
	{
		return this.serviceDescriptions;
	}

	public List<String> getServiceNames()
	{
		return this.serviceNames;
	}

	public boolean isDefault()
	{
		return this.defalt;
	}

}

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
 * Creation Date: 06/10/2006
 * 
 * Purpose: Implements the IdentityAttribute interface to store an attribute of
 * 		arbitrary type
 */
package com.qut.middleware.esoe.sessions.bean.impl;

import java.util.List;
import java.util.Vector;

import com.qut.middleware.esoe.sessions.bean.IdentityAttribute;

/** */
public class IdentityAttributeImpl extends Object implements IdentityAttribute
{
	private static final long serialVersionUID = 1746235388797252959L;
	
	private List<Object> values;
	private String type;
	private List<String> handlers;

	/**
	 * Default constructor
	 */
	public IdentityAttributeImpl()
	{
		this.values = new Vector<Object>(0, 1);
		this.type = null;
		this.handlers = new Vector<String>(0, 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.bean.IdentityAttribute#addValue(java.lang.Object)
	 */
	public void addValue(Object value)
	{
		this.values.add(value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.bean.IdentityAttribute#getType()
	 */
	public String getType()
	{
		return this.type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.bean.IdentityAttribute#getValues()
	 */
	public List<Object> getValues()
	{
		return this.values;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.bean.IdentityAttribute#setType(java.lang.String)
	 */
	public void setType(String type)
	{
		this.type = type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.bean.IdentityAttribute#getHandlers()
	 */
	public List<String> getHandlers()
	{
		return this.handlers;
	}
}

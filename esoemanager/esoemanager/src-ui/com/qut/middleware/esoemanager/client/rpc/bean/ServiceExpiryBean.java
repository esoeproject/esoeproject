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
package com.qut.middleware.esoemanager.client.rpc.bean;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ServiceExpiryBean implements IsSerializable
{
	String name;
	Date expiryDate;
	
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public Date getExpiryDate()
	{
		return expiryDate;
	}
	public void setExpiryDate(Date expiryDate)
	{
		this.expiryDate = expiryDate;
	}
	
	
}

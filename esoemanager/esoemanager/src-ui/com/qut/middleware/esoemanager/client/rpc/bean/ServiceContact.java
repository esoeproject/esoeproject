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

import com.google.gwt.user.client.rpc.IsSerializable;

public class ServiceContact implements IsSerializable
{
	private String contactID;
	private String name;
	private String email;
	private String telephone;	
	private String company;	
	private String type;
	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	/**
	 * @return the email
	 */
	public String getEmail()
	{
		return email;
	}
	/**
	 * @param email the email to set
	 */
	public void setEmail(String email)
	{
		this.email = email;
	}
	/**
	 * @return the telephone
	 */
	public String getTelephone()
	{
		return telephone;
	}
	/**
	 * @param telephone the telephone to set
	 */
	public void setTelephone(String telephone)
	{
		this.telephone = telephone;
	}
	/**
	 * @return the company
	 */
	public String getCompany()
	{
		return company;
	}
	/**
	 * @param company the company to set
	 */
	public void setCompany(String company)
	{
		this.company = company;
	}
	/**
	 * @return the type
	 */
	public String getType()
	{
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type)
	{
		this.type = type;
	}
	/**
	 * @return the contactID
	 */
	public String getContactID()
	{
		return contactID;
	}
	/**
	 * @param contactID the contactID to set
	 */
	public void setContactID(String contactID)
	{
		this.contactID = contactID;
	}
	
	
}

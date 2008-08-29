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
 * Purpose: Contact Person Bean Impl
 */
package com.qut.middleware.esoemanager.bean.impl;

import java.io.Serializable;

import com.qut.middleware.esoemanager.bean.ContactPersonBean;

public class ContactPersonBeanImpl implements Serializable, ContactPersonBean
{
	private static final long serialVersionUID = 73777648379310876L;

	private String contactType = null;
	private String company = null;
	private String givenName = null;
	private String surname = null;
	private String emailAddress = null;
	private String telephoneNumber = null;
	private String contactID = null;
	private boolean modified = false;


	public boolean isModified()
	{
		return modified;
	}


	public void setModified(boolean modified)
	{
		this.modified = modified;
	}


	public String getCompany()
	{
		return this.company;
	}


	public void setCompany(String company)
	{
		this.company = company;
	}


	public String getContactType()
	{
		return this.contactType;
	}


	public void setContactType(String contactType)
	{
		this.contactType = contactType;
	}


	public String getEmailAddress()
	{
		return this.emailAddress;
	}


	public void setEmailAddress(String emailAddress)
	{
		this.emailAddress = emailAddress;
	}


	public String getGivenName()
	{
		return this.givenName;
	}


	public void setGivenName(String givenName)
	{
		this.givenName = givenName;
	}


	public String getSurName()
	{
		return this.surname;
	}


	public void setSurName(String surname)
	{
		this.surname = surname;
	}


	public String getTelephoneNumber()
	{
		return this.telephoneNumber;
	}


	public void setTelephoneNumber(String telephoneNumber)
	{
		this.telephoneNumber = telephoneNumber;
	}


	public String getContactID()
	{
		return this.contactID;
	}


	public void setContactID(String contactID)
	{
		this.contactID = contactID;
	}

}

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
 * Purpose: Bean to store contact details for individual service
 */
package com.qut.middleware.esoemanager.bean;

public interface ContactPersonBean
{
	/**
	 * Used to indicate if this object has been modified since retrieval from data repository
	 * 
	 * @return boolean indicating modification state
	 */
	public boolean isModified();

	public void setModified(boolean modified);

	/**
	 * @return the company
	 */
	public String getCompany();

	/**
	 * @param company
	 *            the company to set
	 */
	public void setCompany(String company);

	/**
	 * @return the contactType
	 */
	public String getContactType();

	/**
	 * @param contactType
	 *            the contactType to set
	 */
	public void setContactType(String contactType);

	/**
	 * @return the emailAddress
	 */
	public String getEmailAddress();

	/**
	 * @param emailAddress
	 *            the emailAddress to set
	 */
	public void setEmailAddress(String emailAddress);

	/**
	 * @return the givenName
	 */
	public String getGivenName();

	/**
	 * @param givenName
	 *            the givenName to set
	 */
	public void setGivenName(String givenName);

	/**
	 * @return the surname
	 */
	public String getSurName();

	/**
	 * @param surname
	 *            the surname to set
	 */
	public void setSurName(String surname);

	/**
	 * @return the telephoneNumber
	 */
	public String getTelephoneNumber();

	/**
	 * @param telephoneNumber
	 *            the telephoneNumber to set
	 */
	public void setTelephoneNumber(String telephoneNumber);

	/**
	 * @return the contactID
	 */
	public String getContactID();

	/**
	 * @param contactID
	 *            the contactID to set
	 */
	public void setContactID(String uid);

}
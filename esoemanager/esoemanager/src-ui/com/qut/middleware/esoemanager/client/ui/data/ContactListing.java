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
package com.qut.middleware.esoemanager.client.ui.data;

import com.google.gwt.user.client.ui.ListBox;
import com.qut.gwtuilib.client.RegexConstants;
import com.qut.gwtuilib.client.display.FlexibleTable;
import com.qut.gwtuilib.client.display.HiddenIntegratedListBox;
import com.qut.gwtuilib.client.display.HiddenIntegratedTextBox;
import com.qut.gwtuilib.client.display.IntegratedLabel;
import com.qut.gwtuilib.client.exceptions.InvalidContentException;

public class ContactListing
{	
	private final String nameRegex = RegexConstants.commonName;
	private final String emailRegex = RegexConstants.emailAddress;
	private final String telephoneRegex = RegexConstants.telephoneNumber;
	private final String companyRegex = RegexConstants.matchAll;
	
	private IntegratedLabel contactID;
	private HiddenIntegratedTextBox name;
	private HiddenIntegratedTextBox email;
	private HiddenIntegratedTextBox telephone;
	private HiddenIntegratedTextBox company;
	private HiddenIntegratedListBox type;
	
	private String areaID;

	public ContactListing(String areaID)
	{
		this.areaID = areaID;
		this.createInterface();
	}
	
	private void createInterface()
	{		
		this.contactID = new IntegratedLabel(this, "ContactID");
		this.name = new HiddenIntegratedTextBox(this, 1, this.nameRegex, "Contacts name must have 1 character",
				"Name", this.areaID);
		this.email = new HiddenIntegratedTextBox(this, 1, this.emailRegex, "Contacts email must be in the form name@org",
				"Email", this.areaID);
		this.telephone = new HiddenIntegratedTextBox(this, 1, this.telephoneRegex, "Contacts phone must be numeric",
				"Phone", this.areaID);
		this.company = new HiddenIntegratedTextBox(this, 1, this.nameRegex, "Contacts company must have 1 character",
				"Company", this.areaID);
		this.type = new HiddenIntegratedListBox(this, "Type", this.areaID);
		
		ListBox types = this.type.getBackingListBox();
		types.addItem("technical");
		types.addItem("administrative");
		types.addItem("support");
		types.addItem("billing");
	}
	
	public void createRow(FlexibleTable table)
	{
		table.insertWidget(this.contactID.getContent());
		table.insertWidget(this.name.getContent());
		table.insertWidget(this.email.getContent());
		table.insertWidget(this.telephone.getContent());
		table.insertWidget(this.company.getContent());
		table.insertWidget(this.type.getContent());
	}
	
	public String getContactID() 
	{
		return this.contactID.getText();
	}
	
	public String getName() throws InvalidContentException
	{
		return name.getText();
	}
	
	public String getEmail() throws InvalidContentException
	{
		return email.getText();
	}
	
	public String getTelephone() throws InvalidContentException
	{
		return telephone.getText();
	}
	
	public String getCompany() throws InvalidContentException
	{
		return company.getText();
	}
	
	public String getType() throws InvalidContentException
	{
		return type.getCurrentValue();
	}
	
	public void setContactID(String contactID) 
	{
			this.contactID.setText(contactID);
	}
	
	public void setName(String name) 
	{
		try
		{
			this.name.setText(name);
		}
		catch (InvalidContentException e)
		{
		}
	}
	
	public void setEmail(String email)
	{
		try
		{
			this.email.setText(email);
		}
		catch (InvalidContentException e)
		{
		}
	}
	
	public void setTelephone(String telephone)
	{
		try
		{
			this.telephone.setText(telephone);
		}
		catch (InvalidContentException e)
		{
		}
	}
	
	public void setCompany(String company)
	{
		try
		{
			this.company.setText(company);
		}
		catch (InvalidContentException e)
		{
		}
	}
	
	public void setType(String type)
	{
		this.type.setValue(type);
	}
}

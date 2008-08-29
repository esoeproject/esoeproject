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
package com.qut.middleware.esoemanager.client.ui.crypto;

import java.util.Date;

import com.google.gwt.user.client.ui.HTML;
import com.qut.gwtuilib.client.display.FlexibleTable;
import com.qut.gwtuilib.client.display.IntegratedLabel;

public class KeyDetailsUI
{	
	IntegratedLabel keypairName;
	IntegratedLabel keypairPassphrase;
	IntegratedLabel expiryDate;
	IntegratedLabel keystorePassphrase;
	String serviceIdentifier;
	
	Date rawExpiryDate;
	
	Object parent;

	public KeyDetailsUI(Object parent, String serviceIdentifier, String keypairName, String keypairPassphrase, Date expiryDate, String keystorePassphrase)
	{
		this.parent = parent;
		this.serviceIdentifier = serviceIdentifier;
		this.rawExpiryDate = expiryDate;
		
		this.keypairName = new IntegratedLabel(this.parent, "Keypair Name");
		this.keypairName.setText(keypairName);
		
		this.keypairPassphrase = new IntegratedLabel(this.parent, "Keypair Passphrase");
		this.keypairPassphrase.setText(keypairPassphrase);
		
		this.expiryDate = new IntegratedLabel(this.parent, "Expiry Date");
		this.expiryDate.setText(expiryDate.toString());
			
		this.keystorePassphrase = new IntegratedLabel(this.parent, "Keystore Passphrase");
		this.keystorePassphrase.setText(keystorePassphrase);
	}
	
	public void addShortTableRow(FlexibleTable table)
	{
		table.insertWidget(this.keypairName.getContent());
		table.insertWidget(this.expiryDate.getContent());
	}
	
	public void addTableRow(FlexibleTable table)
	{
		table.insertWidget(this.keypairName.getContent());
		table.insertWidget(this.keypairPassphrase.getContent());
		table.insertWidget(this.keystorePassphrase.getContent());
		table.insertWidget(this.expiryDate.getContent());
		table.insertWidget(new HTML("<a href='/esoemanager/manager/keyretrieval?sid=" + this.serviceIdentifier + "&n=" + this.keypairName.getText() + "'>download</a>"));
	}

	public Date getRawExpiryDate()
	{
		return this.rawExpiryDate;
	}
}

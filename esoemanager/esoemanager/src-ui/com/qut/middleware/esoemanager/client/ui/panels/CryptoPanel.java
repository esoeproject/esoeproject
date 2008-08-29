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
package com.qut.middleware.esoemanager.client.ui.panels;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.qut.gwtuilib.client.display.FlexibleTable;
import com.qut.gwtuilib.client.display.Loader;
import com.qut.gwtuilib.client.eventdriven.eventmgr.BaseEvent;
import com.qut.gwtuilib.client.eventdriven.eventmgr.EventController;
import com.qut.gwtuilib.client.eventdriven.events.MessageEvent;
import com.qut.gwtuilib.client.input.ConfirmationStyledButton;
import com.qut.middleware.esoemanager.client.CSSConstants;
import com.qut.middleware.esoemanager.client.EsoeManager;
import com.qut.middleware.esoemanager.client.EsoeManagerConstants;
import com.qut.middleware.esoemanager.client.events.EventConstants;
import com.qut.middleware.esoemanager.client.rpc.bean.KeyDetails;
import com.qut.middleware.esoemanager.client.ui.crypto.KeyDetailsUI;

public class CryptoPanel extends VerticalPanel
{
	private final long thirtyDayMillisecondCount = 2592000000L;
	private final long ninetyDayMillisecondCount = 7776000000L;

	String serviceIdentifier;
	List<KeyDetailsUI> keys;

	Loader loader;
	FlexibleTable keyTable;
	FlexibleTable expiredKeyTable;

	public CryptoPanel(String serviceIdentifier)
	{
		this.serviceIdentifier = serviceIdentifier;

		this.keys = new ArrayList<KeyDetailsUI>();
		this.createInterface();
	}

	private void createInterface()
	{
		this.loader = new Loader();
		this.keyTable = new FlexibleTable(5, 5);

		this.keyTable.insertHeading(0, "Name");
		this.keyTable.insertHeading(1, "Passphrase");
		this.keyTable.insertHeading(2, "Keystore Passphrase");
		this.keyTable.insertHeading(3, "Expiry Date");

		this.expiredKeyTable = new FlexibleTable(5, 5);

		this.expiredKeyTable.insertHeading(0, "Name");
		this.expiredKeyTable.insertHeading(1, "Passphrase");
		this.expiredKeyTable.insertHeading(2, "Keystore Passphrase");
		this.expiredKeyTable.insertHeading(3, "Expiry Date");

		EsoeManager.contentService.retrieveServiceKeys(this.serviceIdentifier, new KeysRetrievalHandler(this.loader));

		final ConfirmationStyledButton addKey = new ConfirmationStyledButton(
				"Are you sure you wish to create a new key for this service?", "add", "Add Key");
		addKey.addClickListener(new ClickListener()
		{
			public void onClick(Widget sender)
			{
				CryptoPanel.this.loader.setVisible(true);
				EsoeManager.contentService.createServiceKey(serviceIdentifier, new KeyCreationHandler(
						CryptoPanel.this.loader));
			}
		});

		Label title = new Label("Valid Keys");
		title.addStyleName(CSSConstants.esoeManagerSubSectionTitle);

		Label expiredTitle = new Label("Expired Keys");
		expiredTitle.addStyleName(CSSConstants.esoeManagerSubSectionTitle);

		HorizontalPanel banner = new HorizontalPanel();
		banner.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		banner.setSpacing(5);
		banner.add(title);
		banner.add(this.loader);

		this.add(banner);
		this.add(addKey);
		this.add(this.keyTable);
		this.add(expiredTitle);
		this.add(this.expiredKeyTable);
	}

	private void populateInterface(List<KeyDetails> keys)
	{
		this.keyTable.clear();
		this.expiredKeyTable.clear();

		for (final KeyDetails keyDetails : keys)
		{
			KeyDetailsUI keyDetailsUI = new KeyDetailsUI(this, serviceIdentifier, keyDetails.getKeypairName(), keyDetails
					.getKeypairPassphrase(), keyDetails.getExpiryDate(), keyDetails.getKeystorePassphrase());

			final ConfirmationStyledButton revokeKey = new ConfirmationStyledButton(
					"Are you sure you wish to delete this key pair, this is not reversable and may take this service offline",
					"delete", "");
			revokeKey.addClickListener(new ClickListener()
			{
				public void onClick(Widget sender)
				{
					CryptoPanel.this.loader.setVisible(true);
					EsoeManager.contentService.deleteServiceKey(serviceIdentifier, keyDetails.getKeypairName(),
							new KeyRevocationHandler(CryptoPanel.this.loader));
				}
			});

			if (keyDetails.getExpiryDate().getTime() - System.currentTimeMillis() <= 0)
			{
				keyDetailsUI.addTableRow(this.expiredKeyTable);
				this.expiredKeyTable.nextRow();
			}
			else
			{
				if (keyDetails.isExpireError())
				{
					final ConfirmationStyledButton errorCreateKey = new ConfirmationStyledButton(
							"This key will expire very shortly possibly taking this service offline, would you like to create a new key now?",
							"error", "Key Expiry");
					errorCreateKey.addClickListener(new ClickListener()
					{
						public void onClick(Widget sender)
						{
							EsoeManager.contentService.createServiceKey(CryptoPanel.this.serviceIdentifier,
									new KeyCreationHandler(CryptoPanel.this.loader));
						}
					});

					keyDetailsUI.addTableRow(this.keyTable);
					this.keyTable.insertWidget(errorCreateKey);
					this.keyTable.insertWidget(revokeKey);
					this.keyTable.nextRow();
				}
				else
				{
					if (keyDetails.isExpireWarn())
					{
						final ConfirmationStyledButton warnCreateKey = new ConfirmationStyledButton(
								"This key will expire shortly and should be renewed, would you like to create a new key now?",
								"warning", "Key Expiry");
						warnCreateKey.addClickListener(new ClickListener()
						{
							public void onClick(Widget sender)
							{
								EsoeManager.contentService.createServiceKey(serviceIdentifier, new KeyCreationHandler(
										CryptoPanel.this.loader));
							}
						});

						keyDetailsUI.addTableRow(this.keyTable);
						this.keyTable.insertWidget(warnCreateKey);
						this.keyTable.insertWidget(revokeKey);
						this.keyTable.nextRow();
					}
					else
					{
						/* Valid keypair */
						keyDetailsUI.addTableRow(this.keyTable);
						this.keyTable.insertWidget(null);
						this.keyTable.insertWidget(revokeKey);
						this.keyTable.nextRow();
					}
				}
			}
		}
	}

	private class KeyCreationHandler implements AsyncCallback
	{
		Loader loader;

		public KeyCreationHandler(Loader loader)
		{
			this.loader = loader;
		}

		public void onFailure(Throwable caught)
		{
			this.loader.setVisible(false);
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, EsoeManagerConstants.areaID,
					MessageEvent.error,
					"Unable to create a new key for this service, please contact a system administrator. Server response: "
							+ caught.getLocalizedMessage()));
		}

		public void onSuccess(Object result)
		{
			this.loader.setVisible(false);
			EventController
					.executeEvent(new MessageEvent(
							EventConstants.userMessage,
							EsoeManagerConstants.areaID,
							MessageEvent.ok,
							"Created new key successfully"));

			/* Reload keys now we have a new one */
			EsoeManager.contentService.retrieveServiceKeys(CryptoPanel.this.serviceIdentifier,
					new KeysRetrievalHandler(this.loader));
			
			EventController.executeEvent(new BaseEvent(EventConstants.configurationChange));
		}
	}

	private class KeysRetrievalHandler implements AsyncCallback
	{
		Loader loader;

		public KeysRetrievalHandler(Loader loader)
		{
			this.loader = loader;
		}

		public void onFailure(Throwable caught)
		{
			this.loader.setVisible(false);
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, EsoeManagerConstants.areaID,
					MessageEvent.error,
					"Unable to retrieve key listing for this service, please contact a system administrator. Server response: "
							+ caught.getLocalizedMessage()));
		}

		public void onSuccess(Object result)
		{
			this.loader.setVisible(false);
			if (result != null)
			{
				List<KeyDetails> keys = (List<KeyDetails>) result;
				populateInterface(keys);
			}
		}
	}

	private class KeyRevocationHandler implements AsyncCallback
	{
		Loader loader;

		public KeyRevocationHandler(Loader loader)
		{
			this.loader = loader;
		}

		public void onFailure(Throwable caught)
		{
			this.loader.setVisible(false);
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, EsoeManagerConstants.areaID,
					MessageEvent.error,
					"Unable to retrieve key listing for this service, please contact a system administrator. Server response: "
							+ caught.getLocalizedMessage()));
		}

		public void onSuccess(Object result)
		{
			this.loader.setVisible(false);
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, EsoeManagerConstants.areaID,
					MessageEvent.ok, "Deleted key from this service"));

			/* Reload keys now we have a new one */
			EsoeManager.contentService.retrieveServiceKeys(CryptoPanel.this.serviceIdentifier,
					new KeysRetrievalHandler(this.loader));
			
			EventController.executeEvent(new BaseEvent(EventConstants.configurationChange));
		}
	}

}

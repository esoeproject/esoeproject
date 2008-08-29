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
package com.qut.middleware.esoemanager.client.ui.nodes;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.qut.gwtuilib.client.display.FlexibleTable;
import com.qut.gwtuilib.client.display.IntegratedLabel;
import com.qut.gwtuilib.client.input.StyledButton;
import com.qut.middleware.esoemanager.client.CSSConstants;
import com.qut.middleware.esoemanager.client.rpc.bean.ServiceNodeConfiguration;

public class NodeConfigurationUI extends VerticalPanel 
{
	IntegratedLabel keystorePassword;
	IntegratedLabel spepKeyAlias;
	IntegratedLabel spepKeyPassword;
	IntegratedLabel esoeIdentifier;
	IntegratedLabel spepIdentifier;
	IntegratedLabel metadataURL;
	IntegratedLabel serverInfo;
	IntegratedLabel nodeIdentifier;
	IntegratedLabel attributeConsumingServiceIndex;
	IntegratedLabel assertionConsumerServiceIndex;
	IntegratedLabel authzCacheIndex;
	IntegratedLabel serviceHost;
	IntegratedLabel ipAddress;
	IntegratedLabel defaultURL;

	public void createInterface(ServiceNodeConfiguration nodeConfig)
	{
		this.addStyleName(CSSConstants.esoeManagerNode);

		final VerticalPanel content = new VerticalPanel();

		HorizontalPanel banner = new HorizontalPanel();
		banner.addStyleName(CSSConstants.esoeManagerNodeBanner);
		banner.setSpacing(5);

		StyledButton showContent = new StyledButton("application", "");
		showContent.addClickListener(new ClickListener()
		{
			public void onClick(Widget sender)
			{
				content.setVisible(!content.isVisible());
			}
		});

		banner.add(showContent);
		banner.add(new Label("Node - "));
		banner.add(new Label(nodeConfig.getServiceHost()));

		this.keystorePassword = new IntegratedLabel("keystorePassword", nodeConfig.getKeystorePassword());
		this.spepKeyAlias = new IntegratedLabel("spepKeyAlias", nodeConfig.getSpepKeyAlias());
		this.spepKeyPassword = new IntegratedLabel("spepKeyPassword", nodeConfig.getSpepKeyPassword());
		this.esoeIdentifier = new IntegratedLabel("esoeIdentifier", nodeConfig.getEsoeIdentifier());
		this.spepIdentifier = new IntegratedLabel("spepIdentifier", nodeConfig.getSpepIdentifier());
		this.metadataURL = new IntegratedLabel("metadataURL", nodeConfig.getMetadataURL());
		this.serverInfo = new IntegratedLabel("serverInfo", "A human readable description of the service");
		this.nodeIdentifier = new IntegratedLabel("nodeIdentifier", nodeConfig.getNodeIdentifier());
		this.attributeConsumingServiceIndex = new IntegratedLabel("attributeConsumingServiceIndex", nodeConfig
				.getNodeIdentifier());
		this.assertionConsumerServiceIndex = new IntegratedLabel("assertionConsumerServiceIndex", nodeConfig
				.getNodeIdentifier());
		this.authzCacheIndex = new IntegratedLabel("authzCacheIndex", nodeConfig.getNodeIdentifier());
		this.serviceHost = new IntegratedLabel("serviceHost", nodeConfig.getServiceHost());
		this.ipAddress = new IntegratedLabel("ipAddress",
				"The IP address which this service node accepts connections on");
		this.defaultURL = new IntegratedLabel("defaultURL",
				"The default URL that end users access when using this service");

		FlexibleTable nodeConfigs = new FlexibleTable(5, 5);

		nodeConfigs.insertWidget(this.keystorePassword.getTitle());
		nodeConfigs.insertWidget(this.keystorePassword.getContent());
		nodeConfigs.nextRow();

		nodeConfigs.insertWidget(this.spepKeyAlias.getTitle());
		nodeConfigs.insertWidget(this.spepKeyAlias.getContent());
		nodeConfigs.nextRow();

		nodeConfigs.insertWidget(this.spepKeyPassword.getTitle());
		nodeConfigs.insertWidget(this.spepKeyPassword.getContent());
		nodeConfigs.nextRow();

		nodeConfigs.insertWidget(this.esoeIdentifier.getTitle());
		nodeConfigs.insertWidget(this.esoeIdentifier.getContent());
		nodeConfigs.nextRow();

		nodeConfigs.insertWidget(this.spepIdentifier.getTitle());
		nodeConfigs.insertWidget(this.spepIdentifier.getContent());
		nodeConfigs.nextRow();

		nodeConfigs.insertWidget(this.metadataURL.getTitle());
		nodeConfigs.insertWidget(this.metadataURL.getContent());
		nodeConfigs.nextRow();

		nodeConfigs.insertWidget(this.serverInfo.getTitle());
		nodeConfigs.insertWidget(this.serverInfo.getContent());
		nodeConfigs.nextRow();

		nodeConfigs.insertWidget(this.nodeIdentifier.getTitle());
		nodeConfigs.insertWidget(this.nodeIdentifier.getContent());
		nodeConfigs.nextRow();

		nodeConfigs.insertWidget(this.attributeConsumingServiceIndex.getTitle());
		nodeConfigs.insertWidget(this.attributeConsumingServiceIndex.getContent());
		nodeConfigs.nextRow();

		nodeConfigs.insertWidget(this.assertionConsumerServiceIndex.getTitle());
		nodeConfigs.insertWidget(this.assertionConsumerServiceIndex.getContent());
		nodeConfigs.nextRow();

		nodeConfigs.insertWidget(this.authzCacheIndex.getTitle());
		nodeConfigs.insertWidget(this.authzCacheIndex.getContent());
		nodeConfigs.nextRow();

		nodeConfigs.insertWidget(this.serviceHost.getTitle());
		nodeConfigs.insertWidget(this.serviceHost.getContent());
		nodeConfigs.nextRow();

		nodeConfigs.insertWidget(this.ipAddress.getTitle());
		nodeConfigs.insertWidget(this.ipAddress.getContent());
		nodeConfigs.nextRow();

		nodeConfigs.insertWidget(this.defaultURL.getTitle());
		nodeConfigs.insertWidget(this.defaultURL.getContent());
		nodeConfigs.nextRow();

		content.add(nodeConfigs);
		content.setVisible(false);
		
		this.add(banner);
		this.add(content);
	}
}

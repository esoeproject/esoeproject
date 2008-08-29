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
package com.qut.middleware.esoemanager.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.qut.gwtuilib.client.display.EventMessageLoggingPanel;
import com.qut.gwtuilib.client.display.EventMessagePanel;
import com.qut.middleware.esoemanager.client.rpc.EsoeManagerService;
import com.qut.middleware.esoemanager.client.rpc.EsoeManagerServiceAsync;
import com.qut.middleware.esoemanager.client.ui.CorePanel;

public class EsoeManager extends VerticalPanel implements EntryPoint
{
	private final String editorInstance = "EsoeManagerInstance";
	private final String rpcContentEndpoint = "rpcContentEndpoint";
	private final String managerServletVar = "managerServlet";
	private final String panelInsertionPoint = "esoeManager";
	private final String basePathVar = "basePath";
	private final String areaID = EsoeManagerConstants.areaID;
	
	Map params;
	
	protected EventMessagePanel messagePanel;
	protected EventMessageLoggingPanel loggingPanel;
	protected CorePanel corePanel;
	
	public static EsoeManagerServiceAsync contentService;
	public static String managerServlet;
	public static String basePath;
	
	public void onModuleLoad()
	{
		this.configureInstance();

		EsoeManager.contentService = (EsoeManagerServiceAsync) GWT.create(EsoeManagerService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) this.contentService;
		endpoint.setServiceEntryPoint((String) this.params.get(this.rpcContentEndpoint));
		
		EsoeManager.managerServlet = (String) this.params.get(this.managerServletVar);
		EsoeManager.basePath = (String) this.params.get(this.basePathVar);
	
		this.createInterface();
		
		RootPanel.get(this.panelInsertionPoint).add(this);
	}

	private void createInterface()
	{
		this.addStyleName(CSSConstants.esoeManagerUI);
		
		this.messagePanel = new EventMessagePanel(this.areaID);
		this.corePanel = new CorePanel("1");
		this.loggingPanel = new EventMessageLoggingPanel(this.areaID);
		
		this.add(this.messagePanel);
		this.add(this.loggingPanel);
		this.add(this.corePanel);
		
	}

	private void configureInstance()
	{
		this.params = new HashMap();
		Dictionary editorInstanceVals = Dictionary.getDictionary(this.editorInstance);

		this.params.put(this.rpcContentEndpoint, editorInstanceVals.get(this.rpcContentEndpoint));
		this.params.put(this.managerServletVar, editorInstanceVals.get(this.managerServletVar));
		this.params.put(this.basePathVar, editorInstanceVals.get(this.basePathVar));
	}
}

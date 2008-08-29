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

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.qut.gwtuilib.client.eventdriven.eventmgr.EventController;
import com.qut.gwtuilib.client.eventdriven.events.MessageEvent;
import com.qut.gwtuilib.client.input.ConfirmationStyledButton;
import com.qut.middleware.esoemanager.client.CSSConstants;
import com.qut.middleware.esoemanager.client.EsoeManager;
import com.qut.middleware.esoemanager.client.EsoeManagerConstants;
import com.qut.middleware.esoemanager.client.events.EventConstants;

public class AttributesPanel extends VerticalPanel
{
	private String esoeID;
	private SimplePanel codePanel;
	private Element elem;

	private JavaScriptObject codeMirror;
	private String basePath;

	public AttributesPanel(String esoeID)
	{
		this.esoeID = esoeID;
		this.basePath = EsoeManager.basePath;

		this.createInterface();

		EsoeManager.contentService.getAttributePolicyXML(new AttributeRetrievalHandler());
	}

	private void createInterface()
	{
		this.codePanel = new SimplePanel();
		this.codePanel.addStyleName(CSSConstants.attributePolicyPanel);

		this.elem = this.codePanel.getElement();

		HorizontalPanel controls = new HorizontalPanel();
		controls.setSpacing(5);

		ConfirmationStyledButton saveAttributes = new ConfirmationStyledButton("Save this attribute policy?", "save",
				"Save");
		controls.add(saveAttributes);

		saveAttributes.addClickListener(new ClickListener()
		{
			public void onClick(Widget sender)
			{
				if (codePanel != null)
					EsoeManager.contentService.saveAttributePolicyXML(AttributesPanel.this.getCode(),
							new AttributeSaveHandler());
			}
		});

		ConfirmationStyledButton reloadAttributes = new ConfirmationStyledButton(
				"Refresh this attribute policy? Changes will be lost", "toggle", "Refresh");
		controls.add(reloadAttributes);

		reloadAttributes.addClickListener(new ClickListener()
		{
			public void onClick(Widget sender)
			{
				EsoeManager.contentService.getAttributePolicyXML(new AttributeRetrievalHandler());
			}
		});

		this.add(controls);
		this.add(this.codePanel);
	}

	public String getXMLMarkup()
	{
		return this.getCode();
	}

	private class AttributeRetrievalHandler implements AsyncCallback
	{
		public void onFailure(Throwable caught)
		{
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, EsoeManagerConstants.areaID,
					MessageEvent.error,
					"Unable to retrieve policy, XML please contact a system administrator. Server response: "
							+ caught.getLocalizedMessage()));
		}

		public void onSuccess(Object result)
		{
			String content = (String) result;

			/* Create on first access can take some time to be ready */
			if (AttributesPanel.this.codeMirror == null)
			{
				AttributesPanel.this.codeMirror = AttributesPanel.this.configureCoreMirror(content,
						AttributesPanel.this.basePath);
			}
			else
			{
				AttributesPanel.this.setCode(content);
			}
		}
	}

	private class AttributeSaveHandler implements AsyncCallback
	{
		public void onFailure(Throwable caught)
		{
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, EsoeManagerConstants.areaID,
					MessageEvent.error,
					"Unable to save policy, XML please contact a system administrator. Server response: "
							+ caught.getLocalizedMessage()));
		}

		public void onSuccess(Object result)
		{
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, EsoeManagerConstants.areaID,
					MessageEvent.ok,
					"Attribute release policy saved"));
		}
	}

	/* JS native below this point */
	/**
	 * Returns the current content of the editor, as a string.
	 */
	public native String getCode() /*-{
		return this.@com.qut.middleware.esoemanager.client.ui.panels.AttributesPanel::codeMirror.getCode();
	}-*/;

	/**
	 * Takes a single string argument, and replaces the current content of the
	 * editor with that value.
	 */
	public native void setCode(String content) /*-{
		this.@com.qut.middleware.esoemanager.client.ui.panels.AttributesPanel::codeMirror.setCode(content);
	}-*/;

	/**
	 * Initial component configuration
	 */
	private native JavaScriptObject configureCoreMirror(String content, String basePath) /*-{
		 var codeMirror = new $wnd.CodeMirror(this.@com.qut.middleware.esoemanager.client.ui.panels.AttributesPanel::elem, {
		    height: "400px",
		    width: "795px",
		    parserfile: "parsexml.js",
		    stylesheet: basePath + "codemirror/css/xmlcolors.css",
		    path: basePath + "codemirror/js/",
		    continuousScanning: 500,
		    content: content
		  });
		  
		return codeMirror;
	}-*/;
}

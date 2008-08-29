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
package com.qut.middleware.esoemanager.client.ui.policy;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SimplePanel;
import com.qut.gwtuilib.client.eventdriven.eventmgr.EventController;
import com.qut.gwtuilib.client.eventdriven.events.MessageEvent;
import com.qut.middleware.esoemanager.client.EsoeManager;
import com.qut.middleware.esoemanager.client.EsoeManagerConstants;
import com.qut.middleware.esoemanager.client.events.EventConstants;

public class PolicyXMLUI extends SimplePanel
{
	private SimplePanel codePanel;
	private Element elem;

	private JavaScriptObject codeMirror;

	private String currentSearchTerm;
	private JavaScriptObject searchCursor;
	
	private String basePath;
	private String serviceIdentifier;
	private String policyID;

	public PolicyXMLUI(String serviceIdentifier)
	{
		this.serviceIdentifier = serviceIdentifier;
		this.basePath = EsoeManager.basePath;
		
		this.codePanel = new SimplePanel();
		this.add(this.codePanel);
		
		this.elem = this.codePanel.getElement();
	}
	
	public String getXMLMarkup()
	{
		return this.getCode();
	}
	
	public void enableXMLEditor()
	{
		/* Create on first access can take some time to be ready */
		if (PolicyXMLUI.this.codeMirror == null)
		{
			PolicyXMLUI.this.setVisible(true);
			PolicyXMLUI.this.codeMirror = PolicyXMLUI.this.configureCoreMirror("", PolicyXMLUI.this.basePath);
		}
		else
		{
			PolicyXMLUI.this.setVisible(true);
			PolicyXMLUI.this.setCode("");
		}
	}

	public void enableXMLEditor(String policyID)
	{
		this.policyID = policyID;
		EsoeManager.contentService.retrieveServicePolicyXML(this.serviceIdentifier, this.policyID, new PolicyXMLRetrievalHandler());
	}

	public void search(String searchTerm)
	{
		if (this.currentSearchTerm != null && this.currentSearchTerm.equals(searchTerm))
		{
			this.getNextSearchTerm();
		}
		else
		{
			this.currentSearchTerm = searchTerm;
			this.searchCursor = this.getSearchCursor(this.currentSearchTerm);
			this.getNextSearchTerm();
		}
	}

	public void gotoLine(int lineNumber)
	{
		this.jumpToLine(lineNumber);
	}
	
	private class PolicyXMLRetrievalHandler implements AsyncCallback
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
			if (PolicyXMLUI.this.codeMirror == null)
			{
				PolicyXMLUI.this.setVisible(true);
				PolicyXMLUI.this.codeMirror = PolicyXMLUI.this.configureCoreMirror(content, PolicyXMLUI.this.basePath);
			}
			else
			{
				PolicyXMLUI.this.setVisible(true);
				PolicyXMLUI.this.setCode(content);
			}
		}
	}

	/* JS native below this point */

	/**
	 * Returns a search cursor for the select term
	 */
	public native JavaScriptObject getSearchCursor(String searchTerm) /*-{
		return this.@com.qut.middleware.esoemanager.client.ui.policy.PolicyXMLUI::codeMirror.getSearchCursor(searchTerm, true);
	}-*/;

	public native void getNextSearchTerm() /*-{
		if ( this.@com.qut.middleware.esoemanager.client.ui.policy.PolicyXMLUI::searchCursor.findNext() )
			this.@com.qut.middleware.esoemanager.client.ui.policy.PolicyXMLUI::searchCursor.select();
		
	}-*/;

	/**
	 * Returns the current content of the editor, as a string.
	 */
	public native String getCode() /*-{
		return this.@com.qut.middleware.esoemanager.client.ui.policy.PolicyXMLUI::codeMirror.getCode();
	}-*/;

	/**
	 * Takes a single string argument, and replaces the current content of the editor with that value.
	 */
	public native void setCode(String content) /*-{
		this.@com.qut.middleware.esoemanager.client.ui.policy.PolicyXMLUI::codeMirror.setCode(content);
	}-*/;

	/**
	 *  Returns the text that is currently selected in the editor.
	 */
	public native String selection() /*-{
		this.@com.qut.middleware.esoemanager.client.ui.policy.PolicyXMLUI::codeMirror.selection();
	}-*/;

	/**
	 *  When called with a string argument, replaces the currently selected text with the given string. Can be given a second, optional argument which, if true, causes the editor to also gain focus.
	 */
	public native void replaceSelection(String replacement, boolean setFocus) /*-{
		this.@com.qut.middleware.esoemanager.client.ui.policy.PolicyXMLUI::codeMirror.replaceSelection(replacement, setFocus);
	}-*/;

	/**
	 * Gives focus to the editor frame.
	 */
	public native void focus() /*-{
		this.@com.qut.middleware.esoemanager.client.ui.policy.PolicyXMLUI::codeMirror.focus();
	}-*/;

	/**
	 * Returns a number indicating the line on which the cursor is currently sitting.
	 */
	public native int currentLine() /*-{
		return this.@com.qut.middleware.esoemanager.client.ui.policy.PolicyXMLUI::codeMirror.currentLine();
	}-*/;

	/**
	 * Moves the cursor to the start of the given line (a number).
	 */
	public native void jumpToLine(int line) /*-{
		this.@com.qut.middleware.esoemanager.client.ui.policy.PolicyXMLUI::codeMirror.jumpToLine(line);
	}-*/;

	/**
	 * Initial component configuration
	 */
	private native JavaScriptObject configureCoreMirror(String content, String basePath) /*-{
		 var codeMirror = new $wnd.CodeMirror(this.@com.qut.middleware.esoemanager.client.ui.policy.PolicyXMLUI::elem, {
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

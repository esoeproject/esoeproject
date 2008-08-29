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
package com.qut.gwtuilib.client.display;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.qut.gwtuilib.client.CSSConstants;
import com.qut.gwtuilib.client.input.StyledButton;

public class Help extends FocusPanel
{
	public Help(final String helpLink)
	{
		this.addStyleName(CSSConstants.help);
		
		ToolTip toolTip = new ToolTip ("Click for help");
	    this.addMouseListener(toolTip);
	    
	    this.addClickListener(new ClickListener()
	    {
			public void onClick(Widget sender)
			{
				HelpDialog helpDialog = new HelpDialog(helpLink);
				helpDialog.show();
				helpDialog.center();
			}
	    });
	}
	
	private class HelpDialog extends DialogBox
	{
		VerticalPanel content;
		Frame helpLinkContent;
		StyledButton close;
		
		public HelpDialog(String helpLink)
		{
			this.content = new VerticalPanel();
			this.close = new StyledButton("cancel", "");
			this.close.addClickListener(new ClickListener()
			{
				public void onClick(Widget sender)
				{
					HelpDialog.this.hide();					
				}
			});
			
			
			this.helpLinkContent = new Frame(helpLink);
			this.helpLinkContent.addStyleName(CSSConstants.helpDialog);
			
			this.content.add(this.close);
			this.content.add(this.helpLinkContent);
			
			this.add(this.content);
		}	
	}
}

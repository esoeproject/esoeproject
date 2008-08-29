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
package com.qut.gwtuilib.client.input;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ConfirmationStyledButton extends HorizontalPanel
{
	StyledButton button;
	ConfirmDialog confirmDialog;
	String confirmMsg;
	
	public ConfirmationStyledButton(String confirmMsg, String name, AbstractImagePrototype img)
	{
		this.confirmMsg = confirmMsg;
		this.button = new StyledButton(name, img);
		this.button.addClickListener(new ClickListener()
		{
			public void onClick(Widget sender)
			{
				confirmClick();				
			}
		});
		
		this.confirmDialog = new ConfirmDialog();
		
		this.add(this.button);
	}

	public ConfirmationStyledButton(String confirmMsg, String name, String label)
	{
		this.confirmMsg = confirmMsg;
		this.button = new StyledButton(name, label);
		this.button.addClickListener(new ClickListener()
		{
			public void onClick(Widget sender)
			{
				confirmClick();				
			}
		});
		
		this.confirmDialog = new ConfirmDialog();
		
		this.add(this.button);
	}
	
	/**
	 * @param listener
	 * @see com.google.gwt.user.client.ui.FocusWidget#addClickListener(com.google.gwt.user.client.ui.ClickListener)
	 */
	public void addClickListener(ClickListener listener)
	{
		/* Send all actual registered events to the confirmation button */
		confirmDialog.confirmPanel.addClickListener(listener);
	}	
	
	void confirmClick()
	{
		this.confirmDialog.show();
		this.confirmDialog.center();
	}
	
	void cancelClick()
	{
		this.confirmDialog.hide();
	}
	
	void executeClick()
	{		
		this.confirmDialog.hide();
	}
	
	private class ConfirmationPanel extends VerticalPanel
	{
		StyledButton confirmBtn;
		StyledButton cancelBtn;
		
		public ConfirmationPanel()
		{
			this.setSpacing(5);
			
			Label confirm = new Label(ConfirmationStyledButton.this.confirmMsg);
			
			this.confirmBtn = new StyledButton("confirm", "Confirm");
			this.confirmBtn.addClickListener(new ClickListener()
			{
				public void onClick(Widget sender)
				{
					ConfirmationStyledButton.this.executeClick();		
				}
			});
			
			this.cancelBtn = new StyledButton("cancel", "Cancel");
			this.cancelBtn.addClickListener(new ClickListener()
			{
				public void onClick(Widget sender)
				{
					ConfirmationStyledButton.this.cancelClick();	
				}
			});
			
			this.add(confirm);
			
			HorizontalPanel buttons = new HorizontalPanel();
			buttons.setSpacing(5);
			buttons.add(this.confirmBtn);
			buttons.add(this.cancelBtn);
			
			this.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
			this.add(buttons);
		}

		/**
		 * @param listener
		 * @see com.google.gwt.user.client.ui.FocusWidget#addClickListener(com.google.gwt.user.client.ui.ClickListener)
		 */
		public void addClickListener(ClickListener listener)
		{
			this.confirmBtn.addClickListener(listener);
		}
	}
	
	private class ConfirmDialog extends DialogBox
	{
		protected ConfirmationPanel confirmPanel;
		
		public ConfirmDialog()
		{
			this.setText("Confirm Action");
			this.confirmPanel = new ConfirmationPanel();			
			this.add(this.confirmPanel);
		}	
	}
}

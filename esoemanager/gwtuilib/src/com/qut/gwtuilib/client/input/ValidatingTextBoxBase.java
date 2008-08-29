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

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FocusListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.Widget;
import com.qut.gwtuilib.client.CSSConstants;
import com.qut.gwtuilib.client.display.Help;
import com.qut.gwtuilib.client.display.RequiredField;
import com.qut.gwtuilib.client.eventdriven.eventmgr.EventController;
import com.qut.gwtuilib.client.eventdriven.events.EventConstants;
import com.qut.gwtuilib.client.eventdriven.events.MessageEvent;
import com.qut.gwtuilib.client.exceptions.InvalidContentException;

public abstract class ValidatingTextBoxBase extends HorizontalPanel
{
	private int min;
	private String regex;
	private String errMsg;
	private String areaID;
	
	private RequiredField required;
	private Help help;
	
	public void init(int min, String regex, String errMsg, String areaID, String helpLink)
	{
		this.help = new Help(helpLink);
		this.init(min, regex, errMsg, areaID);
	}
	
	public void init(int min, String regex, String errMsg, String areaID)
	{
		this.min = min;
		this.regex = regex;
		this.errMsg = errMsg;
		this.areaID = areaID;

		this.add(this.getTextBoxBase());
		
		if(this.help != null)
			this.add(this.help);
		
		if(min > 0)
		{
			this.required = new RequiredField();
			this.add(this.required);
		}
		
		this.getTextBoxBase().addChangeListener(new ChangeListener()
		{
			public void onChange(Widget sender)
			{
				if(!isValid(ValidatingTextBoxBase.this.getTextBoxBase().getText()))
				{
					ValidatingTextBoxBase.this.getTextBoxBase().setFocus(true);
				}
			}
		});
	}
	
	protected abstract TextBoxBase getTextBoxBase();
	
	/**
	 * If the content is considered valid the textual data is returned
	 * @return Valid String content of this textbox
	 * @throws InvalidContentException
	 */
	public String getText() throws InvalidContentException
	{
		if(!isValid(this.getTextBoxBase().getText()))
		{
			throw new InvalidContentException ("Content does not match requirements");
			
		}
		return getTextBoxBase().getText();		
	}
	
	/**
	 * If the content is considered valid the textual data is set for the enclosed textbox
	 * @throws InvalidContentException
	 */
	public void setText(String text) throws InvalidContentException
	{
		// Allow null to be set to effectively reset values
		if(text != null && !isValid(text))
		{
			throw new InvalidContentException ("Content does not match requirements");
		}
		this.getTextBoxBase().setText(text);
	}
	/**
	 * Determines if the content of the basetextbox is valid or not
	 * @return boolean state indicating validity
	 */
	public boolean isValid()
	{
		return isValid(this.getTextBoxBase().getText());
	}
	
	/**
	 * Determines if the supplied content is valid to current requirements
	 * @param content The String to be evaluated
	 * @return boolean state indicating validity
	 */
	private boolean isValid(String content)
	{		
		this.getTextBoxBase().removeStyleName(CSSConstants.contentError);
		
		if(this.min == 0 && content.length() == 0)
			return true;
		
		if (content == null || content.length() < this.min )
		{
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, this.areaID, MessageEvent.error, this.errMsg));
			this.getTextBoxBase().addStyleName(CSSConstants.contentError);
			return false;
		}

		if (this.regex != null && this.regex.length() > 0 && !content.matches(this.regex))
		{
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, this.areaID, MessageEvent.error, this.errMsg));
			this.getTextBoxBase().addStyleName(CSSConstants.contentError);
			return false;
		}

		return true;
	}

	/**
	 * @param listener
	 * @see com.google.gwt.user.client.ui.TextBoxBase#addChangeListener(com.google.gwt.user.client.ui.ChangeListener)
	 */
	public void addChangeListener(ChangeListener listener)
	{
		getTextBoxBase().addChangeListener(listener);
	}

	/**
	 * @param listener
	 * @see com.google.gwt.user.client.ui.FocusWidget#addClickListener(com.google.gwt.user.client.ui.ClickListener)
	 */
	public void addClickListener(ClickListener listener)
	{
		getTextBoxBase().addClickListener(listener);
	}

	/**
	 * @param listener
	 * @see com.google.gwt.user.client.ui.FocusWidget#addFocusListener(com.google.gwt.user.client.ui.FocusListener)
	 */
	public void addFocusListener(FocusListener listener)
	{
		getTextBoxBase().addFocusListener(listener);
	}

	/**
	 * @param listener
	 * @see com.google.gwt.user.client.ui.FocusWidget#addKeyboardListener(com.google.gwt.user.client.ui.KeyboardListener)
	 */
	public void addKeyboardListener(KeyboardListener listener)
	{
		getTextBoxBase().addKeyboardListener(listener);
	}

	/**
	 * @return
	 * @see com.google.gwt.user.client.ui.TextBoxBase#getSelectedText()
	 */
	public String getSelectedText()
	{
		return getTextBoxBase().getSelectedText();
	}

	/**
	 * @return
	 * @see com.google.gwt.user.client.ui.TextBoxBase#getSelectionLength()
	 */
	public int getSelectionLength()
	{
		return getTextBoxBase().getSelectionLength();
	}
}

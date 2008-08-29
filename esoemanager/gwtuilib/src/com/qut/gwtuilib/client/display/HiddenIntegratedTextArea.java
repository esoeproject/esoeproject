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
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.qut.gwtuilib.client.CSSConstants;
import com.qut.gwtuilib.client.eventdriven.eventmgr.EventController;
import com.qut.gwtuilib.client.eventdriven.events.EventConstants;
import com.qut.gwtuilib.client.eventdriven.events.HiddenIntegratedTextAreaEvent;
import com.qut.gwtuilib.client.exceptions.InvalidContentException;
import com.qut.gwtuilib.client.input.StyledButton;
import com.qut.gwtuilib.client.input.ValidatingTextArea;

public class HiddenIntegratedTextArea 
{	
	protected Label titleLbl;
	protected HTML editableLbl;
	protected HorizontalPanel data, editor;
	protected ValidatingTextArea validatingTextArea;
	protected StyledButton saveChange;
	protected StyledButton cancelChange;

	boolean dirty;
	
	private String areaID;
	Object parent;

	public HiddenIntegratedTextArea(Object parent, int min, String errMsg, String title, String areaID)
	{
		this.parent = parent;
		this.areaID = areaID;
		createInterface(min, errMsg, title, null);
	}
	
	public HiddenIntegratedTextArea(Object parent, int min, String errMsg, String title, String areaID, String helpLink)
	{
		this.parent = parent;
		this.areaID = areaID;
		createInterface(min, errMsg, title, helpLink);
	}
	
	protected void createInterface(int min, String errMsg, String title, String helpLink)
	{
		this.data = new HorizontalPanel();
		this.editor = new HorizontalPanel();
		this.editor.setSpacing(5);
		
		this.dirty = false;
		this.titleLbl = new Label(title);
		this.titleLbl.addStyleName(CSSConstants.integratedTextBoxTitle);

		if(helpLink != null && helpLink.length() > 0)
			this.validatingTextArea = new ValidatingTextArea(min, errMsg, this.areaID, helpLink);
		else
			this.validatingTextArea = new ValidatingTextArea(min, errMsg, this.areaID);
		
		this.validatingTextArea.addStyleName(CSSConstants.integratedTextBoxValue);
		this.editableLbl = new HTML();
		this.saveChange = new StyledButton("save", "");
		this.cancelChange = new StyledButton("cancel", "");
		
		this.editor.add(this.validatingTextArea);
		this.editor.add(this.saveChange);
		this.editor.add(this.cancelChange);

		this.data.add(this.editableLbl);
		this.data.add(this.editor);
		
		this.editor.setVisible(false);

		this.editableLbl.addClickListener(new ClickListener()
		{
			public void onClick(Widget sender)
			{
				if (HiddenIntegratedTextArea.this.editableLbl.isVisible())
				{
					try
					{
						HiddenIntegratedTextArea.this.validatingTextArea.setText(HiddenIntegratedTextArea.this.editableLbl.getText());
						HiddenIntegratedTextArea.this.editableLbl.setVisible(false);
						HiddenIntegratedTextArea.this.editor.setVisible(true);
					}
					catch (InvalidContentException e)
					{
					}
				}
			}
		});

		this.saveChange.addClickListener(new ClickListener()
		{
			public void onClick(Widget sender)
			{
				if (HiddenIntegratedTextArea.this.validatingTextArea.isValid())
				{
					try
					{
						HiddenIntegratedTextArea.this.dirty = true;
						HiddenIntegratedTextArea.this.editor.setVisible(false);
						
						HiddenIntegratedTextArea.this.editableLbl.setHTML(HiddenIntegratedTextArea.this.validatingTextArea.getText());
						HiddenIntegratedTextArea.this.editableLbl.setVisible(true);
						
						EventController.executeEvent(new HiddenIntegratedTextAreaEvent(EventConstants.integratedTextAreaUpdated,
								HiddenIntegratedTextArea.this.areaID, HiddenIntegratedTextArea.this.parent));
					}
					catch (InvalidContentException e)
					{
					}
				}
			}
		});

		this.cancelChange.addClickListener(new ClickListener()
		{
			public void onClick(Widget sender)
			{
				HiddenIntegratedTextArea.this.editor.setVisible(false);
				HiddenIntegratedTextArea.this.editableLbl.setVisible(true);
				
				EventController.executeEvent(new HiddenIntegratedTextAreaEvent(EventConstants.integratedTextAreaCancel,
						HiddenIntegratedTextArea.this.areaID, HiddenIntegratedTextArea.this.parent));
			}
		});
	}
	
	public void hideEditor()
	{
		HiddenIntegratedTextArea.this.editableLbl.setVisible(false);
		HiddenIntegratedTextArea.this.editor.setVisible(true);
	}
	
	public void showEditor()
	{
		HiddenIntegratedTextArea.this.editableLbl.setVisible(false);
		HiddenIntegratedTextArea.this.editor.setVisible(true);
	}

	public Label getTitle()
	{
		return this.titleLbl;
	}

	public Panel getContent()
	{
		return this.data;
	}

	public void setText(String text) throws InvalidContentException
	{
		this.validatingTextArea.setText(text);
		this.editableLbl.setText(text);

		this.dirty = true;
	}

	public String getText() throws InvalidContentException
	{
		return this.validatingTextArea.getText();
	}

	public boolean isDirty()
	{
		return this.dirty;
	}

	/**
	 * @return
	 * @see com.qut.gwtuilib.client.input.ValidatingTextBoxBase#isValid()
	 */
	public boolean isValid()
	{
		return validatingTextArea.isValid();
	}
	
	public void addTableRow(FlexibleTable table)
	{
		table.insertWidget(this.getTitle());
		table.insertWidget(this.getContent());
		table.nextRow();
	}

}

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
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.qut.gwtuilib.client.CSSConstants;
import com.qut.gwtuilib.client.eventdriven.eventmgr.EventController;
import com.qut.gwtuilib.client.eventdriven.events.EventConstants;
import com.qut.gwtuilib.client.eventdriven.events.HiddenIntegratedTextBoxEvent;
import com.qut.gwtuilib.client.exceptions.InvalidContentException;
import com.qut.gwtuilib.client.input.StyledButton;
import com.qut.gwtuilib.client.input.ValidatingTextBox;

public class HiddenIntegratedTextBox
{
	protected Label titleLbl;
	protected Label editableLbl;
	protected HorizontalPanel data, editor;
	protected ValidatingTextBox validatingTextBox;
	protected StyledButton saveChange;
	protected StyledButton cancelChange;

	boolean dirty;

	private String areaID;
	Object parent;

	public HiddenIntegratedTextBox(Object parent, int min, String regex, String errMsg, String title, String areaID)
	{
		this.parent = parent;
		this.areaID = areaID;
		createInterface(min, regex, errMsg, title, null);
	}

	public HiddenIntegratedTextBox(Object parent, int min, String regex, String errMsg, String title, String areaID,
			String helpLink)
	{
		this.parent = parent;
		this.areaID = areaID;
		createInterface(min, regex, errMsg, title, helpLink);
	}

	protected void createInterface(int min, String regex, String errMsg, String title, String helpLink)
	{
		this.data = new HorizontalPanel();
		this.data.addStyleName(CSSConstants.hiddenIntegratedTextBox);
		this.editor = new HorizontalPanel();
		this.editor.setSpacing(5);

		this.dirty = false;
		this.titleLbl = new Label(title);
		this.titleLbl.addStyleName(CSSConstants.integratedTextBoxTitle);

		if (helpLink != null && helpLink.length() > 0)
			this.validatingTextBox = new ValidatingTextBox(min, regex, errMsg, this.areaID, helpLink);
		else
			this.validatingTextBox = new ValidatingTextBox(min, regex, errMsg, this.areaID);

		this.validatingTextBox.addStyleName(CSSConstants.integratedTextBoxValue);
		this.editableLbl = new Label(" ");
		this.editableLbl.addStyleName(CSSConstants.integratedTextBoxLabel);
		this.saveChange = new StyledButton("confirm", "");
		this.cancelChange = new StyledButton("cancel", "");

		this.editor.add(this.validatingTextBox);
		this.editor.add(this.saveChange);
		this.editor.add(this.cancelChange);

		this.data.add(this.editableLbl);
		this.data.add(this.editor);

		this.editor.setVisible(false);

		this.editableLbl.addClickListener(new ClickListener()
		{
			public void onClick(Widget sender)
			{
				if (HiddenIntegratedTextBox.this.editableLbl.isVisible())
				{
					try
					{
						HiddenIntegratedTextBox.this.validatingTextBox.setText(HiddenIntegratedTextBox.this.editableLbl
								.getText());
						HiddenIntegratedTextBox.this.editableLbl.setVisible(false);
						HiddenIntegratedTextBox.this.editor.setVisible(true);
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
				if (HiddenIntegratedTextBox.this.validatingTextBox.isValid())
				{
					try
					{
						HiddenIntegratedTextBox.this.dirty = true;
						HiddenIntegratedTextBox.this.editor.setVisible(false);
						HiddenIntegratedTextBox.this.editableLbl.setText(HiddenIntegratedTextBox.this.validatingTextBox
								.getText());
						HiddenIntegratedTextBox.this.editableLbl.setVisible(true);

						EventController.executeEvent(new HiddenIntegratedTextBoxEvent(
								EventConstants.integratedTextBoxUpdated, HiddenIntegratedTextBox.this.areaID,
								HiddenIntegratedTextBox.this.parent));
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
				if (HiddenIntegratedTextBox.this.validatingTextBox.isValid())
				{
					HiddenIntegratedTextBox.this.editor.setVisible(false);
					HiddenIntegratedTextBox.this.editableLbl.setVisible(true);

					EventController.executeEvent(new HiddenIntegratedTextBoxEvent(
							EventConstants.integratedTextBoxCancel, HiddenIntegratedTextBox.this.areaID,
							HiddenIntegratedTextBox.this.parent));
				}
			}
		});
	}

	public void hideEditor()
	{
		HiddenIntegratedTextBox.this.editableLbl.setVisible(false);
		HiddenIntegratedTextBox.this.editor.setVisible(true);
	}

	public void showEditor()
	{
		HiddenIntegratedTextBox.this.editableLbl.setVisible(false);
		HiddenIntegratedTextBox.this.editor.setVisible(true);
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
		this.validatingTextBox.setText(text);
		this.editableLbl.setText(text);

		this.dirty = true;
	}

	public String getText() throws InvalidContentException
	{
		return this.validatingTextBox.getText();
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
		return validatingTextBox.isValid();
	}

	public void addTableRow(FlexibleTable table)
	{
		table.insertWidget(this.getTitle());
		table.insertWidget(this.getContent());
		table.nextRow();
	}
}

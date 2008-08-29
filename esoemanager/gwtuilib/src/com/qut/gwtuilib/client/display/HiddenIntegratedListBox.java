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
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.qut.gwtuilib.client.CSSConstants;
import com.qut.gwtuilib.client.eventdriven.eventmgr.EventController;
import com.qut.gwtuilib.client.eventdriven.events.EventConstants;
import com.qut.gwtuilib.client.eventdriven.events.HiddenIntegratedListBoxEvent;
import com.qut.gwtuilib.client.input.StyledButton;

public class HiddenIntegratedListBox
{
	protected Label titleLbl;
	protected Label editableLbl;
	protected HorizontalPanel data, editor;
	protected ListBox listBox;
	protected StyledButton saveChange;
	protected StyledButton cancelChange;
	private Help help;

	boolean dirty;

	private String areaID;
	Object parent;

	public HiddenIntegratedListBox(Object parent, String title, String areaID, String helpLink)
	{
		this.parent = parent;
		this.areaID = areaID;

		this.help = new Help(helpLink);
		createInterface(title);
	}

	public HiddenIntegratedListBox(Object parent, String title, String areaID)
	{
		this.parent = parent;
		this.areaID = areaID;

		createInterface(title);
	}

	protected void createInterface(String title)
	{
		this.data = new HorizontalPanel();
		this.editor = new HorizontalPanel();

		this.dirty = false;
		this.titleLbl = new Label(title);
		this.titleLbl.addStyleName(CSSConstants.integratedTextBoxTitle);

		this.editableLbl = new Label();
		this.listBox = new ListBox();

		this.editor.add(this.listBox);

		if (this.help != null)
			this.editor.add(this.help);

		this.saveChange = new StyledButton("save", "");
		this.cancelChange = new StyledButton("cancel", "");

		this.editor.add(this.saveChange);
		this.editor.add(this.cancelChange);

		this.data.add(this.editableLbl);
		this.data.add(this.editor);

		this.editor.setVisible(false);

		this.editableLbl.addClickListener(new ClickListener()
		{
			public void onClick(Widget sender)
			{
				if (HiddenIntegratedListBox.this.editableLbl.isVisible())
				{
					HiddenIntegratedListBox.this.editableLbl.setVisible(false);
					HiddenIntegratedListBox.this.editor.setVisible(true);
				}
			}
		});

		this.saveChange.addClickListener(new ClickListener()
		{
			public void onClick(Widget sender)
			{
				HiddenIntegratedListBox.this.dirty = true;
				HiddenIntegratedListBox.this.editor.setVisible(false);
				HiddenIntegratedListBox.this.editableLbl.setText(HiddenIntegratedListBox.this.listBox
						.getValue(HiddenIntegratedListBox.this.listBox.getSelectedIndex()));
				HiddenIntegratedListBox.this.editableLbl.setVisible(true);

				EventController.executeEvent(new HiddenIntegratedListBoxEvent(EventConstants.integratedListBoxUpdated,
						HiddenIntegratedListBox.this.areaID, HiddenIntegratedListBox.this.parent));
			}
		});

		this.cancelChange.addClickListener(new ClickListener()
		{
			public void onClick(Widget sender)
			{
				HiddenIntegratedListBox.this.editor.setVisible(false);
				HiddenIntegratedListBox.this.editableLbl.setVisible(true);

				EventController.executeEvent(new HiddenIntegratedListBoxEvent(EventConstants.integratedListBoxCancel,
						HiddenIntegratedListBox.this.areaID, HiddenIntegratedListBox.this.parent));
			}
		});
	}

	public Label getTitle()
	{
		return this.titleLbl;
	}

	public Panel getContent()
	{
		return this.data;
	}
	
	public void setValue(String value)
	{
		this.editableLbl.setText(value);
		
		for(int i = 0; i < this.listBox.getItemCount(); i++)
		{
			String item = this.listBox.getItemText(i);
			if(value.equals(item))
			{
				this.listBox.setSelectedIndex(i);
				break;
			}
		}
	}
	
	public String getValue()
	{
		return this.editableLbl.getText();
	}

	public String getCurrentValue()
	{
		return this.listBox.getItemText(this.listBox.getSelectedIndex());
	}

	public boolean isDirty()
	{
		return this.dirty;
	}

	public ListBox getBackingListBox()
	{
		return this.listBox;
	}

	public void setBackingListBox(ListBox listBox)
	{
		this.listBox = listBox;
	}
	

	/**
	 * @param item
	 * @see com.google.gwt.user.client.ui.ListBox#addItem(java.lang.String)
	 */
	public void addItem(String item)
	{
		listBox.addItem(item);
	}

	/**
	 * @return
	 * @see com.google.gwt.user.client.ui.ListBox#getItemCount()
	 */
	public int getItemCount()
	{
		return listBox.getItemCount();
	}

	/**
	 * @param index
	 * @return
	 * @see com.google.gwt.user.client.ui.ListBox#getItemText(int)
	 */
	public String getItemText(int index)
	{
		return listBox.getItemText(index);
	}

	/**
	 * @return
	 * @see com.google.gwt.user.client.ui.ListBox#getSelectedIndex()
	 */
	public int getSelectedIndex()
	{
		return listBox.getSelectedIndex();
	}

	/**
	 * @param index
	 * @return
	 * @see com.google.gwt.user.client.ui.ListBox#getValue(int)
	 */
	public String getValue(int index)
	{
		return listBox.getValue(index);
	}

	/**
	 * @param index
	 * @see com.google.gwt.user.client.ui.ListBox#removeItem(int)
	 */
	public void removeItem(int index)
	{
		listBox.removeItem(index);
	}
}

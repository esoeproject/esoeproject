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

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.qut.gwtuilib.client.CSSConstants;
import com.qut.gwtuilib.client.exceptions.InvalidContentException;

public class IntegratedListBox
{
	protected Label titleLbl;
	protected HorizontalPanel data;
	protected ListBox listBox;
	private Help help;
	
	Object parent;

	public IntegratedListBox(Object parent, String title, String helpLink)
	{
		this.parent = parent;
		this.help = new Help(helpLink);
		createInterface(title);
	}

	public IntegratedListBox(Object parent, String title)
	{
		this.parent = parent;
		createInterface(title);
	}

	protected void createInterface(String title)
	{
		this.data = new HorizontalPanel();
		this.titleLbl = new Label(title);
		this.titleLbl.addStyleName(CSSConstants.integratedTextBoxTitle);

		this.listBox = new ListBox();

		this.data.add(this.listBox);

		if (this.help != null)
			this.data.add(this.help);
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
	
	public void selectItem(String value) throws InvalidContentException
	{
		for(int i = 0; i < this.listBox.getItemCount(); i++)
		{
			if(this.listBox.getItemText(i).equals(value))
			{
				this.listBox.setSelectedIndex(i);
				return;
			}
		}
		
		throw new InvalidContentException("Value does not exist in the list, it can't be selected");
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

	public Label getTitle()
	{
		return this.titleLbl;
	}

	public Panel getContent()
	{
		return this.data;
	}

	public String getCurrentValue()
	{
		return this.listBox.getItemText(this.listBox.getSelectedIndex());
	}

	public ListBox getBackingListBox()
	{
		return this.listBox;
	}

	public void setBackingListBox(ListBox listBox)
	{
		this.listBox = listBox;
	}
}

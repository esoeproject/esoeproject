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

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Widget;
import com.qut.gwtuilib.client.CSSConstants;

public class FlexibleTable extends FlexTable
{
	int currentRow;
	int currentColumn;
	
	public FlexibleTable(int cellPadding, int cellSpacing)
	{
		super();
		this.addStyleName(CSSConstants.flexTable);
		this.setBorderWidth(0);
		this.setCellPadding(cellPadding);
		this.setCellSpacing(cellSpacing);
		this.setBorderWidth(0);
		
		this.currentRow = 1;
		this.currentColumn = 0;
	}
	
	public void nextRow()
	{
		this.currentRow++;
		this.currentColumn = 0;
	}
	
	public void insertHTML(String html)
	{
		this.setHTML(this.currentRow, this.currentColumn, html);
		this.currentColumn++;
	}
	
	public void insertText(String text)
	{
		this.setText(this.currentRow, this.currentColumn, text);
		this.currentColumn++;
	}
	
	public void insertWidget(Widget widget)
	{
		this.setWidget(this.currentRow, this.currentColumn, widget);
		this.currentColumn++;
	}
	
	public void insertHeading(int column, String text)
	{
		super.setText(0, column, text);
		this.getCellFormatter().addStyleName(0, column, CSSConstants.flexTableHeading);
	}
	
	@Override
	public void clear()
	{
		super.clear();
		this.currentRow = 1;
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.HTMLTable#setHTML(int, int, java.lang.String)
	 */
	@Override
	public void setHTML(int row, int column, String html)
	{
		super.setHTML(row, column, html);
		if((row % 2) == 0)
			this.getRowFormatter().addStyleName(row, CSSConstants.flexTableEvenRow);
		else
			this.getRowFormatter().addStyleName(row, CSSConstants.flexTableOddRow);
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.HTMLTable#setText(int, int, java.lang.String)
	 */
	@Override
	public void setText(int row, int column, String text)
	{
		super.setText(row, column, text);
		if((row % 2) == 0)
			this.getRowFormatter().addStyleName(row, CSSConstants.flexTableEvenRow);
		else
			this.getRowFormatter().addStyleName(row, CSSConstants.flexTableOddRow);
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.HTMLTable#setWidget(int, int, com.google.gwt.user.client.ui.Widget)
	 */
	@Override
	public void setWidget(int row, int column, Widget widget)
	{
		super.setWidget(row, column, widget);
		if((row % 2) == 0)
			this.getRowFormatter().addStyleName(row, CSSConstants.flexTableEvenRow);
		else
			this.getRowFormatter().addStyleName(row, CSSConstants.flexTableOddRow);
	}

}

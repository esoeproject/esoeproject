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

import com.google.gwt.user.client.ui.Label;
import com.qut.gwtuilib.client.CSSConstants;

public class IntegratedLabel
{
	Label titleLbl;
	Label nonEditableLbl;
	
	Object parent;

	public IntegratedLabel(Object parent, String title)
	{
		this.parent = parent;
		
		this.titleLbl = new Label();
		if (title != null && title.length() > 0)
		{
			this.titleLbl.setText(title);
			this.titleLbl.addStyleName(CSSConstants.integratedLabelTitle);
		}

		this.nonEditableLbl = new Label();
	}
	
	public IntegratedLabel(String title, String content)
	{
		this.parent = parent;
		
		this.titleLbl = new Label();
		if (title != null && title.length() > 0)
		{
			this.titleLbl.setText(title);
			this.titleLbl.addStyleName(CSSConstants.integratedLabelTitle);
		}

		this.nonEditableLbl = new Label();
		this.nonEditableLbl.setText(content);
	}

	public void setText(String text)
	{
		this.nonEditableLbl.setText(text);
	}

	public String getText()
	{
		return this.nonEditableLbl.getText();
	}
	
	public Label getTitle()
	{
		return this.titleLbl;
	}
	
	public Label getContent()
	{
		return this.nonEditableLbl;
	}
	
	public void addTableRow(FlexibleTable table)
	{
		table.insertWidget(this.getTitle());
		table.insertWidget(this.getContent());
		table.nextRow();
	}
}

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

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.MouseListenerAdapter;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.qut.gwtuilib.client.CSSConstants;

public class ToolTip extends MouseListenerAdapter
{
	private ToolTipPopup toolTip;
	private String text;
	private int delay, xOff, yOff;

	public ToolTip(String text, int delay, int xOff, int yOff)
	{
		this.text = text;
		this.delay = delay;
		this.xOff = xOff;
		this.yOff = yOff;
	}
	
	public ToolTip(String text)
	{
		this.text = text;
		this.delay = 5000;
		this.xOff = 15;
		this.yOff = 10;
	}

	@Override
	public void onMouseEnter(Widget sender)
	{
		if (this.toolTip != null)
		{
			toolTip.hide();
		}
		toolTip = new ToolTipPopup(sender, this.xOff, this.yOff, this.text, this.delay);
		toolTip.show();
	}

	@Override
	public void onMouseLeave(Widget sender)
	{
		if (toolTip != null)
		{
			toolTip.hide();
		}
	}
	
	private class ToolTipPopup extends PopupPanel
	{
		private int delay;

		public ToolTipPopup(Widget sender, int offsetX, int offsetY, final String text, final int delay)
		{
			super(true);

			this.delay = delay;

			HTML content = new HTML(text);
			add(content);

			int left = sender.getAbsoluteLeft() + offsetX;
			int top = sender.getAbsoluteTop() + offsetY;

			setPopupPosition(left, top);
			setStyleName(CSSConstants.toolTip);
		}

		public void show()
		{
			super.show();

			Timer t = new Timer()
			{
				public void run()
				{
					ToolTipPopup.this.hide();
				}
			};
			t.schedule(delay);
		}
	}
}

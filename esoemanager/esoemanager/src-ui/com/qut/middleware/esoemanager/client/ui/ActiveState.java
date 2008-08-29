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
package com.qut.middleware.esoemanager.client.ui;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.qut.middleware.esoemanager.client.CSSConstants;

public class ActiveState extends HorizontalPanel
{
	private Label status;
	private boolean active;
	
	public ActiveState()
	{
		this.createInterface();
	}
	
	private void createInterface()
	{
		this.status = new Label();
		this.addStyleName(CSSConstants.esoeManagerActiveState);
		this.add(this.status);
	}
	
	public void setActivated()
	{
		this.active = true;
		this.removeStyleName(CSSConstants.esoeManagerActiveStateDeActivated);
		this.addStyleName(CSSConstants.esoeManagerActiveStateActivated);
		this.status.setText("Activated");
	}
	
	public void setDeactivated()
	{
		this.active = false;
		this.removeStyleName(CSSConstants.esoeManagerActiveStateActivated);
		this.addStyleName(CSSConstants.esoeManagerActiveStateDeActivated);
		this.status.setText("Deactivated");
	}
	
	public boolean isActive()
	{
		return this.active;
	}
}

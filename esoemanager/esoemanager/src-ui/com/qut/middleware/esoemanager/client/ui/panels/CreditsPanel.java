package com.qut.middleware.esoemanager.client.ui.panels;

import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.qut.middleware.esoemanager.client.CSSConstants;

public class CreditsPanel extends VerticalPanel
{
	public CreditsPanel()
	{
		this.createInterface();
	}
	
	private void createInterface()
	{
		Frame content = new Frame("/esoemanager/manager/credits.htm");
		content.setWidth("100%");
		content.setHeight("600px");
		content.addStyleName(CSSConstants.creditContent);
		
		this.add(content);
	}
}

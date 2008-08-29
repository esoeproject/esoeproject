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

import org.adamtacy.client.ui.EffectPanel;
import org.adamtacy.client.ui.effects.Effect;
import org.adamtacy.client.ui.effects.EffectHandlerAdapter;
import org.adamtacy.client.ui.effects.impl.Fade;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.qut.gwtuilib.client.CSSConstants;

public class MessagePanel extends HorizontalPanel
{
	protected EffectPanel effectPanel;
	protected HorizontalPanel container;
	protected Label message;
	protected boolean effectsInverted;

	private Timer t;

	private int appearTime = 10000;

	public MessagePanel()
	{
		this.setupEffects();
		this.createMessage();
		this.attachPanels();

		this.effectPanel.beginEffects();
		this.effectsInverted = true;
	}
	
	public MessagePanel(int appearTime)
	{
		this.appearTime = appearTime;
		
		this.setupEffects();
		this.createMessage();
		this.attachPanels();

		this.effectPanel.beginEffects();
		this.effectsInverted = true;
	}

	private void createMessage()
	{
		this.container = new HorizontalPanel();
		this.container.addStyleName(CSSConstants.message);

		this.message = new Label();
		this.container.add(this.message);
	}

	private void attachPanels()
	{
		this.effectPanel.add(this.container);
		this.add(this.effectPanel);
	}

	private void setupEffects()
	{
		this.effectPanel = new EffectPanel();

		Fade fade = new Fade();

		fade.addEffectHandler(new EffectHandlerAdapter()
		{
			public void preEvent(Effect theEffect)
			{
				super.preEvent(theEffect);
			}

			public void postEvent(Effect theEffect)
			{
				super.postEvent(theEffect);
			}
		});

		this.effectPanel.addEffect(fade);
	}

	public void errorMsg(String message)
	{
		this.message.setText(message);
		this.container.addStyleName(CSSConstants.messageError);
		this.container.removeStyleName(CSSConstants.messageInformation);
		this.container.removeStyleName(CSSConstants.messageOk);
		
		doDisplay();
	}

	public void informationMsg(String message)
	{
		this.message.setText(message);
		this.container.addStyleName(CSSConstants.messageInformation);
		this.container.removeStyleName(CSSConstants.messageError);
		this.container.removeStyleName(CSSConstants.messageOk);
		
		doDisplay();
	}

	public void okMsg(String message)
	{
		this.message.setText(message);
		this.container.addStyleName(CSSConstants.messageOk);
		this.container.removeStyleName(CSSConstants.messageError);
		this.container.removeStyleName(CSSConstants.messageInformation);
		
		doDisplay();
	}

	private void doDisplay()
	{

		if (this.effectsInverted)
		{
			this.effectPanel.invertEffects();
			this.effectPanel.beginEffects();
			this.effectsInverted = false;
		}

		t = new Timer()
		{
			public void run()
			{
				if (!MessagePanel.this.effectsInverted)
				{
					MessagePanel.this.effectPanel.invertEffects();
					MessagePanel.this.effectPanel.beginEffects();
					MessagePanel.this.effectsInverted = true;
				}
			}
		};

		t.schedule(this.appearTime);
	}

}

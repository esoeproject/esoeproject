/* 
 * Copyright 2006, Queensland University of Technology
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
 * 
 * Author: Bradley Beddoes
 * Creation Date: 1/5/07
 */
package com.qut.middleware.delegator.openid.pages;

import net.sf.click.Page;
import net.sf.click.util.HtmlStringBuffer;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class BorderPage extends Page implements ApplicationContextAware
{
	/* Spring Integration */
	protected ApplicationContext applicationContext;
	
	/**
	 * Create a BorderedPage and set the model attributes <tt>$title</tt> and <tt>$srcPath</tt>.
	 */
	public BorderPage()
	{
		String className = getClass().getName();

		String shortName = className.substring(className.lastIndexOf('.') + 1);
		HtmlStringBuffer title = new HtmlStringBuffer();
		title.append(shortName.charAt(0));
		for (int i = 1; i < shortName.length(); i++)
		{
			char aChar = shortName.charAt(i);
			if (Character.isUpperCase(aChar))
			{
				title.append(' ');
			}
			title.append(aChar);
		}
		addModel("title", title);

		String srcPath = className.replace('.', '/') + ".java";
		addModel("srcPath", srcPath);
	}

	public void setApplicationContext(ApplicationContext applicationContext)
	{
		this.applicationContext = applicationContext;
	}

	public String getTemplate()
	{
		return "border-template.htm";
	}

	public void storeSession(String name, Object obj)
	{
		this.getContext().setSessionAttribute(name, obj);
	}
	
	public Object retrieveSession(String name)
	{
		return this.getContext().getSessionAttribute(name);
	}
	
	public void removeSession(String name)
	{
		this.getContext().removeSessionAttribute(name);
	}

	protected void removeSessionObject(Class aClass)
	{
		if (getContext().hasSession() && aClass != null)
		{
			getContext().getSession().removeAttribute(aClass.getName());
		}
	}

}

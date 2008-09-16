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
 * 
 * Author: Shaun Mangelsdorf
 * Creation Date: 15/09/2008
 * 
 * Purpose: Loads a plugin component from a bean factory.
 */

package com.qut.middleware.esoe.plugins;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.FactoryBean;

import com.qut.middleware.esoe.plugins.exception.PluginLoaderException;

public class PluginFactoryBean implements FactoryBean
{
	private BeanFactory beanFactory;
	private String beanName;

	public PluginFactoryBean(BeanFactory beanFactory, String beanName)
	{
		this.beanFactory = beanFactory;
		this.beanName = beanName;
	}

	public Object getObject() throws Exception
	{
		if (!this.beanFactory.containsBean(this.beanName))
		{
			throw new PluginLoaderException("Plugin loader did not get bean definition for bean name: " + this.beanName);
		}
		
		return this.beanFactory.getBean(this.beanName);
	}

	public Class<?> getObjectType()
	{
		return null;
	}

	public boolean isSingleton()
	{
		return true;
	}
}


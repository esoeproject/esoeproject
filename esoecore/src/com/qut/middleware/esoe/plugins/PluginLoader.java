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
 * Author: Bradley Beddoes
 * Creation Date: 
 * 
 * Purpose: 
 */
package com.qut.middleware.esoe.plugins;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;

import com.qut.middleware.esoe.plugins.exception.PluginLoaderException;

@SuppressWarnings("unchecked")
public class PluginLoader extends ArrayList implements BeanFactoryAware, InitializingBean
{
	private static final long serialVersionUID = 7668357227552947835L;

	private BeanFactory beanFactory;
	
	private String pluginPath;
	private String pluginType;
	private String[] pluginList;
	private URLClassLoader classLoader;

	private final String localPropertyConfigurer = "localConfig";
	private final String jarLoc = "jars";
	private final String springLoc = "spring";
	private final String pluginSeperator = ","; //$NON-NLS-1$
	private final String jarExt = "jar"; //$NON-NLS-1$
	private final String xmlExt = "xml"; //$NON-NLS-1$
	private final String fileProtocol = "file"; //$NON-NLS-1$
	private final String period = "."; //$NON-NLS-1$

	private Logger logger = LoggerFactory.getLogger(PluginLoader.class.getName());

	public PluginLoader(String pluginType, String pluginPath, String plugins) throws PluginLoaderException
	{
		if(pluginType == null || pluginType.length() ==0)
		{
			throw new PluginLoaderException("Plugin types were not specified for plugin loader");
		}
		if(pluginPath == null || pluginPath.length() ==0)
		{
			throw new PluginLoaderException("Plugin path was not specified for plugin loader");
		}
		if(plugins == null || plugins.length() ==0)
		{
			throw new PluginLoaderException("Plugins are not specified for plugin loader located at - " + pluginPath);
		}
		
		this.pluginType = pluginType;
		this.pluginPath = pluginPath;
		this.pluginList = plugins.split(this.pluginSeperator);
	}

	public void afterPropertiesSet() throws Exception
	{
		this.setupClassloader();
		this.configurePlugins();
	}

	private void configurePlugins()
	{	
		for (String pluginName : pluginList)
		{
			PropertyPlaceholderConfigurer propertyConfigurer;
			
			File configXML = new File(this.pluginPath + File.separatorChar + pluginName.trim() + File.separatorChar + this.springLoc + File.separatorChar + pluginName.trim() + this.pluginType + period + this.xmlExt);
			this.logger.debug("Attempting to load bean definitions from config located at: " + configXML);

			FileSystemResource configResource = new FileSystemResource(configXML);
			XmlBeanFactory fac = new XmlBeanFactory(configResource, this.beanFactory);
			fac.setBeanClassLoader(this.classLoader);
			
			/* Determine if we have a local property configuration instance, use it if so */
			if(fac.containsBean(this.localPropertyConfigurer))
			{
				this.logger.info("Located local property configurer");
				propertyConfigurer = (PropertyPlaceholderConfigurer) fac.getBean(this.localPropertyConfigurer);
				propertyConfigurer.postProcessBeanFactory(fac);
			}

			Object plugin = fac.getBean(pluginName.trim() + this.pluginType);
			this.add(plugin);
		}
	}

	private void setupClassloader() throws PluginLoaderException
	{
		PluginFilter filter = new PluginFilter(this.jarExt);
		List<URL> pluginBinaries = new ArrayList<URL>();

		try
		{
			for (String pluginName : this.pluginList)
			{

				File pluginContent = new File(this.pluginPath  + File.separatorChar + pluginName.trim() + File.separatorChar + this.jarLoc);
				this.logger.debug("Loading plugin content from: " + pluginContent);
				
				String[] pluginBinaryList = pluginContent.list(filter);

				if (pluginBinaryList != null && pluginBinaryList.length > 0)
				{
					for (int i = 0; i < pluginBinaryList.length; i++)
					{
						URL binary = new URL(this.fileProtocol, null, pluginContent.getAbsolutePath() + File.separator + pluginBinaryList[i]);
						this.logger.info("Adding plugin binary for : " + binary);
						pluginBinaries.add(binary);
					}
				}
				else
				{
					this.logger.warn("Unable to locate any binaries for plugin: " + pluginName);
				}
			}

			this.classLoader = new URLClassLoader(pluginBinaries.toArray(new URL[1]), this.getClass().getClassLoader());
		}
		catch (MalformedURLException e)
		{
			this.logger.error("Unable to load classes for configured plugins", e);
			throw new PluginLoaderException(e.getLocalizedMessage(), e);
		}
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException
	{
		if(beanFactory != null)
		{
		this.logger.info("Setting parent bean factory");
		this.beanFactory = beanFactory;
		}
		else
		{
			this.logger.warn("Plugin loader bean factory parent was null");
		}
	}
}

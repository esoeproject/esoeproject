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
 * Purpose: Loads a plugin as a piece of spring config, and if that plugin config
 * 	did not exist, loads the default piece of config.
 */
package com.qut.middleware.esoe.plugins;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.beans.propertyeditors.InputStreamEditor;
import org.springframework.beans.propertyeditors.URLEditor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import com.qut.middleware.esoe.plugins.exception.PluginLoaderException;

@SuppressWarnings("unchecked")
public class OverridePluginLoader implements BeanFactoryAware, ApplicationContextAware, InitializingBean, BeanFactory, ResourceLoader
{
	private BeanFactory beanFactory;
	private ApplicationContext applicationContext;
	private XmlBeanFactory childFactory;

	private String pluginPath;
	private String defaultConfig;
	private PropertyPlaceholderConfigurer globalConfig;
	private ClassLoader classLoader;

	private final String localPropertyConfigurer = "localConfig";
	private final String jarLoc = "jars";
	private final String springLoc = "override";
	private final String jarExt = "jar";
	private final String xmlExt = "xml";
	private final String fileProtocol = "file";
	private final String resourceLoc = "resources";
	private final String period = ".";

	private Logger logger = LoggerFactory.getLogger(OverridePluginLoader.class.getName());

	public OverridePluginLoader(String pluginPath, String defaultConfig, PropertyPlaceholderConfigurer globalConfig) throws PluginLoaderException
	{
		if(pluginPath == null || pluginPath.length() == 0)
		{
			throw new PluginLoaderException("Plugin path was not specified for override plugin loader");
		}
		if(defaultConfig == null || defaultConfig.length() == 0)
		{
			throw new PluginLoaderException("Default config was not specified for override plugin loader");
		}
		if (globalConfig == null)
		{
			// Only warn here. It's not the end of the world.
			this.logger.warn("Global property placeholder configurer was not specified for override plugin loader");
		}
		
		this.pluginPath = pluginPath;
		this.defaultConfig = defaultConfig;
		this.globalConfig = globalConfig;
	}

	public void afterPropertiesSet() throws Exception
	{
		this.setupClassloader();
		this.configurePlugins();
	}

	private void configurePlugins() throws Exception
	{
		// Try to grab override config file.
		File configXML = new File(this.pluginPath + File.separatorChar + this.springLoc + period + this.xmlExt);
		this.logger.debug("Attempting to load bean definitions from config located at: " + configXML);
		
		Resource resource;
		// Get the override config resource if it exists, otherwise fall back to the default.
		if (configXML.exists())
		{
			this.logger.info("Override plugin config exists at plugin path: {} - going to configure from this XML", this.pluginPath);
			resource = new FileSystemResource(configXML);
		}
		else
		{
			this.logger.info("Override plugin config does not exist at plugin path: {} - using default configuration", this.pluginPath);
			resource = this.applicationContext.getResource(this.defaultConfig);
		}
		
		// Load the selected config resource.
		this.childFactory = new XmlBeanFactory(resource, this.beanFactory);
		this.childFactory.setBeanClassLoader(this.classLoader);
		
		// Set up resource loading for the child bean factory
		this.childFactory.registerCustomEditor(Resource.class, new ResourceEditor(this));
		this.childFactory.registerCustomEditor(InputStream.class, new InputStreamEditor(new ResourceEditor(this)));
		this.childFactory.registerCustomEditor(URL.class, new URLEditor(new ResourceEditor(this)));
		
		// Perform global configuration if we have the bean
		if (this.globalConfig != null)
		{
			this.logger.info("Configuring override plugin with global property configurer");
			this.globalConfig.postProcessBeanFactory(this.childFactory);
		}
		
		// Perform local configuration if necessary.
		if (this.childFactory.containsBean(this.localPropertyConfigurer))
		{
			this.logger.info("Located local property configurer");
			PropertyPlaceholderConfigurer propertyConfigurer = (PropertyPlaceholderConfigurer) this.childFactory.getBean(this.localPropertyConfigurer);
			propertyConfigurer.postProcessBeanFactory(this.childFactory);
		}
	}

	private void setupClassloader() throws PluginLoaderException
	{
		PluginFilter filter = new PluginFilter(this.jarExt);
		List<URL> pluginBinaries = new ArrayList<URL>();

		try
		{
			File pluginContent = new File(this.pluginPath  + File.separatorChar + this.jarLoc);
			this.logger.debug("Loading override plugin content from: " + pluginContent);
			
			// Find all the jars in ${plugindir}/jars
			String[] pluginBinaryList = pluginContent.list(filter);

			if (pluginBinaryList != null && pluginBinaryList.length > 0)
			{
				for (int i = 0; i < pluginBinaryList.length; i++)
				{
					// Add the jar as a URL into the list
					URL binary = new URL(this.fileProtocol, null, pluginContent.getAbsolutePath() + File.separator + pluginBinaryList[i]);
					this.logger.info("Adding plugin binary for : " + binary);
					pluginBinaries.add(binary);
				}
			}
			else
			{
				this.logger.warn("Unable to locate any binaries for override plugin, path: " + this.pluginPath);
			}
			
			// If there is a "resources" directory for the plugin, add it to the classpath so they can be resolved properly
			File pluginResources = new File(this.pluginPath + File.separatorChar + this.resourceLoc);
			if (pluginResources.exists())
			{
				String resourcePath = pluginResources.getAbsolutePath();
				if (!resourcePath.endsWith("/"))
				{
					// URLClassLoader doco says that it only treats something as a directory if the name ends with a /
					resourcePath = resourcePath + "/";
				}
				this.logger.info("Adding resources directory: {} to plugin classpath", resourcePath);
				pluginBinaries.add(new URL(this.fileProtocol, null, resourcePath));
			}
			
			// Create a classloader to reference all the jars detected at the location
			this.classLoader = new URLClassLoader(pluginBinaries.toArray(new URL[1]), this.applicationContext.getClassLoader());
		}
		catch (MalformedURLException e)
		{
			this.logger.error("Unable to load classes for configured override plugin", e);
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

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
	{
		if (applicationContext != null)
		{
			this.logger.info("Setting application context");
			this.applicationContext = applicationContext;
		}
		else
		{
			this.logger.warn("Application context was null");
		}
	}

	// Delegate methods
	public boolean containsBean(String arg0)
	{
		return childFactory.containsBean(arg0);
	}

	public String[] getAliases(String arg0)
	{
		return childFactory.getAliases(arg0);
	}

	public Object getBean(String arg0, Class arg1) throws BeansException
	{
		return childFactory.getBean(arg0, arg1);
	}

	public Object getBean(String arg0, Object[] arg1) throws BeansException
	{
		return childFactory.getBean(arg0, arg1);
	}

	public Object getBean(String arg0) throws BeansException
	{
		return childFactory.getBean(arg0);
	}

	public Class getType(String arg0) throws NoSuchBeanDefinitionException
	{
		return childFactory.getType(arg0);
	}

	public boolean isPrototype(String arg0) throws NoSuchBeanDefinitionException
	{
		return childFactory.isPrototype(arg0);
	}

	public boolean isSingleton(String arg0) throws NoSuchBeanDefinitionException
	{
		return childFactory.isSingleton(arg0);
	}

	public boolean isTypeMatch(String arg0, Class arg1) throws NoSuchBeanDefinitionException
	{
		return childFactory.isTypeMatch(arg0, arg1);
	}

	public ClassLoader getClassLoader()
	{
		return this.classLoader;
	}

	public Resource getResource(String path)
	{
		return new ClassPathResource(path, this.classLoader);
	}
}

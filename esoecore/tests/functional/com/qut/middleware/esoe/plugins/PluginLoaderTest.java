package com.qut.middleware.esoe.plugins;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import com.qut.middleware.esoe.authn.pipeline.Handler;

public class PluginLoaderTest
{
	private PluginLoader pluginLoader;

	@Before
	public void setUp() throws Exception
	{
	}

	@After
	public void tearDown() throws Exception
	{
	}

	@Test
	public void testPluginLoader() throws Exception
	{
		this.pluginLoader = new PluginLoader("Handler", "tests/testdata/plugins/authentication", "statsGeneration");
		this.pluginLoader.afterPropertiesSet();
		assertTrue("Ensure plugin loader has been populated with resource reference appropriately", this.pluginLoader.size() == 1);
		
		Handler tmp = (Handler)this.pluginLoader.get(0);
		assertTrue("Ensure correct plugin was referenced", tmp.getHandlerName().equals("ESOE Statistics Generator"));
	}
	
	@Test
	public void testPluginLoader2() throws Exception
	{
		try
		{
			this.pluginLoader = new PluginLoader("Handler", "tests/testdata/plugins/authentication", "noSuchPlugin");
			this.pluginLoader.afterPropertiesSet();
		}
		catch(BeanDefinitionStoreException e)
		{
			// Exactly what we expect to get....
			return;
		}
		
		fail("Expected BeanDefinitionStoreException due to invalid plugin");
	}

}

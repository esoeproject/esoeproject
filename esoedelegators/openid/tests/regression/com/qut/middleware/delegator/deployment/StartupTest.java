package com.qut.middleware.delegator.deployment;

import org.junit.Before;
import org.junit.Test;

public class StartupTest
{
	Startup startup;
	
	@Before
	public void setUp()
	{
		startup = new Startup();
	}

	@Test
	public void testConfigureEnvironment1() throws Exception
	{
		ConfigBean configBean = new ConfigBean();
		
		configBean.setCertExpiryInterval(2);
		configBean.setEsoeKeystore("/home/beddoes/programs/apache-tomcat-5.5.20/webapps/ROOT/WEB-INF/esoeKeystore.ks");
		configBean.setEsoeConfig("/home/beddoes/programs/apache-tomcat-5.5.20/webapps/ROOT/WEB-INF/esoe.config");
		configBean.setOutputDirectory("/tmp/openiddelegator");
		configBean.setOpenIDEndpoint("https://esoe.test.company.com/openiddelegator");
		configBean.setExtractedFiles("/home/beddoes/testing");
		
		startup.setConfigBean(configBean);
		startup.configureEnvironment();
		
	}

}

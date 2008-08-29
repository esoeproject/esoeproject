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
 */
package com.qut.middleware.deployer;

import java.io.IOException;

import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.qut.middleware.deployer.exception.ConfigurationException;
import com.qut.middleware.deployer.exception.RegisterESOEException;

public class Main {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ConfigurationException 
	 * @throws RegisterESOEException 
	 */
	public static void main(String[] args) throws IOException, ConfigurationException, RegisterESOEException {
		Deployer deployer = bootstrapSpring();
		
		deployer.execute();
	}

	private static Deployer bootstrapSpring()
	{
		FileSystemXmlApplicationContext appContext = new FileSystemXmlApplicationContext("spring/deployerContext.xml");
		
		Deployer deployer = (Deployer)appContext.getBean("deployer");
		return deployer;
	}
}

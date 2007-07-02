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
 * 
 * Purpose: Renders config files dynamically for ESOE OpenID delegator
 */
package com.qut.middleware.delegator.deployment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

public class RenderOIDDelegatorConfigLogic
{	
	private final String COMMENT = "#";
	private final String COMMENT_REPLACE = "_#";
	private final String VELOCITY_BEAN_NAME = "configBean";

	public RenderOIDDelegatorConfigLogic()
	{
	}
	
	public String generateConfig(File input, ConfigBean configBean) throws Exception
	{
		StringBuffer inBuffer = null;
		InputStream fileStream = null;
		Reader reader = null;
		BufferedReader in = null;

		/* So whats going on here....
		 * Because java properties files and velocity both use # to indicate comments
		 * and velocity always strips comments, we're forced to replace our # comment symbols with a temporary value _#,
		 * thats ignored by velocity, then on the way out we simply restore.
		 */
		StringWriter stringWriter = new StringWriter();
		String tmp;
		String ammendedDocument;
		inBuffer = new StringBuffer();
		fileStream = new FileInputStream(input);
		reader = new InputStreamReader(fileStream);
		in = new BufferedReader(reader);
					
		try
		{
			while ((tmp = in.readLine()) != null)
			{
				inBuffer.append(tmp);
				inBuffer.append(System.getProperty("line.separator"));
			}
		}
		finally
		{
				in.close();
				reader.close();
				fileStream.close();
		}
		
		ammendedDocument = inBuffer.toString().replace(this.COMMENT, this.COMMENT_REPLACE);
		
		Velocity.init();
		VelocityContext context = new VelocityContext();
		
		/* Put the configuration bean into velocity */
		context.put(this.VELOCITY_BEAN_NAME, configBean);

		Velocity.evaluate(context, stringWriter, input.getName(), ammendedDocument);
		
		return stringWriter.toString().replace(this.COMMENT_REPLACE, this.COMMENT);
	}
}

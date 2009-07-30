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
 * Creation Date: 11/12/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.esoe.sso.plugins.artifact.bean;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.w3c.dom.Element;

import com.qut.middleware.esoe.sso.plugins.artifact.exception.ArtifactBindingException;

public class ArtifactWSClient
{
	private static final String TARGET_METHOD_NAME = "artifactResolve";
	private Object wsClient;
	private Method artifactResolutionMethod;

	public ArtifactWSClient(Object wsClient)
	{
		this.wsClient = wsClient;
		try
		{
			this.wsClient.getClass().getMethod(TARGET_METHOD_NAME, Element.class, String.class);
		}
		catch (SecurityException e)
		{
			throw new IllegalArgumentException("Security exception while trying to get target method for artifact resolution.", e);
		}
		catch (NoSuchMethodException e)
		{
			throw new IllegalArgumentException("No such method while trying to get target method for artifact resolution.", e);
		}
	}
	
	public Element artifactResolve(Element element, String endpoint) throws ArtifactBindingException
	{
		try
		{
			return (Element)this.artifactResolutionMethod.invoke(this.wsClient, element, endpoint);
		}
		catch (IllegalArgumentException e)
		{
			throw new ArtifactBindingException("Illegal argument while invoking artifact resolution web service call", e);
		}
		catch (IllegalAccessException e)
		{
			throw new ArtifactBindingException("Illegal access while invoking artifact resolution web service call", e);
		}
		catch (InvocationTargetException e)
		{
			throw new ArtifactBindingException("Artifact resolution web service call invocation target threw an exception.", e);
		}
	}
}

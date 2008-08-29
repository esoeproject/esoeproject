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
package com.qut.middleware.esoemanager.client.rpc.bean;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ServiceStartupBean implements IsSerializable
{
	String serviceName;
	String nodeID;
	String ipAddress;
	String compiled;
	String compileSystem;
	String version;
	String env;
	Date date;
	public String getServiceName()
	{
		return serviceName;
	}
	public void setServiceName(String serviceName)
	{
		this.serviceName = serviceName;
	}
	public String getNodeID()
	{
		return nodeID;
	}
	public void setNodeID(String nodeID)
	{
		this.nodeID = nodeID;
	}
	public String getIpAddress()
	{
		return ipAddress;
	}
	public void setIpAddress(String ipAddress)
	{
		this.ipAddress = ipAddress;
	}
	public String getCompiled()
	{
		return compiled;
	}
	public void setCompiled(String compiled)
	{
		this.compiled = compiled;
	}
	public String getCompileSystem()
	{
		return compileSystem;
	}
	public void setCompileSystem(String compileSystem)
	{
		this.compileSystem = compileSystem;
	}
	public String getVersion()
	{
		return version;
	}
	public void setVersion(String version)
	{
		this.version = version;
	}
	public String getEnv()
	{
		return env;
	}
	public void setEnv(String env)
	{
		this.env = env;
	}
	public Date getDate()
	{
		return date;
	}
	public void setDate(Date date)
	{
		this.date = date;
	}
}

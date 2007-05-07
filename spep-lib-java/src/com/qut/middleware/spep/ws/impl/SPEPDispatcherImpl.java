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
 * Author:
 * Creation Date:
 * 
 * Purpose:
 */
package com.qut.middleware.spep.ws.impl;

import java.util.StringTokenizer;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.engine.AbstractDispatcher;
import org.apache.axis2.engine.AxisConfiguration;

import com.qut.middleware.spep.ConfigurationConstants;

/** */
public class SPEPDispatcherImpl extends AbstractDispatcher
{
	/* (non-Javadoc)
	 * @see org.apache.axis2.engine.AbstractDispatcher#findOperation(org.apache.axis2.description.AxisService, org.apache.axis2.context.MessageContext)
	 */
	@Override
	public AxisOperation findOperation(AxisService service, MessageContext messageContext) throws AxisFault
	{
		EndpointReference endpoint = messageContext.getTo();
		if (endpoint != null)
		{
			StringTokenizer tokenizer = new StringTokenizer(endpoint.getAddress());
			String operationToken = null;
			while (tokenizer.hasMoreTokens())
			{
				String token = tokenizer.nextToken();
				if (token != null && token.length() > 0)
				{
					operationToken = token;
				}
			}
			
			if (operationToken != null && operationToken.length() > 0)
			{
				QName operationName = new QName(operationToken);
				return service.getOperation(operationName);
			}
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.engine.AbstractDispatcher#findService(org.apache.axis2.context.MessageContext)
	 */
	@Override
	public AxisService findService(MessageContext messageContext) throws AxisFault
	{
		AxisConfiguration axisConfiguration = messageContext.getConfigurationContext().getAxisConfiguration();
		return axisConfiguration.getService(ConfigurationConstants.SPEP_WEBSERVICE_NAME);
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.engine.AbstractDispatcher#initDispatcher()
	 */
	@Override
	public void initDispatcher()
	{
		init(new HandlerDescription(SPEPDispatcherImpl.class.getSimpleName()));
	}
}

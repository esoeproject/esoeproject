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
 * Author: Andre Zitelli
 * Creation Date: 23/10/2006
 * 
 * Purpose: Evaluate the the authorization request data and extracts string values as required.
 * To separate any evaluation logic to avoid any namespace confusion between schema types. Types
 * used by this class are contained in the lxacml-context-schema.xsd and are used between SPEP and
 * PDP.
 * 
 */
package com.qut.middleware.esoe.authz.impl;

import java.util.Iterator;
import java.util.List;

import com.qut.middleware.esoe.authz.exception.InvalidRequestException;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.AttributeValue;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.Request;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.Subject;
import com.qut.middleware.saml2.schemas.esoe.lxacml.protocol.LXACMLAuthzDecisionQuery;

/** Evaluate the the authorization request data and extracts string values as required. Used
 * to separate any evaluation logic to avoid any namespace confusion between schema types. Types
 * used by this class are contained in the lxacml-context-schema.xsd and are used between SPEP and
 * PDP.*/
public class RequestEvaluator
{
		
	/** Get the requested resource from the given LXACMLAuthzDecisionQuery.
	 * 
	 * @param authzRequest The authz request to extract the resource string from.
	 * @return The resource that was located
	 * @throws InvalidRequestException
	 */
	public String getResource(LXACMLAuthzDecisionQuery authzRequest) throws InvalidRequestException
	{
		String value = new String();
		
		try
		{
			Request request = authzRequest.getRequest();
			List<Object> content = request.getResource().getAttribute().getAttributeValue().getContent();
		
			Iterator<Object> iter = content.iterator();
			while(iter.hasNext())
			{
				value += iter.next();
			}
		}
		catch(NullPointerException e)
		{
			throw new InvalidRequestException(Messages.getString("RequestEvaluator.0")); //$NON-NLS-1$
		}
		
		return value;
	}
	
	/** Get specified action from the given LXACMLAuthzDecisionQuery.
	 * 
	 * @param authzRequest The authz request to extract the resource string from.
	 * @return The action that was located, null if non specified (default from SPEPS)
	 * @throws InvalidRequestException
	 */
	public String getAction(LXACMLAuthzDecisionQuery authzRequest) throws InvalidRequestException
	{
		String value = new String();
		
		try
		{
			Request request = authzRequest.getRequest();
			if(request.getAction() != null)
			{
				List<Object> content = request.getAction().getAttribute().getAttributeValue().getContent();
			
				Iterator<Object> iter = content.iterator();
				while(iter.hasNext())
				{
					value += iter.next();
				}
			}
			else
			{
				return null;
			}
		}
		catch(NullPointerException e)
		{
			throw new InvalidRequestException(Messages.getString("RequestEvaluator.0")); //$NON-NLS-1$
		}
		
		return value;
	}
	
	
	/** Get the subjectID from the given LXACMLAuthzDecisionQuery.
	 * 
	 * @param authzRequest The authz request as suplied by an SPEP.
	 * @return The subject ID
	 * @throws InvalidRequestException
	 */
	public String getSubjectID(LXACMLAuthzDecisionQuery authzRequest) throws InvalidRequestException
	{
		String value = new String();
	
		try
		{
			Subject subject = authzRequest.getRequest().getSubject();
			AttributeValue attribute = subject.getAttribute().getAttributeValue();
			Iterator<Object> values = attribute.getContent().iterator();
			
			while(values.hasNext())
			{
				value += values.next().toString();
				
			}
		}
		catch(NullPointerException e)
		{
			throw new InvalidRequestException(Messages.getString("RequestEvaluator.1")); //$NON-NLS-1$
		}
		
		return value;
	}
	
	
	/** Get the entityID from the given LXACMLAuthzDecisionQuery.
	 * 
	 * @param authzRequest The authz request as suplied by an SPEP.
	 * @return The descriptor ID
	 * @throws InvalidRequestException
	 */
	public String getEntityID(LXACMLAuthzDecisionQuery authzRequest) throws InvalidRequestException
	{
		String value = null;
				
		try
		{
			value = authzRequest.getIssuer().getValue();
		}
		catch(NullPointerException e)
		{
			throw new InvalidRequestException(Messages.getString("RequestEvaluator.2")); //$NON-NLS-1$
		}
				
		return value;
	}
}

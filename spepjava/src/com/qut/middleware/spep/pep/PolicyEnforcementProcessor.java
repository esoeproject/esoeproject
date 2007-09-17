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
 * Author: Shaun Mangelsdorf
 * Creation Date: 09/11/2006
 * 
 * Purpose: Interface for the policy enforcement processor component of the SPEP.
 * 		Interprets policy decisions and carries out obligations imposed by the ESOE.
 */
package com.qut.middleware.spep.pep;

import java.text.MessageFormat;

import com.qut.middleware.saml2.exception.MarshallerException;
import com.qut.middleware.saml2.schemas.esoe.lxacml.EffectType;

/** Interface for the policy enforcement processor component of the SPEP.
 * 		Interprets policy decisions and carries out obligations imposed by the ESOE. */
public interface PolicyEnforcementProcessor 
{
	/** Possible outcomes from an authorization decision */
	public enum decision
	{
		/** The decision was to permit the request */
		permit(EffectType.PERMIT.value()),
		/** The decision was to deny the request */
		deny(EffectType.DENY.value()),
		/** Not enough cached data was available to make the decision */
		notcached("NotCached"), //$NON-NLS-1$
		/** An error occurred and the request could not be processed */
		error("Error"); //$NON-NLS-1$
		
		private String value;
		decision(String value)
		{
			this.value = value;
		}
		
		/**
		 * @return The String value of this decision.
		 */
		public String value()
		{
			return this.value;
		}
		
		/**
		 * @param v The value
		 * @return The decision corresponding to the value.
		 */
		public static decision fromValue(String v)
		{
			for (decision d : decision.values())
			{
				if (d.value.equals(v)) return d;
			}
			throw new IllegalArgumentException(MessageFormat.format(Messages.getString("PolicyEnforcementProcessor.0"), new Object[]{v})); //$NON-NLS-1$
		}
	};
	
	/**
	 * Makes an authorization decision.
	 * @param sessionID The session ID to evaluate the decision for.
	 * @param resource The resource being accessed
	 * @return The decision made by or on behalf of the PDP
	 */
	public decision makeAuthzDecision(String sessionID, String resource);
	
	/**
	 * Makes an authorization decision.
	 * @param sessionID The session ID to evaluate the decision for.
	 * @param resource The resource being accessed
	 * @param action The action being undertaken on the resource
	 * @return The decision made by or on behalf of the PDP
	 */
	public decision makeAuthzDecision(String sessionID, String resource, String action);
	
	/**
	 * Clears the authorization cache.
	 * @param requestDocument The request document.
	 * @return The response document.
	 * @throws MarshallerException 
	 */
	public byte[] authzCacheClear(byte[] requestDocument) throws MarshallerException;
}

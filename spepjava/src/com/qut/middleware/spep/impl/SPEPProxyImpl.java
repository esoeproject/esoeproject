/* 
 * Copyright 2007, Queensland University of Technology
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
 * Creation Date: 09/10/2007
 * 
 * Purpose: Implementation of SPEP proxy, calls to generated SPEP instance
 */
package com.qut.middleware.spep.impl;

import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.log4j.Logger;

import com.qut.middleware.spep.SPEP;
import com.qut.middleware.spep.SPEPProxy;
import com.qut.middleware.spep.pep.PolicyEnforcementProcessor;
import com.qut.middleware.spep.sessions.PrincipalSession;

public class SPEPProxyImpl implements SPEPProxy
{
	private SPEP spep;

	/* Local logging instance */
	private Logger logger = Logger.getLogger(SPEPProxyImpl.class.getName());
	
	public SPEPProxyImpl(SPEP spep) throws IllegalArgumentException
	{
		if(spep == null)
			throw new IllegalArgumentException("Passed instance of spep was null, instance MUST be valid");
		
		this.spep = spep;
	}

	public SPEP getSpep()
	{
		return spep;
	}

	public void setSpep(SPEP spep)
	{
		this.spep = spep;
	}

	public String getDefaultUrl()
	{
		return spep.getDefaultUrl();
	}

	public String getEsoeGlobalTokenName()
	{
		return spep.getEsoeGlobalTokenName();
	}

	public defaultAction getLazyInitDefaultAction()
	{
		SPEP.defaultAction action = spep.getLazyInitDefaultAction();

		switch (action)
		{
			case Permit:
				this.logger.info("Proxying lazy init action of permit to caller");
				return defaultAction.permit;
			case Deny:
				this.logger.info("Proxying lazy init action of deny to caller");
				return defaultAction.deny;
			default:
				this.logger.info("Proxying lazy init action of deny to caller after entering undetermined state");
				return defaultAction.deny;
		}
	}

	public String getServiceHost()
	{
		return spep.getServiceHost();
	}

	public String getSsoRedirect()
	{
		return spep.getSsoRedirect();
	}

	public String getTokenName()
	{
		return spep.getTokenName();
	}

	public boolean isLazyInit()
	{
		return spep.isLazyInit();
	}

	public boolean isStarted()
	{
		return spep.isStarted();
	}

	public decision makeAuthzDecision(String sessionID, String resource)
	{
		PolicyEnforcementProcessor.decision pepDecision = spep.getPolicyEnforcementProcessor().makeAuthzDecision(sessionID, resource);
		switch (pepDecision)
		{
			case permit:
				this.logger.info("Proxying PEP decision of permit to caller");
				return decision.permit;
			case deny:
				this.logger.debug("Proxying PEP decision of deny to caller");
				return decision.deny;
			case notcached:
				this.logger.debug("Proxying PEP decision of notcached to caller");
				return decision.notcached;
			case error:
				this.logger.debug("Proxying PEP decision of error to caller");
				return decision.error;
			default:
				this.logger.debug("Proxying PEP decision, undetermined state, returning deny to caller");
				return decision.deny;
		}
	}

	public decision makeAuthzDecision(String sessionID, String resource, String action)
	{
		PolicyEnforcementProcessor.decision pepDecision = spep.getPolicyEnforcementProcessor().makeAuthzDecision(sessionID, resource, action);
		switch (pepDecision)
		{
			case permit:
				this.logger.info("Proxying PEP action based decision of permit to caller");
				return decision.permit;
			case deny:
				this.logger.debug("Proxying PEP action based decision of deny to caller");
				return decision.deny;
			case notcached:
				this.logger.debug("Proxying PEP action based decision of notcached to caller");
				return decision.notcached;
			case error:
				this.logger.debug("Proxying PEP action based decision of error to caller");
				return decision.error;
			default:
				this.logger.debug("Proxying PEP action based decision, undetermined state, returning deny to caller");
				return decision.deny;
		}
	}

	public PrincipalSession verifySession(String sessionID)
	{
		return spep.getAuthnProcessor().verifySession(sessionID);
	}

	public List<String> getLazyInitResources()
	{
		return spep.getLazyInitResources();
	}

	public List<Cookie> getLogoutClearCookies()
	{
		return spep.getLogoutClearCookies();
	}

}

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
package com.qut.middleware.esoemanager.authz;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.qut.middleware.esoemanager.client.rpc.EsoeManagerServiceException;
import com.qut.middleware.esoemanager.client.rpc.bean.SimpleServiceListing;
import com.qut.middleware.spep.SPEPProxy;
import com.qut.middleware.spep.filter.Initializer;
import com.qut.middleware.spep.filter.SPEPFilter;
import com.qut.middleware.spep.filter.exception.SPEPInitializationException;
import com.qut.middleware.spep.sessions.PrincipalSession;

@Aspect
public class SecurityManager extends RequestContextHolder implements ServletContextAware
{
	private ServletContext sc;
	private String spepContextName;

	private final String esoeManagementSuperUser = "esoe:management:privilege:superuser";
	private final String esoeManagementServiceManager = "esoe:management:privilege:serviceid:";

	private Logger logger = LoggerFactory.getLogger(SecurityManager.class);

	public void init() throws Exception
	{
	}
	
	@Around("com.qut.middleware.esoemanager.authz.SystemArchitecture.determineSuperUser()")
	public Boolean doSuperUserAccessCheck(ProceedingJoinPoint pjp)
	{	
		// Let the function execute but we don't care about what it thinks :)
		try
		{
			pjp.proceed();
		}
		catch (Throwable e)
		{
			return false;
		}
		
		SPEPProxy spep = this.establishSPEPProxy();
		
		if(!isSuperUser(spep))
		{
			return false;
		}
		
		return true;
	}

	@AfterReturning(pointcut = "com.qut.middleware.esoemanager.authz.SystemArchitecture.verifyListing()", returning = "retVal")
	public void doAccessCheck(Object retVal)
	{
		SPEPProxy spep = this.establishSPEPProxy();

		if (!isSuperUser(spep))
		{
			List<SimpleServiceListing> services = (List<SimpleServiceListing>) retVal;

			List<SimpleServiceListing> unAuthorizedServices = new ArrayList<SimpleServiceListing>();
			for (SimpleServiceListing service : services)
			{
				this.logger.debug("Evaluating permissions for serviceID " + service.getIdentifier());

				RequestAttributes attributes = this.getRequestAttributes();
				String sessionID = (String) attributes.getAttribute(SPEPFilter.SPEP_SESSIONID,
						RequestAttributes.SCOPE_SESSION);

				// checks policies with resources/rules matching
				// management:privilege:serviceid:X
				SPEPProxy.decision decision = this.determineAccessRights(spep, sessionID,
						this.esoeManagementServiceManager + service.getIdentifier());

				if (decision != SPEPProxy.decision.permit)
				{
					// Not authorized to schedule to remove it
					this.logger.debug("serviceID " + service.getIdentifier() + " removed from response");
					unAuthorizedServices.add(service);
				}

			}

			// Dump all the unauthorized services;
			for (SimpleServiceListing service : unAuthorizedServices)
			{
				services.remove(service);
			}
		}
	}

	@Before("com.qut.middleware.esoemanager.authz.SystemArchitecture.verifyServiceAccess() && args(serviceID,..)")
	public void validateAccount(String serviceID) throws EsoeManagerServiceException
	{
		this.logger.debug("Validating access for serviceID: " + serviceID);

		SPEPProxy spep = this.establishSPEPProxy();

		if (!isSuperUser(spep))
		{
			RequestAttributes attributes = this.getRequestAttributes();
			String sessionID = (String) attributes.getAttribute(SPEPFilter.SPEP_SESSIONID,
					RequestAttributes.SCOPE_SESSION);

			// checks policies with resources/rules matching
			// management:privilege:serviceid:X
			SPEPProxy.decision decision = this.determineAccessRights(spep, sessionID, this.esoeManagementServiceManager
					+ serviceID);

			if (decision != SPEPProxy.decision.permit)
			{
				throw new EsoeManagerServiceException("Access control policies prevent this action from occuring");
			}
		}
	}
	
	private boolean isSuperUser(SPEPProxy spep)
	{
		RequestAttributes attributes = this.getRequestAttributes();
		String sessionID = (String) attributes.getAttribute(SPEPFilter.SPEP_SESSIONID,
				RequestAttributes.SCOPE_SESSION);

		// checks policies with resources/rules matching
		// management:privilege:serviceid:X
		SPEPProxy.decision decision = this.determineAccessRights(spep, sessionID, this.esoeManagementSuperUser);

		if (decision != SPEPProxy.decision.permit)
		{
			return false;
		}
		
		return true;	
	}

	private SPEPProxy.decision determineAccessRights(SPEPProxy spep, String sessionID, String resource)
	{
		SPEPProxy.decision authzDecision = spep.makeAuthzDecision(sessionID, resource);

		// the authz processor may destroy the session if the PDP determines
		// that the client
		// session is no longer valid, so we have to check it again
		PrincipalSession principalSession;
		boolean validSession;
		if ((principalSession = spep.verifySession(sessionID)) != null)
			validSession = true;
		else
			validSession = false;

		if(validSession)
			return authzDecision;
		
		return SPEPProxy.decision.deny;
	}

	private SPEPProxy establishSPEPProxy() throws SecurityException
	{
		ServletContext spepContext = this.sc.getContext(this.spepContextName);

		// Establish SPEPProxy object.
		SPEPProxy spep;
		try
		{
			spep = Initializer.init(spepContext);
		}
		catch (SPEPInitializationException e)
		{
			this.logger.error(e.getLocalizedMessage());
			throw new SecurityException(e.getLocalizedMessage(), e);
		}

		// Ensure SPEP startup.
		if (!spep.isStarted())
		{
			// Don't allow anything to occur if SPEP hasn't started correctly.
			this.logger.error("Unable to process request to acces resource, SPEP is not initialized correcty");
			throw new SecurityException("Unable to process request to acces resource, SPEP is not initialized correcty");
		}

		return spep;
	}

	public void setSpepContextName(String spepContextName)
	{
		this.spepContextName = spepContextName;
	}

	public void setServletContext(ServletContext sc)
	{
		this.sc = sc;
	}

}

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
 * Purpose: Implements the SPEP interface.
 */
package com.qut.middleware.spep.impl;

import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.log4j.Logger;

import com.qut.middleware.spep.SPEP;
import com.qut.middleware.spep.StartupProcessor;
import com.qut.middleware.spep.StartupProcessor.result;
import com.qut.middleware.spep.attribute.AttributeProcessor;
import com.qut.middleware.spep.authn.AuthnProcessor;
import com.qut.middleware.spep.metadata.Metadata;
import com.qut.middleware.spep.pep.PolicyEnforcementProcessor;
import com.qut.middleware.spep.pep.SessionGroupCache;

/** Implements the SPEP interface. */
public class SPEPImpl implements SPEP
{
	private String tokenName;
	private String serviceHost;
	private String ssoRedirect;
	private String defaultURL;
	private AttributeProcessor attributeProcessor;
	private AuthnProcessor authnProcessor;
	private Metadata metadata;
	private PolicyEnforcementProcessor policyEnforcementProcessor;
	private SessionGroupCache sessionGroupCache;
	private boolean started;
	private StartupProcessor startupProcessor;
	private boolean lazyInit;
	private List<String> hardInitQueries;
	private String esoeGlobalTokenName;
	private defaultAction lazyInitDefaultAction;
	
	/* Local logging instance */
	private Logger logger = Logger.getLogger(SPEPImpl.class.getName());
	private List<Cookie> logoutClearCookies;

	
	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.spep.SPEP#getAttributeProcessor()
	 */
	public AttributeProcessor getAttributeProcessor()
	{
		return this.attributeProcessor;
	}
	
	/**
	 * @param attributeProcessor The attributeProcessor to set.
	 */
	public void setAttributeProcessor(AttributeProcessor attributeProcessor)
	{
		this.attributeProcessor = attributeProcessor;
	}
	
	public AuthnProcessor getAuthnProcessor()
	{
		return this.authnProcessor;
	}
	
	/**
	 * @param authnProcessor The authnProcessor to set.
	 */
	public void setAuthnProcessor(AuthnProcessor authnProcessor)
	{
		this.authnProcessor = authnProcessor;
	}
	
	public Metadata getMetadata()
	{
		return this.metadata;
	}
	
	/**
	 * @param metadata The metadata to set.
	 */
	public void setMetadata(Metadata metadata)
	{
		this.metadata = metadata;
	}
	
	public PolicyEnforcementProcessor getPolicyEnforcementProcessor()
	{
		return this.policyEnforcementProcessor;
	}
	
	/**
	 * @param policyEnforcementProcessor The policyEnforcementProcessor to set.
	 */
	public void setPolicyEnforcementProcessor(PolicyEnforcementProcessor policyEnforcementProcessor)
	{
		this.policyEnforcementProcessor = policyEnforcementProcessor;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.spep.SPEP#getTokenName()
	 */
	public String getTokenName()
	{
		return this.tokenName;
	}
	
	/** Set the name of the authentication to be used by the system.
	 * 
	 * @param tokenName The tokenName to set.
	 */
	public void setTokenName(String tokenName)
	{
		this.tokenName = tokenName;
	}	

	/* (non-Javadoc)
	 * @see com.qut.middleware.spep.SPEP#getServiceURL()
	 */
	public String getServiceHost()
	{
		return this.serviceHost;
	}
	
	/**
	 * Sets address of the service in use without any application path e.g. https://myserver.company.com or https://myotherserver.company.com:8443 NOT https://myserver.company.com/myapp/
	 * @param serviceHost serviceHost in required format
	 */
	public void setServiceHost(String serviceHost)
	{
		this.serviceHost = serviceHost;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.spep.SPEP#getDefaultUrl()
	 */
	public String getDefaultUrl()
	{
		return this.defaultURL;
	}
	
	/**
	 * @param defaultUrl The defaultUrl to set.
	 */
	public void setDefaultURL(String defaultUrl)
	{
		this.defaultURL = defaultUrl;
	}

	public SessionGroupCache getSessionGroupCache()
	{
		return this.sessionGroupCache;
	}
	
	/**
	 * @return The startupProcessor
	 */
	public StartupProcessor getStartupProcessor()
	{
		return this.startupProcessor;
	}
	
	/**
	 * @param startupProcessor The startupProcessor to set.
	 */
	public void setStartupProcessor(StartupProcessor startupProcessor)
	{
		this.startupProcessor = startupProcessor;
	}
	
	/**
	 * @param sessionGroupCache The sessionGroupCache to set.
	 */
	public void setSessionGroupCache(SessionGroupCache sessionGroupCache)
	{
		this.sessionGroupCache = sessionGroupCache;
	}

	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.spep.SPEP#isStarted()
	 */
	public boolean isStarted()
	{
		if (this.started) return true;
		
		updateStarted();
		
		return this.started;
	}
	
	public List<Cookie> getLogoutClearCookies()
	{
		return this.logoutClearCookies;
	}
	
	/**
	 * @param logoutClearCookies The list of cookies to be cleared upon logout.
	 */
	public void setLogoutClearCookies(List<Cookie> logoutClearCookies)
	{
		this.logoutClearCookies = logoutClearCookies;
	}

	/*
	 * 
	 */
	private synchronized void updateStarted()
	{
		result startupResult = null;
		
		// while(true) is bad practise, so put something more useful.
		boolean error = true;
		while(error)
		{
			startupResult = this.startupProcessor.allowProcessing();
			
			error = false;
			if (startupResult.equals(result.allow))
			{
				this.started = true;
				return;
			}
			else if (startupResult.equals(result.fail))
			{
				this.started = false;
				return;
			}
			else
			{
				error = true;
				
				// FIXME Don't hard code this.
				try
				{
					Thread.sleep(200);
				}
				catch (InterruptedException e)
				{
					// Ignore interruption.
				}
			}
		}
	}

	public String getSsoRedirect()
	{
		return ssoRedirect;
	}

	public void setSsoRedirect(String ssoRedirect)
	{
		this.ssoRedirect = ssoRedirect;
	}

	public boolean isLazyInit()
	{
		return lazyInit;
	}

	/**
	 * Determines if this spep is operating in lazy mode or not
	 * @param lazyInit Boolean state for lazyInit
	 */
	public void setLazyInit(boolean lazyInit)
	{
		this.lazyInit = lazyInit;
	}

	public List<String> getLazyInitResources()
	{
		return hardInitQueries;
	}

	public void setLazyInitResources(List<String> hardInitQueries)
	{
		this.hardInitQueries = hardInitQueries;
	}

	public String getEsoeGlobalTokenName()
	{
		return esoeGlobalTokenName;
	}

	public void setEsoeGlobalTokenName(String esoeGlobalTokenName)
	{
		this.esoeGlobalTokenName = esoeGlobalTokenName;
	}

	public defaultAction getLazyInitDefaultAction()
	{
		return lazyInitDefaultAction;
	}

	public void setLazyInitDefaultAction(defaultAction lazyInitDefaultAction)
	{
		this.lazyInitDefaultAction = lazyInitDefaultAction;
	}
}

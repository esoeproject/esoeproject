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
 * Creation Date: 13/12/2006
 * 
 * Purpose: Implements the SessionGroupCache
 */
package com.qut.middleware.spep.pep.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.spep.ConfigurationConstants;
import com.qut.middleware.spep.pep.Messages;
import com.qut.middleware.spep.pep.SessionGroupCache;
import com.qut.middleware.spep.pep.PolicyEnforcementProcessor.decision;
import com.qut.middleware.spep.sessions.PrincipalSession;

/** Implements the SessionGroupCache. */
public class SessionGroupCacheImpl implements SessionGroupCache
{
	private boolean initialized;
	private Map<PrincipalSession, GroupCache> groupCaches;
	private Map<String, List<String>> groupTargets;
	private decision defaultPolicyDecision;

	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	private Logger authzLogger = LoggerFactory.getLogger(ConfigurationConstants.authzLogger);

	/**
	 * Default constructor
	 * 
	 * @param defaultPolicyDecision
	 *            The default policy decision
	 */
	public SessionGroupCacheImpl(decision defaultPolicyDecision)
	{
		if (defaultPolicyDecision == null)
		{
			throw new IllegalArgumentException(Messages.getString("SessionGroupCacheImpl.4")); //$NON-NLS-1$
		}

		this.groupCaches = new HashMap<PrincipalSession, GroupCache>();
		this.initialized = false;

		if (decision.permit.equals(defaultPolicyDecision) || decision.deny.equals(defaultPolicyDecision))
		{
			this.defaultPolicyDecision = defaultPolicyDecision;
		}
		else
		{
			throw new IllegalArgumentException(Messages.getString("SessionGroupCacheImpl.0")); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.spep.pep.SessionGroupCache#makeCachedAuthzDecision(com.qut.middleware.spep.sessions.PrincipalSession,
	 *      java.lang.String)
	 */
	public decision makeCachedAuthzDecision(PrincipalSession principalSession, String resource)
	{
		return makeCachedAuthzDecision(principalSession, resource, null);
	}

	public decision makeCachedAuthzDecision(PrincipalSession principalSession, String resource, String action)
	{
		if (!this.initialized)
			throw new IllegalArgumentException(Messages.getString("SessionGroupCacheImpl.1")); //$NON-NLS-1$

		// Look up group cache for this session.
		GroupCache groupCache;

		// if no Grouptargets cached, don't bother with Principal processing. This will ensure that
		// we don't send an Authz Request for an empty PolicySet
		synchronized (this.groupTargets)
		{
			if (this.groupTargets.size() == 0)
			{
				this.logger.warn(MessageFormat.format(Messages.getString("SessionGroupCacheImpl.5"), this.defaultPolicyDecision)); //$NON-NLS-1$
				return this.defaultPolicyDecision;
			}
		}

		synchronized (this.groupCaches)
		{
			groupCache = this.groupCaches.get(principalSession);
		}

		if (groupCache == null)
		{
			return decision.notcached;
		}

		decision result;
		synchronized (groupCache)
		{
			result = groupCache.makeCachedAuthzDecision(resource, action);
		}

		if (result == null)
		{
			this.authzLogger.info(MessageFormat.format("DEFAULT access for Session [{0}] to Resource {1} (cached decision)", principalSession.getEsoeSessionID(), resource));
			return this.defaultPolicyDecision;
		}
		else
			if(result == decision.permit)
				this.authzLogger.info(MessageFormat.format("PERMIT access for Session [{0}] to Resource {1} (cached decision)", principalSession.getEsoeSessionID(), resource));
			else
				if(result == decision.deny)
					this.authzLogger.info(MessageFormat.format("DENY access for Session [{0}] to Resource {1} (cached decision)", principalSession.getEsoeSessionID(), resource));
				else
					this.authzLogger.info(MessageFormat.format("ERROR providing access for Session [{0}] to Resource {1} (cached decision)", principalSession.getEsoeSessionID(), resource));

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.spep.pep.SessionGroupCache#clearCache(java.util.Map)
	 */
	public void clearCache(Map<String, List<String>> groupTargetMap)
	{
		synchronized (this.groupCaches)
		{
			this.groupCaches.clear();
			this.groupTargets = groupTargetMap;
			this.initialized = true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.spep.pep.SessionGroupCache#clearPrincipalSession(java.lang.String)
	 */
	public void clearPrincipalSession(PrincipalSession principal)
	{
		synchronized (this.groupCaches)
		{
			this.groupCaches.remove(principal);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.spep.pep.SessionGroupCache#updateCache(com.qut.middleware.spep.sessions.PrincipalSession,
	 *      java.lang.String, java.util.List, java.lang.String,
	 *      com.qut.middleware.spep.pep.PolicyEnforcementProcessor.decision)
	 */
	public void updateCache(PrincipalSession principalSession, String groupTarget, List<String> authzTargets, String action, decision decision)
	{
		if (!this.initialized)
			throw new IllegalStateException(Messages.getString("SessionGroupCacheImpl.2")); //$NON-NLS-1$

		// Look up group cache for this session.
		GroupCache groupCache;
		synchronized (this.groupCaches)
		{
			groupCache = this.groupCaches.get(principalSession);
			if (groupCache == null)
			{
				groupCache = createDefaultGroupCache();
				this.groupCaches.put(principalSession, groupCache);
			}
		}

		synchronized (groupCache)
		{
			PDPDecision pdpDecision = new PDPDecision();
			pdpDecision.nodeDecision = decision;
			pdpDecision.action = action;
			groupCache.updateCache(groupTarget, authzTargets, pdpDecision);
		}
	}

	private class GroupCache
	{
		private Map<String, AuthzTargetCache> authzTargetMap;

		protected GroupCache()
		{
			// LinkedHashMap used because it is more efficient at keySet()
			this.authzTargetMap = new LinkedHashMap<String, AuthzTargetCache>();
		}

		protected decision makeCachedAuthzDecision(String resource, String action)
		{
			decision result = null;

			// Loop through matching group targets
			for (String key : this.authzTargetMap.keySet())
			{
				if (targetMatch(key, resource))
				{
					AuthzTargetCache authzTargetCache = this.authzTargetMap.get(key);

					if (authzTargetCache == null)
					{
						result = decision.notcached;
					}
					else
					{
						// Call the AuthzTargetCache to get a cached decision. If null we need to update the cache.
						decision nodeDecision = authzTargetCache.makeCachedAuthzDecision(resource, action);
						/*
						 * if (nodeDecision == null) { nodeDecision = decision.cache; }
						 */

						result = addDecisions(result, nodeDecision);

						if (decision.deny.equals(result))
						{
							return result;
						}
					}
				}
			}

			return result;
		}

		protected void updateCache(String groupTarget, List<String> authzTargets, PDPDecision decision)
		{
			// Call the AuthzTargetCache object to update its cache
			AuthzTargetCache authzTargetCache = this.authzTargetMap.get(groupTarget);

			if (authzTargetCache == null)
			{
				authzTargetCache = new AuthzTargetCache();
				this.authzTargetMap.put(groupTarget, authzTargetCache);
			}

			authzTargetCache.updateCache(authzTargets, decision);
		}
	}

	private class PDPDecision
	{
		protected decision nodeDecision = null;
		protected String action = null;
	}

	private class AuthzTargetCache
	{
		private Map<String, List<PDPDecision>> decisionMap;

		protected AuthzTargetCache()
		{
			// LinkedHashMap used because it is more efficient at keySet()
			this.decisionMap = new LinkedHashMap<String, List<PDPDecision>>();
		}

		protected decision makeCachedAuthzDecision(String resource, String action)
		{
			decision result = null;

			// Loop through all matching targets
			for (String key : this.decisionMap.keySet())
			{
				if (targetMatch(key, resource))
				{
					if (this.decisionMap.get(key).size() == 0)
					{
						result = addDecisions(result, decision.notcached);
					}
					else
					{
						for (PDPDecision pdpDecision : this.decisionMap.get(key))
						{
							// Find the cached decision. If null we need to update cache.
							decision nodeDecision = pdpDecision.nodeDecision;
							if (nodeDecision != null)
							{
								if (actionMatch(pdpDecision.action, action))
								{
									// Add the decision to the result
									result = addDecisions(result, nodeDecision);

									if (decision.deny.equals(result))
									{
										return result;
									}
								}
							}
							else
							{
								result = addDecisions(result, decision.notcached);
							}
						}
					}
				}
			}

			return result;
		}

		protected void updateCache(List<String> authzTargets, PDPDecision decision)
		{
			for (String authzTarget : authzTargets)
			{
				List<PDPDecision> pdpDecisionList;

				pdpDecisionList = this.decisionMap.get(authzTarget);
				if (pdpDecisionList == null)
					pdpDecisionList = new ArrayList<PDPDecision>();

				if(decision != null)
					pdpDecisionList.add(decision);

				this.decisionMap.put(authzTarget, pdpDecisionList);
			}
		}
	}

	protected GroupCache createDefaultGroupCache()
	{
		GroupCache defaultGroupCache = new GroupCache();
		PDPDecision pdpDecision = null;

		for (Entry<String, List<String>> groupTargetEntry : this.groupTargets.entrySet())
		{
			String groupTarget = groupTargetEntry.getKey();
			List<String> authzTargets = groupTargetEntry.getValue();
			defaultGroupCache.updateCache(groupTarget, authzTargets, pdpDecision);
		}

		return defaultGroupCache;
	}
	
	protected boolean actionMatch(String target, String action)
	{
		if (target == null && action == null)
			return true;
		
		if (target == null || action == null)
			return false;

		if (target.equals(action))
			return true;

		Pattern pattern = Pattern.compile(target);
		return pattern.matcher(action).matches();
	}

	protected boolean targetMatch(String target, String resource)
	{
		if (target == null || resource == null)
			return true;

		if (target.equals(resource))
			return true;

		Pattern pattern = Pattern.compile(target);
		return pattern.matcher(resource).matches();
	}

	protected decision addDecisions(decision lhs, decision rhs)
	{
		// Handle nulls first so we don't get a null pointer.
		if (lhs == null)
			return rhs;
		if (rhs == null)
			return lhs;

		switch (lhs)
		{
			case permit:
				/*
				 * Since the following are true we can just return rhs: permit + permit = permit permit + deny = deny
				 * permit + cache = cache permit + error = error
				 */
				return rhs;

			case deny:
				/*
				 * deny + permit = deny deny + deny = deny deny + cache = deny deny + error = error
				 */
				if (decision.error.equals(rhs))
					return decision.error;
				return decision.deny;

			case notcached:
				/*
				 * cache + permit = cache cache + deny = deny cache + cache = cache cache + error = error
				 */
				if (decision.permit.equals(rhs))
					return decision.notcached;
				return rhs;

			case error:
				/*
				 * error + permit = error error + deny = error error + cache = error error + error = error
				 */
				return decision.error;
		}

		throw new IllegalArgumentException(Messages.getString("SessionGroupCacheImpl.3")); //$NON-NLS-1$
	}
}

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
 * Creation Date: 06/10/2006
 * 
 * Purpose: Thread implementation of the PolicyCacheProcessor interface.
 */
package com.qut.middleware.esoe.pdp.cache.impl;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.PrivateKey;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.w3._2000._09.xmldsig_.Signature;
import org.w3c.dom.Element;

import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.MonitorThread;
import com.qut.middleware.esoe.crypto.KeyStoreResolver;
import com.qut.middleware.esoe.metadata.Metadata;
import com.qut.middleware.esoe.metadata.exception.InvalidMetadataEndpointException;
import com.qut.middleware.esoe.pdp.cache.AuthzCacheUpdateFailureRepository;
import com.qut.middleware.esoe.pdp.cache.PolicyCacheProcessor;
import com.qut.middleware.esoe.pdp.cache.bean.AuthzPolicyCache;
import com.qut.middleware.esoe.pdp.cache.bean.FailedAuthzCacheUpdate;
import com.qut.middleware.esoe.pdp.cache.bean.impl.FailedAuthzCacheUpdateImpl;
import com.qut.middleware.esoe.pdp.cache.sqlmap.PolicyCacheDao;
import com.qut.middleware.esoe.pdp.cache.sqlmap.impl.PolicyCacheData;
import com.qut.middleware.esoe.pdp.cache.sqlmap.impl.PolicyCacheQueryData;
import com.qut.middleware.esoe.pdp.impl.PolicyEvaluator;
import com.qut.middleware.esoe.util.CalendarUtils;
import com.qut.middleware.esoe.ws.WSClient;
import com.qut.middleware.esoe.ws.exception.WSClientException;
import com.qut.middleware.saml2.StatusCodeConstants;
import com.qut.middleware.saml2.VersionConstants;
import com.qut.middleware.saml2.exception.InvalidSAMLResponseException;
import com.qut.middleware.saml2.exception.MarshallerException;
import com.qut.middleware.saml2.exception.ReferenceValueException;
import com.qut.middleware.saml2.exception.SignatureValueException;
import com.qut.middleware.saml2.exception.UnmarshallerException;
import com.qut.middleware.saml2.handler.Marshaller;
import com.qut.middleware.saml2.handler.Unmarshaller;
import com.qut.middleware.saml2.handler.impl.MarshallerImpl;
import com.qut.middleware.saml2.handler.impl.UnmarshallerImpl;
import com.qut.middleware.saml2.identifier.IdentifierGenerator;
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Policy;
import com.qut.middleware.saml2.schemas.esoe.lxacml.grouptarget.GroupTarget;
import com.qut.middleware.saml2.schemas.esoe.protocol.ClearAuthzCacheRequest;
import com.qut.middleware.saml2.schemas.esoe.protocol.ClearAuthzCacheResponse;
import com.qut.middleware.saml2.schemas.protocol.Extensions;
import com.qut.middleware.saml2.validator.SAMLValidator;


public class PolicyCacheProcessorImpl extends Thread implements PolicyCacheProcessor, MonitorThread
{
	// the cache is a map of policy ID -> PolicyType objects
	private AuthzPolicyCache globalCache;
	private AuthzCacheUpdateFailureRepository failureRep;
	
	private boolean running;
	private boolean cacheInitialized;
	
	private Metadata metadata;
	private int pollInterval;
	private PrivateKey key;
	private String keyName;
	private IdentifierGenerator identifierGenerator;
	
	private String[] schemas = new String[] { ConfigurationConstants.esoeProtocol, ConfigurationConstants.samlProtocol };

	private final PolicyCacheDao sqlConfig;

	private Marshaller<ClearAuthzCacheRequest> clearAuthzCacheRequestMarshaller;
	private Unmarshaller<ClearAuthzCacheResponse> clearAuthzCacheResponseUnmarshaller;
	private Unmarshaller<Policy> policyUnmarshaller;
	private Marshaller<GroupTarget> groupTargetMarshaller;
	private SAMLValidator samlValidator;
	private WSClient wsClient;
	
	private final String UNMAR_PKGNAMES = ClearAuthzCacheResponse.class.getPackage().getName();
	private final String MAR_PKGNAMES = ClearAuthzCacheRequest.class.getPackage().getName() + ":" + GroupTarget.class.getPackage().getName(); //$NON-NLS-1$
   	private final String UNMAR_PKGNAMES2 = Policy.class.getPackage().getName();
	private final String MAR_PKGNAMES2 = GroupTarget.class.getPackage().getName();
	
	/* Local logging instance */
	private Logger logger = Logger.getLogger(PolicyCacheProcessorImpl.class.getName());

	/**
	 * Constructor which takes a spring injected data source as a parameter. Spring will inject a singleton instance of the
	 * failure monitor for us to manipulate. This constructor will need to have the global cache object injected by
	 * spring.
	 * 
	 * @param failureRep
	 *            The cache failure repository
	 * @param cache
	 *            The authz cache used to update.
	 * @param metadata
	 *            The metadata processor instance
	 * @param sqlConfig
	 *            The DAO for accessing policies.
	 * @param wsClient
	 *            The WEB service client used to send requests to SPEPs. param keyStoreResolver The keystore resolver
	 *            used to obtain the ESOE private key and SPEP public keys
	 * @param identifierGenerator
	 *            A SAML2lib-j identifier generator instance
	 * @param samlValidator
	 *            SAML document validator instance
	 * @param pollInterval
	 *            The duration between database (to check for policy changes) polls in seconds.
	 * 
	 * @throws Exception
	 *             If initialisation fails or invalid parameters are supplied to this contructor.
	 * 
	 */
	public PolicyCacheProcessorImpl(AuthzCacheUpdateFailureRepository failureRep, AuthzPolicyCache cache,
			Metadata metadata, PolicyCacheDao sqlConfig, WSClient wsClient, KeyStoreResolver keyStoreResolver,
			IdentifierGenerator identifierGenerator, SAMLValidator samlValidator, int pollInterval) throws MarshallerException, UnmarshallerException
	{
		/* Ensure that a stable base is created when this Processor is setup */
		if (failureRep == null)
			throw new IllegalArgumentException(Messages.getString("PolicyCacheProcessorImpl.0")); //$NON-NLS-1$

		if (cache == null)
			throw new IllegalArgumentException(Messages.getString("PolicyCacheProcessorImpl.1")); //$NON-NLS-1$

		if (metadata == null)
			throw new IllegalArgumentException(Messages.getString("PolicyCacheProcessorImpl.2")); //$NON-NLS-1$

		if (keyStoreResolver == null)
			throw new IllegalArgumentException(Messages.getString("PolicyCacheProcessorImpl.7")); //$NON-NLS-1$

		if (sqlConfig == null)
			throw new IllegalArgumentException(Messages.getString("PolicyCacheProcessorImpl.5")); //$NON-NLS-1$

		if (wsClient == null)
			throw new IllegalArgumentException(Messages.getString("PolicyCacheProcessorImpl.8")); //$NON-NLS-1$

		if (identifierGenerator == null)
			throw new IllegalArgumentException(Messages.getString("PolicyCacheProcessorImpl.31"));  //$NON-NLS-1$

		if (samlValidator == null)
			throw new IllegalArgumentException(Messages.getString("PolicyCacheProcessorImpl.32"));  //$NON-NLS-1$

		if(pollInterval <= 0 || (pollInterval > Integer.MAX_VALUE / 1000) )
			throw new IllegalArgumentException(Messages.getString("PolicyCacheProcessorImpl.9")); //$NON-NLS-1$
			
		this.failureRep = failureRep;
		this.globalCache = cache;
		this.metadata = metadata;
		this.key = keyStoreResolver.getPrivateKey();
		this.keyName = keyStoreResolver.getKeyAlias();
		this.sqlConfig = sqlConfig;
		this.wsClient = wsClient;
		this.pollInterval = pollInterval * 1000;
		this.identifierGenerator = identifierGenerator;
		this.samlValidator = samlValidator;
		this.cacheInitialized = false;
		
		this.clearAuthzCacheRequestMarshaller = new MarshallerImpl<ClearAuthzCacheRequest>(this.MAR_PKGNAMES, this.schemas, this.keyName, this.key);
		this.clearAuthzCacheResponseUnmarshaller = new UnmarshallerImpl<ClearAuthzCacheResponse>(this.UNMAR_PKGNAMES, this.schemas, this.metadata);
		this.policyUnmarshaller = new UnmarshallerImpl<Policy>(this.UNMAR_PKGNAMES2, new String[]{ConfigurationConstants.lxacml});
		this.groupTargetMarshaller = new MarshallerImpl<GroupTarget>(this.MAR_PKGNAMES2, this.schemas);
		
		this.logger.info(MessageFormat.format(Messages.getString("PolicyCacheProcessorImpl.10"), (this.pollInterval/1000)) ); //$NON-NLS-1$

		this.setName(Messages.getString("PolicyCacheProcessorImpl.11")); //$NON-NLS-1$
		this.setDaemon(false);
		this.start();
		
	}

	/**
	 * @see com.qut.middleware.esoe.pdp.cache.PolicyCacheProcessor#spepStartingNotification(String, int)
	 */
	public result spepStartingNotification(String entityID, int authzCacheIndex)
	{		
		String endpoint = null;
		
		try
		{
			Map<Integer,String> cacheClearServiceList = this.metadata.resolveCacheClearService(entityID);
			if (cacheClearServiceList == null)
			{
				this.logger.error(Messages.getString("PolicyCacheProcessorImpl.33") + entityID); //$NON-NLS-1$
				return result.Failure;
			}
			
			endpoint = cacheClearServiceList.get(authzCacheIndex);

			// Get associated policies and create AuthzClearCache request
			byte[] authzClearCacheRequest = this.generateClearCacheRequest(entityID, endpoint, Messages
					.getString("PolicyCacheProcessorImpl.3")); //$NON-NLS-1$

			// If the returned request string is null, then a problem occurred retrieving policy data.
			// Don't send request.
			if(authzClearCacheRequest == null)
			{
				this.logger.warn(Messages.getString("PolicyCacheProcessorImpl.12") + entityID + Messages.getString("PolicyCacheProcessorImpl.13")); //$NON-NLS-1$ //$NON-NLS-2$
				return result.Failure;
			}
						
			result updateResult = this.sendCacheUpdateRequest(authzClearCacheRequest, endpoint);
			
			if (!updateResult.equals(result.Success))
			{
				this.logger.warn(Messages.getString("PolicyCacheProcessorImpl.14") + endpoint); //$NON-NLS-1$
				return result.Failure;
			}
		}
		catch (InvalidMetadataEndpointException e)
		{
			this.logger.error(Messages.getString("PolicyCacheProcessorImpl.15") + endpoint ); //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);
			
			return result.Failure;
		}
		catch(MarshallerException e)
		{
			this.logger.error(Messages.getString("PolicyCacheProcessorImpl.16") + endpoint ); //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);
			
			return result.Failure;
		}

		return result.Success;
	}

	/* Perform a full rebuild of the policy cache.
	 * 
	 */
	private void init() throws SQLException
	{
		// Retrieve the higher sequence number from policies state and compare to our last sequence id
		long latestSequenceId = this.sqlConfig.queryLastSequenceId();
		
		// Reset the policy cache build number to force a rebuild
		this.globalCache.setBuildSequenceId(AuthzPolicyCache.SEQUENCE_UNINITIALIZED);
		
		if(latestSequenceId <= 0)
			throw new SQLException("Cache init failed to retrieve any valid sequence id's from policies data store.");
		
		this.buildCache(latestSequenceId, true);
	}

	/*
	 * Thread implementation run code. Essentially: initialize processor, run forever polling for changes at regular
	 * intervals, rebuilding the cache if any policies have changed.
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run()
	{			
		this.setRunning(true);
		
		try
		{
			init();
			
			this.cacheInitialized = true;
		}
		catch (SQLException e)
		{
			this.logger.fatal(Messages.getString("PolicyCacheProcessorImpl.17")); //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);
		}
		
		while (this.isRunning())
		{
			try
			{
				sleep(this.pollInterval);
				
				this.poll();
			}
			catch(SQLException e)
			{
				this.logger.debug(e.getLocalizedMessage(), e);
			}
			catch(InterruptedException e)
			{
				if(!this.isRunning())
					break;
			}
			// ignore interrupts and other runtime exceptions
			catch (Exception e)
			{
				this.logger.debug(e.getLocalizedMessage(), e);
			}
		}
		
		this.logger.info(this.getName() + Messages.getString("PolicyCacheProcessorImpl.34")); //$NON-NLS-1$
		
		return;
	}

	/**
	 * Poll the data source containing authorization policies to see if there has been a change. If a policy contained in
	 * the associated data source has changed, this method will initiate a cache update - this.updateCache()
	 * 
	 * @throws SQLException
	 */
	private void poll() throws SQLException
	{
		// Retrieve the higher sequence number from policies state and compare to our last sequence id
		long latestSequenceId = this.sqlConfig.queryLastSequenceId();
		
		this.logger.debug(MessageFormat.format("Checking current cache rebuild sequence number {0} against lastest retrieved {1}.", this.globalCache.getBuildSequenceId(), latestSequenceId) ); 
		
		// Initiate cache update if something has changed
		if (this.globalCache.getBuildSequenceId() < latestSequenceId)
		{
			this.logger.info(Messages.getString("PolicyCacheProcessorImpl.29")); //$NON-NLS-1$
			
			// If the cache still isn't initialized, do a full rebuild
			if(this.cacheInitialized)
				this.buildCache(latestSequenceId, false);
			else
				this.buildCache(latestSequenceId, true);
		}
		else
			this.logger.debug(Messages.getString("PolicyCacheProcessorImpl.30")); //$NON-NLS-1$
		
	}

	/**
	 * Builds the global cache object from the data store. If the thread is starting up, it will completely build the
	 * cache from scratch, if not it will replace any PolicySet objects that have been modified since the last time the
	 * cache was built.
	 * 
	 * @param fullRebuild
	 *            If set to false, only updated policies will be refreshed.
	 *  @param latestSequenceId The highest sequenceId retrieved from the policies data store.
	 * @throws SQLException
	 *             If there was a problem connecting to the data store.
	 */
	private void buildCache(long latestSequenceId, boolean fullRebuild) throws SQLException
	{
		Vector<String> modifiedDescriptors = new Vector<String>();
		
		this.logger.debug(MessageFormat.format(Messages.getString("PolicyCacheProcessorImpl.37"), fullRebuild )); //$NON-NLS-1$ 
				
		Map<String, List<Policy>> databasePolicies = null;

		if (fullRebuild)
			// retrieve ALL lxacml policies from data source
			databasePolicies = this.retrievePolicies();
		else
			// only retrieve changed policies
			databasePolicies = this.retrieveChangedPolicies();
				
		if(databasePolicies != null && !databasePolicies.isEmpty())
		{
			// create policy object representations
			Iterator<String> iter = databasePolicies.keySet().iterator();
	
			while (iter.hasNext())
			{
				String currentKey = iter.next();
				modifiedDescriptors.add(currentKey);
			
				// add or replace real time cache policy with updated policies
				this.globalCache.add(currentKey, databasePolicies.get(currentKey)); 
		
			}
			
			// update cache sequence id
			this.globalCache.setBuildSequenceId(latestSequenceId);
			
			this.logger.info(MessageFormat.format(Messages.getString("PolicyCacheProcessorImpl.39"), this.globalCache.getSize()) );//$NON-NLS-1$

		}
		else
		{
			throw new SQLException(Messages.getString("PolicyCacheProcessorImpl.20")); //$NON-NLS-1$
		}
				
		// call SPEP notification method
		this.notifyCacheUpdate(modifiedDescriptors);
	}

	
	/*
	 * Notify ALL SPEP end point of a cache update.
	 * 
	 * @param authzRequest The request document to send to the SPEP @param endPoint The end point node of the SPEP
	 * @param spepStartup Whether or not it is an SPEP starting up request, in which case we do not record failed
	 * updates.
	 */
	private void notifyCacheUpdate(List<String> entities)
	{
		// iterate through list of SPEPS (obtained from SPEP processor)
		Iterator<String> entityIter = entities.iterator();

		while (entityIter.hasNext())
		{
			String entityID = entityIter.next();
			result updateResult = result.Failure;
			byte[] authzClearCacheRequest = null;
			String endpoint = null;
			
			try
			{
				Map<Integer,String> endpoints = this.metadata.resolveCacheClearService(entityID);
				
				Iterator<String> endpointIter = endpoints.values().iterator();
				
				while (endpointIter.hasNext())
				{
					endpoint = endpointIter.next();

					// get associated policies and create AuthzClearCache Request AND generate the request
					authzClearCacheRequest = this.generateClearCacheRequest(entityID, endpoint, Messages
							.getString("PolicyCacheProcessorImpl.4")); //$NON-NLS-1$

					// if the returned request string is null, then a problem occured retrieving policy data. Don't send
					// request.
					if(authzClearCacheRequest == null)
						this.logger.warn(MessageFormat.format(Messages.getString("PolicyCacheProcessorImpl.21"), entityID) ); //$NON-NLS-1$
					else
						updateResult = this.sendCacheUpdateRequest(authzClearCacheRequest, endpoint);
				
					if ( !updateResult.equals(result.Success))
					{
						this.recordFailure(authzClearCacheRequest, endpoint);
					}	
				}
			}
			// nothing we can do but warn, can't be delivered
			catch (InvalidMetadataEndpointException e)
			{				
				this.logger.warn(Messages.getString("PolicyCacheProcessorImpl.23") + entityID); //$NON-NLS-1$
				this.logger.trace(e.getLocalizedMessage(), e);
			}
			// response from SPEP could not be deciphered
			catch(MarshallerException e)
			{			
				this.logger.warn(Messages.getString("PolicyCacheProcessorImpl.24") + entityID); //$NON-NLS-1$
				this.logger.trace(e.getLocalizedMessage(), e);
			}
			
		}

	}

	
	/*
	 * Send an AuthzCacheUpdate request to the specified SPEP endpoint.
	 * 
	 * @param authzClearCacheRequest The xml authz cache request. 
	 * @param endPoint The endpoint to send to. 
	 * 
	 * @return The result of the operation. Either Success or Failure.
	 */
	private result sendCacheUpdateRequest(byte[] authzClearCacheRequest, String endPoint)
	{
		byte[] responseDocument;

		try
		{
			this.logger.info(MessageFormat.format(Messages.getString("PolicyCacheProcessorImpl.47"), endPoint)); //$NON-NLS-1$
			
			responseDocument = this.wsClient.authzCacheClear(authzClearCacheRequest, endPoint);
			
			ClearAuthzCacheResponse clearAuthzCacheResponse = null;
			
			clearAuthzCacheResponse = this.clearAuthzCacheResponseUnmarshaller.unMarshallSigned(responseDocument);

			// validate the response
			this.samlValidator.getResponseValidator().validate(clearAuthzCacheResponse);
			
			// process the Authz cache clear response
			if (clearAuthzCacheResponse != null && clearAuthzCacheResponse.getStatus() != null)
			{
				if(clearAuthzCacheResponse.getStatus().getStatusCode() != null)
				{
					if(StatusCodeConstants.success.equals(clearAuthzCacheResponse.getStatus().getStatusCode().getValue()))
					{
						this.logger.info(Messages.getString("PolicyCacheProcessorImpl.41")); //$NON-NLS-1$
						return result.Success;
					}
					else
						this.logger.error(MessageFormat.format(Messages.getString("PolicyCacheProcessorImpl.48"), clearAuthzCacheResponse.getStatus().getStatusMessage())); //$NON-NLS-1$
				}
				else
					this.logger.debug(Messages.getString("PolicyCacheProcessorImpl.42")); //$NON-NLS-1$
			}
			else
			{
				this.logger.debug(Messages.getString("PolicyCacheProcessorImpl.43")); //$NON-NLS-1$
			}
			
			return result.Failure;
		}		
		catch (UnmarshallerException e)
		{
			this.logger.error(Messages.getString("PolicyCacheProcessorImpl.25")); //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);
			
			return result.Failure;
		}		
		catch(SignatureValueException e)
		{
			this.logger.error(Messages.getString("PolicyCacheProcessorImpl.44"));  //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);
			
			return result.Failure;
		}
		catch(ReferenceValueException e)
		{
			this.logger.error(Messages.getString("PolicyCacheProcessorImpl.45"));  //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);
			
			return result.Failure;
		}
		catch (InvalidSAMLResponseException e)
		{
			this.logger.warn("SAML Response was invalid"); //$NON-NLS-1$
			this.logger.trace(e.getLocalizedMessage(), e);

			return result.Failure;
		}
		catch (WSClientException e)
		{
			this.logger.error(MessageFormat.format(Messages.getString("PolicyCacheProcessorImpl.26"), endPoint) ); //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);
			
			return result.Failure;
		}
		
	}

	
	/*
	 * Add the failure record to the cache update failure repository.
	 * 
	 * @param request The request that failed to deliver. @param endPoint The end point the the request failed to
	 * deliver to.
	 */
	private synchronized void recordFailure(byte[] request, String endPoint)
	{
		// create an UpdateFailure object
		FailedAuthzCacheUpdate failure = new FailedAuthzCacheUpdateImpl();

		failure.setEndPoint(endPoint);
		failure.setRequestDocument(request);
		failure.setTimeStamp(new Date(System.currentTimeMillis()));

		this.logger.info(Messages.getString("PolicyCacheProcessorImpl.46") + endPoint); //$NON-NLS-1$
		
		// add it to failure repository
		this.failureRep.add(failure);

	}

	
	/*
	 * Retrieve all Policy objects from the Policy cache data store.
	 * 
	 * @return A map of descriptorId -> Policy list for all entries in the data store.
	 */
	private Map<String, List<Policy>> retrievePolicies() throws SQLException
	{
		Map<String, List<Policy>> returnMap = Collections.synchronizedMap(new HashMap<String, List<Policy>>());

		PolicyCacheQueryData queryData = new PolicyCacheQueryData();

		List<PolicyCacheData> result = this.sqlConfig.queryPolicyCache(queryData);

		this.logger.debug(MessageFormat.format("Query retrieved {0} results.", result.size()) );
		
		if(result != null && !result.isEmpty())
		{
			List<Policy> datasourcePolicies = null;
			String entityID = null;
		
			Iterator<PolicyCacheData> resultIter = result.iterator();
			while (resultIter.hasNext())
			{
				PolicyCacheData data = resultIter.next();
				String policyId = "";
				try
				{
					if(data != null)
					{
						policyId = data.getPolicyId();
						entityID  = data.getEntityID();
						
						// Unmarshall currently processed policy and add to associated policy list for that descriptor
						Policy replacement = this.policyUnmarshaller.unMarshallUnSigned(data.getLxacmlPolicy());
						
						// if our return map does not already contain a list for the currently processed descriptorId, retrieve it from cache
						if(returnMap.get(entityID) == null)
							datasourcePolicies = this.globalCache.getPolicies(entityID);
						
						if(datasourcePolicies == null)
						{
							this.logger.debug("No current policies found. Creating new Policy List ..");
							// create new list if none stored
							datasourcePolicies = new Vector<Policy>();
						}
						
						try
						{
							this.logger.trace( new String(data.getLxacmlPolicy(), "UTF-16"));
						}
						catch(UnsupportedEncodingException e)
						{
							e.printStackTrace();
						}
						
						// if the policy already exists in the List, remove it first and replace it.
						Policy policyToReplace = this.getMatchingPolicy(datasourcePolicies, policyId);
						if(policyToReplace != null)
						{
							boolean removeResult  = datasourcePolicies.remove(policyToReplace);
							this.logger.debug(MessageFormat.format("Result of removing Policy {0} from data source List is {1}.",policyToReplace, removeResult) );
						}
						
						datasourcePolicies.add(replacement);
						
						this.logger.debug(MessageFormat.format("Added policy {0} to List of Policies for {1}", policyId, entityID) ) ;
					}
				}
				// in case there is junk in the data base
				catch(ClassCastException e)
				{
					this.logger.warn(MessageFormat.format("Invalid object contained in Policy data store cache . Offending entity is {0}." , entityID) );
					this.logger.debug(e.getLocalizedMessage(), e);
				}
				catch (UnmarshallerException e)
				{
					this.logger.warn(MessageFormat.format(Messages.getString("PolicyCacheProcessorImpl.28"), policyId) ); //$NON-NLS-1$
					this.logger.debug(e.getLocalizedMessage(), e);
				}
				
				// replace with updated policy list if valid policies are available
				if(datasourcePolicies.size() != 0)
				{
					// replace with updated policy list
					this.logger.debug(MessageFormat.format("Updating {0} ({1} Policies).", entityID, datasourcePolicies.size()));
					returnMap.put(entityID , datasourcePolicies );
				}
				else
				{
					this.logger.warn(MessageFormat.format("DescriptorID {0} has no valid policies. Removing from cache policy update map.", entityID) );
					returnMap.remove(entityID);
				}
			}
		}

		return returnMap;
	}

	
	/*
	 * Retrieve a list of xacml policies that have a sequence number greater than the current cache rebuild sequence number.
	 * 
	 * NOTE: The data structure returned by this method is assumed to be the replacement for the current cache data. As such,
	 * it will examine the field pollAction() in the returned ibatis bean to see if an operation should be applied to the policy (such
	 * as delete). It can therefore be assumed that this method will return an accurate and up to date representation of any
	 * policies which have changed in the data store, including additions or deletions.
	 * 
	 * @return  A map of descriptorId -> Policy list for all entries in the data store.
	 */
	private Map<String, List<Policy>> retrieveChangedPolicies() throws SQLException
	{
		Map<String, List<Policy>> policies = Collections.synchronizedMap(new HashMap<String, List<Policy>>());

		PolicyCacheQueryData queryData = new PolicyCacheQueryData();
		
		queryData.setSequenceId(new BigDecimal(this.globalCache.getBuildSequenceId()));
		
		List<PolicyCacheData> result = this.sqlConfig.queryPolicyCache(queryData);

		this.logger.debug(MessageFormat.format("Query retrieved {0} results.", result.size()) );
		
		if(result != null)
		{
			List<Policy> datasourcePolicies = null;
			String entityID = null;
			
			Iterator<PolicyCacheData> resultIter = result.iterator();
			while (resultIter.hasNext())
			{
				PolicyCacheData data = resultIter.next();
				entityID = data.getEntityID();
					
				try
				{	
					// first attempt to unmarshall the currently processed policy. If this fails we need do no more
					Policy replacement = this.policyUnmarshaller.unMarshallUnSigned(data.getLxacmlPolicy()) ;
				
					// Retrieve any currently stored policies for currently processed entityID
					try
					{
						this.logger.trace( new String(data.getLxacmlPolicy(), "UTF-16"));
					}
					catch(UnsupportedEncodingException e)
					{
						e.printStackTrace();
					}
					
					// if our return map does not already contain a list for the currently processed entityID, retrieve it from cache
					if(policies.get(entityID) == null)
						datasourcePolicies = this.globalCache.getPolicies(entityID);
					
					if(datasourcePolicies == null)
					{
						// create new list if none stored
						datasourcePolicies = new Vector<Policy>();
					}
					
					this.logger.debug(MessageFormat.format("Retrieved current policy list for {0}. {1} policies found.", entityID, datasourcePolicies.size()) );
					
					// check poll actions to see if replace or remove
					char pollAction = (data.getPollAction().length() != 0 ? data.getPollAction().charAt(0) : ConfigurationConstants.POLICY_STATE_NONE);
					
					this.logger.debug("Poll action to perform is " + pollAction);
					
					if(pollAction == ConfigurationConstants.POLICY_STATE_UPDATE)
					{
						// if the policy already exists in the List, remove it first and replace it.
						Policy policyToReplace = this.getMatchingPolicy(datasourcePolicies, data.getPolicyId());
						if(policyToReplace != null)
						{
							boolean removeResult  = datasourcePolicies.remove(policyToReplace);
							this.logger.info( MessageFormat.format("Result of removing Policy {0} from data source List is {1}.",policyToReplace, removeResult) );
						}
						
						// Add the replacement.
						datasourcePolicies.add(replacement);
						
						this.logger.debug("Updated Policy " + data.getPolicyId());
						
					}
					else if(pollAction == ConfigurationConstants.POLICY_STATE_DELETE)
					{
						// if the policy already exists in the List, remove it first and replace it.
						Policy policyToReplace = this.getMatchingPolicy(datasourcePolicies, data.getPolicyId());
						if(policyToReplace != null)
						{
							boolean removeResult  = datasourcePolicies.remove(policyToReplace);
							this.logger.info(MessageFormat.format("Result of removing Policy {0} from data source List is {1}.",policyToReplace, removeResult) );
						}
											
						this.logger.debug("Deleted Policy " + data.getPolicyId());	
					}
					else if(pollAction == ConfigurationConstants.POLICY_STATE_ADD)
					{
						// if the policy does not already exist in the List, add it.
						if(this.getMatchingPolicy(datasourcePolicies, replacement.getPolicyId()) == null)
						{
							datasourcePolicies.add(replacement);
							this.logger.debug("Added Policy " + data.getPolicyId());
						}
						else
						{
							this.logger.warn("Attempt to add existing Policy. Ignoring update.");
						}
					}
					else
						this.logger.warn(MessageFormat.format("Updated policy {0} did not contain a valid poll action. Ignoring update.", data.getPolicyId()));
									
				}
				// in case there is junk in the data base
				catch(ClassCastException e)
				{
					this.logger.warn(MessageFormat.format("Invalid object contained in Policy data store cache . Offending descriptor is {0}." , entityID) );
					this.logger.debug(e.getLocalizedMessage(), e);
				}
				catch (UnmarshallerException e)
				{
					this.logger.warn(MessageFormat.format(Messages.getString("PolicyCacheProcessorImpl.28"), entityID) ); //$NON-NLS-1$
					this.logger.debug(e.getLocalizedMessage(), e);
				}
				
				// replace with updated policy list if valid policies are available
				if(datasourcePolicies.size() != 0)
				{
					// replace with updated policy list
					this.logger.debug(MessageFormat.format("Retrieved Policies for {0} ({1} Policies).", entityID, datasourcePolicies.size()));
					policies.put(entityID , datasourcePolicies );
				}
				else
				{
					this.logger.warn(MessageFormat.format("DescriptorID {0} has no valid policies. Removing from cache policy update map.", entityID) );
					policies.remove(entityID);
				}
			}
		}

		return policies;
	}

	
	
	/*
	 * Creates and marshall's the AuthzClearCache Request. Note: If no policies are found for the given entityID,
	 * no objects will be marshalled and a null xml string will be returned. This is done because SPEP's with no valid
	 * policies should not be notified of cache updates.
	 * 
	 * @param descriptorId The ID of the SPEP that is being notified of a cache update. Used to retrieve GroupTargets and AuthzTargets. 
	 * @param endpoint The SPEP endpoint identifier of the reciever. 
	 * @param reason The reason for the update notification. 
	 * @return The representation of the xml request object generated by this method.
	 */
	private byte[] generateClearCacheRequest(String entityID, String endpoint, String reason) throws MarshallerException
	{
		byte[] requestDocument = null;
		ClearAuthzCacheRequest request = new ClearAuthzCacheRequest();
		Extensions extensions = new Extensions();
					
		List<Policy> policies = this.globalCache.getPolicies(entityID);
		
		if(policies != null)
		{
			this.logger.debug(MessageFormat.format("Generating clearCacheRequest for {0}. {1} Policies retrieved.", entityID, policies.size()));
			
			Iterator<Policy> policyIter = policies.iterator();
			
			while (policyIter.hasNext())
			{							
				Policy policy = policyIter.next();
	
				// retrieve a list of all resources strings in the policy
				List<String> policyResources = PolicyEvaluator.getPolicyTargetResources(policy);
				Iterator<String> policyTargetIter = policyResources.iterator();
					
				while (policyTargetIter.hasNext())
				{					
					String policyResource = policyTargetIter.next();
				
					GroupTarget groupTarget = PolicyEvaluator.getMatchingGroupTarget(policy, policyResource);
					
					Element groupTargetElement = this.groupTargetMarshaller.marshallUnSignedElement(groupTarget);
					
					extensions.getAnies().add(groupTargetElement);
					
					request.setExtensions(extensions);					
				}
			}
						
			request.setID(this.identifierGenerator.generateSAMLID());
			request.setReason(reason);
			request.setVersion(VersionConstants.saml20);
			
			// set the destination end point ID
			request.setDestination(endpoint);
			
			NameIDType issuer = new NameIDType();
			issuer.setValue(this.metadata.getEsoeEntityID());
			request.setIssuer(issuer);
			
			// Time stamps MUST be set to UTC, no offset
			request.setIssueInstant(CalendarUtils.generateXMLCalendar());
			
			request.setSignature(new Signature());

			// marshall the clear cache request
			requestDocument = this.clearAuthzCacheRequestMarshaller.marshallSigned(request);
				
		}					
			
		return requestDocument;
	}
	
	/* Convenience method to return the Policy object matching the given policy ID in the given list. Assumes that there
	 * is only one such matching policy, as it only returns the first match.
	 * 
	 */
	private Policy getMatchingPolicy(List<Policy> listToSearch, String policyId)
	{
		// find the current element and replace it
		Iterator <Policy>iter = listToSearch.iterator();
		while(iter.hasNext())
		{
			Policy next = iter.next();
			// return any policies thatmatch the given policy id
			if(next.getPolicyId().equals(policyId))
			{
				this.logger.debug("Found matching Policy. Returning " + next);
				return next;
			}
		}
		
		return null;
	}
	
	
	public void shutdown()
	{
		this.setRunning(false);
		
		this.interrupt();
	}
	
	protected synchronized boolean isRunning()
	{
		return this.running;
	}
	
	protected synchronized void setRunning(boolean running)
	{
		this.running = running;
	}
}

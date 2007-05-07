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
import com.qut.middleware.saml2.schemas.esoe.lxacml.PolicySet;
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
	
	private Metadata metadata;
	// we need to know when the cache was last rebuilt so we can compare last modified times
	private Date cacheRebuildTime;
	private int pollInterval;
	private PrivateKey key;
	private String keyName;
	private IdentifierGenerator identifierGenerator;
	
	private String[] schemas = new String[] { ConfigurationConstants.esoeProtocol, ConfigurationConstants.samlProtocol };

	private final PolicyCacheDao sqlConfig;

	private Marshaller<ClearAuthzCacheRequest> clearAuthzCacheRequestMarshaller;
	private Unmarshaller<ClearAuthzCacheResponse> clearAuthzCacheResponseUnmarshaller;
	private Unmarshaller<PolicySet> policySetUnmarshaller;
	private Marshaller<GroupTarget> groupTargetMarshaller;
	private SAMLValidator samlValidator;
	private WSClient wsClient;
	
	private final String UNMAR_PKGNAMES = ClearAuthzCacheResponse.class.getPackage().getName();
	private final String MAR_PKGNAMES = ClearAuthzCacheRequest.class.getPackage().getName() + ":" + GroupTarget.class.getPackage().getName(); //$NON-NLS-1$
   	private final String UNMAR_PKGNAMES2 = PolicySet.class.getPackage().getName();
	private final String MAR_PKGNAMES2 = GroupTarget.class.getPackage().getName();
	
	/* Local logging instance */
	private Logger logger = Logger.getLogger(PolicyCacheProcessorImpl.class.getName());

	/**
	 * Constructor which takes a spring injected datasource as a param. Spring will inject a singleton instance of the
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
		this.cacheRebuildTime = new Date(System.currentTimeMillis());
		this.identifierGenerator = identifierGenerator;
		this.samlValidator = samlValidator;

		this.clearAuthzCacheRequestMarshaller = new MarshallerImpl<ClearAuthzCacheRequest>(this.MAR_PKGNAMES, this.schemas, this.keyName, this.key);
		this.clearAuthzCacheResponseUnmarshaller = new UnmarshallerImpl<ClearAuthzCacheResponse>(this.UNMAR_PKGNAMES, this.schemas, this.metadata);
		this.policySetUnmarshaller = new UnmarshallerImpl<PolicySet>(this.UNMAR_PKGNAMES2, new String[]{ConfigurationConstants.lxacml});
		this.groupTargetMarshaller = new MarshallerImpl<GroupTarget>(this.MAR_PKGNAMES2, this.schemas);
		
		this.logger.info(MessageFormat.format(Messages.getString("PolicyCacheProcessorImpl.10"), (this.pollInterval/1000)) ); //$NON-NLS-1$

		this.setName(Messages.getString("PolicyCacheProcessorImpl.11")); //$NON-NLS-1$
		this.setDaemon(false);
		this.start();
		
	}

	/**
	 * @see com.qut.middleware.esoe.pdp.cache.PolicyCacheProcessor#spepStartingNotification(String, int)
	 */
	public result spepStartingNotification(String descriptorID, int authzCacheIndex)
	{		
		String endpoint = null;
		
		try
		{
			Map<Integer,String> cacheClearServiceList = this.metadata.resolveCacheClearService(descriptorID);
			if (cacheClearServiceList == null)
			{
				this.logger.error(Messages.getString("PolicyCacheProcessorImpl.33") + descriptorID); //$NON-NLS-1$
				return result.Failure;
			}
			
			endpoint = cacheClearServiceList.get(authzCacheIndex);

			// get associated policies and create authz clear cache request AND generate the request
			String authzClearCacheRequest = this.generateClearCacheRequest(descriptorID, endpoint, Messages
					.getString("PolicyCacheProcessorImpl.3")); //$NON-NLS-1$

			// if the returned request string is null, then a problem occured retrieving policy data.
			// Don't send request.
			if(authzClearCacheRequest == null)
			{
				this.logger.warn(Messages.getString("PolicyCacheProcessorImpl.12") + descriptorID + Messages.getString("PolicyCacheProcessorImpl.13")); //$NON-NLS-1$ //$NON-NLS-2$
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

	private void init() throws SQLException
	{
		this.buildCache(true);
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
				this.logger.warn(Messages.getString("PolicyCacheProcessorImpl.18")); //$NON-NLS-1$
				this.logger.debug(e.getLocalizedMessage(), e);
			}
			catch(InterruptedException e)
			{
				if(!this.isRunning())
					break;
			}
			// ignore interrupts and other non-runtime exceptions
			catch (Exception e)
			{
				this.logger.debug(e.getLocalizedMessage(), e);
			}
		}
		
		this.logger.info(this.getName() + Messages.getString("PolicyCacheProcessorImpl.34")); //$NON-NLS-1$
		
		return;
	}

	/**
	 * Poll the datasource containing authorization policies to see if there has been a change. If a policy contained in
	 * the associated datasource has changed, this method will initiate a cache update - this.updateCache()
	 * 
	 * @throws SQLException
	 */
	private void poll() throws SQLException
	{
		Date lastUpdated = this.sqlConfig.queryDateLastUpdated();

		if(lastUpdated == null)
			throw new SQLException(Messages.getString("PolicyCacheProcessorImpl.19")); //$NON-NLS-1$
		
		this.logger.debug(Messages.getString("PolicyCacheProcessorImpl.35") + this.cacheRebuildTime + Messages.getString("PolicyCacheProcessorImpl.36") + lastUpdated); //$NON-NLS-1$ //$NON-NLS-2$
		
		// initiate cache update if something has changed
		if (this.cacheRebuildTime.before(lastUpdated))
		{
			this.logger.info(Messages.getString("PolicyCacheProcessorImpl.29")); //$NON-NLS-1$
			this.buildCache(false);
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
	 * @throws SQLException
	 *             If there was a problem connecting to the data store.
	 */
	private void buildCache(boolean fullRebuild) throws SQLException
	{
		Vector<String> modifiedDescriptors = new Vector<String>();
		
		this.logger.debug(Messages.getString("PolicyCacheProcessorImpl.37") + fullRebuild + Messages.getString("PolicyCacheProcessorImpl.38")); //$NON-NLS-1$ //$NON-NLS-2$
				
		Map<String, PolicySet> databasePolicies = null;

		if (fullRebuild)
			// retrieve ALL laxacml policies from data source
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

				// add or replace realtime cache policy with updated policies
				this.globalCache.add(currentKey, this.getPolicies(databasePolicies.get(currentKey))); 
			}
		}
		else
		{
			throw new SQLException(Messages.getString("PolicyCacheProcessorImpl.20")); //$NON-NLS-1$
		}
		
		this.cacheRebuildTime = new Date(System.currentTimeMillis());
		
		this.logger.info(Messages.getString("PolicyCacheProcessorImpl.39") + this.cacheRebuildTime + Messages.getString("PolicyCacheProcessorImpl.40") + this.globalCache.getSize()); //$NON-NLS-1$ //$NON-NLS-2$
		
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
	private void notifyCacheUpdate(List<String> descriptors)
	{
		// iterate through list of SPEPS (obtained from SPEP processor)
		Iterator<String> descriptorIDIter = descriptors.iterator();

		while (descriptorIDIter.hasNext())
		{
			String descriptorID = descriptorIDIter.next();
			result updateResult = result.Failure;
			String authzClearCacheRequest = null;
			String endpoint = null;
			
			try
			{
				Map<Integer,String> endpoints = this.metadata.resolveCacheClearService(descriptorID);
				
				Iterator<String> endpointIter = endpoints.values().iterator();
				
				while (endpointIter.hasNext())
				{
					endpoint = endpointIter.next();

					// get associated policies and create authz clear cache request AND generate the request
					authzClearCacheRequest = this.generateClearCacheRequest(descriptorID, endpoint, Messages
							.getString("PolicyCacheProcessorImpl.4")); //$NON-NLS-1$

					// if the returned request string is null, then a problem occured retrieving policy data. Don't send
					// request.
					if(authzClearCacheRequest == null)
						this.logger.warn(MessageFormat.format(Messages.getString("PolicyCacheProcessorImpl.21"), descriptorID) ); //$NON-NLS-1$
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
				this.logger.warn(Messages.getString("PolicyCacheProcessorImpl.23") + descriptorID); //$NON-NLS-1$
				this.logger.trace(e.getLocalizedMessage(), e);
			}
			// response from SPEP could not be deciphered
			catch(MarshallerException e)
			{			
				this.logger.warn(Messages.getString("PolicyCacheProcessorImpl.24") + descriptorID); //$NON-NLS-1$
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
	private result sendCacheUpdateRequest(String authzClearCacheRequest, String endPoint)
	{
		String responseDocument;

		try
		{
			this.logger.debug(MessageFormat.format(Messages.getString("PolicyCacheProcessorImpl.47"), endPoint)); //$NON-NLS-1$
			
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
						this.logger.debug(Messages.getString("PolicyCacheProcessorImpl.41")); //$NON-NLS-1$
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
	private synchronized void recordFailure(String request, String endPoint)
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
	 * Retrieve all PolicySet objects the the Policy cache.
	 * 
	 * @return A map of descriptorID -> PolicySet for all entries in the data store.
	 */
	private Map<String, PolicySet> retrievePolicies() throws SQLException
	{
		Map<String, PolicySet> policies = Collections.synchronizedMap(new HashMap<String, PolicySet>());

		// spring will map the results into a list for us

		PolicyCacheQueryData queryData = new PolicyCacheQueryData();

		Map<String, PolicyCacheData> result = this.sqlConfig.queryPolicyCache(queryData);

		if(result != null && !result.isEmpty())
		{
			Iterator<String> resultIter = result.keySet().iterator();
			while (resultIter.hasNext())
			{
				String descriptorID = resultIter.next();
				try
				{
					PolicyCacheData data = result.get(descriptorID);
					if(data != null)
						policies.put(descriptorID, this.policySetUnmarshaller.unMarshallUnSigned(data.getLxacmlPolicy()) );
	
				}
				catch (UnmarshallerException e)
				{
					this.logger.warn(Messages.getString("PolicyCacheProcessorImpl.27") + descriptorID); //$NON-NLS-1$
					this.logger.debug(e.getLocalizedMessage(), e);
				}
			}
		}

		return policies;
	}

	
	/*
	 * Retrieve a list of xacml policies that have a modified date greater than the time the cache was last rebuilt. *
	 * 
	 * @return Internal data type to be determined by spring @throws PolicyDataSourceException
	 */
	private Map<String, PolicySet> retrieveChangedPolicies() throws SQLException
	{
		Map<String, PolicySet> policies = Collections.synchronizedMap(new HashMap<String, PolicySet>());

		PolicyCacheQueryData queryData = new PolicyCacheQueryData();
		
		queryData.setDateLastUpdated(this.cacheRebuildTime);
		
		Map<String, PolicyCacheData> result = this.sqlConfig.queryPolicyCache(queryData);

		Iterator<String> resultIter = result.keySet().iterator();
		while (resultIter.hasNext())
		{
			String descriptorID = resultIter.next();
			try
			{
				PolicyCacheData data = result.get(descriptorID);
				PolicySet policySet = this.policySetUnmarshaller.unMarshallUnSigned(data.getLxacmlPolicy());
				
				policies.put(descriptorID, policySet);
				
			}
			catch (UnmarshallerException e)
			{
				this.logger.warn(Messages.getString("PolicyCacheProcessorImpl.28") + descriptorID ); //$NON-NLS-1$
				this.logger.debug(e.getLocalizedMessage(), e);
			}
		}

		return policies;
	}

	

	/*
	 * Small helper to return the policies.
	 */
	private Vector<Policy> getPolicies(PolicySet policySet)
	{
		Vector<Policy> policies = new Vector<Policy>();

		policies.addAll(policySet.getPolicies());

		return policies;

	}

	
	/*
	 * Creates and marshalls the authz clear cache request. Note: If no policies are found for the given descriptorID,
	 * no objects will be marshalled and a null xml string will be returned. This is done because SPEP's with no valid
	 * policies should not be notified of cache updates.
	 * 
	 * @param descriptorID The ID of the SPEP that is being notified of a cache update. Used to retrieve GroupTargets and AuthzTargets. 
	 * @param endpoint The SPEP endpoint identifier of the reciever. 
	 * @param reason The reason for the update notification. 
	 * @return The string representation of the xml request object generated by this method.
	 */
	private String generateClearCacheRequest(String descriptorID, String endpoint, String reason) throws MarshallerException
	{
		String requestDocument = null;
		ClearAuthzCacheRequest request = new ClearAuthzCacheRequest();
		Extensions extensions = new Extensions();
					
		List<Policy> policies = this.globalCache.getPolicies(descriptorID);
		
		if(policies != null)
		{
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
			
			// set the desintation endpoint ID
			request.setDestination(endpoint);
			
			NameIDType issuer = new NameIDType();
			issuer.setValue(this.metadata.getESOEIdentifier());
			request.setIssuer(issuer);
			
			// Timestamps MUST be set to UTC, no offset
			request.setIssueInstant(CalendarUtils.generateXMLCalendar());
			
			request.setSignature(new Signature());

			// marshall the clear cache request
			requestDocument = this.clearAuthzCacheRequestMarshaller.marshallSigned(request);
				
		}					
			
		return requestDocument;
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

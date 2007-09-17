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
 * Creation Date: 25/10/2006
 * 
 * Purpose: Maintains a background thread that polls the metadata from the given URL, 
 * 		on a given interval, and updates if there have been any changes.
 */
package com.qut.middleware.esoe.metadata.impl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;
import org.springframework.util.FileCopyUtils;
import org.w3c.dom.Element;

import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.MonitorThread;
import com.qut.middleware.esoe.crypto.KeyStoreResolver;
import com.qut.middleware.esoe.metadata.cache.CacheData;
import com.qut.middleware.esoe.metadata.cache.MetadataCache;
import com.qut.middleware.esoe.metadata.cache.impl.CacheDataImpl;
import com.qut.middleware.saml2.exception.UnmarshallerException;
import com.qut.middleware.saml2.handler.Unmarshaller;
import com.qut.middleware.saml2.handler.impl.UnmarshallerImpl;
import com.qut.middleware.saml2.schemas.metadata.EndpointType;
import com.qut.middleware.saml2.schemas.metadata.EntitiesDescriptor;
import com.qut.middleware.saml2.schemas.metadata.EntityDescriptor;
import com.qut.middleware.saml2.schemas.metadata.Extensions;
import com.qut.middleware.saml2.schemas.metadata.IndexedEndpointType;
import com.qut.middleware.saml2.schemas.metadata.RoleDescriptorType;
import com.qut.middleware.saml2.schemas.metadata.SPSSODescriptor;
import com.qut.middleware.saml2.schemas.metadata.extensions.CacheClearService;
import com.qut.middleware.saml2.schemas.metadata.lxacml.LXACMLPDPDescriptor;
import com.qut.middleware.saml2.sec.KeyData;


public class MetadataUpdateMonitor extends Thread implements MonitorThread
{
	private volatile boolean running;
	
	private MetadataCache metadataCache;
	private String metadataURL;
	protected PublicKey publicKey;	
	private String[] schemas;
	private int interval;
	private final String CACHE_CLEAR_SERVICE_NAME = "CacheClearService"; //$NON-NLS-1$
	
	private Unmarshaller<EntitiesDescriptor> unmarshaller;
	private Unmarshaller<CacheClearService> cacheClearServiceUnmarshaller;
	
	private final String UNMAR_PKGNAMES = EntitiesDescriptor.class.getPackage().getName() + ":" + LXACMLPDPDescriptor.class.getPackage().getName();
	private final String UNMAR_PKGNAMES2 = CacheClearService.class.getPackage().getName();
	
	
	/* Local logging instance */
	private Logger logger = Logger.getLogger(MetadataUpdateMonitor.class.getName());

	/**
	 * @param metadataCache
	 *            The metadata cache to manipulate with this thread
	 * @param keyStoreResolver
	 * 			  Used to resolve public keys required for decryption of metadata.
	 * @param metadataURL
	 * 			  The URL containing the metadata file.
	 * @param interval
	 *            The interval at which to refresh the metadata and update if it has changed, in seconds
	 *            
	 * @throws UnmarshallerException if the metadata unmarshallers cannot be created.
	 */
	public MetadataUpdateMonitor(MetadataCache metadataCache, KeyStoreResolver keyStoreResolver, String metadataURL, int interval) throws UnmarshallerException
	{
		super("ESOE Metadata update Monitor"); //$NON-NLS-1$
		
		if(metadataURL == null)
		{
			throw new IllegalArgumentException(Messages.getString("MetadataUpdateMonitor.0")); //$NON-NLS-1$
		}
		if(metadataCache == null)
		{
			throw new IllegalArgumentException(Messages.getString("MetadataUpdateMonitor.1")); //$NON-NLS-1$
		}
		if(keyStoreResolver == null)
		{
			throw new IllegalArgumentException(Messages.getString("MetadataUpdateMonitor.2"));  //$NON-NLS-1$
		}
		if(interval <= 0 || (interval > Integer.MAX_VALUE / 1000) )
		{
			throw new IllegalArgumentException(Messages.getString("MetadataUpdateMonitor.3")); //$NON-NLS-1$
		}
		
		this.metadataCache = metadataCache;
		this.publicKey = keyStoreResolver.getPublicKey();
		this.interval = interval * 1000;
		this.metadataURL = metadataURL;
		
		this.schemas = new String[] { ConfigurationConstants.lxacmlMetadata, ConfigurationConstants.cacheClearService};
		
		this.unmarshaller = new UnmarshallerImpl<EntitiesDescriptor>(this.UNMAR_PKGNAMES, this.schemas);
		this.cacheClearServiceUnmarshaller = new UnmarshallerImpl<CacheClearService>(this.UNMAR_PKGNAMES2, this.schemas);

		this.logger.info(MessageFormat.format(Messages.getString("MetadataUpdateMonitor.4"),  interval));   //$NON-NLS-1$

		this.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run()
	{
		this.setRunning(true);		
		
		try
		{
			doGetMetadata();
			
			// no exceptions = successfull update, set cach state accordingly
			this.metadataCache.setState(MetadataCache.State.Initialized);
			
		}
		catch (Exception e)
		{
			// failed to initialize cache data
			this.metadataCache.setState(MetadataCache.State.UnInitialized);
			
			this.logger.error(MessageFormat.format(Messages.getString("MetadataUpdateMonitor.5"), e.getMessage()));  //$NON-NLS-1$
		}
			
		while (this.isRunning())
		{
			try
			{
				Thread.sleep(this.interval);
				
				this.logger.debug(Messages.getString("MetadataUpdateMonitor.6"));  //$NON-NLS-1$
			
				// retrieve metadata and update if required
				doGetMetadata();
				
				// no exceptions = successfull update, set cach state accordingly
				this.metadataCache.setState(MetadataCache.State.Initialized);
				
				this.logger.debug(Messages.getString("MetadataUpdateMonitor.7")); //$NON-NLS-1$
			}
			catch(InterruptedException e)
			{
				if(!this.isRunning())
					break;
			}
			catch (Exception e)
			{
				// if cache is initialized, set an error state otherwise it's still considered un-initialized
				if(this.metadataCache.getState() != MetadataCache.State.UnInitialized)
						this.metadataCache.setState(MetadataCache.State.Error);
				
				this.logger.error(MessageFormat.format(Messages.getString("MetadataUpdateMonitor.8"), e.getMessage())); //$NON-NLS-1$
				this.logger.trace(e);
			}
		}
		
		this.logger.info(this.getName() + Messages.getString("MetadataUpdateMonitor.9"));  //$NON-NLS-1$
		
		return;
	}

	private void doGetMetadata() throws Exception
	{
		RawMetadata rawMetadata = getRawMetadata(this.metadataURL);

		EntitiesDescriptor entitiesDescriptor = null;
		
		this.logger.debug(MessageFormat.format(Messages.getString("MetadataUpdateMonitor.10"), rawMetadata.hashValue, this.metadataCache.getCurrentRevision() )); //$NON-NLS-1$
		
		if (!rawMetadata.hashValue.equalsIgnoreCase(this.metadataCache.getCurrentRevision()))
		{
			this.logger.trace(MessageFormat.format(Messages.getString("MetadataUpdateMonitor.11"), rawMetadata.hashValue, rawMetadata.data));  //$NON-NLS-1$
		
			try
			{
				this.logger.debug(Messages.getString("MetadataUpdateMonitor.14"));  //$NON-NLS-1$

				// Do the unmarshalling step
				Map<String, KeyData> keyMap = Collections.synchronizedMap(new HashMap<String, KeyData>());
				
				entitiesDescriptor = this.unmarshaller.unMarshallMetadata(this.publicKey, rawMetadata.data, keyMap);
				
				this.rebuildCache(entitiesDescriptor, rawMetadata.hashValue, keyMap);
			}
			catch (ClassCastException e)
			{
				this.logger.error(MessageFormat.format(Messages.getString("MetadataUpdateMonitor.15"), e.getMessage()));  //$NON-NLS-1$
				this.logger.trace(e);
				throw new UnsupportedOperationException(Messages.getString("MetadataUpdateMonitor.16")); //$NON-NLS-1$
			}
		}
	}
	

	/* Obtain the raw metadata from the given string rtepresentation of the URL that contains the 
	 * metadata xml file. The xml document must be encoded in UTF-16.
	 */
	private RawMetadata getRawMetadata(String metadataUrl) throws NoSuchAlgorithmException, MalformedURLException, UnsupportedEncodingException, IOException
	{
		try
		{
			byte[] digestBytes;
			RawMetadata rawMetadata = new RawMetadata();
			
			MessageDigest messageDigest = MessageDigest.getInstance("SHA1"); //$NON-NLS-1$

			URL url = new URL(metadataUrl);
			DigestInputStream digestStream = new DigestInputStream(url.openStream(), messageDigest);
			BufferedInputStream bufferedStream = new BufferedInputStream(digestStream);

			rawMetadata.data = FileCopyUtils.copyToByteArray(bufferedStream);
			
			this.logger.debug(MessageFormat.format(Messages.getString("MetadataUpdateMonitor.17"), Integer.toString(rawMetadata.data.length)));  //$NON-NLS-1$
			
			digestBytes = digestStream.getMessageDigest().digest();
			rawMetadata.hashValue = new String(Hex.encodeHex(digestBytes));

			return rawMetadata;
		}
		catch (IOException e)
		{
			this.logger.debug(Messages.getString("MetadataUpdateMonitor.18"), e);  //$NON-NLS-1$
			throw e;
		}
	}
	
	/*
	 * 
	 */
	private void rebuildCache(EntitiesDescriptor entitiesDescriptor, String newRevision, Map<String,KeyData> newKeyMap)
	{
		this.logger.info(MessageFormat.format(Messages.getString("MetadataUpdateMonitor.19"), newRevision));  //$NON-NLS-1$
		
		Map<String, String> newAssertionConsumerServices = Collections.synchronizedMap(new HashMap<String,String>());
		Map<String, List<String>> newAssertionConsumerServiceIdentifierTypes = Collections.synchronizedMap(new HashMap<String, List<String>>());
		Map<String, List<String>> newSingleLogoutServices = Collections.synchronizedMap(new HashMap<String,List<String>>());
		Map<String, Map<Integer,String>> newCacheClearServices = Collections.synchronizedMap(new HashMap<String,Map<Integer,String>>());

		buildCacheRecurse(entitiesDescriptor, newAssertionConsumerServices, newAssertionConsumerServiceIdentifierTypes, newSingleLogoutServices, newCacheClearServices);
		
		// create the new cache data 
		CacheData data = new CacheDataImpl();
		
		data.setCurrentRevision(newRevision);
		data.setKeyMap(newKeyMap);
		data.setAssertionConsumerServices(newAssertionConsumerServices);
		data.setAssertionConsumerServiceIdentifierTypes(newAssertionConsumerServiceIdentifierTypes);
		data.setSingleLogoutServices(newSingleLogoutServices);
		data.setCacheClearServices(newCacheClearServices);
		
		// call atomic set data function
		this.metadataCache.setCacheData(data);
	
	}
	
	
	private void buildCacheRecurse(EntitiesDescriptor entitiesDescriptor, Map<String, String> newAssertionConsumerServices, Map<String, List<String>> newAssertionConsumerServiceIdentifierTypes, Map<String, List<String>> newSingleLogoutServices, Map<String, Map<Integer,String>> newCacheClearServices)
	{
		this.logger.trace(Messages.getString("MetadataUpdateMonitor.20")); //$NON-NLS-1$

		List<Object> descriptorList = entitiesDescriptor.getEntitiesDescriptorsAndEntityDescriptors();
		// Go through the list of descriptors
		// We will encounter both <EntityDescriptor> and <EntitiesDescriptor> objects. We will recurse for the latter.
		/* No need for sync here as entitiesDescriptor which descriptorList is derived from is thread local */
		for (Object descriptor : descriptorList)
		{
			if (descriptor instanceof EntityDescriptor)
			{
				this.logger.trace(Messages.getString("MetadataUpdateMonitor.21")); //$NON-NLS-1$
				
				EntityDescriptor entityDescriptor = (EntityDescriptor) descriptor;
				List<RoleDescriptorType> roleDescriptorList = entityDescriptor.getIDPDescriptorAndSSODescriptorAndRoleDescriptors();

				// Go through the RoleDescriptors. This includes IDP and SSO descriptors
				for (RoleDescriptorType roleDescriptor : roleDescriptorList)
				{
					// If we have an SPSSODescriptor, populate the caches with the information inside.
					if (roleDescriptor instanceof SPSSODescriptor)
					{
						this.logger.trace(Messages.getString("MetadataUpdateMonitor.22")); //$NON-NLS-1$
						
						// Assertion consumer services..
						SPSSODescriptor spSSODescriptor = (SPSSODescriptor) roleDescriptor;
						List<IndexedEndpointType> assertionConsumerServiceList = spSSODescriptor
								.getAssertionConsumerServices();

						for (IndexedEndpointType assertionConsumerService : assertionConsumerServiceList)
						{
							String assertionConsumerServiceLocation = assertionConsumerService.getLocation();
							
							this.logger.trace(MessageFormat.format(Messages.getString("MetadataUpdateMonitor.23"), assertionConsumerServiceLocation)); //$NON-NLS-1$

							int index = assertionConsumerService.getIndex();
							String key = generateKey(entityDescriptor.getEntityID(), index);
							
							newAssertionConsumerServices.put(key, assertionConsumerServiceLocation);
							
							List<String> identifiers = new ArrayList<String>();
							for(String acceptedIdentifier : spSSODescriptor.getNameIDFormats())
							{
								identifiers.add(acceptedIdentifier);
							}
							
							newAssertionConsumerServiceIdentifierTypes.put(key, identifiers);
						}

						// Single logout services..
						List<EndpointType> singleLogoutServiceList = spSSODescriptor.getSingleLogoutServices();
						
						List<String> singleLogoutServiceLocationList = new Vector<String>(0,1);

						for (EndpointType singleLogoutService : singleLogoutServiceList)
						{
							String singleLogoutServiceLocation = singleLogoutService.getLocation();
							
							this.logger.trace(MessageFormat.format(Messages.getString("MetadataUpdateMonitor.24"), singleLogoutServiceLocation));  //$NON-NLS-1$

							singleLogoutServiceLocationList.add(singleLogoutServiceLocation);
						}
						
						newSingleLogoutServices.put(entityDescriptor.getEntityID(), singleLogoutServiceLocationList);
						
						// Cache clear services..
						Extensions extensions = spSSODescriptor.getExtensions();
						if(extensions != null)
						{
							List<Element> extensionElements = extensions.getImplementedExtensions();
							
							Map<Integer,String> cacheClearServiceLocationMap = Collections.synchronizedMap( new HashMap<Integer,String>() );
							for(Element extension : extensionElements)
							{
								String name = extension.getLocalName();
								if(name.equals(this.CACHE_CLEAR_SERVICE_NAME)) 
								{
									CacheClearService cacheClearService = null;
									try
									{
										cacheClearService = this.cacheClearServiceUnmarshaller.unMarshallUnSigned(extension);
									}
									catch (UnmarshallerException e)
									{
										// The only error here would be that there is no <CacheClearService> element to unmarshal, and so
										// we can safely ignore this error. It should never happen though, since the check was made above.
									}
									
									if(cacheClearService != null)
									{
										String location = cacheClearService.getLocation();
										cacheClearServiceLocationMap.put( Integer.valueOf( cacheClearService.getIndex() ), location );
										this.logger.trace(MessageFormat.format(Messages.getString("MetadataUpdateMonitor.25"), location)); //$NON-NLS-1$
									}
								}
							}
							
							newCacheClearServices.put(entityDescriptor.getEntityID(), cacheClearServiceLocationMap);
						}
					}
				}
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoe.MonitorThread#shutdown()
	 */
	public void shutdown()
	{
		this.setRunning(false);
		
		this.interrupt();
	}
	
	/*
	 * 
	 */
	protected synchronized boolean isRunning()
	{
		return this.running;
	}
	
	/*
	 * 
	 */
	protected synchronized void setRunning(boolean running)
	{
		this.running = running;
	}	
	
	/*
	 * 
	 */
	private String generateKey(String id, int index)
	{
		return Integer.toString(index) + ":" + id; //$NON-NLS-1$
	}
	
	/*
	 * 
	 */
	private class RawMetadata
	{
		/**
		 * Default constructor
		 */
		public RawMetadata()
		{
			// nothing todo?
		}

		protected String hashValue;
		protected byte[] data;
	}
	
}
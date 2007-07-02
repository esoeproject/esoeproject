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
 * Creation Date: 22/11/2006
 * 
 * Purpose: Implements the Metadata interface.
 */
package com.qut.middleware.spep.metadata.impl;

import java.security.PublicKey;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import com.qut.middleware.saml2.exception.UnmarshallerException;
import com.qut.middleware.saml2.handler.Unmarshaller;
import com.qut.middleware.saml2.handler.impl.UnmarshallerImpl;
import com.qut.middleware.saml2.schemas.metadata.AttributeAuthorityDescriptor;
import com.qut.middleware.saml2.schemas.metadata.EndpointType;
import com.qut.middleware.saml2.schemas.metadata.EntitiesDescriptor;
import com.qut.middleware.saml2.schemas.metadata.EntityDescriptor;
import com.qut.middleware.saml2.schemas.metadata.Extensions;
import com.qut.middleware.saml2.schemas.metadata.IDPSSODescriptor;
import com.qut.middleware.saml2.schemas.metadata.IndexedEndpointType;
import com.qut.middleware.saml2.schemas.metadata.RoleDescriptorType;
import com.qut.middleware.saml2.schemas.metadata.SPSSODescriptor;
import com.qut.middleware.saml2.schemas.metadata.extensions.SPEPStartupService;
import com.qut.middleware.saml2.schemas.metadata.lxacml.LXACMLPDPDescriptor;
import com.qut.middleware.saml2.sec.KeyData;
import com.qut.middleware.spep.ConfigurationConstants;
import com.qut.middleware.spep.exception.InvalidSAMLDataException;
import com.qut.middleware.spep.metadata.Messages;
import com.qut.middleware.spep.metadata.Metadata;

/** Implements the Metadata interface. */
public class MetadataImpl implements Metadata
{
	protected static int BUFFER_LEN = 4096;
	
	protected String metadataUrl;
	protected String currentRevision;
	private String spepIdentifier;
	private String esoeIdentifier;
	private int nodeID;
	
	// This could be done better with a different kind of lock.
	protected ReentrantLock lock;
	private AtomicBoolean hasData;
	protected AtomicBoolean hasError;

	private Map<String, KeyData> keyMap;
	private List<String> singleSignOnEndpoints;
	private List<String> attributeServiceEndpoints;
	private List<String> authzServiceEndpoints;
	private List<String> spepStartupServiceEndpoints;
	private EndpointType assertionConsumerServiceLocation;
	
	private SecureRandom secureRandom;
	private String[] extensionSchemas;
	private Unmarshaller<LXACMLPDPDescriptor> lxacmlPDPDescriptorUnmarshaller;
	private String[] spepStartupSchemas;
	private Unmarshaller<SPEPStartupService> spepStartupServiceUnmarshaller;
	private Thread metadataThread;
	protected PublicKey metadataPublicKey;
	
	private final String UNMAR_PKGNAMES = LXACMLPDPDescriptor.class.getPackage().getName();
	private final String UNMAR_PKGNAMES2 = SPEPStartupService.class.getPackage().getName();
	
	/* Local logging instance */
	private Logger logger = Logger.getLogger(MetadataImpl.class.getName());

	/**
	 * Constructor
	 * @param spepIdentifier The SPEP's identifier in the metadata.
	 * @param esoeIdentifier The ESOE's identifier in the metadata.
	 * @param metadataUrl The URL from which to retrieve metadata.
	 * @param metadataPublicKey The public key for validating the metadata document.
	 * @param interval The interval (seconds) on which to re-read the metadata.
	 */
	public MetadataImpl(String spepIdentifier, String esoeIdentifier, String metadataUrl, PublicKey metadataPublicKey, int nodeID, int interval)
	{
		if(spepIdentifier == null)
		{
			throw new IllegalArgumentException(Messages.getString("MetadataImpl.20"));  //$NON-NLS-1$
		}
		if(esoeIdentifier == null)
		{
			throw new IllegalArgumentException(Messages.getString("MetadataImpl.21"));  //$NON-NLS-1$
		}
		if(metadataUrl == null)
		{
			throw new IllegalArgumentException(Messages.getString("MetadataImpl.22"));  //$NON-NLS-1$
		}
		if(metadataPublicKey == null)
		{
			throw new IllegalArgumentException(Messages.getString("MetadataImpl.23"));  //$NON-NLS-1$
		}
		if(interval <= 0 || interval > Long.MAX_VALUE/1000)
		{
			throw new IllegalArgumentException(Messages.getString("MetadataImpl.24") + Integer.MAX_VALUE);  //$NON-NLS-1$
		}
		if(nodeID < 0 || nodeID > Integer.MAX_VALUE)
			throw new IllegalArgumentException("nodeID must be between 0 and " + Integer.MAX_VALUE);
		
		this.esoeIdentifier = esoeIdentifier;
		this.spepIdentifier = spepIdentifier;
		this.metadataUrl = metadataUrl;
		this.metadataPublicKey = metadataPublicKey;
		this.nodeID = nodeID;
		
		this.secureRandom = new SecureRandom();
		
		this.lock = new ReentrantLock();
		this.hasData = new AtomicBoolean(false);
		this.hasError = new AtomicBoolean(false);
				
		this.extensionSchemas = new String[]{ConfigurationConstants.lxacmlMetadata, ConfigurationConstants.samlMetadata};
		try
		{
			this.lxacmlPDPDescriptorUnmarshaller = new UnmarshallerImpl<LXACMLPDPDescriptor>(this.UNMAR_PKGNAMES, this.extensionSchemas);
		}
		catch (UnmarshallerException e)
		{
			this.logger.error(Messages.getString("MetadataImpl.0")); //$NON-NLS-1$
			throw new UnsupportedOperationException(Messages.getString("MetadataImpl.1"), e); //$NON-NLS-1$
		}
		
		this.spepStartupSchemas = new String[]{ConfigurationConstants.spepStartupService};

		try
		{
			this.spepStartupServiceUnmarshaller = new UnmarshallerImpl<SPEPStartupService>(this.UNMAR_PKGNAMES2, this.spepStartupSchemas);
		}
		catch (UnmarshallerException e)
		{
			this.logger.error(Messages.getString("MetadataImpl.0")); //$NON-NLS-1$
			throw new UnsupportedOperationException(Messages.getString("MetadataImpl.1"), e); //$NON-NLS-1$
		}
		
		this.metadataThread = new MetadataThread(this, interval);
		this.logger.debug(Messages.getString("MetadataImpl.17")); //$NON-NLS-1$
		this.metadataThread.start();
		
		this.logger.info(MessageFormat.format(Messages.getString("MetadataImpl.18"), spepIdentifier, esoeIdentifier, metadataUrl, Integer.toString(interval))); //$NON-NLS-1$
	}

	
	/* (non-Javadoc)
	 * @see com.qut.middleware.spep.metadata.Metadata#getSPEPIdentifier()
	 */
	public String getSPEPIdentifier()
	{
		return this.spepIdentifier;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.spep.metadata.Metadata#getESOEIdentifier()
	 */
	public String getESOEIdentifier()
	{
		return this.esoeIdentifier;
	}

	
	/* (non-Javadoc)
	 * @see com.qut.middleware.saml2.ExternalKeyResolver#resolveKey(java.lang.String)
	 */
	public PublicKey resolveKey(String keyName)
	{
		waitForData();
		this.lock();
		try
		{
			KeyData keyData = this.keyMap.get(keyName);
			
			if(keyData == null)
			{
				return null;
			}
			
			return keyData.getPk();
		}
		finally
		{
			this.unlock();
		}
	}
	
	/* Lock the internal metadata cache object.
	 * 
	 */
	protected void lock()
	{
		this.lock.lock();
	}
	
	/* Unlock the internal metadata cache object.
	 * 
	 */
	protected void unlock()
	{
		this.lock.unlock();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.spep.metadata.Metadata#getSingleSignOnEndpoint()
	 */
	public String getSingleSignOnEndpoint()
	{
		waitForData();
		this.lock();
		try
		{
			int size = this.singleSignOnEndpoints.size();
			if (size == 1) 
			{
				return this.singleSignOnEndpoints.get(0);
			}
			return this.singleSignOnEndpoints.get(this.secureRandom.nextInt(size - 1));
		}
		finally
		{
			this.unlock();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.spep.metadata.Metadata#getSingleLogoutEndpoint()
	 */
	public String getSingleLogoutEndpoint()
	{
		throw new UnsupportedOperationException();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.spep.metadata.Metadata#getAttributeServiceEndpoint()
	 */
	public String getAttributeServiceEndpoint()
	{
		waitForData();
		this.lock();
		try
		{
			int size = this.attributeServiceEndpoints.size();
			if (size == 1) 
			{
				return this.attributeServiceEndpoints.get(0);
			}
			return this.attributeServiceEndpoints.get(this.secureRandom.nextInt(this.attributeServiceEndpoints.size() - 1));
		}
		finally
		{
			this.unlock();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.spep.metadata.Metadata#getAuthzServiceEndpoint()
	 */
	public String getAuthzServiceEndpoint()
	{
		waitForData();
		this.lock();
		try
		{
			int size = this.authzServiceEndpoints.size();
			if (size == 1) 
			{
				return this.authzServiceEndpoints.get(0);
			}
			return this.authzServiceEndpoints.get(this.secureRandom.nextInt(size - 1));
		}
		finally
		{
			this.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.spep.metadata.Metadata#getSPEPStartupServiceEndpoint()
	 */
	public String getSPEPStartupServiceEndpoint()
	{
		waitForData();
		this.lock();
		try
		{
			int size = this.spepStartupServiceEndpoints.size();
			if (size == 1) 
			{
				return this.spepStartupServiceEndpoints.get(0);
			}
			return this.spepStartupServiceEndpoints.get(this.secureRandom.nextInt(size - 1));
		}
		finally
		{
			this.unlock();
		}
	}
	
		
	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.spep.metadata.Metadata#getassertionConsumerServiceLocation()
	 */
	public String getSPEPAssertionConsumerLocation() 
	{
		waitForData();
		this.lock();
		try
		{
			return this.assertionConsumerServiceLocation.getLocation();
		}
		finally
		{
			this.unlock();
		}
	}


	/* Wait until there is metadata. 
	 * 
	 */
	private void waitForData()
	{
		while(!this.hasData.get() && !this.hasError.get())
		{
			try
			{
				Thread.sleep(20);
			}
			catch (InterruptedException e)
			{
				// No action
			}
		}
		if (this.hasError.get())
		{
			throw new IllegalStateException(Messages.getString("MetadataImpl.2")); //$NON-NLS-1$
		}
	}

	/* Rebuild the metadata cache with the given values.
	 * 
	 */
	protected void rebuildCache(EntitiesDescriptor entitiesDescriptor, String hashValue, Map<String, KeyData> newKeyMap) throws InvalidSAMLDataException, UnmarshallerException
	{
		this.logger.debug(MessageFormat.format(Messages.getString("MetadataImpl.15"), hashValue)); //$NON-NLS-1$
		
		List<String> newSingleSignOnEndpoints = new Vector<String>(0,1);
		List<String> newSingleLogoutEndpoints = new Vector<String>(0,1);
		List<String> newAttributeServiceEndpoints = new Vector<String>(0,1);
		List<String> newAuthzServiceEndpoints = new Vector<String>(0,1);
		List<String> newSPEPStartupServiceEndpoints = new Vector<String>(0,1);
		EndpointType newSPEPAssertionConsumerLocation = null;
		
		ESOERoles esoeRoles = new ESOERoles();
		SPEPRoles spepRoles = new SPEPRoles();
		
		buildCacheRecurse(entitiesDescriptor, esoeRoles, spepRoles);
		
		for (EndpointType endpoint : esoeRoles.idpSSODescriptor.getSingleSignOnServices())
		{
			newSingleSignOnEndpoints.add(endpoint.getLocation());
		}
		for (EndpointType endpoint : esoeRoles.idpSSODescriptor.getSingleLogoutServices())
		{
			newSingleLogoutEndpoints.add(endpoint.getLocation());
		}
		for (EndpointType endpoint : esoeRoles.attributeAuthorityDescriptor.getAttributeServices())
		{
			newAttributeServiceEndpoints.add(endpoint.getLocation());
		}
		for (EndpointType endpoint : esoeRoles.lxacmlPDPDescriptor.getAuthzServices())
		{
			newAuthzServiceEndpoints.add(endpoint.getLocation());
		}

		newSPEPAssertionConsumerLocation = spepRoles.assertionConsumerLocation;		
		
		Extensions idpExtensions = esoeRoles.idpSSODescriptor.getExtensions();
		if (idpExtensions != null)
		{
			for (Element extension : idpExtensions.getImplementedExtensions())
			{
				if (extension.getLocalName().equals("SPEPStartupService")) //$NON-NLS-1$
				{
					SPEPStartupService spepStartupService = this.spepStartupServiceUnmarshaller.unMarshallUnSigned(extension);
					newSPEPStartupServiceEndpoints.add(spepStartupService.getLocation());
				}
			}
		}
		
		boolean error = false;
		if (newSingleSignOnEndpoints.size() == 0)
		{
			this.logger.error(Messages.getString("MetadataImpl.3")); //$NON-NLS-1$
			error = true;
		}
		if (newAttributeServiceEndpoints.size() == 0)
		{
			this.logger.error(Messages.getString("MetadataImpl.5")); //$NON-NLS-1$
			error = true;
		}
		if (newAuthzServiceEndpoints.size() == 0)
		{
			this.logger.error(Messages.getString("MetadataImpl.6")); //$NON-NLS-1$
			error = true;
		}
		if (newSPEPStartupServiceEndpoints.size() == 0)
		{
			this.logger.error(Messages.getString("MetadataImpl.14")); //$NON-NLS-1$
			error = true;
		}
		if (newSPEPAssertionConsumerLocation == null)
		{
			this.logger.error(Messages.getString("MetadataImpl.25")); //$NON-NLS-1$
			error = true;
		}
		
		if (error)
		{
			throw new InvalidSAMLDataException(Messages.getString("MetadataImpl.7")); //$NON-NLS-1$
		}
		
		this.lock.lock();
		try
		{
			this.currentRevision = hashValue;
			this.keyMap = newKeyMap;
			this.singleSignOnEndpoints = newSingleSignOnEndpoints;
			this.attributeServiceEndpoints = newAttributeServiceEndpoints;
			this.authzServiceEndpoints = newAuthzServiceEndpoints;
			this.spepStartupServiceEndpoints = newSPEPStartupServiceEndpoints;
			this.assertionConsumerServiceLocation = newSPEPAssertionConsumerLocation;
			this.hasData.set(true);
			this.logger.debug(Messages.getString("MetadataImpl.16")); //$NON-NLS-1$
		}
		finally
		{
			this.lock.unlock();
		}
	}
	
	private void buildCacheRecurse(EntitiesDescriptor entitiesDescriptor, ESOERoles esoeRoles, SPEPRoles spepRoles) throws InvalidSAMLDataException, UnmarshallerException
	{
		List<Object> descriptorList = entitiesDescriptor.getEntitiesDescriptorsAndEntityDescriptors();

		// Go through the list of descriptors
		// We will encounter both <EntityDescriptor> and <EntitiesDescriptor> objects. We will recurse for the latter. 
		for (Object descriptor : descriptorList)
		{			
			if (descriptor instanceof EntityDescriptor)
			{
				EntityDescriptor entityDescriptor = (EntityDescriptor) descriptor;
				
				this.logger.debug(MessageFormat.format(Messages.getString("MetadataImpl.26"), this.esoeIdentifier, entityDescriptor.getEntityID()) ); //$NON-NLS-1$
				
				List<RoleDescriptorType> roleDescriptorList = entityDescriptor.getIDPDescriptorAndSSODescriptorAndRoleDescriptors();

				// Go through the RoleDescriptors. This includes IDP and SSO descriptors
				for (RoleDescriptorType roleDescriptor : roleDescriptorList)
				{
					
					// only check the entity that matches the ESOE this SPEP wishes to use.
					if (entityDescriptor.getEntityID().equals(this.esoeIdentifier))
					{					
							if (roleDescriptor instanceof IDPSSODescriptor)
							{
								esoeRoles.idpSSODescriptor = (IDPSSODescriptor)roleDescriptor;
								this.logger.debug(MessageFormat.format(Messages.getString("MetadataImpl.27"), esoeRoles.idpSSODescriptor)); //$NON-NLS-1$
							}
							else if (roleDescriptor instanceof AttributeAuthorityDescriptor)
							{
								esoeRoles.attributeAuthorityDescriptor = (AttributeAuthorityDescriptor)roleDescriptor;
							}
							else if (roleDescriptor instanceof LXACMLPDPDescriptor)
							{
								esoeRoles.lxacmlPDPDescriptor = (LXACMLPDPDescriptor)roleDescriptor;
							}	
							
							Extensions extensions = entityDescriptor.getExtensions();
							if (extensions != null)
							{
								for (Element extensionElement : extensions.getImplementedExtensions())
								{
									if (extensionElement.getLocalName().equals("LXACMLPDPDescriptor")) //$NON-NLS-1$
									{
										try
										{
											esoeRoles.lxacmlPDPDescriptor = this.lxacmlPDPDescriptorUnmarshaller.unMarshallUnSigned(extensionElement);
										}
										catch(Exception e)
										{
											e.printStackTrace();
										}
									}
									
								}
							}
					}					
					else // if the entity has roles for this SPEP, gather required service locations
					{	
						// we also need to know the assertionConsumer and attributeConsumer service locations for this SPEP
						if(roleDescriptor instanceof SPSSODescriptor)
						{
							SPSSODescriptor spepDescriptor = (SPSSODescriptor)roleDescriptor;
							
							this.logger.debug(Messages.getString("MetadataImpl.28") + roleDescriptor.getID()); //$NON-NLS-1$
							
							if(roleDescriptor.getID().equals(this.spepIdentifier))
							{
								this.logger.debug(Messages.getString("MetadataImpl.29")); //$NON-NLS-1$
							
								// find the endpoint index that matches this spep node ID
								List<IndexedEndpointType> endpoints = spepDescriptor.getAssertionConsumerServices();
								
								for(IndexedEndpointType endpoint : endpoints)
								{
									if(endpoint.getIndex() == this.nodeID)
										spepRoles.assertionConsumerLocation = endpoint;											
								}
								
							}
						}
					}
				}				
				
			}
			else if (descriptor instanceof EntitiesDescriptor)
			{
				buildCacheRecurse(entitiesDescriptor, esoeRoles, spepRoles);
			}
		}
		
		boolean error = false;
		if (esoeRoles.idpSSODescriptor == null)
		{
			this.logger.error(Messages.getString("MetadataImpl.9")); //$NON-NLS-1$
			error = true;
		}
		if (esoeRoles.attributeAuthorityDescriptor == null)
		{
			this.logger.error(Messages.getString("MetadataImpl.10")); //$NON-NLS-1$
			error = true;
		}
		if (esoeRoles.lxacmlPDPDescriptor == null)
		{
			this.logger.error(Messages.getString("MetadataImpl.12")); //$NON-NLS-1$
			error = true;
		}
		if (error)
		{
			throw new InvalidSAMLDataException(Messages.getString("MetadataImpl.13")); //$NON-NLS-1$
		}
		
		return;
	}
	
	private class ESOERoles
	{
		IDPSSODescriptor idpSSODescriptor = null;
		AttributeAuthorityDescriptor attributeAuthorityDescriptor = null;
		LXACMLPDPDescriptor lxacmlPDPDescriptor = null;
		
		protected ESOERoles()
		{
			// Explicitly definining this for visibility reasons
		}
	}
	
	private class SPEPRoles
	{
		IndexedEndpointType assertionConsumerLocation = null;
		
		protected SPEPRoles()
		{
			// Explicitly definining this for visibility reasons
		}
	}
}

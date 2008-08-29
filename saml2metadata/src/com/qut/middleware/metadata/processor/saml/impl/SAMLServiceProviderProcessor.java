/*
 * Copyright 2008, Queensland University of Technology
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
 * Creation Date: 17/04/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.metadata.processor.saml.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3._2000._09.xmldsig_.KeyInfo;
import org.w3c.dom.Element;

import com.qut.middleware.metadata.bean.impl.EntityDataImpl;
import com.qut.middleware.metadata.bean.saml.SPEPRole;
import com.qut.middleware.metadata.bean.saml.ServiceProviderRole;
import com.qut.middleware.metadata.bean.saml.attribute.AttributeConsumingService;
import com.qut.middleware.metadata.bean.saml.attribute.RequestedAttribute;
import com.qut.middleware.metadata.bean.saml.attribute.impl.AttributeConsumingServiceImpl;
import com.qut.middleware.metadata.bean.saml.attribute.impl.RequestedAttributeImpl;
import com.qut.middleware.metadata.bean.saml.endpoint.EndpointCollection;
import com.qut.middleware.metadata.bean.saml.endpoint.IndexedEndpointCollection;
import com.qut.middleware.metadata.bean.saml.endpoint.impl.EndpointCollectionImpl;
import com.qut.middleware.metadata.bean.saml.endpoint.impl.EndpointImpl;
import com.qut.middleware.metadata.bean.saml.endpoint.impl.IndexedEndpointCollectionImpl;
import com.qut.middleware.metadata.bean.saml.endpoint.impl.IndexedEndpointImpl;
import com.qut.middleware.metadata.bean.saml.impl.SPEPRoleImpl;
import com.qut.middleware.metadata.bean.saml.impl.ServiceProviderRoleImpl;
import com.qut.middleware.metadata.exception.InvalidMetadataException;
import com.qut.middleware.metadata.processor.saml.SAMLEntityDescriptorProcessor;
import com.qut.middleware.saml2.SchemaConstants;
import com.qut.middleware.saml2.exception.UnmarshallerException;
import com.qut.middleware.saml2.handler.Unmarshaller;
import com.qut.middleware.saml2.handler.impl.UnmarshallerImpl;
import com.qut.middleware.saml2.schemas.metadata.EndpointType;
import com.qut.middleware.saml2.schemas.metadata.EntityDescriptor;
import com.qut.middleware.saml2.schemas.metadata.Extensions;
import com.qut.middleware.saml2.schemas.metadata.IndexedEndpointType;
import com.qut.middleware.saml2.schemas.metadata.KeyDescriptor;
import com.qut.middleware.saml2.schemas.metadata.LocalizedNameType;
import com.qut.middleware.saml2.schemas.metadata.RoleDescriptorType;
import com.qut.middleware.saml2.schemas.metadata.SPSSODescriptor;
import com.qut.middleware.saml2.schemas.metadata.extensions.CacheClearService;

public class SAMLServiceProviderProcessor implements SAMLEntityDescriptorProcessor
{
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private String[] cacheClearSchemas = new String[]{SchemaConstants.cacheClearService, SchemaConstants.samlMetadata};
	private Unmarshaller<CacheClearService> cacheClearServiceUnmarshaller;
	
	public SAMLServiceProviderProcessor()
	{
		try
		{
			this.cacheClearServiceUnmarshaller = new UnmarshallerImpl<CacheClearService>(CacheClearService.class.getPackage().getName(), this.cacheClearSchemas);
		}
		catch (UnmarshallerException e)
		{
			this.logger.error("Cache clear service unmarshaller could not be initialized. Failed to construct SAMLServiceProviderProcessor correctly. Error was: " + e.getMessage());
			throw new UnsupportedOperationException("Cache clear service unmarshaller could not be initialized. Failed to construct SAMLServiceProviderProcessor correctly. Error was: " + e.getMessage(), e);
		}
	}

	public void process(EntityDataImpl entityData, EntityDescriptor entityDescriptor) throws InvalidMetadataException
	{
		// Go through the RoleDescriptors. This includes IDP and SSO descriptors
		for (RoleDescriptorType roleDescriptor : entityDescriptor.getIDPDescriptorAndSSODescriptorAndRoleDescriptors())
		{
			// If we have an SPSSODescriptor, populate the caches with the information inside.
			if (roleDescriptor instanceof SPSSODescriptor)
			{
				SPSSODescriptor spSSODescriptor = (SPSSODescriptor) roleDescriptor;

				List<String> keyNames = keyNamesFromRoleDescriptor(spSSODescriptor);
				List<String> nameIDFormatList = nameIDFormatsFromSPSSODescriptor(spSSODescriptor);
				IndexedEndpointCollection assertionConsumerServiceEndpoints = assertionConsumerServiceEndpointsFromDescriptor(spSSODescriptor, entityData.getRandom());
				EndpointCollection singleLogoutServiceEndpoints = singleLogoutServiceEndpointsFromDescriptor(spSSODescriptor, entityData.getRandom());
				IndexedEndpointCollection cacheClearServiceEndpoints = cacheClearServiceEndpointsFromDescriptor(spSSODescriptor, entityData.getRandom());
				Map<Integer, AttributeConsumingService> attributeConsumingServices = new TreeMap<Integer, AttributeConsumingService>();
				
				if (spSSODescriptor.getAttributeConsumingServices().size() > 0)
				{
					for (com.qut.middleware.saml2.schemas.metadata.AttributeConsumingService attributeConsumingService : spSSODescriptor.getAttributeConsumingServices())
					{
						int index = attributeConsumingService.getIndex();
						boolean isDefault = attributeConsumingService.isIsDefault();
						List<RequestedAttribute> requestedAttributes = new ArrayList<RequestedAttribute>();
						for (com.qut.middleware.saml2.schemas.metadata.RequestedAttribute requested : attributeConsumingService.getRequestedAttributes())
						{
							String name = requested.getName();
							String nameFormat = requested.getNameFormat();
							String friendlyName = requested.getFriendlyName();
							boolean required = requested.isIsRequired();
							
							requestedAttributes.add(new RequestedAttributeImpl(name, nameFormat, friendlyName, required));
						}
						
						List<String> serviceNames = new ArrayList<String>();
						for (LocalizedNameType name : attributeConsumingService.getServiceNames())
						{
							serviceNames.add(name.getValue());
						}
						List<String> serviceDescriptions = new ArrayList<String>();
						for (LocalizedNameType description : attributeConsumingService.getServiceDescriptions())
						{
							serviceDescriptions.add(description.getValue());
						}
						
						attributeConsumingServices.put(index, new AttributeConsumingServiceImpl(index, isDefault, requestedAttributes, serviceDescriptions, serviceNames));
					}
				}
				
				// If there are no cache clear service endpoints, it's not an SPEP
				if (cacheClearServiceEndpoints.getEndpointList().size() == 0)
				{
					this.logger.debug("Identified entity as a SAML service provider. Entity ID: " + entityDescriptor.getEntityID());
					ServiceProviderRole spRole = new ServiceProviderRoleImpl(keyNames, nameIDFormatList, assertionConsumerServiceEndpoints, singleLogoutServiceEndpoints, attributeConsumingServices);
					entityData.addRoleData(spRole);
				}
				else
				{
					// Check that it has all the required endpoints
					if (assertionConsumerServiceEndpoints.getEndpointList().size() == 0)
					{
						this.logger.error("SPEP did not have an assertion consumer service endpoint. Entity ID: " + entityData.getEntityID());
						throw new InvalidMetadataException("SPEP did not have an assertion consumer service endpoint. Entity ID: " + entityData.getEntityID());
					}
					if (singleLogoutServiceEndpoints.getEndpointList().size() == 0)
					{
						this.logger.error("SPEP did not have a single logout endpoint. Entity ID: " + entityData.getEntityID());
						throw new InvalidMetadataException("SPEP did not have a single logout endpoint. Entity ID: " + entityData.getEntityID());
					}
					
					this.logger.debug("Identified entity as an SPEP (or SPEP compatible). Entity ID: " + entityDescriptor.getEntityID());
					SPEPRole spepRole = new SPEPRoleImpl(keyNames, nameIDFormatList, assertionConsumerServiceEndpoints, singleLogoutServiceEndpoints, attributeConsumingServices, cacheClearServiceEndpoints);
					entityData.addRoleData(spepRole);
				}
			}
		}
	}

	private List<String> keyNamesFromRoleDescriptor(RoleDescriptorType roleDescriptor) throws InvalidMetadataException
	{
		List<String> keyNames = new ArrayList<String>();
		
		for (KeyDescriptor key : roleDescriptor.getKeyDescriptors())
		{
			KeyInfo keyInfo = key.getKeyInfo();
			if (keyInfo != null)
			{
				keyNames.add(keyInfo.getId());
			}
			else
			{
				throw new InvalidMetadataException("KeyDescriptor present but KeyInfo was null!");
			}
		}

		return keyNames;
	}

	private List<String> nameIDFormatsFromSPSSODescriptor(SPSSODescriptor spSSODescriptor)
	{
		List<String> nameIDFormats = new ArrayList<String>();

		for (String acceptedIdentifier : spSSODescriptor.getNameIDFormats())
		{
			nameIDFormats.add(acceptedIdentifier);
		}

		return nameIDFormats;
	}

	private IndexedEndpointCollection assertionConsumerServiceEndpointsFromDescriptor(SPSSODescriptor spSSODescriptor, Random random)
	{
		IndexedEndpointCollection indexedEndpoints = new IndexedEndpointCollectionImpl(random);

		for (IndexedEndpointType assertionConsumerService : spSSODescriptor.getAssertionConsumerServices())
		{
			String location = assertionConsumerService.getLocation();
			String binding = assertionConsumerService.getBinding();
			int index = assertionConsumerService.getIndex();

			indexedEndpoints.getEndpointList().add(new IndexedEndpointImpl(binding, location, index));
		}

		return indexedEndpoints;
	}

	private EndpointCollection singleLogoutServiceEndpointsFromDescriptor(SPSSODescriptor spSSODescriptor, Random random)
	{
		EndpointCollection endpoints = new EndpointCollectionImpl(random);

		for (EndpointType singleLogoutService : spSSODescriptor.getSingleLogoutServices())
		{
			String location = singleLogoutService.getLocation();
			String binding = singleLogoutService.getBinding();

			endpoints.getEndpointList().add(new EndpointImpl(binding, location));
		}

		return endpoints;
	}

	private IndexedEndpointCollection cacheClearServiceEndpointsFromDescriptor(SPSSODescriptor spSSODescriptor, Random random) throws InvalidMetadataException
	{
		IndexedEndpointCollection indexedEndpointCollection = new IndexedEndpointCollectionImpl(random);
		
		Extensions extensions = spSSODescriptor.getExtensions();
		if (extensions != null)
		{
			try
			{
				for (Element extension : extensions.getImplementedExtensions())
				{
					if (extension.getLocalName().equals("CacheClearService"))
					{
						CacheClearService cacheClearService = null;
						cacheClearService = this.cacheClearServiceUnmarshaller.unMarshallUnSigned(extension);

						if (cacheClearService != null)
						{
							String binding = cacheClearService.getBinding();
							String location = cacheClearService.getLocation();
							int index = cacheClearService.getIndex();
							indexedEndpointCollection.getEndpointList().add(new IndexedEndpointImpl(binding, location, index));
						}
					}
				}
			}
			catch (UnmarshallerException e)
			{
				this.logger.error("Unable to unmarshal Cache Clear Service element. Error was: " + e.getMessage());
				this.logger.debug("Unable to unmarshal Cache Clear Service element. Exception follows", e);
				throw new InvalidMetadataException("Unable to unmarshal SPEP Startup Service element. Error was: " + e.getMessage());
			}
		}
		
		return indexedEndpointCollection;
	}
}

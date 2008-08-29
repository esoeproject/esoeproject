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
 * Creation Date: 14/04/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.metadata.processor.saml.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.bind.JAXBElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3._2000._09.xmldsig_.KeyInfo;
import org.w3._2000._09.xmldsig_.X509Data;
import org.w3._2000._09.xmldsig_.X509IssuerSerialType;

import com.qut.middleware.metadata.bean.EntityData;
import com.qut.middleware.metadata.bean.KeyEntry;
import com.qut.middleware.metadata.bean.impl.EntityDataImpl;
import com.qut.middleware.metadata.bean.impl.KeyEntryImpl;
import com.qut.middleware.metadata.cache.MetadataCache;
import com.qut.middleware.metadata.constants.FormatConstants;
import com.qut.middleware.metadata.exception.InvalidMetadataException;
import com.qut.middleware.metadata.processor.FormatHandler;
import com.qut.middleware.metadata.processor.saml.SAMLEntityDescriptorProcessor;
import com.qut.middleware.metadata.source.DynamicMetadataSource;
import com.qut.middleware.metadata.source.MetadataSource;
import com.qut.middleware.saml2.ExternalKeyResolver;
import com.qut.middleware.saml2.SchemaConstants;
import com.qut.middleware.saml2.exception.ReferenceValueException;
import com.qut.middleware.saml2.exception.SignatureValueException;
import com.qut.middleware.saml2.exception.UnmarshallerException;
import com.qut.middleware.saml2.handler.Unmarshaller;
import com.qut.middleware.saml2.handler.impl.UnmarshallerImpl;
import com.qut.middleware.saml2.schemas.metadata.EntitiesDescriptor;
import com.qut.middleware.saml2.schemas.metadata.EntityDescriptor;
import com.qut.middleware.saml2.schemas.metadata.KeyDescriptor;
import com.qut.middleware.saml2.schemas.metadata.RoleDescriptorType;
import com.qut.middleware.saml2.schemas.metadata.extensions.SPEPStartupService;
import com.qut.middleware.saml2.schemas.metadata.lxacml.LXACMLPDPDescriptor;
import com.qut.middleware.saml2.sec.KeyData;

public class SAMLMetadataFormatHandler implements FormatHandler
{
	private static final String dynamicSourceLocationString = "(dynamic source)";
	private String[] schemas = new String[] { SchemaConstants.samlMetadata, SchemaConstants.lxacmlMetadata, SchemaConstants.spepStartupService, SchemaConstants.cacheClearService };
	private Unmarshaller<EntitiesDescriptor> unmarshaller;
	private List<SAMLEntityDescriptorProcessor> processors;
	private Random random;
	
	private final String unmarshallerPackageNames = 
		LXACMLPDPDescriptor.class.getPackage().getName() + ":" +
		SPEPStartupService.class.getPackage().getName() + ":" +
		EntitiesDescriptor.class.getPackage().getName();
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public SAMLMetadataFormatHandler(List<SAMLEntityDescriptorProcessor> processors)
	{
		this.processors = processors;
		this.random = new Random();
		try
		{
			this.unmarshaller = new UnmarshallerImpl<EntitiesDescriptor>(this.unmarshallerPackageNames, this.schemas);
		}
		catch (UnmarshallerException e)
		{
			this.logger.error("Error instantiating Unmarshaller for SAML metadata handler.", e);
		}
	}
	
	public SAMLMetadataFormatHandler(ExternalKeyResolver keyResolver, List<SAMLEntityDescriptorProcessor> processors)
	{
		this.processors = processors;
		this.random = new Random();
		try
		{
			this.unmarshaller = new UnmarshallerImpl<EntitiesDescriptor>(this.unmarshallerPackageNames, this.schemas, keyResolver);
		}
		catch (UnmarshallerException e)
		{
			this.logger.error("Error instantiating Unmarshaller for SAML metadata handler.", e);
		}
	}
	
	public boolean canHandle(MetadataSource source)
	{
		return source.getFormat().equalsIgnoreCase(FormatConstants.SAML2);
	}

	public void updateCache(MetadataSource source, MetadataCache cache, byte[] document) throws InvalidMetadataException
	{
		List<KeyEntry> keyList = new ArrayList<KeyEntry>();
		List<EntityData> entityList = new ArrayList<EntityData>();
		
		this.processMetadata(source.getLocation(), source.isTrusted(), source.getPriority(), document, entityList, keyList);
		
		cache.update(source, entityList, keyList);
	}
	
	private void processMetadata(String sourceLocation, boolean trusted, int priority, byte[] document, List<EntityData> entityList, List<KeyEntry> keyList) throws InvalidMetadataException
	{
		EntitiesDescriptor entitiesDescriptor = null;
		Map<String,KeyData> keyDataMap = new HashMap<String, KeyData>();
		try
		{
			// TODO unMarshallUnsignedMetadata ? we have a constructor to do this.
			entitiesDescriptor = this.unmarshaller.unMarshallMetadata(document, keyDataMap, trusted);
			if (entitiesDescriptor == null)
			{
				String message = "null value was returned when unmarshalling SAML metadata document. Source location: " + sourceLocation;
				this.logger.error(message);
				throw new InvalidMetadataException(message);
			}
		}
		catch (SignatureValueException e)
		{
			String message = "Signature was deemed to be invalid on SAML metadata document. Source location: " + sourceLocation;
			this.logger.error(message);
			throw new InvalidMetadataException(message, e);
		}
		catch (ReferenceValueException e)
		{
			String message = "Reference value error on SAML metadata document. Source location: " + sourceLocation;
			this.logger.error(message);
			throw new InvalidMetadataException(message, e);
		}
		catch (UnmarshallerException e)
		{
			String message = "SAML metadata document could not be unmarshalled successfully. Source location: " + sourceLocation;
			this.logger.error(message);
			throw new InvalidMetadataException(message, e);
		}
		
		this.processEntitiesDescriptor(sourceLocation, trusted, priority, entityList, entitiesDescriptor, keyDataMap, keyList);
	}

	private void processEntitiesDescriptor(String sourceLocation, boolean trusted, int priority, List<EntityData> entityList, EntitiesDescriptor entitiesDescriptor, Map<String,KeyData> keyDataMap, List<KeyEntry> keyList) throws InvalidMetadataException
	{
		for (Object obj : entitiesDescriptor.getEntitiesDescriptorsAndEntityDescriptors())
		{
			if (obj instanceof EntityDescriptor)
			{
				EntityDescriptor entityDescriptor = (EntityDescriptor)obj;
				processEntityDescriptor(sourceLocation, trusted, priority, entityList, entityDescriptor, keyDataMap, keyList);
			}
			else if (obj instanceof EntitiesDescriptor)
			{
				EntitiesDescriptor childEntitiesDescriptor = (EntitiesDescriptor)obj;
				processEntitiesDescriptor(sourceLocation, trusted, priority, entityList, childEntitiesDescriptor, keyDataMap, keyList);
			}
			else
			{
				this.logger.warn("Object from SAML metadata (source location: " + sourceLocation + ") contained an unrecognized element. The type of the element was: " + obj.getClass().getName());
			}
		}
	}

	private void processEntityDescriptor(String sourceLocation, boolean trusted, int priority, List<EntityData> entityList, EntityDescriptor entityDescriptor, Map<String,KeyData> keyDataMap, List<KeyEntry> keyList) throws InvalidMetadataException
	{
		EntityDataImpl entityData = new EntityDataImpl(sourceLocation, priority);
		entityData.setTrusted(trusted);
		entityData.setEntityID(entityDescriptor.getEntityID());
		entityData.setRandom(this.random);
		
		for (SAMLEntityDescriptorProcessor processor : this.processors)
		{
			processor.process(entityData, entityDescriptor);
		}
		
		for (RoleDescriptorType roleDescriptor : entityDescriptor.getIDPDescriptorAndSSODescriptorAndRoleDescriptors())
		{
			for (KeyDescriptor keyDescriptor : roleDescriptor.getKeyDescriptors())
			{
				KeyInfo keyInfo = keyDescriptor.getKeyInfo();
				List<Object> keyElementList = keyInfo.getContent();
				
				String name = null;
				String issuerDN = null;
				BigInteger serialNumber = null;
				
				for (Object keyElement : keyElementList)
				{
					this.logger.trace("Key element: {}, checking for name/issuer/serial", keyElement.getClass().getName());
					// <KeyName> is actually a JAXBElement<String> .. so we need to check for that.
					if (keyElement instanceof JAXBElement)
					{
						JAXBElement<?> keyName = (JAXBElement<?>) keyElement;
						this.logger.trace("KeyInfo JAXB element: <{}>  Declared type {}, checking for name", keyName.getName().getLocalPart(), keyName.getDeclaredType().getName());
						
						if (keyName.getDeclaredType().equals(String.class))
						{
							// Then make sure it's called "KeyName"
							if (keyName.getName().getLocalPart().equals("KeyName"))
							{
								this.logger.trace("JAXBElement<String> (KeyName) element");
								name = (String)keyName.getValue();
							}
						}
					}
					else if (keyElement instanceof X509Data)
					{
						this.logger.trace("X509Data element, checking for issuer/serial");
						X509Data x509Data = (X509Data) keyElement;
						for (Object x509DataElement : x509Data.getX509DataContent())
						{
							this.logger.trace("X509Data content element: {}, checking for issuer/serial", x509DataElement.getClass().getName());
							// It will most likely come back as a JAXBElement<X509IssuerSerialType>..
							if (x509DataElement instanceof JAXBElement)
							{
								JAXBElement<?> x509IssuerSerialElement = (JAXBElement<?>) x509DataElement;
								this.logger.trace("X509Data JAXB element: <{}>  Declared type: {}, checking for issuer/serial", ((JAXBElement<?>) x509DataElement).getName().getLocalPart(), x509IssuerSerialElement.getDeclaredType());
								
								if (x509IssuerSerialElement.getDeclaredType().equals(X509IssuerSerialType.class))
								{
									this.logger.trace("JAXBElement<X509IssuerSerial> element");
									X509IssuerSerialType x509IssuerSerialType = (X509IssuerSerialType) x509IssuerSerialElement.getValue();
									issuerDN = x509IssuerSerialType.getX509IssuerName();
									serialNumber = x509IssuerSerialType.getX509SerialNumber();
								}
							}
							// But it could come back as just an X509IssuerSerialType
							else if (x509DataElement instanceof X509IssuerSerialType)
							{
								this.logger.trace("X509IssuerSerial element");
								X509IssuerSerialType x509IssuerSerialType = (X509IssuerSerialType) x509DataElement;
								issuerDN = x509IssuerSerialType.getX509IssuerName();
								serialNumber = x509IssuerSerialType.getX509SerialNumber();
							}
						}
					}
				}
				
				// String.valueOf catches null values, so that will save us some checking.
				this.logger.debug("Key obtained from metadata source {}. Name: {} Issuer DN: {} Serial Number: {}",
						new Object[]{sourceLocation, String.valueOf(name), String.valueOf(issuerDN), String.valueOf(serialNumber)});
				
				if (name == null)
				{
					this.logger.error("KeyInfo did not have a key name. Key belongs to entity with ID: " + entityData.getEntityID());
					throw new InvalidMetadataException("KeyInfo did not have a key name. Key belongs to entity with ID: " + entityData.getEntityID());
				}
				
				KeyData keyData = keyDataMap.get(name);
				if (keyData == null)
				{
					this.logger.error("Got null key data for key alias: " + name + "  Key belongs to entity with ID: " + entityData.getEntityID());
					throw new InvalidMetadataException("Got null key data for key alias: " + name + "  Key belongs to entity with ID: " + entityData.getEntityID());
				}
				
				KeyEntry keyEntry = new KeyEntryImpl(sourceLocation, priority, name, issuerDN, serialNumber, entityData.getEntityID(), keyData.getPk());
				keyList.add(keyEntry);
			}
		}
		
		entityList.add(entityData);
	}

	public boolean canHandle(DynamicMetadataSource source)
	{
		return source.getFormat().equalsIgnoreCase(FormatConstants.SAML2);
	}

	public EntityData dynamicUpdateCache(DynamicMetadataSource source, MetadataCache cache, String entityID, byte[] document) throws InvalidMetadataException
	{
		List<KeyEntry> keyList = new ArrayList<KeyEntry>();
		List<EntityData> entityList = new ArrayList<EntityData>();
		
		this.processMetadata(dynamicSourceLocationString, source.isTrusted(entityID), MetadataSource.DEFAULT_PRIORITY, document, entityList, keyList);
		
		// Find the entity ID we're looking for - we don't want to update other ones.
		// TODO .. Or do we? I don't think so.
		EntityData entity = null;
		for (EntityData e : entityList)
		{
			if (e.getEntityID().equals(entityID))
			{
				entity = e;
				break;
			}
		}
		
		// If the entity was found update the cache.
		if (entity != null)
		{
			entityList.clear();
			entityList.add(entity);
			cache.dynamicUpdate(source, entityList, keyList);
		}
		
		return entity;
	}
}

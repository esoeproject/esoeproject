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
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3._2000._09.xmldsig_.KeyInfo;
import org.w3c.dom.Element;

import com.qut.middleware.metadata.bean.impl.EntityDataImpl;
import com.qut.middleware.metadata.bean.saml.IdentityProviderRole;
import com.qut.middleware.metadata.bean.saml.endpoint.EndpointCollection;
import com.qut.middleware.metadata.bean.saml.endpoint.impl.EndpointCollectionImpl;
import com.qut.middleware.metadata.bean.saml.endpoint.impl.EndpointImpl;
import com.qut.middleware.metadata.bean.saml.impl.AttributeAuthorityRoleImpl;
import com.qut.middleware.metadata.bean.saml.impl.ESOERoleImpl;
import com.qut.middleware.metadata.bean.saml.impl.IdentityProviderRoleImpl;
import com.qut.middleware.metadata.bean.saml.impl.TrustedESOERoleImpl;
import com.qut.middleware.metadata.exception.InvalidMetadataException;
import com.qut.middleware.metadata.processor.saml.SAMLEntityDescriptorProcessor;
import com.qut.middleware.saml2.SchemaConstants;
import com.qut.middleware.saml2.exception.UnmarshallerException;
import com.qut.middleware.saml2.handler.Unmarshaller;
import com.qut.middleware.saml2.handler.impl.UnmarshallerImpl;
import com.qut.middleware.saml2.schemas.metadata.AttributeAuthorityDescriptor;
import com.qut.middleware.saml2.schemas.metadata.EndpointType;
import com.qut.middleware.saml2.schemas.metadata.EntityDescriptor;
import com.qut.middleware.saml2.schemas.metadata.Extensions;
import com.qut.middleware.saml2.schemas.metadata.IDPSSODescriptor;
import com.qut.middleware.saml2.schemas.metadata.KeyDescriptor;
import com.qut.middleware.saml2.schemas.metadata.RoleDescriptorType;
import com.qut.middleware.saml2.schemas.metadata.extensions.SPEPStartupService;
import com.qut.middleware.saml2.schemas.metadata.lxacml.LXACMLPDPDescriptor;

public class SAMLIdentityProviderProcessor implements SAMLEntityDescriptorProcessor
{
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private Unmarshaller<LXACMLPDPDescriptor> lxacmlPDPDescriptorUnmarshaller;
	private Unmarshaller<SPEPStartupService> spepStartupServiceUnmarshaller;

	private String trustedEntityID;

	public SAMLIdentityProviderProcessor(String trustedESOEEntityID)
	{
		this.logger.debug("Constructed SAMLIdentityProviderProcessor (Trusted ESOE Entity ID: {})", trustedESOEEntityID);
		this.trustedEntityID = trustedESOEEntityID;
		
		String[] extensionSchemas = new String[] { SchemaConstants.lxacmlMetadata, SchemaConstants.samlMetadata };
		try
		{
			this.lxacmlPDPDescriptorUnmarshaller = new UnmarshallerImpl<LXACMLPDPDescriptor>(LXACMLPDPDescriptor.class.getPackage().getName(), extensionSchemas);
		}
		catch (UnmarshallerException e)
		{
			this.logger.error("LXACML PDP descriptor unmarshaller could not be initialized. Failed to construct SAMLIdentityProviderProcessor correctly. Error was: " + e.getMessage());
			throw new UnsupportedOperationException("LXACML PDP descriptor unmarshaller could not be initialized. Failed to construct SAMLIdentityProviderProcessor correctly. Error was: " + e.getMessage(), e);
		}

		String[] spepStartupSchemas = new String[] { SchemaConstants.spepStartupService };

		try
		{
			this.spepStartupServiceUnmarshaller = new UnmarshallerImpl<SPEPStartupService>(SPEPStartupService.class.getPackage().getName(), spepStartupSchemas);
		}
		catch (UnmarshallerException e)
		{
			this.logger.error("SPEP Startup Service unmarshaller could not be initialized. Failed to construct SAMLIdentityProviderProcessor correctly. Error was: " + e.getMessage());
			throw new UnsupportedOperationException("SPEP Startup Service unmarshaller could not be initialized. Failed to construct SAMLIdentityProviderProcessor correctly. Error was: " + e.getMessage(), e);
		}
	}

	public void process(EntityDataImpl entityData, EntityDescriptor entityDescriptor) throws InvalidMetadataException
	{
		List<RoleDescriptorType> roleDescriptors = entityDescriptor.getIDPDescriptorAndSSODescriptorAndRoleDescriptors();

		IDPSSODescriptor idpSSODescriptor = null;
		AttributeAuthorityDescriptor attributeAuthorityDescriptor = null;
		LXACMLPDPDescriptor lxacmlPDPDescriptor = null;

		for (RoleDescriptorType roleDescriptor : roleDescriptors)
		{
			if (roleDescriptor instanceof IDPSSODescriptor)
			{
				idpSSODescriptor = (IDPSSODescriptor) roleDescriptor;
			}
			if (roleDescriptor instanceof AttributeAuthorityDescriptor)
			{
				attributeAuthorityDescriptor = (AttributeAuthorityDescriptor) roleDescriptor;
			}
			if (roleDescriptor instanceof LXACMLPDPDescriptor)
			{
				lxacmlPDPDescriptor = (LXACMLPDPDescriptor) roleDescriptor;
			}
		}

		if (lxacmlPDPDescriptor == null)
		{
			//this.logger.debug("Couldn't find LXACMLPDPDescriptor in RoleDescriptor list.. Checking Extensions element");
			lxacmlPDPDescriptor = this.lxacmlPDPDescriptorFromExtensions(entityDescriptor.getExtensions());
		}

		String entityID = entityDescriptor.getEntityID();

		if (entityID.equals(this.trustedEntityID))
		{
			this.logger.debug("Updating trusted ESOE entity from new data. Entity ID: {}", this.trustedEntityID);

			if (lxacmlPDPDescriptor == null)
			{
				this.logger.debug("Couldn't find LXACMLPDPDescriptor in RoleDescriptor list.. Checking Extensions element");
				lxacmlPDPDescriptor = this.lxacmlPDPDescriptorFromExtensions(entityDescriptor.getExtensions());
			}

			if (idpSSODescriptor == null)
			{
				this.logger.error("No IDPSSODescriptor present for specified trusted ESOE. Entity ID: " + this.trustedEntityID);
				throw new InvalidMetadataException("No IDPSSODescriptor present for specified trusted ESOE. Entity ID: " + this.trustedEntityID);
			}
			if (attributeAuthorityDescriptor == null)
			{
				this.logger.error("No AttributeAuthorityDescriptor present for specified trusted ESOE. Entity ID: " + this.trustedEntityID);
				throw new InvalidMetadataException("No AttributeAuthorityDescriptor present for specified trusted ESOE. Entity ID: " + this.trustedEntityID);
			}
			if (lxacmlPDPDescriptor == null)
			{
				this.logger.error("No LXACMLPDPDescriptor present for specified trusted ESOE. Entity ID: " + this.trustedEntityID);
				throw new InvalidMetadataException("No LXACMLPDPDescriptor present for specified trusted ESOE. Entity ID: " + this.trustedEntityID);
			}

			List<String> keyNames = keyNamesFromRoleDescriptor(idpSSODescriptor);
			List<String> nameIDFormat = nameIDFormatsFromIDPSSODescriptor(idpSSODescriptor);
			EndpointCollection singleLogoutServiceEndpoints = singleLogoutServiceEndpointsFromDescriptor(idpSSODescriptor, entityData.getRandom());
			EndpointCollection singleSignOnServiceEndpoints = singleSignOnServiceEndpointsFromDescriptor(idpSSODescriptor, entityData.getRandom());
			EndpointCollection attributeServiceEndpoints = attributeServiceEndpointsFromDescriptor(attributeAuthorityDescriptor, entityData.getRandom());
			EndpointCollection spepStartupServiceEndpoints = spepStartupServiceEndpointsFromDescriptor(idpSSODescriptor, entityData.getRandom());
			EndpointCollection lxacmlAuthzDecisionEndpoints = lxacmlAuthzDecisionEndpointsFromDescriptor(lxacmlPDPDescriptor, entityData.getRandom());
			
			if (singleSignOnServiceEndpoints.getEndpointList().size() == 0)
			{
				this.logger.error("Trusted ESOE did not have any single sign-on endpoints. Entity ID: " + this.trustedEntityID);
				throw new InvalidMetadataException("Trusted ESOE did not have any single sign-on endpoints. Entity ID: " + this.trustedEntityID);
			}
			if (attributeServiceEndpoints.getEndpointList().size() == 0)
			{
				this.logger.error("Trusted ESOE did not have any attribute service endpoints. Entity ID: " + this.trustedEntityID);
				throw new InvalidMetadataException("Trusted ESOE did not have any single sign-on endpoints. Entity ID: " + this.trustedEntityID);
			}
			if (spepStartupServiceEndpoints.getEndpointList().size() == 0)
			{
				this.logger.error("Trusted ESOE did not have any SPEP startup endpoints. Entity ID: " + this.trustedEntityID);
				throw new InvalidMetadataException("Trusted ESOE did not have any single sign-on endpoints. Entity ID: " + this.trustedEntityID);
			}
			if (lxacmlAuthzDecisionEndpoints.getEndpointList().size() == 0)
			{
				this.logger.error("Trusted ESOE did not have any LXACML PDP endpoints. Entity ID: " + this.trustedEntityID);
				throw new InvalidMetadataException("Trusted ESOE did not have any single sign-on endpoints. Entity ID: " + this.trustedEntityID);
			}
			
			TrustedESOERoleImpl role = new TrustedESOERoleImpl(keyNames, nameIDFormat, singleLogoutServiceEndpoints, singleSignOnServiceEndpoints, attributeServiceEndpoints, spepStartupServiceEndpoints, lxacmlAuthzDecisionEndpoints);

			entityData.addRoleData(role);
		}
		else
		{
			boolean isESOE = false;

			// Check if we have an ESOE (or ESOE-compatible) entity
			// TODO Maybe a better way to distinguish ESOE entities?
			if (idpSSODescriptor != null && attributeAuthorityDescriptor != null && lxacmlPDPDescriptor != null)
			{
				this.logger.debug("Identified entity as an ESOE (or ESOE-compatible). Entity ID: " + entityID);
				List<String> keyNames = keyNamesFromRoleDescriptor(idpSSODescriptor);
				List<String> nameIDFormat = nameIDFormatsFromIDPSSODescriptor(idpSSODescriptor);
				EndpointCollection singleLogoutServiceEndpoints = singleLogoutServiceEndpointsFromDescriptor(idpSSODescriptor, entityData.getRandom());
				EndpointCollection singleSignOnServiceEndpoints = singleSignOnServiceEndpointsFromDescriptor(idpSSODescriptor, entityData.getRandom());
				EndpointCollection attributeServiceEndpoints = attributeServiceEndpointsFromDescriptor(attributeAuthorityDescriptor, entityData.getRandom());
				ESOERoleImpl role = new ESOERoleImpl(keyNames, nameIDFormat, singleLogoutServiceEndpoints, singleSignOnServiceEndpoints, attributeServiceEndpoints);

				entityData.addRoleData(role);

				isESOE = true;
			}

			// If it is not an ESOE entity, add each of the SAML roles separately.
			if (!isESOE)
			{
				if (idpSSODescriptor != null)
				{
					this.logger.debug("Identified entity as a SAML identity provider. Entity ID: " + entityID);
					List<String> keyNames = keyNamesFromRoleDescriptor(idpSSODescriptor);
					List<String> nameIDFormat = nameIDFormatsFromIDPSSODescriptor(idpSSODescriptor);
					EndpointCollection singleLogoutServiceEndpoints = singleLogoutServiceEndpointsFromDescriptor(idpSSODescriptor, entityData.getRandom());
					EndpointCollection singleSignOnServiceEndpoints = singleSignOnServiceEndpointsFromDescriptor(idpSSODescriptor, entityData.getRandom());
					IdentityProviderRole role = new IdentityProviderRoleImpl(keyNames, nameIDFormat, singleLogoutServiceEndpoints, singleSignOnServiceEndpoints);

					entityData.addRoleData(role);
				}

				if (attributeAuthorityDescriptor != null)
				{
					this.logger.debug("Identified entity as a SAML attribute authority. Entity ID: " + entityID);

					List<String> keyNames = keyNamesFromRoleDescriptor(idpSSODescriptor);
					EndpointCollection attributeServiceEndpoints = attributeServiceEndpointsFromDescriptor(attributeAuthorityDescriptor, entityData.getRandom());
					AttributeAuthorityRoleImpl role = new AttributeAuthorityRoleImpl(keyNames, attributeServiceEndpoints);

					entityData.addRoleData(role);
				}

				if (lxacmlPDPDescriptor != null)
				{
					// TODO Do we want to store LXACML PDP roles?
					// I think not.
				}
			}
		}
	}

	private LXACMLPDPDescriptor lxacmlPDPDescriptorFromExtensions(Extensions extensions) throws InvalidMetadataException
	{
		if (extensions != null)
		{
			for (Element extensionElement : extensions.getImplementedExtensions())
			{
				if (extensionElement.getLocalName().equals("LXACMLPDPDescriptor")) //$NON-NLS-1$
				{
					try
					{
						return this.lxacmlPDPDescriptorUnmarshaller.unMarshallUnSigned(extensionElement);
					}
					catch (UnmarshallerException e)
					{
						this.logger.error("LXACMLPDPDescriptor failed to be unmarshalled as a single element. This should never occur - DOM has already been validated previously.", e);
						throw new InvalidMetadataException("LXACMLPDPDescriptor failed to be unmarshalled as a single element. This should never occur - DOM has already been validated previously.", e);
					}
				}
			}
		}

		return null;
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

	private List<String> nameIDFormatsFromIDPSSODescriptor(IDPSSODescriptor idpSSODescriptor)
	{
		List<String> nameIDFormats = new ArrayList<String>();
		
		for (String acceptedIdentifier : idpSSODescriptor.getNameIDFormats())
		{
			nameIDFormats.add(acceptedIdentifier);
		}
		
		return nameIDFormats;
	}

	private EndpointCollection attributeServiceEndpointsFromDescriptor(AttributeAuthorityDescriptor attributeAuthorityDescriptor, Random random)
	{
		EndpointCollection endpointCollection = new EndpointCollectionImpl(random);
		
		for (EndpointType endpoint : attributeAuthorityDescriptor.getAttributeServices())
		{
			endpointCollection.getEndpointList().add(new EndpointImpl(endpoint.getBinding(), endpoint.getLocation()));
		}

		return endpointCollection;
	}

	private EndpointCollection singleSignOnServiceEndpointsFromDescriptor(IDPSSODescriptor idpSSODescriptor, Random random)
	{
		EndpointCollection endpointCollection = new EndpointCollectionImpl(random);

		for (EndpointType endpoint : idpSSODescriptor.getSingleSignOnServices())
		{
			endpointCollection.getEndpointList().add(new EndpointImpl(endpoint.getBinding(), endpoint.getLocation()));
		}
		return endpointCollection;
	}

	private EndpointCollection singleLogoutServiceEndpointsFromDescriptor(IDPSSODescriptor idpSSODescriptor, Random random)
	{
		EndpointCollection endpointCollection = new EndpointCollectionImpl(random);

		for (EndpointType endpoint : idpSSODescriptor.getSingleLogoutServices())
		{
			endpointCollection.getEndpointList().add(new EndpointImpl(endpoint.getBinding(), endpoint.getLocation()));
		}

		return endpointCollection;
	}

	private EndpointCollection spepStartupServiceEndpointsFromDescriptor(IDPSSODescriptor idpSSODescriptor, Random random) throws InvalidMetadataException
	{
		EndpointCollection endpointCollection = new EndpointCollectionImpl(random);

		try
		{
			Extensions idpExtensions = idpSSODescriptor.getExtensions();
			if (idpExtensions != null)
			{
				for (Element extension : idpExtensions.getImplementedExtensions())
				{
					if (extension.getLocalName().equals("SPEPStartupService")) //$NON-NLS-1$
					{
						SPEPStartupService endpoint = this.spepStartupServiceUnmarshaller.unMarshallUnSigned(extension);
						endpointCollection.getEndpointList().add(new EndpointImpl(endpoint.getBinding(), endpoint.getLocation()));
					}
				}
			}
		}
		catch (UnmarshallerException e)
		{
			this.logger.error("Unable to unmarshal SPEP Startup Service element. Error was: " + e.getMessage());
			this.logger.debug("Unable to unmarshal SPEP Startup Service element. Exception follows", e);
			throw new InvalidMetadataException("Unable to unmarshal SPEP Startup Service element. Error was: " + e.getMessage());
		}

		return endpointCollection;
	}

	private EndpointCollection lxacmlAuthzDecisionEndpointsFromDescriptor(LXACMLPDPDescriptor lxacmlPDPDescriptor, Random random)
	{
		EndpointCollection endpointCollection = new EndpointCollectionImpl(random);
		
		for (EndpointType endpoint : lxacmlPDPDescriptor.getAuthzServices())
		{
			endpointCollection.getEndpointList().add(new EndpointImpl(endpoint.getBinding(), endpoint.getLocation()));
		}

		return endpointCollection;
	}
}

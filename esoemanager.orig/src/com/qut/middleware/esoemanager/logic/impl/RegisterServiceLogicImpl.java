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
 * Author: Bradley Beddoes
 * Creation Date: 1/5/07
 * 
 * Purpose: Register service logic default implementation
 */
package com.qut.middleware.esoemanager.logic.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.crypto.CryptoProcessor;
import com.qut.middleware.crypto.exception.CryptoException;
import com.qut.middleware.esoemanager.Constants;
import com.qut.middleware.esoemanager.UtilityFunctions;
import com.qut.middleware.esoemanager.bean.ContactPersonBean;
import com.qut.middleware.esoemanager.bean.ServiceBean;
import com.qut.middleware.esoemanager.bean.ServiceNodeBean;
import com.qut.middleware.esoemanager.exception.RegisterServiceException;
import com.qut.middleware.esoemanager.exception.SPEPDAOException;
import com.qut.middleware.esoemanager.logic.RegisterServiceLogic;
import com.qut.middleware.esoemanager.spep.sqlmap.SPEPDAO;
import com.qut.middleware.saml2.BindingConstants;
import com.qut.middleware.saml2.NameIDFormatConstants;
import com.qut.middleware.saml2.ProtocolConstants;
import com.qut.middleware.saml2.exception.MarshallerException;
import com.qut.middleware.saml2.handler.Marshaller;
import com.qut.middleware.saml2.handler.impl.MarshallerImpl;
import com.qut.middleware.saml2.identifier.IdentifierGenerator;
import com.qut.middleware.saml2.schemas.metadata.EndpointType;
import com.qut.middleware.saml2.schemas.metadata.Extensions;
import com.qut.middleware.saml2.schemas.metadata.IndexedEndpointType;
import com.qut.middleware.saml2.schemas.metadata.KeyDescriptor;
import com.qut.middleware.saml2.schemas.metadata.SPSSODescriptor;
import com.qut.middleware.saml2.schemas.metadata.extensions.CacheClearService;
import com.qut.middleware.saml2.schemas.metadata.lxacml.LXACMLPDPDescriptor;

public class RegisterServiceLogicImpl implements RegisterServiceLogic
{
	private CryptoProcessor cryptoProcessor;
	private IdentifierGenerator identifierGenerator;
	private Marshaller<SPSSODescriptor> spMarshaller;
	private Marshaller<CacheClearService> cacheClearMarshaller;
	private SPEPDAO spepDAO;

	/*
	 * In the future the webapp should be extended to allow different organizations to insert their contact data for
	 * federated services, for now the local enterprise owns everything
	 */
	private String organizationName;
	private String organizationDisplayName;
	private String organizationURL;
	private String activeFlag;
	private byte[] defaultPolicy;

	private UtilityFunctions util;

	private final String[] schemas = { Constants.samlMetadata, Constants.lxacmlMetadata, Constants.cacheClearService };

	private final String MAR_PKGNAMES = SPSSODescriptor.class.getPackage().getName() + Constants.SCHEMA_SEPERATOR
			+ LXACMLPDPDescriptor.class.getPackage().getName() + Constants.SCHEMA_SEPERATOR
			+ CacheClearService.class.getPackage().getName();

	private final String MAR_PKGNAMES2 = CacheClearService.class.getPackage().getName();

	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(RegisterServiceLogicImpl.class.getName());

	public RegisterServiceLogicImpl(CryptoProcessor cryptoProcessor, IdentifierGenerator identifierGenerator,
			SPEPDAO spepDAO, String organizationName, String organizationDisplayName, String organizationURL,
			boolean activeFlag, File defaultPolicy) throws MarshallerException, IOException
	{
		if (cryptoProcessor == null)
		{
			this.logger.error("cryptoProcessor for RegisterServiceLogicImpl was NULL");
			throw new IllegalArgumentException("cryptoProcessor for RegisterServiceLogicImpl was NULL");
		}
		if (identifierGenerator == null)
		{
			this.logger.error("identifierGenerator for RegisterServiceLogicImpl was NULL");
			throw new IllegalArgumentException("identifierGenerator for RegisterServiceLogicImpl was NULL");
		}
		if (spepDAO == null)
		{
			this.logger.error("spepDAO for RegisterServiceLogicImpl was NULL");
			throw new IllegalArgumentException("spepDAO for RegisterServiceLogicImpl was NULL");
		}
		if (organizationName == null)
		{
			this.logger.error("organizationName for RegisterServiceLogicImpl was NULL");
			throw new IllegalArgumentException("organizationName for RegisterServiceLogicImpl was NULL");
		}
		if (organizationDisplayName == null)
		{
			this.logger.error("organizationDisplayName for RegisterServiceLogicImpl was NULL");
			throw new IllegalArgumentException("organizationDisplayName for RegisterServiceLogicImpl was NULL");
		}
		if (organizationURL == null)
		{
			this.logger.error("organizationURL for RegisterServiceLogicImpl was NULL");
			throw new IllegalArgumentException("organizationURL for RegisterServiceLogicImpl was NULL");
		}

		this.cryptoProcessor = cryptoProcessor;
		this.identifierGenerator = identifierGenerator;
		this.spepDAO = spepDAO;
		this.organizationName = organizationName;
		this.organizationDisplayName = organizationDisplayName;
		this.organizationURL = organizationURL;

		if (activeFlag)
			this.activeFlag = Constants.SERVICE_ACTIVE;
		else
			this.activeFlag = Constants.SERVICE_INACTIVE;

		this.spMarshaller = new MarshallerImpl<SPSSODescriptor>(this.MAR_PKGNAMES, this.schemas);
		this.cacheClearMarshaller = new MarshallerImpl<CacheClearService>(this.MAR_PKGNAMES2, this.schemas);

		this.util = new UtilityFunctions();
		
		loadDefaultPolicy(defaultPolicy);
	}
	
	private void loadDefaultPolicy(File policyFile) throws IOException
	{
		InputStream fileStream = null;
		
		try
		{
			long length = policyFile.length();
			byte[] byteArray = new byte[(int) length];
			fileStream = new FileInputStream(policyFile);
			fileStream.read(byteArray);
			fileStream.close();

			this.defaultPolicy = byteArray;
		}
		finally
		{
			if (fileStream != null)
				fileStream.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.logic.RegisterServiceLogic#execute(com.qut.middleware.esoemanager.bean.ServiceBean)
	 */
	public void execute(ServiceBean bean) throws RegisterServiceException
	{
		String keyStorePassphrase;
		String keyPairName;
		String keyPairPassphrase;
		String keyPairSubjectDN;
		KeyPair spKeyPair;
		KeyStore keyStore;
		SPSSODescriptor spDescriptor;
		byte[] spDescriptorXML;
		byte[] keyStoreBytes;

		try
		{
			/* Get ID's for the new service */
			bean.setEntID(this.spepDAO.getNextEntID());
			bean.setDescID(this.spepDAO.getNextDescID());
			
			/* Register the service entity */
			this.spepDAO.insertEntityDescriptor(bean.getEntID(), bean.getEntityID(), organizationName, organizationDisplayName, organizationURL,
					activeFlag);

			/* Register the service description */
			this.spepDAO.insertServiceDescription(bean.getEntID(), bean.getServiceName(), bean.getServiceURL(), bean
					.getServiceDescription(), bean.getServiceAuthzFailureMsg());

			for (ContactPersonBean contact : bean.getContacts())
			{
				/* Add a unique identifier to this contact person */
				contact.setContactID(this.util.generateID());

				/* Register the service contact points */
				this.logger.info("Adding contact person with name" + contact.getGivenName() + " "
						+ contact.getSurName() + " and generated id of " + contact.getContactID());
				this.spepDAO.insertServiceContacts(bean.getEntID(), contact.getContactID(), contact.getContactType(), contact
						.getCompany(), contact.getGivenName(), contact.getSurName(), contact.getEmailAddress(), contact
						.getTelephoneNumber());
			}

			keyPairName = this.identifierGenerator.generateXMLKeyName();

			keyStorePassphrase = this.generatePassphrase();
			bean.setKeyStorePassphrase(keyStorePassphrase);

			keyPairPassphrase = this.generatePassphrase();
			bean.setKeyPairPassphrase(keyPairPassphrase);

			keyPairSubjectDN = this.generateSubjectDN(bean);
			keyStore = this.cryptoProcessor.generateKeyStore();
			spKeyPair = this.cryptoProcessor.generateKeyPair();
			this.cryptoProcessor.addKeyPair(keyStore, keyStorePassphrase, spKeyPair, keyPairName, keyPairPassphrase,
					keyPairSubjectDN);
			keyStoreBytes = this.cryptoProcessor.convertKeystoreByteArray(keyStore, keyStorePassphrase);

			spDescriptor = createSPSSODescriptor(bean, (RSAPublicKey) spKeyPair.getPublic(), keyPairName);
			spDescriptorXML = this.spMarshaller.marshallUnSigned(spDescriptor);

			/* Write SP Descriptor to data repostory */
			this.spepDAO.insertDescriptor(bean.getEntID(), bean.getDescID(), spDescriptor.getID(), spDescriptorXML, util
					.getRoleDescriptorTypeId(SPSSODescriptor.class.getCanonicalName()));

			/* Wrtie service nodes to data repository */
			for (ServiceNodeBean node : bean.getServiceNodes())
			{
				this.spepDAO.insertServiceNode(node.getNodeID(), bean.getDescID(), node.getNodeURL(), node
						.getAssertionConsumerService(), node.getSingleLogoutService(), node.getCacheClearService());
			}

			/* Determine expiry date of PKI data */
			Calendar expiryDate = Calendar.getInstance();
			expiryDate.add(Calendar.YEAR, this.cryptoProcessor.getCertExpiryIntervalInYears());
			
			/* Commit PKI data to repository for future retrieval and audit */
			this.spepDAO.insertPublicKey(bean.getDescID(), expiryDate.getTime(), keyPairName, this.cryptoProcessor.convertPublicKeyByteArray(spKeyPair.getPublic()));
			
			this.spepDAO.insertPKIData(bean.getDescID(), expiryDate.getTime(), keyStoreBytes,
					encryptPassphrase(keyStorePassphrase), keyPairName, encryptPassphrase(keyPairPassphrase));
			
			/* Setup default policy */
			this.spepDAO.insertServiceAuthorizationPolicy(bean.getEntID(), "spep-0", this.defaultPolicy);
			
			/* We encode the entityID in base64 before returning */
			bean.setEntityID(new String(Base64.encodeBase64(bean.getEntityID().getBytes())));
		}
		catch (CryptoException e)
		{
			this.logger.error("NoSuchAlgorithmException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new RegisterServiceException(e.getLocalizedMessage(), e);
		}
		catch (MarshallerException e)
		{
			this.logger.error("MarshallerException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new RegisterServiceException(e.getLocalizedMessage(), e);
		}
		catch (SPEPDAOException e)
		{
			this.logger.error("SPEPDAOException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new RegisterServiceException(e.getLocalizedMessage(), e);
		}

	}

	private SPSSODescriptor createSPSSODescriptor(ServiceBean bean, RSAPublicKey pubKey, String keyPairName)
			throws MarshallerException, RegisterServiceException
	{
		/*
		 * Determine how many SP records were submitted for ID generation for endpoints this will assimilate each SP
		 * created into a single record per metadata design
		 */
		SPSSODescriptor spDescriptor = new SPSSODescriptor();
		KeyDescriptor keyDescriptor;

		spDescriptor.setID(this.identifierGenerator.generateSAMLID());

		String serviceHost = bean.getServiceHost();  
		if(serviceHost == null)
		{
			throw new RegisterServiceException("Unable to correctly establish value of service host from supplied data");
		}
		
		for (ServiceNodeBean node : bean.getServiceNodes())
		{
			Extensions extensions;
			Integer nodeID;

			/* Setup node endpoint ID for indexed services and bean */
			node.setNodeID(this.util.generateID());
			nodeID = new Integer(node.getNodeID());

			/* Add the configured assertion consumer service - ACS is set to the service URL, this ensures load balanced services are handled correctly with the SAML POST profile
			 * single node service should have identical service and node urls */
			IndexedEndpointType assertionConsumerService = new IndexedEndpointType();
			assertionConsumerService.setLocation(serviceHost + node.getAssertionConsumerService());
			assertionConsumerService.setBinding(BindingConstants.httpPost);
			assertionConsumerService.setIndex(nodeID.intValue());
			spDescriptor.getAssertionConsumerServices().add(assertionConsumerService);

			/* Add the configured single logout service (non indexed) */
			EndpointType singleLogoutService = new EndpointType();
			singleLogoutService.setLocation(node.getNodeURL() + node.getSingleLogoutService());
			singleLogoutService.setBinding(BindingConstants.soap);
			spDescriptor.getSingleLogoutServices().add(singleLogoutService);

			/* Add the configured cache clear service (non indexed) */
			CacheClearService cacheClearService = new CacheClearService();
			cacheClearService.setLocation(node.getNodeURL() + node.getCacheClearService());
			cacheClearService.setBinding(BindingConstants.soap);
			cacheClearService.setIndex(nodeID.intValue());
			
			extensions = spDescriptor.getExtensions();
			if (extensions == null)
			{
				extensions = new Extensions();
			}
			extensions.getImplementedExtensions().add(
					this.cacheClearMarshaller.marshallUnSignedElement(cacheClearService));
			spDescriptor.setExtensions(extensions);
		}

		spDescriptor.setAuthnRequestsSigned(true);
		spDescriptor.setWantAssertionsSigned(true);
		spDescriptor.getProtocolSupportEnumerations().add(ProtocolConstants.protocol);
		spDescriptor.getNameIDFormats().add(NameIDFormatConstants.trans);
		
		return spDescriptor;
	}

	/*
	 * Takes the service URL from the supplied bean and creates a subject DN
	 */
	private String generateSubjectDN(ServiceBean bean)
	{
		try
		{
			String result = new String();
			URL serviceURL = new URL(bean.getServiceURL());
			String[] components = serviceURL.getHost().split("\\.");
			for (String component : components)
			{
				if (result.length() != 0)
					result = result + ",";

				result = result + "dc=" + component;
			}
			return result;
		}
		catch (MalformedURLException e)
		{
			this.logger.error("Error attempting to generate certificate subjectDN " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			return "dc=" + bean.getServiceName();
		}
	}

	/*
	 * Generates a 10 byte passphrase for use with an SPEP @return populated passphrase
	 */
	private String generatePassphrase()
	{
		SecureRandom random;
		String passphrase;
		byte[] buf;

		try
		{
			/* Attempt to get the specified RNG instance */
			random = SecureRandom.getInstance("SHA1PRNG");
		}
		catch (NoSuchAlgorithmException nsae)
		{
			this.logger.error("NoSuchAlgorithmException when trying to create SecureRandom instance " + nsae.getLocalizedMessage()); //$NON-NLS-1$
			this.logger.debug(nsae.getLocalizedMessage(), nsae);
			random = new SecureRandom();
		}

		buf = new byte[Constants.PASSPHRASE_LENGTH];
		random.nextBytes(buf);
		passphrase = new String(Hex.encodeHex(buf));

		return passphrase;
	}

	/**
	 * Obfuscator for passphrases being stored in data repository
	 * 
	 * @param passphrase
	 *            Clear text passphrase to operate on
	 * @return An obscured passphrase for writing to data repository, not guarented safe to sophisticated attacks
	 */
	private String encryptPassphrase(String passphrase)
	{
		// TODO: Obfuscate this data before returning
		return passphrase;
	}
}

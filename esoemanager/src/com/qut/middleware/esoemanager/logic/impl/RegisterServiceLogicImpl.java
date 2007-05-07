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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;

import com.qut.middleware.esoemanager.Constants;
import com.qut.middleware.esoemanager.UtilityFunctions;
import com.qut.middleware.esoemanager.bean.ContactPersonBean;
import com.qut.middleware.esoemanager.bean.ServiceBean;
import com.qut.middleware.esoemanager.bean.ServiceNodeBean;
import com.qut.middleware.esoemanager.crypto.CryptoProcessor;
import com.qut.middleware.esoemanager.exception.CryptoException;
import com.qut.middleware.esoemanager.exception.RegisterServiceException;
import com.qut.middleware.esoemanager.exception.SPEPDAOException;
import com.qut.middleware.esoemanager.logic.RegisterServiceLogic;
import com.qut.middleware.esoemanager.spep.sqlmap.SPEPDAO;
import com.qut.middleware.saml2.BindingConstants;
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
	private String defaultPolicy;

	private UtilityFunctions util;

	private final String[] schemas = { Constants.samlMetadata, Constants.lxacmlMetadata, Constants.cacheClearService };

	private final String MAR_PKGNAMES = SPSSODescriptor.class.getPackage().getName() + Constants.SCHEMA_SEPERATOR
			+ LXACMLPDPDescriptor.class.getPackage().getName() + Constants.SCHEMA_SEPERATOR
			+ CacheClearService.class.getPackage().getName();

	private final String MAR_PKGNAMES2 = CacheClearService.class.getPackage().getName();

	/* Local logging instance */
	private Logger logger = Logger.getLogger(RegisterServiceLogicImpl.class.getName());

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
		String tmp;
		StringBuffer inBuffer = null;
		InputStream fileStream = null;
		Reader reader = null;
		BufferedReader in = null;

		try
		{
			inBuffer = new StringBuffer();
			fileStream = new FileInputStream(policyFile);
			reader = new InputStreamReader(fileStream, "UTF-16");
			in = new BufferedReader(reader);

			while ((tmp = in.readLine()) != null)
			{
				inBuffer.append(tmp);
				inBuffer.append(System.getProperty("line.separator"));
			}

			this.defaultPolicy = inBuffer.toString();
		}
		finally
		{
			if (in != null)
				in.close();

			if (reader != null)
				reader.close();

			if (fileStream != null)
				fileStream.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.logic.RegisterServiceLogic#execute(com.qut.middleware.esoemanager.bean.ServiceBean)
	 */
	public String execute(ServiceBean bean) throws RegisterServiceException
	{
		String keyStorePassphrase;
		String keyPairName;
		String keyPairPassphrase;
		String keyPairSubjectDN;
		KeyPair spKeyPair;
		KeyStore keyStore;
		SPSSODescriptor spDescriptor;
		String entityID, entityDescriptorID;
		String spDescriptorXML;
		byte[] keyStoreBytes;

		try
		{
			entityID = (this.identifierGenerator.generateSAMLID());
			entityDescriptorID = (this.identifierGenerator.generateSAMLID());
			this.logger.debug("Creating service with EntityID of " + entityDescriptorID);

			/* Register the service entity */
			this.spepDAO.insertEntityDescriptor(entityID, organizationName, organizationDisplayName, organizationURL,
					activeFlag);

			/* Register the service description */
			this.spepDAO.insertServiceDescription(entityID, bean.getServiceName(), bean.getServiceURL(), bean
					.getServiceDescription(), bean.getServiceAuthzFailureMsg());

			for (ContactPersonBean contact : bean.getContacts())
			{
				/* Add a unique identifier to this contact person */
				contact.setContactID(this.util.generateID());

				/* Register the service contact points */
				this.logger.info("Adding contact person with name" + contact.getGivenName() + " "
						+ contact.getSurName() + " and generated id of " + contact.getContactID());
				this.spepDAO.insertServiceContacts(entityID, contact.getContactID(), contact.getContactType(), contact
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
			this.spepDAO.insertDescriptor(entityID, spDescriptor.getID(), spDescriptorXML, util
					.getRoleDescriptorTypeId(SPSSODescriptor.class.getCanonicalName()));

			/* Wrtie service nodes to data repository */
			for (ServiceNodeBean node : bean.getServiceNodes())
			{
				this.spepDAO.insertServiceNode(node.getNodeID(), spDescriptor.getID(), node.getNodeURL(), node
						.getAssertionConsumerService(), node.getSingleLogoutService(), node.getCacheClearService());
			}

			/* Commit PKI data to repository for future retrieval and audit */
			this.spepDAO.insertPKIData(spDescriptor.getID(), new Date(), keyStoreBytes,
					encryptPassphrase(keyStorePassphrase), keyPairName, encryptPassphrase(keyPairPassphrase));
			
			/* Setup default policy */
			this.spepDAO.insertServiceAuthorizationPolicy(spDescriptor.getID(), this.defaultPolicy, new Date());
			
			return entityID;
		}
		catch (CryptoException e)
		{
			this.logger.error("NoSuchAlgorithmException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new RegisterServiceException(e.getLocalizedMessage(), e);
		}
		catch (MarshallerException e)
		{
			this.logger.error("MarshallerException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new RegisterServiceException(e.getLocalizedMessage(), e);
		}
		catch (SPEPDAOException e)
		{
			this.logger.error("SPEPDAOException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new RegisterServiceException(e.getLocalizedMessage(), e);
		}

	}

	private SPSSODescriptor createSPSSODescriptor(ServiceBean bean, RSAPublicKey pubKey, String keyPairName)
			throws MarshallerException
	{
		/*
		 * Determine how many SP records were submitted for ID generation for endpoints this will assimilate each SP
		 * created into a single record per metadata design
		 */
		SPSSODescriptor spDescriptor = new SPSSODescriptor();
		KeyDescriptor keyDescriptor;

		spDescriptor.setID(this.identifierGenerator.generateSAMLID());
		keyDescriptor = this.cryptoProcessor.createSigningKeyDescriptor(pubKey, keyPairName);
		spDescriptor.getKeyDescriptors().add(keyDescriptor);

		for (ServiceNodeBean node : bean.getServiceNodes())
		{
			Extensions extensions;
			Integer nodeID;

			/* Setup node endpoint ID for indexed services and bean */
			node.setNodeID(this.util.generateID());
			nodeID = new Integer(node.getNodeID());

			/* Add the configured assertion consumer service */
			IndexedEndpointType assertionConsumerService = new IndexedEndpointType();
			assertionConsumerService.setLocation(node.getNodeURL() + node.getAssertionConsumerService());
			assertionConsumerService.setBinding(BindingConstants.soap);
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
			this.logger.debug(e);
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
			this.logger
					.fatal("NoSuchAlgorithmException when trying to create SecureRandom instance " + nsae.getLocalizedMessage()); //$NON-NLS-1$
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
	 * @return An obscured passphrase for writing to data repository, not guarenteed safe to sophisticated attacks
	 */
	private String encryptPassphrase(String passphrase)
	{
		// TODO: Obfuscate this data before returning
		return passphrase;
	}
}

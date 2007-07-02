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
 * Purpose: Registers all base compoenents required for an ESOE deployment
 */
package com.qut.middleware.esoestartup.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.w3c.dom.Element;

import com.qut.middleware.crypto.CryptoProcessor;
import com.qut.middleware.crypto.exception.CryptoException;
import com.qut.middleware.esoemanager.UtilityFunctions;
import com.qut.middleware.esoemanager.bean.ContactPersonBean;
import com.qut.middleware.esoemanager.bean.ServiceNodeBean;
import com.qut.middleware.esoemanager.exception.RenderConfigException;
import com.qut.middleware.esoestartup.Constants;
import com.qut.middleware.esoestartup.bean.ESOEBean;
import com.qut.middleware.esoestartup.esoe.sqlmap.ESOEDAO;
import com.qut.middleware.esoestartup.exception.ESOEDAOException;
import com.qut.middleware.esoestartup.exception.RegisterESOEException;
import com.qut.middleware.esoestartup.processor.CryptoProcessorESOEImpl;
import com.qut.middleware.saml2.AttributeFormatConstants;
import com.qut.middleware.saml2.BindingConstants;
import com.qut.middleware.saml2.ProtocolConstants;
import com.qut.middleware.saml2.exception.MarshallerException;
import com.qut.middleware.saml2.handler.Marshaller;
import com.qut.middleware.saml2.handler.impl.MarshallerImpl;
import com.qut.middleware.saml2.identifier.IdentifierGenerator;
import com.qut.middleware.saml2.schemas.metadata.AttributeAuthorityDescriptor;
import com.qut.middleware.saml2.schemas.metadata.EndpointType;
import com.qut.middleware.saml2.schemas.metadata.Extensions;
import com.qut.middleware.saml2.schemas.metadata.IDPSSODescriptor;
import com.qut.middleware.saml2.schemas.metadata.IndexedEndpointType;
import com.qut.middleware.saml2.schemas.metadata.KeyDescriptor;
import com.qut.middleware.saml2.schemas.metadata.SPSSODescriptor;
import com.qut.middleware.saml2.schemas.metadata.extensions.CacheClearService;
import com.qut.middleware.saml2.schemas.metadata.extensions.SPEPStartupService;
import com.qut.middleware.saml2.schemas.metadata.lxacml.LXACMLPDPDescriptor;
import com.qut.middleware.tools.war.bean.AdditionalContent;
import com.qut.middleware.tools.war.exception.GenerateWarException;
import com.qut.middleware.tools.war.logic.GenerateWarLogic;

public class RegisterESOELogic
{
	GenerateWarLogic generateWarLogic;
	RenderESOEConfigLogic renderESOEConfigLogic;

	private CryptoProcessor cryptoProcessor;
	private IdentifierGenerator identifierGenerator;

	private JdbcTemplate jdbcTemplate;
	private BasicDataSource dataSource;

	private ESOEDAO esoeDAO;
	private Map<String, File> generationSQL;
	private UtilityFunctions util;

	private String defaultPolicy;

	private Marshaller<IDPSSODescriptor> idpMarshaller;
	private Marshaller<SPEPStartupService> spsMarshaller;
	private Marshaller<AttributeAuthorityDescriptor> aaMarshaller;
	private Marshaller<LXACMLPDPDescriptor> pdpMarshaller;
	private Marshaller<SPSSODescriptor> spMarshaller;
	private Marshaller<CacheClearService> cacheClearMarshaller;

	private final String[] idpSchemas = { Constants.samlMetadata };
	private final String[] spsSchemas = { Constants.spepStartup };
	private final String[] aaSchemas = { Constants.samlMetadata };
	private final String[] pdpSchemas = { Constants.lxacmlMetadata };
	private final String[] spSchemas = { Constants.samlMetadata, Constants.lxacmlMetadata, Constants.cacheClearService };
	private final String[] ccSchemas = { Constants.cacheClearService };

	private final String MAR_PKGNAMES = SPSSODescriptor.class.getPackage().getName() + Constants.SCHEMA_SEPERATOR + LXACMLPDPDescriptor.class.getPackage().getName() + Constants.SCHEMA_SEPERATOR + CacheClearService.class.getPackage().getName();

	/* Local logging instance */
	private Logger logger = Logger.getLogger(RegisterESOELogic.class.getName());

	public RegisterESOELogic(GenerateWarLogic generateWarLogic, RenderESOEConfigLogic renderESOEConfigLogic, IdentifierGenerator identifierGenerator, ESOEDAO esoeDAO, BasicDataSource dataSource, Map<String, File> generationSQL, File policyFile) throws MarshallerException, IOException
	{
		this.generateWarLogic = generateWarLogic;
		this.renderESOEConfigLogic = renderESOEConfigLogic;
		this.identifierGenerator = identifierGenerator;
		this.esoeDAO = esoeDAO;
		this.dataSource = dataSource;
		this.generationSQL = generationSQL;

		this.util = new UtilityFunctions();
		this.cryptoProcessor = new CryptoProcessorESOEImpl();

		this.loadDefaultPolicy(policyFile);

		this.idpMarshaller = new MarshallerImpl<IDPSSODescriptor>(IDPSSODescriptor.class.getPackage().getName(), this.idpSchemas);
		this.spsMarshaller = new MarshallerImpl<SPEPStartupService>(SPEPStartupService.class.getPackage().getName(), this.spsSchemas);
		this.aaMarshaller = new MarshallerImpl<AttributeAuthorityDescriptor>(AttributeAuthorityDescriptor.class.getPackage().getName(), this.aaSchemas);
		this.pdpMarshaller = new MarshallerImpl<LXACMLPDPDescriptor>(LXACMLPDPDescriptor.class.getPackage().getName(), this.pdpSchemas);
		this.spMarshaller = new MarshallerImpl<SPSSODescriptor>(this.MAR_PKGNAMES, this.spSchemas);
		this.cacheClearMarshaller = new MarshallerImpl<CacheClearService>(CacheClearService.class.getPackage().getName(), this.ccSchemas);
	}

	private void configureDataLayer(ESOEBean bean) throws Exception
	{
		switch (bean.getDatabaseDriver())
		{
			case mysql:
				dataSource.setDriverClassName(Constants.MYSQL_DRIVER);
				break;
			case oracle:
				dataSource.setDriverClassName(Constants.ORACLE_DRIVER);
				break;
			/* Attempt to connect to mysql source if not submitted */
			default:
				dataSource.setDriverClassName(Constants.MYSQL_DRIVER);
		}

		try
		{
			dataSource.setUrl(bean.getDatabaseURL());
			dataSource.setUsername(bean.getDatabaseUsername());
			dataSource.setPassword(bean.getDatabasePassword());

			this.jdbcTemplate = new JdbcTemplate(dataSource);

			/* Setup Crypto Processor with additional details */
			this.cryptoProcessor.setCertIssuerDN(bean.getCertIssuerDN());
			this.cryptoProcessor.setCertIssuerEmail(bean.getCertIssuerEmailAddress());

		}
		catch (DataAccessException e)
		{
			this.logger.error("Unable to connect with database " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new RegisterESOEException(e.getLocalizedMessage(), e);
		}
	}

	private void setupRawDatabase(ESOEBean bean) throws RegisterESOEException, IOException
	{
		try
		{
			Vector<String> sqlCommands;
			switch (bean.getDatabaseDriver())
			{
				case mysql:
					sqlCommands = this.loadDatabaseCreationSQL(this.generationSQL.get(Constants.MYSQL));
					break;
				case oracle:
					sqlCommands = this.loadDatabaseCreationSQL(this.generationSQL.get(Constants.ORACLE));
					break;
				/* Attempt to connect to mysql source if not submitted */
				default:
					sqlCommands = this.loadDatabaseCreationSQL(this.generationSQL.get(Constants.MYSQL));
			}

			/* Execute each statement in the supplied sql file */
			for (String statement : sqlCommands)
			{
				/*
				 * Ensure this is not a comment or blank line before attempting insertion
				 */
				if (statement.length() > 1 && !statement.startsWith("#"))
				{
					this.logger.debug("Executing database creation SQL command: " + statement);
					this.jdbcTemplate.execute(statement);
				}
			}
		}
		catch (DataAccessException e)
		{
			this.logger.error("Unable to connect with database " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new RegisterESOEException(e.getLocalizedMessage(), e);
		}
	}

	public void execute(ESOEBean bean) throws RegisterESOEException
	{
		String entityID;

		KeyDescriptor keyDescriptor;

		try
		{
			configureDataLayer(bean);
			setupRawDatabase(bean);
		}
		catch (Exception e)
		{
			/*
			 * The way we use spring here requires catch of exception, not totally elegent...
			 */
			this.logger.error("Exception when attempting to setup database " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new RegisterESOEException("Exception when attempting to setup database connectivity" + e.getLocalizedMessage());
		}

		try
		{
			/*
			 * Set the issuer DN for the Authentication network which is currently determined as cn="metadata" + ESOE
			 * host name
			 */
			bean.setMetadataIssuerDN(Constants.METADATA_ISSUER + this.generateSubjectDN(bean.getEsoeNodeURL()));

			/* Create all crypto */
			createKeyStores(bean);

			/* Create XML descriptors for ESOE and ESOE Manager webapp */
			createESOEDescriptors(bean, bean.getEsoeKeyPair(), bean.getEsoeKeyPairName());
			createESOEManagerDescriptors(bean, bean.getEsoeManagerKeyPair(), bean.getEsoeManagerKeyPairName());

			/*
			 * Dynamically create WAR files for user deployments so they dont need to do manual config, nice chaps
			 * aren't we
			 */
			createESOEWar(bean);
			createESOEManagerSPEPWar(bean);
			createESOEManagerWar(bean);

		}
		catch (FileNotFoundException e)
		{
			this.logger.error("ESOEDAOException when attempting to configure ESOE details " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new RegisterESOEException("ESOEDAOException when attempting to configure ESOE details " + e.getLocalizedMessage());
		}
		catch (KeyStoreException e)
		{
			this.logger.error("ESOEDAOException when attempting to configure ESOE details " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new RegisterESOEException("ESOEDAOException when attempting to configure ESOE details " + e.getLocalizedMessage());
		}
		catch (NoSuchAlgorithmException e)
		{
			this.logger.error("ESOEDAOException when attempting to configure ESOE details " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new RegisterESOEException("ESOEDAOException when attempting to configure ESOE details " + e.getLocalizedMessage());
		}
		catch (CertificateException e)
		{
			this.logger.error("ESOEDAOException when attempting to configure ESOE details " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new RegisterESOEException("ESOEDAOException when attempting to configure ESOE details " + e.getLocalizedMessage());
		}
		catch (CryptoException e)
		{
			this.logger.error("ESOEDAOException when attempting to configure ESOE details " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new RegisterESOEException("ESOEDAOException when attempting to configure ESOE details " + e.getLocalizedMessage());
		}
		catch (IOException e)
		{
			this.logger.error("ESOEDAOException when attempting to configure ESOE details " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new RegisterESOEException("ESOEDAOException when attempting to configure ESOE details " + e.getLocalizedMessage());
		}
		catch (RenderConfigException e)
		{
			this.logger.error("ESOEDAOException when attempting to configure ESOE details " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new RegisterESOEException("ESOEDAOException when attempting to configure ESOE details " + e.getLocalizedMessage());
		}
		catch (GenerateWarException e)
		{
			this.logger.error("GenerateWarException when attempting to create ESOE WAR " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new RegisterESOEException("GenerateWarException when attempting to create ESOE WAR " + e.getLocalizedMessage());
		}

	}

	private void createESOEWar(ESOEBean bean) throws RenderConfigException, GenerateWarException
	{
		/* Create the ESOE WAR File */
		AdditionalContent keyStore = new AdditionalContent();
		keyStore.setPath(Constants.WEBINF + File.separator + Constants.ESOE_KEYSTORE_NAME);
		keyStore.setFileContent(bean.getEsoeKeystore());

		AdditionalContent esoeConfig = new AdditionalContent();
		esoeConfig.setPath(Constants.WEBINF + File.separator + Constants.ESOECONFIG);
		esoeConfig.setFileContent(this.renderESOEConfigLogic.renderESOEConfig(bean, 0).getBytes());

		List<AdditionalContent> additionalContent = new ArrayList<AdditionalContent>();
		additionalContent.add(keyStore);
		additionalContent.add(esoeConfig);

		this.generateWarLogic.generateWar(additionalContent, new File(bean.getTomcatWebappPath() + File.separator + Constants.WEBAPP_NAME + File.separator + Constants.ESOE_EXPLODED_DIR), new File(bean.getWriteableDirectory() + File.separator + Constants.ESOE_EXPLODED_DIR), new File(bean.getWriteableDirectory() + File.separator + Constants.ESOE_WAR_NAME));
	}

	private void createESOEManagerSPEPWar(ESOEBean bean) throws RenderConfigException, GenerateWarException
	{
		/* Create the ESOE Manager SPEP WAR File */
		AdditionalContent keyStore = new AdditionalContent();
		keyStore.setPath(Constants.WEBINF + File.separator + Constants.ESOE_MANAGER_SPEP_KEYSTORE_NAME);
		keyStore.setFileContent(bean.getEsoeManagerKeystore());

		for (ServiceNodeBean service : bean.getManagerServiceNodes())
		{
			Integer nodeID = new Integer(service.getNodeID());
			AdditionalContent esoeManagerConfig = new AdditionalContent();
			esoeManagerConfig.setPath(Constants.WEBINF + File.separator + Constants.ESOEMANAGERSPEPCONFIG);
			esoeManagerConfig.setFileContent(this.renderESOEConfigLogic.renderESOEManagerSPEPConfig(bean, nodeID.intValue()).getBytes());

			List<AdditionalContent> additionalContent = new ArrayList<AdditionalContent>();
			additionalContent.add(keyStore);
			additionalContent.add(esoeManagerConfig);

			this.generateWarLogic.generateWar(additionalContent, new File(bean.getTomcatWebappPath() + File.separator + Constants.WEBAPP_NAME + File.separator + Constants.ESOE_MANAGER_SPEP_EXPLODED_DIR), new File(bean.getWriteableDirectory() + File.separator + Constants.ESOE_MANAGER_SPEP_EXPLODED_DIR), new File(bean.getWriteableDirectory() + File.separator + Constants.ESOE_MANAGER_SPEP_WAR_NAME + "." + nodeID.toString()));
		}
	}

	private void createESOEManagerWar(ESOEBean bean) throws RenderConfigException, GenerateWarException
	{
		/* Create the ESOE Manager WAR File */
		AdditionalContent keyStore = new AdditionalContent();
		keyStore.setPath(Constants.WEBINF + File.separator + Constants.METADATA_KEYSTORE_NAME);
		keyStore.setFileContent(bean.getEsoeMetadataKeystore());

		AdditionalContent esoeManagerConfig = new AdditionalContent();
		esoeManagerConfig.setPath(Constants.WEBINF + File.separator + Constants.ESOEMANAGERCONFIG);
		esoeManagerConfig.setFileContent(this.renderESOEConfigLogic.renderESOEManagerConfig(bean, 0).getBytes());

		List<AdditionalContent> additionalContent = new ArrayList<AdditionalContent>();
		additionalContent.add(keyStore);
		additionalContent.add(esoeManagerConfig);

		this.generateWarLogic.generateWar(additionalContent, new File(bean.getTomcatWebappPath() + File.separator + Constants.WEBAPP_NAME + File.separator + Constants.ESOE_MANAGER_EXPLODED_DIR), new File(bean.getWriteableDirectory() + File.separator + Constants.ESOE_MANAGER_EXPLODED_DIR), new File(bean.getWriteableDirectory() + File.separator + Constants.ESOE_MANAGER_WAR_NAME));
	}

	private void createESOEDescriptors(ESOEBean bean, KeyPair esoeKeyPair, String esoeKeyPairName) throws RegisterESOEException
	{
		String entityID;
		KeyDescriptor keyDescriptor;
		try
		{
			entityID = (this.identifierGenerator.generateSAMLID());
			bean.setIdpEntityID(entityID);

			this.esoeDAO.insertEntityDescriptor(entityID, bean.getEsoeOrganizationName(), bean.getEsoeOrganizationDisplayName(), bean.getEsoeOrganizationURL(), Constants.ENTITY_ACTIVE);
		}
		catch (ESOEDAOException e)
		{
			this.logger.error("ESOEDAOException when attempting to configure ESOE details " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new RegisterESOEException("ESOEDAOException when attempting to configure ESOE details " + e.getLocalizedMessage());
		}

		setContacts(entityID, bean);

		try
		{
			/* Setup keyDescriptor */
			keyDescriptor = this.cryptoProcessor.createSigningKeyDescriptor((RSAPublicKey) esoeKeyPair.getPublic(), esoeKeyPairName);

			/*
			 * We consider each of these components part of the single ESOE system therefore they each use the same key
			 * details
			 */
			createIDPDescriptor(bean, keyDescriptor);
			createAtributeAuthorityDescriptor(bean, keyDescriptor);
			createLXACMLPDPDescriptor(bean, keyDescriptor);
		}
		catch (MarshallerException e)
		{
			this.logger.error("Marshaller exception when attempting to create ESOE xml descriptor " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new RegisterESOEException("Marshaller exception when attempting to create ESOE xml descriptor " + e.getLocalizedMessage());
		}
		catch (ESOEDAOException e)
		{
			this.logger.error("ESOEDAOException when attempting to configure insert ESOE xml descriptor details " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new RegisterESOEException("ESOEDAOException when attempting to configure insert ESOE xml descriptor details " + e.getLocalizedMessage());
		}
	}

	private void createESOEManagerDescriptors(ESOEBean bean, KeyPair esoeManagerKeyPair, String esoeManagerKeyPairName) throws RegisterESOEException
	{
		String entityID;
		KeyDescriptor keyDescriptor;
		try
		{
			entityID = (this.identifierGenerator.generateSAMLID());
			bean.setSpEntityID(entityID);

			this.esoeDAO.insertEntityDescriptor(entityID, bean.getEsoeOrganizationName(), bean.getEsoeOrganizationDisplayName(), bean.getEsoeOrganizationURL(), Constants.ENTITY_ACTIVE);
			this.esoeDAO.insertServiceDescription(entityID, bean.getManagerServiceName(), bean.getManagerServiceURL(), bean.getManagerServiceDescription(), bean.getManagerServiceAuthzFail());
		}
		catch (ESOEDAOException e)
		{
			this.logger.error("ESOEDAOException when attempting to configure ESOE details " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new RegisterESOEException("ESOEDAOException when attempting to configure ESOE details " + e.getLocalizedMessage());
		}

		setContacts(entityID, bean);

		try
		{
			/* Setup keyDescriptor */
			keyDescriptor = this.cryptoProcessor.createSigningKeyDescriptor((RSAPublicKey) esoeManagerKeyPair.getPublic(), esoeManagerKeyPairName);

			/*
			 * We consider each of these components part of the single ESOE system therefore they each use the same key
			 * details
			 */
			createSPDescriptor(bean, keyDescriptor);
		}
		catch (MarshallerException e)
		{
			this.logger.error("Marshaller exception when attempting to create ESOE Manager xml descriptor " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new RegisterESOEException("Marshaller exception when attempting to create ESOE Manager xml descriptor " + e.getLocalizedMessage());
		}
		catch (ESOEDAOException e)
		{
			this.logger.error("ESOEDAOException when attempting to configure insert ESOE Manager xml descriptor details " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new RegisterESOEException("ESOEDAOException when attempting to configure insert ESOE Manager xml descriptor details " + e.getLocalizedMessage());
		}
	}

	private void setContacts(String entityID, ESOEBean bean) throws RegisterESOEException
	{
		for (ContactPersonBean contact : bean.getContacts())
		{
			/* Add a unique identifier to this contact person */
			contact.setContactID(this.util.generateID());

			/* Register the service contact points */
			this.logger.info("Adding contact person with name" + contact.getGivenName() + " " + contact.getSurName() + " and generated id of " + contact.getContactID());
			try
			{
				this.esoeDAO.insertServiceContacts(entityID, contact.getContactID(), contact.getContactType(), contact.getCompany(), contact.getGivenName(), contact.getSurName(), contact.getEmailAddress(), contact.getTelephoneNumber());
			}
			catch (ESOEDAOException e)
			{
				this.logger.error("ESOEDAOException when attempting to configure insert ESOE contacts " + e.getLocalizedMessage());
				this.logger.debug(e);
				throw new RegisterESOEException("ESOEDAOException when attempting to configure insert ESOE contacts " + e.getLocalizedMessage());
			}
		}
	}

	private void createIDPDescriptor(ESOEBean bean, KeyDescriptor keyDescriptor) throws MarshallerException, ESOEDAOException
	{
		IDPSSODescriptor idpDescriptor = new IDPSSODescriptor();
		SPEPStartupService spepStartupService = new SPEPStartupService();
		Element spepStartupServiceXML;
		String idpDescriptorXML;

		/* Setup SPEP Startup Service */
		spepStartupService.setBinding(BindingConstants.soap);
		spepStartupService.setLocation(bean.getEsoeNodeURL() + bean.getEsoeSPEPStartupService());
		spepStartupServiceXML = this.spsMarshaller.marshallUnSignedElement(spepStartupService);

		/* Setup IDP */
		EndpointType singleSignOnEndpoint = new EndpointType();
		singleSignOnEndpoint.setBinding(BindingConstants.httpPost);
		singleSignOnEndpoint.setLocation(bean.getEsoeNodeURL() + bean.getEsoeSingleSignOn());
		Extensions extenstions = new Extensions();
		extenstions.getImplementedExtensions().add(spepStartupServiceXML);

		idpDescriptor.setID(this.identifierGenerator.generateSAMLID());
		idpDescriptor.setWantAuthnRequestsSigned(true);
		idpDescriptor.getAttributeProfiles().add(AttributeFormatConstants.basic);
		idpDescriptor.getProtocolSupportEnumerations().add(ProtocolConstants.protocol);
		idpDescriptor.getSingleSignOnServices().add(singleSignOnEndpoint);
		idpDescriptor.setExtensions(extenstions);
		idpDescriptor.getKeyDescriptors().add(keyDescriptor);

		idpDescriptorXML = this.idpMarshaller.marshallUnSigned(idpDescriptor);
		this.esoeDAO.insertDescriptor(bean.getIdpEntityID(), idpDescriptor.getID(), idpDescriptorXML, this.util.getRoleDescriptorTypeId(IDPSSODescriptor.class.getCanonicalName()));

		bean.setIdpDescriptorXML(idpDescriptorXML);
	}

	private void createSPDescriptor(ESOEBean bean, KeyDescriptor keyDescriptor) throws MarshallerException, ESOEDAOException
	{
		/*
		 * Determine how many SP records were submitted for ID generation for endpoints this will assimilate each SP
		 * created into a single record per metadata design
		 */
		SPSSODescriptor spDescriptor = new SPSSODescriptor();
		String spDescriptorXML;

		spDescriptor.setID(this.identifierGenerator.generateSAMLID());
		bean.setSpDescriptorID(spDescriptor.getID());

		for (ServiceNodeBean node : bean.getManagerServiceNodes())
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
			extensions.getImplementedExtensions().add(this.cacheClearMarshaller.marshallUnSignedElement(cacheClearService));
			spDescriptor.setExtensions(extensions);
		}

		spDescriptor.setAuthnRequestsSigned(true);
		spDescriptor.setWantAssertionsSigned(true);
		spDescriptor.getProtocolSupportEnumerations().add(ProtocolConstants.protocol);
		spDescriptor.getKeyDescriptors().add(keyDescriptor);

		spDescriptorXML = this.spMarshaller.marshallUnSigned(spDescriptor);

		this.esoeDAO.insertDescriptor(bean.getSpEntityID(), spDescriptor.getID(), spDescriptorXML, this.util.getRoleDescriptorTypeId(SPSSODescriptor.class.getCanonicalName()));

		for (ServiceNodeBean node : bean.getManagerServiceNodes())
		{
			/* Write service node to data repository */
			this.esoeDAO.insertServiceNode(node.getNodeID(), spDescriptor.getID(), node.getNodeURL(), node.getAssertionConsumerService(), node.getSingleLogoutService(), node.getCacheClearService());
		}

		/*
		 * Set default policy for ESOEManager, generally this will be an allow all until admins correct using
		 * ESOEManager
		 */
		this.esoeDAO.insertServiceAuthorizationPolicy(spDescriptor.getID(), this.defaultPolicy, new Date());

		bean.setSpDescriptorXML(spDescriptorXML);
	}

	private void createAtributeAuthorityDescriptor(ESOEBean bean, KeyDescriptor keyDescriptor) throws MarshallerException, ESOEDAOException
	{
		AttributeAuthorityDescriptor aaDescriptor = new AttributeAuthorityDescriptor();
		String aaDescriptorXML;

		EndpointType attributeService = new EndpointType();
		attributeService.setBinding(BindingConstants.soap);
		attributeService.setLocation(bean.getEsoeNodeURL() + bean.getEsoeAttributeService());

		aaDescriptor.setID(this.identifierGenerator.generateSAMLID());
		aaDescriptor.getProtocolSupportEnumerations().add(ProtocolConstants.protocol);
		aaDescriptor.getAttributeServices().add(attributeService);
		aaDescriptor.getKeyDescriptors().add(keyDescriptor);

		aaDescriptorXML = this.aaMarshaller.marshallUnSigned(aaDescriptor);

		this.esoeDAO.insertDescriptor(bean.getIdpEntityID(), aaDescriptor.getID(), aaDescriptorXML, this.util.getRoleDescriptorTypeId(AttributeAuthorityDescriptor.class.getCanonicalName()));

		bean.setAaDescriptorXML(aaDescriptorXML);
	}

	private void createLXACMLPDPDescriptor(ESOEBean bean, KeyDescriptor keyDescriptor) throws MarshallerException, ESOEDAOException
	{
		LXACMLPDPDescriptor pdpDescriptor = new LXACMLPDPDescriptor();
		String pdpDescriptorXML;

		EndpointType authzService = new EndpointType();
		authzService.setBinding(BindingConstants.soap);
		authzService.setLocation(bean.getEsoeNodeURL() + bean.getEsoeLxacmlService());

		pdpDescriptor.setID(this.identifierGenerator.generateSAMLID());
		pdpDescriptor.getProtocolSupportEnumerations().add(ProtocolConstants.protocol);
		pdpDescriptor.getAuthzServices().add(authzService);
		pdpDescriptor.getKeyDescriptors().add(keyDescriptor);

		pdpDescriptorXML = this.pdpMarshaller.marshallUnSigned(pdpDescriptor);

		this.esoeDAO.insertDescriptor(bean.getIdpEntityID(), pdpDescriptor.getID(), pdpDescriptorXML, this.util.getRoleDescriptorTypeId(LXACMLPDPDescriptor.class.getCanonicalName()));

		bean.setPdpDescriptorXML(pdpDescriptorXML);
	}

	private void createKeyStores(ESOEBean bean) throws CryptoException, CertificateException, FileNotFoundException, KeyStoreException, IOException, NoSuchAlgorithmException
	{
		KeyPair esoeKeyPair;
		String esoeKeyPairName;
		KeyPair esoeManagerKeyPair;
		String esoeManagerKeyPairName;

		/*
		 * Create keystore for Authentication Network Metadata. This public key will be inserted in every keystore
		 * created for an IDP, SP etc so that metadata documents may be validated. This will be stored as
		 * metadataKeystore.ks and supplied to the ESOEManager webapp.
		 */
		KeyStore metadataKeyStore;
		KeyPair mdKeyPair;
		String mdKeyStorePassphrase = this.generatePassphrase();
		String mdKeyPairName = this.identifierGenerator.generateXMLKeyName();
		String mdKeyPairPassphrase = this.generatePassphrase();

		bean.setEsoeMetadataKeyStorePassphrase(mdKeyStorePassphrase);
		bean.setEsoeMetadataKeyPairName(mdKeyPairName);
		bean.setEsoeMetadataKeyPairPassphrase(mdKeyPairPassphrase);

		metadataKeyStore = this.cryptoProcessor.generateKeyStore();
		mdKeyPair = this.cryptoProcessor.generateKeyPair();
		this.cryptoProcessor.addKeyPair(metadataKeyStore, mdKeyStorePassphrase, mdKeyPair, mdKeyPairName, mdKeyPairPassphrase, bean.getMetadataIssuerDN());

		serializeKeyStore(metadataKeyStore, mdKeyStorePassphrase, bean.getWriteableDirectory() + File.separatorChar + Constants.METADATA_KEYSTORE_NAME);

		bean.setEsoeMetadataKeystore(this.cryptoProcessor.convertKeystoreByteArray(metadataKeyStore, mdKeyStorePassphrase));

		/*
		 * Create keystore for ESOE, the generated keypair is configured to be used by the IDP, AA, LXACML PDP and other
		 * ESOE services to reduce the number of keys present in metadata
		 */
		KeyStore esoeKeyStore;
		String esoeKeyStorePassphrase = this.generatePassphrase();
		esoeKeyPairName = this.identifierGenerator.generateXMLKeyName();
		String esoeKeyPairPassphrase = this.generatePassphrase();

		bean.setEsoeKeyStorePassphrase(esoeKeyStorePassphrase);
		bean.setEsoeKeyPairName(esoeKeyPairName);
		bean.setEsoeKeyPairPassphrase(esoeKeyPairPassphrase);

		esoeKeyStore = this.cryptoProcessor.generateKeyStore();
		esoeKeyPair = this.cryptoProcessor.generateKeyPair();
		bean.setEsoeKeyPair(esoeKeyPair);
		this.cryptoProcessor.addKeyPair(esoeKeyStore, esoeKeyStorePassphrase, esoeKeyPair, esoeKeyPairName, esoeKeyPairPassphrase, this.generateSubjectDN(bean.getEsoeNodeURL()));
		this.cryptoProcessor.addPublicKey(esoeKeyStore, mdKeyPair, mdKeyPairName, bean.getMetadataIssuerDN());

		serializeKeyStore(esoeKeyStore, esoeKeyStorePassphrase, bean.getWriteableDirectory() + File.separatorChar + Constants.ESOE_KEYSTORE_NAME);

		bean.setEsoeKeystore(this.cryptoProcessor.convertKeystoreByteArray(esoeKeyStore, esoeKeyStorePassphrase));

		/*
		 * Create keystore for ESOE Manager SPEP
		 */
		KeyStore esoeManagerKeyStore;
		String esoeManagerKeyStorePassphrase = this.generatePassphrase();
		esoeManagerKeyPairName = this.identifierGenerator.generateXMLKeyName();
		String esoeManagerKeyPairPassphrase = this.generatePassphrase();

		bean.setEsoeManagerKeyStorePassphrase(esoeManagerKeyStorePassphrase);
		bean.setEsoeManagerKeyPairName(esoeManagerKeyPairName);
		bean.setEsoeManagerKeyPairPassphrase(esoeManagerKeyPairPassphrase);

		esoeManagerKeyStore = this.cryptoProcessor.generateKeyStore();
		esoeManagerKeyPair = this.cryptoProcessor.generateKeyPair();
		bean.setEsoeManagerKeyPair(esoeManagerKeyPair);
		this.cryptoProcessor.addKeyPair(esoeManagerKeyStore, esoeManagerKeyStorePassphrase, esoeManagerKeyPair, esoeManagerKeyPairName, esoeManagerKeyPairPassphrase, this.generateSubjectDN(bean.getManagerServiceURL()));
		this.cryptoProcessor.addPublicKey(esoeManagerKeyStore, mdKeyPair, mdKeyPairName, bean.getMetadataIssuerDN());

		serializeKeyStore(esoeManagerKeyStore, esoeManagerKeyStorePassphrase, bean.getWriteableDirectory() + File.separatorChar + Constants.ESOE_MANAGER_SPEP_KEYSTORE_NAME);

		bean.setEsoeManagerKeystore(this.cryptoProcessor.convertKeystoreByteArray(esoeManagerKeyStore, esoeManagerKeyStorePassphrase));
	}

	/*
	 * Takes the service URL from the supplied bean and creates a subject DN
	 */
	private String generateSubjectDN(String dn)
	{
		try
		{
			String result = new String();
			URL serviceURL = new URL(dn);
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
			return "dc=" + dn;
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
			this.logger.fatal("NoSuchAlgorithmException when trying to create SecureRandom instance " + nsae.getLocalizedMessage()); //$NON-NLS-1$
			this.logger.debug(nsae.getLocalizedMessage(), nsae);
			random = new SecureRandom();
		}

		buf = new byte[Constants.PASSPHRASE_LENGTH];
		random.nextBytes(buf);
		passphrase = new String(Hex.encodeHex(buf));

		return passphrase;
	}

	private void serializeKeyStore(KeyStore keyStore, String keyStorePassphrase, String filename) throws FileNotFoundException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException
	{
		FileOutputStream fos = null;
		try
		{
			fos = new FileOutputStream(filename);
			keyStore.store(fos, keyStorePassphrase.toCharArray());
		}
		finally
		{
			if (fos != null)
			{
				fos.flush();
				fos.close();
			}
		}
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

	private Vector<String> loadDatabaseCreationSQL(File generationSQL) throws IOException
	{
		/* Load database creation sql */
		Vector<String> result = new Vector<String>();
		String tmp;
		InputStream fileStream = null;
		Reader reader = null;
		BufferedReader in = null;
		try
		{
			fileStream = new FileInputStream(generationSQL);
			reader = new InputStreamReader(fileStream);
			in = new BufferedReader(reader);

			while ((tmp = in.readLine()) != null)
			{
				result.add(tmp);
			}

			return result;
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
}

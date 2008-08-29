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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.Calendar;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.w3c.dom.Element;

import com.qut.middleware.crypto.CryptoProcessor;
import com.qut.middleware.crypto.exception.CryptoException;
import com.qut.middleware.esoemanager.UtilityFunctions;
import com.qut.middleware.esoemanager.bean.ContactPersonBean;
import com.qut.middleware.esoemanager.bean.ServiceNodeBean;
import com.qut.middleware.esoemanager.exception.RenderConfigException;
import com.qut.middleware.esoestartup.Constants;
import com.qut.middleware.esoestartup.bean.ESOEBean;
import com.qut.middleware.esoestartup.exception.ESOEDAOException;
import com.qut.middleware.esoestartup.exception.RegisterESOEException;
import com.qut.middleware.esoestartup.processor.CryptoProcessorESOEImpl;
import com.qut.middleware.esoestartup.sqlmap.ESOEDAO;
import com.qut.middleware.saml2.AttributeFormatConstants;
import com.qut.middleware.saml2.BindingConstants;
import com.qut.middleware.saml2.NameIDFormatConstants;
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
import com.qut.middleware.saml2.schemas.metadata.SPSSODescriptor;
import com.qut.middleware.saml2.schemas.metadata.extensions.CacheClearService;
import com.qut.middleware.saml2.schemas.metadata.extensions.SPEPStartupService;
import com.qut.middleware.saml2.schemas.metadata.lxacml.LXACMLPDPDescriptor;
import com.qut.middleware.tools.war.logic.GenerateWarLogic;

public class RegisterESOELogic
{
	GenerateWarLogic generateWarLogic;
	RenderESOEConfigLogic renderESOEConfigLogic;

	private CryptoProcessor cryptoProcessor;
	private IdentifierGenerator identifierGenerator;

	private BasicDataSource dataSource;

	private ESOEDAO esoeDAO;
	private UtilityFunctions util;

	private byte[] defaultPolicyData;
	private byte[] defaultManagementPolicyData;
	private byte[] defaultAttributePolicyData;
	private String defaultPolicyID;
	private String defaultManagementPolicyID;

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
	private Logger logger = LoggerFactory.getLogger(RegisterESOELogic.class.getName());

	public RegisterESOELogic(GenerateWarLogic generateWarLogic, RenderESOEConfigLogic renderESOEConfigLogic, IdentifierGenerator identifierGenerator, ESOEDAO esoeDAO, BasicDataSource dataSource, File policyFile, File managementPolicyFile, File attributePolicyFile) throws MarshallerException, IOException
	{
		this.generateWarLogic = generateWarLogic;
		this.renderESOEConfigLogic = renderESOEConfigLogic;
		this.identifierGenerator = identifierGenerator;
		this.esoeDAO = esoeDAO;
		this.dataSource = dataSource;

		this.util = new UtilityFunctions();
		this.cryptoProcessor = new CryptoProcessorESOEImpl();

		this.loadDefaultPolicy(policyFile);
		this.loadDefaultManagementPolicy(managementPolicyFile);
		this.loadDefaultAttributePolicy(attributePolicyFile);

		this.idpMarshaller = new MarshallerImpl<IDPSSODescriptor>(IDPSSODescriptor.class.getPackage().getName(), this.idpSchemas);
		this.spsMarshaller = new MarshallerImpl<SPEPStartupService>(SPEPStartupService.class.getPackage().getName(), this.spsSchemas);
		this.aaMarshaller = new MarshallerImpl<AttributeAuthorityDescriptor>(AttributeAuthorityDescriptor.class.getPackage().getName(), this.aaSchemas);
		this.pdpMarshaller = new MarshallerImpl<LXACMLPDPDescriptor>(LXACMLPDPDescriptor.class.getPackage().getName(), this.pdpSchemas);
		this.spMarshaller = new MarshallerImpl<SPSSODescriptor>(this.MAR_PKGNAMES, this.spSchemas);
		this.cacheClearMarshaller = new MarshallerImpl<CacheClearService>(CacheClearService.class.getPackage().getName(), this.ccSchemas);
	}

	private void configureDataLayer(ESOEBean bean) throws RegisterESOEException
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
		}
		catch (DataAccessException e)
		{
			this.logger.error("Unable to connect with database " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new RegisterESOEException(e.getLocalizedMessage(), e);
		}
	}

	public void execute(ESOEBean bean) throws RegisterESOEException
	{
		configureDataLayer(bean);

		/* Setup Crypto Processor with additional details */
		this.cryptoProcessor.setCertIssuerDN(bean.getCertIssuerDN());
		this.cryptoProcessor.setCertIssuerEmail(bean.getCertIssuerEmailAddress());

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
			createESOEDescriptors(bean);
			createESOEManagerDescriptors(bean);
			
			/* Set expiry date of PKI data */
			Calendar expiryDate = Calendar.getInstance();
			expiryDate.add(Calendar.YEAR, this.cryptoProcessor.getCertExpiryIntervalInYears());
			
			/* Store all crypto in data repository */
			
			/* ESOE */
			this.esoeDAO.insertPKIData(bean.getEsoeIdpDescID(), expiryDate.getTime(), bean.getEsoeKeystore(), bean.getEsoeKeyStorePassphrase(), bean.getEsoeKeyPairName(), bean.getEsoeKeyPairPassphrase());
			this.esoeDAO.insertPKIData(bean.getEsoeAADescID(), expiryDate.getTime(), bean.getEsoeKeystore(), bean.getEsoeKeyStorePassphrase(), bean.getEsoeKeyPairName(), bean.getEsoeKeyPairPassphrase());
			this.esoeDAO.insertPKIData(bean.getEsoeLxacmlDescID(), expiryDate.getTime(), bean.getEsoeKeystore(), bean.getEsoeKeyStorePassphrase(), bean.getEsoeKeyPairName(), bean.getEsoeKeyPairPassphrase());
			
			this.esoeDAO.insertPublicKey(bean.getEsoeIdpDescID(), expiryDate.getTime(), bean.getEsoeKeyPairName(), this.cryptoProcessor.convertPublicKeyByteArray(bean.getEsoeKeyPair().getPublic()));
			this.esoeDAO.insertPublicKey(bean.getEsoeAADescID(), expiryDate.getTime(), bean.getEsoeKeyPairName(), this.cryptoProcessor.convertPublicKeyByteArray(bean.getEsoeKeyPair().getPublic()));
			this.esoeDAO.insertPublicKey(bean.getEsoeLxacmlDescID(), expiryDate.getTime(), bean.getEsoeKeyPairName(), this.cryptoProcessor.convertPublicKeyByteArray(bean.getEsoeKeyPair().getPublic()));
			
			/* ESOE Manager SPEP */
			this.esoeDAO.insertPKIData(bean.getDescID(), expiryDate.getTime(), bean.getEsoeManagerKeystore(), bean.getEsoeManagerKeyStorePassphrase(), bean.getEsoeManagerKeyPairName(), bean.getEsoeManagerKeyPairPassphrase());
			this.esoeDAO.insertPublicKey(bean.getDescID(), expiryDate.getTime(), bean.getEsoeManagerKeyPairName(), this.cryptoProcessor.convertPublicKeyByteArray(bean.getEsoeManagerKeyPair().getPublic()));

			/* Authentication network Metadata crypto */
			this.esoeDAO.insertMetadataPKIData(expiryDate.getTime(), bean.getEsoeMetadataKeystore(), bean.getEsoeMetadataKeyStorePassphrase(), bean.getEsoeMetadataKeyPairName(), bean.getEsoeMetadataKeyPairPassphrase());
			
			
			/*
			 * Dynamically create config files for user deployments so they dont need to do manual config, nice chaps
			 * aren't we.
			 */
			writeESOEContent(bean);
			writeESOEManagerContent(bean);
			writeSPEPContent(bean);

		}
		catch (FileNotFoundException e)
		{
			this.logger.error("FileNotFoundException when attempting to configure ESOE details " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new RegisterESOEException("FileNotFoundException when attempting to configure ESOE details " + e.getLocalizedMessage());
		}
		catch (KeyStoreException e)
		{
			this.logger.error("KeyStoreException when attempting to configure ESOE details " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new RegisterESOEException("KeyStoreException when attempting to configure ESOE details " + e.getLocalizedMessage());
		}
		catch (NoSuchAlgorithmException e)
		{
			this.logger.error("NoSuchAlgorithmException when attempting to configure ESOE details " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new RegisterESOEException("NoSuchAlgorithmException when attempting to configure ESOE details " + e.getLocalizedMessage());
		}
		catch (CertificateException e)
		{
			this.logger.error("CertificateException when attempting to configure ESOE details " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new RegisterESOEException("CertificateException when attempting to configure ESOE details " + e.getLocalizedMessage());
		}
		catch (CryptoException e)
		{
			this.logger.error("CryptoException when attempting to configure ESOE details " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new RegisterESOEException("CryptoException when attempting to configure ESOE details " + e.getLocalizedMessage());
		}
		catch (IOException e)
		{
			this.logger.error("IOException when attempting to configure ESOE details " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new RegisterESOEException("IOException when attempting to configure ESOE details " + e.getLocalizedMessage());
		}
		catch (RenderConfigException e)
		{
			this.logger.error("RenderConfigException when attempting to configure ESOE details " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new RegisterESOEException("RenderConfigException when attempting to configure ESOE details " + e.getLocalizedMessage());
		}
		catch (ESOEDAOException e)
		{
			this.logger.error("ESOEDAOException when attempting to configure ESOE details " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new RegisterESOEException("ESOEDAOException when attempting to configure ESOE details " + e.getLocalizedMessage());
		}
	}
	
	private void writeESOEContent(ESOEBean bean) throws FileNotFoundException, RenderConfigException, IOException
	{
		FileOutputStream configOutput = null; 
		FileOutputStream keystoreOutput = null;
		
		File configDir = new File(bean.getEsoeDataDirectory()+ Constants.CONFIG_DIR);
		
		if(!configDir.exists() && !configDir.mkdir())
			throw new IOException ("Unable to create config directory, structure not writeable");
		
		File loggingDir = new File(bean.getEsoeDataDirectory()+ Constants.LOGGING_DIR);
		
		if(!loggingDir.exists() && !loggingDir.mkdir())
			throw new IOException ("Unable to create config directory, structure not writeable");
		
		try
		{
			/* Write config file */
			configOutput = new FileOutputStream(bean.getEsoeDataDirectory() + Constants.CONFIG_DIR + File.separatorChar + Constants.ESOECONFIG);
			configOutput.write(this.renderESOEConfigLogic.renderESOEConfig(bean, 0).getBytes());
			configOutput.flush();
			
			/* Write ESOE keystore file */
			keystoreOutput = new FileOutputStream(bean.getEsoeDataDirectory() + Constants.CONFIG_DIR + File.separatorChar + Constants.ESOE_KEYSTORE_NAME);
			keystoreOutput.write(bean.getEsoeKeystore());
			keystoreOutput.flush();
			
		}
		finally
		{
			if(configOutput != null)
				configOutput.close();
			
			if(keystoreOutput != null)
				keystoreOutput.close();
		}
	}
	
	private void writeESOEManagerContent(ESOEBean bean) throws FileNotFoundException, RenderConfigException, IOException
	{
		FileOutputStream configOutput = null;
		FileOutputStream keystoreOutput = null;
		
		File configDir = new File(bean.getEsoemanagerDataDirectory()+ Constants.CONFIG_DIR);
		if(!configDir.exists() && !configDir.mkdir())
			throw new IOException ("Unable to create config directory, structure not writeable");
		
		File metadataHistoryDir = new File(bean.getEsoemanagerDataDirectory()+ Constants.MD_HISTORICAL_DIR);
		if((!metadataHistoryDir.exists()) && (!metadataHistoryDir.mkdir()) )
		{
			throw new IOException ("Unable to create metadata history directory, structure not writeable for " + metadataHistoryDir.getPath());
		}
		
		File loggingDir = new File(bean.getEsoemanagerDataDirectory()+ Constants.LOGGING_DIR);
		if(!loggingDir.exists() && !loggingDir.mkdir())
			throw new IOException ("Unable to create config directory, structure not writeable");
		
		try
		{
			/* Write config file */
			configOutput = new FileOutputStream(bean.getEsoemanagerDataDirectory() + Constants.CONFIG_DIR + File.separatorChar + Constants.ESOEMANAGERCONFIG);
			configOutput.write(this.renderESOEConfigLogic.renderESOEManagerConfig(bean, 0).getBytes());
			configOutput.flush();
			
			/* Write Metadata Keystore */
			keystoreOutput = new FileOutputStream(bean.getEsoemanagerDataDirectory() + Constants.CONFIG_DIR + File.separatorChar + Constants.METADATA_KEYSTORE_NAME);
			keystoreOutput.write(bean.getEsoeMetadataKeystore());
			keystoreOutput.flush();
			
		}
		finally
		{
			if(configOutput != null)
				configOutput.close();
			
			if(keystoreOutput != null)
				keystoreOutput.close();
		}
	}
	
	private void writeSPEPContent(ESOEBean bean) throws FileNotFoundException, RenderConfigException, IOException
	{
		FileOutputStream configOutput = null; 
		FileOutputStream keystoreOutput = null;
		
		File configDir = new File(bean.getSpepDataDirectory()+ Constants.CONFIG_DIR);
		
		if(!configDir.exists() && !configDir.mkdir())
			throw new IOException ("Unable to create config directory, structure not writeable");
		
		File loggingDir = new File(bean.getSpepDataDirectory()+ Constants.LOGGING_DIR);
		if(!loggingDir.exists() && !loggingDir.mkdir())
			throw new IOException ("Unable to create config directory, structure not writeable");
		
		try
		{
			/* Write config file(s) */	
			int x = 0;
			for (ServiceNodeBean service : bean.getServiceNodes())
			{
				Integer nodeID = new Integer(service.getNodeID());
				if(x == 0)
					configOutput = new FileOutputStream(bean.getSpepDataDirectory() + Constants.CONFIG_DIR + File.separatorChar + Constants.ESOEMANAGERSPEPCONFIG);
				else
					configOutput = new FileOutputStream(bean.getSpepDataDirectory() + Constants.CONFIG_DIR + File.separatorChar + Constants.ESOEMANAGERSPEPCONFIG + "." + x);
				
				configOutput.write(this.renderESOEConfigLogic.renderESOEManagerSPEPConfig(bean, nodeID.intValue()).getBytes());
				configOutput.flush();
				configOutput.close();
				x++;
			}
			
			/* Write SPEP keystore file */
			keystoreOutput = new FileOutputStream(bean.getSpepDataDirectory() + Constants.CONFIG_DIR + File.separatorChar + Constants.ESOE_MANAGER_SPEP_KEYSTORE_NAME);
			keystoreOutput.write(bean.getEsoeManagerKeystore());
			keystoreOutput.flush();
			
		}
		finally
		{
			if(configOutput != null)
				configOutput.close();
			
			if(keystoreOutput != null)
				keystoreOutput.close();
		}
	}

	private void createESOEDescriptors(ESOEBean bean) throws RegisterESOEException
	{
		try
		{
			bean.setEsoeEntID(this.esoeDAO.getNextEntID());
			bean.setEsoeIdpDescID(this.esoeDAO.getNextDescID());
			bean.setEsoeAADescID(this.esoeDAO.getNextDescID());
			bean.setEsoeLxacmlDescID(this.esoeDAO.getNextDescID());
			this.esoeDAO.insertEntityDescriptor(bean.getEsoeEntID(), bean.getIdpEntityID(), bean.getEsoeOrganizationName(), bean.getEsoeOrganizationDisplayName(), bean.getEsoeOrganizationURL(), Constants.ENTITY_ACTIVE);
		}
		catch (ESOEDAOException e)
		{
			this.logger.error("ESOEDAOException when attempting to configure ESOE details " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new RegisterESOEException("ESOEDAOException when attempting to configure ESOE details " + e.getLocalizedMessage());
		}

		setContacts(bean.getEsoeEntID(), bean);

		try
		{
			createIDPDescriptor(bean);
			createAtributeAuthorityDescriptor(bean);
			createLXACMLPDPDescriptor(bean);
			
			/* Setup attribute release for this ESOE Instance */
			this.esoeDAO.insertAttributePolicy(bean.getEsoeEntID(), this.defaultAttributePolicyData);
		}
		catch (MarshallerException e)
		{
			this.logger.error("Marshaller exception when attempting to create ESOE xml descriptor " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new RegisterESOEException("Marshaller exception when attempting to create ESOE xml descriptor " + e.getLocalizedMessage());
		}
		catch (ESOEDAOException e)
		{
			this.logger.error("ESOEDAOException when attempting to configure insert ESOE xml descriptor details " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new RegisterESOEException("ESOEDAOException when attempting to configure insert ESOE xml descriptor details " + e.getLocalizedMessage());
		}
	}

	private void createESOEManagerDescriptors(ESOEBean bean) throws RegisterESOEException
	{
		try
		{
			bean.setEntID(this.esoeDAO.getNextEntID());
			bean.setDescID(this.esoeDAO.getNextDescID());
			this.esoeDAO.insertEntityDescriptor(bean.getEntID(), bean.getEntityID(), bean.getEsoeOrganizationName(), bean.getEsoeOrganizationDisplayName(), bean.getEsoeOrganizationURL(), Constants.ENTITY_ACTIVE);
			this.esoeDAO.insertServiceDescription(bean.getEntID(), bean.getServiceName(), bean.getServiceURL(), bean.getServiceDescription(), bean.getServiceAuthzFailureMsg());
		}
		catch (ESOEDAOException e)
		{
			this.logger.error("ESOEDAOException when attempting to configure ESOE details " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new RegisterESOEException("ESOEDAOException when attempting to configure ESOE details " + e.getLocalizedMessage());
		}

		setContacts(bean.getEntID(), bean);

		try
		{
			createSPDescriptor(bean);
		}
		catch (MarshallerException e)
		{
			this.logger.error("Marshaller exception when attempting to create ESOE Manager xml descriptor " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new RegisterESOEException("Marshaller exception when attempting to create ESOE Manager xml descriptor " + e.getLocalizedMessage());
		}
		catch (ESOEDAOException e)
		{
			this.logger.error("ESOEDAOException when attempting to configure insert ESOE Manager xml descriptor details " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new RegisterESOEException("ESOEDAOException when attempting to configure insert ESOE Manager xml descriptor details " + e.getLocalizedMessage());
		}
	}

	private void setContacts(Integer entID, ESOEBean bean) throws RegisterESOEException
	{
		for (ContactPersonBean contact : bean.getContacts())
		{
			/* Add a unique identifier to this contact person */
			contact.setContactID(this.util.generateID());

			/* Register the service contact points */
			this.logger.info("Adding contact person with name" + contact.getGivenName() + " " + contact.getSurName() + " and generated id of " + contact.getContactID());
			try
			{
				this.esoeDAO.insertServiceContacts(entID, contact.getContactID(), contact.getContactType(), contact.getCompany(), contact.getGivenName(), contact.getSurName(), contact.getEmailAddress(), contact.getTelephoneNumber());
			}
			catch (ESOEDAOException e)
			{
				this.logger.error("ESOEDAOException when attempting to configure insert ESOE contacts " + e.getLocalizedMessage());
				this.logger.debug(e.toString());
				throw new RegisterESOEException("ESOEDAOException when attempting to configure insert ESOE contacts " + e.getLocalizedMessage());
			}
		}
	}

	private void createIDPDescriptor(ESOEBean bean) throws MarshallerException, ESOEDAOException
	{
		IDPSSODescriptor idpDescriptor = new IDPSSODescriptor();
		SPEPStartupService spepStartupService = new SPEPStartupService();
		Element spepStartupServiceXML;
		byte[] idpDescriptorXML;

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
		idpDescriptor.getNameIDFormats().add(NameIDFormatConstants.trans);

		idpDescriptorXML = this.idpMarshaller.marshallUnSigned(idpDescriptor);
		this.esoeDAO.insertDescriptor(bean.getEsoeEntID(), bean.getEsoeIdpDescID(), idpDescriptor.getID(), idpDescriptorXML, this.util.getRoleDescriptorTypeId(IDPSSODescriptor.class.getCanonicalName()));

		bean.setIdpDescriptorXML(idpDescriptorXML);
	}

	private void createSPDescriptor(ESOEBean bean) throws MarshallerException, ESOEDAOException
	{
		/*
		 * Determine how many SP records were submitted for ID generation for endpoints this will assimilate each SP
		 * created into a single record per metadata design
		 */
		SPSSODescriptor spDescriptor = new SPSSODescriptor();
		byte[] spDescriptorXML;

		spDescriptor.setID(this.identifierGenerator.generateSAMLID());
		bean.setDescriptorID(spDescriptor.getID());

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
			assertionConsumerService.setLocation(bean.getEsoeManagerHost() + node.getAssertionConsumerService());
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
			extensions.getImplementedExtensions().add(this.cacheClearMarshaller.marshallUnSignedElement(cacheClearService));
			spDescriptor.setExtensions(extensions);
		}

		spDescriptor.setAuthnRequestsSigned(true);
		spDescriptor.setWantAssertionsSigned(true);
		spDescriptor.getProtocolSupportEnumerations().add(ProtocolConstants.protocol);
		spDescriptor.getNameIDFormats().add(NameIDFormatConstants.trans);

		spDescriptorXML = this.spMarshaller.marshallUnSigned(spDescriptor);

		this.esoeDAO.insertDescriptor(bean.getEntID(), bean.getDescID(), spDescriptor.getID(), spDescriptorXML, this.util.getRoleDescriptorTypeId(SPSSODescriptor.class.getCanonicalName()));

		for (ServiceNodeBean node : bean.getServiceNodes())
		{
			/* Write service node to data repository */
			this.esoeDAO.insertServiceNode(node.getNodeID(), bean.getDescID(), node.getNodeURL(), node.getAssertionConsumerService(), node.getSingleLogoutService(), node.getCacheClearService());
		}

		/*
		 * Set default policy for ESOEManager, generally this will be an allow all until administrators correct using
		 * ESOEManager, by default the policyID is 0
		 */
		this.esoeDAO.insertServiceAuthorizationPolicy(bean.getEntID(), this.defaultPolicyID, this.defaultPolicyData);
		this.esoeDAO.insertServiceAuthorizationPolicy(bean.getEntID(), this.defaultManagementPolicyID, this.defaultManagementPolicyData);

		bean.setDescriptorXML(spDescriptorXML);
	}

	private void createAtributeAuthorityDescriptor(ESOEBean bean) throws MarshallerException, ESOEDAOException
	{
		AttributeAuthorityDescriptor aaDescriptor = new AttributeAuthorityDescriptor();
		byte[] aaDescriptorXML;

		EndpointType attributeService = new EndpointType();
		attributeService.setBinding(BindingConstants.soap);
		attributeService.setLocation(bean.getEsoeNodeURL() + bean.getEsoeAttributeService());

		aaDescriptor.setID(this.identifierGenerator.generateSAMLID());
		aaDescriptor.getProtocolSupportEnumerations().add(ProtocolConstants.protocol);
		aaDescriptor.getAttributeServices().add(attributeService);

		aaDescriptorXML = this.aaMarshaller.marshallUnSigned(aaDescriptor);

		this.esoeDAO.insertDescriptor(bean.getEsoeEntID(), bean.getEsoeAADescID(), aaDescriptor.getID(), aaDescriptorXML, this.util.getRoleDescriptorTypeId(AttributeAuthorityDescriptor.class.getCanonicalName()));

		bean.setAaDescriptorXML(aaDescriptorXML);
	}

	private void createLXACMLPDPDescriptor(ESOEBean bean) throws MarshallerException, ESOEDAOException
	{
		LXACMLPDPDescriptor pdpDescriptor = new LXACMLPDPDescriptor();
		byte[] pdpDescriptorXML;

		EndpointType authzService = new EndpointType();
		authzService.setBinding(BindingConstants.soap);
		authzService.setLocation(bean.getEsoeNodeURL() + bean.getEsoeLxacmlService());

		pdpDescriptor.setID(this.identifierGenerator.generateSAMLID());
		pdpDescriptor.getProtocolSupportEnumerations().add(ProtocolConstants.protocol);
		pdpDescriptor.getAuthzServices().add(authzService);

		pdpDescriptorXML = this.pdpMarshaller.marshallUnSigned(pdpDescriptor);

		this.esoeDAO.insertDescriptor(bean.getEsoeEntID(), bean.getEsoeLxacmlDescID(), pdpDescriptor.getID(), pdpDescriptorXML, this.util.getRoleDescriptorTypeId(LXACMLPDPDescriptor.class.getCanonicalName()));

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
		this.cryptoProcessor.addKeyPair(esoeManagerKeyStore, esoeManagerKeyStorePassphrase, esoeManagerKeyPair, esoeManagerKeyPairName, esoeManagerKeyPairPassphrase, this.generateSubjectDN(bean.getServiceURL()));
		this.cryptoProcessor.addPublicKey(esoeManagerKeyStore, mdKeyPair, mdKeyPairName, bean.getMetadataIssuerDN());

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
			this.logger.debug(e.toString());
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
			this.logger.error("NoSuchAlgorithmException when trying to create SecureRandom instance " + nsae.getLocalizedMessage()); //$NON-NLS-1$
			this.logger.debug(nsae.getLocalizedMessage(), nsae);
			random = new SecureRandom();
		}

		buf = new byte[Constants.PASSPHRASE_LENGTH];
		random.nextBytes(buf);
		passphrase = new String(Hex.encodeHex(buf));

		return passphrase;
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

			/*
			 * The default policy is assigned ID 0 on disk, should this need to change for some reason the below
			 * assignment must be modified.
			 */
			this.defaultPolicyData = byteArray;
			this.defaultPolicyID = "spep-0";
		}
		finally
		{
			if (fileStream != null)
				fileStream.close();
		}
	}
	
	private void loadDefaultAttributePolicy(File attribPolicyFile) throws IOException
	{
		InputStream fileStream = null;

		try
		{
			long length = attribPolicyFile.length();
			byte[] byteArray = new byte[(int) length];

			fileStream = new FileInputStream(attribPolicyFile);
			fileStream.read(byteArray);
			fileStream.close();
			
			this.defaultAttributePolicyData = byteArray;
		}
		finally
		{
			if (fileStream != null)
				fileStream.close();
		}
	}

	private void loadDefaultManagementPolicy(File policyFile) throws IOException
	{
		InputStream fileStream = null;

		try
		{
			long length = policyFile.length();
			byte[] byteArray = new byte[(int) length];

			fileStream = new FileInputStream(policyFile);
			fileStream.read(byteArray);
			fileStream.close();

			/*
			 * The default policy is assigned ID 0 on disk, should this need to change for some reason the below
			 * assignment must be modified.
			 */
			this.defaultManagementPolicyData = byteArray;
			this.defaultManagementPolicyID = "manager-0";
		}
		finally
		{
			if (fileStream != null)
				fileStream.close();
		}
	}
}

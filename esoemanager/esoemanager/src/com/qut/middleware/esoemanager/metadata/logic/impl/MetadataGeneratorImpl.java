/* Copyright 2008, Queensland University of Technology
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
 */

package com.qut.middleware.esoemanager.metadata.logic.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3._2000._09.xmldsig_.Signature;

import com.qut.middleware.crypto.CryptoProcessor;
import com.qut.middleware.crypto.KeystoreResolver;
import com.qut.middleware.crypto.exception.CryptoException;
import com.qut.middleware.esoemanager.Constants;
import com.qut.middleware.esoemanager.exception.MetadataDAOException;
import com.qut.middleware.esoemanager.metadata.logic.MetadataGenerator;
import com.qut.middleware.esoemanager.metadata.sqlmap.MetadataDAO;
import com.qut.middleware.saml2.BindingConstants;
import com.qut.middleware.saml2.NameIDFormatConstants;
import com.qut.middleware.saml2.ProtocolConstants;
import com.qut.middleware.saml2.exception.MarshallerException;
import com.qut.middleware.saml2.exception.UnmarshallerException;
import com.qut.middleware.saml2.handler.Marshaller;
import com.qut.middleware.saml2.handler.Unmarshaller;
import com.qut.middleware.saml2.handler.impl.MarshallerImpl;
import com.qut.middleware.saml2.handler.impl.UnmarshallerImpl;
import com.qut.middleware.saml2.identifier.IdentifierGenerator;
import com.qut.middleware.saml2.schemas.metadata.AttributeAuthorityDescriptor;
import com.qut.middleware.saml2.schemas.metadata.ContactPerson;
import com.qut.middleware.saml2.schemas.metadata.ContactTypeType;
import com.qut.middleware.saml2.schemas.metadata.EndpointType;
import com.qut.middleware.saml2.schemas.metadata.EntitiesDescriptor;
import com.qut.middleware.saml2.schemas.metadata.EntityDescriptor;
import com.qut.middleware.saml2.schemas.metadata.Extensions;
import com.qut.middleware.saml2.schemas.metadata.IDPSSODescriptor;
import com.qut.middleware.saml2.schemas.metadata.IndexedEndpointType;
import com.qut.middleware.saml2.schemas.metadata.KeyDescriptor;
import com.qut.middleware.saml2.schemas.metadata.RoleDescriptorType;
import com.qut.middleware.saml2.schemas.metadata.SPSSODescriptor;
import com.qut.middleware.saml2.schemas.metadata.extensions.CacheClearService;
import com.qut.middleware.saml2.schemas.metadata.lxacml.LXACMLPDPDescriptor;

public class MetadataGeneratorImpl implements MetadataGenerator
{
	private MetadataDAO metadataDAO;
	private KeystoreResolver keystoreResolver;
	private CryptoProcessor cryptoProcessor;

	/* SAML Integration */
	private IdentifierGenerator identifierGenerator;
	private Unmarshaller<IDPSSODescriptor> idpUnmarshaller;
	private Unmarshaller<AttributeAuthorityDescriptor> attribAuthUnmarshaller;
	private Unmarshaller<LXACMLPDPDescriptor> lxacmlPDPUnmarshaller;
	private Marshaller<EntitiesDescriptor> metadataMarshaller;
	private Marshaller<CacheClearService> cacheClearMarshaller;
	private Marshaller<SPSSODescriptor> spMarshaller;

	private final String UNMAR_PKGNAMES = IDPSSODescriptor.class.getPackage().getName();
	private final String UNMAR_PKGNAMES3 = AttributeAuthorityDescriptor.class.getPackage().getName();
	private final String UNMAR_PKGNAMES4 = LXACMLPDPDescriptor.class.getPackage().getName() + ":"
			+ RoleDescriptorType.class.getPackage().getName();

	private final String MAR_PKGNAMES = EntitiesDescriptor.class.getPackage().getName() + ":"
			+ LXACMLPDPDescriptor.class.getPackage().getName();
	private final String MAR_PKGNAMES2 = CacheClearService.class.getPackage().getName();
	private final String MAR_PKGNAMES3 = SPSSODescriptor.class.getPackage().getName();

	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(MetadataGeneratorImpl.class.getName());

	MetadataGeneratorImpl(MetadataDAO metadataDAO, IdentifierGenerator identifierGenerator,
			KeystoreResolver keystoreResolver, CryptoProcessor cryptoProcessor) throws UnmarshallerException,
			MarshallerException
	{
		if (metadataDAO == null)
		{
			this.logger.error("Supplied metadataDAO was NULL for MetadataGeneratorImpl");
			throw new IllegalArgumentException("Supplied metadataDAO was NULL for MetadataGeneratorImpl");
		}
		if (identifierGenerator == null)
		{
			this.logger.error("Supplied identifierGenerator was NULL for MetadataGeneratorImpl");
			throw new IllegalArgumentException("Supplied identifierGenerator was NULL for MetadataGeneratorImpl");
		}
		if (keystoreResolver == null)
		{
			this.logger.error("Supplied keystoreResolver was NULL for MetadataGeneratorImpl");
			throw new IllegalArgumentException("Supplied keystoreResolver was NULL for MetadataGeneratorImpl");
		}
		if (cryptoProcessor == null)
		{
			this.logger.error("Supplied cryptoProcessor was NULL for MetadataGeneratorImpl");
			throw new IllegalArgumentException("Supplied cryptoProcessor was NULL for MetadataGeneratorImpl");
		}

		String[] schemas =
		{
				Constants.lxacmlMetadata, Constants.samlMetadata
		};

		this.metadataDAO = metadataDAO;
		this.identifierGenerator = identifierGenerator;
		this.keystoreResolver = keystoreResolver;
		this.cryptoProcessor = cryptoProcessor;

		this.idpUnmarshaller = new UnmarshallerImpl<IDPSSODescriptor>(this.UNMAR_PKGNAMES, schemas);
		this.spMarshaller = new MarshallerImpl<SPSSODescriptor>(this.MAR_PKGNAMES3, schemas);
		this.attribAuthUnmarshaller = new UnmarshallerImpl<AttributeAuthorityDescriptor>(this.UNMAR_PKGNAMES3, schemas);
		this.lxacmlPDPUnmarshaller = new UnmarshallerImpl<LXACMLPDPDescriptor>(this.UNMAR_PKGNAMES4, schemas);

		this.metadataMarshaller = new MarshallerImpl<EntitiesDescriptor>(this.MAR_PKGNAMES, schemas,
				this.keystoreResolver);
		this.cacheClearMarshaller = new MarshallerImpl<CacheClearService>(this.MAR_PKGNAMES2, schemas);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.qut.middleware.esoemanager.metadata.MetadataGenerator#generateMetadata
	 * ()
	 */
	public byte[] generateMetadata(boolean enableExtensions)
	{
		List<Integer> activeEntities;
		List<Map<String, String>> contacts;
		List<Map<String, Object>> idpDescriptors;
		List<Map<String, Object>> spDescriptors;
		List<Map<String, Object>> lxacmlPDPDescriptors;
		List<Map<String, Object>> attribAuthDescriptors;

		EntitiesDescriptor entitiesDescriptor;

		/* Build metadata document from data repository */
		entitiesDescriptor = new EntitiesDescriptor();
		entitiesDescriptor.setID(this.identifierGenerator.generateSAMLID());

		/* Ensure entities descriptor is signed */
		entitiesDescriptor.setSignature(new Signature());

		try
		{
			activeEntities = this.metadataDAO.queryActiveEntities();
			if (activeEntities != null)
			{

				for (Integer entID : activeEntities)
				{
					EntityDescriptor entityDescriptor = new EntityDescriptor();
					entityDescriptor.setEntityID(this.metadataDAO.getEntityID(entID));
					entityDescriptor.setID(this.identifierGenerator.generateSAMLID());

					contacts = this.metadataDAO.queryContacts(entID);
					for (Map<String, String> contact : contacts)
					{
						ContactPerson contactPerson = new ContactPerson();
						contactPerson.setCompany(contact.get(Constants.FIELD_CONTACT_COMPANY));
						contactPerson.setGivenName(contact.get(Constants.FIELD_CONTACT_GIVEN_NAME));
						contactPerson.setSurName(contact.get(Constants.FIELD_CONTACT_SURNAME));
						contactPerson.getEmailAddress().add(contact.get(Constants.FIELD_CONTACT_EMAIL_ADDRESS));
						contactPerson.getTelephoneNumbers().add(contact.get(Constants.FIELD_CONTACT_TELEPHONE_NUMBER));
						contactPerson.setContactType(ContactTypeType.fromValue(contact
								.get(Constants.FIELD_CONTACT_TYPE)));

						entityDescriptor.getContactPersons().add(contactPerson);
					}

					idpDescriptors = this.metadataDAO.queryIDPDescriptor(entID);
					for (Map<String, Object> idpDescriptor : idpDescriptors)
					{
						IDPSSODescriptor idp = this.idpUnmarshaller.unMarshallUnSigned((byte[]) idpDescriptor
								.get(Constants.FIELD_DESCRIPTOR_XML));

						// Remove any extenstion data if they are disabled
						if (!enableExtensions && idp.getExtensions() != null)
						{
							idp.setExtensions(null);
						}

						/*
						 * Retrieve all active public key data for this instance
						 * and insert into object
						 */
						List<Map<String, Object>> publicKeys = this.metadataDAO
								.queryDescriptorActivePublicKeys((Integer) idpDescriptor.get(Constants.FIELD_DESC_ID));
						for (Map<String, Object> publicKey : publicKeys)
						{
							RSAPublicKey key = (RSAPublicKey) this.cryptoProcessor
									.convertByteArrayPublicKey((byte[]) publicKey.get(Constants.FIELD_PK_BINARY));
							KeyDescriptor keyDescriptor = this.cryptoProcessor.createSigningKeyDescriptor(key,
									(String) publicKey.get(Constants.FIELD_PK_KEYPAIR_NAME), (String)publicKey.get(Constants.FIELD_PK_ISSUER), 
									(String)publicKey.get(Constants.FIELD_PK_SERIAL));
							idp.getKeyDescriptors().add(keyDescriptor);
						}

						entityDescriptor.getIDPDescriptorAndSSODescriptorAndRoleDescriptors().add(idp);
						this.logger.debug("Retrieved idpDescriptor of: \n"
								+ idpDescriptor.get(Constants.FIELD_DESCRIPTOR_XML));
					}

					List<SPSSODescriptor> spList = this.createSPSSODescriptor(entID, enableExtensions);
					for (SPSSODescriptor sp : spList)
					{
						entityDescriptor.getIDPDescriptorAndSSODescriptorAndRoleDescriptors().add(sp);
						this.logger.debug("Inserted SP descriptor for ID: " + sp.getID());
					}

					attribAuthDescriptors = this.metadataDAO.queryAttributeAuthorityDescriptor(entID);
					for (Map<String, Object> attribAuthDescriptor : attribAuthDescriptors)
					{
						AttributeAuthorityDescriptor aa = this.attribAuthUnmarshaller
								.unMarshallUnSigned((byte[]) attribAuthDescriptor.get(Constants.FIELD_DESCRIPTOR_XML));

						// Remove any extenstion data if they are disabled
						if (!enableExtensions && aa.getExtensions() != null)
						{
							aa.setExtensions(null);
						}

						/*
						 * Retrieve all active public key data for this instance
						 * and insert into object
						 */
						List<Map<String, Object>> publicKeys = this.metadataDAO
								.queryDescriptorActivePublicKeys((Integer) attribAuthDescriptor
										.get(Constants.FIELD_DESC_ID));
						for (Map<String, Object> publicKey : publicKeys)
						{
							RSAPublicKey key = (RSAPublicKey) this.cryptoProcessor
									.convertByteArrayPublicKey((byte[]) publicKey.get(Constants.FIELD_PK_BINARY));
							KeyDescriptor keyDescriptor = this.cryptoProcessor.createSigningKeyDescriptor(key,
									(String) publicKey.get(Constants.FIELD_PK_KEYPAIR_NAME), (String)publicKey.get(Constants.FIELD_PK_ISSUER), 
									(String)publicKey.get(Constants.FIELD_PK_SERIAL));
							aa.getKeyDescriptors().add(keyDescriptor);
						}

						entityDescriptor.getIDPDescriptorAndSSODescriptorAndRoleDescriptors().add(aa);
						this.logger.debug("Retrieved attribAuthDescriptor of: \n"
								+ attribAuthDescriptor.get(Constants.FIELD_DESCRIPTOR_XML));
					}

					// LXACML is considered an entire extenstion so don't even
					// bother if we have them turned off
					if (enableExtensions)
					{
						lxacmlPDPDescriptors = this.metadataDAO.queryLXACMLPDPDescriptor(entID);
						for (Map<String, Object> lxacmlPDPDescriptor : lxacmlPDPDescriptors)
						{
							LXACMLPDPDescriptor lx = this.lxacmlPDPUnmarshaller
									.unMarshallUnSigned((byte[]) lxacmlPDPDescriptor
											.get(Constants.FIELD_DESCRIPTOR_XML));

							/*
							 * Retrieve all active public key data for this
							 * instance and insert into object
							 */
							List<Map<String, Object>> publicKeys = this.metadataDAO
									.queryDescriptorActivePublicKeys((Integer) lxacmlPDPDescriptor
											.get(Constants.FIELD_DESC_ID));
							for (Map<String, Object> publicKey : publicKeys)
							{
								RSAPublicKey key = (RSAPublicKey) this.cryptoProcessor
										.convertByteArrayPublicKey((byte[]) publicKey.get(Constants.FIELD_PK_BINARY));
								KeyDescriptor keyDescriptor = this.cryptoProcessor.createSigningKeyDescriptor(key,
										(String) publicKey.get(Constants.FIELD_PK_KEYPAIR_NAME), (String)publicKey.get(Constants.FIELD_PK_ISSUER), 
										(String)publicKey.get(Constants.FIELD_PK_SERIAL));
								lx.getKeyDescriptors().add(keyDescriptor);
							}

							entityDescriptor.getIDPDescriptorAndSSODescriptorAndRoleDescriptors().add(lx);
							this.logger.debug("Retrieved lxacmlPDPDescriptor of: \n"
									+ lxacmlPDPDescriptor.get(Constants.FIELD_DESCRIPTOR_XML));
						}
					}

					/*
					 * If this entity actually had descriptors associated with
					 * it, add to metadata otherwise its considered lame
					 */
					if (entityDescriptor.getIDPDescriptorAndSSODescriptorAndRoleDescriptors().size() > 0)
						entitiesDescriptor.getEntitiesDescriptorsAndEntityDescriptors().add(entityDescriptor);
				}

				this.logger.debug("About to marshall metadata, all data loaded from repository correctly");
				return this.metadataMarshaller.marshallSigned(entitiesDescriptor);
			}
		}
		catch (UnmarshallerException e)
		{
			this.logger.error("Exception when attempting to unmarshall metadata");
			this.logger.debug(e.getLocalizedMessage(), e);
		}
		catch (MarshallerException e)
		{
			this.logger.error("Exception when attempting to marshall completed metadata document");
			this.logger.debug(e.getLocalizedMessage(), e);
		}
		catch (MetadataDAOException e)
		{
			this.logger.error("Exception when attempting to retrieve data to create metadata document");
			this.logger.debug(e.getLocalizedMessage(), e);
		}
		catch (CryptoException e)
		{
			this.logger.error("Exception when attempting to retrieve crypto data to create metadata document");
			this.logger.debug(e.getLocalizedMessage(), e);
		}

		return null;
	}

	private void populateDescriptorCrypto(Integer descID, RoleDescriptorType descriptor) throws CryptoException,
			MetadataDAOException
	{
		/*
		 * Retrieve all active public key data for this instance and insert into
		 * object
		 */
		List<Map<String, Object>> publicKeys = this.metadataDAO.queryDescriptorActivePublicKeys(descID);
		for (Map<String, Object> publicKey : publicKeys)
		{
			RSAPublicKey key = (RSAPublicKey) this.cryptoProcessor.convertByteArrayPublicKey((byte[]) publicKey
					.get(Constants.FIELD_PK_BINARY));
			KeyDescriptor keyDescriptor = this.cryptoProcessor.createSigningKeyDescriptor(key, (String) publicKey
					.get(Constants.FIELD_PK_KEYPAIR_NAME), (String)publicKey.get(Constants.FIELD_PK_ISSUER), 
					(String)publicKey.get(Constants.FIELD_PK_SERIAL));
			descriptor.getKeyDescriptors().add(keyDescriptor);
		}
	}

	private List<SPSSODescriptor> createSPSSODescriptor(Integer entID, boolean enableExtensions)
	{
		List<SPSSODescriptor> spDescriptors = new ArrayList<SPSSODescriptor>();

		try
		{
			List<Map<String, Object>> spList = this.metadataDAO.querySPList(entID);
			for (Map<String, Object> sp : spList)
			{
				// Grab details about this service
				Map<String, Object> entityDescriptor = this.metadataDAO.queryServiceDetails(entID);
				String entityHost = (String) entityDescriptor.get(Constants.FIELD_ENTITY_HOST);

				SPSSODescriptor descriptor = new SPSSODescriptor();

				// Specify basic SP requirements
				descriptor.setID(this.identifierGenerator.generateSAMLID());
				descriptor.setAuthnRequestsSigned(true);
				descriptor.getProtocolSupportEnumerations().add(ProtocolConstants.protocol);
				descriptor.setWantAssertionsSigned(true);

				Integer descID = (Integer) sp.get(Constants.FIELD_DESC_ID);

				// Determine supported NameID formats
				List<Map<String, Object>> formats = this.metadataDAO.querySupportedNameIDFormats(descID);
				for (Map<String, Object> format : formats)
				{
					String value = (String) format.get(Constants.FIELD_NAMEID);
					descriptor.getNameIDFormats().add(value);
				}

				// By default if no entry exists specify transient support
				if (formats.size() == 0)
				{
					descriptor.getNameIDFormats().add(NameIDFormatConstants.trans);
				}

				// Process all nodes for this service that are active and place
				// into metadata
				Extensions extensions = null;

				if (enableExtensions)
				{
					extensions = descriptor.getExtensions();
					if (extensions == null)
					{
						extensions = new Extensions();
					}
				}

				List<Map<String, Object>> nodeList = this.metadataDAO.queryActiveServiceNodes(descID);

				// Throw this service away if it has no node data
				if (nodeList.size() == 0)
					continue;

				for (Map<String, Object> node : nodeList)
				{
					Integer nodeID = new Integer((String) node.get(Constants.FIELD_ENDPOINT_ID));

					// Assertion consumer service - use the service host to create spep URL for ACS, ensures
					// multi node environments don't release a specific node address to clients during redirects and
					// provides for enhanced Layer 7 support
					if (node.get(Constants.FIELD_ENDPOINT_ASSERTIONCONSUMER) != null)
					{
						String acs;
						if(entityHost != null && entityHost.length() > 1)
							acs = entityHost + (String) node.get(Constants.FIELD_ENDPOINT_ASSERTIONCONSUMER);
						else
							acs = (String) node.get(Constants.FIELD_ENDPOINT_NODEURL)
							+ (String) node.get(Constants.FIELD_ENDPOINT_ASSERTIONCONSUMER);
						IndexedEndpointType assertionConsumerService = new IndexedEndpointType();
						assertionConsumerService.setLocation(acs);
						assertionConsumerService.setBinding(BindingConstants.httpPost);
						assertionConsumerService.setIndex(nodeID.intValue());
						descriptor.getAssertionConsumerServices().add(assertionConsumerService);
					}

					// Single logout service
					if (node.get(Constants.FIELD_ENDPOINT_SINGLELOGOUT) != null)
					{
						String sls = (String) node.get(Constants.FIELD_ENDPOINT_NODEURL)
								+ (String) node.get(Constants.FIELD_ENDPOINT_SINGLELOGOUT);
						EndpointType singleLogoutService = new EndpointType();
						singleLogoutService.setLocation(sls);
						singleLogoutService.setBinding(BindingConstants.soap);
						descriptor.getSingleLogoutServices().add(singleLogoutService);
					}
					if (enableExtensions)
					{
						// Cache clear service
						if (node.get(Constants.FIELD_ENDPOINT_CACHECLEAR) != null)
						{
							String ccs = (String) node.get(Constants.FIELD_ENDPOINT_NODEURL)
									+ (String) node.get(Constants.FIELD_ENDPOINT_CACHECLEAR);
							CacheClearService cacheClearService = new CacheClearService();
							cacheClearService.setLocation(ccs);
							cacheClearService.setBinding(BindingConstants.soap);
							cacheClearService.setIndex(nodeID.intValue());

							extensions.getImplementedExtensions().add(
									this.cacheClearMarshaller.marshallUnSignedElement(cacheClearService));
							descriptor.setExtensions(extensions);
						}
					}
				}

				// Populate Crypto
				this.populateDescriptorCrypto(descID, descriptor);

				spDescriptors.add(descriptor);

				// Store descriptor details in the database for this service
				byte[] spXML = this.spMarshaller.marshallUnSigned(descriptor);
				this.metadataDAO.updateDescriptor(descID, descriptor.getID(), spXML);
			}
		}
		catch (MetadataDAOException e)
		{
			this.logger.error("Unable to populate all service descriptors for entID: " + entID + " - "
					+ e.getLocalizedMessage());
		}
		catch (MarshallerException e)
		{
			this.logger.error("Unable to populate all service descriptors for entID: " + entID + " - "
					+ e.getLocalizedMessage());
		}
		catch (CryptoException e)
		{
			this.logger.error("Unable to populate all service descriptors for entID: " + entID + " - "
					+ e.getLocalizedMessage());
		}

		return spDescriptors;
	}
}

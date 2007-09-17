package com.qut.middleware.esoe.metadata;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3._2000._09.xmldsig_.KeyInfo;
import org.w3._2000._09.xmldsig_.KeyValue;
import org.w3._2000._09.xmldsig_.RSAKeyValue;
import org.w3._2000._09.xmldsig_.Signature;
import org.w3c.dom.Element;

import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.crypto.KeyStoreResolver;
import com.qut.middleware.esoe.crypto.impl.KeyStoreResolverImpl;
import com.qut.middleware.esoe.metadata.cache.MetadataCache;
import com.qut.middleware.esoe.metadata.cache.impl.MetadataCacheImpl;
import com.qut.middleware.esoe.metadata.impl.MetadataUpdateMonitor;
import com.qut.middleware.saml2.handler.Marshaller;
import com.qut.middleware.saml2.handler.impl.MarshallerImpl;
import com.qut.middleware.saml2.schemas.metadata.EndpointType;
import com.qut.middleware.saml2.schemas.metadata.EntitiesDescriptor;
import com.qut.middleware.saml2.schemas.metadata.EntityDescriptor;
import com.qut.middleware.saml2.schemas.metadata.Extensions;
import com.qut.middleware.saml2.schemas.metadata.IDPSSODescriptor;
import com.qut.middleware.saml2.schemas.metadata.IndexedEndpointType;
import com.qut.middleware.saml2.schemas.metadata.KeyDescriptor;
import com.qut.middleware.saml2.schemas.metadata.KeyTypes;
import com.qut.middleware.saml2.schemas.metadata.SPSSODescriptor;
import com.qut.middleware.saml2.schemas.metadata.extensions.CacheClearService;

@SuppressWarnings({"unqualified-field-access","nls"})
public class MetadataMonitorTest {

	private  String spSSODescriptorID1;
	private  String entityDescriptorIDRoot;
	private  String sp1AssertionConsumerEndpoint1Location1;
	private  String sp1AssertionConsumerEndpoint1Location2;
	private  String sp1AssertionConsumerEndpoint1Location3;
	private  String spSSODescriptorID2;
	private  String sp2AssertionConsumerEndpoint1Location1;
	private  String idpSSODescriptorID2;
	private  String sp2CacheClearService1Location;
	private  String sp1CacheClearService1Location;
	private  File tempFile;
	private  String sp2CacheClearService2Location;
	private  String entityDescriptorID2;
	private  String entityDescriptorID1;
	private  String idp2SingleLogoutEndpoint1Location;
	private  String sp2SingleLogoutEndpoint1Location;
	private  String sp1SingleLogoutEndpoint1Location;
	private  String sp1SingleLogoutEndpoint2Location;
	private  String idp2SingleSignOnEndpoint1Location;
	private  EntitiesDescriptor entitiesDescriptorRoot;
	private  PrivateKey privateKey;
	private  PublicKey publicKey;
	private  KeyStore keyStore;
	private  String sp2KeyID1;
	private  Marshaller<EntitiesDescriptor> marshaller;
	private  Marshaller<CacheClearService> cacheClearMarshaller;
	private  KeyStoreResolver keyStoreResolver;

	private MetadataUpdateMonitor metadataMonitor;
	private MetadataCache metadataCache;

	
	@Before
	public void setUp() throws Exception 
	{	
		
		String keyStorePath = System.getProperty("user.dir") + File.separator + "tests" + File.separator + "testdata" + File.separator + "testskeystore.ks";
		String keyStorePassword = "Es0EKs54P4SSPK";
		String esoeKeyAlias = "esoeprimary";
		String esoeKeyPassword = "Es0EKs54P4SSPK";
		
		keyStoreResolver = new KeyStoreResolverImpl(new File(keyStorePath), keyStorePassword, esoeKeyAlias, esoeKeyPassword);
		privateKey = keyStoreResolver.getPrivateKey();
		publicKey = keyStoreResolver.getPublicKey();
		entityDescriptorIDRoot = "allentities.esoe.test";
		entityDescriptorID1 = "entity1.esoe.test";
		entityDescriptorID2 = "entity2.esoe.test";
		spSSODescriptorID1 = "sp1.esoe.test";
		spSSODescriptorID2 = "sp2.esoe.test";
		idpSSODescriptorID2 = "idp2.esoe.test";
		
		tempFile = File.createTempFile("tmp-metadata", ".xml");
		tempFile.deleteOnExit();
		
		String[] schemaCacheClear = new String[]{ConfigurationConstants.cacheClearService};
		cacheClearMarshaller = new MarshallerImpl<CacheClearService>(CacheClearService.class.getPackage().getName(), schemaCacheClear);
		
		// retrieve initial URL to get metadata from. Created from local file
		URL test = createMetadata(false);
	
		this.metadataCache = new MetadataCacheImpl();
		this.metadataMonitor = new MetadataUpdateMonitor(this.metadataCache, this.keyStoreResolver, test.toString(), 1);
		
		
	}

	@After
	public void tearDown()
	{
		if(this.metadataMonitor.isAlive())
			this.metadataMonitor.shutdown();
	}
	
	
	@Test
	public void testMetadataUpdateMonitor()
	{
		assertTrue(this.metadataMonitor.isAlive());
	}

	@Test
	public void testRun()
	{
		try
		{
			Thread.sleep(5000);
			
			// get current revision of metadata for later comparison
			String metadataRevision = this.metadataCache.getCurrentRevision();
			
						
			// modify the metadata to ensure the cache is updated by the monitor
			createMetadata(true);
			
			// wait for it to update (must be more than set interval)
			Thread.sleep(5000);
			
			assertNotSame(metadataRevision, this.metadataCache.getCurrentRevision());
		}
		catch(Exception e)
		{
			// do nothing
		}
	}
	
	
	@Test
	public void testShutdown() throws Exception
	{
		Thread.sleep(1000);
		
		this.metadataMonitor.shutdown();
		
		// give it a second to shutdown
		Thread.sleep(1000);
		
		assertTrue("MetdataMonitor thread failed to shutdown correctly", !this.metadataMonitor.isAlive());
	}

	/* Creates a metadata object and returns a local URL from the file that was created when
	 * the metadata object is marshalled
	 */
	private URL createMetadata(boolean modify) throws Exception
	{
		String modifiedData = "http://sp1.test.url/assertionConsumer";
		
		if(modify)
			modifiedData = "http://sp1.new.url/assertionConsumer";
		
		SPSSODescriptor spSSODescriptor1 = new SPSSODescriptor();
		spSSODescriptor1.setID(spSSODescriptorID1);
		spSSODescriptor1.getProtocolSupportEnumerations().add("roar");
		
			// Giving these their own blocks so I don't end up with copy/paste errors
			{
				IndexedEndpointType sp1AssertionConsumerEndpoint1 = new IndexedEndpointType();
				sp1AssertionConsumerEndpoint1Location1 = modifiedData;
				sp1AssertionConsumerEndpoint1.setIsDefault(true);
				sp1AssertionConsumerEndpoint1.setIndex(0);
				sp1AssertionConsumerEndpoint1.setLocation(sp1AssertionConsumerEndpoint1Location1);
				sp1AssertionConsumerEndpoint1.setBinding("binding");
				spSSODescriptor1.getAssertionConsumerServices().add(sp1AssertionConsumerEndpoint1);
			}
			
			{
				IndexedEndpointType sp1AssertionConsumerEndpoint2 = new IndexedEndpointType();
				sp1AssertionConsumerEndpoint1Location2 = "http://sp1.test.url/assertionConsumer2";
				sp1AssertionConsumerEndpoint2.setIndex(1);
				sp1AssertionConsumerEndpoint2.setLocation(sp1AssertionConsumerEndpoint1Location2);
				sp1AssertionConsumerEndpoint2.setBinding("binding");
				spSSODescriptor1.getAssertionConsumerServices().add(sp1AssertionConsumerEndpoint2);
			}
			
			{
				IndexedEndpointType sp1AssertionConsumerEndpoint3 = new IndexedEndpointType();
				sp1AssertionConsumerEndpoint1Location3 = "http://sp1.test.url/assertionConsumer/3";
				sp1AssertionConsumerEndpoint3.setIndex(2);
				sp1AssertionConsumerEndpoint3.setLocation(sp1AssertionConsumerEndpoint1Location3);
				sp1AssertionConsumerEndpoint3.setBinding("binding");
				spSSODescriptor1.getAssertionConsumerServices().add(sp1AssertionConsumerEndpoint3);
			}
			
			{
				CacheClearService sp1CacheClearService1 = new CacheClearService();
				sp1CacheClearService1Location = "http://sp1.test.url/cacheClear";
				sp1CacheClearService1.setResponseLocation("http://sp1.test.url/cacheClearResponseLocation");
				sp1CacheClearService1.setLocation(sp1CacheClearService1Location);
				sp1CacheClearService1.setBinding("binding");

				Extensions extensions = new Extensions();
				spSSODescriptor1.setExtensions(extensions);
				
				Element element = cacheClearMarshaller.marshallUnSignedElement( sp1CacheClearService1);
				
				// If this came back valid, then we don't need to worry about the structure. The Node (Document) will 
				// have exactly 1 child, an element containing the CacheClearService
				extensions.getImplementedExtensions().add(element);
			}
			
			{
				EndpointType sp1SingleLogoutEndpoint1 = new EndpointType();
				
				sp1SingleLogoutEndpoint1Location = "http://sp1.test.url/singleLogout";
				sp1SingleLogoutEndpoint1.setLocation(sp1SingleLogoutEndpoint1Location);
				sp1SingleLogoutEndpoint1.setBinding("binding");
				sp1SingleLogoutEndpoint1.setResponseLocation("http://sp1.test.url/singleLogoutResponseLocation");

				spSSODescriptor1.getSingleLogoutServices().add(sp1SingleLogoutEndpoint1);
			}
			
			{
				EndpointType sp1SingleLogoutEndpoint2 = new EndpointType();
				
				sp1SingleLogoutEndpoint2Location = "http://sp1.test.url/singleLogout2";
				sp1SingleLogoutEndpoint2.setLocation(sp1SingleLogoutEndpoint2Location);
				sp1SingleLogoutEndpoint2.setBinding("binding");
				sp1SingleLogoutEndpoint2.setResponseLocation("http://sp1.test.url/singleLogout2ResponseLocation");

				spSSODescriptor1.getSingleLogoutServices().add(sp1SingleLogoutEndpoint2);
			}
			
			SPSSODescriptor spSSODescriptor2 = new SPSSODescriptor();
			spSSODescriptor2.setID(spSSODescriptorID2);
			spSSODescriptor2.getProtocolSupportEnumerations().add("lol");
			
			{
				IndexedEndpointType sp2AssertionConsumerEndpoint1 = new IndexedEndpointType();
				sp2AssertionConsumerEndpoint1Location1 = "http://sp2.another.test.url/assertionConsumer";
				sp2AssertionConsumerEndpoint1.setIsDefault(true);
				sp2AssertionConsumerEndpoint1.setIndex(0);
				sp2AssertionConsumerEndpoint1.setLocation(sp2AssertionConsumerEndpoint1Location1);
				sp2AssertionConsumerEndpoint1.setBinding("binding");
				spSSODescriptor2.getAssertionConsumerServices().add(sp2AssertionConsumerEndpoint1);
			}
			
			{
				CacheClearService sp2CacheClearService1 = new CacheClearService();
				sp2CacheClearService1Location = "http://sp2.another.test.url/cacheClear";
				sp2CacheClearService1.setResponseLocation("http://sp2.another.test.url/cacheClearResponseLocation");
				sp2CacheClearService1.setLocation(sp2CacheClearService1Location);
				sp2CacheClearService1.setBinding("binding");

				Extensions extensions = new Extensions();
				spSSODescriptor2.setExtensions(extensions);
				
				Element element = cacheClearMarshaller.marshallUnSignedElement(sp2CacheClearService1);
				
				// If this came back valid, then we don't need to worry about the structure. The Node (Document) will 
				// have exactly 1 child, an element containing the CacheClearService
				extensions.getImplementedExtensions().add(element);
			}
			
			{
				CacheClearService sp2CacheClearService2 = new CacheClearService();
				sp2CacheClearService2Location = "http://sp2.another.test.url/cacheClear2";
				sp2CacheClearService2.setResponseLocation("http://sp2.another.test.url/cacheClear2ResponseLocation");
				sp2CacheClearService2.setLocation(sp2CacheClearService2Location);
				sp2CacheClearService2.setBinding("binding");
				
				Element element = cacheClearMarshaller.marshallUnSignedElement(sp2CacheClearService2);
				
				// If this came back valid, then we don't need to worry about the structure. The Node (Document) will 
				// have exactly 1 child, an element containing the CacheClearService
				spSSODescriptor2.getExtensions().getImplementedExtensions().add(element);
			}
			
			{
				EndpointType sp2SingleLogoutEndpoint1 = new EndpointType();
				
				sp2SingleLogoutEndpoint1Location = "http://sp2.another.test.url/singleLogout";
				sp2SingleLogoutEndpoint1.setLocation(sp2SingleLogoutEndpoint1Location);
				sp2SingleLogoutEndpoint1.setBinding("binding");
				sp2SingleLogoutEndpoint1.setResponseLocation("http://sp2.another.test.url/singleLogoutResponseLocation");

				spSSODescriptor2.getSingleLogoutServices().add(sp2SingleLogoutEndpoint1);
			}
			
			IDPSSODescriptor idpSSODescriptor2 = new IDPSSODescriptor();
			idpSSODescriptor2.setID(idpSSODescriptorID2);
			idpSSODescriptor2.getProtocolSupportEnumerations().add("heh");

			{
				EndpointType idp2SingleLogoutEndpoint1 = new EndpointType();

				idp2SingleLogoutEndpoint1Location = "http://idp2.test.url/singleLogout";
				
				idp2SingleLogoutEndpoint1.setBinding("binding");
				idp2SingleLogoutEndpoint1.setLocation(idp2SingleLogoutEndpoint1Location);
				idp2SingleLogoutEndpoint1.setResponseLocation("http://idp2.test.url/singleLogoutResponseLocation");
				
				idpSSODescriptor2.getSingleLogoutServices().add(idp2SingleLogoutEndpoint1);
			}
			
			{
				EndpointType idp2SingleSignOnEndpoint1 = new EndpointType();
				
				idp2SingleSignOnEndpoint1Location = "http://idp2.test.url/singleSignOn";
				
				idp2SingleSignOnEndpoint1.setLocation(idp2SingleLogoutEndpoint1Location);
				idp2SingleSignOnEndpoint1.setBinding("binding");
				idp2SingleSignOnEndpoint1.setResponseLocation("http://idp2.test.url/singleSignOnResponseLocation");
				
				idpSSODescriptor2.getSingleSignOnServices().add(idp2SingleSignOnEndpoint1);
			}
			
			{
				KeyDescriptor keyDescriptor = new KeyDescriptor();
				KeyInfo keyInfo = new KeyInfo();
				KeyTypes keyType = KeyTypes.SIGNING;
				
				keyInfo.setId("esoeprimary");
				
				RSAKeyValue rsaKeyValue = new RSAKeyValue();
				
				rsaKeyValue.setExponent(((RSAPublicKey)publicKey).getPublicExponent().toByteArray());
				rsaKeyValue.setModulus(((RSAPublicKey)publicKey).getModulus().toByteArray());

				KeyValue keyValue = new KeyValue();
				keyValue.getContent().add(rsaKeyValue);
				
				JAXBElement<String> keyName = new JAXBElement<String>(new QName("http://www.w3.org/2000/09/xmldsig#", "KeyName"), String.class, "esoeprimary");
				
				keyInfo.getContent().add(keyValue);
				keyInfo.getContent().add(keyName);
				
				keyDescriptor.setKeyInfo(keyInfo);
				keyDescriptor.setUse(keyType);
				
				idpSSODescriptor2.getKeyDescriptors().add(keyDescriptor);
			}
			
			entitiesDescriptorRoot = new EntitiesDescriptor();
			entitiesDescriptorRoot.setID(entityDescriptorIDRoot);
			
			EntityDescriptor entityDescriptor1 = new EntityDescriptor();
			entityDescriptor1.setEntityID(entityDescriptorID1);

			EntityDescriptor entityDescriptor2 = new EntityDescriptor();
			entityDescriptor2.setEntityID(entityDescriptorID2);
			
			entityDescriptor1.getIDPDescriptorAndSSODescriptorAndRoleDescriptors().add(spSSODescriptor1);
			entityDescriptor2.getIDPDescriptorAndSSODescriptorAndRoleDescriptors().add(spSSODescriptor2);
			entityDescriptor2.getIDPDescriptorAndSSODescriptorAndRoleDescriptors().add(idpSSODescriptor2);
			
			entitiesDescriptorRoot.getEntitiesDescriptorsAndEntityDescriptors().add(entityDescriptor1);
			entitiesDescriptorRoot.getEntitiesDescriptorsAndEntityDescriptors().add(entityDescriptor2);
			
			entitiesDescriptorRoot.setSignature(new Signature());
			
			// Now, marshal it
			marshalMetadata();
			
			return new URL("file", null, tempFile.getAbsolutePath());
	}
	
	private  void marshalMetadata() throws Exception
	{
		String[] schemas = new String[]{ConfigurationConstants.samlMetadata};

		this.marshaller = new MarshallerImpl<EntitiesDescriptor>(EntitiesDescriptor.class.getPackage().getName(), schemas,"esoeprimary",privateKey);
		byte[] document = this.marshaller.marshallSigned(entitiesDescriptorRoot);
	}
		
}

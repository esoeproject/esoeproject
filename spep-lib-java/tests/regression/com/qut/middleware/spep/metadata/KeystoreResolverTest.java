package com.qut.middleware.spep.metadata;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;

import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.spep.metadata.impl.KeyStoreResolverImpl;

@SuppressWarnings("nls")
public class KeystoreResolverTest 
{

	private KeyStoreResolver keystoreResolver;
	private InputStream keystoreInputStream;
	
	private KeyStore testKeyStore;
	
	// keystore password
	private String keyStorePassword;
	// private key alias
	private String keyAlias;
	// private key password
	private String keyPassword;
		
	private PublicKey publicKey;
	private Certificate certificate;
	
	@Before
	public void setUp() throws Exception
	{
		this.keyStorePassword = "Es0EKs54P4SSPK";
		this.keyAlias = "esoeprimary";	
		this.keyPassword = "Es0EKs54P4SSPK";
		
		this.keystoreInputStream = new FileInputStream("secure" + File.separator + "esoekeystore.ks");
		this.keystoreResolver = new KeyStoreResolverImpl(this.keystoreInputStream, this.keyStorePassword, this.keyAlias, this.keyPassword);

		// we''l check it againt this keystore
		try
		{
			FileInputStream stream = new FileInputStream("secure" + File.separator + "esoekeystore.ks");
	
			this.testKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			this.testKeyStore.load(stream, this.keyStorePassword.toCharArray());
		
			stream.close();
			
			this.certificate = this.testKeyStore.getCertificate(this.keyAlias);
			this.publicKey = this.certificate.getPublicKey();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail("Could not load test keystore");
		}
	
	}

	
	/** Ensure the key returned is what we think it will be.
	 * 
	 */
	@Test
	public void testResolveKey() 
	{
		assertEquals(this.publicKey, this.keystoreResolver.resolveKey(this.keyAlias));
	}

	/** Ensure the cert returned is what we think it will be.
	 * 
	 */
	@Test
	public void testResolveCertificate()
	{
		assertEquals(this.certificate, this.keystoreResolver.resolveCertificate(this.keyAlias));
	}

	@Test
	public void testGetPrivateKey() throws Exception
	{
		assertEquals((PrivateKey)this.testKeyStore.getKey(this.keyAlias, this.keyPassword.toCharArray()), this.keystoreResolver.getPrivateKey());
	}

	@Test
	public void testGetKeyAlias() 
	{
		assertEquals(this.keyAlias, this.keystoreResolver.getKeyAlias());
	}

	@Test
	public void testGetPublicKey()
	{
		assertEquals(this.publicKey, this.keystoreResolver.resolveKey(this.keyAlias));
	}

	
	/** Test invalid params.
	 *
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction1()
	{
		this.keystoreResolver = new KeyStoreResolverImpl(null, this.keyStorePassword, this.keyAlias, this.keyPassword);
	}
	
	/** Test invalid params.
	 *
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction2()
	{
		this.keystoreResolver = new KeyStoreResolverImpl(this.keystoreInputStream, null, this.keyAlias, this.keyPassword);
	}
	
	/** Test invalid params.
	 *
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction3()
	{
		this.keystoreResolver = new KeyStoreResolverImpl(this.keystoreInputStream, this.keyStorePassword, null, this.keyPassword);
	}
	
	/** Test invalid params.
	 *
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction4()
	{
		this.keystoreResolver = new KeyStoreResolverImpl(this.keystoreInputStream, this.keyStorePassword, this.keyAlias, null);
	}
	
}

/* Copyright 2006, Queensland University of Technology
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
 * Creation Date: 08/08/2007
 * 
 * Purpose: Extracts keys in PEM format from a JKS format keystore.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;

import sun.misc.BASE64Encoder;

public class KeyStoreUtil {

	static final String PRIVKEY_START = "-----BEGIN PRIVATE KEY-----";
	static final String PRIVKEY_END = "-----END PRIVATE KEY-----";
	static final String CERT_START = "-----BEGIN CERTIFICATE-----";
	static final String CERT_END = "-----END CERTIFICATE-----";
	static final String KS_TYPE = "JKS";

	public static void main(String[] args) {

		String filename = "/path/to/spepKeystore.ks";

		String storePass = "This is where your keystore password goes.";
		
		String spepKeyAlias = "SPEP key alias here";
		String spepKeyPassPhrase = "Key pass phrase here";
		
		String metadataKeyAlias = "Metadata key alias here";
		
		KeyStore ks = readKeyStore( filename, storePass );
		
		PrivateKey spepPrivateKey = getPrivateKeyPair(ks, spepKeyAlias, spepKeyPassPhrase);
		Certificate spepCertificate = getCertificate(ks, spepKeyAlias);
		Certificate metadataCertficiate = getCertificate(ks, metadataKeyAlias);
		
		System.out.println("\nSPEP key: ");
		writePrivateKeyPEM(System.out, spepPrivateKey);
		
		System.out.println("\n\nSPEP cert: ");
		writeCertificatePEM(System.out, spepCertificate);
		
		System.out.println("\n\nMetadata cert: ");
		writeCertificatePEM(System.out, metadataCertficiate);
				System.out.println();
	}
	
	public static KeyStore readKeyStore( String filename, String storePass )
	{
		try {
			KeyStore ks = KeyStore.getInstance( KS_TYPE );
			
			File keystoreFile = new File( filename );
			if (!keystoreFile.exists()) {
				// No file to read.
				return null;
			}

			ks.load( new FileInputStream(keystoreFile), storePass.toCharArray() );
			
			return ks;
		} catch (KeyStoreException e) {
			throw new InvalidParameterException( "Couldn't create a keystore of type: " + KS_TYPE );
		} catch (NoSuchAlgorithmException e) {
			throw new InvalidParameterException( "No such algorithm. Exception was: " + e.getMessage() );
		} catch (CertificateException e) {
			throw new InvalidParameterException( "Certificate exception. Exception was: " + e.getMessage() );
		} catch (FileNotFoundException e) {
			throw new InvalidParameterException( "File was not found: " + filename );
		} catch (IOException e) {
			throw new InvalidParameterException( "I/O error reading keystore from " + filename + " - Exception was: " + e.getMessage() );
		}
	}
	
	public static Certificate getCertificate( KeyStore keyStore, String alias )
	{
		try {
			return keyStore.getCertificate(alias);
		} catch (KeyStoreException e) {
			throw new InvalidParameterException( "Keystore exception occurred. Exception was: " + e.getMessage() );
		}
	}

	public static PrivateKey getPrivateKeyPair(KeyStore keyStore, String alias, String password) {
		try {
			Key key = keyStore.getKey(alias, password.toCharArray());
			if (key instanceof PrivateKey) {
				return (PrivateKey) key;
			}
			
			throw new InvalidParameterException( "No private key found with alias: " + alias );
		} catch (UnrecoverableKeyException e) {
			throw new InvalidParameterException( "Unrecoverable key. Exception was: " + e.getMessage() );
		} catch (NoSuchAlgorithmException e) {
			throw new InvalidParameterException( "No such algorithm. Exception was: " + e.getMessage() );
		} catch (KeyStoreException e) {
			throw new InvalidParameterException( "Keystore exception occurred. Exception was: " + e.getMessage() );
		}
	}
	
	public static void writeCertificatePEM( PrintStream out, Certificate cert )
	{
		try {
			BASE64Encoder myB64 = new BASE64Encoder();
			String b64 = myB64.encode(cert.getEncoded());

			out.println(CERT_START);
			out.println(b64);
			out.println(CERT_END);
		} catch (CertificateEncodingException e) {
			throw new InvalidParameterException( "Invalid certificate. Exception was: " + e.getMessage() );
		}
	}
	
	public static void writePrivateKeyPEM( PrintStream out, PrivateKey privKey )
	{
		BASE64Encoder myB64 = new BASE64Encoder();
		String b64 = myB64.encode(privKey.getEncoded());

		out.println(PRIVKEY_START);
		out.println(b64);
		out.println(PRIVKEY_END);
	}
}

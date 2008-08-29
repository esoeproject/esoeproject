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
 * Creation Date: 18/06/2007
 * 
 * Purpose: Generates OpenID delegator customised to local environment
 */
package com.qut.middleware.delegator.deployment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyPair;
import java.security.KeyStore;
import java.util.Properties;

import com.qut.middleware.crypto.CryptoProcessor;
import com.qut.middleware.crypto.KeystoreResolver;
import com.qut.middleware.crypto.impl.CryptoProcessorImpl;
import com.qut.middleware.crypto.impl.KeystoreResolverImpl;
import com.qut.middleware.saml2.identifier.IdentifierGenerator;
import com.qut.middleware.saml2.identifier.impl.IdentifierCacheImpl;
import com.qut.middleware.saml2.identifier.impl.IdentifierGeneratorImpl;
import com.qut.middleware.tools.war.logic.GenerateWarLogic;

public class Startup
{
	private final String CONFIG_TEMPLATE = "openiddelegator.config";
	private final String CONFIG_TEMPLATE_PATH = File.separatorChar + CONFIG_TEMPLATE;
	private final String WARFILES = File.separatorChar + "war";
	private final String ESOE_KEYSTORE_NAME = "esoeKeystore.ks";
	private final String WEBINF = "WEB-INF";
	private final String OPENID_DELEGATOR_KEYSTORE_NAME = "openidKeystore.ks";
	private final String CERT_ISSUER_DN = "cn=esoeopeniddelegator";
	private final String CERT_ISSUER_EMAIL = "not@required";
	private final int KEY_SIZE = 2048;

	private IdentifierGenerator identifierGenerator;
	private KeystoreResolver keyStoreResolver;
	private CryptoProcessor cryptoProcessor;

	private ConfigBean configBean;

	public static void main(String[] args)
	{
		Startup startup;
		String confirm = "N";
		String correctExit = "X";

		startup = new Startup();
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		try
		{
			System.out.println("Welcome to the Enterprise Sign On Engine, OpenID installation process");
			while (confirm.equalsIgnoreCase("N"))
			{
				startup.getEnvironmentDetails(br);
				confirm = br.readLine();

				if (confirm.length() == 0)
					confirm = "N";

				if (confirm.equalsIgnoreCase(("Y")))
					break;

				System.out.print("Would you like to exit (X) or correct (C) values? [X]: ");
				correctExit = br.readLine();

				if (correctExit.length() == 0)
					correctExit = "X";

				if (correctExit.equalsIgnoreCase("X"))
					return;
			}

			/* User has provided all we need to configure the openID delegator, perform operations to create war */
			startup.configureEnvironment();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{

		}
	}

	public Startup()
	{
		this.identifierGenerator = new IdentifierGeneratorImpl(new IdentifierCacheImpl());
		this.configBean = new ConfigBean();
	}

	private void getEnvironmentDetails(BufferedReader br) throws IOException
	{
		String tmp;

		System.out.print("Please enter the ESOE keystore location ");
		if (this.configBean.getEsoeKeystore() == null)
			System.out.print("(eg: ${esoe.data}/config/esoeKeystore.ks): ");
		else
			System.out.print("[" + this.configBean.getEsoeKeystore() + "] :");
		tmp = br.readLine();
		if (tmp.length() > 0)
			this.configBean.setEsoeKeystore(tmp);

		System.out.print("Please enter the ESOE configuration file location ");
		if (this.configBean.getEsoeConfig() == null)
			System.out.print("(eg: ${esoe.data}/config/esoe.config): ");
		else
			System.out.print("[" + this.configBean.getEsoeConfig() + "] :");
		tmp = br.readLine();
		if (tmp.length() > 0)
			this.configBean.setEsoeConfig(tmp);

		System.out.print("Please enter the URL where users will interact with the ESOE OpenID component ");
		if (this.configBean.getOpenIDEndpoint() == null)
			System.out.print("(eg: https://esoe.company.com/openiddelegator - must end in openiddelegator): ");
		else
			System.out.print("[" + this.configBean.getOpenIDEndpoint() + "] :");
		tmp = br.readLine();
		if (tmp.length() > 0)
			this.configBean.setOpenIDEndpoint(tmp);
		
		System.out.print("Are you deploying the delegator in a https offloaded, load balanced environment ");
		if (this.configBean.getExtractedFiles() == null)
			System.out.print("(n): ");
		else
			System.out.print("[" + this.configBean.getHttpsOffload() + "] :");
		tmp = br.readLine();
		if (tmp.length() > 0)
		{
			if(tmp.equals("y"))
				this.configBean.setHttpsOffload("true");
			else
				this.configBean.setHttpsOffload("false");
		}

		System.out.print("Please enter the amount of time in years the openID delegator should have a valid key for ESOE communication ");
		if (this.configBean.getCertExpiryInterval() == 0)
			System.out.print("(eg: 2): ");
		else
			System.out.print("[" + this.configBean.getCertExpiryInterval() + "] :");
		tmp = br.readLine();
		if (tmp.length() > 0)
			this.configBean.setCertExpiryInterval((new Integer(tmp)).intValue());

		System.out.print("Please enter the directory where you have extracted the openid delegator files to ");
		if (this.configBean.getExtractedFiles() == null)
			System.out.print("(eg: /home/username/openiddelegator): ");
		else
			System.out.print("[" + this.configBean.getExtractedFiles() + "] :");
		tmp = br.readLine();
		if (tmp.length() > 0)
			this.configBean.setExtractedFiles(tmp);

		System.out.print("Please enter a directory this application can write to ");
		if (this.configBean.getOutputDirectory() == null)
			System.out.print("(eg: /tmp/openiddelegator): ");
		else
			System.out.print("[" + this.configBean.getOutputDirectory() + "] :");
		tmp = br.readLine();
		if (tmp.length() > 0)
			this.configBean.setOutputDirectory(tmp);

		System.out.println("Is the following information correct?: ");
		System.out.println("ESOE keystore location: " + this.configBean.getEsoeKeystore());
		System.out.println("ESOE configuration file: " + this.configBean.getEsoeConfig());
		System.out.println("OpenID URL: " + this.configBean.getOpenIDEndpoint());
		System.out.println("Key validity period in years: " + this.configBean.getCertExpiryInterval());
		System.out.println("Location of extracted files: " + this.configBean.getExtractedFiles());
		System.out.println("Output Directory: " + this.configBean.getOutputDirectory());
		System.out.print("Yes (Y) / No (N) [N] :");
	}

	public void configureEnvironment() throws Exception
	{
		System.out.println("*** Configuring your openID delegator please wait....");

		/* Process the ESOE properties file to get further important information */
		loadESOEConfigProperties();
		this.configBean.setIssuerID(this.identifierGenerator.generateSAMLID());

		this.keyStoreResolver = new KeystoreResolverImpl(new File(this.configBean.getEsoeKeystore()), this.configBean.getEsoeKeyStorePassphrase(), this.configBean.getEsoeKeyName(), this.configBean.getEsoeKeyPassphrase());
		this.cryptoProcessor = new CryptoProcessorImpl(this.keyStoreResolver, this.CERT_ISSUER_DN, this.CERT_ISSUER_EMAIL, this.configBean.getCertExpiryInterval(), this.KEY_SIZE);

		RenderOIDDelegatorConfigLogic renderer = new RenderOIDDelegatorConfigLogic();
		GenerateWarLogic logic = new GenerateWarLogic();
		String renderedConfig;

		System.out.println("*** Created keystores...");
		createKeystores();
		
		System.out.println("*** Rendered config...");
		renderedConfig = renderer.generateConfig(new File(this.configBean.getExtractedFiles() + this.CONFIG_TEMPLATE_PATH), this.configBean);
		
		/* Store the new openid delegator KeyStore */
		this.cryptoProcessor.serializeKeyStore(this.configBean.getKeyStore(), this.configBean.getOidKeyStorePassphrase(), this.configBean.getOutputDirectory() + File.separatorChar + this.OPENID_DELEGATOR_KEYSTORE_NAME);
				
		/* Store the rendered openid delegator config */
		FileOutputStream output = new FileOutputStream( this.configBean.getOutputDirectory() + File.separatorChar + this.CONFIG_TEMPLATE );
		output.write(renderedConfig.getBytes());
		output.close();
		
		System.out.println("Completed, files written to " + this.configBean.getOutputDirectory() + ". Copy openiddeleg.war to your tomcat instance, copy created files to ${openiddeleg.data}/config and after backing up your current esoeKeystore.ks replace with the newly generated version.");
	}

	private void createKeystores() throws Exception
	{
		/*
		 * This will create two keystores, one for the OpenID delegator and an updated ESOE keystore with the OpenID
		 * Delegators public key inserted
		 */
		KeyStore oidKeyStore;
		KeyPair oidKeyPair, esoeKeyPair;

		String oidKeyStorePassphrase = this.cryptoProcessor.generatePassphrase();
		this.configBean.setOidKeyStorePassphrase(oidKeyStorePassphrase);

		String oidKeyPairName = this.identifierGenerator.generateXMLKeyName();
		this.configBean.setOidKeyPairName(oidKeyPairName);

		String oidKeyPairPassphrase = this.cryptoProcessor.generatePassphrase();
		this.configBean.setOidKeyPairPassphrase(oidKeyPairPassphrase);

		oidKeyStore = this.cryptoProcessor.generateKeyStore();
		oidKeyPair = this.cryptoProcessor.generateKeyPair();
		this.cryptoProcessor.addKeyPair(oidKeyStore, oidKeyStorePassphrase, oidKeyPair, oidKeyPairName, oidKeyPairPassphrase, this.generateSubjectDN(this.configBean.getOpenIDEndpoint()));

		esoeKeyPair = new KeyPair(this.keyStoreResolver.getLocalPublicKey(), this.keyStoreResolver.getLocalPrivateKey());
		this.cryptoProcessor.addPublicKey(oidKeyStore, esoeKeyPair, this.keyStoreResolver.getLocalKeyAlias(), this.generateSubjectDN(this.configBean.getEsoeURL()));
		this.cryptoProcessor.addPublicKey(this.keyStoreResolver., oidKeyPair, oidKeyPairName, this.generateSubjectDN(this.configBean.getOpenIDEndpoint()));

		/* Store the updated ESOE keystore to disk for manual updating by deployer */
		this.cryptoProcessor.serializeKeyStore(this.keyStoreResolver.getKeyStore(), this.configBean.getEsoeKeyStorePassphrase(), this.configBean.getOutputDirectory() + File.separatorChar + ESOE_KEYSTORE_NAME);

		/* Store new OpenID delegator keystore for insertion to war */
		this.configBean.setKeyStore(oidKeyStore);
	}

	private void loadESOEConfigProperties() throws FileNotFoundException, IOException
	{
		Properties props = new Properties();
		props.load(new FileInputStream(new File(this.configBean.getEsoeConfig())));

		/*
		 * Load up all properties from the ESOE config file we need so users aren't being pestered with additional
		 * confusing questions over what we are forced to ask
		 */
		this.configBean.setEsoeKeyStorePassphrase(getProperty(props, "esoekeystoreresolver.keystorePassword"));
		this.configBean.setEsoeKeyName(getProperty(props, "esoekeystoreresolver.keyAlias"));
		this.configBean.setEsoeKeyPassphrase(getProperty(props, "esoekeystoreresolver.keyPassword"));
		this.configBean.setEsoeSessionDomain(getProperty(props, "sessionDomain"));
		this.configBean.setEsoeSSOURL(getProperty(props, "ssoURL"));
		
		/* Derive ESOE URL from SSO URL, this may be incorrect in a minority of cases */
		URL esoeURL = new URL(this.configBean.getEsoeSSOURL());
		if(esoeURL.getPort() == -1)
			this.configBean.setEsoeURL(esoeURL.getProtocol() + "://" + esoeURL.getHost());
		else
			this.configBean.setEsoeURL(esoeURL.getProtocol() + "://" + esoeURL.getHost() + ":" + esoeURL.getPort());
	}

	private String getProperty(Properties props, String prop)
	{
		/* Recursively get the value of the property if subsequent variables are returned */
		String tmp = props.getProperty(prop);
		while (tmp.startsWith("$"))
		{
			int start = tmp.indexOf("{") + 1;
			int end = tmp.indexOf("}");
			tmp = tmp.substring(start, end);
			tmp = props.getProperty(tmp);
		}

		return tmp;
	}

	/*
	 * Takes the service URL and creates a subject DN
	 */
	private String generateSubjectDN(String URL)
	{
		try
		{
			String result = new String();
			URL serviceURL = new URL(URL);
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
			System.out.println("Error attempting to generate certificate subjectDN " + e.getLocalizedMessage());
			return "dc=" + URL;
		}
	}

	public ConfigBean getConfigBean()
	{
		return configBean;
	}

	public void setConfigBean(ConfigBean configBean)
	{
		this.configBean = configBean;
	}

}

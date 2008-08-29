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
 */
package com.qut.middleware.deployer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qut.middleware.deployer.bean.ContactPersonBean;
import com.qut.middleware.deployer.bean.DeploymentBean;
import com.qut.middleware.deployer.exception.ConfigurationException;
import com.qut.middleware.deployer.exception.RegisterESOEException;
import com.qut.middleware.deployer.exception.TestDatabaseException;
import com.qut.middleware.deployer.logic.RegisterESOELogic;
import com.qut.middleware.deployer.logic.TestDatabaseLogic;

/**
 * Provides an initial deployment of ESOE onto central servers
 * 
 * @author Bradley Beddoes
 */
public class Deployer {

	InputStreamReader converter;
	BufferedReader in;
	List<String> supportedLang;
	DeploymentBean deploymentBean;
	RegisterESOELogic register;
	TestDatabaseLogic testDBLogic;

	private final String lang_en = "en";

	public Deployer() {
		this.converter = new InputStreamReader(System.in);
		this.in = new BufferedReader(this.converter);
		this.deploymentBean = new DeploymentBean();
		
		this.supportedLang = new ArrayList<String>();
		this.supportedLang.add(lang_en);
	}

	/** 
	 * Determines language and proceeds to configure ESOE.
	 * 
	 * @throws IOException
	 * @throws ConfigurationException 
	 * @throws RegisterESOEException 
	 */
	public void execute() throws IOException, ConfigurationException, RegisterESOEException {
		outputln("***** Welcome to the ESOE deployer *****");
		outputln("You'll now be asked a series of questions to configure your environment\n");
		
		output("Language [en]: ");
		String lang = input("en");

		if (!this.supportedLang.contains(lang)) {
			outputln("Language not currently supported");
			return;
		}

		this.configureESOEEnvironment(lang);
		this.configureDataRepository(lang);
		this.configureESOEManager(lang);
		this.configureESOECrypto(lang);
		
		this.register.execute(this.deploymentBean);
	}
	
	private void configureESOEEnvironment(String lang) throws IOException, ConfigurationException 
	{
		final String URL = "url";
		final String ENTITYID = "entityID";
		final String COMMONDOMAIN = "domain";
		final String ORGNAME = "orgname";
		final String ORGDISPLAY= "orgdisplay";
		final String ORGURL = "orgurl";
		final String NAME = "name";
		final String EMAIL = "email";
		final String PHONE = "phone";
		final String COMPANY = "company";
		
		String url, entityID, commonDomain, orgname, orgdisplay, orgurl, name, email, phone, company;
		
		URL questions = Deployer.class.getResource("environment." + lang	+ ".txt");
		
		boolean validRepository = false;
		while(!validRepository)
		{
			Map<String, String> queryVariables = this.queryUser(questions, "ESOE Environment Configuration");
			
			url = queryVariables.get(URL);
			entityID = queryVariables.get(ENTITYID);
			commonDomain = queryVariables.get(COMMONDOMAIN);
			orgname = queryVariables.get(ORGNAME);
			orgdisplay = queryVariables.get(ORGDISPLAY);
			orgurl = queryVariables.get(ORGURL);
			name = queryVariables.get(NAME);
			email = queryVariables.get(EMAIL);
			phone = queryVariables.get(PHONE);
			company = queryVariables.get(COMPANY);
			
			if(url == null || url.length() == 0)
			{
				outputln("ESOE URL not provided for environment configuration");
				validRepository = false;
				continue;
			}
			if(entityID == null || entityID.length() == 0)
			{
				outputln("ESOE entityID not provided for environment configuration");
				validRepository = false;
				continue;
			}
			if(commonDomain == null || commonDomain.length() == 0)
			{
				outputln("Common Domain not provided for environment configuration");
				validRepository = false;
				continue;
			}
			if(orgname == null || orgname.length() == 0)
			{
				outputln("Organization name not provided for environment configuration");
				validRepository = false;
				continue;
			}
			if(orgdisplay == null || orgdisplay.length() == 0)
			{
				outputln("Organization display name not provided for environment configuration");
				validRepository = false;
				continue;
			}
			if(orgurl == null || orgurl.length() == 0)
			{
				outputln("Organization url not provided for environment configuration");
				validRepository = false;
				continue;
			}
			if(name == null || name.length() == 0)
			{
				outputln("Contact name not provided for environment configuration");
				validRepository = false;
				continue;
			}
			if(email == null || email.length() == 0)
			{
				outputln("Contact email not provided for environment configuration");
				validRepository = false;
				continue;
			}
			if(phone == null || phone.length() == 0)
			{
				outputln("Contact phone not provided for environment configuration");
				validRepository = false;
				continue;
			}
			if(company == null || company.length() == 0)
			{
				outputln("Contact company not provided for environment configuration");
				validRepository = false;
				continue;
			}
			
			this.deploymentBean.setEsoeNode(url);
			this.deploymentBean.setEsoeEntityID(entityID);
			this.deploymentBean.setCommonDomain(commonDomain);
			
			this.deploymentBean.setEsoeOrganizationName(orgname);
			this.deploymentBean.setEsoeOrganizationDisplayName(orgdisplay);
			this.deploymentBean.setEsoeOrganizationURL(orgurl);
			
			ContactPersonBean contact = new ContactPersonBean();
			contact.setName(name);
			contact.setEmail(email);
			contact.setTelephone(phone);
			contact.setCompany(company);
			
			this.deploymentBean.setContact(contact);
			
			validRepository = true;
		}
	}
	
	private void configureESOEManager(String lang) throws IOException, ConfigurationException 
	{
		final String URL = "url";
		final String ENTITYID = "entityID";
				
		String url, entityID; 
		
		URL questions = Deployer.class.getResource("management." + lang	+ ".txt");
		
		boolean validRepository = false;
		while(!validRepository)
		{
			Map<String, String> queryVariables = this.queryUser(questions, "ESOE Manager Configuration");
			
			url = queryVariables.get(URL);
			entityID = queryVariables.get(ENTITYID);
			
			if(url == null || url.length() == 0)
			{
				outputln("Host not provided for ESOE manager configuration");
				validRepository = false;
				continue;
			}
			if(entityID == null || entityID.length() == 0)
			{
				outputln("EntityID not provided for ESOE manager configuration");
				validRepository = false;
				continue;
			}
			
			this.deploymentBean.setManagerServiceNode(url);
			this.deploymentBean.setManagerEntityID(entityID);
			
			validRepository = true;
		}
	}
	
	private void configureESOECrypto(String lang) throws IOException, ConfigurationException 
	{
		final String ISSUER = "issuer";
		final String MAIL = "mail";
				
		String issuer, mail; 
		
		URL questions = Deployer.class.getResource("crypto." + lang	+ ".txt");
		
		boolean validRepository = false;
		while(!validRepository)
		{
			Map<String, String> queryVariables = this.queryUser(questions, "ESOE Crypto Configuration");
			
			issuer = queryVariables.get(ISSUER);
			mail = queryVariables.get(MAIL);
			
			if(mail == null || mail.length() == 0)
			{
				outputln("Email data not provided for crypto configuration");
				validRepository = false;
				continue;
			}
			if(issuer == null || issuer.length() == 0)
			{
				outputln("Issuer DN data not provided for crypto configuration");
				validRepository = false;
				continue;
			}
			
			this.deploymentBean.setCertIssuerDN(issuer);
			this.deploymentBean.setCertIssuerEmailAddress(mail);
			
			validRepository = true;
		}
	}

	/** 
	 * Determines settings for ESOE data repository.
	 * 
	 * @param lang Language in user by the user
	 * @throws IOException
	 */
	private void configureDataRepository(String lang) throws IOException, ConfigurationException {
		
		final String DRIVER = "driver";
		final String URL = "url";
		final String USERNAME = "username";
		final String PASSWORD = "password";
		
		String driver, url, username, password;
		
		URL questions = Deployer.class.getResource("repository." + lang	+ ".txt");
		
		boolean validRepository = false;
		while(!validRepository)
		{
			Map<String, String> queryVariables = this.queryUser(questions, "ESOE Data Repository Configuration");
			
			driver = queryVariables.get(DRIVER);
			url = queryVariables.get(URL);
			username = queryVariables.get(USERNAME);
			password = queryVariables.get(PASSWORD);
			
			if(driver == null || driver.length() == 0)
			{
				outputln("Driver data not provided for data repository configuration");
				validRepository = false;
				continue;
			}
			if(url == null || url.length() == 0)
			{
				outputln("URL data not provided for data repository configuration");
				validRepository = false;
				continue;
			}
			if(username == null || username.length() == 0)
			{
				outputln("Username data not provided for data repository configuration");
				validRepository = false;
				continue;
			}
			if(password == null || password.length() == 0)
			{
				outputln("Password data not provided for data repository configuration");
				validRepository = false;
				continue;
			}
			
			try 
			{
				if(driver.equals(Constants.MYSQL))
				{
					testDBLogic.testDatabase(Constants.DatabaseDrivers.mysql, url, username, password);
					this.deploymentBean.setDatabaseDriver(Constants.DatabaseDrivers.mysql);
				}
				else
				{
					testDBLogic.testDatabase(Constants.DatabaseDrivers.oracle, url, username, password);
					this.deploymentBean.setDatabaseDriver(Constants.DatabaseDrivers.oracle);
				}
				
				this.deploymentBean.setDatabaseURL(url);
				this.deploymentBean.setDatabaseUsername(username);
				this.deploymentBean.setDatabasePassword(password);
				
				validRepository = true;
			} 
			catch (TestDatabaseException e) {
				outputln("Unable to successfully connect with database\nError was:\n" + e.getLocalizedMessage());
			}
		}
	}

	
	/** Loads a question formatted text file to query user
	 * @param data A question formatted file to parse and present to user
	 * @return Populated list of questions for further parsing
	 * @throws IOException
	 */
	private List<String> loadQuestions(URL data) throws IOException {
		InputStreamReader reader = new InputStreamReader(data.openStream());
		BufferedReader buf = new BufferedReader(reader);

		List<String> questions = new ArrayList<String>();
		
		String val = buf.readLine();
		while (val != null) {
			if (!val.startsWith("#") && val.length() > 1) {
				questions.add(val);
			}
			val = buf.readLine();
		}
		
		return questions;
	}

	/** Processes the provided question file, undertakes all user querying and responds with configuration data
	 * @param data A question formatted file to parse and present to user
	 * @param sectionName The name of this configuration section
	 * @return Populated configuration data per question file format
	 * @throws IOException
	 */
	private Map<String, String> queryUser(URL data, String sectionName) throws IOException {
		if (data == null)
			throw new IOException(
					"Can't locate data file, passed value was null");

		List<String> questions = loadQuestions(data);

		Map<String, String> answers = new HashMap<String, String>();
		boolean valid = false;
		
		outputln("\n*****\n" + sectionName + "\n*****");

		while (!valid) {
			for (String question : questions) {
				String res;
				boolean resValid = false;
				
				String[] components = question.split("\\|");
				if(components.length < 2)
				{
					outputln("Error in question configuration format");
					continue;
				}
				
				while (!resValid) {
					String key = components[0];
					String query = components[1];
					
					output("(" + key + ") " + query );

					if (components.length >= 3)
						res = input(components[2]);
					else
						res = input("");

					if (components.length == 4) {
						String[] validValues = components[3].split(",");
						for (String value : validValues) {
							if (res.equals(value)) {
								resValid = true;
								answers.put((String) key, res);
								break;
							}
						}

						if (!resValid) {
							outputln("Entered value was not valid");
						}
					} else {
						answers.put((String) key, res);
						resValid = true;
					}
				}
			}

			output("\nAre all values you've entered in this section correct? y/n [n]:");

			String validInput = input("n");
			if (validInput.equals("Y") | validInput.equals("y"))
				valid = true;
			else
				outputln("--");
		}
		return answers;
	}

	private void output(String output) {
		System.out.print(output);
	}

	private void outputln(String output) {
		System.out.println(output);
	}

	private String input(String def) throws IOException {
		String input = this.in.readLine();

		if (input == null || input.length() == 0)
			return def;

		return input;
	}

	public RegisterESOELogic getRegister() {
		return register;
	}

	public void setRegister(RegisterESOELogic register) {
		this.register = register;
	}

	public TestDatabaseLogic getTestDBLogic() {
		return testDBLogic;
	}

	public void setTestDBLogic(TestDatabaseLogic testLogic) {
		this.testDBLogic = testLogic;
	}
}

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
 * Creation Date: 24/9/2007
 *
 * Purpose: Tests OpenID AuthnProcessorImpl
 */
package com.qut.middleware.delegator.openid.authn.bean;


import static org.easymock.EasyMock.createMock;

import java.util.List;

import org.junit.After;
import org.junit.Before;

import com.qut.middleware.crypto.KeystoreResolver;
import com.qut.middleware.delegator.openid.authn.impl.AuthnProcessorImpl;
import com.qut.middleware.esoe.ws.WSClient;
import com.qut.middleware.saml2.identifier.IdentifierGenerator;
import com.qut.middleware.saml2.validator.SAMLValidator;

/**
 * @author beddoes
 *
 */
public class AuthnProcessorImplTest
{
	String responseEndpoint;
	String issuerID;
	String principalRegistrationEndpoint;
	String userIdentifier;
	boolean httpsOffload;

	List<OpenIDAttribute> defaultSiteAttributes;
	List<OpenIDAttribute> requestedAttribute;

	AuthnProcessorImpl authnProcessor;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		SAMLValidator validator = createMock(SAMLValidator.class);
		WSClient wsClient = createMock(WSClient.class);
		IdentifierGenerator identiferGenerator = createMock(IdentifierGenerator.class);
		KeystoreResolver keyStoreResolver = createMock(KeystoreResolver.class);


		responseEndpoint = "https://esoe.test.com";
		issuerID = "https://openiddeleg.esoe.test.com";
		principalRegistrationEndpoint = "https://esoe.test.com/ws/services/delegators";
		userIdentifier = "uid";
		httpsOffload = false;


		//this.authnProcessor =  = new AuthnProcessorImpl();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception
	{
	}

}

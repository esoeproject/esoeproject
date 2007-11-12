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
 * Creation Date: 04/04/2007
 * 
 * Purpose: 
 */

package com.qut.middleware.esoe;

import static com.qut.middleware.test.Capture.capture;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import org.apache.xerces.jaxp.SAXParserFactoryImpl;
import org.apache.xerces.jaxp.validation.XMLSchemaFactory;
import org.easymock.IAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.context.WebApplicationContext;
import org.w3._2000._09.xmldsig_.Signature;

import com.qut.middleware.esoe.aa.AttributeAuthorityProcessor;
import com.qut.middleware.esoe.aa.impl.AttributeAuthorityProcessorImpl;
import com.qut.middleware.esoe.authn.AuthnProcessor;
import com.qut.middleware.esoe.authn.bean.AuthnIdentityAttribute;
import com.qut.middleware.esoe.authn.bean.AuthnProcessorData;
import com.qut.middleware.esoe.authn.bean.impl.AuthnIdentityAttributeImpl;
import com.qut.middleware.esoe.authn.bean.impl.AuthnProcessorDataImpl;
import com.qut.middleware.esoe.authn.impl.AuthnProcessorImpl;
import com.qut.middleware.esoe.authn.pipeline.UserPassAuthenticator;
import com.qut.middleware.esoe.authn.pipeline.Authenticator.result;
import com.qut.middleware.esoe.authn.pipeline.handlers.UsernamePasswordHandler;
import com.qut.middleware.esoe.authn.servlet.AuthnServlet;
import com.qut.middleware.esoe.crypto.KeyStoreResolver;
import com.qut.middleware.esoe.delegauthn.DelegatedAuthenticationProcessor;
import com.qut.middleware.esoe.delegauthn.impl.DelegatedAuthenticationProcessorImpl;
import com.qut.middleware.esoe.metadata.Metadata;
import com.qut.middleware.esoe.pdp.AuthorizationProcessor;
import com.qut.middleware.esoe.pdp.cache.AuthzCacheUpdateFailureRepository;
import com.qut.middleware.esoe.pdp.cache.PolicyCacheProcessor;
import com.qut.middleware.esoe.pdp.cache.bean.AuthzPolicyCache;
import com.qut.middleware.esoe.pdp.cache.bean.impl.AuthzPolicyCacheImpl;
import com.qut.middleware.esoe.pdp.cache.impl.AuthzCacheUpdateFailureRepositoryImpl;
import com.qut.middleware.esoe.pdp.cache.impl.PolicyCacheProcessorImpl;
import com.qut.middleware.esoe.pdp.cache.sqlmap.PolicyCacheDao;
import com.qut.middleware.esoe.pdp.cache.sqlmap.impl.PolicyCacheData;
import com.qut.middleware.esoe.pdp.cache.sqlmap.impl.PolicyCacheQueryData;
import com.qut.middleware.esoe.pdp.impl.AuthorizationProcessorImpl;
import com.qut.middleware.esoe.sessions.Create;
import com.qut.middleware.esoe.sessions.Query;
import com.qut.middleware.esoe.sessions.SessionsProcessor;
import com.qut.middleware.esoe.sessions.Terminate;
import com.qut.middleware.esoe.sessions.Update;
import com.qut.middleware.esoe.sessions.bean.IdentityAttribute;
import com.qut.middleware.esoe.sessions.bean.IdentityData;
import com.qut.middleware.esoe.sessions.bean.SessionConfigData;
import com.qut.middleware.esoe.sessions.cache.SessionCache;
import com.qut.middleware.esoe.sessions.cache.impl.SessionCacheImpl;
import com.qut.middleware.esoe.sessions.exception.DataSourceException;
import com.qut.middleware.esoe.sessions.identity.IdentityResolver;
import com.qut.middleware.esoe.sessions.identity.impl.IdentityResolverImpl;
import com.qut.middleware.esoe.sessions.identity.pipeline.Handler;
import com.qut.middleware.esoe.sessions.impl.CreateImpl;
import com.qut.middleware.esoe.sessions.impl.QueryImpl;
import com.qut.middleware.esoe.sessions.impl.SessionsProcessorImpl;
import com.qut.middleware.esoe.sessions.impl.TerminateImpl;
import com.qut.middleware.esoe.sessions.impl.UpdateImpl;
import com.qut.middleware.esoe.spep.SPEPProcessor;
import com.qut.middleware.esoe.spep.SPEPRegistrationCache;
import com.qut.middleware.esoe.spep.Startup;
import com.qut.middleware.esoe.spep.impl.SPEPProcessorImpl;
import com.qut.middleware.esoe.spep.impl.SPEPRegistrationCacheImpl;
import com.qut.middleware.esoe.spep.impl.StartupImpl;
import com.qut.middleware.esoe.spep.sqlmap.SPEPRegistrationDao;
import com.qut.middleware.esoe.sso.SSOProcessor;
import com.qut.middleware.esoe.sso.bean.FailedLogoutRepository;
import com.qut.middleware.esoe.sso.bean.SSOProcessorData;
import com.qut.middleware.esoe.sso.bean.impl.FailedLogoutRepositoryImpl;
import com.qut.middleware.esoe.sso.impl.AuthenticationAuthorityProcessor;
import com.qut.middleware.esoe.sso.impl.LogoutAuthorityProcessor;
import com.qut.middleware.esoe.sso.servlet.SSOAAServlet;
import com.qut.middleware.esoe.sso.servlet.SSOLogoutServlet;
import com.qut.middleware.esoe.ws.WSClient;
import com.qut.middleware.esoe.ws.WSProcessor;
import com.qut.middleware.esoe.ws.exception.WSClientException;
import com.qut.middleware.esoe.ws.impl.WSProcessorImpl;
import com.qut.middleware.saml2.NameIDFormatConstants;
import com.qut.middleware.saml2.StatusCodeConstants;
import com.qut.middleware.saml2.VersionConstants;
import com.qut.middleware.saml2.exception.MarshallerException;
import com.qut.middleware.saml2.exception.UnmarshallerException;
import com.qut.middleware.saml2.handler.Marshaller;
import com.qut.middleware.saml2.handler.Unmarshaller;
import com.qut.middleware.saml2.handler.impl.MarshallerImpl;
import com.qut.middleware.saml2.handler.impl.UnmarshallerImpl;
import com.qut.middleware.saml2.identifier.IdentifierCache;
import com.qut.middleware.saml2.identifier.IdentifierGenerator;
import com.qut.middleware.saml2.identifier.impl.IdentifierCacheImpl;
import com.qut.middleware.saml2.identifier.impl.IdentifierGeneratorImpl;
import com.qut.middleware.saml2.schemas.assertion.Assertion;
import com.qut.middleware.saml2.schemas.assertion.AuthnStatement;
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.assertion.StatementAbstractType;
import com.qut.middleware.saml2.schemas.esoe.lxacml.assertion.LXACMLAuthzDecisionStatement;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.Attribute;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.AttributeValue;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.DecisionType;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.Request;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.Resource;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.Subject;
import com.qut.middleware.saml2.schemas.esoe.lxacml.protocol.LXACMLAuthzDecisionQuery;
import com.qut.middleware.saml2.schemas.esoe.protocol.ClearAuthzCacheRequest;
import com.qut.middleware.saml2.schemas.esoe.protocol.ClearAuthzCacheResponse;
import com.qut.middleware.saml2.schemas.esoe.sessions.AttributeType;
import com.qut.middleware.saml2.schemas.esoe.sessions.DataType;
import com.qut.middleware.saml2.schemas.esoe.sessions.IdentityType;
import com.qut.middleware.saml2.schemas.protocol.AuthnRequest;
import com.qut.middleware.saml2.schemas.protocol.LogoutRequest;
import com.qut.middleware.saml2.schemas.protocol.NameIDPolicy;
import com.qut.middleware.saml2.schemas.protocol.Response;
import com.qut.middleware.saml2.schemas.protocol.Status;
import com.qut.middleware.saml2.schemas.protocol.StatusCode;
import com.qut.middleware.saml2.validator.SAMLValidator;
import com.qut.middleware.saml2.validator.impl.SAMLValidatorImpl;
import com.qut.middleware.test.Capture;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

public class CompleteESOETest
{
	Logger logger = Logger.getLogger(CompleteESOETest.class);

	private class SPEPInformation
	{
		public String entityDesciptorID;
		public String keyAlias;
		public KeyPair keyPair;
		public String authzCacheClearEndpoint;
		public String singleLogoutServiceEndpoint;

		public Marshaller<ClearAuthzCacheResponse> clearAuthzCacheResponseMarshaller;
		public Marshaller<Response> logoutResponseMarshaller;
		public Marshaller<AuthnRequest> authnRequestMarshaller;
		public Marshaller<LXACMLAuthzDecisionQuery> lxacmlAuthzDecisionQueryMarshaller;

		public SPEPInformation()
		{
		}
	}

	private class UserInformation
	{
		public String username;
		public String password;
		public Map<String, String> attributeValues;
		public Map<String, Boolean> expectedAuthzBehaviour;

		public UserInformation()
		{
		}
	}

	private class TestHttpSession implements HttpSession
	{
		private static final int DEFAULT_INACTIVE_INTERVAL = 600;

		protected Map<String, Object> attributes;
		protected Date creationTime;
		protected String id;
		protected IdentifierGenerator idGen;
		protected ServletContext servletContext;
		protected int inactiveInterval;

		public void reset()
		{
			attributes = new HashMap<String, Object>();
			creationTime = new Date();
			id = idGen.generateSAMLSessionID();
			inactiveInterval = DEFAULT_INACTIVE_INTERVAL;
		}

		TestHttpSession(IdentifierGenerator idGen)
		{
			this.idGen = idGen;
			reset();
		}

		TestHttpSession(TestHttpSession other, ServletContext servletContext)
		{
			attributes = other.attributes;
			creationTime = other.creationTime;
			id = other.id;
			idGen = other.idGen;
			this.servletContext = servletContext;
			inactiveInterval = other.inactiveInterval;
		}

		public Object getAttribute(String name)
		{
			return attributes.get(name);
		}

		public Enumeration<?> getAttributeNames()
		{
			return new Enumeration<Object>()
			{
				private Iterator<String> attributeNames;
				{
					attributeNames = TestHttpSession.this.attributes.keySet().iterator();
				}

				public boolean hasMoreElements()
				{
					return attributeNames.hasNext();
				}

				public Object nextElement()
				{
					return attributeNames.next();
				}
			};
		}

		public long getCreationTime()
		{
			return creationTime.getTime();
		}

		public String getId()
		{
			return this.id;
		}

		public long getLastAccessedTime()
		{
			return creationTime.getTime();
		}

		public int getMaxInactiveInterval()
		{
			return 120;
		}

		public ServletContext getServletContext()
		{
			return this.servletContext;
		}

		public HttpSessionContext getSessionContext()
		{
			return null;
		}

		public Object getValue(String name)
		{
			return attributes.get(name);
		}

		public String[] getValueNames()
		{
			// TODO Auto-generated method stub
			return null;
		}

		public void invalidate()
		{
			reset();
		}

		public boolean isNew()
		{
			return false;
		}

		public void putValue(String name, Object value)
		{
			attributes.put(name, value);
		}

		public void removeAttribute(String name)
		{
			attributes.remove(name);
		}

		public void removeValue(String name)
		{
			attributes.remove(name);
		}

		public void setAttribute(String name, Object value)
		{
			attributes.put(name, value);
		}

		public void setMaxInactiveInterval(int interval)
		{
			this.inactiveInterval = interval;
		}
	}

	public static final String AUTHN_DYNAMIC_URL_PARAM = "redirectURL";
	public static final String SESSION_TOKEN_NAME = "esoeSession";
	public static final String COOKIE_SESSION_DOMAIN = "example.com";
	public static final String DISABLE_SSO_TOKEN_NAME = "esoeNoAuto";
	public static final String AUTHN_REDIRECT_URL = "https://esoe.example.com/signin";
	public static final String LOGOUT_REDIRECT_URL = "https://esoe.example.com/logout.jsp";
	public static final String LOGOUT_RESPONSE_REDIRECT_URL = "https://esoe.example.com/logout_success.jsp";
	public static final String REQUIRE_CREDENTIALS_URL = "https://esoe.example.com/login.jsp";
	public static final String AUTHENTICATION_FAILED_NAME_VALUE = "rc=authnfail";
	public static final String AUTHENTICATION_SUCCESS_URL = "https://esoe.example.com/login_success.jsp";
	public static final String AUTHENTICATION_FAIL_URL = "https://esoe.example.com/failure.jsp";

	public static final String SSO_URL = "ssoURL";

	private static final String ESOE_SERVER_NAME = "esoe.example.com";

	private static final String KEY_ALGORITHM = "RSA";
	// Not very secure, but who cares? We're not testing the performance of large signatures here.
	private static final int KEY_SIZE = 512;

	private static final int MAX_SESSION_LENGTH = 120;
	private static final int ALLOWED_TIME_SKEW = 300;
	private static final int POLICY_POLL_INTERVAL = 120;
	private static final int SESSION_REMAINING_TIME = 0;

	private static final String UID_ATTRIBUTE = "uid";

	private static final String AUTHORIZATION_DEFAULT_MODE = "DENY";

	private static final String SECURITY_LEVEL_IDENTIFIER = "SecurityLevel";
	private static final String SECURITY_LEVEL_1 = "Level 1";
	private static final String SECURITY_LEVEL_2 = "Level 2";

	private static final int NUMBER_OF_SPEPS = 3;
	private static final int NUMBER_OF_THREADS = 5;
	private static final int NUMBER_OF_RUNS = 5;

	private List<Exception> errors;

	private List<SPEPInformation> speps;
	private UnmarshallerImpl<ClearAuthzCacheRequest> clearAuthzCacheRequestUnmarshaller;
	private Unmarshaller<Response> authnResponseUnmarshaller;
	private IdentifierCache localIdentifierCache;
	private IdentifierGenerator localIdentifierGenerator;
	private Unmarshaller<LogoutRequest> logoutRequestUnmarshaller;
	private UnmarshallerImpl<Response> lxacmlAuthzDecisionResponseUnmarshaller;
	private Metadata metadata;
	private KeyStoreResolver keystoreResolver;
	private SessionConfigData sessionConfigData;
	private SPEPRegistrationDao spepRegistrationDao;
	private PolicyCacheDao policyCacheDao;
	private List<UserInformation> goodUserInformation;
	private List<String[]> badUsernamePasswordPairs;
	private SSOAAServlet ssoAAServlet;
	private SSOLogoutServlet ssoLogoutServlet;
	private AuthnServlet authnServlet;
	private UserPassAuthenticator userPassAuthenticator;
	private ServletConfig servletConfig;
	private ServletContext servletContext;
	private WebApplicationContext webApplicationContext;
	private Random random;
	private boolean up = false;
	private XMLInputFactory xmlInputFactory;
	private Map<String, UserInformation> userMap;

	private static final String XML_DOCUMENT_BUILDER_FACTORY_CLASS = DocumentBuilderFactoryImpl.class.getName();
	private static final String XML_SAX_PARSER_FACTORY_CLASS = SAXParserFactoryImpl.class.getName();
	private static final String XML_TRANSFORMER_FACTORY_CLASS = TransformerFactoryImpl.class.getName();
	private static final String XML_SCHEMA_FACTORY_CLASS = XMLSchemaFactory.class.getName();

	@Before
	public void setUp() throws Exception
	{
		System.setProperty("javax.xml.parsers.DocumentBuilderFactory", XML_DOCUMENT_BUILDER_FACTORY_CLASS);
		System.setProperty("javax.xml.parsers.SAXParserFactory", XML_SAX_PARSER_FACTORY_CLASS);
		System.setProperty("javax.xml.transform.TransformerFactory", XML_TRANSFORMER_FACTORY_CLASS);
		System.setProperty("javax.xml.validation.SchemaFactory", XML_SCHEMA_FACTORY_CLASS);
		System.setProperty("java.protocol.handler.pkgs", "com.qut.middleware.esoe");

		this.random = new Random();
		this.userMap = Collections.synchronizedMap(new HashMap<String, UserInformation>());

		errors = Collections.synchronizedList(new Vector<Exception>());

		// Identifier cache/generator from SAML2lib-j
		IdentifierCache identifierCache = new IdentifierCacheImpl();
		IdentifierGenerator identifierGenerator = new IdentifierGeneratorImpl(identifierCache);

		this.localIdentifierCache = new IdentifierCacheImpl();
		this.localIdentifierGenerator = new IdentifierGeneratorImpl(this.localIdentifierCache);

		metadata = createMock(Metadata.class);
		// SAML Validator from SAML2lib-j
		SAMLValidator samlValidator = new SAMLValidatorImpl(identifierCache, ALLOWED_TIME_SKEW);

		// Generate key pairs and identifiers for the ESOE and SPEPs
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
		keyPairGenerator.initialize(KEY_SIZE);

		// Generate key pair for ESOE
		logger.debug("Generating key pairs..");
		KeyPair esoeKeyPair = keyPairGenerator.generateKeyPair();
		String esoeKeyAlias = identifierGenerator.generateXMLKeyName();
		String esoeEntityDescriptorID = identifierGenerator.generateSAMLID();
		logger.debug("Generated ESOE key pair.");

		expect(metadata.getEsoeEntityID()).andReturn(esoeEntityDescriptorID).anyTimes();
		expect(metadata.resolveKey(esoeKeyAlias)).andReturn(esoeKeyPair.getPublic()).anyTimes();

		// Create Marshaller/Unmarshaller instances
		String[] clearAuthzCacheSchemas = new String[] { ConfigurationConstants.esoeProtocol, ConfigurationConstants.samlAssertion, ConfigurationConstants.samlProtocol };
		this.clearAuthzCacheRequestUnmarshaller = new UnmarshallerImpl<ClearAuthzCacheRequest>(ClearAuthzCacheRequest.class.getPackage().getName(), clearAuthzCacheSchemas, metadata);
		String[] logoutSchemas = new String[] { ConfigurationConstants.samlProtocol };
		this.logoutRequestUnmarshaller = new UnmarshallerImpl<LogoutRequest>(LogoutRequest.class.getPackage().getName(), logoutSchemas, metadata);
		String[] authnSchemas = new String[] { ConfigurationConstants.samlProtocol, ConfigurationConstants.samlAssertion };
		this.authnResponseUnmarshaller = new UnmarshallerImpl<Response>(Response.class.getPackage().getName(), authnSchemas, metadata);
		String[] authzDecisionSchemas = new String[] { ConfigurationConstants.lxacmlSAMLAssertion, ConfigurationConstants.lxacmlSAMLProtocol, ConfigurationConstants.samlProtocol };
		this.lxacmlAuthzDecisionResponseUnmarshaller = new UnmarshallerImpl<Response>(Response.class.getPackage().getName(), authzDecisionSchemas, metadata);

		this.speps = new Vector<SPEPInformation>();
		for (int i = 0; i < NUMBER_OF_SPEPS; ++i)
		{
			SPEPInformation spep = new SPEPInformation();
			spep.keyPair = keyPairGenerator.generateKeyPair();
			spep.keyAlias = identifierGenerator.generateXMLKeyName();
			spep.entityDesciptorID = identifierGenerator.generateSAMLID();
			spep.authzCacheClearEndpoint = MessageFormat.format("https://spep{0}.example.com/spep/ws/services/authzCacheClear", i);
			spep.singleLogoutServiceEndpoint = MessageFormat.format("https://spep{0}.example.com/spep/ws/services/logout", i);

			spep.clearAuthzCacheResponseMarshaller = new MarshallerImpl<ClearAuthzCacheResponse>(ClearAuthzCacheResponse.class.getPackage().getName(), clearAuthzCacheSchemas, spep.keyAlias, spep.keyPair.getPrivate());
			spep.logoutResponseMarshaller = new MarshallerImpl<Response>(Response.class.getPackage().getName(), logoutSchemas, spep.keyAlias, spep.keyPair.getPrivate());
			spep.authnRequestMarshaller = new MarshallerImpl<AuthnRequest>(AuthnRequest.class.getPackage().getName(), authnSchemas, spep.keyAlias, spep.keyPair.getPrivate());
			spep.lxacmlAuthzDecisionQueryMarshaller = new MarshallerImpl<LXACMLAuthzDecisionQuery>(LXACMLAuthzDecisionQuery.class.getPackage().getName(), authzDecisionSchemas, spep.keyAlias, spep.keyPair.getPrivate());

			logger.error("Generated SPEP " + i + " key pair.");

			Map<Integer, String> cacheClearServiceMap = new HashMap<Integer, String>();
			cacheClearServiceMap.put(0, spep.authzCacheClearEndpoint);
			List<String> singleLogoutServiceList = new Vector<String>();
			singleLogoutServiceList.add(spep.singleLogoutServiceEndpoint);

			expect(metadata.resolveAssertionConsumerService(spep.entityDesciptorID, 0)).andReturn(MessageFormat.format("https://spep{0}.example.com/spep/sso", i)).anyTimes();
			expect(metadata.resolveCacheClearService(spep.entityDesciptorID)).andReturn(cacheClearServiceMap).anyTimes();
			expect(metadata.resolveKey(spep.keyAlias)).andReturn(spep.keyPair.getPublic()).anyTimes();
			expect(metadata.resolveSingleLogoutService(spep.entityDesciptorID)).andReturn(singleLogoutServiceList).anyTimes();

			this.speps.add(spep);
		}

		keystoreResolver = createMock(KeyStoreResolver.class);
		expect(keystoreResolver.getPrivateKey()).andReturn(esoeKeyPair.getPrivate()).anyTimes();
		expect(keystoreResolver.getPublicKey()).andReturn(esoeKeyPair.getPublic()).anyTimes();
		expect(keystoreResolver.getKeyAlias()).andReturn(esoeKeyAlias).anyTimes();

		// Session cache
		SessionCache sessionCache = new SessionCacheImpl();

		List<Handler> identityHandlers = new Vector<Handler>();
		// Add handlers to the identity handlers list
		Handler identityHandler = new Handler()
		{
			public result execute(IdentityData data) throws DataSourceException
			{
				String principal = data.getPrincipalAuthnIdentifier();
				UserInformation info = CompleteESOETest.this.userMap.get(principal);

				for (Map.Entry<String, IdentityAttribute> entry : data.getAttributes().entrySet())
				{
					for (Map.Entry<String, String> attributeEntry : info.attributeValues.entrySet())
					{
						if (attributeEntry.getKey().equals(entry.getKey()))
						{
							entry.getValue().addValue(attributeEntry.getValue());
						}
					}
				}

				return result.Successful;
			}

			public String getHandlerName()
			{
				return "SystemTestHandler";
			}
		};
		identityHandlers.add(identityHandler);

		IdentityResolver identityResolver = new IdentityResolverImpl(identityHandlers);

		// List of identity data
		List<IdentityType> identityList = new Vector<IdentityType>();
		IdentityType identity = new IdentityType();
		AttributeType uidAttribute = new AttributeType();
		uidAttribute.setIdentifier(UID_ATTRIBUTE);
		uidAttribute.setSingleton(true);
		uidAttribute.setType(DataType.STRING);
		AttributeType somethingAttribute = new AttributeType();
		somethingAttribute.setIdentifier("SomethingLevel");
		somethingAttribute.setSingleton(true);
		somethingAttribute.setType(DataType.STRING);
		identity.getAttributes().add(uidAttribute);
		identity.getAttributes().add(somethingAttribute);
		identityList.add(identity);

		sessionConfigData = createMock(SessionConfigData.class);
		expect(sessionConfigData.getIdentity()).andReturn(identityList).anyTimes();

		// Session manipulation interfaces for the sessions processor.
		Create create = new CreateImpl(sessionCache, sessionConfigData, identityResolver, identifierGenerator, MAX_SESSION_LENGTH);
		Query query = new QueryImpl(sessionCache);
		Terminate terminate = new TerminateImpl(sessionCache);
		Update update = new UpdateImpl(sessionCache);

		SessionsProcessor sessionsProcessor = new SessionsProcessorImpl(create, query, terminate, update);

		userPassAuthenticator = createMock(UserPassAuthenticator.class);
		goodUserInformation = new Vector<UserInformation>();
		badUsernamePasswordPairs = new Vector<String[]>();
		// Number of users == Number of threads
		for (int i = 0; i < NUMBER_OF_THREADS; ++i)
		{
			UserInformation user = new UserInformation();

			user.username = identifierGenerator.generateXMLKeyName();
			user.password = identifierGenerator.generateXMLKeyName();

			user.attributeValues = new HashMap<String, String>();
			user.attributeValues.put(UID_ATTRIBUTE, user.username);

			user.expectedAuthzBehaviour = new HashMap<String, Boolean>();
			user.expectedAuthzBehaviour.put("/secure/index.jsp", Boolean.TRUE);

			boolean highAuth = ((i % 2) == 0);

			if (highAuth)
			{
				user.attributeValues.put("SomethingLevel", SECURITY_LEVEL_2);
				user.expectedAuthzBehaviour.put("/secure/higherauth.jsp", Boolean.TRUE);
			}
			else
			{
				user.attributeValues.put("SomethingLevel", SECURITY_LEVEL_1);
				user.expectedAuthzBehaviour.put("/secure/higherauth.jsp", Boolean.FALSE);
			}

			expect(userPassAuthenticator.authenticate(user.username, user.password)).andReturn(result.Successful).anyTimes();

			goodUserInformation.add(user);

			String username = identifierGenerator.generateXMLKeyName();
			String password = identifierGenerator.generateXMLKeyName();
			badUsernamePasswordPairs.add(new String[] { username, password });
			expect(userPassAuthenticator.authenticate(username, password)).andReturn(result.Failure).anyTimes();

			this.userMap.put(user.username, user);
		}

		replay(userPassAuthenticator);
		List<com.qut.middleware.esoe.authn.pipeline.Handler> authnHandlers = new Vector<com.qut.middleware.esoe.authn.pipeline.Handler>();
		// Add handlers to the authn handlers list
		List<AuthnIdentityAttribute> authnIdentityAttributeList = new Vector<AuthnIdentityAttribute>();
		AuthnIdentityAttribute securityLevelAttribute = new AuthnIdentityAttributeImpl();
		securityLevelAttribute.setName(SECURITY_LEVEL_IDENTIFIER);
		List<String> securityLevelValues = new Vector<String>();
		// securityLevelValues.add( SECURITY_LEVEL_1 );
		securityLevelAttribute.setValues(securityLevelValues);
		authnIdentityAttributeList.add(securityLevelAttribute);

		UsernamePasswordHandler handler = new UsernamePasswordHandler(userPassAuthenticator, sessionsProcessor, identifierGenerator, authnIdentityAttributeList, REQUIRE_CREDENTIALS_URL, AUTHENTICATION_FAILED_NAME_VALUE, AUTHENTICATION_SUCCESS_URL, AUTHENTICATION_FAIL_URL);
		authnHandlers.add(handler);

		WSClient wsClient = new WSClient()
		{
			public byte[] authzCacheClear(byte[] request, String endpoint) throws WSClientException
			{
				return CompleteESOETest.this.respondAuthzCacheClear(request, endpoint);
			}

			public byte[] singleLogout(byte[] request, String endpoint) throws WSClientException
			{
				return CompleteESOETest.this.respondSingleLogout(request, endpoint);
			}
		};

		List<String> deniedIdentifiers = new Vector<String>();
		deniedIdentifiers.add(SECURITY_LEVEL_IDENTIFIER);

		String delegatedAuthnIdentifier = esoeEntityDescriptorID;

		spepRegistrationDao = createMock(SPEPRegistrationDao.class);

		policyCacheDao = createMock(PolicyCacheDao.class);
		expect(policyCacheDao.queryLastSequenceId()).andAnswer(new IAnswer<Long>()
		{
			public Long answer() throws Throwable
			{
				return new Long(System.currentTimeMillis());
			}
		}).anyTimes();

		expect(policyCacheDao.queryPolicyCache((PolicyCacheQueryData) anyObject())).andReturn(makePolicyCacheDataMap()).anyTimes();

		servletConfig = createMock(ServletConfig.class);
		servletContext = createMock(ServletContext.class);

		expect(servletConfig.getServletContext()).andReturn(servletContext).anyTimes();
		expect(servletConfig.getInitParameterNames()).andReturn(new Enumeration<Object>()
		{
			public boolean hasMoreElements()
			{
				return false;
			}

			public Object nextElement()
			{
				return null;
			}
		}).anyTimes();
		expect(servletConfig.getInitParameter((String) notNull())).andReturn(null).anyTimes();

		URL esoeconfigURL = new URL("esoeconfig://just.a.test/");

		expect(servletContext.getResource(ConfigurationConstants.ESOE_CONFIG)).andReturn(esoeconfigURL).anyTimes();

		webApplicationContext = createMock(WebApplicationContext.class);
		expect(servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE)).andReturn(webApplicationContext).anyTimes();

		replay(metadata);
		replay(keystoreResolver);
		replay(sessionConfigData);
		replay(spepRegistrationDao);
		replay(policyCacheDao);
		replay(servletConfig);
		replay(servletContext);
		
		Map<String, String>identifierMap = new HashMap<String, String>();
		identifierMap.put(NameIDFormatConstants.emailAddress, "mail");

		AuthzCacheUpdateFailureRepository authzCacheUpdateFailureRepository = new AuthzCacheUpdateFailureRepositoryImpl();

		SPEPRegistrationCache spepRegistrationCache = new SPEPRegistrationCacheImpl(spepRegistrationDao);

		AuthzPolicyCache authzPolicyCache = new AuthzPolicyCacheImpl();

		PolicyCacheProcessor policyCacheProcessor = new PolicyCacheProcessorImpl(authzCacheUpdateFailureRepository, authzPolicyCache, metadata, policyCacheDao, wsClient, keystoreResolver, identifierGenerator, samlValidator, POLICY_POLL_INTERVAL);

		Startup startup = new StartupImpl(samlValidator, identifierGenerator, spepRegistrationCache, metadata, keystoreResolver, policyCacheProcessor);

		SPEPProcessor spepProcessor = new SPEPProcessorImpl(metadata, startup, authzCacheUpdateFailureRepository, wsClient, identifierGenerator, samlValidator, keystoreResolver);

		AttributeAuthorityProcessor attributeAuthority = new AttributeAuthorityProcessorImpl(metadata, sessionsProcessor, samlValidator, identifierGenerator, keystoreResolver, ALLOWED_TIME_SKEW);

		AuthnProcessor authnProcessor = new AuthnProcessorImpl(spepProcessor, sessionsProcessor, authnHandlers);

		FailedLogoutRepository failedLogoutRepository = new FailedLogoutRepositoryImpl();

		LogoutAuthorityProcessor logoutAuthorityProcessor = new LogoutAuthorityProcessor(failedLogoutRepository, samlValidator, sessionsProcessor, metadata, identifierGenerator, keystoreResolver, wsClient);

		DelegatedAuthenticationProcessor delegatedAuthenticationProcessor = new DelegatedAuthenticationProcessorImpl(samlValidator, sessionsProcessor, identifierGenerator, keystoreResolver, deniedIdentifiers, delegatedAuthnIdentifier);

		SSOProcessor ssoProcessor = new AuthenticationAuthorityProcessor(samlValidator, sessionsProcessor, metadata, identifierGenerator, metadata, keystoreResolver, ALLOWED_TIME_SKEW, SESSION_REMAINING_TIME, false, identifierMap);

		AuthorizationProcessor authorizationProcessor = new AuthorizationProcessorImpl(authzPolicyCache, sessionsProcessor, metadata, samlValidator, identifierGenerator, keystoreResolver, AUTHORIZATION_DEFAULT_MODE, ALLOWED_TIME_SKEW);

		expect(webApplicationContext.getBean(eq(ConfigurationConstants.AUTHN_PROCESSOR), (Class) notNull())).andReturn(authnProcessor).anyTimes();
		expect(webApplicationContext.getBean(eq(ConfigurationConstants.AUTHN_PROCESSOR))).andReturn(authnProcessor).anyTimes();
		expect(webApplicationContext.getBean(eq(ConfigurationConstants.LOGOUT_AUTHORITY_PROCESSOR), (Class) notNull())).andReturn(logoutAuthorityProcessor).anyTimes();
		expect(webApplicationContext.getBean(eq(ConfigurationConstants.LOGOUT_AUTHORITY_PROCESSOR))).andReturn(logoutAuthorityProcessor).anyTimes();
		expect(webApplicationContext.getBean(eq(ConfigurationConstants.AUTHN_AUTHORITY_PROCESSOR), (Class) notNull())).andReturn(ssoProcessor).anyTimes();
		expect(webApplicationContext.getBean(eq(ConfigurationConstants.AUTHN_AUTHORITY_PROCESSOR))).andReturn(authnProcessor).anyTimes();

		replay(webApplicationContext);

		ssoAAServlet = new SSOAAServlet();
		ssoAAServlet.init(servletConfig);

		ssoLogoutServlet = new SSOLogoutServlet();
		ssoLogoutServlet.init(servletConfig);

		authnServlet = new AuthnServlet();
		authnServlet.init(servletConfig);

		wsProcessor = new WSProcessorImpl(attributeAuthority, authorizationProcessor, spepProcessor, delegatedAuthenticationProcessor);

		xmlInputFactory = XMLInputFactory.newInstance();
		this.up = true;
	}

	private List<PolicyCacheData> makePolicyCacheDataMap()
	{
		List<PolicyCacheData> map = new Vector<PolicyCacheData>();
		byte[] policyXML = new String("<?xml version=\"1.0\" encoding=\"UTF-16\"?><PolicySet xmlns=\"http://www.qut.com/middleware/lxacmlSchema\"><Description>A blah! policy set for the default SPEP deployment.</Description><Policy PolicyId=\"spep2:default\"><Description>Simple Policy</Description><Target><Resources><Resource><AttributeValue>/secure/.*</AttributeValue></Resource></Resources></Target><Rule Effect=\"Permit\" RuleId=\"rule1\"><Description>Description</Description><Condition><Apply FunctionId=\"or\"><Apply FunctionId=\"string-regex-match\"><SubjectAttributeDesignator AttributeId=\"uid\" /><AttributeValue>.*</AttributeValue></Apply><Apply FunctionId=\"string-regex-match\"><SubjectAttributeDesignator AttributeId=\"openIDprovider\" /><AttributeValue>.*</AttributeValue></Apply><Apply FunctionId=\"string-regex-match\"><SubjectAttributeDesignator AttributeId=\"shibidporigin\" /><AttributeValue>urn\\:mace\\:federation\\.org\\.au\\:testfed\\:level\\-1.*</AttributeValue></Apply></Apply></Condition></Rule><Rule Effect=\"Deny\" RuleId=\"rule2\"><Description>Description</Description><Target><Resources><Resource><AttributeValue>/secure/higherauth.jsp</AttributeValue></Resource></Resources></Target><Condition><Apply FunctionId=\"not\"><Apply FunctionId=\"string-equal\"><SubjectAttributeDesignator AttributeId=\"SomethingLevel\" /><AttributeValue>Level 2</AttributeValue></Apply></Apply></Condition></Rule><Rule Effect=\"Deny\" RuleId=\"rule3\"><Description>Description</Description><Target><Resources><Resource><AttributeValue>/secure/shibauth.jsp</AttributeValue></Resource></Resources></Target><Condition><Apply FunctionId=\"not\"><Apply FunctionId=\"string-regex-match\"><SubjectAttributeDesignator AttributeId=\"shibidporigin\" /><AttributeValue>urn\\:mace\\:federation\\.org\\.au\\:testfed\\:level\\-1\\:qut.*</AttributeValue></Apply></Apply></Condition></Rule></Policy></PolicySet>").getBytes();

		for (int i = 0; i < this.speps.size(); ++i)
		{
			SPEPInformation spep = this.speps.get(i);

			PolicyCacheData policyCacheData = new PolicyCacheData();
			policyCacheData.setSequenceId(new BigDecimal(System.currentTimeMillis()) );
			policyCacheData.setEntityID(spep.entityDesciptorID);
			policyCacheData.setLxacmlPolicy(policyXML);

			map.add(policyCacheData);
		}

		return map;
	}

	protected byte[] respondAuthzCacheClear(byte[] requestDocument, String endpoint) throws WSClientException
	{
		SPEPInformation targetSPEP = null;
		for (SPEPInformation spep : this.speps)
		{
			if (spep != null && endpoint.equals(spep.authzCacheClearEndpoint))
			{
				targetSPEP = spep;
				break;
			}
		}

		if (targetSPEP == null)
		{
			throw new WSClientException("Couldn't find the endpoint.");
		}

		try
		{
			ClearAuthzCacheRequest request = this.clearAuthzCacheRequestUnmarshaller.unMarshallUnSigned(requestDocument);

			byte[] responseDocument = null;
			ClearAuthzCacheResponse clearAuthzCacheResponse = null;

			NameIDType issuer = new NameIDType();
			issuer.setValue(targetSPEP.entityDesciptorID);

			Status status = new Status();
			StatusCode statusCode = new StatusCode();
			statusCode.setValue(StatusCodeConstants.success);
			status.setStatusCode(statusCode);
			status.setStatusMessage("Success!");

			clearAuthzCacheResponse = new ClearAuthzCacheResponse();
			clearAuthzCacheResponse.setInResponseTo(request.getID());
			clearAuthzCacheResponse.setID(this.localIdentifierGenerator.generateSAMLID());
			clearAuthzCacheResponse.setIssueInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));
			clearAuthzCacheResponse.setIssuer(issuer);
			clearAuthzCacheResponse.setSignature(new Signature());
			clearAuthzCacheResponse.setVersion(VersionConstants.saml20);
			clearAuthzCacheResponse.setStatus(status);

			responseDocument = targetSPEP.clearAuthzCacheResponseMarshaller.marshallSigned(clearAuthzCacheResponse);

			return responseDocument;
		}
		catch (UnmarshallerException e)
		{
			throw new WSClientException(e.getMessage());
		}
		catch (MarshallerException e)
		{
			throw new WSClientException(e.getMessage());
		}
	}

	protected byte[] respondSingleLogout(byte[] requestDocument, String endpoint) throws WSClientException
	{
		SPEPInformation targetSPEP = null;
		for (SPEPInformation spep : this.speps)
		{
			if (spep != null && endpoint.equals(spep.authzCacheClearEndpoint))
			{
				targetSPEP = spep;
				break;
			}
		}

		if (targetSPEP == null)
		{
			throw new WSClientException("Couldn't find the endpoint.");
		}

		try
		{
			LogoutRequest request = this.logoutRequestUnmarshaller.unMarshallUnSigned(requestDocument);

			NameIDType issuer = new NameIDType();
			issuer.setValue(targetSPEP.entityDesciptorID);

			Status status = new Status();
			StatusCode statusCode = new StatusCode();
			statusCode.setValue(StatusCodeConstants.success);
			status.setStatusCode(statusCode);
			status.setStatusMessage("Success");

			Response response = new Response();
			response.setID(this.localIdentifierGenerator.generateSAMLID());
			response.setInResponseTo(request.getID());
			response.setIssueInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));
			response.setIssuer(issuer);
			response.setSignature(new Signature());
			response.setStatus(status);
			response.setVersion(VersionConstants.saml20);

			return targetSPEP.logoutResponseMarshaller.marshallSigned(response);
		}
		catch (UnmarshallerException e)
		{
			throw new WSClientException(e.getMessage());
		}
		catch (MarshallerException e)
		{
			throw new WSClientException(e.getMessage());
		}
	}

	@After
	public void tearDown() throws Exception
	{
		if (up)
		{
			verify(userPassAuthenticator);
			verify(metadata);
			verify(keystoreResolver);
			verify(sessionConfigData);
			verify(spepRegistrationDao);
			verify(policyCacheDao);
			verify(servletConfig);
			verify(servletContext);
			verify(webApplicationContext);
		}
	}

	@Test
	public void testRun()
	{
		List<Thread> threadList = new Vector<Thread>();
		for (int i = 0; i < NUMBER_OF_THREADS; ++i)
		{
			// need a 'final' variable so that the anonymous class can use it
			final int threadNumber = i;

			Thread thread = new Thread()
			{
				@Override
				public void run()
				{
					for (int j = 0; j < CompleteESOETest.NUMBER_OF_RUNS; ++j)
					{
						doTest(CompleteESOETest.this.random.nextInt(CompleteESOETest.this.NUMBER_OF_SPEPS), threadNumber);
					}
				}
			};

			thread.start();

			threadList.add(thread);
		}

		for (Thread t : threadList)
		{
			while (t.isAlive())
			{
				try
				{
					Thread.sleep(100);
				}
				catch (InterruptedException e)
				{
				}
			}
		}

		for (Exception e : errors)
		{
			e.printStackTrace(System.err);
		}

		assertTrue(errors.size() == 0);
	}

	private void doTest(int spepIndex, int userIndex)
	{
		SPEPInformation spep = this.speps.get(spepIndex);
		UserInformation user = this.goodUserInformation.get(userIndex);
		String badUsername = this.badUsernamePasswordPairs.get(userIndex)[0];
		String badPassword = this.badUsernamePasswordPairs.get(userIndex)[1];

		List<String> resourceURLList = new Vector<String>();

		int badAttempts = 0;// this.random.nextInt(3);

		SessionData sessionData = new SessionData();

		sessionData.spep = spep;
		sessionData.user = user;
		sessionData.session = new TestHttpSession(this.localIdentifierGenerator);

		try
		{
			for (int i = 0; i < badAttempts; ++i)
			{
				badAuthnRequest(sessionData);
			}

			// synchronized( this )
			// {
			goodAuthnRequest(sessionData);
			// }

			for (int i = 0; i < badAttempts; ++i)
			{
				badLogin(sessionData, badUsername, badPassword);
			}

			goodLogin(sessionData);

			for (int i = 0; i < badAttempts; ++i)
			{
				badSSO(sessionData);
			}

			goodSSO(sessionData);

			pdp(sessionData);
		}
		catch (Exception e)
		{
			errors.add(e);
		}
	}

	private static final String DYNAMIC_RESPONSE_URL_SESSION_NAME = "com.qut.middleware.esoe.authn.servlet.dynamicresponseurl";
	private static final String FORM_USER_IDENTIFIER = "esoeauthn_user";
	private static final String FORM_PASSWORD_IDENTIFIER = "esoeauthn_pw";
	private static final String FORM_REDIRECT_URL = "redirectURL";
	private static final String SAML_REQUEST_FORM_ELEMENT = "SAMLRequest";
	private static final String SAML_RESPONSE_FORM_ELEMENT = "SAMLResponse";

	private static final Pattern pattern = Pattern.compile(".*name=\"" + SAML_RESPONSE_FORM_ELEMENT + "\"\\s+value=\"([^\"]+)\".*", Pattern.DOTALL);
	private WSProcessor wsProcessor;

	private class SessionData
	{
		public SPEPInformation spep;
		public UserInformation user;
		public SSOProcessorData ssoProcessorData;
		public AuthnProcessorData authnProcessorData;
		public Map<String, Cookie> cookies = new HashMap<String, Cookie>();
		public String esoeSessionID;
		public String esoeSessionIndex;

		public TestHttpSession session;

		public SessionData()
		{
		}
	}

	private void goodAuthnRequest(SessionData sessionData) throws Exception
	{
		this.logger.error("goodAuthnRequest");

		long startTime;
		boolean first = true;

		StringWriter out;
		HttpServletRequest request;
		HttpServletResponse response;
		Capture<Cookie> cookieCapture;
		do
		{
			if (!first)
				this.logger.error("retrying pdp - timed out - thread " + Thread.currentThread().getId());
			first = false;

			startTime = System.currentTimeMillis();

			String authnRequestSAMLID = localIdentifierGenerator.generateSAMLID();

			AuthnRequest authnRequest = new AuthnRequest();
			authnRequest.setIssueInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));
			NameIDPolicy nameIDPolicy = new NameIDPolicy();
			nameIDPolicy.setFormat(NameIDFormatConstants.trans);
			nameIDPolicy.setAllowCreate(Boolean.TRUE);
			authnRequest.setNameIDPolicy(nameIDPolicy);
			authnRequest.setForceAuthn(Boolean.FALSE);
			authnRequest.setIsPassive(Boolean.FALSE);
			authnRequest.setVersion(VersionConstants.saml20);
			authnRequest.setSignature(new Signature());
			authnRequest.setID(authnRequestSAMLID);
			authnRequest.setAssertionConsumerServiceIndex(0);
			authnRequest.setAttributeConsumingServiceIndex(0);

			NameIDType issuer = new NameIDType();
			issuer.setValue(sessionData.spep.entityDesciptorID);
			authnRequest.setIssuer(issuer);

			byte[] requestDocument = sessionData.spep.authnRequestMarshaller.marshallSigned(authnRequest);

			request = createMock(HttpServletRequest.class);
			response = createMock(HttpServletResponse.class);

			sessionData.session = new TestHttpSession(sessionData.session, null);

			expect(request.getSession()).andReturn(sessionData.session).anyTimes();
			expect(request.getCookies()).andReturn(new Cookie[] {}).anyTimes();
			expect(request.getMethod()).andReturn("POST").anyTimes();
			expect(request.getParameter(SAML_REQUEST_FORM_ELEMENT)).andReturn(new String(Base64.encodeBase64(requestDocument))).anyTimes();

			cookieCapture = new Capture<Cookie>();
			response.addCookie(capture(cookieCapture));
			expectLastCall().anyTimes();

			response.sendRedirect(AUTHN_REDIRECT_URL + "?" + AUTHN_DYNAMIC_URL_PARAM + "=" + new String(Base64.encodeBase64(SSO_URL.getBytes())));
			expectLastCall().once();

			out = new StringWriter();
			expect(response.getWriter()).andReturn(new PrintWriter(out)).anyTimes();

			replay(request);
			replay(response);

			ssoAAServlet.service(request, response);
		} while ((System.currentTimeMillis() - startTime) / 1000 > ALLOWED_TIME_SKEW);

		if (out.toString().length() > 0)
		{
			throw new Exception("Unexpected content: \n" + getResponeSAMLDocument(out.toString()));
		}

		verify(request);
		verify(response);

		for (Cookie cookie : cookieCapture.getCaptured())
		{
			sessionData.cookies.put(cookie.getName(), cookie);
		}
	}

	private void badAuthnRequest( SessionData sessionData ) throws Exception
	{
		
		this.logger.error("badAuthnRequest");
		
		AuthnRequest authnRequest = new AuthnRequest();
		authnRequest.setAssertionConsumerServiceIndex(0);
		authnRequest.setID( this.localIdentifierGenerator.generateSAMLID() );
		authnRequest.setIsPassive( Boolean.FALSE );
		authnRequest.setIssueInstant( new XMLGregorianCalendarImpl( new GregorianCalendar() ) );
		authnRequest.setVersion( "invalid" );
		
		byte[] requestDocument = sessionData.spep.authnRequestMarshaller.marshallUnSigned( authnRequest );

		HttpServletRequest request = createMock( HttpServletRequest.class );
		HttpServletResponse response = createMock( HttpServletResponse.class );
		sessionData.session = new TestHttpSession( sessionData.session, null );
		
		expect( request.getSession() ).andReturn( sessionData.session ).anyTimes();
		expect( request.getCookies() ).andReturn( new Cookie[]{} ).anyTimes();
		expect( request.getMethod() ).andReturn( "POST" ).anyTimes();
		expect( request.getParameter(SAML_REQUEST_FORM_ELEMENT) ).andReturn( new String(Base64.encodeBase64( requestDocument ))).anyTimes();
		
		Capture<Cookie> cookieCapture = new Capture<Cookie>();
		response.addCookie( capture(cookieCapture) );
		expectLastCall().anyTimes();
		
		response.sendError( eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), (String) anyObject() );
		expectLastCall().once();
		
		replay( request );
		replay( response );
		
		ssoAAServlet.service( request, response );
		
		verify( request );
		verify( response );
		
		for( Cookie cookie : cookieCapture.getCaptured() )
		{
			sessionData.cookies.put( cookie.getName(), cookie );
		}
	}

	private void goodLogin(SessionData sessionData) throws Exception
	{
		this.logger.error("goodLogin");

		HttpServletRequest request = createMock(HttpServletRequest.class);
		HttpServletResponse response = createMock(HttpServletResponse.class);
		sessionData.session = new TestHttpSession(sessionData.session, null);

		String redirectURL = "https://esoe.example.com/sso";
		String base64RedirectURL = new String(Base64.encodeBase64(redirectURL.getBytes()));

		expect(request.getSession()).andReturn(sessionData.session).anyTimes();
		expect(request.getCookies()).andReturn(new Cookie[] {}).anyTimes();
		expect(request.getMethod()).andReturn("POST").anyTimes();
		expect(request.getParameter(FORM_USER_IDENTIFIER)).andReturn(sessionData.user.username).anyTimes();
		expect(request.getParameter(FORM_PASSWORD_IDENTIFIER)).andReturn(sessionData.user.password).anyTimes();
		expect(request.getParameter(FORM_REDIRECT_URL)).andReturn(base64RedirectURL).anyTimes();

		Capture<Cookie> cookieCapture = new Capture<Cookie>();
		response.addCookie(capture(cookieCapture));
		expectLastCall().anyTimes();

		try
		{
			response.sendRedirect(redirectURL);
			expectLastCall().once();
		}
		catch (IOException e)
		{
		}

		Capture<AuthnProcessorData> captureAuthnProcessorData = new Capture<AuthnProcessorData>();

		sessionData.authnProcessorData = new AuthnProcessorDataImpl();

		replay(request);
		replay(response);

		authnServlet.service(request, response);

		verify(request);
		verify(response);

		for (Cookie cookie : cookieCapture.getCaptured())
		{
			sessionData.cookies.put(cookie.getName(), cookie);
		}
	}

	private void badLogin(SessionData sessionData, String username, String password) throws Exception
	{
		this.logger.error("badLogin");

		HttpServletRequest request = createMock(HttpServletRequest.class);
		HttpServletResponse response = createMock(HttpServletResponse.class);
		sessionData.session = new TestHttpSession(sessionData.session, null);

		String redirectURL = "https://esoe.example.com/sso";
		String base64RedirectURL = new String(Base64.encodeBase64(redirectURL.getBytes()));

		expect(request.getSession()).andReturn(sessionData.session).anyTimes();
		expect(request.getCookies()).andReturn(new Cookie[] {}).anyTimes();
		expect(request.getMethod()).andReturn("POST").anyTimes();
		expect(request.getParameter(FORM_USER_IDENTIFIER)).andReturn(username).anyTimes();
		expect(request.getParameter(FORM_PASSWORD_IDENTIFIER)).andReturn(password).anyTimes();
		expect(request.getParameter(FORM_REDIRECT_URL)).andReturn(base64RedirectURL).anyTimes();

		response.sendRedirect(REQUIRE_CREDENTIALS_URL + "?" + AUTHENTICATION_FAILED_NAME_VALUE);
		expectLastCall().once();

		replay(request);
		replay(response);

		authnServlet.service(request, response);

		verify(request);
		verify(response);
	}

	public void goodSSO(SessionData sessionData) throws Exception
	{
		this.logger.error("goodSSO");

		HttpServletRequest request = createMock(HttpServletRequest.class);
		HttpServletResponse response = createMock(HttpServletResponse.class);
		sessionData.session = new TestHttpSession(sessionData.session, null);

		Collection<Cookie> cookieValues = sessionData.cookies.values();
		Cookie[] cookies = new Cookie[cookieValues.size()];
		int i = 0;
		for (Cookie c : cookieValues)
		{
			cookies[i++] = c;
		}

		expect(request.getSession()).andReturn(sessionData.session).anyTimes();
		expect(request.getCookies()).andReturn(cookies).anyTimes();
		expect(request.getMethod()).andReturn("GET").anyTimes();
		expect(request.getServerName()).andReturn(ESOE_SERVER_NAME).anyTimes();

		StringWriter out = new StringWriter();
		expect(response.getWriter()).andReturn(new PrintWriter(out)).atLeastOnce();

		response.addCookie((Cookie) anyObject());
		expectLastCall().anyTimes();

		replay(request);
		replay(response);

		ssoAAServlet.service(request, response);

		verify(request);
		verify(response);

		Response samlResponse = this.authnResponseUnmarshaller.unMarshallSigned(getResponeSAMLDocument(out.toString()));

		processSAMLAuthnResponse(sessionData, samlResponse);
	}

	private byte[] getResponeSAMLDocument(String htmlDocument) throws Exception
	{
		Matcher matcher = pattern.matcher(htmlDocument);
		if (matcher.matches() && matcher.groupCount() > 0)
		{
			String base64ResponseDocument = matcher.group(1);

			return Base64.decodeBase64(base64ResponseDocument.getBytes());
		}
		else
		{
			throw new Exception("No response document");
		}
	}

	private void processSAMLAuthnResponse(SessionData sessionData, Response samlResponse) throws Exception
	{

		Assertion assertion = null;
		AuthnStatement authnStatement = null;

		for (Object maybeAssertion : samlResponse.getEncryptedAssertionsAndAssertions())
		{
			if (!(maybeAssertion instanceof Assertion))
			{
				continue;
			}

			assertion = (Assertion) maybeAssertion;

			for (Object maybeAuthnStatement : assertion.getAuthnStatementsAndAuthzDecisionStatementsAndAttributeStatements())
			{
				if (!(maybeAuthnStatement instanceof AuthnStatement))
				{
					continue;
				}

				authnStatement = (AuthnStatement) maybeAuthnStatement;
				break;
			}

			if (authnStatement != null)
				break;
		}

		if (authnStatement == null)
			throw new Exception("No AuthnStatement");

		sessionData.esoeSessionID = assertion.getSubject().getNameID().getValue();
		sessionData.esoeSessionIndex = authnStatement.getSessionIndex();
	}

	public void badSSO(SessionData sessionData) throws Exception
	{
		this.logger.error("badSSO");

		HttpServletRequest request = createMock(HttpServletRequest.class);
		HttpServletResponse response = createMock(HttpServletResponse.class);
		sessionData.session = new TestHttpSession(sessionData.session, null);

		expect(request.getSession()).andReturn(sessionData.session).anyTimes();
		expect(request.getCookies()).andReturn(new Cookie[] {}).anyTimes();
		expect(request.getMethod()).andReturn("GET").anyTimes();

		response.sendRedirect(AUTHN_REDIRECT_URL + "?" + AUTHN_DYNAMIC_URL_PARAM + "=" + new String(Base64.encodeBase64(SSO_URL.getBytes())));
		expectLastCall().once();

		response.addCookie((Cookie) anyObject());
		expectLastCall().anyTimes();

		replay(request);
		replay(response);

		ssoAAServlet.service(request, response);

		verify(request);
		verify(response);
	}

	public void pdp(SessionData sessionData) throws Exception
	{

		this.logger.error("pdp");

		for (Map.Entry<String, Boolean> entry : sessionData.user.expectedAuthzBehaviour.entrySet())
		{
			boolean first = true;

			byte[] responseDocument;
			long startTime;
			do
			{
				if (!first)
					this.logger.error("retrying pdp - timed out - thread " + Thread.currentThread().getId());
				first = false;

				startTime = System.currentTimeMillis();

				// The resource being accessed by the client
				Resource resource = new Resource();
				Attribute resourceAttribute = new Attribute();
				AttributeValue resourceAttributeValue = new AttributeValue();
				resourceAttributeValue.getContent().add(entry.getKey());
				resourceAttribute.setAttributeValue(resourceAttributeValue);
				resource.setAttribute(resourceAttribute);
				// Set the subject of the query..
				Subject subject = new Subject();
				Attribute subjectAttribute = new Attribute();
				AttributeValue subjectAttributeValue = new AttributeValue();
				subjectAttributeValue.getContent().add(sessionData.esoeSessionID); // .. to the session
				subjectAttribute.setAttributeValue(subjectAttributeValue);
				subject.setAttribute(subjectAttribute);
				Request request = new Request();
				request.setResource(resource);
				request.setSubject(subject);
				// SPEP <Issuer> tag
				NameIDType issuer = new NameIDType();
				issuer.setValue(sessionData.spep.entityDesciptorID);
				// The actual authz query.
				LXACMLAuthzDecisionQuery lxacmlAuthzDecisionQuery = new LXACMLAuthzDecisionQuery();
				lxacmlAuthzDecisionQuery.setRequest(request);
				lxacmlAuthzDecisionQuery.setID(this.localIdentifierGenerator.generateSAMLID());
				lxacmlAuthzDecisionQuery.setIssueInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));
				lxacmlAuthzDecisionQuery.setVersion(VersionConstants.saml20);
				lxacmlAuthzDecisionQuery.setIssuer(issuer);
				lxacmlAuthzDecisionQuery.setSignature(new Signature());
				byte[] requestDocument = sessionData.spep.lxacmlAuthzDecisionQueryMarshaller.marshallSigned(lxacmlAuthzDecisionQuery);

				ByteArrayOutputStream writer;
				OMElement requestElement, resultElement;
				ByteArrayInputStream reader;
				reader = new ByteArrayInputStream(requestDocument);
				synchronized (this)
				{
					XMLStreamReader xmlreader;
					StAXOMBuilder builder;
					xmlreader = xmlInputFactory.createXMLStreamReader(reader);
					builder = new StAXOMBuilder(xmlreader);
					requestElement = builder.getDocumentElement();
				}
				resultElement = wsProcessor.policyDecisionPoint(requestElement);
				writer = new ByteArrayOutputStream();
				if (resultElement != null)
				{
					synchronized (this)
					{
						resultElement.serialize(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));
					}
				}
				else
				{
					throw new Exception("No web service response to PDP query");
				}

				responseDocument = writer.toByteArray();
				
			} while ((System.currentTimeMillis() - startTime) / 1000 > ALLOWED_TIME_SKEW);

			Response response = this.lxacmlAuthzDecisionResponseUnmarshaller.unMarshallUnSigned(responseDocument);
			Assertion assertion = null;
			LXACMLAuthzDecisionStatement lxacmlAuthzDecisionStatement = null;
			for (Object maybeAssertion : response.getEncryptedAssertionsAndAssertions())
			{

				if (!(maybeAssertion instanceof Assertion))
				{
					continue;
				}

				assertion = (Assertion) maybeAssertion;

				for (StatementAbstractType statement : assertion.getAuthnStatementsAndAuthzDecisionStatementsAndAttributeStatements())
				{

					if (!(statement instanceof LXACMLAuthzDecisionStatement))
					{
						continue;
					}

					lxacmlAuthzDecisionStatement = (LXACMLAuthzDecisionStatement) statement;
					break;

				}

				if (lxacmlAuthzDecisionStatement != null)
					break;

			}

			if (lxacmlAuthzDecisionStatement == null)
				throw new Exception("No LXACML Authz Decision Statement from PDP query");

			Boolean value = lxacmlAuthzDecisionStatement.getResponse().getResult().getDecision().equals(DecisionType.PERMIT);
			if (value.booleanValue() != entry.getValue())
			{
				throw new Exception("Wrong decision for " + sessionData.user.username + " " + entry.getKey() + " expected " + entry.getValue());
			}
		}

	}

}

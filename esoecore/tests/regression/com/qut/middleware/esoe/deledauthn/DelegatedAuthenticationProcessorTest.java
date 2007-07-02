package com.qut.middleware.esoe.deledauthn;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.security.PrivateKey;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;
import org.w3._2000._09.xmldsig_.Signature;

import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.crypto.KeyStoreResolver;
import com.qut.middleware.esoe.crypto.impl.KeyStoreResolverImpl;
import com.qut.middleware.esoe.delegauthn.DelegatedAuthenticationProcessor;
import com.qut.middleware.esoe.delegauthn.bean.DelegatedAuthenticationData;
import com.qut.middleware.esoe.delegauthn.bean.impl.DelegatedAuthenticationDataImpl;
import com.qut.middleware.esoe.delegauthn.impl.DelegatedAuthenticationProcessorImpl;
import com.qut.middleware.esoe.sessions.Create;
import com.qut.middleware.esoe.sessions.SessionsProcessor;
import com.qut.middleware.esoe.sessions.exception.DataSourceException;
import com.qut.middleware.esoe.sessions.exception.DuplicateSessionException;
import com.qut.middleware.saml2.StatusCodeConstants;
import com.qut.middleware.saml2.VersionConstants;
import com.qut.middleware.saml2.exception.MarshallerException;
import com.qut.middleware.saml2.handler.Marshaller;
import com.qut.middleware.saml2.handler.Unmarshaller;
import com.qut.middleware.saml2.handler.impl.MarshallerImpl;
import com.qut.middleware.saml2.handler.impl.UnmarshallerImpl;
import com.qut.middleware.saml2.identifier.IdentifierCache;
import com.qut.middleware.saml2.identifier.IdentifierGenerator;
import com.qut.middleware.saml2.identifier.impl.IdentifierCacheImpl;
import com.qut.middleware.saml2.identifier.impl.IdentifierGeneratorImpl;
import com.qut.middleware.saml2.schemas.assertion.AttributeType;
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.esoe.delegated.RegisterPrincipalRequest;
import com.qut.middleware.saml2.schemas.esoe.delegated.RegisterPrincipalResponse;
import com.qut.middleware.saml2.validator.SAMLValidator;
import com.qut.middleware.saml2.validator.impl.SAMLValidatorImpl;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

@SuppressWarnings("nls")
public class DelegatedAuthenticationProcessorTest
{
	private DelegatedAuthenticationProcessor processor;
	private SAMLValidator samlValidator;
	private SessionsProcessor sessionsProcessor;
	private IdentifierGenerator identifierGenerator;
	private KeyStoreResolver keyStoreResolver;
	private List<String> deniedIdentifiers;
	private String delegatedAuthnIdentifier;
	
	// mocked internal objects
	private Create create;
	
	private PrivateKey privKey;
	private IdentifierCache identifierCache;
	private String issuerID = "SAML84237880932ID";
	
	Unmarshaller<RegisterPrincipalResponse> responseUnmarshaller;
	Marshaller<RegisterPrincipalRequest> requestMarshaller;

	@Before
	public void setUp() throws Exception
	{		
		// create mocks (NOTE: you must call replay(MockedClass) in the calling function)
		this.sessionsProcessor= createMock(SessionsProcessor.class);
		this.create = createMock(Create.class);
		
		int skew = 2000; //(Integer.MAX_VALUE / 1000 -1);
		this.identifierCache = new IdentifierCacheImpl();
		this.identifierGenerator = new IdentifierGeneratorImpl(new IdentifierCacheImpl());
			
		String keyStorePath = "tests/testdata/testskeystore.ks";
		String keyStorePassword = "Es0EKs54P4SSPK";
		String esoeKeyAlias = "esoeprimary";
		String esoeKeyPassword = "Es0EKs54P4SSPK";
		
		this.keyStoreResolver = new KeyStoreResolverImpl(new File(keyStorePath), keyStorePassword, esoeKeyAlias, esoeKeyPassword);
		
		this.privKey = this.keyStoreResolver.getPrivateKey();
		String keyName = this.keyStoreResolver.getKeyAlias();
		
		this.samlValidator = new SAMLValidatorImpl(this.identifierCache, skew);		
			
		String[] schemas = new String[]  {ConfigurationConstants.delegatedAuthn};
		this.responseUnmarshaller = new UnmarshallerImpl<RegisterPrincipalResponse>(RegisterPrincipalResponse.class.getPackage().getName(), schemas, keyStoreResolver);
		this.requestMarshaller = new MarshallerImpl<RegisterPrincipalRequest>(RegisterPrincipalRequest.class.getPackage().getName(), schemas, keyName, this.privKey);
		
		// add a denied attribute. The delagator should never add this to a user session.
		this.deniedIdentifiers = new Vector<String>();
		this.deniedIdentifiers.add("password");
		
		this.delegatedAuthnIdentifier = "testAuthnID";
		
		this.processor = new DelegatedAuthenticationProcessorImpl(this.samlValidator, this.sessionsProcessor, this.identifierGenerator, this.keyStoreResolver, this.deniedIdentifiers, this.delegatedAuthnIdentifier);
	}



	/** Test a valid request.
	 * 
	 */
	@Test 
	public void testExecute1()
	{		
		expect(this.sessionsProcessor.getCreate()).andReturn(this.create).anyTimes();
			
		try
		{
			expect(this.create.createDelegatedSession((String)notNull(), (String)notNull(), (String)notNull(), (List)notNull())).andReturn(Create.result.SessionCreated).once();
		}
		catch(Exception e)
		{
			fail("Mocked invocation failed unexpectedly");
		}
	
		replay(this.sessionsProcessor);
		replay(this.create);
	
		DelegatedAuthenticationData processorData = new DelegatedAuthenticationDataImpl();
		
		processorData.setRequestDocument(this.generateRegisterPrincipalRequest());
		
		this.processor.execute(processorData);
	
	}
	
	
	/** Test a valid request, and confirm returned data.
	 * 
	 */
	@Test 
	public void testExecute2()
	{		
		expect(this.sessionsProcessor.getCreate()).andReturn(this.create).anyTimes();
			
		try
		{
			expect(this.create.createDelegatedSession((String)notNull(), (String)notNull(), (String)notNull(), (List)notNull())).andReturn(Create.result.SessionCreated).once();
		}
		catch(Exception e)
		{
			fail("Mocked invocation failed unexpectedly");
		}
	
		replay(this.sessionsProcessor);
		replay(this.create);
	
		DelegatedAuthenticationData processorData = new DelegatedAuthenticationDataImpl();
		
		processorData.setRequestDocument(this.generateRegisterPrincipalRequest());
				
		this.processor.execute(processorData);
	
		assertNotNull(processorData);
		
		assertNotNull(processorData.getResponseDocument());
		
		// validate the returned document
		try
		{
			RegisterPrincipalResponse response = this.responseUnmarshaller.unMarshallSigned(processorData.getResponseDocument());
		
			this.samlValidator.getResponseValidator().validate(response);
			
			// check that response status is OK
			assertEquals("Returned status code incorrect", StatusCodeConstants.success,  response.getStatus().getStatusCode().getValue());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	/** Test a valid request that fails authentication mechanisms and confirm returned data. This case
	 * tests failure due to a DuplicateSessionException.
	 * 
	 */
	@Test 
	public void testExecute3()
	{		
		expect(this.sessionsProcessor.getCreate()).andReturn(this.create).anyTimes();
			
		try
		{
			expect(this.create.createDelegatedSession((String)notNull(), (String)notNull(), (String)notNull(), (List)notNull())).andThrow(new DuplicateSessionException());
		}
		catch(Exception e)
		{
			// continue on and process data
		}
	
		replay(this.sessionsProcessor);
		replay(this.create);
	
		DelegatedAuthenticationData processorData = new DelegatedAuthenticationDataImpl();
		
		processorData.setRequestDocument(this.generateRegisterPrincipalRequest());
				
		this.processor.execute(processorData);
	
		assertNotNull(processorData);
		
		assertNotNull(processorData.getResponseDocument());
		
		// validate the returned document
		try
		{
			RegisterPrincipalResponse response = this.responseUnmarshaller.unMarshallSigned(processorData.getResponseDocument());
		
			this.samlValidator.getResponseValidator().validate(response);
			
			// check that response status is OK
			assertEquals("Returned status code incorrect", StatusCodeConstants.authnFailed,  response.getStatus().getStatusCode().getValue());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	/** Test a valid request that fails authentication mechanisms and confirm returned data. This case
	 * tests failure due to a DataSourceException.
	 * 
	 */
	@Test 
	public void testExecute4()
	{		
		expect(this.sessionsProcessor.getCreate()).andReturn(this.create).anyTimes();
			
		try
		{
			expect(this.create.createDelegatedSession((String)notNull(), (String)notNull(), (String)notNull(), (List)notNull())).andThrow(new DataSourceException());
		}
		catch(Exception e)
		{
			// continue on and process data
		}
	
		replay(this.sessionsProcessor);
		replay(this.create);
	
		DelegatedAuthenticationData processorData = new DelegatedAuthenticationDataImpl();
		
		processorData.setRequestDocument(this.generateRegisterPrincipalRequest());
				
		this.processor.execute(processorData);
	
		assertNotNull(processorData);
		
		assertNotNull(processorData.getResponseDocument());
		
		// validate the returned document
		try
		{
			RegisterPrincipalResponse response = this.responseUnmarshaller.unMarshallSigned(processorData.getResponseDocument());
		
			this.samlValidator.getResponseValidator().validate(response);
			
			// check that response status is OK
			assertEquals("Returned status code incorrect", StatusCodeConstants.authnFailed,  response.getStatus().getStatusCode().getValue());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/** Test sending an invalid request. No request document supplied. The validator will throw
	 * the expected exception.
	 * 
	 *
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testExecute6()
	{
		DelegatedAuthenticationData processorData = new DelegatedAuthenticationDataImpl();
				
		this.processor.execute(processorData);
	
	}

	
	/** Test sending an invalid request. Invalid request document supplied. The validator will throw
	 * the expected exception.
	 * 
	 *
	 */
	@Test 
	public void testExecute7()
	{
		DelegatedAuthenticationData processorData = new DelegatedAuthenticationDataImpl();
		processorData.setRequestDocument(this.generateRegisterPrincipalRequest() + "<woopsie>");
		
		this.processor.execute(processorData);
		//	validate the returned document
		try
		{
			RegisterPrincipalResponse response = this.responseUnmarshaller.unMarshallSigned(processorData.getResponseDocument());
		
			this.samlValidator.getResponseValidator().validate(response);
			
			// check that response status is OK
			assertEquals("Returned status code incorrect", StatusCodeConstants.authnFailed,  response.getStatus().getStatusCode().getValue());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/** Test block coverage of constructor
	 * 
	 *
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testDelegatedAuthenticationProcessorImpl1() throws Exception
	{
		this.processor = new DelegatedAuthenticationProcessorImpl(null, this.sessionsProcessor, this.identifierGenerator, this.keyStoreResolver, this.deniedIdentifiers, this.delegatedAuthnIdentifier);
		
	}
	
	
	/** Test block coverage of constructor
	 * 
	 *
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testDelegatedAuthenticationProcessorImpl2() throws Exception
	{
		this.processor = new DelegatedAuthenticationProcessorImpl(this.samlValidator, null, this.identifierGenerator, this.keyStoreResolver, this.deniedIdentifiers, this.delegatedAuthnIdentifier);
		
	}
	
	/** Test block coverage of constructor
	 * 
	 *
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testDelegatedAuthenticationProcessorImpl3() throws Exception
	{
		this.processor = new DelegatedAuthenticationProcessorImpl(this.samlValidator, this.sessionsProcessor, null, this.keyStoreResolver, this.deniedIdentifiers, this.delegatedAuthnIdentifier);
		
	}
	
	/** Test block coverage of constructor
	 * 
	 *
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testDelegatedAuthenticationProcessorImpl4() throws Exception
	{
		this.processor = new DelegatedAuthenticationProcessorImpl(this.samlValidator, this.sessionsProcessor, this.identifierGenerator, null, this.deniedIdentifiers, this.delegatedAuthnIdentifier);
	}
	
	/** Test block coverage of constructor
	 * 
	 *
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testDelegatedAuthenticationProcessorImpl5() throws Exception
	{
		this.processor = new DelegatedAuthenticationProcessorImpl(this.samlValidator, this.sessionsProcessor, this.identifierGenerator, this.keyStoreResolver, null, this.delegatedAuthnIdentifier);
		
	}
	
	/** Test block coverage of constructor
	 * 
	 *
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testDelegatedAuthenticationProcessorImpl6() throws Exception
	{
		this.processor = new DelegatedAuthenticationProcessorImpl(this.samlValidator, this.sessionsProcessor, this.identifierGenerator, this.keyStoreResolver, this.deniedIdentifiers, null);
		
	}
		
	
	/** Creates a valid RegisterPrincipalRequest XML string.
	 * 
	 */
	private String generateRegisterPrincipalRequest()
	{
		List<AttributeType> attributes = new Vector<AttributeType>();
		AttributeType attr1 = new AttributeType();
		attr1.setName("email");
		
		// this attribute is in banned list, should not be added
		AttributeType attr2 = new AttributeType();
		attr2.setName("password");
		
		attributes.add(attr1);
		attributes.add(attr2);
		
		RegisterPrincipalRequest request = new RegisterPrincipalRequest();
		String document = null;
		
		NameIDType issuer = new NameIDType();
		issuer.setValue(this.issuerID);
		request.setIssuer(issuer);
		request.setVersion(VersionConstants.saml20);
		request.getAttributesAndEncryptedAttributes().addAll(attributes);
		request.setID(this.identifierGenerator.generateSAMLID());
		request.setSource(this.delegatedAuthnIdentifier);
		request.setPrincipalAuthnIdentifier("unknown");
		request.setIssueInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));
		request.setSignature(new Signature());
		
		try
		{
			document = this.requestMarshaller.marshallSigned(request);
		}
		catch (MarshallerException e)
		{
			e.printStackTrace();
			fail("Error occured marshalling RegisterPrincipalRequest");
		}
		
		return document;
	}
	
}

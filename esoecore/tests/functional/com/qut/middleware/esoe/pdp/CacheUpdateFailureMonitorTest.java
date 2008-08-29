package com.qut.middleware.esoe.pdp;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.security.PublicKey;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3._2000._09.xmldsig_.Signature;

import com.qut.middleware.crypto.KeystoreResolver;
import com.qut.middleware.crypto.impl.KeystoreResolverImpl;
import com.qut.middleware.esoe.authz.cache.AuthzCacheUpdateFailureRepository;
import com.qut.middleware.esoe.authz.cache.bean.FailedAuthzCacheUpdate;
import com.qut.middleware.esoe.authz.cache.bean.impl.FailedAuthzCacheUpdateImpl;
import com.qut.middleware.esoe.authz.cache.impl.AuthzCacheUpdateFailureRepositoryImpl;
import com.qut.middleware.esoe.authz.cache.impl.CacheUpdateFailureMonitor;
import com.qut.middleware.esoe.ws.WSClient;
import com.qut.middleware.esoe.ws.exception.WSClientException;
import com.qut.middleware.metadata.processor.MetadataProcessor;
import com.qut.middleware.saml2.SchemaConstants;
import com.qut.middleware.saml2.StatusCodeConstants;
import com.qut.middleware.saml2.VersionConstants;
import com.qut.middleware.saml2.exception.MarshallerException;
import com.qut.middleware.saml2.handler.Marshaller;
import com.qut.middleware.saml2.handler.impl.MarshallerImpl;
import com.qut.middleware.saml2.identifier.impl.IdentifierCacheImpl;
import com.qut.middleware.saml2.identifier.impl.IdentifierGeneratorImpl;
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.esoe.lxacml.protocol.LXACMLAuthzDecisionQuery;
import com.qut.middleware.saml2.schemas.esoe.protocol.ClearAuthzCacheRequest;
import com.qut.middleware.saml2.schemas.esoe.protocol.ClearAuthzCacheResponse;
import com.qut.middleware.saml2.schemas.protocol.RequestAbstractType;
import com.qut.middleware.saml2.schemas.protocol.Status;
import com.qut.middleware.saml2.schemas.protocol.StatusCode;
import com.qut.middleware.saml2.schemas.protocol.StatusResponseType;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;


@SuppressWarnings({"nls", "unqualified-field-access"})
/* NOTE some of these tests spit out errors owing to the fact that the mocked clear cache response 
 * is invalid. TODO unmarshall a valid response
 * 
 */
public class CacheUpdateFailureMonitorTest
{
	private CacheUpdateFailureMonitor testMonitor;
	private AuthzCacheUpdateFailureRepository failures;
	private WSClient webServiceClient;
	private KeystoreResolver keyStoreResolver;
	private MetadataProcessor metadata ;
	
	// use this as SAML issuer ID in requests
	private String issuerIDRequest1 = "_948756943y897fudghs99";
	private String issuerIDRequest2 = "request2483728f8sf";
		
	private MarshallerImpl<ClearAuthzCacheRequest> clearAuthzCacheRequestMarshaller;
	private Marshaller<ClearAuthzCacheResponse>  clearAuthzCacheResponseMarshaller;
	private  PublicKey publicKey;
	
	private int retryInterval = 2;
	private int maxAge = 4;
	
	@Before
	public void setUp() throws Exception
	{
		String keyStorePath = "tests" + File.separator + "testdata" + File.separator + "testskeystore.ks";
		String keyStorePassword = "Es0EKs54P4SSPK";
		String esoeKeyAlias = "esoeprimary";
		String esoeKeyPassword = "Es0EKs54P4SSPK";
	
		this.keyStoreResolver = new KeystoreResolverImpl(new File(keyStorePath), keyStorePassword, esoeKeyAlias, esoeKeyPassword);
		publicKey = keyStoreResolver.getLocalPublicKey();
		
		this.metadata = createMock(MetadataProcessor.class);
		expect(metadata.resolveKey((String)notNull())).andReturn(this.publicKey).anyTimes();
		replay(metadata);
		
		this.webServiceClient = createMock(WSClient.class);
		
		// setup Request marshaller
		String cacheClearPackages = ClearAuthzCacheRequest.class.getPackage().getName() + ":" +
		StatusResponseType.class.getPackage().getName() + ":" + RequestAbstractType.class.getPackage().getName();
		String[] cacheClearSchemas = new String[]{SchemaConstants.samlProtocol, SchemaConstants.samlAssertion, SchemaConstants.esoeProtocol};
		this.clearAuthzCacheRequestMarshaller = new MarshallerImpl<ClearAuthzCacheRequest>(cacheClearPackages, cacheClearSchemas, keyStoreResolver);
		
			// setu response marshaller
		String[] clearAuthzCacheSchemas = new String[] { SchemaConstants.esoeProtocol,
				SchemaConstants.samlAssertion, SchemaConstants.samlProtocol };
		String MAR_PKGNAMES = LXACMLAuthzDecisionQuery.class.getPackage().getName() + ":" + ClearAuthzCacheResponse.class.getPackage().getName();
		this.clearAuthzCacheResponseMarshaller =   new MarshallerImpl<ClearAuthzCacheResponse>(MAR_PKGNAMES,
		clearAuthzCacheSchemas, keyStoreResolver);
		
		
		// setup some authz failures and add to failure repository
		failures = new AuthzCacheUpdateFailureRepositoryImpl();
	
		FailedAuthzCacheUpdate failure = new FailedAuthzCacheUpdateImpl();
		failure.setEndPoint("blah.com");
		failure.setRequestDocument(generateClearCacheRequest(this.issuerIDRequest1));
		failure.setTimeStamp(new Date());
		
		FailedAuthzCacheUpdate failure2 = new FailedAuthzCacheUpdateImpl();
		failure2.setEndPoint("somewhere.else.com");
		failure2.setRequestDocument(generateClearCacheRequest(this.issuerIDRequest2));
		failure2.setTimeStamp(new Date(System.currentTimeMillis()));
		
		failures.add(failure);
		failures.add(failure2);			
	}
	
	
	@After
	public void tearDown()
	{
		if(this.testMonitor != null && this.testMonitor.isAlive())
			this.testMonitor.shutdown();
	}
	
	/* This first failure will be removed from the repositoryafter first poll, as the failure date has not been set.
	 * 
	 * THIS TEST is no longer required as the removal of invalif failure objects has been taken out of the
	 * monitor. Invalid failures are no longer permitted by the failure repository.
	 *
	@SuppressWarnings("nls")
	@Test
	public final void testRemoveInvalidFailure() throws Exception
	{
		this.testMonitor = new CacheUpdateFailureMonitor(failures, metadata, webServiceClient, keyStoreResolver, new IdentifierGeneratorImpl(new IdentifierCacheImpl()), retryInterval, 6);		
		
		assertTrue(this.testMonitor.isAlive());
		
		try
		{
			expect(webServiceClient.authzCacheClear((byte[])notNull(),(String)notNull())).andReturn(this.generateClearCacheResponse(this.issuerIDRequest1, StatusCodeConstants.invalidAttr)).anyTimes();
		
		}
		catch(MarshallerException e)
		{
			e.printStackTrace();
			fail("Unable to create authzCacheClear Request.");
		}
		catch(WSClientException e)
		{
			// cant get one with the mocked class
		}
		
		replay(webServiceClient);
		
		assertTrue(this.testMonitor.isAlive());		
	
	}*/


	/** Test the behaviour of the monitor with regards to removing successfully sent entries.
	 * after first poll (this.retryInterval) there should be no entries left.
	 */
	@Test
	public final void testBehaviour1() throws Exception
	{	
		this.testMonitor = new CacheUpdateFailureMonitor(failures, metadata, webServiceClient, keyStoreResolver, new IdentifierGeneratorImpl(new IdentifierCacheImpl()), retryInterval, maxAge);		
		
		assertTrue(this.testMonitor.isAlive());
		
		try
		{
			expect(webServiceClient.authzCacheClear((byte[])notNull(),(String)notNull())).andReturn(this.generateClearCacheResponse(this.issuerIDRequest2, StatusCodeConstants.success)).anyTimes();
		
		}
		catch(MarshallerException e)
		{
			e.printStackTrace();
			fail("Unable to create authzCacheClear Request.");
		}
		catch(WSClientException e)
		{
			// cant get one with the mocked class
		}
		
		replay(webServiceClient);
		
		// before
		assertEquals("Failure repository size before sleep is incorrect", 2, this.failures.getSize());
		
		// sleep the test thread so we can observe failure monitor thread behaviour. 
		Thread.sleep(this.retryInterval * 2000);		
		
		// failure repository should have decreased in size by 2. One for invalid and one for successfully sent.
		assertEquals("Failure repository size after sleep is incorrect", 0, this.failures.getSize());
		
	}
	
	
	
	/** Test the behaviour of the monitor when it recieves an invalid response. Should leave
	 * failure in repository.
	 *
	 */
	@Test
	public void testBehaviour2b() throws Exception
	{
		this.testMonitor = new CacheUpdateFailureMonitor(failures, metadata, webServiceClient, keyStoreResolver, new IdentifierGeneratorImpl(new IdentifierCacheImpl()), retryInterval, maxAge);		
		
		assertTrue(this.testMonitor.isAlive());
		
		try
		{
			expect(webServiceClient.authzCacheClear((byte[])notNull(),(String)notNull())).andReturn(new String("<invalid />").getBytes()).anyTimes();
		
		}
		catch(WSClientException e)
		{
			// cant get one with the mocked class unless we explicitly thorw it
		}
		
		replay(webServiceClient);
		
		Thread.sleep(this.retryInterval * 1500);
		
		// add a new failure with invalid request document, it should stay until it expires.
		FailedAuthzCacheUpdate failure = new FailedAuthzCacheUpdateImpl();
		failure.setEndPoint("new.invalid.com");
		failure.setRequestDocument(new String("<hello />").getBytes());
		failure.setTimeStamp(new Date(System.currentTimeMillis()));

		this.failures.add(failure);

		//	sleep some more
		Thread.sleep(this.retryInterval * 1500);
	
		// failure repository should have decreased in size by 1 again, for expired.
		assertEquals("Failure repository size after sleep 2 is incorrect", 1, this.failures.getSize());
		
	}
		
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction1() throws Exception
	{
		this.testMonitor = new CacheUpdateFailureMonitor(null, metadata, webServiceClient, keyStoreResolver, new IdentifierGeneratorImpl(new IdentifierCacheImpl()), retryInterval, maxAge);		
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction2() throws Exception
	{
		this.testMonitor = new CacheUpdateFailureMonitor(failures, null, webServiceClient, keyStoreResolver, new IdentifierGeneratorImpl(new IdentifierCacheImpl()), retryInterval, maxAge);		
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction3() throws Exception
	{
		this.testMonitor = new CacheUpdateFailureMonitor(failures, metadata, null, keyStoreResolver, new IdentifierGeneratorImpl(new IdentifierCacheImpl()), retryInterval, maxAge);		
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction4() throws Exception
	{
		this.testMonitor = new CacheUpdateFailureMonitor(failures, metadata, webServiceClient, null, new IdentifierGeneratorImpl(new IdentifierCacheImpl()), retryInterval, maxAge);		
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction5() throws Exception
	{
		this.testMonitor = new CacheUpdateFailureMonitor(failures, metadata, webServiceClient, keyStoreResolver, null , retryInterval, maxAge);		
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction6() throws Exception
	{
		this.testMonitor = new CacheUpdateFailureMonitor(failures, metadata, webServiceClient, keyStoreResolver, new IdentifierGeneratorImpl(new IdentifierCacheImpl()), -1, maxAge);		
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction7() throws Exception
	{
		this.testMonitor = new CacheUpdateFailureMonitor(failures, metadata, webServiceClient, keyStoreResolver, new IdentifierGeneratorImpl(new IdentifierCacheImpl()), retryInterval, -933);		
	}
	
	@Test
	public final void testShutdown() throws Exception
	{
		this.testMonitor = new CacheUpdateFailureMonitor(failures, metadata, webServiceClient, keyStoreResolver, new IdentifierGeneratorImpl(new IdentifierCacheImpl()), retryInterval, maxAge);		
		
		assertTrue(this.testMonitor.isAlive());
		
		try
		{
			expect(webServiceClient.authzCacheClear((byte[])notNull(),(String)notNull())).andReturn(this.generateClearCacheResponse(this.issuerIDRequest2, StatusCodeConstants.success)).anyTimes();
		
		}
		catch(MarshallerException e)
		{
			fail("Unable to create authzCacheCkear Request.");
		}
			
		replay(webServiceClient);
		
		Thread.sleep(10000);
		
		this.testMonitor.shutdown();
		
		Thread.sleep(10000);
		
		assertTrue(!this.testMonitor.isAlive());
	}
	
	
	/* Generate a valid authzclear cache request xml document.
	 * 
	 */
	private byte[] generateClearCacheRequest(String issuerID) throws MarshallerException
	{
		byte[] requestDocument = null;
					
		ClearAuthzCacheRequest request = new ClearAuthzCacheRequest();
		NameIDType issuer = new NameIDType();
		issuer.setValue(issuerID);
		String requestID = "_asfjopiwejoiqjweorijqwoejroqwiejtroiqwjetoiqwjetpoiqjweporijqwpeorjipqwer";
		request.setID(requestID);
		request.setIssueInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));
		request.setIssuer(issuer);
		request.setReason("Lol");
		request.setSignature(new Signature());
		request.setVersion(VersionConstants.saml20);
				
		requestDocument = this.clearAuthzCacheRequestMarshaller.marshallSigned(request);

		return requestDocument;
	}
	
	
	/* Generate a valid ClearCache Request XML string
	 * 
	 */
	private byte[] generateClearCacheResponse(String inResponseTo, String statusCodeValue)
	throws MarshallerException
	{
		byte[] responseDocument = null;
		ClearAuthzCacheResponse clearAuthzCacheResponse = null;
		
		NameIDType issuer = new NameIDType();
		issuer.setValue("esoe74832942842");
		
		Status status = new Status();
		StatusCode statusCode = new StatusCode();
		statusCode.setValue(statusCodeValue);
		status.setStatusCode(statusCode);
		status.setStatusMessage("TEST response");
		
		clearAuthzCacheResponse = new ClearAuthzCacheResponse();
		clearAuthzCacheResponse.setInResponseTo(inResponseTo);
		clearAuthzCacheResponse.setID("_743982472472");
		clearAuthzCacheResponse.setIssueInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));
		clearAuthzCacheResponse.setIssuer(issuer);
		clearAuthzCacheResponse.setSignature(new Signature());
		clearAuthzCacheResponse.setVersion(VersionConstants.saml20);
		clearAuthzCacheResponse.setStatus(status);
		
		responseDocument = clearAuthzCacheResponseMarshaller.marshallSigned(clearAuthzCacheResponse);
		
		return responseDocument;
	}
}

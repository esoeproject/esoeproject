package com.qut.middleware.esoe.pdp;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.Vector;

import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Before;
import org.junit.Test;
import org.w3._2000._09.xmldsig_.Signature;

import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.crypto.KeyStoreResolver;
import com.qut.middleware.esoe.crypto.impl.KeyStoreResolverImpl;
import com.qut.middleware.esoe.metadata.Metadata;
import com.qut.middleware.esoe.pdp.bean.AuthorizationProcessorData;
import com.qut.middleware.esoe.pdp.bean.impl.AuthorizationProcessorDataImpl;
import com.qut.middleware.esoe.pdp.cache.bean.AuthzPolicyCache;
import com.qut.middleware.esoe.pdp.exception.InvalidRequestException;
import com.qut.middleware.esoe.pdp.impl.AuthorizationProcessorImpl;
import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.Query;
import com.qut.middleware.esoe.sessions.SessionsProcessor;
import com.qut.middleware.esoe.sessions.bean.IdentityAttribute;
import com.qut.middleware.esoe.sessions.bean.impl.IdentityAttributeImpl;
import com.qut.middleware.esoe.sessions.exception.InvalidSessionIdentifierException;
import com.qut.middleware.esoe.spep.SPEPProcessor;
import com.qut.middleware.saml2.VersionConstants;
import com.qut.middleware.saml2.exception.KeyResolutionException;
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
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.assertion.SubjectConfirmation;
import com.qut.middleware.saml2.schemas.assertion.SubjectConfirmationDataType;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Policy;
import com.qut.middleware.saml2.schemas.esoe.lxacml.PolicySet;
import com.qut.middleware.saml2.schemas.esoe.lxacml.assertion.LXACMLAuthzDecisionStatement;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.Attribute;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.AttributeValue;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.DecisionType;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.Request;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.Resource;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.Result;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.Subject;
import com.qut.middleware.saml2.schemas.esoe.lxacml.protocol.LXACMLAuthzDecisionQuery;
import com.qut.middleware.saml2.schemas.protocol.Response;
import com.qut.middleware.saml2.validator.SAMLValidator;
import com.qut.middleware.saml2.validator.impl.SAMLValidatorImpl;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

@SuppressWarnings(value={"nls", "unqualified-field-access", "unchecked"})
public class AuthorizationProcessorTest
{
	
	AuthorizationProcessorImpl authProcessor;
	
	AuthzPolicyCache cache;
	SessionsProcessor sessionsProcessor;
	Principal principal;
	Query query;
	Map<String, List<Policy>> database;
	Unmarshaller<PolicySet> policySetUnmarshaller;
	Unmarshaller<Response> responseUnmarshaller;
	Marshaller requestMarshaller;
	Metadata metadata;
	SPEPProcessor spepProcessor;
	
	String samlIdentifier = "5564840920";
	Map<String, IdentityAttribute> attributeList;
	private SAMLValidator validator;
	private IdentifierCache identifierCache;
	private IdentifierGenerator identifierGenerator;
	private PublicKey pubKey;
	private PrivateKey privKey;
	KeyStoreResolver keyStoreResolver;
	
	@Before
	public void setUp() throws Exception
	{

		int skew = 2000; //(Integer.MAX_VALUE / 1000 -1);
		this.identifierCache = new IdentifierCacheImpl();
		this.identifierGenerator = new IdentifierGeneratorImpl(new IdentifierCacheImpl());
		
		this.principal = createMock(Principal.class);
		this.query = createMock(Query.class);
		this.sessionsProcessor= createMock(SessionsProcessor.class);
		this.cache = createMock(AuthzPolicyCache.class);
		this.metadata = createMock(Metadata.class);
		this.spepProcessor = createMock(SPEPProcessor.class);
		
		this.attributeList = new HashMap<String, IdentityAttribute>();
		IdentityAttribute emailAttr = new IdentityAttributeImpl();
		emailAttr.addValue("a.zitelli@qut.edu.au");
		emailAttr.addValue("t.smith@blah.com");
		IdentityAttribute typeAttr = new IdentityAttributeImpl();
		typeAttr.addValue("STUDENT");
		typeAttr.addValue("STAFF");
		typeAttr.addValue("part-time-staff");
		IdentityAttribute userAttr = new IdentityAttributeImpl();
		userAttr.addValue("zitelli");
		attributeList.put("email", emailAttr);
		attributeList.put("type", typeAttr);
		attributeList.put("username", userAttr);
		
		//this.cache = new AuthzPolicyCacheImpl();	
		setupPolicyCache();
		
		String keyStorePath = System.getProperty("user.dir") + File.separator + "tests" + File.separator + "testdata" + File.separator + "testskeystore.ks";
		String keyStorePassword = "Es0EKs54P4SSPK";
		String esoeKeyAlias = "esoeprimary";
		String esoeKeyPassword = "Es0EKs54P4SSPK";
		
		this.keyStoreResolver = new KeyStoreResolverImpl(new File(keyStorePath), keyStorePassword, esoeKeyAlias, esoeKeyPassword);
		
		this.privKey = this.keyStoreResolver.getPrivateKey();
		this.pubKey = this.keyStoreResolver.getPublicKey();
		
		expect(this.metadata.resolveKey("esoeprimary")).andReturn(this.pubKey).anyTimes();
		expect(this.metadata.getESOEIdentifier()).andReturn(esoeKeyAlias).anyTimes();
		replay(this.spepProcessor);	
		replay(this.metadata);
	
		this.validator = new SAMLValidatorImpl(this.identifierCache, skew);		
		this.authProcessor = new AuthorizationProcessorImpl(cache, sessionsProcessor,  this.metadata, this.validator, this.identifierGenerator, this.keyStoreResolver, "DENY", 20);
			
		String[] schemas = new String[]  {ConfigurationConstants.lxacmlSAMLAssertion, ConfigurationConstants.lxacmlSAMLProtocol};
		this.responseUnmarshaller = new UnmarshallerImpl<Response>(Response.class.getPackage().getName() + ":" + LXACMLAuthzDecisionStatement.class.getPackage().getName(), schemas, metadata);
		
		String keyName = keyStoreResolver.getKeyAlias();
		this.privKey =  keyStoreResolver.getPrivateKey();
		
		this.requestMarshaller = new MarshallerImpl<LXACMLAuthzDecisionQuery>(LXACMLAuthzDecisionQuery.class.getPackage().getName(), schemas, keyName, this.privKey);

	}

	
	private void setupMock()
	{
		/* Start the replay for all our configured mock objects */
		replay(this.sessionsProcessor);
		replay(this.cache);
		replay(this.principal);
		replay(query);
	}
	
	
	/* Using test policy PolicySetSimple.xml. It PERMITS access to /default/* all else should be denied.
	 * 
	 */
	@Test
	public final void testValidateAuthzRequest1() throws InvalidRequestException, InvalidSessionIdentifierException, KeyResolutionException
	{
		AuthorizationProcessorData authData = new AuthorizationProcessorDataImpl();
	
		AuthorizationProcessor.result requestResult;
		
		expect(this.principal.getSAMLAuthnIdentifier()).andReturn(samlIdentifier);
		expect(this.principal.getAttributes()).andReturn(this.attributeList).anyTimes();
		expect(this.sessionsProcessor.getQuery()).andReturn(this.query).anyTimes();
		expect(this.query.querySAMLSession((String)notNull())).andReturn(this.principal).anyTimes();
		expect(this.cache.getPolicies("urn:test:spep:id:s")).andReturn(new Vector(this.database.get("urn:test:spep:id:s"))).anyTimes();
		expect(this.cache.getSize()).andReturn(this.database.size()).anyTimes();
		setupMock();		
		
		// TEST case 1 . Request a resource that should eval to PERMIT. Unmarshall and check response
		authData.setRequestDocument(createRequestXml("/default/hello.jsp", "urn:test:spep:id:s"));		
	
		requestResult = this.authProcessor.execute(authData);				
		
		assertEquals("Unexpected return value. ",AuthorizationProcessor.result.Successful, requestResult );		
		// check the returned response string
		String responseXml = authData.getResponseDocument();		
		assertNotNull(responseXml);		
		////System.out.println(responseXml);		
		// unmarshall and ensure the result is PERMIT as expected
		Result result = this.getResult(responseXml);			
		assertNotNull("Failed to unmarshall returned response. ", result);		
		assertEquals("Incorrect result returned. ", DecisionType.PERMIT, result.getDecision());
		
		
		// TEST case 2 . Request a resource that should eval to DENY. In the case of this policy, there are no
		// explicit DENY rules, therefore any request that is a miss, should fall through to the default policy
		// of DENY. For greater explnation, examine the returned response string to verify the message.
		authData.setRequestDocument(createRequestXml("/some/denied/resource.jsp", "urn:test:spep:id:s"));		
	
		requestResult = this.authProcessor.execute(authData);				
	
		assertEquals("Unexpected return value. ",AuthorizationProcessor.result.Successful, requestResult );		
		// check the returned response string
		responseXml = authData.getResponseDocument();		
		assertNotNull(responseXml);		
	//	//System.out.println(responseXml);		
		// unmarshall and ensure the result is PERMIT as expected
		result = this.getResult(responseXml);			
		assertNotNull("Failed to unmarshall returned response. ", result);		
		assertEquals("Incorrect result returned. ", DecisionType.DENY, result.getDecision());
		
		// ensure the returned response is valid
		try
		{
			Response response = this.responseUnmarshaller.unMarshallSigned(responseXml);
		
			this.validator.getResponseValidator().validate(response);
			
			
			// check the notonorafter fields
			// Find all assertions in the response.
			for (Object encryptedAssertionOrAssertion : response.getEncryptedAssertionsAndAssertions())
			{
				if (encryptedAssertionOrAssertion instanceof Assertion)
				{
					Assertion assertion = (Assertion)encryptedAssertionOrAssertion;
					
					// ensure parameters are present. not the prettiest way to do it but it avoids
					// namespace clashes with Subject
					if(assertion.getSubject() == null)
					{
						fail("No subject in assertion");
					}
					else
					{
						if(assertion.getSubject().getSubjectConfirmationNonID() == null)
						{
							fail("No subject confirmation in assertion");
						}
					}				
					
					// verify SubjectConfirmationData  fields
					List<SubjectConfirmation> subjectConfirmations = assertion.getSubject().getSubjectConfirmationNonID();
					if(subjectConfirmations.size() == 0)
					{
						fail("0 subject confirmations found in assertion");
					}
					
					for(SubjectConfirmation confirmation: subjectConfirmations)
					{
						SubjectConfirmationDataType confirmationData = confirmation.getSubjectConfirmationData();
						
						if(confirmationData == null)
						{
							fail("No subject confirmation data in assertion");
						}
											
						// validate data has not expired
						XMLGregorianCalendar xmlCalendar = confirmationData.getNotOnOrAfter();
						GregorianCalendar notOnOrAfterCal = xmlCalendar.toGregorianCalendar();
						
						TimeZone utc = new SimpleTimeZone(0, ConfigurationConstants.timeZone); 
						GregorianCalendar thisCal = new GregorianCalendar(utc);
						
						//System.out.println("Comparing this date " + thisCal.getTimeInMillis() + " to recieved date of " + notOnOrAfterCal.getTimeInMillis());
						if(thisCal.after(notOnOrAfterCal))
						{
							fail("Subject confirmation expired");
						}
						
					}
				}
			}
		}
		catch(Exception e)
		{
			fail("Unexpected expetion thrown. Auth processor did not return a valid response.");
		}
	}
	
	
	/* Using test policy PolicyComplexity1.xml. The attributes sent with the SPEP request are defined in the
	 * setup() mthod of this test class. The policy has 4 rules as follows:
	 * 
	 * RULE 1 (RuleId="complexity:1-1a")
	 * 	PERMIT any users with username containing '*z*' access to Policy target of '/default/*' AND/OR '*'/other/'*'
	 *
	 * RULE 2 (complexity:1-1a)
	 * 	PERMIT any users with username zITELli (this rule is normalised to lower case, therefore upper case requests
	 *  should be denied) access to Policy target of /default/'*' AND/OR '*'/other/'*'
	 * 
	 * RULE 3 (complexity:1-2)
	 * 	DENY all users access to resources /default/private/* AND /default/secret/*
	 * 
	 * RULE 4 (complexity:1-3)
	 * 	This rule contains no AttributeDesignators and thus should be ignored by the authz processor. Access to 
	 *  /default/public/* and /default/some/other/myfile.jsp MUST be tested to ensure that requests for these resources
	 *  fall through to default state.
	 * 
	 */
	@Test
	public final void testValidateAuthzRequest2() throws InvalidRequestException, InvalidSessionIdentifierException, KeyResolutionException
	{
		AuthorizationProcessorData authData = new AuthorizationProcessorDataImpl();
		AuthorizationProcessor.result requestResult;				
		String responseXml;
		Result result;
		
		expect(this.principal.getSAMLAuthnIdentifier()).andReturn(samlIdentifier).anyTimes();
		expect(this.principal.getAttributes()).andReturn(this.attributeList).anyTimes();
		expect(this.sessionsProcessor.getQuery()).andReturn(this.query).anyTimes();
		expect(this.query.querySAMLSession((String)notNull())).andReturn(this.principal).anyTimes();
		expect(this.cache.getPolicies("urn:test:spep:id:1")).andReturn(new Vector(this.database.get("urn:test:spep:id:1"))).anyTimes();
		expect(this.cache.getSize()).andReturn(this.database.size()).anyTimes();
		setupMock();
		
		// TEST CASE 1. Rule 1 should allow access for user 'zitelli' to default policy target of /default/*.
		// Check the returned xml response to verify that the group target has been set to /default/* AND
		// the authztarget has been set to the matching rule target (in the case the same as no explicit 
		// target specified)
		authData.setRequestDocument(createRequestXml("/default/public/hello.jsp", "urn:test:spep:id:1"));		
		requestResult = this.authProcessor.execute(authData);				
		assertEquals("Unexpected return value. ",AuthorizationProcessor.result.Successful, requestResult );		
		// check the returned response string
		responseXml = authData.getResponseDocument();
		assertNotNull(responseXml);		
		////System.out.println(responseXml);
		// unmarshall and ensure the result is PERMIT as expected
		result = this.getResult(responseXml);			
		assertNotNull("Failed to unmarshall returned response. ", result);		
		assertEquals("Incorrect result returned. ", DecisionType.PERMIT, result.getDecision());
		
		
		// TEST case 2. As above but different resource as specified by policy target. The group and authz targets
		// should have changed to match new request.
		authData.setRequestDocument(createRequestXml("/some/other/myfile.jsp", "urn:test:spep:id:1"));	
		requestResult = this.authProcessor.execute(authData);		
		assertEquals("Unexpected return value. ",AuthorizationProcessor.result.Successful, requestResult );		
		// check the returned response string
		responseXml = authData.getResponseDocument();		
		assertNotNull(responseXml);		
	//	//System.out.println(responseXml);
		// unmarshall and ensure the result is PERMIT as expected
		result = this.getResult(responseXml);			
		assertNotNull("Failed to unmarshall returned response. ", result);		
		assertEquals("Incorrect result returned. ", DecisionType.PERMIT, result.getDecision());		
				
		// TEST CASES 3 & 4 . Test the more complex regualar expression used by the third target of the policy
		// [A-Z]/other/\n/[y]{2}*. This case should eval to PERMIT while the next should fall through to default.
		authData.setRequestDocument(createRequestXml("ZOIKS/regex/1245/yy.jsp", "urn:test:spep:id:1"));	
		requestResult = this.authProcessor.execute(authData);		
		assertEquals("Unexpected return value. ",AuthorizationProcessor.result.Successful, requestResult );		
		// check the returned response string
		responseXml = authData.getResponseDocument();		
		assertNotNull(responseXml);		
		////System.out.println(responseXml);
		// unmarshall and ensure the result is PERMIT as expected
		result = this.getResult(responseXml);			
		assertNotNull("Failed to unmarshall returned response. ", result);		
		assertEquals("Incorrect result returned. ", DecisionType.PERMIT, result.getDecision());		
		
		authData.setRequestDocument(createRequestXml("Zoiks/regex/124d5/yyyyy.jsp", "urn:test:spep:id:1"));	
		requestResult = this.authProcessor.execute(authData);		
		assertEquals("Unexpected return value. ",AuthorizationProcessor.result.Successful, requestResult );		
		// check the returned response string
		responseXml = authData.getResponseDocument();		
		assertNotNull(responseXml);		
		////System.out.println(responseXml);
		// unmarshall and ensure the result is as expected
		result = this.getResult(responseXml);			
		assertNotNull("Failed to unmarshall returned response. ", result);		
		assertEquals("Incorrect result returned. ", DecisionType.DENY, result.getDecision());		
		
		
		// TEST CASE 5. Rule 2 should allow zitelli access to policy target. We must also ensure that BOTH matching rules
		// have been evaulated by checking that thge authz targets have both been set (IE rules 1 & 2 match this request)
		authData.setRequestDocument(createRequestXml("/default/-i?*&#*(#^$(=-9430jk/am in yo", "urn:test:spep:id:1"));	
		requestResult = this.authProcessor.execute(authData);		
		assertEquals("Unexpected return value. ",AuthorizationProcessor.result.Successful, requestResult );		
		// check the returned response string
		responseXml = authData.getResponseDocument();		
		assertNotNull(responseXml);		
		////System.out.println(responseXml);
		// unmarshall and ensure the result is as expected
		result = this.getResult(responseXml);			
		assertNotNull("Failed to unmarshall returned response. ", result);		
		assertEquals("Incorrect result returned. ", DecisionType.PERMIT, result.getDecision());		
				
		// TEST CASES 6 & 7. Rule 3 should DENY any requests to /default/private/* or /other/secret/*
		// The group target should be
		authData.setRequestDocument(createRequestXml("/default/private/haxor.com", "urn:test:spep:id:1"));	
		requestResult = this.authProcessor.execute(authData);		
		assertEquals("Unexpected return value. ",AuthorizationProcessor.result.Successful, requestResult );		
		// check the returned response string
		responseXml = authData.getResponseDocument();		
		assertNotNull(responseXml);		
		////System.out.println(responseXml);
		// unmarshall and ensure the result is PERMIT as expected
		result = this.getResult(responseXml);			
		assertNotNull("Failed to unmarshall returned response. ", result);		
		assertEquals("Incorrect result returned. ", DecisionType.DENY, result.getDecision());		
		
		authData.setRequestDocument(createRequestXml("/other/secret/haxor.com", "urn:test:spep:id:1"));	
		requestResult = this.authProcessor.execute(authData);		
		assertEquals("Unexpected return value. ",AuthorizationProcessor.result.Successful, requestResult );		
		// check the returned response string
		responseXml = authData.getResponseDocument();		
		assertNotNull(responseXml);		
		////System.out.println(responseXml);
		// unmarshall and ensure the result is PERMIT as expected
		result = this.getResult(responseXml);			
		assertNotNull("Failed to unmarshall returned response. ", result);		
		assertEquals("Incorrect result returned. ", DecisionType.DENY, result.getDecision());	
		
		// TEST CASE 8. Rule 4 will fail because it has an apply element with no attribute designator,
		// thus making the rule invalid. In this case the opposite of the rule is applied 
		// If the rule denies the following request, it is an error.
		authData.setRequestDocument(createRequestXml("/other/other/not/so/public.jsp", "urn:test:spep:id:1"));	
		requestResult = this.authProcessor.execute(authData);		
		assertEquals("Unexpected return value. ",AuthorizationProcessor.result.Successful, requestResult );		
		// check the returned response string
		responseXml = authData.getResponseDocument();		
		assertNotNull(responseXml);		
		////System.out.println(responseXml);
		// unmarshall and ensure the result is PERMIT as expected
		result = this.getResult(responseXml);			
		assertNotNull("Failed to unmarshall returned response. ", result);		
		assertEquals("Incorrect result returned. ", DecisionType.PERMIT, result.getDecision());	
		
//		 ensure the returned response is valid
		try
		{
			Response response = this.responseUnmarshaller.unMarshallSigned(responseXml);
		
			this.validator.getResponseValidator().validate(response);
		}
		catch(Exception e)
		{
			fail("Unexpected expetion thrown. Auth processor did not return a valid response.");
		}
		
	}
	
	
	/* Using test policy PolicyComplexity2.xml. The attributes sent with the SPEP request are defined in the
	 * setup() method of this test class. The policySet has 2 policies, each with numerous rules. IT is important
	 * that all policies are evaluated during processing. Use the output to ensure that multiple policies are 
	 * included in evauluation. General rule definitions are supplied below with each test case.
	 * 
	 * RULE 2 (complexity:1-1a)
	 * 	PERMIT any users with username zITELli (this rule is normalised to lower case, therefore upper case requests
	 *  should be denied) access to Policy target of /default/'*' AND/OR '*'/other/'*'
	 * 
	 * RULE 3 (complexity:1-2)
	 * 	DENY all users access to resources /default/private/* AND /default/secret/*
	 * 
	 * RULE 4 (complexity:1-3)
	 * 	This rule contains no AttributeDesignators and thus should be ignored by the authz processor. Access to 
	 *  /default/public/* and /default/some/other/myfile.jsp MUST be tested to ensure that requests for these resources
	 *  fall through to default state.
	 */
	@Test
	public final void testValidateAuthzRequest3() throws InvalidRequestException, InvalidSessionIdentifierException, KeyResolutionException
	{
		AuthorizationProcessorData authData = new AuthorizationProcessorDataImpl();
		AuthorizationProcessor.result requestResult;
				
		expect(this.principal.getSAMLAuthnIdentifier()).andReturn(samlIdentifier).anyTimes();
		expect(this.principal.getAttributes()).andReturn(this.attributeList).anyTimes();
		expect(this.sessionsProcessor.getQuery()).andReturn(this.query).anyTimes();
		expect(this.query.querySAMLSession((String)notNull())).andReturn(this.principal).anyTimes();
		expect(this.cache.getPolicies("urn:test:spep:id:2")).andReturn(new Vector(this.database.get("urn:test:spep:id:2"))).anyTimes();
		expect(this.cache.getSize()).andReturn(this.database.size()).anyTimes();
		setupMock();
		

		// TEST CASE 1 RULE 2 (complexity:2-12) All rules bar one should eval to PERMIT. This test is essentially
		// an additional test to ensure that all matching targets are included as authztargets.
		authData.setRequestDocument(createRequestXml("http://new.com/public/this.html", "urn:test:spep:id:2"));
		requestResult = this.authProcessor.execute(authData);				
		assertEquals("Unexpected return value. ",AuthorizationProcessor.result.Successful, requestResult );
		// check the returned response string
		String responseXml = authData.getResponseDocument();
		assertNotNull(responseXml);
		////System.out.println(responseXml);	
		// unmarshall and ensure the result is as expected
		Result result = this.getResult(responseXml);			
		assertNotNull("Failed to unmarshall returned response. ", result);		
		assertEquals("Incorrect result returned. ", DecisionType.PERMIT, result.getDecision());	
		
		
		// TEST CASE 2 (complexity:6-143) DENY any user with type attribute = STUDENT access to
		// /test/denytarget/some/other/file.txt, but allow staff. It just so happens our test user has
		// both attributes, therefore one rule should eval to permit, with the ultimate outcome DENY, yet both rules
		// should show up in the response.
		authData.setRequestDocument(createRequestXml("/test/staff.txt", "urn:test:spep:id:2"));
		requestResult = this.authProcessor.execute(authData);				
		assertEquals("Unexpected return value. ",AuthorizationProcessor.result.Successful, requestResult );
		// check the returned response string
		responseXml = authData.getResponseDocument();
		assertNotNull(responseXml);
		System.out.println(responseXml);	
		// unmarshall and ensure the result is as expected
		result = this.getResult(responseXml);			
		assertNotNull("Failed to unmarshall returned response. ", result);		
		assertEquals("Incorrect result returned. ", DecisionType.DENY, result.getDecision());	
		
//		 ensure the returned response is valid
		try
		{
			Response response = this.responseUnmarshaller.unMarshallSigned(responseXml);
		
			this.validator.getResponseValidator().validate(response);
		}
		catch(Exception e)
		{
			fail("Unexpected expetion thrown. Auth processor did not return a valid response.");
		}
	}
	
	
	/* Using test policy PolicyComplexity3.xml. The attributes sent with the SPEP request are defined in the
	 * setup() method of this test class. The policy has a number of complex rules containing various conditions
	 * 
	 * RULE 1  
	 * 
	 * 
	 */
	@Test
	public final void testValidateAuthzRequest4() throws InvalidRequestException, InvalidSessionIdentifierException, Exception
	{
		AuthorizationProcessorData authData = new AuthorizationProcessorDataImpl();
	
		AuthorizationProcessor.result requestResult;
				
		expect(this.principal.getSAMLAuthnIdentifier()).andReturn(samlIdentifier);
		expect(this.principal.getAttributes()).andReturn(this.attributeList).anyTimes();
		expect(this.sessionsProcessor.getQuery()).andReturn(this.query).anyTimes();
		expect(this.query.querySAMLSession((String)notNull())).andReturn(this.principal).anyTimes();
		expect(this.cache.getPolicies("urn:test:spep:id:3")).andReturn(new Vector(this.database.get("urn:test:spep:id:3"))).anyTimes();
		expect(this.cache.getSize()).andReturn(this.database.size()).anyTimes();
		setupMock();
		
	
		// for these test we will be resetting the identity of the requestor
		this.attributeList = new HashMap<String, IdentityAttribute>();
		IdentityAttribute emailAttr = new IdentityAttributeImpl();
		emailAttr.addValue("a.zitelli@qut.edu.au");
		IdentityAttribute typeAttr = new IdentityAttributeImpl();
		typeAttr.addValue("STAFF");
		IdentityAttribute userAttr = new IdentityAttributeImpl();
		userAttr.addValue("zitelli");
		attributeList.put("email", emailAttr);
		attributeList.put("type", typeAttr);
		attributeList.put("username", userAttr);
		
		// TEST CASE 1 - (rule complexity:3-136) PERMIT zitelli access to .default/private.* ALL 
		// else should be denied. See test case 5 below for the same request with a different identity.
		// this one should eval to permit.
		authData.setRequestDocument(createRequestXml("/default/private/myporn.vob", "urn:test:spep:id:3"));
		requestResult = this.authProcessor.execute(authData);				
		assertEquals("Unexpected return value. ",AuthorizationProcessor.result.Successful, requestResult );
		// check the returned response string
		String responseXml = authData.getResponseDocument();
		assertNotNull(responseXml);
		////System.out.println(responseXml);	
		// unmarshall and ensure the result is as expected
		Result result = this.getResult(responseXml);			
		assertNotNull("Failed to unmarshall returned response. ", result);		
		assertEquals("Incorrect result returned. ", DecisionType.PERMIT, result.getDecision());	
	
		
		// TEST CASE 2 - (rule complexity:3-136) PERMIT zitelli access to .default/private.* ALL 
		// else should be denied. See test case 5 below for the same request with a different identity.
		// this one should eval to permit.
		authData.setRequestDocument(createRequestXml("/default/private/myporn.vob", "urn:test:spep:id:3"));
		requestResult = this.authProcessor.execute(authData);				
		assertEquals("Unexpected return value. ",AuthorizationProcessor.result.Successful, requestResult );
		// check the returned response string
		responseXml = authData.getResponseDocument();
		assertNotNull(responseXml);
		////System.out.println(responseXml);	
		// unmarshall and ensure the result is as expected
		result = this.getResult(responseXml);			
		assertNotNull("Failed to unmarshall returned response. ", result);		
		assertEquals("Incorrect result returned. ", DecisionType.PERMIT, result.getDecision());	
					
//		 ensure the returned response is valid
		try
		{
			Response response = this.responseUnmarshaller.unMarshallSigned(responseXml);
		
			this.validator.getResponseValidator().validate(response);
		}
		catch(Exception e)
		{
			fail("Unexpected expetion thrown. Auth processor did not return a valid response.");
		}
	}
	
	/* Using test policy PolicyComplexity3.xml. The attributes sent with the SPEP request are defined in the
	 * setup() method of this test class. The policy has a number of complex rules containing various conditions
	 * 
	 */
	@Test
	public final void testValidateAuthzRequest4a() throws InvalidRequestException, InvalidSessionIdentifierException, Exception
	{
	
		AuthorizationProcessorData authData = new AuthorizationProcessorDataImpl();
		AuthorizationProcessor.result requestResult;
		
		// for these test we will be resetting the identity of the requestor
		this.attributeList = new HashMap<String, IdentityAttribute>();
		IdentityAttribute emailAttr = new IdentityAttributeImpl();
		emailAttr.addValue("a.blahky@qut.edu.au");
		IdentityAttribute typeAttr = new IdentityAttributeImpl();
		typeAttr.addValue("STUDENYT");
		IdentityAttribute userAttr = new IdentityAttributeImpl();
		userAttr.addValue("zitelli");
		attributeList.put("type", typeAttr);
		attributeList.put("username", userAttr);
		
		expect(this.principal.getSAMLAuthnIdentifier()).andReturn(samlIdentifier);
		expect(this.principal.getAttributes()).andReturn(this.attributeList).anyTimes();
		expect(this.sessionsProcessor.getQuery()).andReturn(this.query).anyTimes();
		expect(this.query.querySAMLSession((String)notNull())).andReturn(this.principal).anyTimes();
		expect(this.cache.getPolicies("urn:test:spep:id:3")).andReturn(new Vector(this.database.get("urn:test:spep:id:3"))).anyTimes();
		expect(this.cache.getSize()).andReturn(this.database.size()).anyTimes();
		setupMock();
		
		// No email attribute in above fake principal means the condition will return false and this Rule
		// will be ignored. Ie the AND email matches blah section of the rule will fail. As the Policy also
		// contains a Rule which will have eralier evaulated to Permit, the entitre request should be PERMIT.
		authData.setRequestDocument(createRequestXml("/painful/new/complex/rule.target", "urn:test:spep:id:3"));
		requestResult = this.authProcessor.execute(authData);				
		assertEquals("Unexpected return value. ",AuthorizationProcessor.result.Successful, requestResult );
		// check the returned response string
		String responseXml = authData.getResponseDocument();
		assertNotNull(responseXml);
		////System.out.println(responseXml);	
		// unmarshall and ensure the result is as expected
		Result result = this.getResult(responseXml);			
		assertNotNull("Failed to unmarshall returned response. ", result);		
		assertEquals("Incorrect result returned. ", DecisionType.PERMIT, result.getDecision());	
	
	}
	
	
	/* Using test policy PolicyComplexity3.xml. The attributes sent with the SPEP request are defined in the
	 * setup() method of this test class. The policy has a number of complex rules containing various conditions
	 * 
	 */
	@Test
	public final void testValidateAuthzRequest4b() throws InvalidRequestException, InvalidSessionIdentifierException, Exception
	{
	
		AuthorizationProcessorData authData = new AuthorizationProcessorDataImpl();
		AuthorizationProcessor.result requestResult;
		
		// for these test we will be resetting the identity of the requestor
		this.attributeList = new HashMap<String, IdentityAttribute>();
		IdentityAttribute emailAttr = new IdentityAttributeImpl();
		emailAttr.addValue("a.zitelli@qut.edu.au");
		IdentityAttribute typeAttr = new IdentityAttributeImpl();
		typeAttr.addValue("STudent ");
		IdentityAttribute userAttr = new IdentityAttributeImpl();
		userAttr.addValue("zitelli");
		attributeList.put("type", typeAttr);
		attributeList.put("username", userAttr);
		attributeList.put("email", emailAttr);
		
		expect(this.principal.getSAMLAuthnIdentifier()).andReturn(samlIdentifier);
		expect(this.principal.getAttributes()).andReturn(this.attributeList).anyTimes();
		expect(this.sessionsProcessor.getQuery()).andReturn(this.query).anyTimes();
		expect(this.query.querySAMLSession((String)notNull())).andReturn(this.principal).anyTimes();
		expect(this.cache.getPolicies("urn:test:spep:id:3")).andReturn(new Vector(this.database.get("urn:test:spep:id:3"))).anyTimes();
		expect(this.cache.getSize()).andReturn(this.database.size()).anyTimes();
		setupMock();
		
		// Contrary to test 4b, this one should PERMIT, as the 3 required attributes in the second OR block
		// match the attributes specified above
		authData.setRequestDocument(createRequestXml("/painful/new/complex/rule.target", "urn:test:spep:id:3"));
		requestResult = this.authProcessor.execute(authData);				
		assertEquals("Unexpected return value. ",AuthorizationProcessor.result.Successful, requestResult );
		// check the returned response string
		String responseXml = authData.getResponseDocument();
		assertNotNull(responseXml);
		////System.out.println(responseXml);	
		// unmarshall and ensure the result is as expected
		Result result = this.getResult(responseXml);			
		assertNotNull("Failed to unmarshall returned response. ", result);		
		assertEquals("Incorrect result returned. ", DecisionType.PERMIT, result.getDecision());	
	
	}
	
	/* Explicit tests to ensure that processing of rules continues if conditions within a Rule fail. All the rules in this
	 * policy should be evaulated, but the final decision should be from the default auth mode. Check the output to 
	 * ensure all Rules are evaluated. 
	 */
	@Test
	public final void testValidateAuthzRequest4c() throws InvalidRequestException, InvalidSessionIdentifierException, Exception
	{
	
		AuthorizationProcessorData authData = new AuthorizationProcessorDataImpl();
		AuthorizationProcessor.result requestResult;
		
		// for these test we will be resetting the identity of the requestor
		this.attributeList = new HashMap<String, IdentityAttribute>();
		IdentityAttribute emailAttr = new IdentityAttributeImpl();
		emailAttr.addValue("a.zitelli@qut.edu.au");
		IdentityAttribute typeAttr = new IdentityAttributeImpl();
		typeAttr.addValue("STudent ");
		IdentityAttribute userAttr = new IdentityAttributeImpl();
		userAttr.addValue("zitelli");
		attributeList.put("type", typeAttr);
		attributeList.put("username", userAttr);
		attributeList.put("email", emailAttr);
		
		expect(this.principal.getSAMLAuthnIdentifier()).andReturn(samlIdentifier);
		expect(this.principal.getAttributes()).andReturn(this.attributeList).anyTimes();
		expect(this.sessionsProcessor.getQuery()).andReturn(this.query).anyTimes();
		expect(this.query.querySAMLSession((String)notNull())).andReturn(this.principal).anyTimes();
		expect(this.cache.getPolicies("urn:test:spep:id:s2")).andReturn(new Vector(this.database.get("urn:test:spep:id:s2"))).anyTimes();
		expect(this.cache.getSize()).andReturn(this.database.size()).anyTimes();
		setupMock();
		
		// The user created above will not match any conditions specified in the permit rule, therefore
		// the request should fall through to default decision of deny.
		authData.setRequestDocument(createRequestXml("/default/", "urn:test:spep:id:s2"));
		requestResult = this.authProcessor.execute(authData);				
		assertEquals("Unexpected return value. ",AuthorizationProcessor.result.Successful, requestResult );
		// check the returned response string
		String responseXml = authData.getResponseDocument();
		assertNotNull(responseXml);
		////System.out.println(responseXml);	
		// unmarshall and ensure the result is as expected
		Result result = this.getResult(responseXml);			
		assertNotNull("Failed to unmarshall returned response. ", result);		
		assertEquals("Incorrect result returned. ", DecisionType.DENY, result.getDecision());	
	
	}
	
	/* Test a non existent policy. Ie The requesting SPEP admin has not set up a policy.
	 * 
	 */
	@Test
	public final void testValidateAuthzRequest5() throws InvalidRequestException, InvalidSessionIdentifierException, Exception
	{
		AuthorizationProcessorData authData = new AuthorizationProcessorDataImpl();
		authData.setRequestDocument(createRequestXml("/default/private/hello.jsp", "urn:test:spep:id:3"));
	
		AuthorizationProcessor.result requestResult;
				
		expect(this.principal.getSAMLAuthnIdentifier()).andReturn(samlIdentifier);
		expect(this.principal.getAttributes()).andReturn(this.attributeList).anyTimes();
		expect(this.sessionsProcessor.getQuery()).andReturn(this.query);
		expect(this.query.querySAMLSession((String)notNull())).andReturn(this.principal);
		// Mocked return here indicates NO policies found
		expect(this.cache.getPolicies("urn:test:spep:id:3")).andReturn(null);
		expect(this.cache.getSize()).andReturn(this.database.size()).anyTimes();
		setupMock();	
		
		requestResult = this.authProcessor.execute(authData);				
		assertEquals("Unexpected return value. ",AuthorizationProcessor.result.Successful, requestResult );
		// check the returned response string
		String responseXml = authData.getResponseDocument();
		assertNotNull(responseXml);
		////System.out.println(responseXml);	
		// unmarshall and ensure the result is as expected
		Result result = this.getResult(responseXml);			
		assertNotNull("Failed to unmarshall returned response. ", result);		
		assertEquals("Incorrect result returned. ", DecisionType.DENY, result.getDecision());	
				
	}
	
	
	/* Test Invalid Rules in a policy. These are rules that cannot be matched due to omitted elements
	 * that are not enforced by the schemas.
	  * 
	 */
	@Test
	public final void testValidateAuthzRequest5a() throws InvalidRequestException, InvalidSessionIdentifierException, Exception
	{
		AuthorizationProcessorData authData = new AuthorizationProcessorDataImpl();
		AuthorizationProcessor.result requestResult;
				
		expect(this.principal.getSAMLAuthnIdentifier()).andReturn(samlIdentifier);
		expect(this.principal.getAttributes()).andReturn(this.attributeList).anyTimes();
		expect(this.sessionsProcessor.getQuery()).andReturn(this.query);
		expect(this.query.querySAMLSession((String)notNull())).andReturn(this.principal);
		expect(this.cache.getPolicies("urn:test:spep:id:1")).andReturn(new Vector(this.database.get("urn:test:spep:id:1"))).anyTimes();
		expect(this.cache.getSize()).andReturn(this.database.size()).anyTimes();
		setupMock();		
	

		// TEST CASE 1. Rule complexity:1-3 in PolicySetComplexity1.xml is the inverse of a Rule complexity:1-1.
		// Howver, it will fail because it has an apply element with no attribute designator, thus making the rule invalid. In this case the
		// the rule is ignored and access should remain granted by the previous match.
		authData.setRequestDocument(createRequestXml("/other/test/brokenrule.jsp", "urn:test:spep:id:1"));	
		requestResult = this.authProcessor.execute(authData);		
		assertEquals("Unexpected return value. ",AuthorizationProcessor.result.Successful, requestResult );		
		// check the returned response string
		String responseXml = authData.getResponseDocument();		
		assertNotNull(responseXml);		
		////System.out.println(responseXml);
		// unmarshall and ensure the result is PERMIT as expected
		Result result = this.getResult(responseXml);			
		assertNotNull("Failed to unmarshall returned response. ", result);		
		assertEquals("Incorrect result returned. ", DecisionType.PERMIT, result.getDecision());	
				
	}
	
	/* Test sending an invalid request. The authz processor should return response and throw an exception.
	 * 
	 */
	@Test
	public final void testValidateAuthzRequest6() throws InvalidSessionIdentifierException, KeyResolutionException
	{
		AuthorizationProcessorData authData = new AuthorizationProcessorDataImpl();
		AuthorizationProcessor.result requestResult;
		Result result = null;
		
		try
		{
			expect(this.principal.getSAMLAuthnIdentifier()).andReturn(samlIdentifier);
			expect(this.principal.getAttributes()).andReturn(this.attributeList).anyTimes();
			expect(this.sessionsProcessor.getQuery()).andReturn(this.query);
			expect(this.query.querySAMLSession((String)notNull())).andReturn(this.principal);
			expect(this.cache.getPolicies("urn:test:spep:id:3")).andReturn(null);
			expect(this.cache.getSize()).andReturn(this.database.size()).anyTimes();
			setupMock();	
			
			String bodgyRequest = createRequestXml("/test/staff.txt", "urn:test:spep:id:3");
			bodgyRequest = bodgyRequest.replaceAll("LXACMLAuthzDecisionQuery", "bgggg6666666llleeeeeblah");
			
			authData.setRequestDocument(bodgyRequest);
			requestResult = this.authProcessor.execute(authData);				
			assertEquals("Unexpected return value. ",AuthorizationProcessor.result.Successful, requestResult );
			// check the returned response string
			String responseXml = authData.getResponseDocument();
			assertNotNull(responseXml);
			////System.out.println(responseXml);	
			// unmarshall and ensure the result is as expected
			result = this.getResult(responseXml);			
			assertNotNull("Failed to unmarshall returned response. ", result);		
			assertEquals("Incorrect result returned. ", DecisionType.DENY, result.getDecision());	
		}
		catch(InvalidRequestException e)
		{
			// we want to ensure that the auth response is still set to deny, with an authnfailed status
			String responseXml = authData.getResponseDocument();
			assertNotNull(responseXml);
			////System.out.println(responseXml);	
			// unmarshall and ensure the result is as expected
			result = this.getResult(responseXml);			
			assertNotNull("Failed to unmarshall returned response. ", result);		
			assertEquals("Incorrect result returned. ", DecisionType.DENY, result.getDecision());	
			
			return;
			
		}
		catch(InvalidSessionIdentifierException e)
		{
			throw e;
		}
		
		fail("No Invalid exception thrown. ");
		
		
	}
	
	/* add some policies to the cache for testing 
	 * 
	 */
	private void setupPolicyCache()
	{
		this.database = new HashMap<String, List<Policy>>();
		
		String path = System.getProperty("user.dir") + File.separator +"tests" + File.separator+ "testdata"+  File.separator  ;
		
		// map of policy sets and associated test SPEP ID's 
		String filenames[] = new String[]{
		path + "PolicySetSimple.xml" , //};
		path + "PolicySetComplexity1.xml" , //};
		path + "PolicySetComplexity2.xml", //};
		path + "PolicySetComplexity3.xml",
		path + "PolicySetSimple2.xml"};
		
		Map<String, String> config = new HashMap<String, String>();
		config.put(filenames[0], "urn:test:spep:id:s");
		config.put(filenames[1], "urn:test:spep:id:1");
		config.put(filenames[2], "urn:test:spep:id:2");
		config.put(filenames[3], "urn:test:spep:id:3");
		config.put(filenames[4], "urn:test:spep:id:s2");
		
		
		try
		{			
			this.policySetUnmarshaller = new UnmarshallerImpl<PolicySet>(PolicySet.class.getPackage().getName(), new String[]{ConfigurationConstants.lxacml});
			
			for(String s: filenames )
			{
				StringBuffer xml = new StringBuffer();			
				InputStream fileStream = new FileInputStream(s);
				Reader reader = new InputStreamReader(fileStream, "UTF-16");
				BufferedReader in = new BufferedReader(reader);
				
			    String str;
			    while ((str = in.readLine()) != null)
			    {
			    	xml.append(str);
			    }
			    
			    in.close();
			   			   
			    ////System.out.println(xml.toString());
				
			    PolicySet policySet = policySetUnmarshaller.unMarshallUnSigned(xml.toString());
			    
				assertNotNull(policySet);
								
				//this.cache.add(config.get(s), policySet);			
				this.database.put(config.get(s), policySet.getPolicies());
				////System.out.println("Added " + config.get(s) + " -> " + (policySet.getPolicies().size() ) );
			
			}
		}
		catch(UnmarshallerException e)
		{
			e.getCause().printStackTrace();
			fail("Caught exception loading policy cache.");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	//	assertNotNull("call to cache.getCache() returned null", cache.getCache());
		
	//	//System.out.println("Cache size is: " + this.cache.getCache().size());
		
	}
	

	/* Creates an xml string for an authz request to be sent to the authz processor.
	 * 
	 */
	private String createRequestXml(String requestedResource, String descriptorID)
	{
		String requestXml = new String();
		
		// set up the resources we're requesting access to
		Resource resource = new Resource();
		Attribute attribute = new Attribute();
		AttributeValue value = new AttributeValue();
		value.getContent().add(requestedResource);
		attribute.setAttributeValue(value);
		resource.setAttribute(attribute);
		
		// put a request for the resource together 
		Request request = new Request();
		request.setResource(resource);
	
		Subject subject = new Subject();
		Attribute subjAttr = new Attribute();
		AttributeValue subval = new AttributeValue();
		subval.getContent().add(samlIdentifier);
		subjAttr.setAttributeValue(subval);
		subject.setAttribute(subjAttr);
		
		request.setSubject(subject);
		
		LXACMLAuthzDecisionQuery authRequest = new LXACMLAuthzDecisionQuery();
		authRequest.setRequest(request);
		authRequest.setIssueInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));		
		authRequest.setID(this.identifierGenerator.generateSAMLID());
		authRequest.setVersion(VersionConstants.saml20);
			
		// this will be retrieved from the SAML request (it is the SPEP ID)
		NameIDType issuer = new NameIDType();
		issuer.setNameQualifier(descriptorID);
		issuer.setValue(descriptorID);
		authRequest.setIssuer(issuer);
		Signature signature = new Signature();
		authRequest.setSignature(signature);
		
		try
		{
			// Supplied private/public key will be in RSA format 
						
			requestXml = this.requestMarshaller.marshallSigned(authRequest);
			
			////System.out.println(requestXml);
			
		}
		catch(Exception e)
		{
			//Marshaller
			e.printStackTrace();
			//e.getCause().printStackTrace();
			
			fail("Failed to marshal auth request");
		}		
		
		return requestXml;
	}
	
	
	/* Unmarshall the returned assertion and extract the Result object for validation.
	 */
	private Result getResult(String authzResponseXmlAssertionString)
	{
		Result result = null;
		
		try
		{
			Response response = this.responseUnmarshaller.unMarshallSigned(authzResponseXmlAssertionString);	
			
			Assertion assertion = (Assertion)response.getEncryptedAssertionsAndAssertions().get(0);
			
			LXACMLAuthzDecisionStatement authzResponse = (LXACMLAuthzDecisionStatement)assertion.getAuthnStatementsAndAuthzDecisionStatementsAndAttributeStatements().get(0);
			
			result = authzResponse.getResponse().getResult();
		}
		catch(UnmarshallerException e)
		{
			e.getCause().printStackTrace();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return result;
	}
}

package com.qut.middleware.esoe.ws;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.qut.middleware.esoe.aa.AttributeAuthorityProcessor;
import com.qut.middleware.esoe.aa.bean.AAProcessorData;
import com.qut.middleware.esoe.aa.exception.InvalidPrincipalException;
import com.qut.middleware.esoe.aa.exception.InvalidRequestException;
import com.qut.middleware.esoe.authz.AuthorizationProcessor;
import com.qut.middleware.esoe.authz.bean.AuthorizationProcessorData;
import com.qut.middleware.esoe.delegauthn.DelegatedAuthenticationProcessor;
import com.qut.middleware.esoe.delegauthn.DelegatedAuthenticationProcessor.result;
import com.qut.middleware.esoe.delegauthn.bean.DelegatedAuthenticationData;
import com.qut.middleware.esoe.spep.SPEPProcessor;
import com.qut.middleware.esoe.spep.Startup;
import com.qut.middleware.esoe.spep.bean.SPEPProcessorData;
import com.qut.middleware.esoe.ws.exception.WSProcessorException;
import com.qut.middleware.esoe.ws.impl.WSProcessorImpl;
import com.qut.middleware.saml2.NameIDFormatConstants;
import com.qut.middleware.saml2.SchemaConstants;
import com.qut.middleware.saml2.handler.Marshaller;
import com.qut.middleware.saml2.handler.SOAPHandler;
import com.qut.middleware.saml2.handler.impl.MarshallerImpl;
import com.qut.middleware.saml2.handler.impl.SOAPv12Handler;
import com.qut.middleware.saml2.schemas.assertion.Assertion;
import com.qut.middleware.saml2.schemas.assertion.AttributeStatement;
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.assertion.Subject;
import com.qut.middleware.saml2.schemas.protocol.AttributeQuery;
import com.qut.middleware.saml2.schemas.protocol.Response;
import com.qut.middleware.test.Modify;

@SuppressWarnings("nls")
public class WSProcessorTest
{

	WSProcessor processor;
	AttributeAuthorityProcessor attribProcessor;
	AuthorizationProcessor authProcessor;
	SPEPProcessor spepProcessor;
	DelegatedAuthenticationProcessor delegAuthnProcessor;
	List<SOAPHandler> soapHandlers;
	SOAPHandler handler = new SOAPv12Handler();
	Logger logger = LoggerFactory.getLogger(this.getClass());
	private Startup startup;
	
	@Before
	public void setUp() throws Exception 
	{
		this.attribProcessor = createMock(AttributeAuthorityProcessor.class);
		this.authProcessor = createMock(AuthorizationProcessor.class);
		this.spepProcessor = createMock(SPEPProcessor.class);
		this.startup = createMock(Startup.class);
		this.delegAuthnProcessor = createMock(DelegatedAuthenticationProcessor.class);
		this.soapHandlers = new ArrayList<SOAPHandler>();
		this.soapHandlers.add(this.handler);

		expect(this.spepProcessor.getStartup()).andReturn(this.startup).anyTimes();
	}
	
	private void startMock()
	{
		replay(this.attribProcessor);
		replay(this.authProcessor);
		replay(this.spepProcessor);
		replay(this.delegAuthnProcessor);
	}
	
	private void endMock()
	{
		verify(this.attribProcessor);
		verify(this.authProcessor);
		verify(this.spepProcessor);
		verify(this.delegAuthnProcessor);
	}

	/** Test construction.
	 * 
	 *
	 */
	@Test
	public void testWSProcessorImpl1()
	{
		this.processor = new WSProcessorImpl(this.attribProcessor, this.authProcessor, this.spepProcessor, this.delegAuthnProcessor, this.soapHandlers);
	}

	/* Test case for attribute auth processor returning success.
	 * 
	 */
	@Test
	public void testAttributeAuthority1() throws Exception
	{
		final Element samlResponse = generateSAMLResponse();
		Modify<AAProcessorData> modify = new Modify<AAProcessorData>(){
			public void operate(AAProcessorData object)
			{
				object.setResponseDocument(samlResponse);
			}
		};
		
		expect(this.attribProcessor.execute(Modify.modify(modify))).andReturn(AttributeAuthorityProcessor.result.Successful).anyTimes();
		startMock();
		
		this.processor = new WSProcessorImpl(this.attribProcessor, this.authProcessor, this.spepProcessor, this.delegAuthnProcessor, this.soapHandlers);
		
		// SETUP the mocked data types. As we are mocking the attributeAuthorityProcessor it will
		// not be possible to test all paths of execution because the mocked processor will never
		// set the AAProcessorData.responseDocument. Ie: it will always throw an AxisFault.
		
		// attrib processor returns success. Test normal execution path.
		
		byte[] request = generateSOAPRequest();
		
		this.logger.error(new String(request, "UTF-16"));
		this.processor.attributeAuthority(request, SOAPv12Handler.SOAP12_CONTENT_TYPE);
		
		endMock();
	}

	private byte[] generateSOAPRequest() throws Exception
	{
		Marshaller<AttributeQuery> marshaller = new MarshallerImpl<AttributeQuery>(AttributeQuery.class.getPackage().getName(), new String[]{SchemaConstants.samlProtocol});
		AttributeQuery attributeQuery = new AttributeQuery();
		attributeQuery.setID("_" + String.valueOf(System.currentTimeMillis()));
		Subject subject = new Subject();
		NameIDType nameID = new NameIDType();
		nameID.setFormat(NameIDFormatConstants.emailAddress);
		nameID.setValue("a@b.com");
		subject.setNameID(nameID);
		attributeQuery.setSubject(subject);
		Element samlDocument = marshaller.marshallUnSignedElement(attributeQuery);
		byte[] request = this.handler.wrapDocument(samlDocument);
		
		return request;
	}

	/* Test case for attribute auth processor returning failure.
	 * 
	 */
	@Test(expected = WSProcessorException.class)
	public void testAttributeAuthority2() throws Exception
	{		
		// attrib processor returns success. Test first exception execution path.
		expect(this.attribProcessor.execute((AAProcessorData)anyObject())).andThrow(new InvalidPrincipalException("Invalid principal dude"));
	
		startMock();
		this.processor = new WSProcessorImpl(this.attribProcessor, this.authProcessor, this.spepProcessor, this.delegAuthnProcessor, this.soapHandlers);
		
		// SETUP the mocked data types. As we are mocking the attributeAuthorityProcessor it will
		// not be possible to test all paths of execution because the mocked processor will never
		// set the AAProcessorData.responseDocument. Ie: it will always throw an AxisFault.
		
		byte[] request = generateSOAPRequest();
		this.processor.attributeAuthority(request, SOAPv12Handler.SOAP12_CONTENT_TYPE);
		
		endMock();
	}

	
	/* Test case for attribute authority processor throwing invalid request exception.
	 */
	@Test(expected = WSProcessorException.class)
	public void testAttributeAuthority3() throws Exception
	{		
		// attrib processor returns success. Test second exception execution path.
		expect(this.attribProcessor.execute((AAProcessorData)anyObject())).andThrow(new InvalidRequestException("Invalid request recieved")).once();

		startMock();
		
		this.processor = new WSProcessorImpl(this.attribProcessor, this.authProcessor, this.spepProcessor, this.delegAuthnProcessor, this.soapHandlers);
		
		// SETUP the mocked data types. As we are mocking the attributeAuthorityProcessor it will
		// not be possible to test all paths of execution because the mocked processor will never
		// set the AAProcessorData.responseDocument. Ie: it will always throw an AxisFault.
		
		byte[] request = generateSOAPRequest();
		this.processor.attributeAuthority(request, SOAPv12Handler.SOAP12_CONTENT_TYPE);

		endMock();
	}

	
	/* Test case for PDP processor returning success.
	 * 
	 */
	@Test
	public void testPolicyDecisionPoint1() throws Exception
	{
		final Element samlResponse = generateSAMLResponse();
		Modify<AuthorizationProcessorData> modify = new Modify<AuthorizationProcessorData>(){
			public void operate(AuthorizationProcessorData object)
			{
				object.setResponseDocument(samlResponse);
			}
		};
		
		expect(this.authProcessor.execute(Modify.modify(modify))).andReturn(AuthorizationProcessor.result.Successful).anyTimes();
	
		startMock();
		this.processor = new WSProcessorImpl(this.attribProcessor, this.authProcessor, this.spepProcessor, this.delegAuthnProcessor, this.soapHandlers);
		
		// SETUP the mocked data types. As we are mocking the authorizationProcessor it will
		// not be possible to test all paths of execution because the mocked processor will never
		// set the AuthorizationProcessorData.responseDocument. Ie: it will always throw an AxisFault.
		
		byte[] request = generateSOAPRequest();
		this.processor.policyDecisionPoint(request, SOAPv12Handler.SOAP12_CONTENT_TYPE);
		
		endMock();
	}

	
	/* Test case for PDP processor throwinf invalid request exception.
	 */
	@Test(expected = WSProcessorException.class)
	public void testPolicyDecisionPoint2() throws Exception
	{
		// attrib processor returns success. Test normal execution path.
		expect(this.authProcessor.execute((AuthorizationProcessorData)anyObject())).andThrow(new com.qut.middleware.esoe.authz.exception.InvalidRequestException("Invalid Request dude"));

		startMock();
		
		this.processor = new WSProcessorImpl(this.attribProcessor, this.authProcessor, this.spepProcessor, this.delegAuthnProcessor, this.soapHandlers);
		
		// SETUP the mocked data types. As we are mocking the authorizationProcessor it will
		// not be possible to test all paths of execution because the mocked processor will never
		// set the AuthorizationProcessorData.responseDocument. Ie: it will always throw an AxisFault.
		
		byte[] request = generateSOAPRequest();
		this.processor.policyDecisionPoint(request, SOAPv12Handler.SOAP12_CONTENT_TYPE);
		
		endMock();
	}
	
	@Test
	public void testSpepStartup() throws Exception
	{
		final Element samlResponse = generateSAMLResponse();
		Modify<SPEPProcessorData> modify = new Modify<SPEPProcessorData>(){
			public void operate(SPEPProcessorData object)
			{
				object.setResponseDocument(samlResponse);
			}
		};
		
		this.startup.registerSPEPStartup(Modify.modify(modify));
		expectLastCall().anyTimes();
		
		startMock();
		
		byte[] request = generateSOAPRequest();
		this.processor = new WSProcessorImpl(this.attribProcessor, this.authProcessor, this.spepProcessor, this.delegAuthnProcessor, this.soapHandlers);
		
		this.processor.spepStartup(request, SOAPv12Handler.SOAP12_CONTENT_TYPE);
	}

	@Test
	public void testRegisterPrincipal() throws Exception
	{
		final Element samlResponse = generateSAMLResponse();
		Modify<DelegatedAuthenticationData> modify = new Modify<DelegatedAuthenticationData>()
		{
			public void operate(DelegatedAuthenticationData object)
			{
				object.setResponseDocument(samlResponse);
			}
		};
		
		expect(this.delegAuthnProcessor.execute(Modify.modify(modify))).andReturn(result.Successful).anyTimes();
		startMock();
		
		byte[] request = generateSOAPRequest();
		this.processor = new WSProcessorImpl(this.attribProcessor, this.authProcessor, this.spepProcessor, this.delegAuthnProcessor, this.soapHandlers);
		
		this.processor.registerPrincipal(request, SOAPv12Handler.SOAP12_CONTENT_TYPE);
	}
	
	public Element generateSAMLResponse() throws Exception
	{
		String[] schemas = new String[]{SchemaConstants.samlProtocol, SchemaConstants.samlAssertion};
		String packageNames = Response.class.getPackage().getName() + ":" + Assertion.class.getPackage().getName();
		
		Marshaller<Response> marshaller = new MarshallerImpl<Response>(packageNames, schemas);
		Response response = new Response();
		response.setID("_" + (System.currentTimeMillis() - 2));
		
		Assertion assertion = new Assertion();
		assertion.setID("_" + (System.currentTimeMillis() - 1));

		Subject subject = new Subject();
		NameIDType nameID = new NameIDType();
		nameID.setFormat(NameIDFormatConstants.emailAddress);
		nameID.setValue("a@b.com");
		subject.setNameID(nameID);
		assertion.setSubject(subject);
		
		AttributeStatement attributeStatement = new AttributeStatement();
		assertion.getAuthnStatementsAndAuthzDecisionStatementsAndAttributeStatements().add(attributeStatement);

		response.getEncryptedAssertionsAndAssertions().add(assertion);
		
		return marshaller.marshallUnSignedElement(response);
	}
}

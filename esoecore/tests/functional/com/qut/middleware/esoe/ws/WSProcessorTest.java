package com.qut.middleware.esoe.ws;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.fail;

import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.esoe.aa.AttributeAuthorityProcessor;
import com.qut.middleware.esoe.aa.bean.AAProcessorData;
import com.qut.middleware.esoe.aa.exception.InvalidPrincipalException;
import com.qut.middleware.esoe.aa.exception.InvalidRequestException;
import com.qut.middleware.esoe.authz.AuthorizationProcessor;
import com.qut.middleware.esoe.authz.bean.AuthorizationProcessorData;
import com.qut.middleware.esoe.delegauthn.DelegatedAuthenticationProcessor;
import com.qut.middleware.esoe.spep.SPEPProcessor;
import com.qut.middleware.esoe.ws.impl.WSProcessorImpl;

@SuppressWarnings("nls")
public class WSProcessorTest
{

	WSProcessor processor;
	AttributeAuthorityProcessor attribProcessor;
	AuthorizationProcessor authProcessor;
	SPEPProcessor spepProcessor;
	DelegatedAuthenticationProcessor delegAuthnProcessor;
	
	@Before
	public void setUp() throws Exception 
	{
		this.attribProcessor = createMock(AttributeAuthorityProcessor.class);
		this.authProcessor = createMock(AuthorizationProcessor.class);
		this.spepProcessor = createMock(SPEPProcessor.class);
		this.delegAuthnProcessor = createMock(DelegatedAuthenticationProcessor.class);
	}

	/** Test construction.
	 * 
	 *
	 */
	@Test
	public void testWSProcessorImpl1()
	{
		this.processor = new WSProcessorImpl(this.attribProcessor, this.authProcessor, this.spepProcessor, this.delegAuthnProcessor);
	}

	/* Test case for attribute auth processor returning success.
	 * 
	 */
	@Test
	public void testAttributeAuthority1()
	{		
		this.processor = new WSProcessorImpl(this.attribProcessor, this.authProcessor, this.spepProcessor, this.delegAuthnProcessor);
		
		// SETUP the mocked data types. As we are mocking the attributeAuthorityProcessor it will
		// not be possible to test all paths of execution because the mocked processor will never
		// set the AAProcessorData.responseDocument. Ie: it will always throw an AxisFault.
		
		// attrib processor returns success. Test normal execution path.
		try
		{			
			expect(this.attribProcessor.execute((AAProcessorData)anyObject())).andReturn(AttributeAuthorityProcessor.result.Successful).anyTimes();
		
			replayMocks();
				
			StringReader reader = new StringReader("<test>hello</test>");

			XMLInputFactory xif = XMLInputFactory.newInstance();
			XMLStreamReader xmlreader = xif.createXMLStreamReader(reader);

			StAXOMBuilder builder = new StAXOMBuilder(xmlreader);
			OMElement request = builder.getDocumentElement();

			this.processor.attributeAuthority(request);
		}
		catch (AxisFault f)
		{
			//f.printStackTrace();
		}
		catch(Exception e)
		{
			//e.printStackTrace();
			fail("Unexpected exception occured during service call.");
		}
	}

	/* Test case for attribute auth processor returning failure.
	 * 
	 */
	@Test
	public void testAttributeAuthority2()
	{		
		this.processor = new WSProcessorImpl(this.attribProcessor, this.authProcessor, this.spepProcessor, this.delegAuthnProcessor);
		
		// SETUP the mocked data types. As we are mocking the attributeAuthorityProcessor it will
		// not be possible to test all paths of execution because the mocked processor will never
		// set the AAProcessorData.responseDocument. Ie: it will always throw an AxisFault.
		
		// attrib processor returns success. Test first exception execution path.
		try
		{			
			expect(this.attribProcessor.execute((AAProcessorData)anyObject())).andThrow(new InvalidPrincipalException("Invalid principal dude"));
		
			replayMocks();
				
			StringReader reader = new StringReader("<test>hello</test>");

			XMLInputFactory xif = XMLInputFactory.newInstance();
			XMLStreamReader xmlreader = xif.createXMLStreamReader(reader);

			StAXOMBuilder builder = new StAXOMBuilder(xmlreader);
			OMElement request = builder.getDocumentElement();

			this.processor.attributeAuthority(request);
		}
		catch (AxisFault f)
		{
			//f.printStackTrace();
		}
		catch(Exception e)
		{
			//e.printStackTrace();
			fail("Unexpected exception occured during service call.");
		}
	}

	
	/* Test case for attribute authority processor throwing invalid request exception.
	 */
	@Test
	public void testAttributeAuthority3()
	{		
		this.processor = new WSProcessorImpl(this.attribProcessor, this.authProcessor, this.spepProcessor, this.delegAuthnProcessor);
		
		// SETUP the mocked data types. As we are mocking the attributeAuthorityProcessor it will
		// not be possible to test all paths of execution because the mocked processor will never
		// set the AAProcessorData.responseDocument. Ie: it will always throw an AxisFault.
		
		// attrib processor returns success. Test second exception execution path.
		try
		{			
			expect(this.attribProcessor.execute((AAProcessorData)anyObject())).andThrow(new InvalidRequestException("Invalid request recieved")).once();
		
			replayMocks();
				
			StringReader reader = new StringReader("<test>hello</test>");

			XMLInputFactory xif = XMLInputFactory.newInstance();
			XMLStreamReader xmlreader = xif.createXMLStreamReader(reader);

			StAXOMBuilder builder = new StAXOMBuilder(xmlreader);
			OMElement request = builder.getDocumentElement();

			this.processor.attributeAuthority(request);
		}
		catch (AxisFault f)
		{
			//f.printStackTrace();
		}
		catch(Exception e)
		{
			//e.printStackTrace();
			fail("Unexpected exception occured during service call.");
		}
	}

	
	/* Test case for PDP processor returning success.
	 * 
	 */
	//@Test
	public void testPolicyDecisionPoint1()
	{
		this.processor = new WSProcessorImpl(this.attribProcessor, this.authProcessor, this.spepProcessor, this.delegAuthnProcessor);
		
		// SETUP the mocked data types. As we are mocking the authorizationProcessor it will
		// not be possible to test all paths of execution because the mocked processor will never
		// set the AuthorizationProcessorData.responseDocument. Ie: it will always throw an AxisFault.
		
		// attrib processor returns success. Test normal execution path.
		try
		{			
			expect(this.authProcessor.execute((AuthorizationProcessorData)anyObject())).andReturn(AuthorizationProcessor.result.Successful).anyTimes();
		
			replayMocks();
				
			StringReader reader = new StringReader("<test>hello</test>");

			XMLInputFactory xif = XMLInputFactory.newInstance();
			XMLStreamReader xmlreader = xif.createXMLStreamReader(reader);

			StAXOMBuilder builder = new StAXOMBuilder(xmlreader);
			OMElement request = builder.getDocumentElement();

			this.processor.policyDecisionPoint(request);
		}
		catch (AxisFault f)
		{
			//f.printStackTrace();
		}
		catch(Exception e)
		{
			//e.printStackTrace();
			fail("Unexpected exception occured during service call.");
		}
	}

	
	/* Test case for PDP processor throwinf invalid request exception.
	 */
	@Test
	public void testPolicyDecisionPoint2()
	{
		this.processor = new WSProcessorImpl(this.attribProcessor, this.authProcessor, this.spepProcessor, this.delegAuthnProcessor);
		
		// SETUP the mocked data types. As we are mocking the authorizationProcessor it will
		// not be possible to test all paths of execution because the mocked processor will never
		// set the AuthorizationProcessorData.responseDocument. Ie: it will always throw an AxisFault.
		
		// attrib processor returns success. Test normal execution path.
		try
		{			
			expect(this.authProcessor.execute((AuthorizationProcessorData)anyObject())).andThrow(new com.qut.middleware.esoe.authz.exception.InvalidRequestException("Invalid Request dude"));
		
			replayMocks();
				
			StringReader reader = new StringReader("<test>hello</test>");

			XMLInputFactory xif = XMLInputFactory.newInstance();
			XMLStreamReader xmlreader = xif.createXMLStreamReader(reader);

			StAXOMBuilder builder = new StAXOMBuilder(xmlreader);
			OMElement request = builder.getDocumentElement();

			this.processor.policyDecisionPoint(request);
		}
		catch (AxisFault f)
		{
			//f.printStackTrace();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail("Unexpected exception occured during service call.");
		}
	}
	
	//@Test
	public void testSpepStartup() {
		fail("Not yet implemented");
	}

	//@Test
	public void testRegisterPrincipal() {
		fail("Not yet implemented");
	}

	
	private void replayMocks()
	{
		replay(this.attribProcessor);
		replay(this.authProcessor);
		replay(this.spepProcessor);
		replay(this.delegAuthnProcessor);
	}
}

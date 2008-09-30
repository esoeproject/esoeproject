/* Copyright 2008, Queensland University of Technology
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
 * Creation Date: 19/09/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.saml2.handler;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;

import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3._2000._09.xmldsig_.Signature;
import org.w3c.dom.Element;

import com.qut.middleware.saml2.ExternalKeyResolver;
import com.qut.middleware.saml2.LocalKeyResolver;
import com.qut.middleware.saml2.SchemaConstants;
import com.qut.middleware.saml2.VersionConstants;
import com.qut.middleware.saml2.exception.SOAPException;
import com.qut.middleware.saml2.handler.SOAPHandler.FaultCode;
import com.qut.middleware.saml2.handler.impl.MarshallerImpl;
import com.qut.middleware.saml2.handler.impl.SOAPv11Handler;
import com.qut.middleware.saml2.handler.impl.SOAPv12Handler;
import com.qut.middleware.saml2.handler.impl.UnmarshallerImpl;
import com.qut.middleware.saml2.schemas.protocol.Response;
import com.qut.middleware.saml2.schemas.protocol.Status;
import com.qut.middleware.saml2.schemas.protocol.StatusCode;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

public class SOAPv12HandlerTest
{
	private SOAPHandler handler;
	private Marshaller<Response> responseMarshaller;
	private UnmarshallerImpl<Response> responseUnmarshaller;

	private String responseID = "_1234567890-abcdefghijklmnopqrstuvwxyz";
	private String statusMessage = "Status message";
	private String statusCodeValue = "status-code";
	private String faultReason = "fault-reason";

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Before
	public void setUp() throws Exception
	{
		String path = System.getProperty("user.dir") + File.separator + "tests" + File.separator + "testdata" + File.separator;

		KeyStore ks = KeyStore.getInstance("PKCS12");
		FileInputStream fis = new FileInputStream(path + "tests.ks");
		char[] passwd = { 't', 'e', 's', 't', 'p', 'a', 's', 's' };
		ks.load(fis, passwd);

		String keyAlias = "myrsakey";

		PrivateKey privKey = (PrivateKey) ks.getKey(keyAlias, passwd);
		X509Certificate cert = (X509Certificate)ks.getCertificate(keyAlias);
		PublicKey pk = cert.getPublicKey();

		LocalKeyResolver localKeyResolver = createMock(LocalKeyResolver.class);
		expect(localKeyResolver.getLocalCertificate()).andReturn(cert).anyTimes();
		expect(localKeyResolver.getLocalKeyAlias()).andReturn(keyAlias).anyTimes();
		expect(localKeyResolver.getLocalPrivateKey()).andReturn(privKey).anyTimes();
		expect(localKeyResolver.getLocalPublicKey()).andReturn(pk).anyTimes();

		replay(localKeyResolver);
		
		ExternalKeyResolver externalKeyResolver = createMock(ExternalKeyResolver.class);
		expect(externalKeyResolver.resolveKey(keyAlias)).andReturn(pk).anyTimes();
		expect(externalKeyResolver.resolveKey(cert.getIssuerDN().getName(), cert.getSerialNumber())).andReturn(pk).anyTimes();
		
		replay(externalKeyResolver);

		this.handler = new SOAPv12Handler();
		String[] schemas = new String[] { SchemaConstants.samlProtocol };
		this.responseMarshaller = new MarshallerImpl<Response>(Response.class.getPackage().getName(), schemas, localKeyResolver);
		this.responseUnmarshaller = new UnmarshallerImpl<Response>(Response.class.getPackage().getName(), schemas, externalKeyResolver);
	}

	@Test
	public void testCanHandle()
	{
		assertFalse(this.handler.canHandle(SOAPv11Handler.SOAP11_CONTENT_TYPE + "; charset=utf-16"));
		assertTrue(this.handler.canHandle(SOAPv12Handler.SOAP12_CONTENT_TYPE + "; charset=utf-16"));
	}

	@Test
	public void testWrapDocument() throws Exception
	{
		Response response = new Response();
		response.setID(this.responseID);
		response.setVersion(VersionConstants.saml20);
		response.setIssueInstant(this.generateXMLCalendar(0));
		
		response.setSignature(new Signature());

		Status status = new Status();
		status.setStatusMessage(this.statusMessage);

		StatusCode statusCode = new StatusCode();
		statusCode.setValue(this.statusCodeValue);
		status.setStatusCode(statusCode);

		response.setStatus(status);
		Element element = this.responseMarshaller.marshallSignedElement(response);

		byte[] document = this.handler.wrapDocument(element);

		this.logger.debug(new String(document, "UTF-16"));
	}

	@Test
	public void testWrapNull() throws Exception
	{
		byte[] document = this.handler.wrapDocument(null);

		this.logger.debug(new String(document, "UTF-16"));
	}

	@Test
	public void testGenerateFaultResponse() throws Exception
	{
		this.logger.debug(new String(this.handler.generateFaultResponse(this.faultReason, FaultCode.Receiver, null, null, "UTF-16"), "UTF-16"));
	}

	/*
	 * Unwrap tests below.
	 * 
	 * These tests will most likely fail if the above tests fail. If the above tests fail, investigate the problem there
	 * before worrying about these ones.
	 */

	@Test
	public void testUnwrapDocument() throws Exception
	{

		byte[] document;
		{		
			Response response = new Response();
			response.setID(this.responseID);
			response.setVersion(VersionConstants.saml20);
			response.setIssueInstant(this.generateXMLCalendar(0));
			response.setSignature(new Signature());
			Status status = new Status();
			status.setStatusMessage(this.statusMessage);
			StatusCode statusCode = new StatusCode();
			statusCode.setValue(this.statusCodeValue);
			status.setStatusCode(statusCode);
			response.setStatus(status);

			Element element = this.responseMarshaller.marshallSignedElement(response);
			document = this.handler.wrapDocument(element);
		}
		
		Element element = this.handler.unwrapDocument(document);
		
		Response response = this.responseUnmarshaller.unMarshallSigned(element);
		
		assertEquals(this.responseID, response.getID());
		assertEquals(this.statusMessage, response.getStatus().getStatusMessage());
		assertEquals(this.statusCodeValue, response.getStatus().getStatusCode().getValue());
	}
	
	@Test
	public void testBidirectionalElementMarshalling() throws Exception
	{
		Response response = new Response();
		response.setID(this.responseID);
		response.setVersion(VersionConstants.saml20);
		response.setIssueInstant(this.generateXMLCalendar(0));
		response.setSignature(new Signature());
		Status status = new Status();
		status.setStatusMessage(this.statusMessage);
		StatusCode statusCode = new StatusCode();
		statusCode.setValue(this.statusCodeValue);
		status.setStatusCode(statusCode);
		response.setStatus(status);

		Element element = this.responseMarshaller.marshallSignedElement(response);
		this.responseUnmarshaller.unMarshallSigned(element);
	}

	@Test(expected = SOAPException.class)
	public void testUnwrapFaultDocument1() throws Exception
	{
		byte[] document = this.handler.generateFaultResponse(this.faultReason, FaultCode.Receiver, null, null, SOAPv12Handler.SOAP12_DEFAULT_ENCODING);

		this.handler = new SOAPv12Handler();

		this.handler.unwrapDocument(document);
	}

	@Test
	public void testUnwrapFaultDocument2() throws Exception
	{
		try
		{
			byte[] document = this.handler.generateFaultResponse(this.faultReason, FaultCode.Receiver, null, null, SOAPv12Handler.SOAP12_DEFAULT_ENCODING);
	
			this.handler = new SOAPv12Handler();
	
			this.handler.unwrapDocument(document);
			
			fail("No exception thrown when unwrapping fault document");
		}
		catch (SOAPException e)
		{
			assertTrue(e.getMessage(), e.isFault());
			assertEquals(FaultCode.Receiver.name(), e.getFaultCode());
			assertEquals(this.faultReason, e.getFaultMessage());
			assertTrue(e.getFaultDetail() == null || e.getFaultDetail().size() == 0);
		}
	}

	@Test(expected = SOAPException.class)
	public void testUnwrapNull() throws Exception
	{
		byte[] document = this.handler.wrapDocument(null);

		this.handler = new SOAPv12Handler();

		this.handler.unwrapDocument(document);
	}

	private XMLGregorianCalendar generateXMLCalendar(int offset)
	{
		SimpleTimeZone gmt;
		GregorianCalendar calendar;
		XMLGregorianCalendar xmlCalendar;

		/* GMT timezone TODO: I am sure this isn't correct need to ensure we set GMT */
		// GMT or UTC ??
		gmt = new SimpleTimeZone(0, "UTC");
		calendar = new GregorianCalendar(gmt);
		calendar.add(Calendar.MILLISECOND, offset);
		xmlCalendar = new XMLGregorianCalendarImpl(calendar);

		return xmlCalendar;
	}
}

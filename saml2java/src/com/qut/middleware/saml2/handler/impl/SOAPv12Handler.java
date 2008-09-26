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
 * Creation Date: 18/09/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.saml2.handler.impl;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.qut.middleware.saml2.SchemaConstants;
import com.qut.middleware.saml2.exception.MarshallerException;
import com.qut.middleware.saml2.exception.SOAPException;
import com.qut.middleware.saml2.exception.UnmarshallerException;
import com.qut.middleware.saml2.handler.Marshaller;
import com.qut.middleware.saml2.handler.SOAPHandler;
import com.qut.middleware.saml2.handler.Unmarshaller;
import com.qut.middleware.saml2.namespace.NamespacePrefixMapperImpl;
import com.qut.middleware.saml2.schemas.soap.v12.Body;
import com.qut.middleware.saml2.schemas.soap.v12.Detail;
import com.qut.middleware.saml2.schemas.soap.v12.Envelope;
import com.qut.middleware.saml2.schemas.soap.v12.Fault;
import com.qut.middleware.saml2.schemas.soap.v12.Faultcode;
import com.qut.middleware.saml2.schemas.soap.v12.Faultreason;
import com.qut.middleware.saml2.schemas.soap.v12.Reasontext;
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

public class SOAPv12Handler implements SOAPHandler
{
	public static final String SOAP12_CONTENT_TYPE = "application/soap+xml";
	public static final String SOAP12_DEFAULT_ENCODING = "utf-16";
	public static final String SOAP12_SCHEMA_URI = "http://www.w3.org/2003/05/soap-envelope";

	private Marshaller<Envelope> envelopeMarshaller;
	private Unmarshaller<Envelope> envelopeUnmarshaller;
	private Marshaller<Fault> faultMarshaller;
	private Unmarshaller<Fault> faultUnmarshaller;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public SOAPv12Handler()
	{
		try
		{
			String[] schema = new String[]{SchemaConstants.soapv12};
			this.envelopeMarshaller = new MarshallerImpl<Envelope>(Envelope.class.getPackage().getName(), schema);
			this.envelopeUnmarshaller = new UnmarshallerImpl<Envelope>(Envelope.class.getPackage().getName(), schema);
			this.faultMarshaller = new MarshallerImpl<Fault>(Fault.class.getPackage().getName(), schema);
			this.faultUnmarshaller = new UnmarshallerImpl<Fault>(Fault.class.getPackage().getName(), schema);
		}
		catch (MarshallerException e)
		{
			this.logger.debug("Exception occurred trying to initialize marshallers for SOAP v1.2 handler.", e);
			throw new IllegalArgumentException("Exception occurred trying to initialize marshallers for SOAP v1.2 handler. Error was: " + e.getMessage());
		}
		catch (UnmarshallerException e)
		{
			this.logger.debug("Exception occurred trying to initialize unmarshallers for SOAP v1.2 handler.", e);
			throw new IllegalArgumentException("Exception occurred trying to initialize unmarshallers for SOAP v1.2 handler. Error was: " + e.getMessage());
		}
	}
	
	public boolean canHandle(String contentType)
	{
		this.logger.debug("Checking ability to handle content type '{}'. Type expected by this handler is '{}'", contentType, SOAP12_CONTENT_TYPE);
		return contentType.startsWith(SOAP12_CONTENT_TYPE);
	}

	public byte[] wrapDocument(Element samlDocument) throws SOAPException
	{
		return this.wrapDocument(samlDocument, SOAPv12Handler.SOAP12_DEFAULT_ENCODING);
	}
	
	public byte[] wrapDocument(Element samlDocument, String encoding) throws SOAPException
	{
		try
		{
			Body body = new Body();
			body.getAnies().add(samlDocument);
			
			Envelope envelope = new Envelope();
			envelope.setBody(body);
			
			return this.envelopeMarshaller.marshallUnSigned(envelope);
		}
		catch (MarshallerException e)
		{
			this.logger.debug("Exception occurred marshalling the document into a SOAP envelope. Error was: " + e.getMessage(), e);
			throw new SOAPException("Exception occurred marshalling the document into a SOAP envelope. Error was: " + e.getMessage(), e);
		}
	}

	public byte[] generateFaultResponse(String reason, FaultCode faultCodeValue, String subCodeValue, Collection<Element> detailElements, String encoding) throws SOAPException
	{
		this.logger.debug("Creating SOAP fault with reason: {}", reason);
		try
		{
			NamespacePrefixMapper mapper = new NamespacePrefixMapperImpl();
			
			Body body = new Body();
			Fault fault = new Fault();
			
			Faultreason faultreason = new Faultreason();
			Reasontext reasontext = new Reasontext();
			reasontext.setValue(reason);
			reasontext.setLang(Locale.getDefault().getLanguage());
			faultreason.getTexts().add(reasontext);
			fault.setReason(faultreason);
			
			Faultcode faultcode = new Faultcode();
			String soapPrefix = mapper.getPreferredPrefix(SOAP12_SCHEMA_URI, null, true);
			faultcode.setValue(new QName(SOAP12_SCHEMA_URI, faultCodeValue.name(), soapPrefix));
			fault.setCode(faultcode);
			
			if (detailElements != null && detailElements.size() > 0)
			{
				Detail detail = new Detail();
				
				for (Element detailElement : detailElements)
				{
					detail.getAnies().add(detailElement);
				}
				
				fault.setDetail(detail);
			}

			this.logger.debug("Creating SOAP fault - Marshalling fault element to insert into header");
			body.getAnies().add(this.faultMarshaller.marshallUnSignedElement(fault));
			this.logger.debug("Creating SOAP fault - Finished marshalling fault element");

			Envelope envelope = new Envelope();
			envelope.setBody(body);

			this.logger.debug("Creating SOAP fault - Going to marshal fault envelope");
			return this.envelopeMarshaller.marshallUnSigned(envelope, encoding);
		}
		catch (MarshallerException e)
		{
			this.logger.debug("Exception occurred while marshalling fault response. Exception follows", e);
			throw new SOAPException("Exception occurred while marshalling fault response. Error was: " + e.getMessage(), e);
		}
	}

	public Element unwrapDocument(byte[] soapDocument) throws SOAPException
	{
		try
		{
			Envelope envelope = this.envelopeUnmarshaller.unMarshallUnSigned(soapDocument);
			Body body = envelope.getBody();
			
			Element result = null;
			for (Element element : body.getAnies())
			{
				// We only expect one element in the body, so grab any non-null element
				// Check for faults on the way through.
				if (element != null)
				{
					if (element.getLocalName().equals("Fault"))
					{
						this.processErrorBody(body);
					}
					
					result = element;
				}
			}
			
			if (result != null) return result;
		}
		catch (UnmarshallerException e)
		{
			this.logger.debug("Exception occurred while unmarshalling request document.", e);
			throw new SOAPException("Exception occurred while unmarshalling request document. Error was: " + e.getMessage(), e);
		}
		
		this.logger.debug("SOAP body present but no document element. Throwing.");
		throw new SOAPException("SOAP body was present but no document element contained within. Unable to process.");
	}

	private void processErrorBody(Body body) throws SOAPException
	{
		if (body != null)
		{
			try
			{
				for (Element element : body.getAnies())
				{
					if (element.getLocalName().equals("Fault"))
					{
						this.logger.debug("Unwrapping SOAP body, got fault element. Namespace {}  local name {}", element.getNamespaceURI(), element.getLocalName());
						Fault fault = this.faultUnmarshaller.unMarshallUnSigned(element);
						
						String faultCode = fault.getCode().getValue().getLocalPart();
						Faultreason reason = fault.getReason();

						if (reason == null)
						{
							throw new SOAPException("SOAP Fault occurred. No Faultreason to explain the error.");
						}
						String reasons = "SOAP Fault occurred. Faultreason was:";
						for (Reasontext reasontext : reason.getTexts())
						{
							reasons = reasons + "\n[lang: " + String.valueOf(reasontext.getLang()) + "] " + String.valueOf(reasontext.getValue());
						}
						
						List<Element> details = fault.getDetail().getAnies();

						throw new SOAPException(faultCode, reasons, details);
					}
				}
			}
			catch (UnmarshallerException e)
			{
				this.logger.debug("Unmarshaller exception occurred trying to unwrap a SOAP fault. Unable to continue processing erroneous envelope.", e);
				throw new SOAPException("SOAP Fault occurred (presumably), but unmarshalling the Fault failed, so no further information is available. Unmarshalling error was: " + e.getMessage(), e);
			}
		}
		throw new SOAPException("No content and no SOAP fault was sent with the SOAP envelope. Unable to process.");
	}
	
	public String getContentType(String encoding)
	{
		return MessageFormat.format("{0}; charset={1}", SOAPv12Handler.SOAP12_CONTENT_TYPE, encoding);
	}
	
	public String getDefaultEncoding()
	{
		return SOAPv12Handler.SOAP12_DEFAULT_ENCODING;
	}
}

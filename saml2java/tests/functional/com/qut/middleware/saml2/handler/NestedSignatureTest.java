package com.qut.middleware.saml2.handler;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;

import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Test;
import org.w3._2000._09.xmldsig_.Signature;
import org.w3c.dom.Element;

import com.qut.middleware.saml2.ExternalKeyResolver;
import com.qut.middleware.saml2.LocalKeyResolver;
import com.qut.middleware.saml2.SchemaConstants;
import com.qut.middleware.saml2.StatusCodeConstants;
import com.qut.middleware.saml2.VersionConstants;
import com.qut.middleware.saml2.handler.impl.MarshallerImpl;
import com.qut.middleware.saml2.handler.impl.UnmarshallerImpl;
import com.qut.middleware.saml2.schemas.protocol.ArtifactResponse;
import com.qut.middleware.saml2.schemas.protocol.Response;
import com.qut.middleware.saml2.schemas.protocol.Status;
import com.qut.middleware.saml2.schemas.protocol.StatusCode;
import com.qut.middleware.saml2.schemas.soap.v12.Body;
import com.qut.middleware.saml2.schemas.soap.v12.Envelope;


public class NestedSignatureTest {

	private String keyAlias;
	private PrivateKey privKey;
	private PublicKey pk;
	private LocalKeyResolver localKeyResolver;
	private ExternalKeyResolver externalKeyResolver;

	public NestedSignatureTest() throws Exception
	{
		KeyStore ks = KeyStore.getInstance("PKCS12");
		FileInputStream fis = new FileInputStream("tests/testdata/tests.ks");
		char[] passwd = { 't', 'e', 's', 't', 'p', 'a', 's', 's' };
		ks.load(fis, passwd);

		keyAlias = "myrsakey";

		privKey = (PrivateKey) ks.getKey(keyAlias, passwd);
		Certificate cert = ks.getCertificate(keyAlias);
		pk = cert.getPublicKey();

		localKeyResolver = createMock(LocalKeyResolver.class);
		expect(localKeyResolver.getLocalCertificate()).andReturn(cert).anyTimes();
		expect(localKeyResolver.getLocalKeyAlias()).andReturn(keyAlias).anyTimes();
		expect(localKeyResolver.getLocalPrivateKey()).andReturn(privKey).anyTimes();
		expect(localKeyResolver.getLocalPublicKey()).andReturn(pk).anyTimes();
		replay(localKeyResolver);

		externalKeyResolver = createMock(ExternalKeyResolver.class);
		expect(externalKeyResolver.resolveKey((String)notNull())).andReturn(pk).anyTimes();
		expect(externalKeyResolver.resolveKey((String)notNull(), (BigInteger)notNull())).andReturn(pk).anyTimes();
		replay(externalKeyResolver);
	}

	@Test
	public void testSignature() throws Exception
	{
		String packages = Response.class.getPackage().getName();
		String[] schemas = new String[]{SchemaConstants.samlProtocol};
		String soapPackage = Envelope.class.getPackage().getName();
		String[] soapSchemas = new String[]{SchemaConstants.soapv12};

		Marshaller<Response> marshaller = new MarshallerImpl<Response>(packages, schemas, this.localKeyResolver);
		Marshaller<ArtifactResponse> artifactMarshaller = (Marshaller)marshaller;
		Unmarshaller<Response> unmarshaller = new UnmarshallerImpl<Response>(packages, schemas, this.externalKeyResolver);
		Unmarshaller<ArtifactResponse> artifactUnmarshaller = (Unmarshaller)unmarshaller;
		Marshaller<Envelope> soapMarshaller = new MarshallerImpl<Envelope>(soapPackage, soapSchemas, this.localKeyResolver);
		Unmarshaller<Envelope> soapUnmarshaller = new UnmarshallerImpl<Envelope>(soapPackage, soapSchemas, this.externalKeyResolver);

		byte[] doc = null;

		{
			Response response = new Response();
			response.setSignature(new Signature());
			response.setID("Abcd");
			response.setIssueInstant(generateXMLCalendar());
			response.setVersion(VersionConstants.saml20);
			response.setStatus(new Status());
			response.getStatus().setStatusMessage("message");
			response.getStatus().setStatusCode(new StatusCode());
			response.getStatus().getStatusCode().setValue(StatusCodeConstants.success);

			Element element1 = marshaller.marshallSignedElement(response);

			ArtifactResponse artifactResponse = new ArtifactResponse();
			artifactResponse.setSignature(new Signature());
			artifactResponse.setID("Efgh");
			artifactResponse.setIssueInstant(generateXMLCalendar());
			artifactResponse.setVersion(VersionConstants.saml20);
			artifactResponse.setStatus(new Status());
			artifactResponse.getStatus().setStatusMessage("message");
			artifactResponse.getStatus().setStatusCode(new StatusCode());
			artifactResponse.getStatus().getStatusCode().setValue(StatusCodeConstants.success);

			artifactResponse.setAny(element1);

			Element artifactResponseElement = artifactMarshaller.marshallSignedElement(artifactResponse);

			Envelope envelope = new Envelope();
			envelope.setBody(new Body());
			envelope.getBody().getAnies().add(artifactResponseElement);

			doc = soapMarshaller.marshallUnSigned(envelope);
		}

		Envelope envelope = soapUnmarshaller.unMarshallUnSigned(doc);
		ArtifactResponse artifactResponse = artifactUnmarshaller.unMarshallSigned(envelope.getBody().getAnies().get(0));
		Response response = unmarshaller.unMarshallSigned(artifactResponse.getAny());
	}

	private static XMLGregorianCalendar generateXMLCalendar()
	{
		GregorianCalendar calendar;

		SimpleTimeZone tz = new SimpleTimeZone(0, "UTC");
		calendar = new GregorianCalendar(tz);

		try
		{
			DatatypeFactory factory = DatatypeFactory.newInstance();
			return factory.newXMLGregorianCalendar(calendar);
		}
		catch(DatatypeConfigurationException e)
		{
			return null;
		}
	}
}

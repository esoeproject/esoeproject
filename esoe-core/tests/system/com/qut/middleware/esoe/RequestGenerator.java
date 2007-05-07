package com.qut.middleware.esoe;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.SimpleTimeZone;
import java.util.Vector;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.w3._2000._09.xmldsig_.Signature;

import com.qut.middleware.saml2.NameIDFormatConstants;
import com.qut.middleware.saml2.VersionConstants;
import com.qut.middleware.saml2.handler.impl.MarshallerImpl;
import com.qut.middleware.saml2.identifier.impl.IdentifierCacheImpl;
import com.qut.middleware.saml2.identifier.impl.IdentifierGeneratorImpl;
import com.qut.middleware.saml2.schemas.assertion.AttributeType;
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.Attribute;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.AttributeValue;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.Request;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.Resource;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.Subject;
import com.qut.middleware.saml2.schemas.esoe.lxacml.protocol.LXACMLAuthzDecisionQuery;
import com.qut.middleware.saml2.schemas.protocol.AttributeQuery;
import com.qut.middleware.saml2.schemas.protocol.AuthnRequest;
import com.qut.middleware.saml2.schemas.protocol.NameIDPolicy;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;


/** Helper class for testing purposes which can be used to generate various Request documents
 * for use. The class was primarily created so request documents could be generated for load testing.
 * The generated documents may not necessarily yield any useful results, but the timstamp, format and
 * signatures will be valid so that the ESOE will accept them.
 * 
 * The main method will generate the following:
 * 
 * An AuthzDecisionQuery Request
 * An AttributeQuery Request
 * 
 * NOTE: The keystore used must be a valid keystore as far as metadata is concerned for the ESOE
 * that you are testing. This class will use the private key designated by the spepPrivateKeyAlias 
 * command line arg to sign the generated documents. As such, if you send these documents to the ESOE
 * the key alias must exist in the metadata.
 *
 */
@SuppressWarnings("nls")

public class RequestGenerator
{
	private String spepID;
	private String keyStore;
	private String keyStorePassword;
	private String spepKeyAlias;
	private String spepKeyPassword;
	
	
	public RequestGenerator(String spepId, String keyStore, String keyStorePassword, String spepKeyAlias, String spepKeyPassword)
	{
		this.spepID = spepId;
		this.keyStore = keyStore;
		this.keyStorePassword = keyStorePassword;
		this.spepKeyAlias = spepKeyAlias;
		this.spepKeyPassword = spepKeyPassword;
	}
	
	
	public String generateAuthzDecisionQuery() throws Exception
	{
		String requestDocument = null;
		String esoeSessionIndex = "fake-esoe-session-index";
		
		// The resource being accessed by the client
		Resource resource = new Resource();
		Attribute resourceAttribute = new Attribute();
		AttributeValue resourceAttributeValue = new AttributeValue();
		resourceAttributeValue.getContent().add("/test/resource.jsp");
		resourceAttribute.setAttributeValue(resourceAttributeValue);
		resource.setAttribute(resourceAttribute);
		
		// Set the subject of the query..
		Subject subject = new Subject();
		Attribute subjectAttribute = new Attribute();
		AttributeValue subjectAttributeValue = new AttributeValue();
		subjectAttributeValue.getContent().add(esoeSessionIndex); // .. to the session
		subjectAttribute.setAttributeValue(subjectAttributeValue);
		subject.setAttribute(subjectAttribute);
		
		Request request = new Request();
		request.setResource(resource);
		request.setSubject(subject);
		
		// SPEP <Issuer> tag
		NameIDType issuer = new NameIDType();
		issuer.setValue(this.spepID);
		
		// The actual authz query.
		LXACMLAuthzDecisionQuery lxacmlAuthzDecisionQuery = new LXACMLAuthzDecisionQuery();
		lxacmlAuthzDecisionQuery.setRequest(request);
		lxacmlAuthzDecisionQuery.setID(new IdentifierGeneratorImpl(new IdentifierCacheImpl()).generateSAMLID());
		lxacmlAuthzDecisionQuery.setIssueInstant(this.generateXMLCalendar(3000));
		lxacmlAuthzDecisionQuery.setVersion(VersionConstants.saml20);
		lxacmlAuthzDecisionQuery.setIssuer(issuer);
		lxacmlAuthzDecisionQuery.setSignature(new Signature());
		
		InputStream ksStream = new FileInputStream(this.keyStore);
		KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		keystore.load(ksStream, this.keyStorePassword.toCharArray());
		
		Key key = keystore.getKey(this.spepKeyAlias, this.spepKeyPassword.toCharArray());
		
		if (key instanceof PrivateKey)
		{
			String[] authzDecisionSchemas = new String[]{ConfigurationConstants.lxacmlSAMLAssertion, ConfigurationConstants.lxacmlSAMLProtocol, ConfigurationConstants.samlProtocol};
			MarshallerImpl<LXACMLAuthzDecisionQuery> lxacmlAuthzDecisionQueryMarshaller = new MarshallerImpl<LXACMLAuthzDecisionQuery>(LXACMLAuthzDecisionQuery.class.getPackage().getName(), authzDecisionSchemas, spepKeyAlias, (PrivateKey)key);
		
			requestDocument = lxacmlAuthzDecisionQueryMarshaller.marshallSigned(lxacmlAuthzDecisionQuery);
		}
		else
			throw new Exception("Given keyStore does not contain a private key matching spepKeyAlias !!");
		
		return requestDocument;
	}
	
	
	public String generateAttributeQuery() throws Exception
	{
		com.qut.middleware.saml2.schemas.assertion.Subject subject = new com.qut.middleware.saml2.schemas.assertion.Subject();
		NameIDType subjectNameID = new NameIDType();
		subjectNameID.setValue("some:fake:index");
		subject.setNameID(subjectNameID);
		
		NameIDType issuer = new NameIDType();
		issuer.setValue(this.spepID);
		
		String SAMLID = new IdentifierGeneratorImpl(new IdentifierCacheImpl()).generateSAMLID();
		
		// Build the attribute query.
		AttributeQuery attributeQuery = new AttributeQuery();
		attributeQuery.setID(SAMLID);
		attributeQuery.setVersion(VersionConstants.saml20);
		attributeQuery.setIssueInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));
		attributeQuery.setSignature(new Signature());
		attributeQuery.setSubject(subject);
		attributeQuery.setIssuer(issuer);
		
		List<AttributeType> attributes = new Vector<AttributeType>();
		attributeQuery.getAttributes().addAll(attributes);
		
		String requestDocument = null;
		
		InputStream ksStream = new FileInputStream(this.keyStore);
		KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		keystore.load(ksStream, this.keyStorePassword.toCharArray());
		
		Key key = keystore.getKey(this.spepKeyAlias, this.spepKeyPassword.toCharArray());
		
		if (key instanceof PrivateKey)
		{
			String[] schemas = new String[]{ConfigurationConstants.samlAssertion, ConfigurationConstants.samlProtocol};
			
			MarshallerImpl<AttributeQuery> attributeQueryMarshaller = new MarshallerImpl<AttributeQuery>(AttributeQuery.class.getPackage().getName(), schemas, this.spepKeyAlias, (PrivateKey)key);
				
			requestDocument = attributeQueryMarshaller.marshallSigned(attributeQuery);
		}
		else
			throw new Exception("Given keyStore does not contain a private key matching spepKeyAlias !!");
				
		return requestDocument;
	}
	
	public String generateAuthnRequest() throws Exception
	{		
		AuthnRequest authnRequest = new AuthnRequest();
		
		authnRequest.setIssueInstant(this.generateXMLCalendar(3600));
		
		NameIDPolicy nameIDPolicy = new NameIDPolicy();
		
		nameIDPolicy.setFormat(NameIDFormatConstants.trans);
		nameIDPolicy.setAllowCreate(Boolean.TRUE);
		authnRequest.setNameIDPolicy(nameIDPolicy);

		authnRequest.setForceAuthn(Boolean.FALSE);
		authnRequest.setIsPassive(Boolean.FALSE);
		authnRequest.setVersion(VersionConstants.saml20);
		authnRequest.setSignature(new Signature());		
		
		String authnRequestSAMLID = new IdentifierGeneratorImpl(new IdentifierCacheImpl()).generateSAMLID();
		
		authnRequest.setID(authnRequestSAMLID);
		authnRequest.setAssertionConsumerServiceIndex(0);
		authnRequest.setAttributeConsumingServiceIndex(0);
		
		NameIDType issuer = new NameIDType();
		issuer.setValue(this.spepID);
		authnRequest.setIssuer(issuer);
				
		String requestDocument = null;
		
		InputStream ksStream = new FileInputStream(this.keyStore);
		KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		keystore.load(ksStream, this.keyStorePassword.toCharArray());
		
		Key key = keystore.getKey(this.spepKeyAlias, this.spepKeyPassword.toCharArray());
		
		if (key instanceof PrivateKey)
		{
			String[] authnSchemas = new String[]{ConfigurationConstants.samlProtocol, ConfigurationConstants.samlAssertion};
			
			MarshallerImpl<AuthnRequest> authnRequestMarshaller = new MarshallerImpl<AuthnRequest>(AuthnRequest.class.getPackage().getName(), authnSchemas, this.spepKeyAlias, (PrivateKey)key);
			
			requestDocument = authnRequestMarshaller.marshallSigned(authnRequest);
		}
		else
			throw new Exception("Given keyStore does not contain a private key matching spepKeyAlias !!");
				
		return requestDocument;
	}

	
	private XMLGregorianCalendar generateXMLCalendar(int offset)
	{
		GregorianCalendar calendar;
		
		SimpleTimeZone tz = new SimpleTimeZone(0, ConfigurationConstants.timeZone);
		calendar = new GregorianCalendar(tz);
		calendar.add(Calendar.SECOND, offset);
		
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
	
	
	public static void main(String[] args) throws Exception
	{
		String result = null;
		String filename = null;
		
		if(args.length != 5)
		{
			System.err.println("Usage: RequestGenerator spepId keystorePath keystorePassword spepPrivateKeyAlias spepPrivateKeyPassword");
		}
		
		String keystore = args[1];
		
		RequestGenerator generator = new RequestGenerator(args[0], keystore, args[2], args[3], args[4]);
		
		result =  generator.generateAuthzDecisionQuery();
		filename = "tests/generated_data/AuthzDecisionQuery.xml";
		//System.out.println("Generating file: " + filename);
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filename), "UTF-16");
		BufferedWriter buffWriter = new BufferedWriter(writer);
		buffWriter.write(result);
		buffWriter.flush();
				
		result =  generator.generateAttributeQuery();
		filename = "tests" + File.separator + "generated_data" + File.separator +  "AttributeQuery.xml";
		//System.out.println("Generating file: " + filename);
		writer = new OutputStreamWriter(new FileOutputStream(filename), "UTF-16");
		buffWriter = new BufferedWriter(writer);
		buffWriter.write(result);
		buffWriter.flush();
		
		
		result =  generator.generateAuthnRequest();
		filename = "tests" + File.separator + "generated_data" + File.separator +  "AuthnRequest.xml";
		//System.out.println("Generating file: " + filename);
		writer = new OutputStreamWriter(new FileOutputStream(filename), "UTF-16");
		buffWriter = new BufferedWriter(writer);
		buffWriter.write(result);
		buffWriter.flush();
		
		//System.out.println("Done.");
		
		buffWriter.close();
	}
}

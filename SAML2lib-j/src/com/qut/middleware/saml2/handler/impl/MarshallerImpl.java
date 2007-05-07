/* 
 * Copyright 2006, Queensland University of Technology
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
 * Author: Bradley Beddoes
 * Creation Date: 18/10/2006
 * 
 * Purpose: Concrete implementation of all marshalling operations supported by saml2lib-j
 */
package com.qut.middleware.saml2.handler.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyName;
import javax.xml.crypto.dsig.spec.ExcC14NParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.qut.middleware.saml2.Constants;
import com.qut.middleware.saml2.exception.MarshallerException;
import com.qut.middleware.saml2.exception.ResourceException;
import com.qut.middleware.saml2.namespace.NamespacePrefixMapperImpl;
import com.qut.middleware.saml2.resolver.ResourceResolver;
import com.qut.middleware.saml2.resolver.SchemaResolver;

/**
 * Concrete implementation of all marshalling operations supported by saml2lib-j.
 */
public class MarshallerImpl<T> implements com.qut.middleware.saml2.handler.Marshaller<T>
{
	private TransformerFactory transFac;
	private Properties properties;

	private ResourceResolver resourceResolver;

	private JAXBContext jaxbContext;
	private SchemaFactory schemaFactory;
	private Schema schema;

	private MarshallerValidationHandler validationHandler;

	private String keyPairName;
	private PrivateKey pk;

	/* Local logging instance */
	private Logger logger = Logger.getLogger(MarshallerImpl.class.getName());

	/**
	 * Constructor for MarshallerImpl
	 * 
	 * @param packageName
	 *            MUST be the package where xmlObj was created in original JAXB compilation, it may also contain
	 *            additional colon seperated package names where other class files required to unmarshall an object have
	 *            been placed by JAXB
	 * @param schemaList
	 *            List of schema files to perform validation against, files must exist in the local classpath for
	 *            MarshallerImpl
	 * @throws MarshallerException
	 *             if an error occurs creating the marshaller.
	 */
	public MarshallerImpl(String packageName, String[] schemaList) throws MarshallerException
	{
		if ((packageName == null) || (packageName.length() <= 0))
		{
			this.logger.fatal(Messages.getString("MarshallerImpl.6")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("MarshallerImpl.6")); //$NON-NLS-1$
		}
		if ((schemaList == null) || (schemaList.length == 0))
		{
			this.logger.fatal(Messages.getString("MarshallerImpl.2")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("MarshallerImpl.2")); //$NON-NLS-1$
		}

		URL location;

		try
		{
			/* Schema Factory is expensive to create so preload everything at start up */
			this.resourceResolver = new ResourceResolver();

			Source[] schemaSource = new Source[schemaList.length];

			/* Prepare all schemas requested by caller to be used in validation */
			for (int i = 0; i < schemaList.length; i++)
			{
				location = SchemaResolver.class.getResource(schemaList[i]);
				if (location == null)
				{
					this.logger.fatal(Messages.getString("UnmarshallerImpl.49") + schemaList[i] //$NON-NLS-1$
							+ Messages.getString("UnmarshallerImpl.50")); //$NON-NLS-1$
					throw new IllegalArgumentException(
							Messages.getString("UnmarshallerImpl.26") + schemaList[i] + Messages.getString("UnmarshallerImpl.27")); //$NON-NLS-1$//$NON-NLS-2$
				}

				schemaSource[i] = new StreamSource(location.openStream());
			}

			this.jaxbContext = JAXBContext.newInstance(packageName);

			this.schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			this.schemaFactory.setResourceResolver(this.resourceResolver);
			this.schema = this.schemaFactory.newSchema(schemaSource);

			this.logger.info(Messages.getString("MarshallerImpl.23")); //$NON-NLS-1$
		}
		catch (JAXBException je)
		{
			this.logger.warn(Messages.getString("MarshallerImpl.95")); //$NON-NLS-1$
			this.logger.debug(je.getLocalizedMessage(), je);
			throw new MarshallerException(je.getMessage(), je);
		}
		catch (SAXException saxe)
		{
			this.logger.fatal(Messages.getString("MarshallerImpl.39")); //$NON-NLS-1$
			this.logger.debug(saxe.getLocalizedMessage(), saxe);
			throw new MarshallerException(saxe.getMessage(), saxe);
		}
		catch (IOException ioe)
		{
			this.logger.fatal(Messages.getString("MarshallerImpl.66")); //$NON-NLS-1$
			this.logger.debug(ioe.getLocalizedMessage(), ioe);
			throw new MarshallerException(ioe.getMessage(), ioe);
		}
		catch (ResourceException e)
		{
			this.logger.fatal(Messages.getString("MarshallerImpl.97")); //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new MarshallerException(e.getLocalizedMessage(), e);
		}
	}

	/**
	 * Constructor for MarshallerImpl where marshaller will also sign generated document
	 * 
	 * @param packageName
	 *            MUST be the package where xmlObj was created in original JAXB compilation, it may also contain
	 *            additional colon seperated package names where other class files required to unmarshall an object have
	 *            been placed by JAXB
	 * @param schemaList
	 *            List of schema files to perform validation against, files must exist in the classpath of
	 *            MarshallerImpl.class
	 * @param keyPairName
	 *            Name to insert in XML for keypair that was used in signing this document for resolution by remote
	 *            party
	 * @param pk
	 *            Private key to sign documents with
	 * @throws MarshallerException
	 *             if an error occurs creating the marshaller.
	 */
	public MarshallerImpl(String packageName, String[] schemaList, String keyPairName, PrivateKey pk)
			throws MarshallerException
	{
		if ((packageName == null) || (packageName.length() <= 0))
		{
			this.logger.fatal(Messages.getString("MarshallerImpl.6")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("MarshallerImpl.6")); //$NON-NLS-1$
		}
		if ((schemaList == null) || (schemaList.length <= 0))
		{
			this.logger.fatal(Messages.getString("MarshallerImpl.3")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("MarshallerImpl.3")); //$NON-NLS-1$
		}

		if ((keyPairName == null) || (keyPairName.length() <= 0))
		{
			this.logger.fatal(Messages.getString("MarshallerImpl.4")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("MarshallerImpl.4")); //$NON-NLS-1$
		}

		if (pk == null)
		{
			this.logger.fatal(Messages.getString("MarshallerImpl.5")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("MarshallerImpl.5")); //$NON-NLS-1$
		}

		URL location;

		try
		{
			this.resourceResolver = new ResourceResolver();
			this.properties = new Properties();
			Source[] schemaSource = new Source[schemaList.length];

			/* Prepare all schemas requested by caller to be used in validation */
			for (int i = 0; i < schemaList.length; i++)
			{
				location = SchemaResolver.class.getResource(schemaList[i]);
				if (location == null)
				{
					this.logger.fatal(Messages.getString("UnmarshallerImpl.49") + schemaList[i] //$NON-NLS-1$
							+ Messages.getString("UnmarshallerImpl.50")); //$NON-NLS-1$
					throw new IllegalArgumentException(
							Messages.getString("UnmarshallerImpl.26") + schemaList[i] + Messages.getString("UnmarshallerImpl.27")); //$NON-NLS-1$//$NON-NLS-2$
				}

				schemaSource[i] = new StreamSource(location.openStream());
			}

			this.jaxbContext = JAXBContext.newInstance(packageName);

			this.schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			this.schemaFactory.setResourceResolver(this.resourceResolver);
			this.schema = this.schemaFactory.newSchema(schemaSource);

			this.properties.setProperty(OutputKeys.ENCODING, "UTF-16"); //$NON-NLS-1$
			this.transFac = TransformerFactory.newInstance();

			this.validationHandler = new MarshallerValidationHandler();

			this.keyPairName = keyPairName;
			this.pk = pk;

			this.logger.info(Messages.getString("MarshallerImpl.32")); //$NON-NLS-1$
		}
		catch (JAXBException je)
		{
			this.logger.warn(Messages.getString("MarshallerImpl.96")); //$NON-NLS-1$
			this.logger.debug(je.getLocalizedMessage(), je);
			throw new MarshallerException(je.getMessage(), je);
		}
		catch (SAXException saxe)
		{
			this.logger.fatal(Messages.getString("MarshallerImpl.39")); //$NON-NLS-1$
			this.logger.debug(saxe.getLocalizedMessage(), saxe);
			throw new MarshallerException(saxe.getMessage(), saxe);
		}
		catch (IOException ioe)
		{
			this.logger.fatal(Messages.getString("MarshallerImpl.67")); //$NON-NLS-1$
			this.logger.debug(ioe.getLocalizedMessage(), ioe);
			throw new MarshallerException(ioe.getMessage(), ioe);
		}
		catch (ResourceException e)
		{
			this.logger.fatal(Messages.getString("MarshallerImpl.98")); //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new MarshallerException(e.getLocalizedMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.saml2.handler.Marshaller#marshallSigned(java.lang.String, java.lang.String,
	 *      java.security.PrivateKey, java.lang.String[], java.lang.Object)
	 */
	public String marshallSigned(T xmlObj) throws MarshallerException
	{
		this.logger.debug(Messages.getString("MarshallerImpl.40")); //$NON-NLS-1$

		if (xmlObj == null)
		{
			this.logger.fatal(Messages.getString("MarshallerImpl.8")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("MarshallerImpl.8")); //$NON-NLS-1$
		}

		if (this.keyPairName == null || this.pk == null)
		{
			this.logger.fatal(Messages.getString("MarshallerImpl.9")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("MarshallerImpl.9")); //$NON-NLS-1$
		}

		ByteArrayInputStream byteArrayInputStream;
		String xml = marshallUnSigned(xmlObj);

		try
		{
			byteArrayInputStream = new ByteArrayInputStream(xml.getBytes("UTF-16")); //$NON-NLS-1$
			xml = sign(byteArrayInputStream);

			return xml;
		}
		catch (UnsupportedEncodingException uee)
		{
			this.logger.warn(Messages.getString("MarshallerImpl.41")); //$NON-NLS-1$
			this.logger.debug(uee.getLocalizedMessage(), uee);
			throw new MarshallerException(uee.getLocalizedMessage(), uee);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.saml2.handler.Marshaller#marshallUnSigned(java.lang.String, java.lang.Object)
	 */
	public String marshallUnSigned(T xmlObj) throws MarshallerException
	{
		this.logger.debug(Messages.getString("MarshallerImpl.42")); //$NON-NLS-1$

		if (xmlObj == null)
		{
			this.logger.fatal(Messages.getString("MarshallerImpl.12")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("MarshallerImpl.12")); //$NON-NLS-1$
		}

		Marshaller marshaller;
		StringWriter stringWriter = new StringWriter();
		StreamResult streamResult = new StreamResult(stringWriter);

		try
		{
			marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty("jaxb.encoding", "UTF-16"); //$NON-NLS-1$ //$NON-NLS-2$

			/* Setup the configured prefix mapper to make our saml easy for human consumption */
			try
			{
				marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapperImpl()); //$NON-NLS-1$
			}
			catch (PropertyException pe)
			{
				this.logger.error(Messages.getString("MarshallerImpl.43")); //$NON-NLS-1$
				this.logger.debug(pe.getLocalizedMessage(), pe);
				throw new MarshallerException(pe.getMessage(), pe);
			}
			marshaller.marshal(xmlObj, streamResult);

			return stringWriter.toString();
		}
		catch (JAXBException je)
		{
			this.logger.warn(Messages.getString("MarshallerImpl.44")); //$NON-NLS-1$
			this.logger.debug(je.getLocalizedMessage(), je);
			throw new MarshallerException(je.getMessage(), je);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.saml2.handler.Marshaller#marshallUnSignedNode(java.lang.String, java.lang.Object)
	 */
	public Element marshallUnSignedElement(T xmlObj) throws MarshallerException
	{
		this.logger.debug(Messages.getString("MarshallerImpl.45")); //$NON-NLS-1$

		if (xmlObj == null)
		{
			this.logger.fatal(Messages.getString("MarshallerImpl.14")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("MarshallerImpl.14")); //$NON-NLS-1$
		}

		Marshaller marshaller;
		DocumentBuilderFactory docBuildFac;
		DocumentBuilder docBuilder;
		Document doc;

		try
		{
			this.logger.debug(Messages.getString("MarshallerImpl.48")); //$NON-NLS-1$
			docBuildFac = DocumentBuilderFactory.newInstance();
			docBuildFac.setNamespaceAware(true);
			docBuilder = docBuildFac.newDocumentBuilder();
			doc = docBuilder.newDocument();

			marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty("jaxb.encoding", "UTF-16"); //$NON-NLS-1$ //$NON-NLS-2$

			/* Setup the configured prefix mapper to make our SAML xml output easy for human consumption */
			try
			{
				marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapperImpl()); //$NON-NLS-1$
			}
			catch (PropertyException pe)
			{
				this.logger.error(Messages.getString("MarshallerImpl.49")); //$NON-NLS-1$
				this.logger.debug(pe.getLocalizedMessage(), pe);
				throw new MarshallerException(pe.getMessage(), pe);
			}

			marshaller.marshal(xmlObj, doc);

			return doc.getDocumentElement();
		}
		catch (JAXBException je)
		{
			this.logger.warn(Messages.getString("MarshallerImpl.50")); //$NON-NLS-1$
			this.logger.debug(je.getLocalizedMessage(), je);
			throw new MarshallerException(je.getMessage(), je);
		}
		catch (ParserConfigurationException pce)
		{
			this.logger.warn(Messages.getString("MarshallerImpl.51")); //$NON-NLS-1$
			this.logger.debug(pce.getLocalizedMessage(), pce);
			throw new MarshallerException(pce.getMessage(), pce);
		}
	}

	/**
	 * Validates, Signs and creates XML content.
	 * 
	 * This function will validate the supplied schema. For each supplied XML compliant id in idList an empty
	 * <Signature/> block is assumed to be present so that the Signature generation output can be inserted in a valid
	 * position in the final document.
	 * 
	 * @param schema
	 *            The name of the schema file eg "my-schema.xsd" to validate against
	 * @param document
	 *            The validated xml content to be signed
	 * @return A string representation of the completed document
	 */
	private String sign(InputStream document) throws MarshallerException
	{
		XMLSignatureFactory xmlSigFac;
		DigestMethod digestMethod;
		CanonicalizationMethod canocMeth;
		SignatureMethod sigMeth;
		Transformer trans;

		ArrayList<Transform> transformList;
		Document doc;
		SignedInfo signedInfo;
		DOMSignContext domSignContext;
		Element signatureParent = null;
		Element signatureElement = null;
		XMLSignature signature;
		Element postSignature;
		Reference ref;
		KeyInfoFactory factory;
		KeyInfo keyInfo;
		KeyName keyName;
		NodeList nodeList;
		String id;

		StreamResult streamResult;

		this.logger.debug(Messages.getString("MarshallerImpl.52")); //$NON-NLS-1$
		try
		{
			/* XMLSignatureFactory instances are not thread safe outside static functions so create in local scope */
			xmlSigFac = XMLSignatureFactory
					.getInstance(
							Constants.DOM_FACTORY, (Provider) Class.forName(System.getProperty(Constants.JSR_MECHANISM, Constants.JSR_PROVIDER)).newInstance()); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			digestMethod = xmlSigFac.newDigestMethod(DigestMethod.SHA1, null);
			canocMeth = xmlSigFac.newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE,
					(ExcC14NParameterSpec) null);

			if (this.pk.getAlgorithm().equals(Constants.RSA_KEY)) //$NON-NLS-1$
			{
				this.logger.info(Messages.getString("MarshallerImpl.29")); //$NON-NLS-1$
				sigMeth = xmlSigFac.newSignatureMethod(SignatureMethod.RSA_SHA1, null);
			}
			else
			{
				if (this.pk.getAlgorithm().equals(Constants.DSA_KEY)) //$NON-NLS-1$
				{
					this.logger.info(Messages.getString("MarshallerImpl.30")); //$NON-NLS-1$
					sigMeth = xmlSigFac.newSignatureMethod(SignatureMethod.DSA_SHA1, null);
				}
				else
				{
					this.logger.fatal(Messages.getString("MarshallerImpl.19")); //$NON-NLS-1$
					throw new MarshallerException(Messages.getString("MarshallerImpl.19")); //$NON-NLS-1$
				}
			}

			doc = validate(document);

			/* Create Transformer */
			trans = this.transFac.newTransformer();
			trans.setOutputProperties(this.properties);

			/* Locate all the empty Signature elements we wish to populate */
			nodeList = doc.getElementsByTagNameNS(XMLSignature.XMLNS, Constants.SIGNATURE_ELEMENT);
			if (nodeList.getLength() <= 0)
			{
				/* Client did not instantiate a <Signature/> element template correctly */
				this.logger.error(Messages.getString("MarshallerImpl.53")); //$NON-NLS-1$
				throw new MarshallerException(Messages.getString("MarshallerImpl.0")); //$NON-NLS-1$
			}

			/* Sign elements in reverse so that outer calculations take into account inner signature blocks */
			for (int i = nodeList.getLength() - 1; i >= 0; i--)
			{
				/* Create new transform object for each element being signed */
				transformList = new ArrayList<Transform>();
				TransformParameterSpec transformSpec = null;

				Transform exc14nTransform = xmlSigFac.newTransform(
						Constants.EXC14NTRANS, transformSpec); //$NON-NLS-1$
				Transform envTransform = xmlSigFac.newTransform(
						Constants.ENVTRANS, transformSpec); //$NON-NLS-1$

				transformList.add(envTransform);
				transformList.add(exc14nTransform);

				/* Get the signature element and parent element for this enveloped signature */
				signatureElement = (Element) nodeList.item(i);

				// signatureParent = doc.getElementById(elementID.get(i));
				signatureParent = (Element) nodeList.item(i).getParentNode();
				id = signatureParent.getAttribute(Constants.ID_ATTRIBUTE);

				if (id == null || id.length() <= 0)
				{
					this.logger.warn(Messages.getString("MarshallerImpl.68")); //$NON-NLS-1$
					throw new MarshallerException(Messages.getString("MarshallerImpl.69")); //$NON-NLS-1$
				}

				this.logger.debug(Messages.getString("MarshallerImpl.54") + id); //$NON-NLS-1$

				ref = xmlSigFac.newReference("#" + id, digestMethod, transformList, null, null); //$NON-NLS-1$
				signedInfo = xmlSigFac.newSignedInfo(canocMeth, sigMeth, Collections.singletonList(ref));

				factory = KeyInfoFactory
						.getInstance(
								Constants.DOM_FACTORY, (Provider) Class.forName(System.getProperty(Constants.JSR_MECHANISM, Constants.JSR_PROVIDER)).newInstance()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				keyName = factory.newKeyName(this.keyPairName);

				/* Future additions to the KeyInfo block should be extended here */
				keyInfo = factory.newKeyInfo(Collections.singletonList(keyName));

				/* Skip text nodes here (handles human generated xml correctly) */
				if ((signatureElement.getNextSibling() != null)
						&& (signatureElement.getNextSibling().getNodeType() == Node.TEXT_NODE))
				{
					postSignature = (Element) signatureElement.getNextSibling().getNextSibling();
				}
				else
				{
					postSignature = (Element) signatureElement.getNextSibling();
				}

				/* Strip away any additional textnodes that are located (handles human generated xml correctly) */
				if ((signatureElement.getPreviousSibling() != null)
						&& (signatureElement.getPreviousSibling().getNodeType() == Node.TEXT_NODE))
				{
					signatureParent.removeChild(signatureElement.getPreviousSibling());
				}

				signatureParent.removeChild(signatureElement);
				signature = xmlSigFac.newXMLSignature(signedInfo, keyInfo);

				/* Determine signature context dependant on document contents */
				if (postSignature != null)
					domSignContext = new DOMSignContext(this.pk, signatureParent, postSignature);
				else
					domSignContext = new DOMSignContext(this.pk, signatureParent);

				domSignContext.putNamespacePrefix(XMLSignature.XMLNS, "ds"); //$NON-NLS-1$
				signature.sign(domSignContext);
			}

			this.logger.debug(Messages.getString("MarshallerImpl.55")); //$NON-NLS-1$

			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			streamResult = new StreamResult(bytes);
			trans.transform(new DOMSource(doc), streamResult);

			return bytes.toString("UTF-16"); //$NON-NLS-1$
		}
		catch (InvalidAlgorithmParameterException iape)
		{
			this.logger.fatal(Messages.getString("MarshallerImpl.34")); //$NON-NLS-1$
			this.logger.debug(iape.getLocalizedMessage(), iape);
			throw new MarshallerException(iape.getMessage(), iape);
		}
		catch (NoSuchAlgorithmException nsae)
		{
			this.logger.fatal(Messages.getString("MarshallerImpl.35")); //$NON-NLS-1$
			this.logger.debug(nsae.getLocalizedMessage(), nsae);
			throw new MarshallerException(nsae.getMessage(), nsae);
		}
		catch (ClassNotFoundException cfe)
		{
			this.logger.fatal(Messages.getString("MarshallerImpl.36")); //$NON-NLS-1$
			this.logger.debug(cfe.getLocalizedMessage(), cfe);
			throw new MarshallerException(cfe.getMessage(), cfe);
		}
		catch (IllegalAccessException iae)
		{
			this.logger.fatal(Messages.getString("MarshallerImpl.37")); //$NON-NLS-1$
			this.logger.debug(iae.getLocalizedMessage(), iae);
			throw new MarshallerException(iae.getMessage(), iae);
		}
		catch (InstantiationException cfe)
		{
			this.logger.fatal(Messages.getString("MarshallerImpl.38")); //$NON-NLS-1$
			this.logger.debug(cfe.getLocalizedMessage(), cfe);
			throw new MarshallerException(cfe.getMessage(), cfe);
		}
		catch (TransformerConfigurationException tce)
		{
			this.logger.fatal(Messages.getString("MarshallerImpl.33")); //$NON-NLS-1$
			this.logger.debug(tce.getLocalizedMessage(), tce);
			throw new MarshallerException(tce.getMessage(), tce);
		}
		catch (IOException ioe)
		{
			this.logger.warn(Messages.getString("MarshallerImpl.56")); //$NON-NLS-1$
			this.logger.debug(ioe.getLocalizedMessage(), ioe);
			throw new MarshallerException(ioe.getMessage(), ioe);
		}
		catch (ParserConfigurationException pce)
		{
			this.logger.warn(Messages.getString("MarshallerImpl.57")); //$NON-NLS-1$
			this.logger.debug(pce.getLocalizedMessage(), pce);
			throw new MarshallerException(pce.getMessage(), pce);
		}
		catch (SAXException saxe)
		{
			this.logger.warn(Messages.getString("MarshallerImpl.58")); //$NON-NLS-1$
			this.logger.debug(saxe.getLocalizedMessage(), saxe);
			throw new MarshallerException(saxe.getMessage(), saxe);
		}
		catch (XMLSignatureException xmlse)
		{
			this.logger.error(Messages.getString("MarshallerImpl.61")); //$NON-NLS-1$
			this.logger.debug(xmlse.getLocalizedMessage(), xmlse);
			throw new MarshallerException(xmlse.getMessage(), xmlse);
		}
		catch (MarshalException me)
		{
			this.logger.warn(Messages.getString("MarshallerImpl.62")); //$NON-NLS-1$
			this.logger.debug(me.getLocalizedMessage(), me);
			throw new MarshallerException(me.getMessage(), me);
		}
		catch (TransformerException te)
		{
			this.logger.warn(Messages.getString("MarshallerImpl.63")); //$NON-NLS-1$
			this.logger.debug(te.getLocalizedMessage(), te);
			throw new MarshallerException(te.getMessage(), te);
		}
	}

	/**
	 * Validates the supplied document against schema, returns DOMResult on success.
	 * 
	 * @param schema
	 *            Schema document to validate against
	 * @param document
	 *            InputStream representation of the document to validate
	 * @return
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	private Document validate(InputStream document) throws ParserConfigurationException, IOException, SAXException
	{
		this.logger.debug(Messages.getString("MarshallerImpl.64")); //$NON-NLS-1$

		DocumentBuilderFactory docBuildFac = DocumentBuilderFactory.newInstance();
		docBuildFac.setNamespaceAware(true);
		docBuildFac.setValidating(false);
		docBuildFac.setAttribute("http://apache.org/xml/features/dom/defer-node-expansion", Boolean.FALSE); //$NON-NLS-1$

		DocumentBuilder docBuilder = docBuildFac.newDocumentBuilder();
		Document doc = docBuilder.parse(document);

		Validator validator = this.schema.newValidator();
		validator.setErrorHandler(this.validationHandler);

		validator.validate(new DOMSource(doc));

		return doc;

	}
}

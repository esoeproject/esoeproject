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
 * Purpose: Concrete implementation of all unmarshalling operations supported by saml2lib-j
 */
package com.qut.middleware.saml2.handler.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.Key;
import java.security.KeyException;
import java.security.Provider;
import java.security.PublicKey;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyName;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.qut.middleware.saml2.Constants;
import com.qut.middleware.saml2.ExternalKeyResolver;
import com.qut.middleware.saml2.exception.KeyResolutionException;
import com.qut.middleware.saml2.exception.ReferenceValueException;
import com.qut.middleware.saml2.exception.ResourceException;
import com.qut.middleware.saml2.exception.SignatureValueException;
import com.qut.middleware.saml2.exception.UnmarshallerException;
import com.qut.middleware.saml2.resolver.ResourceResolver;
import com.qut.middleware.saml2.resolver.SchemaResolver;
import com.qut.middleware.saml2.schemas.metadata.KeyTypes;
import com.qut.middleware.saml2.sec.KeyData;

/** Concrete implementation of all unmarshalling operations supported by saml2lib-j. */
public class UnmarshallerImpl<T> implements com.qut.middleware.saml2.handler.Unmarshaller<T>
{

	/* Local logging instance */
	private Logger logger = Logger.getLogger(UnmarshallerImpl.class.getName());

	/**
	 * @author Bradley Beddoes KeySelectorResult Implementation for use by KeyResolver
	 */
	private class ExternalKeyResolverResult implements KeySelectorResult
	{
		Key key;

		/* Local logging instance */
		private Logger logger = Logger.getLogger(ExternalKeyResolverResult.class.getName());

		/**
		 * @param key
		 *            Key to be referenced by this result object
		 */
		public ExternalKeyResolverResult(Key key)
		{
			/* Ensure that a stable base is created when this result is setup */
			if (key == null)
			{
				this.logger.fatal(Messages.getString("UnmarshallerImpl.20")); //$NON-NLS-1$
				throw new IllegalArgumentException(Messages.getString("UnmarshallerImpl.20")); //$NON-NLS-1$
			}

			this.key = key;
			this.logger.info(Messages.getString("UnmarshallerImpl.34")); //$NON-NLS-1$
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.xml.crypto.KeySelectorResult#getKey()
		 */
		public Key getKey()
		{
			return this.key;
		}
	}

	/**
	 * @author Bradley Beddoes Resolves keys by name from a supplied external key resolver, could be easily extended to
	 *         trust key data embedded in supplied XML document
	 */
	private class KeyResolver extends KeySelector
	{
		private ExternalKeyResolver extKeyRes;

		/* Local logging instance */
		private Logger logger = Logger.getLogger(ExternalKeyResolverResult.class.getName());

		/**
		 * @param extKeyResolver
		 *            Externally created key resolver capable of performing logic to resolve keys needed to validate XML
		 *            given keynames
		 */
		public KeyResolver(ExternalKeyResolver extKeyResolver)
		{
			super();

			/* Ensure that a stable base is created when this result is setup */
			if (extKeyResolver == null)
			{
				this.logger.fatal(Messages.getString("UnmarshallerImpl.17")); //$NON-NLS-1$
				throw new IllegalArgumentException(Messages.getString("UnmarshallerImpl.17")); //$NON-NLS-1$
			}

			this.extKeyRes = extKeyResolver;
			this.logger.info(Messages.getString("UnmarshallerImpl.36")); //$NON-NLS-1$
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.xml.crypto.KeySelector#select(javax.xml.crypto.dsig.keyinfo.KeyInfo,
		 *      javax.xml.crypto.KeySelector.Purpose, javax.xml.crypto.AlgorithmMethod,
		 *      javax.xml.crypto.XMLCryptoContext)
		 */
		@Override
		public KeySelectorResult select(KeyInfo keyInfo, KeySelector.Purpose purpose, AlgorithmMethod method,
				XMLCryptoContext context) throws KeySelectorException
		{
			if (keyInfo == null)
			{
				this.logger.warn(Messages.getString("UnmarshallerImpl.18")); //$NON-NLS-1$ 
				throw new KeySelectorException(Messages.getString("UnmarshallerImpl.18")); //$NON-NLS-1$ 
			}

			String name = null;
			ExternalKeyResolverResult result;
			List<XMLStructure> keyElementList;
			keyElementList = keyInfo.getContent();

			this.logger.debug(Messages.getString("UnmarshallerImpl.38")); //$NON-NLS-1$ 

			for (XMLStructure keyElement : keyElementList)
			{
				if (keyElement instanceof KeyName)
				{
					KeyName keyName = (KeyName) keyElement;
					name = keyName.getName();
					this.logger.debug(Messages.getString("UnmarshallerImpl.39") + keyName.getName()); //$NON-NLS-1$ 
				}
			}
			if (name == null)
			{
				this.logger.warn(Messages.getString("UnmarshallerImpl.19")); //$NON-NLS-1$ 
				throw new KeySelectorException(Messages.getString("UnmarshallerImpl.19")); //$NON-NLS-1$ 
			}

			try
			{
				this.logger.debug(Messages.getString("UnmarshallerImpl.41")); //$NON-NLS-1$ 
				result = new ExternalKeyResolverResult(this.extKeyRes.resolveKey(name));
			}
			catch (KeyResolutionException e)
			{
				this.logger.warn(Messages.getString("UnmarshallerImpl.42"), e); //$NON-NLS-1$ 
				throw new KeySelectorException(Messages.getString("UnmarshallerImpl.43"), e); //$NON-NLS-1$ 
			}
			catch (IllegalArgumentException ipe)
			{
				this.logger.warn(Messages.getString("UnmarshallerImpl.42") + name, ipe); //$NON-NLS-1$ 
				throw new KeySelectorException(Messages.getString("UnmarshallerImpl.43"), ipe); //$NON-NLS-1$
			}

			return result;
		}
	}

	private String providerName;
	private JAXBContext jaxbContext;
	// private XMLSignatureFactory xmlSigFac;
	private ResourceResolver resourceResolver;
	private ExternalKeyResolver extKeyResolver;

	private SchemaFactory schemaFactory;
	private Schema schema;
	private UnmarshallerValidationHandler validationHandler;

	private final String KEY_PURPOSE = "use"; //$NON-NLS-1$
	private final String KEY_DESCRIPTOR = "KeyDescriptor"; //$NON-NLS-1$

	/**
	 * Constructor for UnmarshallerImpl
	 * 
	 * @param packageName
	 *            MUST be the package where xmlObj was created in original JAXB compilation, it may also contain
	 *            additional colon seperated package names where other class files required to unmarshall an object have
	 *            been placed by JAXB
	 * @param schemaList
	 *            List of schema files to perform validation against, files must exist in the local classpath for
	 *            UnmarshallerImpl
	 * @throws UnmarshallerException
	 */
	public UnmarshallerImpl(String packageName, String[] schemaList) throws UnmarshallerException
	{
		init(packageName, schemaList);
	}

	/**
	 * Constructor for UnmarshallerImpl
	 * 
	 * @param packageName
	 *            MUST be the package where xmlObj was created in original JAXB compilation, it may also contain
	 *            additional colon seperated package names where other class files required to unmarshall an object have
	 *            been placed by JAXB
	 * @param schemaList
	 *            List of schema files to perform validation against, files must exist in the local classpath for
	 *            UnmarshallerImpl
	 * @param extKeyResolver
	 *            An implementation of ExternalKeyResolver to resolve public keys for signed xml documents
	 * @throws UnmarshallerException
	 */
	public UnmarshallerImpl(String packageName, String[] schemaList, ExternalKeyResolver extKeyResolver)
			throws UnmarshallerException
	{
		if (extKeyResolver == null)
		{
			this.logger.fatal(Messages.getString("UnmarshallerImpl.3")); //$NON-NLS-1$ 
			throw new IllegalArgumentException(Messages.getString("UnmarshallerImpl.3")); //$NON-NLS-1$ 
		}

		init(packageName, schemaList);
		this.extKeyResolver = extKeyResolver;
	}

	/**
	 * Sets up unmarshaller internal state
	 * 
	 * @param schemaList
	 *            String array of schemas to initalise
	 * @throws UnmarshallerException
	 */
	private void init(String packageName, String[] schemaList) throws UnmarshallerException
	{
		this.logger.debug(Messages.getString("UnmarshallerImpl.45")); //$NON-NLS-1$ 
		if ((schemaList == null) || (schemaList.length == 0))
		{
			this.logger.fatal("Schemas not supplied correctly, please initalise schema list"); //$NON-NLS-1$ 
			throw new IllegalArgumentException("Schemas not supplied correctly, please initalise schema list"); //$NON-NLS-1$ 
		}

		if ((packageName == null) || (packageName.length() <= 0))
		{
			this.logger.fatal(Messages.getString("UnmarshallerImpl.4")); //$NON-NLS-1$ 
			throw new IllegalArgumentException(Messages.getString("UnmarshallerImpl.4")); //$NON-NLS-1$
		}

		URL location;

		try
		{
			this.resourceResolver = new ResourceResolver();

			/* JAXBContext is thread safe and expensive to initiate so create once only */
			this.jaxbContext = JAXBContext.newInstance(packageName);

			this.validationHandler = new UnmarshallerValidationHandler();

			Source[] schemaSource = new Source[schemaList.length];

			/* Prepare all schemas requested by caller to be used in validation */
			for (int i = 0; i < schemaList.length; i++)
			{
				location = SchemaResolver.class.getResource(schemaList[i]);
				if (location == null)
				{
					this.logger.fatal(Messages.getString("UnmarshallerImpl.49") + schemaList[i] //$NON-NLS-1$
							+ Messages.getString("UnmarshallerImpl.50")); //$NON-NLS-1$   //$NON-NLS-2$
					throw new IllegalArgumentException(
							Messages.getString("UnmarshallerImpl.26") + schemaList[i] + Messages.getString("UnmarshallerImpl.27")); //$NON-NLS-1$ //$NON-NLS-2$
				}

				schemaSource[i] = new StreamSource(location.openStream());
			}

			this.schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			this.schemaFactory.setResourceResolver(this.resourceResolver);
			this.schema = this.schemaFactory.newSchema(schemaSource);

			this.logger.info(Messages.getString("UnmarshallerImpl.51")); //$NON-NLS-1$ 
		}
		catch (JAXBException je)
		{
			this.logger.warn(Messages.getString("UnmarshallerImpl.124")); //$NON-NLS-1$
			this.logger.debug(je.getLocalizedMessage(), je);
			throw new UnmarshallerException(je.getMessage(), je);
		}
		catch (IOException ioe)
		{
			this.logger.fatal(Messages.getString("UnmarshallerImpl.121")); //$NON-NLS-1$ 
			this.logger.debug(ioe.getLocalizedMessage(), ioe);
			throw new UnmarshallerException(ioe.getMessage(), ioe);
		}
		catch (SAXException saxe)
		{
			this.logger.fatal(Messages.getString("UnmarshallerImpl.122")); //$NON-NLS-1$ 
			this.logger.debug(saxe.getLocalizedMessage(), saxe);
			throw new UnmarshallerException(saxe.getMessage(), saxe);
		}
		catch (ResourceException e)
		{
			this.logger.fatal(Messages.getString("UnmarshallerImpl.125")); //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new UnmarshallerException(e.getLocalizedMessage(), e);
		}
	}

	/**
	 * Translates a String into a w3c XML document object and validates against schema
	 * 
	 * @param schema
	 *            String representation of the schema file name eh "my-schema.xsd" to perform validation against
	 * @param document
	 *            The XML document in string format that is being processed
	 * @return A valid w3c.org Document object
	 * @throws UnmarshallerException
	 */
	private Document generateDocument(byte[] document) throws UnmarshallerException
	{
		this.logger.debug(Messages.getString("UnmarshallerImpl.56")); //$NON-NLS-1$ 

		ByteArrayInputStream doc;
		Document result;

		try
		{
			/* Create JAXP DocumentBuilder to retrieve Document */
			doc = new ByteArrayInputStream(document); //$NON-NLS-1$ 
			result = validate(doc);

			// return (Document) result.getNode();
			return result;
		}
		catch (IOException ioe)
		{
			this.logger.warn(Messages.getString("UnmarshallerImpl.58")); //$NON-NLS-1$ 
			this.logger.debug(ioe.getLocalizedMessage(), ioe);
			throw new UnmarshallerException(ioe.getMessage(), ioe);
		}
		catch (ParserConfigurationException pce)
		{
			this.logger.warn(Messages.getString("UnmarshallerImpl.59")); //$NON-NLS-1$ 
			this.logger.debug(pce.getLocalizedMessage(), pce);
			throw new UnmarshallerException(pce.getMessage(), pce);
		}
		catch (SAXException saxe)
		{
			this.logger.warn(Messages.getString("UnmarshallerImpl.60")); //$NON-NLS-1$ 
			this.logger.debug(saxe.getLocalizedMessage(), saxe);
			throw new UnmarshallerException(saxe.getMessage(), saxe);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.saml2.handler.Unmarshaller#unMarshallMetadata(java.lang.String, java.lang.String,
	 *      java.security.PublicKey, java.lang.String, java.util.Map)
	 */
	public T unMarshallMetadata(PublicKey pk, byte[] document, Map<String, KeyData> keyList)
			throws SignatureValueException, ReferenceValueException, UnmarshallerException
	{
		this.logger.debug(Messages.getString("UnmarshallerImpl.61")); //$NON-NLS-1$ 

		if (pk == null)
		{
			this.logger.fatal(Messages.getString("UnmarshallerImpl.6")); //$NON-NLS-1$ 
			throw new IllegalArgumentException(Messages.getString("UnmarshallerImpl.6")); //$NON-NLS-1$ 
		}

		if ((document == null) || (document.length <= 0))
		{
			this.logger.fatal(Messages.getString("UnmarshallerImpl.7")); //$NON-NLS-1$ 
			throw new IllegalArgumentException(Messages.getString("UnmarshallerImpl.7")); //$NON-NLS-1$ 
		}

		if ((keyList == null))
		{
			this.logger.fatal(Messages.getString("UnmarshallerImpl.28")); //$NON-NLS-1$ 
			throw new IllegalArgumentException(Messages.getString("UnmarshallerImpl.28")); //$NON-NLS-1$
		}

		Unmarshaller unmarshaller;
		Document doc;
		NodeList nodeList;
		KeyInfo keyInfo;
		List<XMLStructure> keyElementList;
		KeyData descriptor;
		DOMStructure domStructure;
		String name;
		PublicKey localPK;
		XMLSignatureFactory xmlSigFac;

		try
		{
			/* XMLSignatureFactory instances are not thread safe outside static functions so create in local scope */
			xmlSigFac = XMLSignatureFactory.getInstance(Constants.DOM_FACTORY, (Provider) Class.forName(
					System.getProperty(Constants.JSR_MECHANISM, Constants.JSR_PROVIDER)).newInstance()); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$

			this.logger.debug(Messages.getString("UnmarshallerImpl.67")); //$NON-NLS-1$ 
			doc = this.generateDocument(document);

			this.logger.debug(Messages.getString("UnmarshallerImpl.68")); //$NON-NLS-1$ 
			validateSignature(doc, pk);

			/* Document and Signatures are all valid, unmarshall object for application use */
			unmarshaller = this.jaxbContext.createUnmarshaller();

			/*
			 * Metadata unmarshalling requires that we extract all keys in the authentication network for use by the
			 * client application, do this and store. Additionally schema specification states that KeyInfo will always
			 * be the first sibling of <KeyDescriptor>
			 */
			this.logger.debug(Messages.getString("UnmarshallerImpl.69")); //$NON-NLS-1$ 
			nodeList = doc.getElementsByTagNameNS("*", this.KEY_DESCRIPTOR); //$NON-NLS-1$ 
			for (int i = 0; i < nodeList.getLength(); i++)
			{
				name = null;
				localPK = null;
				descriptor = null;

				/* Skip text nodes here (handles human generated xml correctly) */
				if (nodeList.item(i).getFirstChild().getNodeType() == Node.TEXT_NODE)
				{
					domStructure = new DOMStructure(nodeList.item(i).getFirstChild().getNextSibling());
				}
				else
				{
					domStructure = new DOMStructure(nodeList.item(i).getFirstChild());
				}

				keyInfo = xmlSigFac.getKeyInfoFactory().unmarshalKeyInfo(domStructure);

				/* Key info has been submitted with this descriptor */
				if (keyInfo != null)
				{
					keyElementList = keyInfo.getContent();

					for (XMLStructure keyElement : keyElementList)
					{
						if (keyElement instanceof KeyName)
						{
							KeyName keyName = (KeyName) keyElement;
							name = keyName.getName();
						}
						if (keyElement instanceof KeyValue)
						{
							KeyValue keyValue = (KeyValue) keyElement;
							localPK = keyValue.getPublicKey();
						}
					}

					if (name == null || name.length() <= 0 || localPK == null)
					{
						this.logger.warn(Messages.getString("UnmarshallerImpl.24")); //$NON-NLS-1$ 
						throw new UnmarshallerException(Messages.getString("UnmarshallerImpl.24")); //$NON-NLS-1$ 
					}

					this.logger.info(Messages.getString("UnmarshallerImpl.74") + name); //$NON-NLS-1$

					descriptor = new KeyData(KeyTypes.fromValue(nodeList.item(i).getAttributes().getNamedItem(
							this.KEY_PURPOSE).getNodeValue()), localPK);
					
					if (keyList.containsKey(name))
					{
						
						// Encode both the keys in their "primary encoding format"
						byte[] encodedLocalKey = localPK.getEncoded();
						byte[] encodedListKey = keyList.get(name).getPk().getEncoded();
						boolean equal = true;
						
						// Differing lengths implied differing keys
						if( encodedLocalKey.length != encodedListKey.length )
						{
							equal = false;
						}
						else
						{
							// Otherwise we compare byte by byte to make sure they're equal.
							for (int index=0; index<encodedLocalKey.length; ++index)
							{
								if ( encodedLocalKey[index] != encodedListKey[index] )
								{
									equal = false;
									break;
								}
							}
						}
						
						// Same key alias + different key value is an error condition.
						if (!equal)
						{
							this.logger.error(Messages.getString("UnmarshallerImpl.25")); //$NON-NLS-1$
							throw new UnmarshallerException(Messages.getString("UnmarshallerImpl.25")); //$NON-NLS-1$
						}
					}
					
					keyList.put(name, descriptor);
				}
			}

			this.logger.debug(Messages.getString("UnmarshallerImpl.75")); //$NON-NLS-1$

			/* Signatures are all valid, unmarshall object for application use */
			return (T) unmarshaller.unmarshal(doc);
		}
		catch (ClassNotFoundException cfe)
		{
			this.logger.fatal(Messages.getString("UnmarshallerImpl.52")); //$NON-NLS-1$ 
			this.logger.debug(cfe.getLocalizedMessage(), cfe);
			throw new UnmarshallerException(cfe.getMessage(), cfe);
		}
		catch (IllegalAccessException iae)
		{
			this.logger.fatal(Messages.getString("UnmarshallerImpl.53")); //$NON-NLS-1$ 
			this.logger.debug(iae.getLocalizedMessage(), iae);
			throw new UnmarshallerException(iae.getMessage(), iae);
		}
		catch (InstantiationException cfe)
		{
			this.logger.fatal(Messages.getString("UnmarshallerImpl.54")); //$NON-NLS-1$ 
			this.logger.debug(cfe.getLocalizedMessage(), cfe);
			throw new UnmarshallerException(cfe.getMessage(), cfe);
		}
		catch (JAXBException je)
		{
			this.logger.error(Messages.getString("UnmarshallerImpl.76")); //$NON-NLS-1$ 
			this.logger.debug(je.getLocalizedMessage(), je);
			throw new UnmarshallerException(je.getMessage(), je);
		}
		catch (MarshalException me)
		{
			this.logger.error(Messages.getString("UnmarshallerImpl.77")); //$NON-NLS-1$ 
			this.logger.debug(me.getLocalizedMessage(), me);
			throw new UnmarshallerException(me.getMessage(), me);
		}
		catch (KeyException ke)
		{
			this.logger.error(Messages.getString("UnmarshallerImpl.78")); //$NON-NLS-1$
			this.logger.debug(ke.getLocalizedMessage(), ke);
			throw new UnmarshallerException(ke.getMessage(), ke);
		}
		catch (XMLSignatureException xse)
		{
			this.logger.error(Messages.getString("UnmarshallerImpl.79")); //$NON-NLS-1$ 
			this.logger.debug(xse.getLocalizedMessage(), xse);
			throw new UnmarshallerException(xse.getMessage(), xse);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.saml2.handler.UnMarshaller#unMarshall(java.lang.String, java.net.URL)
	 */
	public T unMarshallSigned(PublicKey pk, byte[] document) throws SignatureValueException, ReferenceValueException,
			UnmarshallerException
	{
		this.logger.debug(Messages.getString("UnmarshallerImpl.80")); //$NON-NLS-1$

		if (pk == null)
		{
			this.logger.fatal(Messages.getString("UnmarshallerImpl.6")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("UnmarshallerImpl.6")); //$NON-NLS-1$
		}

		if ((document == null) || (document.length <= 0))
		{
			this.logger.fatal(Messages.getString("UnmarshallerImpl.7")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("UnmarshallerImpl.7")); //$NON-NLS-1$
		}
		Unmarshaller unmarshaller;
		Document doc;

		try
		{
			this.logger.debug(Messages.getString("UnmarshallerImpl.81")); //$NON-NLS-1$
			doc = this.generateDocument(document);

			this.logger.debug(Messages.getString("UnmarshallerImpl.82")); //$NON-NLS-1$
			validateSignature(doc, pk);

			/* Document and Signatures are all valid, unmarshall object for application use */
			unmarshaller = this.jaxbContext.createUnmarshaller();
			return (T) unmarshaller.unmarshal(doc);
		}
		catch (JAXBException je)
		{
			this.logger.warn(Messages.getString("UnmarshallerImpl.83")); //$NON-NLS-1$
			this.logger.debug(je.getLocalizedMessage(), je);
			throw new UnmarshallerException(je.getMessage(), je);
		}
		catch (MarshalException me)
		{
			this.logger.warn(Messages.getString("UnmarshallerImpl.84")); //$NON-NLS-1$ 
			this.logger.debug(me.getLocalizedMessage(), me);
			throw new UnmarshallerException(me.getMessage(), me);
		}
		catch (XMLSignatureException xse)
		{
			this.logger.error(Messages.getString("UnmarshallerImpl.85")); //$NON-NLS-1$ 
			this.logger.debug(xse.getLocalizedMessage(), xse);
			throw new UnmarshallerException(xse.getMessage(), xse);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.saml2.handler.UnMarshaller#unMarshall(java.lang.String, java.net.URL)
	 */
	public T unMarshallSigned(byte[] document) throws SignatureValueException, ReferenceValueException,
			UnmarshallerException
	{
		this.logger.debug(Messages.getString("UnmarshallerImpl.86")); //$NON-NLS-1$ 

		if ((document == null) || (document.length <= 0))
		{
			this.logger.fatal(Messages.getString("UnmarshallerImpl.10")); //$NON-NLS-1$ 
			throw new IllegalArgumentException(Messages.getString("UnmarshallerImpl.10")); //$NON-NLS-1$
		}

		if (this.extKeyResolver == null)
		{
			this.logger.fatal(Messages.getString("UnmarshallerImpl.32")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("UnmarshallerImpl.32")); //$NON-NLS-1$
		}

		Unmarshaller unmarshaller;
		Document doc;

		try
		{
			this.logger.debug(Messages.getString("UnmarshallerImpl.91")); //$NON-NLS-1$ 
			doc = this.generateDocument(document);

			this.logger.debug(Messages.getString("UnmarshallerImpl.92")); //$NON-NLS-1$
			validateSignature(doc, null);

			/* Document and Signatures are all valid, unmarshall object for application use */
			unmarshaller = this.jaxbContext.createUnmarshaller();
			return (T) unmarshaller.unmarshal(doc);
		}
		catch (JAXBException je)
		{
			this.logger.warn(Messages.getString("UnmarshallerImpl.93")); //$NON-NLS-1$
			this.logger.debug(je.getLocalizedMessage(), je);
			throw new UnmarshallerException(je.getMessage(), je);
		}
		catch (MarshalException me)
		{
			this.logger.warn(Messages.getString("UnmarshallerImpl.94")); //$NON-NLS-1$
			this.logger.debug(me.getLocalizedMessage(), me);
			throw new UnmarshallerException(me.getMessage(), me);
		}
		catch (XMLSignatureException xse)
		{
			this.logger.error(Messages.getString("UnmarshallerImpl.95")); //$NON-NLS-1$ 
			this.logger.debug(xse.getLocalizedMessage(), xse);
			throw new UnmarshallerException(xse.getMessage(), xse);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.saml2.handler.Unmarshaller#unMarshallUnSigned(java.lang.String, java.lang.String,
	 *      org.w3c.dom.Node)
	 */
	public T unMarshallUnSigned(Node node) throws UnmarshallerException
	{
		this.logger.debug(Messages.getString("UnmarshallerImpl.96")); //$NON-NLS-1$

		if (node == null)
		{
			this.logger.fatal(Messages.getString("UnmarshallerImpl.16")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("UnmarshallerImpl.16")); //$NON-NLS-1$ 
		}

		try
		{
			this.logger.debug(Messages.getString("UnmarshallerImpl.100")); //$NON-NLS-1$
			validate(node);

			/* Document is valid, unmarshall object for application use */
			T unmarshalledObject;

			Unmarshaller unmarshaller = this.jaxbContext.createUnmarshaller();
			unmarshalledObject = (T) unmarshaller.unmarshal(node);
			return unmarshalledObject;
		}
		catch (JAXBException je)
		{
			this.logger.warn(Messages.getString("UnmarshallerImpl.101")); //$NON-NLS-1$ 
			this.logger.debug(je.getLocalizedMessage(), je);
			throw new UnmarshallerException(je.getMessage(), je);
		}
		catch (IOException ioe)
		{
			this.logger.warn(Messages.getString("UnmarshallerImpl.103")); //$NON-NLS-1$ 
			this.logger.debug(ioe.getLocalizedMessage(), ioe);
			throw new UnmarshallerException(ioe.getMessage(), ioe);
		}
		catch (SAXException se)
		{
			this.logger.warn(Messages.getString("UnmarshallerImpl.104")); //$NON-NLS-1$
			this.logger.debug(se.getLocalizedMessage(), se);
			throw new UnmarshallerException(se.getMessage(), se);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.saml2.handler.Unmarshaller#unMarshallUnSigned(java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
	public T unMarshallUnSigned(byte[] document) throws UnmarshallerException
	{
		this.logger.debug(Messages.getString("UnmarshallerImpl.105")); //$NON-NLS-1$

		if ((document == null) || (document.length <= 0))
		{
			this.logger.fatal(Messages.getString("UnmarshallerImpl.13")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("UnmarshallerImpl.13")); //$NON-NLS-1$
		}

		Document doc;
		try
		{
			this.logger.debug(Messages.getString("UnmarshallerImpl.109")); //$NON-NLS-1$
			doc = this.generateDocument(document);

			T unmarshalledObject;
			Unmarshaller unmarshaller = this.jaxbContext.createUnmarshaller();
			unmarshalledObject = (T) unmarshaller.unmarshal(doc);
			return unmarshalledObject;
		}
		catch (JAXBException je)
		{
			this.logger.warn(Messages.getString("UnmarshallerImpl.110")); //$NON-NLS-1$
			this.logger.debug(je.getMessage(), je);
			throw new UnmarshallerException(je.getMessage(), je);
		}
	}

	/**
	 * Validates the supplied document against the contructor initialized schema.
	 * 
	 * @param document
	 *            InputStream representation of the document to validate
	 * @return A Document representation of the supplied xml stream.
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	private Document validate(InputStream document) throws ParserConfigurationException, IOException, SAXException
	{
		this.logger.debug(Messages.getString("UnmarshallerImpl.111")); //$NON-NLS-1$

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

	/**
	 * Validates the supplied document against schema, returns DOMResult on success.
	 * 
	 * @param node
	 *            Node representation of the node to validate
	 * @return
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	private void validate(Node node) throws IOException, SAXException
	{
		this.logger.debug(Messages.getString("UnmarshallerImpl.113")); //$NON-NLS-1$

		Validator validator = this.schema.newValidator();
		validator.setErrorHandler(this.validationHandler);

		this.logger.debug(Messages.getString("UnmarshallerImpl.114")); //$NON-NLS-1$
		validator.validate(new DOMSource(node));
	}

	/**
	 * Validates enveloped signatures for XML documents
	 * 
	 * @param valContext
	 *            A populated DOMValidateContext
	 * @return True when enveloped signature for element is considered correct
	 * @throws UnmarshallerException
	 * @throws SignatureValueException
	 * @throws ReferenceValueException
	 */
	private void validateSignature(Document doc, PublicKey pk) throws UnmarshallerException, SignatureValueException,
			ReferenceValueException, XMLSignatureException, MarshalException
	{
		this.logger.debug(Messages.getString("UnmarshallerImpl.115")); //$NON-NLS-1$

		boolean validSig = false;
		NodeList nodeList;
		KeyResolver resolver;
		DOMValidateContext valContext;
		XMLSignature signature;
		XMLSignatureFactory xmlSigFac;

		try
		{
			/* XMLSignatureFactory instances are not thread safe outside static functions so create in local scope */
			xmlSigFac = XMLSignatureFactory.getInstance(Constants.DOM_FACTORY, (Provider) Class.forName(
					System.getProperty(Constants.JSR_MECHANISM, Constants.JSR_PROVIDER)).newInstance()); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		}
		catch (ClassNotFoundException cfe)
		{
			this.logger.fatal(Messages.getString("UnmarshallerImpl.52")); //$NON-NLS-1$ 
			this.logger.debug(cfe.getLocalizedMessage(), cfe);
			throw new UnmarshallerException(cfe.getMessage(), cfe);
		}
		catch (IllegalAccessException iae)
		{
			this.logger.fatal(Messages.getString("UnmarshallerImpl.53")); //$NON-NLS-1$ 
			this.logger.debug(iae.getLocalizedMessage(), iae);
			throw new UnmarshallerException(iae.getMessage(), iae);
		}
		catch (InstantiationException cfe)
		{
			this.logger.fatal(Messages.getString("UnmarshallerImpl.54")); //$NON-NLS-1$ 
			this.logger.debug(cfe.getLocalizedMessage(), cfe);
			throw new UnmarshallerException(cfe.getMessage(), cfe);
		}

		nodeList = doc.getElementsByTagNameNS(XMLSignature.XMLNS, SIGNATURE_ELEMENT);
		if (nodeList.getLength() == 0)
		{
			this.logger.warn(Messages.getString("UnmarshallerImpl.116")); //$NON-NLS-1$
			throw new UnmarshallerException(Messages.getString("UnmarshallerImpl.22")); //$NON-NLS-1$
		}

		for (int i = 0; i < nodeList.getLength(); i++)
		{
			if (pk != null)
				valContext = new DOMValidateContext(pk, nodeList.item(i));
			else
			{
				resolver = new KeyResolver(this.extKeyResolver);
				valContext = new DOMValidateContext(resolver, nodeList.item(i));
			}

			signature = xmlSigFac.unmarshalXMLSignature(valContext);

			validSig = signature.validate(valContext);

			if (!validSig)
			{
				this.logger.debug(Messages.getString("UnmarshallerImpl.117")); //$NON-NLS-1$

				/* Signature fault, determine why for caller */
				if (!signature.getSignatureValue().validate(valContext))
				{
					this.logger.warn(Messages.getString("UnmarshallerImpl.118")); //$NON-NLS-1$ 
					throw new SignatureValueException(Messages.getString("UnmarshallerImpl.0")); //$NON-NLS-1$ 
				}

				Iterator<Reference> j = signature.getSignedInfo().getReferences().iterator();
				while (j.hasNext())
				{
					if (!j.next().validate(valContext))
					{
						this.logger.warn(Messages.getString("UnmarshallerImpl.119")); //$NON-NLS-1$
						throw new ReferenceValueException(Messages.getString("UnmarshallerImpl.1")); //$NON-NLS-1$
					}
				}

				/* Can't assertain whats gone wrong at this point */
				this.logger.warn(Messages.getString("UnmarshallerImpl.120")); //$NON-NLS-1$
				throw new UnmarshallerException(Messages.getString("UnmarshallerImpl.2"), null); //$NON-NLS-1$
			}
		}
	}
}

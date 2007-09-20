/*
 * Copyright 2006-2007, Queensland University of Technology
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
 * Creation Date: 19/12/2006
 * 
 * Purpose: Concrete implementation of all marshalling operations supported by saml2lib-cpp
 */

#ifndef MARSHALLERIMPL_H_
#define MARSHALLERIMPL_H_

/* OpenSSL */
#include <openssl/bio.h>
#include <openssl/evp.h>
#include <openssl/pem.h>

/* STL */
#include <map>
#include <vector>
#include <string>

/* XML Security */
#include <xsec/dsig/DSIGSignature.hpp>
#include <xsec/enc/OpenSSL/OpenSSLCryptoKeyRSA.hpp>
#include <xsec/framework/XSECException.hpp>

/* Xerces */
#include <xercesc/dom/DOMDocument.hpp>
#include <xercesc/dom/DOMElement.hpp>
#include <xercesc/dom/DOMImplementation.hpp>
#include <xercesc/dom/DOMWriter.hpp>
#include <xercesc/dom/DOMException.hpp>
#include <xercesc/framework/MemBufFormatTarget.hpp>
#include <xercesc/framework/MemBufInputSource.hpp>
#include <xercesc/sax/SAXException.hpp>
#include <xercesc/util/XMLException.hpp>

/* XSD */
#include <xsd/cxx/tree/elements.hxx>
#include <xsd/cxx/tree/exceptions.hxx>

/* Local Codebase */
#include "exceptions/InvalidParameterException.h"
#include "exceptions/MarshallerException.h"
#include "handlers/Marshaller.h"
#include "handlers/SAMLDocument.h"
#include "namespace/NamespacePrefixMapper.h"
#include "SAML2Defs.h"

XERCES_CPP_NAMESPACE_USE

namespace saml2
{
	/*
	 * Concrete implementation of marshalling operations supported by the library.
	 */
	template <class T>
	class MarshallerImpl : public saml2::Marshaller<T>
	{
		public:
			MarshallerImpl( std::string schemaDir, std::vector<std::string> schemaList, std::string rootElementName, std::string namespaceURI );
			MarshallerImpl( std::string schemaDir, std::vector<std::string> schemaList, std::string rootElementName, std::string namespaceURI, std::string keyPairName, XSECCryptoKey* pk );
			~MarshallerImpl();

			SAMLDocument  marshallSigned( T*  xmlObj, std::vector<std::string> idList );
			SAMLDocument  marshallUnSigned( T*  xmlObj  );
			SAMLDocument  marshallUnSignedElement( T*  xmlObj );
			SAMLDocument  generateOutput ( DOMElement* elem );
			
		private:
			std::string schemaDir;
			std::vector<std::string> schemaList;
			std::string rootElementName;
			std::string namespaceURI;

			XMLCh* defaultURI;
			XMLCh* rootElement;
			XMLCh* xmlns;
			XMLCh* implFlags;
			XMLCh* encoding;
			XMLCh* dsigURI;
			XMLCh* dsigSigElem;
			XMLCh* dsigNS;
			XMLCh* dsigKeyPairName;
			XMLCh* samlSiblingInsertAfter;

			DOMImplementation* domImpl;
			XSECCryptoKey* pk;

			NamespacePrefixMapper namespaceMapper;

			SAML2ErrorHandler* errorHandler;
			ResourceResolver* resourceResolver;

			void init();
			void sign (DOMDocument* domDoc, std::vector<std::string> idList);
			
			DOMDocument* validate (DOMDocument* domDoc);
			DOMImplementation* generateDOMImplementation();
			DOMDocument* generateDOMDocument( T* xmlObj );
			SAMLDocument generateOutput ( DOMDocument* domDoc );
			DOMElement* generateDOMElement( T* xmlObj );
	};

	/*
	 * Constructor for marshaller instances that are not required to do any cryptography
	 */
	template <class T>
	MarshallerImpl<T>::MarshallerImpl( std::string schemaDir, std::vector<std::string> schemaList, std::string rootElementName, std::string namespaceURI )
	{
		this->schemaDir = schemaDir;
		this->schemaList = schemaList;
		this->rootElementName = rootElementName;
		this->namespaceURI = namespaceURI;
		this->pk = NULL;
		
		this->dsigKeyPairName = NULL;

		init();
	}

	/*
	 * Constructor for marshaller instances that are required to do cryptography.
	 * 
	 * Caller is responsible for memory management of all passed data.
	 */
	template <class T>
	MarshallerImpl<T>::MarshallerImpl( std::string schemaDir, std::vector<std::string> schemaList, std::string rootElementName, std::string namespaceURI, std::string keyPairName, XSECCryptoKey* pk )
	{
		if(pk == NULL)
			SAML2LIB_INVPARAM_EX("Supplied private key was NULL");
		
		this->schemaDir = schemaDir;
		this->schemaList = schemaList;
		this->rootElementName = rootElementName;
		this->namespaceURI = namespaceURI;
		this->pk = pk;

		init();
		this->dsigKeyPairName = XMLString::transcode(keyPairName.c_str());
	}

	/*
	 * Destructor, frees all memory and terminates usage of Xerces and XSEC
	 */
	template <class T>
	MarshallerImpl<T>::~MarshallerImpl()
	{
		// No need to 'NULL' the pointers here - they won't be used again.
		delete errorHandler;
		delete resourceResolver;

		XMLString::release(&defaultURI);
		XMLString::release(&rootElement);
		XMLString::release(&implFlags);
		XMLString::release(&xmlns);
		XMLString::release(&encoding);
		XMLString::release(&dsigURI);
		XMLString::release(&dsigSigElem);
		XMLString::release(&dsigNS);
		XMLString::release(&samlSiblingInsertAfter);
		
		if(dsigKeyPairName != NULL)
			XMLString::release(&dsigKeyPairName);
		
		// According to the xml-security-c API docs, Terminate() can be called once for each time Initialize() was called.
		// This means that this won't affect other instances of Marshallers that we have.
		XSECPlatformUtils::Terminate();
		XMLPlatformUtils::Terminate();
	}

	/*
	 * Initialization tasks common to both types of marshaller, initializes members,
	 * Xerces and XSEC libraries
	 */
	template <class T>
	void MarshallerImpl<T>::init()
	{
		XMLPlatformUtils::Initialize();
		XSECPlatformUtils::Initialise();

		this->defaultURI = XMLString::transcode(this->namespaceURI.c_str());
		this->rootElement = XMLString::transcode(this->rootElementName.c_str());
		this->implFlags = XMLString::transcode( IMPL_FLAGS );
		this->xmlns = XMLString::transcode( XMLNS );
		this->encoding = XMLString::transcode( ENCODING );
		this->dsigURI = XMLString::transcode ( DSIG_URI );
		this->dsigSigElem = XMLString::transcode ( DSIG_SIG_ELEM );
		this->dsigNS = XMLString::transcode( DSIG_NS );
		this->samlSiblingInsertAfter = XMLString::transcode( SAML_SIBLING_INSERT_AFTER );

		this->domImpl = DOMImplementationRegistry::getDOMImplementation(this->implFlags);

		this->errorHandler =  new SAML2ErrorHandler();
		this->resourceResolver = new ResourceResolver( this->schemaDir );
	}

	/*
	 * Marshalls and performs cryptography on the presented object to a DOMDocument
	 * 
	 * @param xmlObj input object of generic type which the marshaller is acting for (XSD generated)
	 * @param idList a list of ID's in the document to be signed. Id's should present in outter most to inner most order,
	 * ie: Root element Id would be presented first with subsequent Id's to be signed in the document coming later,
	 * all Id's in each "branch" of the document must be presented before moving to the next branch
	 * 
	 * @exception MarshallerException, wrapped exception detailing error state
	 * @exception InvalidParameterException, when invalid data is fed to the library
	 */
	template <class T>
	SAMLDocument MarshallerImpl<T>::marshallSigned ( T*  xmlObj, std::vector<std::string> idList )
	{
		DOMDocument* domDoc = NULL;
		SAMLDocument samlDocument;
		
		if(xmlObj == NULL)
			SAML2LIB_INVPARAM_EX("Supplied xml object was NULL");
		
		if(this->dsigKeyPairName == NULL)
			SAML2LIB_MAR_EX( "Incorrect constructor called for signing document, re-initialise marshaller correctly" );

		try
		{
			domDoc = this->generateDOMDocument ( xmlObj );
			domDoc = validate(domDoc);

			sign(domDoc, idList);
			samlDocument = this->generateOutput( domDoc );
			domDoc->release();
			domDoc = NULL;
		}
		catch(xsd::cxx::tree::serialization< wchar_t >  &exc)
		{
			SAML2LIB_MAR_EX_CAUSE( "XSD Serialization exception while marshalling signed document", exc.what() );
		}
		catch(xsd::cxx::tree::expected_element< wchar_t >  &exc)
		{
			SAML2LIB_MAR_EX_CAUSE( "XSD Excpected Element exception while marshalling signed document", exc.what() );
		}
		catch(xsd::cxx::tree::no_namespace_mapping< wchar_t >  &exc)
		{
			SAML2LIB_MAR_EX_CAUSE( "XSD No Namespace Mapping exception while marshalling signed document", exc.what() );
		}
		catch(xsd::cxx::tree::no_prefix_mapping< wchar_t >  &exc)
		{
			SAML2LIB_MAR_EX_CAUSE( "XSD No Prefix Mapping exception while marshalling signed document", exc.what() );
		}
		catch(xsd::cxx::tree::xsi_already_in_use< wchar_t >  &exc)
		{
			SAML2LIB_MAR_EX_CAUSE( "XSD XSI Already In Use exception while marshalling signed document", exc.what() );
		}
		catch(xsd::cxx::tree::exception< wchar_t >  &exc)
		{
			SAML2LIB_MAR_EX_CAUSE( "XSD generic exception while marshalling signed document", exc.what() );
		}
		catch (std::bad_alloc &exc)
		{
			SAML2LIB_MAR_EX_CAUSE( "Bad memory alloc while marshalling signed document", exc.what() );
		}
		catch(SAXException &exc)
		{
			SAML2LIB_MAR_EX_CAUSE( "SAXException while marshalling signed document", exc.getMessage() );
		}
		catch(DOMException &exc)
		{
			SAML2LIB_MAR_EX_CAUSE( "DOMException while marshalling signed document", exc.getMessage() );
		}
		catch(XMLException &exc)
		{
			SAML2LIB_MAR_EX_CAUSE( "XMLException while marshalling signed document", exc.getMessage() );
		}
		catch(std::exception &exc)
		{
			SAML2LIB_MAR_EX_CAUSE( "Exception while marshalling signed document", exc.what() );
		}
		catch(...)
		{
			SAML2LIB_MAR_EX( "Generic exception while marshalling signed document" );
		}
		
		return samlDocument;
	}

	/*
	 * Marshalls the presented object to a DOMDocument
	 * 
	 * @param xmlObj input object of generic type which the marshaller is acting for (XSD generated)
	 *
 	 * @exception MarshallerException, wrapped exception detailing error state
	 * @exception InvalidParameterException, when invalid data is fed to the library
 	 */
	template <class T>
	SAMLDocument MarshallerImpl<T>::marshallUnSigned (T* xmlObj)
	{
		DOMDocument* domDoc;
		SAMLDocument samlDocument;

		if(xmlObj == NULL)
			SAML2LIB_INVPARAM_EX("Supplied xml object was NULL");
			
		try
		{
			domDoc = this->generateDOMDocument ( xmlObj );
			samlDocument = this->generateOutput( domDoc );
			domDoc->release();
			domDoc = NULL;
		}
		catch(xsd::cxx::tree::serialization< wchar_t >  &exc)
		{
			SAML2LIB_MAR_EX_CAUSE( "XSD Serialization exception while marshalling unsigned document", exc.what() );
		}
		catch(xsd::cxx::tree::expected_element< wchar_t >  &exc)
		{
			SAML2LIB_MAR_EX_CAUSE( "XSD Excpected Element exception while marshalling unsigned document", exc.what() );
		}
		catch(xsd::cxx::tree::no_namespace_mapping< wchar_t >  &exc)
		{
			SAML2LIB_MAR_EX_CAUSE( "XSD No Namespace Mapping exception while marshalling unsigned document", exc.what() );
		}
		catch(xsd::cxx::tree::no_prefix_mapping< wchar_t >  &exc)
		{
			SAML2LIB_MAR_EX_CAUSE( "XSD No Prefix Mapping exception while marshalling unsigned document", exc.what() );
		}
		catch(xsd::cxx::tree::xsi_already_in_use< wchar_t >  &exc)
		{
			SAML2LIB_MAR_EX_CAUSE( "XSD XSI Already In Use exception while marshalling unsigned document", exc.what() );
		}
		catch(xsd::cxx::tree::exception< wchar_t >  &exc)
		{
			SAML2LIB_MAR_EX_CAUSE( "XSD generic exception while marshalling unsigned document", exc.what() );
		}
		catch (std::bad_alloc &exc)
		{
			SAML2LIB_MAR_EX_CAUSE( "Bad memory alloc while marshalling unsigned document", exc.what() );
		}
		catch(SAXException &exc)
		{
			SAML2LIB_MAR_EX_CAUSE( "SAXException while marshalling unsigned document", exc.getMessage() );
		}
		catch(DOMException &exc)
		{
			SAML2LIB_MAR_EX_CAUSE( "DOMException while marshalling unsigned document", exc.getMessage() );
		}
		catch(XMLException &exc)
		{
			SAML2LIB_MAR_EX_CAUSE( "XMLException while marshalling unsigned document", exc.getMessage() );
		}
		catch(std::exception &exc)
		{
			SAML2LIB_MAR_EX_CAUSE( "Exception while marshalling unsigned document", exc.what() );
		}
		catch(...)
		{
			SAML2LIB_MAR_EX( "Generic exception while marshalling unsigned document" );
		}
		
		return samlDocument;
	}

	/*
	 * Marshalls the presented object to a DOMElement
	 * 
	 * @param xmlObj input object of generic type which the marshaller is acting for (XSD generated)
	 *
 	 * @exception MarshallerException, wrapped exception detailing error state
	 * @exception InvalidParameterException, when invalid data is fed to the library
 	 */
	template <class T>
	SAMLDocument MarshallerImpl<T>::marshallUnSignedElement ( T*  xmlObj )
	{
		DOMElement* elem;
		SAMLDocument samlDocument;

		if(xmlObj == NULL)
			SAML2LIB_INVPARAM_EX("Supplied xml element was NULL");
				
		try
		{
			elem = generateDOMElement( xmlObj );
			samlDocument = this->generateOutput( elem );
		}
		catch(xsd::cxx::tree::serialization< wchar_t >  &exc)
		{
			SAML2LIB_MAR_EX_CAUSE( "XSD Serialization exception while marshalling unsigned element", exc.what() );
		}
		catch(xsd::cxx::tree::expected_element< wchar_t >  &exc)
		{
			SAML2LIB_MAR_EX_CAUSE( "XSD Excpected Element exception while marshalling unsigned element", exc.what() );
		}
		catch(xsd::cxx::tree::no_namespace_mapping< wchar_t >  &exc)
		{
			SAML2LIB_MAR_EX_CAUSE( "XSD No Namespace Mapping exception while marshalling unsigned element", exc.what() );
		}
		catch(xsd::cxx::tree::no_prefix_mapping< wchar_t >  &exc)
		{
			SAML2LIB_MAR_EX_CAUSE( "XSD No Prefix Mapping exception while marshalling unsigned element", exc.what() );
		}
		catch(xsd::cxx::tree::xsi_already_in_use< wchar_t >  &exc)
		{
			SAML2LIB_MAR_EX_CAUSE( "XSD XSI Already In Use exception while marshalling unsigned element", exc.what() );
		}
		catch(xsd::cxx::tree::exception< wchar_t >  &exc)
		{
			SAML2LIB_MAR_EX_CAUSE( "XSD generic exception while marshalling unsigned element", exc.what() );
		}
		catch (std::bad_alloc &exc)
		{
			SAML2LIB_MAR_EX_CAUSE( "Bad memory alloc while marshalling unsigned element", exc.what() );
		}
		catch(SAXException &exc)
		{
			SAML2LIB_MAR_EX_CAUSE( "SAXException while marshalling unsigned element", exc.getMessage() );
		}
		catch(DOMException &exc)
		{
			SAML2LIB_MAR_EX_CAUSE( "DOMException while marshalling unsigned element", exc.getMessage() );
		}
		catch(XMLException &exc)
		{
			SAML2LIB_MAR_EX_CAUSE( "XMLException while marshalling unsigned element", exc.getMessage() );
		}
		catch(std::exception &exc)
		{
			SAML2LIB_MAR_EX_CAUSE( "Exception while marshalling unsigned element", exc.what() );
		}
		catch(...)
		{
			SAML2LIB_MAR_EX( "Generic exception while marshalling unsigned element" );
		}
		
		return samlDocument;
	}

	/*
	 * Translates generated DOMDocument into a series of bytes to return to caller
	 */
	template <class T>
	SAMLDocument MarshallerImpl<T>::generateOutput ( DOMDocument* domDoc )
	{
		DOMWriter* writer = NULL;
		MemBufFormatTarget *formatTarget = NULL;

		try
		{
			writer = this->domImpl->createDOMWriter();
			formatTarget = new MemBufFormatTarget();

			/* Ensure we generate BOM */
			writer->setFeature(XMLUni::fgDOMWRTBOM, true);
			writer->setEncoding( this->encoding );
			writer->writeNode(formatTarget, *domDoc);

			long length = formatTarget->getLen();
			SAMLByte *document = new SAMLByte[length];
			std::memcpy(document, formatTarget->getRawBuffer(), length);

			delete formatTarget;
			formatTarget = NULL;
			
			writer->release();
			writer = NULL;
			
			return SAMLDocument( document, length );
		}
		catch(...)
		{
			if(formatTarget != NULL)
			{
				delete formatTarget;
				formatTarget = NULL;
			}

			if(writer != NULL)
			{
				writer->release();
				writer = NULL;
			}

			throw;
		}
	}
	
	/*
	 * Translates generated DOMElement into a series of bytes to return to caller
	 */
	template <class T>
	SAMLDocument MarshallerImpl<T>::generateOutput ( DOMElement* elem )
	{
		DOMWriter* writer = NULL;
		MemBufFormatTarget *formatTarget = NULL;

		try
		{
			writer = this->domImpl->createDOMWriter();
			formatTarget = new MemBufFormatTarget();

			/* Ensure we generate BOM */
			writer->setFeature(XMLUni::fgDOMWRTBOM, true);
			writer->setEncoding( this->encoding );
			writer->writeNode(formatTarget, *elem);

			long length = formatTarget->getLen();
			SAMLByte *document = new SAMLByte[length];
			std::memcpy(document, formatTarget->getRawBuffer(), length);

			delete formatTarget;
			formatTarget = NULL;
			
			writer->release();
			writer = NULL;
			
			return SAMLDocument( document, length );
		}
		catch(...)
		{
			if(formatTarget != NULL)
			{
				delete formatTarget;
				formatTarget = NULL;
			}

			if(writer != NULL)
			{
				writer->release();
				writer = NULL;
			}

			throw;
		}
	}

	/*
	 * Retrieves DOMImplmentation from Xerces registry
	 */
	template <class T>
	DOMImplementation* MarshallerImpl<T>::generateDOMImplementation()
	{
		DOMImplementation* domImpl;
		domImpl = DOMImplementationRegistry::getDOMImplementation(this->implFlags);
		return domImpl;
	}
	
	/*
	 * Utilising XSD generated code translates the supplied object into
	 * Xerces DOMDocument format for further processing
	 */
	template <class T>
	DOMDocument* MarshallerImpl<T>::generateDOMDocument( T* xmlObj )
	{
		DOMDocument* domDoc = NULL;
		DOMElement* rootElem = NULL;

		XMLCh* uriVal = NULL;
		XMLCh* qualName = NULL;

		try
		{
			domDoc = this->domImpl->createDocument(defaultURI, rootElement, 0);
			domDoc->setStandalone(true); //TODO: confirm this is accurate
			rootElem = domDoc->getDocumentElement();

			std::map<char*, char*> namespaces = namespaceMapper.getNamespaces();
			for( std::map<char*, char*>::iterator i = namespaces.begin(); i != namespaces.end(); i++ )
			{
				qualName = XMLString::transcode(i->first);
				uriVal = XMLString::transcode(i->second);
				rootElem->setAttributeNS ( this->xmlns, qualName, uriVal);

				XMLString::release(&qualName);
				XMLString::release(&uriVal);
				uriVal = NULL;
				qualName = NULL;
			}

			/* Populate the root node of the generated DOMDocument with data from the supplied
			 * object generated by XSD schema translator
			 */
			*rootElem << *xmlObj;
		}
		catch(...)
		{
			if(uriVal != NULL)
			{
				XMLString::release(&uriVal);
				uriVal = NULL;
			}

			if(qualName != NULL)
			{
				XMLString::release(&qualName);
				qualName = NULL;
			}

			throw;
		}
		
		return domDoc;
	}

	/*
	 * Utilising XSD generated code translates the supplied object into
	 * Xerces DOMElement format for further processing
	 */
	template <class T>
	DOMElement* MarshallerImpl<T>::generateDOMElement( T* xmlObj )
	{
		DOMDocument* domDoc = NULL;
		DOMElement* rootElem = NULL;

		XMLCh* uriVal = NULL;
		XMLCh* qualName = NULL;

		try
		{
			domDoc = this->domImpl->createDocument(defaultURI, rootElement, 0);
			domDoc->setStandalone(true); //TODO: confirm this is accurate
			rootElem = domDoc->getDocumentElement();

			std::map<char*, char*> namespaces = namespaceMapper.getNamespaces();
			for( std::map<char*, char*>::iterator i = namespaces.begin(); i != namespaces.end(); i++ )
			{
				qualName = XMLString::transcode(i->first);
				uriVal = XMLString::transcode(i->second);
				rootElem->setAttributeNS ( this->xmlns, qualName, uriVal);

				XMLString::release(&qualName);
				XMLString::release(&uriVal);
				uriVal = NULL;
				qualName = NULL;
			}

			/* Populate the root node of the generated DOMDocument with data from the supplied
			 * object generated by XSD schema translator
			 */
			*rootElem << *xmlObj;
		}
		catch(...)
		{
			if(uriVal != NULL)
			{
				XMLString::release(&uriVal);
				uriVal = NULL;
			}

			if(qualName != NULL)
			{
				XMLString::release(&qualName);
				qualName = NULL;
			}

			throw;
		}
		
		return rootElem;
	}

	/*
	 * Utilises Xerces DOM level 3 DOMBuilder object to parse and validate the supplied DOMDocument
	 * instance, returns instance of DOMDocument generated by parsing
	 */
	template <class T>
	DOMDocument* MarshallerImpl<T>::validate (DOMDocument* domDoc)
	{
		Wrapper4InputSource* wrapedSource = NULL;
		DOMBuilder* parser = NULL;
		DOMWriter* writer = NULL;
		MemBufInputSource* inputSource = NULL;
		MemBufFormatTarget *formatTarget = NULL;
		XMLCh* schema = NULL;

		try
		{
			parser = this->domImpl->createDOMBuilder(DOMImplementationLS::MODE_SYNCHRONOUS, NULL);

			/* Setup all the required parser variables, NB dont trust 'defaults' */
			parser->setErrorHandler(this->errorHandler);
			parser->setEntityResolver(this->resourceResolver);

			parser->setFeature(XMLUni::fgXercesSchema, true);
			parser->setFeature(XMLUni::fgDOMValidation, true);
			parser->setFeature(XMLUni::fgDOMNamespaces, true);
			parser->setFeature(XMLUni::fgDOMWhitespaceInElementContent, false);
			parser->setFeature(XMLUni::fgXercesUseCachedGrammarInParse, true);
			parser->setFeature(XMLUni::fgXercesUserAdoptsDOMDocument, true);

			/* This is particuarly important so that signatures are not corrupted*/
			parser->setFeature(XMLUni::fgDOMDatatypeNormalization, false);

			for(std::vector<std::string>::iterator i = this->
			        schemaList.begin();
			        i != this->schemaList.end();
			        i++)
			{
				schema = XMLString::transcode ( i->c_str() );
				parser->loadGrammar(schema, Grammar::SchemaGrammarType, true);
				XMLString::release(&schema);
			}

			/* Xerces parser requires input in the form of a mem buffer and can't at this time
			 * take a straight DOMDocument, use the DOMWriter below to set up the environment for validating
			 * our outgoing XML representation
			 */
			writer = (this->domImpl->createDOMWriter ());
			formatTarget = new MemBufFormatTarget();

			/* This is particuarly important so that signatures are not corrupted*/
			writer->setFeature(XMLUni::fgDOMWRTDiscardDefaultContent, false);

			writer->setEncoding( this->encoding );
			writer->writeNode(formatTarget, *domDoc);
			
			writer->release();
			writer = NULL;
			
			domDoc->release();
			domDoc = NULL;
			
			inputSource = new MemBufInputSource( formatTarget->getRawBuffer(), formatTarget->getLen(), MARSHALLER_ID );
			wrapedSource = new Wrapper4InputSource(inputSource);
			domDoc = parser->parse(*wrapedSource);

			parser->release();
			parser = NULL;
			
			wrapedSource->release();
			wrapedSource = NULL;
			
			delete formatTarget;
		}
		catch(...)
		{
			/* Clean up any memory allocated and throw back up stack */
			if(schema != NULL)
			{
				XMLString::release(&schema);
				schema = NULL;
			}

			if(writer != NULL)
			{
				writer->release();
				writer = NULL;
			}

			if(domDoc != NULL)
			{
				domDoc->release();
				domDoc = NULL;
			}

			if (wrapedSource != NULL)
			{
				wrapedSource->release();
				wrapedSource = NULL;
			}
				
			if(parser != NULL)
			{
				parser->release();
				parser = NULL;
			}

			throw;
		}
		
		return domDoc;
	}

	/*
	 * Performs all cryptography operations associated with signing the supplied domDocument,
	 * utilises the apache XML Security C library
	 */
	template <class T>
	void MarshallerImpl<T>::sign (DOMDocument* domDoc, std::vector<std::string> idList)
	{
		XSECProvider prov;
		DSIGSignature* sig = NULL;
		DOMElement* sigNode = NULL;
		DOMElement* elem = NULL;;
		DSIGReference* ref = NULL;
		
		XMLCh* id = NULL;
		XMLCh* refURL = NULL;
		std::string currentId;
		
		try
		{										
			for(std::vector<std::string>::reverse_iterator i = idList.rbegin(); i < idList.rend(); i++)
			{
				/* Create Id and reference URL data types to xmldisg spec */
				currentId = *i;
				
				id = XMLString::transcode( currentId.c_str() );	
				currentId.insert(0, DSIG_ID_LEAD_CH);
				refURL = XMLString::transcode( currentId.c_str() );
							
				/* Get the parent element for which we wish to create a signature by Id, create a new signature element
				 * assign the Marshaller private key for signing purposes, create all references and sign the actual document.
				 * This process is repeated for each Id the caller wishes to sign in the reverse order to which Id's are presented,
				 * to ensure that outer signature blocks are calculated with inner signature blocks as part of the byte stream.
				 */
				elem = domDoc->getElementById( id );
				
				if(elem == NULL)
					SAML2LIB_MAR_EX( "Provided Id was not able to be located in document as a reference to a suitable parent element" );
					
				sig = prov.newSignature();
				sig->setDSIGNSPrefix( this->dsigNS );
				sig->setECNSPrefix( NULL );
				sig->setXPFNSPrefix( NULL );
				sig->setPrettyPrint( false );
				
				sigNode = sig->createBlankSignature(domDoc, CANON_C14NE_NOC, SIGNATURE_RSA, HASH_SHA1);
				/* Keydata is cloned and owned by crypto library here which it will clean up */
				// TODO Probably leaking memory here - I think you need to delete the cloned key -SM
				sig->setSigningKey(this->pk->clone());
				sig->appendKeyName(this->dsigKeyPairName, false);
				
				ref = sig->createReference( refURL, HASH_SHA1);
				ref->appendEnvelopedSignatureTransform();
				ref->appendCanonicalizationTransform(CANON_C14NE_NOC);
	
				if(elem->hasChildNodes())
				{
					/* This needs to be investigated further when time permits to find a better way to position
					 * the generated Signature element to ensure a valid document, at the moment with SAML 2.0 schema
					 * we know the Signature block is either the first child of the parent node or the second child of the parent node
					 * if an optional Issuer element is present, at any rate its not 100% clean and should be fixed up
					 */
					if( XMLString::equals(elem->getFirstChild()->getNodeName(), samlSiblingInsertAfter) )
						if(elem->getFirstChild()->getNextSibling() == NULL)
							elem->appendChild(sigNode);
						else
							elem->insertBefore(sigNode, elem->getFirstChild()->getNextSibling());
					else
						elem->insertBefore(sigNode, elem->getFirstChild());
				}
				else
				{
					elem->appendChild(sigNode);
				}
	
				sig->sign();	
				prov.releaseSignature(sig);
				sig = NULL;
				
				XMLString::release(&id);
				id = NULL;
				XMLString::release(&refURL);
				refURL = NULL;
			}
		}
		catch (XSECCryptoException &exc)
		{		
			if(domDoc != NULL)
			{
				domDoc->release();
				domDoc = NULL;
			}
				
			if(sig != NULL)
			{
				prov.releaseSignature(sig);
				sig = NULL;
			}
				
			if(id != NULL)
			{
				XMLString::release(&id);
				id = NULL;
			}
				
			if(refURL != NULL)
			{
				XMLString::release(&refURL);
				refURL = NULL;
			}
				
			SAML2LIB_MAR_EX_CAUSE( "An exception occurred during an encryption operation", exc.getMsg() );
		}
		catch (XSECException &exc)
		{
			if(domDoc != NULL)
			{
				domDoc->release();
				domDoc = NULL;
			}
				
			if(sig != NULL)
			{
				prov.releaseSignature(sig);
				sig = NULL;
			}
				
			if(id != NULL)
			{
				XMLString::release(&id);
				id = NULL;
			}
				
			if(refURL != NULL)
			{
				XMLString::release(&refURL);
				refURL = NULL;
			}
				
			SAML2LIB_MAR_EX_CAUSE( "An exception occurred during a security operation", exc.getMsg() );
		}
		catch(...)
		{
			if(domDoc != NULL)
			{
				domDoc->release();
				domDoc = NULL;
			}
				
			if(sig != NULL)
			{
				prov.releaseSignature(sig);
				sig = NULL;
			}
				
			if(id != NULL)
			{
				XMLString::release(&id);
				id = NULL;
			}
				
			if(refURL != NULL)
			{
				XMLString::release(&refURL);
				refURL = NULL;
			}
				
			throw;
		}
	}
}

#endif /*MARSHALLERIMPL_H_*/

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
 * Purpose: Concrete implementation of all unmarshalling operations supported by saml2lib-cpp
 */

#ifndef UNMARSHALLERIMPL_H_
#define UNMARSHALLERIMPL_H_

/* STL */
#include <exception>
#include <string>
#include <vector>

/* XML Security */
#include <xsec/dsig/DSIGSignature.hpp>
#include <xsec/dsig/DSIGReference.hpp>
#include <xsec/dsig/DSIGTransformList.hpp>
#include <xsec/enc/XSECCryptoException.hpp>
#include <xsec/enc/XSECCryptoKey.hpp>
#include <xsec/framework/XSECProvider.hpp>
#include <xsec/framework/XSECException.hpp>

/* Xerces */
#include <xercesc/dom/DOMDocument.hpp>
#include <xercesc/dom/DOMElement.hpp>
#include <xercesc/dom/DOMBuilder.hpp>
#include <xercesc/dom/DOMWriter.hpp>
#include <xercesc/framework/MemBufFormatTarget.hpp>
#include <xercesc/framework/MemBufInputSource.hpp>
#include <xercesc/framework/Wrapper4InputSource.hpp>
#include <xercesc/sax/SAXException.hpp>
#include <xercesc/util/PlatformUtils.hpp>
#include <xercesc/util/XMLException.hpp>
#include <xercesc/validators/schema/SchemaGrammar.hpp>

/* XSD */
#include <xsd/cxx/tree/elements.hxx>
#include <xsd/cxx/tree/exceptions.hxx>

/* Local Codebase */
#include "saml2/exceptions/InvalidParameterException.h"
#include "saml2/exceptions/UnmarshallerException.h"
#include "saml2/handlers/Unmarshaller.h"
#include "saml2/handlers/SAML2ErrorHandler.h"
#include "saml2/handlers/MetadataOutput.h"
#include "saml2/resolver/ResourceResolver.h"
#include "saml2/resolver/ExternalKeyResolver.h"
#include "saml2/SAML2Defs.h"
#include "saml2/logging/api.h"
#include "saml2/xsd/xml-schema.hxx"

#include "saml2/handlers/SAMLDocument.h"

XERCES_CPP_NAMESPACE_USE

namespace saml2
{
	/*
	 * Concreate implementation of all unmarshalling operations supported by the library
	 */
	template <class T>
	class UnmarshallerImpl : public saml2::Unmarshaller<T>
	{
		public:
			UnmarshallerImpl(Logger* logger, std::string schemaDir, std::vector<std::string> schemaList);
			UnmarshallerImpl(Logger* logger, std::string schemaDir, std::vector<std::string> schemaList, ExternalKeyResolver* extKeyResolver);

			~UnmarshallerImpl();

			T* unMarshallSigned (const SAMLDocument& document, XSECCryptoKey* pk, bool keepDOM = false);
			T* unMarshallSigned (const SAMLDocument& document, bool keepDOM = false);
			T* unMarshallUnSigned (const SAMLDocument& document, bool keepDOM = false);
			T* unMarshallUnSignedElement (DOMElement* elem, bool keepDOM = false);
			saml2::MetadataOutput<T>* unMarshallMetadata (const SAMLDocument& document, bool keepDOM = false);
			void validateSignature(DOMDocument* doc, XSECCryptoKey* pk = NULL);
			
		private:
			LocalLogger localLogger;
			
			void init();
			DOMDocument* validate(const SAMLDocument& document);

			ExternalKeyResolver* extKeyResolver;
			std::vector<std::string> schemaList;
			std::string schemaDir;

			SAML2ErrorHandler* errorHandler;
			ResourceResolver* resourceResolver;

			XMLCh* implFlags;
			XMLCh* dsigURI;
			XMLCh* dsigSigElem;
			XMLCh* metadataURI;
			XMLCh* keyDescriptor;
			XMLCh* keyInfo;
			XMLCh* keyName;
			XMLCh* keyValue;
			XMLCh* rsaKeyValue;
			XMLCh* dsaKeyValue;
			XMLCh* dsaP;
			XMLCh* dsaQ;
			XMLCh* dsaG;
			XMLCh* dsaY;
			XMLCh* dsaJ;
			
			DOMImplementation* domImpl;
	};

	/*
	 * Constructor for unmarshaller instances that do not require usage of an external key resolver
	 */
	template <class T>
	UnmarshallerImpl<T>::UnmarshallerImpl(Logger* logger, std::string schemaDir, std::vector<std::string> schemaList)
	: localLogger(logger, "saml2::UnmarshallerImpl")
	{
		try
		{
			this->schemaDir = schemaDir;
			this->schemaList = schemaList;
			this->extKeyResolver = NULL;

			init();
		}
		catch (XMLException &exc)
		{
			SAML2LIB_UNMAR_EX( "Error during initialisation of Xerces and XMLSec" );
		}
	}

	/*
	 * Constructor for unmarshaller instances that do require usage of an external key resolver
	 */
	template <class T>
	UnmarshallerImpl<T>::UnmarshallerImpl(Logger* logger, std::string schemaDir, std::vector<std::string> schemaList, ExternalKeyResolver* extKeyResolver)
	: localLogger(logger, "saml2::UnmarshallerImpl")
	{
		if(extKeyResolver == NULL)
			SAML2LIB_INVPARAM_EX("Supplied external key resolver was NULL");
			
		try
		{
			this->schemaDir = schemaDir;
			this->schemaList = schemaList;
			this->extKeyResolver = extKeyResolver;

			init();
		}
		catch (XMLException &exc)
		{
			SAML2LIB_UNMAR_EX_CAUSE( "Error during initialisation of Xerces and XMLSec", exc.getMessage() );
		}
	}

	/*
	 * Destructor, frees all memory and terminates usage of Xerces and XSEC
	 */
	template <class T>
	UnmarshallerImpl<T>::~UnmarshallerImpl()
	{
		
		delete errorHandler;
		delete resourceResolver;

		/* Cleanup XMLCh strings */
		XMLString::release(&implFlags);
		XMLString::release(&dsigURI);
		XMLString::release(&dsigSigElem);
		XMLString::release(&metadataURI);
		XMLString::release(&keyDescriptor);
		XMLString::release(&keyInfo);
		XMLString::release(&keyName);
		XMLString::release(&keyValue);
		XMLString::release(&rsaKeyValue);
		XMLString::release(&dsaKeyValue);
		XMLString::release(&dsaP);
		XMLString::release(&dsaQ);
		XMLString::release(&dsaG);
		XMLString::release(&dsaY);
		XMLString::release(&dsaJ);
		
		XSECPlatformUtils::Terminate();
		XMLPlatformUtils::Terminate();
	}

	/*
	 * Initialization tasks common to both types of unmarshaller, initializes members,
	 * Xerces and XSEC libraries
	 */
	template <class T>
	void UnmarshallerImpl<T>::init()
	{
		XMLPlatformUtils::Initialize();
		XSECPlatformUtils::Initialise();

		errorHandler = NULL;
		resourceResolver = NULL;

		errorHandler =  new SAML2ErrorHandler();
		resourceResolver = new ResourceResolver( this->schemaDir );

		implFlags = XMLString::transcode( IMPL_FLAGS );
		dsigURI = XMLString::transcode ( DSIG_URI );
		dsigSigElem = XMLString::transcode ( DSIG_SIG_ELEM );
		metadataURI = XMLString::transcode ( METADATA_URI );
		keyDescriptor = XMLString::transcode ( KEY_DESCRIPTOR );
		keyInfo = XMLString::transcode ( KEY_INFO );
		keyName = XMLString::transcode ( KEY_NAME );
		keyValue = XMLString::transcode ( KEY_VALUE );
		rsaKeyValue = XMLString::transcode ( RSA_KEY_VALUE );
		dsaKeyValue = XMLString::transcode ( DSA_KEY_VALUE );
		dsaP = XMLString::transcode ( DSA_P );
		dsaQ = XMLString::transcode ( DSA_Q );
		dsaG = XMLString::transcode ( DSA_G );
		dsaY = XMLString::transcode ( DSA_Y );
		dsaJ = XMLString::transcode ( DSA_J );

		domImpl = DOMImplementationRegistry::getDOMImplementation(implFlags);
	}

	/*
	 * Unmarshalls an instance of a SAML document and verifies all cryptography, on success returns the
	 * associated XSD generated object representation for further processing
	 * 
	 * @param doc Byte representation of the document to process, caller responsible for memory management
	 * @param size Number of bytes present in the doc buffer that should be processed
	 * @param pk The public key to be used for cryptography validation operations, caller responsible for memory management
	 * @param keepDOM Indicates if the dom structure should be retained for use by the caller, default false
	 * 
	 * @exception UnmarshallerException, wrapped exception detailing error state
	 * @exception InvalidParameterException, when invalid data is fed to the library
	 */
	template <class T>
	T*  UnmarshallerImpl<T>::unMarshallSigned (const SAMLDocument& document, XSECCryptoKey* pk, bool keepDOM)
	{
		T* obj = NULL;
		
		if(document.getData() == NULL || document.getLength() == 0 )
			SAML2LIB_INVPARAM_EX("Supplied xml document was NULL or empty.");
			
		if(pk == NULL)
			SAML2LIB_INVPARAM_EX("Supplied public key was NULL");
			
		try
		{
			xml_schema::dom::auto_ptr<DOMDocument> domDoc( validate(document) );
			validateSignature(domDoc.get(), pk);
			if(keepDOM)
			{
				/* Note that the memory referenced by domDoc becomes owned by our XSD object implementation
				 * in this case and will be cleaned up as required when this object is removed 
				 */
				domDoc->setUserData( xml_schema::dom::tree_node_key, &domDoc, 0 );
				obj = new T ( *domDoc->getDocumentElement(), xsd::cxx::tree::flags::keep_dom | xsd::cxx::tree::flags::dont_validate | xsd::cxx::tree::flags::dont_initialize);
			}
			else
			{
				obj = new T ( *domDoc->getDocumentElement(), xsd::cxx::tree::flags::dont_validate | xsd::cxx::tree::flags::dont_initialize);
			}
		}
		catch (xsd::cxx::tree::parsing< wchar_t > &exc)
		{
			localLogger.warn() << "XSD Parsing exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD Parsing exception while unmarshalling signed document", exc.what() );
		}
		catch (xsd::cxx::tree::expected_element< wchar_t > &exc)
		{
			localLogger.warn() << "XSD Expected Element exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD expected elements exception while unmarshalling signed document", exc.what() );
		}
		catch (xsd::cxx::tree::unexpected_element< wchar_t > &exc)
		{
			localLogger.warn() << "XSD Unexpected Element exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD unexpected elements exception while unmarshalling signed document", exc.what() );
		}
		catch (xsd::cxx::tree::expected_attribute< wchar_t > &exc)
		{
			localLogger.warn() << "XSD Expected Attribute exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD expected attribute exception while unmarshalling signed document", exc.what() );
		}
		catch (xsd::cxx::tree::unexpected_enumerator< wchar_t > &exc)
		{
			localLogger.warn() << "XSD Unexpected Enumerator exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD unexpected enumerator exception while unmarshalling signed document", exc.what() );
		}
		catch (xsd::cxx::tree::no_type_info< wchar_t > &exc)
		{
			localLogger.warn() << "XSD No Type Info exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD no type info exception while unmarshalling signed document", exc.what() );
		}
		catch (xsd::cxx::tree::not_derived< wchar_t > &exc)
		{
			localLogger.warn() << "XSD Not Derived exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD not derived exception while unmarshalling signed document", exc.what() );
		}
		catch (xsd::cxx::tree::exception< wchar_t > &exc)
		{
			localLogger.warn() << "XSD Tree exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD generic exception while unmarshalling signed document", exc.what() );
		}
		catch (std::bad_alloc &exc)
		{
			SAML2LIB_UNMAR_EX_CAUSE( "Bad memory alloc while unmarshalling signed document", exc.what() );
		}
		catch(SAXException &exc)
		{
			localLogger.warn() << "SAX exception: " << exc.getMessage();
			SAML2LIB_UNMAR_EX_CAUSE( "SAXException while unmarshalling signed document", exc.getMessage() );
		}
		catch(DOMException &exc)
		{
			localLogger.warn() << "DOM exception: " << exc.getMessage();
			SAML2LIB_UNMAR_EX_CAUSE( "DOMException while unmarshalling signed document", exc.getMessage() );
		}
		catch(XMLException &exc)
		{
			localLogger.warn() << "XML exception: " << exc.getMessage();
			SAML2LIB_UNMAR_EX_CAUSE( "XMLException while unmarshalling signed document", exc.getMessage() );
		}
		catch(std::exception &exc)
		{
			localLogger.warn() << "Exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "Exception while unmarshalling signed document", exc.what() );
		}
		catch(...)
		{
			SAML2LIB_UNMAR_EX( "Generic exception while unmarshalling signed document" );
		}

		return obj;
	}

	/*
	 * Unmarshalls an instance of a SAML document and verifies all cryptography, on success returns the
	 * associated XSD generated object representation for further processing. Will utilise external key resolver
	 * set in constructor call
	 * 
	 * @param doc Byte representation of the document to process
	 * @param size Number of bytes present in the doc buffer that should be processed
	 * @param keepDOM Indicates if the dom structure should be retained for use by the caller, default false
	 * 
	 * @exception UnmarshallerException, wrapped exception detailing error state
	 * @exception InvalidParameterException, when invalid data is fed to the library
	 */
	template <class T>
	T*  UnmarshallerImpl<T>::unMarshallSigned (const SAMLDocument& document, bool keepDOM)
	{
		T* obj = NULL;

		if(document.getData() == NULL || document.getLength() == 0)
			SAML2LIB_INVPARAM_EX("Supplied xml document was NULL or empty");
			
		try
		{
			if(extKeyResolver == NULL)
				SAML2LIB_UNMAR_EX( "Incorrect constructor called for validation of content with external key resolver, re-initialise unmarshaller correctly" );
				
			xml_schema::dom::auto_ptr<DOMDocument> domDoc( validate(document) );
			validateSignature(domDoc.get(), NULL);
			if(keepDOM)
			{
				/* Note that the memory referenced by domDoc becomes owned by our XSD object implementation
				 * in this case and will be cleaned up as required when this object is removed 
				 */
				domDoc->setUserData( xml_schema::dom::tree_node_key, &domDoc, 0 );
				obj = new T ( *domDoc->getDocumentElement(), xsd::cxx::tree::flags::keep_dom | xsd::cxx::tree::flags::dont_validate | xsd::cxx::tree::flags::dont_initialize);
			}
			else
			{
				obj = new T ( *domDoc->getDocumentElement(), xsd::cxx::tree::flags::dont_validate | xsd::cxx::tree::flags::dont_initialize);
			}
		}
		catch (xsd::cxx::tree::parsing< wchar_t > &exc)
		{
			localLogger.warn() << "XSD Parsing exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD Parsing exception while unmarshalling signed document", exc.what() );
		}
		catch (xsd::cxx::tree::expected_element< wchar_t > &exc)
		{
			localLogger.warn() << "XSD Expected Element exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD expected elements exception while unmarshalling signed document", exc.what() );
		}
		catch (xsd::cxx::tree::unexpected_element< wchar_t > &exc)
		{
			localLogger.warn() << "XSD Unexpected Element exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD unexpected elements exception while unmarshalling signed document", exc.what() );
		}
		catch (xsd::cxx::tree::expected_attribute< wchar_t > &exc)
		{
			localLogger.warn() << "XSD Expected Attribute exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD expected attribute exception while unmarshalling signed document", exc.what() );
		}
		catch (xsd::cxx::tree::unexpected_enumerator< wchar_t > &exc)
		{
			localLogger.warn() << "XSD Unexpected Enumerator exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD unexpected enumerator exception while unmarshalling signed document", exc.what() );
		}
		catch (xsd::cxx::tree::no_type_info< wchar_t > &exc)
		{
			localLogger.warn() << "XSD No Type Info exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD no type info exception while unmarshalling signed document", exc.what() );
		}
		catch (xsd::cxx::tree::not_derived< wchar_t > &exc)
		{
			localLogger.warn() << "XSD Not Derived exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD not derived exception while unmarshalling signed document", exc.what() );
		}
		catch (xsd::cxx::tree::exception< wchar_t > &exc)
		{
			localLogger.warn() << "XSD Exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD generic exception while unmarshalling signed document", exc.what() );
		}
		catch (std::bad_alloc &exc)
		{
			SAML2LIB_UNMAR_EX_CAUSE( "Bad memory alloc while unmarshalling signed document", exc.what() );
		}
		catch(SAXException &exc)
		{
			localLogger.warn() << "SAX Exception: " << exc.getMessage();
			SAML2LIB_UNMAR_EX_CAUSE( "SAXException while unmarshalling signed document", exc.getMessage() );
		}
		catch(DOMException &exc)
		{
			localLogger.warn() << "DOM Exception: " << exc.getMessage();
			SAML2LIB_UNMAR_EX_CAUSE( "DOMException while unmarshalling signed document", exc.getMessage() );
		}
		catch(XMLException &exc)
		{
			localLogger.warn() << "XML Exception: " << exc.getMessage();
			SAML2LIB_UNMAR_EX_CAUSE( "XMLException while unmarshalling signed document", exc.getMessage() );
		}
		catch(std::exception &exc)
		{
			localLogger.warn() << "Exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "Exception while unmarshalling signed document", exc.what() );
		}
		catch(...)
		{
			SAML2LIB_UNMAR_EX( "Generic exception while unmarshalling signed document" );
		}

		return obj;
	}

	/*
	 * Unmarshalls an instance of a SAML document with no verification of cryptography, on success returns the
	 * associated XSD generated object representation for further processing.
	 * 
	 * @param doc Byte representation of the document to process
	 * @param size Number of bytes present in the doc buffer that should be processed
	 * @param keepDOM Indicates if the dom structure should be retained for use by the caller, default false
	 * 
	 * @exception UnmarshallerException, wrapped exception detailing error state
	 * @exception InvalidParameterException, when invalid data is fed to the library
	 */
	template <class T>
	T* UnmarshallerImpl<T>::unMarshallUnSigned (const SAMLDocument& document, bool keepDOM)
	{
		T* obj = NULL;

		if(document.getData() == NULL || document.getLength() == 0)
			SAML2LIB_INVPARAM_EX("Supplied xml document was NULL or empty");

		try
		{
			xml_schema::dom::auto_ptr<DOMDocument> domDoc( validate(document) );
			if(keepDOM)
			{
				/* Note that the memory referenced by domDoc becomes owned by our XSD object implementation
				 * in this case and will be cleaned up as required when this object is removed 
				 */
				domDoc->setUserData( xml_schema::dom::tree_node_key, &domDoc, 0 );
				obj = new T ( *domDoc->getDocumentElement(), xsd::cxx::tree::flags::keep_dom | xsd::cxx::tree::flags::dont_validate | xsd::cxx::tree::flags::dont_initialize);
			}
			else
			{
				obj = new T ( *domDoc->getDocumentElement(), xsd::cxx::tree::flags::dont_validate | xsd::cxx::tree::flags::dont_initialize);
			}
		}
		catch (xsd::cxx::tree::parsing< wchar_t > &exc)
		{
			localLogger.warn() << "XSD Parsing exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD Parsing exception while unmarshalling unsigned document", exc.what() );
		}
		catch (xsd::cxx::tree::expected_element< wchar_t > &exc)
		{
			localLogger.warn() << "XSD Expected Element exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD expected elements exception while unmarshalling unsigned document", exc.what() );
		}
		catch (xsd::cxx::tree::unexpected_element< wchar_t > &exc)
		{
			localLogger.warn() << "XSD Unexpected Element exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD unexpected elements exception while unmarshalling unsigned document", exc.what() );
		}
		catch (xsd::cxx::tree::expected_attribute< wchar_t > &exc)
		{
			localLogger.warn() << "XSD Expected Attribute exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD expected attribute exception while unmarshalling unsigned document", exc.what() );
		}
		catch (xsd::cxx::tree::unexpected_enumerator< wchar_t > &exc)
		{
			localLogger.warn() << "XSD Unexpected Enumerator exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD unexpected enumerator exception while unmarshalling unsigned document", exc.what() );
		}
		catch (xsd::cxx::tree::no_type_info< wchar_t > &exc)
		{
			localLogger.warn() << "XSD No Type Info exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD no type info exception while unmarshalling unsigned document", exc.what() );
		}
		catch (xsd::cxx::tree::not_derived< wchar_t > &exc)
		{
			localLogger.warn() << "XSD Not Derived exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD not derived exception while unmarshalling unsigned document", exc.what() );
		}
		catch (xsd::cxx::tree::exception< wchar_t > &exc)
		{
			localLogger.warn() << "XSD Exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD generic exception while unmarshalling unsigned document", exc.what() );
		}
		catch (std::bad_alloc &exc)
		{
			SAML2LIB_UNMAR_EX_CAUSE( "Bad memory alloc while unmarshalling unsigned document", exc.what() );
		}
		catch(SAXException &exc)
		{
			localLogger.warn() << "SAX Exception: " << exc.getMessage();
			SAML2LIB_UNMAR_EX_CAUSE( "SAXException while unmarshalling unsigned document", exc.getMessage() );
		}
		catch(DOMException &exc)
		{
			localLogger.warn() << "DOM Exception: " << exc.getMessage();
			SAML2LIB_UNMAR_EX_CAUSE( "DOMException while unmarshalling unsigned document", exc.getMessage() );
		}
		catch(XMLException &exc)
		{
			localLogger.warn() << "XML Exception: " << exc.getMessage();
			SAML2LIB_UNMAR_EX_CAUSE( "XMLException while unmarshalling unsigned document", exc.getMessage() );
		}
		catch(std::exception &exc)
		{
			localLogger.warn() << "Exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "Exception while unmarshalling unsigned document", exc.what() );
		}
		catch(...)
		{
			SAML2LIB_UNMAR_EX( "Generic exception while unmarshalling unsigned document" );
		}
		
		return obj;
	}
	
	/*
	 * Unmarshalls an instance of DOMElement with no verification of cryptography, on success returns the
	 * associated XSD generated object representation for further processing, this is generally to be used for ##anyType elements
	 * extracted from an already processed XSD document, there is no element level validation performed, callers should exercise care
	 * with returned objects when using.
	 * 
	 * @param elem DOMElement to process, caller is responsible for ensuring valid element and for all memory management for this element.
	 * @param keepDOM Indicates if the dom structure should be retained for use by the caller, default false
	 * 
	 * @exception UnmarshallerException, wrapped exception detailing error state
	 * @exception InvalidParameterException, when invalid data is fed to the library
	 */
	template <class T>
	T* UnmarshallerImpl<T>::unMarshallUnSignedElement (DOMElement* elem, bool keepDOM)
	{
		T* obj = NULL;

		if(elem == NULL)
			SAML2LIB_INVPARAM_EX("Supplied DOMElement was NULL");

		try
		{
			if(keepDOM)
			{
				xml_schema::dom::auto_ptr<DOMDocument> domDoc( this->domImpl->createDocument( elem->getNamespaceURI(), elem->getLocalName(), 0 ) );
				if( domDoc->getDocumentElement() != NULL )
				{
					domDoc->removeChild( domDoc->getDocumentElement() );
				}
				
				domDoc->appendChild( domDoc->importNode( elem, true ) );
				
				/*
				 * Note here that we take a clone of the element, and that cloned DOM structure
				 * becomes owned by the unmarshaller object. This allows DOMElements that resulted
				 * from other unmarshalling operations to be unmarshaller further.
				 */
				domDoc->setUserData( xml_schema::dom::tree_node_key, &domDoc, 0 );
				obj = new T ( *(domDoc->getDocumentElement()), xsd::cxx::tree::flags::keep_dom | xsd::cxx::tree::flags::dont_validate | xsd::cxx::tree::flags::dont_initialize);
			}
			else
			{
				obj = new T ( *elem, xsd::cxx::tree::flags::dont_validate | xsd::cxx::tree::flags::dont_initialize);
			}
			return obj;
		}
		catch (xsd::cxx::tree::parsing< wchar_t > &exc)
		{
			localLogger.warn() << "XSD Parsing exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD Parsing exception while unmarshalling unsigned document", exc.what() );
		}
		catch (xsd::cxx::tree::expected_element< wchar_t > &exc)
		{
			localLogger.warn() << "XSD Expected Element exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD expected elements exception while unmarshalling unsigned document", exc.what() );
		}
		catch (xsd::cxx::tree::unexpected_element< wchar_t > &exc)
		{
			localLogger.warn() << "XSD Unexpected Element exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD unexpected elements exception while unmarshalling unsigned document", exc.what() );
		}
		catch (xsd::cxx::tree::expected_attribute< wchar_t > &exc)
		{
			localLogger.warn() << "XSD Expected Attribute exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD expected attribute exception while unmarshalling unsigned document", exc.what() );
		}
		catch (xsd::cxx::tree::unexpected_enumerator< wchar_t > &exc)
		{
			localLogger.warn() << "XSD Unexpected Enumerator exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD unexpected enumerator exception while unmarshalling unsigned document", exc.what() );
		}
		catch (xsd::cxx::tree::no_type_info< wchar_t > &exc)
		{
			localLogger.warn() << "XSD No Type Info exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD no type info exception while unmarshalling unsigned document", exc.what() );
		}
		catch (xsd::cxx::tree::not_derived< wchar_t > &exc)
		{
			localLogger.warn() << "XSD Not Derived exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD not derived exception while unmarshalling unsigned document", exc.what() );
		}
		catch (xsd::cxx::tree::exception< wchar_t > &exc)
		{
			localLogger.warn() << "XSD exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD generic exception while unmarshalling unsigned document", exc.what() );
		}
		catch (std::bad_alloc &exc)
		{
			SAML2LIB_UNMAR_EX_CAUSE( "Bad memory alloc while unmarshalling unsigned document", exc.what() );
		}
		catch(SAXException &exc)
		{
			localLogger.warn() << "SAX Exception: " << exc.getMessage();
			SAML2LIB_UNMAR_EX_CAUSE( "SAXException while unmarshalling unsigned document", exc.getMessage() );
		}
		catch(DOMException &exc)
		{
			localLogger.warn() << "DOM Exception: " << exc.getMessage();
			SAML2LIB_UNMAR_EX_CAUSE( "DOMException while unmarshalling unsigned document", exc.getMessage() );
		}
		catch(XMLException &exc)
		{
			localLogger.warn() << "XML Exception: " << exc.getMessage();
			SAML2LIB_UNMAR_EX_CAUSE( "XMLException while unmarshalling unsigned document", exc.getMessage() );
		}
		catch(std::exception &exc)
		{
			localLogger.warn() << "Exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "Exception while unmarshalling unsigned document", exc.what() );
		}
		catch(...)
		{
			SAML2LIB_UNMAR_EX( "Generic exception while unmarshalling unsigned document" );
		}
		
		return obj;
	}

	/*
	 * Unmarshalls an instance of a SAML metadata document and verifies all cryptography, on success returns the
	 * associated XSD generated object representation for further processing. Additionally all keys are extracted from
	 * metadata and stored in a map with a key of the name of the crpto key and object of type XSECCryptoKey
	 * 
	 * @param doc Byte representation of the document to process
	 * @param size Number of bytes present in the doc buffer that should be processed
	 * @param pk The public key to be used for cryptography validation operations
	 * @param keepDOM Indicates if the dom structure should be retained for use by the caller, default false
	 * 
	 * @exception UnmarshallerException, wrapped exception detailing error state
	 * @exception InvalidParameterException, when invalid data is fed to the library
	 */
	template <class T>
	saml2::MetadataOutput<T>*  UnmarshallerImpl<T>::unMarshallMetadata (const SAMLDocument& document, bool keepDOM)
	{
		DOMNodeList* keyDescriptorList = NULL;	
		T* xmlObj = NULL;
		
		if(document.getData() == NULL || document.getLength() == 0)
			SAML2LIB_INVPARAM_EX("Supplied xml document was NULL");

		saml2::MetadataOutput<T>* output = new MetadataOutput<T>();
		bool validKeyData = false;
		try
		{
			xml_schema::dom::auto_ptr<DOMDocument> domDoc( validate(document) );
			validateSignature(domDoc.get(), NULL);
			
			/* Once metadata is validated extract all public keys for usage by caller, most likely in a key resolver impl */		
			keyDescriptorList = domDoc->getElementsByTagNameNS(this->metadataURI, this->keyDescriptor);
			
			for(XMLSize_t i = 0; i < keyDescriptorList->getLength(); i++)
			{
				DOMNodeList* keyInfoList;
				DOMElement* descriptor;
		
				/* We have a valid document to schema by this stage so this will only be a single node */
				descriptor = (DOMElement*)keyDescriptorList->item(i);
				keyInfoList = descriptor->getElementsByTagNameNS(this->dsigURI, this->keyInfo);
				for(XMLSize_t j = 0; j < keyInfoList->getLength(); j++)
				{
					DOMElement* info = NULL;
					DOMElement* keyValue = NULL;
					DOMNodeList* keyNameList = NULL;
					DOMNodeList* keyValueList = NULL;
					DOMNodeList* RSAKeyDataList = NULL;
					DOMNodeList* DSAKeyDataList = NULL;
					char* keyName = NULL;
					
					/* Successfully extracted a KeyInfo element to process, currently only interested in the name, type and actual key data,
					 * this may be extended in the future, particuarly with differentiation around usage for signing or encryption etc
					 */
					info = (DOMElement*)keyInfoList->item(j);
					keyNameList = info->getElementsByTagNameNS(this->dsigURI, this->keyName);
					if ( keyNameList->getLength() == 0)
						SAML2LIB_UNMAR_EX( "While parsing key info no KeyName element present, key can't be referenced" );
					
					keyValueList = info->getElementsByTagNameNS(this->dsigURI, this->keyValue);
					if ( keyValueList->getLength() == 0)
						SAML2LIB_UNMAR_EX( "While parsing key info no KeyValue element present, key data can't be used" );
						
					/* Provide support for either RSA or DSA at the current point in time - Data must live at 0 due to validity of document and schema
					 * structure defined
					 */
					keyValue = (DOMElement*)keyValueList->item(0);			
					
					RSAKeyDataList = keyValue->getElementsByTagNameNS(this->dsigURI, this->rsaKeyValue);
					DSAKeyDataList = keyValue->getElementsByTagNameNS(this->dsigURI, this->dsaKeyValue);
							
					if( RSAKeyDataList->getLength() == 1 )
					{
						DOMElement* RSAKeyData = NULL;
						DOMElement* RSAKeyDataModulus = NULL;
						DOMElement* RSAKeyDataExponent = NULL;
						char* modulus = NULL;
						char* exponent = NULL;
						saml2::KeyData keyData;
		
						validKeyData = true;
						RSAKeyData = (DOMElement*)RSAKeyDataList->item(0);
						
						RSAKeyDataModulus = (DOMElement*)RSAKeyData->getChildNodes()->item(0);
						RSAKeyDataExponent = (DOMElement*)RSAKeyData->getChildNodes()->item(1);
						
						modulus = XMLString::transcode( RSAKeyDataModulus->getFirstChild()->getNodeValue());
						exponent = XMLString::transcode( RSAKeyDataExponent->getFirstChild()->getNodeValue());
						keyName = XMLString::transcode( keyNameList->item(0)->getFirstChild()->getNodeValue());

						/* Populate the KeyData object */
						keyData.type = saml2::KeyData::RSA;
						keyData.modulus = std::string( modulus, XMLString::stringLen(RSAKeyDataModulus->getFirstChild()->getNodeValue()) );
						keyData.exponent = std::string( exponent, XMLString::stringLen(RSAKeyDataExponent->getFirstChild()->getNodeValue()) );
						keyData.keyName = std::string( keyName, XMLString::stringLen(keyNameList->item(0)->getFirstChild()->getNodeValue()) );
						
						/* insert the created crypto key into the response map with its map key being the key name */
						output->keyList.insert(std::make_pair( keyName, keyData ));
						
						delete [] keyName;
						XMLString::release(&modulus);
						XMLString::release(&exponent);
					}
					
					if( DSAKeyDataList->getLength() == 1 )
					{
						DOMElement* DSAKeyData = NULL;
						DOMNodeList* Pn = NULL;
						DOMNodeList* Qn = NULL;
						DOMNodeList* Gn = NULL;
						DOMNodeList* Yn = NULL;
						DOMNodeList* Jn = NULL;
						char* P = NULL;
						char* Q = NULL;
						char* G = NULL;
						char* Y = NULL;
						char* J = NULL;
						saml2::KeyData keyData;
						
						validKeyData = true;
						DSAKeyData = (DOMElement*)DSAKeyDataList->item(0);
						
						Pn = DSAKeyData->getElementsByTagNameNS(this->dsigURI, this->dsaP);
						Qn = DSAKeyData->getElementsByTagNameNS(this->dsigURI, this->dsaQ);
						Gn = DSAKeyData->getElementsByTagNameNS(this->dsigURI, this->dsaG);
						Yn = DSAKeyData->getElementsByTagNameNS(this->dsigURI, this->dsaY);
						Jn = DSAKeyData->getElementsByTagNameNS(this->dsigURI, this->dsaJ);
						
						if(Pn->getLength() == 1)
						{
							P = XMLString::transcode( Pn->item(0)->getFirstChild()->getNodeValue());
							keyData.p = std::string( P, XMLString::stringLen(Pn->item(0)->getFirstChild()->getNodeValue()) );
						}
						if(Qn->getLength() == 1)
						{	 
							Q = XMLString::transcode( Qn->item(0)->getFirstChild()->getNodeValue());
							keyData.q = std::string( Q, XMLString::stringLen(Qn->item(0)->getFirstChild()->getNodeValue()) );
						}
						if(Gn->getLength() == 1)
						{	
							G = XMLString::transcode( Gn->item(0)->getFirstChild()->getNodeValue());
							keyData.g = std::string( G, XMLString::stringLen(Gn->item(0)->getFirstChild()->getNodeValue()) );
						}
						if(Yn->getLength() == 1)
						{
							Y = XMLString::transcode( Yn->item(0)->getFirstChild()->getNodeValue());
							keyData.y = std::string( Y, XMLString::stringLen(Yn->item(0)->getFirstChild()->getNodeValue()) );
						}
						if(Jn->getLength() == 1)
						{
							J =  XMLString::transcode( Jn->item(0)->getFirstChild()->getNodeValue());
							keyData.j = std::string( J, XMLString::stringLen(Jn->item(0)->getFirstChild()->getNodeValue()) );
						} 
						
						keyName = XMLString::transcode( keyNameList->item(0)->getFirstChild()->getNodeValue());
						keyData.keyName = std::string( keyName );
						
						/* insert the created crypto key into the response map with its map key being the key name */
						output->keyList.insert(std::make_pair( keyName, keyData ));
						
						delete keyName;	
						XMLString::release(&P);
						XMLString::release(&Q);
						XMLString::release(&G);
						XMLString::release(&Y);
						XMLString::release(&J);					
					}
					
					if(!validKeyData)
						SAML2LIB_UNMAR_EX( "While parsing key info invalid Key Data type found, support for DSA and RSA keys only currently" );  
				}
			}			
			
			if(keepDOM)
			{
				/* Note that the memory referenced by domDoc becomes owned by our XSD object implementation
				 * in this case and will be cleaned up as required when this object is removed 
				 */
				domDoc->setUserData( xml_schema::dom::tree_node_key, &domDoc, 0 );
				xmlObj = new T ( *domDoc->getDocumentElement(), xsd::cxx::tree::flags::keep_dom | xsd::cxx::tree::flags::dont_validate | xsd::cxx::tree::flags::dont_initialize);
			}
			else
			{
				xmlObj = new T ( *domDoc->getDocumentElement(), xsd::cxx::tree::flags::dont_validate | xsd::cxx::tree::flags::dont_initialize);
			}
			
			output->xmlObj = xmlObj;			
		}
		catch (xsd::cxx::tree::parsing< wchar_t > &exc)
		{
			localLogger.warn() << "XSD Parsing exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD Parsing exception while unmarshalling metadata", exc.what() );
		}
		catch (xsd::cxx::tree::expected_element< wchar_t > &exc)
		{
			localLogger.warn() << "XSD Expected Element exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD expected elements exception while unmarshalling metadata", exc.what() );
		}
		catch (xsd::cxx::tree::unexpected_element< wchar_t > &exc)
		{
			localLogger.warn() << "XSD Unexpected Element exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD unexpected elements exception while unmarshalling metadata", exc.what() );
		}
		catch (xsd::cxx::tree::expected_attribute< wchar_t > &exc)
		{
			localLogger.warn() << "XSD Expected Attribute exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD expected attribute exception while unmarshalling metadata", exc.what() );
		}
		catch (xsd::cxx::tree::unexpected_enumerator< wchar_t > &exc)
		{
			localLogger.warn() << "XSD Unexpected Enumerator exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD unexpected enumerator exception while unmarshalling metadata", exc.what() );
		}
		catch (xsd::cxx::tree::no_type_info< wchar_t > &exc)
		{
			localLogger.warn() << "XSD No Type Info exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD no type info exception while unmarshalling metadata", exc.what() );
		}
		catch (xsd::cxx::tree::not_derived< wchar_t > &exc)
		{
			localLogger.warn() << "XSD Not Derived exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD not derived exception while unmarshalling metadata", exc.what() );
		}
		catch (xsd::cxx::tree::exception< wchar_t > &exc)
		{
			localLogger.warn() << "XSD Exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "XSD generic exception while unmarshalling metadata", exc.what() );
		}
		catch (std::bad_alloc &exc)
		{
			SAML2LIB_UNMAR_EX_CAUSE( "Bad memory alloc while unmarshalling metadata", exc.what() );
		}
		catch(SAXException &exc)
		{
			localLogger.warn() << "SAX Exception: " << exc.getMessage();
			SAML2LIB_UNMAR_EX_CAUSE( "SAXException while unmarshalling metadata", exc.getMessage() );
		}
		catch(DOMException &exc)
		{
			localLogger.warn() << "DOM Exception: " << exc.getMessage();
			SAML2LIB_UNMAR_EX_CAUSE( "DOMException while unmarshalling metadata", exc.getMessage() );
		}
		catch(XMLException &exc)
		{
			localLogger.warn() << "XML Exception: " << exc.getMessage();
			SAML2LIB_UNMAR_EX_CAUSE( "XMLException while unmarshalling metadata", exc.getMessage() );
		}
		catch(std::exception &exc)
		{
			localLogger.warn() << "Exception: " << exc.what();
			SAML2LIB_UNMAR_EX_CAUSE( "Exception while unmarshalling metadata", exc.what() );
		}
		catch(...)
		{
			SAML2LIB_UNMAR_EX( "Generic exception while unmarshalling metadata" );
		}
		
		return output;
	}

	/*
	 * Utilises Xerces DOM level 3 DOMBuilder object to parse and validate the supplied DOMDocument
	 * instance, returns instance of DOMDocument generated by parsing
	 */
	template <class T>
	DOMDocument* UnmarshallerImpl<T>::validate (const SAMLDocument& document)
	{
		Wrapper4InputSource* wrapedSource = NULL;
		DOMBuilder* parser = NULL;
		DOMDocument* domDoc = NULL;
		MemBufInputSource* inputSource = NULL;
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
			parser->setFeature(XMLUni::fgDOMEntities, false);
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

			inputSource = new MemBufInputSource ((const XMLByte*)document.getData(), document.getLength(), UNMARSHALLER_ID);
			wrapedSource = new Wrapper4InputSource(inputSource);
			domDoc = parser->parse(*wrapedSource);

			parser->release();
			parser = NULL;
			delete wrapedSource;
			wrapedSource = NULL;
		}
		catch(...)
		{
			/* Clean up any memory allocated and throw back up stack */
			if(schema != NULL)
				XMLString::release(&schema);

			if(parser != NULL)
			{
				parser->release();
				parser = NULL;
			}
			
			if(wrapedSource != NULL)
			{
				delete wrapedSource;
				wrapedSource = NULL;
			}
			throw;
		}
		
		return domDoc;
	}

	/*
	 * Performs all cryptography operations associated with validating cryptography in the supplied domDocument,
	 * utilises the apache XML Security C library
	 */
	template <class T>
	void UnmarshallerImpl<T>::validateSignature (DOMDocument* doc, XSECCryptoKey* pk)
	{
		XSECProvider prov;
		DSIGSignature* sig = NULL;
		DSIGReferenceList* refList = NULL;
		DOMNodeList* nodeList = NULL;
		XMLSize_t size;
		XMLSize_t refListSize;
		bool validSig;

		try
		{
			// TODO Leaking memory here?
			nodeList = doc->getElementsByTagNameNS(this->dsigURI, this->dsigSigElem);
			/* Valgrind reports:
			==9094== 67,150 (436 direct, 66,714 indirect) bytes in 1 blocks are definitely lost in loss record 1,313 of 1,355
			==9094==    at 0x4005BA5: operator new(unsigned) (vg_replace_malloc.c:163)
			==9094==    by 0x502D295: xercesc_2_7::MemoryManagerImpl::allocate(unsigned) (in /usr/local/site/dev/xerces-c-src_2_7_0/lib/libxerces-c.so.27.0)
			==9094==    by 0x4FC1EA1: xercesc_2_7::DOMDeepNodeListPool<xercesc_2_7::DOMDeepNodeListImpl>::initialize(unsigned long) (in /usr/local/site/dev/xerces-c-src_2_7_0/lib/libxerces-c.so.27.0)
			==9094==    by 0x4FC1F2A: xercesc_2_7::DOMDeepNodeListPool<xercesc_2_7::DOMDeepNodeListImpl>::DOMDeepNodeListPool(unsigned long, bool, unsigned long) (in /usr/local/site/dev/xerces-c-src_2_7_0/lib/libxerces-c.so.27.0)
			==9094==    by 0x4FBCE18: xercesc_2_7::DOMDocumentImpl::getDeepNodeList(xercesc_2_7::DOMNode const*, unsigned short const*, unsigned short const*) (in /usr/local/site/dev/xerces-c-src_2_7_0/lib/libxerces-c.so.27.0)
			==9094==    by 0x4FBCEE1: xercesc_2_7::DOMDocumentImpl::getElementsByTagNameNS(unsigned short const*, unsigned short const*) const (in /usr/local/site/dev/xerces-c-src_2_7_0/lib/libxerces-c.so.27.0)
			==9094==    by 0x49CEAE3: saml2::UnmarshallerImpl<middleware::ESOEProtocolSchema::ValidateInitializationResponseType>::validateSignature(xercesc_2_7::DOMDocument*, XSECCryptoKey*) (UnmarshallerImpl.h:984)
			==9094==    by 0x49C4B55: middleware::ESOEProtocolSchema::ValidateInitializationResponseType* spep::SOAPUtil::SOAP11Handler::unwrap<middleware::ESOEProtocolSchema::ValidateInitializationResponseType>(saml2::Unmarshaller<middleware::ESOEProtocolSchema::ValidateInitializationResponseType>*, saml2::ManagedDocument<unsigned char, long, unsigned> const&) (SOAPUtil.h:250)
			==9094==    by 0x49C4E64: middleware::ESOEProtocolSchema::ValidateInitializationResponseType* spep::SOAPUtil::unwrapObjectFromSOAP<middleware::ESOEProtocolSchema::ValidateInitializationResponseType>(saml2::Unmarshaller<middleware::ESOEProtocolSchema::ValidateInitializationResponseType>*, saml2::ManagedDocument<unsigned char, long, unsigned> const&, spep::SOAPUtil::SOAPVersion) (SOAPUtil.h:140)
			==9094==    by 0x49C5391: middleware::ESOEProtocolSchema::ValidateInitializationResponseType* spep::WSClient::doWSCall<middleware::ESOEProtocolSchema::ValidateInitializationResponseType>(std::string, xercesc_2_7::DOMDocument*, saml2::Unmarshaller<middleware::ESOEProtocolSchema::ValidateInitializationResponseType>*, spep::SOAPUtil::SOAPVersion) (WSClient.h:106)
			==9094==    by 0x49C176E: spep::StartupProcessorImpl::doStartup() (StartupProcessorImpl.cpp:228)
			==9094==    by 0x49C2460: spep::StartupProcessorImpl::StartupProcessorThread::operator()() (StartupProcessorImpl.cpp:305)
			*/
			size = nodeList->getLength();

			if(size == 0)
				SAML2LIB_UNMAR_EX( "Document contained no signature elements to validate on" );
				
			for(XMLSize_t i = 0; i < size; i++)
			{
				validSig = false;
				sig = prov.newSignatureFromDOM(doc, nodeList->item(i));
				sig->load();
				
				/* Use either the supplied pk or if null the registered instance of external key resolver */
				if( pk != NULL )
					sig->setSigningKey(pk->clone());
				else
					sig->setKeyInfoResolver(this->extKeyResolver);

				validSig = sig->verify();

				if(!validSig)
				{
					if (!sig->verifySignatureOnly())
					{
						SAML2LIB_UNMAR_EX( "Signature validation failed with invalid signature for supplied public key" );
					}
					else
					{
						refList = sig->getReferenceList();
						refListSize = nodeList->getLength();

						for(XMLSize_t j = 0; j < refListSize; i++)
						{
							if( !refList->item(j)->checkHash() )
							{
								SAML2LIB_UNMAR_EX( "Signature validation failed with invalid supplied reference, the request document has been tampered with or the parent element to this signature block has no ID attribute");
							}
						}
					}
				}
				
				prov.releaseSignature(sig);
				sig = NULL;
			}
		}
		catch (XSECCryptoException &e)
		{
			if(sig != NULL)
				prov.releaseSignature(sig);

			SAML2LIB_UNMAR_EX_CAUSE( "An exception occurred during an encryption operation", e.getMsg() );
		}
		catch (XSECException &e)
		{
			if(sig != NULL)
				prov.releaseSignature(sig);

			SAML2LIB_UNMAR_EX_CAUSE( "An exception occurred during a security operation", e.getMsg() );
		}
		catch(...)
		{
			if(sig != NULL)
				prov.releaseSignature(sig);

			throw;
		}
	}
}

#endif /*UNMARSHALLERIMPL_H_*/

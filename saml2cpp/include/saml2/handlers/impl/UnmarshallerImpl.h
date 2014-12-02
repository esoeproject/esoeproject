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
#include <xercesc/parsers/XercesDomParser.hpp>
#include <xercesc/dom/DOMLSSerializer.hpp>
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
    class UnmarshallerImpl : public saml2::Unmarshaller < T >
    {
    public:
        UnmarshallerImpl(Logger* logger, const std::string& schemaDir, const std::vector<std::string>& schemaList);
        UnmarshallerImpl(Logger* logger, const std::string& schemaDir, const std::vector<std::string>& schemaList, ExternalKeyResolver* extKeyResolver);

        ~UnmarshallerImpl();

        T* unMarshallSigned(const SAMLDocument& document, XSECCryptoKey* pk, bool keepDOM = false) override;
        T* unMarshallSigned(const SAMLDocument& document, bool keepDOM = false) override;
        T* unMarshallUnSigned(const SAMLDocument& document, bool keepDOM = false) override;
        T* unMarshallUnSignedElement(DOMElement* elem, bool keepDOM = false override);
        saml2::MetadataOutput<T>* unMarshallMetadata(const SAMLDocument& document, bool keepDOM = false) override;
        void validateSignature(DOMDocument* doc, XSECCryptoKey* pk = NULL) override;

    private:
        LocalLogger mLocalLogger;

        void init();
        DOMDocument* validate(const SAMLDocument& document);

        ExternalKeyResolver* mExtKeyResolver;
        std::vector<std::string> mSchemaList;
        std::string mSchemaDir;

        //SAML2ErrorHandler* errorHandler;
        //ResourceResolver* resourceResolver;
        std::unique_ptr<SAML2ErrorHandler> mErrorHandler;
        std::unique_ptr<ResourceResolver> mResourceResolver;

        XMLCh* mImplFlags;
        XMLCh* mDsigURI;
        XMLCh* mDsigSigElem;
        XMLCh* mMetadataURI;
        XMLCh* mKeyDescriptor;
        XMLCh* mKeyInfo;
        XMLCh* mKeyName;
        XMLCh* mKeyValue;
        XMLCh* mRsaKeyValue;
        XMLCh* mDsaKeyValue;
        XMLCh* mDsaP;
        XMLCh* mDsaQ;
        XMLCh* mDsaG;
        XMLCh* mDsaY;
        XMLCh* mDsaJ;

        DOMImplementation* mDomImpl;
    };

    /*
     * Constructor for unmarshaller instances that do not require usage of an external key resolver
     */
    template <class T>
    UnmarshallerImpl<T>::UnmarshallerImpl(Logger* logger, const std::string& schemaDir, const std::vector<std::string>& schemaList) :
        mLocalLogger(logger, "saml2::UnmarshallerImpl"),
        mSchemaDir(schemaDir),
        mSchemaList(schemaList),
        mExtKeyResolver(nullptr)
    {
        try
        {
            init();
        }
        catch (XMLException &exc)
        {
            SAML2LIB_UNMAR_EX("Error during initialisation of Xerces and XMLSec");
        }
    }

    /*
     * Constructor for unmarshaller instances that do require usage of an external key resolver
     */
    template <class T>
    UnmarshallerImpl<T>::UnmarshallerImpl(Logger* logger, const std::string& schemaDir, const std::vector<std::string>& schemaList, ExternalKeyResolver* extKeyResolver) :
        mLocalLogger(logger, "saml2::UnmarshallerImpl"),
        mSchemaDir(schemaDir),
        mSchemaList(schemaList)
    {
        if (extKeyResolver == NULL)
            SAML2LIB_INVPARAM_EX("Supplied external key resolver was NULL");

        mExtKeyResolver = extKeyResolver;

        try
        {
            init();
        }
        catch (XMLException &exc)
        {
            SAML2LIB_UNMAR_EX_CAUSE("Error during initialisation of Xerces and XMLSec", exc.getMessage());
        }
    }

    /*
     * Destructor, frees all memory and terminates usage of Xerces and XSEC
     */
    template <class T>
    UnmarshallerImpl<T>::~UnmarshallerImpl()
    {
        /* Cleanup XMLCh strings */
        XMLString::release(&mImplFlags);
        XMLString::release(&mDsigURI);
        XMLString::release(&mDsigSigElem);
        XMLString::release(&mMetadataURI);
        XMLString::release(&mKeyDescriptor);
        XMLString::release(&mKeyInfo);
        XMLString::release(&mKeyName);
        XMLString::release(&mKeyValue);
        XMLString::release(&mRsaKeyValue);
        XMLString::release(&mDsaKeyValue);
        XMLString::release(&mDsaP);
        XMLString::release(&mDsaQ);
        XMLString::release(&mDsaG);
        XMLString::release(&mDsaY);
        XMLString::release(&mDsaJ);

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

        mErrorHandler = std::make_unique<SAML2ErrorHandler>();
        mResourceResolver = std::make_unique<ResourceResolver>(mSchemaDir);

        mImplFlags = XMLString::transcode(IMPL_FLAGS);
        mDsigURI = XMLString::transcode(DSIG_URI);
        mDsigSigElem = XMLString::transcode(DSIG_SIG_ELEM);
        mMetadataURI = XMLString::transcode(METADATA_URI);
        mKeyDescriptor = XMLString::transcode(KEY_DESCRIPTOR);
        mKeyInfo = XMLString::transcode(KEY_INFO);
        mKeyName = XMLString::transcode(KEY_NAME);
        mKeyValue = XMLString::transcode(KEY_VALUE);
        mRsaKeyValue = XMLString::transcode(RSA_KEY_VALUE);
        mDsaKeyValue = XMLString::transcode(DSA_KEY_VALUE);
        mDsaP = XMLString::transcode(DSA_P);
        mDsaQ = XMLString::transcode(DSA_Q);
        mDsaG = XMLString::transcode(DSA_G);
        mDsaY = XMLString::transcode(DSA_Y);
        mDsaJ = XMLString::transcode(DSA_J);

        mDomImpl = DOMImplementationRegistry::getDOMImplementation(mImplFlags);
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
    T*  UnmarshallerImpl<T>::unMarshallSigned(const SAMLDocument& document, XSECCryptoKey* pk, bool keepDOM)
    {
        T* obj = NULL;

        if (document.getData() == NULL || document.getLength() == 0)
            SAML2LIB_INVPARAM_EX("Supplied xml document was NULL or empty.");

        if (pk == NULL)
            SAML2LIB_INVPARAM_EX("Supplied public key was NULL");

        try
        {
            xml_schema::dom::auto_ptr<DOMDocument> domDoc(validate(document));
            validateSignature(domDoc.get(), pk);
            if (keepDOM)
            {
                /* Note that the memory referenced by domDoc becomes owned by our XSD object implementation
                 * in this case and will be cleaned up as required when this object is removed
                 */
                domDoc->setUserData(xml_schema::dom::tree_node_key, &domDoc, 0);
                obj = new T(*domDoc->getDocumentElement(), xsd::cxx::tree::flags::keep_dom | xsd::cxx::tree::flags::dont_validate | xsd::cxx::tree::flags::dont_initialize);
            }
            else
            {
                obj = new T(*domDoc->getDocumentElement(), xsd::cxx::tree::flags::dont_validate | xsd::cxx::tree::flags::dont_initialize);
            }
        }
        catch (xsd::cxx::tree::parsing< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD Parsing exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD Parsing exception while unmarshalling signed document", exc.what());
        }
        catch (xsd::cxx::tree::expected_element< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD Expected Element exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD expected elements exception while unmarshalling signed document", exc.what());
        }
        catch (xsd::cxx::tree::unexpected_element< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD Unexpected Element exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD unexpected elements exception while unmarshalling signed document", exc.what());
        }
        catch (xsd::cxx::tree::expected_attribute< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD Expected Attribute exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD expected attribute exception while unmarshalling signed document", exc.what());
        }
        catch (xsd::cxx::tree::unexpected_enumerator< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD Unexpected Enumerator exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD unexpected enumerator exception while unmarshalling signed document", exc.what());
        }
        catch (xsd::cxx::tree::no_type_info< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD No Type Info exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD no type info exception while unmarshalling signed document", exc.what());
        }
        catch (xsd::cxx::tree::not_derived< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD Not Derived exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD not derived exception while unmarshalling signed document", exc.what());
        }
        catch (xsd::cxx::tree::exception< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD Tree exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD generic exception while unmarshalling signed document", exc.what());
        }
        catch (std::bad_alloc &exc)
        {
            SAML2LIB_UNMAR_EX_CAUSE("Bad memory alloc while unmarshalling signed document", exc.what());
        }
        catch (SAXException &exc)
        {
            mLocalLogger.warn() << "SAX exception: " << exc.getMessage();
            SAML2LIB_UNMAR_EX_CAUSE("SAXException while unmarshalling signed document", exc.getMessage());
        }
        catch (DOMException &exc)
        {
            mLocalLogger.warn() << "DOM exception: " << exc.getMessage();
            SAML2LIB_UNMAR_EX_CAUSE("DOMException while unmarshalling signed document", exc.getMessage());
        }
        catch (XMLException &exc)
        {
            mLocalLogger.warn() << "XML exception: " << exc.getMessage();
            SAML2LIB_UNMAR_EX_CAUSE("XMLException while unmarshalling signed document", exc.getMessage());
        }
        catch (std::exception &exc)
        {
            mLocalLogger.warn() << "Exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("Exception while unmarshalling signed document", exc.what());
        }
        catch (...)
        {
            SAML2LIB_UNMAR_EX("Generic exception while unmarshalling signed document");
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
    T*  UnmarshallerImpl<T>::unMarshallSigned(const SAMLDocument& document, bool keepDOM)
    {
        T* obj = NULL;

        if (document.getData() == NULL || document.getLength() == 0)
            SAML2LIB_INVPARAM_EX("Supplied xml document was NULL or empty");

        try
        {
            if (mExtKeyResolver == NULL)
                SAML2LIB_UNMAR_EX("Incorrect constructor called for validation of content with external key resolver, re-initialise unmarshaller correctly");

            xml_schema::dom::auto_ptr<DOMDocument> domDoc(validate(document));
            validateSignature(domDoc.get(), NULL);
            if (keepDOM)
            {
                /* Note that the memory referenced by domDoc becomes owned by our XSD object implementation
                 * in this case and will be cleaned up as required when this object is removed
                 */
                domDoc->setUserData(xml_schema::dom::tree_node_key, &domDoc, 0);
                obj = new T(*domDoc->getDocumentElement(), xsd::cxx::tree::flags::keep_dom | xsd::cxx::tree::flags::dont_validate | xsd::cxx::tree::flags::dont_initialize);
            }
            else
            {
                obj = new T(*domDoc->getDocumentElement(), xsd::cxx::tree::flags::dont_validate | xsd::cxx::tree::flags::dont_initialize);
            }
        }
        catch (xsd::cxx::tree::parsing< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD Parsing exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD Parsing exception while unmarshalling signed document", exc.what());
        }
        catch (xsd::cxx::tree::expected_element< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD Expected Element exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD expected elements exception while unmarshalling signed document", exc.what());
        }
        catch (xsd::cxx::tree::unexpected_element< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD Unexpected Element exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD unexpected elements exception while unmarshalling signed document", exc.what());
        }
        catch (xsd::cxx::tree::expected_attribute< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD Expected Attribute exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD expected attribute exception while unmarshalling signed document", exc.what());
        }
        catch (xsd::cxx::tree::unexpected_enumerator< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD Unexpected Enumerator exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD unexpected enumerator exception while unmarshalling signed document", exc.what());
        }
        catch (xsd::cxx::tree::no_type_info< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD No Type Info exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD no type info exception while unmarshalling signed document", exc.what());
        }
        catch (xsd::cxx::tree::not_derived< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD Not Derived exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD not derived exception while unmarshalling signed document", exc.what());
        }
        catch (xsd::cxx::tree::exception< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD Exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD generic exception while unmarshalling signed document", exc.what());
        }
        catch (std::bad_alloc &exc)
        {
            SAML2LIB_UNMAR_EX_CAUSE("Bad memory alloc while unmarshalling signed document", exc.what());
        }
        catch (SAXException &exc)
        {
            mLocalLogger.warn() << "SAX Exception: " << exc.getMessage();
            SAML2LIB_UNMAR_EX_CAUSE("SAXException while unmarshalling signed document", exc.getMessage());
        }
        catch (DOMException &exc)
        {
            mLocalLogger.warn() << "DOM Exception: " << exc.getMessage();
            SAML2LIB_UNMAR_EX_CAUSE("DOMException while unmarshalling signed document", exc.getMessage());
        }
        catch (XMLException &exc)
        {
            mLocalLogger.warn() << "XML Exception: " << exc.getMessage();
            SAML2LIB_UNMAR_EX_CAUSE("XMLException while unmarshalling signed document", exc.getMessage());
        }
        catch (std::exception &exc)
        {
            mLocalLogger.warn() << "Exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("Exception while unmarshalling signed document", exc.what());
        }
        catch (...)
        {
            SAML2LIB_UNMAR_EX("Generic exception while unmarshalling signed document");
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
    T* UnmarshallerImpl<T>::unMarshallUnSigned(const SAMLDocument& document, bool keepDOM)
    {
        T* obj = NULL;

        if (document.getData() == NULL || document.getLength() == 0)
            SAML2LIB_INVPARAM_EX("Supplied xml document was NULL or empty");

        try
        {
            xml_schema::dom::auto_ptr<DOMDocument> domDoc(validate(document));
            if (keepDOM)
            {
                /* Note that the memory referenced by domDoc becomes owned by our XSD object implementation
                 * in this case and will be cleaned up as required when this object is removed
                 */
                domDoc->setUserData(xml_schema::dom::tree_node_key, &domDoc, 0);
                obj = new T(*domDoc->getDocumentElement(), xsd::cxx::tree::flags::keep_dom | xsd::cxx::tree::flags::dont_validate | xsd::cxx::tree::flags::dont_initialize);
            }
            else
            {
                obj = new T(*domDoc->getDocumentElement(), xsd::cxx::tree::flags::dont_validate | xsd::cxx::tree::flags::dont_initialize);
            }
        }
        catch (xsd::cxx::tree::parsing< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD Parsing exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD Parsing exception while unmarshalling unsigned document", exc.what());
        }
        catch (xsd::cxx::tree::expected_element< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD Expected Element exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD expected elements exception while unmarshalling unsigned document", exc.what());
        }
        catch (xsd::cxx::tree::unexpected_element< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD Unexpected Element exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD unexpected elements exception while unmarshalling unsigned document", exc.what());
        }
        catch (xsd::cxx::tree::expected_attribute< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD Expected Attribute exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD expected attribute exception while unmarshalling unsigned document", exc.what());
        }
        catch (xsd::cxx::tree::unexpected_enumerator< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD Unexpected Enumerator exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD unexpected enumerator exception while unmarshalling unsigned document", exc.what());
        }
        catch (xsd::cxx::tree::no_type_info< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD No Type Info exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD no type info exception while unmarshalling unsigned document", exc.what());
        }
        catch (xsd::cxx::tree::not_derived< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD Not Derived exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD not derived exception while unmarshalling unsigned document", exc.what());
        }
        catch (xsd::cxx::tree::exception< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD Exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD generic exception while unmarshalling unsigned document", exc.what());
        }
        catch (std::bad_alloc &exc)
        {
            SAML2LIB_UNMAR_EX_CAUSE("Bad memory alloc while unmarshalling unsigned document", exc.what());
        }
        catch (SAXException &exc)
        {
            mLocalLogger.warn() << "SAX Exception: " << exc.getMessage();
            SAML2LIB_UNMAR_EX_CAUSE("SAXException while unmarshalling unsigned document", exc.getMessage());
        }
        catch (DOMException &exc)
        {
            mLocalLogger.warn() << "DOM Exception: " << exc.getMessage();
            SAML2LIB_UNMAR_EX_CAUSE("DOMException while unmarshalling unsigned document", exc.getMessage());
        }
        catch (XMLException &exc)
        {
            mLocalLogger.warn() << "XML Exception: " << exc.getMessage();
            SAML2LIB_UNMAR_EX_CAUSE("XMLException while unmarshalling unsigned document", exc.getMessage());
        }
        catch (std::exception &exc)
        {
            mLocalLogger.warn() << "Exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("Exception while unmarshalling unsigned document", exc.what());
        }
        catch (...)
        {
            SAML2LIB_UNMAR_EX("Generic exception while unmarshalling unsigned document");
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
    T* UnmarshallerImpl<T>::unMarshallUnSignedElement(DOMElement* elem, bool keepDOM)
    {
        T* obj = NULL;

        if (elem == NULL)
            SAML2LIB_INVPARAM_EX("Supplied DOMElement was NULL");

        try
        {
            if (keepDOM)
            {
                xml_schema::dom::auto_ptr<DOMDocument> domDoc(this->mDomImpl->createDocument(elem->getNamespaceURI(), elem->getLocalName(), 0));
                if (domDoc->getDocumentElement() != NULL)
                {
                    domDoc->removeChild(domDoc->getDocumentElement());
                }

                domDoc->appendChild(domDoc->importNode(elem, true));

                /*
                 * Note here that we take a clone of the element, and that cloned DOM structure
                 * becomes owned by the unmarshaller object. This allows DOMElements that resulted
                 * from other unmarshalling operations to be unmarshaller further.
                 */
                domDoc->setUserData(xml_schema::dom::tree_node_key, &domDoc, 0);
                obj = new T(*(domDoc->getDocumentElement()), xsd::cxx::tree::flags::keep_dom | xsd::cxx::tree::flags::dont_validate | xsd::cxx::tree::flags::dont_initialize);
            }
            else
            {
                obj = new T(*elem, xsd::cxx::tree::flags::dont_validate | xsd::cxx::tree::flags::dont_initialize);
            }
            return obj;
        }
        catch (xsd::cxx::tree::parsing< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD Parsing exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD Parsing exception while unmarshalling unsigned document", exc.what());
        }
        catch (xsd::cxx::tree::expected_element< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD Expected Element exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD expected elements exception while unmarshalling unsigned document", exc.what());
        }
        catch (xsd::cxx::tree::unexpected_element< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD Unexpected Element exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD unexpected elements exception while unmarshalling unsigned document", exc.what());
        }
        catch (xsd::cxx::tree::expected_attribute< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD Expected Attribute exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD expected attribute exception while unmarshalling unsigned document", exc.what());
        }
        catch (xsd::cxx::tree::unexpected_enumerator< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD Unexpected Enumerator exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD unexpected enumerator exception while unmarshalling unsigned document", exc.what());
        }
        catch (xsd::cxx::tree::no_type_info< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD No Type Info exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD no type info exception while unmarshalling unsigned document", exc.what());
        }
        catch (xsd::cxx::tree::not_derived< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD Not Derived exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD not derived exception while unmarshalling unsigned document", exc.what());
        }
        catch (xsd::cxx::tree::exception< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD generic exception while unmarshalling unsigned document", exc.what());
        }
        catch (std::bad_alloc &exc)
        {
            SAML2LIB_UNMAR_EX_CAUSE("Bad memory alloc while unmarshalling unsigned document", exc.what());
        }
        catch (SAXException &exc)
        {
            mLocalLogger.warn() << "SAX Exception: " << exc.getMessage();
            SAML2LIB_UNMAR_EX_CAUSE("SAXException while unmarshalling unsigned document", exc.getMessage());
        }
        catch (DOMException &exc)
        {
            mLocalLogger.warn() << "DOM Exception: " << exc.getMessage();
            SAML2LIB_UNMAR_EX_CAUSE("DOMException while unmarshalling unsigned document", exc.getMessage());
        }
        catch (XMLException &exc)
        {
            mLocalLogger.warn() << "XML Exception: " << exc.getMessage();
            SAML2LIB_UNMAR_EX_CAUSE("XMLException while unmarshalling unsigned document", exc.getMessage());
        }
        catch (std::exception &exc)
        {
            mLocalLogger.warn() << "Exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("Exception while unmarshalling unsigned document", exc.what());
        }
        catch (...)
        {
            SAML2LIB_UNMAR_EX("Generic exception while unmarshalling unsigned document");
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
    saml2::MetadataOutput<T>*  UnmarshallerImpl<T>::unMarshallMetadata(const SAMLDocument& document, bool keepDOM)
    {
        DOMNodeList* keyDescriptorList = NULL;
        T* xmlObj = NULL;

        if (document.getData() == NULL || document.getLength() == 0)
            SAML2LIB_INVPARAM_EX("Supplied xml document was NULL");

        saml2::MetadataOutput<T>* output = new MetadataOutput<T>();
        bool validKeyData = false;
        try
        {
            xml_schema::dom::auto_ptr<DOMDocument> domDoc(validate(document));
            validateSignature(domDoc.get(), NULL);

            /* Once metadata is validated extract all public keys for usage by caller, most likely in a key resolver impl */
            keyDescriptorList = domDoc->getElementsByTagNameNS(mMetadataURI, mKeyDescriptor);

            for (XMLSize_t i = 0; i < keyDescriptorList->getLength(); i++)
            {
                DOMNodeList* keyInfoList{ nullptr };
                DOMElement* descriptor{ nullptr };

                /* We have a valid document to schema by this stage so this will only be a single node */
                descriptor = (DOMElement*)keyDescriptorList->item(i);
                keyInfoList = descriptor->getElementsByTagNameNS(mDsigURI, mKeyInfo);
                for (XMLSize_t j = 0; j < keyInfoList->getLength(); j++)
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
                    keyNameList = info->getElementsByTagNameNS(mDsigURI, mKeyName);
                    if (keyNameList->getLength() == 0)
                        SAML2LIB_UNMAR_EX("While parsing key info no KeyName element present, key can't be referenced");

                    keyValueList = info->getElementsByTagNameNS(mDsigURI, mKeyValue);
                    if (keyValueList->getLength() == 0)
                        SAML2LIB_UNMAR_EX("While parsing key info no KeyValue element present, key data can't be used");

                    /* Provide support for either RSA or DSA at the current point in time - Data must live at 0 due to validity of document and schema
                     * structure defined
                     */
                    keyValue = (DOMElement*)keyValueList->item(0);

                    RSAKeyDataList = keyValue->getElementsByTagNameNS(mDsigURI, mRsaKeyValue);
                    DSAKeyDataList = keyValue->getElementsByTagNameNS(mDsigURI, mDsaKeyValue);

                    if (RSAKeyDataList->getLength() == 1)
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

                        modulus = XMLString::transcode(RSAKeyDataModulus->getFirstChild()->getNodeValue());
                        exponent = XMLString::transcode(RSAKeyDataExponent->getFirstChild()->getNodeValue());
                        keyName = XMLString::transcode(keyNameList->item(0)->getFirstChild()->getNodeValue());

                        /* Populate the KeyData object */
                        keyData.type = saml2::KeyData::RSA;
                        keyData.modulus = std::string(modulus, XMLString::stringLen(RSAKeyDataModulus->getFirstChild()->getNodeValue()));
                        keyData.exponent = std::string(exponent, XMLString::stringLen(RSAKeyDataExponent->getFirstChild()->getNodeValue()));
                        keyData.keyName = std::string(keyName, XMLString::stringLen(keyNameList->item(0)->getFirstChild()->getNodeValue()));

                        /* insert the created crypto key into the response map with its map key being the key name */
                        output->keyList.insert(std::make_pair(keyName, keyData));

                        XMLString::release(&keyName);
                        XMLString::release(&modulus);
                        XMLString::release(&exponent);
                    }

                    if (DSAKeyDataList->getLength() == 1)
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

                        Pn = DSAKeyData->getElementsByTagNameNS(mDsigURI, mDsaP);
                        Qn = DSAKeyData->getElementsByTagNameNS(mDsigURI, mDsaQ);
                        Gn = DSAKeyData->getElementsByTagNameNS(mDsigURI, mDsaG);
                        Yn = DSAKeyData->getElementsByTagNameNS(mDsigURI, mDsaY);
                        Jn = DSAKeyData->getElementsByTagNameNS(mDsigURI, mDsaJ);

                        if (Pn->getLength() == 1)
                        {
                            P = XMLString::transcode(Pn->item(0)->getFirstChild()->getNodeValue());
                            keyData.p = std::string(P, XMLString::stringLen(Pn->item(0)->getFirstChild()->getNodeValue()));
                        }
                        if (Qn->getLength() == 1)
                        {
                            Q = XMLString::transcode(Qn->item(0)->getFirstChild()->getNodeValue());
                            keyData.q = std::string(Q, XMLString::stringLen(Qn->item(0)->getFirstChild()->getNodeValue()));
                        }
                        if (Gn->getLength() == 1)
                        {
                            G = XMLString::transcode(Gn->item(0)->getFirstChild()->getNodeValue());
                            keyData.g = std::string(G, XMLString::stringLen(Gn->item(0)->getFirstChild()->getNodeValue()));
                        }
                        if (Yn->getLength() == 1)
                        {
                            Y = XMLString::transcode(Yn->item(0)->getFirstChild()->getNodeValue());
                            keyData.y = std::string(Y, XMLString::stringLen(Yn->item(0)->getFirstChild()->getNodeValue()));
                        }
                        if (Jn->getLength() == 1)
                        {
                            J = XMLString::transcode(Jn->item(0)->getFirstChild()->getNodeValue());
                            keyData.j = std::string(J, XMLString::stringLen(Jn->item(0)->getFirstChild()->getNodeValue()));
                        }

                        keyName = XMLString::transcode(keyNameList->item(0)->getFirstChild()->getNodeValue());
                        keyData.keyName = std::string(keyName);

                        /* insert the created crypto key into the response map with its map key being the key name */
                        output->keyList.insert(std::make_pair(keyName, keyData));

                        delete keyName;
                        XMLString::release(&P);
                        XMLString::release(&Q);
                        XMLString::release(&G);
                        XMLString::release(&Y);
                        XMLString::release(&J);
                    }

                    if (!validKeyData)
                        SAML2LIB_UNMAR_EX("While parsing key info invalid Key Data type found, support for DSA and RSA keys only currently");
                }
            }

            if (keepDOM)
            {
                /* Note that the memory referenced by domDoc becomes owned by our XSD object implementation
                 * in this case and will be cleaned up as required when this object is removed
                 */
                domDoc->setUserData(xml_schema::dom::tree_node_key, &domDoc, 0);
                xmlObj = new T(*domDoc->getDocumentElement(), xsd::cxx::tree::flags::keep_dom | xsd::cxx::tree::flags::dont_validate | xsd::cxx::tree::flags::dont_initialize);
            }
            else
            {
                xmlObj = new T(*domDoc->getDocumentElement(), xsd::cxx::tree::flags::dont_validate | xsd::cxx::tree::flags::dont_initialize);
            }

            output->xmlObj = xmlObj;
        }
        catch (xsd::cxx::tree::parsing< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD Parsing exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD Parsing exception while unmarshalling metadata", exc.what());
        }
        catch (xsd::cxx::tree::expected_element< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD Expected Element exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD expected elements exception while unmarshalling metadata", exc.what());
        }
        catch (xsd::cxx::tree::unexpected_element< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD Unexpected Element exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD unexpected elements exception while unmarshalling metadata", exc.what());
        }
        catch (xsd::cxx::tree::expected_attribute< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD Expected Attribute exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD expected attribute exception while unmarshalling metadata", exc.what());
        }
        catch (xsd::cxx::tree::unexpected_enumerator< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD Unexpected Enumerator exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD unexpected enumerator exception while unmarshalling metadata", exc.what());
        }
        catch (xsd::cxx::tree::no_type_info< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD No Type Info exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD no type info exception while unmarshalling metadata", exc.what());
        }
        catch (xsd::cxx::tree::not_derived< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD Not Derived exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD not derived exception while unmarshalling metadata", exc.what());
        }
        catch (xsd::cxx::tree::exception< wchar_t > &exc)
        {
            mLocalLogger.warn() << "XSD Exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("XSD generic exception while unmarshalling metadata", exc.what());
        }
        catch (std::bad_alloc &exc)
        {
            SAML2LIB_UNMAR_EX_CAUSE("Bad memory alloc while unmarshalling metadata", exc.what());
        }
        catch (SAXException &exc)
        {
            mLocalLogger.warn() << "SAX Exception: " << exc.getMessage();
            SAML2LIB_UNMAR_EX_CAUSE("SAXException while unmarshalling metadata", exc.getMessage());
        }
        catch (DOMException &exc)
        {
            mLocalLogger.warn() << "DOM Exception: " << exc.getMessage();
            SAML2LIB_UNMAR_EX_CAUSE("DOMException while unmarshalling metadata", exc.getMessage());
        }
        catch (XMLException &exc)
        {
            mLocalLogger.warn() << "XML Exception: " << exc.getMessage();
            SAML2LIB_UNMAR_EX_CAUSE("XMLException while unmarshalling metadata", exc.getMessage());
        }
        catch (std::exception &exc)
        {
            mLocalLogger.warn() << "Exception: " << exc.what();
            SAML2LIB_UNMAR_EX_CAUSE("Exception while unmarshalling metadata", exc.what());
        }
        catch (...)
        {
            SAML2LIB_UNMAR_EX("Generic exception while unmarshalling metadata");
        }

        return output;
    }

#include <iostream>

    /*
     * Utilises Xerces DOM level 3 DOMBuilder object to parse and validate the supplied DOMDocument
     * instance, returns instance of DOMDocument generated by parsing
     */
    template <class T>
    DOMDocument* UnmarshallerImpl<T>::validate(const SAMLDocument& document)
    {
        Wrapper4InputSource* wrapedSource = NULL;
        DOMLSParser* parser = nullptr;
        DOMDocument* domDoc = NULL;
        MemBufInputSource* inputSource = NULL;
        XMLCh* schema = NULL;

        try
        {
            ResourceResolver* resourceResolver = mResourceResolver.get();

            parser = ((DOMImplementationLS*)mDomImpl)->createLSParser(DOMImplementationLS::MODE_SYNCHRONOUS, 0);
            parser->getDomConfig()->setParameter(XMLUni::fgXercesSchema, true);
            parser->getDomConfig()->setParameter(XMLUni::fgDOMValidate, true);
            parser->getDomConfig()->setParameter(XMLUni::fgDOMNamespaces, true);
            parser->getDomConfig()->setParameter(XMLUni::fgDOMElementContentWhitespace, false);
            parser->getDomConfig()->setParameter(XMLUni::fgXercesUseCachedGrammarInParse, true);
            parser->getDomConfig()->setParameter(XMLUni::fgXercesUserAdoptsDOMDocument, true);
            // This is particuarly important so that signatures are not corrupted
            parser->getDomConfig()->setParameter(XMLUni::fgDOMDatatypeNormalization, false);
            parser->getDomConfig()->setParameter(XMLUni::fgXercesEntityResolver, resourceResolver);
            parser->getDomConfig()->setParameter(XMLUni::fgDOMErrorHandler, mErrorHandler.get());


            //for (std::vector<std::string>::iterator i = mSchemaList.begin(); i != mSchemaList.end(); i++)
            for (const auto& i: mSchemaList)
            {
                schema = XMLString::transcode(i.c_str());
                parser->loadGrammar(schema, Grammar::SchemaGrammarType, true);
                XMLString::release(&schema);
            }

            inputSource = new MemBufInputSource((const XMLByte*)document.getData(), document.getLength(), UNMARSHALLER_ID);
            Wrapper4InputSource wrapedSource(inputSource);
            domDoc = parser->parse(&wrapedSource);

            parser->release();
            parser = NULL;
        }
        catch (...)
        {
            /* Clean up any memory allocated and throw back up stack */
            if (schema != NULL)
                XMLString::release(&schema);

            if (parser != NULL)
            {
                parser->release();
                parser = NULL;
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
    void UnmarshallerImpl<T>::validateSignature(DOMDocument* doc, XSECCryptoKey* pk)
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
            nodeList = doc->getElementsByTagNameNS(mDsigURI, mDsigSigElem);
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

            if (size == 0)
                SAML2LIB_UNMAR_EX("Document contained no signature elements to validate on");

            for (XMLSize_t i = 0; i < size; i++)
            {
                validSig = false;
                sig = prov.newSignatureFromDOM(doc, nodeList->item(i));
                sig->setIdByAttributeName(true);
                sig->registerIdAttributeName(MAKE_UNICODE_STRING("ID"));
                sig->load();

                /* Use either the supplied pk or if null the registered instance of external key resolver */
                if (pk != NULL)
                    sig->setSigningKey(pk->clone());
                else
                    sig->setKeyInfoResolver(mExtKeyResolver);

                validSig = sig->verify();

                if (!validSig)
                {
                    if (!sig->verifySignatureOnly())
                    {
                        SAML2LIB_UNMAR_EX("Signature validation failed with invalid signature for supplied public key");
                    }
                    else
                    {
                        refList = sig->getReferenceList();
                        refListSize = nodeList->getLength();

                        for (XMLSize_t j = 0; j < refListSize; i++)
                        {
                            if (!refList->item(j)->checkHash())
                            {
                                SAML2LIB_UNMAR_EX("Signature validation failed with invalid supplied reference, the request document has been tampered with or the parent element to this signature block has no ID attribute");
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
            if (sig != NULL)
                prov.releaseSignature(sig);

            SAML2LIB_UNMAR_EX_CAUSE("An exception occurred during an encryption operation", e.getMsg());
        }
        catch (XSECException &e)
        {
            if (sig != NULL)
                prov.releaseSignature(sig);

            SAML2LIB_UNMAR_EX_CAUSE("An exception occurred during a security operation", e.getMsg());
        }
        catch (...)
        {
            if (sig != NULL)
                prov.releaseSignature(sig);

            throw;
        }
    }
}

#endif /*UNMARSHALLERIMPL_H_*/

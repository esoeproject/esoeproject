/* Copyright 2006-2007, Queensland University of Technology
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
 * Creation Date: Aug 14, 2007
 * 
 * Purpose: 
 */

#ifndef SOAPUTIL_H_
#define SOAPUTIL_H_

// For both SAMLDocument and the template class ManagedDocument
#include "saml2/handlers/SAMLDocument.h"
#include "saml2/handlers/Marshaller.h"
#include "saml2/handlers/Unmarshaller.h"
#include "saml2/handlers/impl/UnmarshallerImpl.h"
#include "saml2/handlers/impl/MarshallerImpl.h"

#include "saml2/bindings/soap-1.1-envelope.hxx"
#include "saml2/bindings/soap-1.2-envelope.hxx"

#include "spep/Util.h"
#include "saml2/logging/api.h"
#include "saml2/logging/api.h"

#include <utility>
#include <string>

namespace spep
{

    typedef saml2::SAMLDocument SOAPDocument;

    class SPEPEXPORT SOAPUtil
    {
    public:

        enum SOAPVersion
        {
            UNINITIALIZED,
            SOAP11,
            SOAP12
        };

        SOAPUtil(saml2::Logger *logger, const std::string& schemaPath);
        ~SOAPUtil();

        /**
         * Wraps a given SAML object and marshalls into a SOAP document ready for a web service request/response.
         */
        SOAPDocument wrapObjectInSOAP(DOMElement *objectElement, const std::string& characterEncoding, SOAPVersion soapVersion);

        /**
         * Unwraps a SAML object and unmarshalls into an expected SAML object from a web service request/response.
         */
        template<typename T>
        T* unwrapObjectFromSOAP(saml2::Unmarshaller<T> *unmarshaller, const SOAPDocument& soapDocument, SOAPVersion soapVersion)
        {
            switch (soapVersion)
            {
            case SOAP12:
                mLocalLogger.debug() << "Going to unwrap SOAP/1.2 envelope.";
                return mSoap12Handler->unwrap<T>(unmarshaller, soapDocument);

                // Default to SOAP/1.1 processing
            case SOAP11:
            default:
                mLocalLogger.debug() << "Going to unwrap SOAP/1.1 envelope.";
                return mSoap11Handler->unwrap<T>(unmarshaller, soapDocument);
            }
        }

    private:

        /**
        * Class definition for SOAP 1.1 handler.
        */
        class SOAP11Handler
        {
        public:

            SOAP11Handler(saml2::Logger *logger, const std::string& schemaPath);
            ~SOAP11Handler();
            template <typename T>
            T* unwrap(saml2::Unmarshaller<T> *unmarshaller, const SOAPDocument& soapDocument);
            SOAPDocument wrap(DOMElement *objectElement, const std::string& characterEncoding);

        private:

            SOAP11Handler(const SOAP11Handler& other);
            SOAP11Handler& operator=(const SOAP11Handler& other);

            saml2::LocalLogger mLocalLogger;
            std::unique_ptr<saml2::Unmarshaller<soap::v11::Envelope>> mEnvelopeUnmarshaller;
            std::unique_ptr<saml2::Marshaller<soap::v11::Envelope>> mEnvelopeMarshaller;
            std::unique_ptr<saml2::Unmarshaller<soap::v11::Body>> mBodyUnmarshaller;
            std::unique_ptr<saml2::Marshaller<soap::v11::Body>> mBodyMarshaller;
            XMLCh* mImplFlags;
            DOMImplementation* mDomImpl;
        };

        /**
        * Class definition for SOAP 1.2 handler.
        */
        class SOAP12Handler
        {
        public:

            SOAP12Handler(saml2::Logger *logger, const std::string& schemaPath);
            ~SOAP12Handler();
            template <typename T>
            T* unwrap(saml2::Unmarshaller<T> *unmarshaller, const SOAPDocument& soapDocument);
            SOAPDocument wrap(DOMElement *objectElement, const std::string& characterEncoding);

        private:

            SOAP12Handler(const SOAP12Handler& other);
            SOAP12Handler& operator=(const SOAP12Handler& other);

            saml2::LocalLogger mLocalLogger;
            std::unique_ptr<saml2::Unmarshaller<soap::v12::Envelope>> mEnvelopeUnmarshaller;
            std::unique_ptr<saml2::Marshaller<soap::v12::Envelope>> mEnvelopeMarshaller;
            XMLCh* mImplFlags;
            DOMImplementation* mDomImpl;
        };

        saml2::LocalLogger mLocalLogger;
        std::unique_ptr<SOAP11Handler> mSoap11Handler;
        std::unique_ptr<SOAP12Handler> mSoap12Handler;

        SOAPUtil(const SOAPUtil& other);
        SOAPUtil& operator=(const SOAPUtil& other);
    };

    /*
     * Despite the code for these methods being very similar, they will remain seperate
     * so that any differences between SOAP 1.1 and 1.2 don't require massive refactoring
     * to implement.
     *
     * That said, I know this could be done more cleanly with templates :)
     */
    template <typename T>
    inline T* SOAPUtil::SOAP11Handler::unwrap(saml2::Unmarshaller<T> *unmarshaller, const SOAPDocument& soapDocument)
    {
        std::auto_ptr<soap::v11::Envelope> envelope(mEnvelopeUnmarshaller->unMarshallUnSigned(soapDocument, true));

        mLocalLogger.debug() << "Unmarshalled envelope successfully. Going to process Body.";

        // Not going to change anything. Just seems dumb that we can't get a reference without being const
        soap::v11::Body &body = const_cast<soap::v11::Body&>(envelope->Body());

        soap::v11::Body::any_iterator bodyAnyIterator;
        for (bodyAnyIterator = body.any().begin();
            bodyAnyIterator != body.any().end();
            ++bodyAnyIterator)
        {
            DOMElement *root = &(*bodyAnyIterator);
            xml_schema::dom::auto_ptr<DOMDocument> domDoc(mDomImpl->createDocument(root->getNamespaceURI(), root->getLocalName(), 0));

            {
                XercesCharStringAdapter localName(XMLString::transcode(root->getLocalName()));
                XercesCharStringAdapter namespaceURI(XMLString::transcode(root->getNamespaceURI()));
                mLocalLogger.debug() << "Got document element with namespace " << namespaceURI.get() << " and local name " << localName.get();
            }

            DOMElement *documentElement = domDoc->getDocumentElement();
            if (documentElement != NULL)
            {
                domDoc->removeChild(documentElement);
            }

            domDoc->appendChild(domDoc->importNode(root, true));
            unmarshaller->validateSignature(domDoc.get());

            return unmarshaller->unMarshallUnSignedElement(static_cast<DOMElement*>(root->cloneNode(true)), true);
        }

        mLocalLogger.error() << "Fell off the loop while looking for a SOAP <Body> element. Returning NULL to caller.";
        return NULL;
    }

    template <typename T>
    inline T* SOAPUtil::SOAP12Handler::unwrap(saml2::Unmarshaller<T> *unmarshaller, const SOAPDocument& soapDocument)
    {
        std::auto_ptr<soap::v12::Envelope> envelope(mEnvelopeUnmarshaller->unMarshallUnSigned(soapDocument, true));

        mLocalLogger.debug() << "Unmarshalled envelope successfully. Going to process Body.";

        // Not going to change anything. Just seems dumb that we can't get a reference without being const
        soap::v12::Body &body = const_cast<soap::v12::Body&>(envelope->Body());

        soap::v12::Body::any_iterator bodyAnyIterator;
        for (bodyAnyIterator = body.any().begin();
            bodyAnyIterator != body.any().end();
            ++bodyAnyIterator)
        {
            DOMElement *root = &(*bodyAnyIterator);
            xml_schema::dom::auto_ptr<DOMDocument> domDoc(mDomImpl->createDocument(root->getNamespaceURI(), root->getLocalName(), 0));

            {
                XercesCharStringAdapter localName(XMLString::transcode(root->getLocalName()));
                XercesCharStringAdapter namespaceURI(XMLString::transcode(root->getNamespaceURI()));
                mLocalLogger.debug() << "Got document element with namespace " << namespaceURI.get() << " and local name " << localName.get();
            }

            DOMElement *documentElement = domDoc->getDocumentElement();
            if (documentElement != NULL)
            {
                domDoc->removeChild(documentElement);
            }

            domDoc->appendChild(domDoc->importNode(root, true));
            unmarshaller->validateSignature(domDoc.get());

            return unmarshaller->unMarshallUnSignedElement(static_cast<DOMElement*>(root->cloneNode(true)), true);
        }

        mLocalLogger.error() << "Fell off the loop while looking for a SOAP <Body> element. Returning NULL to caller.";
        return NULL;
    }
}

#endif /*SOAPUTIL_H_*/

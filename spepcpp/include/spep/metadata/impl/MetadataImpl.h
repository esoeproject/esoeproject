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
 * Creation Date: 28/02/2007
 * 
 * Purpose: 
 */

#ifndef METADATAIMPL_H_
#define METADATAIMPL_H_

#include "spep/Util.h"
#include "spep/metadata/Metadata.h"
#include "spep/metadata/MetadataThread.h"

#include "saml2/handlers/Marshaller.h"
#include "saml2/handlers/Unmarshaller.h"
#include "saml2/handlers/MetadataOutput.h"
#include "saml2/logging/api.h"
#include "saml2/logging/api.h"

#include <map>

#include <boost/thread.hpp>

#include "saml2/bindings/saml-schema-metadata-2.0.hxx"
#include "saml2/bindings/lxacml-schema-metadata.hxx"
#include "saml2/bindings/spepstartup-schema-saml-metadata.hxx"

namespace spep
{
	
    class SPEPEXPORT MetadataImpl : public Metadata
    {

        friend class MetadataThread;
        friend class MetadataThread::ThreadHandler;

        typedef std::map<std::string, saml2::KeyData> KeyMap; // todo check to see if this should be an unordered_map

        class MetadataCache
        {
        public:
            std::vector<std::string> singleSignOnEndpoints;
            std::vector<std::string> singleLogoutEndpoints;
            std::vector<std::string> attributeServiceEndpoints;
            std::vector<std::string> authzServiceEndpoints;
            std::vector<std::string> spepStartupServiceEndpoints;
            KeyMap mKeys;
        };

        class MetadataKeyResolver : public XSECKeyInfoResolver
        {
        public:
            MetadataKeyResolver(saml2::Logger *logger, const KeyMap& map);
            virtual ~MetadataKeyResolver();
            virtual XSECCryptoKey *resolveKey(DSIGKeyInfoList *list) override;
            virtual saml2::KeyData resolveKey(const std::string& keyName);
            virtual XSECKeyInfoResolver* clone() const;

        private:
            saml2::Logger *mLogger;
            saml2::LocalLogger mLocalLogger;
            KeyMap mKeys;
        };

    public:
        MetadataImpl(saml2::Logger *logger, const std::string& schemaPath, const std::wstring& spepIdentifier, const std::wstring& esoeIdentifier, const std::string& metadataURL, const std::string& caBundle, KeyResolver *keyResolver, int assertionConsumerIndex, int interval);
        virtual ~MetadataImpl();

        virtual const std::wstring getSPEPIdentifier() const override;
        virtual const std::wstring getESOEIdentifier() const override;
        virtual const std::string getSingleSignOnEndpoint() const override;
        virtual const std::string getSingleLogoutEndpoint() const override;
        virtual const std::string getAttributeServiceEndpoint() const override;
        virtual const std::string getAuthzServiceEndpoint() const override;
        virtual const std::string getSPEPStartupServiceEndpoint() const override;
        virtual XSECCryptoKey *resolveKey(DSIGKeyInfoList *list) override;
        virtual saml2::KeyData resolveKey(const std::string& keyName) override;

        /**
         * This method should never be called on the MetadataImpl.
         */
        virtual XSECKeyInfoResolver* clone() const override;

    private:

        /**
        * Rebuilds the metadata cache from an XSD object
        * @param entitiesDescriptor The XSD object for the root of the metadata document
        * @param hashValue The hash value of the document (algorithm doesn't matter, just needs to be the same each time)
        * @param keyMap The map of key data returned from the unmarshaller
        */
        void rebuildCache(saml2::metadata::EntitiesDescriptorType &entitiesDescriptor, const std::string& hashValue, KeyMap &keyMap);

        /**
        * Recursive call for rebuilding cache data.
        * @see rebuildCache
        */
        void buildCacheRecurse(saml2::metadata::EntitiesDescriptorType &entitiesDescriptor, MetadataCache &cache);

        /**
        * Initializes the metadata
        */
        virtual void init();
        int nextBalancingIndex(int modulus) const;
        void waitForData() const;

        mutable Mutex mMetadataMutex;
        mutable saml2::Logger *mLogger;
        mutable saml2::LocalLogger mLocalLogger;
        std::wstring mSpepIdentifier;
        std::wstring mEsoeIdentifier;
        std::string mMetadataURL;
        std::string mSchemaPath;
        KeyResolver *mKeyResolver;
        int mAssertionConsumerIndex;
        int mInterval;
        mutable int mBalancingIndex;
        mutable Mutex mIndexMutex;

        std::unique_ptr<saml2::Unmarshaller<middleware::spepStartupServiceSchema::SPEPStartupServiceType>> mSpepStartupServiceUnmarshaller;

        std::string mCurrentRevision;
        bool mError;
        bool mHasData;
        MetadataCache *mCache;  // TODO: should this be a unique_ptr?

        std::string mCABundle;
        std::unique_ptr<MetadataThread> mThread;
        boost::thread_group mThreadGroup;
    };
	
}

#endif /*METADATAIMPL_H_*/

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
#include "spep/reporting/ReportingProcessor.h"
#include "spep/reporting/LocalReportingProcessor.h"

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
		
		typedef std::map<std::string, saml2::KeyData> KeyMap;
		
		class MetadataCache
		{
			public:
			std::vector<std::string> singleSignOnEndpoints;
			std::vector<std::string> singleLogoutEndpoints;
			std::vector<std::string> attributeServiceEndpoints;
			std::vector<std::string> authzServiceEndpoints;
			std::vector<std::string> spepStartupServiceEndpoints;
			KeyMap keyMap;
		};
		
		class MetadataKeyResolver : public XSECKeyInfoResolver
		{
		
			private:
			ReportingProcessor *_reportingProcessor;
			LocalReportingProcessor _localReportingProcessor;
			KeyMap _map;
			
			public:
			MetadataKeyResolver( ReportingProcessor *reportingProcessor, const KeyMap& map );
			virtual ~MetadataKeyResolver();
			virtual XSECCryptoKey *resolveKey (DSIGKeyInfoList *list);
			virtual saml2::KeyData resolveKey (std::string keyName);
			virtual XSECKeyInfoResolver* clone() const;
			
		};
		
		private:
		mutable Mutex _metadataMutex;
		mutable ReportingProcessor *_reportingProcessor;
		mutable LocalReportingProcessor _localReportingProcessor;
		std::wstring _spepIdentifier;
		std::wstring _esoeIdentifier;
		std::string _metadataURL;
		std::string _schemaPath;
		KeyResolver *_keyResolver;
		int _assertionConsumerIndex;
		int _interval;
		mutable int _balancingIndex;
		mutable Mutex _indexMutex;
		
		//saml2::Marshaller<middleware::spepStartupServiceSchema::SPEPStartupServiceType> *_domElementMarshaller;
		saml2::Unmarshaller<middleware::spepStartupServiceSchema::SPEPStartupServiceType> *_spepStartupServiceUnmarshaller;
		
		std::string _currentRevision;
		bool _error;
		bool _hasData;
		MetadataCache *_cache;
		
		std::string _caBundle;
		MetadataThread *_thread;
		boost::thread_group _threadGroup;
		
		/**
		 * Rebuilds the metadata cache from an XSD object
		 * @param entitiesDescriptor The XSD object for the root of the metadata document
		 * @param hashValue The hash value of the document (algorithm doesn't matter, just needs to be the same each time)
		 * @param keyMap The map of key data returned from the unmarshaller
		 */
		void rebuildCache( saml2::metadata::EntitiesDescriptorType &entitiesDescriptor, std::string hashValue, KeyMap &keyMap );
		
		/**
		 * Recursive call for rebuilding cache data.
		 * @see rebuildCache
		 */
		void buildCacheRecurse( saml2::metadata::EntitiesDescriptorType &entitiesDescriptor, MetadataCache &cache );
		
		/**
		 * Initializes the metadata
		 */
		virtual void init();
		
		int nextBalancingIndex( int modulus ) const;
		
		void waitForData() const;
		
		public:
		MetadataImpl( ReportingProcessor *reportingProcessor, std::string schemaPath, std::wstring spepIdentifier, std::wstring esoeIdentifier, std::string metadataURL, std::string caBundle, KeyResolver *keyResolver, int assertionConsumerIndex, int interval );
		virtual ~MetadataImpl();
		
		virtual const std::wstring getSPEPIdentifier() const;
		virtual const std::wstring getESOEIdentifier() const;
		virtual const std::string getSingleSignOnEndpoint() const;
		virtual const std::string getSingleLogoutEndpoint() const;
		virtual const std::string getAttributeServiceEndpoint() const;
		virtual const std::string getAuthzServiceEndpoint() const;
		virtual const std::string getSPEPStartupServiceEndpoint() const;
		virtual XSECCryptoKey *resolveKey (DSIGKeyInfoList *list);
		virtual saml2::KeyData resolveKey (std::string keyName);
		
		/**
		 * This method should never be called on the MetadataImpl.
		 */
		virtual XSECKeyInfoResolver* clone() const;
		
	};
	
}

#endif /*METADATAIMPL_H_*/

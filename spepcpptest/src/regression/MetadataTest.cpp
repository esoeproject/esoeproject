/* Copyright 2006, Queensland University of Technology
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
 * Creation Date: 27/02/2007
 * 
 * Purpose: 
 */
#if 0
#include "regression/MetadataTest.h"

#include <string>

#include <curl/curl.h>

#include "metadata/KeyResolver.h"
#include "metadata/Metadata.h"
#include "metadata/MetadataThread.h"
#include "metadata/impl/MetadataImpl.h"

#include "reporting/ReportingProcessor.h"

#include "regression/Common.h"

#include "SAML2Defs.h"

SUITE( MetadataTest )
{
	
	class MetadataTest_TestObjects
	{
		
		public:
		spep::ReportingProcessor reportingProcessor;
		std::wstring spepIdentifier;
		std::wstring esoeIdentifier;
		std::string metadataURL;
		spep::KeyResolver keyResolver;
		KeyResolverInitializer keyResolverInitializer;
		int assertionConsumerIndex;
		int interval;
		std::string schemaPath;
		spep::MetadataImpl metadata;
		spep::MetadataThread metadataThread;
		
		MetadataTest_TestObjects()
		:
		reportingProcessor(),
		spepIdentifier( L"_2f4a8d8262dbcb8c4d139fcf0e877840334c27e7-85410fe4b9cca40e37145b45516f8d94" ),
		esoeIdentifier( L"_56b90453a68f16ccb686f658fd5ea81d40b1bf1a-4b9c6ac16f9069a5986bc2b77fd3813d" ),
		metadataURL( METADATA_URL ),
		keyResolver( TESTDATA_PATH, "myrsakey" ),
		keyResolverInitializer( this->keyResolver ),
		assertionConsumerIndex(0),
		interval(120),
		schemaPath(SCHEMA_PATH),
		metadata( &reportingProcessor, schemaPath, spepIdentifier, esoeIdentifier, metadataURL, &keyResolver, assertionConsumerIndex, interval ),
		metadataThread( &reportingProcessor, &metadata, schemaPath, interval )
		{
		}
		
	};


	TEST( MetadataTest_testMetadataLoad )
	{
		CHECK( curl_global_init( CURL_GLOBAL_ALL ) == 0 );
		
		// TODO This won't pass until the metadata document we have matches the key in the testdata directory.
		{
			MetadataTest_TestObjects testObjects;
			
			testObjects.metadataThread.doGetMetadata();
		}
	}

}
#endif

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
 * Creation Date: 15/03/2007
 * 
 * Purpose: 
 */

#ifndef METADATADISPATCHER_H_
#define METADATADISPATCHER_H_

#include "spep/Util.h"
#include "spep/ipc/Dispatcher.h"

#include "spep/metadata/Metadata.h"

namespace spep { namespace ipc {
	
#define METADATA "spep/metadata/Metadata/"
#define METADATA_getSPEPIdentifier METADATA "getSPEPIdentifier"
#define METADATA_getESOEIdentifier METADATA "getESOEIdentifier"
#define METADATA_getSingleSignOnEndpoint METADATA "getSingleSignOnEndpoint"
#define METADATA_getSingleLogoutEndpoint METADATA "getSingleLogoutEndpoint"
#define METADATA_getAttributeServiceEndpoint METADATA "getAttributeServiceEndpoint"
#define METADATA_getAuthzServiceEndpoint METADATA "getAuthzServiceEndpoint"
#define METADATA_getSPEPStartupServiceEndpoint METADATA "getSPEPStartupServiceEndpoint"
#define METADATA_resolveKey METADATA "resolveKey"

	class SPEPEXPORT MetadataDispatcher : public Dispatcher
	{
		
		std::string _prefix;
		spep::Metadata *_metadata;
		
		public:
		MetadataDispatcher( spep::Metadata *metadata );
		virtual bool dispatch( MessageHeader &header, Engine &en );
		virtual ~MetadataDispatcher();
		
	};

} }

#endif /*METADATADISPATCHER_H_*/

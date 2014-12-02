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
 * Creation Date: Sep 7, 2007
 * 
 * Purpose: 
 */

#ifndef IDENTIFIERCACHEDISPATCHER_H_
#define IDENTIFIERCACHEDISPATCHER_H_

#include "saml2/identifier/IdentifierCache.h"

#include "spep/Util.h"
#include "spep/ipc/Dispatcher.h"


#define IDENTIFIERCACHE "saml2/identifier/IdentifierCache/"
#define IDENTIFIERCACHE_registerIdentifier IDENTIFIERCACHE "registerIdentifier"
#define IDENTIFIERCACHE_containsIdentifier IDENTIFIERCACHE "containsIdentifier"
#define IDENTIFIERCACHE_cleanCache IDENTIFIERCACHE "cleanCache"

namespace spep { namespace ipc {
	
	class SPEPEXPORT IdentifierCacheDispatcher : public Dispatcher
	{
	public:

		IdentifierCacheDispatcher(saml2::IdentifierCache *identifierCache);
        virtual ~IdentifierCacheDispatcher();

		virtual bool dispatch(MessageHeader &header, Engine &en) override;
	
    private:

        std::string mPrefix;
        saml2::IdentifierCache *mIdentifierCache;
	};
	
} }

#endif /*IDENTIFIERCACHEDISPATCHER_H_*/

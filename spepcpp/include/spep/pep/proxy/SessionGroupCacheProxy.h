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
 * Creation Date: 14/02/2007
 * 
 * Purpose: 
 */

#ifndef SESSIONGROUPCACHEPROXY_H_
#define SESSIONGROUPCACHEPROXY_H_

#include "spep/Util.h"
#include "spep/pep/SessionGroupCache.h"

#include "spep/ipc/Socket.h"

namespace spep{ namespace ipc{
	
    class SPEPEXPORT SessionGroupCacheProxy : public spep::SessionGroupCache
    {
    public:
        SessionGroupCacheProxy(spep::ipc::ClientSocketPool *socketPool);
        virtual ~SessionGroupCacheProxy();
        virtual void updateCache(const std::wstring &sessionID, const UnicodeString& groupTarget, std::vector<UnicodeString> &authzTargets, Decision decision) override;
        virtual void clearCache(std::map< UnicodeString, std::vector<UnicodeString> > &groupTargets) override;
        virtual Decision makeCachedAuthzDecision(const std::wstring& sessionID, const UnicodeString& resource) override;

    private:
        spep::ipc::ClientSocketPool *mSocketPool;

    };
	
}}

#endif /*SESSIONGROUPCACHEPROXY_H_*/

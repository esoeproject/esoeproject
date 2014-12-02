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
 * Creation Date: 22/06/2009
 *
 * Purpose:
 */

#ifndef IDENTIFIERCACHEPROXY_H_
#define IDENTIFIERCACHEPROXY_H_

#include "spep/Util.h"
#include "spep/ipc/Socket.h"

#include "saml2/identifier/IdentifierCache.h"

#include <string>

namespace spep { namespace ipc {

	class SPEPEXPORT IdentifierCacheProxy : public saml2::IdentifierCache
	{
	public:

		IdentifierCacheProxy(ClientSocketPool *socketPool);
        virtual ~IdentifierCacheProxy();

		/// @see saml2::IdentifierCache
		/**@{*/
		virtual void registerIdentifier(const std::string& identifier) override;
		virtual bool containsIdentifier(const std::string& identifier) override;
		virtual int cleanCache(long age) override;
		/**@}*/
	
    private:

        ClientSocketPool *mSocketPool;
    };

} }

#endif /*IDENTIFIERCACHEPROXY_H_*/

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

#ifndef METADATAPROXY_H_
#define METADATAPROXY_H_

#include "saml2/handlers/MetadataOutput.h"

#include "spep/Util.h"
#include "spep/metadata/Metadata.h"
#include "spep/ipc/Socket.h"

#include <unicode/unistr.h>

#include "saml2/SAML2Defs.h"

namespace spep { namespace ipc {
	
	class SPEPEXPORT MetadataProxy : public Metadata
	{
		
		typedef std::vector< XSECCryptoKey* > CryptoKeyPointerList;
		
		public:
		MetadataProxy( spep::ipc::ClientSocketPool *socketPool );
		virtual ~MetadataProxy();
		
		virtual const std::wstring getSPEPIdentifier() const;
		virtual const std::wstring getESOEIdentifier() const;
		virtual const std::string getSingleSignOnEndpoint() const;
		virtual const std::string getSingleLogoutEndpoint() const;
		virtual const std::string getAttributeServiceEndpoint() const;
		virtual const std::string getAuthzServiceEndpoint() const;
		virtual const std::string getSPEPStartupServiceEndpoint() const;
		virtual XSECCryptoKey* resolveKey(DSIGKeyInfoList *lst);
		virtual saml2::KeyData resolveKey(const std::string& keyName) override;
		virtual XSECKeyInfoResolver* clone() const override;
				
		private:
		spep::ipc::ClientSocketPool *mSocketPool;
		CryptoKeyPointerList mCryptoKeyList;
		
	};
	
} }

#endif /*METADATAPROXY_H_*/

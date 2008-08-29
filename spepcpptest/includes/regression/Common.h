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
 * Creation Date: 13/03/2007
 * 
 * Purpose: 
 */

#ifndef COMMON_H_
#define COMMON_H_

#include "metadata/KeyResolver.h"
#include "metadata/Metadata.h"

#include <exception>

#define WORKSPACE_PATH			"/home/shaun/workspaces/esoe"
#define SPEP_LIB_CPP_PATH		WORKSPACE_PATH "/spepcpp"
#define SAML2LIB_CPP_PATH		WORKSPACE_PATH "/saml2cpp"

#define SCHEMA_PATH				SAML2LIB_CPP_PATH "/schema"
// TESTDATA_PATH must be suitable to go in a URL, as in sprintf(b,"file://%s/filename",TESTDATA_PATH)
#define TESTDATA_PATH			SPEP_LIB_CPP_PATH "/testdata"

#define METADATA_URL			"file://" TESTDATA_PATH "/authenticationNetworkMetadata.xml"

class KeyResolverInitializer
{
	public:
	KeyResolverInitializer(spep::KeyResolver &keyResolver)
	{
		try
		{
			keyResolver.loadSPEPPublicKey( "testkey.pem" );
			keyResolver.loadSPEPPrivateKey( "testkey-priv.pem" );
			keyResolver.loadMetadataKey( "aaamanager.pem" );
		}
		catch (...)
		{
			std::cerr << "Exception while trying to load keys. Check " TESTDATA_PATH << std::endl;
		}
	}
};

class NotImplementedException : public std::exception
{
	std::string _reason;
	
	public:
	NotImplementedException( std::string reason )
	: _reason(reason)
	{}
	
	const char *what() const throw()
	{
		return _reason.c_str();
	}
	
	~NotImplementedException() throw(){}
};

class TestMetadata : virtual spep::Metadata
{
	
	private:
	std::wstring _spepIdentifier;
	std::wstring _esoeIdentifier;
	spep::KeyResolver *_keyResolver;
	
	public:
	TestMetadata( std::wstring spepIdentifier, std::wstring esoeIdentifier, spep::KeyResolver *keyResolver )
	:
	_spepIdentifier( spepIdentifier ),
	_esoeIdentifier( esoeIdentifier ),
	_keyResolver( keyResolver )
	{
	}
	
	virtual const std::wstring getSPEPIdentifier() const
	{
		return _spepIdentifier;
	}
	
	virtual const std::wstring getESOEIdentifier() const
	{
		return _esoeIdentifier;
	}
	
	virtual const std::string getSingleSignOnEndpoint() const
	{
		throw NotImplementedException( std::string("Test metadata has no single sign-on endpoint implementation" ) );
	}
	
	virtual const std::string getSingleLogoutEndpoint() const
	{
		throw NotImplementedException( std::string("Test metadata has no single logout endpoint implementation" ) );
	}
	
	virtual const std::string getAttributeServiceEndpoint() const
	{
		throw NotImplementedException( std::string("Test metadata has no attribute service endpoint implementation" ) );
	}
	
	virtual const std::string getAuthzServiceEndpoint() const
	{
		throw NotImplementedException( std::string("Test metadata has no authz service endpoint implementation" ) );
	}
	
	virtual const std::string getSPEPStartupServiceEndpoint() const
	{
		throw NotImplementedException( std::string("Test metadata has no spep startup service endpoint implementation" ) );
	}

	virtual XSECCryptoKey *resolveKey (DSIGKeyInfoList *lst)
	{
		return _keyResolver->getSPEPPublicKey();
	}
	
	virtual saml2::KeyData resolveKey (std::string keyName)
	{
		throw NotImplementedException( std::string("Test metadata has no KeyData resolveKey(string) implementation" ) );
	}
	
};

extern int globalTestCounter;

#endif /*COMMON_H_*/

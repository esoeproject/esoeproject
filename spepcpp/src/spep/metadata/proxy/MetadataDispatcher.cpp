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

#include "spep/metadata/proxy/MetadataDispatcher.h"

static const char *getSPEPIdentifier = METADATA_getSPEPIdentifier;
static const char *getESOEIdentifier = METADATA_getESOEIdentifier;
static const char *getSingleSignOnEndpoint = METADATA_getSingleSignOnEndpoint;
static const char *getSingleLogoutEndpoint = METADATA_getSingleLogoutEndpoint;
static const char *getAttributeServiceEndpoint = METADATA_getAttributeServiceEndpoint;
static const char *getAuthzServiceEndpoint = METADATA_getAuthzServiceEndpoint;
static const char *getSPEPStartupServiceEndpoint = METADATA_getSPEPStartupServiceEndpoint;
static const char *resolveKey = METADATA_resolveKey;

spep::ipc::MetadataDispatcher::MetadataDispatcher( spep::Metadata *metadata )
:
_prefix( METADATA ),
_metadata( metadata )
{
}

spep::ipc::MetadataDispatcher::~MetadataDispatcher()
{
}

bool spep::ipc::MetadataDispatcher::dispatch( spep::ipc::MessageHeader &header, spep::ipc::Engine &en )
{	
	std::string dispatch = header.getDispatch();
	
	// Make sure the prefix matches the expected prefix for this dispatcher.
	if ( dispatch.compare( 0, strlen( METADATA ), _prefix ) != 0 )
		return false;
	
	if ( dispatch.compare( getSPEPIdentifier ) == 0 )
	{
		std::wstring spepIdentifier( _metadata->getSPEPIdentifier() );
		
		if ( header.getType() == SPEPIPC_REQUEST )
		{
			// Return the value
			en.sendResponseHeader();
			en.sendObject( spepIdentifier );
		}
		
		return true;
		
	}
	
	if ( dispatch.compare( getESOEIdentifier ) == 0 )
	{
		std::wstring esoeIdentifier( _metadata->getESOEIdentifier() );
		
		if ( header.getType() == SPEPIPC_REQUEST )
		{
			// Return the value
			en.sendResponseHeader();
			en.sendObject( esoeIdentifier );
		}
		
		return true;
	}

	if ( dispatch.compare( getSingleSignOnEndpoint ) == 0 )
	{
		std::string singleSignOnEndpoint( _metadata->getSingleSignOnEndpoint() );
		
		if ( header.getType() == SPEPIPC_REQUEST )
		{
			// Return the value
			en.sendResponseHeader();
			en.sendObject( singleSignOnEndpoint );
		}
		
		return true;
	}

	if ( dispatch.compare( getSingleLogoutEndpoint ) == 0 )
	{
		std::string singleLogoutEndpoint( _metadata->getSingleLogoutEndpoint() );
		
		if ( header.getType() == SPEPIPC_REQUEST )
		{
			// Return the value
			en.sendResponseHeader();
			en.sendObject( singleLogoutEndpoint );
		}
		
		return true;
	}

	if ( dispatch.compare( getAttributeServiceEndpoint ) == 0 )
	{
		std::string attributeServiceEndpoint( _metadata->getAttributeServiceEndpoint() );
		
		if ( header.getType() == SPEPIPC_REQUEST )
		{
			// Return the value
			en.sendResponseHeader();
			en.sendObject( attributeServiceEndpoint );
		}
		
		return true;
	}

	if ( dispatch.compare( getAuthzServiceEndpoint ) == 0 )
	{
		std::string authzServiceEndpoint( _metadata->getAuthzServiceEndpoint() );
		
		if ( header.getType() == SPEPIPC_REQUEST )
		{
			// Return the value
			en.sendResponseHeader();
			en.sendObject( authzServiceEndpoint );
		}
		
		return true;
	}

	if ( dispatch.compare( getSPEPStartupServiceEndpoint ) == 0 )
	{
		std::string spepStartupServiceEndpoint( _metadata->getSPEPStartupServiceEndpoint() );
		
		if ( header.getType() == SPEPIPC_REQUEST )
		{
			// Return the value
			en.sendResponseHeader();
			en.sendObject( spepStartupServiceEndpoint );
		}
		
		return true;
	}

	if ( dispatch.compare( resolveKey ) == 0 )
	{
		std::string keyName;
		en.getObject( keyName );
		
		saml2::KeyData keyData( _metadata->resolveKey( keyName ) );
		
		if ( header.getType() == SPEPIPC_REQUEST )
		{
			// Return the value
			en.sendResponseHeader();
			en.sendObject( keyData );
		}
		
		return true;
	}

	return false;
}

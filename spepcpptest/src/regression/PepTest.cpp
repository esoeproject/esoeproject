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
 * Creation Date: 13/02/2007
 * 
 * Purpose: 
 */

#include "regression/PepTest.h"

#include "ipc/Platform.h"
#include "ipc/Socket.h"
#include "ipc/Exceptions.h"

#include "metadata/KeyResolver.h"
#include "metadata/impl/MetadataImpl.h"
#include "pep/SessionGroupCache.h"
#include "pep/impl/SessionGroupCacheImpl.h"
#include "pep/proxy/SessionGroupCacheProxy.h"
#include "pep/proxy/SessionGroupCacheDispatcher.h"
#include "pep/PolicyEnforcementProcessor.h"
#include "pep/PolicyEnforcementProcessorData.h"

#include "regression/Common.h"

#include "identifier/IdentifierCache.h"
#include "identifier/IdentifierGenerator.h"

#include <string>

#include <iostream>

std::ostream& operator<<(std::ostream& lhs, spep::Decision rhs)
{
	std::string permit("PERMIT"),deny("DENY"),cache("CACHE"),error("ERROR"),none("NONE");
	if (rhs == spep::Decision::PERMIT)
	{ lhs << permit; }
	else if (rhs == spep::Decision::DENY)
	{ lhs << deny; }
	else if (rhs == spep::Decision::CACHE)
	{ lhs << cache; }
	else if (rhs == spep::Decision::ERROR)
	{ lhs << error; }
	else
	{ lhs << none; }
	
	return lhs;
}

#define RETURN_IF_FALSE(x) if(!(x)) return false

SUITE( PepTest )
{
	
	static int port = 41000;
	
	bool testSessionGroupCacheBody( spep::SessionGroupCache *sessionGroupCache, spep::Decision defaultPolicyDecision )
	{
		{
			UnicodeString groupTarget1( UNICODE_STRING_SIMPLE( "/.*.jsp" ) );
			std::vector<UnicodeString> authzTargets1;
			authzTargets1.push_back( UNICODE_STRING_SIMPLE( "/admin/.*.jsp" ) );
			
			UnicodeString groupTarget2( UNICODE_STRING_SIMPLE( "/admin/.*" ) );
			std::vector<UnicodeString> authzTargets2;
			authzTargets2.push_back( UNICODE_STRING_SIMPLE( "/admin/secure/.*" ) );
			
			UnicodeString groupTarget3( UNICODE_STRING_SIMPLE( "/admin/secure/.*" ) );
			std::vector<UnicodeString> authzTargets3;
			authzTargets3.push_back( UNICODE_STRING_SIMPLE( ".*/secure/.*.gif" ) );
			
			std::map<UnicodeString, std::vector<UnicodeString> > groupTargetMap;
			groupTargetMap.insert(std::make_pair(groupTarget1, authzTargets1));
			groupTargetMap.insert(std::make_pair(groupTarget2, authzTargets2));
			groupTargetMap.insert(std::make_pair(groupTarget3, authzTargets3));
			sessionGroupCache->clearCache(groupTargetMap);
			
			std::wstring sessionID( L"thisisastringlol" );
			
			sessionGroupCache->updateCache(sessionID, groupTarget1, authzTargets1, spep::Decision::PERMIT);
			sessionGroupCache->updateCache(sessionID, groupTarget2, authzTargets2, spep::Decision::PERMIT);
			
			
			UnicodeString resource1( UNICODE_STRING_SIMPLE( "/somepage.jsp" ) );
			spep::Decision decision1 = defaultPolicyDecision;
			UnicodeString resource2( UNICODE_STRING_SIMPLE( "/admin/somepage.jsp" ) );
			spep::Decision decision2 = spep::Decision::PERMIT;
			UnicodeString resource3( UNICODE_STRING_SIMPLE( "/admin/secure/somepage.jsp" ) );
			spep::Decision decision3 = spep::Decision::PERMIT;
			UnicodeString resource4( UNICODE_STRING_SIMPLE( "/admin/secure/icon.gif" ) );
			spep::Decision decision4 = spep::Decision::CACHE;
			
			RETURN_IF_FALSE( decision1 == sessionGroupCache->makeCachedAuthzDecision(sessionID, resource1) );
			RETURN_IF_FALSE( decision2 == sessionGroupCache->makeCachedAuthzDecision(sessionID, resource2) );
			RETURN_IF_FALSE( decision3 == sessionGroupCache->makeCachedAuthzDecision(sessionID, resource3) );
			RETURN_IF_FALSE( decision4 == sessionGroupCache->makeCachedAuthzDecision(sessionID, resource4) );
		}
		
		{
			UnicodeString groupTarget1( UNICODE_STRING_SIMPLE( "/.*.jsp" ) );
			std::vector<UnicodeString> authzTargets1;
			authzTargets1.push_back( UNICODE_STRING_SIMPLE( "/admin/.*.jsp" ) );
			
			UnicodeString groupTarget2( UNICODE_STRING_SIMPLE( "/admin/.*" ) );
			std::vector<UnicodeString> authzTargets2;
			authzTargets2.push_back( UNICODE_STRING_SIMPLE( "/admin/secure/.*" ) );
			
			UnicodeString groupTarget3( UNICODE_STRING_SIMPLE( "/admin/secure/.*" ) );
			std::vector<UnicodeString> authzTargets3;
			authzTargets3.push_back( UNICODE_STRING_SIMPLE( ".*/secure/.*.gif" ) );
			
			std::map<UnicodeString, std::vector<UnicodeString> > groupTargetMap;
			groupTargetMap.insert(std::make_pair(groupTarget1, authzTargets1));
			groupTargetMap.insert(std::make_pair(groupTarget2, authzTargets2));
			groupTargetMap.insert(std::make_pair(groupTarget3, authzTargets3));
			sessionGroupCache->clearCache(groupTargetMap);
			
			std::wstring sessionID( L"thisisastringlol" );
			
			sessionGroupCache->updateCache(sessionID, groupTarget1, authzTargets1, spep::Decision::DENY);
			sessionGroupCache->updateCache(sessionID, groupTarget2, authzTargets2, spep::Decision::PERMIT);
			sessionGroupCache->updateCache(sessionID, groupTarget3, authzTargets3, spep::Decision::PERMIT);
			
			
			UnicodeString resource1( UNICODE_STRING_SIMPLE( "/somepage.jsp" ) );
			spep::Decision decision1 = defaultPolicyDecision;
			UnicodeString resource2( UNICODE_STRING_SIMPLE( "/admin/somepage.jsp" ) );
			spep::Decision decision2 = spep::Decision::DENY;
			UnicodeString resource3( UNICODE_STRING_SIMPLE( "/admin/secure/somepage.jsp" ) );
			spep::Decision decision3 = spep::Decision::DENY;
			UnicodeString resource4( UNICODE_STRING_SIMPLE( "/admin/secure/icon.gif" ) );
			spep::Decision decision4 = spep::Decision::PERMIT;
			
			RETURN_IF_FALSE( decision1 == sessionGroupCache->makeCachedAuthzDecision(sessionID, resource1) );
			RETURN_IF_FALSE( decision2 == sessionGroupCache->makeCachedAuthzDecision(sessionID, resource2) );
			RETURN_IF_FALSE( decision3 == sessionGroupCache->makeCachedAuthzDecision(sessionID, resource3) );
			RETURN_IF_FALSE( decision4 == sessionGroupCache->makeCachedAuthzDecision(sessionID, resource4) );
		}
	
		{
			UnicodeString groupTarget1( UNICODE_STRING_SIMPLE( "/.*.jsp" ) );
			std::vector<UnicodeString> authzTargets1;
			authzTargets1.push_back( UNICODE_STRING_SIMPLE( "/admin/.*.jsp" ) );
			
			UnicodeString groupTarget2( UNICODE_STRING_SIMPLE( "/admin/.*" ) );
			std::vector<UnicodeString> authzTargets2;
			authzTargets2.push_back( UNICODE_STRING_SIMPLE( "/admin/secure/.*" ) );
			
			UnicodeString groupTarget3( UNICODE_STRING_SIMPLE( "/admin/secure/.*" ) );
			std::vector<UnicodeString> authzTargets3;
			authzTargets3.push_back( UNICODE_STRING_SIMPLE( ".*/secure/.*.gif" ) );
			
			std::map<UnicodeString, std::vector<UnicodeString> > groupTargetMap;
			groupTargetMap.insert(std::make_pair(groupTarget1, authzTargets1));
			groupTargetMap.insert(std::make_pair(groupTarget2, authzTargets2));
			groupTargetMap.insert(std::make_pair(groupTarget3, authzTargets3));
			sessionGroupCache->clearCache(groupTargetMap);
			
			std::wstring sessionID( L"thisisastringlol" );
			
			sessionGroupCache->updateCache(sessionID, groupTarget1, authzTargets1, spep::Decision::PERMIT);
			sessionGroupCache->updateCache(sessionID, groupTarget2, authzTargets2, spep::Decision::DENY);
			sessionGroupCache->updateCache(sessionID, groupTarget3, authzTargets3, spep::Decision::PERMIT);
			
			
			UnicodeString resource1( UNICODE_STRING_SIMPLE( "/somepage.jsp" ) );
			spep::Decision decision1 = defaultPolicyDecision;
			UnicodeString resource2( UNICODE_STRING_SIMPLE( "/admin/somepage.jsp" ) );
			spep::Decision decision2 = spep::Decision::PERMIT;
			UnicodeString resource3( UNICODE_STRING_SIMPLE( "/admin/secure/somepage.jsp" ) );
			spep::Decision decision3 = spep::Decision::DENY;
			UnicodeString resource4( UNICODE_STRING_SIMPLE( "/admin/secure/icon.gif" ) );
			spep::Decision decision4 = spep::Decision::DENY;
			
			RETURN_IF_FALSE( decision1 == sessionGroupCache->makeCachedAuthzDecision(sessionID, resource1) );
			RETURN_IF_FALSE( decision2 == sessionGroupCache->makeCachedAuthzDecision(sessionID, resource2) );
			RETURN_IF_FALSE( decision3 == sessionGroupCache->makeCachedAuthzDecision(sessionID, resource3) );
			RETURN_IF_FALSE( decision4 == sessionGroupCache->makeCachedAuthzDecision(sessionID, resource4) );
		}
	
		{
			UnicodeString groupTarget1( UNICODE_STRING_SIMPLE( "/.*.jsp" ) );
			std::vector<UnicodeString> authzTargets1;
			authzTargets1.push_back( UNICODE_STRING_SIMPLE( "/admin/.*.jsp" ) );
			
			UnicodeString groupTarget2( UNICODE_STRING_SIMPLE( "/admin/.*" ) );
			std::vector<UnicodeString> authzTargets2;
			authzTargets2.push_back( UNICODE_STRING_SIMPLE( "/admin/secure/.*" ) );
			
			UnicodeString groupTarget3( UNICODE_STRING_SIMPLE( "/admin/secure/.*" ) );
			std::vector<UnicodeString> authzTargets3;
			authzTargets3.push_back( UNICODE_STRING_SIMPLE( ".*/secure/.*.gif" ) );
			
			std::map<UnicodeString, std::vector<UnicodeString> > groupTargetMap;
			groupTargetMap.insert(std::make_pair(groupTarget1, authzTargets1));
			groupTargetMap.insert(std::make_pair(groupTarget2, authzTargets2));
			groupTargetMap.insert(std::make_pair(groupTarget3, authzTargets3));
			sessionGroupCache->clearCache(groupTargetMap);
			
			std::wstring sessionID( L"thisisastringlol" );
			
			sessionGroupCache->updateCache(sessionID, groupTarget1, authzTargets1, spep::Decision::NONE);
			sessionGroupCache->updateCache(sessionID, groupTarget2, authzTargets2, spep::Decision::PERMIT);
			sessionGroupCache->updateCache(sessionID, groupTarget3, authzTargets3, spep::Decision::PERMIT);
			
			
			UnicodeString resource1( UNICODE_STRING_SIMPLE( "/somepage.jsp" ) );
			spep::Decision decision1 = defaultPolicyDecision;
			UnicodeString resource2( UNICODE_STRING_SIMPLE( "/admin/somepage.jsp" ) );
			spep::Decision decision2 = spep::Decision::CACHE;
			UnicodeString resource3( UNICODE_STRING_SIMPLE( "/admin/secure/somepage.jsp" ) );
			spep::Decision decision3 = spep::Decision::CACHE;
			UnicodeString resource4( UNICODE_STRING_SIMPLE( "/admin/secure/icon.gif" ) );
			spep::Decision decision4 = spep::Decision::PERMIT;
			
			RETURN_IF_FALSE( decision1 == sessionGroupCache->makeCachedAuthzDecision(sessionID, resource1) );
			RETURN_IF_FALSE( decision2 == sessionGroupCache->makeCachedAuthzDecision(sessionID, resource2) );
			RETURN_IF_FALSE( decision3 == sessionGroupCache->makeCachedAuthzDecision(sessionID, resource3) );
			RETURN_IF_FALSE( decision4 == sessionGroupCache->makeCachedAuthzDecision(sessionID, resource4) );
		}
	
		{
			UnicodeString groupTarget1( UNICODE_STRING_SIMPLE( "/.*.jsp" ) );
			std::vector<UnicodeString> authzTargets1;
			authzTargets1.push_back( UNICODE_STRING_SIMPLE( ".*/secure/.*" ) );
			
			UnicodeString groupTarget2( UNICODE_STRING_SIMPLE( "/admin/.*" ) );
			std::vector<UnicodeString> authzTargets2;
			authzTargets2.push_back( UNICODE_STRING_SIMPLE( "/admin/secure/.*" ) );
			authzTargets2.push_back( UNICODE_STRING_SIMPLE( "/admin/.*\\.jsp" ) );
			
			UnicodeString groupTarget3( UNICODE_STRING_SIMPLE( "/admin/secure/.*" ) );
			std::vector<UnicodeString> authzTargets3;
			authzTargets3.push_back( UNICODE_STRING_SIMPLE( ".*/secure/.*\\.gif" ) );
			
			std::map<UnicodeString, std::vector<UnicodeString> > groupTargetMap;
			groupTargetMap.insert(std::make_pair(groupTarget1, authzTargets1));
			groupTargetMap.insert(std::make_pair(groupTarget2, authzTargets2));
			groupTargetMap.insert(std::make_pair(groupTarget3, authzTargets3));
			sessionGroupCache->clearCache(groupTargetMap);
			
			std::wstring sessionID( L"thisisastringlol" );
			
			sessionGroupCache->updateCache(sessionID, groupTarget1, authzTargets1, spep::Decision::DENY);
			sessionGroupCache->updateCache(sessionID, groupTarget2, authzTargets2, spep::Decision::PERMIT);
			sessionGroupCache->updateCache(sessionID, groupTarget3, authzTargets3, spep::Decision::PERMIT);
			
			
			UnicodeString resource1( UNICODE_STRING_SIMPLE( "/somepage.jsp" ) );
			spep::Decision decision1 = defaultPolicyDecision;
			UnicodeString resource2( UNICODE_STRING_SIMPLE( "/admin/somepage.jsp" ) );
			spep::Decision decision2 = spep::Decision::PERMIT;
			UnicodeString resource3( UNICODE_STRING_SIMPLE( "/admin/secure/somepage.jsp" ) );
			spep::Decision decision3 = spep::Decision::DENY;
			UnicodeString resource4( UNICODE_STRING_SIMPLE( "/admin/secure/icon.gif" ) );
			spep::Decision decision4 = spep::Decision::PERMIT;
			
			RETURN_IF_FALSE( decision1 == sessionGroupCache->makeCachedAuthzDecision(sessionID, resource1) );
			RETURN_IF_FALSE( decision2 == sessionGroupCache->makeCachedAuthzDecision(sessionID, resource2) );
			RETURN_IF_FALSE( decision3 == sessionGroupCache->makeCachedAuthzDecision(sessionID, resource3) );
			RETURN_IF_FALSE( decision4 == sessionGroupCache->makeCachedAuthzDecision(sessionID, resource4) );
		}
	
		return true;
	}
	
	bool testSessionGroupCacheProxyBody( spep::Decision defaultPolicyDecision )
	{
		int testport = port++;
		spep::ipc::platform::socket_t serverSock = spep::ipc::platform::openSocket();
		spep::ipc::platform::bindLoopbackSocket( serverSock, testport );
		spep::ipc::platform::listenSocket( serverSock );
		
		pid_t pid = fork();
		
		if (pid > 0)
		{
			spep::SessionGroupCache *sessionGroupCache;
			try
			{
				spep::ipc::ClientSocket clientSocket( testport );
				spep::ipc::SessionGroupCacheProxy sessionGroupCacheProxy( &clientSocket );
				sessionGroupCache = &sessionGroupCacheProxy;
				
				RETURN_IF_FALSE( testSessionGroupCacheBody( sessionGroupCache, defaultPolicyDecision ) );
			}
			catch (spep::ipc::IPCException e)
			{
				return false;
			}
		}
		else
		{
			try
			{
				spep::ipc::platform::socket_t clientSock = spep::ipc::platform::acceptSocket( serverSock );
				spep::ReportingProcessor reportingProcessor;
				spep::SessionGroupCacheImpl sessionGroupCache( &reportingProcessor, defaultPolicyDecision );
				spep::ipc::SessionGroupCacheDispatcher dispatcher( &sessionGroupCache );
				spep::ipc::ServerSocket<spep::ipc::SessionGroupCacheDispatcher> ss( dispatcher, port++ );
				
				ss.run(clientSock);
			}
			catch (std::exception e)
			{
				std::cerr << e.what() << std::endl;
			}
			catch (...)
			{
				std::cerr << "uncaught exception in child" << std::endl;
			}
			
			_exit(0);
		}
		
		return true;
	}
	
	TEST( PepTest_testSessionGroupCacheImpl )
	{
		spep::ReportingProcessor reportingProcessor;
		spep::SessionGroupCache *sessionGroupCache = new spep::SessionGroupCacheImpl( &reportingProcessor, spep::Decision::DENY );
		CHECK( testSessionGroupCacheBody( sessionGroupCache, spep::Decision::DENY ) );
		sessionGroupCache = new spep::SessionGroupCacheImpl( &reportingProcessor, spep::Decision::PERMIT );
		CHECK( testSessionGroupCacheBody( sessionGroupCache, spep::Decision::PERMIT ) );
	}
	
	TEST( PepTest_testSessionGroupCacheProxyDeny )
	{
		spep::Decision defaultPolicyDecision( spep::Decision::DENY );
		CHECK( testSessionGroupCacheProxyBody( defaultPolicyDecision ) );
		defaultPolicyDecision = spep::Decision::PERMIT;
		CHECK( testSessionGroupCacheProxyBody( defaultPolicyDecision ) );
	}
	
	class PepTest_TestObjects
	{
		
		public:
		spep::ReportingProcessor reportingProcessor;
		spep::WSClient wsClient;
		spep::SessionGroupCacheImpl sessionGroupCache;
		saml2::IdentifierCache identifierCache;
		saml2::IdentifierGenerator identifierGenerator;
		spep::KeyResolver keyResolver;
		KeyResolverInitializer keyResolverInit;
		std::string schemaPath;
		spep::MetadataImpl metadata;
		spep::PolicyEnforcementProcessor policyEnforcementProcessor;
		
		PepTest_TestObjects(spep::Decision defaultPolicyDecision)
		:
		reportingProcessor(),
		wsClient( &reportingProcessor ),
		sessionGroupCache( &reportingProcessor, defaultPolicyDecision ),
		identifierCache(),
		identifierGenerator( &identifierCache ),
		keyResolver( TESTDATA_PATH, "myrsakey" ),
		keyResolverInit( keyResolver ),
		schemaPath( SCHEMA_PATH ),
		metadata( &reportingProcessor, schemaPath, L"spep", L"esoe", METADATA_URL, &keyResolver, 0, 0 ),
		policyEnforcementProcessor( &reportingProcessor, &wsClient, &sessionGroupCache, &metadata, &identifierGenerator, &keyResolver, schemaPath )
		{
		}
		
	};
	
	TEST( PepTest_testGenerateAuthzDecisionQuery )
	{
		try
		{
			std::auto_ptr<PepTest_TestObjects> testObjects( new PepTest_TestObjects( spep::Decision::DENY ) );
			spep::PolicyEnforcementProcessorData data;
			
			std::wstring esoeSessionID( L"_9898u19283u90812u09tuq0w9e09qi-oiasdfjaslkjflkwjeroijqwerq" );
			UnicodeString resource( UNICODE_STRING_SIMPLE( "/secure/admin.jsp" ) );
			
			data.setESOESessionID( esoeSessionID );
			data.setResource( resource );
			
			//testObjects->policyEnforcementProcessor.generateAuthzDecisionQuery( data );
			
			//uint16_t *buf = (uint16_t*)data.getRequestDocument();
			//for( std::size_t i=0; i<data.getRequestDocumentLength()/sizeof(uint16_t); ++i )
			//{ std::cout << (char)(buf[i] & 0xFF); }
			//std::cout << std::endl;
		}
		catch( std::exception &ex )
		{
			std::cout << ex.what() << std::endl;
		}
	}
	
}

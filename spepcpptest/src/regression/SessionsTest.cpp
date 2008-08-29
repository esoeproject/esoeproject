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
 * Creation Date: 07/02/2007
 * 
 * Purpose: 
 */
 
#include "regression/SessionsTest.h"

#include "sessions/SessionCache.h"
#include "sessions/impl/SessionCacheImpl.h"
#include "sessions/proxy/SessionCacheProxy.h"
#include "sessions/proxy/SessionCacheDispatcher.h"

#include "ipc/Platform.h"
#include "ipc/Socket.h"
#include "ipc/Exceptions.h"

#include <unicode/unistr.h>

#define RETURN_IF_FALSE(x) if(!(x)) return false
SUITE( SessionsTest )
{
	
	static int port = 48000;
	
	/*void* operator new (std::size_t size) throw (std::bad_alloc)
	{
		CPPUNIT_ASSERT( size > 0 );
	 void *p=malloc(size); 
	 if (p==0) // did malloc succeed?
	  throw std::bad_alloc(); // ANSI/ISO compliant behavior
	 return p;
	}
	
	void operator delete (void* ptr) throw()
	{
		CPPUNIT_ASSERT( ptr != NULL );
		free(ptr);
	}*/
	
	bool testSessionCacheBody( spep::SessionCache *sessionCache )
	{
		std::string sessionID( "_1234" );
		std::wstring esoeSessionID( L"_abcd" );
		std::wstring esoeSessionIndex( L"_9876" );
	
		{	
			spep::PrincipalSession principalSession;
			
			principalSession.setESOESessionID( esoeSessionID );
			principalSession.addESOESessionIndexAndLocalSessionID( esoeSessionIndex, sessionID );
			
			sessionCache->insertPrincipalSession( sessionID, principalSession );
		}
		
		{
			spep::PrincipalSession principalSession;
			sessionCache->getPrincipalSession( principalSession, sessionID );
			
			RETURN_IF_FALSE( principalSession.getESOESessionID() == esoeSessionID );
			RETURN_IF_FALSE( principalSession.getESOESessionIndexMap()[esoeSessionIndex] == sessionID );
		}
		
		{
			spep::PrincipalSession principalSession;
			sessionCache->getPrincipalSessionByEsoeSessionID( principalSession, esoeSessionID );
			
			RETURN_IF_FALSE( principalSession.getESOESessionID() == esoeSessionID );
			RETURN_IF_FALSE( principalSession.getESOESessionIndexMap()[esoeSessionIndex] == sessionID );
		}
		
		std::wstring requestID( L"abcd" );
		{
			spep::UnauthenticatedSession unauthenticatedSession;
			
			unauthenticatedSession.setAuthnRequestSAMLID( requestID );
			
			sessionCache->insertUnauthenticatedSession( unauthenticatedSession );
		}
		
		{
			spep::UnauthenticatedSession unauthenticatedSession;
			
			sessionCache->getUnauthenticatedSession( unauthenticatedSession, requestID );
			
			RETURN_IF_FALSE( unauthenticatedSession.getAuthnRequestSAMLID() == requestID );
		}
		
		sessionCache->terminateUnauthenticatedSession( requestID );
		
		{
			spep::UnauthenticatedSession unauthenticatedSession;
			
			unauthenticatedSession.setAuthnRequestSAMLID( L"" );
			
			bool caught = false;
			try
			{
				sessionCache->getUnauthenticatedSession( unauthenticatedSession, requestID );
			}
			catch (...)
			{
				caught = true;
			}
			
			RETURN_IF_FALSE( caught );
			RETURN_IF_FALSE( unauthenticatedSession.getAuthnRequestSAMLID() == L"" );
		}
		
		sessionCache->terminatePrincipalSession( sessionID );
	
		{
			spep::PrincipalSession principalSession;
			
			std::wstring emptyString(L"");
			principalSession.setESOESessionID( emptyString );
			
			bool caught = false;
			try
			{
				sessionCache->getPrincipalSession( principalSession, sessionID );
			}
			catch (...)
			{
				caught = true;
			}
			
			RETURN_IF_FALSE( caught );
			RETURN_IF_FALSE( principalSession.getESOESessionID() == L"" );
		}
		
		sessionCache->terminatePrincipalSession( sessionID );
	
		{
			spep::PrincipalSession principalSession;
			
			std::wstring emptyString(L"");
			principalSession.setESOESessionID( emptyString );
			
			bool caught = false;
			try
			{
				sessionCache->getPrincipalSessionByEsoeSessionID( principalSession, esoeSessionIndex );
			}
			catch (...)
			{
				caught = true;
			}
			
			RETURN_IF_FALSE( caught );
			RETURN_IF_FALSE( principalSession.getESOESessionID() == L"" );
		}
		
		return true;
	}
	TEST( SessionsTest_testSessionCacheImpl )
	{
		spep::SessionCache *sessionCache = new spep::SessionCacheImpl();
		
		CHECK( testSessionCacheBody( sessionCache ) );
	}
	
	TEST( SessionsTest_testSessionCacheProxy )
	{
		int testport = port++;
		spep::ipc::platform::socket_t serverSock = spep::ipc::platform::openSocket();
		spep::ipc::platform::bindLoopbackSocket( serverSock, testport );
		spep::ipc::platform::listenSocket( serverSock );
		
		pid_t pid = fork();
		
		if (pid > 0)
		{
			spep::SessionCache *sessionCache;
			try
			{
				spep::ipc::ClientSocket clientSocket( testport );
				spep::ipc::SessionCacheProxy sessionCacheProxy( &clientSocket );
				sessionCache = &sessionCacheProxy;
				
				CHECK( testSessionCacheBody( sessionCache) );
			}
			catch (spep::ipc::IPCException e)
			{
				CHECK(false);
				return;
			}
		}
		else
		{
			try
			{
				spep::ipc::platform::socket_t clientSock = spep::ipc::platform::acceptSocket( serverSock );
				spep::SessionCacheImpl sessionCache;
				spep::ipc::SessionCacheDispatcher dispatcher( &sessionCache );
				spep::ipc::ServerSocket<spep::ipc::SessionCacheDispatcher> ss( dispatcher, port++ );
				
				ss.run(clientSock);
			}
			catch (spep::ipc::IPCException e)
			{
				std::cerr << e.message() << std::endl;
			}
			catch (...)
			{
				std::cerr << "uncaught exception in child" << std::endl;
			}
			
			_exit(0);
		}
	}

}

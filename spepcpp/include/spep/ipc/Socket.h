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
 * Creation Date: 29/01/2007
 * 
 * Purpose: 
 */
 

#ifndef SOCKET_H_
#define SOCKET_H_

#include "saml2/identifier/IdentifierGenerator.h"

#include "spep/Util.h"
#include "spep/ipc/Platform.h"
#include "spep/ipc/Engine.h"
#include "spep/ipc/Dispatcher.h"

#include <vector>
#include <iostream>
#include <map>
#include <queue>

#include <boost/thread/thread.hpp>
#include <boost/thread/condition.hpp>

namespace spep
{
	namespace ipc
	{
		// To solve cyclic dependency
		class ClientSocketPool;
		
		/**
		 * IPC client socket. Connects to a loopback address, makes requests 
		 * and (optionally) awaits and returns a reply.
		 */
		class SPEPEXPORT ClientSocket
		{
			
			private:
			static platform::socket_t newSocket( int port );
			
			void reconnect( int retry );

			ClientSocketPool* _pool;
			platform::socket_t _socket;
			Engine *_engine;
			int _port;
			Mutex _mutex;
			
			public:
			ClientSocket( ClientSocketPool* pool, int port );
			int getSocketID();
			
			/**
			 * Makes a request and awaits a reply
			 * @param dispatch The string describing where to dispatch the request
			 * @param req The request object
			 * @return The response object
			 */
			template <class Res, class Req>
			Res makeRequest( std::string &dispatch, Req &req )
			{
				ScopedLock lock( _mutex );
				
				int retry = 0;
				
				for(;;)
				{
					try
					{
						if( _engine != NULL )
						{
							return _engine->makeRequest<Res,Req>( dispatch, req );
						}
					}
					catch( SocketException e )
					{
						if (platform::validSocket(_socket)) platform::closeSocket(_socket);
						_socket = SocketWrapper();

						delete _engine;
						_engine = NULL;
					}

					// Connection died. Get a new one.
					reconnect( retry++ );
				}
			}
			
			/**
			 * Makes a request and returns immediately, expecting no reply.
			 * @param dispatch The string describing where to dispatch the request
			 * @param req The request object
			 */
			template <class Req>
			void makeNonBlockingRequest( std::string &dispatch, Req &req )
			{
				ScopedLock lock( _mutex );
				
				for(;;)
				{
					int retry = 0;
					
					try
					{
						if( _engine != NULL )
						{
							_engine->makeNonBlockingRequest( dispatch, req );
							return;
						}
					}
					catch( SocketException e )
					{
						if (platform::validSocket(_socket)) platform::closeSocket(_socket);
						_socket = SocketWrapper();

						delete _engine;
						_engine = NULL;
					}

					// Connection died. Get a new one.
					reconnect( retry++ );
				}
			}
			
		};
		
		class ClientSocketPool
		{
			private:
			std::queue<ClientSocket*> _free;
			boost::condition _condition;
			Mutex _mutex;
			std::string _serviceID;
			
			public:
			ClientSocketPool( int port, std::size_t n );
			ClientSocket* get();
			void release( ClientSocket* socket );
			const std::string& getServiceID();
			void setServiceID( const std::string& serviceID );
		};
		
		class ClientSocketLease
		{
			private:
			ClientSocketPool *_pool;
			ClientSocket *_socket;
			
			public:
			ClientSocketLease( ClientSocketPool* pool );
			~ClientSocketLease();
			ClientSocket* operator->();
			ClientSocket* operator*();
		};
		
		/**
		 * IPC server socket. Listens on a loopback address, receives and dispatches
		 * and sends replies where expected.
		 */
		template <class Dispatcher>
		class ServerSocket
		{
			
			private:
			/**
			 * Creates a new socket and binds it to the given port.
			 * No listen call is made.
			 */
			static platform::socket_t newSocket(int port)
			{
				platform::socket_t socket = platform::openSocket();
				if (! platform::validSocket(socket)) throw SocketException( "The new server socket is invalid." );
				
				try
				{
					platform::bindLoopbackSocket( socket, port );
					platform::setReadTimeout( socket, 500 );
				}
				catch (SocketException e)
				{
					platform::closeSocket( socket );
					throw;
				}
				
				return socket;
			}
			
			/**
			 * Class for internal use to handle a specific incoming connection.
			 */
			class ServerSocketThread
			{
				ServerSocket<Dispatcher> &_serverSocket;
				platform::socket_t _socket;
				
				public:
				ServerSocketThread(ServerSocket<Dispatcher> &serverSocket, platform::socket_t socket)
				: _serverSocket( serverSocket ), _socket(socket)
				{}
				
				// We can copy this as many times as we like.. as long as operator() is only invoked once.
				void operator()()
				{
					try {
						_serverSocket.run(_socket);
					} catch (...) {
						platform::closeSocket( _socket );
					}
				}
			};
			
			Dispatcher& _dispatcher;
			platform::socket_t _socket;
			std::string _id;
				
			public:
			
			/**
			 * Constructor.
			 * @param dispatcher The dispatcher to use when a request is received.
			 * @param port The port on which to listen for incoming connections.
			 */
			ServerSocket(Dispatcher &dispatcher, int port)
			: _dispatcher( dispatcher ),
			_socket ( newSocket( port ) )
			{
				saml2::IdentifierCache identifierCache;
				saml2::IdentifierGenerator identifierGenerator( &identifierCache );
				
				_id = identifierGenerator.generateSessionID();
			}
			
			/**
			 * Body of listen method. Blocks indefinitely.
			 */
			void listen( bool *running )
			{
				
				try
				{
					platform::listenSocket( _socket );
				}
				catch (SocketException e)
				{
					platform::closeSocket(_socket);
					throw;
				}
				
				while( *running )
				{
					try
					{
						// accept a connection..
						platform::socket_t clientSocket = platform::acceptSocket( _socket );
						
						if( platform::validSocket( clientSocket ) )
						{
							// .. and fire off a thread for it
							ServerSocketThread threadFunction( *this, clientSocket );
							
							/* What's happening here?
							 * Well according to the boost thread api docs, the constructor
							 * for the thread object fires off the thread in the background
							 * and the destructor detaches the thread so that it cleans
							 * itself up with it's done. That's the exact behaviour we want.
							 */
							delete ( new boost::thread( threadFunction ) );
						}
					}
					catch (SocketException e)
					{
						// ignore
					}
				}
			}
			
			/**
			 * Connection processing method. Not intended to be invoked directly.
			 * @param socket The connected socket to use for 
			 */
			void run(platform::socket_t socket)
			{
				Engine engine( socket );
				engine.sendObject( _id );
				for(;;)
				{
					try
					{
						MessageHeader messageHeader = engine.getRequestHeader();
						if ( !_dispatcher.dispatch( messageHeader, engine ) )
						{
							engine.sendErrorResponseHeader();
							InvocationTargetException exception( "No dispatcher was available to handle the requested call." );
							engine.sendObject(exception);
						}
					}
					catch (SocketException s)
					{
						break;
					}
					// If another error occurs, we can trap it and terminate the connection.
					// It's not ideal, but at least it stops the daemon falling over.
					catch (std::exception e)
					{
						break;
					}
				}
			}
			
		};
		
	}
}

#endif /*SOCKET_H_*/

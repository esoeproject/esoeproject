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
#include "spep/ipc/Engine.h"
#include "spep/ipc/Dispatcher.h"
#include "spep/ipc/Exceptions.h"

#include <vector>
#include <iostream>
#include <map>
#include <queue>

#include <asio.hpp>

#include <boost/bind.hpp>
#include <boost/function.hpp>
#include <boost/thread/thread.hpp>
#include <boost/thread/condition.hpp>

namespace spep
{
	namespace ipc
	{
		using asio::ip::tcp;
		using namespace boost;

		// To solve cyclic dependency
		class ClientSocketPool;

		void SPEPEXPORT writeSocket(tcp::socket* socket, const std::vector<char>& buffer);
		void SPEPEXPORT readSocket(tcp::socket* socket, std::vector<char>& buffer);

		/**
		 * IPC client socket. Connects to a loopback address, makes requests
		 * and (optionally) awaits and returns a reply.
		 */
		class SPEPEXPORT ClientSocket
		{

			private:
			tcp::socket* newSocket();

			void reconnect( int retry );

			ClientSocketPool* _pool;
			tcp::socket* _socket;
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
						asio::error_code error;
						_socket->shutdown(tcp::socket::shutdown_both, error);
						_socket->close(error);

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
						asio::error_code error;
						_socket->shutdown(tcp::socket::shutdown_both, error);
						_socket->close(error);

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
			asio::io_service _ioService;

			public:
			ClientSocketPool( int port, std::size_t n );
			ClientSocket* get();
			void release( ClientSocket* socket );
			const std::string& getServiceID();
			void setServiceID( const std::string& serviceID );
			asio::io_service& getIoService();
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
			saml2::LocalLogger log;
			Dispatcher* dispatcher;
			asio::io_service ioService;
			asio::ip::tcp::acceptor acceptor;
			std::string id;

			public:

			/**
			 * Constructor.
			 * @param dispatcher The dispatcher to use when a request is received.
			 * @param port The port on which to listen for incoming connections.
			 */
			ServerSocket(saml2::Logger *logger, Dispatcher *disp, int port)
			:
			log(logger, "spep::ipc::ServerSocket"),
			dispatcher(disp),
			ioService(),
			acceptor(ioService)
			{
				saml2::IdentifierCache identifierCache;
				saml2::IdentifierGenerator identifierGenerator( &identifierCache );

				id = identifierGenerator.generateSessionID();

				tcp::endpoint endpoint(asio::ip::address_v4::loopback(), port);
				asio::error_code error;
				// The truth value of the return indicates an error, so this piece of code doesn't read particularly nicely, but works.
				if (acceptor.open(endpoint.protocol(), error) ||
					acceptor.set_option(tcp::acceptor::reuse_address(true), error) ||
					acceptor.bind(endpoint, error) ||
					acceptor.listen(asio::socket_base::max_connections, error)) {

					log.error() << "An error occurred while opening the server socket: " << error.message();
				}
			}

			/**
			 * Body of listen method. Blocks indefinitely.
			 */
			void listen(bool *running)
			{
				while(*running) {
					try {
						tcp::socket* socket = new tcp::socket(ioService);
						acceptor.accept(*socket);

						function<void()> threadFunction( bind(&ServerSocket::run, this, socket) );

						/* What's happening here?
						 * According to the boost thread api docs, the constructor for
						 * the thread object fires off the thread in the background and
						 * the destructor detaches the thread so that it cleans itself
						 * up with it's done. That's the exact behaviour we want.
						 */
						delete ( new thread( threadFunction ) );
					} catch (std::exception &e) {
					}
				}
			}

			/**
			 * Connection processing method. Not intended to be invoked directly.
			 * @param socket The connected socket to use for
			 */
			void run(tcp::socket* socket_)
			{
				std::auto_ptr<tcp::socket> socket(socket_);

				Engine engine( bind(spep::ipc::writeSocket, socket_, _1), bind(spep::ipc::readSocket, socket_, _1) );
				engine.sendObject( id );
				for(;;)
				{
					try
					{
						MessageHeader messageHeader = engine.getRequestHeader();
						if ( !dispatcher->dispatch( messageHeader, engine ) )
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

				asio::error_code error;
				if (socket->shutdown(tcp::socket::shutdown_both, error)
					|| socket->close(error)) {

					log.error() << "Error closing socket: " << error.message();
				}
			}

		};

	}
}

#endif /*SOCKET_H_*/

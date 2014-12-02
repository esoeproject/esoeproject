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

#include <vector>
#include <iostream>
#include <map>
#include <queue>

#include <boost/asio.hpp>
#include <boost/bind.hpp>
#include <boost/function.hpp>
#include <boost/thread/thread.hpp>
#include <boost/thread/condition.hpp>

#include "saml2/identifier/IdentifierGenerator.h"

#include "spep/Util.h"
#include "spep/ipc/Engine.h"
#include "spep/ipc/Dispatcher.h"
#include "spep/ipc/Exceptions.h"


namespace spep
{
	namespace ipc
	{
		// To solve cyclic dependency
		class ClientSocketPool;

		void SPEPEXPORT writeSocket(boost::asio::ip::tcp::socket* socket, const std::vector<char>& buffer);
        void SPEPEXPORT readSocket(boost::asio::ip::tcp::socket* socket, std::vector<char>& buffer);

		/**
		 * IPC client socket. Connects to a loopback address, makes requests
		 * and (optionally) awaits and returns a reply.
		 */
        class SPEPEXPORT ClientSocket
        {

        public:
            ClientSocket(ClientSocketPool* pool, int port);

            /**
             * Makes a request and awaits a reply
             * @param dispatch The string describing where to dispatch the request
             * @param req The request object
             * @return The response object
             */
            template <class Res, class Req>
            Res makeRequest(const std::string &dispatch, Req &req)
            {
                ScopedLock lock(mMutex);

                int retry = 0;

                for (;;)
                {
                    try
                    {
                        if (mEngine != NULL)
                        {
                            return mEngine->makeRequest<Res, Req>(dispatch, req);
                        }
                    }
                    catch (SocketException& e)
                    {
                        boost::system::error_code error;
                        mSocket->shutdown(boost::asio::ip::tcp::socket::shutdown_both, error);
                        mSocket->close(error);

                        delete mEngine;
                        mEngine = NULL;
                    }

                    // Connection died. Get a new one.
                    reconnect(retry++);
                }
            }

            /**
             * Makes a request and returns immediately, expecting no reply.
             * @param dispatch The string describing where to dispatch the request
             * @param req The request object
             */
            template <class Req>
            void makeNonBlockingRequest(const std::string &dispatch, Req &req)
            {
                ScopedLock lock(mMutex);

                for (;;)
                {
                    int retry = 0;

                    try
                    {
                        if (mEngine != NULL)
                        {
                            mEngine->makeNonBlockingRequest(dispatch, req);
                            return;
                        }
                    }
                    catch (SocketException& e)
                    {
                        boost::system::error_code error;
                        mSocket->shutdown(boost::asio::ip::tcp::socket::shutdown_both, error);
                        mSocket->close(error);

                        delete mEngine;
                        mEngine = NULL;
                    }

                    // Connection died. Get a new one.
                    reconnect(retry++);
                }
            }

        private:

            boost::asio::ip::tcp::socket* newSocket();
            void reconnect(int retry);

            ClientSocketPool* mPool;
            boost::asio::ip::tcp::socket* mSocket;
            Engine *mEngine;
            int mPort;
            mutable Mutex mMutex;
        };

        //!< 
        typedef std::shared_ptr<ClientSocket> ClientSocketPtr;


        class ClientSocketPool
        {
        public:

            ClientSocketPool(int port, std::size_t n);

            ClientSocketPtr get();
            void release(ClientSocketPtr socket);
            std::string getServiceID() const;
            void setServiceID(const std::string& serviceID);
            boost::asio::io_service& getIoService();

        private:

            std::queue<ClientSocketPtr> mFree;
            boost::condition mCondition;
            mutable Mutex mMutex;
            std::string mServiceID;
            boost::asio::io_service mIOService;
        };

        class ClientSocketLease
        {
        public:

            ClientSocketLease(ClientSocketPool* pool);
            ~ClientSocketLease();

            ClientSocketPtr operator->();
            ClientSocketPtr operator*();

        private:

            ClientSocketPool *mPool;
            ClientSocketPtr mSocket;
        };

        /**
         * IPC server socket. Listens on a loopback address, receives and dispatches
         * and sends replies where expected.
         */
        template <class Dispatcher>
        class ServerSocket
        {
        public:

            /**
             * Constructor.
             * @param dispatcher The dispatcher to use when a request is received.
             * @param port The port on which to listen for incoming connections.
             */
            ServerSocket(saml2::Logger *logger, Dispatcher *disp, int port) :
                mLogger(logger, "spep::ipc::ServerSocket"),
                mDispatcher(disp),
                mIOService(),
                mAcceptor(mIOService)
            {
                saml2::IdentifierCache identifierCache;
                saml2::IdentifierGenerator identifierGenerator(&identifierCache);

                mID = identifierGenerator.generateSessionID();

                boost::asio::ip::tcp::endpoint endpoint(boost::asio::ip::address_v4::loopback(), port);
                boost::system::error_code error;

                // The truth value of the return indicates an error, so this piece of code doesn't read particularly nicely, but works.
                if (mAcceptor.open(endpoint.protocol(), error) ||
                    mAcceptor.set_option(boost::asio::ip::tcp::acceptor::reuse_address(true), error) ||
                    mAcceptor.bind(endpoint, error) ||
                    mAcceptor.listen(boost::asio::socket_base::max_connections, error)) {

                    mLogger.error() << "An error occurred while opening the server socket: " << error.message();
                }
            }

            /**
             * Body of listen method. Blocks indefinitely.
             */
            void listen(bool *running)
            {
                while (*running) {
                    try {
                        boost::asio::ip::tcp::socket* socket = new boost::asio::ip::tcp::socket(mIOService);
                        mAcceptor.accept(*socket);

                        boost::function<void()> threadFunction(boost::bind(&ServerSocket::run, this, socket));

                        /* What's happening here?
                         * According to the boost thread api docs, the constructor for
                         * the thread object fires off the thread in the background and
                         * the destructor detaches the thread so that it cleans itself
                         * up when it's done. That's the exact behaviour we want.
                         */
                        delete (new boost::thread(threadFunction));
                    }
                    catch (std::exception &e) {
                    }
                }
            }

            /**
             * Connection processing method. Not intended to be invoked directly.
             * @param socket The connected socket to use for
             */
            void run(boost::asio::ip::tcp::socket* socket_)
            {
                std::auto_ptr<boost::asio::ip::tcp::socket> socket(socket_);

                Engine engine(boost::bind(spep::ipc::writeSocket, socket_, _1), boost::bind(spep::ipc::readSocket, socket_, _1));
                engine.sendObject(mID);

                for (;;)
                {
                    try
                    {
                        MessageHeader messageHeader = engine.getRequestHeader();
                        if (!mDispatcher->dispatch(messageHeader, engine))
                        {
                            engine.sendErrorResponseHeader();
                            InvocationTargetException exception("No dispatcher was available to handle the requested call.");
                            engine.sendObject(exception);
                        }
                    }
                    catch (SocketException& s)
                    {
                        break;
                    }
                    // If another error occurs, we can trap it and terminate the connection.
                    // It's not ideal, but at least it stops the daemon falling over.
                    catch (std::exception& e)
                    {
                        break;
                    }
                }

                boost::system::error_code error;
                if (socket->shutdown(boost::asio::ip::tcp::socket::shutdown_both, error)
                    || socket->close(error)) {

                    mLogger.error() << "Error closing socket: " << error.message();
                }
            }

        private:

            saml2::LocalLogger mLogger;
            Dispatcher* mDispatcher;
            boost::asio::io_service mIOService;
            boost::asio::ip::tcp::acceptor mAcceptor;
            std::string mID;
        };

    }
}

#endif /*SOCKET_H_*/

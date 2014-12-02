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
 * Creation Date: 30/01/2007
 * 
 * Purpose: 
 */

#include "spep/ipc/Socket.h"
#include <iostream>

#include <boost/thread.hpp>
#include <boost/bind.hpp>

#include <boost/asio.hpp>


void spep::ipc::writeSocket(boost::asio::ip::tcp::socket* socket, const std::vector<char>& buffer) {
	boost::system::error_code error;

	std::size_t len = buffer.size();
	boost::asio::write(*socket, boost::asio::buffer(&len, sizeof(len)), boost::asio::transfer_all(), error);
	if (!error) {
		boost::asio::write(*socket, boost::asio::buffer(&buffer.front(), len * sizeof(char)), boost::asio::transfer_all(), error);
	}

	if (error) {
		throw SocketException(error.message());
	}
}
void spep::ipc::readSocket(boost::asio::ip::tcp::socket* socket, std::vector<char>& buffer) {
    boost::system::error_code error;

	std::size_t len;
	boost::asio::read(*socket, boost::asio::buffer(&len, sizeof(len)), boost::asio::transfer_all(), error);

	if (!error) {
		buffer.resize(len);
		boost::asio::read(*socket, boost::asio::buffer(&buffer.front(), len * sizeof(char)), boost::asio::transfer_all(), error);
	}

	if (error) {
		throw SocketException(error.message());
	}
}

boost::asio::ip::tcp::socket* spep::ipc::ClientSocket::newSocket()
{
    // FIXME: potential leak when socket exception thrown?
    boost::asio::ip::tcp::socket* socket = new boost::asio::ip::tcp::socket(mPool->getIoService());
    boost::system::error_code error;

    socket->connect(boost::asio::ip::tcp::endpoint(boost::asio::ip::address_v4::loopback(), mPort), error);
	if (error) {
		throw SocketException(error.message());
	}
	return socket;
}

spep::ipc::ClientSocket::ClientSocket(spep::ipc::ClientSocketPool* pool, int port) :
    mPool(pool),
    mSocket(nullptr),
    mEngine(nullptr),
    mPort(port)
{
}

void spep::ipc::ClientSocket::reconnect(int retry)
{
	if (retry > 0) {
		throw SocketException("Retry limit (0) exceeded.");
	}
	
	if (mEngine != nullptr)
	{
		// Unset it so we don't keep hammering a broken connection. The OS might not like that so much.
		delete mEngine;
		mEngine = nullptr;
	}
	
	{
		ScopedLock lock(mMutex);
		
		try
		{
			mSocket = newSocket();
			mEngine = new Engine(boost::bind(&spep::ipc::writeSocket, mSocket, _1), boost::bind(&spep::ipc::readSocket, mSocket, _1));
			
			std::string serviceID;
			mEngine->getObject(serviceID);
			mPool->setServiceID(serviceID);
		}
		catch (SocketException& e)
		{
			// Failed. We'll throw when we retry though.
			delete mEngine;
			mEngine = nullptr;
		}
	}
}

spep::ipc::ClientSocketPool::ClientSocketPool(int port, std::size_t n)
{
	for (std::size_t i = 0; i < n; ++i)
	{
		mFree.push(ClientSocketPtr(new ClientSocket(this, port )));
	}
}

spep::ipc::ClientSocketPtr spep::ipc::ClientSocketPool::get()
{
	ScopedLock lock(mMutex);
	if (mFree.empty())
	{
		mCondition.wait( lock );
	}
	
	ClientSocketPtr sock = mFree.front();
	mFree.pop();
	return sock;
}

void spep::ipc::ClientSocketPool::release(spep::ipc::ClientSocketPtr socket)
{
	ScopedLock lock(mMutex);
	mFree.push(socket);
	mCondition.notify_one();
}

std::string spep::ipc::ClientSocketPool::getServiceID() const
{
	ScopedLock lock(mMutex);
	
	return this->mServiceID;
}

void spep::ipc::ClientSocketPool::setServiceID(const std::string& serviceID)
{
	ScopedLock lock(mMutex);
	
	this->mServiceID = serviceID;
}

boost::asio::io_service& spep::ipc::ClientSocketPool::getIoService()
{
	return this->mIOService;
}

spep::ipc::ClientSocketLease::ClientSocketLease(spep::ipc::ClientSocketPool* pool) :
    mPool(pool),
    mSocket(pool->get())
{
}

spep::ipc::ClientSocketLease::~ClientSocketLease()
{
	mPool->release(mSocket);
}

spep::ipc::ClientSocketPtr spep::ipc::ClientSocketLease::operator->()
{
	return mSocket;
}

spep::ipc::ClientSocketPtr spep::ipc::ClientSocketLease::operator*()
{
	return mSocket;
}

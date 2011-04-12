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

#include <asio.hpp>

using asio::ip::tcp;

void spep::ipc::writeSocket(tcp::socket* socket, const std::vector<char>& buffer) {
	asio::error_code error;

	std::size_t len = buffer.size();
	asio::write(*socket, asio::buffer(&len, sizeof(len)), asio::transfer_all(), error);
	if (!error) {
		asio::write(*socket, asio::buffer(&buffer.front(), len*sizeof(char)), asio::transfer_all(), error);
	}

	if (error) {
		throw SocketException(error.message());
	}
}
void spep::ipc::readSocket(tcp::socket* socket, std::vector<char>& buffer) {
	asio::error_code error;

	std::size_t len;
	asio::read(*socket, asio::buffer(&len, sizeof(len)), asio::transfer_all(), error);

	if (!error) {
		buffer.resize(len);
		asio::read(*socket, asio::buffer(&buffer.front(), len*sizeof(char)), asio::transfer_all(), error);
	}

	if (error) {
		throw SocketException(error.message());
	}
}

tcp::socket* spep::ipc::ClientSocket::newSocket()
{
	tcp::socket* socket = new tcp::socket(_pool->getIoService());
	asio::error_code error;

	socket->connect(tcp::endpoint(asio::ip::address_v4::loopback(), _port), error);
	if (error) {
		throw SocketException(error.message());
	}
	return socket;
}

spep::ipc::ClientSocket::ClientSocket( spep::ipc::ClientSocketPool* pool, int port )
:
_pool( pool ),
_socket( NULL ),
_engine( NULL ),
_port( port )
{
}

void spep::ipc::ClientSocket::reconnect( int retry )
{
	if (retry > 0) {
		throw SocketException("Retry limit (0) exceeded.");
	}
	
	if( _engine != NULL )
	{
		// Unset it so we don't keep hammering a broken connection. The OS might not like that so much.
		delete _engine;
		_engine = NULL;
	}
	
	{
		ScopedLock lock( _mutex );
		
		try
		{
			_socket = this->newSocket();
			_engine = new Engine( boost::bind(&spep::ipc::writeSocket, _socket, _1), boost::bind(&spep::ipc::readSocket, _socket, _1) );
			
			std::string serviceID;
			_engine->getObject( serviceID );
			_pool->setServiceID( serviceID );
		}
		catch( SocketException e )
		{
			// Failed. We'll throw when we retry though.
			if( _engine != NULL ) delete _engine;
			_engine = NULL;
		}
	}
}

spep::ipc::ClientSocketPool::ClientSocketPool( int port, std::size_t n )
{
	for (std::size_t i = 0; i < n; ++i)
	{
		_free.push(ClientSocketPtr(new ClientSocket(this, port )));
	}
}

spep::ipc::ClientSocketPtr spep::ipc::ClientSocketPool::get()
{
	ScopedLock lock(_mutex);
	if (_free.empty())
	{
		_condition.wait( lock );
	}
	
	ClientSocketPtr sock = _free.front();
	_free.pop();
	return sock;
}

void spep::ipc::ClientSocketPool::release( spep::ipc::ClientSocketPtr socket )
{
	ScopedLock lock(_mutex);
	_free.push( socket );
	_condition.notify_one();
}

const std::string& spep::ipc::ClientSocketPool::getServiceID()
{
	ScopedLock lock( _mutex );
	
	return this->_serviceID;
}

void spep::ipc::ClientSocketPool::setServiceID( const std::string& serviceID )
{
	ScopedLock lock( _mutex );
	
	this->_serviceID = serviceID;
}

asio::io_service& spep::ipc::ClientSocketPool::getIoService() {
	return this->_ioService;
}

spep::ipc::ClientSocketLease::ClientSocketLease( spep::ipc::ClientSocketPool* pool )
:
_pool( pool ),
_socket( pool->get() )
{
}

spep::ipc::ClientSocketLease::~ClientSocketLease()
{
	_pool->release( _socket );
}

spep::ipc::ClientSocketPtr spep::ipc::ClientSocketLease::operator->()
{
	return _socket;
}

spep::ipc::ClientSocketPtr spep::ipc::ClientSocketLease::operator*()
{
	return _socket;
}

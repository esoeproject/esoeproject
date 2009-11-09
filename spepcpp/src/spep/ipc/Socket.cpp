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

using asio::ip::tcp;

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
			_engine = new Engine( _socket );
			
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
	for( std::size_t i=0; i<n; ++i )
	{
		_free.push( new ClientSocket( this, port ) );
	}
}

spep::ipc::ClientSocket* spep::ipc::ClientSocketPool::get()
{
	ScopedLock lock( _mutex );
	if( _free.empty() )
	{
		_condition.wait( lock );
	}
	
	ClientSocket* sock = _free.front();
	_free.pop();
	return sock;
}

void spep::ipc::ClientSocketPool::release( spep::ipc::ClientSocket* socket )
{
	ScopedLock lock( _mutex );
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

spep::ipc::ClientSocket* spep::ipc::ClientSocketLease::operator->()
{
	return _socket;
}

spep::ipc::ClientSocket* spep::ipc::ClientSocketLease::operator*()
{
	return _socket;
}

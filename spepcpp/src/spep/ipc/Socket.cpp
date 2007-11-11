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

spep::ipc::platform::socket_t spep::ipc::ClientSocket::newSocket( int port )
{
	platform::socket_t socket = platform::openSocket();
	platform::connectLoopbackSocket( socket, port );
	return socket;
}

spep::ipc::ClientSocket::ClientSocket( int port )
:
_socket( ),//spep::ipc::ClientSocket::ClientSocket::newSocket(port) ),
_engine( NULL ),
_port( port ),
_connectionSequenceID( 0 )
{
	//_engine = new Engine( _socket );
}

void spep::ipc::ClientSocket::reconnect( int retry )
{
	int delay = ( (retry>2) ? 3 : retry );
	
	boost::xtime retryAt;
	// TODO Maybe I should care if this fails [i.e. if (boost::xtime_get(..) == 0)]
	boost::xtime_get( &retryAt, boost::TIME_UTC );
	retryAt.sec += delay;
	
	if( _engine != NULL )
	{
		// Unset it so we don't keep hammering a broken connection. The OS might not like that so much.
		delete _engine;
		_engine = NULL;
	}
	
	boost::thread::sleep( retryAt );
	
	try
	{
		_socket = this->newSocket(_port);
		_engine = new Engine( _socket );
		_connectionSequenceID++;
	}
	catch( SocketException e )
	{
	}
}

long spep::ipc::ClientSocket::getConnectionSequenceID()
{
	ScopedLock lock( _mutex );
	
	return this->_connectionSequenceID;
}

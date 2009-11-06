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
 * Creation Date: 22/10/2007
 * 
 * Purpose: 
 */

#include "spep/ipc/Platform.h"


protoent* spep::ipc::platform::tcpProtocol = NULL;

#ifdef WIN32
WSADATA spep::ipc::platform::wsaData = WSADATA();
#endif /*WIN32*/

spep::ipc::SocketWrapper::SocketWrapper(NATIVE_SOCKET_TYPE s)
:
socket(s)
{
}

spep::ipc::SocketWrapper::SocketWrapper()
:
socket(INVALID_SOCKET)
{
}

spep::ipc::SocketWrapper::SocketWrapper(const SocketWrapper &rhs)
:
socket(rhs.socket)
{
}

spep::ipc::SocketWrapper &spep::ipc::SocketWrapper::operator=(const spep::ipc::SocketWrapper &rhs)
{
	socket = rhs.socket;
	return *this;
}

bool spep::ipc::SocketWrapper::operator==(const spep::ipc::SocketWrapper &rhs) const
{
	return socket == rhs.socket;
}

bool spep::ipc::SocketWrapper::operator!=(const spep::ipc::SocketWrapper &rhs) const
{
	return socket == rhs.socket;
}

spep::ipc::platform::socket_t spep::ipc::platform::openSocket()
{
	if( platform::tcpProtocol == NULL )
	{
#ifdef WIN32
		WORD winsockVersion = WINSOCK_VERSION;
		// This is the first time we are invoked. Start winsock before making any calls.
		if( WSAStartup( winsockVersion, &(platform::wsaData) ) != 0 )
		{
			throw SocketException( "Unable to start Winsock." );
		}
		platform::tcpProtocol = getprotobyname( SOCKET_PROTO );
#else /*WIN32*/
		setprotoent(0);
		platform::tcpProtocol = getprotobyname( SOCKET_PROTO );
		endprotoent();
#endif /*WIN32*/
	}
	
	// Open the socket.
	socket_t retval( socket( SOCKET_DOMAIN, SOCKET_TYPE, platform::tcpProtocol->p_proto ) );
	
	// Make sure the socket is valid.
	if ( !validSocket( retval ) )
	{
		throw SocketException( strerror(errno) );
	}
	
	// Set the socket to allow address reuse.
	int value = 1;
	setsockopt( SOCKET(retval), SOL_SOCKET, SO_REUSEADDR, (SOCKOPT_TYPE*)&value, sizeof(value) );
	
	return retval;
}

void spep::ipc::platform::connectSocket( spep::ipc::platform::socket_t sock, const char *addr, int port )
{
	sockaddr_in sa;
	sa.sin_family = AF_INET;
	sa.sin_port = htons( port );
	
	hostent *host = gethostbyname(addr);
	if (!host)
	{
		/* TODO Log */
		throw SocketException( "Unable to resolve host" );
	}
	
	char *hostaddr = host->h_addr_list[0];
	memcpy( &sa.sin_addr.s_addr, hostaddr, strlen(hostaddr) );
	
	if ( connect( SOCKET(sock), (sockaddr*)&sa, sizeof(sa) ) == 0 ) return;
	
	throw SocketException( strerror(errno) );
}

void spep::ipc::platform::connectLoopbackSocket( spep::ipc::platform::socket_t sock, int port )
{
	sockaddr_in sa;
	sa.sin_family = AF_INET;
	sa.sin_port = htons( port );
	sa.sin_addr.s_addr = htonl(INADDR_LOOPBACK);
	
	if ( connect( SOCKET(sock), (sockaddr*)&sa, sizeof(sa) ) == 0 ) return;
	
	throw SocketException( strerror(errno) );
}

void spep::ipc::platform::bindSocket( spep::ipc::platform::socket_t sock, const char *addr, int port )
{
	sockaddr_in sa;
	sa.sin_family = AF_INET;
	sa.sin_port = htons( port );
	
	hostent *host = gethostbyname(addr);
	if (!host)
	{
		/* TODO Log */
		throw SocketException( "Unable to resolve host" );
	}
	
	char *hostaddr = host->h_addr_list[0];
	sa.sin_addr.s_addr = *((in_addr_t*) hostaddr);
	
	if ( bind( SOCKET(sock), (sockaddr*)&sa, sizeof(sa) ) == 0 ) return;
	
	throw SocketException( strerror(errno) );
}

void spep::ipc::platform::bindLoopbackSocket( spep::ipc::platform::socket_t sock, int port )
{
	sockaddr_in sa;
	memset( &sa, 0, sizeof(sa) );
	sa.sin_family = AF_INET;
	sa.sin_port = htons( port );
	sa.sin_addr.s_addr = htonl(INADDR_LOOPBACK);
	
	int result = bind( SOCKET(sock), (sockaddr*)&sa, sizeof(sa) );
	if ( result == 0 ) return;
	
	throw SocketException( strerror(errno) );
}

void spep::ipc::platform::listenSocket( spep::ipc::platform::socket_t sock )
{
	if ( listen( SOCKET(sock), SOCKET_BACKLOG ) == 0 ) return;
	throw SocketException( strerror(errno) );
}

spep::ipc::platform::socket_t spep::ipc::platform::acceptSocket( spep::ipc::platform::socket_t sock )
{
	sockaddr_in sa;
	socklen_t len = sizeof(sockaddr_in);
	socket_t retval = INVALID_SOCKET;

	retval = accept( SOCKET(sock), (sockaddr*)&sa, &len );
	
	if ( validSocket( retval ) ) return retval;
	
	if( errno != EAGAIN && errno != EWOULDBLOCK ) throw SocketException( strerror(errno) );

	return INVALID_SOCKET;
}

void spep::ipc::platform::setReadTimeout( spep::ipc::platform::socket_t sock, int waitMillis )
{
	struct timeval value;
	value.tv_sec = waitMillis / 1000;
	value.tv_usec = (waitMillis % 1000) * 1000;

	setsockopt( SOCKET(sock), SOL_SOCKET, SO_RCVTIMEO, (SOCKOPT_TYPE*)&value, sizeof(value) );
}

ssize_t spep::ipc::platform::readSocket( spep::ipc::platform::socket_t sock, char *buf, int buflen, int flags )
{
#ifdef WIN32
	int result;
#else //WIN32
	ssize_t result;
#endif //WIN32

	result = recv( SOCKET(sock), buf, buflen, flags );
	if (result < 0)
	{
		throw SocketException( strerror(errno) );
	}
	
	return result;
}

ssize_t spep::ipc::platform::writeSocket( spep::ipc::platform::socket_t sock, const char *buf, int buflen, int flags )
{
#ifdef WIN32
	int result;
#else //WIN32
	ssize_t result;
#endif //WIN32

	result = send( SOCKET(sock), buf, buflen, flags );
	if (result < 0)
	{
		throw SocketException( strerror(errno) );
	}
	
	return result;
}
		
void spep::ipc::platform::closeSocket( spep::ipc::platform::socket_t sock )
{
#ifdef WIN32
	if ( closesocket( SOCKET(sock) ) == 0 ) return;
#else
	if ( close( SOCKET(sock) ) == 0 ) return;
#endif
	
	// We don't care about data loss since we're terminating a bad connection. No need to throw.
	//throw SocketException( strerror(errno) );
}
		
bool spep::ipc::platform::validSocket( spep::ipc::platform::socket_t sock )
{
	return SOCKET(sock) != INVALID_SOCKET;
}

#define TEXTENCODING_HEXCHARS "0123456789abcdef"
#define TEXTENCODING_HEX_CHAR_TO_DECIMAL_DIGIT(x) \
	(  	isdigit(x)	?  (  x - '0' )  :\
	( isalpha(x) && (tolower(x) <= 'f') )	?  ( tolower(x) - 'a' + 10 )  :  0  )
	
std::size_t spep::ipc::platform::textEncoding::encodedSize( std::size_t len )
{
	return 2*len + 1;
}
			
std::size_t spep::ipc::platform::textEncoding::decodedSize( std::size_t len )
{
	return (len - 1)/2;
}
			
void spep::ipc::platform::textEncoding::encode( char *dst, const char *buf, const std::size_t dstlen )
{
	static const char *hexchars = TEXTENCODING_HEXCHARS;
	dst[0] = '_';

	// double var 
	for (std::size_t bufpos = 0, dstpos = 1; dstpos < (dstlen - 1); )
	{
		unsigned char c = (unsigned char)buf[bufpos++];
		unsigned char lo = c & 0x0F;
		unsigned char hi = (c & 0xF0) / 0x10;
		
		dst[dstpos++] = hexchars[hi];
		dst[dstpos++] = hexchars[lo];
	}
}
			
void spep::ipc::platform::textEncoding::decode( char *dst, const char *buf, const std::size_t dstlen )
{
	for (std::size_t bufpos = 1, dstpos = 0; dstpos < dstlen; )
	{
		unsigned char hi = buf[bufpos++];
		unsigned char lo = buf[bufpos++];
		
		if (hi == '\0' || lo == '\0')
		{
			dst[dstpos] = '\0';
			return;
		}
		
		hi = TEXTENCODING_HEX_CHAR_TO_DECIMAL_DIGIT(hi);
		lo = TEXTENCODING_HEX_CHAR_TO_DECIMAL_DIGIT(lo);
		unsigned char c = (hi * 0x10) + lo;
		
		dst[dstpos++] = c;
	}
}

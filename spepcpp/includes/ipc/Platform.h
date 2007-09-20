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

#ifndef PLATFORM_H_
#define PLATFORM_H_

#include <errno.h>
#include <netdb.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/ip.h>
#include <netinet/in.h>

#include <iostream>

#include "ipc/Exceptions.h"

namespace spep { namespace ipc {
		
	
#define SOCKET_DOMAIN PF_INET
#define SOCKET_TYPE SOCK_STREAM
#define SOCKET_PROTO "tcp"
#define SOCKET_BACKLOG 1


/// Define to get the actual socket out of the wrapper object
#define SOCKET(x) ((x).socket)

	/**
	 * A wrapper for a socket. This is here current to ensure that all socket operations
	 * performed outside this class are abstract.
	 */
	class SocketWrapper
	{
		friend class platform;
		int socket;
		SocketWrapper(int s):socket(s){}
		public:
		/// Default constructor, creates the object with an invalid socket.
		SocketWrapper():socket(-1){}
		/// Copy constructor, duplicates the socket object given without modifying the original
		SocketWrapper(const SocketWrapper &rhs):socket(rhs.socket){}
		/// Assignment operator, duplicates the socket object. No action is performed
		/// to close the current socket before it is overwritten
		SocketWrapper &operator=(const SocketWrapper &rhs)
		{ socket = rhs.socket; return *this; }
		
		bool operator==(const SocketWrapper &rhs) const
		{ return socket == rhs.socket; }
		bool operator!=(const SocketWrapper &rhs) const
		{ return socket == rhs.socket; }
	};
	
	/**
	 * Platform dependant operations.
	 * 
	 * Contains static member functions which can be reimplemented for different 
	 * platforms without modifying the calling code
	 */
	class platform
	{
	
		private:
		static protoent* tcpProtocol;
		
		public:
		
		typedef SocketWrapper socket_t;
		
		/**
		 * Open a socket
		 * @return The socket as a platform::socket_t
		 * @throw SocketException if an error occurred
		 */
		inline static socket_t openSocket() throw(SocketException)
		{
			if( platform::tcpProtocol == NULL )
			{
				setprotoent(0);
				platform::tcpProtocol = getprotobyname( SOCKET_PROTO );
				endprotoent();
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
			setsockopt( SOCKET(retval), SOL_SOCKET, SO_REUSEADDR, &value, sizeof(value) );
			
			return retval;
		}
		
		/**
		 * Connects a socket to an endpoint.
		 * @param sock The socket to connect
		 * @param addr The address to connect to
		 * @param port The port
		 * @throw SocketException if an error occurred
		 */
		inline static void connectSocket( socket_t sock, const char *addr, int port ) throw(SocketException)
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
		
		/**
		 * Connects a socket to a loopback endpoint.
		 * @param sock The socket to connect
		 * @param port The port
		 * @throw SocketException if an error occurred
		 */
		inline static void connectLoopbackSocket( socket_t sock, int port ) throw(SocketException)
		{
			sockaddr_in sa;
			sa.sin_family = AF_INET;
			sa.sin_port = htons( port );
			sa.sin_addr.s_addr = htonl(INADDR_LOOPBACK);
			
			if ( connect( SOCKET(sock), (sockaddr*)&sa, sizeof(sa) ) == 0 ) return;
			
			throw SocketException( strerror(errno) );
		}
		
		/**
		 * Binds a socket to an address
		 * @param sock The socket to bind
		 * @param addr The address to bind to
		 * @param port The port to bind to
		 * @throw SocketException if an error occurred
		 */
		inline static void bindSocket( socket_t sock, const char *addr, int port ) throw(SocketException)
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

		/**
		 * Binds a socket to a loopback address
		 * @param sock The socket to bind
		 * @param port The port to bind to
		 * @throw SocketException if an error occurred
		 */		
		inline static void bindLoopbackSocket( socket_t sock, int port ) throw(SocketException)
		{
			sockaddr_in sa;
			sa.sin_family = AF_INET;
			sa.sin_port = htons( port );
			sa.sin_addr.s_addr = htonl(INADDR_LOOPBACK);
			
			int result = bind( SOCKET(sock), (sockaddr*)&sa, sizeof(sa) );
			if ( result == 0 ) return;
			
			throw SocketException( strerror(errno) );
		}
		
		/**
		 * Starts a socket listening
		 * @param sock The socket
		 * @throw SocketException if an error occurred
		 */
		inline static void listenSocket( socket_t sock ) throw(SocketException)
		{
			if ( listen( SOCKET(sock), SOCKET_BACKLOG ) == 0 ) return;
			throw SocketException( strerror(errno) );
		}
		
		/**
		 * Accepts an incoming connection on the socket
		 * @param sock The socket
		 * @return A new socket_t which has the accepted connection
		 * @throw SocketException if an error occurred
		 */
		inline static socket_t acceptSocket( socket_t sock ) throw(SocketException)
		{
			sockaddr_in sa;
			socklen_t len = sizeof(sockaddr_in);
			socket_t retval = -1;
			retval = accept( SOCKET(sock), (sockaddr*)&sa, &len );
			
			if ( validSocket( retval ) ) return retval;
			
			throw SocketException( strerror(errno) );
		}
		
		/**
		 * Reads data from the given socket
		 * @param sock The socket
		 * @param buf The output buffer to read into
		 * @param buflen The length of the output buffer (the amount of data to read)
		 * @param flags Flags to be used when reading.
		 * @return Number of bytes read, or 0 if EOF has been reached
		 * @throw SocketException if an error occurred
		 */
		inline static ssize_t readSocket( socket_t sock, char *buf, int buflen, int flags ) throw(SocketException)
		{
			ssize_t result = recv( SOCKET(sock), buf, buflen, flags );
			if (result < 0)
			{
				throw SocketException( strerror(errno) );
			}
			//else if (result == 0)
			// ignore eof, calling method can handle it.
			
			return result;
		}
		
		/**
		 * Writes data to the given socket
		 * @param sock The socket
		 * @param buf The buffer to write into the socket
		 * @param buflen The number of bytes to write
		 * @param flags Flags to be used when writing
		 * @return Number of bytes written
		 * @throw SocketException if a write error occurred
		 */
		inline static ssize_t writeSocket( socket_t sock, const char *buf, int buflen, int flags ) throw(SocketException)
		{
			ssize_t result = send( SOCKET(sock), buf, buflen, flags );
			if (result < 0)
			{
				throw SocketException( strerror(errno) );
			}
			
			return result;
		}
		
		/**
		 * Closes the socket
		 * @param sock The socket
		 * @throw SocketException if an error occurred
		 */
		inline static void closeSocket( socket_t sock ) throw(SocketException)
		{
			if ( close( SOCKET(sock) ) == 0 ) return;
			
			throw SocketException( strerror(errno) );
		}
		
		/**
		 * Determines whether this is a valid socket.
		 * Note: Being a valid socket does not mean that it is ready for read/write, it just means
		 * for example that in linux it has a descriptor >= 0
		 * @param sock The socket
		 * @return boolean value to indicate validitity
		 */
		inline static bool validSocket( socket_t sock )
		{
			return SOCKET(sock) >= 0;
		}
		
		class flags
		{
			public:
			static const int nonBlocking =
#ifdef __CYGWIN__
				0
#else 
				MSG_DONTWAIT
#endif
				;
		};
		
		/**
		 * Performs text encoding operations
		 */
		class textEncoding
		{
#define TEXTENCODING_HEXCHARS "0123456789abcdef"
#define TEXTENCODING_HEX_CHAR_TO_DECIMAL_DIGIT(x) \
	(  	isdigit(x)	?  (  x - '0' )  :\
	( isalpha(x) && (tolower(x) <= 'f') )	?  ( tolower(x) - 'a' + 10 )  :  0  )
	
			public:
			
			/**
			 * Calculates the size that a binary block of 'len' bytes will occupy when
			 * it is encoded by #encode
			 * @param len The length of binary data
			 * @return The amount of space required to encode the binary data
			 */
			inline static std::size_t encodedSize( std::size_t len )
			{
				return 2*len + 1;
			}
			
			/**
			 * Calculates the size that a binary block of data bytes will occupy when
			 * it is decoded by #decode from 'len' bytes of encoded data
			 * @param len The length of binary data
			 * @return The amount of space required to encode the binary data
			 */
			inline static std::size_t decodedSize( std::size_t len )
			{
				return (len - 1)/2;
			}
			
			/**
			 * Encodes a block of binary data into the destination buffer given.
			 * @param dst The destination buffer
			 * @param buf The source data buffer
			 * @param The number of bytes available in the destination buffer
			 */
			inline static void encode( char *dst, const char *buf, const std::size_t dstlen )
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
			
			/**
			 * Decodes a block of binary data into the destination buffer given.
			 * @param dst The destination buffer
			 * @param buf The source data buffer
			 * @param The number of bytes available in the destination buffer
			 */
			inline static void decode( char *dst, const char *buf, const std::size_t dstlen )
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
			
		};
		
	};
		
}}
#endif /*PLATFORM_H_*/

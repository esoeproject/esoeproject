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

namespace spep{namespace ipc{}}

#include "spep/Util.h"
#include "spep/ipc/Exceptions.h"

#ifdef WIN32

// Stop the windows headers from bringing in extra stuff we don't want.
#define WIN32_LEAN_AND_MEAN 1

/* Windows includes */
#include <winsock2.h> // Include this first, otherwise we get errors
#include <windows.h>

#include <string> // For std::size_t below

/* Defines for Windows builds. */
#define NATIVE_SOCKET_TYPE SOCKET

typedef const char SOCKOPT_TYPE; // Type to cast to for setsockopt()
typedef int in_addr_t; // Missing type
typedef int socklen_t; // Missing type
typedef std::size_t ssize_t;

#else /*WIN32*/

/* *nix includes */
#include <errno.h>
#include <netdb.h>
#include <sys/types.h>
#include <sys/socket.h>
// in_systm.h is needed to build on FreeBSD
#include <netinet/in_systm.h>
// in.h needs to be included before ip.h on FreeBSD
#include <netinet/in.h>
#include <netinet/ip.h>

/* Defines for *nix builds. */
#define NATIVE_SOCKET_TYPE int
#define INVALID_SOCKET -1

typedef int SOCKOPT_TYPE; // Type to cast to for setsockopt()

#endif /*WIN32*/


/* Generic includes */
#include <iostream>


/* Generic defines */
#define SOCKET_DOMAIN PF_INET
#define SOCKET_TYPE SOCK_STREAM
#define SOCKET_PROTO "tcp"
#define SOCKET_BACKLOG 1



namespace spep { namespace ipc {
		
	

/// Define to get the actual socket out of the wrapper object
#define SOCKET(x) ((x).socket)

	/**
	 * A wrapper for a socket. This is here currently to ensure that all socket operations
	 * performed outside this class are abstract.
	 */
	class SPEPEXPORT SocketWrapper
	{
		friend class platform;
		NATIVE_SOCKET_TYPE socket;
		SocketWrapper(NATIVE_SOCKET_TYPE s);
		public:
		/// Default constructor, creates the object with an invalid socket.
		SocketWrapper();
		/// Copy constructor, duplicates the socket object given without modifying the original
		SocketWrapper(const SocketWrapper &rhs);
		/// Assignment operator, duplicates the socket object. No action is performed
		/// to close the current socket before it is overwritten
		SocketWrapper &operator=(const SocketWrapper &rhs);
		
		bool operator==(const SocketWrapper &rhs) const;
		bool operator!=(const SocketWrapper &rhs) const;
	};
	
	/**
	 * Platform dependant operations.
	 * 
	 * Contains static member functions which can be reimplemented for different 
	 * platforms without modifying the calling code
	 */
	class SPEPEXPORT platform
	{
	
		private:
		static protoent* tcpProtocol;
#ifdef WIN32
		static WSADATA wsaData;
#endif /*WIN32*/
		
		public:
		
		typedef SocketWrapper socket_t;
		
		/**
		 * Open a socket.
		 * Care MUST be taken to ensure that this method is invoked at least once
		 * in a thread safe manner. After the first call, thread safety is OS-defined.
		 * @return The socket as a platform::socket_t
		 * @throw SocketException if an error occurred
		 */
		static socket_t openSocket();
		
		/**
		 * Connects a socket to an endpoint.
		 * @param sock The socket to connect
		 * @param addr The address to connect to
		 * @param port The port
		 * @throw SocketException if an error occurred
		 */
		static void connectSocket( socket_t sock, const char *addr, int port );
		
		/**
		 * Connects a socket to a loopback endpoint.
		 * @param sock The socket to connect
		 * @param port The port
		 * @throw SocketException if an error occurred
		 */
		static void connectLoopbackSocket( socket_t sock, int port );
		
		/**
		 * Binds a socket to an address
		 * @param sock The socket to bind
		 * @param addr The address to bind to
		 * @param port The port to bind to
		 * @throw SocketException if an error occurred
		 */
		static void bindSocket( socket_t sock, const char *addr, int port );

		/**
		 * Binds a socket to a loopback address
		 * @param sock The socket to bind
		 * @param port The port to bind to
		 * @throw SocketException if an error occurred
		 */		
		static void bindLoopbackSocket( socket_t sock, int port );
		
		/**
		 * Starts a socket listening
		 * @param sock The socket
		 * @throw SocketException if an error occurred
		 */
		static void listenSocket( socket_t sock );
		
		/**
		 * Accepts an incoming connection on the socket
		 * @param sock The socket
		 * @return A new socket_t which has the accepted connection
		 * @throw SocketException if an error occurred
		 */
		static socket_t acceptSocket( socket_t sock );
		
		/**
		 * Reads data from the given socket
		 * @param sock The socket
		 * @param buf The output buffer to read into
		 * @param buflen The length of the output buffer (the amount of data to read)
		 * @param flags Flags to be used when reading.
		 * @return Number of bytes read, or 0 if EOF has been reached
		 * @throw SocketException if an error occurred
		 */
		static ssize_t readSocket( socket_t sock, char *buf, int buflen, int flags );
		
		/**
		 * Writes data to the given socket
		 * @param sock The socket
		 * @param buf The buffer to write into the socket
		 * @param buflen The number of bytes to write
		 * @param flags Flags to be used when writing
		 * @return Number of bytes written
		 * @throw SocketException if a write error occurred
		 */
		static ssize_t writeSocket( socket_t sock, const char *buf, int buflen, int flags );
		
		/**
		 * Closes the socket
		 * @param sock The socket
		 * @throw SocketException if an error occurred
		 */
		static void closeSocket( socket_t sock );
		
		/**
		 * Determines whether this is a valid socket.
		 * Note: Being a valid socket does not mean that it is ready for read/write, it just means
		 * for example that in linux it has a descriptor >= 0
		 * @param sock The socket
		 * @return boolean value to indicate validitity
		 */
		static bool validSocket( socket_t sock );
		
		class flags
		{
			public:
			static const int nonBlocking =
#if defined(__CYGWIN__) || defined(WIN32)
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
			static std::size_t encodedSize( std::size_t len );
			
			/**
			 * Calculates the size that a binary block of data bytes will occupy when
			 * it is decoded by #decode from 'len' bytes of encoded data
			 * @param len The length of binary data
			 * @return The amount of space required to encode the binary data
			 */
			static std::size_t decodedSize( std::size_t len );
			
			/**
			 * Encodes a block of binary data into the destination buffer given.
			 * @param dst The destination buffer
			 * @param buf The source data buffer
			 * @param The number of bytes available in the destination buffer
			 */
			static void encode( char *dst, const char *buf, const std::size_t dstlen );
			
			/**
			 * Decodes a block of binary data into the destination buffer given.
			 * @param dst The destination buffer
			 * @param buf The source data buffer
			 * @param The number of bytes available in the destination buffer
			 */
			static void decode( char *dst, const char *buf, const std::size_t dstlen );
			
		};
		
	};
		
}}
#endif /*PLATFORM_H_*/

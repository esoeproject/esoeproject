#include "spep/Util.h"
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

#ifndef IPC_H_
#define IPC_H_

#include "spep/Util.h"
#include "spep/ipc/Serialization.h"
#include <string>

namespace spep { namespace ipc {
	
	/**
	 * Base class for IPC exceptions. Methods are purposely not declared virtual, since
	 * this must be able to be serialized via the templated serialize() function.
	 */
	class SPEPEXPORT IPCException : std::exception
	{
		friend class spep::ipc::access;
		
		public:
		IPCException(){}
		IPCException(std::string message):_message(message){}
		~IPCException()throw(){}

		/** Returns the message provided when constructed */		
		const std::string &message() { return _message; }
		
		/** Returns C string version of message */
		const char *what() throw()
		{ return _message.c_str(); }
		
		/** Assignment operator. Copies the message */
		IPCException &operator=( const IPCException &rhs )
		{ _message = rhs._message; return *this; }
		
		private:
		template <class Archive>
		void serialize( Archive &ar, const unsigned int version )
		{ ar & _message; }
		
		std::string _message;
	};
	
	/**
	 * Thrown to indicate that the socket archive was in an invalid state.
	 * Means that invalid data was received in a message header.
	 */
	class SPEPEXPORT InvalidArchiveStateException : public IPCException
	{
		public:
		InvalidArchiveStateException(std::string message):IPCException(message){}
	};
	
	/**
	 * Thrown to indicate that an exception was thrown by the target of invocation.
	 * Would be thrown by the dispatcher.
	 */
	class SPEPEXPORT InvocationTargetException : public IPCException
	{
		public:
		InvocationTargetException(std::string message):IPCException(message){}
	};
	
	/**
	 * Exception thrown when some error occurs in a socket operation
	 */
	class SPEPEXPORT SocketException : public IPCException
	{
		public:
		SocketException (std::string message):IPCException(message){}
	};	
} }

#endif /*IPC_H_*/


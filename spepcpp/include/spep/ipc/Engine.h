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
 * Creation Date: 07/02/2007
 * 
 * Purpose: 
 */

#ifndef ENGINE_H_
#define ENGINE_H_

#include "spep/Util.h"
#include "spep/ipc/MessageHeader.h"
#include "spep/ipc/SocketArchive.h"

#include <asio.hpp>

namespace spep { namespace ipc {

	using namespace boost;
	using asio::ip::tcp;

	class SPEPEXPORT Engine
	{
		public:
		Engine(function<void(const std::vector<char>&)> writeCallback, function<void(std::vector<char>&)> readCallback)
		: _archive(writeCallback, readCallback) {}
		
		/**
		 * Sends a request with the dispatch string provided and awaits a response.
		 * @param dispatch Where to dispatch the request at the other end
		 * @param req The request object
		 * @return A result object
		 */
		template <class Res, class Req>
		Res makeRequest( std::string &dispatch, Req &req );
		
		/**
		 * Sends a request with the dispatch string provided but does not await any response.
		 * It would be erroneous for the other end to respond to a non blocking request.
		 * @param dispatch Where to dispatch the request at the other end
		 * @param req The request object
		 */
		template <class Req>
		void makeNonBlockingRequest( std::string &dispatch, Req &req );
		
		/**
		 * Deserializes an object from the connection stream.
		 */
		template <class T>
		void getObject(T &t);
		
		/**
		 * Serializes an object to the connection stream.
		 */
		template <class T>
		void sendObject(T &t);
		
		/**
		 * Deserializes a request header from the connection stream.
		 * @return A MessageHeader object, not guaranteed to be a request
		 */
		MessageHeader getRequestHeader();
		
		/**
		 * Sends an error response header. No parameters are needed for a response header.
		 */
		void sendErrorResponseHeader();
		
		/**
		 * Sends a response header. No parameters are needed for a response header.
		 */
		void sendResponseHeader();
		
		private:
		SocketArchive _archive;
		
	};
	
	
	template <class Res, class Req>
	Res Engine::makeRequest( std::string &dispatch, Req &req )
	{
		// Create a request header with the given dispatch string
		MessageHeader requestHeader( SPEPIPC_REQUEST, dispatch );
		
		// Send the request header first, then the request object
		_archive.out() << requestHeader; 
		_archive.out() << req;
		
		MessageHeader responseHeader;
		Res res;
		
		_archive.in() >> responseHeader;
		
		// If the response is a normal response, deserialize the response object and return it.
		if (SPEPIPC_RESPONSE == responseHeader.getType())
		{
			_archive.in() >> res;
			return res;
		}
		
		// If the response is an error response, deserialize the exception and throw it.
		if (SPEPIPC_RESPONSE_ERROR == responseHeader.getType())
		{
			IPCException e;
			_archive.in() >> e;
			throw e;
		}
		
		throw InvalidArchiveStateException( "Expected RESPONSE object but got different type" );
	}
	
	template <class Req>
	void Engine::makeNonBlockingRequest( std::string &dispatch, Req &req )
	{
		// Create the request header.
		MessageHeader requestHeader( SPEPIPC_NONBLOCKING, dispatch );
		
		// Send request header, then request object. No response is expected for SPEPIPC_NONBLOCKING
		_archive.out() << requestHeader << req;
	}

	template <class T>
	void Engine::getObject(T &t)
	{
		_archive.in() >> t;
	}
	
	template <class T>
	void Engine::sendObject(T &t)
	{
		_archive.out() << t;
	}

} }

#endif /*ENGINE_H_*/

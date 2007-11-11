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

#ifndef MESSAGEHEADER_H_
#define MESSAGEHEADER_H_

#include "spep/Util.h"
#include "spep/ipc/Serialization.h"

#include <string>

namespace spep { namespace ipc {
	
	/**
	 * Type of message that a header represents
	 */
	enum MessageType
	{
		SPEPIPC_REQUEST,	/* request - expect response */
		SPEPIPC_RESPONSE,	/* response - follows request */
		SPEPIPC_RESPONSE_ERROR, /* error response - follows request, indicates failure with exception in response */
		SPEPIPC_NONBLOCKING	/* non blocking - request with no response.. fire & forget */
	};
	
	/**
	 * Message header, to be serialized as the first (or only) part of an 
	 * RPC message, be it request or response.
	 */
	class SPEPEXPORT MessageHeader
	{
		
		friend class spep::ipc::access;
		
		template <class Archive>
		void serialize( Archive &ar, const unsigned int version );
		
		public:
		/**
		 * Constructs a new MessageHeader object with no values.
		 */
		MessageHeader();
		
		/**
		 * Constructs a new MessageHeader object with the given type and dispatch string
		 */
		MessageHeader( MessageType messageType, std::string dispatch );
		
		/**
		 * Returns the message type, one of the MessageType enum values.
		 */
		MessageType getType();
		
		/**
		 * Returns the dispatch string for this message. Only used for request messages.
		 */
		std::string &getDispatch();
		
		bool operator==(MessageHeader &rhs);
		bool operator!=(MessageHeader &rhs);
		
		private:
		MessageType _messageType;
		std::string _dispatch;
		
	};

	template <class Archive>
	void MessageHeader::serialize( Archive &ar, const unsigned int version )
	{
		ar & (unsigned int&)_messageType & _dispatch;
	}

} }

#endif /*MESSAGEHEADER_H_*/

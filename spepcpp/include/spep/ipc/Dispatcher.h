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
 * Creation Date: 08/02/2007
 * 
 * Purpose: 
 */
 
#ifndef DISPATCHER_H_
#define DISPATCHER_H_

#include "spep/Util.h"
#include "spep/ipc/MessageHeader.h"
#include "spep/ipc/Engine.h"

#include "saml2/logging/api.h"
#include "saml2/logging/api.h"

namespace spep { namespace ipc {
	
	/**
	 * Pure virtual base class for dispatchers
	 */
	class SPEPEXPORT Dispatcher
	{
		
		public:
		virtual ~Dispatcher(){}
		
		/**
		 * Dispatches the call, given the message header, and if necessary 
		 * writes the reply directly to the archive. If the call is dispatched
		 * a true value MUST be returned. If false is returned, the archive
		 * MUST not have been changed. If an exception is thrown, the request
		 * MUST have been fully read, but no response sent.
		 * @param header The IPC message header.
		 * @param en The engine used for the IPC.
		 * @return true if this dispatcher handled the call
		 */
		virtual bool dispatch( MessageHeader &header, Engine &en ) = 0;
		
	};
	
	/**
	 * Handles dispatching to multiple dispatchers.
	 */
	class SPEPEXPORT MultifacetedDispatcher : public Dispatcher
	{
		
		typedef std::vector<Dispatcher*>::iterator DispatcherIterator;
		
		public:
		
		MultifacetedDispatcher( saml2::Logger *logger, std::vector<Dispatcher*> dispatchers );
		
		virtual ~MultifacetedDispatcher();
		virtual bool dispatch( MessageHeader &header, Engine &en );
		
		private:
		saml2::LocalLogger _localLogger;
		std::vector<Dispatcher*> _dispatchers;
		
	};
	
	/**
	 * Handles dispatching to a dispatcher that may throw an exception
	 */
	class SPEPEXPORT ExceptionCatchingDispatcher : public Dispatcher
	{
		
		public:
		ExceptionCatchingDispatcher( saml2::Logger *logger, Dispatcher* nextDispatcher );
		
		virtual ~ExceptionCatchingDispatcher();
		virtual bool dispatch( MessageHeader &header, Engine &en );
		
		private:
		saml2::LocalLogger _localLogger;
		Dispatcher *_nextDispatcher;
		
	};
	
} }

#endif /*DISPATCHER_H_*/

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
 * Creation Date: Aug 8, 2007
 * 
 * Purpose: 
 */

#ifndef STARTUPPROCESSORPROXY_H_
#define STARTUPPROCESSORPROXY_H_

#include "ipc/Socket.h"

#include "startup/StartupProcessor.h"

namespace spep { namespace ipc {
	
	class StartupProcessorProxy : public spep::StartupProcessor
	{
		
		private:
		ClientSocket *_clientSocket;
		
		public:
		StartupProcessorProxy( ClientSocket *clientSocket );
		virtual ~StartupProcessorProxy();
		
		virtual spep::StartupResult allowProcessing();
		virtual void beginSPEPStart();
	};
	
} }

#endif /*STARTUPPROCESSORPROXY_H_*/

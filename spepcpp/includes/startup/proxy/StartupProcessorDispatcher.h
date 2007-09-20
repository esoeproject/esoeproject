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

#ifndef STARTUPPROCESSORDISPATCHER_H_
#define STARTUPPROCESSORDISPATCHER_H_

#include "ipc/Dispatcher.h"

#include "startup/StartupProcessor.h"

namespace spep { namespace ipc {
	
	class StartupProcessorDispatcher : public Dispatcher
	{
		
		private:
		spep::StartupProcessor *_startupProcessor;
		std::string _prefix;
		
#define STARTUPPROCESSOR "spep/startup/StartupProcessor/"
#define STARTUPPROCESSOR_allowProcessing  STARTUPPROCESSOR "allowProcessing"
		
		public:
		StartupProcessorDispatcher( spep::StartupProcessor *startupProcessor );
		virtual ~StartupProcessorDispatcher();
		
		virtual bool dispatch( MessageHeader &header, Engine &en );
		
	};
	
} }

#endif /*STARTUPPROCESSORDISPATCHER_H_*/

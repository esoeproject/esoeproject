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
 * Creation Date: 19/06/2007
 * 
 * Purpose: 
 */

#ifndef CONFIGURATIONDISPATCHER_H_
#define CONFIGURATIONDISPATCHER_H_

#include "spep/Util.h"
#include "spep/ipc/Dispatcher.h"

#include "spep/config/SPEPConfigData.h"
#include "spep/config/Configuration.h"

namespace spep{ namespace ipc{

	
	class SPEPEXPORT ConfigurationDispatcher : public Dispatcher
	{

		private:
		spep::Configuration *_configuration;
		std::string _prefix;
		
#define CONFIGURATION "spep/config/Configuration/"
#define CONFIGURATION_getSPEPConfigData CONFIGURATION "getSPEPConfigData"
		public:
		ConfigurationDispatcher( spep::Configuration *configuration );
		virtual ~ConfigurationDispatcher();
		
		virtual bool dispatch( MessageHeader &header, Engine &en );
		
	};
	
}}

#endif /*CONFIGURATIONDISPATCHER_H_*/

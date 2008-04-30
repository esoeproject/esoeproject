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
 * Creation Date: 20/06/2007
 * 
 * Purpose: 
 */

#ifndef CONFIGURATIONPROXY_H_
#define CONFIGURATIONPROXY_H_

#include "spep/Util.h"
#include "spep/ipc/Socket.h"

#include "spep/config/Configuration.h"

namespace spep{ namespace ipc{
	
	class SPEPEXPORT ConfigurationProxy : public spep::Configuration
	{
		
		private:
		spep::ipc::ClientSocketPool *_socketPool;
		
		public:
		ConfigurationProxy( spep::ipc::ClientSocketPool *socketPool );
		virtual ~ConfigurationProxy();

		virtual spep::SPEPConfigData getSPEPConfigData();
		
	};
	
}}

#endif /*CONFIGURATIONPROXY_H_*/

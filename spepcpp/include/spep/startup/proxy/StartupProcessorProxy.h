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

#include "spep/Util.h"
#include "spep/ipc/Socket.h"

#include "spep/startup/StartupProcessor.h"

namespace spep { namespace ipc {
	
    class SPEPEXPORT StartupProcessorProxy : public spep::StartupProcessor
    {
    public:
        StartupProcessorProxy(ClientSocketPool *socketPool);
        virtual ~StartupProcessorProxy();

        virtual spep::StartupResult allowProcessing() override;
        virtual void beginSPEPStart() override;

    private:
        ClientSocketPool *mSocketPool;

	};
	
} }

#endif /*STARTUPPROCESSORPROXY_H_*/

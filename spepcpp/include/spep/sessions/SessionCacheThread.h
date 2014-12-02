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
 * Creation Date: Sep 5, 2007
 * 
 * Purpose: 
 */

#ifndef SESSIONCACHETHREAD_H_
#define SESSIONCACHETHREAD_H_

#include "spep/Util.h"
#include "saml2/logging/api.h"
#include "saml2/logging/api.h"
#include "spep/sessions/SessionCache.h"

#include <boost/thread.hpp>

namespace spep
{
	
    class SPEPEXPORT SessionCacheThread
    {
    public:
        /**
         * Instantiates the thread object and starts the thread running.
         */
        SessionCacheThread(saml2::Logger *logger, SessionCache *sessionCache, int timeout, int interval);
        ~SessionCacheThread();

    private:
        saml2::LocalLogger mLocalLogger;
        SessionCache *mSessionCache;
        boost::thread_group mThreadGroup;
        int mTimeout;
        int mInterval;
        bool mDie;

        class ThreadHandler
        {
        public:
            ThreadHandler(SessionCacheThread *sessionCacheThread);
            ThreadHandler(const ThreadHandler& other);
            ThreadHandler& operator=(const ThreadHandler& other);

            /**
            * Thread target method.
            */
            void operator()();

        private:
            SessionCacheThread *mSessionCacheThread;
        };

        /**
        * Thread method body.
        */
        void doThreadAction();
    };

}

#endif /*SESSIONCACHETHREAD_H_*/

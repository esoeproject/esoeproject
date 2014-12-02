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

#include "spep/sessions/SessionCacheThread.h"
#include "spep/Util.h"

spep::SessionCacheThread::SessionCacheThread(saml2::Logger *logger, spep::SessionCache *sessionCache, int timeout, int interval) :
    mLocalLogger(logger, "spep::SessionCacheThread"),
    mSessionCache(sessionCache),
    mThreadGroup(),
    mTimeout(timeout),
    mInterval(interval),
    mDie(false)
{
    mLocalLogger.info() << "Session cache thread starting..";
    mThreadGroup.create_thread(ThreadHandler(this));
}

spep::SessionCacheThread::~SessionCacheThread()
{
    mDie = true;
    mThreadGroup.join_all();
}

spep::SessionCacheThread::ThreadHandler::ThreadHandler(spep::SessionCacheThread *sessionCacheThread) :
    mSessionCacheThread(sessionCacheThread)
{
}

spep::SessionCacheThread::ThreadHandler::ThreadHandler(const spep::SessionCacheThread::ThreadHandler &other) :
    mSessionCacheThread(other.mSessionCacheThread)
{
}

spep::SessionCacheThread::ThreadHandler& spep::SessionCacheThread::ThreadHandler::operator=(const spep::SessionCacheThread::ThreadHandler &other)
{
    mSessionCacheThread = other.mSessionCacheThread;
    return *this;
}

void spep::SessionCacheThread::ThreadHandler::operator()()
{
    mSessionCacheThread->doThreadAction();
}

void spep::SessionCacheThread::doThreadAction()
{
    for (;;)
    {
        mLocalLogger.info() << "Session cache thread sleeping for " << mInterval << " seconds.";
        InterruptibleSleeper(mInterval, 0, 500, &mDie).sleep();
        if (mDie)
        {
            mLocalLogger.info() << "Session cache thread shutting down.";
            return;
        }

        mLocalLogger.info() << "Going to check expiry times on session cache.";
        mSessionCache->terminateExpiredSessions(mTimeout);
    }
}

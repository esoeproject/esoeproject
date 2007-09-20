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

#include "sessions/SessionCacheThread.h"
#include "Util.h"

spep::SessionCacheThread::SessionCacheThread( spep::ReportingProcessor *reportingProcessor, spep::SessionCache *sessionCache, int timeout, int interval )
:
_localReportingProcessor( reportingProcessor->localReportingProcessor( "spep::SessionCacheThread" ) ),
_sessionCache( sessionCache ),
_threadGroup(),
_timeout( timeout ),
_interval( interval ),
_die( false )
{
	this->_localReportingProcessor.log( DEBUG, "Session cache thread starting.." );
	
	_threadGroup.create_thread( ThreadHandler( this ) );
}

spep::SessionCacheThread::~SessionCacheThread()
{
	_die = true;
	
	_threadGroup.join_all();
}

spep::SessionCacheThread::ThreadHandler::ThreadHandler( spep::SessionCacheThread *sessionCacheThread )
:
_sessionCacheThread( sessionCacheThread )
{
}

spep::SessionCacheThread::ThreadHandler::ThreadHandler( const spep::SessionCacheThread::ThreadHandler &other )
:
_sessionCacheThread( other._sessionCacheThread )
{
}

spep::SessionCacheThread::ThreadHandler& spep::SessionCacheThread::ThreadHandler::operator=( const spep::SessionCacheThread::ThreadHandler &other )
{
	_sessionCacheThread = other._sessionCacheThread;
	
	return *this;
}

void spep::SessionCacheThread::ThreadHandler::operator()()
{
	_sessionCacheThread->doThreadAction();
}

void spep::SessionCacheThread::doThreadAction()
{
	for(;;)
	{
		InterruptibleSleeper( _interval, 500, &_die ).sleep();
		if( _die )
		{
			this->_localReportingProcessor.log( INFO, "Session cache thread shutting down." );
			return;
		}
		
		this->_localReportingProcessor.log( INFO, "Going to check expiry times on session cache." );
		_sessionCache->terminateExpiredSessions( _timeout );
	}
}

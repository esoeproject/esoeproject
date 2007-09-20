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

#include "reporting/ReportingProcessor.h"
#include "reporting/LocalReportingProcessor.h"
#include "sessions/SessionCache.h"

#include <boost/thread.hpp>

namespace spep
{
	
	class SessionCacheThread
	{
	
		private:
		LocalReportingProcessor _localReportingProcessor;
		SessionCache *_sessionCache;
		boost::thread_group _threadGroup;
		int _timeout;
		int _interval;
		bool _die;
		
		class ThreadHandler
		{
		
			private:
			SessionCacheThread *_sessionCacheThread;
			
			public:
			ThreadHandler( SessionCacheThread *sessionCacheThread );
			ThreadHandler( const ThreadHandler& other );
			ThreadHandler& operator=( const ThreadHandler& other );
			
			/**
			 * Thread target method.
			 */
			void operator()();
		};
		
		/**
		 * Thread method body.
		 */
		void doThreadAction();

		public:
		/**
		 * Instantiates the thread object and starts the thread running.
		 */
		SessionCacheThread( ReportingProcessor *reportingProcessor, SessionCache *sessionCache, int timeout, int interval );
		~SessionCacheThread();
		
		
	};
	
}

#endif /*SESSIONCACHETHREAD_H_*/

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
 * Creation Date: Jul 20, 2007
 * 
 * Purpose: 
 */
#ifndef APRFILELOGGER_H_
#define APRFILELOGGER_H_

#include "spep/reporting/Handler.h"
#include "APRDefinitions.h"

namespace spep { namespace apache {
	
	class APRFileLogger : public spep::Handler
	{
		
		private:
		class APRFileException : public std::exception
		{
			private:
			char *_reason;
			
			public:
			APRFileException( apr_status_t reason );
			virtual const char *what() const throw();
			virtual ~APRFileException() throw(){}
		};
		
		class APRLockException : public std::exception
		{
			public:
			virtual const char *what() const throw();
			virtual ~APRLockException() throw(){}
		};
		
		class APRFileLock
		{
			
			private:
			apr_file_t *_file;
			
			APRFileLock( const APRFileLock& other );
			APRFileLock& operator=( const APRFileLock& other );
			
			public:
			APRFileLock( apr_file_t *file, bool writeLock = true );
			~APRFileLock();
			
		};
		
		std::string _fileName;
		apr_file_t *_file;
		Level _level;
		
		APRFileLogger( const APRFileLogger& other );
		APRFileLogger& operator=( const APRFileLogger& other );
		
		public:
		APRFileLogger( apr_pool_t *pool, std::string fileName, Level level );
		virtual ~APRFileLogger();
		virtual void log(const std::string &name, const Level level, const std::string &message);
		
	};
	
} }

#endif /*APRFILELOGGER_H_*/

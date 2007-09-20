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

#include <sstream>

#include "reporting/ReportingProcessor.h"
#include "APRFileLogger.h"

#include "APRDefinitions.h"

#define APR_ERROR_LENGTH 512

spep::apache::APRFileLogger::APRFileLogger( apr_pool_t *pool, std::string fileName, spep::Level level )
:
_fileName( fileName ),
_file( NULL ),
_level( level )
{
	/* Flags:
	 *   open for writing
	 *   create if doesn't exist
	 *   every write is an append
	 *   use cross-thread support
	 *   use read/write locking support
	 */
	apr_int32_t flags = APR_WRITE | APR_CREATE | APR_APPEND | APR_XTHREAD | APR_SHARELOCK;
	apr_fileperms_t perms = APR_UREAD | APR_UWRITE | APR_GREAD;
	apr_status_t status = apr_file_open( &_file, fileName.c_str(), flags, perms, pool );
	if( status != APR_SUCCESS )
	{
		// File open failed. We probably want to abort httpd startup.
		throw APRFileException( status );
	}
}

spep::apache::APRFileLogger::~APRFileLogger()
{
	apr_file_close( _file );
}

void spep::apache::APRFileLogger::log(const std::string &name, const Level level, const std::string &message)
{
	if( level >= this->_level )
	{
		APRFileLock lock( _file );
		
		// Build the line to be printed in a string stream.
		std::stringstream lineStream;
		
		lineStream << spep::ReportingProcessor::timestamp() << " [" << level << "] " << name << " - " << message << std::endl << std::flush;
		
		std::string line = lineStream.str();
		apr_file_puts( line.c_str(), _file );
	}
}

spep::apache::APRFileLogger::APRFileException::APRFileException( apr_status_t reason )
:
_reason( new char[APR_ERROR_LENGTH] )
{
	apr_strerror( reason, _reason, APR_ERROR_LENGTH );
}

const char *spep::apache::APRFileLogger::APRFileException::what() const throw()
{
	return this->_reason;
}

const char *spep::apache::APRFileLogger::APRLockException::what() const throw()
{
	return "Couldn't obtain APR lock on file.";
}

spep::apache::APRFileLogger::APRFileLock::APRFileLock( apr_file_t *file, bool writeLock )
:
_file( file )
{
	int type = 0;
	
	if( writeLock )
	{
		type = APR_FLOCK_EXCLUSIVE;
	}
	else
	{
		type = APR_FLOCK_SHARED;
	}
	
	if( apr_file_lock( file, type ) != APR_SUCCESS )
	{
		throw APRLockException();
	}
}

spep::apache::APRFileLogger::APRFileLock::~APRFileLock()
{
	if( apr_file_unlock( _file ) != APR_SUCCESS )
	{
		throw APRLockException();
	}
}

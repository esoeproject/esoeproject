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
 * Creation Date: 04/06/2007
 * 
 * Purpose: 
 */

#include "Daemon.h"

#include "signal.h"

#include <cstdio>
#include <cstdlib>

#include <iostream>
#include <fstream>

#ifdef WIN32

void spep::daemon::Daemon::daemonize()
{
}

void spep::daemon::Daemon::prepare()
{
}

#else /*WIN32*/

#include <unistd.h>

std::vector<std::string> spep::daemon::Daemon::pidFileList;

void spep::daemon::Daemon::daemonize()
{
	// Become a daemon.
	int pid = fork();
	if( pid == 0 )
	{
		
		pid = fork();
		if( pid == 0 )
		{
			
			// Child process.
			setsid();
			return;
		}
		else if( pid == -1 )
		{
			// TODO Throw a better exception
			throw new std::exception();
		}
		else
		{
			// Parent process. Write child PID and terminate
			for( std::vector<std::string>::const_iterator pidFileIterator = pidFileList.begin(); pidFileIterator != pidFileList.end(); ++pidFileIterator )
			{
				std::ofstream pidFileOutput( pidFileIterator->c_str(), std::ios::out|std::ios::trunc );
				if( !pidFileOutput.good() )
				{
					std::cerr << "Couldn't write PID to file: " << *pidFileIterator << " .. continuing anyway." << std::endl;
				}
				else
				{
					pidFileOutput << pid << std::flush;
					pidFileOutput.close();
				}
			}
			
			_exit(0);
		}
		
	}
	else if( pid == -1 )
	{
		// TODO Throw a better exception
		throw new std::exception();
	}
	else
	{
		// Parent process. Terminate
		_exit(0);
	}
}

void spep::daemon::Daemon::prepare()
{
	signal( SIGPIPE, SIG_IGN );
}

#endif /*WIN32*/

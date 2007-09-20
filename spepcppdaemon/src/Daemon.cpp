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

void spep::daemon::Daemon::daemonize()
{
	
	// TODO Do we need this for other platforms? I'm guessing not.
	// Become a daemon.
	int pid = fork();
	if( pid == 0 )
	{
		
		pid = fork();
		if( pid == 0 )
		{
			// Child process. Close file descriptors and return safely.
			Daemon::closefd();
			return;
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

void spep::daemon::Daemon::closefd()
{
	
	//fclose( stdin );
	//fclose( stdout );
	//fclose( stderr );
	
}

void spep::daemon::Daemon::prepare()
{
	
	signal( SIGPIPE, SIG_IGN );
	
}

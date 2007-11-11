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
#include "StreamLogHandler.h"

#include "spep/config/ConfigurationReader.h"
#include "spep/SPEP.h"
#include "spep/Util.h"
#include "spep/UnicodeStringConversion.h"

#include "spep/ipc/Platform.h"
#include "spep/ipc/SocketArchive.h"
#include "spep/ipc/MessageHeader.h"
#include "spep/ipc/Engine.h"
#include "spep/ipc/Socket.h"

#include "spep/metadata/proxy/MetadataDispatcher.h"
#include "spep/config/proxy/ConfigurationDispatcher.h"
#include "spep/sessions/proxy/SessionCacheDispatcher.h"
#include "spep/pep/proxy/SessionGroupCacheDispatcher.h"
#include "spep/startup/proxy/StartupProcessorDispatcher.h"
#include "spep/identifier/proxy/IdentifierCacheDispatcher.h"

#include <iostream>
#include <fstream>
#include <vector>
#include <string>

#include <cstdlib>

#include <boost/program_options/cmdline.hpp>
#include <boost/program_options/config.hpp>
#include <boost/program_options/option.hpp>
#include <boost/program_options/options_description.hpp>
#include <boost/program_options/parsers.hpp>
#include <boost/program_options/value_semantic.hpp>
#include <boost/program_options/variables_map.hpp>

#ifdef WIN32
#define WIN32_LEAN_AND_MEAN
// Include winsock2 before windows so it doesn't screw with our socket stuff
#include <winsock2.h>
#include <windows.h>

int main( int argc, char **argv );

int WINAPI WinMain(
	HINSTANCE hInstance,
	HINSTANCE hPrevInstance,
	LPSTR lpCmdLine,
	int nCmdShow
	)
{
	LPWSTR* argList;
	int argc = 0;
	argList = CommandLineToArgvW( GetCommandLineW(), &argc );

	if( argList == NULL )
	{
		std::cerr << "Failed to get command line arguments." << std::endl;
		return 1;
	}

	spep::AutoArray< spep::CArray<char> > translatedArgs( argc );
	char **argv = new char*[ argc + 1 ];
	for( int i = 0; i < argc; ++i )
	{
		std::string currentArg = spep::UnicodeStringConversion::toString( std::wstring(argList[i]) );
		std::size_t length = currentArg.length() + 1;
		translatedArgs[i].resize( length );
		currentArg.copy( translatedArgs[i].get(), length );

		argv[i] = translatedArgs[i].get();
	}
	argv[argc] = NULL;

	LocalFree( argList );
	return main( argc, argv );
}
#endif

int main( int argc, char **argv )
{
	
	boost::program_options::options_description commandLineDescription( "spepd options" );
	// Declare command line arguments
	commandLineDescription.add_options()
		( "help", "display this help message" )
		( "config-file,f", boost::program_options::value< std::vector<std::string> >(), "the spepd configuration file to use" )
		( "log-file,l", boost::program_options::value< std::string >(), "file to send log output to" )
		( "debug", "run in debug mode (don't fork to become daemon)" )
		( "verbose,v", "run in verbose mode (display some messages on startup to describe what is happening)" )
	;
	boost::program_options::variables_map commandLineVariableMap;
	// Parse and store command line args in the variable map
	boost::program_options::store(
		boost::program_options::parse_command_line(argc, argv, commandLineDescription), 
		commandLineVariableMap
	);
	boost::program_options::notify(commandLineVariableMap);
	
	// Check to see if the user wanted (or should get!) help
	if( commandLineVariableMap.count("help") || !(commandLineVariableMap.count("config-file")) )
	{
		// Display help text and terminate.
		std::cout << commandLineDescription << std::endl;
		exit(1);
	}
	
	bool verbose = (commandLineVariableMap.count("help") != 0);
	
	// Set up the configuration parameters.
	boost::program_options::options_description configDescription( "spepd configuration options" );
	spep::ConfigurationReader::addOptions( configDescription );
	
	// Load config
	boost::program_options::variables_map configFileVariableMap;
	const std::vector<std::string> &configFilenameList = commandLineVariableMap["config-file"].as< std::vector<std::string> >();
	// Loop through every configuration filename specified.
	for( std::vector<std::string>::const_iterator filenameIterator = configFilenameList.begin(); filenameIterator != configFilenameList.end(); ++filenameIterator )
	{
		
		if( verbose )
		{
			std::cout << "Reading configuration file: " << *filenameIterator << std::endl << std::flush;
		}
		
		// Open the file
		std::ifstream configFileInput( filenameIterator->c_str() );
		
		if( !configFileInput.good() )
		{
			std::cerr << "Couldn't open configuration file: " << *filenameIterator << " .. aborting" << std::endl;
			exit(1);
		}
		
		// Parse and store it in the variable map
		boost::program_options::store(
			boost::program_options::parse_config_file(configFileInput, configDescription),
			configFileVariableMap
		);
		
		// File will be closed when configFileInput object is deleted
	}
	
	if( verbose )
	{
		std::cout << "Finished reading configuration." << std::endl << std::flush;
	}
	
	boost::program_options::notify(configFileVariableMap);
	
	// Validate everything
	spep::ConfigurationReader configuration( configFileVariableMap );
	
	if( configuration.isValid() )
	{
		if( verbose )
		{
			std::cout << "Validated configuration OK" << std::endl;
		}
	}
	else
	{
		std::cerr << "Error validating configuration" << std::endl;
		exit(2);
	}
	
	// Make preparations to become a daemon - still need to do this even for debug mode.
	spep::daemon::Daemon::prepare();
	
	// Then become a daemon, unless we're in debug mode
	if( !commandLineVariableMap.count("debug") )
	{
		if( verbose )
		{
			std::cout << "About to become a daemon." << std::endl;
		}
		
		spep::daemon::Daemon::daemonize();
	}
	
	std::auto_ptr<spep::daemon::StreamLogHandler> logHandler;
	std::auto_ptr<std::ostream> stream;
	std::vector<spep::Handler*> handlers;
	
	if( commandLineVariableMap.count("log-file") )
	{
		std::string filename( commandLineVariableMap["log-file"].as<std::string>() );
		stream.reset( new std::ofstream( filename.c_str() ) );
		if( ! stream->good() )
		{
			std::cerr << "Couldn't open output stream for " << filename << std::endl;
			exit(3);
		}
		
		// TODO This should be configurable.
		logHandler.reset( new spep::daemon::StreamLogHandler( *stream, spep::INFO ) );
		handlers.push_back( logHandler.get() );
	}
	
	std::auto_ptr<spep::daemon::StreamLogHandler> debugHandler;
	if( commandLineVariableMap.count("debug") )
	{
		debugHandler.reset( new spep::daemon::StreamLogHandler( std::cout, spep::DEBUG ) );
		handlers.push_back( debugHandler.get() );
	}
	
	std::auto_ptr<spep::SPEP> spep( spep::SPEP::initializeServer( configuration, handlers ) );
	
	std::vector<spep::ipc::Dispatcher*> dispatcherList;
	
	spep::ipc::ConfigurationDispatcher configurationDispatcher( spep->getConfiguration() );
	dispatcherList.push_back( &configurationDispatcher );
	spep::ipc::MetadataDispatcher metadataDispatcher( spep->getMetadata() );
	dispatcherList.push_back( &metadataDispatcher );
	spep::ipc::SessionCacheDispatcher sessionCacheDispatcher( spep->getSessionCache() );
	dispatcherList.push_back( &sessionCacheDispatcher );
	spep::ipc::SessionGroupCacheDispatcher sessionGroupCacheDispatcher( spep->getSessionGroupCache() );
	dispatcherList.push_back( &sessionGroupCacheDispatcher );
	spep::ipc::StartupProcessorDispatcher startupProcessorDispatcher( spep->getStartupProcessor() );
	dispatcherList.push_back( &startupProcessorDispatcher );
	spep::ipc::IdentifierCacheDispatcher identifierCacheDispatcher( spep->getIdentifierCache() );
	dispatcherList.push_back( &identifierCacheDispatcher );
	
	spep::ipc::MultifacetedDispatcher multiDispatcher( spep->getReportingProcessor(), dispatcherList );
	
	spep::ipc::ExceptionCatchingDispatcher dispatcher( spep->getReportingProcessor(), &multiDispatcher );
	
	int port = configuration.getIntegerValue( CONFIGURATION_SPEPDAEMONPORT );
	spep::ipc::ServerSocket<spep::ipc::ExceptionCatchingDispatcher> serverSocket( dispatcher, port );
	serverSocket.listen();
}

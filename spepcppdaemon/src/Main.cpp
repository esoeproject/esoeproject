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

#define _CRTDBG_MAP_ALLOC
#include <stdlib.h>
#include <crtdbg.h>

#include "Daemon.h"
#include "StreamLogHandler.h"

#include "saml2/logging/api.h"

#include "spep/config/ConfigurationReader.h"
#include "spep/SPEP.h"
#include "spep/Util.h"
#include "spep/UnicodeStringConversion.h"

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
#include "spep/logging/proxy/LoggerDispatcher.h"

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



    
bool SPEPDaemonIsRunning = true;

void doInit(spep::ConfigurationReader &configuration, std::vector<saml2::Handler*> &handlers, bool verbose, bool debug);

// Dirty, but it will let us log errors before the logging system is initialized properly.
// Hopefully this will disappear when there is a better logging library in place.
void directLog(const std::vector<saml2::Handler*> &handlers, const std::string& message)
{
    for (auto iter: handlers)
    {
        iter->log(saml2::ERROR, std::string("Startup"), message);
    }
}

void parseConfig(boost::program_options::variables_map& configFileVariableMap, std::istream& configFileInput, const std::vector<saml2::Handler*>& handlers)
{
    // Set up the configuration parameters.
    boost::program_options::options_description configDescription("spep configuration options");
    spep::ConfigurationReader::addOptions(configDescription);

    try
    {
        // Parse and store it in the variable map
        boost::program_options::store(
            boost::program_options::parse_config_file(configFileInput, configDescription),
            configFileVariableMap
            );
    }
    catch (boost::program_options::ambiguous_option& ex)
    {
        std::cerr << "Ambiguous option while reading the configuration. Error was: " << ex.what() << std::endl;
        directLog(handlers, "Ambiguous option while reading the configuration.");
        directLog(handlers, std::string("Error was: ") + ex.what());
        exit(2);
    }
    catch (boost::program_options::invalid_syntax& ex)
    {
        std::cerr << "Invalid syntax while reading the configuration. Error was: " << ex.what() << std::endl;
        directLog(handlers, "Invalid syntax while reading the configuration.");
        directLog(handlers, std::string("Error was: ") + ex.what());
        exit(2);
    }
    catch (boost::program_options::unknown_option& ex)
    {
        std::cerr << "Unknown option while reading the configuration. Error was: " << ex.what() << std::endl;
        directLog(handlers, "Unknown option while reading the configuration.");
        directLog(handlers, std::string("Error was: ") + ex.what());
        exit(2);
    }
    catch (boost::program_options::error& ex)
    {
        std::cerr << "Unexpected error occurred reading the configuration." << std::endl;
        directLog(handlers, "Unexpected error occurred reading the configuration.");
        exit(2);
    }
    catch (std::exception& ex)
    {
        std::cerr << "Error occurred reading the configuration. Error was: " << ex.what() << std::endl;
        directLog(handlers, "Error occurred reading the configuration.");
        directLog(handlers, std::string("Error was: ") + ex.what());
        exit(2);
    }
}

#ifdef WIN32
#define WIN32_LEAN_AND_MEAN
// Include winsock2 before windows so it doesn't screw with our socket stuff
#include <winsock2.h>
#include <windows.h>
#include <winsvc.h>

static const std::string REGISTRY_KEY_SOFTWARE{ "Software" };
static const std::string REGISTRY_KEY_ESOEPROJECT{ "ESOE Project" };
static const std::string REGISTRY_KEY_SPEP{ "SPEP" };

#define		SPEPSERVICE_NAME			"SPEP Service"
#define		SPEPSERVICE_DESCRIPTION		"Handles the caching of data for SPEP protected services."

void WINAPI ServiceMain(DWORD argc, LPSTR *argv);


struct SPEPService
{
    static bool isInited;
    static bool debug;
    static SERVICE_STATUS serviceStatus;
    static SERVICE_STATUS_HANDLE serviceStatusHandle;

    static void WINAPI ServiceControlHandler(DWORD code)
    {
        if (code == SERVICE_CONTROL_STOP)
        {
            serviceStatus.dwWin32ExitCode = NO_ERROR;
            updateStatus(SERVICE_STOPPED);
            SPEPDaemonIsRunning = false;
        }
    }

    static void updateStatus(DWORD status)
    {
        if (!isInited)
        {
            serviceStatusHandle = RegisterServiceCtrlHandler(SPEPSERVICE_NAME, ServiceControlHandler);
            isInited = true;
        }

        serviceStatus.dwCurrentState = status;
        SetServiceStatus(serviceStatusHandle, &serviceStatus);
    }
};

bool SPEPService::isInited = false;
bool SPEPService::debug = false;
SERVICE_STATUS SPEPService::serviceStatus;
SERVICE_STATUS_HANDLE SPEPService::serviceStatusHandle;

class ServiceController
{
private:
    SC_HANDLE _scManager;
public:
    ServiceController()
    {
        _scManager = OpenSCManager(NULL, NULL, SC_MANAGER_ALL_ACCESS);
    }

    bool registerService(const std::string& serviceName, const std::string& description, const std::string& filename)
    {
        if (_scManager == NULL) return false;

        SC_HANDLE service = CreateService(_scManager,
            serviceName.c_str(),
            description.c_str(),
            SERVICE_ALL_ACCESS,
            SERVICE_WIN32_OWN_PROCESS,
            SERVICE_AUTO_START,
            SERVICE_ERROR_NORMAL,
            filename.c_str(),
            NULL,
            NULL,
            NULL,
            NULL,
            NULL);

        return(service != NULL);
    }

    bool unregisterService(const std::string& serviceName)
    {
        if (_scManager == NULL) return false;

        SC_HANDLE service = OpenService(_scManager, serviceName.c_str(), SERVICE_ALL_ACCESS);

        if (service == NULL) return false;

        return DeleteService(service) != 0;
    }
};


/*
 * This is only used locally, that's why it's defined in the .cpp file
 */
class RegistryKey
{
private:
    HKEY _hKey;
    LONG _result;

public:
    RegistryKey(HKEY parent, const char *name, REGSAM samDesired = KEY_READ | KEY_WOW64_64KEY)
    {
        this->_result = RegOpenKeyEx(parent, name, 0, samDesired, &(this->_hKey));
    }

    RegistryKey(const RegistryKey& parent, const char *name, REGSAM samDesired = KEY_READ | KEY_WOW64_64KEY)
    {
        this->_result = RegOpenKeyEx(parent._hKey, name, 0, samDesired, &(this->_hKey));
    }

    ~RegistryKey()
    {
        if (this->valid())
        {
            RegCloseKey(this->_hKey);
        }
    }

    bool valid()
    {
        // Yeah, I laughed too.
        return (this->_result == ERROR_SUCCESS);
    }

    std::string queryValueString(const char *name, DWORD keyType = REG_SZ)
    {
        DWORD keyDataLength = 0;
        DWORD keyTypeParam = keyType;

        // One call to get the size
        RegQueryValueEx(this->_hKey, name, NULL, &keyTypeParam, NULL, &keyDataLength);
        spep::CArray<char> keyData(keyDataLength + 1);

        keyTypeParam = keyType;

        // One call to get the value
        RegQueryValueEx(this->_hKey, name, NULL, &keyTypeParam, reinterpret_cast<PBYTE>(keyData.get()), &keyDataLength);
        keyData[keyDataLength] = '\0';

        return std::string(keyData.get(), keyDataLength);
    }
};

int main(int argc, char** argv)
{
    if (argc > 1)
    {
        ServiceController sc;

        std::string arg(argv[1]);
        if (arg.compare("-i") == 0)
        {
            std::cout << "Going to install service... ";

            TCHAR path[MAX_PATH];
            GetModuleFileName(NULL, path, sizeof(path));

            if (sc.registerService(SPEPSERVICE_NAME, SPEPSERVICE_NAME, path))
            {
                std::cout << "done." << std::endl;
            }
            else
            {
                std::cout << "FAILED." << std::endl;
            }
        }
        else if (arg.compare("-u") == 0)
        {
            std::cout << "Going to uninstall service... ";
            if (sc.unregisterService(SPEPSERVICE_NAME))
            {
                std::cout << "done." << std::endl;
            }
            else
            {
                std::cout << "FAILED." << std::endl;
            }
        }
        else if (arg.compare("-x") == 0)
        {
            SPEPService::debug = true;
            ServiceMain(argc, argv);
        }
        else
        {
            std::cout << "Usage: " << argv[0] << " [-i | -u | -x]" << std::endl;
            std::cout << "  -i   Install service" << std::endl;
            std::cout << "  -u   Uninstall service" << std::endl;
            std::cout << "  -x   Debug service" << std::endl;
            return 1;
        }

        return 0;
    }

    SERVICE_TABLE_ENTRY serviceTable[] = {
            { "SPEP Service", &ServiceMain },
            { NULL, NULL }
    };

    StartServiceCtrlDispatcher(serviceTable);

    _CrtDumpMemoryLeaks();
}

void WINAPI ServiceMain(DWORD argc, LPSTR *argv)
{
    std::memset(&SPEPService::serviceStatus, 0, sizeof(SERVICE_STATUS));
    SPEPService::serviceStatus.dwServiceType = SERVICE_WIN32;
    SPEPService::serviceStatus.dwControlsAccepted = SERVICE_ACCEPT_STOP;

    SPEPService::updateStatus(SERVICE_START_PENDING);

    std::vector<saml2::Handler*> handlers;

    std::auto_ptr<spep::daemon::StreamLogHandler> logHandler;
    std::auto_ptr<spep::daemon::StreamLogHandler> debugHandler;
    std::auto_ptr<std::ostream> stream;

    std::string logFilename;

    boost::program_options::variables_map configFileVariableMap;
    bool readConfig = false;

    // Open the registry to get the filename
    RegistryKey rKeySoftware{ HKEY_LOCAL_MACHINE, REGISTRY_KEY_SOFTWARE.c_str(), KEY_ENUMERATE_SUB_KEYS | KEY_WOW64_64KEY };
    if (rKeySoftware.valid())
    {
        RegistryKey rKeyESOEProject{ rKeySoftware, REGISTRY_KEY_ESOEPROJECT.c_str(), KEY_ENUMERATE_SUB_KEYS | KEY_WOW64_64KEY };
        if (rKeyESOEProject.valid())
        {
            RegistryKey rKeySPEP{ rKeyESOEProject, REGISTRY_KEY_SPEP.c_str(), KEY_READ | KEY_WOW64_64KEY };
            if (rKeySPEP.valid())
            {
                std::string configFilename(rKeySPEP.queryValueString("ConfigFile"));
                logFilename = (rKeySPEP.queryValueString("LogFile"));
                std::string logLevelStr(rKeySPEP.queryValueString("LogLevel"));

                saml2::LogLevel logLevel = saml2::INFO;
                if (!logLevelStr.empty() && logLevelStr == "DEBUG")
                    logLevel = saml2::DEBUG;

                if (!logFilename.empty())
                {
                    stream.reset(new std::ofstream(logFilename.c_str(), std::ios_base::out | std::ios_base::app));
                    logHandler.reset(new spep::daemon::StreamLogHandler(*stream, logLevel));

                    if (SPEPService::debug) {
                        debugHandler.reset(new spep::daemon::StreamLogHandler(std::cout, saml2::DEBUG));
                        handlers.push_back(debugHandler.get());
                    }

                    handlers.push_back(logHandler.get());
                }

                // Read the file
                std::ifstream configFileInput(configFilename.c_str());

                if (configFileInput.good())
                {
                    parseConfig(configFileVariableMap, configFileInput, handlers);

                    readConfig = true;
                }
                else
                {
                    directLog(handlers, "The config file specified in the registry key doesn't exist.");
                    return;
                }
            }
            else
            {
                directLog(handlers, "Couldn't open HKEY_LOCAL_MACHINE\Software\ESOE Project\SPEP");
                return;
            }
        }
        else
        {
            directLog(handlers, "Couldn't open HKEY_LOCAL_MACHINE\Software\ESOE Project");
            return;
        }
    }
    else
    {
        directLog(handlers, "Couldn't open HKEY_LOCAL_MACHINE\Software");
        return;
    }

    // Config wasn't read
    if (!readConfig)
    {
        directLog(handlers, "No config file could be read. Terminating.");
        return;
    }

    // Initialize curl. This is needed on win32 platforms to make sure sockets function properly.
    curl_global_init(CURL_GLOBAL_ALL);

    // Get configuration
    spep::ConfigurationReader configReader(configFileVariableMap);

    SPEPService::updateStatus(SERVICE_RUNNING);
    doInit(configReader, handlers, false, false);
}

#else //WIN32

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
        ( "pid-file,p", boost::program_options::value< std::vector<std::string> >(), "file to store pid when starting" )
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

    bool verbose = (commandLineVariableMap.count("verbose") != 0);
    bool debug = (commandLineVariableMap.count("debug") != 0);

    std::vector<saml2::Handler*> handlers;

    std::auto_ptr<spep::daemon::StreamLogHandler> logHandler;
    std::auto_ptr<std::ostream> stream;

    if( commandLineVariableMap.count("log-file") )
    {
        std::string filename( commandLineVariableMap["log-file"].as<std::string>() );
        stream.reset( new std::ofstream( filename.c_str(), std::ios_base::out | std::ios_base::app ) );
        if( ! stream->good() )
        {
            std::cerr << "Couldn't open output stream for " << filename << std::endl;
            exit(3);
        }

        // TODO This should be configurable.
        logHandler.reset( new spep::daemon::StreamLogHandler( *stream, saml2::INFO ) );
        handlers.push_back( logHandler.get() );
    }

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

        parseConfig( configFileVariableMap, configFileInput, handlers );

        // File will be closed when configFileInput object is deleted
    }

    if( commandLineVariableMap.count("pid-file") > 0 )
    {
        const std::vector<std::string> &pidFileList = commandLineVariableMap["pid-file"].as< std::vector<std::string> >();
        // Loop through every pid file specified.
        for( std::vector<std::string>::const_iterator pidFileIterator = pidFileList.begin(); pidFileIterator != pidFileList.end(); ++pidFileIterator )
        {
            if( verbose )
            {
                std::cout << "Delayed writing PID to " << *pidFileIterator << " until after fork." << std::endl << std::flush;
            }

            spep::daemon::Daemon::pidFileList.push_back( *pidFileIterator );
        }
    }

    if( verbose )
    {
        std::cout << "Finished reading configuration." << std::endl << std::flush;
    }

    boost::program_options::notify(configFileVariableMap);

    // Validate everything
    spep::ConfigurationReader configuration( configFileVariableMap );

    doInit( configuration, handlers, verbose, debug );
}

#endif //WIN32


void doInit(spep::ConfigurationReader &configuration, std::vector<saml2::Handler*> &handlers, bool verbose, bool debug)
{
    if (configuration.isValid())
    {
        if (verbose)
        {
            std::cout << "Validated configuration OK" << std::endl;
        }
    }
    else
    {
        std::cerr << "Error validating configuration" << std::endl;
        directLog(handlers, "Error occurred during startup. Config was invalid.");

        exit(2);
    }

    // Make preparations to become a daemon - still need to do this even for debug mode.
    spep::daemon::Daemon::prepare();

    // Then become a daemon, unless we're in debug mode
    if (!debug)
    {
        if (verbose)
        {
            std::cout << "About to become a daemon." << std::endl;
        }

        spep::daemon::Daemon::daemonize();
    }

    std::auto_ptr<spep::daemon::StreamLogHandler> debugHandler;
    if (debug)
    {
        debugHandler.reset(new spep::daemon::StreamLogHandler(std::cout, saml2::DEBUG));
        handlers.push_back(debugHandler.get());
    }

    std::auto_ptr<spep::SPEP> spep;
    try
    {
        spep.reset(spep::SPEP::initializeServer(configuration, handlers));
    }
    catch (std::exception& ex)
    {
        std::cout << "Error occurred during startup. Error was: " << ex.what() << std::endl;
        directLog(handlers, "Error occurred during startup.");
        directLog(handlers, std::string("Error encountered: ") + ex.what());

        return;
    }

    std::vector<spep::ipc::Dispatcher*> dispatcherList;

    spep::ipc::ConfigurationDispatcher configurationDispatcher(spep->getConfiguration());
    dispatcherList.push_back(&configurationDispatcher);
    spep::ipc::MetadataDispatcher metadataDispatcher(spep->getMetadata());
    dispatcherList.push_back(&metadataDispatcher);
    spep::ipc::SessionCacheDispatcher sessionCacheDispatcher(spep->getSessionCache());
    dispatcherList.push_back(&sessionCacheDispatcher);
    spep::ipc::SessionGroupCacheDispatcher sessionGroupCacheDispatcher(spep->getSessionGroupCache());
    dispatcherList.push_back(&sessionGroupCacheDispatcher);
    spep::ipc::StartupProcessorDispatcher startupProcessorDispatcher(spep->getStartupProcessor());
    dispatcherList.push_back(&startupProcessorDispatcher);
    spep::ipc::IdentifierCacheDispatcher identifierCacheDispatcher(spep->getIdentifierCache());
    dispatcherList.push_back(&identifierCacheDispatcher);
    spep::ipc::LoggerDispatcher loggerDispatcher(spep->getLogger());
    dispatcherList.push_back(&loggerDispatcher);

    spep::ipc::MultifacetedDispatcher multiDispatcher(spep->getLogger(), dispatcherList);

    spep::ipc::ExceptionCatchingDispatcher dispatcher(spep->getLogger(), &multiDispatcher);

    int port = configuration.getIntegerValue(CONFIGURATION_SPEPDAEMONPORT);
    spep::ipc::ServerSocket<spep::ipc::ExceptionCatchingDispatcher> serverSocket(spep->getLogger(), &dispatcher, port);
    serverSocket.listen(&SPEPDaemonIsRunning);
}

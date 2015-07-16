/* Copyright 2015, Queensland University of Technology
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
* Author: Andrew McGregor
* Creation Date: June 10, 2015
*
*/

#include <string>
#include <boost/program_options/config.hpp>
#include <boost/program_options/parsers.hpp>
#include <boost/program_options/variables_map.hpp>

#include "SPEPModule.h"
#include "SPEPExtension.h"
#include "RegistryKey.h"
#include "spep/exceptions/InvalidStateException.h"


#define	SPEP_ISAPI_EXTENSION_DESCRIPTION  "SPEP ISAPI v1.0"
#define	REGISTRY_KEY_SOFTWARE 		"Software"
#define	REGISTRY_KEY_ESOEPROJECT 	"ESOE Project"
#define	REGISTRY_KEY_SPEP 			"SPEP"


namespace spep {
namespace isapi {

static SPEPExtension *extensionInstance = nullptr;
static HANDLE EVENT_LOG = nullptr;


// Define a method that writes to the Event Viewer.
BOOL WriteEventViewerLog(LPCSTR szNotification) {
	// Test whether the handle for the Event Viewer is open.
	if (nullptr != EVENT_LOG)
	{
		// Write any strings to the Event Viewer and return.
		return ReportEvent(EVENT_LOG, EVENTLOG_INFORMATION_TYPE, 0, 0, NULL, 1, 0, &szNotification, NULL);
	}
	return FALSE;
}


void RegisterEventLog() {
	// Open a handle to the Event Viewer.
	EVENT_LOG = RegisterEventSource(NULL, "IISADMIN");
}

void DeregisterEventLog() {
	// Test whether the handle for the Event Viewer is open.
	if (nullptr != EVENT_LOG)
	{
		// Close the handle to the Event Viewer.
		DeregisterEventSource(EVENT_LOG);
		EVENT_LOG = nullptr;
	}
}


bool InitialiseSPEPModule() {
	
	WriteEventViewerLog("Initialising SPEP Module");

	std::string logFilename;

	boost::program_options::variables_map configFileVariableMap;
	bool readConfig = false;

	// Open the registry to get the filename
	RegistryKey rKeySoftware(HKEY_LOCAL_MACHINE, REGISTRY_KEY_SOFTWARE, KEY_ENUMERATE_SUB_KEYS);
	if (rKeySoftware.valid())
	{
		RegistryKey rKeyESOEProject(rKeySoftware, REGISTRY_KEY_ESOEPROJECT, KEY_ENUMERATE_SUB_KEYS);
		if (rKeyESOEProject.valid())
		{
			RegistryKey rKeySPEP(rKeyESOEProject, REGISTRY_KEY_SPEP, KEY_READ);
			if (rKeySPEP.valid())
			{
				std::string configFilename(rKeySPEP.queryValueString("ConfigFile"));
				logFilename = (rKeySPEP.queryValueString("LogFile"));

				// Read the file
				std::ifstream configFileInput(configFilename.c_str());

				if (configFileInput.good())
				{
					// Set up the configuration parameters.
					boost::program_options::options_description configDescription("spep configuration options");
					spep::ConfigurationReader::addOptions(configDescription);

					// Parse and store it in the variable map
					boost::program_options::store(
						boost::program_options::parse_config_file(configFileInput, configDescription),
						configFileVariableMap
						);

					readConfig = true;
				}
				else
				{
					// Yeah, hilarious.. I know.
					// This is if the config file specified in the registry key doesn't exist.
					SetLastError(ERROR_OUT_OF_PAPER);
					return false;
				}
			}
			else
			{
				// Couldn't open HKEY_LOCAL_MACHINE\Software\ESOE Project\SPEP
				SetLastError(ERROR_CANTOPEN);
				return false;
			}
		}
		else
		{
			// Couldn't open HKEY_LOCAL_MACHINE\Software\ESOE Project
			SetLastError(ERROR_CANTOPEN);
			return false;
		}
	}
	else
	{
		// Couldn't open HKEY_LOCAL_MACHINE\Software
		SetLastError(ERROR_CANTOPEN);
		return false;
	}

	// Config wasn't read
	if (!readConfig)
	{
		SetLastError(ERROR_BADKEY);
		return false;
	}

	try
	{
		// Initialize curl. This is needed on win32 platforms to make sure sockets function properly.
		curl_global_init(CURL_GLOBAL_ALL);

		// Get configuration
		spep::ConfigurationReader configReader(configFileVariableMap);

		// Establish the SPEP instance.
		WriteEventViewerLog("Creating SPEP Module Instance");
		extensionInstance = new spep::isapi::SPEPExtension(configReader, logFilename);
		WriteEventViewerLog("SPEP Module Created");
	}
	catch (...)
	{
		WriteEventViewerLog("Exception occurred creating SPEP Module");
	}

	return true;
}



REQUEST_NOTIFICATION_STATUS SPEPModule::OnBeginRequest(IHttpContext* pHttpContext, IHttpEventProvider* pProvider)
{
	UNREFERENCED_PARAMETER(pProvider);

	if (!extensionInstance) {
		WriteEventViewerLog("SPEP Extension Instance has not been initialised");
		return RQ_NOTIFICATION_FINISH_REQUEST;
	}

	try {
		HttpRequest request(pHttpContext);
		auto status = extensionInstance->processRequest(&request);

		switch (status)
		{
		case RequestResultStatus::STATUS_ERROR:
			return RQ_NOTIFICATION_FINISH_REQUEST;
		case RequestResultStatus::STATUS_SUCCESS:
			return RQ_NOTIFICATION_CONTINUE;
		case RequestResultStatus::STATUS_SUCCESS_AND_FINISH_REQUEST:
			return RQ_NOTIFICATION_FINISH_REQUEST;
		case RequestResultStatus::STATUS_SUCCESS_AND_KEEP_CONN:
			return RQ_NOTIFICATION_CONTINUE;
		default:
			return RQ_NOTIFICATION_FINISH_REQUEST;
		}
	}
	catch (InvalidStateException& e) {
		// TODO: set error code
		return RQ_NOTIFICATION_FINISH_REQUEST;
	}

	// Return processing to the pipeline.
	return RQ_NOTIFICATION_CONTINUE;
}

}
}
/* Copyright 2006, Queensland University of Technology
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
 * Creation Date: Oct 25, 2007
 * 
 * Purpose: 
 */

#include <fstream>

#include <winsock2.h>
#include <windows.h>
#include <winreg.h>

#include <boost/program_options/config.hpp>
#include <boost/program_options/parsers.hpp>
#include <boost/program_options/variables_map.hpp>

#include "ISAPI.h"
#include "SPEPExtension.h"

#include "spep/Util.h"


#define 	SPEP_ISAPI_EXTENSION_DESCRIPTION  "SPEP ISAPI v1.0"

#define 	REGISTRY_KEY_SOFTWARE 		"Software"
#define 	REGISTRY_KEY_ESOEPROJECT 	"ESOE Project"
#define 	REGISTRY_KEY_SPEP 			"SPEP"

namespace spep {
    namespace isapi {
        static SPEPExtension *extensionInstance = nullptr;

        /*
         * This is only used locally, that's why it's defined in the .cpp file
         */
        class RegistryKey
        {
        private:
            HKEY _hKey;
            LONG _result;

        public:
            RegistryKey(HKEY parent, const char *name, REGSAM samDesired = KEY_READ)
            {
                this->_result = RegOpenKeyEx(parent, name, 0, samDesired, &(this->_hKey));
            }

            RegistryKey(const RegistryKey& parent, const char *name, REGSAM samDesired = KEY_READ)
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
    }
}

using namespace spep::isapi;

extern "C"
BOOL WINAPI GetExtensionVersion(
OUT HSE_VERSION_INFO *pVer
)
{
    pVer->dwExtensionVersion = MAKELONG(HSE_VERSION_MINOR, HSE_VERSION_MAJOR);

    lstrcpyn(pVer->lpszExtensionDesc, SPEP_ISAPI_EXTENSION_DESCRIPTION, HSE_MAX_EXT_DLL_NAME_LEN - 1);

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
        spep::isapi::extensionInstance = new spep::isapi::SPEPExtension(configReader, logFilename);
    }
    catch (...)
    {
    }

    return true;
}

extern "C"
DWORD WINAPI HttpExtensionProc(
IN EXTENSION_CONTROL_BLOCK *pECB
)
{
    spep::isapi::ISAPIRequestImpl request(pECB);
    return spep::isapi::extensionInstance->processRequest(&request);
}

extern "C"
BOOL WINAPI TerminateExtension(
IN DWORD dwFlags
)
{
    delete spep::isapi::extensionInstance;
    spep::isapi::extensionInstance = nullptr;

    return true;
}

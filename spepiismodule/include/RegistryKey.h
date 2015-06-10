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

#include <windows.h>
#include <winreg.h>
#include <string>

#include "spep/Util.h"

namespace spep {
namespace isapi {

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

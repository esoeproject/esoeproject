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

#define _WINSOCKAPI_
#include <windows.h>
#include <sal.h>
#include <httpserv.h>
#include "HttpRequest.h"

namespace spep {
namespace isapi {

BOOL WriteEventViewerLog(LPCSTR szNotification);
void RegisterEventLog();
void DeregisterEventLog();
bool InitialiseSPEPModule();


// Create the module class.
class SPEPModule : public CHttpModule
{
public:

	//virtual ~SPEPModule() {}

	virtual REQUEST_NOTIFICATION_STATUS OnBeginRequest(IHttpContext* pHttpContext, IHttpEventProvider* pProvider) override;
};

// Create the module's class factory.
class SPEPModuleFactory : public IHttpModuleFactory
{
public:
	HRESULT	GetHttpModule(OUT CHttpModule ** ppModule, IN IModuleAllocator * pAllocator)
	{
		UNREFERENCED_PARAMETER(pAllocator);

		// Create a new instance.
		SPEPModule * pModule = new SPEPModule;
		
		// Test for an error.
		if (!pModule)
		{
			// Return an error if the factory cannot create the instance.
			return HRESULT_FROM_WIN32(ERROR_NOT_ENOUGH_MEMORY);
		}
		else
		{
			// Return a pointer to the module.
			*ppModule = pModule;
			pModule = NULL;
			// Return a success status.
			return S_OK;
		}
	}

	void Terminate()
	{
		// Remove the class from memory.
		delete this;
	}
};

}
}


extern "C" {
// Create the module's exported registration function.
__declspec(dllexport) HRESULT __stdcall RegisterModule(DWORD dwServerVersion, IHttpModuleRegistrationInfo * pModuleInfo, IHttpServer * pGlobalInfo)
{
	UNREFERENCED_PARAMETER(dwServerVersion);
	UNREFERENCED_PARAMETER(pGlobalInfo);
	
	spep::isapi::RegisterEventLog();

	auto configLoaded = spep::isapi::InitialiseSPEPModule();

	if (!configLoaded) {
		spep::isapi::WriteEventViewerLog("Error Initialising SPEP Module");
		return RQ_NOTIFICATION_FINISH_REQUEST;
	}

	// Set the request notifications and exit.
	return pModuleInfo->SetRequestNotifications(new spep::isapi::SPEPModuleFactory, RQ_BEGIN_REQUEST, 0);
}
}
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

#ifndef ISAPI_H_
#define ISAPI_H_

/* We won't be using any winsock 1.0 stuff, so we bring in winsock2.h
 * before windows.h so we don't break other modules. */
#include <winsock2.h>
#include <windows.h>

#include <httpext.h>


#define 	SPEP_ISAPI_EXTENSION_DESCRIPTION  "SPEP ISAPI v1.0"

#define 	REGISTRY_KEY_SOFTWARE 		"Software"
#define 	REGISTRY_KEY_ESOEPROJECT 	"ESOE Project"
#define 	REGISTRY_KEY_SPEP 			"SPEP"

extern "C"
BOOL WINAPI GetExtensionVersion(
		OUT HSE_VERSION_INFO *pVer
);

extern "C"
DWORD WINAPI HttpExtensionProc(
		IN EXTENSION_CONTROL_BLOCK *pECB
);

extern "C"
BOOL WINAPI TerminateExtension(
		IN DWORD dwFlags
);

#endif /*ISAPI_H_*/

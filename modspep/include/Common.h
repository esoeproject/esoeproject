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
 * Creation Date: Jul 4, 2007
 * 
 * Purpose: 
 */

#ifndef COMMON_H_
#define COMMON_H_

#include "spep/SPEP.h"
#include "spep/reporting/Handler.h"

#include "ModuleConstants.h"

#include "APRDefinitions.h"

#if defined(WIN32) && defined(_MSC_VER)

#ifdef BUILDING_SPEP
#define MODSPEPEXPORT __declspec(dllexport)
#define MODSPEPCONSTANT __declspec(dllexport) extern
#else
#define MODSPEPEXPORT __declspec(dllimport)
#define MODSPEPCONSTANT __declspec(dllimport) extern
#endif /*BUILDING_SPEP*/

// VC++ (or more accurately, the windows platform SDK) doesn't define snprintf.. 
// instead it defines _snprintf
#define snprintf _snprintf

// Also, it complains about half the functions not being safe... so
// we flag them not to be deprecated.
#define _CRT_SECURE_NO_DEPRECATE 1

// Include this ASAP so other libraries including windows.h can't break us.
#include <winsock2.h>

// Bring this in for the class definition
#include <xercesc/dom/DOMDocument.hpp>

// Windows defines a DOMDocument type, so we need to get around that..
namespace spep
{
	typedef XERCES_CPP_NAMESPACE::DOMDocument DOMDocument;
}
#endif /*WIN32 && _MSC_VER */

#ifdef __GNUC__

#ifdef BUILDING_SPEP
#define MODSPEPEXPORT __attribute((visibility("default")))
#define MODSPEPCONSTANT
#else
#define MODSPEPEXPORT
#define MODSPEPCONSTANT
#endif /*BUILDING_SPEP*/

#endif

#ifndef MODSPEPEXPORT
// If nothing has been defined, we don't need any special flags to export symbols
#define MODSPEPEXPORT 
#define MODSPEPCONSTANT 
#endif /*MODSPEPEXPORT*/

// Not using this can cause a problem for some platforms.
#if defined(AP_HAVE_DESIGNATED_INITIALIZER)
#define CAST_CMD_FUNC(f) f
#else
#define CAST_CMD_FUNC(f) (cmd_func)f
#endif

/// Operations to check an SPEP "enabled" flag for various values
/*@{*/
#define SPEP_ENABLED(x) (((x)->enabled) > 0)
#define SPEP_DISABLED(x) (((x)->enabled) == 0)
#define SPEP_UNSPECIFIED_VALUE -1
#define SPEP_UNSPECIFIED(x) ((x)->enabled == SPEP_UNSPECIFIED_VALUE)
/*@}*/



// This header relies on some of above definitions, so we need to include it here.
#include "APRDefinitions.h"

/// Container struct for SPEP instance
struct SPEPInstance
{
	int port;
	spep::SPEP *spep;
	spep::Handler *logger;
	const char *spepBasePath;
	const char *spepSSOPath;
	const char *spepWebServices;
	const char *spepAuthzCacheClear;
	const char *spepSingleLogout;
	
	SPEPInstance();
};

/// Per-server config struct.
struct SPEPServerConfig
{
	apr_pool_t *serverPool;
	const char *logFilename;
	SPEPInstance *instance;
};

/// Per-directory config struct
struct SPEPDirectoryConfig
{
	int enabled;
};

extern "C" const char *parse_port_string( cmd_parms *parms, void *cfg, const char *value );
extern "C" const char *set_log_string( cmd_parms *parms, void* cfg, const char *value );
extern "C" const char *set_enabled_flag( cmd_parms *parms, void *cfg, int arg );
extern "C" const char *set_spep_base_path( cmd_parms *parms, void *cfg, const char *value );

spep::SPEP* init_spep_instance( SPEPServerConfig *serverConfig );

/**
 * create and merge methods for per-dir and per-server config
 */
/** @{ */
extern "C" void* modspep_create_dir_config( apr_pool_t *pool, char *str );
extern "C" void* modspep_merge_dir_config( apr_pool_t *pool, void *BASE, void *ADD );
extern "C" void* modspep_create_server_config( apr_pool_t *pool, server_rec *server );
extern "C" void* modspep_merge_server_config( apr_pool_t *pool, void *BASE, void *ADD );
/** @} */

// Return type differs between apache 1.3 and 2.x
#ifndef APACHE1
apr_status_t modspep_cleanup_config( void *data );
#else
void modspep_cleanup_config( void *data );
#endif

extern "C" void modspep_child_init( apr_pool_t *pchild, server_rec *s );
extern "C" int modspep_check_session( request_rec *req );
extern "C" int modspep_handler( request_rec *req );

#endif /*COMMON_H_*/

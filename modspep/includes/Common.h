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

#include "SPEP.h"
#include "reporting/Handler.h"

#include "ModuleConstants.h"

#include "APRDefinitions.h"

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
};

/// Per-server config struct.
struct SPEPServerConfig
{
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

void init_spep_instance( apr_pool_t *pool, SPEPServerConfig *serverConfig );

/**
 * create and merge methods for per-dir and per-server config
 */
/** @{ */
extern "C" void* modspep_create_dir_config( apr_pool_t *pool, char *str );
extern "C" void* modspep_merge_dir_config( apr_pool_t *pool, void *BASE, void *ADD );
extern "C" void* modspep_create_server_config( apr_pool_t *pool, server_rec *server );
extern "C" void* modspep_merge_server_config( apr_pool_t *pool, void *BASE, void *ADD );
/** @} */

extern "C" void modspep_child_init( apr_pool_t *pchild, server_rec *s );
extern "C" int modspep_check_session( request_rec *req );
extern "C" int modspep_handler( request_rec *req );

#endif /*COMMON_H_*/

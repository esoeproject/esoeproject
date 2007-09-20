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
 * Creation Date: 12/06/2007
 * 
 * Purpose: 
 */

// Module implementation for Apache 2.0

// Guard to stop compilation if we're making for Apache 1.3
#ifndef APACHE1


// Includes from apache
#include "httpd.h"
#include "http_config.h"
#include "http_core.h"
#include "http_log.h"
#include "http_main.h"
#include "http_protocol.h"
#include "http_request.h"
#include "util_script.h"
//#include "http_connection.h"

// Includes from APR
#include "APRDefinitions.h"

// Boost
#include <boost/lexical_cast.hpp>

// STL
#include <iostream>

// This project
#include "Common.h"


/**
 * Hook method prototypes
 */
/** @{ */
extern "C" int modspep_check_session( request_rec *req );
extern "C" int modspep_handler( request_rec *req );
extern "C" void modspep_child_init( apr_pool_t *pchild, server_rec *s );
/** @} */

extern "C" void modspep_register_hooks( apr_pool_t *pool )
{
	ap_hook_access_checker( modspep_check_session, NULL, NULL, APR_HOOK_FIRST );
	ap_hook_child_init( modspep_child_init, NULL, NULL, APR_HOOK_MIDDLE );
	ap_hook_handler( modspep_handler, NULL, NULL, APR_HOOK_FIRST );
}

extern "C" const command_rec modspep_cmds[] =
{
	AP_INIT_TAKE1(
		"SPEPDaemonPort",
		CAST_CMD_FUNC(parse_port_string),
		NULL,
		RSRC_CONF, // Allow in server config only
		"The port that the SPEP daemon is expected to be listening on the localhost address."
	),
	AP_INIT_FLAG(
		"SPEPEnabled",
		CAST_CMD_FUNC(set_enabled_flag),
		NULL,//(void*)APR_OFFSETOF(SPEPDirectoryConfig, enabled),
		ACCESS_CONF,
		"Defines whether or not the SPEP is enabled for this directory."
	),
	AP_INIT_TAKE1(
		"SPEPLogFile",
		CAST_CMD_FUNC(set_log_string),
		NULL,
		RSRC_CONF,
		"The file to log modspep.so output to."
	),
	{NULL}
};


extern "C"
{
module AP_MODULE_DECLARE_DATA spep_module =
{
	STANDARD20_MODULE_STUFF,
	modspep_create_dir_config,
	modspep_merge_dir_config,
	modspep_create_server_config,
	modspep_merge_server_config,
	modspep_cmds,
	modspep_register_hooks
};
}

/**
 * Hook method implementation.
 */
/** @{ */




/** @} */

#endif /*!APACHE1*/

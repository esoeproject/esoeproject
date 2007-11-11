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
 * Creation Date: Aug 29, 2007
 * 
 * Purpose: 
 */


#ifdef APACHE1

#include "Common.h"
#include "APRDefinitions.h"

#include "http_config.h"
#include "http_protocol.h"


#define SPEP_HANDLER_NAME "spep-handler"


#if MODULE_MAGIC_NUMBER < 19970719
#error The Apache1.3 version in use is too old to support the child_init hook.
#endif

extern "C" int modspep_type_checker( request_rec *req );
extern "C" void modspep_child_init_apache13( server_rec *s, apr_pool_t *pool );

extern "C" const handler_rec spep_handlers[] =
{
		{ SPEP_HANDLER_NAME, modspep_handler },
		{NULL}
};

extern "C" const command_rec modspep_cmds[] =
{
	{
		"SPEPDaemonPort",
		CAST_CMD_FUNC(parse_port_string),
		NULL,
		RSRC_CONF, // Allow in server config only
		TAKE1,
		"The port that the SPEP daemon is expected to be listening on the localhost address."
	},
	{
		"SPEPEnabled",
		CAST_CMD_FUNC(set_enabled_flag),
		NULL,
		ACCESS_CONF,
		FLAG,
		"Defines whether or not the SPEP is enabled for this directory."
	},
	{
		"SPEPLogFile",
		CAST_CMD_FUNC(set_log_string),
		NULL,
		RSRC_CONF,
		TAKE1,
		"The file to log modspep.so output to."
	},
	{
		"SPEPBasePath",
		CAST_CMD_FUNC(set_spep_base_path),
		NULL,
		RSRC_CONF,
		TAKE1,
		"The base path to accept requests for SPEP endpoints. (default: /spep)"
	},
	{NULL}
};

MODSPEPEXPORT module spep_module = { // was MODULE_VAR_EXPORT
		STANDARD_MODULE_STUFF,
		NULL,		/* initializer */
		modspep_create_dir_config,		/* create per-dir config */
		modspep_merge_dir_config,		/* merge per-dir config */
		modspep_create_server_config,		/* server config */
		modspep_merge_server_config,		/* merge server config */
		modspep_cmds,		/* command table */
		spep_handlers,		/* handlers */
		NULL,		/* filename translation */
		NULL,		/* check_user_id */
		NULL,		/* check auth */
		modspep_check_session,		/* check access */
		modspep_type_checker,		/* type_checker */
		NULL,		/* fixups */
		NULL,		/* logger */
#if MODULE_MAGIC_NUMBER >= 19970103
		NULL,		/* header parser */
#endif
#if MODULE_MAGIC_NUMBER >= 19970719
		modspep_child_init_apache13,		/* child_init */
#endif
#if MODULE_MAGIC_NUMBER >= 19970728	
		NULL,		/* child_exit */
#endif
#if MODULE_MAGIC_NUMBER >= 19970902
		NULL		/* post read-request */
#endif
};

extern "C" int modspep_type_checker( request_rec *req )
{
	SPEPServerConfig *serverConfig = (SPEPServerConfig*)ap_get_module_config( req->server->module_config, &spep_module );

	if( std::string(req->parsed_uri.path).compare( 0, strlen(serverConfig->instance->spepBasePath), serverConfig->instance->spepBasePath ) == 0 )
	{
		// This is one of ours! Handle it!
		req->handler = SPEP_HANDLER_NAME;
		
		return OK;
	}
	
	return DECLINED;
}

extern "C" void modspep_child_init_apache13( server_rec *s, apr_pool_t *pool )
{
	modspep_child_init( pool, s );
}


#endif /*APACHE1*/

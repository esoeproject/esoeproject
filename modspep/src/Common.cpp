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

#include "Common.h"

// This project
#include "SSOHandler.h"
#include "RequestHandler.h"
#include "WSHandler.h"
#include "Common.h"

// SPEP library
#include "spep/SPEP.h"


static std::vector<SPEPServerConfig*> global_SPEPServerConfigList;

SPEPInstance::SPEPInstance()
{
	this->port = 0;
	this->spep = NULL;
	this->spepBasePath = 		DEFAULT_URL_SPEP_WEBAPP;
	this->spepSSOPath = 		DEFAULT_URL_SPEP_WEBAPP DEFAULT_URL_SPEP_SSO;
	this->spepWebServices = 	DEFAULT_URL_SPEP_WEBAPP DEFAULT_URL_SPEP_WEBSERVICES;
	this->spepAuthzCacheClear = DEFAULT_URL_SPEP_WEBAPP DEFAULT_URL_SPEP_WEBSERVICES DEFAULT_URL_SPEP_AUTHZCACHECLEAR;
	this->spepSingleLogout = 	DEFAULT_URL_SPEP_WEBAPP DEFAULT_URL_SPEP_WEBSERVICES DEFAULT_URL_SPEP_SINGLELOGOUT;
}

extern "C"
{
	extern module spep_module;
}

extern "C" const char *parse_port_string( cmd_parms *parms, void* cfg, const char *value )
{
	SPEPServerConfig *config = (SPEPServerConfig*)ap_get_module_config( parms->server->module_config, &spep_module );

	if( config == NULL )
	{
		return "Got NULL for module_config struct.";
	}

	if ( value == NULL || *value == '\0' )
	{
		return "No value was present for the SPEP daemon port.";
	}

	try
	{
		int port = boost::lexical_cast<int>( value );

		if( config->instance == NULL )
		{
			config->instance = new SPEPInstance;
		}

		config->instance->port = port;

		config->instance->spep = NULL;

		global_SPEPServerConfigList.push_back( config );
		apr_pool_cleanup_register( config->serverPool, static_cast<void*>(config), modspep_cleanup_config, apr_pool_cleanup_null );

		return NULL;
	}
	catch( boost::bad_lexical_cast )
	{
		return "Invalid value specified for SPEP daemon port.";
	}
}

extern "C" const char *set_log_string( cmd_parms *parms, void* cfg, const char *value )
{
	// TODO Remove this option completely. Just deprecated for now.
	return NULL;
}

extern "C" const char *set_enabled_flag( cmd_parms *parms, void *cfg, int arg )
{
	SPEPDirectoryConfig *directoryConfig = (SPEPDirectoryConfig*)ap_get_module_config( parms->context, &spep_module );
	if( directoryConfig == NULL )
	{
		return "Got NULL for directoryConfig struct.";
	}

	directoryConfig->enabled = ( (arg != 0) ? 1 : 0 );

	if( !SPEP_ENABLED(directoryConfig)
		&& !SPEP_DISABLED(directoryConfig)
		&& !SPEP_UNSPECIFIED(directoryConfig) )
	{
		return "SPEP is neither enable, disabled nor unspecified. Something went wrong.";
	}

	return NULL;
}

extern "C" const char *set_spep_base_path( cmd_parms *parms, void *cfg, const char *value )
{
	SPEPServerConfig *config = (SPEPServerConfig*)ap_get_module_config( parms->server->module_config, &spep_module );

	if( config == NULL )
	{
		return "Got NULL for module_config struct.";
	}

	if ( value == NULL || *value == '\0' )
	{
		return "No value was present for the SPEP base path.";
	}

	// Stop stupid people putting / as their spep path
	if( *value != '/' || strlen(value) <= 1 )
	{
		return "SPEP base path must begin with / and must not be the root path";
	}

	if( config->instance == NULL )
	{
		config->instance = new SPEPInstance;
	}

	config->instance->spepBasePath = apr_pstrdup( parms->pool, value );
	config->instance->spepSSOPath = apr_psprintf( parms->pool, "%s%s", value, DEFAULT_URL_SPEP_SSO );
	config->instance->spepWebServices = apr_psprintf( parms->pool, "%s%s", value, DEFAULT_URL_SPEP_WEBSERVICES );
	config->instance->spepAuthzCacheClear = apr_psprintf( parms->pool, "%s%s", config->instance->spepWebServices, DEFAULT_URL_SPEP_AUTHZCACHECLEAR );
	config->instance->spepSingleLogout = apr_psprintf( parms->pool, "%s%s", config->instance->spepWebServices, DEFAULT_URL_SPEP_SINGLELOGOUT );

	return NULL;
}

spep::SPEP* init_spep_instance( SPEPServerConfig *serverConfig )
{
	// Allocate the file logger in the same pool as the server config for lifetime reasons.
	apr_pool_t *pool = serverConfig->serverPool;

	if( serverConfig->instance == NULL )
	{
		serverConfig->instance = new SPEPInstance;
	}

	if( serverConfig->instance->spep == NULL && serverConfig->instance->port != 0 )
	{
		serverConfig->instance->spep = spep::SPEP::initializeClient( serverConfig->instance->port );
	}

	return serverConfig->instance->spep;
}

extern "C" void* modspep_create_dir_config( apr_pool_t *pool, char *str )
{
	SPEPDirectoryConfig *config = (SPEPDirectoryConfig*)apr_pcalloc( pool, sizeof( SPEPDirectoryConfig ) );
	config->enabled = SPEP_UNSPECIFIED_VALUE;

	return config;
}

extern "C" void* modspep_merge_dir_config( apr_pool_t *pool, void *BASE, void *ADD )
{
	SPEPDirectoryConfig *base = (SPEPDirectoryConfig*)BASE;
	SPEPDirectoryConfig *add = (SPEPDirectoryConfig*)ADD;
	SPEPDirectoryConfig *result = (SPEPDirectoryConfig*)apr_pcalloc( pool, sizeof( SPEPDirectoryConfig ) );

	if( SPEP_UNSPECIFIED( add ) )
	{
		result->enabled = base->enabled;
	}
	else
	{
		result->enabled = add->enabled;
	}

	return result;
}

extern "C" void* modspep_create_server_config( apr_pool_t *pool, server_rec *server )
{
	SPEPServerConfig *config = (SPEPServerConfig*)apr_pcalloc( pool, sizeof( SPEPServerConfig ) );
	config->instance = NULL;
	config->serverPool = pool;

	return config;
}

extern "C" void* modspep_merge_server_config( apr_pool_t *pool, void *BASE, void *ADD )
{
	SPEPServerConfig *base = (SPEPServerConfig*)BASE;
	SPEPServerConfig *add = (SPEPServerConfig*)ADD;
	SPEPServerConfig *result = (SPEPServerConfig*)apr_pcalloc( pool, sizeof( SPEPServerConfig ) );
	result->serverPool = pool;

	if( add->instance == NULL )
	{
		result->instance = base->instance;
	}
	else
	{
		result->instance = add->instance;
	}

	return result;
}

// Return type differs between apache 1.3 and 2.x
#ifndef APACHE1
apr_status_t modspep_cleanup_config( void *data )
#else
void modspep_cleanup_config( void *data )
#endif
{
	for( std::vector<SPEPServerConfig*>::iterator iter = global_SPEPServerConfigList.begin();
		iter != global_SPEPServerConfigList.end(); /* increment in body */ )
	{
		if( *iter == static_cast<SPEPServerConfig*>(data) )
		{
			// Erase and assign the "next" element as the iterator position
			iter = global_SPEPServerConfigList.erase( iter );
		}
		else
		{
			// Need to increment here since we aren't doing it in the for loop
			++iter;
		}
	}

#ifndef APACHE1
	return APR_SUCCESS;
#endif
}

extern "C" void modspep_child_init( apr_pool_t *pchild, server_rec *s )
{
	/*SPEPServerConfig *config = (SPEPServerConfig*)ap_get_module_config( s->module_config, &spep_module );
	const char *sname = s->server_hostname;

	if( sname == NULL ) sname = "no hostname.";

	init_spep_instance( config );*/

	for( std::vector<SPEPServerConfig*>::iterator iter = global_SPEPServerConfigList.begin();
		iter != global_SPEPServerConfigList.end();
		++iter )
	{
		init_spep_instance( *iter );
	}

	global_SPEPServerConfigList.clear();
}

extern "C" int modspep_check_session( request_rec *req )
{
	SPEPServerConfig *serverConfig = (SPEPServerConfig*)ap_get_module_config( req->server->module_config, &spep_module );
	SPEPDirectoryConfig *directoryConfig = (SPEPDirectoryConfig*)ap_get_module_config( req->per_dir_config, &spep_module );

	if( serverConfig->instance == NULL )
	{
		// No SPEP configured at this virtual host
		return DECLINED;
	}

	// Check if it starts with the defined default spep webapp url
	if( req->parsed_uri.path != NULL &&
		std::string(req->parsed_uri.path).compare( 0, strlen(serverConfig->instance->spepBasePath), serverConfig->instance->spepBasePath ) == 0 )
	{
		return DECLINED;
	}

	if( SPEP_ENABLED( directoryConfig ) )
	{
		spep::SPEP* spep = init_spep_instance( serverConfig );

		if( spep == NULL )
		{
#ifndef APACHE1 // The following won't work on 1.3.x
			// SPEPEnabled On but no daemon port specified.
			ap_log_error( APLOG_MARK, APLOG_ALERT, APR_SUCCESS, req->server, "SPEPEnabled is set On with no daemon port specified. Can't continue." );
#endif //APACHE1

			return HTTP_INTERNAL_SERVER_ERROR;
		}

		spep::apache::RequestHandler handler( spep );
		return handler.handleRequest( req );
	}

	return DECLINED;
}

extern "C" int modspep_handler( request_rec *req )
{
	SPEPServerConfig *serverConfig = (SPEPServerConfig*)ap_get_module_config( req->server->module_config, &spep_module );
	//SPEPDirectoryConfig *directoryConfig = (SPEPDirectoryConfig*)ap_get_module_config( req->per_dir_config, &spep_module );

	if( serverConfig->instance == NULL )
	{
		// No SPEP configured at this virtual host
		return DECLINED;
	}

	// Check if it starts with the defined default spep webapp url
	if( std::string(req->parsed_uri.path).compare( 0, strlen(serverConfig->instance->spepBasePath), serverConfig->instance->spepBasePath ) == 0 )
	{
		spep::SPEP* spep = init_spep_instance( serverConfig );

		if( spep == NULL )
		{
			// No SPEP configured on this virtual host .. so we don't need to care.
			return DECLINED;
		}

		if( std::string(req->parsed_uri.path).compare( serverConfig->instance->spepSSOPath ) == 0 )
		{
			spep::apache::SSOHandler handler( spep );
			int result = handler.handleRequest( req );

			return result;
		}
		else if( std::string( req->parsed_uri.path ).compare( 0, strlen(serverConfig->instance->spepWebServices), serverConfig->instance->spepWebServices ) == 0 )
		{
			spep::apache::WSHandler handler( spep );
			int result = handler.handleRequest( req );

			return result;
		}

		return HTTP_NOT_FOUND;
	}

	return DECLINED;
}

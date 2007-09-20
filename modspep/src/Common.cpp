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
#include "APRFileLogger.h"

// This project
#include "SSOHandler.h"
#include "RequestHandler.h"
#include "WSHandler.h"
#include "APRFileLogger.h"
#include "Common.h"

// SPEP library
#include "SPEP.h"
#include "reporting/Handler.h"

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
		config->instance->logger = NULL;
		
		return NULL;
	}
	catch( boost::bad_lexical_cast )
	{
		return "Invalid value specified for SPEP daemon port.";
	}
}

extern "C" const char *set_log_string( cmd_parms *parms, void* cfg, const char *value )
{
	SPEPServerConfig *config = (SPEPServerConfig*)ap_get_module_config( parms->server->module_config, &spep_module );

	if( config == NULL )
	{
		return "Got NULL for module_config struct.";
	}
	
	if ( value == NULL || *value == '\0' )
	{
		return "No value was present for the SPEP log file.";
	}
	
	config->logFilename = apr_pstrdup( parms->pool, value );
	
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

void init_spep_instance( apr_pool_t *pool, SPEPServerConfig *serverConfig )
{
	if( serverConfig->instance->logger == NULL && serverConfig->logFilename != NULL )
	{
		// TODO This should be configurable
		serverConfig->instance->logger = new spep::apache::APRFileLogger( pool, serverConfig->logFilename, spep::INFO );
	}
	
	if( serverConfig->instance->spep == NULL )
	{
		std::vector<spep::Handler*> handlers;
		if( serverConfig->instance->logger != NULL )
		{
			handlers.push_back( serverConfig->instance->logger );
		}
		
		serverConfig->instance->spep = spep::SPEP::initializeClient( serverConfig->instance->port, handlers );
	}
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
	
	return config;
}

extern "C" void* modspep_merge_server_config( apr_pool_t *pool, void *BASE, void *ADD )
{
	SPEPServerConfig *base = (SPEPServerConfig*)BASE;
	SPEPServerConfig *add = (SPEPServerConfig*)ADD;
	SPEPServerConfig *result = (SPEPServerConfig*)apr_pcalloc( pool, sizeof( SPEPServerConfig ) );
	
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

extern "C" void modspep_child_init( apr_pool_t *pchild, server_rec *s )
{
	SPEPServerConfig *config = (SPEPServerConfig*)ap_get_module_config( s->module_config, &spep_module );
	const char *sname = s->server_hostname;
	
	if( sname == NULL ) sname = "no hostname.";
	
	init_spep_instance( pchild, config );
}

extern "C" int modspep_check_session( request_rec *req )
{
	SPEPServerConfig *serverConfig = (SPEPServerConfig*)ap_get_module_config( req->server->module_config, &spep_module );
	SPEPDirectoryConfig *directoryConfig = (SPEPDirectoryConfig*)ap_get_module_config( req->per_dir_config, &spep_module );
	
	// Check if it starts with the defined default spep webapp url
	if( req->parsed_uri.path != NULL && 
		std::string(req->parsed_uri.path).compare( 0, strlen(DEFAULT_URL_SPEP_WEBAPP), DEFAULT_URL_SPEP_WEBAPP ) == 0 )
	{
		return DECLINED;
	}
	if( SPEP_ENABLED( directoryConfig ) )
	{
		spep::apache::RequestHandler handler( serverConfig->instance->spep );
		return handler.handleRequest( req );
	}
	
	return DECLINED;
}

extern "C" int modspep_handler( request_rec *req )
{
	SPEPServerConfig *serverConfig = (SPEPServerConfig*)ap_get_module_config( req->server->module_config, &spep_module );
	//SPEPDirectoryConfig *directoryConfig = (SPEPDirectoryConfig*)ap_get_module_config( req->per_dir_config, &spep_module );
	
	// Check if it starts with the defined default spep webapp url
	if( std::string(req->parsed_uri.path).compare( 0, strlen(DEFAULT_URL_SPEP_WEBAPP), DEFAULT_URL_SPEP_WEBAPP ) == 0 )
	{
		if( std::string(req->parsed_uri.path).compare( DEFAULT_URL_SPEP_SSO ) == 0 )
		{
			spep::apache::SSOHandler handler( serverConfig->instance->spep );
			int result = handler.handleRequest( req );
			
			return result;
		}
		else if( std::string( req->parsed_uri.path ).compare( 0, strlen(DEFAULT_URL_SPEP_WEBSERVICES), DEFAULT_URL_SPEP_WEBSERVICES ) == 0 )
		{
			spep::apache::WSHandler handler( serverConfig->instance->spep );
			int result = handler.handleRequest( req );
			
			return result;
		}
		
		return HTTP_NOT_FOUND;
	}
	
	return DECLINED;
}

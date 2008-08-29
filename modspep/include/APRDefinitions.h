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
 * Creation Date: Aug 27, 2007
 * 
 * Purpose: 
 */

#ifndef APRDEFINITIONS_H_
#define APRDEFINITIONS_H_

#include "ModuleConstants.h"

/* Begin Apache 2.x code. */
#ifndef APACHE1

#include "httpd.h"
#include "http_config.h"

#include "apr_file_io.h"

#include "apr_strings.h"
#include "apr_uri.h"
#include "apr_tables.h"
#include "apr_pools.h"

#include "apr_time.h"

#include "apr_base64.h"
#include "apr_buckets.h"

#ifdef HAVE_MODSSL
#include "mod_ssl.h"

inline bool isSecureRequest( request_rec *req )
{
	return ( ssl_is_https( req->connection ) != 0 );
}

#else

inline bool isSecureRequest( request_rec *req )
{
	return false;
}

#endif

/* End Apache 2.x code. */



/* Begin Apache 1.3 code. */
#else 


#ifdef LINUX
/* Need this to work around a weird bug in the apache 1.3 headers. If the LINUX macro
 * is defined but 0 length, we get compilation errors.
 */
#if (LINUX + 0) == 0
#undef LINUX
#define LINUX 1
#endif /*(LINUX + 0) == 0*/
#endif /*LINUX*/


#include <algorithm>
#include <cstdio>

#include "httpd.h"
#include "http_config.h"

#include "ap_config.h"
#include "ap_alloc.h"


typedef ap_pool apr_pool_t;
typedef table apr_table_t;

typedef int32_t apr_status_t;
typedef int32_t apr_int32_t;
typedef std::size_t apr_size_t;

typedef apr_int32_t apr_fileperms_t;

typedef const char *(*cmd_func)();

typedef uri_components apr_uri_t;

#define apr_psprintf 	ap_psprintf
#define apr_pstrdup		ap_pstrdup
#define apr_pcalloc		ap_pcalloc
#define apr_pool_find	ap_pool_find

#define apr_pool_cleanup_register 	ap_register_cleanup
#define apr_pool_cleanup_null 		ap_null_cleanup

#define apr_table_get 	ap_table_get
#define apr_table_make 	ap_make_table
#define apr_table_set 	ap_table_set
#define apr_table_setn	ap_table_setn

#define apr_uri_parse ap_parse_uri_components

#define APR_WRITE		1
#define APR_CREATE		2
#define APR_APPEND		4
#define APR_XTHREAD		8
#define APR_SHARELOCK	16
#define APR_UREAD		0400
#define APR_UWRITE		0200
#define APR_GREAD		0040

#define APR_FLOCK_EXCLUSIVE 1
#define APR_FLOCK_SHARED 2

// Define APR_NOT_SUCCESS here so we have something to return from the mock APR wrappers on failure.
#define APR_SUCCESS 0
#define APR_NOT_SUCCESS 1

/**
 * Since we need a reference to the pool and the file pointer when we close the file,
 * we need to declare apr_file_t to be a struct.
 */
typedef struct apr_file_struct
{
	apr_pool_t *pool;
	FILE *file;
}apr_file_t;

inline apr_status_t apr_file_open( apr_file_t** file, const char *name, apr_int32_t flags, apr_fileperms_t perms, apr_pool_t *pool )
{
	/*
	 * Implementation note:
	 * This is safe for cross-platform stuff, because we have defined the APR_* flags above and we directly map them
	 * to fopen() C89 standard-compliant mode strings here. The umask() however is not our problem.
	 */

	// Max length for open mode is 2 chars, add 1 for the trailing '\0'
	const char *mode = NULL;
	if( (flags & APR_APPEND) != 0 )
	{
		mode = "a";
	}
	else if( (flags & APR_WRITE) != 0 )
	{
		mode = "w";
	}
	else
	{
		// TODO Why do we need a default? I don't know.
		mode = "r";
	}

	*file = static_cast<apr_file_t*>( ap_palloc( pool, sizeof( apr_file_t ) ) );
	(*file)->pool = pool;
	(*file)->file = ap_pfopen( pool, name, mode );

	return APR_SUCCESS;
}

inline apr_status_t apr_file_close( apr_file_t *file )
{
	ap_pfclose( file->pool, file->file );
	return APR_SUCCESS;
}

inline apr_status_t apr_file_puts( const char *str, apr_file_t *file )
{
	if( file != NULL && file->file != NULL )
	{
		std::fputs( str, file->file );
	}
	return APR_SUCCESS;
}

inline apr_status_t apr_file_lock( apr_file_t *file, int type )
{
	// TODO No-op for now. Leave it that way? Depends if std::fputs is safe across processes.
	// Note: apr_file_unlock (defined below) must always reverse anything done by this method.
	return APR_SUCCESS;
}

inline apr_status_t apr_file_unlock( apr_file_t *file )
{
	return APR_SUCCESS;
}

inline void apr_strerror( apr_status_t code, char *buf, apr_size_t bufsize )
{
	const char *error;
	if( code == APR_SUCCESS )
	{
		error = "No error.";
	}
	else
	{
		error = "An error occurred with an APR call.";
	}

	apr_size_t copysize = std::min( bufsize, strlen(error) );
	strncpy( buf, error, copysize );
}

inline void ap_set_content_type( request_rec *req, const char *contentType )
{
	//apr_table_set( req->headers_out, HEADER_NAME_CONTENT_TYPE, contentType );
	req->content_type = contentType;
}

inline bool isSecureRequest( request_rec *req )
{
	// TODO Find a better way to do this.
	return ( apr_table_get( req->subprocess_env, "SSL_PROTOCOL" ) != NULL );
}

/* End Apache 1.3 code. */
#endif /*APACHE1DEFS*/

#endif /*APRDEFINITIONS_H_*/

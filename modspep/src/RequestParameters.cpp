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
 * Creation Date: Aug 28, 2007
 * 
 * Purpose: 
 */

#include "spep/Util.h"

#include "RequestParameters.h"

#include "httpd.h"
#include "http_protocol.h"

#define QUERY_PARAMS_DEFAULT_TABLE_SIZE 5
#define REQUEST_READ_BUFFER_SIZE 512

spep::apache::HTTPContentDocument spep::apache::RequestParameters::readHTTPRequestContent()
{
	if( ap_setup_client_block( _req, REQUEST_CHUNKED_DECHUNK ) != OK )
	{
		return HTTPContentDocument();
	}
	
	if( ap_should_client_block( _req ) != 0 )
	{
		CArray<char> cDocument(1);
		std::size_t len = 0;
		
		char buffer[REQUEST_READ_BUFFER_SIZE];
		long bytes;
		while( (bytes = ap_get_client_block( _req, buffer, REQUEST_READ_BUFFER_SIZE )) > 0 )
		{
			std::size_t newLength = len + bytes;
			// Always make it 1 byte larger so we don't need to resize again for the semi-colon
			cDocument.resize( newLength + 1 );
			
			std::memcpy( &(cDocument[len]), buffer, bytes );
			len = newLength;
		}
		
		// If we don't append a semi-colon, libapreq2 barfs over the length of the posted document.
		cDocument[len] = ';';
		++len;
		
		// -1 is an error condition from ap_get_client_block
		if( bytes < 0 )
		{
			return HTTPContentDocument();
		}
		
		char *document = new char[len];
		std::memcpy( document, cDocument.get(), len );
		
		return HTTPContentDocument( document, len );
	}
	
	return HTTPContentDocument();
}

const char *spep::apache::RequestParameters::operator[]( const char *name )
{
	if( _params != NULL )
		return apr_table_get( _params, name );
	
	return NULL;
}




#ifndef APACHE1

#include "apreq2/apreq_param.h"
#include "apreq2/apreq_parser.h"

spep::apache::RequestParameters::RequestParameters( request_rec *req )
:
_params( apr_table_make( req->pool, QUERY_PARAMS_DEFAULT_TABLE_SIZE ) ),
_req( req )
{
	if( req->method_number == M_POST )
	{
		conn_rec *conn = req->connection;
		
		apreq_parser_function_t parserFunction = apreq_parser( req->content_type );
		if( parserFunction == NULL )
		{
			// If we failed to get a parser for the reported content type, try using the url-encoded parser anyway.
			parserFunction = apreq_parse_urlencoded;
		}
		
		// Brigade limit and temp dir aren't needed for apreq_parse_urlencoded, so set them to 0 and null respectively.
		// For some reason apreq requests "a pool which outlasts the bucket_alloc" - so using the connection pool rather than the request one.
		apreq_parser_t* parser = apreq_parser_make( conn->pool, conn->bucket_alloc, req->content_type, apreq_parse_urlencoded, 0, NULL, NULL, NULL );
		
		apr_bucket_brigade* brigade = apr_brigade_create( req->pool, conn->bucket_alloc );
		
		// Get POST parameters from request and put them into the brigade.
		HTTPContentDocument document( readHTTPRequestContent() );
		apr_brigade_write( brigade, NULL, NULL, document.getData(), document.getLength() );
		
		apr_status_t result = apreq_parser_run( parser, _params, brigade );
		if( result != APR_SUCCESS )
		{
			if( result == APR_INCOMPLETE )
			{
			}
			else
			{
				_params = NULL;
			}
		}
	}
	else
	{
		if( req->args == NULL || apreq_parse_query_string( req->pool, _params, req->args ) != APR_SUCCESS )
		{
			_params = NULL;
		}
	}
}

void spep::apache::RequestParameters::sendResponseDocument( const char *document, std::size_t length )
{
	apr_bucket_brigade* brigade = apr_brigade_create( _req->pool, _req->connection->bucket_alloc );
	// apr_brigade_puts doesn't assume that it has a flush function, so we can just pass NULL..
	apr_brigade_write( brigade, NULL, NULL, document, length );
	
	ap_pass_brigade( _req->output_filters, brigade );
}

#else

#include "libapreq/apache_request.h"

// Apache 1 implementation.

spep::apache::RequestParameters::RequestParameters( request_rec *req )
:
_params( NULL ),
_req( req )
{
	if( req->method_number == M_POST )
	{
		ApacheRequest *apacheRequest = ApacheRequest_new( req );
		
		ApacheRequest_parse( apacheRequest );
		_params = ApacheRequest_post_params( apacheRequest, req->pool );
	}
	else
	{
		ApacheRequest *apacheRequest = ApacheRequest_new( req );
		
		ApacheRequest_parse( apacheRequest );
		_params = ApacheRequest_query_params( apacheRequest, req->pool );
	}
}

void spep::apache::RequestParameters::sendResponseDocument( const char *document, std::size_t length )
{
	ap_send_http_header( _req );
	
	if( _req->header_only )
	{
		return;
	}
	
	ap_rwrite( document, length, _req );
}

#endif //0

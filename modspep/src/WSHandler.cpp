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
 * Creation Date: Jul 30, 2007
 * 
 * Purpose: 
 */

#include "WSHandler.h"
#include "RequestParameters.h"

#include "Common.h"

#include "exceptions/InvalidStateException.h"
#include "ws/SOAPUtil.h"
#include "UnicodeStringConversion.h"

spep::apache::WSHandler::WSHandler( spep::SPEP *spep )
:
_spep( spep )
{
}

void spep::apache::WSHandler::readRequestDocument( request_rec *req, WSProcessorData& data )
{
	std::string contentType( apr_table_get( req->headers_in, HEADER_NAME_CONTENT_TYPE ) );
	if( contentType.find( SOAP12_DOCUMENT_CONTENT_TYPE ) != std::string::npos )
	{
		data.setSOAPVersion( SOAPUtil::SOAP12 );
	}
	else if( contentType.find( SOAP11_DOCUMENT_CONTENT_TYPE ) != std::string::npos )
	{
		data.setSOAPVersion( SOAPUtil::SOAP11 );
	}
	else
	{
		// TODO Throw something more useful
		throw spep::InvalidStateException();
	}
	
	// This is the way it's done everywhere I can see... Seems kinda dodgy but what can you do.
	std::size_t i;
	if( ( i = contentType.find( ";" ) ) != std::string::npos )
	{
		if( ( i = contentType.find( "=", i ) ) != std::string::npos )
		{
			data.setCharacterEncoding( contentType.substr( i+1 ) );
		}
	}
	
	if( ap_setup_client_block( req, REQUEST_CHUNKED_DECHUNK ) != OK )
	{
		throw spep::InvalidStateException();
	}
	
	if( ap_should_client_block( req ) != 0 )
	{
		CArray<char> bigBuffer( SMALL_BUFFER_SIZE );
		std::size_t length = 0;
		
		long bytes;
		while( (bytes = ap_get_client_block( req, &bigBuffer[length], SMALL_BUFFER_SIZE )) > 0 )
		{
			length += static_cast<std::size_t>(bytes);
			// Make sure that bigBuffer always has SMALL_BUFFER_SIZE free space.
			bigBuffer.resize( length + SMALL_BUFFER_SIZE );
		}
		
		// -1 is an error condition from ap_get_client_block
		if( bytes < 0 )
		{
			throw spep::InvalidStateException();
		}
		
		char *requestDocument = new char[length];
		std::memcpy( requestDocument, bigBuffer.get(), length );
		
		data.setSOAPRequestDocument( spep::SOAPDocument( requestDocument, length ) );
		
		return;
	}
	
	throw spep::InvalidStateException();
}

void spep::apache::WSHandler::sendResponseDocument( request_rec *req, WSProcessorData& data )
{
	apr_table_set( req->headers_out, HEADER_NAME_CONTENT_TYPE, apr_table_get( req->headers_in, HEADER_NAME_CONTENT_TYPE ) );
	
	spep::SOAPDocument soapResponse( data.getSOAPResponseDocument() );

	RequestParameters params( req );
	params.sendResponseDocument( soapResponse.getData(), soapResponse.getLength() );
	
#if 0
	apr_bucket_brigade* brigade = apr_brigade_create( req->pool, req->connection->bucket_alloc );
	
	// apr_brigade_write doesn't assume that it has a flush function, so we can just pass NULL..
	apr_brigade_write( brigade, NULL, NULL, soapResponse.getData(), soapResponse.getLength() );
	
	if( ap_pass_brigade( req->output_filters, brigade ) != APR_SUCCESS )
	{
		throw spep::InvalidStateException();
	}
#endif /*0*/
}

int spep::apache::WSHandler::authzCacheClear( request_rec *req )
{
	std::string requestXML;
	try
	{
		spep::WSProcessorData wsProcessorData;
		this->readRequestDocument( req, wsProcessorData );
		
		spep::WSProcessor *wsProcessor = this->_spep->getWSProcessor();
		
		wsProcessor->authzCacheClear( wsProcessorData );
		
		this->sendResponseDocument( req, wsProcessorData );
	}
	catch (spep::InvalidStateException e)
	{
		return HTTP_INTERNAL_SERVER_ERROR;
	}
	catch (...)
	{
		return HTTP_INTERNAL_SERVER_ERROR;
	}
	
	return OK;
}

int spep::apache::WSHandler::singleLogout( request_rec *req )
{
	std::string requestXML;
	try
	{
		spep::WSProcessorData wsProcessorData;
		this->readRequestDocument( req, wsProcessorData );

		spep::WSProcessor *wsProcessor = this->_spep->getWSProcessor();
		
		wsProcessor->singleLogout( wsProcessorData );
		
		this->sendResponseDocument( req, wsProcessorData );
	}
	catch (spep::InvalidStateException e)
	{
		return HTTP_INTERNAL_SERVER_ERROR;
	}
	catch (...)
	{
		return HTTP_INTERNAL_SERVER_ERROR;
	}
	
	return OK;
}

int spep::apache::WSHandler::handleRequest( request_rec *req )
{
	// We don't check if the SPEP is started here, because we want the initial authz cache clear request to succeed.
	
	try
	{
		std::string path( req->parsed_uri.path );
		if( path.compare( DEFAULT_URL_SPEP_AUTHZCACHECLEAR ) == 0 )
		{
			// This request is bound for /spep/services/spep/authzCacheClear - handle it
			if( req->method_number == M_POST )
			{
				return this->authzCacheClear( req );
			}
			
			return HTTP_METHOD_NOT_ALLOWED;
		}
		else if( path.compare( DEFAULT_URL_SPEP_SINGLELOGOUT ) == 0 )
		{
			// This request is bound for /spep/services/spep/singleLogout - handle it.
			if( req->method_number == M_POST )
			{
				return this->singleLogout( req );
			}
			
			return HTTP_METHOD_NOT_ALLOWED;
		}
		
		return HTTP_NOT_FOUND;
	}
	catch (...)
	{
		return HTTP_INTERNAL_SERVER_ERROR;
	}
}

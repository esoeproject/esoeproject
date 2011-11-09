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
 */

#include "WSHandler.h"
#include "RequestParameters.h"

#include "Common.h"

#include "spep/exceptions/InvalidStateException.h"
#include "spep/ws/SOAPUtil.h"
#include "spep/UnicodeStringConversion.h"

extern "C"
{
	extern module spep_module;
}

spep::apache::WSHandler::WSHandler( spep::SPEP *spep )
:
_spep( spep )
{
	m_localLogger = LocalLoggerPtr(new saml2::LocalLogger(_spep->getLogger(), "spep::apache::WSHandler"));
}

spep::SOAPDocument spep::apache::WSHandler::readRequestDocument( request_rec *req, spep::SOAPUtil::SOAPVersion *soapVersion, std::string &characterEncoding )
{
	std::string contentType( apr_table_get( req->headers_in, HEADER_NAME_CONTENT_TYPE ) );
	if( contentType.find( SOAP12_DOCUMENT_CONTENT_TYPE ) != std::string::npos )
	{
		*soapVersion = SOAPUtil::SOAP12;
	}
	else if( contentType.find( SOAP11_DOCUMENT_CONTENT_TYPE ) != std::string::npos )
	{
		*soapVersion = SOAPUtil::SOAP11;
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
			characterEncoding = contentType.substr( i+1 );
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
		
		unsigned char *requestDocument = new unsigned char[length];
		std::memcpy( requestDocument, bigBuffer.get(), length );
		
		return spep::SOAPDocument( requestDocument, length );
	}
	
	throw spep::InvalidStateException();
}

void spep::apache::WSHandler::sendResponseDocument( request_rec *req, spep::SOAPDocument soapResponse )
{
	apr_table_set( req->headers_out, HEADER_NAME_CONTENT_TYPE, apr_table_get( req->headers_in, HEADER_NAME_CONTENT_TYPE ) );

	RequestParameters params( req );
	params.sendResponseDocument( reinterpret_cast<const char*>( soapResponse.getData() ), soapResponse.getLength() );
	
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
		std::string characterEncoding;
		spep::SOAPUtil::SOAPVersion soapVersion;
		SOAPDocument requestDocument = this->readRequestDocument( req, &soapVersion, characterEncoding );
		
		spep::WSProcessor *wsProcessor = this->_spep->getWSProcessor();
		
		SOAPDocument responseDocument = wsProcessor->authzCacheClear( requestDocument, soapVersion, characterEncoding );
		
		if( responseDocument.getData() == NULL || responseDocument.getLength() == 0 )
		{
			m_localLogger->error() << "Internal Server Error when proccessing authzCacheClear: ResponseDocument data was null or length was 0.";
			return HTTP_INTERNAL_SERVER_ERROR;
		}
		
		this->sendResponseDocument( req, responseDocument );
	}
	catch (spep::InvalidStateException& e)
	{
		m_localLogger->error() << "Internal Server Error when proccessing authzCacheClear: " << e.what();
		return HTTP_INTERNAL_SERVER_ERROR;
	}
	catch (std::exception& e)
	{
		m_localLogger->error() << "Internal Server Error when proccessing authzCacheClear: " << e.what();
		return HTTP_INTERNAL_SERVER_ERROR;
	}
	catch (...)
	{
		m_localLogger->error() << "Internal Server Error when proccessing authzCacheClear.";
		return HTTP_INTERNAL_SERVER_ERROR;
	}
	
	return OK;
}

int spep::apache::WSHandler::singleLogout( request_rec *req )
{
	std::string requestXML;
	try
	{
		std::string characterEncoding;
		spep::SOAPUtil::SOAPVersion soapVersion;
		SOAPDocument requestDocument = this->readRequestDocument( req, &soapVersion, characterEncoding );
		
		spep::WSProcessor *wsProcessor = this->_spep->getWSProcessor();
		
		SOAPDocument responseDocument = wsProcessor->singleLogout( requestDocument, soapVersion, characterEncoding );
		
		if( responseDocument.getData() == NULL || responseDocument.getLength() == 0 )
		{
			m_localLogger->error() << "Internal Server Error when proccessing singleLogout: ResponseDocument data was null or length was 0.";
			return HTTP_INTERNAL_SERVER_ERROR;
		}
		
		this->sendResponseDocument( req, responseDocument );
	}
	catch (spep::InvalidStateException& e)
	{
		m_localLogger->error() << "Internal Server Error when proccessing singleLogout: " << e.what();
		return HTTP_INTERNAL_SERVER_ERROR;
	}
	catch (std::exception& e)
	{
		m_localLogger->error() << "Internal Server Error when proccessing singleLogout: " << e.what();
		return HTTP_INTERNAL_SERVER_ERROR;
	}
	catch (...)
	{
		m_localLogger->error() << "Internal Server Error when proccessing singleLogout.";
		return HTTP_INTERNAL_SERVER_ERROR;
	}
	
	return OK;
}

int spep::apache::WSHandler::handleRequest( request_rec *req )
{
	// We don't check if the SPEP is started here, because we want the initial authz cache clear request to succeed.
	SPEPServerConfig *serverConfig = (SPEPServerConfig*)ap_get_module_config( req->server->module_config, &spep_module );
	
	try
	{
		std::string path( req->parsed_uri.path );
		if( path.compare( serverConfig->instance->spepAuthzCacheClear ) == 0 )
		{
			// This request is bound for /spep/services/spep/authzCacheClear - handle it
			if( req->method_number == M_POST )
			{
				return this->authzCacheClear( req );
			}
			
			m_localLogger->error() << "Request method '" << req->method_number << "' not allowed when calling authzCacheClear - returning HTTP_METHOD_NOT_ALLOWED.";
			return HTTP_METHOD_NOT_ALLOWED;
		}
		else if( path.compare( serverConfig->instance->spepSingleLogout ) == 0 )
		{
			// This request is bound for /spep/services/spep/singleLogout - handle it.
			if( req->method_number == M_POST )
			{
				return this->singleLogout( req );
			}
			
			m_localLogger->error() << "Request method '" << req->method_number << "' not allowed when calling singleLogout - returning HTTP_METHOD_NOT_ALLOWED.";
			return HTTP_METHOD_NOT_ALLOWED;
		}
		
		m_localLogger->info() << "Requested path '" << path << "' does not exist - returning HTTP_NOT_FOUND.";
		return HTTP_NOT_FOUND;
	}
	catch (std::exception& e)
	{
		m_localLogger->error() << "Internal Server Error when handling WS Request: " << e.what();
		return HTTP_INTERNAL_SERVER_ERROR;
	}
	catch (...)
	{
		m_localLogger->error() << "Internal Server Error when handling WS Request.";
		return HTTP_INTERNAL_SERVER_ERROR;
	}
}

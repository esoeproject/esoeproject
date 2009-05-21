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

#include "saml2/SAML2Defs.h"

#include "spep/authn/AuthnProcessorData.h"
#include "spep/Util.h"
#include "spep/UnicodeStringConversion.h"
#include "spep/Base64.h"

#include "SSOHandler.h"
#include "Cookies.h"
#include "Common.h"

#include "RequestParameters.h"

#include <unicode/ucnv.h>

#include "httpd.h"


#define HTTP_POST_VAR_SAML_RESPONSE "SAMLResponse"

extern "C"
{
	extern module spep_module;
}

spep::apache::SSOHandler::SSOHandler( spep::SPEP *spep )
:
_spep( spep )
{
}

int spep::apache::SSOHandler::handleSSOGetRequest( request_rec *req )
{
	RequestParameters params( req );
	
	Cookies cookies( req );
	// Set expiry to 48 hours to be consistent with Java land.
	cookies.addCookie( req, "spepAutoSubmit", "enabled", "/", NULL, false, 172800 );
	cookies.sendCookies( req );
	
	const char *base64RedirectURLChars = params[ REDIRECT_URL_PARAM ];
	//spep::AuthnProcessorData data;
	std::string base64RedirectURL;
	
	if( base64RedirectURLChars != NULL )
	{
		base64RedirectURL = std::string( base64RedirectURLChars );

		// Work around Apache's need to URL decode everything it touches
		for ( int i=0; i<base64RedirectURL.length(); ++i ) {
			// + is translated to a space, change it back
			if (base64RedirectURL[i] == ' ') base64RedirectURL[i] = '+';

			// No other Base64 chars are treated specially by Apache.
		}
	}
	
	// Try and get the hostname from the Host: header in the request.
	const char *hostname = apr_table_get( req->headers_in, "Host" );
	// Failing that, use the ServerName that apache determined
	if( hostname == NULL )
	{
		hostname = req->server->server_hostname;
	}
	
	// Check that we have a port to use - needs to come from the connection
	// VirtualHosted apache servers don't keep port info in each vhost.
#ifdef APACHE1
	int port = req->connection->local_addr.sin_port;
#else
	int port = req->connection->local_addr->port;
#endif
	if( port == 0 )
	{
		hostname = NULL;
	}
	
	// Parse the service host URL so we can compare.
	apr_uri_t *uri = static_cast<apr_uri_t*>( apr_pcalloc( req->pool, sizeof(apr_uri_t) ) );
	apr_uri_parse( req->pool, this->_spep->getSPEPConfigData()->getServiceHost().c_str(), uri );
	
	std::string baseRequestURL;
	
	// If we didn't get a hostname (or port), or it was the same as the service host.
	if( hostname == NULL || std::strcmp( uri->hostinfo, hostname ) == 0 )
	{
		baseRequestURL = this->_spep->getSPEPConfigData()->getServiceHost(); 
	}
	else
	{
		// Use mod_ssl to detemine if this was a https request, otherwise assume http.
		const char *scheme = isSecureRequest(req) ? "https" : "http";
		// Prepend the scheme to the hostname
		char *baseRequestURLChars = apr_psprintf( req->pool, "%s://%s", scheme, hostname );
		
		// If it's on a non-standard port, append a port to the base URL
		if( ( !isSecureRequest(req) && port != 80 ) 
			|| ( isSecureRequest(req) && port != 443 ) )
		{
			baseRequestURLChars = apr_psprintf( req->pool, "%s:%d", baseRequestURLChars, port );
		}
		
		baseRequestURL = baseRequestURLChars;
	}
	
	// Build the authentication request document
	std::string authnRequestDocument( this->buildAuthnRequestDocument( req->pool, base64RedirectURL, baseRequestURL ) );
	
	ap_set_content_type( req, HTTP_POST_REQUEST_DOCUMENT_CONTENT_TYPE );
	req->status = HTTP_OK;
	
	params.sendResponseDocument( authnRequestDocument.c_str(), authnRequestDocument.length() );
	
	return OK;
}

int spep::apache::SSOHandler::handleSSOPostRequest( request_rec *req )
{
	
	RequestParameters params( req );
	
	const char *base64SAMLResponse = params[HTTP_POST_VAR_SAML_RESPONSE];

	spep::Base64Decoder decoder;
	decoder.push( base64SAMLResponse, strlen(base64SAMLResponse) );
	decoder.close();
	
	spep::Base64Document samlResponse( decoder.getResult() );

	long documentLength = samlResponse.getLength();
	SAMLByte *document = new SAMLByte[ documentLength ];
	std::memcpy( document, samlResponse.getData(), documentLength );
	
	spep::AuthnProcessorData data;
	data.setResponseDocument( saml2::SAMLDocument( document, documentLength ) );
	data.setDisableAttributeQuery( this->_spep->getSPEPConfigData()->disableAttributeQuery() );
	
	try
	{
		this->_spep->getAuthnProcessor()->processAuthnResponse( data );
	}
	catch( ... )
	{
		//std::string reason( e.what() );
		return HTTP_INTERNAL_SERVER_ERROR;
	}
	
	Cookies cookies;
	
	const char *tokenName = this->_spep->getSPEPConfigData()->getTokenName().c_str();
	std::string tokenDomain( apr_table_get( req->headers_in, "Host" ) ); 
		//this->_spep->getSPEPConfigData()->getTokenDomain().c_str();
	if( tokenDomain.find_first_of( ':' ) != std::string::npos )
	{
		tokenDomain = tokenDomain.substr( 0, tokenDomain.find_first_of( ':' ) );
	}
	
	bool secure = isSecureRequest( req );
	cookies.addCookie( req, tokenName, data.getSessionID().c_str(), NULL, tokenDomain.c_str(), secure );
	cookies.sendCookies( req );
	
	// Establish return URL..
	std::string base64RedirectURL( data.getRequestURL() );
	if( base64RedirectURL.length() > 0 )
	{
		spep::Base64Decoder decoder;
		decoder.push( base64RedirectURL.c_str(), base64RedirectURL.length() );
		decoder.close();
		
		// Technically it's not a document, but it's all the same to Base64Decoder
		spep::Base64Document redirectURLDocument( decoder.getResult() );
		std::string redirectURL( redirectURLDocument.getData(), redirectURLDocument.getLength() );
		
		apr_table_set( req->headers_out, HEADER_NAME_REDIRECT_URL, redirectURL.c_str() );
	}
	else
	{
		apr_table_set( req->headers_out, HEADER_NAME_REDIRECT_URL, this->_spep->getSPEPConfigData()->getDefaultUrl().c_str() );
	}
	
	// Force external redirect by not returning the code to apache.
	req->status = HTTP_MOVED_TEMPORARILY;

#if APACHE1
	// This call was required in apache 1.3
	// apache 2.x is smart enough to fill in the blanks
	ap_send_http_header( req );
#endif
	
	return OK;
}

int spep::apache::SSOHandler::handleRequest( request_rec *req )
{
	SPEPServerConfig *serverConfig = (SPEPServerConfig*)ap_get_module_config( req->server->module_config, &spep_module );

	try
	{
		// First we need to determine where the request was headed.
		std::string path( req->parsed_uri.path );
		if( path.compare( serverConfig->instance->spepSSOPath ) == 0 )
		{
			if( ! this->_spep->isStarted() )
			{
				return HTTP_SERVICE_UNAVAILABLE;
			}
			
			// This is request is bound for /spep/sso - handle it.
			if( req->method_number == M_GET )
			{
				return this->handleSSOGetRequest( req );
			}
			else if( req->method_number == M_POST )
			{
				return this->handleSSOPostRequest( req );
			}
			
			return HTTP_METHOD_NOT_ALLOWED;
		}
		
		return DECLINED;
	}
	catch (...)
	{
		return HTTP_INTERNAL_SERVER_ERROR;
	}
}

std::string spep::apache::SSOHandler::buildAuthnRequestDocument( apr_pool_t *pool, const std::string &base64RedirectURL, const std::string& baseRequestURL )
{
	AuthnProcessorData data;
	data.setRequestURL( base64RedirectURL );
	data.setBaseRequestURL( baseRequestURL );
	
	this->_spep->getAuthnProcessor()->generateAuthnRequest( data );
	
	saml2::SAMLDocument requestDocument( data.getRequestDocument() );

	spep::Base64Encoder encoder;
	encoder.push( reinterpret_cast<const char*>( requestDocument.getData() ), requestDocument.getLength() );
	encoder.close();
	
	spep::Base64Document base64EncodedDocument( encoder.getResult() );
	std::string encodedDocumentString( base64EncodedDocument.getData(), base64EncodedDocument.getLength() );
	
	std::string ssoURL = this->_spep->getMetadata()->getSingleSignOnEndpoint();
	
	return std::string( FORMAT_HTTP_POST_REQUEST_DOCUMENT( pool, ssoURL.c_str(), encodedDocumentString.c_str() ) );
}

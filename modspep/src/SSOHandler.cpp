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
	spep::AuthnProcessorData data;
	std::string base64RedirectURL;
	
	if( base64RedirectURLChars != NULL )
	{
		base64RedirectURL = std::string( base64RedirectURLChars );
	}
	
	std::string authnRequestDocument( this->buildAuthnRequestDocument( req->pool, base64RedirectURL ) );
	
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
	const char *tokenDomain = this->_spep->getSPEPConfigData()->getTokenDomain().c_str();
	bool secure = isSecureRequest( req );
	cookies.addCookie( req, tokenName, data.getSessionID().c_str(), NULL, tokenDomain, secure );
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

std::string spep::apache::SSOHandler::buildAuthnRequestDocument( apr_pool_t *pool, std::string &base64RedirectURL )
{
	AuthnProcessorData data;
	data.setRequestURL( base64RedirectURL );
	
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

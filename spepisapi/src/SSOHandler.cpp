/* Copyright 2008, Queensland University of Technology
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
 * Creation Date: Jan 10, 2008
 * 
 * Purpose: 
 */

#include "SSOHandler.h"
#include "RequestParameters.h"
#include "Cookies.h"
#include "FilterConstants.h"

#include "spep/Base64.h"

namespace spep { namespace isapi {
	

#define HTTP_POST_VAR_SAML_RESPONSE "SAMLResponse"

SSOHandler::SSOHandler( spep::SPEP *spep, SPEPExtension *spepExtension )
:
_spep( spep ),
_spepExtension( spepExtension )
{
}

DWORD SSOHandler::handleSSOGetRequest( ISAPIRequest *request )
{
	RequestParameters params( request );
	
	Cookies cookies( request );
	// Set expiry to 48 hours to be consistent with Java land.
	cookies.addCookie( request, "spepAutoSubmit", "enabled", "/", NULL, false, 172800 );
	
	std::string base64RedirectURL( params[ REDIRECT_URL_PARAM ] );
	
	// Try and get the hostname from the Host: header in the request.
	std::string hostname( request->getServerVariable( "SERVER_NAME" ) );
	// Failing that, use the ServerName that IIS determined
	if( hostname.length() == 0 )
	{
		//hostname = req->server->server_hostname;
	}
	
	// Check that we have a port to use - needs to come from the connection
	// VirtualHosted apache servers don't keep port info in each vhost.
	int port = 0;//req->connection->local_addr->port;
	if( port == 0 )
	{
		hostname = std::string();
	}
	
	// Parse the service host URL so we can compare.
	std::string serviceHost( this->_spep->getSPEPConfigData()->getServiceHost() );
	std::size_t start = serviceHost.find_first_of( '/' );
	while( serviceHost.at(start) == '/' ) start++;
	std::size_t end = serviceHost.find_first_of( '/', start );
	std::string serviceHostname( serviceHost.substr( start, end-start ) );
	
	std::string baseRequestURL;
	
	// If we didn't get a hostname (or port), or it was the same as the service host.
	if( hostname.length() == 0 || serviceHostname.compare( hostname ) == 0 )
	{
		baseRequestURL = this->_spep->getSPEPConfigData()->getServiceHost(); 
	}
	else
	{
		// Use mod_ssl to detemine if this was a https request, otherwise assume http.
		const char *scheme = request->isSecureRequest() ? "https" : "http";
		// Prepend the scheme to the hostname
		char *baseRequestURLChars = request->isprintf( "%s://%s", scheme, hostname );
		
		// If it's on a non-standard port, append a port to the base URL
		if( ( !request->isSecureRequest() && port != 80 ) 
			|| ( request->isSecureRequest() && port != 443 ) )
		{
			baseRequestURLChars = request->isprintf( "%s:%d", baseRequestURLChars, port );
		}
		
		baseRequestURL = baseRequestURLChars;
	}
	
	// Build the authentication request document
	std::string authnRequestDocument( this->buildAuthnRequestDocument( request, base64RedirectURL, baseRequestURL ) );
	
	return request->sendResponseDocument( HTTP_OK_STATUS_LINE, authnRequestDocument.c_str(), authnRequestDocument.length(), HTTP_POST_REQUEST_DOCUMENT_CONTENT_TYPE );
}

DWORD SSOHandler::handleSSOPostRequest( ISAPIRequest *request )
{
	
	RequestParameters params( request );
	
	std::string base64SAMLResponse( params[HTTP_POST_VAR_SAML_RESPONSE] );

	spep::Base64Decoder decoder;
	decoder.push( base64SAMLResponse.c_str(), base64SAMLResponse.length() );
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
		return request->sendErrorDocument( HTTP_INTERNAL_SERVER_ERROR );
	}
	
	Cookies cookies( request );
	
	std::string tokenName( this->_spep->getSPEPConfigData()->getTokenName() );
	std::string tokenDomain( request->getServerVariable( "SERVER_NAME" ) ); 
	const char* tokenDomainChars = NULL;
	if( tokenDomain.length() != 0 ) tokenDomainChars = tokenDomain.c_str();
	
	bool secure = request->isSecureRequest();
	cookies.addCookie( request, tokenName.c_str(), data.getSessionID().c_str(), NULL, tokenDomainChars, secure );
	
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
		
		return request->sendRedirectResponse( redirectURL );
		//request->setHeader( redirectHeader );
	}
	else
	{
		return request->sendRedirectResponse( this->_spep->getSPEPConfigData()->getDefaultUrl() );
		//request->setHeader( redirectHeader );
	}
	
	// Force external redirect by not using ExecuteUrl
	//request->sendResponseHeader( HTTP_REDIRECT_STATUS_LINE );
	//return HSE_STATUS_SUCCESS;
	//return request->sendResponseHeader( HTTP_REDIRECT_STATUS_LINE, TRUE );
}

DWORD SSOHandler::handleRequest( ISAPIRequest *request )
{
	try
	{
		// First we need to determine where the request was headed.
		std::string path( request->getRequestURL() );
		if( path.compare( _spepExtension->_spepSSOURL ) == 0 )
		{
			if( ! this->_spep->isStarted() )
			{
				return request->sendErrorDocument( HTTP_SERVICE_UNAVAILABLE );
			}
			
			// This is request is bound for /spep/sso - handle it.
			if( request->getRequestMethod().compare( "GET" ) == 0 )
			{
				return this->handleSSOGetRequest( request );
			}
			else if( request->getRequestMethod().compare( "POST" ) == 0 )
			{
				return this->handleSSOPostRequest( request );
			}
			
			return request->sendErrorDocument( HTTP_METHOD_NOT_ALLOWED );
		}
		
		return request->continueRequest();
	}
	catch (...)
	{
		return request->sendErrorDocument( HTTP_INTERNAL_SERVER_ERROR );
	}
}

std::string SSOHandler::buildAuthnRequestDocument( ISAPIRequest *request, const std::string &base64RedirectURL, const std::string& baseRequestURL )
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
	
	return std::string( FORMAT_HTTP_POST_REQUEST_DOCUMENT( request, ssoURL.c_str(), encodedDocumentString.c_str() ) );
}

	
} }
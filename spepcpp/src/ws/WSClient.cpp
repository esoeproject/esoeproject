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
 * Creation Date: 08/01/2007
 * 
 * Purpose: 
 */

#include "ws/WSClient.h"
#include "Util.h"
#include "exceptions/WSException.h"

#include <boost/lexical_cast.hpp>

// TODO These values stolen from includes/versions/Common.h from the modspep project. Find somewhere common to put them.
/// Name of the HTTP header for content type
#define HEADER_NAME_CONTENT_TYPE "Content-Type"
#define HEADER_NAME_CONTENT_LENGTH "Content-Length"

/// SOAP Document content types
/**@{*/
#define SOAP11_DOCUMENT_CONTENT_TYPE "text/xml"
#define SOAP12_DOCUMENT_CONTENT_TYPE "application/soap+xml"
/**@}*/


spep::WSClient::WSClient( spep::ReportingProcessor *reportingProcessor )
:
_localReportingProcessor( reportingProcessor->localReportingProcessor( "spep::WSClient" ) ),
_curl( curl_easy_init() )
{
}

spep::WSClient::~WSClient()
{
	curl_easy_cleanup( _curl );
}

void spep::WSClient::createSOAPRequest( spep::WSProcessorData& data )
{
	SOAPDocument document = SOAPUtil::wrapDocumentInSOAP( data.getSAMLRequestDocument(), data.getCharacterEncoding(), data.getSOAPVersion() );
	
	data.setSOAPRequestDocument( document );
}

void spep::WSClient::createSAMLResponse( spep::WSProcessorData& data )
{
	saml2::SAMLDocument document = SOAPUtil::unwrapDocumentFromSOAP( data.getSOAPResponseDocument(), data.getCharacterEncoding(), data.getSOAPVersion() );
	
	data.setSAMLResponseDocument( document );
}

void spep::WSClient::doSOAPRequest( spep::WSProcessorData& data, std::string endpoint )
{
	
	RawSOAPDocument requestDocument;
	// I do this cast here because I know for a fact that the string will never be changed anyway.
	requestDocument.data = const_cast<char*>( data.getSOAPRequestDocument().getData() );
	requestDocument.len = data.getSOAPRequestDocument().getLength();
	
	// Initialize an empty response document container.
	RawSOAPDocument responseDocument;
	
	AutoArray<char> errorBuffer( CURL_ERROR_SIZE );
	std::memset( errorBuffer.get(), 0, CURL_ERROR_SIZE );
	
	this->_localReportingProcessor.log( DEBUG, "Calling cURL to make web service call to " + endpoint );
	
	// Set the URL for curl to retrieve from
	curl_easy_setopt( this->_curl, CURLOPT_URL, endpoint.c_str() );
	// Give curl something to call with its data
	curl_easy_setopt( this->_curl, CURLOPT_WRITEFUNCTION, spep::WSClient::curlWriteCallback );
	curl_easy_setopt( this->_curl, CURLOPT_WRITEDATA, (void*)&responseDocument );
	// Give curl the request content
	curl_easy_setopt( this->_curl, CURLOPT_READFUNCTION, spep::WSClient::curlReadCallback );
	curl_easy_setopt( this->_curl, CURLOPT_READDATA, (void*)&requestDocument );
	// Tell curl we're doing a HTTP POST
	curl_easy_setopt( this->_curl, CURLOPT_POST, 1 );
	// Buffer to output an error message if the call fails
	curl_easy_setopt( this->_curl, CURLOPT_ERRORBUFFER, errorBuffer.get() );
	// Don't give us any content on a HTTP >=400 response
	curl_easy_setopt( this->_curl, CURLOPT_FAILONERROR, 1 );
	
	std::string contentTypeHeader( HEADER_NAME_CONTENT_TYPE );
	contentTypeHeader += ": ";
	
	if( data.getSOAPVersion() == SOAPUtil::UNINITIALIZED )
	{
		data.setSOAPVersion( SOAPUtil::SOAP11 );
	}
	
	if( data.getSOAPVersion() == SOAPUtil::SOAP11 )
	{
		contentTypeHeader += SOAP11_DOCUMENT_CONTENT_TYPE;
	}
	else if( data.getSOAPVersion() == SOAPUtil::SOAP12 )
	{
		contentTypeHeader += SOAP12_DOCUMENT_CONTENT_TYPE;
	}
	else
	{
		throw WSException( "Invalid SOAP version specifier." );
	}
	
	if( data.getCharacterEncoding().length() > 0 )
	{
		contentTypeHeader += "; charset=";
		contentTypeHeader += data.getCharacterEncoding();
	}
	
	this->_localReportingProcessor.log( DEBUG, std::string( "Setting content type header to - " ) + contentTypeHeader );
	
	std::string contentLengthHeader( HEADER_NAME_CONTENT_LENGTH );
	contentLengthHeader += ": ";
	contentLengthHeader += boost::lexical_cast<std::string>( requestDocument.len );
	
	struct curl_slist *headerList=NULL;
	headerList = curl_slist_append( headerList, contentTypeHeader.c_str() );
	headerList = curl_slist_append( headerList, contentLengthHeader.c_str() );
	
	curl_easy_setopt( this->_curl, CURLOPT_HTTPHEADER, headerList );

	// Perform the call. This will block until complete.
	CURLcode result = curl_easy_perform(this->_curl);
	curl_slist_free_all( headerList );
	
	// If the request didn't succeed, handle the error condition.
	if (result != CURLE_OK)
	{
		this->_localReportingProcessor.log( ERROR, std::string("Web service call failed. Error message was: ") + errorBuffer.get() );
		throw WSException( std::string("Web service call failed. Error message was: ") + errorBuffer.get() );
	}
	
	data.setSOAPResponseDocument( SOAPDocument( responseDocument.data, responseDocument.len ) );
	// Make sure the RawSOAPDocument destructor doesn't delete the document.
	responseDocument.data = NULL;
}

void spep::WSClient::doWSCall( WSProcessorData& data, std::string endpoint )
{
	// Default to SOAP 1.1
	if( data.getSOAPVersion() == SOAPUtil::UNINITIALIZED )
	{
		data.setSOAPVersion( SOAPUtil::SOAP12 );
	}
	
	// Default to UTF-16
	if( data.getCharacterEncoding().length() == 0 )
	{
		data.setCharacterEncoding( "UTF-16" );
	}
	
	this->createSOAPRequest( data );
	
	this->doSOAPRequest( data, endpoint );
	
	this->createSAMLResponse( data );
	
}

std::size_t spep::WSClient::curlWriteCallback( void *buffer, std::size_t size, std::size_t nmemb, void *userp )
{
	// This will only be invoked from an internal call to cURL, so we 
	// will always have the right kind of object being passed in here.
	RawSOAPDocument *data = (RawSOAPDocument*)userp;
	
	// Number of bytes is size*nmemb (see man curl_easy_setopt(3))
	std::size_t bytes = ( size * nmemb );
	std::size_t newSize = data->len + bytes;
	// Reallocate the buffer and copy the data
	char *newBuffer = new char[newSize];
	
	if( data->data != NULL )
		memcpy( newBuffer, data->data, data->len );
	
	memcpy( &newBuffer[data->len], buffer, bytes );
	
	delete[] data->data;
	data->data = newBuffer;
	data->len = newSize;
	
	return bytes;
}

std::size_t spep::WSClient::curlReadCallback( void *buffer, std::size_t size, std::size_t nmemb, void *userp )
{
	// This will only be invoked from an internal call to cURL, so we 
	// will always have the right kind of object being passed in here.
	RawSOAPDocument *data = (RawSOAPDocument*)userp;
	
	// Number of bytes is size*nmemb (see man curl_easy_setopt(3))
	std::size_t bytes = ( size * nmemb );
	// Number of bytes not yet "read" from the buffer.
	std::size_t remaining = data->len - data->pos;
	if( bytes > remaining )
		bytes = remaining;
	
	// First call is at position 0, so every call will point at the first character to be copied.
	if( bytes > 0 )
	{
		// copy "bytes" bytes starting at "data->pos" in the array.
		std::memcpy( buffer, &(data->data[data->pos]), bytes );
		data->pos += bytes;
	}
	
	return bytes;
}

// It's just a semantic difference, point these all at the above code.
void spep::WSClient::attributeAuthority( spep::WSProcessorData& data, std::string endpoint )
{ this->doWSCall( data, endpoint ); }

void spep::WSClient::policyDecisionPoint( spep::WSProcessorData& data, std::string endpoint )
{ this->doWSCall( data, endpoint ); }

void spep::WSClient::spepStartup( spep::WSProcessorData& data, std::string endpoint )
{ this->doWSCall( data, endpoint ); }


spep::WSClient::RawSOAPDocument::RawSOAPDocument()
:
data( NULL ),
len( 0 ),
pos( 0 )
{
}

spep::WSClient::RawSOAPDocument::RawSOAPDocument( const RawSOAPDocument& other )
:
data( other.data ),
len( other.len ),
pos( other.pos )
{
}

spep::WSClient::RawSOAPDocument::~RawSOAPDocument()
{
	// The document in here will always be managed by saml2::ManagedDocument object, so we don't need to delete it here.
}

spep::WSClient::RawSOAPDocument& spep::WSClient::RawSOAPDocument::operator=( const spep::WSClient::RawSOAPDocument& other )
{
	this->len = other.len;
	this->pos = other.pos;
	this->data = other.data;
	
	return *this;
}




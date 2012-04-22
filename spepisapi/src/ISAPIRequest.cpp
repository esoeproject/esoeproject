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
 * Creation Date: Jan 8, 2008
 * 
 * Purpose: 
 */

#include "ISAPIRequest.h"
#include "FilterConstants.h"

#include "spep/exceptions/InvalidStateException.h"

#include <ctype.h>

#include <boost/lexical_cast.hpp>

// This is only safe for direct variable parameters
// Any expression with a side effect, such as a ++ or a function call should NOT be passed into this macro.
#define URLDECODE_HEX2DEC(x) ( ( x <= 'f' && x >= 'a' ) ? ( 10 + x - 'a' ) : ( x <= '9' && x >= '0' ) ? ( x - '0' ) : -1 )

namespace spep { namespace isapi {

	extern "C"
	VOID WINAPI IsapiExecuteUrlHandleCompletion(
			IN EXTENSION_CONTROL_BLOCK *pECB,
			IN LPVOID context,
			IN DWORD cbIo,
			IN DWORD dwError
	)
	{
		pECB->ServerSupportFunction( pECB->ConnID, HSE_REQ_DONE_WITH_SESSION, NULL, NULL, NULL );
	}
	
	ISAPIRequestImpl::ISAPIRequestImpl( LPEXTENSION_CONTROL_BLOCK extensionControlBlock )
	:
	_extensionControlBlock( extensionControlBlock ),
	_serverSupportFunction( extensionControlBlock->ServerSupportFunction ),
	_writeClient( extensionControlBlock->WriteClient ),
	_readClient( extensionControlBlock->ReadClient ),
	_getServerVariable( extensionControlBlock->GetServerVariable ),
	_requestURL(),
	_requestMethod( extensionControlBlock->lpszMethod ),
	_queryString( extensionControlBlock->lpszQueryString ),
	_scriptName(),
	_contentType(),
	_contentLength( 0 ),
	_headersSent(false)
	{
		_requestURL = getServerVariable( "URL" );
		_scriptName = getServerVariable( "SCRIPT_NAME" );
		_contentType = getServerVariable( "CONTENT_TYPE" );
		_remoteAddress = getServerVariable("REMOTE_ADDR");
		_isSecureRequest = (getServerVariable("SERVER_PORT_SECURE").compare("1") == 0 );
		try
		{
			_contentLength = boost::lexical_cast<DWORD>(getServerVariable("CONTENT_LENGTH"));
		}
		catch( boost::bad_lexical_cast& )
		{
		}
	}
	
	ISAPIRequestImpl::~ISAPIRequestImpl()
	{
		for( std::vector<LPVOID>::iterator iter = _freeList.begin(); iter != _freeList.end(); ++iter )
		{
			free( *iter );
		}
	}
	
	std::string ISAPIRequestImpl::getHeader( const std::string &name )
	{
		DWORD bufferSize = HTTP_HEADER_VALUE_DEFAULT_LENGTH;
		spep::CArray<char> buffer( bufferSize );
		
		DWORD prefixLength = strlen( HTTP_HEADER_VARIABLE_PREFIX );
		DWORD bufferLength = prefixLength + name.size() + 1;
		spep::CArray<char> nameBuffer( bufferLength );
		std::strncpy( nameBuffer.get(), HTTP_HEADER_VARIABLE_PREFIX, prefixLength );
		std::strncpy( &nameBuffer[prefixLength], name.c_str(), name.size() );
		nameBuffer[prefixLength + name.size()] = '\0';
		for( std::size_t i = prefixLength; i < bufferLength; ++i )
		{
			if( nameBuffer[i] == ':' )
			{
				nameBuffer[i] = '\0';
				break;
			}
			else if( nameBuffer[i] == '-' )
			{
				nameBuffer[i] = '_';
			}
			else
			{
				nameBuffer[i] = ::toupper( nameBuffer[i] );
			}
		}
		
		while( !this->_getServerVariable( this->_extensionControlBlock->ConnID, nameBuffer.get(), buffer.get(), &bufferSize ) )
		{
			DWORD error = GetLastError();
			if( error == ERROR_INSUFFICIENT_BUFFER )
			{
				buffer.resize( bufferSize );
			}
			else
			{
				return std::string();
			}
		}
		
		// This is safe because GetHeader() null terminates the string.
		return std::string( buffer.get() );
	}
	
	void ISAPIRequestImpl::setHeader( const std::string& headerValue )
	{
		if( _headersSent ) throw spep::InvalidStateException();
		
		std::size_t begin = headerValue.find_first_not_of( "\r\n" );
		std::size_t end = headerValue.find_first_of( "\r\n", begin );
		
		if( begin == std::string::npos ) return;
		if( end == std::string::npos )
		{
			_responseHeaders.push_back( headerValue.substr( begin ) );
		}
		else
		{
			_responseHeaders.push_back( headerValue.substr( begin, (end-begin) ) );
		}
	}
	
	std::string ISAPIRequestImpl::getServerVariable( const std::string& name )
	{
		DWORD size = 128;
		spep::CArray<char> buf( size );
		
		if( this->_getServerVariable( this->_extensionControlBlock->ConnID, const_cast<char*>( name.c_str() ), buf.get(), &size ) )
		{
			std::string ret( buf.get(), size );
			return ret.substr( 0, ret.find_first_of( '\0' ) );
		}
		
		if( GetLastError() != ERROR_INSUFFICIENT_BUFFER )
		{
			return std::string();
		}
		
		/* According to the ISAPI documentation, GetServerVariable will set the DWORD param to the
		 * size of the buffer required to hold the value.
		 * 
		 * If it doesn't do that, there isn't much we can do about it, so just fail gracefully.
		 */
		buf.resize( size );

		if( this->_getServerVariable( this->_extensionControlBlock->ConnID, const_cast<char*>( name.c_str() ), buf.get(), &size ) )
		{
			std::string ret( buf.get(), size );
			return ret.substr( 0, ret.find_first_of( '\0' ) );
		}
		
		return std::string();
	}

	const std::string& ISAPIRequestImpl::getRequestURL()
	{
		return _requestURL;
	}
	
	const std::string& ISAPIRequestImpl::getRequestMethod()
	{
		return _requestMethod;
	}

	const std::string& ISAPIRequestImpl::getQueryString()
	{
		return _queryString;
	}
	
	const std::string& ISAPIRequestImpl::getScriptName()
	{
		return _scriptName;
	}
	
	const std::string& ISAPIRequestImpl::getContentType()
	{
		return _contentType;
	}
	
	DWORD ISAPIRequestImpl::getContentLength()
	{
		return _extensionControlBlock->cbTotalBytes;
	}
	
	std::string ISAPIRequestImpl::getRemoteAddress()
	{
		return _remoteAddress;
	}

	BOOL ISAPIRequestImpl::isSecureRequest()
	{
		return _isSecureRequest;
	}
	
	DWORD ISAPIRequestImpl::sendResponseHeader( const std::string& statusLine, BOOL keepConn )
	{
		if( _headersSent ) throw spep::InvalidStateException();
		
		//std::string headers;
		std::stringstream ss;
		for( std::vector<std::string>::const_iterator iter = _responseHeaders.begin(); iter != _responseHeaders.end(); ++iter )
		{
			std::size_t len = iter->find_first_of( '\0' );
			ss << iter->substr( 0, len ) << HTTP_HEADER_ENDLINE;
			//headers = headers + iter->substr( 0, len ) + HTTP_HEADER_ENDLINE;
		}
		ss << HTTP_HEADER_ENDLINE;
		std::string headers( ss.str() );
		//headers = headers + HTTP_HEADER_ENDLINE;
		
		HSE_SEND_HEADER_EX_INFO headerInfo;
		headerInfo.pszStatus = statusLine.c_str();
		headerInfo.pszHeader = headers.c_str();
		headerInfo.cchStatus = statusLine.length();
		headerInfo.cchHeader = headers.length();
		headerInfo.fKeepConn = keepConn;
		
		if( this->_serverSupportFunction(
				this->_extensionControlBlock->ConnID, 
				HSE_REQ_SEND_RESPONSE_HEADER_EX,
				&headerInfo,
				NULL,
				NULL
			) )
		{
			_headersSent = true;
			
			return HSE_STATUS_SUCCESS;
		}
		
		return HSE_STATUS_ERROR;
	}

	DWORD ISAPIRequestImpl::sendResponseDocument( const std::string& statusLine, const char *document, DWORD documentLength, const std::string& contentType )
	{
		std::stringstream ss;
		std::string currentHeader;
		
		// Strip the separator(s) off the end, if any.
		this->setHeader( CONTENT_TYPE_HEADER + contentType );
		
		ss << CONTENT_LENGTH_HEADER << documentLength;
		currentHeader = ss.str();
		this->setHeader( currentHeader );
		
		ss.str(std::string());
		
		DWORD contentLength = documentLength;
		
		if( !_headersSent )
		{
			DWORD result = this->sendResponseHeader( statusLine );
			if( result != HSE_STATUS_SUCCESS )
				return result;
		}
		
		return this->_writeClient( this->_extensionControlBlock->ConnID, (LPVOID)document, &contentLength, HSE_IO_SYNC );
	}
	
	DWORD ISAPIRequestImpl::sendErrorDocument( int errorCode, int minorCode )
	{
		const char *statusLine;
		const char *document;
		const char *contentType;
		DWORD contentLength;
		
		switch( errorCode )
		{
			case HTTP_FORBIDDEN:
			statusLine = HTTP_FORBIDDEN_STATUS_LINE;
			document = HTTP_FORBIDDEN_DOCUMENT;
			contentType = HTTP_FORBIDDEN_DOCUMENT_TYPE;
			break;
			
			case HTTP_METHOD_NOT_ALLOWED:
			statusLine = HTTP_METHOD_NOT_ALLOWED_STATUS_LINE;
			document = HTTP_METHOD_NOT_ALLOWED_DOCUMENT;
			contentType = HTTP_METHOD_NOT_ALLOWED_DOCUMENT_TYPE;
			break;
			
			case HTTP_SERVICE_UNAVAILABLE:
			statusLine = HTTP_SERVICE_UNAVAILABLE_STATUS_LINE;
			document = HTTP_SERVICE_UNAVAILABLE_DOCUMENT;
			contentType = HTTP_SERVICE_UNAVAILABLE_DOCUMENT_TYPE;
			break;
			
			case HTTP_INTERNAL_SERVER_ERROR:
			default:
			statusLine = HTTP_INTERNAL_SERVER_ERROR_STATUS_LINE;
			document = HTTP_INTERNAL_SERVER_ERROR_DOCUMENT;
			contentType = HTTP_INTERNAL_SERVER_ERROR_DOCUMENT_TYPE;
			break;
		}
		contentLength = strlen(document);
		
		HSE_CUSTOM_ERROR_INFO errorInfo;
		errorInfo.pszStatus = (CHAR*)statusLine;
		errorInfo.uHttpSubError = minorCode;
		errorInfo.fAsync = FALSE;
		if( this->_serverSupportFunction( this->_extensionControlBlock->ConnID, HSE_REQ_SEND_CUSTOM_ERROR, &errorInfo, NULL, NULL ) )
		{
			return HSE_STATUS_SUCCESS;
		}
		
		return this->sendResponseDocument( statusLine, document, contentLength, contentType );
	}
	
	DWORD ISAPIRequestImpl::sendRedirectResponse( const std::string& location )
	{
		std::string redirectHeader( std::string(REDIRECT_HEADER) + location );
		this->setHeader( redirectHeader );
		
		return this->sendResponseHeader( HTTP_REDIRECT_STATUS_LINE );
	}
	
	BOOL ISAPIRequestImpl::readRequestDocument( spep::CArray<char> &buffer, DWORD &size )
	{
		DWORD pos = 0, bytes = size, inc = size;
		if( this->_extensionControlBlock->cbAvailable > 0 )
		{
			size = this->_extensionControlBlock->cbAvailable;
			buffer.resize( size );
			
			std::memcpy( buffer.get(), this->_extensionControlBlock->lpbData, size );
			pos = size;
		}
		
		while( bytes != 0 )
		{
			buffer.resize( pos + inc );
			
			bytes = inc;
			if( !this->_readClient( this->_extensionControlBlock->ConnID, &(buffer[pos]), &bytes ) )
				return FALSE;
			
			pos += bytes;
			size = pos;
		}
		
		return TRUE;
	}
	
	VOID* ISAPIRequestImpl::allocMem( DWORD size )
	{
		LPVOID retval = malloc( size );
		_freeList.push_back( retval );
		return retval;
	}
	
	char* ISAPIRequestImpl::istrndup( const char *str, size_t len )
	{
		char *dst = static_cast<char*>( this->allocMem( len + 1 ) );
		std::strncpy( dst, str, len );
		dst[len] = '\0';
		return dst;
	}
	
	char *ISAPIRequestImpl::isprintf( const char *fmt, ... )
	{
		int size = strlen( fmt ) + 1;
		CArray<char> dst( size );
		
		// Yeah, but see below
		while(1)
		{
			int written;
			va_list vargs;
			va_start( vargs, fmt );
			written = vsnprintf( dst.get(), size, fmt, vargs );
			va_end( vargs );
			
			// We won't loop, because "size" is always growing, so eventually it will be big enough.
			if( written < 0 )
			{
				size = 2*size;
				dst.resize( size );
			}
			else
			{
				return this->istrndup( dst.get(), written );
			}
		}
	}
	
	LPEXTENSION_CONTROL_BLOCK ISAPIRequestImpl::getExtensionControlBlock()
	{
		return this->_extensionControlBlock;
	}
	
	void ISAPIRequestImpl::addRequestHeader( const std::string& name, const std::string& value )
	{
		if( _childHeaders.length() == 0 )
		{
			_childHeaders = this->getServerVariable( "ALL_RAW" );
		}
		
		_childHeaders = name + ": " + value + "\r\n" + _childHeaders;
	}
	
	DWORD ISAPIRequestImpl::continueRequest()
	{
		
		HSE_EXEC_URL_USER_INFO execUrlUserInfo;
		execUrlUserInfo.hImpersonationToken = NULL;
		execUrlUserInfo.pszCustomUserName = NULL;
		execUrlUserInfo.pszCustomAuthType = NULL;

		// Add auth info, if any.
		if( _remoteUser.length() != 0 )
		{
			execUrlUserInfo.pszCustomUserName = const_cast<char*>( _remoteUser.c_str() );
			execUrlUserInfo.pszCustomAuthType = const_cast<char*>( "SPEP" );
		}
		
		/* If we don't give the child request any new information
		 * it just gets it from the parent (i.e. this) request.
		 */
		HSE_EXEC_URL_INFO execUrlInfo;
		execUrlInfo.pszUrl = NULL;
		execUrlInfo.pszMethod = NULL;
		execUrlInfo.pszChildHeaders = NULL;
		
		// If we added our own headers... Put them in the child request.
		if( _childHeaders.length() != 0 )
		{
			execUrlInfo.pszChildHeaders = const_cast<char*>( _childHeaders.c_str() );
		}
		
		execUrlInfo.pUserInfo = NULL;
		if( execUrlUserInfo.pszCustomUserName != NULL )
		{
			execUrlInfo.pUserInfo = &execUrlUserInfo;
		}

		execUrlInfo.pEntity = NULL;
		// Aside from this.. we don't want to spin inside the SPEP logic forever.
		execUrlInfo.dwExecUrlFlags = HSE_EXEC_URL_IGNORE_CURRENT_INTERCEPTOR;
		
		// Give it a callback for when it's finished.
		if( this->_serverSupportFunction( 
				this->_extensionControlBlock->ConnID, 
				HSE_REQ_IO_COMPLETION, 
				spep::isapi::IsapiExecuteUrlHandleCompletion, 
				NULL, 
				(DWORD*)TRUE 
			) 
			== FALSE )
		{
			// Error condition. Something asploded.
			SetLastError( ERROR_INVALID_PARAMETER );
			return HSE_STATUS_ERROR;
		}
		
		// ExecuteUrl call.. make the child request.
		if( this->_serverSupportFunction( 
				this->_extensionControlBlock->ConnID, 
				HSE_REQ_EXEC_URL, 
				&execUrlInfo, 
				NULL, 
				NULL 
			) 
			== FALSE )
		{
			SetLastError( ERROR_INVALID_PARAMETER );
			return HSE_STATUS_SUCCESS;
		}
		
		// Return pending so the child request can execute asynchronously.
		return HSE_STATUS_PENDING;
	}
	
	void ISAPIRequestImpl::urlDecode( std::string& url )
	{
		// This algorithm adapted from Apache httpd(trunk-2.3) server/util.c 'unescape_url'
		
		std::size_t i, j;
		// Jump to the first % sign ... we don't need to process anything before that.
		i = url.find_first_of( '%' );
		
		for( j = i; i < url.length(); ++i, ++j )
		{
			if( url[i] != '%' )
			{
				url[j] = url[i];
			}
			else
			{
				char hi_ = tolower(url[i+1]);
				char lo_ = tolower(url[i+2]);
				int hi = URLDECODE_HEX2DEC( hi_ );
				int lo = URLDECODE_HEX2DEC( lo_ );
				if( hi == -1 || lo == -1 )
				{
					url[j] = url[i];
				}
				else
				{
					url[j] = ( 0x10 * hi ) + lo;
					i += 2;
				}
			}
		}
		url = url.substr( 0, j );
	}
	
	void ISAPIRequestImpl::setRemoteUser( const std::string& username )
	{
		_remoteUser = username;
	}

	void ISAPIRequestImpl::setRemoteAddress(const std::string& ipaddress)
	{
		_remoteAddress = ipaddress;
	}
	
} }
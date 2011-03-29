/* Copyright 2006, Queensland University of Technology
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
 * Creation Date: Oct 29, 2007
 * 
 * Purpose: 
 */

#ifndef ISAPIREQUEST_H_
#define ISAPIREQUEST_H_

#include <string>
#include <sstream>
#include <vector>

#define HTTP_HEADER_VALUE_DEFAULT_LENGTH 64
#define HTTP_HEADER_VARIABLE_PREFIX "HTTP_"

//#define ISAPI_HEADER_URL "url"
//#define ISAPI_HEADER_METHOD "method"
//#define ISAPI_HEADER_VERSION "version"

//#define HEADER_CONTENT_TYPE "Content-Type: "
//#define HEADER_CONTENT_LENGTH "Content-Length: "

#include <winsock2.h>
#include <windows.h>
#include <httpfilt.h>
#include <httpext.h>

#include "spep/Util.h"

#include "FilterConstants.h"

namespace spep{ namespace isapi{
	
	class ISAPIRequest
	{
		public:
		virtual ~ISAPIRequest(){}
		virtual std::string getHeader( const std::string &name ) = 0;
		virtual void setHeader( const std::string& headerValue ) = 0;
		virtual std::string getServerVariable( const std::string& name ) = 0;
		virtual const std::string& getRequestURL() = 0;
		virtual const std::string& getRequestMethod() = 0;
		virtual const std::string& getQueryString() = 0;
		virtual const std::string& getScriptName() = 0;
		virtual const std::string& getContentType() = 0;
		virtual std::string getRemoteAddress() = 0;
		virtual DWORD getContentLength() = 0;
		virtual BOOL isSecureRequest() = 0;
		virtual DWORD sendResponseHeader( const std::string& statusLine, BOOL keepConn = FALSE ) = 0;
		virtual DWORD sendResponseDocument( const std::string& statusLine, const char *document, DWORD documentLength, const std::string& contentType ) = 0;
		virtual DWORD sendErrorDocument( int errorCode, int minorCode = 0 ) = 0;
		virtual DWORD sendRedirectResponse( const std::string& location ) = 0;
		virtual BOOL readRequestDocument( spep::CArray<char> &buffer, DWORD &size ) = 0;
		virtual VOID* allocMem( DWORD size ) = 0;
		virtual char* istrndup( const char *str, size_t len ) = 0;
		virtual char *isprintf( const char *fmt, ... ) = 0;
		virtual LPEXTENSION_CONTROL_BLOCK getExtensionControlBlock() = 0;
		virtual void addRequestHeader( const std::string& name, const std::string& value ) = 0;
		virtual DWORD continueRequest() = 0;
		virtual void urlDecode( std::string& url ) = 0;
		virtual void setRemoteUser( const std::string& username ) = 0;
		virtual void setRemoteAddress(const std::string& ipaddress) = 0;
	};
	
	class ISAPIRequestImpl : public ISAPIRequest
	{
	    typedef BOOL (WINAPI * ISAPIServerSupportFunction) ( HCONN, DWORD, LPVOID, LPDWORD, LPDWORD );
	    typedef BOOL (WINAPI * ISAPIWriteClientFunction) ( HCONN, LPVOID, LPDWORD, DWORD );
	    typedef BOOL (WINAPI * ISAPIReadClientFunction) ( HCONN, LPVOID, LPDWORD );
	    typedef BOOL (WINAPI * ISAPIGetServerVariableFunction) ( HCONN, LPSTR, LPVOID, LPDWORD );
	    
		private:
		LPEXTENSION_CONTROL_BLOCK _extensionControlBlock;
		ISAPIServerSupportFunction _serverSupportFunction;
		ISAPIWriteClientFunction _writeClient;
		ISAPIReadClientFunction _readClient;
		ISAPIGetServerVariableFunction _getServerVariable;
		std::vector<LPVOID> _freeList;
		std::vector<std::string> _responseHeaders;
		std::string _requestURL;
		std::string _requestMethod;
		std::string _queryString;
		std::string _scriptName;
		std::string _contentType;
		std::string _remoteUser;
		std::string _remoteAddress;
		std::string _childHeaders;
		BOOL _isSecureRequest;
		DWORD _contentLength;
		
		bool _headersSent;
		
		public:
		ISAPIRequestImpl( LPEXTENSION_CONTROL_BLOCK extensionControlBlock );
		
		virtual ~ISAPIRequestImpl();
		
		virtual std::string getHeader( const std::string &name );
		
		virtual void setHeader( const std::string& headerValue );
		
		virtual std::string getServerVariable( const std::string& name );
		
		virtual const std::string& getRequestURL();
		
		virtual const std::string& getRequestMethod();

		virtual const std::string& getQueryString();
		
		virtual const std::string& getScriptName();
		
		virtual const std::string& getContentType();

		virtual std::string getRemoteAddress();

		virtual DWORD getContentLength();

		virtual BOOL isSecureRequest();

		virtual DWORD sendResponseHeader( const std::string& statusLine, BOOL keepConn = FALSE );

		virtual DWORD sendResponseDocument( const std::string& statusLine, const char *document, DWORD documentLength, const std::string& contentType );

		virtual DWORD sendErrorDocument( int errorCode, int minorCode = 0 );
		
		virtual DWORD sendRedirectResponse( const std::string& location );

		virtual BOOL readRequestDocument( spep::CArray<char> &buffer, DWORD &size );

		virtual VOID* allocMem( DWORD size );
		
		virtual char* istrndup( const char *str, size_t len );
		
		virtual char *isprintf( const char *fmt, ... );
		
		virtual LPEXTENSION_CONTROL_BLOCK getExtensionControlBlock();
		
		virtual void addRequestHeader( const std::string& name, const std::string& value );
		
		virtual DWORD continueRequest();
		
		virtual void urlDecode( std::string& url );
		
		virtual void setRemoteUser( const std::string& username );

		virtual void setRemoteAddress(const std::string& ipaddress);
	};
	
}}

#endif /*ISAPIREQUEST_H_*/

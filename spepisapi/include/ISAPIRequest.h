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

namespace spep{
    namespace isapi{

        class ISAPIRequest
        {
        public:
            virtual ~ISAPIRequest(){}
            virtual std::string getHeader(const std::string &name) = 0;
            virtual void setHeader(const std::string& headerValue) = 0;
            virtual std::string getServerVariable(const std::string& name) = 0;
            virtual std::string getRequestMethod() const = 0;
            virtual std::string getQueryString() const = 0;
            virtual std::string getScriptName() const = 0;
            virtual std::string getRequestURL() const = 0;
            virtual std::string getContentType() const = 0;
            virtual std::string getRemoteAddress() const = 0;
            virtual DWORD getContentLength() const = 0;
            virtual BOOL isSecureRequest() const = 0;
            virtual DWORD sendResponseHeader(const std::string& statusLine, BOOL keepConn = FALSE) = 0;
            virtual DWORD sendResponseDocument(const std::string& statusLine, const char *document, DWORD documentLength, const std::string& contentType) = 0;
            virtual DWORD sendErrorDocument(int errorCode, int minorCode = 0) = 0;
            virtual DWORD sendRedirectResponse(const std::string& location) = 0;
            virtual BOOL readRequestDocument(spep::CArray<char> &buffer, DWORD &size) = 0;
            virtual VOID* allocMem(DWORD size) = 0;
            virtual char* istrndup(const char *str, size_t len) = 0;
            virtual char *isprintf(const char *fmt, ...) = 0;
            virtual LPEXTENSION_CONTROL_BLOCK getExtensionControlBlock() = 0;
            virtual void addRequestHeader(const std::string& name, const std::string& value) = 0;
            virtual DWORD continueRequest() = 0;
            virtual void urlDecode(std::string& url) = 0;
            virtual void setRemoteUser(const std::string& username) = 0;
            virtual void setRemoteAddress(const std::string& ipaddress) = 0;
        };

        class ISAPIRequestImpl : public ISAPIRequest
        {
            typedef BOOL(WINAPI * ISAPIServerSupportFunction) (HCONN, DWORD, LPVOID, LPDWORD, LPDWORD);
            typedef BOOL(WINAPI * ISAPIWriteClientFunction) (HCONN, LPVOID, LPDWORD, DWORD);
            typedef BOOL(WINAPI * ISAPIReadClientFunction) (HCONN, LPVOID, LPDWORD);
            typedef BOOL(WINAPI * ISAPIGetServerVariableFunction) (HCONN, LPSTR, LPVOID, LPDWORD);

        public:
            ISAPIRequestImpl(LPEXTENSION_CONTROL_BLOCK extensionControlBlock);

            virtual ~ISAPIRequestImpl();

            virtual std::string getHeader(const std::string &name) override;
            virtual void setHeader(const std::string& headerValue) override;
            virtual std::string getServerVariable(const std::string& name) override;
            virtual std::string getRequestURL() const override;
            virtual std::string getRequestMethod() const override;
            virtual std::string getQueryString() const override;
            virtual std::string getScriptName() const override;
            virtual std::string getContentType() const override;
            virtual std::string getRemoteAddress() const override;
            virtual DWORD getContentLength() const override;
            virtual BOOL isSecureRequest() const override;
            virtual DWORD sendResponseHeader(const std::string& statusLine, BOOL keepConn = FALSE) override;
            virtual DWORD sendResponseDocument(const std::string& statusLine, const char *document, DWORD documentLength, const std::string& contentType) override;
            virtual DWORD sendErrorDocument(int errorCode, int minorCode = 0) override;
            virtual DWORD sendRedirectResponse(const std::string& location) override;
            virtual BOOL readRequestDocument(spep::CArray<char> &buffer, DWORD &size) override;
            virtual VOID* allocMem(DWORD size) override;
            virtual char* istrndup(const char *str, size_t len) override;
            virtual char *isprintf(const char *fmt, ...) override;
            virtual LPEXTENSION_CONTROL_BLOCK getExtensionControlBlock() override;
            virtual void addRequestHeader(const std::string& name, const std::string& value) override;
            virtual DWORD continueRequest() override;
            virtual void urlDecode(std::string& url) override;
            virtual void setRemoteUser(const std::string& username) override;
            virtual void setRemoteAddress(const std::string& ipaddress) override;

        private:
            LPEXTENSION_CONTROL_BLOCK mExtensionControlBlock;
            ISAPIServerSupportFunction mServerSupportFunction;
            ISAPIWriteClientFunction mWriteClient;
            ISAPIReadClientFunction mReadClient;
            ISAPIGetServerVariableFunction mGetServerVariable;
            std::vector<LPVOID> mFreeList;
            std::vector<std::string> mResponseHeaders;
            std::string mRequestURL;
            std::string mRequestMethod;
            std::string mQueryString;
            std::string mScriptName;
            std::string mContentType;
            std::string mRemoteUser;
            std::string mRemoteAddress;
            std::string mChildHeaders;
            BOOL mIsSecureRequest;
            DWORD mContentLength;

            bool mHeadersSent;
        };

    }
}

#endif /*ISAPIREQUEST_H_*/

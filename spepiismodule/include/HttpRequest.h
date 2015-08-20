/* Copyright 2015, Queensland University of Technology
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
*/
#ifndef HTTPREQUEST_H_
#define HTTPREQUEST_H_

#include "spep/Util.h"

#include <string>
#include <sstream>
#include <vector>
#include <unordered_map>

class IHttpContext;
class IHttpRequest;
class IHttpResponse;


namespace spep{
namespace isapi{

enum class RequestResultStatus {
	STATUS_ERROR = 0,
	STATUS_SUCCESS,
	STATUS_SUCCESS_AND_FINISH_REQUEST,
	STATUS_SUCCESS_AND_KEEP_CONN,
};


class HttpRequest
{
public:
	HttpRequest(IHttpContext *pHttpContext);
	~HttpRequest();

	std::string getHeader(const std::string &name);
	void setHeader(const std::string& headerName, const std::string& headerValue);
	std::string getServerVariable(const std::string& name);
	std::string getRequestURL() const;
	std::string getRequestMethod() const;
	std::string getQueryString() const;
	std::string getScriptName() const;
	std::string getContentType() const;
	std::string getRemoteAddress() const;
	size_t getContentLength() const;
	bool isSecureRequest() const;
	IHttpContext *getHttpContext();
	void setRemoteUser(const std::string& username);
	void setRemoteAddress(const std::string& ipaddress);

	//!< Main functions for reading the HTTP request body and returing the data in whatever array format you need
	//!< Read the HTTP request body
	bool readRequestDocument(spep::CArray<char>& buffer, size_t& size);
	//!< Read the HTTP request body
	std::pair<char*, size_t> readRequestDocument();
	//!< Read the HTTP request body
	std::vector<char> readRequestBody();
	//!< Read the HTTP request body
	std::string readRequestBodyAsString();

	//!< Add a header to the current HTTP request
	void addRequestHeader(const std::string& name, const std::string& value);

	//!< HTTP Repsonse processing. TODO: this should be moved into a HttpResponse class
	RequestResultStatus sendResponseHeader(int statuscode, const std::string& statusLine, bool keepConn = FALSE);
	RequestResultStatus sendResponseDocument(int statuscode, const std::string& statusLine, const char *document, size_t documentLength, const std::string& contentType);
	RequestResultStatus sendErrorDocument(int errorCode, int minorCode = 0);
	RequestResultStatus sendRedirectResponse(const std::string& location);
	RequestResultStatus continueRequest();

	char* istrndup(const char *str, size_t len);
	char *isprintf(const char *fmt, ...);
	void urlDecode(std::string& url);
	
private:

	void* allocMem(size_t size);

	IHttpContext* mHttpContext;
	IHttpRequest* mHttpRequest;
	IHttpResponse* mHttpResponse;

	std::vector<void*> mFreeList;
	std::unordered_map<std::string, std::string> mResponseHeaders;
	std::string mRequestURL;
	std::string mRequestMethod;
	std::string mQueryString;
	std::string mScriptName;
	std::string mContentType;
	std::string mRemoteUser;
	std::string mRemoteAddress;
	std::vector<std::pair<std::string, std::string>> mChildHeaders;
	bool mIsSecureRequest;
	size_t mContentLength;
	bool mHeadersSent;
};
}
}
#endif /*HTTPREQUEST_H_*/
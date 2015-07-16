#ifndef HTTPREQUEST_H_
#define HTTPREQUEST_H_

#include <string>
#include <sstream>
#include <vector>
#include <unordered_map>

#define _WINSOCKAPI_
#include <windows.h>
#include <sal.h>
#include <httpserv.h>

#include "spep/Util.h"

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
	DWORD getContentLength() const;
	bool isSecureRequest() const;
	IHttpContext *getHttpContext();
	void setRemoteUser(const std::string& username);
	void setRemoteAddress(const std::string& ipaddress);

	//!< Main functions for reading the HTTP request body and returing the data in whatever array format you need
	//!< Read the HTTP request body
	bool readRequestDocument(spep::CArray<char>& buffer, DWORD& size);
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
	RequestResultStatus sendResponseDocument(int statuscode, const std::string& statusLine, const char *document, DWORD documentLength, const std::string& contentType);
	RequestResultStatus sendErrorDocument(int errorCode, int minorCode = 0);
	RequestResultStatus sendRedirectResponse(const std::string& location);


	VOID* allocMem(DWORD size);
	char* istrndup(const char *str, size_t len);
	char *isprintf(const char *fmt, ...);
	RequestResultStatus continueRequest();
	void urlDecode(std::string& url);
	
private:
	IHttpContext* mHttpContext;
	IHttpRequest* mHttpRequest;
	IHttpResponse* mHttpResponse;

	std::vector<LPVOID> mFreeList;
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
	DWORD mContentLength;
	bool mHeadersSent;
};
}
}
#endif /*HTTPREQUEST_H_*/
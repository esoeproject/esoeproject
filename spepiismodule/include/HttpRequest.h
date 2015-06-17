#ifndef HTTPREQUEST_H_
#define HTTPREQUEST_H_

#include <string>
#include <sstream>
#include <vector>
#include <unordered_map>

#define HTTP_HEADER_VALUE_DEFAULT_LENGTH 64
#define HTTP_HEADER_VARIABLE_PREFIX "HTTP_"

//#include <winsock2.h>
#define _WINSOCKAPI_
#include <windows.h>
#include <sal.h>
#include <httpserv.h>

#include "spep/Util.h"

#include "FilterConstants.h"

namespace spep{
	namespace isapi{
		class HttpRequest
		{
		public:
			virtual ~HttpRequest(){}
			virtual std::string getHeader(const std::string &name) = 0;
			virtual void setHeader(const std::string& headerName, const std::string& headerValue) = 0;
			virtual std::string getServerVariable(const std::string& name) = 0;
			virtual std::string getRequestMethod() const = 0;
			virtual std::string getQueryString() const = 0;
			virtual std::string getScriptName() const = 0;
			virtual std::string getRequestURL() const = 0;
			virtual std::string getContentType() const = 0;
			virtual std::string getRemoteAddress() const = 0;
			virtual DWORD getContentLength() const = 0;
			virtual BOOL isSecureRequest() const = 0;
			virtual IHttpContext *getHttpContext() = 0;
			virtual DWORD sendResponseHeader(int statuscode, const std::string& statusLine, BOOL keepConn = FALSE) = 0;
			virtual DWORD sendResponseDocument(int statuscode, const std::string& statusLine, const char *document, DWORD documentLength, const std::string& contentType) = 0;
			virtual DWORD sendErrorDocument(int errorCode, int minorCode = 0) = 0;
			virtual DWORD sendRedirectResponse(const std::string& location) = 0;
			virtual BOOL readRequestDocument(spep::CArray<char> &buffer, DWORD &size) = 0;
			virtual VOID* allocMem(DWORD size) = 0;
			virtual char* istrndup(const char *str, size_t len) = 0;
			virtual char *isprintf(const char *fmt, ...) = 0;
			virtual void addRequestHeader(const std::string& name, const std::string& value) = 0;
			virtual DWORD continueRequest() = 0;
			virtual void urlDecode(std::string& url) = 0;
			virtual std::wstring convertStrToWString(const std::string& str) = 0;
			virtual std::string convertWStrToString(const std::wstring& str) = 0;
			virtual void setRemoteUser(const std::string& username) = 0;
			virtual void setRemoteAddress(const std::string& ipaddress) = 0;
			
		};

		class HttpRequestImpl : public HttpRequest
		{

		public:
			HttpRequestImpl(IHttpContext *pHttpContext);
			
			virtual ~HttpRequestImpl();

			virtual std::string getHeader(const std::string &name) override;
			virtual void setHeader(const std::string& headerName, const std::string& headerValue) override;
			virtual std::string getServerVariable(const std::string& name) override;
			virtual std::string getRequestURL() const override;
			virtual std::string getRequestMethod() const override;
			virtual std::string getQueryString() const override;
			virtual std::string getScriptName() const override;
			virtual std::string getContentType() const override;
			virtual std::string getRemoteAddress() const override;
			virtual DWORD getContentLength() const override;
			virtual BOOL isSecureRequest() const override;
			virtual IHttpContext *getHttpContext() override;
			virtual void setRemoteUser(const std::string& username) override;
			virtual void setRemoteAddress(const std::string& ipaddress) override;

			virtual DWORD sendResponseHeader(int statuscode, const std::string& statusLine, BOOL keepConn = FALSE) override;
			virtual DWORD sendResponseDocument(int statuscode, const std::string& statusLine, const char *document, DWORD documentLength, const std::string& contentType) override;
			virtual DWORD sendErrorDocument(int errorCode, int minorCode = 0) override;
			virtual DWORD sendRedirectResponse(const std::string& location) override;
			virtual BOOL readRequestDocument(spep::CArray<char> &buffer, DWORD &size) override;
			virtual void addRequestHeader(const std::string& name, const std::string& value) override;

			virtual VOID* allocMem(DWORD size) override;
			virtual char* istrndup(const char *str, size_t len) override;
			virtual char *isprintf(const char *fmt, ...) override;
			virtual DWORD continueRequest() override;
			virtual void urlDecode(std::string& url) override;
			virtual std::wstring convertStrToWString(const std::string& str) override;
			virtual std::string convertWStrToString(const std::wstring& str) override;

			

		private:
			IHttpContext * httpContext;
			std::vector<LPVOID> mFreeList;
			std::unordered_map<std::string, std::string> mResponseHeaders;
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
#endif /*HTTPREQUEST_H_*/
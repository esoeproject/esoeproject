#include "HttpRequest.h"
#include "FilterConstants.h"

#include "spep/exceptions/InvalidStateException.h"

#include <ctype.h>
#include <AtlBase.h>
#include <utility>

#include <boost/lexical_cast.hpp>

// This is only safe for direct variable parameters
// Any expression with a side effect, such as a ++ or a function call should NOT be passed into this macro.
#define URLDECODE_HEX2DEC(x) ( ( x <= 'f' && x >= 'a' ) ? ( 10 + x - 'a' ) : ( x <= '9' && x >= '0' ) ? ( x - '0' ) : -1 )

namespace spep {
	namespace isapi {

		HttpRequestImpl::HttpRequestImpl(IHttpContext * pHttpContext) :
			httpContext(pHttpContext),
			mRequestURL(),
			mRequestMethod(pHttpContext->GetRequest()->GetHttpMethod()),
			mQueryString(convertWStrToString(pHttpContext->GetRequest()->GetRawHttpRequest()->CookedUrl.pQueryString)),
			mScriptName(),
			mContentType(),
			mContentLength(0),
			mHeadersSent(false)
		{
			mRequestURL = getServerVariable("URL");
			mScriptName = getServerVariable("SCRIPT_NAME");
			mContentType = getServerVariable("CONTENT_TYPE");
			mRemoteAddress = getServerVariable("REMOTE_ADDR");
			mIsSecureRequest = (getServerVariable("SERVER_PORT_SECURE").compare(std::string("1")) == 0);
			try
			{
				mContentLength = boost::lexical_cast<DWORD>(getServerVariable("CONTENT_LENGTH"));
			}
			catch (boost::bad_lexical_cast&)
			{
			}
		}

		HttpRequestImpl::~HttpRequestImpl(){
			for (auto iter = mFreeList.begin(); iter != mFreeList.end(); ++iter)
			{
				free(*iter);
			}
		}

		std::string HttpRequestImpl::getHeader(const std::string &name){
			std::string header;

			PCSTR headerValue;
			USHORT buf;
			headerValue = httpContext->GetRequest()->GetHeader(name.c_str(), &buf);
			if (buf) {
				header = std::string(headerValue);
			}
			return header;
		}

		void HttpRequestImpl::setHeader(const std::string& headerName, const std::string& headerValue){
			if (mHeadersSent)
				throw spep::InvalidStateException();

			mResponseHeaders.insert(std::make_pair<const std::string&, const std::string&>(headerName, headerValue));
		}

		std::string HttpRequestImpl::getServerVariable(const std::string& name){

			// Create an HRESULT to receive return values from methods.
			HRESULT hr;

			DWORD size = 128;
			std::string variableValue;
			PCWSTR returnString;

			hr = httpContext->GetServerVariable(name.c_str(), &returnString, &size);

			if (!FAILED(hr)){
				variableValue = convertWStrToString(returnString);
			}
			return variableValue;
		}

		IHttpContext * HttpRequestImpl::getHttpContext(){
			return httpContext;
		}

		std::string HttpRequestImpl::getRequestURL() const
		{
			return mRequestURL;
		}

		std::string HttpRequestImpl::getRequestMethod() const
		{
			return mRequestMethod;
		}

		std::string HttpRequestImpl::getQueryString() const
		{
			return mQueryString;
		}

		std::string HttpRequestImpl::getScriptName() const
		{
			return mScriptName;
		}

		std::string HttpRequestImpl::getContentType() const
		{
			return mContentType;
		}

		DWORD HttpRequestImpl::getContentLength() const
		{
			return mContentLength; //ask why?
		}

		std::string HttpRequestImpl::getRemoteAddress() const
		{
			return mRemoteAddress;
		}

		BOOL HttpRequestImpl::isSecureRequest() const
		{
			return mIsSecureRequest;
		}

		void HttpRequestImpl::setRemoteUser(const std::string& username)
		{
			mRemoteUser = username;
		}

		void HttpRequestImpl::setRemoteAddress(const std::string& ipaddress)
		{
			mRemoteAddress = ipaddress;
		}


		DWORD HttpRequestImpl::sendResponseHeader(int statuscode, const std::string& statusLine, BOOL keepConn)
		{
			if (mHeadersSent) throw spep::InvalidStateException();
			HRESULT hr;
			BOOL errorOccured = false;

			for (const auto& it: mResponseHeaders)
			{
				hr = httpContext->GetResponse()->SetHeader(it.first.c_str(),it.second.c_str(), (USHORT)(it.second.size()), true);
				if (FAILED(hr)){
					errorOccured = true;
				}
			}

			hr = httpContext->GetResponse()->SetStatus(statuscode, statusLine.c_str(), 0, S_OK);
			if (FAILED(hr)){
				errorOccured = true;
			}
			if (errorOccured){
				return HSE_STATUS_ERROR;
			}
			else{
				mHeadersSent = true;
			}
			return HSE_STATUS_SUCCESS;
		}

		DWORD HttpRequestImpl::sendResponseDocument(int statuscode, const std::string& statusLine, const char *document, DWORD documentLength, const std::string& contentType)
		{
			HRESULT hr;

			setHeader(CONTENT_TYPE_HEADER, contentType);
			setHeader(CONTENT_LENGTH_HEADER, std::to_string(documentLength));

			if (!mHeadersSent)
			{
				DWORD result = sendResponseHeader(statuscode, statusLine);
				if (result != HSE_STATUS_SUCCESS)
					return result;
			}

			HTTP_DATA_CHUNK dc;
			dc.DataChunkType = HttpDataChunkFromMemory;
			dc.FromMemory.BufferLength = documentLength;
			dc.FromMemory.pBuffer = httpContext->AllocateRequestMemory(documentLength + 1);

			if (!dc.FromMemory.pBuffer){
				return HSE_STATUS_ERROR;
			}
			char *p = static_cast<char *>(dc.FromMemory.pBuffer);
			strcpy_s(p, documentLength + 1, document);

			hr = httpContext->GetResponse()->WriteEntityChunkByReference(&dc, -1);

			if (FAILED(hr)){
				return HSE_STATUS_ERROR;
			}
			return HSE_STATUS_SUCCESS;
		}

		DWORD HttpRequestImpl::sendErrorDocument(int errorCode, int minorCode)
		{
			HRESULT hr;
			const char *statusLine;
			const char *document;
			const char *contentType;
			DWORD contentLength;

			switch (errorCode)
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
			//check this
			hr = httpContext->GetResponse()->SetStatus(errorCode, statusLine, 0, S_OK);

			if (!FAILED(hr))
			{
				return HSE_STATUS_SUCCESS;
			}

			return sendResponseDocument(errorCode, statusLine, document, contentLength, contentType);
		}

		DWORD HttpRequestImpl::sendRedirectResponse(const std::string& location)
		{
			setHeader(std::string(REDIRECT_HEADER), location);
//check this
			return sendResponseHeader(HTTP_REDIRECT, HTTP_REDIRECT_STATUS_LINE);
		}

		BOOL HttpRequestImpl::readRequestDocument(spep::CArray<char> &buffer, DWORD &size)
		{
			HRESULT hr;
			DWORD bytesReceived = 1024;

			if (httpContext->GetRequest()->GetRemainingEntityBytes() > 0)
			{
				while (httpContext->GetRequest()->GetRemainingEntityBytes() != 0)
				{
					hr = httpContext->GetRequest()->ReadEntityBody(buffer.get(), size, false, &bytesReceived, NULL);

					if (FAILED(hr))
					{
						return HSE_STATUS_ERROR;
					}
				}
			}

			return TRUE;
		}

		void HttpRequestImpl::addRequestHeader(const std::string& name, const std::string& value)
		{
			if (mChildHeaders.empty())
			{
				mChildHeaders = getServerVariable("ALL_RAW");
			}

			mChildHeaders = name + ": " + value + "\r\n" + mChildHeaders;
		}

		std::wstring HttpRequestImpl::convertStrToWString(const std::string& str){
			CA2W ca2wstr(str.c_str());
			std::wstring returnwstr = ca2wstr;
			return returnwstr;
		}

		std::string HttpRequestImpl::convertWStrToString(const std::wstring& str){
			CW2A cw2astr(str.c_str());
			std::string returnstr = cw2astr;
			return returnstr;
		}

		VOID* HttpRequestImpl::allocMem(DWORD size)
		{
			LPVOID retval = malloc(size);
			mFreeList.push_back(retval); 
			return retval;
		}

		char* HttpRequestImpl::istrndup(const char *str, size_t len)
		{
			char *dst = static_cast<char*>(allocMem(len + 1));
			std::strncpy(dst, str, len);
			dst[len] = '\0';
			return dst;
		}

		char *HttpRequestImpl::isprintf(const char *fmt, ...)
		{
			int size = strlen(fmt) + 1;
			CArray<char> dst(size);

			// Yeah, but see below
			while (1)
			{
				int written;
				va_list vargs;
				va_start(vargs, fmt);
				written = vsnprintf(dst.get(), size, fmt, vargs);
				va_end(vargs);

				// We won't loop, because "size" is always growing, so eventually it will be big enough.
				if (written < 0)
				{
					size = 2 * size;
					dst.resize(size);
				}
				else
				{
					return istrndup(dst.get(), written);
				}
			}
		}

		void HttpRequestImpl::urlDecode(std::string& url)
		{
			// This algorithm adapted from Apache httpd(trunk-2.3) server/util.c 'unescape_url'

			std::size_t i, j;
			// Jump to the first % sign ... we don't need to process anything before that.
			i = url.find_first_of('%');

			for (j = i; i < url.length(); ++i, ++j)
			{
				if (url[i] != '%')
				{
					url[j] = url[i];
				}
				else
				{
					char hi_ = tolower(url[i + 1]);
					char lo_ = tolower(url[i + 2]);
					int hi = URLDECODE_HEX2DEC(hi_);
					int lo = URLDECODE_HEX2DEC(lo_);
					if (hi == -1 || lo == -1)
					{
						url[j] = url[i];
					}
					else
					{
						url[j] = (0x10 * hi) + lo;
						i += 2;
					}
				}
			}
			url = url.substr(0, j);
		}
	}
}
#include <ctype.h>
#include <AtlBase.h>
#include <utility>
#include <codecvt>
#include <boost/lexical_cast.hpp>

#include "HttpRequest.h"
#include "FilterConstants.h"
#include "spep/exceptions/InvalidStateException.h"


// This is only safe for direct variable parameters
// Any expression with a side effect, such as a ++ or a function call should NOT be passed into this macro.
#define URLDECODE_HEX2DEC(x) ( ( x <= 'f' && x >= 'a' ) ? ( 10 + x - 'a' ) : ( x <= '9' && x >= '0' ) ? ( x - '0' ) : -1 )

namespace spep {
namespace isapi {

static std::wstring convertStrToWString(const char* str) {
	if (!str) {
		return std::wstring();
	}
	CA2W ca2wstr(str);
	std::wstring returnwstr = ca2wstr;
	return returnwstr;
}

static std::string convertWStrToString(const wchar_t* str) {
	if (!str) {
		return std::string();
	}
	CW2A cw2astr(str);
	std::string returnstr = cw2astr;
	return returnstr;
}


std::wstring s2ws(const std::string& str)
{
	typedef std::codecvt_utf8<wchar_t> convert_typeX;
	std::wstring_convert<convert_typeX, wchar_t> converterX;

	return converterX.from_bytes(str);
}

std::string ws2s(const std::wstring& wstr)
{
	typedef std::codecvt_utf8<wchar_t> convert_typeX;
	std::wstring_convert<convert_typeX, wchar_t> converterX;

	return converterX.to_bytes(wstr);
}


HttpRequest::HttpRequest(IHttpContext* pHttpContext) :
mHttpContext(pHttpContext),
mContentLength(0),
mHeadersSent(false)
{
	if (!pHttpContext) {
		throw InvalidStateException();
	}

	mHttpRequest = pHttpContext->GetRequest();

	if (!mHttpRequest) {
		throw InvalidStateException();
	}

	mHttpResponse = pHttpContext->GetResponse();

	if (!mHttpResponse) {
		throw InvalidStateException();
	}

	// Get the raw HTTP request.
	HTTP_REQUEST* pRawRequest = mHttpRequest->GetRawHttpRequest();

	if (!pRawRequest) {
		throw InvalidStateException();
	}

	mQueryString = convertWStrToString(pRawRequest->CookedUrl.pQueryString);
	if (!mQueryString.empty()) {
		mQueryString.erase(mQueryString.begin()); // remove the leading '?' character
	}

	mRequestMethod = std::string(mHttpRequest->GetHttpMethod());
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

	// Clear the existing response.
	mHttpResponse->Clear();
}

HttpRequest::~HttpRequest() {
	for (auto iter = mFreeList.begin(); iter != mFreeList.end(); ++iter)
	{
		free(*iter);
	}
}

std::string HttpRequest::getHeader(const std::string &name) {
	PCSTR headerValue = nullptr;
	USHORT buf = 0;
	headerValue = mHttpContext->GetRequest()->GetHeader(name.c_str(), &buf);
	if (buf) {
		return std::string(headerValue);
	}
	return std::string();
}

void HttpRequest::setHeader(const std::string& headerName, const std::string& headerValue) {
	if (mHeadersSent)
		throw spep::InvalidStateException();

	mResponseHeaders.insert(std::make_pair<std::string, std::string>(std::string(headerName), std::string(headerValue)));
}

std::string HttpRequest::getServerVariable(const std::string& name) {
	HRESULT hr;
	DWORD size;
	PCSTR returnString;

	hr = mHttpContext->GetServerVariable(name.c_str(), &returnString, &size);

	if (!FAILED(hr)) {
		return std::string(returnString);
	}
	return std::string();
}

IHttpContext* HttpRequest::getHttpContext() {
	return mHttpContext;
}

std::string HttpRequest::getRequestURL() const {
	return mRequestURL;
}

std::string HttpRequest::getRequestMethod() const {
	return mRequestMethod;
}

std::string HttpRequest::getQueryString() const {
	return mQueryString;
}

std::string HttpRequest::getScriptName() const {
	return mScriptName;
}

std::string HttpRequest::getContentType() const {
	return mContentType;
}

DWORD HttpRequest::getContentLength() const {
	return mContentLength;
}

std::string HttpRequest::getRemoteAddress() const {
	return mRemoteAddress;
}

bool HttpRequest::isSecureRequest() const {
	return mIsSecureRequest;
}

void HttpRequest::setRemoteUser(const std::string& username) {
	mRemoteUser = username;
}

void HttpRequest::setRemoteAddress(const std::string& ipaddress) {
	mRemoteAddress = ipaddress;
}


/*!  Read the http request body and populate the provided buffer and size attributes */
bool HttpRequest::readRequestDocument(spep::CArray<char>& buffer, DWORD& size) {
	
	const size_t initialBufferSize = mHttpRequest->GetRemainingEntityBytes();

	if (initialBufferSize > 0)	{
		DWORD pos = 0, bytesReceived = size, inc = size;
		buffer.resize(initialBufferSize);

		while (mHttpRequest->GetRemainingEntityBytes() != 0)
		{
			bytesReceived = inc;

			HRESULT hr = mHttpRequest->ReadEntityBody(&(buffer[pos]), size, false, &bytesReceived);

			if (FAILED(hr))	{
				if (ERROR_HANDLE_EOF != (hr & 0x0000FFFF)) {
					return false;
				}
			}

			pos += bytesReceived;
			size = pos;
		}
	}

	return true;
}

/*! */
std::pair<char*, size_t> HttpRequest::readRequestDocument() {

	const size_t initialBufferSize = mHttpRequest->GetRemainingEntityBytes();
	

	if (initialBufferSize > 0)	{
		auto size = size_t{ initialBufferSize };
		DWORD pos = 0, bytesReceived = size, inc = size;
		
		auto buffer = new char[initialBufferSize];
		std::memset(buffer, 0, initialBufferSize);

		while (mHttpRequest->GetRemainingEntityBytes() != 0)
		{
			//buffer.resize(pos + inc);
			bytesReceived = inc;

			HRESULT hr = mHttpRequest->ReadEntityBody(&(buffer[pos]), size, false, &bytesReceived);

			if (FAILED(hr))	{
				if (ERROR_HANDLE_EOF != (hr & 0x0000FFFF)) {
					return std::make_pair(nullptr, 0);
				}
			}

			pos += bytesReceived;
			size = pos;
		}

		return std::make_pair(buffer, size);
	}

	return std::make_pair(nullptr, 0);
}


/*! */
std::vector<char> HttpRequest::readRequestBody() {
	
	const size_t initialBufferSize = mHttpRequest->GetRemainingEntityBytes();
	auto size = size_t{ initialBufferSize };
	std::vector<char> buffer(initialBufferSize, '\0');

	if (initialBufferSize > 0)	{
		DWORD pos = 0, bytesReceived = size, inc = size;
		
		while (mHttpRequest->GetRemainingEntityBytes() != 0)
		{
			bytesReceived = inc;

			HRESULT hr = mHttpRequest->ReadEntityBody(&(buffer[pos]), size, false, &bytesReceived);

			if (FAILED(hr))	{
				if (ERROR_HANDLE_EOF != (hr & 0x0000FFFF)) {
					//return false;
					return std::vector<char>();
				}
			}

			pos += bytesReceived;
			size = pos;
		}
	}

	return buffer;
}

/*! */
std::string HttpRequest::readRequestBodyAsString() {

	const size_t initialBufferSize = mHttpRequest->GetRemainingEntityBytes();
	auto size = size_t{ initialBufferSize };
	std::string buffer(initialBufferSize, '\0');

	if (initialBufferSize > 0)	{
		DWORD pos = 0, bytesReceived = size, inc = size;

		while (mHttpRequest->GetRemainingEntityBytes() != 0)
		{
			bytesReceived = inc;

			HRESULT hr = mHttpRequest->ReadEntityBody(&(buffer[pos]), size, false, &bytesReceived);

			if (FAILED(hr))	{
				if (ERROR_HANDLE_EOF != (hr & 0x0000FFFF)) {
					//return false;
					return std::string();
				}
			}

			pos += bytesReceived;
			size = pos;
		}
	}

	return buffer;
}

RequestResultStatus HttpRequest::continueRequest()
{
	// Set remote user if we have one
	if (!mRemoteUser.empty()) {
		HRESULT hr = mHttpRequest->SetHeader("REMOTE_USER", mRemoteUser.c_str(), (USHORT)mRemoteUser.size(), true);

		// Test for an error.
		if (FAILED(hr))
		{
			// Set the error status.
			//pProvider->SetErrorStatus(hr);
			// End additional processing.
			return RequestResultStatus::STATUS_ERROR;
		}
	}

	// Set any headers if we have them
	for (const auto& header : mChildHeaders) {
		HRESULT hr = mHttpRequest->SetHeader(header.first.c_str(), header.second.c_str(), (USHORT)header.second.size(), true);

		// Test for an error.
		if (FAILED(hr))
		{
			// Set the error status.
			//pProvider->SetErrorStatus(hr);
			// End additional processing.
			return RequestResultStatus::STATUS_ERROR;
		}
	}
		
	return RequestResultStatus::STATUS_SUCCESS;
}

void HttpRequest::addRequestHeader(const std::string& name, const std::string& value) {
	mChildHeaders.push_back(std::make_pair(name, value));
}


RequestResultStatus HttpRequest::sendResponseHeader(int statuscode, const std::string& statusLine, bool keepConn) {
	if (mHeadersSent)
		throw spep::InvalidStateException();


	for (const auto& it : mResponseHeaders)
	{
		HRESULT hr = mHttpResponse->SetHeader(it.first.c_str(), it.second.c_str(), (USHORT)(it.second.size()), false);
		if (FAILED(hr)) {
			return RequestResultStatus::STATUS_ERROR;
		}
	}

	HRESULT hr = mHttpResponse->SetStatus(statuscode, statusLine.c_str(), 0, S_OK);
	if (FAILED(hr)) {
		mHeadersSent = false;
		return RequestResultStatus::STATUS_ERROR;
	}

	mHeadersSent = true;

	return RequestResultStatus::STATUS_SUCCESS;
}

RequestResultStatus HttpRequest::sendResponseDocument(int statuscode, const std::string& statusLine, const char *document, DWORD documentLength, const std::string& contentType) {

	//setHeader(CONTENT_TYPE_HEADER, contentType);
	//setHeader(CONTENT_LENGTH_HEADER, std::to_string(documentLength));

	mHttpResponse->SetHeader(HttpHeaderContentType, contentType.c_str(), (USHORT)contentType.size(), true);
	mHttpResponse->SetHeader(HttpHeaderContentLength, std::to_string(documentLength).c_str(), (USHORT)documentLength, true);

	if (!mHeadersSent) {
		RequestResultStatus result = sendResponseHeader(statuscode, statusLine);
		if (result != RequestResultStatus::STATUS_SUCCESS)
			return result;
	}

	HTTP_DATA_CHUNK dc;
	dc.DataChunkType = HttpDataChunkFromMemory;
	dc.FromMemory.BufferLength = documentLength;
	dc.FromMemory.pBuffer = mHttpContext->AllocateRequestMemory(documentLength + 1);

	if (!dc.FromMemory.pBuffer) {
		return RequestResultStatus::STATUS_ERROR;
	}

	// documentLength + 1 does not seem to be correct to copy (crashes?)
	std::memcpy(dc.FromMemory.pBuffer, document, documentLength);

	HRESULT hr = mHttpResponse->WriteEntityChunkByReference(&dc, -1);

	if (FAILED(hr)) {
		return RequestResultStatus::STATUS_ERROR;
	}
	return RequestResultStatus::STATUS_SUCCESS_AND_FINISH_REQUEST;
}

RequestResultStatus HttpRequest::sendErrorDocument(int errorCode, int minorCode) {

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
	HRESULT hr = mHttpResponse->SetStatus(errorCode, statusLine, 0, S_OK);

	if (!FAILED(hr)) {
		return RequestResultStatus::STATUS_SUCCESS;
	}

	return sendResponseDocument(errorCode, statusLine, document, contentLength, contentType);
}

RequestResultStatus HttpRequest::sendRedirectResponse(const std::string& location) {
	setHeader("Location", location);
	//check this
	RequestResultStatus status = sendResponseHeader(HTTP_REDIRECT, HTTP_REDIRECT_STATUS_LINE);

	if (status != RequestResultStatus::STATUS_SUCCESS) {
		return RequestResultStatus::STATUS_ERROR;
	}

	return RequestResultStatus::STATUS_SUCCESS_AND_FINISH_REQUEST;
}


VOID* HttpRequest::allocMem(DWORD size) {
	LPVOID retval = malloc(size);
	mFreeList.push_back(retval);
	return retval;
}

char* HttpRequest::istrndup(const char *str, size_t len) {
	char *dst = static_cast<char*>(allocMem(len + 1));
	std::strncpy(dst, str, len);
	dst[len] = '\0';
	return dst;
}

char *HttpRequest::isprintf(const char *fmt, ...) {
	std::size_t size = strlen(fmt) + 1;
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

void HttpRequest::urlDecode(std::string& url) {
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
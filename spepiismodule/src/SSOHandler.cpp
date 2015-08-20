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
#include "HttpRequest.h"
#include "SPEPExtension.h"
#include "spep/SPEP.h"
#include "RequestParameters.h"
#include "Cookies.h"
#include "FilterConstants.h"

#include "spep/Base64.h"

namespace spep {
namespace isapi {

#define HTTP_POST_VAR_SAML_RESPONSE "SAMLResponse"

SSOHandler::SSOHandler(spep::SPEP *spep, SPEPExtension *spepExtension) :
mSpep(spep),
mSpepExtension(spepExtension) {

	mLocalLogger = std::make_shared<saml2::LocalLogger>(mSpep->getLogger(), "spep::isapi::SSOHandler");
}

RequestResultStatus SSOHandler::processRequest(HttpRequest *request) {
	try
	{
		// First we need to determine where the request was headed.
		if (request->getRequestURL() == mSpepExtension->mSpepSSOURL) {
			if (!mSpep->isStarted()) {
				return request->sendErrorDocument(HTTP_SERVICE_UNAVAILABLE);
			}

			// This is request is bound for /spep/sso - handle it.
			if (request->getRequestMethod() == "GET") {
				return this->handleSSOGetRequest(request);
			} else if (request->getRequestMethod() == "POST") {
				return this->handleSSOPostRequest(request);
			}

			return request->sendErrorDocument(HTTP_METHOD_NOT_ALLOWED);
		}

		return request->continueRequest();
	}
	catch (...)
	{
		return request->sendErrorDocument(HTTP_INTERNAL_SERVER_ERROR);
	}
}

RequestResultStatus SSOHandler::handleSSOGetRequest(HttpRequest *request) {
	RequestParameters params(request);

	Cookies cookies(request);
	// Set expiry to 48 hours to be consistent with Java land.
	cookies.addCookie(request, "spepAutoSubmit", "enabled", "/", NULL, false, 172800);

	std::string base64RedirectURL(params[REDIRECT_URL_PARAM]);

	// Try and get the hostname from the Host: header in the request.
	std::string hostname(request->getServerVariable("SERVER_NAME"));
	// Failing that, use the ServerName that IIS determined
	if (hostname.empty()) {
		//FIXME: should this still work?
		//hostname = req->server->server_hostname;
	}

	// Check that we have a port to use - needs to come from the connection
	// VirtualHosted apache servers don't keep port info in each vhost.
	int port = 0;//req->connection->local_addr->port;
	if (port == 0) {
		hostname = "";
	}

	// Parse the service host URL so we can compare.
	std::string serviceHost(mSpep->getSPEPConfigData()->getServiceHost());
	std::size_t start = serviceHost.find_first_of('/');

	while (serviceHost.at(start) == '/')
		start++;

	std::size_t end = serviceHost.find_first_of('/', start);
	std::string serviceHostname(serviceHost.substr(start, end - start));

	std::string baseRequestURL;

	// If we didn't get a hostname (or port), or it was the same as the service host.
	if (hostname.empty() || serviceHostname == hostname) {
		baseRequestURL = mSpep->getSPEPConfigData()->getServiceHost();
	} else {
		// Use mod_ssl to detemine if this was a https request, otherwise assume http.
		const char *scheme = request->isSecureRequest() ? "https" : "http";
		// Prepend the scheme to the hostname
		char *baseRequestURLChars = request->isprintf("%s://%s", scheme, hostname);

		// If it's on a non-standard port, append a port to the base URL
		if ((!request->isSecureRequest() && port != 80) || (request->isSecureRequest() && port != 443))	{
			baseRequestURLChars = request->isprintf("%s:%d", baseRequestURLChars, port);
		}

		baseRequestURL = baseRequestURLChars;
	}

	// Build the authentication request document
	std::string authnRequestDocument(buildAuthnRequestDocument(request, base64RedirectURL, baseRequestURL));

	return request->sendResponseDocument(HTTP_OK, HTTP_OK_STATUS_LINE, authnRequestDocument.c_str(), authnRequestDocument.length(), HTTP_POST_REQUEST_DOCUMENT_CONTENT_TYPE);
}


RequestResultStatus SSOHandler::handleSSOPostRequest(HttpRequest *request) {
	RequestParameters params(request);
	
	std::string base64SAMLResponse(params[HTTP_POST_VAR_SAML_RESPONSE]);

	//mLocalLogger->trace() << "SAMLResponse: " << base64SAMLResponse;

	spep::Base64Decoder decoder;
	decoder.push(base64SAMLResponse.c_str(), base64SAMLResponse.length());
	decoder.close();

	spep::Base64Document samlResponse(decoder.getResult());

	const auto documentLength = samlResponse.getLength();
	SAMLByte *document = new SAMLByte[documentLength];
	std::memcpy(document, samlResponse.getData(), documentLength);

	spep::AuthnProcessorData data;
	data.setResponseDocument(saml2::SAMLDocument(document, documentLength));
	data.setDisableAttributeQuery(mSpep->getSPEPConfigData()->disableAttributeQuery());

	try
	{
		mSpep->getAuthnProcessor()->processAuthnResponse(data);
	}
	catch (...)
	{
		return request->sendErrorDocument(HTTP_INTERNAL_SERVER_ERROR);
	}

	Cookies cookies(request);

	std::string tokenName(mSpep->getSPEPConfigData()->getTokenName());
	std::string tokenDomain(request->getServerVariable("SERVER_NAME"));
	const char* tokenDomainChars = NULL;
	if (!tokenDomain.empty())
		tokenDomainChars = tokenDomain.c_str();

	bool secure = request->isSecureRequest();
	cookies.addCookie(request, tokenName.c_str(), data.getSessionID().c_str(), NULL, tokenDomainChars, secure);

	// Establish return URL..
	std::string base64RedirectURL(data.getRequestURL());
	if (!base64RedirectURL.empty())	{
		spep::Base64Decoder decoder;
		decoder.push(base64RedirectURL.c_str(), base64RedirectURL.length());
		decoder.close();

		// Technically it's not a document, but it's all the same to Base64Decoder
		spep::Base64Document redirectURLDocument(decoder.getResult());
		std::string redirectURL(redirectURLDocument.getData(), redirectURLDocument.getLength());

		return request->sendRedirectResponse(redirectURL);
	}
	
	return request->sendRedirectResponse(mSpep->getSPEPConfigData()->getDefaultUrl());
}

std::string SSOHandler::buildAuthnRequestDocument(HttpRequest* request, const std::string& base64RedirectURL, const std::string& baseRequestURL) {
	AuthnProcessorData data;
	data.setRequestURL(base64RedirectURL);
	data.setBaseRequestURL(baseRequestURL);
	data.setRemoteIpAddress(request->getRemoteAddress());

	mSpep->getAuthnProcessor()->generateAuthnRequest(data);

	const saml2::SAMLDocument requestDocument(data.getRequestDocument());

	spep::Base64Encoder encoder;
	encoder.push(reinterpret_cast<const char*>(requestDocument.getData()), requestDocument.getLength());
	encoder.close();

	spep::Base64Document base64EncodedDocument(encoder.getResult());
	const std::string encodedDocumentString(base64EncodedDocument.getData(), base64EncodedDocument.getLength());

	const std::string ssoURL = mSpep->getMetadata()->getSingleSignOnEndpoint();

	return std::string(FORMAT_HTTP_POST_REQUEST_DOCUMENT(request, ssoURL.c_str(), encodedDocumentString.c_str()));
}

}
}
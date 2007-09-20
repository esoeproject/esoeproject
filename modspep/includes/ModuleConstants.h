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
 * Creation Date: Aug 29, 2007
 * 
 * Purpose: 
 */

#ifndef MODULECONSTANTS_H_
#define MODULECONSTANTS_H_

// Webapp isn't the correct term for this in apache, but to keep the idea consistent with
// the Java SPEP filter, I'm gonna use it anyway.

/// Default URL values.
/**@{*/
#define DEFAULT_URL_SPEP_WEBAPP 			"/spep"
#define DEFAULT_URL_SPEP_SSO 				DEFAULT_URL_SPEP_WEBAPP "/sso"
#define DEFAULT_URL_SPEP_WEBSERVICES		DEFAULT_URL_SPEP_WEBAPP "/services/spep"
#define DEFAULT_URL_SPEP_AUTHZCACHECLEAR	DEFAULT_URL_SPEP_WEBSERVICES "/authzCacheClear"
#define DEFAULT_URL_SPEP_SINGLELOGOUT		DEFAULT_URL_SPEP_WEBSERVICES "/singleLogout"
/**@}*/

/// Name of the URL parameter for the redirection.
#define REDIRECT_URL_PARAM				"redirectURL"


// 0.5MB at the moment - that should be enough for any feasible scenario.. with the current implementation of ESOE.
/// Maximum length of HTTP POST content supported.
#define MAXIMUM_POST_LENGTH				524288

/// Small buffer size used for reading request documents in.
#define SMALL_BUFFER_SIZE 1024

/// Request document in which to embed a SAML request.
#define HTTP_POST_REQUEST_DOCUMENT \
"<html>" \
"<head>" \
"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">" \
"<meta http-equiv=\"Pragma\" content=\"no-cache\">" \
"</head>" \
"<body onLoad=\"document.samlRequest.submit()\">" \
"<form method=\"post\" action=\"%s\" name=\"samlRequest\">" \
	"<input type=\"hidden\" name=\"SAMLRequest\" value=\"%s\"/>" \
	"If you are not automatically redirected, please click below to begin using this service" \
	"<input type=\"submit\" value=\"Continue\" />" \
"</form>" \
"</body>" \
"</html>"

/// Name of HTTP header for doing a redirect
#define HEADER_NAME_REDIRECT_URL "Location"

/// Content type of SAML request HTML document
#define HTTP_POST_REQUEST_DOCUMENT_CONTENT_TYPE "text/html; charset=utf-8"

/// Name of the HTTP header for content type
#define HEADER_NAME_CONTENT_TYPE "Content-Type"

/// SOAP Document content types
/**@{*/
#define SOAP11_DOCUMENT_CONTENT_TYPE "text/xml"
#define SOAP12_DOCUMENT_CONTENT_TYPE "application/soap+xml"
/**@}*/

/// Macro to create the document.
#define FORMAT_HTTP_POST_REQUEST_DOCUMENT(pool, endpoint, document)  apr_psprintf(pool, HTTP_POST_REQUEST_DOCUMENT, endpoint, document)

#endif /*MODULECONSTANTS_H_*/

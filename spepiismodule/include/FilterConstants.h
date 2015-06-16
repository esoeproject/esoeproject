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
#define DEFAULT_URL_SPEP_SSO 				"/sso"
#define DEFAULT_URL_SPEP_WEBSERVICES		"/services/spep"
#define DEFAULT_URL_SPEP_AUTHZCACHECLEAR	"/authzCacheClear"
#define DEFAULT_URL_SPEP_SINGLELOGOUT		"/singleLogout"
/**@}*/

/// Name of the URL parameter for the redirection.
#define REDIRECT_URL_PARAM				"redirectURL"


// 0.5MB at the moment - that should be enough for any feasible scenario.. with the current implementation of ESOE.
/// Maximum length of HTTP POST content supported.
//#define MAXIMUM_POST_LENGTH				524288

/// Small buffer size used for reading request documents in.
//#define SMALL_BUFFER_SIZE 1024

/// Request document in which to embed a SAML request.
#define HTTP_POST_REQUEST_DOCUMENT \
"<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n" \
		"<html>\n" \
		"<head>\n" \
		"<title>ESOE secure resource verification</title>\n" \
		"\n" \
		"<!-- This file is specially formatted with addition escape characters for Java MessageFormatter edit with care -->\n" \
		"\n" \
		"<style type=\"text/css\">\n" \
		"body {\n" \
		"  background-color: #fff;\n" \
		"  font-size: 100.01%;\n" \
		"  font-weight: normal;\n" \
		"  font-family: arial, verdana, helvetica, sans-serif;\n" \
		"  margin: 10px;\n" \
		"  padding: 0;\n" \
		"  color: #000;\n" \
		"  text-align: center;\n" \
		"}\n" \
		"\n" \
		"h1 {\n" \
		"  font-size: 40px;\n" \
		"  font-weight: bold;\n" \
		"  margin: 1em 0;\n" \
		"  color:  #cccccc;\n" \
		"  text-align: center;\n" \
		"}\n" \
		"\n" \
		"a {\n" \
		"text-decoration: none;\n" \
		"}\n" \
		"\n" \
		"div#nonjavascript\n" \
		"{\n" \
		"  	margin: 0px 20px 0px 20px;\n" \
		"	display: block;\n" \
		"}\n" \
		"\n" \
		"div#main \n" \
		"{\n" \
		"  width: 600px;\n" \
		"  margin-left: auto;\n" \
		"  margin-right: auto;\n" \
		"  text-align: left;\n" \
		"}\n" \
		"\n" \
		"</style>\n" \
		"\n" \
		"<script>\n" \
		"function enableAutoSubmit(name)\n" \
		"{ \n" \
		"   index = document.cookie.indexOf(name + \'=\');\n" \
		"         \n" \
		"    if(index > -1)\n" \
		"       return 1;\n" \
		"       \n" \
		"    return 0;\n" \
		"}\n" \
		"\n" \
		"function deleteCookie(name)\n" \
		"{\n" \
		"	var expires = new Date();\n" \
		"	expires.setUTCFullYear(expires.getUTCFullYear() - 1);\n" \
		"	document.cookie = name + \'=; expires=\' + expires.toUTCString() + \'; path=/\';\n" \
		"} \n" \
		"\n" \
		"function loader()\n" \
		"{\n" \
		"        var el = document.getElementById(\'nonjavascript\');\n" \
		"        el.style.display = \'none\';\n" \
		"        \n" \
		"        var submit = enableAutoSubmit(\'spepAutoSubmit\');\n" \
		"        if(submit == 1)\n" \
		"        {\n" \
		"        	deleteCookie(\'spepAutoSubmit\');\n" \
		"        	document.samlRequest.submit()\n" \
		"        }\n" \
		" }\n" \
		"\n" \
		"function toggle()\n" \
		"    {\n" \
		"        var el = document.getElementById(\'nonjavascript\');\n" \
		"        if ( el.style.display != \'none\' )\n" \
		"        {\n" \
		"            el.style.display = \'none\';\n" \
		"        }\n" \
		"        else\n" \
		"        {\n" \
		"            el.style.display = \'block\';\n" \
		"        }\n" \
		"    }\n" \
		"</script>\n" \
		"\n" \
		"</head>\n" \
		"<body onLoad=\"loader()\">\n" \
		"\n" \
		"<div id=\"main\">\n" \
		"	<h1> Security Verification </h1>\n" \
		"	<p>The resource you are accessing has been identified as secure content.</p>\n" \
		"	<p/>\n" \
		"	<p>This site is currently verifying your digital identity.\n" \
		"		<ul>\n" \
		"			<li>For most web browsers this process is automatic, you will be taken to your original resource upon completion.</li>\n" \
		"			<li>The verification process is <strong>encrypted</strong> to protect your digital identity.</li>\n" \
		"			<li>The verification process should take no more than 20 seconds to complete.</li>\n" \
		"			<li>The verification process is required only once per session for each unique site you visit</li>\n" \
		"		</ul>\n" \
		"	</p>\n" \
		"	<br/>\n" \
		"		<noscript>\n" \
		"			<p>Your browser does not support Javascript or it is not enabled. For the best experience we recommend you <strong>enable Javascript for this site</strong> or change to a Javascript enabled browser. You <strong>MUST</strong> manually click the button below.</p>\n" \
		"		</noscript>\n" \
		"	<br/>\n" \
		"	<p>If you\'re navigating using the browser back or forward buttons you can safely skip over this page, your session is already valid and won\'t be affected.</p>\n" \
		"	<br/>\n" \
		"	<br/>\n" \
		"	<p><small>If you\'re having problems, or using an older browser please see the <a href=\"#\" onclick=\"toggle(); return false\">extended functionality</small></a>.</p>\n" \
		"	\n" \
		"	<form method=\"post\" action=\"%s\" name=\"samlRequest\">\n" \
		"		<input type=\"hidden\" name=\"SAMLRequest\" value=\"%s\"/>\n" \
		"		<div id=\"nonjavascript\">\n" \
		"			<ul>\n" \
		"			<li><p>Some common causes of requiring this extended functionality include having <strong>javascript</strong> or <strong>cookies</strong> disabled. Please ensure both of these technologies are enabled in your browser</p></li>\n" \
		"			<li>\n" \
		"				<p><small>If the automated process is not functioning for you please click this button: <input type=\"submit\" value=\"Verify Identity\" /></small></p>\n" \
		"			</li>\n" \
		"			<li><small>If you continue to have problems please report these to your local helpdesk or computer support professional, include as much detail as possible including the date and time of the problem, the type of computer you use and the version of web browser you\'re using.</small></li>\n" \
		"			</ul>\n" \
		"		</div>\n" \
		"	</form>\n" \
		"</div>\n" \
		"</body>\n" \
		"</html>\n"

#define HTTP_OK 200
#define HTTP_REDIRECT 302
#define HTTP_FORBIDDEN 403
#define HTTP_FORBIDDEN_READ 403, 2
#define HTTP_METHOD_NOT_ALLOWED 405
#define HTTP_INTERNAL_SERVER_ERROR 500
#define HTTP_SERVICE_UNAVAILABLE 503

#define HTTP_OK_STATUS_LINE "200 OK"

#define HTTP_FORBIDDEN_STATUS_LINE "403 Forbidden"
#define HTTP_FORBIDDEN_DOCUMENT "<html><head><title>403 Forbidden</title></head><body><h1>Forbidden</h1><p>You are not permitted to access this resource.</p></body></html>"
#define HTTP_FORBIDDEN_DOCUMENT_TYPE "text/html; charset=utf-8"

#define HTTP_METHOD_NOT_ALLOWED_STATUS_LINE "405 Method Not Allowed"
#define HTTP_METHOD_NOT_ALLOWED_DOCUMENT "<html><head><title>403 Method Not Allowed</title></head><body><h1>Method Not Allowed</h1><p>The requested method is not allowed for this URL.</p></body></html>"
#define HTTP_METHOD_NOT_ALLOWED_DOCUMENT_TYPE "text/html; charset=utf-8"

#define HTTP_INTERNAL_SERVER_ERROR_STATUS_LINE "500 Internal Server Error"
#define HTTP_INTERNAL_SERVER_ERROR_DOCUMENT "<html><head><title>500 Internal Server Error</title></head><body><h1>Internal Server Error</h1><p>An internal error occurred while processing this request.</p></body></html>"
#define HTTP_INTERNAL_SERVER_ERROR_DOCUMENT_TYPE "text/html; charset=utf-8"

#define HTTP_SERVICE_UNAVAILABLE_STATUS_LINE "503 Service Temporarily Unavailable"
#define HTTP_SERVICE_UNAVAILABLE_DOCUMENT "<html><head><title>503 Service Temporarily Unavailable</title></head><body><h1>Service Temporarily Unavailable</h1><p>The server is temporarily unable to service your request due to maintenance downtime or capacity problems. Please try again later.</p></body></html>"
#define HTTP_SERVICE_UNAVAILABLE_DOCUMENT_TYPE "text/html; charset=utf-8"

#define HTTP_REDIRECT_STATUS_LINE "302 Found"
#define REDIRECT_HEADER "Location: "
#define CONTENT_TYPE_HEADER "Content-Type: "
#define CONTENT_LENGTH_HEADER "Content-Length: "
#define HOST_HEADER "Host: "
#define HTTP_HEADER_ENDLINE "\r\n"

/// Name of HTTP header for doing a redirect
//#define HEADER_NAME_REDIRECT_URL "Location"

/// Content type of SAML request HTML document
#define HTTP_POST_REQUEST_DOCUMENT_CONTENT_TYPE "text/html; charset=utf-8"

/// Name of the HTTP header for content type
//#define HEADER_NAME_CONTENT_TYPE "Content-Type"

/// SOAP Document content types
/**@{*/
#define SOAP11_DOCUMENT_CONTENT_TYPE "text/xml"
#define SOAP12_DOCUMENT_CONTENT_TYPE "application/soap+xml"
/**@}*/

/// Macro to create the document.
#define FORMAT_HTTP_POST_REQUEST_DOCUMENT(request, endpoint, document)  request->isprintf(HTTP_POST_REQUEST_DOCUMENT, endpoint, document)

#endif /*MODULECONSTANTS_H_*/

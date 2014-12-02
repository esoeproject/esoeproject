/*
 * Copyright 2006-2007, Queensland University of Technology
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
 * Author: Bradley Beddoes
 * Creation Date: 03/01/2007
 * 
 * Purpose: Stores common constants and defines required across SAML2lib-cpp
 */
 
#ifndef CONSTANTS_H_
#define CONSTANTS_H_

// These definitions are required here because of the way that the template classes are used across dll boundaries.
// It's not nice to do this, but it's preventing multiply defined strings
#include <string>
template class __declspec(dllexport) std::basic_string<char, std::char_traits<char>, std::allocator<char>>;
template class __declspec(dllexport) std::basic_string<wchar_t, std::char_traits<wchar_t>, std::allocator<wchar_t>>;


/* TODO: Make this different for unix / windows */
#ifdef WIN32
#define FILE_SEPERATOR "\\"
#else //WIN32
#define FILE_SEPERATOR "/"
#endif //WIN32

/* SAML2lib Byte Typedef */
typedef unsigned char SAMLByte;

/* Marshaller Defines */
#define XMLNS "http://www.w3.org/2000/xmlns/"
#define ENCODING "UTF-16"
#define SAML_SIBLING_INSERT_AFTER "saml:Issuer"

/* Unmarshaller Defines */
#define METADATA_URI "urn:oasis:names:tc:SAML:2.0:metadata"
#define KEY_DESCRIPTOR "KeyDescriptor"

/* Xerces Identifiers */
#define IMPL_FLAGS "xml"
#define MARSHALLER_ID "saml2libmarshaller"
#define UNMARSHALLER_ID "saml2libunmarshaller"

/* Digitial Signature Defines */
#define DSIG_URI "http://www.w3.org/2000/09/xmldsig#"
#define DSIG_SIG_ELEM "Signature"
#define DSIG_NS "ds"
#define KEY_INFO "KeyInfo"
#define KEY_NAME "KeyName"
#define KEY_VALUE "KeyValue"
#define RSA_KEY_VALUE "RSAKeyValue"
#define DSA_KEY_VALUE "DSAKeyValue"
#define DSA_P "P"
#define DSA_Q "Q"
#define DSA_G "G"
#define DSA_Y "Y"
#define DSA_J "J"
#define DSIG_ID_LEAD_CH "#"

/* Identifier Generator Defines */
#define MAX_BYTES 20
#define MAX_CHARS (MAX_BYTES * 2) + 1
#define HEX_FORMAT "%02x"
#ifdef WIN32
#define TIME_FORMAT "%y%m%d%H%M%S"
#else //WIN32
#define TIME_FORMAT "%s"
#endif //WIN32
#define XS_ID_DELIM "_"
#define ID_DELIM "-"

/* Boost date / pTime integration */
#define XML_DATE_TIME_TO_BOOST_REGEX "([\\d-]+)T([\\d:.]+)Z"
#define BOOST_TO_XML_DATE_TIME_REGEX "([\\d-]+)T([\\d:.]+)"


/* Workaround for Cygwin's missing std::wstring and std::wcslen() 
 * needed by most of the XML code. */
#ifdef __CYGWIN__
#include <string>
/*#include <iostream>*/
/*#include <xsd/cxx/tree/types.hxx>*/

namespace std
{
	typedef basic_string<wchar_t> wstring;
	inline size_t wcslen(const wchar_t *s){size_t size = 0; for(const wchar_t *p = s; ((unsigned int)*p)!=0; p++) size++; return size;}
}
#endif /* __CYGWIN__ */

#if defined(WIN32) && defined(_MSC_VER)

#ifdef BUILDING_SAML2
#define SAML2EXPORT __declspec(dllexport)
#define SAML2CONSTANT static
#else
#define SAML2EXPORT __declspec(dllimport)
#define SAML2CONSTANT static
#endif /*BUILDING_SAML2*/

// VC++ (or more accurately, the windows platform SDK) doesn't define snprintf.. 
// instead it defines _snprintf
#define snprintf _snprintf

// Also, it complains about half the functions not being safe... so
// we flag them not to be deprecated.
#define _CRT_SECURE_NO_DEPRECATE 1

// Include this ASAP so other libraries including windows.h can't break us.
#include <winsock2.h>

// Bring this in for the class definition
#include <xercesc/dom/DOMDocument.hpp>

// Windows defines a DOMDocument type, so we need to get around that..
namespace saml2
{
	typedef XERCES_CPP_NAMESPACE::DOMDocument DOMDocument;
}
#endif /*WIN32 && _MSC_VER */

#ifdef __GNUC__

#ifdef BUILDING_SAML2
#define SAML2EXPORT __attribute((visibility("default")))
#define SAML2CONSTANT
#else
#define SAML2EXPORT
#define SAML2CONSTANT
#endif /*BUILDING_SAML2*/

#endif

#ifndef SAML2EXPORT
// If nothing has been defined, we don't need any special flags to export symbols
#define SAML2EXPORT 
#define SAML2CONSTANT 
#endif /*SAML2EXPORT*/

#endif /*CONSTANTS_H_*/

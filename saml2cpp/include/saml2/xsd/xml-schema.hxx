// Copyright (C) 2005-2008 Code Synthesis Tools CC
//
// This program was generated by CodeSynthesis XSD, an XML Schema to
// C++ data binding compiler.
//
// Licensed under the Apache License, Version 2.0 (the "License"); you may not 
// use this file except in compliance with the License. You may obtain a copy of 
// the License at 
// 
//   http://www.apache.org/licenses/LICENSE-2.0 
// 
// Unless required by applicable law or agreed to in writing, software 
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
// License for the specific language governing permissions and limitations under 
// the License.

/**
 * @file
 * @brief Generated from xml-schema.xsd.
 */

#ifndef XML_SCHEMA_HXX
#define XML_SCHEMA_HXX

// Begin prologue.
//
#include "saml2/SAML2Defs.h"
//
// End prologue.

#include <xsd/cxx/config.hxx>

#if (XSD_INT_VERSION != 3020000L)
#error XSD runtime version mismatch
#endif

#include <xsd/cxx/pre.hxx>

#ifndef XSD_USE_WCHAR
#define XSD_USE_WCHAR
#endif

#ifndef XSD_CXX_TREE_USE_WCHAR
#define XSD_CXX_TREE_USE_WCHAR
#endif

#include <xsd/cxx/tree/exceptions.hxx>
#include <xsd/cxx/tree/elements.hxx>
#include <xsd/cxx/tree/types.hxx>

#include <xsd/cxx/xml/error-handler.hxx>
#include <xsd/cxx/tree/serialization.hxx>

#include <xsd/cxx/xml/dom/auto-ptr.hxx>

#include <xsd/cxx/tree/parsing.hxx>
#include <xsd/cxx/tree/parsing/byte.hxx>
#include <xsd/cxx/tree/parsing/unsigned-byte.hxx>
#include <xsd/cxx/tree/parsing/short.hxx>
#include <xsd/cxx/tree/parsing/unsigned-short.hxx>
#include <xsd/cxx/tree/parsing/int.hxx>
#include <xsd/cxx/tree/parsing/unsigned-int.hxx>
#include <xsd/cxx/tree/parsing/long.hxx>
#include <xsd/cxx/tree/parsing/unsigned-long.hxx>
#include <xsd/cxx/tree/parsing/boolean.hxx>
#include <xsd/cxx/tree/parsing/float.hxx>
#include <xsd/cxx/tree/parsing/double.hxx>
#include <xsd/cxx/tree/parsing/decimal.hxx>

#include <xsd/cxx/xml/dom/serialization-header.hxx>
#include <xsd/cxx/tree/serialization.hxx>
#include <xsd/cxx/tree/serialization/byte.hxx>
#include <xsd/cxx/tree/serialization/unsigned-byte.hxx>
#include <xsd/cxx/tree/serialization/short.hxx>
#include <xsd/cxx/tree/serialization/unsigned-short.hxx>
#include <xsd/cxx/tree/serialization/int.hxx>
#include <xsd/cxx/tree/serialization/unsigned-int.hxx>
#include <xsd/cxx/tree/serialization/long.hxx>
#include <xsd/cxx/tree/serialization/unsigned-long.hxx>
#include <xsd/cxx/tree/serialization/boolean.hxx>
#include <xsd/cxx/tree/serialization/float.hxx>
#include <xsd/cxx/tree/serialization/double.hxx>
#include <xsd/cxx/tree/serialization/decimal.hxx>

/**
 * @brief C++ namespace for the %http://www.w3.org/2001/XMLSchema
 * schema namespace.
 */
namespace xml_schema
{
  // anyType and anySimpleType.
  //

  /**
   * @brief C++ type corresponding to the anyType XML Schema
   * built-in type.
   */
  typedef ::xsd::cxx::tree::type type;

  /**
   * @brief C++ type corresponding to the anySimpleType XML Schema
   * built-in type.
   */
  typedef ::xsd::cxx::tree::simple_type< type > simple_type;

  /**
   * @brief Alias for the anyType type.
   */
  typedef ::xsd::cxx::tree::type container;


  // 8-bit
  //

  /**
   * @brief C++ type corresponding to the byte XML Schema
   * built-in type.
   */
  typedef signed char byte;

  /**
   * @brief C++ type corresponding to the unsignedByte XML Schema
   * built-in type.
   */
  typedef unsigned char unsigned_byte;


  // 16-bit
  //

  /**
   * @brief C++ type corresponding to the short XML Schema
   * built-in type.
   */
  typedef short short_;

  /**
   * @brief C++ type corresponding to the unsignedShort XML Schema
   * built-in type.
   */
  typedef unsigned short unsigned_short;


  // 32-bit
  //

  /**
   * @brief C++ type corresponding to the int XML Schema
   * built-in type.
   */
  typedef int int_;

  /**
   * @brief C++ type corresponding to the unsignedInt XML Schema
   * built-in type.
   */
  typedef unsigned int unsigned_int;


  // 64-bit
  //

  /**
   * @brief C++ type corresponding to the long XML Schema
   * built-in type.
   */
  typedef long long long_;

  /**
   * @brief C++ type corresponding to the unsignedLong XML Schema
   * built-in type.
   */
  typedef unsigned long long unsigned_long;


  // Supposed to be arbitrary-length integral types.
  //

  /**
   * @brief C++ type corresponding to the integer XML Schema
   * built-in type.
   */
  typedef long long integer;

  /**
   * @brief C++ type corresponding to the nonPositiveInteger XML Schema
   * built-in type.
   */
  typedef long long non_positive_integer;

  /**
   * @brief C++ type corresponding to the nonNegativeInteger XML Schema
   * built-in type.
   */
  typedef unsigned long long non_negative_integer;

  /**
   * @brief C++ type corresponding to the positiveInteger XML Schema
   * built-in type.
   */
  typedef unsigned long long positive_integer;

  /**
   * @brief C++ type corresponding to the negativeInteger XML Schema
   * built-in type.
   */
  typedef long long negative_integer;


  // Boolean.
  //

  /**
   * @brief C++ type corresponding to the boolean XML Schema
   * built-in type.
   */
  typedef bool boolean;


  // Floating-point types.
  //

  /**
   * @brief C++ type corresponding to the float XML Schema
   * built-in type.
   */
  typedef float float_;

  /**
   * @brief C++ type corresponding to the double XML Schema
   * built-in type.
   */
  typedef double double_;

  /**
   * @brief C++ type corresponding to the decimal XML Schema
   * built-in type.
   */
  typedef double decimal;


  // String types.
  //

  /**
   * @brief C++ type corresponding to the string XML Schema
   * built-in type.
   */
  typedef ::xsd::cxx::tree::string< wchar_t, simple_type > string;

  /**
   * @brief C++ type corresponding to the normalizedString XML Schema
   * built-in type.
   */
  typedef ::xsd::cxx::tree::normalized_string< wchar_t, string > normalized_string;

  /**
   * @brief C++ type corresponding to the token XML Schema
   * built-in type.
   */
  typedef ::xsd::cxx::tree::token< wchar_t, normalized_string > token;

  /**
   * @brief C++ type corresponding to the Name XML Schema
   * built-in type.
   */
  typedef ::xsd::cxx::tree::name< wchar_t, token > name;

  /**
   * @brief C++ type corresponding to the NMTOKEN XML Schema
   * built-in type.
   */
  typedef ::xsd::cxx::tree::nmtoken< wchar_t, token > nmtoken;

  /**
   * @brief C++ type corresponding to the NMTOKENS XML Schema
   * built-in type.
   */
  typedef ::xsd::cxx::tree::nmtokens< wchar_t, simple_type, nmtoken > nmtokens;

  /**
   * @brief C++ type corresponding to the NCName XML Schema
   * built-in type.
   */
  typedef ::xsd::cxx::tree::ncname< wchar_t, name > ncname;

  /**
   * @brief C++ type corresponding to the language XML Schema
   * built-in type.
   */
  typedef ::xsd::cxx::tree::language< wchar_t, token > language;


  // ID/IDREF.
  //

  /**
   * @brief C++ type corresponding to the ID XML Schema
   * built-in type.
   */
  typedef ::xsd::cxx::tree::id< wchar_t, ncname > id;

  /**
   * @brief C++ type corresponding to the IDREF XML Schema
   * built-in type.
   */
  typedef ::xsd::cxx::tree::idref< type, wchar_t, ncname > idref;

  /**
   * @brief C++ type corresponding to the IDREFS XML Schema
   * built-in type.
   */
  typedef ::xsd::cxx::tree::idrefs< wchar_t, simple_type, idref > idrefs;


  // URI.
  //

  /**
   * @brief C++ type corresponding to the anyURI XML Schema
   * built-in type.
   */
  typedef ::xsd::cxx::tree::uri< wchar_t, simple_type > uri;


  // Qualified name.
  //

  /**
   * @brief C++ type corresponding to the QName XML Schema
   * built-in type.
   */
  typedef ::xsd::cxx::tree::qname< wchar_t, simple_type, uri, ncname > qname;


  // Binary.
  //

  /**
   * @brief Binary buffer type.
   */
  typedef ::xsd::cxx::tree::buffer< wchar_t > buffer;

  /**
   * @brief C++ type corresponding to the base64Binary XML Schema
   * built-in type.
   */
  typedef ::xsd::cxx::tree::base64_binary< wchar_t, simple_type > base64_binary;

  /**
   * @brief C++ type corresponding to the hexBinary XML Schema
   * built-in type.
   */
  typedef ::xsd::cxx::tree::hex_binary< wchar_t, simple_type > hex_binary;


  // Date/time.
  //

  /**
   * @brief Time zone type.
   */
  typedef ::xsd::cxx::tree::time_zone time_zone;

  /**
   * @brief C++ type corresponding to the date XML Schema
   * built-in type.
   */
  class date;

  /**
   * @brief C++ type corresponding to the dateTime XML Schema
   * built-in type.
   */
  class date_time;

  /**
   * @brief C++ type corresponding to the duration XML Schema
   * built-in type.
   */
  typedef ::xsd::cxx::tree::duration< wchar_t, simple_type > duration;

  /**
   * @brief C++ type corresponding to the gDay XML Schema
   * built-in type.
   */
  typedef ::xsd::cxx::tree::gday< wchar_t, simple_type > gday;

  /**
   * @brief C++ type corresponding to the gMonth XML Schema
   * built-in type.
   */
  typedef ::xsd::cxx::tree::gmonth< wchar_t, simple_type > gmonth;

  /**
   * @brief C++ type corresponding to the gMonthDay XML Schema
   * built-in type.
   */
  typedef ::xsd::cxx::tree::gmonth_day< wchar_t, simple_type > gmonth_day;

  /**
   * @brief C++ type corresponding to the gYear XML Schema
   * built-in type.
   */
  typedef ::xsd::cxx::tree::gyear< wchar_t, simple_type > gyear;

  /**
   * @brief C++ type corresponding to the gYearMonth XML Schema
   * built-in type.
   */
  typedef ::xsd::cxx::tree::gyear_month< wchar_t, simple_type > gyear_month;

  /**
   * @brief C++ type corresponding to the time XML Schema
   * built-in type.
   */
  typedef ::xsd::cxx::tree::time< wchar_t, simple_type > time;


  // Entity.
  //

  /**
   * @brief C++ type corresponding to the ENTITY XML Schema
   * built-in type.
   */
  typedef ::xsd::cxx::tree::entity< wchar_t, ncname > entity;

  /**
   * @brief C++ type corresponding to the ENTITIES XML Schema
   * built-in type.
   */
  typedef ::xsd::cxx::tree::entities< wchar_t, simple_type, entity > entities;


  // Namespace information and list stream. Used in
  // serialization functions.
  //
  /**
   * @brief Namespace serialization information.
   */
  typedef ::xsd::cxx::xml::dom::namespace_info < wchar_t > namespace_info;

  /**
   * @brief Namespace serialization information map.
   */
  typedef ::xsd::cxx::xml::dom::namespace_infomap < wchar_t > namespace_infomap;

  /**
   * @brief List serialization stream.
   */
  typedef ::xsd::cxx::tree::list_stream < wchar_t > list_stream;

  /**
   * @brief Serialization wrapper for the %double type.
   */
  typedef ::xsd::cxx::tree::as_double < double_ > as_double;

  /**
   * @brief Serialization wrapper for the %decimal type.
   */
  typedef ::xsd::cxx::tree::as_decimal < decimal > as_decimal;

  /**
   * @brief Simple type facet.
   */
  typedef ::xsd::cxx::tree::facet facet;

  // Flags and properties.
  //

  /**
   * @brief Parsing and serialization flags.
   */
  typedef ::xsd::cxx::tree::flags flags;

  /**
   * @brief Parsing properties.
   */
  typedef ::xsd::cxx::tree::properties< wchar_t > properties;

  // Exceptions.
  //

  /**
   * @brief Root of the C++/Tree %exception hierarchy.
   */
  typedef ::xsd::cxx::tree::exception< wchar_t > exception;

  /**
   * @brief Exception indicating that the size argument exceeds
   * the capacity argument.
   */
  typedef ::xsd::cxx::tree::bounds< wchar_t > bounds;

  /**
   * @brief Exception indicating that a duplicate ID value
   * was encountered in the object model.
   */
  typedef ::xsd::cxx::tree::duplicate_id< wchar_t > duplicate_id;

  /**
   * @brief Exception indicating a parsing failure.
   */
  typedef ::xsd::cxx::tree::parsing< wchar_t > parsing;

  /**
   * @brief Exception indicating that an expected element
   * was not encountered.
   */
  typedef ::xsd::cxx::tree::expected_element< wchar_t > expected_element;

  /**
   * @brief Exception indicating that an unexpected element
   * was encountered.
   */
  typedef ::xsd::cxx::tree::unexpected_element< wchar_t > unexpected_element;

  /**
   * @brief Exception indicating that an expected attribute
   * was not encountered.
   */
  typedef ::xsd::cxx::tree::expected_attribute< wchar_t > expected_attribute;

  /**
   * @brief Exception indicating that an unexpected enumerator
   * was encountered.
   */
  typedef ::xsd::cxx::tree::unexpected_enumerator< wchar_t > unexpected_enumerator;

  /**
   * @brief Exception indicating that the text content was
   * expected for an element.
   */
  typedef ::xsd::cxx::tree::expected_text_content< wchar_t > expected_text_content;

  /**
   * @brief Exception indicating that a prefix-namespace
   * mapping was not provided.
   */
  typedef ::xsd::cxx::tree::no_prefix_mapping< wchar_t > no_prefix_mapping;

  /**
   * @brief Exception indicating a serialization failure.
   */
  typedef ::xsd::cxx::tree::serialization< wchar_t > serialization;

  // Parsing/serialization diagnostics.
  //

  /**
   * @brief Error severity.
   */
  typedef ::xsd::cxx::tree::severity severity;

  /**
   * @brief Error condition.
   */
  typedef ::xsd::cxx::tree::error< wchar_t > error;

  /**
   * @brief List of %error conditions.
   */
  typedef ::xsd::cxx::tree::diagnostics< wchar_t > diagnostics;

  /**
   * @brief Error handler callback interface.
   */
  typedef ::xsd::cxx::xml::error_handler< wchar_t > error_handler;

  /**
   * @brief DOM interaction.
   */
  namespace dom
  {
    /**
     * @brief Automatic pointer for DOMDocument.
     */
    using ::xsd::cxx::xml::dom::auto_ptr;

    /**
     * @brief DOM user data key for back pointers to tree nodes.
     */
    const XMLCh* const tree_node_key = ::xsd::cxx::tree::user_data_keys::node;
  }
}

#include <xsd/cxx/post.hxx>

// Begin epilogue.
//
#include "saml2/xsd/xml-schema-custom.h"
//
// End epilogue.

#endif // XML_SCHEMA_HXX

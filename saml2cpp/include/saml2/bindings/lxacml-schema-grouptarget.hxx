// Copyright (C) 2005-2008 Code Synthesis Tools CC
//
// This program was generated by CodeSynthesis XSD, an XML Schema to
// C++ data binding compiler.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License version 2 as
// published by the Free Software Foundation.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
//
// In addition, as a special exception, Code Synthesis Tools CC gives
// permission to link this program with the Xerces-C++ library (or with
// modified versions of Xerces-C++ that use the same license as Xerces-C++),
// and distribute linked combinations including the two. You must obey
// the GNU General Public License version 2 in all respects for all of
// the code used other than Xerces-C++. If you modify this copy of the
// program, you may extend this exception to your version of the program,
// but you are not obligated to do so. If you do not wish to do so, delete
// this exception statement from your version.
//
// Furthermore, Code Synthesis Tools CC makes a special exception for
// the Free/Libre and Open Source Software (FLOSS) which is described
// in the accompanying FLOSSE file.
//

#ifndef CXX_HOME_KAURM_DEV_WORKSPACE_TEMPESOEPROJ_ESOEPROJECT_SAML2CPP_SCHEMA_LXACML_SCHEMA_GROUPTARGET_HXX
#define CXX_HOME_KAURM_DEV_WORKSPACE_TEMPESOEPROJ_ESOEPROJECT_SAML2CPP_SCHEMA_LXACML_SCHEMA_GROUPTARGET_HXX

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

#include "saml2/xsd/xml-schema.hxx"

// Forward declarations.
//
namespace middleware
{
  namespace lxacmlGroupTargetSchema
  {
    class GroupTargetType;
  }
}


#include <memory>    // std::auto_ptr
#include <algorithm> // std::binary_search

#include <xsd/cxx/tree/exceptions.hxx>
#include <xsd/cxx/tree/elements.hxx>
#include <xsd/cxx/tree/containers.hxx>
#include <xsd/cxx/tree/list.hxx>

#include <xsd/cxx/xml/dom/parsing-header.hxx>

#include <xsd/cxx/tree/containers-wildcard.hxx>

namespace middleware
{
  namespace lxacmlGroupTargetSchema
  {
    class GroupTargetType: public ::xml_schema::type
    {
      public:
      // GroupTargetID
      // 
      typedef ::xml_schema::string GroupTargetID_type;
      typedef ::xsd::cxx::tree::traits< GroupTargetID_type, wchar_t > GroupTargetID_traits;

      const GroupTargetID_type&
      GroupTargetID () const;

      GroupTargetID_type&
      GroupTargetID ();

      void
      GroupTargetID (const GroupTargetID_type& x);

      void
      GroupTargetID (::std::auto_ptr< GroupTargetID_type > p);

      // AuthzTarget
      // 
      typedef ::xml_schema::string AuthzTarget_type;
      typedef ::xsd::cxx::tree::sequence< AuthzTarget_type > AuthzTarget_sequence;
      typedef AuthzTarget_sequence::iterator AuthzTarget_iterator;
      typedef AuthzTarget_sequence::const_iterator AuthzTarget_const_iterator;
      typedef ::xsd::cxx::tree::traits< AuthzTarget_type, wchar_t > AuthzTarget_traits;

      const AuthzTarget_sequence&
      AuthzTarget () const;

      AuthzTarget_sequence&
      AuthzTarget ();

      void
      AuthzTarget (const AuthzTarget_sequence& s);

      // Constructors.
      //
      GroupTargetType ();

      GroupTargetType (const GroupTargetID_type&);

      GroupTargetType (::std::auto_ptr< GroupTargetID_type >&);

      GroupTargetType (const ::xercesc::DOMElement& e,
                       ::xml_schema::flags f = 0,
                       ::xml_schema::container* c = 0);

      GroupTargetType (const GroupTargetType& x,
                       ::xml_schema::flags f = 0,
                       ::xml_schema::container* c = 0);

      virtual GroupTargetType*
      _clone (::xml_schema::flags f = 0,
              ::xml_schema::container* c = 0) const;

      virtual 
      ~GroupTargetType ();

      // Implementation.
      //
      protected:
      void
      parse (::xsd::cxx::xml::dom::parser< wchar_t >&,
             ::xml_schema::flags);

      protected:
      ::xsd::cxx::tree::one< GroupTargetID_type > GroupTargetID_;
      AuthzTarget_sequence AuthzTarget_;
    };
  }
}

#include <iosfwd>

#include <xercesc/sax/InputSource.hpp>
#include <xercesc/dom/DOMDocument.hpp>
#include <xercesc/dom/DOMErrorHandler.hpp>

namespace middleware
{
  namespace lxacmlGroupTargetSchema
  {
    // Parse a URI or a local file.
    //

    ::std::auto_ptr< ::middleware::lxacmlGroupTargetSchema::GroupTargetType >
    GroupTarget (const ::std::wstring& uri,
                 ::xml_schema::flags f = 0,
                 const ::xml_schema::properties& p = ::xml_schema::properties ());

    ::std::auto_ptr< ::middleware::lxacmlGroupTargetSchema::GroupTargetType >
    GroupTarget (const ::std::wstring& uri,
                 ::xml_schema::error_handler& eh,
                 ::xml_schema::flags f = 0,
                 const ::xml_schema::properties& p = ::xml_schema::properties ());

    ::std::auto_ptr< ::middleware::lxacmlGroupTargetSchema::GroupTargetType >
    GroupTarget (const ::std::wstring& uri,
                 ::xercesc::DOMErrorHandler& eh,
                 ::xml_schema::flags f = 0,
                 const ::xml_schema::properties& p = ::xml_schema::properties ());

    // Parse std::istream.
    //

    ::std::auto_ptr< ::middleware::lxacmlGroupTargetSchema::GroupTargetType >
    GroupTarget (::std::istream& is,
                 ::xml_schema::flags f = 0,
                 const ::xml_schema::properties& p = ::xml_schema::properties ());

    ::std::auto_ptr< ::middleware::lxacmlGroupTargetSchema::GroupTargetType >
    GroupTarget (::std::istream& is,
                 ::xml_schema::error_handler& eh,
                 ::xml_schema::flags f = 0,
                 const ::xml_schema::properties& p = ::xml_schema::properties ());

    ::std::auto_ptr< ::middleware::lxacmlGroupTargetSchema::GroupTargetType >
    GroupTarget (::std::istream& is,
                 ::xercesc::DOMErrorHandler& eh,
                 ::xml_schema::flags f = 0,
                 const ::xml_schema::properties& p = ::xml_schema::properties ());

    ::std::auto_ptr< ::middleware::lxacmlGroupTargetSchema::GroupTargetType >
    GroupTarget (::std::istream& is,
                 const ::std::wstring& id,
                 ::xml_schema::flags f = 0,
                 const ::xml_schema::properties& p = ::xml_schema::properties ());

    ::std::auto_ptr< ::middleware::lxacmlGroupTargetSchema::GroupTargetType >
    GroupTarget (::std::istream& is,
                 const ::std::wstring& id,
                 ::xml_schema::error_handler& eh,
                 ::xml_schema::flags f = 0,
                 const ::xml_schema::properties& p = ::xml_schema::properties ());

    ::std::auto_ptr< ::middleware::lxacmlGroupTargetSchema::GroupTargetType >
    GroupTarget (::std::istream& is,
                 const ::std::wstring& id,
                 ::xercesc::DOMErrorHandler& eh,
                 ::xml_schema::flags f = 0,
                 const ::xml_schema::properties& p = ::xml_schema::properties ());

    // Parse xercesc::InputSource.
    //

    ::std::auto_ptr< ::middleware::lxacmlGroupTargetSchema::GroupTargetType >
    GroupTarget (::xercesc::InputSource& is,
                 ::xml_schema::flags f = 0,
                 const ::xml_schema::properties& p = ::xml_schema::properties ());

    ::std::auto_ptr< ::middleware::lxacmlGroupTargetSchema::GroupTargetType >
    GroupTarget (::xercesc::InputSource& is,
                 ::xml_schema::error_handler& eh,
                 ::xml_schema::flags f = 0,
                 const ::xml_schema::properties& p = ::xml_schema::properties ());

    ::std::auto_ptr< ::middleware::lxacmlGroupTargetSchema::GroupTargetType >
    GroupTarget (::xercesc::InputSource& is,
                 ::xercesc::DOMErrorHandler& eh,
                 ::xml_schema::flags f = 0,
                 const ::xml_schema::properties& p = ::xml_schema::properties ());

    // Parse xercesc::DOMDocument.
    //

    ::std::auto_ptr< ::middleware::lxacmlGroupTargetSchema::GroupTargetType >
    GroupTarget (const ::xercesc::DOMDocument& d,
                 ::xml_schema::flags f = 0,
                 const ::xml_schema::properties& p = ::xml_schema::properties ());

    ::std::auto_ptr< ::middleware::lxacmlGroupTargetSchema::GroupTargetType >
    GroupTarget (::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument >& d,
                 ::xml_schema::flags f = 0,
                 const ::xml_schema::properties& p = ::xml_schema::properties ());
  }
}

#include <iosfwd>

#include <xercesc/dom/DOMDocument.hpp>
#include <xercesc/dom/DOMErrorHandler.hpp>
#include <xercesc/framework/XMLFormatter.hpp>

#include <xsd/cxx/xml/dom/auto-ptr.hxx>

namespace middleware
{
  namespace lxacmlGroupTargetSchema
  {
    // Serialize to std::ostream.
    //

    void
    GroupTarget (::std::ostream& os,
                 const ::middleware::lxacmlGroupTargetSchema::GroupTargetType& x, 
                 const ::xml_schema::namespace_infomap& m = ::xml_schema::namespace_infomap (),
                 const ::std::wstring& e = L"UTF-8",
                 ::xml_schema::flags f = 0);

    void
    GroupTarget (::std::ostream& os,
                 const ::middleware::lxacmlGroupTargetSchema::GroupTargetType& x, 
                 ::xml_schema::error_handler& eh,
                 const ::xml_schema::namespace_infomap& m = ::xml_schema::namespace_infomap (),
                 const ::std::wstring& e = L"UTF-8",
                 ::xml_schema::flags f = 0);

    void
    GroupTarget (::std::ostream& os,
                 const ::middleware::lxacmlGroupTargetSchema::GroupTargetType& x, 
                 ::xercesc::DOMErrorHandler& eh,
                 const ::xml_schema::namespace_infomap& m = ::xml_schema::namespace_infomap (),
                 const ::std::wstring& e = L"UTF-8",
                 ::xml_schema::flags f = 0);

    // Serialize to xercesc::XMLFormatTarget.
    //

    void
    GroupTarget (::xercesc::XMLFormatTarget& ft,
                 const ::middleware::lxacmlGroupTargetSchema::GroupTargetType& x, 
                 const ::xml_schema::namespace_infomap& m = ::xml_schema::namespace_infomap (),
                 const ::std::wstring& e = L"UTF-8",
                 ::xml_schema::flags f = 0);

    void
    GroupTarget (::xercesc::XMLFormatTarget& ft,
                 const ::middleware::lxacmlGroupTargetSchema::GroupTargetType& x, 
                 ::xml_schema::error_handler& eh,
                 const ::xml_schema::namespace_infomap& m = ::xml_schema::namespace_infomap (),
                 const ::std::wstring& e = L"UTF-8",
                 ::xml_schema::flags f = 0);

    void
    GroupTarget (::xercesc::XMLFormatTarget& ft,
                 const ::middleware::lxacmlGroupTargetSchema::GroupTargetType& x, 
                 ::xercesc::DOMErrorHandler& eh,
                 const ::xml_schema::namespace_infomap& m = ::xml_schema::namespace_infomap (),
                 const ::std::wstring& e = L"UTF-8",
                 ::xml_schema::flags f = 0);

    // Serialize to an existing xercesc::DOMDocument.
    //

    void
    GroupTarget (::xercesc::DOMDocument& d,
                 const ::middleware::lxacmlGroupTargetSchema::GroupTargetType& x,
                 ::xml_schema::flags f = 0);

    // Serialize to a new xercesc::DOMDocument.
    //

    ::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument >
    GroupTarget (const ::middleware::lxacmlGroupTargetSchema::GroupTargetType& x, 
                 const ::xml_schema::namespace_infomap& m = ::xml_schema::namespace_infomap (),
                 ::xml_schema::flags f = 0);

    void
    operator<< (::xercesc::DOMElement&, const GroupTargetType&);
  }
}

#include <xsd/cxx/post.hxx>

// Begin epilogue.
//
//
// End epilogue.

#endif // CXX_HOME_KAURM_DEV_WORKSPACE_TEMPESOEPROJ_ESOEPROJECT_SAML2CPP_SCHEMA_LXACML_SCHEMA_GROUPTARGET_HXX

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

#ifndef CXX_HOME_KAURM_DEV_WORKSPACE_TEMPESOEPROJ_ESOEPROJECT_SAML2CPP_SCHEMA_DELEGATED_SCHEMA_SAML_PROTOCOL_HXX
#define CXX_HOME_KAURM_DEV_WORKSPACE_TEMPESOEPROJ_ESOEPROJECT_SAML2CPP_SCHEMA_DELEGATED_SCHEMA_SAML_PROTOCOL_HXX

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
  namespace DelegatedProtocolSchema
  {
    class RegisterPrincipalRequestType;
    class RegisterPrincipalResponseType;
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

#include "saml-schema-assertion-2.0.hxx"

#include "saml-schema-protocol-2.0.hxx"

namespace middleware
{
  namespace DelegatedProtocolSchema
  {
    class RegisterPrincipalRequestType: public ::saml2::protocol::RequestAbstractType
    {
      public:
      // principalAuthnIdentifier
      // 
      typedef ::xml_schema::string principalAuthnIdentifier_type;
      typedef ::xsd::cxx::tree::traits< principalAuthnIdentifier_type, wchar_t > principalAuthnIdentifier_traits;

      const principalAuthnIdentifier_type&
      principalAuthnIdentifier () const;

      principalAuthnIdentifier_type&
      principalAuthnIdentifier ();

      void
      principalAuthnIdentifier (const principalAuthnIdentifier_type& x);

      void
      principalAuthnIdentifier (::std::auto_ptr< principalAuthnIdentifier_type > p);

      // Attribute
      // 
      typedef ::saml2::assertion::AttributeType Attribute_type;
      typedef ::xsd::cxx::tree::sequence< Attribute_type > Attribute_sequence;
      typedef Attribute_sequence::iterator Attribute_iterator;
      typedef Attribute_sequence::const_iterator Attribute_const_iterator;
      typedef ::xsd::cxx::tree::traits< Attribute_type, wchar_t > Attribute_traits;

      const Attribute_sequence&
      Attribute () const;

      Attribute_sequence&
      Attribute ();

      void
      Attribute (const Attribute_sequence& s);

      // EncryptedAttribute
      // 
      typedef ::saml2::assertion::EncryptedElementType EncryptedAttribute_type;
      typedef ::xsd::cxx::tree::sequence< EncryptedAttribute_type > EncryptedAttribute_sequence;
      typedef EncryptedAttribute_sequence::iterator EncryptedAttribute_iterator;
      typedef EncryptedAttribute_sequence::const_iterator EncryptedAttribute_const_iterator;
      typedef ::xsd::cxx::tree::traits< EncryptedAttribute_type, wchar_t > EncryptedAttribute_traits;

      const EncryptedAttribute_sequence&
      EncryptedAttribute () const;

      EncryptedAttribute_sequence&
      EncryptedAttribute ();

      void
      EncryptedAttribute (const EncryptedAttribute_sequence& s);

      // Source
      // 
      typedef ::xml_schema::string Source_type;
      typedef ::xsd::cxx::tree::traits< Source_type, wchar_t > Source_traits;

      const Source_type&
      Source () const;

      Source_type&
      Source ();

      void
      Source (const Source_type& x);

      void
      Source (::std::auto_ptr< Source_type > p);

      // Constructors.
      //
      RegisterPrincipalRequestType ();

      RegisterPrincipalRequestType (const ID_type&,
                                    const Version_type&,
                                    const IssueInstant_type&,
                                    const principalAuthnIdentifier_type&,
                                    const Source_type&);

      RegisterPrincipalRequestType (const ID_type&,
                                    const Version_type&,
                                    const IssueInstant_type&,
                                    ::std::auto_ptr< principalAuthnIdentifier_type >&,
                                    const Source_type&);

      RegisterPrincipalRequestType (const ::xercesc::DOMElement& e,
                                    ::xml_schema::flags f = 0,
                                    ::xml_schema::container* c = 0);

      RegisterPrincipalRequestType (const RegisterPrincipalRequestType& x,
                                    ::xml_schema::flags f = 0,
                                    ::xml_schema::container* c = 0);

      virtual RegisterPrincipalRequestType*
      _clone (::xml_schema::flags f = 0,
              ::xml_schema::container* c = 0) const;

      virtual 
      ~RegisterPrincipalRequestType ();

      // Implementation.
      //
      protected:
      void
      parse (::xsd::cxx::xml::dom::parser< wchar_t >&,
             ::xml_schema::flags);

      protected:
      ::xsd::cxx::tree::one< principalAuthnIdentifier_type > principalAuthnIdentifier_;
      Attribute_sequence Attribute_;
      EncryptedAttribute_sequence EncryptedAttribute_;
      ::xsd::cxx::tree::one< Source_type > Source_;
    };

    class RegisterPrincipalResponseType: public ::saml2::protocol::StatusResponseType
    {
      public:
      // sessionIdentifier
      // 
      typedef ::xml_schema::string sessionIdentifier_type;
      typedef ::xsd::cxx::tree::optional< sessionIdentifier_type > sessionIdentifier_optional;
      typedef ::xsd::cxx::tree::traits< sessionIdentifier_type, wchar_t > sessionIdentifier_traits;

      const sessionIdentifier_optional&
      sessionIdentifier () const;

      sessionIdentifier_optional&
      sessionIdentifier ();

      void
      sessionIdentifier (const sessionIdentifier_type& x);

      void
      sessionIdentifier (const sessionIdentifier_optional& x);

      void
      sessionIdentifier (::std::auto_ptr< sessionIdentifier_type > p);

      // Constructors.
      //
      RegisterPrincipalResponseType ();

      RegisterPrincipalResponseType (const Status_type&,
                                     const ID_type&,
                                     const Version_type&,
                                     const IssueInstant_type&);

      RegisterPrincipalResponseType (::std::auto_ptr< Status_type >&,
                                     const ID_type&,
                                     const Version_type&,
                                     const IssueInstant_type&);

      RegisterPrincipalResponseType (const ::xercesc::DOMElement& e,
                                     ::xml_schema::flags f = 0,
                                     ::xml_schema::container* c = 0);

      RegisterPrincipalResponseType (const RegisterPrincipalResponseType& x,
                                     ::xml_schema::flags f = 0,
                                     ::xml_schema::container* c = 0);

      virtual RegisterPrincipalResponseType*
      _clone (::xml_schema::flags f = 0,
              ::xml_schema::container* c = 0) const;

      virtual 
      ~RegisterPrincipalResponseType ();

      // Implementation.
      //
      protected:
      void
      parse (::xsd::cxx::xml::dom::parser< wchar_t >&,
             ::xml_schema::flags);

      protected:
      sessionIdentifier_optional sessionIdentifier_;
    };
  }
}

#include <iosfwd>

#include <xercesc/sax/InputSource.hpp>
#include <xercesc/dom/DOMDocument.hpp>
#include <xercesc/dom/DOMErrorHandler.hpp>

namespace middleware
{
  namespace DelegatedProtocolSchema
  {
    // Parse a URI or a local file.
    //

    ::std::auto_ptr< ::middleware::DelegatedProtocolSchema::RegisterPrincipalRequestType >
    RegisterPrincipalRequest (const ::std::wstring& uri,
                              ::xml_schema::flags f = 0,
                              const ::xml_schema::properties& p = ::xml_schema::properties ());

    ::std::auto_ptr< ::middleware::DelegatedProtocolSchema::RegisterPrincipalRequestType >
    RegisterPrincipalRequest (const ::std::wstring& uri,
                              ::xml_schema::error_handler& eh,
                              ::xml_schema::flags f = 0,
                              const ::xml_schema::properties& p = ::xml_schema::properties ());

    ::std::auto_ptr< ::middleware::DelegatedProtocolSchema::RegisterPrincipalRequestType >
    RegisterPrincipalRequest (const ::std::wstring& uri,
                              ::xercesc::DOMErrorHandler& eh,
                              ::xml_schema::flags f = 0,
                              const ::xml_schema::properties& p = ::xml_schema::properties ());

    // Parse std::istream.
    //

    ::std::auto_ptr< ::middleware::DelegatedProtocolSchema::RegisterPrincipalRequestType >
    RegisterPrincipalRequest (::std::istream& is,
                              ::xml_schema::flags f = 0,
                              const ::xml_schema::properties& p = ::xml_schema::properties ());

    ::std::auto_ptr< ::middleware::DelegatedProtocolSchema::RegisterPrincipalRequestType >
    RegisterPrincipalRequest (::std::istream& is,
                              ::xml_schema::error_handler& eh,
                              ::xml_schema::flags f = 0,
                              const ::xml_schema::properties& p = ::xml_schema::properties ());

    ::std::auto_ptr< ::middleware::DelegatedProtocolSchema::RegisterPrincipalRequestType >
    RegisterPrincipalRequest (::std::istream& is,
                              ::xercesc::DOMErrorHandler& eh,
                              ::xml_schema::flags f = 0,
                              const ::xml_schema::properties& p = ::xml_schema::properties ());

    ::std::auto_ptr< ::middleware::DelegatedProtocolSchema::RegisterPrincipalRequestType >
    RegisterPrincipalRequest (::std::istream& is,
                              const ::std::wstring& id,
                              ::xml_schema::flags f = 0,
                              const ::xml_schema::properties& p = ::xml_schema::properties ());

    ::std::auto_ptr< ::middleware::DelegatedProtocolSchema::RegisterPrincipalRequestType >
    RegisterPrincipalRequest (::std::istream& is,
                              const ::std::wstring& id,
                              ::xml_schema::error_handler& eh,
                              ::xml_schema::flags f = 0,
                              const ::xml_schema::properties& p = ::xml_schema::properties ());

    ::std::auto_ptr< ::middleware::DelegatedProtocolSchema::RegisterPrincipalRequestType >
    RegisterPrincipalRequest (::std::istream& is,
                              const ::std::wstring& id,
                              ::xercesc::DOMErrorHandler& eh,
                              ::xml_schema::flags f = 0,
                              const ::xml_schema::properties& p = ::xml_schema::properties ());

    // Parse xercesc::InputSource.
    //

    ::std::auto_ptr< ::middleware::DelegatedProtocolSchema::RegisterPrincipalRequestType >
    RegisterPrincipalRequest (::xercesc::InputSource& is,
                              ::xml_schema::flags f = 0,
                              const ::xml_schema::properties& p = ::xml_schema::properties ());

    ::std::auto_ptr< ::middleware::DelegatedProtocolSchema::RegisterPrincipalRequestType >
    RegisterPrincipalRequest (::xercesc::InputSource& is,
                              ::xml_schema::error_handler& eh,
                              ::xml_schema::flags f = 0,
                              const ::xml_schema::properties& p = ::xml_schema::properties ());

    ::std::auto_ptr< ::middleware::DelegatedProtocolSchema::RegisterPrincipalRequestType >
    RegisterPrincipalRequest (::xercesc::InputSource& is,
                              ::xercesc::DOMErrorHandler& eh,
                              ::xml_schema::flags f = 0,
                              const ::xml_schema::properties& p = ::xml_schema::properties ());

    // Parse xercesc::DOMDocument.
    //

    ::std::auto_ptr< ::middleware::DelegatedProtocolSchema::RegisterPrincipalRequestType >
    RegisterPrincipalRequest (const ::xercesc::DOMDocument& d,
                              ::xml_schema::flags f = 0,
                              const ::xml_schema::properties& p = ::xml_schema::properties ());

    ::std::auto_ptr< ::middleware::DelegatedProtocolSchema::RegisterPrincipalRequestType >
    RegisterPrincipalRequest (::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument >& d,
                              ::xml_schema::flags f = 0,
                              const ::xml_schema::properties& p = ::xml_schema::properties ());

    // Parse a URI or a local file.
    //

    ::std::auto_ptr< ::middleware::DelegatedProtocolSchema::RegisterPrincipalResponseType >
    RegisterPrincipalResponse (const ::std::wstring& uri,
                               ::xml_schema::flags f = 0,
                               const ::xml_schema::properties& p = ::xml_schema::properties ());

    ::std::auto_ptr< ::middleware::DelegatedProtocolSchema::RegisterPrincipalResponseType >
    RegisterPrincipalResponse (const ::std::wstring& uri,
                               ::xml_schema::error_handler& eh,
                               ::xml_schema::flags f = 0,
                               const ::xml_schema::properties& p = ::xml_schema::properties ());

    ::std::auto_ptr< ::middleware::DelegatedProtocolSchema::RegisterPrincipalResponseType >
    RegisterPrincipalResponse (const ::std::wstring& uri,
                               ::xercesc::DOMErrorHandler& eh,
                               ::xml_schema::flags f = 0,
                               const ::xml_schema::properties& p = ::xml_schema::properties ());

    // Parse std::istream.
    //

    ::std::auto_ptr< ::middleware::DelegatedProtocolSchema::RegisterPrincipalResponseType >
    RegisterPrincipalResponse (::std::istream& is,
                               ::xml_schema::flags f = 0,
                               const ::xml_schema::properties& p = ::xml_schema::properties ());

    ::std::auto_ptr< ::middleware::DelegatedProtocolSchema::RegisterPrincipalResponseType >
    RegisterPrincipalResponse (::std::istream& is,
                               ::xml_schema::error_handler& eh,
                               ::xml_schema::flags f = 0,
                               const ::xml_schema::properties& p = ::xml_schema::properties ());

    ::std::auto_ptr< ::middleware::DelegatedProtocolSchema::RegisterPrincipalResponseType >
    RegisterPrincipalResponse (::std::istream& is,
                               ::xercesc::DOMErrorHandler& eh,
                               ::xml_schema::flags f = 0,
                               const ::xml_schema::properties& p = ::xml_schema::properties ());

    ::std::auto_ptr< ::middleware::DelegatedProtocolSchema::RegisterPrincipalResponseType >
    RegisterPrincipalResponse (::std::istream& is,
                               const ::std::wstring& id,
                               ::xml_schema::flags f = 0,
                               const ::xml_schema::properties& p = ::xml_schema::properties ());

    ::std::auto_ptr< ::middleware::DelegatedProtocolSchema::RegisterPrincipalResponseType >
    RegisterPrincipalResponse (::std::istream& is,
                               const ::std::wstring& id,
                               ::xml_schema::error_handler& eh,
                               ::xml_schema::flags f = 0,
                               const ::xml_schema::properties& p = ::xml_schema::properties ());

    ::std::auto_ptr< ::middleware::DelegatedProtocolSchema::RegisterPrincipalResponseType >
    RegisterPrincipalResponse (::std::istream& is,
                               const ::std::wstring& id,
                               ::xercesc::DOMErrorHandler& eh,
                               ::xml_schema::flags f = 0,
                               const ::xml_schema::properties& p = ::xml_schema::properties ());

    // Parse xercesc::InputSource.
    //

    ::std::auto_ptr< ::middleware::DelegatedProtocolSchema::RegisterPrincipalResponseType >
    RegisterPrincipalResponse (::xercesc::InputSource& is,
                               ::xml_schema::flags f = 0,
                               const ::xml_schema::properties& p = ::xml_schema::properties ());

    ::std::auto_ptr< ::middleware::DelegatedProtocolSchema::RegisterPrincipalResponseType >
    RegisterPrincipalResponse (::xercesc::InputSource& is,
                               ::xml_schema::error_handler& eh,
                               ::xml_schema::flags f = 0,
                               const ::xml_schema::properties& p = ::xml_schema::properties ());

    ::std::auto_ptr< ::middleware::DelegatedProtocolSchema::RegisterPrincipalResponseType >
    RegisterPrincipalResponse (::xercesc::InputSource& is,
                               ::xercesc::DOMErrorHandler& eh,
                               ::xml_schema::flags f = 0,
                               const ::xml_schema::properties& p = ::xml_schema::properties ());

    // Parse xercesc::DOMDocument.
    //

    ::std::auto_ptr< ::middleware::DelegatedProtocolSchema::RegisterPrincipalResponseType >
    RegisterPrincipalResponse (const ::xercesc::DOMDocument& d,
                               ::xml_schema::flags f = 0,
                               const ::xml_schema::properties& p = ::xml_schema::properties ());

    ::std::auto_ptr< ::middleware::DelegatedProtocolSchema::RegisterPrincipalResponseType >
    RegisterPrincipalResponse (::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument >& d,
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
  namespace DelegatedProtocolSchema
  {
    // Serialize to std::ostream.
    //

    void
    RegisterPrincipalRequest (::std::ostream& os,
                              const ::middleware::DelegatedProtocolSchema::RegisterPrincipalRequestType& x, 
                              const ::xml_schema::namespace_infomap& m = ::xml_schema::namespace_infomap (),
                              const ::std::wstring& e = L"UTF-8",
                              ::xml_schema::flags f = 0);

    void
    RegisterPrincipalRequest (::std::ostream& os,
                              const ::middleware::DelegatedProtocolSchema::RegisterPrincipalRequestType& x, 
                              ::xml_schema::error_handler& eh,
                              const ::xml_schema::namespace_infomap& m = ::xml_schema::namespace_infomap (),
                              const ::std::wstring& e = L"UTF-8",
                              ::xml_schema::flags f = 0);

    void
    RegisterPrincipalRequest (::std::ostream& os,
                              const ::middleware::DelegatedProtocolSchema::RegisterPrincipalRequestType& x, 
                              ::xercesc::DOMErrorHandler& eh,
                              const ::xml_schema::namespace_infomap& m = ::xml_schema::namespace_infomap (),
                              const ::std::wstring& e = L"UTF-8",
                              ::xml_schema::flags f = 0);

    // Serialize to xercesc::XMLFormatTarget.
    //

    void
    RegisterPrincipalRequest (::xercesc::XMLFormatTarget& ft,
                              const ::middleware::DelegatedProtocolSchema::RegisterPrincipalRequestType& x, 
                              const ::xml_schema::namespace_infomap& m = ::xml_schema::namespace_infomap (),
                              const ::std::wstring& e = L"UTF-8",
                              ::xml_schema::flags f = 0);

    void
    RegisterPrincipalRequest (::xercesc::XMLFormatTarget& ft,
                              const ::middleware::DelegatedProtocolSchema::RegisterPrincipalRequestType& x, 
                              ::xml_schema::error_handler& eh,
                              const ::xml_schema::namespace_infomap& m = ::xml_schema::namespace_infomap (),
                              const ::std::wstring& e = L"UTF-8",
                              ::xml_schema::flags f = 0);

    void
    RegisterPrincipalRequest (::xercesc::XMLFormatTarget& ft,
                              const ::middleware::DelegatedProtocolSchema::RegisterPrincipalRequestType& x, 
                              ::xercesc::DOMErrorHandler& eh,
                              const ::xml_schema::namespace_infomap& m = ::xml_schema::namespace_infomap (),
                              const ::std::wstring& e = L"UTF-8",
                              ::xml_schema::flags f = 0);

    // Serialize to an existing xercesc::DOMDocument.
    //

    void
    RegisterPrincipalRequest (::xercesc::DOMDocument& d,
                              const ::middleware::DelegatedProtocolSchema::RegisterPrincipalRequestType& x,
                              ::xml_schema::flags f = 0);

    // Serialize to a new xercesc::DOMDocument.
    //

    ::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument >
    RegisterPrincipalRequest (const ::middleware::DelegatedProtocolSchema::RegisterPrincipalRequestType& x, 
                              const ::xml_schema::namespace_infomap& m = ::xml_schema::namespace_infomap (),
                              ::xml_schema::flags f = 0);

    void
    operator<< (::xercesc::DOMElement&, const RegisterPrincipalRequestType&);

    // Serialize to std::ostream.
    //

    void
    RegisterPrincipalResponse (::std::ostream& os,
                               const ::middleware::DelegatedProtocolSchema::RegisterPrincipalResponseType& x, 
                               const ::xml_schema::namespace_infomap& m = ::xml_schema::namespace_infomap (),
                               const ::std::wstring& e = L"UTF-8",
                               ::xml_schema::flags f = 0);

    void
    RegisterPrincipalResponse (::std::ostream& os,
                               const ::middleware::DelegatedProtocolSchema::RegisterPrincipalResponseType& x, 
                               ::xml_schema::error_handler& eh,
                               const ::xml_schema::namespace_infomap& m = ::xml_schema::namespace_infomap (),
                               const ::std::wstring& e = L"UTF-8",
                               ::xml_schema::flags f = 0);

    void
    RegisterPrincipalResponse (::std::ostream& os,
                               const ::middleware::DelegatedProtocolSchema::RegisterPrincipalResponseType& x, 
                               ::xercesc::DOMErrorHandler& eh,
                               const ::xml_schema::namespace_infomap& m = ::xml_schema::namespace_infomap (),
                               const ::std::wstring& e = L"UTF-8",
                               ::xml_schema::flags f = 0);

    // Serialize to xercesc::XMLFormatTarget.
    //

    void
    RegisterPrincipalResponse (::xercesc::XMLFormatTarget& ft,
                               const ::middleware::DelegatedProtocolSchema::RegisterPrincipalResponseType& x, 
                               const ::xml_schema::namespace_infomap& m = ::xml_schema::namespace_infomap (),
                               const ::std::wstring& e = L"UTF-8",
                               ::xml_schema::flags f = 0);

    void
    RegisterPrincipalResponse (::xercesc::XMLFormatTarget& ft,
                               const ::middleware::DelegatedProtocolSchema::RegisterPrincipalResponseType& x, 
                               ::xml_schema::error_handler& eh,
                               const ::xml_schema::namespace_infomap& m = ::xml_schema::namespace_infomap (),
                               const ::std::wstring& e = L"UTF-8",
                               ::xml_schema::flags f = 0);

    void
    RegisterPrincipalResponse (::xercesc::XMLFormatTarget& ft,
                               const ::middleware::DelegatedProtocolSchema::RegisterPrincipalResponseType& x, 
                               ::xercesc::DOMErrorHandler& eh,
                               const ::xml_schema::namespace_infomap& m = ::xml_schema::namespace_infomap (),
                               const ::std::wstring& e = L"UTF-8",
                               ::xml_schema::flags f = 0);

    // Serialize to an existing xercesc::DOMDocument.
    //

    void
    RegisterPrincipalResponse (::xercesc::DOMDocument& d,
                               const ::middleware::DelegatedProtocolSchema::RegisterPrincipalResponseType& x,
                               ::xml_schema::flags f = 0);

    // Serialize to a new xercesc::DOMDocument.
    //

    ::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument >
    RegisterPrincipalResponse (const ::middleware::DelegatedProtocolSchema::RegisterPrincipalResponseType& x, 
                               const ::xml_schema::namespace_infomap& m = ::xml_schema::namespace_infomap (),
                               ::xml_schema::flags f = 0);

    void
    operator<< (::xercesc::DOMElement&, const RegisterPrincipalResponseType&);
  }
}

#include <xsd/cxx/post.hxx>

// Begin epilogue.
//
//
// End epilogue.

#endif // CXX_HOME_KAURM_DEV_WORKSPACE_TEMPESOEPROJ_ESOEPROJECT_SAML2CPP_SCHEMA_DELEGATED_SCHEMA_SAML_PROTOCOL_HXX

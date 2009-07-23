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

// Begin prologue.
//
//
// End prologue.

#include <xsd/cxx/pre.hxx>

#include "saml2/bindings/cacheclear-schema-saml-metadata.hxx"

namespace middleware
{
  namespace cacheClearServiceSchema
  {
    // CacheClearServiceType
    // 
  }
}

#include <xsd/cxx/xml/dom/wildcard-source.hxx>

#include <xsd/cxx/xml/dom/parsing-source.hxx>

#include <xsd/cxx/tree/type-factory-map.hxx>

namespace _xsd
{
  static
  const ::xsd::cxx::tree::type_factory_plate< 0, wchar_t >
  type_factory_plate_init;
}

namespace middleware
{
  namespace cacheClearServiceSchema
  {
    // CacheClearServiceType
    //

    CacheClearServiceType::
    CacheClearServiceType ()
    : ::saml2::metadata::IndexedEndpointType ()
    {
    }

    CacheClearServiceType::
    CacheClearServiceType (const Binding_type& Binding,
                           const Location_type& Location,
                           const index_type& index)
    : ::saml2::metadata::IndexedEndpointType (Binding,
                                              Location,
                                              index)
    {
    }

    CacheClearServiceType::
    CacheClearServiceType (const CacheClearServiceType& x,
                           ::xml_schema::flags f,
                           ::xml_schema::container* c)
    : ::saml2::metadata::IndexedEndpointType (x, f, c)
    {
    }

    CacheClearServiceType::
    CacheClearServiceType (const ::xercesc::DOMElement& e,
                           ::xml_schema::flags f,
                           ::xml_schema::container* c)
    : ::saml2::metadata::IndexedEndpointType (e, f, c)
    {
    }

    CacheClearServiceType* CacheClearServiceType::
    _clone (::xml_schema::flags f,
            ::xml_schema::container* c) const
    {
      return new class CacheClearServiceType (*this, f, c);
    }

    CacheClearServiceType::
    ~CacheClearServiceType ()
    {
    }

    static
    const ::xsd::cxx::tree::type_factory_initializer< 0, wchar_t, CacheClearServiceType >
    _xsd_CacheClearServiceType_type_factory_init (
      L"CacheClearServiceType",
      L"http://www.qut.com/middleware/cacheClearServiceSchema");
  }
}

#include <istream>
#include <xsd/cxx/xml/sax/std-input-source.hxx>
#include <xsd/cxx/tree/error-handler.hxx>

namespace middleware
{
  namespace cacheClearServiceSchema
  {
    ::std::auto_ptr< ::middleware::cacheClearServiceSchema::CacheClearServiceType >
    CacheClearService (const ::std::wstring& u,
                       ::xml_schema::flags f,
                       const ::xml_schema::properties& p)
    {
      ::xsd::cxx::xml::auto_initializer i (
        (f & ::xml_schema::flags::dont_initialize) == 0,
        (f & ::xml_schema::flags::keep_dom) == 0);

      ::xsd::cxx::tree::error_handler< wchar_t > h;

      ::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument > d (
        ::xsd::cxx::xml::dom::parse< wchar_t > (u, h, p, f));

      h.throw_if_failed< ::xsd::cxx::tree::parsing< wchar_t > > ();

      ::std::auto_ptr< ::middleware::cacheClearServiceSchema::CacheClearServiceType > r (
        ::middleware::cacheClearServiceSchema::CacheClearService (
          d, f | ::xml_schema::flags::own_dom, p));

      return r;
    }

    ::std::auto_ptr< ::middleware::cacheClearServiceSchema::CacheClearServiceType >
    CacheClearService (const ::std::wstring& u,
                       ::xml_schema::error_handler& h,
                       ::xml_schema::flags f,
                       const ::xml_schema::properties& p)
    {
      ::xsd::cxx::xml::auto_initializer i (
        (f & ::xml_schema::flags::dont_initialize) == 0,
        (f & ::xml_schema::flags::keep_dom) == 0);

      ::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument > d (
        ::xsd::cxx::xml::dom::parse< wchar_t > (u, h, p, f));

      if (!d.get ())
        throw ::xsd::cxx::tree::parsing< wchar_t > ();

      ::std::auto_ptr< ::middleware::cacheClearServiceSchema::CacheClearServiceType > r (
        ::middleware::cacheClearServiceSchema::CacheClearService (
          d, f | ::xml_schema::flags::own_dom, p));

      return r;
    }

    ::std::auto_ptr< ::middleware::cacheClearServiceSchema::CacheClearServiceType >
    CacheClearService (const ::std::wstring& u,
                       ::xercesc::DOMErrorHandler& h,
                       ::xml_schema::flags f,
                       const ::xml_schema::properties& p)
    {
      ::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument > d (
        ::xsd::cxx::xml::dom::parse< wchar_t > (u, h, p, f));

      if (!d.get ())
        throw ::xsd::cxx::tree::parsing< wchar_t > ();

      ::std::auto_ptr< ::middleware::cacheClearServiceSchema::CacheClearServiceType > r (
        ::middleware::cacheClearServiceSchema::CacheClearService (
          d, f | ::xml_schema::flags::own_dom, p));

      return r;
    }

    ::std::auto_ptr< ::middleware::cacheClearServiceSchema::CacheClearServiceType >
    CacheClearService (::std::istream& is,
                       ::xml_schema::flags f,
                       const ::xml_schema::properties& p)
    {
      ::xsd::cxx::xml::auto_initializer i (
        (f & ::xml_schema::flags::dont_initialize) == 0,
        (f & ::xml_schema::flags::keep_dom) == 0);

      ::xsd::cxx::xml::sax::std_input_source isrc (is);
      return ::middleware::cacheClearServiceSchema::CacheClearService (isrc, f, p);
    }

    ::std::auto_ptr< ::middleware::cacheClearServiceSchema::CacheClearServiceType >
    CacheClearService (::std::istream& is,
                       ::xml_schema::error_handler& h,
                       ::xml_schema::flags f,
                       const ::xml_schema::properties& p)
    {
      ::xsd::cxx::xml::auto_initializer i (
        (f & ::xml_schema::flags::dont_initialize) == 0,
        (f & ::xml_schema::flags::keep_dom) == 0);

      ::xsd::cxx::xml::sax::std_input_source isrc (is);
      return ::middleware::cacheClearServiceSchema::CacheClearService (isrc, h, f, p);
    }

    ::std::auto_ptr< ::middleware::cacheClearServiceSchema::CacheClearServiceType >
    CacheClearService (::std::istream& is,
                       ::xercesc::DOMErrorHandler& h,
                       ::xml_schema::flags f,
                       const ::xml_schema::properties& p)
    {
      ::xsd::cxx::xml::sax::std_input_source isrc (is);
      return ::middleware::cacheClearServiceSchema::CacheClearService (isrc, h, f, p);
    }

    ::std::auto_ptr< ::middleware::cacheClearServiceSchema::CacheClearServiceType >
    CacheClearService (::std::istream& is,
                       const ::std::wstring& sid,
                       ::xml_schema::flags f,
                       const ::xml_schema::properties& p)
    {
      ::xsd::cxx::xml::auto_initializer i (
        (f & ::xml_schema::flags::dont_initialize) == 0,
        (f & ::xml_schema::flags::keep_dom) == 0);

      ::xsd::cxx::xml::sax::std_input_source isrc (is, sid);
      return ::middleware::cacheClearServiceSchema::CacheClearService (isrc, f, p);
    }

    ::std::auto_ptr< ::middleware::cacheClearServiceSchema::CacheClearServiceType >
    CacheClearService (::std::istream& is,
                       const ::std::wstring& sid,
                       ::xml_schema::error_handler& h,
                       ::xml_schema::flags f,
                       const ::xml_schema::properties& p)
    {
      ::xsd::cxx::xml::auto_initializer i (
        (f & ::xml_schema::flags::dont_initialize) == 0,
        (f & ::xml_schema::flags::keep_dom) == 0);

      ::xsd::cxx::xml::sax::std_input_source isrc (is, sid);
      return ::middleware::cacheClearServiceSchema::CacheClearService (isrc, h, f, p);
    }

    ::std::auto_ptr< ::middleware::cacheClearServiceSchema::CacheClearServiceType >
    CacheClearService (::std::istream& is,
                       const ::std::wstring& sid,
                       ::xercesc::DOMErrorHandler& h,
                       ::xml_schema::flags f,
                       const ::xml_schema::properties& p)
    {
      ::xsd::cxx::xml::sax::std_input_source isrc (is, sid);
      return ::middleware::cacheClearServiceSchema::CacheClearService (isrc, h, f, p);
    }

    ::std::auto_ptr< ::middleware::cacheClearServiceSchema::CacheClearServiceType >
    CacheClearService (::xercesc::InputSource& i,
                       ::xml_schema::flags f,
                       const ::xml_schema::properties& p)
    {
      ::xsd::cxx::tree::error_handler< wchar_t > h;

      ::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument > d (
        ::xsd::cxx::xml::dom::parse< wchar_t > (i, h, p, f));

      h.throw_if_failed< ::xsd::cxx::tree::parsing< wchar_t > > ();

      ::std::auto_ptr< ::middleware::cacheClearServiceSchema::CacheClearServiceType > r (
        ::middleware::cacheClearServiceSchema::CacheClearService (
          d, f | ::xml_schema::flags::own_dom, p));

      return r;
    }

    ::std::auto_ptr< ::middleware::cacheClearServiceSchema::CacheClearServiceType >
    CacheClearService (::xercesc::InputSource& i,
                       ::xml_schema::error_handler& h,
                       ::xml_schema::flags f,
                       const ::xml_schema::properties& p)
    {
      ::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument > d (
        ::xsd::cxx::xml::dom::parse< wchar_t > (i, h, p, f));

      if (!d.get ())
        throw ::xsd::cxx::tree::parsing< wchar_t > ();

      ::std::auto_ptr< ::middleware::cacheClearServiceSchema::CacheClearServiceType > r (
        ::middleware::cacheClearServiceSchema::CacheClearService (
          d, f | ::xml_schema::flags::own_dom, p));

      return r;
    }

    ::std::auto_ptr< ::middleware::cacheClearServiceSchema::CacheClearServiceType >
    CacheClearService (::xercesc::InputSource& i,
                       ::xercesc::DOMErrorHandler& h,
                       ::xml_schema::flags f,
                       const ::xml_schema::properties& p)
    {
      ::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument > d (
        ::xsd::cxx::xml::dom::parse< wchar_t > (i, h, p, f));

      if (!d.get ())
        throw ::xsd::cxx::tree::parsing< wchar_t > ();

      ::std::auto_ptr< ::middleware::cacheClearServiceSchema::CacheClearServiceType > r (
        ::middleware::cacheClearServiceSchema::CacheClearService (
          d, f | ::xml_schema::flags::own_dom, p));

      return r;
    }

    ::std::auto_ptr< ::middleware::cacheClearServiceSchema::CacheClearServiceType >
    CacheClearService (const ::xercesc::DOMDocument& d,
                       ::xml_schema::flags f,
                       const ::xml_schema::properties& p)
    {
      if (f & ::xml_schema::flags::keep_dom)
      {
        ::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument > c (
          static_cast< ::xercesc::DOMDocument* > (d.cloneNode (true)));

        ::std::auto_ptr< ::middleware::cacheClearServiceSchema::CacheClearServiceType > r (
          ::middleware::cacheClearServiceSchema::CacheClearService (
            c, f | ::xml_schema::flags::own_dom, p));

        return r;
      }

      const ::xercesc::DOMElement& e (*d.getDocumentElement ());
      const ::xsd::cxx::xml::qualified_name< wchar_t > n (
        ::xsd::cxx::xml::dom::name< wchar_t > (e));

      ::xsd::cxx::tree::type_factory_map< wchar_t >& tfm (
        ::xsd::cxx::tree::type_factory_map_instance< 0, wchar_t > ());

      ::std::auto_ptr< ::xsd::cxx::tree::type > tmp (
        tfm.create (
          L"CacheClearService",
          L"http://www.qut.com/middleware/cacheClearServiceSchema",
          &::xsd::cxx::tree::factory_impl< ::middleware::cacheClearServiceSchema::CacheClearServiceType >,
          true, true, e, n, f, 0));

      if (tmp.get () != 0)
      {
        ::std::auto_ptr< ::middleware::cacheClearServiceSchema::CacheClearServiceType > r (
          dynamic_cast< ::middleware::cacheClearServiceSchema::CacheClearServiceType* > (tmp.get ()));

        if (r.get ())
          tmp.release ();
        else
          throw ::xsd::cxx::tree::not_derived< wchar_t > ();

        return r;
      }

      throw ::xsd::cxx::tree::unexpected_element < wchar_t > (
        n.name (),
        n.namespace_ (),
        L"CacheClearService",
        L"http://www.qut.com/middleware/cacheClearServiceSchema");
    }

    ::std::auto_ptr< ::middleware::cacheClearServiceSchema::CacheClearServiceType >
    CacheClearService (::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument >& d,
                       ::xml_schema::flags f,
                       const ::xml_schema::properties&)
    {
      ::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument > c (
        ((f & ::xml_schema::flags::keep_dom) &&
         !(f & ::xml_schema::flags::own_dom))
        ? static_cast< ::xercesc::DOMDocument* > (d->cloneNode (true))
        : 0);

      ::xercesc::DOMDocument& doc (c.get () ? *c : *d);
      const ::xercesc::DOMElement& e (*doc.getDocumentElement ());

      const ::xsd::cxx::xml::qualified_name< wchar_t > n (
        ::xsd::cxx::xml::dom::name< wchar_t > (e));

      if (f & ::xml_schema::flags::keep_dom)
        doc.setUserData (::xml_schema::dom::tree_node_key,
                         (c.get () ? &c : &d),
                         0);

      ::xsd::cxx::tree::type_factory_map< wchar_t >& tfm (
        ::xsd::cxx::tree::type_factory_map_instance< 0, wchar_t > ());

      ::std::auto_ptr< ::xsd::cxx::tree::type > tmp (
        tfm.create (
          L"CacheClearService",
          L"http://www.qut.com/middleware/cacheClearServiceSchema",
          &::xsd::cxx::tree::factory_impl< ::middleware::cacheClearServiceSchema::CacheClearServiceType >,
          true, true, e, n, f, 0));

      if (tmp.get () != 0)
      {

        ::std::auto_ptr< ::middleware::cacheClearServiceSchema::CacheClearServiceType > r (
          dynamic_cast< ::middleware::cacheClearServiceSchema::CacheClearServiceType* > (tmp.get ()));

        if (r.get ())
          tmp.release ();
        else
          throw ::xsd::cxx::tree::not_derived< wchar_t > ();

        return r;
      }

      throw ::xsd::cxx::tree::unexpected_element < wchar_t > (
        n.name (),
        n.namespace_ (),
        L"CacheClearService",
        L"http://www.qut.com/middleware/cacheClearServiceSchema");
    }
  }
}

#include <ostream>
#include <xsd/cxx/xml/dom/serialization-source.hxx>
#include <xsd/cxx/tree/error-handler.hxx>

#include <xsd/cxx/tree/type-serializer-map.hxx>

namespace _xsd
{
  static
  const ::xsd::cxx::tree::type_serializer_plate< 0, wchar_t >
  type_serializer_plate_init;
}

namespace middleware
{
  namespace cacheClearServiceSchema
  {
    void
    CacheClearService (::std::ostream& o,
                       const ::middleware::cacheClearServiceSchema::CacheClearServiceType& s,
                       const ::xml_schema::namespace_infomap& m,
                       const ::std::wstring& e,
                       ::xml_schema::flags f)
    {
      ::xsd::cxx::xml::auto_initializer i (
        (f & ::xml_schema::flags::dont_initialize) == 0);

      ::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument > d (
        ::middleware::cacheClearServiceSchema::CacheClearService (s, m, f));

      ::xsd::cxx::tree::error_handler< wchar_t > h;

      ::xsd::cxx::xml::dom::ostream_format_target t (o);
      if (!::xsd::cxx::xml::dom::serialize (t, *d, e, h, f))
      {
        h.throw_if_failed< ::xsd::cxx::tree::serialization< wchar_t > > ();
      }
    }

    void
    CacheClearService (::std::ostream& o,
                       const ::middleware::cacheClearServiceSchema::CacheClearServiceType& s,
                       ::xml_schema::error_handler& h,
                       const ::xml_schema::namespace_infomap& m,
                       const ::std::wstring& e,
                       ::xml_schema::flags f)
    {
      ::xsd::cxx::xml::auto_initializer i (
        (f & ::xml_schema::flags::dont_initialize) == 0);

      ::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument > d (
        ::middleware::cacheClearServiceSchema::CacheClearService (s, m, f));
      ::xsd::cxx::xml::dom::ostream_format_target t (o);
      if (!::xsd::cxx::xml::dom::serialize (t, *d, e, h, f))
      {
        throw ::xsd::cxx::tree::serialization< wchar_t > ();
      }
    }

    void
    CacheClearService (::std::ostream& o,
                       const ::middleware::cacheClearServiceSchema::CacheClearServiceType& s,
                       ::xercesc::DOMErrorHandler& h,
                       const ::xml_schema::namespace_infomap& m,
                       const ::std::wstring& e,
                       ::xml_schema::flags f)
    {
      ::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument > d (
        ::middleware::cacheClearServiceSchema::CacheClearService (s, m, f));
      ::xsd::cxx::xml::dom::ostream_format_target t (o);
      if (!::xsd::cxx::xml::dom::serialize (t, *d, e, h, f))
      {
        throw ::xsd::cxx::tree::serialization< wchar_t > ();
      }
    }

    void
    CacheClearService (::xercesc::XMLFormatTarget& t,
                       const ::middleware::cacheClearServiceSchema::CacheClearServiceType& s,
                       const ::xml_schema::namespace_infomap& m,
                       const ::std::wstring& e,
                       ::xml_schema::flags f)
    {
      ::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument > d (
        ::middleware::cacheClearServiceSchema::CacheClearService (s, m, f));

      ::xsd::cxx::tree::error_handler< wchar_t > h;

      if (!::xsd::cxx::xml::dom::serialize (t, *d, e, h, f))
      {
        h.throw_if_failed< ::xsd::cxx::tree::serialization< wchar_t > > ();
      }
    }

    void
    CacheClearService (::xercesc::XMLFormatTarget& t,
                       const ::middleware::cacheClearServiceSchema::CacheClearServiceType& s,
                       ::xml_schema::error_handler& h,
                       const ::xml_schema::namespace_infomap& m,
                       const ::std::wstring& e,
                       ::xml_schema::flags f)
    {
      ::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument > d (
        ::middleware::cacheClearServiceSchema::CacheClearService (s, m, f));
      if (!::xsd::cxx::xml::dom::serialize (t, *d, e, h, f))
      {
        throw ::xsd::cxx::tree::serialization< wchar_t > ();
      }
    }

    void
    CacheClearService (::xercesc::XMLFormatTarget& t,
                       const ::middleware::cacheClearServiceSchema::CacheClearServiceType& s,
                       ::xercesc::DOMErrorHandler& h,
                       const ::xml_schema::namespace_infomap& m,
                       const ::std::wstring& e,
                       ::xml_schema::flags f)
    {
      ::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument > d (
        ::middleware::cacheClearServiceSchema::CacheClearService (s, m, f));
      if (!::xsd::cxx::xml::dom::serialize (t, *d, e, h, f))
      {
        throw ::xsd::cxx::tree::serialization< wchar_t > ();
      }
    }

    void
    CacheClearService (::xercesc::DOMDocument& d,
                       const ::middleware::cacheClearServiceSchema::CacheClearServiceType& s,
                       ::xml_schema::flags)
    {
      ::xercesc::DOMElement& e (*d.getDocumentElement ());
      const ::xsd::cxx::xml::qualified_name< wchar_t > n (
        ::xsd::cxx::xml::dom::name< wchar_t > (e));

      if (typeid (::middleware::cacheClearServiceSchema::CacheClearServiceType) == typeid (s))
      {
        if (n.name () == L"CacheClearService" &&
            n.namespace_ () == L"http://www.qut.com/middleware/cacheClearServiceSchema")
        {
          e << s;
        }
        else
        {
          throw ::xsd::cxx::tree::unexpected_element < wchar_t > (
            n.name (),
            n.namespace_ (),
            L"CacheClearService",
            L"http://www.qut.com/middleware/cacheClearServiceSchema");
        }
      }
      else
      {
        ::xsd::cxx::tree::type_serializer_map< wchar_t >& tsm (
          ::xsd::cxx::tree::type_serializer_map_instance< 0, wchar_t > ());

        tsm.serialize (
          L"CacheClearService",
          L"http://www.qut.com/middleware/cacheClearServiceSchema",
          e, n, s);
      }
    }

    ::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument >
    CacheClearService (const ::middleware::cacheClearServiceSchema::CacheClearServiceType& s,
                       const ::xml_schema::namespace_infomap& m,
                       ::xml_schema::flags f)
    {
      ::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument > d;

      if (typeid (::middleware::cacheClearServiceSchema::CacheClearServiceType) == typeid (s))
      {
        ::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument > r (
          ::xsd::cxx::xml::dom::serialize< wchar_t > (
            L"CacheClearService",
            L"http://www.qut.com/middleware/cacheClearServiceSchema",
            m, f));
        d = r;
      }
      else
      {
        ::xsd::cxx::tree::type_serializer_map< wchar_t >& tsm (
          ::xsd::cxx::tree::type_serializer_map_instance< 0, wchar_t > ());

        ::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument > r (
          tsm.serialize (
            L"CacheClearService",
            L"http://www.qut.com/middleware/cacheClearServiceSchema",
            m, s, f));
        d = r;
      }

      ::middleware::cacheClearServiceSchema::CacheClearService (*d, s, f);
      return d;
    }

    void
    operator<< (::xercesc::DOMElement& e, const CacheClearServiceType& i)
    {
      e << static_cast< const ::saml2::metadata::IndexedEndpointType& > (i);
    }

    static
    const ::xsd::cxx::tree::type_serializer_initializer< 0, wchar_t, CacheClearServiceType >
    _xsd_CacheClearServiceType_type_serializer_init (
      L"CacheClearServiceType",
      L"http://www.qut.com/middleware/cacheClearServiceSchema");
  }
}

#include <xsd/cxx/post.hxx>

// Begin epilogue.
//
//
// End epilogue.


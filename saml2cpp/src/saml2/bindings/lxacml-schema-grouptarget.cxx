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

#include "saml2/bindings/lxacml-schema-grouptarget.hxx"

namespace middleware
{
  namespace lxacmlGroupTargetSchema
  {
    // GroupTargetType
    // 

    const GroupTargetType::GroupTargetID_type& GroupTargetType::
    GroupTargetID () const
    {
      return this->GroupTargetID_.get ();
    }

    GroupTargetType::GroupTargetID_type& GroupTargetType::
    GroupTargetID ()
    {
      return this->GroupTargetID_.get ();
    }

    void GroupTargetType::
    GroupTargetID (const GroupTargetID_type& x)
    {
      this->GroupTargetID_.set (x);
    }

    void GroupTargetType::
    GroupTargetID (::std::auto_ptr< GroupTargetID_type > x)
    {
      this->GroupTargetID_.set (x);
    }

    const GroupTargetType::AuthzTarget_sequence& GroupTargetType::
    AuthzTarget () const
    {
      return this->AuthzTarget_;
    }

    GroupTargetType::AuthzTarget_sequence& GroupTargetType::
    AuthzTarget ()
    {
      return this->AuthzTarget_;
    }

    void GroupTargetType::
    AuthzTarget (const AuthzTarget_sequence& s)
    {
      this->AuthzTarget_ = s;
    }
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
  namespace lxacmlGroupTargetSchema
  {
    // GroupTargetType
    //

    GroupTargetType::
    GroupTargetType ()
    : ::xml_schema::type (),
      GroupTargetID_ (::xml_schema::flags (), this),
      AuthzTarget_ (::xml_schema::flags (), this)
    {
    }

    GroupTargetType::
    GroupTargetType (const GroupTargetID_type& GroupTargetID)
    : ::xml_schema::type (),
      GroupTargetID_ (GroupTargetID, ::xml_schema::flags (), this),
      AuthzTarget_ (::xml_schema::flags (), this)
    {
    }

    GroupTargetType::
    GroupTargetType (::std::auto_ptr< GroupTargetID_type >& GroupTargetID)
    : ::xml_schema::type (),
      GroupTargetID_ (GroupTargetID, ::xml_schema::flags (), this),
      AuthzTarget_ (::xml_schema::flags (), this)
    {
    }

    GroupTargetType::
    GroupTargetType (const GroupTargetType& x,
                     ::xml_schema::flags f,
                     ::xml_schema::container* c)
    : ::xml_schema::type (x, f, c),
      GroupTargetID_ (x.GroupTargetID_, f, this),
      AuthzTarget_ (x.AuthzTarget_, f, this)
    {
    }

    GroupTargetType::
    GroupTargetType (const ::xercesc::DOMElement& e,
                     ::xml_schema::flags f,
                     ::xml_schema::container* c)
    : ::xml_schema::type (e, f | ::xml_schema::flags::base, c),
      GroupTargetID_ (f, this),
      AuthzTarget_ (f, this)
    {
      if ((f & ::xml_schema::flags::base) == 0)
      {
        ::xsd::cxx::xml::dom::parser< wchar_t > p (e, true, false);
        this->parse (p, f);
      }
    }

    void GroupTargetType::
    parse (::xsd::cxx::xml::dom::parser< wchar_t >& p,
           ::xml_schema::flags f)
    {
      for (; p.more_elements (); p.next_element ())
      {
        const ::xercesc::DOMElement& i (p.cur_element ());
        const ::xsd::cxx::xml::qualified_name< wchar_t > n (
          ::xsd::cxx::xml::dom::name< wchar_t > (i));

        // GroupTargetID
        //
        {
          ::xsd::cxx::tree::type_factory_map< wchar_t >& tfm (
            ::xsd::cxx::tree::type_factory_map_instance< 0, wchar_t > ());

          ::std::auto_ptr< ::xsd::cxx::tree::type > tmp (
            tfm.create (
              L"GroupTargetID",
              L"http://www.qut.com/middleware/lxacmlGroupTargetSchema",
              &::xsd::cxx::tree::factory_impl< GroupTargetID_type >,
              false, true, i, n, f, this));

          if (tmp.get () != 0)
          {
            if (!GroupTargetID_.present ())
            {
              ::std::auto_ptr< GroupTargetID_type > r (
                dynamic_cast< GroupTargetID_type* > (tmp.get ()));

              if (r.get ())
                tmp.release ();
              else
                throw ::xsd::cxx::tree::not_derived< wchar_t > ();

              this->GroupTargetID_.set (r);
              continue;
            }
          }
        }

        // AuthzTarget
        //
        {
          ::xsd::cxx::tree::type_factory_map< wchar_t >& tfm (
            ::xsd::cxx::tree::type_factory_map_instance< 0, wchar_t > ());

          ::std::auto_ptr< ::xsd::cxx::tree::type > tmp (
            tfm.create (
              L"AuthzTarget",
              L"http://www.qut.com/middleware/lxacmlGroupTargetSchema",
              &::xsd::cxx::tree::factory_impl< AuthzTarget_type >,
              false, true, i, n, f, this));

          if (tmp.get () != 0)
          {
            ::std::auto_ptr< AuthzTarget_type > r (
              dynamic_cast< AuthzTarget_type* > (tmp.get ()));

            if (r.get ())
              tmp.release ();
            else
              throw ::xsd::cxx::tree::not_derived< wchar_t > ();

            this->AuthzTarget_.push_back (r);
            continue;
          }
        }

        break;
      }

      if (!GroupTargetID_.present ())
      {
        throw ::xsd::cxx::tree::expected_element< wchar_t > (
          L"GroupTargetID",
          L"http://www.qut.com/middleware/lxacmlGroupTargetSchema");
      }
    }

    GroupTargetType* GroupTargetType::
    _clone (::xml_schema::flags f,
            ::xml_schema::container* c) const
    {
      return new class GroupTargetType (*this, f, c);
    }

    GroupTargetType::
    ~GroupTargetType ()
    {
    }

    static
    const ::xsd::cxx::tree::type_factory_initializer< 0, wchar_t, GroupTargetType >
    _xsd_GroupTargetType_type_factory_init (
      L"GroupTargetType",
      L"http://www.qut.com/middleware/lxacmlGroupTargetSchema");
  }
}

#include <istream>
#include <xsd/cxx/xml/sax/std-input-source.hxx>
#include <xsd/cxx/tree/error-handler.hxx>

namespace middleware
{
  namespace lxacmlGroupTargetSchema
  {
    ::std::auto_ptr< ::middleware::lxacmlGroupTargetSchema::GroupTargetType >
    GroupTarget (const ::std::wstring& u,
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

      ::std::auto_ptr< ::middleware::lxacmlGroupTargetSchema::GroupTargetType > r (
        ::middleware::lxacmlGroupTargetSchema::GroupTarget (
          d, f | ::xml_schema::flags::own_dom, p));

      return r;
    }

    ::std::auto_ptr< ::middleware::lxacmlGroupTargetSchema::GroupTargetType >
    GroupTarget (const ::std::wstring& u,
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

      ::std::auto_ptr< ::middleware::lxacmlGroupTargetSchema::GroupTargetType > r (
        ::middleware::lxacmlGroupTargetSchema::GroupTarget (
          d, f | ::xml_schema::flags::own_dom, p));

      return r;
    }

    ::std::auto_ptr< ::middleware::lxacmlGroupTargetSchema::GroupTargetType >
    GroupTarget (const ::std::wstring& u,
                 ::xercesc::DOMErrorHandler& h,
                 ::xml_schema::flags f,
                 const ::xml_schema::properties& p)
    {
      ::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument > d (
        ::xsd::cxx::xml::dom::parse< wchar_t > (u, h, p, f));

      if (!d.get ())
        throw ::xsd::cxx::tree::parsing< wchar_t > ();

      ::std::auto_ptr< ::middleware::lxacmlGroupTargetSchema::GroupTargetType > r (
        ::middleware::lxacmlGroupTargetSchema::GroupTarget (
          d, f | ::xml_schema::flags::own_dom, p));

      return r;
    }

    ::std::auto_ptr< ::middleware::lxacmlGroupTargetSchema::GroupTargetType >
    GroupTarget (::std::istream& is,
                 ::xml_schema::flags f,
                 const ::xml_schema::properties& p)
    {
      ::xsd::cxx::xml::auto_initializer i (
        (f & ::xml_schema::flags::dont_initialize) == 0,
        (f & ::xml_schema::flags::keep_dom) == 0);

      ::xsd::cxx::xml::sax::std_input_source isrc (is);
      return ::middleware::lxacmlGroupTargetSchema::GroupTarget (isrc, f, p);
    }

    ::std::auto_ptr< ::middleware::lxacmlGroupTargetSchema::GroupTargetType >
    GroupTarget (::std::istream& is,
                 ::xml_schema::error_handler& h,
                 ::xml_schema::flags f,
                 const ::xml_schema::properties& p)
    {
      ::xsd::cxx::xml::auto_initializer i (
        (f & ::xml_schema::flags::dont_initialize) == 0,
        (f & ::xml_schema::flags::keep_dom) == 0);

      ::xsd::cxx::xml::sax::std_input_source isrc (is);
      return ::middleware::lxacmlGroupTargetSchema::GroupTarget (isrc, h, f, p);
    }

    ::std::auto_ptr< ::middleware::lxacmlGroupTargetSchema::GroupTargetType >
    GroupTarget (::std::istream& is,
                 ::xercesc::DOMErrorHandler& h,
                 ::xml_schema::flags f,
                 const ::xml_schema::properties& p)
    {
      ::xsd::cxx::xml::sax::std_input_source isrc (is);
      return ::middleware::lxacmlGroupTargetSchema::GroupTarget (isrc, h, f, p);
    }

    ::std::auto_ptr< ::middleware::lxacmlGroupTargetSchema::GroupTargetType >
    GroupTarget (::std::istream& is,
                 const ::std::wstring& sid,
                 ::xml_schema::flags f,
                 const ::xml_schema::properties& p)
    {
      ::xsd::cxx::xml::auto_initializer i (
        (f & ::xml_schema::flags::dont_initialize) == 0,
        (f & ::xml_schema::flags::keep_dom) == 0);

      ::xsd::cxx::xml::sax::std_input_source isrc (is, sid);
      return ::middleware::lxacmlGroupTargetSchema::GroupTarget (isrc, f, p);
    }

    ::std::auto_ptr< ::middleware::lxacmlGroupTargetSchema::GroupTargetType >
    GroupTarget (::std::istream& is,
                 const ::std::wstring& sid,
                 ::xml_schema::error_handler& h,
                 ::xml_schema::flags f,
                 const ::xml_schema::properties& p)
    {
      ::xsd::cxx::xml::auto_initializer i (
        (f & ::xml_schema::flags::dont_initialize) == 0,
        (f & ::xml_schema::flags::keep_dom) == 0);

      ::xsd::cxx::xml::sax::std_input_source isrc (is, sid);
      return ::middleware::lxacmlGroupTargetSchema::GroupTarget (isrc, h, f, p);
    }

    ::std::auto_ptr< ::middleware::lxacmlGroupTargetSchema::GroupTargetType >
    GroupTarget (::std::istream& is,
                 const ::std::wstring& sid,
                 ::xercesc::DOMErrorHandler& h,
                 ::xml_schema::flags f,
                 const ::xml_schema::properties& p)
    {
      ::xsd::cxx::xml::sax::std_input_source isrc (is, sid);
      return ::middleware::lxacmlGroupTargetSchema::GroupTarget (isrc, h, f, p);
    }

    ::std::auto_ptr< ::middleware::lxacmlGroupTargetSchema::GroupTargetType >
    GroupTarget (::xercesc::InputSource& i,
                 ::xml_schema::flags f,
                 const ::xml_schema::properties& p)
    {
      ::xsd::cxx::tree::error_handler< wchar_t > h;

      ::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument > d (
        ::xsd::cxx::xml::dom::parse< wchar_t > (i, h, p, f));

      h.throw_if_failed< ::xsd::cxx::tree::parsing< wchar_t > > ();

      ::std::auto_ptr< ::middleware::lxacmlGroupTargetSchema::GroupTargetType > r (
        ::middleware::lxacmlGroupTargetSchema::GroupTarget (
          d, f | ::xml_schema::flags::own_dom, p));

      return r;
    }

    ::std::auto_ptr< ::middleware::lxacmlGroupTargetSchema::GroupTargetType >
    GroupTarget (::xercesc::InputSource& i,
                 ::xml_schema::error_handler& h,
                 ::xml_schema::flags f,
                 const ::xml_schema::properties& p)
    {
      ::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument > d (
        ::xsd::cxx::xml::dom::parse< wchar_t > (i, h, p, f));

      if (!d.get ())
        throw ::xsd::cxx::tree::parsing< wchar_t > ();

      ::std::auto_ptr< ::middleware::lxacmlGroupTargetSchema::GroupTargetType > r (
        ::middleware::lxacmlGroupTargetSchema::GroupTarget (
          d, f | ::xml_schema::flags::own_dom, p));

      return r;
    }

    ::std::auto_ptr< ::middleware::lxacmlGroupTargetSchema::GroupTargetType >
    GroupTarget (::xercesc::InputSource& i,
                 ::xercesc::DOMErrorHandler& h,
                 ::xml_schema::flags f,
                 const ::xml_schema::properties& p)
    {
      ::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument > d (
        ::xsd::cxx::xml::dom::parse< wchar_t > (i, h, p, f));

      if (!d.get ())
        throw ::xsd::cxx::tree::parsing< wchar_t > ();

      ::std::auto_ptr< ::middleware::lxacmlGroupTargetSchema::GroupTargetType > r (
        ::middleware::lxacmlGroupTargetSchema::GroupTarget (
          d, f | ::xml_schema::flags::own_dom, p));

      return r;
    }

    ::std::auto_ptr< ::middleware::lxacmlGroupTargetSchema::GroupTargetType >
    GroupTarget (const ::xercesc::DOMDocument& d,
                 ::xml_schema::flags f,
                 const ::xml_schema::properties& p)
    {
      if (f & ::xml_schema::flags::keep_dom)
      {
        ::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument > c (
          static_cast< ::xercesc::DOMDocument* > (d.cloneNode (true)));

        ::std::auto_ptr< ::middleware::lxacmlGroupTargetSchema::GroupTargetType > r (
          ::middleware::lxacmlGroupTargetSchema::GroupTarget (
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
          L"GroupTarget",
          L"http://www.qut.com/middleware/lxacmlGroupTargetSchema",
          &::xsd::cxx::tree::factory_impl< ::middleware::lxacmlGroupTargetSchema::GroupTargetType >,
          true, true, e, n, f, 0));

      if (tmp.get () != 0)
      {
        ::std::auto_ptr< ::middleware::lxacmlGroupTargetSchema::GroupTargetType > r (
          dynamic_cast< ::middleware::lxacmlGroupTargetSchema::GroupTargetType* > (tmp.get ()));

        if (r.get ())
          tmp.release ();
        else
          throw ::xsd::cxx::tree::not_derived< wchar_t > ();

        return r;
      }

      throw ::xsd::cxx::tree::unexpected_element < wchar_t > (
        n.name (),
        n.namespace_ (),
        L"GroupTarget",
        L"http://www.qut.com/middleware/lxacmlGroupTargetSchema");
    }

    ::std::auto_ptr< ::middleware::lxacmlGroupTargetSchema::GroupTargetType >
    GroupTarget (::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument >& d,
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
          L"GroupTarget",
          L"http://www.qut.com/middleware/lxacmlGroupTargetSchema",
          &::xsd::cxx::tree::factory_impl< ::middleware::lxacmlGroupTargetSchema::GroupTargetType >,
          true, true, e, n, f, 0));

      if (tmp.get () != 0)
      {

        ::std::auto_ptr< ::middleware::lxacmlGroupTargetSchema::GroupTargetType > r (
          dynamic_cast< ::middleware::lxacmlGroupTargetSchema::GroupTargetType* > (tmp.get ()));

        if (r.get ())
          tmp.release ();
        else
          throw ::xsd::cxx::tree::not_derived< wchar_t > ();

        return r;
      }

      throw ::xsd::cxx::tree::unexpected_element < wchar_t > (
        n.name (),
        n.namespace_ (),
        L"GroupTarget",
        L"http://www.qut.com/middleware/lxacmlGroupTargetSchema");
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
  namespace lxacmlGroupTargetSchema
  {
    void
    GroupTarget (::std::ostream& o,
                 const ::middleware::lxacmlGroupTargetSchema::GroupTargetType& s,
                 const ::xml_schema::namespace_infomap& m,
                 const ::std::wstring& e,
                 ::xml_schema::flags f)
    {
      ::xsd::cxx::xml::auto_initializer i (
        (f & ::xml_schema::flags::dont_initialize) == 0);

      ::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument > d (
        ::middleware::lxacmlGroupTargetSchema::GroupTarget (s, m, f));

      ::xsd::cxx::tree::error_handler< wchar_t > h;

      ::xsd::cxx::xml::dom::ostream_format_target t (o);
      if (!::xsd::cxx::xml::dom::serialize (t, *d, e, h, f))
      {
        h.throw_if_failed< ::xsd::cxx::tree::serialization< wchar_t > > ();
      }
    }

    void
    GroupTarget (::std::ostream& o,
                 const ::middleware::lxacmlGroupTargetSchema::GroupTargetType& s,
                 ::xml_schema::error_handler& h,
                 const ::xml_schema::namespace_infomap& m,
                 const ::std::wstring& e,
                 ::xml_schema::flags f)
    {
      ::xsd::cxx::xml::auto_initializer i (
        (f & ::xml_schema::flags::dont_initialize) == 0);

      ::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument > d (
        ::middleware::lxacmlGroupTargetSchema::GroupTarget (s, m, f));
      ::xsd::cxx::xml::dom::ostream_format_target t (o);
      if (!::xsd::cxx::xml::dom::serialize (t, *d, e, h, f))
      {
        throw ::xsd::cxx::tree::serialization< wchar_t > ();
      }
    }

    void
    GroupTarget (::std::ostream& o,
                 const ::middleware::lxacmlGroupTargetSchema::GroupTargetType& s,
                 ::xercesc::DOMErrorHandler& h,
                 const ::xml_schema::namespace_infomap& m,
                 const ::std::wstring& e,
                 ::xml_schema::flags f)
    {
      ::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument > d (
        ::middleware::lxacmlGroupTargetSchema::GroupTarget (s, m, f));
      ::xsd::cxx::xml::dom::ostream_format_target t (o);
      if (!::xsd::cxx::xml::dom::serialize (t, *d, e, h, f))
      {
        throw ::xsd::cxx::tree::serialization< wchar_t > ();
      }
    }

    void
    GroupTarget (::xercesc::XMLFormatTarget& t,
                 const ::middleware::lxacmlGroupTargetSchema::GroupTargetType& s,
                 const ::xml_schema::namespace_infomap& m,
                 const ::std::wstring& e,
                 ::xml_schema::flags f)
    {
      ::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument > d (
        ::middleware::lxacmlGroupTargetSchema::GroupTarget (s, m, f));

      ::xsd::cxx::tree::error_handler< wchar_t > h;

      if (!::xsd::cxx::xml::dom::serialize (t, *d, e, h, f))
      {
        h.throw_if_failed< ::xsd::cxx::tree::serialization< wchar_t > > ();
      }
    }

    void
    GroupTarget (::xercesc::XMLFormatTarget& t,
                 const ::middleware::lxacmlGroupTargetSchema::GroupTargetType& s,
                 ::xml_schema::error_handler& h,
                 const ::xml_schema::namespace_infomap& m,
                 const ::std::wstring& e,
                 ::xml_schema::flags f)
    {
      ::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument > d (
        ::middleware::lxacmlGroupTargetSchema::GroupTarget (s, m, f));
      if (!::xsd::cxx::xml::dom::serialize (t, *d, e, h, f))
      {
        throw ::xsd::cxx::tree::serialization< wchar_t > ();
      }
    }

    void
    GroupTarget (::xercesc::XMLFormatTarget& t,
                 const ::middleware::lxacmlGroupTargetSchema::GroupTargetType& s,
                 ::xercesc::DOMErrorHandler& h,
                 const ::xml_schema::namespace_infomap& m,
                 const ::std::wstring& e,
                 ::xml_schema::flags f)
    {
      ::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument > d (
        ::middleware::lxacmlGroupTargetSchema::GroupTarget (s, m, f));
      if (!::xsd::cxx::xml::dom::serialize (t, *d, e, h, f))
      {
        throw ::xsd::cxx::tree::serialization< wchar_t > ();
      }
    }

    void
    GroupTarget (::xercesc::DOMDocument& d,
                 const ::middleware::lxacmlGroupTargetSchema::GroupTargetType& s,
                 ::xml_schema::flags)
    {
      ::xercesc::DOMElement& e (*d.getDocumentElement ());
      const ::xsd::cxx::xml::qualified_name< wchar_t > n (
        ::xsd::cxx::xml::dom::name< wchar_t > (e));

      if (typeid (::middleware::lxacmlGroupTargetSchema::GroupTargetType) == typeid (s))
      {
        if (n.name () == L"GroupTarget" &&
            n.namespace_ () == L"http://www.qut.com/middleware/lxacmlGroupTargetSchema")
        {
          e << s;
        }
        else
        {
          throw ::xsd::cxx::tree::unexpected_element < wchar_t > (
            n.name (),
            n.namespace_ (),
            L"GroupTarget",
            L"http://www.qut.com/middleware/lxacmlGroupTargetSchema");
        }
      }
      else
      {
        ::xsd::cxx::tree::type_serializer_map< wchar_t >& tsm (
          ::xsd::cxx::tree::type_serializer_map_instance< 0, wchar_t > ());

        tsm.serialize (
          L"GroupTarget",
          L"http://www.qut.com/middleware/lxacmlGroupTargetSchema",
          e, n, s);
      }
    }

    ::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument >
    GroupTarget (const ::middleware::lxacmlGroupTargetSchema::GroupTargetType& s,
                 const ::xml_schema::namespace_infomap& m,
                 ::xml_schema::flags f)
    {
      ::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument > d;

      if (typeid (::middleware::lxacmlGroupTargetSchema::GroupTargetType) == typeid (s))
      {
        ::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument > r (
          ::xsd::cxx::xml::dom::serialize< wchar_t > (
            L"GroupTarget",
            L"http://www.qut.com/middleware/lxacmlGroupTargetSchema",
            m, f));
        d = r;
      }
      else
      {
        ::xsd::cxx::tree::type_serializer_map< wchar_t >& tsm (
          ::xsd::cxx::tree::type_serializer_map_instance< 0, wchar_t > ());

        ::xml_schema::dom::auto_ptr< ::xercesc::DOMDocument > r (
          tsm.serialize (
            L"GroupTarget",
            L"http://www.qut.com/middleware/lxacmlGroupTargetSchema",
            m, s, f));
        d = r;
      }

      ::middleware::lxacmlGroupTargetSchema::GroupTarget (*d, s, f);
      return d;
    }

    void
    operator<< (::xercesc::DOMElement& e, const GroupTargetType& i)
    {
      e << static_cast< const ::xml_schema::type& > (i);

      // GroupTargetID
      //
      {
        ::xsd::cxx::tree::type_serializer_map< wchar_t >& tsm (
          ::xsd::cxx::tree::type_serializer_map_instance< 0, wchar_t > ());

        const GroupTargetType::GroupTargetID_type& x (i.GroupTargetID ());
        if (typeid (GroupTargetType::GroupTargetID_type) == typeid (x))
        {
          ::xercesc::DOMElement& s (
            ::xsd::cxx::xml::dom::create_element (
              L"GroupTargetID",
              L"http://www.qut.com/middleware/lxacmlGroupTargetSchema",
              e));

          s << x;
        }
        else
          tsm.serialize (
            L"GroupTargetID",
            L"http://www.qut.com/middleware/lxacmlGroupTargetSchema",
            false, true, e, x);
      }

      // AuthzTarget
      //
      {
        ::xsd::cxx::tree::type_serializer_map< wchar_t >& tsm (
          ::xsd::cxx::tree::type_serializer_map_instance< 0, wchar_t > ());

        for (GroupTargetType::AuthzTarget_const_iterator
             b (i.AuthzTarget ().begin ()), n (i.AuthzTarget ().end ());
             b != n; ++b)
        {
          if (typeid (GroupTargetType::AuthzTarget_type) == typeid (*b))
          {
            ::xercesc::DOMElement& s (
              ::xsd::cxx::xml::dom::create_element (
                L"AuthzTarget",
                L"http://www.qut.com/middleware/lxacmlGroupTargetSchema",
                e));

            s << *b;
          }
          else
            tsm.serialize (
              L"AuthzTarget",
              L"http://www.qut.com/middleware/lxacmlGroupTargetSchema",
              false, true, e, *b);
        }
      }
    }

    static
    const ::xsd::cxx::tree::type_serializer_initializer< 0, wchar_t, GroupTargetType >
    _xsd_GroupTargetType_type_serializer_init (
      L"GroupTargetType",
      L"http://www.qut.com/middleware/lxacmlGroupTargetSchema");
  }
}

#include <xsd/cxx/post.hxx>

// Begin epilogue.
//
//
// End epilogue.


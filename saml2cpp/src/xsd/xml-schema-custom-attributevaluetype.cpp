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
 * Author: Bradley Beddoes
 * Creation Date: 13/03/2007
 * 
 * Purpose: This is a special type to define a serialization operations for LXACML AttributeValueType when the type data contains String,
 * due to the special needs XSD has for creating and supporting elements which are defined in schema to be ##any
 */
 
/* STL */
#include <string>

/* Xerces */
#include <xercesc/dom/DOMElement.hpp>

/* Local codebase */
#include "xsd/xml-schema.hxx"
#include "lxacml-schema-context.hxx"
#include "xsd/xml-schema-custom-attributevaluetype.h"

XERCES_CPP_NAMESPACE_USE

namespace middleware
{
  namespace lxacmlContextSchema
  {
  	
  	/* Create default inplementation */
    AttributeValueType::AttributeValueType ()
    : ::xml_schema::type ()
    {
    }
	
	/* Copy constructor used by _clone */
    AttributeValueType::AttributeValueType (const AttributeValueType& _xsd_AttributeValueType,
                        ::xml_schema::flags f,
                        ::xml_schema::type* c)
    : ::xml_schema::type (_xsd_AttributeValueType, f, c)
    {
    	this->value = _xsd_AttributeValueType.Value();
    }

    AttributeValueType::AttributeValueType (const DOMElement& e,
                        ::xml_schema::flags f,
                        ::xml_schema::type* c)
    : ::xml_schema::type (e, f, c)
    {
    	
    }

    AttributeValueType::
    AttributeValueType (const ::std::basic_string< wchar_t >& s,
                        const DOMElement* e,
                        ::xml_schema::flags f,
                        ::xml_schema::type* c)
    : ::xml_schema::type (s, e, f, c)
    {
    }

    AttributeValueType* AttributeValueType::
    _clone (::xml_schema::flags f,
            ::xml_schema::type* c) const
    {
      return new AttributeValueType (*this, f, c);
    }
    
    void AttributeValueType::Value(std::wstring value)
  	{
  		this->value = value;
  	}
  	
  	const std::wstring AttributeValueType::Value() const
  	{
  		return this->value;
  	}

    void operator<< (DOMElement& e, const AttributeValueType& x)
    {
  		e.setTextContent( xsd::cxx::xml::string(x.Value()).c_str() ); 	  
    }
  }
}

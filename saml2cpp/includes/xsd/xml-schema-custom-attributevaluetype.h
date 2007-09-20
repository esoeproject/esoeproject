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
 * Purpose: This is a special type to define a serialization operations for LXACML AttributeValueType due to the special needs XSD has for creating and supporting elements which are defined in schema to be ##any.
 * Presently this supports only marshalling String to an element, though in the future may be extended to support other requirements in the C++ library to support LXACML extenstions.
 */
 
#ifndef ATTRIBUTETYPESTRING_H_
#define ATTRIBUTETYPESTRING_H_

/* STL */
#include <string>

/* Xerces */
#include <xercesc/dom/DOMElement.hpp>

/* XSD */
#include <xsd/cxx/tree/elements.hxx>

/* SAML2lib-cpp */
#include "xsd/xml-schema.hxx"

XERCES_CPP_NAMESPACE_USE

namespace middleware
{
  namespace lxacmlContextSchema
  {
  	/*
	class AttributeValueType : public ::xml_schema::type
	{
		public:
			
			 * Default Constructor
			 *
			AttributeValueType(::xml_schema::flags = 0, ::xml_schema::type* = 0);
			
			
			 * XSD Constructor
			 *
		  	AttributeValueType (const xercesc::DOMElement&, ::xml_schema::flags = 0, ::xml_schema::type* = 0);
		  	
		  	/
			 * XSD Constructor
			 *
		    AttributeValueType (const AttributeValueType&, ::xml_schema::flags = 0, ::xml_schema::type* = 0);
		    
		
			
			 * Local extenstions to hold string type
			 *
			void Value(std::wstring value);
			const std::wstring Value() const;
			
			
			 * XSD Cloning operation
			 *
		    AttributeValueType* _clone (::xml_schema::flags = 0, ::xml_schema::type* = 0) const;
			
		private:
			std::wstring value;
	};
	*/
	
	class AttributeValueType: public ::xml_schema::type
    {
      public:

      struct _xsd_AttributeValueType
      {
        typedef ::xml_schema::type base_;
      };

      // Constructors.
      //
      public:
	      AttributeValueType ();
	
	      AttributeValueType (const DOMElement&,
	                          ::xml_schema::flags = 0,
	                          ::xml_schema::type* = 0);

	      AttributeValueType (const ::std::basic_string< wchar_t >&,
	                          const DOMElement*,
	                          ::xml_schema::flags = 0,
	                          ::xml_schema::type* = 0);
	
	      AttributeValueType (const AttributeValueType&,
	                          ::xml_schema::flags = 0,
	                          ::xml_schema::type* = 0);
	
	      virtual AttributeValueType*
	      _clone (::xml_schema::flags = 0,
	              ::xml_schema::type* = 0) const;
	              
	     void Value(std::wstring value);
		 const std::wstring Value() const;
	 
	 private:
	 	std::wstring value;
    };
    
    void operator<< (DOMElement&, const AttributeValueType&);
	
	//void operator<< (DOMElement& e, const AttributeValueType& x);
  }
}

#endif /*ATTRIBUTETYPESTRING_H_*/

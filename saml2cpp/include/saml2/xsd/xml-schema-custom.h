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
 * Creation Date: 01/02/2007
 * 
 * Purpose: XSD customisation to use the Boost gregorian date class
 */
 
/* Boost */
#include <boost/date_time/string_convert.hpp>
#include <boost/date_time/gregorian/gregorian.hpp>
#include <boost/date_time/posix_time/ptime.hpp>
#include <boost/date_time/posix_time/time_parsers.hpp>

/* for std::wstring workaround */
#include "saml2/SAML2Defs.h"


namespace xml_schema
{
	// Workaround for newer version of XSD not specifying namespace_infomap where we expected.
#if (XSD_INT_VERSION >= 3000000L)
	typedef xsd::cxx::xml::dom::namespace_infomap<wchar_t> namespace_infomap;
#endif
	
	class SAML2EXPORT date: public simple_type,
	            public boost::gregorian::date
	{
	 public:
			date ();
		    date (const xercesc::DOMElement&, flags = 0, type* = 0);
		    date (const xercesc::DOMAttr&, flags = 0, type* = 0);
		    date (const date&, flags = 0, type* = 0);
	
	    virtual date*
	    _clone (flags = 0, type* = 0) const;
	};
  
	class SAML2EXPORT date_time: public simple_type, 
					 public boost::posix_time::ptime
	{
	 	public:
	 		/*
	 		 * Constructor
	 		 * 
	 		 * Creates date_time implementation set to UTC
	 		 */
			date_time ();
			
			/*
			 * Constructor
			 * 
			 * Creates date_time implementation from existing ptime
			 */
			date_time (boost::posix_time::ptime time);
			
			/* 
			 * XSD Constructor
			 */
		  	date_time (const xercesc::DOMElement&, flags = 0, type* = 0);
		  	
		  	/* 
			 * XSD Constructor
			 */
		    date_time (const date_time&, flags = 0, type* = 0);
		    
		    /* 
			 * XSD Constructor
			 */
			date_time (const xercesc::DOMAttr&, flags = 0, type* = 0);
			
			/* 
			 * XSD Cloning operation
			 */		
		    virtual date_time*
		    _clone (flags = 0, type* = 0) const;
	};
  
  	/* 
  	 * Overloaded ostream operator to correctly output date
  	 * 
  	 * @param os The ostream to serialize to
  	 * @param d the boost date object to serialize
  	 */
	SAML2EXPORT std::ostream& operator<< (std::ostream& os, const date& d);
	
	/* 
	 * Overloaded ostream operator to correctly output pTime
	 *
	 * @param os The ostream to serialize to
  	 * @param dt the boost ptime object to serialize
  	 */
	SAML2EXPORT std::ostream& operator<< (std::ostream& os, const date_time& dt);
	
	/* 
	 * Overloaded serialization operator to correctly push date to DOMAttribute 
	 * 
	 * @param a the DOMAttr object to serialize to
	 * @param d the date object to serialize
	 */
	SAML2EXPORT void operator<< (xercesc::DOMAttr& a, const date& d);
	
	/* 
	 * Overloaded serialization operator to correctly push pTime to DOMAttribute 
	 * @param a the DOMAttr object to serialize to
	 * @param dt the ptime object to serialize
	 */
	SAML2EXPORT void operator<< (xercesc::DOMAttr& a, const date_time& dt);
  
	/* Creates a boost date object from XML date attribute
	 * 
	 * @param date date stored in XML element for conversion
	 * 
	 * @return date representation of XML time
	 */ 
	SAML2EXPORT boost::gregorian::date createDate(std::wstring);
	
	/* Converts string from UTC XML dateTime format of YYYY-MM-DDTHH:MM:SS.mmmZ
	 * to boost pTime format of YYYY-MM-DD HH:MM:SS.mmm
	 * 
	 * @param dateTime Time stored in XML dateTime element for converstion
	 * 
	 * @return ptime representation of XML time
	 * 
	 * @exception InvalidParameterException if presented XML time is invalid
	 */
	SAML2EXPORT boost::posix_time::ptime createPTime(std::wstring dateTime);
}

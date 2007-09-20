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
 * Creation Date: 10/02/2007
 * 
 * Purpose: Implementation of XSD customisation to use the Boost gregorian date class
 */

/* Boost */
#include <boost/regex.hpp>
#include <boost/date_time/posix_time/time_formatters.hpp>

/* XSD */
#include <xsd/cxx/xml/string.hxx>
#include <xsd/cxx/tree/serialization.hxx>

/* Xerces */
#include <xercesc/util/PlatformUtils.hpp>
#include <xercesc/util/XMLString.hpp>

/* Local codebase */
#include "exceptions/InvalidParameterException.h"
#include "xsd/xml-schema.hxx"
#include "SAML2Defs.h"

namespace xml = xsd::cxx::xml;

XERCES_CPP_NAMESPACE_USE

namespace xml_schema
{
  date:: date (): boost::gregorian::date ( boost::gregorian::day_clock::universal_day() )
  {
  }
  
  date::date (const DOMElement& e, flags f, type* container)
      : simple_type (e, f, container),
        boost::gregorian::date (xml_schema::createDate ( xml::transcode<wchar_t> ( e.getTextContent () ) ) )
  {
  }

  date::date (const DOMAttr& a, flags f, type* container)
      : simple_type (a, f, container),
        boost::gregorian::date (xml_schema::createDate ( xml::transcode<wchar_t> ( a.getValue () ) ) )
  {
  }

  date::date (const date& d, flags f, type* container)
      : simple_type (d, f, container),
        boost::gregorian::date (d)
  {
  }

  date* date::_clone (flags f, type* container) const
  {
    return new date (*this, f, container);
  }
  
 
  date_time::date_time ()
      : boost::posix_time::ptime ( boost::posix_time::microsec_clock::universal_time() )
  {
  }
  
	date_time::date_time (boost::posix_time::ptime time)
	  : boost::posix_time::ptime ( time )
  {
  }
  
  date_time::date_time (const DOMElement& e, flags f, type* container)
      : simple_type (e, f, container),
        boost::posix_time::ptime (xml_schema::createPTime ( xml::transcode<wchar_t> ( e.getTextContent () ) ) )
  {
  }

  date_time::date_time (const DOMAttr& a, flags f, type* container)
      : simple_type (a, f, container),
        boost::posix_time::ptime (xml_schema::createPTime ( xml::transcode<wchar_t> ( a.getValue () ) ) )
  {
  }

  date_time::date_time (const date_time& d, flags f, type* container)
      : simple_type (d, f, container),
        boost::posix_time::ptime (d)
  {
  }

  date_time* date_time::_clone (flags f, type* container) const
  {
    return new date_time (*this, f, container);
  }
   
  	boost::gregorian::date createDate(std::wstring date)
	{	
		boost::gregorian::date xmlDate ( boost::gregorian::from_string( boost::date_time::convert_string_type<wchar_t, char> (date) ) );
		return xmlDate;
	}
	
	boost::posix_time::ptime createPTime(std::wstring dateTime)
	{
		boost::regex expression(XML_DATE_TIME_TO_BOOST_REGEX);
		boost::cmatch matcher; 
		std::string xmlDate;
		std::string pDate;
		
		/* We can be conifdent using this conversion here as the dateTime field should never contain non ascii characters */
		xmlDate = boost::date_time::convert_string_type<wchar_t, char> (dateTime);
		
   		if(boost::regex_match(xmlDate.c_str(), matcher, expression))
   		{
   			pDate = matcher[1] + " " + matcher[2];
   		}
   		else
   		{
   					SAML2LIB_INVPARAM_EX("Value determined from xml document was unable to be parsed to boost ptime format for time_from_string");
   		}

		boost::posix_time::ptime xmlDateTime( boost::posix_time::time_from_string( pDate ) );
		return xmlDateTime;
	}
	
	std::ostream& operator<< (std::ostream& os, const date& d)
	  {
	    /* Convert from boost date to extended iso format of YYYY-MM-DD
		 */
		os << std::endl << "date: " << boost::gregorian::to_iso_extended_string(d);
	    return os;
	  }
  
	std::ostream&  operator<< (std::ostream& os, const date_time& dt)
    {
		boost::regex expression(BOOST_TO_XML_DATE_TIME_REGEX);
		boost::cmatch matcher; 
		std::string convDate, date;
	
	    /* Convert from boost pTime extended iso format of YYYY-MM-DDTHH:MM:SS.mmm
		 * to UTC XML dateTime format of YYYY-MM-DDTHH:MM:SS.mmmZ
		 */
		convDate = boost::posix_time::to_iso_extended_string(dt);
		
		if(boost::regex_match(convDate.c_str(), matcher, expression))
		{
			date = matcher[1] + "T" + matcher[2] + "Z";
		}
		else
		{
			SAML2LIB_INVPARAM_EX("Value determined from boost pTime to_iso_extended_string implementation was invalid and unable to be parsed to XML time format");
		}
		os << std::endl << "dateTime: " << convDate;
	    return os;
    }  
	
	void operator<< (DOMAttr& a, const date& d)
	  {
	  		XMLCh* xmlDate;
			std::string date;
	
			/* Serialize base simple_type data to the attribute */
	  		const simple_type& b (d);
	  		a << b;
	  		
	  		/* Convert from boost pTime extended iso format of YYYY-MM-DDTHH:MM:SS.mmm
			 * to UTC XML dateTime format of YYYY-MM-DDTHH:MM:SS.mmmZ
			 */
			date = boost::gregorian::to_iso_extended_string(d);
	   		
	   		xmlDate = XMLString::transcode(date.c_str());
	  		a.setValue(xmlDate);
	  		XMLString::release(&xmlDate);
	  }
  
    void operator<< (DOMAttr& a, const date_time& dt)
	  {
	  		XMLCh* xmlDate;
	  		boost::regex expression(BOOST_TO_XML_DATE_TIME_REGEX);
			boost::cmatch matcher; 
			std::string convDate, date;
	
			/* Serialize base simple_type data to the attribute */
	  		const simple_type& b (dt);
	  		a << b;
	  		
	  		/* Convert from boost pTime extended iso format of YYYY-MM-DDTHH:MM:SS.mmm
			 * to UTC XML dateTime format of YYYY-MM-DDTHH:MM:SS.mmmZ
			 */
			convDate = boost::posix_time::to_iso_extended_string(dt);
			
	   		if(boost::regex_match(convDate.c_str(), matcher, expression))
	   		{
	   			date = matcher[1] + "T" + matcher[2] + "Z";
	   		}
	   		else
	   		{
	   			SAML2LIB_INVPARAM_EX("Value determined from boost pTime to_iso_extended_string implementation was invalid and unable to be parsed to XML time format");
	   		}
	   		
	   		xmlDate = XMLString::transcode(date.c_str());
	  		a.setValue(xmlDate);
	  		XMLString::release(&xmlDate);
	  }
}

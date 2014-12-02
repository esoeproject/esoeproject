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
 * Author: Shaun Mangelsdorf
 * Creation Date: Jul 30, 2007
 * 
 * Purpose: 
 */

#ifndef WSPROCESSORDATA_H_
#define WSPROCESSORDATA_H_

#include <string>

#include "saml2/handlers/SAMLDocument.h"
#include "spep/Util.h"
#include "spep/ws/SOAPUtil.h"

namespace spep
{
	
    class SPEPEXPORT WSProcessorData
    {
    public:
        WSProcessorData();

        const spep::SOAPDocument& getSOAPRequestDocument();
        void setSOAPRequestDocument(const spep::SOAPDocument& requestDocument);

        void setSAMLRequestDocument(const saml2::SAMLDocument& requestDocument);
        const saml2::SAMLDocument& getSAMLRequestDocument();

        const spep::SOAPDocument& getSOAPResponseDocument();
        void setSOAPResponseDocument(const spep::SOAPDocument& responseDocument);

        void setSAMLResponseDocument(const saml2::SAMLDocument& responseDocument);
        const saml2::SAMLDocument& getSAMLResponseDocument();

        std::string getCharacterEncoding() const;
        void setCharacterEncoding(std::string characterEncoding);

        SOAPUtil::SOAPVersion getSOAPVersion() const;
        void setSOAPVersion(SOAPUtil::SOAPVersion soapVersion);
	
    private:
        spep::SOAPDocument mRequestSOAPDocument;
        spep::SOAPDocument mResponseSOAPDocument;
        saml2::SAMLDocument mRequestSAMLDocument;
        saml2::SAMLDocument mResponseSAMLDocument;
        std::string mCharacterEncoding;
        SOAPUtil::SOAPVersion mSoapVersion;
	};
	
}

#endif /*WSPROCESSORDATA_H_*/

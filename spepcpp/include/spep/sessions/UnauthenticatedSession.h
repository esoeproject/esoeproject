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
 * Creation Date: 08/01/2007
 * 
 * Purpose: 
 */

#ifndef UNAUTHENTICATEDSESSION_H_
#define UNAUTHENTICATEDSESSION_H_

#include "spep/Util.h"
#include "spep/ipc/Serialization.h"
#include "unicode/utypes.h"
#include "unicode/unistr.h"
#include <iostream>

// For CygWin std::wstring workaround.
#include "saml2/SAML2Defs.h"

#include <boost/date_time/posix_time/posix_time.hpp>

namespace spep
{
	
    class SPEPEXPORT UnauthenticatedSession
    {
        friend class spep::ipc::access;

    public:

        UnauthenticatedSession();
        UnauthenticatedSession(const UnauthenticatedSession &rhs);

        UnauthenticatedSession &operator=(const UnauthenticatedSession &rhs);

        /**
         * Sets the AuthnRequest SAML identifier for this session
         */
        void setAuthnRequestSAMLID(const std::wstring& authnRequestSAMLID);
        std::wstring getAuthnRequestSAMLID() const;

        /**
         * Sets the original request URL for this session. This would be the URL
         * that caused the SPEP to request authentication.
         */
        void setRequestURL(const std::string& requestURL);
        std::string getRequestURL() const;

        /**
         * Updates the timestamp on this session to record that the session has not
         * been idle.
         */
        void updateTime();
        /**
         * Gets the amount of time since updateTime() was last called.
         */
        long getIdleTime() const;

    private:
        std::wstring mAuthnRequestSAMLID;
        std::string mRequestURL;
        boost::posix_time::ptime mTimestamp;

        template <class Archive>
        void serialize(Archive &ar, const unsigned int &version)
        {
            ar & mAuthnRequestSAMLID & mRequestURL & mTimestamp;
        }
    };

}

#endif /* UNAUTHENTICATEDSESSION_H_ */

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
 * Creation Date: 07/03/2007
 * 
 * Purpose: Represents an authorization decision made on behalf of the PDP.
 */

#ifndef DECISION_H_
#define DECISION_H_

#include "spep/Util.h"
#include "spep/ipc/Serialization.h"

#include <string>

/* This gets around a defined symbol on win32 platforms */
#ifdef ERROR
#undef ERROR
#endif /*ERROR*/

namespace spep
{
	
	/** Represents an authorization decision made on behalf of the PDP. */
    class SPEPEXPORT Decision
    {
        friend class spep::ipc::access;

    public:
        /** The possible Decision values. */
        enum Result
        {
            PERMIT, DENY, CACHE, ERROR, NONE
        } mResult;

        Decision();
        Decision(Result result);
        Decision(const Decision& rhs);
        Decision(const std::string& stringValue);

        /** Standard set of operators */
        /*@{*/
        bool operator==(const Decision &rhs);
        bool operator!=(const Decision &rhs);
        bool operator==(Result rhs);
        bool operator!=(Result rhs);
        const Decision &operator=(Result result);
        /*@}*/

        /**
         * Addition/assignment operator. This is similar in logic to an 'and' operation
         * except that it understands the specifics of LXACML Authz decisions.
         *
         * Adding two concurrent decisions will give the resulting decision.
         */
        const Decision &operator+=(const Decision &rhs);

    private:

        template <class Archive>
        void serialize(Archive &ar, const unsigned int version);
    };

    template <class Archive>
    inline void Decision::serialize(Archive &ar, const unsigned int version)
    {
        ar & (unsigned int&)mResult;
    }

}

#endif /*DECISION_H_*/

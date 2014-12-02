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
 * Purpose: 
 */

#include "spep/pep/Decision.h"


spep::Decision::Decision() :
mResult(NONE)
{}

spep::Decision::Decision(spep::Decision::Result result) :
mResult(result)
{}

spep::Decision::Decision(const spep::Decision& rhs) :
mResult(rhs.mResult)
{}

spep::Decision::Decision(const std::string& stringValue)
{
    const char *value = stringValue.c_str();
    mResult = DENY;
    if (stringValue.length() >= 4)
    {
        if (value[0] == 'D' || value[0] == 'd')
        {
            if ((value[1] == 'E' || value[1] == 'e')
                && (value[2] == 'N' || value[2] == 'n')
                && (value[3] == 'Y' || value[3] == 'y'))
            {
                mResult = DENY;
            }
        }

        if (stringValue.length() >= 6)
        {
            if (value[0] == 'P' || value[0] == 'p')
            {
                if ((value[1] == 'E' || value[1] == 'e')
                    && (value[2] == 'R' || value[2] == 'r')
                    && (value[2] == 'M' || value[2] == 'm')
                    && (value[2] == 'I' || value[2] == 'i')
                    && (value[3] == 'T' || value[3] == 't'))
                {
                    mResult = PERMIT;
                }
            }
        }
    }
}

bool spep::Decision::operator==(const spep::Decision &rhs)
{
    return rhs.mResult == mResult;
}
bool spep::Decision::operator!=(const spep::Decision &rhs)
{
    return rhs.mResult != mResult;
}
bool spep::Decision::operator==(spep::Decision::Result rhs)
{
    return rhs == mResult;
}

bool spep::Decision::operator!=(spep::Decision::Result rhs)
{
    return rhs != mResult;
}

const spep::Decision &spep::Decision::operator=(spep::Decision::Result result)
{
    mResult = result; return *this;
}

const spep::Decision &spep::Decision::operator+=(const spep::Decision &rhs)
{
    /* Refactored from the Java implementation. Original comments inline */

    // Guard the switch, we don't need to do anything if rhs is null
    if (NONE != rhs.mResult)
        switch (mResult)
    {
        // Same effect for NULL and PERMIT.
        case NONE:
        case PERMIT:
            /*
             * permit + permit = permit
             * permit + deny = deny
             * permit + cache = cache
             * permit + error = error
             */
            mResult = rhs.mResult;
            break;

        case DENY:
            /*
             * deny + permit = deny
             * deny + deny = deny
             * deny + cache = deny
             * deny + error = error
             */
            if (ERROR == rhs.mResult) mResult = ERROR;
            else mResult = DENY;
            break;

        case CACHE:
            /*
             * cache + permit = cache
             * cache + deny = deny
             * cache + cache = cache
             * cache + error = error
             */
            if (PERMIT == rhs.mResult) mResult = CACHE;
            else mResult = rhs.mResult;
            break;

        case ERROR:
            /*
             * error + permit = error
             * error + deny = error
             * error + cache = error
             * error + error = error
             */
            mResult = ERROR;
            break;
    }

    return *this;
}

/* 
 * Copyright 2006, Queensland University of Technology
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
 * Creation Date: 11/10/2006
 * 
 * Purpose: Allows mock objects to capture and store objects which are created in private space inside the class being tested,
 * this in turn allows our testing framework to interrogate objects for correctness which it could otherwise never achieve. This implementation
 * allows for multiple objects to be captured and referenced.
 * 
 */

package com.qut.middleware.test;

import static org.easymock.EasyMock.reportMatcher;

import java.util.Vector;

import org.easymock.IArgumentMatcher;

public class Capture<T> implements IArgumentMatcher
{
    private Vector<T> captured = new Vector<T>();

    public void appendTo(StringBuffer buffer)
    {
        buffer.append("capture()");
    }

    public boolean matches(Object parameter)
    {
        captured.add( (T) parameter );

        return true;
    }

    public Vector<T> getCaptured()
    {
        return captured;
    }

    public static <T> T capture(Capture<T> capture)
    {
        reportMatcher(capture);

        return null;
    }

}

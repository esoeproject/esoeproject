/* Copyright 2006, Queensland University of Technology
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
 * Creation Date: 26/08/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.esoe.authn.plugins.spnego.handlers;

import org.junit.*;

import com.qut.middleware.esoe.authn.plugins.spnego.bean.CommaSeparatedStringList;
import com.qut.middleware.esoe.authn.plugins.spnego.bean.CommaSeparatedStringListImpl;

import static org.junit.Assert.*;

public class TestCommaSeparatedList
{
	@Test
	public void testList()
	{
		String list = "a, b, c, d, e";
		CommaSeparatedStringList commaSeparatedStringList = new CommaSeparatedStringListImpl(list);
		
		int i = 0;
		for (String str : new String[]{"a","b","c","d","e"})
		{
			assertTrue(commaSeparatedStringList.get(i++).equals(str));
		}
	}
}

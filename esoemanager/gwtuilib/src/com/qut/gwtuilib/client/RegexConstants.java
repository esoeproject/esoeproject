/* Copyright 2008, Queensland University of Technology
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
 */
package com.qut.gwtuilib.client;

public class RegexConstants
{
	public final static String matchAll = ".*";
	public final static String commonName = "([A-Z]|[a-z]|[0-9]| )+";
	public final static String telephoneNumber = "([0-9]| |-)+";
	public final static String URLRegex = "([A-Za-z][A-Za-z0-9+.-]{1,120}:[A-Za-z0-9/](([A-Za-z0-9$_.+!*,;/?:@&~=-])|%[A-Fa-f0-9]{2}){1,333}(#([a-zA-Z0-9][a-zA-Z0-9$_.+!*,;/?:@&~=%-]{0,1000}))?)";
	public final static String PathRegex = "([A-Z]|[a-z]|[0-9]|/|.)+";
	public final static String emailAddress = "^[a-zA-Z0-9\\.\\-_\\*\\']+(\\+[a-zA-Z0-9\\.\\-_\\*\\']+)?\\@[a-zA-Z0-9\\-]+(\\.[a-zA-Z0-9\\-]+)+\\.?$";
	public final static String numeric = "[0-9]*";
}

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
 * Creation Date: 31/10/2006
 * 
 * Purpose: Stores references to all SAML 2.0 protocol enumeration values. 
 * Docuemnt: saml-metadata-2.0-os.pdf, 2.4.1
 */
package com.qut.middleware.saml2;

/** Stores references to all SAML 2.0 protocol enumeration values. */
public class ProtocolConstants
{
	/**
	 * A whitespace-delimited set of URIs that identify the set of protocol specifications supported by the role
	 * element. For SAML V2.0 entities, this set MUST include the SAML protocol namespace URI,
	 * urn:oasis:names:tc:SAML:2.0:protocol. Note that future SAML specifications might share the same namespace URI,
	 * but SHOULD provide alternate "protocol support" identifiers to ensure discrimination when necessary.
	 */
	public static String protocol = "urn:oasis:names:tc:SAML:2.0:protocol"; //$NON-NLS-1$
}

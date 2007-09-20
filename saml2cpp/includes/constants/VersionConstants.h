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
 * Creation Date: 14/02/2007
 * 
 * Purpose: Stores references to all SAML version code values
 * Docuemnt: saml-core-2.0-os.pdf, various
 */
 
 #ifndef VERSIONCONSTANTS_H_
 #define VERSIONCONSTANTS_H_

 /* STL */
 #include <string>
 /* for std::wstring workaround */
 #include "SAML2Defs.h"
 
 namespace saml2
 {
 	namespace versions
 	{
		/** Version string for all SAML 2.0 compliant response documents */
		const static std::wstring SAML_20 = L"2.0";
 	}
 }
 
 #endif


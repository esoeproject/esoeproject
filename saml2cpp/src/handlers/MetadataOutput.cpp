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
 * Creation Date: 01/03/2007
 * 
 * Purpose: 
 */

#include "handlers/MetadataOutput.h"



XSECCryptoKey* saml2::KeyData::createXSECCryptoKey()
{
	OpenSSLCryptoKeyRSA *rsaKey = NULL;
	OpenSSLCryptoKeyDSA *dsaKey = NULL;
	
	switch (this->type)
	{
		case RSA:
		/* Build the XML-Security key object for RSA key */
		rsaKey = new OpenSSLCryptoKeyRSA();
		rsaKey->loadPublicModulusBase64BigNums( this->modulus.c_str(), this->modulus.length() );
		rsaKey->loadPublicExponentBase64BigNums( this->exponent.c_str(), this->exponent.length() );
		return rsaKey;
		
		case DSA:
		/* Build the XML-Security key object for DSA key */
		dsaKey = new OpenSSLCryptoKeyDSA();
		dsaKey->loadPBase64BigNums( this->p.c_str(), this->p.length() );
		dsaKey->loadQBase64BigNums( this->q.c_str(), this->q.length() );
		dsaKey->loadGBase64BigNums( this->g.c_str(), this->g.length() );
		dsaKey->loadYBase64BigNums( this->y.c_str(), this->y.length() );
		dsaKey->loadJBase64BigNums( this->j.c_str(), this->j.length() );
		return dsaKey;
		
		default:
		return NULL;
	}
	
}

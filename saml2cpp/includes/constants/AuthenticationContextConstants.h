/* 
 * Copyright 2006-2007, Queensland University of Technology
 * Licensed under the Apache License, Version 2.0 (the= L"License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy of 
 * the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an= L"AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations under 
 * the License.
 * 
 * Author: Bradley Beddoes
 * Creation Date: 14/02/2007
 * 
 * Purpose: Stores references to all SAML 2.0 authentication context classes values
 * Docuemnt: saml-authn-context-2.0-os.pdf, 3.0
 */
 
#ifndef AUTHENTICATIONCONSTANTS_H_
#define AUTHENTICATIONCONSTANTS_H_
 
/* STL */
 #include <string>
 
 namespace saml2
 {
 	namespace authentication
 	{
		/**
		 * The Internet Protocol class is applicable when a principal is authenticated through the use of a provided IP
		 * address.
		 */
		const static std::wstring INTERNET_PROTOCOL = L"urn:oasis:names:tc:SAML:2.0:ac:classes:InternetProtocol";
	
	
		/**
		 * The Internet Protocol Password class is applicable when a principal is authenticated through the use of a
		 * provided IP address, in addition to a username/password.
		 */
		const static std::wstring INTERNET_PROTOCOL_PASS = L"urn:oasis:names:tc:SAML:2.0:ac:classes:InternetProtocolPassword";
	
		/**
		 * This class is applicable when the principal has authenticated using a password to a local authentication
		 * authority, in order to acquire a Kerberos ticket. That Kerberos ticket is then used for subsequent network
		 * authentication. Note: It is possible for the authentication authority to indicate (via this context class) a pre-
		 * authentication data type which was used by the Kerberos Key Distribution Center [RFC 1510] when authenticating
		 * the principal. The method used by the authentication authority to obtain this information is outside of the scope
		 * of this specification, but it is strongly recommended that a trusted method be deployed to pass the
		 * pre-authentication data type and any other Kerberos related context details (e.g. ticket lifetime) to the
		 * authentication authority.
		 */
		const static std::wstring KERBEROS = L"urn:oasis:names:tc:SAML:2.0:ac:classes:Kerberos";
	
	
		/**
		 * Reflects no mobile customer registration procedures and an authentication of the mobile device without requiring
		 * explicit end-user interaction. This context class authenticates only the device and never the user; it is useful
		 * when services other than the mobile operator want to add a secure device authentication to their authentication
		 * process.
		 */
		const static std::wstring MOBILE_ONE_FACTOR_UNREG = L"urn:oasis:names:tc:SAML:2.0:ac:classes:MobileOneFactorUnregistered";
	
	
		/**
		 * Reflects no mobile customer registration procedures and a two-factor based authentication, such as secure device
		 * and user PIN. This context class is useful when a service other than the mobile operator wants to link their
		 * customer ID to a mobile supplied two-factor authentication service by capturing mobile phone data at enrollment.
		 */
		const static std::wstring MOBILE_TWO_FACTOR_UNREG = L"urn:oasis:names:tc:SAML:2.0:ac:classes:MobileTwoFactorUnregistered";
	
		/**
		 * Reflects mobile contract customer registration procedures and a single factor authentication. For example, a
		 * digital signing device with tamper resistant memory for key storage, such as the mobile MSISDN, but no required
		 * PIN or biometric for real-time user authentication.
		 */
		const static std::wstring MOBILE_ONE_FACTOR_CONTRACT = L"urn:oasis:names:tc:SAML:2.0:ac:classes:MobileOneFactorContract";
	
		/**
		 * Reflects mobile contract customer registration procedures and a two-factor based authentication. For example, a
		 * digital signing device with tamper resistant memory for key storage, such as a GSM SIM, that requires explicit
		 * proof of user identity and intent, such as a PIN or biometric.
		 */
		const static std::wstring MOBILE_TWO_FACTOR_CONTRACT = L"urn:oasis:names:tc:SAML:2.0:ac:classes:MobileTwoFactorContract";
	
		/**
		 * The Password class is applicable when a principal authenticates to an authentication authority through the
		 * presentation of a password over an unprotected HTTP session.
		 */
		const static std::wstring PASSWORD = L"urn:oasis:names:tc:SAML:2.0:ac:classes:Password";
	
	
		/**
		 * The PasswordProtectedTransport class is applicable when a principal authenticates to an authentication authority
		 * through the presentation of a password over a protected session.
		 */
		const static std::wstring PASSWORD_PROTECTED_TRANSPORT = L"urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport";
	
		/**
		 * The PreviousSession class is applicable when a principal had authenticated to an authentication authority at some
		 * point in the past using any authentication context supported by that authentication authority. Consequently, a
		 * subsequent authentication event that the authentication authority will assert to the relying party may be
		 * significantly separated in time from the principal's current resource access request. The context for the
		 * previously authenticated session is explicitly not included in this context class because the user has not
		 * authenticated during this session, and so the mechanism that the user employed to authenticate in a previous
		 * session should not be used as part of a decision on whether to now allow access to a resource.
		 */
		const static std::wstring PREVIOUS_SESSION = L"urn:oasis:names:tc:SAML:2.0:ac:classes:PreviousSession";
	
		/**
		 * The X509 context class indicates that the principal authenticated by means of a digital signature where the key
		 * was validated as part of an X.509 Public Key Infrastructure.
		 */
		const static std::wstring X509 = L"urn:oasis:names:tc:SAML:2.0:ac:classes:X509";
	
		/**
		 * The PGP context class indicates that the principal authenticated by means of a digital signature where the key
		 * was validated as part of a PGP Public Key Infrastructure.
		 */
		const static std::wstring PGP = L"urn:oasis:names:tc:SAML:2.0:ac:classes:PGP";
	
		/**
		 * The SPKI context class indicates that the principal authenticated by means of a digital signature where the key
		 * was validated via an SPKI Infrastructure.
		 */
		const static std::wstring SPKI = L"urn:oasis:names:tc:SAML:2.0:ac:classes:SPKI";
	
		/**
		 * Note that this URI is also used as the target namespace in the corresponding authentication context class schema
		 * document [SAMLAC-XSig] This context class indicates that the principal authenticated by means of a digital
		 * signature according to the processing rules specified in the XML Digital Signature specification [XMLSig]
		 */
		const static std::wstring XMLDSIG = L"urn:oasis:names:tc:SAML:2.0:ac:classes:XMLDSig";
	
		/**
		 * The Smartcard class is identified when a principal authenticates to an authentication authority using a
		 * smartcard.
		 */
		const static std::wstring SMART_CARD = L"urn:oasis:names:tc:SAML:2.0:ac:classes:Smartcard";
	
		/**
		 * The SmartcardPKI class is applicable when a principal authenticates to an authentication authority through a
		 * two-factor authentication mechanism using a smartcard with enclosed private key and a PIN.
		 */
		const static std::wstring SMART_CARD_PKI = L"urn:oasis:names:tc:SAML:2.0:ac:classes:SmartcardPKI";
	
		/**
		 * Note that this URI is also used as the target namespace in the corresponding authentication context class schema
		 * document [SAMLAC-SwPKI] . The Software-PKI class is applicable when a principal uses an X.509 certificate stored
		 * in software to authenticate to the authentication authority.
		 */
		const static std::wstring SOFTWARE_PKI = L"urn:oasis:names:tc:SAML:2.0:ac:classes:SoftwarePKI";
	
		/**
		 * This class is used to indicate that the principal authenticated via the provision of a fixed-line telephone
		 * number, transported via a telephony protocol such as ADSL.
		 */
		const static std::wstring TELEPHONY = L"urn:oasis:names:tc:SAML:2.0:ac:classes:Telephony";
	
		/**
		 * Indicates that the principal is= L"roaming" (perhaps using a phone card) and authenticates via the means of the
		 * line number, a user suffix, and a password element.
		 */
		const static std::wstring NOMAD_TELEPHONY = L"urn:oasis:names:tc:SAML:2.0:ac:classes:NomadTelephony";
	
		/**
		 * This class is used to indicate that the principal authenticated via the provision of a fixed-line telephone
		 * number and a user suffix, transported via a telephony protocol such as ADSL.
		 */
		const static std::wstring PERSONAL_TELEPHONY = L"urn:oasis:names:tc:SAML:2.0:ac:classes:PersonalTelephony";
	
		/**
		 * Indicates that the principal authenticated via the means of the line number, a user suffix, and a password
		 * element.
		 */
		const static std::wstring AUTHENTICATION_TELEPHONY = L"urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony";
	
		/**
		 * The Secure Remote Password class is applicable when the authentication was performed by means of Secure Remote
		 * Password as specified in [RFC 2945].
		 */
		const static std::wstring SECURE_REMOTE_PASSWORD = L"urn:oasis:names:tc:SAML:2.0:ac:classes:SecureRemotePassword";
	
		/**
		 * This class indicates that the principal authenticated by means of a client certificate, secured with the SSL/TLS
		 * transport.
		 */
		const static std::wstring TLS_CLIENT = L"urn:oasis:names:tc:SAML:2.0:ac:classes:TLSClient";
	
		/**
		 * The TimeSyncToken class is applicable when a principal authenticates through a time synchronization token.
		 */
		const static std::wstring TIME_SYNC_TOKEN = L"urn:oasis:names:tc:SAML:2.0:ac:classes:TimeSyncToken";
	
		/**
		 * The Unspecified class indicates that the authentication was performed by unspecified means.
		 */
		const static std::wstring UNSPECIFIED = L"urn:oasis:names:tc:SAML:2.0:ac:classes:unspecified";
	}
 }
 
 #endif

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
 * Creation Date: 30/10/2006
 * 
 * Purpose: Stores references to all SAML 2.0 authentication context classes values
 * Docuemnt: saml-authn-context-2.0-os.pdf, 3.0
 */

package com.qut.middleware.saml2;

/** Stores references to all SAML 2.0 authentication context classes values.*/
public class AuthenticationContextConstants
{
	/**
	 * The Internet Protocol class is applicable when a principal is authenticated through the use of a provided IP
	 * address.
	 */
	public static final String internetProtocol = "urn:oasis:names:tc:SAML:2.0:ac:classes:InternetProtocol"; //$NON-NLS-1$


	/**
	 * The Internet Protocol Password class is applicable when a principal is authenticated through the use of a
	 * provided IP address, in addition to a username/password.
	 */
	public static final String internetProtocolPassword = "urn:oasis:names:tc:SAML:2.0:ac:classes:InternetProtocolPassword"; //$NON-NLS-1$

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
	public static final String kerberos = "urn:oasis:names:tc:SAML:2.0:ac:classes:Kerberos"; //$NON-NLS-1$


	/**
	 * Reflects no mobile customer registration procedures and an authentication of the mobile device without requiring
	 * explicit end-user interaction. This context class authenticates only the device and never the user; it is useful
	 * when services other than the mobile operator want to add a secure device authentication to their authentication
	 * process.
	 */
	public static final String mobileOneFactorUnreg = "urn:oasis:names:tc:SAML:2.0:ac:classes:MobileOneFactorUnregistered"; //$NON-NLS-1$


	/**
	 * Reflects no mobile customer registration procedures and a two-factor based authentication, such as secure device
	 * and user PIN. This context class is useful when a service other than the mobile operator wants to link their
	 * customer ID to a mobile supplied two-factor authentication service by capturing mobile phone data at enrollment.
	 */
	public static final String mobileTwoFactoryUnreg = "urn:oasis:names:tc:SAML:2.0:ac:classes:MobileTwoFactorUnregistered"; //$NON-NLS-1$

	/**
	 * Reflects mobile contract customer registration procedures and a single factor authentication. For example, a
	 * digital signing device with tamper resistant memory for key storage, such as the mobile MSISDN, but no required
	 * PIN or biometric for real-time user authentication.
	 */
	public static final String mobileOneFactorContract = "urn:oasis:names:tc:SAML:2.0:ac:classes:MobileOneFactorContract"; //$NON-NLS-1$

	/**
	 * Reflects mobile contract customer registration procedures and a two-factor based authentication. For example, a
	 * digital signing device with tamper resistant memory for key storage, such as a GSM SIM, that requires explicit
	 * proof of user identity and intent, such as a PIN or biometric.
	 */
	public static final String mobileTwoFactorContract = "urn:oasis:names:tc:SAML:2.0:ac:classes:MobileTwoFactorContract"; //$NON-NLS-1$

	/**
	 * The Password class is applicable when a principal authenticates to an authentication authority through the
	 * presentation of a password over an unprotected HTTP session.
	 */
	public static final String password = "urn:oasis:names:tc:SAML:2.0:ac:classes:Password"; //$NON-NLS-1$


	/**
	 * The PasswordProtectedTransport class is applicable when a principal authenticates to an authentication authority
	 * through the presentation of a password over a protected session.
	 */
	public static final String passwordProtectedTransport = "urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport"; //$NON-NLS-1$

	/**
	 * The PreviousSession class is applicable when a principal had authenticated to an authentication authority at some
	 * point in the past using any authentication context supported by that authentication authority. Consequently, a
	 * subsequent authentication event that the authentication authority will assert to the relying party may be
	 * significantly separated in time from the principal's current resource access request. The context for the
	 * previously authenticated session is explicitly not included in this context class because the user has not
	 * authenticated during this session, and so the mechanism that the user employed to authenticate in a previous
	 * session should not be used as part of a decision on whether to now allow access to a resource.
	 */
	public static final String previousSession = "urn:oasis:names:tc:SAML:2.0:ac:classes:PreviousSession"; //$NON-NLS-1$

	/**
	 * The X509 context class indicates that the principal authenticated by means of a digital signature where the key
	 * was validated as part of an X.509 Public Key Infrastructure.
	 */
	public static final String x509 = "urn:oasis:names:tc:SAML:2.0:ac:classes:X509"; //$NON-NLS-1$

	/**
	 * The PGP context class indicates that the principal authenticated by means of a digital signature where the key
	 * was validated as part of a PGP Public Key Infrastructure.
	 */
	public static final String pgp = "urn:oasis:names:tc:SAML:2.0:ac:classes:PGP"; //$NON-NLS-1$

	/**
	 * The SPKI context class indicates that the principal authenticated by means of a digital signature where the key
	 * was validated via an SPKI Infrastructure.
	 */
	public static final String spki = "urn:oasis:names:tc:SAML:2.0:ac:classes:SPKI"; //$NON-NLS-1$

	/**
	 * Note that this URI is also used as the target namespace in the corresponding authentication context class schema
	 * document [SAMLAC-XSig] This context class indicates that the principal authenticated by means of a digital
	 * signature according to the processing rules specified in the XML Digital Signature specification [XMLSig]
	 */
	public static final String xmldsig = "urn:oasis:names:tc:SAML:2.0:ac:classes:XMLDSig"; //$NON-NLS-1$

	/**
	 * The Smartcard class is identified when a principal authenticates to an authentication authority using a
	 * smartcard.
	 */
	public static final String smartcard = "urn:oasis:names:tc:SAML:2.0:ac:classes:Smartcard"; //$NON-NLS-1$

	/**
	 * The SmartcardPKI class is applicable when a principal authenticates to an authentication authority through a
	 * two-factor authentication mechanism using a smartcard with enclosed private key and a PIN.
	 */
	public static final String smartcardPKI = "urn:oasis:names:tc:SAML:2.0:ac:classes:SmartcardPKI"; //$NON-NLS-1$

	/**
	 * Note that this URI is also used as the target namespace in the corresponding authentication context class schema
	 * document [SAMLAC-SwPKI] . The Software-PKI class is applicable when a principal uses an X.509 certificate stored
	 * in software to authenticate to the authentication authority.
	 */
	public static final String softwarePKI = "urn:oasis:names:tc:SAML:2.0:ac:classes:SoftwarePKI"; //$NON-NLS-1$

	/**
	 * This class is used to indicate that the principal authenticated via the provision of a fixed-line telephone
	 * number, transported via a telephony protocol such as ADSL.
	 */
	public static final String telephony = "urn:oasis:names:tc:SAML:2.0:ac:classes:Telephony"; //$NON-NLS-1$

	/**
	 * Indicates that the principal is "roaming" (perhaps using a phone card) and authenticates via the means of the
	 * line number, a user suffix, and a password element.
	 */
	public static final String nomadTelephony = "urn:oasis:names:tc:SAML:2.0:ac:classes:NomadTelephony"; //$NON-NLS-1$

	/**
	 * This class is used to indicate that the principal authenticated via the provision of a fixed-line telephone
	 * number and a user suffix, transported via a telephony protocol such as ADSL.
	 */
	public static final String personalTelephony = "urn:oasis:names:tc:SAML:2.0:ac:classes:PersonalTelephony"; //$NON-NLS-1$

	/**
	 * Indicates that the principal authenticated via the means of the line number, a user suffix, and a password
	 * element.
	 */
	public static final String authenticatedTelephony = "urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony"; //$NON-NLS-1$

	/**
	 * The Secure Remote Password class is applicable when the authentication was performed by means of Secure Remote
	 * Password as specified in [RFC 2945].
	 */
	public static final String secureRemotePassword = "urn:oasis:names:tc:SAML:2.0:ac:classes:SecureRemotePassword"; //$NON-NLS-1$

	/**
	 * This class indicates that the principal authenticated by means of a client certificate, secured with the SSL/TLS
	 * transport.
	 */
	public static final String tlsClient = "urn:oasis:names:tc:SAML:2.0:ac:classes:TLSClient"; //$NON-NLS-1$

	/**
	 * The TimeSyncToken class is applicable when a principal authenticates through a time synchronization token.
	 */
	public static final String timeSyncToken = "urn:oasis:names:tc:SAML:2.0:ac:classes:TimeSyncToken"; //$NON-NLS-1$

	/**
	 * The Unspecified class indicates that the authentication was performed by unspecified means.
	 */
	public static final String unspecified = "urn:oasis:names:tc:SAML:2.0:ac:classes:unspecified"; //$NON-NLS-1$

}

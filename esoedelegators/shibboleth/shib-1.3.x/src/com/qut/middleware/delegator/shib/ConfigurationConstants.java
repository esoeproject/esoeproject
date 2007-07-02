package com.qut.middleware.delegator.shib;

/** */
@SuppressWarnings("nls")
public class ConfigurationConstants
{
	/* Schema constants, included across the project */

	/** SAML Protocol schema */
	public static final String samlProtocol = "saml-schema-protocol-2.0.xsd";
	/** SAML Assertion schema */
	public static final String samlAssertion = "saml-schema-assertion-2.0.xsd";
	/** SAML Metadata schema */
	public static final String samlMetadata = "saml-schema-metadata-2.0.xsd";
	/** LXACML schema */
	public static final String lxacml = "lxacml-schema.xsd";
	/** LXACML SAML Protocol schema */
	public static final String lxacmlSAMLProtocol = "lxacml-schema-saml-protocol.xsd";
	/** LXACML SAML Assertion schema */
	public static final String lxacmlSAMLAssertion = "lxacml-schema-saml-assertion.xsd";
	/** LXACML Group Target schema */
	public static final String lxacmlGroupTarget = "lxacml-schema-grouptarget.xsd";
	/** LXACML Context schema */
	public static final String lxacmlContext = "lxacml-schema-context.xsd";
	/** LXACML Metadata schema */
	public static final String lxacmlMetadata = "lxacml-schema-metadata.xsd";
	/** ESOE Protocol schema */
	public static final String esoeProtocol = "esoe-schema-saml-protocol.xsd";
	/** Cache Clear Service schema */
	public static final String cacheClearService = "cacheclear-schema-saml-metadata.xsd";
	/** Session Data schema */
	public static final String sessionData = "sessiondata-schema.xsd";
	/** Delegated Authentication schema */
	public static final String delegatedAuthn = "delegated-schema-saml-protocol.xsd";

	/** Timezone in use for the ESOE */
	public static final String timeZone = "UTC";

	/** Spring IoC Integration */
	public static final String DELEGATOR_CONFIG = "/WEB-INF/shibdelegator.config";
	public static final String AUTHN_PROCESSOR = "authnProcessor";
	public static final String AUTHN_AUTHORITY_PROCESSOR = "authnAuthorityProcessor";
	public static final String SESSION_TOKEN_NAME = "sessionTokenName";
	public static final String AUTHN_REDIRECT_URL = "authenticationURL";
	public static final String COOKIE_SESSION_DOMAIN = "sessionDomain";
	public static final String SSO_URL = "ssoURL";
	public static final String DENIED_URL = "deniedURL";
	public static final String ACCEPT_URL = "acceptURL";
	public static final String FAIL_URL = "failURL";
	
	/** Form controls **/
	public static final String ACCEPTED_POLICY = "Accept";
	public static final String DENIED_POLICY = "Deny";
	public static final String ACCEPTED_IDENTITY_FORM_ELEMENT = "identityacceptance";
	
	/** Sotred session data */
	public static final String RELEASED_ATTRIBUTES_SESSION_IDENTIFIER = "shib_released_attributes";
	
}

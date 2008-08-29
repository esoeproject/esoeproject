package com.qut.middleware.deployer.bean;

public class DeploymentBeanConsts 
{	
	private final String esoeConfiguration = "data/core";
	private final String esoeManagerConfiguration = "data/manager";
	private final String esoeManagerSpepConfiguration = "data/spep";
	
	private final String esoeLxacmlService = "/ws/services/esoe/policyDecisionPoint";
	private final String esoeAttributeService = "/ws/services/esoe/attributeAuthority";
	private final String esoeSPEPStartupService = "/ws/services/esoe/spepStartup";
	private final String esoeSingleSignOn = "/sso";
	
	private final String spAssertionConsumerService = "/spep/sso";
	private final String spSingleLogoutService = "/spep/services/spep/singleLogout";
	private final String spCacheClearService = "/spep/services/spep/authzCacheClear";
	
	private final String managerServiceDescription = "ESOE Manager, configures and maintains ESOE components";
	private final String managerServiceName = "ESOE Manager";
	private final String managerAccessDenied = "Your access to ESOE Manager has been denied, please contact a system administrator";
		
	public String getEsoeConfiguration() {
		return esoeConfiguration;
	}

	public String getEsoeManagerConfiguration() {
		return esoeManagerConfiguration;
	}

	public String getEsoeManagerSpepConfiguration() {
		return esoeManagerSpepConfiguration;
	}

	public String getEsoeLxacmlService() {
		return esoeLxacmlService;
	}

	public String getEsoeAttributeService() {
		return esoeAttributeService;
	}
	
	public String getEsoeSPEPStartupService() {
		return esoeSPEPStartupService;
	}

	public String getEsoeSingleSignOn() {
		return esoeSingleSignOn;
	}

	public String getManagerServiceDescription() {
		return managerServiceDescription;
	}

	public String getManagerServiceName() {
		return managerServiceName;
	}

	public String getManagerAccessDenied() {
		return managerAccessDenied;
	}

	public String getSpAssertionConsumerService() {
		return spAssertionConsumerService;
	}

	public String getSpSingleLogoutService() {
		return spSingleLogoutService;
	}

	public String getSpCacheClearService() {
		return spCacheClearService;
	}
	
		
}

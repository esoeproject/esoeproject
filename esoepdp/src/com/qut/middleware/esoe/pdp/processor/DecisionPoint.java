package com.qut.middleware.esoe.pdp.processor;

import java.util.List;
import java.util.Map;

import com.qut.middleware.esoe.pdp.processor.impl.DecisionData;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.DecisionType;

public interface DecisionPoint 
{
		
	/** Make an authorization decision based on the given request parameters. The implementation
	 * will obtain a list of valid Policies for the given issuer and validate the rules therein to determine
	 * if the given request is allowable for the given attributes. 
	 * 
	 * @param resource The resource for which a decision request is made.
	 * @param issuer The issuer of the decision request. This is typically the identifier of the SPEP making the request.
	 * @param identityAttributes A list of attributes to match against policy specified rules. Typically attributes belonging
	 * to a Principal for which the request is being made. 
	 * @return Permit if the requested resource is allowed as specified by a matching Policy, else Deny.
	 */
	public DecisionType makeAuthzDecision(String resource, String issuer, Map<String, List<String>> identityAttributes);
	
	
	/** Make an authorization decision based on the given request parameters. The implementation
	 * will obtain a list of valid Policies for the given issuer and validate the rules therein to determine
	 * if the given request is allowable for the given attributes. 
	 * 
	 * @param resource The resource for which a decision request is made.
	 * @param action The action to match against the retrieved policy, if exists.
	 * @param issuer The issuer of the decision request. This is typically the identifier of the SPEP making the request.
	 * @param identityAttributes A list of attributes to match against policy specified rules. Typically attributes belonging
	 * to a Principal for which the request is being made. 
	 * @return Permit if the requested resource is allowed as specified by a matching Policy, else Deny.
	 */
	public DecisionType makeAuthzDecision(String resource, String issuer, Map<String, List<String>>  identityAttributes, String action);

	/** Make an authorization decision based on the given request parameters. The implementation
	 * 
	 * @param resource
	 * @param action
	 * @param issuer
	 * @param identityAttributes
	 * @return
	 */
	public DecisionType makeAuthzDecision(String resource, String issuer, Map<String, List<String>>  identityAttributes, String action, DecisionData decisiondata);

	/** Retrieve the default authorization mode of the decision point.
	 * 
	 * @return  A String representation of the default mode.
	 */
	public String getDefaultMode();
}

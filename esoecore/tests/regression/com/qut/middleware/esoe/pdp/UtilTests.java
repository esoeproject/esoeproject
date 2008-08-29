package com.qut.middleware.esoe.pdp;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.esoe.authz.exception.InvalidRequestException;
import com.qut.middleware.esoe.authz.impl.PolicyEvaluator;
import com.qut.middleware.esoe.authz.impl.ProtocolTools;
import com.qut.middleware.esoe.authz.impl.RequestEvaluator;
import com.qut.middleware.esoe.pdp.processor.impl.DecisionData;
import com.qut.middleware.saml2.schemas.esoe.lxacml.AttributeAssignment;
import com.qut.middleware.saml2.schemas.esoe.lxacml.EffectType;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Obligation;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.DecisionType;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.Result;
import com.qut.middleware.saml2.schemas.esoe.lxacml.grouptarget.GroupTarget;
import com.qut.middleware.saml2.schemas.esoe.lxacml.protocol.LXACMLAuthzDecisionQuery;

/** Test various methods in utility classes used by the PDP component.
 */

@SuppressWarnings("nls")
public class UtilTests {

	PolicyEvaluator policyEval;
	ProtocolTools protoTools;
	RequestEvaluator requestEval;
	
	@Before
	public void setUp() throws Exception
	{
		this.policyEval = new PolicyEvaluator();		
		this.protoTools = new ProtocolTools();
		this.requestEval = new RequestEvaluator();
	}

	/** Test the createResult() function in PolicyEvaluator
	 * 
	 *
	 */
	@Test
	public void testPolicyEvaluator1()
	{
		DecisionData decisionData = new DecisionData();
		
		// add a group target match to policy data
		decisionData.addGroupTarget("/resource1");
		
		// add an authz match to policy data
		decisionData.addMatch("/resource1/myResource.jsp");
		decisionData.addMatch("/resource1/myResource2.doc");
		
		
		assertTrue(decisionData.getMatches().contains("/resource1/myResource.jsp"));		
		assertTrue(decisionData.getMatches().contains("/resource1/myResource2.doc"));
		
		// small check to ensure policy data returns actual current (latest added) match
		assertTrue(decisionData.getCurrentMatch().equals("/resource1/myResource2.doc"));
		
		Result result = this.policyEval.createDefaultResult("TestMessage", "DENY", decisionData);
		
		// make sure it set decision to what we stated
		assertEquals(DecisionType.DENY, result.getDecision());

		// make sure result contains our group targets
		for(Obligation obligation : result.getObligations().getObligations())
		{
			for(AttributeAssignment attributeAssignment : obligation.getAttributeAssignments())
			{
				// assert that there is only 1 GroupTarget match, as thats all we added to policy data
				assertEquals("Returned result contained incorrect number of GroupTargets", 1, attributeAssignment.getContent().size());
				
				for(Object groupTarget : attributeAssignment.getContent())
				{
					// check that our group target match is there
					GroupTarget target = (GroupTarget)groupTarget;
					assertEquals("Attribute assignments did not contain our GroupTarget", "/resource1", target.getGroupTargetID());
					
					// make sure both our authz matches are there
					assertTrue("Authz target /resource1/myResource.jsp was not present in result", target.getAuthzTargets().contains("/resource1/myResource.jsp"));
					assertTrue("Authz target /resource1/myResource2.doc was not present in result", target.getAuthzTargets().contains("/resource1/myResource2.doc"));
					
				}
			}
		}
	}

	/** Test RequestEvaluator getResource() function with invalid data.
	 * 
	 *
	 */
	@Test
	public void testRequestEvaluator1()
	{
		try
		{
			this.requestEval.getResource(null);
		}
		catch(InvalidRequestException e)
		{
			// good
		}
		
		try
		{
			this.requestEval.getResource(new LXACMLAuthzDecisionQuery());
		}
		catch(InvalidRequestException e)
		{
			// good
		}
	}

	/** Test invalid and valid data. createDecision() method.
	 * 
	 *
	 */
	@Test
	public void testProtocolTools1()
	{
		try
		{
			ProtocolTools.createDecision("invalid option");
			
			fail("ProtocolTools.createResult() should not accept invalid data.");
		}
		catch(IllegalArgumentException e)
		{
			// good
		}
		
		DecisionType decision = ProtocolTools.createDecision("PERMIT");
		
		assertEquals("Incorrect decision type returned.", DecisionType.PERMIT, decision);
		
		decision = ProtocolTools.createDecision("DENY");
		
		assertEquals("Incorrect decision type returned.", DecisionType.DENY, decision);
		
	}
	
	/** Test invalid and valid data. createOblogation() method.
	 * 
	 *
	 */
	@Test
	public void testProtocolTools2()
	{
		try
		{
			ProtocolTools.createObligation("invalid option");
			
			fail("ProtocolTools.createResult() should not accept invalid data.");
		}
		catch(IllegalArgumentException e)
		{
			// good
		}
		
		Obligation obligation = ProtocolTools.createObligation("PERMIT");
		
		assertEquals("Incorrect decision type returned.", EffectType.PERMIT, obligation.getFulfillOn());
		
		obligation = ProtocolTools.createObligation("DENY");
		
		assertEquals("Incorrect decision type returned.", EffectType.DENY, obligation.getFulfillOn());
		
	}
}

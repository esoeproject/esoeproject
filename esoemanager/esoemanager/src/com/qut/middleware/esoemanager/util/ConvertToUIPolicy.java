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
package com.qut.middleware.esoemanager.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.esoemanager.Constants;
import com.qut.middleware.saml2.exception.UnmarshallerException;
import com.qut.middleware.saml2.handler.Unmarshaller;
import com.qut.middleware.saml2.handler.impl.UnmarshallerImpl;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Action;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Actions;
import com.qut.middleware.saml2.schemas.esoe.lxacml.ApplyType;
import com.qut.middleware.saml2.schemas.esoe.lxacml.AttributeValueType;
import com.qut.middleware.saml2.schemas.esoe.lxacml.ConditionType;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Policy;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Resource;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Resources;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Rule;
import com.qut.middleware.saml2.schemas.esoe.lxacml.SubjectAttributeDesignatorType;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Target;

public class ConvertToUIPolicy
{
	private final String FUNCTION_STRING_EQUAL = "string-equal";
	private final String FUNCTION_STRING_REGEX = "string-regex-match";
	private final String FUNCTION_STRING_NORMALIZE_SPACE = "string-normalize-space";
	private final String FUNCTION_STRING_NORMALIZE_TO_LOWER = "string-normalize-to-lower-case";
	private final String FUNCTION_OR = "or";
	private final String FUNCTION_AND = "and";
	private final String FUNCTION_NOT = "not";

	private Unmarshaller<Policy> unmarshaller;

	private String[] schema =
	{
		Constants.lxacml
	};

	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(ConvertToUIPolicy.class);

	public ConvertToUIPolicy() throws UnmarshallerException
	{

		this.unmarshaller = new UnmarshallerImpl<Policy>(Policy.class.getPackage().getName(), schema);
	}

	public com.qut.middleware.esoemanager.client.rpc.bean.Policy convert(byte[] rawPolicy, boolean activated)
	{
		try
		{
			Policy policy = this.unmarshaller.unMarshallUnSigned(rawPolicy);
			return this.createUiPolicy(policy, activated);
		}
		catch (UnmarshallerException e)
		{
			return null;
		}
	}

	private com.qut.middleware.esoemanager.client.rpc.bean.Policy createUiPolicy(Policy lpol, boolean activated)
	{

		com.qut.middleware.esoemanager.client.rpc.bean.Policy uiPol = new com.qut.middleware.esoemanager.client.rpc.bean.Policy();

		uiPol.setActivated(activated);
		uiPol.setPolicyID(lpol.getPolicyId());
		uiPol.setDescription(lpol.getDescription());

		com.qut.middleware.esoemanager.client.rpc.bean.Target uiTar = new com.qut.middleware.esoemanager.client.rpc.bean.Target();
		uiPol.setTarget(uiTar);

		List<com.qut.middleware.esoemanager.client.rpc.bean.Rule> uiRules = new ArrayList<com.qut.middleware.esoemanager.client.rpc.bean.Rule>();
		uiPol.setRules(uiRules);

		this.createUiPolicyTarget(lpol.getTarget(), uiTar);
		this.createUiPolicyRules(lpol, uiRules);

		return uiPol;
	}

	private void createUiPolicyTarget(Target target, com.qut.middleware.esoemanager.client.rpc.bean.Target uiTar)
	{
		List<String> resourceList = new ArrayList<String>();
		List<String> actionList = new ArrayList<String>();

		Resources resources = target.getResources();
		if (resources != null)
		{
			for (Resource resource : resources.getResources())
			{
				resourceList.add((String) resource.getAttributeValue().getContent().get(0));
			}
			uiTar.setResources(resourceList);
		}

		Actions actions = target.getActions();
		if (actions != null)
		{
			for (Action action : actions.getActions())
			{
				actionList.add((String) action.getAttributeValue().getContent().get(0));
			}
			uiTar.setActions(actionList);
		}
	}

	private void createUiPolicyRules(Policy lpol, List<com.qut.middleware.esoemanager.client.rpc.bean.Rule> uiRules)
	{
		for (Rule rule : lpol.getRules())
		{
			com.qut.middleware.esoemanager.client.rpc.bean.Rule uiRule = new com.qut.middleware.esoemanager.client.rpc.bean.Rule();
			uiRule.setDescription(rule.getDescription());
			uiRule.setEffect(rule.getEffect().value());
			uiRule.setRuleID(rule.getRuleId());

			com.qut.middleware.esoemanager.client.rpc.bean.Target uiTar = new com.qut.middleware.esoemanager.client.rpc.bean.Target();

			if (rule.getTarget() != null)
			{
				this.createUiPolicyTarget(rule.getTarget(), uiTar);
				uiRule.setTarget(uiTar);
			}

			com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Condition uiRuleCondition = new com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Condition();
			uiRule.setCondition(uiRuleCondition);

			ConditionType cond = rule.getCondition();
			if (cond != null)
			{
				JAXBElement<ApplyType> expression = (JAXBElement<ApplyType>) cond.getExpression();
				if (expression != null)
				{
					JAXBElement<ApplyType> apply = expression;
					uiRuleCondition.setChild(this.processApply(apply.getValue()));
				}
			}

			uiRules.add(uiRule);
		}
	}

	private com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Operator processApply(ApplyType apply)
	{
		com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Operator operator;
		String function = apply.getFunctionId();

		if (function.equals(FUNCTION_OR))
			operator = processOR(apply);
		else
			if (function.equals(FUNCTION_NOT))
				operator = processNOT(apply);
			else
				if (function.equals(FUNCTION_AND))
					operator = processAND(apply);
				else
					if (function.equals(FUNCTION_STRING_REGEX))
						operator = processSTRINGREGEX(apply);
					else
						operator = processSTRINGEQUAL(apply);

		return operator;
	}

	private com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Operator processOR(ApplyType apply)
	{
		com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Or operator = new com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Or();
		operator.setDescription(apply.getDescription());
		operator.setChildren(this.processBooleanChildren(apply));
		return operator;
	}

	private com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Operator processNOT(ApplyType apply)
	{
		com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Not operator = new com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Not();
		operator.setDescription(apply.getDescription());
		operator.setChildren(this.processBooleanChildren(apply));
		return operator;
	}

	private com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Operator processAND(ApplyType apply)
	{
		com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.And operator = new com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.And();
		operator.setDescription(apply.getDescription());
		operator.setChildren(this.processBooleanChildren(apply));
		return operator;
	}

	private com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Operator processSTRINGREGEX(ApplyType apply)
	{
		com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.StringRegexMatch operator = new com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.StringRegexMatch();
		operator.setDescription(apply.getDescription());
		processStringChildren(apply, operator);
		return operator;
	}

	private com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Operator processSTRINGEQUAL(ApplyType apply)
	{
		com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.StringEqual operator = new com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.StringEqual();
		operator.setDescription(apply.getDescription());
		processStringChildren(apply, operator);
		return operator;
	}

	private List<com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Operator> processBooleanChildren(
			ApplyType apply)
	{
		List<com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Operator> children = new ArrayList<com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Operator>();
		Iterator<JAXBElement<?>> iter = apply.getExpressions().iterator();
		while (iter.hasNext())
		{
			JAXBElement child = iter.next();
			if (child.getDeclaredType() != ApplyType.class)
			{
				this.logger.debug("Unhandled child type in Or rule");
				continue;
			}
			JAXBElement<ApplyType> childFunction = child;
			children.add(this.processApply(childFunction.getValue()));
		}

		return children;
	}

	private com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Operator processStringChildren(
			ApplyType apply, com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.StringOperator operator)
	{
		List<String> attributeValues = new ArrayList<String>();
		operator.setAttributeValues(attributeValues);

		List<com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Operator> children = new ArrayList<com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Operator>();
		operator.setChildren(children);

		Iterator<JAXBElement<?>> iter = apply.getExpressions().iterator();
		while (iter.hasNext())
		{
			JAXBElement child = iter.next();

			if (child.getDeclaredType() == SubjectAttributeDesignatorType.class)
			{
				// set the attributes of the subject to compare against
				JAXBElement<SubjectAttributeDesignatorType> subj = child;
				operator.setAttributeName(subj.getValue().getAttributeId());
			}
			else
				if (child.getDeclaredType() == AttributeValueType.class)
				{
					// Contains the actual parameters of the function
					JAXBElement<AttributeValueType> attr = child;

					List content = attr.getValue().getContent();
					Iterator contentIter = content.iterator();
					while (contentIter.hasNext())
					{
						String value = contentIter.next().toString();
						attributeValues.add(value);
					}
				}
				else
				{
					if (child.getDeclaredType() == ApplyType.class)
					{
						JAXBElement<ApplyType> normalizeFunction = child;
						String childFunction = normalizeFunction.getValue().getFunctionId();

						if (childFunction.equals(FUNCTION_STRING_NORMALIZE_TO_LOWER))
						{
							children
									.add(new com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.StringLowerCase());
						}
						else
						{
							if (childFunction.equals(FUNCTION_STRING_NORMALIZE_SPACE))
							{
								children
										.add(new com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.StringNormalizeSpace());
							}
							else
							{
								this.logger.debug("Unhandled child type in String operator");
								continue;
							}
						}
					}
				}
		}

		return operator;
	}
}

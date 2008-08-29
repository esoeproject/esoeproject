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
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.And;
import com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Not;
import com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Or;
import com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.StringEqual;
import com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.StringRegexMatch;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Action;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Actions;
import com.qut.middleware.saml2.schemas.esoe.lxacml.ApplyType;
import com.qut.middleware.saml2.schemas.esoe.lxacml.AttributeValueType;
import com.qut.middleware.saml2.schemas.esoe.lxacml.ConditionType;
import com.qut.middleware.saml2.schemas.esoe.lxacml.EffectType;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Policy;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Resource;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Resources;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Rule;
import com.qut.middleware.saml2.schemas.esoe.lxacml.SubjectAttributeDesignatorType;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Target;

public class ConvertToLXACMLPolicy
{
	private final String FUNCTION_STRING_EQUAL = "string-equal";
	private final String FUNCTION_STRING_REGEX = "string-regex-match";
	private final String FUNCTION_STRING_NORMALIZE_SPACE = "string-normalize-space";
	private final String FUNCTION_STRING_NORMALIZE_TO_LOWER = "string-normalize-to-lower-case";
	private final String FUNCTION_OR = "or";
	private final String FUNCTION_AND = "and";
	private final String FUNCTION_NOT = "not";

	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(ConvertToLXACMLPolicy.class);

	public Policy convert(com.qut.middleware.esoemanager.client.rpc.bean.Policy uiPol)
	{
		return createUiPolicy(uiPol);
	}

	private Policy createUiPolicy(com.qut.middleware.esoemanager.client.rpc.bean.Policy uiPol)
	{
		Policy lpol = new Policy();

		lpol.setPolicyId(uiPol.getPolicyID());
		lpol.setDescription(uiPol.getDescription());

		Target tar = this.creatLXACMLPolicyTarget(uiPol.getTarget());
		
		if(tar != null)
			lpol.setTarget(tar);
		
		this.createLXACMLPolicyRules(lpol, uiPol.getRules());

		return lpol;
	}

	private Target creatLXACMLPolicyTarget(com.qut.middleware.esoemanager.client.rpc.bean.Target uiTar)
	{
		Target tar = new Target();

		List<String> resourceList = new ArrayList<String>();
		List<String> actionList = new ArrayList<String>();

		if (uiTar.getResources() != null && uiTar.getResources().size() > 0)
		{
			Resources resources = new Resources();
			tar.setResources(resources);
			for (String resVal : uiTar.getResources())
			{
				Resource resource = new Resource();
				AttributeValueType value = new AttributeValueType();
				value.getContent().add(resVal);
				resource.setAttributeValue(value);
				resources.getResources().add(resource);
			}

			if (uiTar.getActions() != null && uiTar.getActions().size() > 0)
			{
				Actions actions = new Actions();
				tar.setActions(actions);
				for (String actVal : uiTar.getActions())
				{
					Action action = new Action();
					AttributeValueType value = new AttributeValueType();
					value.getContent().add(actVal);
					action.setAttributeValue(value);
					actions.getActions().add(action);
				}
			}
			return tar;
		}
		else
			return null;
	}

	private void createLXACMLPolicyRules(Policy lpol, List<com.qut.middleware.esoemanager.client.rpc.bean.Rule> uiRules)
	{
		for (com.qut.middleware.esoemanager.client.rpc.bean.Rule uiRule : uiRules)
		{
			Rule rule = new Rule();
			lpol.getRules().add(rule);

			rule.setDescription(uiRule.getDescription());
			rule.setEffect(EffectType.fromValue(uiRule.getEffect()));
			rule.setRuleId(uiRule.getRuleID());

			Target tar = this.creatLXACMLPolicyTarget(uiRule.getTarget());
			if(tar != null)
				rule.setTarget(tar);
			
			ConditionType con = new ConditionType();
			rule.setCondition(con);

			com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Operator operator = uiRule.getCondition()
					.getChild();
			con.setExpression(this.processOperator(operator));
		}
	}

	private JAXBElement processOperator(com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Operator operator)
	{
		if (operator instanceof Or)
			return processOR(operator);
		else
			if (operator instanceof And)
				return processAND(operator);
			else
				if (operator instanceof Not)
					return processNOT(operator);
				else
					if (operator instanceof StringEqual)
						return processSTRINGEQUAL(operator);
					else
						if (operator instanceof StringRegexMatch)
							return processSTRINGREGEX(operator);

		/* Default fall back */
		return processSTRINGEQUAL(operator);
	}

	private JAXBElement processOR(com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Operator operator)
	{
		ApplyType apply = new ApplyType();
		JAXBElement<ApplyType> element = new JAXBElement<ApplyType>(new QName(
				"http://www.qut.com/middleware/lxacmlSchema", "Apply"), ApplyType.class, apply);
		apply.setFunctionId(this.FUNCTION_OR);
		apply.setDescription(operator.getDescription());
		this.processBooleanChildren(apply, operator);
		return element;
	}

	private JAXBElement processNOT(com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Operator operator)
	{
		ApplyType apply = new ApplyType();
		JAXBElement<ApplyType> element = new JAXBElement<ApplyType>(new QName(
				"http://www.qut.com/middleware/lxacmlSchema", "Apply"), ApplyType.class, apply);
		apply.setFunctionId(this.FUNCTION_NOT);
		apply.setDescription(operator.getDescription());
		this.processBooleanChildren(apply, operator);
		return element;
	}

	private JAXBElement processAND(com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Operator operator)
	{
		ApplyType apply = new ApplyType();
		JAXBElement<ApplyType> element = new JAXBElement<ApplyType>(new QName(
				"http://www.qut.com/middleware/lxacmlSchema", "Apply"), ApplyType.class, apply);
		apply.setFunctionId(this.FUNCTION_AND);
		apply.setDescription(operator.getDescription());
		this.processBooleanChildren(apply, operator);
		return element;
	}

	private JAXBElement processSTRINGREGEX(
			com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Operator operator)
	{
		ApplyType apply = new ApplyType();
		JAXBElement<ApplyType> element = new JAXBElement<ApplyType>(new QName(
				"http://www.qut.com/middleware/lxacmlSchema", "Apply"), ApplyType.class, apply);
		apply.setFunctionId(this.FUNCTION_STRING_REGEX);
		apply.setDescription(operator.getDescription());
		processStringChildren(apply,
				(com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.StringOperator) operator);
		return element;
	}

	private JAXBElement processSTRINGEQUAL(
			com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Operator operator)
	{
		ApplyType apply = new ApplyType();
		JAXBElement<ApplyType> element = new JAXBElement<ApplyType>(new QName(
				"http://www.qut.com/middleware/lxacmlSchema", "Apply"), ApplyType.class, apply);
		apply.setFunctionId(this.FUNCTION_STRING_EQUAL);
		apply.setDescription(operator.getDescription());
		processStringChildren(apply,
				(com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.StringOperator) operator);
		return element;
	}

	private void processBooleanChildren(ApplyType apply,
			com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Operator operator)
	{
		if (operator.getChildren() != null)
		{
			for (com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Operator uiChild : operator
					.getChildren())
			{
				JAXBElement element = this.processOperator(uiChild);
				apply.getExpressions().add(element);
			}
		}
	}

	private void processStringChildren(ApplyType apply,
			com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.StringOperator operator)
	{
		SubjectAttributeDesignatorType subjectAttribute = new SubjectAttributeDesignatorType();
		subjectAttribute.setAttributeId(operator.getAttributeName());
		JAXBElement<SubjectAttributeDesignatorType> subject = new JAXBElement<SubjectAttributeDesignatorType>(
				new QName("http://www.qut.com/middleware/lxacmlSchema", "SubjectAttributeDesignator"),
				SubjectAttributeDesignatorType.class, subjectAttribute);
		apply.getExpressions().add(subject);

		if (operator.getAttributeValues() != null)
		{
			for (String value : operator.getAttributeValues())
			{
				AttributeValueType attributeValue = new AttributeValueType();
				attributeValue.getContent().add(value);
				JAXBElement<AttributeValueType> attribute = new JAXBElement<AttributeValueType>(new QName(
						"http://www.qut.com/middleware/lxacmlSchema", "AttributeValue"), AttributeValueType.class,
						attributeValue);
				apply.getExpressions().add(attribute);

			}
		}

		if (operator.getChildren() != null)
		{
			for (com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Operator uiChild : operator
					.getChildren())
			{
				if (uiChild instanceof com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.StringLowerCase)
				{
					ApplyType child = new ApplyType();
					child.setFunctionId(this.FUNCTION_STRING_NORMALIZE_TO_LOWER);
					JAXBElement<ApplyType> element = new JAXBElement<ApplyType>(new QName(
							"http://www.qut.com/middleware/lxacmlSchema", "Apply"), ApplyType.class, child);
					apply.getExpressions().add(element);
				}
				if (uiChild instanceof com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.StringNormalizeSpace)
				{
					ApplyType child = new ApplyType();
					child.setFunctionId(this.FUNCTION_STRING_NORMALIZE_SPACE);
					JAXBElement<ApplyType> element = new JAXBElement<ApplyType>(new QName(
							"http://www.qut.com/middleware/lxacmlSchema", "Apply"), ApplyType.class, child);
					apply.getExpressions().add(element);
				}
			}
		}
	}
}

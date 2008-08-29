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
 * Author: Andre Zitelli
 * Creation Date: 23/10/2006
 * 
 * Purpose:
 */

package com.qut.middleware.esoe.pdp.processor.applyfunctions;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.saml2.schemas.esoe.lxacml.ApplyType;

public class Or 
{
	
	// Function name MUST match schema defined name 
	public static String FUNCTION_NAME = "or";
	
	Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	/** Evaluates a logical OR function against the given node. The method is recursive in nature and may
	 *  call other functions to determine the outcome based on child elements of the given node.
	 * 
	 * @param node  The node to apply this operation to. 
	 * @param principal The associated principal to use when evaluating. This is used to determine what attributes
	 * to match, if applicable.
	 * @return true IFF at least one child nodes of the given ApplyType evaluate to true, else false.
	 */
	@SuppressWarnings("unchecked")//$NON-NLS-1$
	public boolean evaluateExpression(JAXBElement<ApplyType> node, Map<String, List<String>> principalAttributes)
	{
		String function = null;

		if (node.getDeclaredType() != ApplyType.class)
		{
			throw new IllegalArgumentException("An Invalid Element was passed to And.evaluateExpression(). Only Apply Elements can be evaluated as Expressions."); //$NON-NLS-1$
		}

		JAXBElement<ApplyType> apply = node;

		function = apply.getValue().getFunctionId();

		if (function == null || !function.equals(FUNCTION_NAME))
			throw new IllegalArgumentException(MessageFormat.format("An Invalid Element was passed to And.evaluateExpression().The Apply element is not an '{0}' function.", FUNCTION_NAME)); //$NON-NLS-1$

		// process children as arguments to this function
		Iterator<JAXBElement<?>> iter = apply.getValue().getExpressions().iterator();
		while (iter.hasNext())
		{
			JAXBElement child = iter.next();

			// or function can only hold other apply types
			if (child.getDeclaredType() != ApplyType.class)
			{
				return false;
			}

			JAXBElement<ApplyType> childFunction = child;
			String childFunctionID = childFunction.getValue().getFunctionId();

			boolean result = false;

			if (childFunctionID.equals(Or.FUNCTION_NAME))
				result = new Or().evaluateExpression(child, principalAttributes);
			else
				if (childFunctionID.equals(Not.FUNCTION_NAME))
					result = new Not().evaluateExpression(child, principalAttributes);
				else
					if (childFunctionID.equals(And.FUNCTION_NAME))
						result = new And().evaluateExpression(child, principalAttributes);
					else
						if (childFunctionID.equals(StringRegex.FUNCTION_NAME))
							result = new StringRegex().evaluateExpression(child, principalAttributes);
						else
							if (childFunctionID.equals(StringEqual.FUNCTION_NAME))
								result =  new StringEqual().evaluateExpression(child, principalAttributes);

			// if any returns evaluate to true, the OR is successful
			if (result)
			{
				this.logger.debug(MessageFormat.format("Evaluation of {0} completed. Returning TRUE.", FUNCTION_NAME ) );
				return true;
			}
		}

		this.logger.debug(MessageFormat.format("Evaluation of {0} completed. Returning FALSE.", FUNCTION_NAME ) );
		return false;
	}
}


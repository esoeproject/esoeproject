package com.qut.middleware.esoe.pdp.processor.applyfunctions;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.saml2.schemas.esoe.lxacml.ApplyType;

/** This class represents a logical AND function as defined by the LXACML schema.
 * 
 * @author Andre Zitelli.
 *
 */
public class And
{
	// Function name MUST match schema defined name 
	public final static String FUNCTION_NAME = "and";
	
	Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	/** Evaluate the given expressions as a logical AND function.
	 * 
	 * @param node The ApplyType node containing the AND function to apply. Children of the given node will be
	 * evaluated recursively as applicable until no more children are found.  
	 * @param principalAttributes The attributes to pass on to any string evaluation function contained in children. 
	 * @return True IFF all child functions return True.
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

			// This function can only hold other apply types
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

			// if any returns evaluate to false, no dice
			if (!result)
			{
				this.logger.debug(MessageFormat.format("Evaluation of {0} completed. Returning FALSE.", FUNCTION_NAME ) );
				return false;
			}
		}

		this.logger.debug(MessageFormat.format("Evaluation of {0} completed. Returning TRUE.", FUNCTION_NAME ) );
		return true;
	}

}

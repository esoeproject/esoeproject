package com.qut.middleware.esoe.pdp.processor.applyfunctions;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.PatternSyntaxException;

import javax.xml.bind.JAXBElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.saml2.schemas.esoe.lxacml.ApplyType;
import com.qut.middleware.saml2.schemas.esoe.lxacml.AttributeValueType;
import com.qut.middleware.saml2.schemas.esoe.lxacml.SubjectAttributeDesignatorType;

public class StringRegex 
{
	/** Regular expression string for replacement functions */
	private final String REGEX_WHITESPACE_START = "^\\s*"; //$NON-NLS-1$
	private final String REGEX_WHITESPACE_END = "\\s*$"; //$NON-NLS-1$
	private final String REGEX_REPLACE_WITH = ""; //$NON-NLS-1$
	
	// Function name MUST match schema defined name 
	public final static String FUNCTION_NAME = "string-regex-match";
	
	Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	/** Evaluate the given expressions to determine if they are a regex match to associated Principal attribute Strings. The 
	 * SubjectAttributeDesignator contained in the string-equal ApplyType node is used to match against the attribute
	 * of the principal. For example: If the node contains SubjectAttributeDesigantor of "uid", the Principal attributes
	 * will matched on this key, then the String values therein will be compared to the ApplyType AttributeValue string
	 * foe equalness. 
	 * 
	 * @param node The ApplyType node containing the String Regex function to apply. Children of the given node MUST 
	 * only be SubjectAttributeDesigator OR AttributeValue Types OR normalize ApplyType functions. Ie: they cannot have
	 * AND, OR , NOT ApplyTypes. Nodes that contain invalid types will cause the function to return false.  
	 * @param principalAttributes The attributes to match for equality. 
	 * @return True IFF A SubjectAttributeDesigator AND AttributeValue pair is contained in the given node AND the
	 * SubjectAttributeDesignator matches a key in principalAttributes Map AND The values contained in the List of strings
	 * for that matching key contains a String that matches at least ONE of the Attribute values contained in the given node. 
	 * @throws IllegalArgumentException if the node does not contain BOTH a SubjectAttributeDesigator and AttributeValue
	 * element.
	 */
	@SuppressWarnings("unchecked")//$NON-NLS-1$
	public boolean evaluateExpression(JAXBElement<ApplyType> node, Map<String, List<String>> principalAttributes)
	{		
		String logMessage = "Evaluating regex ";
		String function = null;
		boolean toLower = false;
		boolean normalizeSpaces = false;

		// Content of AttributeValue elements
		List<String> policyAttributeValues = new Vector<String>();
		List<String> subjectDesignatorAttributes = new Vector<String>();

		if (node.getDeclaredType() != ApplyType.class)
		{
			throw new IllegalArgumentException();
		}

		JAXBElement<ApplyType> apply = node;
		function = apply.getValue().getFunctionId();

		if (function == null || !function.equals(StringRegex.FUNCTION_NAME))
			throw new IllegalArgumentException();

		// Process children as expression arguments to this function
		Iterator<JAXBElement<?>> expressions = apply.getValue().getExpressions().iterator();
		while (expressions.hasNext())
		{
			JAXBElement child = expressions.next();

			// add subject attribute designators
			if (child.getDeclaredType() == SubjectAttributeDesignatorType.class)
			{
				// set the attributes of the subject that we will use to match in the principal
				JAXBElement<SubjectAttributeDesignatorType> subj = child;
				if (subj != null && subj.getValue() != null)
					subjectDesignatorAttributes.add(subj.getValue().getAttributeId());
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
						policyAttributeValues.add(value);
					}
				}
				// We can have an ApplyType child, ONLY if it's a normalize function
				else
					if (child.getDeclaredType() == ApplyType.class)
					{
						JAXBElement<ApplyType> normalizeFunction = child;
						String childFunction = normalizeFunction.getValue().getFunctionId();

						if (childFunction.equals(StringNormalizeLower.FUNCTION_NAME))
							toLower = true;
						else
							if (childFunction.equals(StringNormalizeSpace.FUNCTION_NAME))
								normalizeSpaces = true;
							else
								throw new IllegalArgumentException(MessageFormat.format("Invalid Element. The {0} function can only contain a {1} OR {2}. Received {0}.", FUNCTION_NAME, StringNormalizeLower.FUNCTION_NAME, StringNormalizeSpace.FUNCTION_NAME, childFunction) ); //$NON-NLS-1$
					}
		}

		Iterator subjectAttributeIterator = subjectDesignatorAttributes.iterator();
		// if the Expression doesn't contain a subject attribute designator we can't match it against  principal attributes, so
		// don't bother processing it
		if (!subjectAttributeIterator.hasNext() || policyAttributeValues.isEmpty())
		{
			throw new IllegalArgumentException("Given Expression does not contain BOTH a SubjectAttributeDesignator and AttributeValue. Unable to match content." );
		}
	
		this.logger.debug("Finding matching Principal attributes ..");
		
		// for each policy specified subject attribute designator, store any identity attributes that match the designator.
		List<List<String>> matchingIdentityAttributes = new Vector<List<String>>();
		Iterator subjDesignatorIter = subjectDesignatorAttributes.iterator();
		while (subjDesignatorIter.hasNext())
		{			
			if (principalAttributes != null)
			{
				List<String> attribs = principalAttributes.get(subjDesignatorIter.next());
				if (attribs != null)
					matchingIdentityAttributes.add(attribs);
			}
			else
				this.logger.debug("Attributes for given Principal are null. Unable to match any values.");
		}

		// Attempt to match against policy defined values
		Iterator policyAttributes = policyAttributeValues.iterator();
		while (policyAttributes.hasNext())
		{
			String matcher = (String) policyAttributes.next();

			// if there were matching attributes, apply the function against them
			//Iterator<IdentityAttribute> identityAttributeIterator = identityAttributes.iterator();
			Iterator<List<String>> matchingAttributes = matchingIdentityAttributes.iterator();
			
			// If the principal's identity attributes did not match any requested attributes as
			// specified by the policy, format our message accordingly.
			if (!matchingAttributes.hasNext())
			{
				logMessage = "{null.matches(" + matcher + ")}"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			
			while (matchingAttributes.hasNext())
			{
				// Iterate through values of attributes
				List<String>attribute = matchingAttributes.next();
				
				// perform any string normalization required
				if (toLower)
					attribute = new StringNormalizeLower().evaluateExpression(attribute);

				if (normalizeSpaces)
				{
					attribute = new StringNormalizeSpace().evaluateExpression(attribute);				
				}
				
				Iterator attributeValueIterator = attribute.iterator();

				// if no attribute values for the slected attribute, format our message accordingly
				if (!attributeValueIterator.hasNext())
				{
					logMessage = "{null.equals(" + matcher + ")}"; //$NON-NLS-1$ //$NON-NLS-2$
				}
				while (attributeValueIterator.hasNext())
				{
					String attrValue = attributeValueIterator.next().toString();

					try
					{						
						logMessage += "{" + attrValue + ".matches(" + matcher + ")}" ;
						
						if (attrValue.matches(matcher))
						{
							this.logger.debug(logMessage + ". Returning TRUE."); //$NON-NLS-1$
							return true;
						}
					}
					catch (PatternSyntaxException e)
					{
						this.logger.warn("Invalid regex found in content for expression" + function + "."); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			}
		}

		this.logger.debug(logMessage + ". Returning FALSE."); //$NON-NLS-1$
		return false;
	}

}

package com.qut.middleware.esoe.pdp.processor.applyfunctions;

import java.util.List;
import java.util.Vector;

public class StringNormalizeSpace 
{
	
	private final String REGEX_WHITESPACE_START = "^\\s*"; //$NON-NLS-1$
	private final String REGEX_WHITESPACE_END = "\\s*$"; //$NON-NLS-1$
	private final String REGEX_REPLACE_WITH = ""; //$NON-NLS-1$
	
	public final static String FUNCTION_NAME = "string-normalize-space";
	
	public List<String> evaluateExpression(List<String> values)
	{
		List<String> newValues = new Vector<String>();
		
		for(String value: values)
		{
			if(value != null)
			{
				String newValue =value.replaceAll(this.REGEX_WHITESPACE_START, this.REGEX_REPLACE_WITH);
				newValue = value.replaceAll(this.REGEX_WHITESPACE_END, this.REGEX_REPLACE_WITH);				
				newValues.add(newValue);
			}
		}
	
		return newValues;
	}
	
}

package com.qut.middleware.esoe.pdp.processor.applyfunctions;

import java.util.List;
import java.util.Vector;

public class StringNormalizeLower
{
	// Function name MUST match schema defined name 
	public final static String FUNCTION_NAME = "string-normalize-to-lower-case";
	
	public List<String> evaluateExpression(List<String> values)
	{
		List<String> newValues = new Vector<String>();
		
		for(String value: values)
		{
			if(value != null)
				newValues.add(value.toLowerCase());
		}
	
		return newValues;
	}
}


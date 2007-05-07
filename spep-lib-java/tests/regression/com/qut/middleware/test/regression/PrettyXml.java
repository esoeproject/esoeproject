package com.qut.middleware.test.regression;


public class PrettyXml
{
	private String prefix;

	public PrettyXml(String prefix)
	{
		this.prefix = prefix;
	}
	
	public String makePretty(String xml)
	{
		if(xml == null) return null;
		xml = xml.replace("\r", "");
		xml = xml.replace("\n", "");
		StringBuffer buffer = new StringBuffer();
		boolean newLined = true;
		int prefixes = 0;
		buffer.append(xml.charAt(0));
		if(xml.charAt(0) == '<') prefixes++;
		
		for (int i=1; i<xml.length()-1; i++)
		{
			char lastChar = xml.charAt(i-1);
			char currentChar = xml.charAt(i);
			char nextChar = xml.charAt(i+1);
			if (currentChar == '<')
			{
				if (!newLined) buffer.append('\n');

				if (nextChar == '/')
				{
					prefixes--;
					for(int j=0; j<prefixes; j++) buffer.append(this.prefix);
				}
				else
				{
					for(int j=0; j<prefixes; j++) buffer.append(this.prefix);
					prefixes++;
				}
				
				buffer.append(currentChar);
				newLined = false;
			}
			else if (currentChar == '>')
			{
				if (lastChar == '/') prefixes--;
				buffer.append(currentChar);
				buffer.append('\n');
				newLined = true;
				if (nextChar != '<') for(int j=0; j<prefixes; j++) buffer.append(this.prefix);
			}
			else
			{
				buffer.append(currentChar);
				newLined = false;
			}
		}
		buffer.append(xml.charAt(xml.length()-1));
		
		return buffer.toString();
	}
}

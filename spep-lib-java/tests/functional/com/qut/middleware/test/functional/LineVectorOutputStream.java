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
 * Author: Shaun Mangelsdorf
 * Creation Date: 13/11/2006
 * 
 * Purpose: Output stream that splits lines and stores them in a list.
 */
package com.qut.middleware.test.functional;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Vector;


/** */
public class LineVectorOutputStream extends OutputStream
{
	private List<String> lines = new Vector<String>();
	private CharArrayWriter charArrayWriter;
	
	/**
	 * Default constructor.
	 */
	public LineVectorOutputStream()
	{
		this.charArrayWriter = new CharArrayWriter();
	}

	/**
	 * @return The lines that were written to this stream.
	 */
	public List<String> getLines()
	{
		return this.lines;
	}
	
	@Override
	public void write(int b) throws IOException
	{
		this.charArrayWriter.write(b);
		
		if(b == '\n' || b == '\r')
		{
			char[] array = this.charArrayWriter.toCharArray();
			String line = new String(array, 0, array.length - 1);
				
			if(line.length() > 0)
			{
				this.lines.add(line);
			}
			
			this.charArrayWriter.reset();
		}
	}
}
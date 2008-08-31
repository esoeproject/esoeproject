/* Copyright 2006, Queensland University of Technology
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
 * Creation Date: 26/08/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.esoe.authn.plugins.spnego.bean.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.qut.middleware.esoe.authn.plugins.spnego.bean.CommaSeparatedStringList;

public class CommaSeparatedStringListImpl implements CommaSeparatedStringList
{
	private List<String> strings;
	
	public CommaSeparatedStringListImpl(String list)
	{
		this.strings = new ArrayList<String>();
		
		// Split at commas and trim whitespace to build the list.
		for (String str : list.split(","))
		{
			this.strings.add(str.trim());
		}
	}

	// Auto-generated delegate methods, courtesy of Eclipse.
	
	public void add(int arg0, String arg1)
	{
		this.strings.add(arg0, arg1);
	}

	public boolean add(String arg0)
	{
		return this.strings.add(arg0);
	}

	public boolean addAll(Collection<? extends String> arg0)
	{
		return this.strings.addAll(arg0);
	}

	public boolean addAll(int arg0, Collection<? extends String> arg1)
	{
		return this.strings.addAll(arg0, arg1);
	}

	public void clear()
	{
		this.strings.clear();
	}

	public boolean contains(Object arg0)
	{
		return this.strings.contains(arg0);
	}

	public boolean containsAll(Collection<?> arg0)
	{
		return this.strings.containsAll(arg0);
	}

	public boolean equals(Object arg0)
	{
		return this.strings.equals(arg0);
	}

	public String get(int arg0)
	{
		return this.strings.get(arg0);
	}

	public int hashCode()
	{
		return this.strings.hashCode();
	}

	public int indexOf(Object arg0)
	{
		return this.strings.indexOf(arg0);
	}

	public boolean isEmpty()
	{
		return this.strings.isEmpty();
	}

	public Iterator<String> iterator()
	{
		return this.strings.iterator();
	}

	public int lastIndexOf(Object arg0)
	{
		return this.strings.lastIndexOf(arg0);
	}

	public ListIterator<String> listIterator()
	{
		return this.strings.listIterator();
	}

	public ListIterator<String> listIterator(int arg0)
	{
		return this.strings.listIterator(arg0);
	}

	public String remove(int arg0)
	{
		return this.strings.remove(arg0);
	}

	public boolean remove(Object arg0)
	{
		return this.strings.remove(arg0);
	}

	public boolean removeAll(Collection<?> arg0)
	{
		return this.strings.removeAll(arg0);
	}

	public boolean retainAll(Collection<?> arg0)
	{
		return this.strings.retainAll(arg0);
	}

	public String set(int arg0, String arg1)
	{
		return this.strings.set(arg0, arg1);
	}

	public int size()
	{
		return this.strings.size();
	}

	public List<String> subList(int arg0, int arg1)
	{
		return this.strings.subList(arg0, arg1);
	}

	public Object[] toArray()
	{
		return this.strings.toArray();
	}

	public <T> T[] toArray(T[] arg0)
	{
		return this.strings.toArray(arg0);
	}
}

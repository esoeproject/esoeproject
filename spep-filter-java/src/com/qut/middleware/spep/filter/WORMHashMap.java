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
 * Creation Date: 03/01/2007
 * 
 * Purpose: Provides a hashmap implementation that can be closed for writing once
 * 		it has been populated.
 */
package com.qut.middleware.spep.filter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/** Provides a hashmap implementation that can be closed for writing once
 * 		it has been populated.
 * @param <K> Data type for hashmap Key 
 * @param <V> Data type for hashmap Value
 */
public final class WORMHashMap<K,V> extends HashMap<K,V>
{
	private static final long serialVersionUID = 7559202641704380483L;
	private boolean closed;
	
	/**
	 * Default constructor.
	 */
	public WORMHashMap()
	{
		super();
		
		this.closed = false;
	}
	
	protected void close()
	{
		this.closed = true;
	}

	private void checkClosed()
	{
		if (this.closed) throw new UnsupportedOperationException("Cannot modify this Map when it has been closed."); //$NON-NLS-1$
	}

	@Override
	public V put(K arg0, V arg1)
	{
		checkClosed();
		return super.put(arg0, arg1);
	}
	
	@Override
	public void putAll(Map<? extends K, ? extends V> m)
	{
		checkClosed();
		super.putAll(m);
	}
	
	@Override
	public V remove(Object key)
	{
		checkClosed();
		return super.remove(key);
	}
	
	@Override
	public void clear()
	{
		checkClosed();
		super.clear();
	}
	
	@Override
	public Set<Entry<K, V>> entrySet()
	{
		Set<Entry<K, V>> entrySet = super.entrySet();
		
		if (this.closed)
		{
			entrySet = new LockedSet<Entry<K, V>>(entrySet);
		}
		
		return entrySet;
	}
	
	@Override
	public Set<K> keySet()
	{
		Set<K> keySet = super.keySet();
		
		if (this.closed)
		{
			keySet = new LockedSet<K>(keySet);
		}
		
		return keySet;
	}
	
	@Override
	public Collection<V> values()
	{
		Collection<V> values = super.values();
		
		if (this.closed)
		{
			values = new LockedSet<V>(values);
		}
		
		return values;
	}
	
	private class LockedSet<E> implements Set<E>
	{
		private Collection<E> set;
		
		protected LockedSet(Collection<E> set)
		{
			this.set = set;
		}
		
		public int size()
		{
			return this.set.size();
		}

		public boolean contains(Object arg0)
		{
			return this.set.contains(arg0);
		}

		public boolean containsAll(Collection<?> arg0)
		{
			return this.set.containsAll(arg0);
		}

		public boolean isEmpty()
		{
			return this.set.isEmpty();
		}

		public Iterator<E> iterator()
		{
			return new LockedIterator<E>(this.set.iterator());
		}

		public Object[] toArray()
		{
			return this.set.toArray();
		}

		public <T> T[] toArray(T[] arg0)
		{
			return this.set.toArray(arg0);
		}
		
		
		/* Private method to throw an exception */
		
		private void attemptedWrite()
		{
			throw new UnsupportedOperationException("Cannot modify this Set because it is locked."); //$NON-NLS-1$
		}
		
		/* Operations that modify the Set - these all throw UnsupportedOperationException */
		
		public boolean add(E arg0)
		{
			attemptedWrite(); // Will never return
			return false;
		}

		public boolean addAll(Collection<? extends E> arg0)
		{
			attemptedWrite(); // Will never return
			return false;
		}

		public void clear()
		{
			attemptedWrite(); // Will never return
		}

		public boolean remove(Object arg0)
		{
			attemptedWrite(); // Will never return
			return false;
		}

		public boolean removeAll(Collection<?> arg0)
		{
			attemptedWrite(); // Will never return
			return false;
		}

		public boolean retainAll(Collection<?> arg0)
		{
			attemptedWrite(); // Will never return
			return false;
		}

		@SuppressWarnings("hiding")
		private class LockedIterator<E> implements Iterator<E>
		{
			private Iterator<E> iterator;

			protected LockedIterator(Iterator<E> iterator)
			{
				this.iterator = iterator;
			}

			public boolean hasNext()
			{
				return this.iterator.hasNext();
			}

			public E next()
			{
				return this.iterator.next();
			}

			public void remove()
			{
				throw new UnsupportedOperationException("Cannot remove from this Iterator because it is locked."); //$NON-NLS-1$
			}
		}
	}
}

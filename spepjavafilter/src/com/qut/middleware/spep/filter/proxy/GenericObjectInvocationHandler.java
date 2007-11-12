/* 
 * Copyright 2007, Queensland University of Technology
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
 * Creation Date: Oct 4, 2007
 * 
 * Purpose: 
 */

package com.qut.middleware.spep.filter.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * 
 */
public class GenericObjectInvocationHandler extends Object implements
		InvocationHandler
{
	private Logger logger = Logger.getLogger(GenericObjectInvocationHandler.class);
	
	private Object invocationTarget;
	
	/**
	 * Constructor specifying an invocation target. Target object must not be null
	 * @param invocationTarget
	 */
	public GenericObjectInvocationHandler( Object invocationTarget )
	{		
		if( invocationTarget == null )
		{
			throw new IllegalArgumentException( "Cannot create an invocation handler for a null object." );
		}
		
		this.invocationTarget = invocationTarget;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
	 *      java.lang.reflect.Method, java.lang.Object[])
	 */
	public Object invoke(Object proxyObject, Method method, Object[] localArgs)
			throws Throwable
	{
		
		this.logger.debug( "Calling " + method.getDeclaringClass().getName() + " method " + method.getName() + "(" + method.getParameterTypes().length + " args). Target type is " + this.invocationTarget.getClass().getName() );
		
		ClassLoader remoteClassLoader = this.invocationTarget.getClass().getClassLoader();
		
		// Build list of the parameters expected on the remote side so we can resolve the method.
		Class<?>[] parameters = new Class<?>[ method.getParameterTypes().length ];
		for( int i=0; i<method.getParameterTypes().length; ++i )
		{
			String name = method.getParameterTypes()[i].getName();
			parameters[i] = remoteClassLoader.loadClass( name );
		}
		
		// Resolve the method on the remote object
		Method targetMethod = this.invocationTarget.getClass().getMethod( method.getName(), parameters );
		
		// Build reverse proxies to the object we are using as parameters, so that the
		// remote class can handle our types cleanly.
		Object[] args = null;
		if( localArgs != null )
		{
			args = localArgs.clone();
			for( int i=0; i<args.length; ++i )
			{
				args[i] = this.reverseAutoProxy( args[i], remoteClassLoader );
			}
		}
		
		// Invoke the method with the proxied args
		Object retval = targetMethod.invoke( this.invocationTarget, args );

		if( retval == null )
		{
			this.logger.debug( "Returned type is null." );
		}
		else
		{
			this.logger.debug( "Returned type is " + retval.getClass().getName() + ". Trying to auto-proxy" );
		}
		
		// Auto-proxy the object if we can, and return.
		return this.forwardAutoProxy( retval );

	}
	
	/**
	 * Sets up a proxy object "from" local interface(s) "to" the remote target object.
	 * @param x
	 * @return
	 */
	private Object forwardAutoProxy( Object target )
	{
		// Use the class loader for this class to load the proxy interfaces
		return autoProxy( target, this.getClass().getClassLoader() );
	}
	
	/**
	 * Sets up a proxy object "from" remote interface(s) "to" the local target object
	 * @param target
	 * @param remoteClassLoader
	 * @return
	 */
	private Object reverseAutoProxy( Object target, ClassLoader remoteClassLoader )
	{
		return autoProxy( target, remoteClassLoader );
	}
	
	/**
	 * Creates an auto proxy for the object given, using the given class loader to
	 * resolve interfaces. The meaning of an auto proxy here is:
	 * - If the target object is null or a primitive type, the auto proxy is 
	 * 		the object itself
	 * - If the target object's class is loaded locally, the auto proxy is the 
	 * 		object itself
	 * - If the target object is an enum, and the enum is defined locally, the 
	 * 		auto proxy is the enum constant with the same value as the object 
	 * 		had originally.
	 * - If the target object has interfaces that can be resolved locally, a 
	 * 		proxy object is created that implements as many of those interfaces
	 * 		as possible.
	 * - Failing all these avenues, the object itself is returned.
	 * @param target The target object to proxy for.
	 * @param targetClassLoader The class loader to use when resolving interfaces if a proxy
	 * 		object needs to be created.
	 * @return The auto proxy for the target object.
	 */
	private Object autoProxy( Object target, ClassLoader targetClassLoader )
	{
		if( target == null || target.getClass().isPrimitive() )
		{
			this.logger.debug( "Not proxying a primitive object (or null). Returning the object." );
			return target;
		}
		
		if( targetClassLoader == null )
		{
			this.logger.debug( "Null classloader given for type " + target.getClass() + " .. trying system classloader" );
			targetClassLoader = ClassLoader.getSystemClassLoader();
		}
		
		try 
		{
			// If we can cast the object to a local type of the exact same name,
			// we don't need to proxy it.
			Class<?> clazz = targetClassLoader.loadClass( target.getClass().getName() );
			if( clazz != null )
			{
				Object retval = clazz.cast( target );
				
				String classLoaderName = "couldn't get classloader name";
				if( clazz.getClassLoader() != null )
				{
					if( clazz.getClassLoader().getClass() != null )
					{
						classLoaderName = clazz.getClassLoader().getClass().getName();
					}
				}
				
				this.logger.debug( "Resolved class to local class name: " + clazz.getName() + ". Class loader: " + classLoaderName );
				return retval;
			}
		} 
		catch (ClassNotFoundException e) 
		{
			// Otherwise, we need to proxy it.
		}
		catch (ClassCastException e)
		{
			// Obviously a different class loader.
		}
		
		if( target.getClass().isEnum() )
		{
			/* Special case: proxying for an enum.
			 * We need to have an enum of the same name locally, and use the value
			 * to resolve it to a local value, because you can't cast a proxy
			 * to an enum value. Ech!
			 */
			try 
			{
				Class<?> clazz = targetClassLoader.loadClass( target.getClass().getName() );
				if( !clazz.isEnum() )
				{
					this.logger.error( "Target class: " + clazz.getName() + " is not an enum, but original class: " + target.getClass().getName() + " is. Returning original object. This will probably cause a ClassCastException." );
					return target;
				}
				
				Class<Enum> enumClass = (Class<Enum>)clazz;
				
				// So when we get to here, we have an enum class to target. Now we just need to get the value.
				if( target instanceof Enum )
				{
					Enum<?> e = (Enum<?>)target;
					return Enum.valueOf( enumClass, e.name() );
				}
			} 
			catch (ClassNotFoundException e) 
			{
				this.logger.error( "Target class for enum " + target.getClass().getName() + " not found... Returning original object. This will probably cause a ClassCastException." );
				return target;
			}
		}
		
		if( target.getClass().isArray() )
		{
			throw new UnsupportedOperationException( "No array proxy yet" );
		}
		
		// All other methods failed. We need to build a proxy to access this object.
		this.logger.debug( "Couldn't resolve " + target.getClass().getName() + " to a local class. Going to build a proxy" );
				
		List<Class<?>> targetInterfaces = buildInterfaceList( target.getClass() );
		List<Class<?>> proxyInterfaces = new Vector<Class<?>>();
		
		for( Class<?> clazz : targetInterfaces )
		{
			try
			{
				proxyInterfaces.add( targetClassLoader.loadClass( clazz.getName() ) );
				this.logger.trace( "Added interface " + clazz.getName() + " for proxy" );
			}
			catch( ClassNotFoundException e )
			{
				// Class not found remotely. Just means we can't proxy it.
			}
		}
		
		if( proxyInterfaces.size() > 0 )
		{
			this.logger.debug( "Auto-proxying for type " + target.getClass().getName() + ". " + proxyInterfaces.size() + " matching interfaces." );
			
			// We got at least 1 interface on the class that can be proxied. Auto-proxy it.
			return Proxy.newProxyInstance( this.getClass().getClassLoader(), proxyInterfaces.toArray(new Class<?>[]{}), new GenericObjectInvocationHandler( target ) );
		}
		else
		{
			this.logger.debug( "Not auto-proxying for type " + target.getClass().getName() );
			
			return target;
		}
	}
	
	/**
	 * Builds a list of interfaces implemented by the target class, its parent 
	 * class(es) and any interfaces implemented by these interfaces.
	 * @param targetClass
	 * @return
	 */
	private List<Class<?>> buildInterfaceList( Class<?> targetClass )
	{
		List<Class<?>> interfaces = new Vector<Class<?>>();
		
		// No support for arrays here.
		if( targetClass.isArray() || targetClass.isEnum() )
		{
			throw new UnsupportedOperationException( "Request for interface list for Array or Enum. This is a bug in implementation, and should never occur." );
		}
		
		// Recurse to build the list of interfaces
		buildInterfaceListRecurse( interfaces, targetClass );
		
		return interfaces;
	}
	
	private void buildInterfaceListRecurse( List<Class<?>> list, Class<?> clazz )
	{
		// If it's an interface, add it
		if( clazz.isInterface() )
		{
			list.add( clazz );
		}
		// If it's not an interface, it might have a parent class. Recurse to that.
		else if( clazz.getSuperclass() != null )
		{
			buildInterfaceListRecurse( list, clazz.getSuperclass() );
		}
		
		// Recurse to interfaces implemented by this class/interface.
		for( Class<?> interfayse : clazz.getInterfaces() )
		{
			buildInterfaceListRecurse( list, interfayse );
		}
	}
	
}

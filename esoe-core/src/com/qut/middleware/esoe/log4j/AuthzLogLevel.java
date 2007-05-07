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
 * Author: Bradley Beddoes
 * Creation Date: 21/12/2006
 * 
 * Purpose: Defines the Authz logging level for log4j to publish events relating to Authz
 */
package com.qut.middleware.esoe.log4j;

import org.apache.log4j.Level;

public class AuthzLogLevel extends Level
{
	private static final long serialVersionUID = -8851843153652403317L;

	private static final String ID = "Authz"; //$NON-NLS-1$

	/**
	 * Value of Authz level.
	 */
	public static final int Authz_INT = INFO_INT + 101;

	/**
	 * level representing Authz
	 */
	public static final Level Authz = new AuthzLogLevel(Authz_INT, ID, 6);

	/**
	 * Checks if level is Authz_INT if yes then returns Authz else calls toLevel
	 * 
	 * @param val the current level
	 */
	public static Level toLevel(int val)
	{
		if (val == Authz_INT)
		{
			return Authz;
		}
		return toLevel(val, Level.INFO);
	}

	/**
	 * Checks if level is Authz_INT if yes then returns Authz else calls toLevel
	 * 
	 * @override
	 */
	public static Level toLevel(int val, Level defaultLevel)
	{
		if (val == Authz_INT)
		{
			return Authz;
		}
		return Level.toLevel(val, defaultLevel);
	}

	/**
	 * Checks if level is "Authz" level, if yes then returns Authz else calls toLevel
	 * 
	 * @override
	 */
	public static Level toLevel(String level)
	{
		if (level != null && level.toUpperCase().equals(ID))
		{
			return Authz;
		}
		return toLevel(level, Level.INFO);
	}

	/**
	 * Checks if level is "Authz" level, if yes then returns Authz else calls toLevel
	 * 
	 * @param level the current level
	 */
	public static Level toLevel(String level, Level defaultLevel)
	{
		if (level != null && level.toUpperCase().equals(ID))
		{
			return Authz;
		}
		return Level.toLevel(level, defaultLevel);
	}

	/**
	 * Constructor
	 * @param level integer value of this logging level
	 * @param name name of this logging level
	 * @param syslogequiv syslog value of this level
	 */
	protected AuthzLogLevel(int level, String name, int syslogequiv)
	{
		super(level, name, syslogequiv);
	}
}
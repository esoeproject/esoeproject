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
 * Purpose: Defines the AUTHN logging level for log4j to publish events relating to AUTHN
 */
package com.qut.middleware.spep.log4j;

import org.apache.log4j.Level;

public class AuthnLogLevel extends Level
{
	private static final long serialVersionUID = -8851843153652403317L;

	private static final String ID = "AUTHN"; //$NON-NLS-1$

	/**
	 * Value of Authn level.
	 */
	public static final int Authn_INT = INFO_INT + 100;

	/**
	 * level representing Authn
	 */
	public static final Level AUTHN = new AuthnLogLevel(Authn_INT, ID, 6);

	/**
	 * Checks if level is Authn_INT if yes then returns Authn else calls toLevel
	 * 
	 * @param val the current level
	 */
	public static Level toLevel(int val)
	{
		if (val == Authn_INT)
		{
			return AUTHN;
		}
		return toLevel(val, Level.INFO);
	}

	/**
	 * Checks if level is Authn_INT if yes then returns Authn else calls toLevel
	 * 
	 * @param val value to set 
	 * @param defaultLevel default log level to set
	 */
	public static Level toLevel(int val, Level defaultLevel)
	{
		if (val == Authn_INT)
		{
			return AUTHN;
		}
		return Level.toLevel(val, defaultLevel);
	}

	/**
	 * Checks if level is "Authn" level, if yes then returns Authn else calls toLevel
	 * 
	 * @param level the current level
	 */
	public static Level toLevel(String level)
	{
		if (level != null && level.toUpperCase().equals(ID))
		{
			return AUTHN;
		}
		return toLevel(level, Level.INFO);
	}

	/**
	 * Checks if level is "Authn" level, if yes then returns Authn else calls toLevel
	 * 
	 * @param level the current level
	 */
	public static Level toLevel(String level, Level defaultLevel)
	{
		if (level != null && level.toUpperCase().equals(ID))
		{
			return AUTHN;
		}
		return Level.toLevel(level, defaultLevel);
	}

	/**
	 * Constructor
	 * @param level integer value of this logging level
	 * @param name name of this logging level
	 * @param syslogequiv syslog value of this level
	 */
	protected AuthnLogLevel(int level, String name, int syslogequiv)
	{
		super(level, name, syslogequiv);
	}
}
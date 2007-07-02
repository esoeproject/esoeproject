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
 * Purpose: Defines the INSANE logging level for log4j to publish streams of data such as full SAML requests etc
 */
package com.qut.middleware.esoe.log4j;

import org.apache.log4j.Level;

public class InsaneLogLevel extends Level
{
	private static final long serialVersionUID = -8851843153652403317L;

	private static final String ID = "INSANE"; //$NON-NLS-1$

	/**
	 * Value of INSANE level.
	 */
	public static final int INSANE_INT = DEBUG_INT - 100;

	/**
	 * level representing insane
	 */
	public static final Level INSANE = new InsaneLogLevel(ALL_INT, ID, 9);

	/**
	 * Checks if level is INSANE_INT if yes then returns INSANE else calls toLevel
	 * 
	 * @param level the current level
	 */
	public static Level toLevel(int val)
	{
		if (val == INSANE_INT)
		{
			return INSANE;
		}
		return toLevel(val, Level.DEBUG);
	}

	/**
	 * Checks if level is INSANE_INT if yes then returns INSANE else calls toLevel
	 * 
	 * @param level the current level
	 */
	public static Level toLevel(int val, Level defaultLevel)
	{
		if (val == INSANE_INT)
		{
			return INSANE;
		}
		return Level.toLevel(val, defaultLevel);
	}

	/**
	 * Checks if level is "INSANE" level, if yes then returns INSANE else calls toLevel
	 * 
	 * @param level the current level
	 */
	public static Level toLevel(String level)
	{
		if (level != null && level.toUpperCase().equals(ID))
		{
			return INSANE;
		}
		return toLevel(level, Level.DEBUG);
	}

	/**
	 * Checks if level is "INSANE" level, if yes then returns INSANE else calls toLevel
	 * 
	 * @param level the current level
	 */
	public static Level toLevel(String level, Level defaultLevel)
	{
		if (level != null && level.toUpperCase().equals(ID))
		{
			return INSANE;
		}
		return Level.toLevel(level, defaultLevel);
	}

	/**
	 * Constructor
	 * @param level integer value of this logging level
	 * @param name name of this logging level
	 * @param syslogequiv syslog value of this level
	 */
	protected InsaneLogLevel(int level, String name, int syslogequiv)
	{
		super(level, name, syslogequiv);
	}
}

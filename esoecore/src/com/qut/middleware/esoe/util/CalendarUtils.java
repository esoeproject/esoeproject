package com.qut.middleware.esoe.util;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.qut.middleware.esoe.ConfigurationConstants;

public class CalendarUtils 
{
	
	
	/**
	 * Generates an XML gregorian calendar instance based on 0 offset UTC current time.
	 * 
	 * @return The created calendar for the current UTC time, else null if an error
	 * occurs creating the calendar.
	 */
	public static XMLGregorianCalendar generateXMLCalendar()
	{
		GregorianCalendar calendar;
				
		SimpleTimeZone tz = new SimpleTimeZone(0, ConfigurationConstants.timeZone);
		calendar = new GregorianCalendar(tz);
		
		try
		{
			DatatypeFactory factory = DatatypeFactory.newInstance();		
			return factory.newXMLGregorianCalendar(calendar);
		}
		catch(DatatypeConfigurationException e)
		{
			return null;
		}
	}
	
	
	/**
	 * Generates an XML gregorian calendar instance based on 0 offset UTC current time.
	 * 
	 * @param offset
	 *            The offset from UTC+0, in seconds.
	 * @return The created calendar for the current time + offset, else null if an error
	 * occurs creating the calendar.
	 */
	public static XMLGregorianCalendar generateXMLCalendar(int offset)
	{
		GregorianCalendar calendar;
				
		SimpleTimeZone tz = new SimpleTimeZone(0, ConfigurationConstants.timeZone);
		calendar = new GregorianCalendar(tz);
		calendar.add(Calendar.SECOND, offset);
		try
		{
			DatatypeFactory factory = DatatypeFactory.newInstance();		
			return factory.newXMLGregorianCalendar(calendar);
		}
		catch(DatatypeConfigurationException e)
		{
			return null;
		}
	}
	
	
	/**
	 * Generates an XML gregorian calendar instance based on 0 offset UTC current time.
	 * 
	 * @param offset
	 *            The offset from UTC+0, in the given incremental unit.
	 * @param increment The incremental unit to use when adding the given offset to
	 * the created calendar. Eg Calendar.MILLISECOND. See Calendar.x for valid fields.
	 * @return The created calendar for the current time + offset, else null if an error
	 * 
	 * occurs creating the calendar.
	 */
	public static XMLGregorianCalendar generateXMLCalendar(int offset, int increment)
	{
		GregorianCalendar calendar;
				
		SimpleTimeZone tz = new SimpleTimeZone(0, ConfigurationConstants.timeZone);
		calendar = new GregorianCalendar(tz);
		calendar.add(increment, offset);
		
		try
		{
			DatatypeFactory factory = DatatypeFactory.newInstance();		
			return factory.newXMLGregorianCalendar(calendar);
		}
		catch(DatatypeConfigurationException e)
		{
			return null;
		}
	}

	
	/**
	 * Generates an XML gregorian calendar instance using the given millisecond value.
	 * 
	 * @param offset
	 *            The offset from UTC+0, in the given incremental unit.
	 * @param increment The incremental unit to use when adding the given offset to
	 * the created calendar. Eg Calendar.MILLISECOND. See Calendar.x for valid fields.
	 * @return The created calendar for the current time + offset, else null if an error
	 * 
	 * occurs creating the calendar.
	 */
	public static XMLGregorianCalendar generateXMLCalendar(long millis)
	{
		GregorianCalendar calendar;
				
		SimpleTimeZone tz = new SimpleTimeZone(0, ConfigurationConstants.timeZone);
		calendar = new GregorianCalendar(tz);
		calendar.setTimeInMillis(millis);
		
		try
		{
			DatatypeFactory factory = DatatypeFactory.newInstance();		
			return factory.newXMLGregorianCalendar(calendar);
		}
		catch(DatatypeConfigurationException e)
		{
			return null;
		}
	}
}

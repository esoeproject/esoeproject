/* Copyright 2008, Queensland University of Technology
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
 * Creation Date: 29/09/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.spep.ws.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.spep.Initializer;
import com.qut.middleware.spep.SPEP;
import com.qut.middleware.spep.exception.SPEPInitializationException;
import com.qut.middleware.spep.ws.WSProcessor;
import com.qut.middleware.spep.ws.exception.WSProcessorException;

public class WSServlet extends HttpServlet
{
	public static final String WS_PROCESSOR = "wsProcessor";
	private static final int BUF_SIZE = 1024;
	private static final long serialVersionUID = -3677982616252086171L;
	private WSProcessor wsProcessor;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private Map<String, Method> wsProcessorMethods;
	
	@Override
	public void init() throws ServletException
	{
		Set<Class<?>> expectedExceptions = new TreeSet<Class<?>>(new Comparator<Class<?>>(){
			public int compare(Class<?> o1, Class<?> o2)
			{
				return o1.getName().compareTo(o2.getName());
			}
		});
		
		Class<?>[] expectedParameters;
		Class<?> expectedReturn;
		
		this.wsProcessorMethods = new TreeMap<String, Method>();
		
		// *** If the WSProcessor method signatures change, this is the place to update it. ***
		// *** Also in the doPost method if you have changed the return type ***
		expectedParameters = new Class<?>[]{byte[].class, String.class};
		expectedReturn = byte[].class;
		expectedExceptions.add(WSProcessorException.class);
		// *** The rest of the code below should be ok ***
		
		SPEP spep;
		try
		{
			spep = Initializer.init(this.getServletContext());
		}
		catch (SPEPInitializationException e)
		{
			this.logger.debug("SPEP intialization failed. Unable to retrieve WS processor to intialize WS servlet", e);
			throw new ServletException("SPEP initialization failed. Unable to retrieve WS processor to intialize WS servlet. Error was: " + e.getMessage());
		}
		
		this.wsProcessor = spep.getWSProcessor();
		
		if (this.wsProcessor == null)
		{
			this.logger.debug("WS Processor from SPEP was null. No exception was thrown while retrieving from the SPEP, but a null value was returned.");
			throw new ServletException("WS Processor from SPEP was null. Check config to ensure that this is defined correctly.");
		}
		
		Method[] wsProcessorMethods = WSProcessor.class.getMethods();
		for (Method method : wsProcessorMethods)
		{
			Class<?>[] methodParameters = method.getParameterTypes();
			boolean reject = false;
			
			// Check for the correct number of parameters.
			if (methodParameters.length != expectedParameters.length)
			{
				this.logger.debug("Rejecting {} as WS method due to incorrect number of parameters. Got {} but expected {}.",
						new Object[]{method.getName(), methodParameters.length, expectedParameters.length}
				);
				
				continue;
			}
			
			// Check the type of each parameter.
			for (int i=0; i<expectedParameters.length; ++i)
			{
				if (!methodParameters[i].equals(expectedParameters[i]))
				{
					this.logger.debug("Rejecting {} as WS method due to invalid parameter type for parameter {}. Got {} but expected {}.",
							new Object[]{method.getName(), i, methodParameters[i], expectedParameters[i]}
					);
					
					reject = true;
				}
			}
			
			Class<?>[] methodExceptions = method.getExceptionTypes();
			// Check the declared exceptions to make sure they are expected.
			for (Class<?> exceptionType : methodExceptions)
			{
				if (!expectedExceptions.contains(exceptionType))
				{
					this.logger.debug("Rejecting {} as WS method due to unexpected exception type. Type was {} but this was not in the set of expected types.",
							new Object[]{method.getName(), exceptionType.getName()}
					);
					
					reject = true;
				}
			}
			
			if (!method.getReturnType().equals(expectedReturn))
			{
				this.logger.debug("Rejecting {} as WS method due to incorrect return type. Got {} but expected {}", 
						new Object[]{method.getName(), method.getReturnType().getName(), expectedReturn.getName()}
				);
			}
			
			// If we did not reject the method, add it to the method map.
			if (!reject)
			{
				this.logger.info("Publishing web service endpoint: {}", method.getName());
				this.wsProcessorMethods.put(method.getName(), method);
			}
		}
		
		this.logger.info("WSServlet initialized. {} endpoints published.", this.wsProcessorMethods.size());
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		InputStream requestInputStream = req.getInputStream();
		byte[] buf = new byte[BUF_SIZE];
		int count;
		while ((count = requestInputStream.read(buf)) > 0)
		{
			byteArrayOutputStream.write(buf, 0, count);
		}
		
		byte[] document = byteArrayOutputStream.toByteArray();
		
		this.logger.debug("Processing request URI {}", req.getRequestURI());

		// Split the URI by / to get the last component.
		String[] uriParts = req.getRequestURI().split("/");
		if (uriParts.length == 0)
		{
			// This message would never hit a user's browser under normal flow, so just report the error as it lies.
			throw new ServletException("Request URI could not be mapped to an endpoint as there were 0 URI parts.");
		}
		
		// Grab the last part of the URI to use as the request method.
		String wsMethodName = uriParts[uriParts.length - 1];
		if (wsMethodName == null || wsMethodName.length() == 0)
		{
			throw new ServletException("Request URI could not be mapped to an endpoint as the last component had 0 length.");
		}
		
		this.logger.debug("URI {} split into {} parts.. Finding method for endpoint {}",
				new Object[]{req.getRequestURI(), uriParts.length, wsMethodName}
		);

		Method wsMethod = this.wsProcessorMethods.get(wsMethodName);
		if (wsMethod == null)
		{
			throw new ServletException("Request URI could not be mapped to an endpoint as there is no known endpoint called " + wsMethodName);
		}
		
		try
		{
			this.logger.debug("URI {}.. Invoking method for endpoint {}  content type is {}  document length {} bytes  remote addr {}",
					new Object[]{req.getRequestURI(), wsMethodName, req.getContentType(), document.length, req.getRemoteAddr()}
			);
			
			Object result = wsMethod.invoke(this.wsProcessor, document, req.getContentType());
			
			if (result instanceof byte[])
			{
				byte[] response = (byte[])result;
				resp.getOutputStream().write(response);
				return;
			}
			
			this.logger.error("Return object was of the wrong type. Expected {} but got {}", byte[].class.getName(), result.getClass().getName());
			throw new ServletException("Response document was invalid. See server log for details.");
		}
		catch (IllegalArgumentException e)
		{
			this.logger.error("Illegal argument invoking WS method: " + e.getMessage());
			throw new ServletException("Unable to invoke WS method due to an illegal argument.");
		}
		catch (IllegalAccessException e)
		{
			this.logger.error("Illegal access invoking WS method: " + e.getMessage());
			throw new ServletException("Unable to invoke WS method due to an illegal access.");
		}
		catch (InvocationTargetException e)
		{
			if (e.getCause() instanceof WSProcessorException)
			{
				WSProcessorException wspe = (WSProcessorException)e.getCause();
				this.logger.error("WS Processor error occurred. Error message was: " + wspe.getMessage());
				throw new ServletException("WS Processor error occurred. Error message was: " + wspe.getMessage());
			}
			
			this.logger.error("Exception thrown invoking WS method: " + e.getMessage());
			this.logger.debug("Invocation target exception: ", e);
			throw new ServletException("Unable to invoke WS method due to an invocation target exception.");
		}
	}
}

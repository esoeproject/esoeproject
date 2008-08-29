package com.qut.middleware.spep.integrators.blackboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.qut.middleware.spep.filter.SPEPFilter;

public class BlackboardSPEPFilter extends SPEPFilter {
	
	private List<String> spepIgnoredIPAddresses;
	
	public BlackboardSPEPFilter()
	{
		/* Load IP's from disk to List */
		this.spepIgnoredIPAddresses = new ArrayList<String>();
		
		// TODO Actually load the IPs
		// put the file in spep.data/config
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException
	{
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		
		/*
		 * If IP is not in List or request does have "spep=disabled" then bypass
		 * the SPEP
		 */
		String spepEnable = (String)request.getParameter("spep");
		if( ( spepEnable != null &&  spepEnable.equals("disabled") ) || this.spepIgnoredIPAddresses.contains(request.getRemoteAddr()))
		{
			chain.doFilter(request, response);
			return;
		}
		
		/*
		 * This is a normal request from a standard user that should be
		 * correctly authenticated
		 */
		super.doFilter(servletRequest, servletResponse, chain);
	}
}

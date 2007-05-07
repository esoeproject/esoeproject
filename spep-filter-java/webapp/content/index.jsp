<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map.Entry"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Attributes page.</title>
</head>
<body>
<%
Map<String,List<Object>> attributes = (Map<String,List<Object>>)session.getAttribute("attributes");

if (attributes == null)
{
	%><p>No attributes available for session</p><%
}
else
{
	%><table><%
	for (Map.Entry<String,List<Object>> entry : attributes.entrySet())
	{
		%><tr><%
		
		List<Object> values = entry.getValue();
		int size = values.size();
		String attributeName = entry.getKey();
		
		if (size > 0)
		{
			%><td rowspan="<%= size %>"><%= attributeName %></td><%
			
			boolean first = true;
			for (Object value : values)
			{
				if (!first)
				{
					%></tr><tr><%
				}
				
				%><td><%= value.toString() %></td><%
			}
		}
		else
		{
			%><td><%= attributeName %></td><td>(null)</td><%
		}
		
		%></tr><%
	}
	%></table><%
}
%>
</body>
</html>
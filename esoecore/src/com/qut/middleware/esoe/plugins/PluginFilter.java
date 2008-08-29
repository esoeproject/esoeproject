package com.qut.middleware.esoe.plugins;

import java.io.File;
import java.io.FilenameFilter;

public class PluginFilter implements FilenameFilter
{
	private String matchExt;
	
	public PluginFilter(String ext)
	{
		if(!ext.startsWith("."))
		{
			ext = "." + ext;
		}
		
		this.matchExt = ext;
	}

	public boolean accept(File dir, String name)
	{
		if(name.endsWith(this.matchExt))
			return true;
		
		return false;
	}

}

package com.qut.middleware.metadata.source.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.metadata.bean.EntityData;
import com.qut.middleware.metadata.exception.MetadataCacheUpdateException;
import com.qut.middleware.metadata.processor.MetadataProcessor;
import com.qut.middleware.metadata.source.DynamicMetadataSource;

public abstract class DynamicMetadataSourceBase implements DynamicMetadataSource
{
	public static final int BUFFER_LENGTH = 1024;
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	List<String> trustedEntities;
	
	public DynamicMetadataSourceBase()
	{
		this.trustedEntities = new ArrayList<String>();
	}
	
	protected EntityData readMetadata(InputStream input, MetadataProcessor processor, String entityID) throws IOException
	{
		ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
		
		this.logger.debug("Dynamic metadata source - going to read input stream for entity ID: " + entityID);
		long startTime = System.currentTimeMillis();

		byte[] buf = new byte[BUFFER_LENGTH];
		int bytes = 0;
		while ((bytes = input.read(buf)) != -1)
		{
			byteOutput.write(buf, 0, bytes);
		}
		
		input.close();
		byteOutput.close();
		
		byte[] document = byteOutput.toByteArray();
		long endTime = System.currentTimeMillis();
		
		this.logger.debug(
			"Dynamic metadata source - read {} bytes of metadata in {} ms. Going to process.",
			new Object[]{document.length, (endTime - startTime)}
		);
		
		startTime = System.currentTimeMillis();
		EntityData data = this.processMetadata(document, processor, entityID);
		endTime = System.currentTimeMillis();
		
		if (data == null)
		{
			this.logger.debug(
					"Dynamic metadata source - entity with ID {} NOT found - processed document and updated cache in {} ms",
					new Object[]{entityID, (endTime - startTime)}
			);
		}
		else
		{
			this.logger.debug(
					"Dynamic metadata source - entity with ID {} was found successfully - processed document and updated cache in {} ms",
					new Object[]{entityID, (endTime - startTime)}
			);
		}
		
		return data;
	}
	
	protected EntityData processMetadata(byte[] document, MetadataProcessor processor, String entityID)
	{
		try
		{
			return processor.updateFromDynamicSource(this, entityID, document);
		}
		catch (MetadataCacheUpdateException e)
		{
			this.logger.error("Dynamic metadata source update for entity ID: {} - update cache FAILED. Error was: {}", new Object[]{entityID, e.getMessage()});
			this.logger.debug("Dynamic metadata source update for entity ID: " + entityID, e);
		}
		
		return null;
	}

	public boolean isTrusted(String entityID)
	{
		return this.trustedEntities.contains(entityID);
	}
	
	public void addTrusted(String entityID)
	{
		this.trustedEntities.add(entityID);
	}
}

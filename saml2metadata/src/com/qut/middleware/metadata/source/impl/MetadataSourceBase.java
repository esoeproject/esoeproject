/*
 * Copyright 2008, Queensland University of Technology
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
 * Creation Date: 10/04/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.metadata.source.impl;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.metadata.exception.MetadataCacheUpdateException;
import com.qut.middleware.metadata.processor.MetadataProcessor;
import com.qut.middleware.metadata.source.MetadataSource;

/**
 * Defines basic operations for a metadata source that is based on a
 * document being obtained from an InputStream.
 */
public abstract class MetadataSourceBase implements MetadataSource
{
	public static final int 	BUFFER_LENGTH = 1024;
	public static final String 	DEFAULT_HASH_ALGORITHM = "SHA1";
	
	private String hashAlgorithm;
	private byte[] digest;
	
	protected int priority;
	protected boolean mandatory;
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private boolean trusted;
	
	public MetadataSourceBase()
	{
		this.hashAlgorithm = MetadataSourceBase.DEFAULT_HASH_ALGORITHM;
		this.digest = new byte[0];
		
		// Set defaults
		this.priority = MetadataSource.DEFAULT_PRIORITY;
		this.mandatory = MetadataSource.DEFAULT_MANDATORY;
		this.trusted = MetadataSource.DEFAULT_TRUSTED;
		
		// Try to trap a "no such algorithm" problem at instantiation time.
		if (this.getMessageDigestInstance() == null)
		{
			String message = "The default hash algorithm: " + this.hashAlgorithm + " is not supported.";
			this.logger.error(message);
			throw new UnsupportedOperationException(message);
		}
		
		this.logger.debug("Constructed MetadataSourceBase with default hash algorithm: {}", this.hashAlgorithm);
	}
	
	public MetadataSourceBase(String hashAlgorithm)
	{
		this.hashAlgorithm = hashAlgorithm;
		this.digest = new byte[0];
		
		// Set defaults
		this.priority = MetadataSource.DEFAULT_PRIORITY;
		this.mandatory = MetadataSource.DEFAULT_MANDATORY;
		this.trusted = MetadataSource.DEFAULT_TRUSTED;
		
		// Try to trap a "no such algorithm" problem at instantiation time.
		if (this.getMessageDigestInstance() == null)
		{
			String message = "The supplied hash algorithm: " + this.hashAlgorithm + " is not supported.";
			this.logger.error(message);
			throw new UnsupportedOperationException(message);
		}

		this.logger.debug("Constructed MetadataSourceBase with specified hash algorithm: {}", this.hashAlgorithm);
	}
	
	public MessageDigest getMessageDigestInstance()
	{
		try
		{
			return MessageDigest.getInstance(this.hashAlgorithm);
		}
		catch (NoSuchAlgorithmException e)
		{
			this.logger.error("Couldn't open algorithm: " + this.hashAlgorithm + ". Exception follows", e);
			return null;
		}
	}
	
	protected boolean updateDigest(byte[] newDigest)
	{
		// Construct string from both digests to form log statement
		String newDigestString = new String(Hex.encodeHex(newDigest));
		String digestString = new String(Hex.encodeHex(this.digest));
		this.logger.debug(
			"Metadata source {} - comparing new digest {} to previous {}.", 
			new Object[]{this.getLocation(), newDigestString, digestString}
		);
		
		// Compare and update
		if (!MessageDigest.isEqual(this.digest, newDigest))
		{
			this.digest = newDigest;
			
			this.logger.info(
				"Metadata source {} - document has been updated and will be updated in the cache. New hash: {}. Old hash: {}",
				new Object[]{this.getLocation(), newDigestString, digestString}
			);
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Reads the provided input stream, calculates and updates an internal hash.
	 * If the internal hash has changed, the byte array obtained from reading the
	 * InputStream is passed to the processMetadata method, along with the
	 * provided MetadataProcessor object.
	 * @param input Input stream to send 
	 * @param processor
	 * @throws IOException
	 */
	protected void readMetadata(InputStream input, MetadataProcessor processor) throws IOException
	{
		byte[] buf = new byte[BUFFER_LENGTH];
		long startTime = System.currentTimeMillis();
		
		// Pipe everything through a digest stream so we get a hash value at the end
		DigestInputStream digestInput = new DigestInputStream(input, this.getMessageDigestInstance());
		BufferedInputStream bufferedInput = new BufferedInputStream(digestInput);
		
		ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
		
		this.logger.debug("Metadata source {} - going to read input stream", this.getLocation());
		int bytes = 0;
		while ((bytes = bufferedInput.read(buf)) != -1)
		{
			byteOutput.write(buf, 0, bytes);
		}
		
		bufferedInput.close();
		digestInput.close();
		byteOutput.close();
		
		long endTime = System.currentTimeMillis();
		
		byte[] document = byteOutput.toByteArray();
		byte[] hash = digestInput.getMessageDigest().digest();
		
		this.logger.debug(
			"Metadata source {} - read {} bytes of metadata in {} ms",
			new Object[]{this.getLocation(), document.length, (endTime - startTime)}
		);
		
		// If the document has changed, the hash will be updated, and then we go to process the new document
		if (this.updateDigest(hash))
		{
			startTime = System.currentTimeMillis();
			this.logger.debug("Metadata source {} - updated. Going to process.", this.getLocation());
			this.processMetadata(document, processor);
			endTime = System.currentTimeMillis();
			this.logger.info(
					"Metadata source {} - processed document and updated cache in {} ms",
					this.getLocation(), (endTime - startTime)
			);
		}
		else
		{
			this.logger.info("Metadata source {} - has not been updated.", this.getLocation());
		}
	}
	
	/**
	 * Default implementation hands the document directly to the MetadataProcessor
	 * provided, so that it can be processed as configured.
	 * This may be overridden to provide different behaviour.
	 * @param document
	 * @param processor
	 */
	protected void processMetadata(byte[] document, MetadataProcessor processor)
	{
		try
		{
			processor.updateFromSource(this, document);
		}
		catch (MetadataCacheUpdateException e)
		{
			this.logger.error("Metadata source {} - update cache FAILED. Error was: {}", new Object[]{this.getLocation(), e.getMessage()});
			this.logger.debug("Metadata source " + this.getLocation() + " - update cache FAILED.", e);
		}
	}

	public int getPriority()
	{
		return this.priority;
	}
	
	public void setPriority(int priority)
	{
		this.priority = priority;
	}

	public boolean isMandatory()
	{
		return this.mandatory;
	}
	
	public void setMandatory(boolean mandatory)
	{
		this.mandatory = mandatory;
	}
	
	public boolean isTrusted()
	{
		return this.trusted;
	}
	
	public void setTrusted(boolean trusted)
	{
		this.trusted = trusted;
	}
}

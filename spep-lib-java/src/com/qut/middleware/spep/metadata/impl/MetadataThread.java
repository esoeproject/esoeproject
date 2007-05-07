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
 * Author: Shaun Mangelsdorf
 * Creation Date: 25/10/2006
 * 
 * Purpose: Maintains a background thread that polls the metadata from the given URL, 
 * 		on a given interval, and updates if there have been any changes.
 */
package com.qut.middleware.spep.metadata.impl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.CharBuffer;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;

import com.qut.middleware.saml2.exception.ReferenceValueException;
import com.qut.middleware.saml2.exception.SignatureValueException;
import com.qut.middleware.saml2.exception.UnmarshallerException;
import com.qut.middleware.saml2.handler.Unmarshaller;
import com.qut.middleware.saml2.handler.impl.UnmarshallerImpl;
import com.qut.middleware.saml2.schemas.metadata.EntitiesDescriptor;
import com.qut.middleware.saml2.schemas.metadata.lxacml.LXACMLPDPDescriptor;
import com.qut.middleware.saml2.sec.KeyData;
import com.qut.middleware.spep.ConfigurationConstants;
import com.qut.middleware.spep.exception.InvalidSAMLDataException;
import com.qut.middleware.spep.metadata.Messages;

/** Maintains a background thread that polls the metadata from the given URL, 
 * 		on a given interval, and updates if there have been any changes.*/
public class MetadataThread extends Thread
{
	private MetadataImpl metadata;
	private String[] schemas;
	private int interval;
	private Unmarshaller<EntitiesDescriptor> unmarshaller;
	
	private final String UNMAR_PKGNAMES = EntitiesDescriptor.class.getPackage().getName() + ":" + LXACMLPDPDescriptor.class.getPackage().getName();
	
	/* Local logging instance */
	private Logger logger = Logger.getLogger(MetadataThread.class.getName());

	/**
	 * @param reportingProcessor 
	 * @param metadata
	 *            The metadata implementation class to manipulate with this thread
	 * @param interval
	 *            The interval at which to refresh the metadata and update if it has changed, in seconds
	 */
	public MetadataThread(MetadataImpl metadata, int interval)
	{
		super("SPEP Metadata update thread"); //$NON-NLS-1$
		this.metadata = metadata;
		this.interval = interval * 1000;

		this.schemas = new String[] { ConfigurationConstants.samlMetadata, ConfigurationConstants.lxacmlMetadata, ConfigurationConstants.spepStartupService };
		try
		{
			this.unmarshaller = new UnmarshallerImpl<EntitiesDescriptor>(this.UNMAR_PKGNAMES, this.schemas);
		}
		catch (UnmarshallerException e)
		{
			this.logger.error(Messages.getString("MetadataThread.0") + e.getLocalizedMessage()); //$NON-NLS-1$
			throw new UnsupportedOperationException(e);
		}
		
		this.logger.info(MessageFormat.format(Messages.getString("MetadataThread.22"), interval) ); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run()
	{
		try
		{
			doGetMetadata();
			this.metadata.hasError.set(false);
			this.logger.debug(Messages.getString("MetadataThread.23")); //$NON-NLS-1$
		}
		catch (SignatureValueException e)
		{
			this.logger.error(Messages.getString("MetadataThread.1") + e.getLocalizedMessage()); //$NON-NLS-1$
			this.metadata.hasError.set(true);
		}
		catch (NoSuchAlgorithmException e)
		{
			this.logger.error(Messages.getString("MetadataThread.2") + e.getLocalizedMessage()); //$NON-NLS-1$
			this.metadata.hasError.set(true);
		}
		catch (ReferenceValueException e)
		{
			this.logger.error(Messages.getString("MetadataThread.3") + e.getLocalizedMessage()); //$NON-NLS-1$
			this.metadata.hasError.set(true);
		}
		catch (UnmarshallerException e)
		{
			this.logger.error(Messages.getString("MetadataThread.4") + e.getLocalizedMessage()); //$NON-NLS-1$
			this.metadata.hasError.set(true);
		}
		catch (IOException e)
		{
			this.logger.error(Messages.getString("MetadataThread.5") + e.getLocalizedMessage()); //$NON-NLS-1$
			this.metadata.hasError.set(true);
		}
		catch (InvalidSAMLDataException e)
		{
			this.logger.error(Messages.getString("MetadataThread.7") + e.getLocalizedMessage()); //$NON-NLS-1$
			this.metadata.hasError.set(true);
		}
		catch(UnsupportedOperationException e)
		{
			this.logger.error("Initial metadata update failed: " + e.getMessage()); //$NON-NLS-1$
			this.metadata.hasError.set(true);		
		}
		
		while (true)
		{
			try
			{
				Thread.sleep(this.interval);
				this.logger.debug("Metadata Thread woke up. Processing metadata ..."); //$NON-NLS-1$
				doGetMetadata();
				this.metadata.hasError.set(false);
			}
			catch (SignatureValueException e)
			{
				this.logger.error(Messages.getString("MetadataThread.9") + e.getLocalizedMessage()); //$NON-NLS-1$
			}
			catch (NoSuchAlgorithmException e)
			{
				this.logger.error(Messages.getString("MetadataThread.10") + e.getLocalizedMessage()); //$NON-NLS-1$
			}
			catch (ReferenceValueException e)
			{
				this.logger.error(Messages.getString("MetadataThread.11") + e.getLocalizedMessage()); //$NON-NLS-1$
			}
			catch (UnmarshallerException e)
			{
				this.logger.error(Messages.getString("MetadataThread.12") + e.getLocalizedMessage()); //$NON-NLS-1$
			}
			catch (IOException e)
			{
				this.logger.error(Messages.getString("MetadataThread.13") + e.getLocalizedMessage()); //$NON-NLS-1$
			}
			catch (InvalidSAMLDataException e)
			{
				this.logger.error(Messages.getString("MetadataThread.16") + e.getLocalizedMessage()); //$NON-NLS-1$
			}
			catch (InterruptedException e)
			{
				this.logger.warn(Messages.getString("MetadataThread.17")); //$NON-NLS-1$
			}
			catch(UnsupportedOperationException e)
			{
				this.logger.error("Metadata update failed. Ignoring new metadata. " + e.getMessage()); //$NON-NLS-1$
			}
		}
	}

	/*
	 * 
	 */
	private void doGetMetadata() throws SignatureValueException, ReferenceValueException, UnmarshallerException, NoSuchAlgorithmException, IOException, InvalidSAMLDataException
	{		
		RawMetadata rawMetadata = getRawMetadata(this.metadata.metadataUrl);
		
		EntitiesDescriptor entitiesDescriptor = null;
	
		this.logger.debug(MessageFormat.format(Messages.getString("MetadataThread.24"), rawMetadata.hashValue, this.metadata.currentRevision) ); //$NON-NLS-1$

		if (!rawMetadata.hashValue.equalsIgnoreCase(this.metadata.currentRevision))
		{
			this.logger.info(Messages.getString("MetadataThread.18")); //$NON-NLS-1$

			if (!rawMetadata.data.contains("EntitiesDescriptor")) //$NON-NLS-1$
			{
				throw new IllegalArgumentException(Messages.getString("MetadataThread.19")); //$NON-NLS-1$
			}
			
			try
			{
				// Do the unmarshalling step
				Map<String, KeyData> keyMap = new HashMap<String, KeyData>();
			
				entitiesDescriptor = this.unmarshaller.unMarshallMetadata(this.metadata.metadataPublicKey, rawMetadata.data, keyMap);
			
				this.metadata.rebuildCache(entitiesDescriptor, rawMetadata.hashValue, keyMap);
			}
			catch (ClassCastException e)
			{
				this.logger.error(MessageFormat.format(Messages.getString("MetadataThread.25"), e.getMessage())); //$NON-NLS-1$
				throw new IllegalArgumentException(Messages.getString("MetadataThread.20")); //$NON-NLS-1$
			}

			this.logger.info(Messages.getString("MetadataThread.21")); //$NON-NLS-1$
		}
	}

	private RawMetadata getRawMetadata(String metadataUrl) throws IOException, NoSuchAlgorithmException
	{
		MessageDigest messageDigest = MessageDigest.getInstance("SHA1"); //$NON-NLS-1$

		// Figure out which protocol to use
		URL url = new URL(metadataUrl);
		
		// Open the URL and get a stream.
		URLConnection connection = url.openConnection();
		DigestInputStream digestStream = new DigestInputStream(connection.getInputStream(), messageDigest);
		BufferedInputStream bufferedStream = new BufferedInputStream(digestStream);
		InputStreamReader in = new InputStreamReader(bufferedStream, "UTF-16"); //$NON-NLS-1$
		
		// Read the file.
		StringBuffer stringBuffer = new StringBuffer();
		CharBuffer charBuffer = CharBuffer.allocate(MetadataImpl.BUFFER_LEN);
		while (in.read(charBuffer) >= 0)
		{
			charBuffer.flip();
			stringBuffer.append(charBuffer.toString());
			charBuffer.clear();
		}

		RawMetadata rawMetadata = new RawMetadata();

		rawMetadata.data = stringBuffer.toString();
		byte[] digestBytes = digestStream.getMessageDigest().digest();
		rawMetadata.hashValue = new String(Hex.encodeHex(digestBytes));

		this.logger.debug(MessageFormat.format(Messages.getString("MetadataThread.26"), Integer.toString(rawMetadata.data.length()), rawMetadata.hashValue)); //$NON-NLS-1$

		return rawMetadata;
	}

	private class RawMetadata
	{
		/**
		 * Default constructor
		 */
		public RawMetadata()
		{
			//
		}

		protected String hashValue;
		protected String data;
	}
}
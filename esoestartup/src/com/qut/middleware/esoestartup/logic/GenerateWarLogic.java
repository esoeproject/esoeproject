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
 * Creation Date: 1/5/07
 * 
 * Purpose: Dynamically generates WAR files for base components of ESOE deployment
 */
package com.qut.middleware.esoestartup.logic;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.apache.log4j.Logger;

import com.qut.middleware.esoestartup.bean.AdditionalContent;
import com.qut.middleware.esoestartup.exception.GenerateWarException;

public class GenerateWarLogic {
	/*
	 * NB: Good class name eh? Perhaps I should have called this
	 * SteveBalmerSaysOpenSourceSucksLogic ;-)
	 */

	/* Local logging instance */
	private Logger logger = Logger.getLogger(GenerateWarLogic.class.getName());

	private List<File> getFileListing(File dir) {
		File[] filesAndDirs = dir.listFiles();
		this.logger.debug("processing " + dir.getPath());

		return Arrays.asList(filesAndDirs);
	}

	private void generateWarContents(String parentDir, File dir,
			JarOutputStream out) throws IOException {
		List<File> jarContents;
		byte buffer[] = new byte[10000];

		jarContents = getFileListing(dir);

		for (File jarFile : jarContents) {
			if (jarFile == null || !jarFile.exists()) {
				continue;
			}

			/* Recursively add directory content */
			if (jarFile.isDirectory()) {
				if (parentDir != null)
					generateWarContents(parentDir + File.separator
							+ jarFile.getName(), jarFile, out);
				else
					generateWarContents(jarFile.getName(), jarFile, out);

				continue;
			}

			JarEntry jarAdd;
			if (parentDir != null)
				jarAdd = new JarEntry(parentDir + File.separator
						+ jarFile.getName());
			else
				jarAdd = new JarEntry(jarFile.getName());

			jarAdd.setTime(jarFile.lastModified());
			out.putNextEntry(jarAdd);

			if (jarFile.isFile()) {
				/* Write file to war archive */
				FileInputStream in = new FileInputStream(jarFile);
				while (true) {
					int nRead = in.read(buffer, 0, buffer.length);
					if (nRead <= 0)
						break;
					out.write(buffer, 0, nRead);
				}
				in.close();
			}
		}
	}

	private void copyFiles(File src, File dest) throws IOException {
		byte buffer[] = new byte[10000];

		/* Make the destination directory */
		dest.mkdirs();
		List<File> srcList = getFileListing(src);

		for (File file : srcList) {
			if (file.isDirectory()) {
				File recDest = new File(dest.getAbsolutePath() + File.separator
						+ file.getName());
				File recSrc = new File(src.getAbsolutePath() + File.separator
						+ file.getName());
				copyFiles(recSrc, recDest);
				continue;
			}

			FileInputStream in = null;
			FileOutputStream out = null;
			try {
				in = new FileInputStream(file);
				out = new FileOutputStream(dest + File.separator
						+ file.getName());
				while (true) {
					int nRead = in.read(buffer, 0, buffer.length);
					if (nRead <= 0)
						break;
					out.write(buffer, 0, nRead);
				}
			} finally {
				if (in != null)
					in.close();

				if (out != null)
					out.close();
			}
		}
	}

	private void writeAdditionalContent(AdditionalContent content, File dest)
			throws IOException {
		byte buffer[] = new byte[10000];
		ByteArrayInputStream in = null;
		FileOutputStream out = null;
		try {
			in = new ByteArrayInputStream(content.getFileContent());
			out = new FileOutputStream(dest + File.separator
					+ content.getPath());
			while (true) {
				int nRead = in.read(buffer, 0, buffer.length);
				if (nRead <= 0)
					break;
				out.write(buffer, 0, nRead);
			}
		} finally {
			if (in != null)
				in.close();

			if (out != null)
				out.close();
		}
	}

	public void generateWar(List<AdditionalContent> additionalContent,
			File srcDir, File outDir, File warFile) throws GenerateWarException {
		FileOutputStream stream = null;
		JarOutputStream out = null;
		try {
			stream = new FileOutputStream(warFile);
			out = new JarOutputStream(stream, new Manifest());

			copyFiles(srcDir, outDir);

			if (additionalContent != null) {
				/* Write extra content to output directory */
				for (AdditionalContent content : additionalContent) {
					writeAdditionalContent(content, outDir);
				}
			}

			generateWarContents(null, outDir, out);

			out.close();
			stream.close();
		} catch (IOException e) {
			this.logger.error("Error attempting to create war from directory "
					+ srcDir.getName() + " " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new GenerateWarException(
					"Error attempting to create war from directory "
							+ srcDir.getName() + " " + e.getLocalizedMessage());
		} finally {
			try {
				if (out != null)
					out.close();

				if (stream != null)
					stream.close();
			} catch (IOException e) {
				/* No real way to handle this */
				this.logger.error("Unable to close streams correctly "
						+ e.getLocalizedMessage());
			}
		}
	}
}

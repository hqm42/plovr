/*
 * $Id$
 * (c) Copyright 2012 freiheit.com technologies GmbH
 *
 * Created on 21.07.2012 by marco
 *
 * This file contains unpublished, proprietary trade secret information of
 * freiheit.com technologies GmbH. Use, transcription, duplication and
 * modification are strictly prohibited without prior written consent of
 * freiheit.com technologies GmbH.
 */
package org.plovr.fam;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


import net.contentobjects.jnotify.JNotifyException;
import net.contentobjects.jnotify.JNotifyListener;

public class JNotifyWrapper {
	  
	  public static final int FILE_CREATED = 1;
	  
	  public static final int FILE_DELETED = 2;
	  
	  public static final int FILE_MODIFIED = 4;
	  
	  public static final int FILE_RENAMED = 8;
	  

	private static String getLibraryForCurrentOS() {

		String arch = System.getProperty("sun.arch.data.model");
		String os = System.getProperty("os.name").toLowerCase();
		System.out.println("Current os is >" + os + "<");
		if (os.contains("linux")) {
			if (arch.contains("64")) {
				return "/jni64/libjnotify.so";
			} else {
				return "/jni32/libjnotify.so";
			}
		} else
		if (os.contains("win")) {
			if (arch.contains("64")) {
				return "/jni64/jnotify_64bit.dll";
			} else {
				return "/jni32/jnotify.dll";
			}

		} else
		if (os.contains("mac os x")){
			return "/jni64/jnotify.jnilib";
		}
		return null;
	}

	private static void loadLibrary() {
		String libraryName = getLibraryForCurrentOS();
		File f = calculateTempFilename(libraryName);
				
		InputStream in = JNotifyWrapper.class.getResourceAsStream(libraryName);
		if (in ==null){
			System.err.println("Failed to read library: "+libraryName);
			return;
		}
			byte[] buffer = new byte[1024];
			int read = -1;
			try {
				FileOutputStream fos = new FileOutputStream(f);
				while ((read = in.read(buffer)) != -1) {
					fos.write(buffer, 0, read);
				}
				fos.close();
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    System.err.println("Loading library: "+libraryName+ " from "+f.getAbsolutePath());
	    
	    /**
	     * Override the default jnotify library
	     */
	    System.setProperty("jnotify.library.override",f.getAbsolutePath());
	}
	
	static {
		loadLibrary();
	}

	private static File calculateTempFilename(String lib) {
		String tempDir = System.getProperty("java.io.tmpdir");
		String fileName = new File(lib).getName();
		File f = new File(tempDir, fileName );
		return f;
	}
	
	public int addWatch(String path, int mask, boolean watchSubtree, JNotifyListener l) throws JNotifyException{
		return net.contentobjects.jnotify.JNotify.addWatch(path, mask, watchSubtree, l);
	}
	
	public boolean removeWatch(int watch) throws JNotifyException{
		return net.contentobjects.jnotify.JNotify.removeWatch(watch);
	}
	
	
}

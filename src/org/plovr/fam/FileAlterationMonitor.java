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

/**
 * A Wrapper for Source File monitoring which switches 
 * between JNotify if available and a simple implementation 
 * which just compares file timestamps.
 * 
 * Comparing timestamps could be slow on large input 
 * filesets but jnotify uses native code and is as such platform dependend.
 * 
 * @author marco (initial creation)
 */
public abstract class FileAlterationMonitor {

	
	public abstract long getLastModificationTimestamp();
	
	public abstract void watch(File f);
	
	public abstract void clearWatches();

	public static FileAlterationMonitor newInstance() {
		boolean useJNotify = "true".equals(System.getProperty("plovr.usejnotify"));
		if (useJNotify){
			return new JNotifyFileAlterationMonitor();
		} else {
			return new SimpleFileAlterationMonitor();
		}
	}
}

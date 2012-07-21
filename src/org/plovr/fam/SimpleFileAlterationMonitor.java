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
import java.util.HashSet;
import java.util.Set;

public class SimpleFileAlterationMonitor extends FileAlterationMonitor   {
	
	
		private Set<String> watchedPaths= new HashSet<String>();
		private Set<File> watchedFiles = new HashSet<File>();
		
		
		
		public synchronized void clearWatches(){
			watchedFiles.clear();
		}

		@Override
		protected void finalize() throws Throwable {
			clearWatches();
			super.finalize();
		}

		@Override
		public long getLastModificationTimestamp() {
			long last =0;
			for(File f:watchedFiles){
				last = Math.max(last, f.lastModified());
			}
			return last;
		}

		@Override
		public void watch(File file) {
			try {
				File canonical= file.getCanonicalFile();
				if (!this.watchedFiles.contains(canonical)) {
			
					this.watchedFiles.add(canonical);
				}
			} catch (Exception e){
				e.printStackTrace();
			}
		}


	
}

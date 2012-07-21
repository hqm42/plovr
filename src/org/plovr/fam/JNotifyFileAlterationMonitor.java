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

import net.contentobjects.jnotify.JNotifyException;
import net.contentobjects.jnotify.JNotifyListener;

public class JNotifyFileAlterationMonitor extends FileAlterationMonitor implements JNotifyListener {
	
		private long lastModification;
	
		private Set<Integer> watches = new HashSet<Integer>();
		private Set<String> watchedDirs = new HashSet<String>();
		
		
		private void updateTimestamp() {
			
			long now = System.currentTimeMillis();
			if (now> this.lastModification ){
				System.out.println("Detected File System change.");
				this.lastModification = now;
			}
		}

		public void fileRenamed(int wd, String rootPath, String oldName,
				String newName) {
			updateTimestamp();
		}

		public void fileModified(int wd, String rootPath, String name) {
			updateTimestamp();
		}

		public void fileDeleted(int wd, String rootPath, String name) {
			updateTimestamp();
		}

		public void fileCreated(int wd, String rootPath, String name) {
			updateTimestamp();
		}
		
		
		public synchronized void clearWatches(){
			JNotifyWrapper jNotifyWrapper = new JNotifyWrapper();
			for(int watch:this.watches){
				try {
					jNotifyWrapper.removeWatch(watch);
				} catch (JNotifyException e) {
					e.printStackTrace();
				}
			}
			this.watches.clear();
			this.watchedDirs.clear();
		}

		@Override
		protected void finalize() throws Throwable {
			clearWatches();
			super.finalize();
		}

		@Override
		public long getLastModificationTimestamp() {
			return this.lastModification;
		}

		@Override
		public void watch(File file) {
			try {
			String fullName = file.getParentFile().getCanonicalPath();
			if (!this.watchedDirs.contains(fullName)) {
				int watch = new JNotifyWrapper().addWatch(fullName,
						JNotifyWrapper.FILE_CREATED
								| JNotifyWrapper.FILE_DELETED
								| JNotifyWrapper.FILE_MODIFIED
								| JNotifyWrapper.FILE_RENAMED, false,
						this);
				this.watches.add(watch);
				this.watchedDirs.add(fullName);
			}
			} catch (Exception e){
				e.printStackTrace();
			}
		}

		
		

	
}

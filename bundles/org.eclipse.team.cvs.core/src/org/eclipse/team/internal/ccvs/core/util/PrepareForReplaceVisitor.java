/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.core.util;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.ccvs.core.ICVSFile;
import org.eclipse.team.ccvs.core.ICVSFolder;
import org.eclipse.team.ccvs.core.ICVSResourceVisitor;
import org.eclipse.team.ccvs.core.ICVSRunnable;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;

/**
 * This class is used to prepare a local CVS workspace for replacement by
 * the corresponding remote resources. More specifically, this class will
 * unmanage added and deleted resources so that, after the operation, the
 * resources in the local workspace will either correspond to the remote
 * counterparts or be unmanaged.
 */
public class PrepareForReplaceVisitor implements ICVSResourceVisitor {

	private IProgressMonitor monitor;
	private int depth;

	/**
	 * @see ICVSResourceVisitor#visitFile(ICVSFile)
	 */
	public void visitFile(ICVSFile file) throws CVSException {
		ResourceSyncInfo info = file.getSyncInfo();
		if (info == null) {
			// If the file is unmanaged, just leave it as is
		} else if (info.isAdded()) {
			// For added files, delete and unmanage
			file.delete();
			file.unmanage(null);
		} else if (info.isDeleted()) {
			// If deleted, null the sync info so the file will be refetched
			file.unmanage(null);
		}
		monitor.worked(1);
	}

	/**
	 * @see ICVSResourceVisitor#visitFolder(ICVSFolder)
	 */
	public void visitFolder(ICVSFolder folder) throws CVSException {
		// Visit the children of the folder as appropriate
		if (depth == IResource.DEPTH_INFINITE)
			folder.acceptChildren(this);
		else if (depth == IResource.DEPTH_ONE) {
			ICVSFile[] files = folder.getFiles();
			for (int i = 0; i < files.length; i++) {
				files[i].accept(this);
			}
		}
		monitor.worked(1);
	}
	
	public void visitResources(IProject project, final IResource[] resources, final String key, int depth, IProgressMonitor pm) throws CVSException {
		this.depth = depth;
		CVSWorkspaceRoot.getCVSFolderFor(project).run(new ICVSRunnable() {
			public void run(IProgressMonitor pm) throws CVSException {
				monitor = Policy.infiniteSubMonitorFor(pm, 100);
				monitor.beginTask(null, 512);
				for (int i = 0; i < resources.length; i++) {
					if (key != null) {
						monitor.subTask(Policy.bind(key, resources[i].getFullPath().toString())); //$NON-NLS-1$
					}
					IResource resource = resources[i];
					CVSWorkspaceRoot.getCVSResourceFor(resource).accept(PrepareForReplaceVisitor.this);
				}
				monitor.done();
			}
		}, pm);
	}
}

/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui.compare;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * A model buffer is used to buffer changes made when comparing
 * or merging a model. A buffer can be shared between multiple
 * typed elements within a comparison. The buffer is used by the comparison
 * container in order to determine when a save is required.
 * <p>
 * Clients may subclass this class.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @since 3.2
 */
public abstract class ModelBuffer implements IModelBuffer {

	private boolean dirty;
	private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.compare.IModelBuffer#isDirty()
	 */
	public boolean isDirty() {
		return dirty;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.compare.IModelBuffer#save(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void save(IProgressMonitor monitor) throws CoreException {
		if (!isDirty())
			return;
		doSave(monitor);
		setDirty(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.compare.IModelBuffer#revert(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void revert(IProgressMonitor monitor) throws CoreException {
		if (!isDirty())
			return;
		doRevert(monitor);
		setDirty(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.compare.IModelBuffer#addPropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.compare.IModelBuffer#removePropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Set the dirty state of this buffer. If the state
	 * has changed, a property change event will be fired.
	 * @param dirty the dirty state
	 */
	protected void setDirty(boolean dirty) {
		if (this.dirty == dirty) {
			return;
		}
		this.dirty = dirty;
		firePropertyChange(P_DIRTY, Boolean.valueOf(!dirty), Boolean.valueOf(dirty));
	}

	/**
	 * Fire a property change event for this buffer.
	 * @param property the property that changed
	 * @param oldValue the old value
	 * @param newValue the new value
	 */
	protected void firePropertyChange(String property, Object oldValue, Object newValue) {
		final PropertyChangeEvent event = new PropertyChangeEvent(this, property, oldValue, newValue);
		Object[] allListeners = listeners.getListeners();
		for (int i = 0; i < allListeners.length; i++) {
			final Object object = allListeners[i];
			Platform.run(new ISafeRunnable() {
				public void run() throws Exception {
					((IPropertyChangeListener)object).propertyChange(event);
				}
				public void handleException(Throwable exception) {
					// handled by platform
				}
			});
		}
	}
	
	/**
	 * Method invoked from {@link #save(IProgressMonitor)} to write
	 * out the buffer.
	 * @param monitor a progress monitor
	 * @throws CoreException if errors occur
	 */
	protected abstract void doSave(IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Method invoked from {@link #revert(IProgressMonitor)} to discard the 
	 * changes in the buffer.
	 * @param monitor a progress monitor
	 * @throws CoreException if errors occur
	 */
	protected abstract void doRevert(IProgressMonitor monitor) throws CoreException;

}

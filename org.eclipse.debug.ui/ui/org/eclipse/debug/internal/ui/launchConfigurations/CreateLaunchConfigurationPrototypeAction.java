/*******************************************************************************
 * Copyright (c) 2017 Obeo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.launchConfigurations;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTabGroup;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;

/**
 * Creates a new launch configuration prototype based on the selection.
 *
 * @since 3.13
 */
public class CreateLaunchConfigurationPrototypeAction extends AbstractLaunchConfigurationAction {

	/**
	 * Action identifier for IDebugView#getAction(String)
	 */
	public static final String ID_CREATE_PROTOTYPE_ACTION = DebugUIPlugin.getUniqueIdentifier() + ".ID_CREATE_PROTOTYPE_ACTION"; //$NON-NLS-1$

	/**
	 * Constructs an action to create a launch configuration prototype
	 *
	 * @param viewer the viewer
	 * @param mode the mode the action applies to
	 */
	public CreateLaunchConfigurationPrototypeAction(Viewer viewer, String mode) {
		super(LaunchConfigurationsMessages.CreateLaunchConfigurationPrototypeAction_Ne_w_1, viewer, mode);
	}

	/**
	 * @see AbstractLaunchConfigurationAction#performAction()
	 */
	@Override
	protected void performAction() {
		Object object = getStructuredSelection().getFirstElement();
		// double click with Ctrl key mask results in empty selection: bug
		// 156087 do no work if the selection is null
		if (object != null) {
			ILaunchConfigurationType type = null;
			// Construct a new configuration prototype of the selected type
			if (object instanceof ILaunchConfiguration) {
				ILaunchConfiguration config = (ILaunchConfiguration) object;
				try {
					type = config.getType();
				} catch (CoreException e) {
					errorDialog(e);
					return;
				}
			} else {
				type = (ILaunchConfigurationType) object;
			}
			try {
				ILaunchConfigurationWorkingCopy wc = type.newPrototypeInstance(null, DebugPlugin.getDefault().getLaunchManager().generateLaunchConfigurationName(LaunchConfigurationsMessages.CreateLaunchConfigurationPrototypeAction_New_prototype_2));
				ILaunchConfigurationTabGroup tabGroup = LaunchConfigurationPresentationManager.getDefault().getTabGroup(wc, getMode());
				// this only works because this action is only present when the
				// dialog is open
				ILaunchConfigurationDialog dialog = LaunchConfigurationsDialog.getCurrentlyVisibleLaunchConfigurationDialog();
				tabGroup.createTabs(dialog, dialog.getMode());
				ILaunchConfigurationTab[] tabs = tabGroup.getTabs();
				for (int i = 0; i < tabs.length; i++) {
					tabs[i].setLaunchConfigurationDialog(dialog);
				}
				tabGroup.setDefaults(wc);
				tabGroup.dispose();
				wc.doSave();
			} catch (CoreException e) {
				errorDialog(e);
				return;
			}
		}
	}

	/**
	 * @see org.eclipse.ui.actions.SelectionListenerAction#updateSelection(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		if (selection.size() == 1) {
			Object object = getStructuredSelection().getFirstElement();
			if (object != null) {
				ILaunchConfigurationType type = null;
				// Construct a new configuration prototype of the selected type
				if (object instanceof ILaunchConfiguration) {
					ILaunchConfiguration config = (ILaunchConfiguration) object;
					try {
						type = config.getType();
					} catch (CoreException e) {
						errorDialog(e);
					}
				} else {
					type = (ILaunchConfigurationType) object;
				}
				if (type != null) {
					return type.supportsPrototypes();
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#getDisabledImageDescriptor()
	 */
	@Override
	public ImageDescriptor getDisabledImageDescriptor() {
		return DebugUITools.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_NEW_PROTO);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#getImageDescriptor()
	 */
	@Override
	public ImageDescriptor getImageDescriptor() {
		return DebugUITools.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_NEW_PROTO);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#getToolTipText()
	 */
	@Override
	public String getToolTipText() {
		return LaunchConfigurationsMessages.LaunchConfigurationsDialog_2;
	}
}

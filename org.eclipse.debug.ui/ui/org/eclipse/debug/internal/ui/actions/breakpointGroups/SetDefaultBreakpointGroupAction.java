/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.breakpointGroups;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.views.breakpoints.WorkingSetBreakpointOrganizer;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;

/**
 * Action which prompts the user to set a default breakpoint group.
 * @see org.eclipse.debug.core.IBreakpointManager#setAutoGroup(String)
 */
public class SetDefaultBreakpointGroupAction extends AbstractBreakpointsViewAction {
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        IWorkingSet workingSet = WorkingSetBreakpointOrganizer.getDefaultWorkingSet();
        IWorkingSetSelectionDialog selectionDialog = PlatformUI.getWorkbench().getWorkingSetManager().createWorkingSetSelectionDialog(DebugUIPlugin.getShell(), false);
        if (workingSet != null) {
            selectionDialog.setSelection(new IWorkingSet[]{workingSet});
        }
        if (selectionDialog.open() == Window.OK) {
            IWorkingSet[] sets = selectionDialog.getSelection();
            if (sets.length == 1) {
                WorkingSetBreakpointOrganizer.setDefaultWorkingSet(sets[0]);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
    }

}

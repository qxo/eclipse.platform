package org.eclipse.team.internal.ccvs.ui.merge;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.model.BranchTag;
import org.eclipse.team.internal.ccvs.ui.wizards.CVSWizardPage;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class MergeWizardEndPage extends CVSWizardPage {
	IProject project;
	TreeViewer tree;
	CVSTag result;
	ICVSRemoteFolder remote;
	CVSTeamProvider provider;
	
	class ProjectElement implements IWorkbenchAdapter, IAdaptable {
		public Object[] getChildren(Object o) {
			return new Object[] {
				new BranchesElement(),
				new TagElement(CVSTag.DEFAULT),
				new VersionsElement(remote)
			};
		}
		public Object getAdapter(Class adapter) {
			if (adapter == IWorkbenchAdapter.class) return this;
			return null;
		}
		public ImageDescriptor getImageDescriptor(Object object) {
			return null;
		}
		public String getLabel(Object o) {
			return null;
		}
		public Object getParent(Object o) {
			return null;
		}
	};
	class BranchesElement implements IWorkbenchAdapter, IAdaptable {
		public Object[] getChildren(Object o) {
			BranchTag[] tags = CVSUIPlugin.getPlugin().getRepositoryManager().getKnownBranchTags(remote.getRepository());
			TagElement[] result = new TagElement[tags.length];
			for (int i = 0; i < tags.length; i++) {
				result[i] = new TagElement(tags[i].getTag());
			}
			return result;
		}
		public Object getAdapter(Class adapter) {
			if (adapter == IWorkbenchAdapter.class) return this;
			return null;
		}
		public ImageDescriptor getImageDescriptor(Object object) {
			return CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_BRANCHES_CATEGORY);
		}
		public String getLabel(Object o) {
			return Policy.bind("MergeWizardEndPage.branches");
		}
		public Object getParent(Object o) {
			return project;
		}
	};
	/**
	 * MergeWizardEndPage constructor.
	 * 
	 * @param pageName  the name of the page
	 * @param title  the title of the page
	 * @param titleImage  the image for the page
	 */
	public MergeWizardEndPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}
	/*
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 2);
		// set F1 help
		// WorkbenchHelp.setHelp(composite, new DialogPageContextComputer (this, ITeamHelpContextIds.REPO_CONNECTION_MAIN_PAGE));
		
		Label description = new Label(composite, SWT.WRAP);
		GridData data = new GridData();
		data.horizontalSpan = 2;
		data.widthHint = 350;
		description.setLayoutData(data);
		description.setText(Policy.bind("MergeWizardEndPage.description"));
		
		tree = createTree(composite);
		tree.setContentProvider(new WorkbenchContentProvider());
		tree.setLabelProvider(new WorkbenchLabelProvider());
		tree.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				Object selected = ((IStructuredSelection)tree.getSelection()).getFirstElement();
				if (selected instanceof TagElement) {
					result = ((TagElement)selected).getTag();
					setPageComplete(true);
				} else {
					result = null;
					setPageComplete(false);
				}
			}
		});
		setControl(composite);
		tree.setInput(new ProjectElement());
	}
	protected TreeViewer createTree(Composite parent) {
		Tree tree = new Tree(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.SINGLE);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		return new TreeViewer(tree);
	}
	public void setProject(IProject project) {
		this.project = project;
		try {
			this.provider = ((CVSTeamProvider)TeamPlugin.getManager().getProvider(project));
			this.remote = (ICVSRemoteFolder)provider.getRemoteResource(project);
		} catch (TeamException e) {
			// To do
		}
	}
	public CVSTag getTag() {
		return result;
	}
}

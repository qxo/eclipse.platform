package org.eclipse.update.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.configuration.IConfiguredSite;
import org.eclipse.update.core.model.IncludedFeatureReferenceModel;
import org.eclipse.update.internal.core.UpdateManagerPlugin;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

/**
 * This is a utility class representing the options of a nested feature.
 * Feature will include other features. This class will represent the options of the inclusion.
 * <p>
 * Clients may instantiate; not intended to be subclassed by clients.
 * </p> 
 * @see org.eclipse.update.core.VersionedIdentifier
 * @since 2.0.1
 */
public class IncludedFeatureReference extends IncludedFeatureReferenceModel implements IIncludedFeatureReference {
	
	private IFeature bestMatchFeature;	 

	/**
	 * Construct a included feature reference
	 * 
	 * @since 2.1
	 */
	public IncludedFeatureReference() {
		super();
	}


	/**
	 * Construct a feature options 
	 * 
	 * @param name string representation of the feature
	 * @param isOptional <code>true</code> if the feature is optional, <code>false</code> otherwise.
	 * @param matchingRule the matching rule
	 * @param searchLocation the location to search for this feature's updates.
	 * @since 2.0.2
	 */
	public IncludedFeatureReference(IIncludedFeatureReference includedFeatureRef) {
		super((IncludedFeatureReferenceModel)includedFeatureRef);
	}

	/**
	 * Constructor IncludedFeatureReference.
	 * @param iFeatureReference
	 */
	public IncludedFeatureReference(IFeatureReference featureReference) {
		super(featureReference);
	}


	/**
	* Method matches.
	* @param identifier
	* @param id
	* @param options
	* @return boolean
	*/
	private boolean matches(VersionedIdentifier baseIdentifier, VersionedIdentifier id) {
		if (baseIdentifier == null || id == null)
			return false;
		if (!id.getIdentifier().equals(baseIdentifier.getIdentifier()))
			return false;

		switch (getMatch()) {
			case IImport.RULE_PERFECT :
				return id.getVersion().isPerfect(baseIdentifier.getVersion());
			case IImport.RULE_COMPATIBLE :
				return id.getVersion().isCompatibleWith(baseIdentifier.getVersion());
			case IImport.RULE_EQUIVALENT :
				return id.getVersion().isEquivalentTo(baseIdentifier.getVersion());
			case IImport.RULE_GREATER_OR_EQUAL :
				return id.getVersion().isGreaterOrEqualTo(baseIdentifier.getVersion());
		}
		UpdateManagerPlugin.warn("Unknown matching rule:" + getMatch());
		return false;
	}


	/*
	 * Method retrieveEnabledFeatures.
	 * @param site
	 */
	private IFeatureReference[] retrieveEnabledFeatures(ISite site) {
		IConfiguredSite configuredSite = site.getCurrentConfiguredSite();
		if (configuredSite == null)
			return new IFeatureReference[0];
		return configuredSite.getConfiguredFeatures();
	}
	
	/*
	 * Method isDisabled.
	 * @return boolean
	 */
	private boolean isDisabled() {
		/*IConfiguredSite cSite = getSite().getConfiguredSite();
		if (cSite==null) return false;
		IFeatureReference[] configured = cSite.getConfiguredFeatures();
		for (int i = 0; i < configured.length; i++) {
			if (this.equals(configured[i])) return false;
		}
		return true;*/
		// FIXME: this code was never executed, should we remove it ?
		return false;
	}
	
	/**
	 * @see org.eclipse.update.core.IFeatureReference#getFeature(boolean)
	 */
	public IFeature getFeature(boolean perfectMatch,IConfiguredSite configuredSite) throws CoreException {

		// if perfect match is asked or if the feature is disabled
		// we return the exact match 		
		if (perfectMatch || getMatch() == IImport.RULE_PERFECT || isDisabled()) {
			return super.getFeature();
		} else {
			if (bestMatchFeature == null) {
				// find best match
				if (configuredSite==null)
					configuredSite = getSite().getCurrentConfiguredSite();
				IFeatureReference bestMatch = getBestMatch(configuredSite);
				bestMatchFeature = getFeature(bestMatch);
			}
			return bestMatchFeature;
		}
	}
	
	/*
	 * Method getBestMatch.
	 * @param enabledFeatures
	 * @param identifier
	 * @param options
	 * @return Object
	 */
	private IIncludedFeatureReference getBestMatch(IConfiguredSite configuredSite) throws CoreException {
		IncludedFeatureReference newRef = null;

		if (configuredSite==null) return this;
		IFeatureReference[] enabledFeatures = configuredSite.getConfiguredFeatures();

		// find the best feature based on match from enabled features
		for (int ref = 0; ref < enabledFeatures.length; ref++) {
			if (enabledFeatures[ref] != null) {
				VersionedIdentifier id = enabledFeatures[ref].getVersionedIdentifier();
				if (matches(getVersionedIdentifier(), id)) {
					if (newRef == null || id.getVersion().isGreaterThan(newRef.getVersionedIdentifier().getVersion())) {
						newRef = new IncludedFeatureReference(enabledFeatures[ref]);
						newRef.setMatchingRule(getMatch());
						newRef.isOptional(isOptional());
						newRef.setLabel(getLabel());
					}
				}
			}
		}

		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS){
			UpdateManagerPlugin.warn("Found best match feature:"+newRef+" for feature reference "+this.getURLString());
		}

		if (newRef != null)
			return newRef;
		else 
			return this;
	}			
	/**
	 * @see org.eclipse.update.core.IFeatureReference#getFeature()
	 */
	public IFeature getFeature() throws CoreException {
		return getFeature(false,null);
	}
}
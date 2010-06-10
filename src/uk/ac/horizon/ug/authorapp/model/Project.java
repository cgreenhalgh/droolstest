/**
 * 
 */
package uk.ac.horizon.ug.authorapp.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.List;

import org.drools.KnowledgeBase;
import org.drools.lang.descr.TypeDeclarationDescr;

import uk.ac.horizon.ug.exserver.DroolsUtils;
import uk.ac.horizon.ug.exserver.protocol.RulesetErrors;
import uk.ac.horizon.ug.exserver.protocol.TypeDescription;
import uk.ac.horizon.ug.exserver.DroolsSession.RulesetException;

/**
 * @author cmg
 *
 */
public class Project {
	/** project info */
	protected ProjectInfo projectInfo;
	/** changed */
	protected boolean changed;
	/** file */
	protected File file;
	/** ruleset errors */
	protected RulesetErrors[] rulesetErrors;
	/** knowledge base */
	protected KnowledgeBase kbase;
	/** type info */
	protected List<TypeDescription> types;
	/** listener support */
	protected PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
	/**
	 * @return the projectInfo
	 */
	public ProjectInfo getProjectInfo() {
		return projectInfo;
	}
	/**
	 * @param projectInfo the projectInfo to set
	 */
	public void setProjectInfo(ProjectInfo projectInfo) {
		this.projectInfo = projectInfo;
	}
	/**
	 * @return the changed
	 */
	public boolean isChanged() {
		return changed;
	}
	/**
	 * @param changed the changed to set
	 */
	public void setChanged(boolean changed) {
		this.changed = changed;
	}
	/**
	 * @return the file
	 */
	public File getFile() {
		return file;
	}
	/**
	 * @param file the file to set
	 */
	public void setFile(File file) {
		this.file = file;
	}
	/**
	 * @return the rulesetErrors
	 */
	public RulesetErrors[] getRulesetErrors() {
		return rulesetErrors;
	}
	/**
	 * @param rulesetErrors the rulesetErrors to set
	 */
	public void setRulesetErrors(RulesetErrors[] rulesetErrors) {
		this.rulesetErrors = rulesetErrors;
	}
	/**
	 * @return the kbase
	 */
	public KnowledgeBase getKbase() {
		return kbase;
	}
	/**
	 * @param kbase the kbase to set
	 */
	public void setKbase(KnowledgeBase kbase) {
		this.kbase = kbase;
	}
	/**
	 * @return the types
	 */
	public List<TypeDescription> getTypes() {
		return types;
	}
	/**
	 * @param types the types to set
	 */
	public void setTypes(List<TypeDescription> types) {
		this.types = types;
	}
	/** reload rules. Updates RulesetErrors.
	 * @return ok */
	public boolean reloadRuleFiles() {
		try {
			String ruleFileUrls [] = new String[getProjectInfo().getRuleFiles().size()];
			for (int i=0; i<ruleFileUrls.length; i++) 
				ruleFileUrls[i] = "file:///"+getProjectInfo().getRuleFiles().get(i);
			rulesetErrors = null;
			kbase = null;
			List<TypeDescription> oldTypes = types;
			types = null;
			kbase = DroolsUtils.getKnowledgeBase(ruleFileUrls);
			types = DroolsUtils.getTypeDescriptions(ruleFileUrls);
			propertyChangeSupport.firePropertyChange("types", oldTypes, types);
			if (types==null) 
				throw new RuntimeException("reloadRuleFiles created KnowledgeBase but not types");
			return true;
		} catch (RulesetException re) {
			rulesetErrors = DroolsUtils.getRulesetErros(re);
			return false;
		}
	}
	/**
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}
	/**
	 * @param propertyName
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}
	/**
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}
	/**
	 * @param propertyName
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(propertyName,
				listener);
	}
	/** get TypeDescription for client of given type name */
	public TypeDescription getClientTypeDescription(String name) {
		if (types==null)
			return null;
		for (TypeDescription type: types) {
			if (type.isClient() && name.equals(type.getTypeName()))
				return type;
		}
		return null;
	}
	
}

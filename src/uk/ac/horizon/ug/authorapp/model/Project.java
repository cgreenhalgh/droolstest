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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import uk.ac.horizon.ug.authorapp.FactStore;
import uk.ac.horizon.ug.exserver.DroolsUtils;
import uk.ac.horizon.ug.exserver.protocol.RulesetError;
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
	/** get configured project XStream */
	public static XStream getProjectXStream() {
		XStream xs = new XStream(new DomDriver());
		xs.alias("project", ProjectInfo.class);
		return xs;
	}
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
		if (changed)
			return true;
		if (projectInfo!=null) {
			List<FactStore> fss = projectInfo.getFactStores();
			for (FactStore fs : fss) 
				if (fs.isChanged())
					return true;
		}
		return false;
	}
	/**
	 * @param changed the changed to set
	 */
	public void setChanged(boolean changed) {
		this.changed = changed;
		if (projectInfo!=null) {
			List<FactStore> fss = projectInfo.getFactStores();
			for (FactStore fs : fss) 
				fs.setChanged(false);
		}
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
			for (int i=0; i<ruleFileUrls.length; i++) {
				if (getProjectInfo().getRuleFiles().get(i).indexOf(':')<4) {
					getProjectInfo().getRuleFiles().set(i, "file:///"+getProjectInfo().getRuleFiles().get(i));
					setChanged(true);
				}
				ruleFileUrls[i] = getProjectInfo().getRuleFiles().get(i);
			}
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
		} catch (Exception e) {
			rulesetErrors = new RulesetErrors[1];
			rulesetErrors[0] = new RulesetErrors();
			RulesetError errors[] = new RulesetError[1];
			errors[0] = new RulesetError();
			Throwable t = e;
			if (e instanceof RuntimeException && e.getCause()!=null)
				t = e.getCause();
			errors[0].setErrorType(t.getClass().getName());
			errors[0].setMessage(t.toString());
			errors[0].setLongMessage(t.getMessage());
			rulesetErrors[0].setErrors(errors);//setRulesetUrl();

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
	/** get TypeDescription for client of given type name */
	public TypeDescription getTypeDescription(String name) {
		if (types==null)
			return null;
		for (TypeDescription type: types) {
			if (name.equals(type.getTypeName()))
				return type;
		}
		return null;
	}
	
}

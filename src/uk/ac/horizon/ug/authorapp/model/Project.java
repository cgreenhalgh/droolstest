/**
 * 
 */
package uk.ac.horizon.ug.authorapp.model;

import java.io.File;

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
	
}

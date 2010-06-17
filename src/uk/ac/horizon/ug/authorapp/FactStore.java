/**
 * 
 */
package uk.ac.horizon.ug.authorapp;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.LinkedList;
import java.util.List;

import uk.ac.horizon.ug.exserver.devclient.Fact;

/** Store of Facts :-)
 * 
 * @author cmg
 *
 */
public class FactStore {
	/** name */
	protected String name;
	/** trivial implementation  */
	protected List<Fact> facts = new LinkedList<Fact>();
	protected transient boolean changed = false;
	/** listener support */
	protected transient PropertyChangeSupport propertyChangeSupport;
	protected  PropertyChangeSupport getPropertyChangeSupport() {
		if (propertyChangeSupport==null) 
			propertyChangeSupport = new PropertyChangeSupport(this);
		return propertyChangeSupport;
	}
	/** cons */
	public FactStore() {	
	}
	/**
	 * @param name
	 */
	public FactStore(String name) {
		super();
		this.name = name;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
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
	/** add fact */
	public void addFact(Fact fact) 
	{
		facts.add(fact);
		changed = true;
	}
	/** get all facts */
	public List<Fact> getFacts() {
		return facts;
	}
	/** get facts by type name */
	public List<Fact> getFacts(String typeName) {
		LinkedList<Fact> fs = new LinkedList<Fact>();
		for (Fact fact : facts) {
			if (typeName.equals(fact.getTypeName()))
				fs.add(fact);
		}	
		return fs;
	}
	/** get facts by type name, field name and field value */
	public List<Fact> getFacts(String typeName, String fieldName, Object fieldValue) {
		LinkedList<Fact> fs = new LinkedList<Fact>();
		for (Fact fact : facts) {
			if (typeName.equals(fact.getTypeName()) && fieldValue.equals(fact.getFieldValues().get(fieldName)))
				fs.add(fact);
		}	
		return fs;		
	}
	/** get one/first (?!) fact by type name, field name & value */
	public Fact getFact(String typeName, String fieldName, Object fieldValue) {
		List<Fact> fs = getFacts(typeName, fieldName, fieldValue);
		if (fs.size()==0)
			return null;
		return fs.get(0);
	}
	/** delete fact(s) */
	public void removeFact(Fact fact) {
		facts.remove(fact);
		changed = true;
	}
	/** remove facts by type name, field name and field value */
	public void removeFacts(String typeName, String fieldName, Object fieldValue) {
		List<Fact> fs = getFacts(typeName, fieldName, fieldValue);
		for (Fact fact : fs) {
			facts.remove(fact);
			changed = true;
		}
	}
	/** "update" a fact previously added/got (actually just mark changed for now) */
	public void updateFact(Fact fact) {
		changed = true;
	}
	public void fireFactsChanged() {
		getPropertyChangeSupport().firePropertyChange("facts", null, facts);
	}
	/**
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		getPropertyChangeSupport().addPropertyChangeListener(listener);
	}
	/**
	 * @param propertyName
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		getPropertyChangeSupport().addPropertyChangeListener(propertyName, listener);
	}
	/**
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		getPropertyChangeSupport().removePropertyChangeListener(listener);
	}
	/**
	 * @param propertyName
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		getPropertyChangeSupport().removePropertyChangeListener(propertyName,
				listener);
	}
}

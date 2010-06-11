/**
 * 
 */
package uk.ac.horizon.ug.authorapp;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/** Panel supporting choice of subset of items from a list.
 * 
 * @author cmg
 *
 */
public class ListSubsetPanel extends JPanel {
	/** property change support */
	protected PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
	/** unselected list */
	protected JList unselectedList;
	protected DefaultListModel unselectedListModel;
	/** selected list */
	protected JList selectedList;
	protected DefaultListModel selectedListModel;
	/**
	 * @param items
	 * @param selectedItems
	 */
	public ListSubsetPanel() {
		super(new GridBagLayout());
		
		unselectedListModel = new DefaultListModel();
		unselectedList = new JList(unselectedListModel);
		unselectedList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		selectedListModel = new DefaultListModel();
		selectedList = new JList(selectedListModel);
		selectedList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = c.weighty = 1;
		c.gridx = c.gridy = 0;
		c.gridheight = 2;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.BOTH;
		add(new JScrollPane(unselectedList), c);
		c.gridx++;
		c.gridheight = 1;
		c.fill = GridBagConstraints.NONE;
		add(new JButton(new AbstractAction("Include ->") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Object selected[] = unselectedList.getSelectedValues();				
				moveSelected(unselectedListModel, selectedListModel, selected, true);
				unselectedList.clearSelection();
			}
		}), c);
		c.gridy++;
		add(new JButton(new AbstractAction("<- Exclude") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Object selected[] = selectedList.getSelectedValues();
				moveSelected(selectedListModel, unselectedListModel, selected, false);
				selectedList.clearSelection();
			}
		}), c);
		c.gridx++;
		c.gridy = 0;
		c.gridheight = 2;
		c.fill = GridBagConstraints.BOTH;
		add(new JScrollPane(selectedList), c);
	}
	protected void moveSelected(DefaultListModel fromListModel,
			DefaultListModel toListModel, Object[] items, boolean selecting) {
		
		if (items==null || items.length==0)
			return;
		for (int i=0; i<items.length; i++) {
			fromListModel.removeElement(items[i]);
			int j=0;
			for (; j<toListModel.getSize(); j++) 
				if (((String)items[i]).compareTo((String)toListModel.getElementAt(j))<0)
					break;
			toListModel.add(j, items[i]);
		}
		propertyChangeSupport.firePropertyChange("selectedListModel", null, selectedListModel);
	}
	
	/**
	 * @return the unselectedListModel
	 */
	public DefaultListModel getUnselectedListModel() {
		return unselectedListModel;
	}
	/**
	 * @return the selectedListModel
	 */
	public DefaultListModel getSelectedListModel() {
		return selectedListModel;
	}
	
	public void clear() {
		selectedListModel.removeAllElements();
		unselectedListModel.removeAllElements();
	}
	/**
	 * @param arg0
	 * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(PropertyChangeListener arg0) {
		propertyChangeSupport.addPropertyChangeListener(arg0);
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(String arg0,
			PropertyChangeListener arg1) {
		propertyChangeSupport.addPropertyChangeListener(arg0, arg1);
	}
	/**
	 * @param arg0
	 * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(PropertyChangeListener arg0) {
		propertyChangeSupport.removePropertyChangeListener(arg0);
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(String arg0,
			PropertyChangeListener arg1) {
		propertyChangeSupport.removePropertyChangeListener(arg0, arg1);
	}
	
}

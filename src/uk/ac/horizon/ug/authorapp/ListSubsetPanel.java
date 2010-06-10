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
import java.beans.PropertyChangeSupport;

/** Panel supporting choice of subset of items from a list.
 * 
 * @author cmg
 *
 */
public class ListSubsetPanel extends JPanel {
	/** all items */
	protected List<String> items;
	/** selected items */
	protected List<String> selectedItems;
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
	public ListSubsetPanel(List<String> items, List<String> selectedItems) {
		super(new GridBagLayout());
		this.items = items;
		this.selectedItems = selectedItems;
		
		unselectedListModel = new DefaultListModel();
		unselectedList = new JList(unselectedListModel);
		unselectedList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		Collections.sort(items);
		for (Object item : items)
			if (!selectedItems.contains(item))
			unselectedListModel.addElement(item);
		
		selectedListModel = new DefaultListModel();
		selectedList = new JList(selectedListModel);
		selectedList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		Collections.sort(selectedItems);
		for (Object item : selectedItems)
			selectedListModel.addElement(item);
		
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
		List<String> oldItems = new LinkedList<String>();
		oldItems.addAll(this.items);
		for (int i=0; i<items.length; i++) {
			fromListModel.removeElement(items[i]);
			int j=0;
			for (; j<toListModel.getSize(); j++) 
				if (((String)items[i]).compareTo((String)toListModel.getElementAt(j))<0)
					break;
			toListModel.add(j, items[i]);
			if (selecting)
				this.items.add((String)items[i]);
			else
				this.items.remove((String)items[i]);
		}
		propertyChangeSupport.firePropertyChange("items", oldItems, items);
	}
	/**
	 * @return the items
	 */
	public List<String> getItems() {
		return items;
	}
	/**
	 * @param items the items to set
	 */
	public void setItems(List<String> items) {
		this.items = items;
	}
	/**
	 * @return the selectedItems
	 */
	public List<String> getSelectedItems() {
		return selectedItems;
	}
	/**
	 * @param selectedItems the selectedItems to set
	 */
	public void setSelectedItems(List<String> selectedItems) {
		this.selectedItems = selectedItems;
	}
	
}

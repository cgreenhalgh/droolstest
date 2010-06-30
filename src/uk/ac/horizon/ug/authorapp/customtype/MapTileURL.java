/**
 * 
 */
package uk.ac.horizon.ug.authorapp.customtype;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import uk.ac.horizon.ug.authorapp.EntityTableModel;

/** URL custom field type. (initial - test/demo)
 * 
 * @author cmg
 *
 */
public class MapTileURL implements CustomFieldType {
	static Logger logger = Logger.getLogger(MapTileURL.class.getName());

	/* (non-Javadoc)
	 * @see uk.ac.horizon.ug.authorapp.customtype.CustomFieldType#getFieldTypeName()
	 */
	@Override
	public String getFieldTypeName() {
		return "MapTileURL";
	}

	/* (non-Javadoc)
	 * @see uk.ac.horizon.ug.authorapp.customtype.CustomFieldType#getTableCellEditor()
	 */
	@Override
	public TableCellEditor getTableCellEditor() {
		// TODO Auto-generated method stub
		return new URLEditor();
	}

	/* (non-Javadoc)
	 * @see uk.ac.horizon.ug.authorapp.customtype.CustomFieldType#getTableCellRenderer()
	 */
	@Override
	public TableCellRenderer getTableCellRenderer() {
		// TODO Auto-generated method stub
		return null;
	}

	static class URLEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
		/** button - "editor" in place */
		JButton button;
		String currentValue;
		JDialog dialog;
		JTextField dialogTextField;
		protected static final String EDIT = "edit";
		protected static final String OK = "ok";
		protected static final String CHOOSE = "choose";

		/** cons */
		URLEditor() {
			button = new JButton();
	        button.setActionCommand(EDIT);
	        button.addActionListener(this);
	        button.setBorderPainted(false);

	        //Set up the dialog that the button brings up.
	        dialog = new JDialog();
	        dialog.setTitle("URL");
	        dialog.setModal(true);
	        JPanel p = new JPanel(new BorderLayout());
	        //p.setPreferredSize(new Dimension(600,400));
	        dialogTextField = new JTextField(80);
	        dialogTextField.addActionListener(this);
	        p.add(dialogTextField, BorderLayout.CENTER);
	        JPanel buttons = new JPanel(new FlowLayout());
	        JButton chooseButton = new JButton("Open Street Map...");
	        chooseButton.setActionCommand(CHOOSE);
	        chooseButton.addActionListener(this);
	        buttons.add(chooseButton);
	        JButton okButton = new JButton("OK");
	        okButton.setActionCommand(OK);
	        okButton.addActionListener(this);
	        buttons.add(okButton);
	        p.add(buttons, BorderLayout.SOUTH);
	        dialog.setContentPane(p);
	        dialog.pack();
	        
		}
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value,
				boolean arg2, int arg3, int arg4) {
			this.currentValue = (String)value;
			dialog.setLocationRelativeTo(table);
			return button;
		}

		@Override
		public Object getCellEditorValue() {
			return currentValue;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
		     if (EDIT.equals(e.getActionCommand())) {
		    	 //The user has clicked the cell, so
		    	 //bring up the dialog.
		    	 button.setText(currentValue);
		    	 dialogTextField.setText(currentValue);
		    	 dialog.validate();
		    	 dialog.setVisible(true);

		    	 fireEditingStopped(); //Make the renderer reappear.

		     } else if (OK.equals(e.getActionCommand())) { //User pressed dialog's "OK" button.
		    	 currentValue = dialogTextField.getText();
		    	 dialog.setVisible(false);
		     }
		     else if (CHOOSE.equals(e.getActionCommand())) {
		    	 String path = dialogTextField.getText();
		    	 // Java1.6!
		    	 try {
		    		 Desktop.getDesktop().browse(new URI(OPEN_STREET_MAP_URL));
		    	 }
		    	 catch (Exception ex) {
		    		 logger.log(Level.WARNING, "Unable to launch browser for "+OPEN_STREET_MAP_URL, e);
		    	 }
		     }
		}
	}
	static final String OPEN_STREET_MAP_URL = "http://dev.openstreetmap.de/staticmap/wizzard/";
}

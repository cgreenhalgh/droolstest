/**
 * 
 */
package uk.ac.horizon.ug.authorapp;

import java.awt.BorderLayout;
import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import uk.ac.horizon.ug.authorapp.model.Project;

/**
 * @author cmg
 *
 */
public class ProjectInfoPanel extends JPanel {
	/** Main app */
	protected Project project;
	protected JFileChooser ruleFileChooser = null;
	protected JTable ruleFileTable;
	protected RuleFileTableModel ruleFileTableModel = new RuleFileTableModel();
	/** get/make file chooser */
	JFileChooser getRuleFileChooser() {
		if (ruleFileChooser!=null)
			return ruleFileChooser;
		ruleFileChooser = new JFileChooser();
		return ruleFileChooser;
	}

	/** cons */
	public ProjectInfoPanel(Project project) {
		super(new BorderLayout());
		setProject(project);
		add(new JLabel("Rule files"), BorderLayout.NORTH);
		ruleFileTable = new JTable(ruleFileTableModel);
		add(new JScrollPane(ruleFileTable), BorderLayout.CENTER);
	}
	/** swing thread - refresh */
	public void setProject(Project project) {
		this.project = project;
		ruleFileTableModel.fireTableDataChanged();
	}
	/** swing thread */
	public void handleAddRuleFile() {
		int opt = getRuleFileChooser().showOpenDialog(this);
		if (opt!=JOptionPane.YES_OPTION)
			return;
		File newFile = ruleFileChooser.getSelectedFile();
		if (!newFile.canRead()) {
			JOptionPane.showMessageDialog(this, "Cannot read file "+newFile, "Add Rule File", JOptionPane.ERROR_MESSAGE);
			return;
		}
		List<String> currentFiles = project.getProjectInfo().getRuleFiles();
		for (String currentFile : currentFiles)
			if (currentFile.equals(newFile))
				return;
		currentFiles.add(newFile.getPath());
		ruleFileTableModel.fireTableRowsInserted(currentFiles.size()-1, currentFiles.size()-1);
	}
	
	/** rule file table model */
	class RuleFileTableModel extends AbstractTableModel {

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
		 */
		@Override
		public String getColumnName(int col) {
			switch (col) {
			case 0:
				return "File name";
			}
			return null;
		}

		@Override
		public int getColumnCount() {
			return 1;
		}

		@Override
		public int getRowCount() {
			if (project==null || project.getProjectInfo()==null || project.getProjectInfo().getRuleFiles()==null)
				return 0;
			// TODO Auto-generated method stub
			return project.getProjectInfo().getRuleFiles().size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			String filename = project.getProjectInfo().getRuleFiles().get(row);
			switch(col) {
			case 0:
				return filename;
			}
			return null;
		}
		
	}
}

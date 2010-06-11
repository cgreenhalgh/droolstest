/**
 * 
 */
package uk.ac.horizon.ug.authorapp;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import uk.ac.horizon.ug.authorapp.model.Project;
import uk.ac.horizon.ug.exserver.protocol.RulesetError;
import uk.ac.horizon.ug.exserver.protocol.RulesetErrors;

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
	protected JTable ruleErrorTable;
	protected RuleErrorTableModel ruleErrorTableModel = new RuleErrorTableModel();
	protected JSplitPane splitPane;
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
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		add(splitPane, BorderLayout.CENTER);
		JPanel top = new JPanel(new BorderLayout());
		splitPane.setTopComponent(top);
		splitPane.setResizeWeight(0.5);
		top.add(new JLabel("Rule files"), BorderLayout.NORTH);
		ruleFileTable = new JTable(ruleFileTableModel);
		JScrollPane sp = new JScrollPane(ruleFileTable);
		sp.setPreferredSize(new Dimension(500,300));
		top.add(sp, BorderLayout.CENTER);
		JPanel bottom = new JPanel(new BorderLayout());
		splitPane.setBottomComponent(bottom);
		bottom.add(new JLabel("Errors"), BorderLayout.NORTH);
		ruleErrorTable = new JTable(ruleErrorTableModel);
		bottom.add(new JScrollPane(ruleErrorTable), BorderLayout.CENTER);

		//splitPane.setDividerLocation(0.5);

		setProject(project);
	}
	/** swing thread - refresh */
	public void setProject(Project project) {
		this.project = project;
		ruleFileTableModel.fireTableDataChanged();
		handleReloadRules();
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
		handleReloadRules();
	}
	/** swing thread */
	public void handleReloadRules() {		
		Cursor c = this.getCursor();
		try {
			setCursor(Cursor.getPredefinedCursor(JFrame.WAIT_CURSOR));
			if (project!=null)
				project.reloadRuleFiles();
			ruleErrorTableModel.fireTableDataChanged();
		}
		finally {
			setCursor(c);
		}
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
	
	static String ERROR_COLUMNS [] = new String[] {"Rule file", "Line", "Error", "Message"};
	/** rule error model */
	class RuleErrorTableModel extends AbstractTableModel {
		
		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
		 */
		@Override
		public String getColumnName(int col) {
			return ERROR_COLUMNS[col];
		}

		@Override
		public int getColumnCount() {
			return ERROR_COLUMNS.length;
		}

		@Override
		public int getRowCount() {
			if (project==null || project.getProjectInfo()==null || project.getRulesetErrors()==null)
				return 0;
			int row = 0;
			RulesetErrors ress [] = project.getRulesetErrors();
			for (int i=0; i<ress.length; i++)
				row += ress[i].getErrors().length;
			return row;
		}

		@Override
		public Object getValueAt(int row, int col) {
			RulesetErrors ress [] = project.getRulesetErrors();
			for (int i=0; i<ress.length; i++) {
				RulesetError res[] = ress[i].getErrors();
				if (row<res.length) {
					switch(col) {
					case 0: 
						// file
						return ress[i].getRulesetUrl();
					case 1: {
						// lines
						StringBuilder sb = new StringBuilder();
						int [] lines = res[row].getErrorLines();
						for (int j=0; lines!=null && j<lines.length; j++)
						{
							if (j>0)
								sb.append(",");
							sb.append(lines[j]);
						}
						return sb.toString();
					}
					case 2:
						// type
						return res[row].getErrorType();
					case 3:
						// message
						return res[row].getMessage();
					}
					// default
					return null;
				}
				row -= res.length;
			}			
			return null;
		}
		
	}
}

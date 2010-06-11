/**
 * 
 */
package uk.ac.horizon.ug.authorapp;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.ac.horizon.ug.authorapp.model.*;
import uk.ac.horizon.ug.exserver.protocol.TypeDescription;

/** Desktop authoring tool (work in progress)
 * 
 * @author cmg
 *
 */
public class Main {
	static Logger logger = Logger.getLogger(Main.class.getName());
	
	private static final int DEFAULT_WIDTH = 800;
	private static final int DEFAULT_HEIGHT = 600;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new Main().createGui();
	}
	/** main frame */
	protected JFrame mainFrame;
	/** tabbed pane */
	protected JTabbedPane tabbedPane;
	/** current project */
	protected Project project = null;
	
	/** create gui */
	void createGui() {
		mainFrame = new JFrame("Horizon/UrbanGames/AuthorApp");
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				handleExit();
			}			
		});
		mainFrame.setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		JMenuBar menuBar = new JMenuBar();
		mainFrame.setJMenuBar(menuBar);
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		fileMenu.add(new JMenuItem(new AbstractAction("New Project") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO changed? ....
				newProject();				
			}
		}));
		fileMenu.add(new JMenuItem(new AbstractAction("Load") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO changed?
				loadProject();
			}
		}));

		fileMenu.add(new JMenuItem(new AbstractAction("Save") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				saveProject();				
			}
		}));
		fileMenu.add(new JMenuItem(new AbstractAction("Save as...") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				saveAsProject();
			}
		}));
		fileMenu.add(new JMenuItem(new AbstractAction("Exit") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (JOptionPane.showConfirmDialog(mainFrame, "Exit?", "Exit", JOptionPane.YES_NO_CANCEL_OPTION)!=JOptionPane.YES_OPTION)
					return;
				handleExit();
			}
		}));
		
		// tabs
		tabbedPane = new JTabbedPane();
		mainFrame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
		projectInfoPanel = new ProjectInfoPanel(project);
		tabbedPane.add("Rule Files", projectInfoPanel);
		
		JMenu editMenu = new JMenu("Edit");
		menuBar.add(editMenu);
		editMenu.add(new JMenuItem(new AbstractAction("Add rule file...") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				tabbedPane.setSelectedComponent(projectInfoPanel);
				projectInfoPanel.handleAddRuleFile();
			}
		}));
		editMenu.add(new JMenuItem(new AbstractAction("Reload rules") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				tabbedPane.setSelectedComponent(projectInfoPanel);
				projectInfoPanel.handleReloadRules();
			}
		}));
		editMenu.add(new JMenuItem(new AbstractAction("New client type...") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				newClientType();
			}
		}));

		JMenu viewMenu = new JMenu("View");
		menuBar.add(viewMenu);
		
		browserPanel = new BrowserPanel(project);
		tabbedPane.add("Types", browserPanel);
		viewMenu.add(new JMenuItem(browserPanel.getViewAction(this)));

		mainFrame.pack();
		mainFrame.setVisible(true);
		
		// initial project
		newProject();
	}
	protected void handleExit() {
		if (project.isChanged()) {
			int opt = JOptionPane.showConfirmDialog(mainFrame, "Project has changed; save?", "Load", JOptionPane.YES_NO_CANCEL_OPTION);
			if (opt==JOptionPane.CANCEL_OPTION)
				return;
			if (opt==JOptionPane.YES_OPTION)
				if (!saveProject())
					// failed save
					return;
			// otherwise ok (no/saved)
		}
		System.exit(0);
	}
	/** post dialog to collect a String */
	public static String getUserInput(Frame parent, String question, String title, String defaultValue) {
		if (defaultValue!=null)
			return JOptionPane.showInputDialog(parent, question, defaultValue);
		else
			return JOptionPane.showInputDialog(parent, question);
		/*
		final JDialog dialog = new JDialog(parent, title, true);
		dialog.setLayout(new BorderLayout());
		dialog.add(new JLabel(question), BorderLayout.NORTH);
		final JTextField textField = new JTextField(80);
		textField.setEditable(true);
		if (defaultValue!=null)
			textField.setText(defaultValue);
		final boolean confirmed[] = new boolean[1];
		AbstractAction ok = new AbstractAction("OK") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				confirmed[0] = true;
				dialog.setVisible(false);
			}			
		};
		textField.setAction(ok);
		dialog.add(new JButton(ok), BorderLayout.SOUTH);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
		if (!confirmed[0])
			return null;
		return textField.getText();
		*/
	}
	/** create a new client type... */
	protected void newClientType() {
		getname:
		while(true) {
			String name = getUserInput(mainFrame, "New Client Type Name:", "New Client Type", null);
			if (name==null || name.length()==0)
				return;
			List<ClientTypeInfo> clientTypes = project.getProjectInfo().getClientTypes();
			for (ClientTypeInfo clientType : clientTypes) {
				if (name.equals(clientType.getName())) {
					JOptionPane.showMessageDialog(mainFrame, "Client type "+name+" already exists", "New Client Type", JOptionPane.ERROR_MESSAGE);
					openClientTypePanel(clientType);
					continue getname;
				}
			}
			ClientTypeInfo clientType = new ClientTypeInfo(name);
			project.getProjectInfo().getClientTypes().add(clientType);
			project.setChanged(true);
			openClientTypePanel(clientType);
			return;
		}		
	}
	protected ProjectInfoPanel projectInfoPanel;
	protected BrowserPanel browserPanel;
	
	protected JFileChooser projectFileChooser = null;
	/** get/make file chooser */
	JFileChooser getProjectFileChooser() {
		if (projectFileChooser!=null)
			return projectFileChooser;
		projectFileChooser = new JFileChooser();
		return projectFileChooser;
	}
	
	/** get configured project XStream */
	XStream getProjectXStream() {
		XStream xs = new XStream(new DomDriver());
		xs.alias("project", ProjectInfo.class);
		return xs;
	}
	/** save - swing thread */
	protected boolean saveProject() {
		if (project.getFile()==null) {
			return saveAsProject();
		}
		try {
			XStream xs = getProjectXStream();
			FileOutputStream fos = new FileOutputStream(project.getFile());
			xs.toXML(project.getProjectInfo(), fos);
			fos.close();
			this.project.setChanged(false);
			return true;
		}
		
		catch (Exception e) {
			logger.log(Level.WARNING,"Error saving project to "+project.getFile(), e);
			JOptionPane.showMessageDialog(mainFrame, "Error saving project:\n"+e, "Save", JOptionPane.WARNING_MESSAGE);
			return false;
		}
	}

	void closeProjectInternal() {
		project = new Project();
		for (ClientTypePanel clientTypePanel : clientTypePanels.values()) {
			tabbedPane.remove(clientTypePanel);
		}
		for (EntityTablePanel entityTablePanel : entityTablePanels.values()) {
			tabbedPane.remove(entityTablePanel);
		}
	}
	protected boolean saveAsProject() {
		JFileChooser fileChooser = getProjectFileChooser();
		if (project.getFile()!=null)
			fileChooser.setSelectedFile(project.getFile());
		// loop in case repeat file choice
		while (true) {
			if (fileChooser.showSaveDialog(mainFrame)!=JFileChooser.APPROVE_OPTION)
				return false;
			File file = fileChooser.getSelectedFile();
			if ((file.exists() && (!file.isFile() || !file.canWrite())) || (!file.exists() && (!file.getParentFile().isDirectory()))) {
				JOptionPane.showMessageDialog(mainFrame, "Cannot write to file "+file, "Save", JOptionPane.ERROR_MESSAGE);
				continue;
			}
			if (file.exists()) {
				int opt = JOptionPane.showConfirmDialog(mainFrame, "Replace file "+file+"?", "Save", JOptionPane.YES_NO_CANCEL_OPTION);
				if (opt==JOptionPane.CANCEL_OPTION)
					return false;
				if (opt==JOptionPane.NO_OPTION)
					continue;				
			}
			project.setFile(file);
			return saveProject();
		}		
	}

	protected void loadProject() {
		if (project.isChanged()) {
			int opt = JOptionPane.showConfirmDialog(mainFrame, "Project has changed; save?", "Load", JOptionPane.YES_NO_CANCEL_OPTION);
			if (opt==JOptionPane.CANCEL_OPTION)
				return;
			if (opt==JOptionPane.YES_OPTION)
				if (!saveProject())
					// failed save
					return;
			// otherwise ok (no/saved)
		}
		JFileChooser fileChooser = getProjectFileChooser();
		if (project.getFile()!=null)
			fileChooser.setSelectedFile(project.getFile());
		if (fileChooser.showOpenDialog(mainFrame)!=JFileChooser.APPROVE_OPTION)
			return;
		XStream xs = getProjectXStream();
		File file = fileChooser.getSelectedFile();
		// clear
		closeProjectInternal();
		project = new Project();
		try {
			FileInputStream fis = new FileInputStream(file);
			// this might fail
			project.setProjectInfo((ProjectInfo)xs.fromXML(fis));
			// if not these will be ok
			project.setFile(file);
			project.setChanged(false);
			fis.close();
			refresh();
		}
		catch (Exception e) {
			logger.log(Level.WARNING,"Error reading project "+file, e);
			JOptionPane.showMessageDialog(mainFrame, "Error reading "+file+":\n"+e, "Load", JOptionPane.ERROR_MESSAGE);
			newProject();
		}		
	}

	/** new project - swing thread */
	void newProject() {
		if (project!=null && project.isChanged()) {
			int opt = JOptionPane.showConfirmDialog(mainFrame, "Project has changed; save?", "Load", JOptionPane.YES_NO_CANCEL_OPTION);
			if (opt==JOptionPane.CANCEL_OPTION)
				return;
			if (opt==JOptionPane.YES_OPTION)
				if (!saveProject())
					// failed save
					return;
			// otherwise ok (no/saved)
		}
		closeProjectInternal();
		project = new Project();
		project.setProjectInfo(new ProjectInfo());
		project.getProjectInfo().setName("");
		project.setChanged(false);
		project.setFile(null);
		// TODO refresh....
		refresh();
	}
	
	void refresh() {
		projectInfoPanel.setProject(project);
		browserPanel.setProject(project);
		for (ClientTypePanel clientTypePanel : clientTypePanels.values()) {
			String name = clientTypePanel.getName();
			clientTypePanel.refresh(project);
		}
		for (ClientTypeInfo clientTypeInfo : project.getProjectInfo().getClientTypes()) {
			if (!clientTypePanels.containsKey(clientTypeInfo.getName()))
				openClientTypePanel(clientTypeInfo);
		}
	}

	/** client panels, key by type name */
	protected Map<String,ClientTypePanel> clientTypePanels = new HashMap<String,ClientTypePanel>();
	/** open/to front client panel for type - swing thread */
	public void openClientTypePanel(ClientTypeInfo clientType) {
		String name = clientType.getName();
		if (clientTypePanels.containsKey(name)) {
			tabbedPane.setSelectedComponent(clientTypePanels.get(name));
			return;
		}
		ClientTypePanel clientTypePanel = new ClientTypePanel(clientType, project);
		clientTypePanels.put(name, clientTypePanel);
		tabbedPane.add(name,clientTypePanel);
		tabbedPane.setSelectedComponent(clientTypePanel);		
	}
	/** client panels, key by type name */
	protected Map<String,EntityTablePanel> entityTablePanels = new HashMap<String,EntityTablePanel>();
	/** open/to front client panel for type - swing thread */
	public void openEntityTablePanel(TypeDescription type) {
		String name = type.getTypeName();
		if (entityTablePanels.containsKey(name)) {
			tabbedPane.setSelectedComponent(entityTablePanels.get(name));
			return;
		}
		EntityTablePanel entityTablePanel = new EntityTablePanel(name, project, type);
		entityTablePanels.put(name, entityTablePanel);
		tabbedPane.add(name,entityTablePanel);
		tabbedPane.setSelectedComponent(entityTablePanel);		
	}
}

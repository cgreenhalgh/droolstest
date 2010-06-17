/**
 * 
 */
package uk.ac.horizon.ug.authorapp.session;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.transaction.UserTransaction;

import org.drools.KnowledgeBase;
import org.drools.definition.type.FactField;
import org.drools.definition.type.FactType;
import org.restlet.Component;
import org.restlet.data.Protocol;

import bitronix.tm.resource.jdbc.PoolingDataSource;

import uk.ac.horizon.ug.authorapp.BrowserPanel;
import uk.ac.horizon.ug.authorapp.BrowserPanelCallback;
import uk.ac.horizon.ug.authorapp.EntityTablePanel;
import uk.ac.horizon.ug.authorapp.FactStore;
import uk.ac.horizon.ug.authorapp.Main;
import uk.ac.horizon.ug.authorapp.model.ClientTypeInfo;
import uk.ac.horizon.ug.authorapp.model.Project;
import uk.ac.horizon.ug.authorapp.model.ProjectInfo;
import uk.ac.horizon.ug.exserver.DroolsSession;
import uk.ac.horizon.ug.exserver.RawSessionResource;
import uk.ac.horizon.ug.exserver.RestletApplication;
import uk.ac.horizon.ug.exserver.SessionResource;
import uk.ac.horizon.ug.exserver.DroolsSession.RulesetException;
import uk.ac.horizon.ug.exserver.clientapi.RegisterClientHandler;
import uk.ac.horizon.ug.exserver.devclient.Fact;
import uk.ac.horizon.ug.exserver.model.ClientConversation;
import uk.ac.horizon.ug.exserver.model.ConversationStatus;
import uk.ac.horizon.ug.exserver.model.Session;
import uk.ac.horizon.ug.exserver.model.SessionTemplate;
import uk.ac.horizon.ug.exserver.model.SessionType;
import uk.ac.horizon.ug.exserver.protocol.Operation;
import uk.ac.horizon.ug.exserver.protocol.RawFactHolder;
import uk.ac.horizon.ug.exserver.protocol.TypeDescription;

/**
 * @author cmg
 *
 */
public class SessionFrame extends JFrame implements BrowserPanelCallback {
	static Logger logger = Logger.getLogger(SessionFrame.class.getName());
	private static final int DEFAULT_WIDTH = 800;
	private static final int DEFAULT_HEIGHT = 600;
	protected JTabbedPane tabbedPane;
	
	/** session */
	Session session;
	/** drools session */
	DroolsSession droolsSession;
	/** our fake project
	 */
	protected Project fakeProject;
	/** project info */
	ProjectInfo projectInfo;
	/** client types */
	List<ClientTypeInfo> clientTypes;
	/** session fact store */
	protected SessionFactStore sessionFactStore;
	
	/** cons */
	public SessionFrame(JFrame parent, Project project) {
		super("Session");
		
		if (currentFrame!=null) {
			JOptionPane.showMessageDialog(parent, "Session already open (only one can be active at once)", "New Session", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		// try reading project info
		try {
			projectInfo = DroolsSession.readProjectInfo(project.getFile().toURI().toString());
		}
		catch (Exception e) {
			logger.log(Level.WARNING, "Error loading project "+project.getFile(), e);
			JOptionPane.showMessageDialog(parent, "Problem re-loading project\n"+e, "New Session", JOptionPane.ERROR_MESSAGE);
			return;			
		}
		fakeProject = new Project();
		fakeProject.setProjectInfo(projectInfo);
		if (!fakeProject.reloadRuleFiles()) 
		{
			JOptionPane.showMessageDialog(parent, "Error parsing rules\n", "New Session", JOptionPane.ERROR_MESSAGE);
			return;						
		}
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {

			/* (non-Javadoc)
			 * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
			 */
			@Override
			public void windowClosing(WindowEvent arg0) {
				// TODO Auto-generated method stub
				super.windowClosing(arg0);
				handleClose();
			}			
		});
		setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));

		JMenuBar menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		fileMenu.add(new JMenuItem(new AbstractAction("Exit") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				handleClose();
			}			
		}));

		JMenu viewMenu = new JMenu("View");
		menuBar.add(viewMenu);
				
		JMenu clientMenu = new JMenu("Client");
		menuBar.add(clientMenu);
		clientMenu.add(new JMenuItem(new AbstractAction("Register new...") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				registerNewClient();
			}
		}));
		
		tabbedPane = new JTabbedPane();
		getContentPane().add(tabbedPane, BorderLayout.CENTER);

		eventsPanel = new EventsPanel();
		tabbedPane.add("Events", eventsPanel);		

		// initialise
		if (!init(parent, project))
			return;
		
		sessionFactStore = new SessionFactStore("session:"+sessionId, droolsSession.getKsession());
	
		// must make properties panel after init!
		PropertiesPanel pp = new PropertiesPanel(this);
		tabbedPane.add("Properties", pp);
		
		BrowserPanel browserPanel = new BrowserPanel(fakeProject);
		tabbedPane.add("Types", browserPanel);
		viewMenu.add(new JMenuItem(browserPanel.getViewAction(this, true)));
		viewMenu.add(new JMenuItem(new AbstractAction("Refresh Session Views") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				logger.info("Reloading session facts");
				sessionFactStore.refresh();
			}
		}));
		
		pack();
		setVisible(true);
		currentFrame = this;
	}
	/** client panels, key by type name */
	protected Map<String,EntityTablePanel> entityTablePanels = new HashMap<String,EntityTablePanel>();
	/** open/to front client panel for type - swing thread */
	@Override
	public void openEntityTablePanel(TypeDescription type) {
		String name = type.getTypeName();
		if (entityTablePanels.containsKey(name)) {
			tabbedPane.setSelectedComponent(entityTablePanels.get(name));
			return;
		}
		EntityTablePanel entityTablePanel = new EntityTablePanel(name, fakeProject, type, this.sessionFactStore, false);
		entityTablePanels.put(name, entityTablePanel);
		tabbedPane.add("Entity: "+name,entityTablePanel);
		tabbedPane.setSelectedComponent(entityTablePanel);		
	}

	protected void registerNewClient() {
		final JDialog dialog = new JDialog();
		dialog.setTitle("Register new client");
		dialog.setLocationRelativeTo(this);
		dialog.setModal(true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 0;
		c.weighty = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(new JLabel("Client ID"), c);
		c.gridx = 1;
		c.weightx = 1;
		final JTextField clientIdField = new JTextField(30);
		panel.add(clientIdField, c);
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 0;
		panel.add(new JLabel("Client Type"), c);
		c.gridx = 1;
		c.weightx = 1;
		Vector<String> clientTypeNames = new Vector<String>();
		for (ClientTypeInfo cti : clientTypes) {
			clientTypeNames.addElement(cti.getName());
		}
		if (clientTypeNames.size()==0) {
			JOptionPane.showMessageDialog(this, "This project does not include any client types", "Register new client", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		final JComboBox clientTypeCombo = new JComboBox(clientTypeNames);
		clientTypeCombo.setEditable(false);
		panel.add(clientTypeCombo, c);
		
		dialog.getContentPane().setLayout(new BorderLayout());
		dialog.getContentPane().add(panel, BorderLayout.CENTER);
		
		JPanel buttons = new JPanel(new FlowLayout());
		buttons.add(new JButton(new AbstractAction("OK") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				String clientId = clientIdField.getText();
				String clientType = (String)clientTypeCombo.getSelectedItem();
				if (clientId==null || clientId.length()==0 || clientType==null) {
					JOptionPane.showMessageDialog(dialog, "Please enter client ID and type", "Register new client", JOptionPane.ERROR_MESSAGE);
					return;
				}
				registerNewClient(clientId, clientType);
				dialog.setVisible(false);
			}
		}));
		dialog.getContentPane().add(buttons, BorderLayout.SOUTH);
		
		dialog.pack();
		dialog.setVisible(true);
		dialog.dispose();
	}

	protected void registerNewClient(String clientId, String clientType) {
		ClientConversation conversation = new ClientConversation();
		conversation.setClientId(clientId);
		conversation.setClientType(clientType);
		conversation.setConversationId(newClientId());
		conversation.setStatus(ConversationStatus.ACTIVE);
		conversation.setSessionId(sessionId);
		//conversation.setSessionId("0");
		// TODO Auto-generated method stub
		boolean ok = false;
		try {
			ok = RegisterClientHandler.registerInternal(conversation);
		}
		catch (Exception e) {
			logger.log(Level.WARNING, "registering new client", e);
			ok = false;
		}
		if (!ok)
			JOptionPane.showMessageDialog(this, "Problem registering new client", "Register new client", JOptionPane.ERROR_MESSAGE);
		else {
			showClientFrame(conversation);
		}
	}

	private void showClientFrame(ClientConversation conversation) {
		ClientPanel cp  = new ClientPanel(conversation);
		tabbedPane.add("Client "+conversation.getClientType()+" "+conversation.getClientId(), cp);
		tabbedPane.setSelectedComponent(cp);
	}

	private String newClientId() {
		// TODO proper GUID
		return ""+System.currentTimeMillis();
	}

	protected EventsPanel eventsPanel;
	
	static SessionFrame currentFrame = null;
	
	protected void handleClose() {
		// stop server...
		currentFrame = null;
		try {
			SessionResource.setGlobalSession(null, null);
		}
		catch (Exception e) {
			logger.log(Level.WARNING, "Problem stopping application", e);
		}
		if (eventsPanel!=null)
			eventsPanel.dispose();
		if (droolsSession!=null)
			droolsSession.getKsession().dispose();

		dispose();		
	}
	/** Restlet server */
	protected static Component restletComponent;
	protected static String serverUrl;
	/** get server url */
	public String getServerUrl() {
		return serverUrl;
	}
	/** session id */
	protected String sessionId;
	private boolean init(JFrame parent, Project project) {
	
		if (!oneTimeInitialise(parent))
			return false;
		
		clientTypes = projectInfo.getClientTypes();
		
		String templateName = "unnamed";
		if (project.getFile()!=null)
			templateName = project.getFile().getName();
		// create session
		SessionTemplate template = new SessionTemplate();
		template.setName(templateName);
		template.setProjectUrl(project.getFile().toURI().toString());
		
		session = new Session();
		session.setCreatedDate(new Date());
		session.setDroolsId(0);
		sessionId = ""+System.currentTimeMillis();
		session.setId(sessionId);
		session.setProjectUrl(template.getProjectUrl());
		session.setSessionType(SessionType.TRANSIENT);
		session.setTemplateName(template.getName());
		session.setUpdateSystemTime(true);
		
		try {
			// this seems to be needed first to set things up
			UserTransaction ut =
				  (UserTransaction) new InitialContext().lookup( "java:comp/UserTransaction" );
			// then this - configured by authorapp/META-INF/persistence.xml
			EntityManagerFactory emf = Persistence.createEntityManagerFactory( "droolstest" );
			EntityManager em = emf.createEntityManager();

			ut.begin();
			em.joinTransaction();
			if (em.find(SessionTemplate.class, template.getName())!=null) {
				logger.info("SessionTemplate already present");
			}
			else {
				em.persist(template);
			}
			em.persist(session);
			ut.commit();
			em.close();
		}
		catch (Exception e) {
			// NamingException, RulesetException
			logger.log(Level.WARNING,"Error persisting session info", e);
			JOptionPane.showMessageDialog(parent, "Unable to create session info\n"+e, "New Session", JOptionPane.ERROR_MESSAGE);
			return false;			
		}
		
		try {
			// NB requires Transaction manager - ensure jndi.properties is in class path to 
			// configure use of Bitronix JNDI implementation
			droolsSession = DroolsSession.createSessionNoFacts(template, session.getSessionType(), session.isLogged(), session.getLogId());
			// listener(s)
			if(eventsPanel!=null)
				eventsPanel.setKsession(droolsSession.getKsession());
			droolsSession.loadFacts();
//			droolsSession.getKsession().addEventListener(arg0);
		} catch (Exception e) {
			// NamingException, RulesetException
			logger.log(Level.WARNING,"Error creating drools session", e);
			JOptionPane.showMessageDialog(parent, "Unable to create drools session\n"+e, "New Session", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		return true;
	}


	static boolean oneTimeInitialised = false;
	private static synchronized boolean oneTimeInitialise(JFrame parent) {
		// TODO Auto-generated method stub
		if (oneTimeInitialised)
			return true;
		
		// set up JNDI jdbc/db1 - done by authorapp/btm-config.properties -> authorapp/resources.properties
		
		// start server
	    // Create a new Component.   
	    restletComponent = new Component();   
	  
	    // Add a new HTTP server listening on port 8182.   
	    restletComponent.getServers().add(Protocol.HTTP, 8182);   

	    // Attach the sample application.   
	    restletComponent.getDefaultHost().attach("/droolstest/1",   
	            new RestletApplication());   

	    SessionResource.setGlobalSession(null, null);

	    // Start the component.   
	    try {
	    	restletComponent.start();
	    	serverUrl = "http://localhost:8182/droolstest/";
	    	logger.info("Server started on port 8181, path /droolstest");
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error starting server", e);
			JOptionPane.showMessageDialog(parent, "Unable to create server\n"+e, "New Session", JOptionPane.ERROR_MESSAGE);
			return false;
		}   
		try {
//			Hashtable env = new Hashtable();
//			env.put(Context.INITIAL_CONTEXT_FACTORY, "bitronix.tm.jndi.BitronixInitialContextFactory");
//			Context ctx = new InitialContext(env);
			new InitialContext();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error doing one-time initialisation", e);
			JOptionPane.showMessageDialog(parent, "Unable to create session\n"+e, "New Session", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		oneTimeInitialised = true;
		return true;
	}
}

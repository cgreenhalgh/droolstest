/**
 * 
 */
package uk.ac.horizon.ug.authorapp.session;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.transaction.UserTransaction;

import org.restlet.Component;
import org.restlet.data.Protocol;

import bitronix.tm.resource.jdbc.PoolingDataSource;

import uk.ac.horizon.ug.authorapp.Main;
import uk.ac.horizon.ug.authorapp.model.Project;
import uk.ac.horizon.ug.exserver.DroolsSession;
import uk.ac.horizon.ug.exserver.RestletApplication;
import uk.ac.horizon.ug.exserver.SessionResource;
import uk.ac.horizon.ug.exserver.DroolsSession.RulesetException;
import uk.ac.horizon.ug.exserver.model.Session;
import uk.ac.horizon.ug.exserver.model.SessionTemplate;
import uk.ac.horizon.ug.exserver.model.SessionType;

/**
 * @author cmg
 *
 */
public class SessionFrame extends JFrame {
	static Logger logger = Logger.getLogger(SessionFrame.class.getName());
	private static final int DEFAULT_WIDTH = 800;
	private static final int DEFAULT_HEIGHT = 600;
	protected JTabbedPane tabbedPane;
	
	/** session */
	Session session;
	/** drools session */
	DroolsSession droolsSession;
	
	/** cons */
	public SessionFrame(JFrame parent, Project project) {
		super("Session");
		if (currentFrame!=null) {
			JOptionPane.showMessageDialog(parent, "Session already open (only one can be active at once)", "New Session", JOptionPane.ERROR_MESSAGE);
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

		tabbedPane = new JTabbedPane();
		getContentPane().add(tabbedPane, BorderLayout.CENTER);

		// initialise
		if (!init(parent, project))
			return;
		
		pack();
		setVisible(true);
		currentFrame = this;
	}

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
		if (droolsSession!=null)
			droolsSession.getKsession().dispose();
		dispose();		
	}
	/** Restlet server */
	protected static Component restletComponent;
	
	private boolean init(JFrame parent, Project project) {
	
		if (!oneTimeInitialise(parent))
			return false;
		
		String templateName = "unnamed";
		if (project.getFile()!=null)
			templateName = project.getFile().getName();
		// create session
		SessionTemplate template = new SessionTemplate();
		template.setName(templateName);
		if (project.getFile()!=null)
			template.setFactUrls(new String[] { project.getFile().getPath() });
		else
			template.setFactUrls(new String[] { "unnamed" });
		template.setRulesetUrls(project.getProjectInfo().getRuleFiles().toArray(new String[0]));
		
		Session session = new Session();
		session.setCreatedDate(new Date());
		session.setDroolsId(0);
		session.setId("0");
		session.setRulesetUrls(template.getRulesetUrls());
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
			em.persist(template);
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
			droolsSession = DroolsSession.createSession(template, session.getSessionType(), session.isLogged(), session.getLogId());
//			SessionResource.setGlobalSession(session, droolsSession);
		} catch (Exception e) {
			// NamingException, RulesetException
			logger.log(Level.WARNING,"Error creating drools session", e);
			JOptionPane.showMessageDialog(parent, "Unable to create drools session\n"+e, "New Session", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		// TODO Auto-generated method stub
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

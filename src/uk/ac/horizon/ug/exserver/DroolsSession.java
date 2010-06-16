/**
 * 
 */
package uk.ac.horizon.ug.exserver;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.definition.KnowledgePackage;
import org.drools.definition.type.FactField;
import org.drools.definition.type.FactType;
import org.drools.event.rule.DebugAgendaEventListener;
import org.drools.io.ResourceFactory;
import org.drools.logger.KnowledgeRuntimeLogger;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.persistence.jpa.JPAKnowledgeService;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.KnowledgeBaseConfiguration;

import com.thoughtworks.xstream.XStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.swing.JOptionPane;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import uk.ac.horizon.ug.authorapp.FactStore;
import uk.ac.horizon.ug.authorapp.model.Project;
import uk.ac.horizon.ug.authorapp.model.ProjectInfo;
import uk.ac.horizon.ug.exserver.devclient.Fact;
import uk.ac.horizon.ug.exserver.model.SessionTemplate;
import uk.ac.horizon.ug.exserver.model.Session;
import uk.ac.horizon.ug.exserver.model.SessionType;
import uk.ac.horizon.ug.exserver.protocol.Operation;
import uk.ac.horizon.ug.exserver.protocol.RawFactHolder;

/**
 * @author cmg
 *
 */
public class DroolsSession {
	static Logger logger = Logger.getLogger(DroolsSession.class.getName());
	/** default log file directory */
	protected static String logFileDir;
	
	/**
	 * @return the logFileDir
	 */
	public static String getLogFileDir() {
		return logFileDir;
	}
	/**
	 * @param logFileDir the logFileDir to set
	 */
	public static void setLogFileDir(String logFileDir) {
		DroolsSession.logFileDir = logFileDir;
	}
	/** drools knowledge session */
	protected StatefulKnowledgeSession ksession;
	/**
	 * @return the ksession
	 */
	public StatefulKnowledgeSession getKsession() {
		return ksession;
	}
	/** get drools id */
	public int getId() {
		return ksession.getId();
	}
	/** logged */
	protected boolean logged;
	/** map of DroolsSessions (weak refs) */
	protected static Map<Integer,DroolsSession> sessions = new HashMap<Integer,DroolsSession>();
	/** project info */
	protected ProjectInfo projectInfo;
	/**
	 * @return the projectInfo
	 */
	public ProjectInfo getProjectInfo() {
		return projectInfo;
	}
	/** read project info 
	 * @throws URISyntaxException */
	protected ProjectInfo readProjectInfo(String projectUrl) throws IOException {
		ProjectInfo projectInfo = null;
		try {
			File projectFile = new File(new URI(projectUrl));
			XStream xs = Project.getProjectXStream();
			FileInputStream fis = new FileInputStream(projectFile);
			// this might fail
			projectInfo = ((ProjectInfo)xs.fromXML(fis));
			// if not these will be ok
			fis.close();
		}
		catch (Exception e) {
			logger.log(Level.WARNING,"Error reading project "+projectUrl, e);
			throw new IOException("Error reading project "+projectUrl, e);
		}		
		return projectInfo;
	}
	/** create a new drools session 
	 * @param logged 
	 * @throws NamingException 
	 * @throws RulesetException 
	 * @throws IOException 
	 * @throws NotSupportedException 
	 * @throws SystemException 
	 * @throws SecurityException 
	 * @throws IllegalStateException */
	public synchronized static DroolsSession createSession(SessionTemplate template, SessionType sessionType, boolean logged, int logId) throws NamingException, RulesetException, IOException, IllegalStateException, SecurityException, SystemException, NotSupportedException {
		DroolsSession ds = new DroolsSession(template.getProjectUrl(), true, 0, sessionType, logged);
		ds.startLog(logId);
		sessions.put(ds.ksession.getId(), ds);		
		return ds;
	}
	/** force reloading of an existing session (and start logging if required)
	 * @return 
	 * @throws NamingException 
	 * @throws RulesetException 
	 * @throws IOException 
	 * @throws NotSupportedException 
	 * @throws SystemException 
	 * @throws SecurityException 
	 * @throws IllegalStateException */
	public synchronized static DroolsSession reloadSession(Session session, EntityManager em) throws NamingException, RulesetException, IOException, IllegalStateException, SecurityException, SystemException, NotSupportedException {
		if (session.getSessionType()==SessionType.TRANSIENT)
			throw new RuntimeException("Cannot restore a transient session ("+session.getId()+")");
		DroolsSession ds = sessions.get(session.getDroolsId());
		if (ds!=null) {
			ds.closeLog();
			ds.getKsession().dispose();
		}
		ds = new DroolsSession(session.getProjectUrl(), false, session.getDroolsId(), session.getSessionType(), session.isLogged());
		try {
			ds.startLog(nextLogId(em, session.getId()));
		}
		catch (Exception e) {
			logger.log(Level.WARNING, "Unable to start log", e);
		}
		sessions.put(ds.ksession.getId(), ds);
		return ds;
	}
	/** rotate log id 
	 * @param sessionId 
	 * @throws NamingException 
	 * @throws SystemException 
	 * @throws RollbackException 
	 * @throws HeuristicRollbackException 
	 * @throws HeuristicMixedException 
	 * @throws SecurityException 
	 * @throws IllegalStateException 
	 * @throws NotSupportedException */
	private static int nextLogId(EntityManager em, String sessionId) throws NamingException, IllegalStateException, SecurityException, HeuristicMixedException, HeuristicRollbackException, RollbackException, SystemException, NotSupportedException {
		UserTransaction ut =
			  (UserTransaction) new InitialContext().lookup( "java:comp/UserTransaction" );
		boolean localTransaction = false;
		if (ut.getStatus()!=Status.STATUS_ACTIVE) {
			localTransaction = true;
			ut.begin();
		}
		em.joinTransaction();

		int logId = 0;
		Session session = em.find(Session.class, sessionId);
		logId = session.getLogId()+1;
		session.setLogId(logId);
		//em.persist(session);
		
		if (localTransaction)
			ut.commit();

		return logId;
	}
	/** get existing session (and start logging if required)
	 * @param em 
	 * @throws NamingException 
	 * @throws RulesetException 
	 * @throws IOException 
	 * @throws NotSupportedException 
	 * @throws SystemException 
	 * @throws SecurityException 
	 * @throws IllegalStateException */
	public synchronized static DroolsSession getSession(Session session, EntityManager em) throws NamingException, RulesetException, IOException, IllegalStateException, SecurityException, SystemException, NotSupportedException {
		DroolsSession ds = sessions.get(session.getDroolsId());
		if (ds!=null)
			return ds;
		if (session.getSessionType()==SessionType.TRANSIENT)
			throw new RuntimeException("Cannot restore a transient session ("+session.getId()+")");
		ds = new DroolsSession(session.getProjectUrl(), false, session.getDroolsId(), session.getSessionType(), session.isLogged());
		try {
			ds.startLog(nextLogId(em, session.getId()));
		}
		catch (Exception e) {
			logger.log(Level.WARNING, "Unable to re-start log", e);
		}
		sessions.put(ds.ksession.getId(), ds);
		return ds;
	}
	/** session building exception */
	public static class RulesetException extends Exception {
		String rulesetUrl;
		KnowledgeBuilderError errors[];
		/**
		 * @param rulesetUrl
		 * @param errors
		 */
		public RulesetException(String rulesetUrl, KnowledgeBuilderError errors[]) {
			super();
			this.rulesetUrl = rulesetUrl;
			this.errors = errors;
		}
	}
	/** cons 
	 * @param logged 
	 * @throws NamingException 
	 * @throws RulesetException 
	 * @throws IOException 
	 * @throws NotSupportedException 
	 * @throws SystemException 
	 * @throws SecurityException 
	 * @throws IllegalStateException */
	private DroolsSession(String projectUrl, boolean newFlag, int sessionId, SessionType sessionType, boolean logged) throws NamingException, RulesetException, IOException, IllegalStateException, SecurityException, SystemException, NotSupportedException {	
		projectInfo = this.readProjectInfo(projectUrl);
		String ruleFiles[] = projectInfo.getRuleFiles().toArray(new String[projectInfo.getRuleFiles().size()]);
		
		final KnowledgeBase kbase = DroolsUtils.getKnowledgeBase(ruleFiles);

		UserTransaction ut =
			  (UserTransaction) new InitialContext().lookup( "java:comp/UserTransaction" );

		switch(sessionType) {
		case JPA_SERIALIZED: {
			Environment env = KnowledgeBaseFactory.newEnvironment();
			env.set( EnvironmentName.ENTITY_MANAGER_FACTORY,
			         Persistence.createEntityManagerFactory( "droolstest" ) );
			
			// bitronix specific... !
			//env.set( EnvironmentName.TRANSACTION_MANAGER,
			//         bitronix.tm.TransactionManagerServices.getTransactionManager() );
			//ut.begin();
			// KnowledgeSessionConfiguration may be null, and a default will be used
			if (newFlag) {
				this.ksession = JPAKnowledgeService.newStatefulKnowledgeSession( kbase, null, env );
				sessionId = ksession.getId();
			
				logger.info("New session "+sessionId);
			} else
			{
				this.ksession = JPAKnowledgeService.loadStatefulKnowledgeSession(sessionId, kbase, null, env);
				logger.info("Loaded session "+sessionId);
			}
			// can't commit - nested not supported: ut.commit();
			break;
		}
		case TRANSIENT: 
			this.ksession = kbase.newStatefulKnowledgeSession();
			break;
		}

		this.logged = logged;
		
		// load facts...
		List<FactStore> factStores = projectInfo.getFactStores();
		for (FactStore factStore : factStores) {
			loadFacts(factStore);
		}

	}
	/** log file name */
	protected String logFileName;
	/** rotate log (if required).
	 * @return 
	 * @return old log file
	 */
	protected synchronized String startLog(int logId) {
		String returnFileName = logFileName;
		closeLog();
		
		// log...
		if (logged) {
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd'T'hhmmssSSS");
			logFileName = (logFileDir!=null ? logFileDir : "") + "droolslog-"+this.getId()+"-"+logId+"-"+df.format(new Date());
			try {
				this.droolsLogger = KnowledgeRuntimeLoggerFactory.newFileLogger(ksession, logFileName);
				
				// workaround - seems to add .log
				logFileName = logFileName+".log";
				logger.info("Start drools logger for "+ksession.getId()+" as "+logFileName);
			}
			catch (Exception e) {
				logger.log(Level.WARNING, "Problem creating droolsLogger w file "+logFileName, e);
				logFileName = null;
			}
		}
		return returnFileName;
	}
	/** rotate log (if required).
	 * @return old log file */
	public synchronized String rotateLog(EntityManager em, Session session) {
		try {
			return startLog(nextLogId(em, session.getId()));			
		}
		catch (Exception e) {
			logger.log(Level.WARNING, "Unable to rotate log", e);
		}
		return null;
	}
	/** close log (if required).
	 * @return WHether session was logging */
	protected synchronized boolean closeLog() {
		if (droolsLogger!=null) {
			try {
				droolsLogger.close();
				// workaround - append </object-stream> ?!
				File f = new File(logFileName);
				FileWriter fw = new FileWriter(f, true);
				fw.write("</object-stream>");
				fw.close();
			} catch (Exception e) 
			{
				logger.log(Level.WARNING, "Problem closing droolsLogger for drools session "+this.ksession.getId(), e);
			}
			droolsLogger = null;
			logFileName = null;
		}
		return logged;
	}
	/** logger */
	protected KnowledgeRuntimeLogger droolsLogger;
	public synchronized void loadFacts(FactStore factStore) throws IllegalStateException, SecurityException, SystemException, NotSupportedException, NamingException {
		UserTransaction ut =
			  (UserTransaction) new InitialContext().lookup( "java:comp/UserTransaction" );
		boolean localTransaction = false;
		if (ut.getStatus()!=Status.STATUS_ACTIVE) {
			localTransaction = true;
			ut.begin();
		}

        try {

    		KnowledgeBase kb = getKsession().getKnowledgeBase();
    		for (Fact fact : factStore.getFacts()) {
    			FactType factType = kb.getFactType(fact.getNamespace(), fact.getTypeName());
    			if (factType==null) {
    				logger.log(Level.WARNING,"Unknonwn fact type "+fact.getNamespace()+"."+fact.getTypeName());
    				continue;
    			}    		
    			Object object = factType.newInstance();
    			for (Map.Entry<String, Object> fieldEntry : fact.getFieldValues().entrySet()) {
    				FactField field = factType.getField(fieldEntry.getKey());
    				if (field==null) {
        				logger.log(Level.WARNING,"Fact type "+fact.getNamespace()+"."+fact.getTypeName()+" has no field "+fieldEntry.getKey());
        				continue;
    				}
    				Object fieldValue = fieldEntry.getValue();
    				if (fieldValue instanceof String)
    					fieldValue = RawSessionResource.coerce((String)fieldValue, field);
    				// type?
    				if (fieldValue!=null)
    					field.set(object, fieldValue);
    			}
    			
        		// add the fact
    			this.ksession.insert(object);
    		}
    		
    		if (localTransaction)
    			ut.commit();	
    		
		}
        catch (Exception e) {
        	logger.log(Level.WARNING,"Problem loading facts", e);
        	if (localTransaction)
        		ut.rollback();
        }
	}
}

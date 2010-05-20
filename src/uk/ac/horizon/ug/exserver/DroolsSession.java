/**
 * 
 */
package uk.ac.horizon.ug.exserver;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.definition.KnowledgePackage;
import org.drools.event.rule.DebugAgendaEventListener;
import org.drools.io.ResourceFactory;
import org.drools.persistence.jpa.JPAKnowledgeService;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.KnowledgeBaseConfiguration;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.Persistence;
import javax.transaction.UserTransaction;

import uk.ac.horizon.apptest.desktop.DroolsTest;
import uk.ac.horizon.ug.exserver.model.SessionTemplate;
import uk.ac.horizon.ug.exserver.model.Session;
import uk.ac.horizon.ug.exserver.model.SessionType;

/**
 * @author cmg
 *
 */
public class DroolsSession {
	static Logger logger = Logger.getLogger(DroolsSession.class.getName());
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
	/** map of DroolsSessions (weak refs) */
	protected static Map<Integer,DroolsSession> sessions = new HashMap<Integer,DroolsSession>();
	/** create a new drools session 
	 * @throws NamingException */
	public synchronized static DroolsSession createSession(SessionTemplate template, SessionType sessionType) throws NamingException {
		DroolsSession ds = new DroolsSession(template.getRulesetUrls(), true, 0, sessionType);
		sessions.put(ds.ksession.getId(), ds);
		ds.addFacts(template.getFactUrls());
		return ds;
	}
	/** get existing session 
	 * @throws NamingException */
	public synchronized static DroolsSession getSession(Session session) throws NamingException {
		DroolsSession ds = sessions.get(session.getDroolsId());
		if (ds!=null)
			return ds;
		if (session.getSessionType()==SessionType.TRANSIENT)
			throw new RuntimeException("Cannot restore a transient session ("+session.getId()+")");
		ds = new DroolsSession(session.getRulesetUrls(), false, session.getDroolsId(), session.getSessionType());
		sessions.put(ds.ksession.getId(), ds);
		return ds;
	}
	/** cons 
	 * @throws NamingException */
	private DroolsSession(String rulesetUrls[], boolean newFlag, int sessionId, SessionType sessionType) throws NamingException {	
		// force use of JANINO
		System.setProperty("drools.dialect.java.compiler", "JANINO");

		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

		// this will parse and compile in one step
		for (int i=0; i<rulesetUrls.length; i++) 			
			kbuilder.add(ResourceFactory.newUrlResource(rulesetUrls[i]), ResourceType.DRL);

		// Check the builder for errors
		if (kbuilder.hasErrors()) {
			throw new RuntimeException("Unable to compile rules: "+kbuilder.getErrors());
		}

		// get the compiled packages (which are serializable)
		final Collection<KnowledgePackage> pkgs = kbuilder.getKnowledgePackages();

		// add the packages to a knowledgebase (deploy the knowledge packages).
		KnowledgeBaseConfiguration conf = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
		// identity changes when persistent session is re-read so we need to use equality for facts
		// (make sure you define it correctly and don't intend to add multiple copies of the same fact).
		// this doesn't work at least up to 5.1.0.M2 because the EqualityAssertMapComparator is (IMO) broken.
		// so requires custom fix :-(
		//conf.setProperty("drools.assertBehaviour", "equality");
		final KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase(conf);
		
		kbase.addKnowledgePackages(pkgs);

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

		
//		ksession.addEventListener(new DebugAgendaEventListener());
	}
	/** add facts */
	private void addFacts(String factUrls[]) {
		// TODO
	}
}

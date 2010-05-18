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
	public synchronized static DroolsSession createSession(SessionTemplate template) throws NamingException {
		DroolsSession ds = new DroolsSession(template.getRulesetUrls(), true, 0);
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
		ds = new DroolsSession(session.getRulesetUrls(), false, session.getDroolsId());
		sessions.put(ds.ksession.getId(), ds);
		return ds;
	}
	/** cons 
	 * @throws NamingException */
	private DroolsSession(String rulesetUrls[], boolean newFlag, int sessionId) throws NamingException {	
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
		final KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
		kbase.addKnowledgePackages(pkgs);

		Environment env = KnowledgeBaseFactory.newEnvironment();
		env.set( EnvironmentName.ENTITY_MANAGER_FACTORY,
		         Persistence.createEntityManagerFactory( "droolstest" ) );
		// bitronix specific... !
		//env.set( EnvironmentName.TRANSACTION_MANAGER,
		//         bitronix.tm.TransactionManagerServices.getTransactionManager() );

		UserTransaction ut =
			  (UserTransaction) new InitialContext().lookup( "java:comp/UserTransaction" );

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
		
//		ksession.addEventListener(new DebugAgendaEventListener());
	}
	/** add facts */
	private void addFacts(String factUrls[]) {
		// TODO
	}
}

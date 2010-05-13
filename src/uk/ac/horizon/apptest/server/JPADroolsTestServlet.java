/**
 * 
 */
package uk.ac.horizon.apptest.server;

import java.io.IOException;
import java.io.Writer;

import javax.naming.InitialContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.persistence.Persistence;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.definition.KnowledgePackage;
import org.drools.event.rule.DebugAgendaEventListener;
import org.drools.io.ResourceFactory;
import org.drools.logger.KnowledgeRuntimeLogger;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.persistence.jpa.JPAKnowledgeService;

import uk.ac.horizon.apptest.desktop.DroolsTest;
import uk.ac.horizon.apptest.model.ContentMapping;
import uk.ac.horizon.apptest.model.UserRegion;
import uk.ac.horizon.apptest.model.Region;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author cmg
 *
 */
public class JPADroolsTestServlet extends HttpServlet {
	static Logger logger = Logger.getLogger(JPADroolsTestServlet.class.getName());
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	//@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			// force use of JANINO
			System.setProperty("drools.dialect.java.compiler", "JANINO");
			// TODO Auto-generated method stub
			final KnowledgeBuilder kbuilder = KnowledgeBuilderFactory
			.newKnowledgeBuilder();

			// this will parse and compile in one step
			kbuilder.add(ResourceFactory.newClassPathResource("HelloWorld.drl",
					DroolsTest.class), ResourceType.DRL);

			// Check the builder for errors
			if (kbuilder.hasErrors()) {
				System.out.println(kbuilder.getErrors().toString());
				throw new RuntimeException("Unable to compile \"HelloWorld.drl\".");
			}

			// get the compiled packages (which are serializable)
			final Collection<KnowledgePackage> pkgs = kbuilder
			.getKnowledgePackages();

			// add the packages to a knowledgebase (deploy the knowledge packages).
			final KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
			kbase.addKnowledgePackages(pkgs);

			Environment env = KnowledgeBaseFactory.newEnvironment();
			env.set( EnvironmentName.ENTITY_MANAGER_FACTORY,
			         Persistence.createEntityManagerFactory( "droolstest" ) );
			// bitronix specific... !
			env.set( EnvironmentName.TRANSACTION_MANAGER,
			         bitronix.tm.TransactionManagerServices.getTransactionManager() );
			          
			// KnowledgeSessionConfiguration may be null, and a default will be used
			StatefulKnowledgeSession ksession = null;
			if (req.getParameter("session")==null) {
				ksession = JPAKnowledgeService.newStatefulKnowledgeSession( kbase, null, env );
				int sessionId = ksession.getId();
			
				logger.info("New session "+sessionId);
			} else
			{
				int sessionId = Integer.parseInt(req.getParameter("session"));
				ksession = JPAKnowledgeService.loadStatefulKnowledgeSession(sessionId, kbase, null, env);
				logger.info("Loaded session "+sessionId);
			}
			UserTransaction ut =
			  (UserTransaction) new InitialContext().lookup( "java:comp/UserTransaction" );

			// new stateful session for this "game"
//			final StatefulKnowledgeSession ksession = kbase
//			.newStatefulKnowledgeSession();
			//ksession.setGlobal("list", new ArrayList<Object>());

			ksession.addEventListener(new DebugAgendaEventListener());

			// setup the audit logging
			KnowledgeRuntimeLogger logger = KnowledgeRuntimeLoggerFactory
			.newFileLogger(ksession, "helloworld");

			JPADroolsTestServlet.logger.info("No op...");
			// add "authored" content
			ut.begin();
			ut.commit();

			JPADroolsTestServlet.logger.info("Add R...");
			// add "authored" content
			ut.begin();
			
			final Region r = new Region();
			r.setId("R2");
			ksession.insert(r);

			// this only works at present if Region implements Serializable :-<
			// a post suggests JPA persistence is also possible for facts...
			ut.commit();

			JPADroolsTestServlet.logger.info("fire rules");
			ut.begin();
			ksession.fireAllRules();
			ut.commit();
			

			JPADroolsTestServlet.logger.info("Add CM...");
			// add "authored" content
			ut.begin();
			
			final ContentMapping cm = new ContentMapping();
			cm.setContent_id("C1");
			cm.setRegion_id("R1");
			ksession.insert(cm);

			ksession.fireAllRules();
			ut.commit();
			
			JPADroolsTestServlet.logger.info("Add UR...");
			ut.begin();
			// add "game" content
			final UserRegion ur = new UserRegion();
			ur.setUser_id("U1");
			ur.setRegion_id("R1");
			ksession.insert(ur);
			
			ksession.fireAllRules();
			ut.commit();

			ksession.dispose();

			logger.close();
		} catch (Exception e) {
			logger.log(Level.WARNING, e.toString(), e);
		}
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("text/plain");
		Writer w = resp.getWriter();
		w.write("Hello!\n");
		w.close();
	}

}

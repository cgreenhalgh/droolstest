/**
 * 
 */
package uk.ac.horizon.ug.exserver;

import java.util.List;
import java.util.LinkedList;
import java.util.Collection;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import org.drools.runtime.rule.FactHandle;
import org.drools.KnowledgeBase;
import org.drools.common.InternalFactHandle;
import org.drools.definition.type.FactType;

import org.restlet.data.MediaType;
import org.restlet.ext.xstream.XstreamRepresentation;
import org.restlet.ext.xstream.XstreamConverter;
//import org.restlet.ext.xml.XmlRepresentation;
import org.restlet.resource.Get;   
import org.restlet.resource.Post;   
import org.restlet.representation.Representation;   
import org.restlet.representation.StringRepresentation;   
import org.restlet.resource.ResourceException;   

import org.restlet.data.Form;   
import org.restlet.data.MediaType;   
import org.restlet.data.Status;   

import uk.ac.horizon.ug.exserver.RestletApplication;
import uk.ac.horizon.ug.exserver.model.SessionTemplate;
import uk.ac.horizon.ug.exserver.model.Session;

import java.util.logging.Level;
import java.util.logging.Logger;

/** "raw" facts,i.e. Xstream
 * 
 * @author cmg
 *
 */
public class RawSessionResource extends SessionResource {
	static Logger logger = Logger.getLogger(RawSessionResource.class.getName());
	
	/** XML list of all facts */
    @Get("xml")   
    public Representation toXml() throws java.io.IOException, javax.naming.NamingException, javax.transaction.SystemException, javax.transaction.NotSupportedException, javax.transaction.RollbackException, javax.transaction.HeuristicRollbackException, javax.transaction.HeuristicMixedException {   
    	//
    	List<RawFactHolder> facts = new LinkedList<RawFactHolder>();
    	synchronized(droolsSession) {
        	UserTransaction ut = this.getTransaction();
        	ut.begin();
        	try {
        		EntityManager em = getEntityManager();
        		Collection<FactHandle> factHandles = droolsSession.getKsession().getFactHandles();
        		KnowledgeBase kb = droolsSession.ksession.getKnowledgeBase();
        		for (FactHandle fh : factHandles) {
        			//logger.info("Looking for fact "+fh+" ("+fh.getClass()+")");
        			// this doesn't work in 5.1.0.M1 but is fixed in SVN
        			//Object fact = droolsSession.getKsession().getObject(fh);
        			//logger.info("Found fact "+fh+": "+fact);
        			RawFactHolder rf = new RawFactHolder();
        			rf.setHandle(fh.toExternalForm());
        			if (fh instanceof InternalFactHandle) {
        				InternalFactHandle ifh = (InternalFactHandle)fh;
        				rf.setFact(ifh.getObject());
        			}
        			//
        			facts.add(rf);
        		}
        		ut.commit();
        		em.close();
        	}
        	catch (Exception e) {
        		ut.rollback();
        		setStatus(Status.SERVER_ERROR_INTERNAL, e);
        		return null;
        	}
    	}
    	XstreamRepresentation<List<RawFactHolder>> xml = new XstreamRepresentation<List<RawFactHolder>>(MediaType.APPLICATION_XML, facts);
    	addAliases(xml);
    	return xml;
    }
    private static void addAliases(XstreamRepresentation xr) {
    	xr.getXstream().alias("holder", RawFactHolder.class);
    	xr.getXstream().alias("list", LinkedList.class);    	
//    	xr.getXstream().getConverterLookup().
    }
    /**  
     * Handle POST requests: create a new session.  
     */  
    @Post  
    public Representation addFacts(Representation entity) throws java.io.IOException, javax.naming.NamingException, javax.transaction.SystemException, javax.transaction.NotSupportedException, javax.transaction.RollbackException, javax.transaction.HeuristicRollbackException, javax.transaction.HeuristicMixedException {   
    	// some tests..
    	// try reading User class
    	try {
    		Class clazz = RawSessionResource.class.getClassLoader().loadClass("uk.ac.horizon.exserver.test1.User");
    		logger.info("Loaded User class from own loader");
    	} catch (Exception e) {
    		logger.log(Level.WARNING, "failed read from own classload", e);
    	}
    	try {
    		Class clazz = this.droolsSession.getKsession().getKnowledgeBase().getClass().getClassLoader().loadClass("uk.ac.horizon.exserver.test1.User");
    		logger.info("Loaded User class from KB loader");
    	} catch (Exception e) {
    		logger.log(Level.WARNING, "failed read from KB classload", e);
    	}
    	try {
    		Class clazz = Thread.currentThread().getContextClassLoader().loadClass("uk.ac.horizon.exserver.test1.User");
    		logger.info("Loaded User class from context class loader");
    	} catch (Exception e) {
    		logger.log(Level.WARNING, "failed read from context classload", e);
    	}
    	
    	
    	Representation result = null;   
        // Parse the given representation and retrieve pairs of   
        // "name=value" tokens.   
        List<RawFactHolder> facts = new LinkedList<RawFactHolder>();
        // parse ... ?!
        try {
        	Form form = new Form(entity);   
        	String factText = form.getFirstValue("facts");   
        	if (factText==null || factText.length()==0) {
        		return null;
        	}
        	
        	
        	XstreamRepresentation xml = new XstreamRepresentation<List<RawFactHolder>>(new StringRepresentation(factText));
        	addAliases(xml);

        	facts = (List<RawFactHolder>) new XstreamConverter().toObject(xml, List.class, null);
        }
        catch (Exception e) {
        	logger.log(Level.WARNING, "error parsing input xml", e);
        	setStatus(Status.SERVER_ERROR_INTERNAL, e);
        	return null;
        }
        logger.info("facts = "+facts);
        synchronized (droolsSession) {
        	UserTransaction ut = this.getTransaction();
        	ut.begin();
        	try {
        		EntityManager em = this.getEntityManager();
        		for (RawFactHolder rfh : facts) {
        			switch (rfh.getOperation()) {
        			case add: {
        				droolsSession.getKsession().insert(rfh.getFact());
        				logger.info("added "+rfh.getFact());
        				break;
        			}
        			case update:
        				// ...
        				break;
        			case delete:
        				// ....
        				break;
        			}
        		}
        		ut.commit();
        		em.close();
        	}
        	catch (Exception e) {
            	logger.log(Level.WARNING, "error adding facts "+facts, e);
        		ut.rollback();
        		setStatus(Status.SERVER_ERROR_INTERNAL, e);
        		return null;
        	}
        }
       
        return null;   
    }  
}

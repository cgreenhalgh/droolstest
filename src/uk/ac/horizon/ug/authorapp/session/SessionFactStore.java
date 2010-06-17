/**
 * 
 */
package uk.ac.horizon.ug.authorapp.session;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.drools.KnowledgeBase;
import org.drools.definition.type.FactField;
import org.drools.definition.type.FactType;
import org.drools.runtime.StatefulKnowledgeSession;

import uk.ac.horizon.ug.authorapp.FactStore;
import uk.ac.horizon.ug.exserver.devclient.Fact;

/**
 * @author cmg
 *
 */
public class SessionFactStore extends FactStore {
	static Logger logger = Logger.getLogger(SessionFactStore.class.getName());
	/** session */
	protected StatefulKnowledgeSession ksession;
	/**
	 * @param name
	 */
	public SessionFactStore(String name, StatefulKnowledgeSession ksession) {
		super(name);
		this.ksession = ksession;
		refresh();
	}

	/** refresh */
	public synchronized void refresh() {
		List<Fact> newFacts = new LinkedList<Fact>();
		// TODO
		Collection<Object> objects = ksession.getObjects();
		KnowledgeBase kb = ksession.getKnowledgeBase();
		for (Object object : objects) {
			String className = object.getClass().getName();
			int ix = className.lastIndexOf(".");
			String namespace = (ix>=0) ? className.substring(0, ix) : "";
			String typeName = (ix>=0) ? className.substring(ix+1) : className;
			FactType factType = kb.getFactType(namespace, typeName);
			if (factType==null) {
				logger.log(Level.WARNING, "Could not get FactType for "+className);
				continue;
			}
			Fact fact = new Fact();
			fact.setNamespace(namespace);
			fact.setTypeName(typeName);
			List<FactField> fieldTypes = factType.getFields();
			for (FactField fieldType : fieldTypes) {
				Object value = fieldType.get(object);
				String fieldName = fieldType.getName();
				if (value!=null)
					fact.getFieldValues().put(fieldName, value);
			}
			newFacts.add(fact);
		}
		this.facts = newFacts;
		this.changed = false;
		this.fireFactsChanged();
	}
	
}

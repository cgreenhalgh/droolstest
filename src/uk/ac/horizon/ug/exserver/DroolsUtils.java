/**
 * 
 */
package uk.ac.horizon.ug.exserver;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.naming.NamingException;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.compiler.DescrBuildError;
import org.drools.compiler.DrlParser;
import org.drools.definition.KnowledgePackage;
import org.drools.io.ResourceFactory;
import org.drools.lang.descr.PackageDescr;
import org.drools.lang.descr.TypeDeclarationDescr;
import org.drools.lang.descr.TypeFieldDescr;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import java.util.logging.Logger;

import uk.ac.horizon.ug.exserver.DroolsSession.RulesetException;
import uk.ac.horizon.ug.exserver.model.SessionType;
import uk.ac.horizon.ug.exserver.protocol.RulesetError;
import uk.ac.horizon.ug.exserver.protocol.RulesetErrors;
import uk.ac.horizon.ug.exserver.protocol.SessionBuildResult;
import uk.ac.horizon.ug.exserver.protocol.TypeDescription;
import uk.ac.horizon.ug.exserver.protocol.TypeFieldDescription;

/**
 * @author cmg
 *
 */
public class DroolsUtils {
	static Logger logger = Logger.getLogger(DroolsUtils.class.getName());
	/** cons 
	 * @param logged 
	 * @throws NamingException 
	 * @throws RulesetException */
	public static KnowledgeBase getKnowledgeBase(String rulesetUrls[]) throws RulesetException {	
		// force use of JANINO
		System.setProperty("drools.dialect.java.compiler", "JANINO");

		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

		// this will parse and compile in one step
		for (int i=0; i<rulesetUrls.length; i++) {			
			kbuilder.add(ResourceFactory.newUrlResource(rulesetUrls[i]), ResourceType.DRL);

			// Check the builder for errors
			if (kbuilder.hasErrors()) {
				
				KnowledgeBuilderErrors errors = kbuilder.getErrors();
				Iterator<KnowledgeBuilderError> it = errors.iterator();
				KnowledgeBuilderError eout [] = new KnowledgeBuilderError[errors.size()];
				int ei = 0;
				while(it.hasNext()) {
					KnowledgeBuilderError error = it.next();
					eout[ei++] = error;
				}
				throw new RulesetException(rulesetUrls[i], eout);
			}
		}

		// get the compiled packages (which are serializable)
		final Collection<KnowledgePackage> pkgs = kbuilder.getKnowledgePackages();

		// add the packages to a knowledgebase (deploy the knowledge packages).
		KnowledgeBaseConfiguration conf = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
		// identity changes when persistent session is re-read so we need to use equality for facts
		// (make sure you define it correctly and don't intend to add multiple copies of the same fact).
		// this doesn't work at least up to 5.1.0.M2 because the EqualityAssertMapComparator is (IMO) broken.
		// so requires custom fix :-(
		conf.setProperty("drools.assertBehaviour", "equality");
		final KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase(conf);
		
		kbase.addKnowledgePackages(pkgs);

		return kbase;
	}
	/** convert drools errors to portable form */
	public static RulesetErrors[] getRulesetErros(DroolsSession.RulesetException re) {
		//res.setId(s.getId());
		RulesetErrors errors[] = new RulesetErrors[re.errors.length];
		for (int ei=0; ei<errors.length; ei++) 
		{
			errors[ei] = new RulesetErrors();
			errors[ei].setRulesetUrl(re.rulesetUrl);
			RulesetError details[] = new RulesetError[re.errors.length];
			for (int di=0; di<details.length; di++) {
				KnowledgeBuilderError kbe = re.errors[di];
				//logger.info("KBError: "+kbe);
				details[di] = new RulesetError(kbe.getClass().getName(), kbe.getErrorLines(), kbe.getMessage(), kbe.toString());
				if (kbe instanceof DescrBuildError) {
					DescrBuildError dbe = (DescrBuildError)kbe;
					//logger.info("DescrBuildError: line="+dbe.getLine()+", descr="+dbe.getDescr()+", object="+dbe.getObject()+", parent="+dbe.getParentDescr()+" ("+dbe+")");
					if (details[di].getErrorLines()==null || details[di].getErrorLines().length==0)
						details[di].setErrorLines(new int [] { dbe.getLine() });
				}
			}
			errors[ei].setErrors(details);
		}
		return errors;
	}
	/** get drools type declarations with metadata */
	public static List<TypeDescription> getTypeDescriptions(String ruleSetUrls []) {
		List<TypeDeclarationDescr> types = new LinkedList<TypeDeclarationDescr>();
		//String ruleSetUrls [] = this.sessionInfo.getRulesetUrls();
		DrlParser parser = new DrlParser();
		PackageDescr packageDescr = null;
		for (int ti=0; ruleSetUrls!=null && ti<ruleSetUrls.length; ti++) {
			try {
				packageDescr = parser.parse(true, new URL(ruleSetUrls[ti]).openStream());
				types.addAll(packageDescr.getTypeDeclarations());
			} catch (Exception e) {
				logger.log(Level.WARNING, "Error parsing rule set "+ruleSetUrls[ti], e);
				//setStatus(Status.SERVER_ERROR_INTERNAL);
				return null;
			}
		}
		List<TypeDescription> tds = new LinkedList<TypeDescription>();
		for (TypeDeclarationDescr type : types) {
			TypeDescription td = new TypeDescription();
			td.setNamespace(type.getNamespace());
			if (type.getNamespace()==null)
				td.setNamespace(packageDescr.getNamespace());
			td.setTypeName(type.getTypeName());
			td.setTypeMeta(type.getMetaAttributes());
			Map<String,TypeFieldDescr> fields = type.getFields();
			Map<String,TypeFieldDescription> tfds = new HashMap<String,TypeFieldDescription>();
			for (Map.Entry<String,TypeFieldDescr> entry : fields.entrySet()) {
				TypeFieldDescription tfd = new TypeFieldDescription();
				TypeFieldDescr field = entry.getValue();
				tfd.setFieldMeta(field.getMetaAttributes());
				// pattern has id & objectType; latter seems to be java type.
				logger.info("Field "+entry.getKey()+": pattern="+field.getPattern()+", initExpr="+field.getInitExpr()+", ="+field.getText());
				tfd.setTypeName(field.getPattern().getObjectType());
				tfds.put(entry.getKey(), tfd);
			}
			td.setFields(tfds);
			tds.add(td);
		}
		return tds;
	}
}

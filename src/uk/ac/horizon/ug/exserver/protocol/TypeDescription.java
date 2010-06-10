/**
 * 
 */
package uk.ac.horizon.ug.exserver.protocol;

import java.util.Map;

import org.drools.lang.descr.TypeFieldDescr;

import uk.ac.horizon.ug.exserver.protocol.TypeFieldDescription.FieldMetaKeys;

/** Protocol proxy for drools TypeDefinitionDescr
 * 
 * @author cmg
 *
 */
public class TypeDescription {
	/** package */
	protected String namespace;
	/** name */
	protected String typeName;
	/** type-wide metadata */
	protected Map<String,String> typeMeta;
	/** system type metadata keys */
	public static enum TypeMetaKeys { physical, digital, 
		type,
		client,
		entity, relationship, property,
		describedbyauthor, describedbyself, describedbyother, inferred, sensed,
		fixed,
		requires,
		event, message, 
		};
	/** map of fields */
	protected Map<String,TypeFieldDescription> fields;
	/**
	 * 
	 */
	public TypeDescription() {
		super();
	}
	/**
	 * @param typeMeta
	 * @param fields
	 */
	public TypeDescription(Map<String, String> typeMeta,
			Map<String, TypeFieldDescription> fields) {
		super();
		this.typeMeta = typeMeta;
		this.fields = fields;
	}
	/**
	 * @return the namespace
	 */
	public String getNamespace() {
		return namespace;
	}
	/**
	 * @param namespace the namespace to set
	 */
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	/**
	 * @return the typeName
	 */
	public String getTypeName() {
		return typeName;
	}
	/**
	 * @param typeName the typeName to set
	 */
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	/**
	 * @return the typeMeta
	 */
	public Map<String, String> getTypeMeta() {
		return typeMeta;
	}
	/**
	 * @param typeMeta the typeMeta to set
	 */
	public void setTypeMeta(Map<String, String> typeMeta) {
		this.typeMeta = typeMeta;
	}
	/**
	 * @return the fields
	 */
	public Map<String, TypeFieldDescription> getFields() {
		return fields;
	}
	/**
	 * @param fields the fields to set
	 */
	public void setFields(Map<String, TypeFieldDescription> fields) {
		this.fields = fields;
	}
	/** get type metadata */
	public String getType() {
		return typeMeta.get(TypeMetaKeys.type.name());
	}
	/** get physical metadata */
	public boolean isPhysical() {
		return typeMeta.containsKey(TypeMetaKeys.physical.name());
	}
	/** get digital metadata */
	public boolean isDigital() {
		return typeMeta.containsKey(TypeMetaKeys.digital.name());
	}
	/** get client metadata */
	public boolean isClient() {
		return typeMeta.containsKey(TypeMetaKeys.client.name());
	}
	/** get entity metadata */
	public boolean isEntity() {
		return typeMeta.containsKey(TypeMetaKeys.entity.name());
	}
	/** get relationship metadata */
	public boolean isRelationship() {
		return typeMeta.containsKey(TypeMetaKeys.relationship.name());
	}
	/** get property metadata */
	public boolean isProperty() {
		return typeMeta.containsKey(TypeMetaKeys.property.name());
	}
	/** get type metadata */
	public Cardinality getRelationshipCardinality() {
		return new Cardinality(typeMeta.get(TypeMetaKeys.relationship.name()));
	}
	/** get type metadata */
	public Cardinality getPropertyCardinality() {
		return new Cardinality(typeMeta.get(TypeMetaKeys.relationship.name()));
	}
	/** get describedbyself metadata */
	public boolean isDescribedbyself() {
		return typeMeta.containsKey(TypeMetaKeys.describedbyself.name());
	}
	/** get describedbyauthor metadata */
	public boolean isDescribedbyauthor() {
		return typeMeta.containsKey(TypeMetaKeys.describedbyauthor.name());
	}
	/** get describedbyother metadata */
	public boolean isDescribedbyother() {
		return typeMeta.containsKey(TypeMetaKeys.describedbyother.name());
	}
	/** get inferred metadata */
	public boolean isInferred() {
		return typeMeta.containsKey(TypeMetaKeys.inferred.name());
	}
	/** get fixed metadata */
	public boolean isFixed() {
		return typeMeta.containsKey(TypeMetaKeys.fixed.name());
	}
	/** get event metadata */
	public boolean isEvent() {
		return typeMeta.containsKey(TypeMetaKeys.event.name());
	}
	/** get requires metadata */
	public String getRequires() {
		return typeMeta.get(TypeMetaKeys.requires.name());
	}
	/** cardinality processing helper */
	public static class Cardinality {
		int subjectMin = 1, subjectMax = 1, objectMin = 1, objectMax = 1;
		/** cons "[range:]range", range = [min..]max (default min/max is 1) */
		public Cardinality(String cardinality) throws IllegalArgumentException {
			try {
				int cx = cardinality.indexOf(':');
				if (cx >= 0) {
					String subject = cardinality.substring(0, cx);
					int dx = subject.indexOf("..");
					if (dx>=0) {
						if (dx>0)
							subjectMin = Integer.parseInt(subject.substring(0,dx));
						else
							subjectMin = 0;
						subject = subject.substring(dx+"..".length());
					}
					if (subject.length()==0 || "*".equals(subject))
						subjectMax = Integer.MAX_VALUE;
					else
						subjectMax = Integer.parseInt(subject);
					cardinality = cardinality.substring(cx+1);
				}
				String object = cardinality.substring(0, cx);
				int dx = object.indexOf("..");
				if (dx>=0) {
					if (dx>0)
						objectMin = Integer.parseInt(object.substring(0,dx));
					else
						objectMin = 0;
					object = object.substring(dx+"..".length());
				}
				if (object.length()==0 || "*".equals(object))
					objectMax = Integer.MAX_VALUE;
				else
					objectMax = Integer.parseInt(object);			
			}
			catch (NumberFormatException nfe) {
				throw new IllegalArgumentException("Parsing cardinality "+cardinality, nfe);
			}
		}
		/**
		 * @return the subjectMin
		 */
		public int getSubjectMin() {
			return subjectMin;
		}
		/**
		 * @return the subjectMax
		 */
		public int getSubjectMax() {
			return subjectMax;
		}
		/**
		 * @return the objectMin
		 */
		public int getObjectMin() {
			return objectMin;
		}
		/**
		 * @return the objectMax
		 */
		public int getObjectMax() {
			return objectMax;
		}
		/** to string */
		public String toString() {
			StringBuilder sb = new StringBuilder();
			if (subjectMin!=1 || subjectMax!=1) {
				if (subjectMin==subjectMax)
					sb.append(subjectMin);
				else {
					 sb.append(subjectMin);
					 sb.append("..");
					 if (subjectMax==Integer.MAX_VALUE)
						 sb.append("*");
					 else
						 sb.append(subjectMax);
				}
				sb.append(':');
				if (objectMin==objectMax)
					sb.append(objectMin);
				else {
					 sb.append(objectMin);
					 sb.append("..");
					 if (objectMax==Integer.MAX_VALUE)
						 sb.append("*");
					 else
						 sb.append(objectMax);
				}
			}
			return sb.toString();
		}
	}
}

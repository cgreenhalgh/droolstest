/**
 * 
 */
package uk.ac.horizon.ug.authorapp.model;

/** type of QueryConstraint
 * 
 * @author cmg
 *
 */
public enum QueryConstraintType {
	EQUAL_TO("==", true), 
	NOT_EQUAL_TO("!=", true),
	LESS_THAN("<", true), 
	LESS_THAN_OR_EQUAL_TO("<=", true), 
	GREATER_THAN(">", true),
	GREATER_THAN_OR_EQUAL_TO(">=", true),
	EQUAL_TO_CLIENT_ID("==clientId", false), 
	EQUAL_TO_CONVERSATION_ID("==conversationId", false),
	IS_NULL("==null", false), 
	IS_NOT_NULL("!=null", false);
	
	private String text;
	private boolean requiresValue;
	QueryConstraintType(String text, boolean requiresValue) {
		this.text = text;
		this.requiresValue = requiresValue;
	}
	public String text() {
		return text;
	}
	public boolean requiresValue() {
		return requiresValue;
	}
	public static QueryConstraintType valueOfText(String t) {
		QueryConstraintType qcts[] = QueryConstraintType.values();
		for (int i=0; i<qcts.length; i++)
			if (qcts[i].name().equals(t))
				return qcts[i];
		return null;
	}
}

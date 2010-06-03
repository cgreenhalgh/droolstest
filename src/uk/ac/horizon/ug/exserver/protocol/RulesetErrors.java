package uk.ac.horizon.ug.exserver.protocol;

import java.util.Arrays;

import org.drools.builder.KnowledgeBuilderError;

/** result ruleset error class */
public class RulesetErrors {
	protected String rulesetUrl;
	protected RulesetError errors[];
	/**
	 * @return the rulesetUrl
	 */
	public String getRulesetUrl() {
		return rulesetUrl;
	}
	/**
	 * @param rulesetUrl the rulesetUrl to set
	 */
	public void setRulesetUrl(String rulesetUrl) {
		this.rulesetUrl = rulesetUrl;
	}
	/**
	 * @return the errors
	 */
	public RulesetError[] getErrors() {
		return errors;
	}
	/**
	 * @param errors the errors to set
	 */
	public void setErrors(RulesetError errors[]) {
		this.errors = errors;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RulesetErrors [errors=" + Arrays.toString(errors)
				+ ", rulesetUrl=" + rulesetUrl + "]";
	}
	
}
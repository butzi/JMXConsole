package com.jabubo.jmx.util.parser;

import java.util.regex.Pattern;

/**
 * This class represents one parameter.
 * 
 * Parameters normally have a name, but in some cases, parameters can also be specified without
 * naming them explicitly - some users might find it more convenient to call
 * <pre>
 * 	./jmx_console set_value FrobnicationInterval 42
 * </pre>
 * than
 * <pre>
 *   ./jmx_console set_value name=FrobnicationInterveral value=42
 * </pre>
 * 
 * Also, some parameters might be required, others are optional.
 * Optionally, a validationPattern can be specified against which the parameter value is validated
 * 
 * @author butzi
 *
 */
public class Param {
	
	String name;
	String shortName;
	String description;
	boolean mightBeUnnamed;
	boolean isRequired;
	boolean isMultiParam;
	boolean hasNoValue; // doesn't mean it is useless, though ;-)
	Pattern validationPattern;
	
	public Param(String name) {
		super();
		this.name = name;
	}
	
	public Param(String name, String description, boolean required) {
		this(name, null, description, false, required, false, null);
	}

	public Param(String name, String shortName, String description, 
				 boolean mightBeUnnamed, boolean isRequired, boolean isMultiParam,
				 Pattern validationPattern) {
		super();
		this.name = name;
		this.description = description;
		this.mightBeUnnamed = mightBeUnnamed;
		this.isRequired = isRequired;
		this.isMultiParam = isMultiParam;
		this.validationPattern = validationPattern;
	}
	
	public String getName() {
		return name;
	}

	public Param setName(String name) {
		this.name = name;
		return this;
	}

	public boolean isMightBeUnnamed() {
		return mightBeUnnamed;
	}

	public Param setMightBeUnnamed(boolean mightBeUnnamed) {
		this.mightBeUnnamed = mightBeUnnamed;
		return this;
	}

	public boolean isRequired() {
		return isRequired;
	}

	public Param setRequired(boolean isRequired) {
		this.isRequired = isRequired;
		return this;
	}

	public Pattern getValidationPattern() {
		return validationPattern;
	}

	public Param setValidationPattern(Pattern validationPattern) {
		this.validationPattern = validationPattern;
		return this;
	}

	public String getShortName() {
		return shortName;
	}

	public Param setShortName(String shortName) {
		this.shortName = shortName;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public Param setDescription(String description) {
		this.description = description;
		return this;
	}

	public boolean isMultiParam() {
		return isMultiParam;
	}

	public Param setMultiParam(boolean isMultiParam) {
		this.isMultiParam = isMultiParam;
		return this;
	}
	
	public boolean hasNoValue() {
		return hasNoValue;
	}
	
	public Param setHasNoValue(boolean hasNoValue) {
		this.hasNoValue = hasNoValue;
		return this;
	}

	public String getExampleValue(String suffix) {
		StringBuilder sb = new StringBuilder();
		sb.append("--");
		sb.append(getName());
		
		if (! hasNoValue) {
			sb.append("=");
			sb.append("<");
			sb.append(getName());
			if (suffix != null)
				sb.append(suffix);
			sb.append(">");
		}
		
		return sb.toString();
	}
	
	public interface ExampleValueFormatter {
		
		String getExampleValue(String suffix);
		
	}
	
	public class DefaultExampleValueFormatter implements ExampleValueFormatter {

		public String getExampleValue(String suffix) {
			StringBuilder sb = new StringBuilder();
			sb.append("--");
			sb.append(getName());
			
			if (! hasNoValue) {
				sb.append("=");
				sb.append("<");
				sb.append(getName());
				if (suffix != null)
					sb.append(suffix);
				sb.append(">");
			}
			
			return sb.toString();
		}
		
	}
	
	public class WebExampleValueFormatter implements ExampleValueFormatter {

		public String getExampleValue(String suffix) {
			StringBuilder sb = new StringBuilder();
			sb.append(getName());
			
			sb.append("=");
			sb.append("<i>");
			if (hasNoValue()) {
				sb.append("true");
			} else {
				sb.append(getName());
				if (suffix != null)
					sb.append(suffix);
			}
			sb.append("</i>");

			return sb.toString();
		}
		
	}
	
}
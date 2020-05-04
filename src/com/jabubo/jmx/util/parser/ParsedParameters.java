package com.jabubo.jmx.util.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

class ParsedParameters implements ParsedCommandLine {

	private Map<String, ArrayList<String>> collectedValues = new HashMap<String, ArrayList<String>>();
	private Collection<String> unprocessedParams = new ArrayList<String>();
	
	/**
	 * @param param
	 */
	public void storeUnknownParam(String param) {
		unprocessedParams.add(param);
	}

	/**
	 * @param value
	 * @param param
	 */
	public void storeParamValue(String value, Param param) {
		ArrayList<String> valuesForThisParam = null;
		if (collectedValues.containsKey(param.getName())) {
			if (! param.isMultiParam)
				throw new IllegalArgumentException("found more than one value for the param '" + param.getName() + "', but expected only one!");
			valuesForThisParam = collectedValues.get(param.getName());
		} else {
			valuesForThisParam = new ArrayList<String>();
			collectedValues.put(param.getName(), valuesForThisParam);
		}
		
		if (param.validationPattern != null) {
			Matcher matcher = param.validationPattern.matcher(value);
			if (! matcher.matches()) {
				throw new IllegalArgumentException("invalid value '" + value + "' for param '" + param.getName() + "'");
			}
		}
		
		valuesForThisParam.add(value);
	}

	
	public String getParamValue(String name) {
		ArrayList<String> values = collectedValues.get(name);
		if (values == null) {
			return null;
		} else {
			return values.toArray(new String[values.size()])[0];
		}
	}
	
	public Collection<String> getParamValues(String name) {
		ArrayList<String> values = collectedValues.get(name);
		if (values == null) {
			return null;
		} else {
			return values;
		}
	}

	public Collection<String> getUnprocessedArguments() {
		return unprocessedParams;
	}

	/* compatibility methods for Apache's commons-CLI project */
	public String getOptionValue(String string) {
		return getParamValue(string);
	}

	public boolean hasOption(String string) {
		return (getParamValue(string) != null);
	}

	public String[] getOptionValues(String string) {
		Collection<String> paramValues = getParamValues(string);
		return paramValues.toArray(new String[paramValues.size()]);
	}
	
}

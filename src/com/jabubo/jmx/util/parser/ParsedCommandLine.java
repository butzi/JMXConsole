package com.jabubo.jmx.util.parser;

import java.util.Collection;

/**
 * A ParsedCommandLine represents the result parsing a command line String[] for the parameters
 * specified in a ParameterParser. It should be used by clients to get the parameters's values.
 * 
 * @author butzi
 *
 */
public interface ParsedCommandLine {

	/**
	 * returns the value of a parameter (it might make sense to call the parse() method first)
	 * @param name the name of the parameter whose value should be returned
	 * @return the value of the specified param or null if such a param does not exist
	 */
	String getParamValue(String name);

	/**
	 * returns the values of a parameter that might contain multiple values
	 * 
	 * Note: If the param is not a param that might hold multiple values, the returned collection holds only a single value
	 * 
	 * @param name the name of the parameter whose values should be returned
	 * @return a collection of string values or null if such a param does not exist
	 */
	Collection<String> getParamValues(String name);
	
	Collection<String> getUnprocessedArguments();

	boolean hasOption(String string);

	String getOptionValue(String string);

	String[] getOptionValues(String string);

}

package com.jabubo.jmx.util.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class ParameterParser {

	static final Logger LOGGER = Logger.getLogger(ParameterParser.class);
	
	static final Pattern namedParamPattern = Pattern.compile("^(?:--)?(.+?)=(.+)$");
	static final Pattern flagPattern = Pattern.compile("^(?:--)?(.+)$");
	
	private Collection<Param> params = new ArrayList<Param>();
	
	private boolean acceptUnknownParams = false;
	
	// marks the position of the current unnamed parameter that should be processed
	private int unnamedParamIndex = 0;
	private Param currentUnnamedParam;
	
	public ParameterParser() {
		super();
		init();
	}

	private void init() {
		unnamedParamIndex = 0;
		currentUnnamedParam = getNextUnnamedParam();
	}
	
	private void checkThatAllRequiredParamsExist(ParsedParameters parsedParameters) {
		for (Param param : params) {
			if (param.isRequired()) {
				Collection<String> values = parsedParameters.getParamValues(param.name);
				if (values == null || values.size() == 0) {
					throw new IllegalArgumentException("missing required parameter : '" + param.name + "'");
				}
			}
		}
	}	
	
	public boolean isAcceptUnknownParams() {
		return acceptUnknownParams;
	}

	public void setAcceptUnknownParams(boolean acceptUnknownParams) {
		this.acceptUnknownParams = acceptUnknownParams;
	}
	
	/**
	 * returns the next unnamed param or null if there's no other unnamed param
	 */
	private Param getNextUnnamedParam() {
		int count = 0;
		for (Param param : params) {
			if (param.isMightBeUnnamed()) {
				if (count++ == unnamedParamIndex) {
					unnamedParamIndex++;
					return param;
				}
			}
		}
		return null;
	}
	
	/**
	 * add a new param that the parser should parse
	 * @param param the param that should be added
	 */
	public void addParam(Param param) {
		// TODO make sure that we don't have more than one unnamed multi-param
		params.add(param);
	}
	
	/**
	 * returns all parameters known to the parser
	 * @return a Collection of Parameter objects
	 */
	public Collection<Param> getParams() {
		return params;
	}
	
	/**
	 * returns a configured parameter object by name or null if no such parameter exists
	 * 
	 * This method does not return the parameter's value, but the parameter object itself. If you want to retrieve the value,
	 * see {@link ParameterParser.getParamValue()}
	 * 
	 * @param name
	 * @return
	 */
	public Param getParamByName(String name) {
		// TODO handle short name
		for (Param param : params) {			
			if (param.name.equals(name)) {
				return param;
			} else if (param.isMultiParam()) {
				Pattern pattern = Pattern.compile(param.getName() + "\\d+");
				if (pattern.matcher(name).matches()) {
					LOGGER.debug("decoded param name '" + name + "' to param originally named '" + param.getName() + "'");
					return param;
				}
			}
		}
		return null;
	}
	
	/**
	 * parses the passed arguments against the known parameters that have been added by calls to addParam() beforehand
	 * @param params a string array of parameters (e.g. the String[] param to a main() method)
	 * @throws IllegalArgumentException if the passed arguments do not match the parser's expectations
	 */
	public ParsedCommandLine parse(String[] params) throws IllegalArgumentException {
		init();
		
		LOGGER.debug("starting to parse - found " + params.length + " params");
		ParsedParameters parsedParameters = new ParsedParameters();
		
		for (String param : params) {
			// check if this is a named parameter
			Matcher namedParamMatcher = namedParamPattern.matcher(param);
			if (namedParamMatcher.matches()) {
				String key = namedParamMatcher.group(1);
				String value = namedParamMatcher.group(2);
				
				storeNamedParam(parsedParameters, param, key, value);
			} else {
				// check if this is a flag
				Matcher flagMatcher = flagPattern.matcher(param);
				
				if (flagMatcher.matches()) {
					storeNamedParam(parsedParameters, param, flagMatcher.group(1), "true");
				} else {
					// it's neither a named param nor a flag, must be an unknown parameter
					storeUnnamedParam(parsedParameters, param);
				}
			}
		}
		
		checkThatAllRequiredParamsExist(parsedParameters);
		
		return parsedParameters;
	}

	private void storeNamedParam(ParsedParameters parsedParameters,
			String param, String key, String value) {
		Param paramByName = getParamByName(key);
		if (paramByName == null) {
			storeUnnamedParam(parsedParameters, param);
		} else {
			LOGGER.debug("storing value '" + value + "' for named param '" + key + "'");
			parsedParameters.storeParamValue(value, paramByName);
		}
	}

	/**
	 * @param parsedParameters
	 * @param param
	 */
	private void storeUnnamedParam(ParsedParameters parsedParameters,
			String param) {
		if (currentUnnamedParam != null) {
			LOGGER.debug("storing value '" + param + "' for current unnamed param '" + currentUnnamedParam.getName() + "'");
			parsedParameters.storeParamValue(param, currentUnnamedParam);
			if (! currentUnnamedParam.isMultiParam()) {
				LOGGER.debug("advancing to next unnamed param - this one is not a multi-param");
				currentUnnamedParam = getNextUnnamedParam();
			}
		} else {
			storeUnknownParam(parsedParameters, param);
		}
	}

	/**
	 * @param param
	 */
	private void storeUnknownParam(ParsedParameters parsedParameters, String param) {
		if (acceptUnknownParams) {
			parsedParameters.storeUnknownParam(param);
		} else {
			throw new IllegalArgumentException("unknown param '" + param + "'");
		}
	}
	
}

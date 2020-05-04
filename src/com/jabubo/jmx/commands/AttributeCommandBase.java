package com.jabubo.jmx.commands;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.hitchhackers.tools.jmx.util.parser.Param;
import org.hitchhackers.tools.jmx.util.parser.ParameterParser;
import org.hitchhackers.tools.jmx.util.parser.ParsedCommandLine;

/**
 * base class for commands that deal with attributes
 * 
 * @author butzi
 */
abstract public class AttributeCommandBase extends CommandBase {

	protected ObjectName objectName;
	protected String attributeName;
	protected String compositeKey;
	protected String fullName;
	
	@Override
	protected void initParams(ParameterParser parser) {
		parser.addParam(
			new Param("object_name")
				.setDescription("object name of the attribute that should be queried")
				.setMightBeUnnamed(true)
				.setRequired(true)
				.setMultiParam(false)
		);
		parser.addParam(
			new Param("attribute_name")
				.setDescription("name of the attribute that should be queried")
				.setMightBeUnnamed(true)
				.setRequired(true)
				.setMultiParam(false)
		);
		parser.addParam(
			new Param("composite_key")
				.setDescription("name of the composite element that should be extracted (makes sense only for attributes returning a composite structure)")
				.setMightBeUnnamed(true)
				.setRequired(false)
				.setMultiParam(false)
		);
	}

	@Override
	public void processParams(ParsedCommandLine commandLine) {
		try {
			objectName = ObjectName.getInstance(commandLine.getOptionValue("object_name"));
		} catch (MalformedObjectNameException e) {
			throw new IllegalArgumentException("invalid object name '" + commandLine.getOptionValue("object_name"));
		} catch (NullPointerException e) {
			throw new IllegalArgumentException("invalid object name '" + commandLine.getOptionValue("object_name"));
		}
		
		attributeName = commandLine.getOptionValue("attribute_name");
		compositeKey = commandLine.getOptionValue("composite_key");	
		fullName = objectName + "/" + attributeName;
		if (compositeKey != null)
			fullName = fullName + "/" + compositeKey;
	}
}

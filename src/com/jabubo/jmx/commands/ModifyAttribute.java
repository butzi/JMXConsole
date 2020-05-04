package com.jabubo.jmx.commands;

import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;

import org.apache.log4j.Logger;
import org.hitchhackers.tools.jmx.util.TypeConversionHelper;
import org.hitchhackers.tools.jmx.util.parser.Param;
import org.hitchhackers.tools.jmx.util.parser.ParameterParser;
import org.hitchhackers.tools.jmx.util.parser.ParsedCommandLine;

/**
 * 
 * @author butzi
 */
public class ModifyAttribute extends AttributeCommandBase {

	// TODO does it make sense to modify composite values?
	
	private static final Logger LOGGER = Logger.getLogger(ModifyAttribute.class);
	
	private String newValue;

	@Override
	public String getUsageHeader() {
		return 
			"This tool sets JMX attribute values.<br/><br/>" +
			"Examples:<br/>" +
			"set_attribute attribute=java.lang:type=Memory/HeapMemoryUsage/used value=42";
	}

	@Override
	protected void initParams(ParameterParser parser) {
		super.initParams(parser);
		
		parser.addParam(
			new Param("value")
				.setDescription("the new value for the specified attribute")
				.setMightBeUnnamed(true)
				.setRequired(true)
				.setShortName("v")
		);
	}

	@Override
	public void processParams(ParsedCommandLine commandLine) {
		super.processParams(commandLine);
		newValue = commandLine.getOptionValue("value");
	}
	
	@Override
	public String run() throws Exception {
		LOGGER.info("setting '" + fullName + "' to new value '" + newValue + "'");
		
		MBeanAttributeInfo[] attributes = getConnection().getMBeanInfo(objectName).getAttributes();
		MBeanAttributeInfo attributeFound = null;
		for (MBeanAttributeInfo beanAttributeInfo : attributes) {
			if (beanAttributeInfo.getName().equals(attributeName)) {
				attributeFound = beanAttributeInfo;
				break;
			}
		}
		
		if (attributeFound != null) {
			if (attributeFound.isWritable()) {
				String targetTypeName = attributeFound.getType();
				
				Object newValueObject = TypeConversionHelper.getObjectFromString(targetTypeName, newValue);
				
				Attribute attribute = new Attribute(attributeName, newValueObject);
				getConnection().setAttribute(objectName, attribute);
			} else {
				throw new IllegalArgumentException("attribute '" + fullName + "' is read-only!");
			}
		} else {
			throw new IllegalArgumentException("attribute '" + fullName + "' not found!");
		}
		
		return "";
	}
	
	
	
}

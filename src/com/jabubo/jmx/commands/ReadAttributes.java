package com.jabubo.jmx.commands;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

/**
 * Extraordinarily primitive command line client for querying JMX values via RMI.
 *
 * This client is written to be run either standalone for testing purposes 
 * or as "glue" component to monitoring applications (e.g. Nagios, Cacti etc.)
 * Therefore, this client should be able to run as standalone as possible, 
 * i.e it reads everything it needs from command line and does not rely on 
 * any external jars, configuration files etc.
 * 
 * @author butzi
 */
public class ReadAttributes extends AttributeCommandBase {

	Logger LOGGER = Logger.getLogger(ReadAttributes.class);
	
	public ReadAttributes() {
		super();
		addFormatter(CommandBase.OutputType.TEXT, TextFormatter.class);
		addFormatter(CommandBase.OutputType.JSON, JsonFormatter.class);
	}
	
	@Override
	public String getUsageHeader() {
		// TODO update documentation
		return "This command retrieves the specified attribute from the target VM. The attribute should be specified as " +
				"object_name/attribute_name[/sub_attribute_name], e.g. java.lang:type=Memory/HeapMemoryUsage/used.";
	}

	@Override
	public String run() throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException, IOException, InstantiationException, IllegalAccessException {
		Formatter formatter = (Formatter) getFormatter();
		
		// get the attribute specified
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("querying attribute '" + fullName + "'");
		}
		Object attributeValue = getConnection().getAttribute(
			objectName, attributeName
		);
				
		// extract the sub-value of a CompositeData structure if necessary
		if (compositeKey != null) {
			CompositeData compositeData = (CompositeData) attributeValue;
			attributeValue = compositeData.get(compositeKey);
		}

		formatter.addAttributeValue(objectName, attributeName, compositeKey, fullName, attributeValue);
		
		return formatter.asString();
	}
	
	interface Formatter extends OutputFormatter {
		void addAttributeValue(ObjectName objectName, String attributeName, String compositeKey, String fullName, Object value);
	}
	
	static class TextFormatter implements Formatter {

		private final StringBuilder sb = new StringBuilder();
		
		public void addAttributeValue(ObjectName objectName, String attributeName, String compositeKey, String fullName, Object value) {
			// TODO do we really want to output the attribute name here?
			sb.append(fullName);
			sb.append(" : ");
			if (value instanceof String[]) {
				String[] values = (String[]) value;
				for (int i = 0; i < values.length; i++) {
					if (i > 0)
						sb.append(",");
					sb.append(values[i]);
				}
			} else {
				sb.append(value);
			}
		}

		public String asString() {
			return sb.toString();
		}
		
	}
	
	static class JsonFormatter implements Formatter {

		private final Map<String, Object> data = new HashMap<String, Object>();
		
		public void addAttributeValue(ObjectName objectName, String attributeName, String compositeKey, String fullName, Object value) {
			// TODO if the value is composite, we can build a composite structure in JSON as well
			data.put("object_name", objectName.toString());
			data.put("attribute_name", attributeName);
			data.put("composite_key", compositeKey);
			if (value instanceof String[]) {
				String[] values = (String[]) value;
				data.put("value", values);
			} else {
				data.put("value", value.toString());
			}
		}

		public String asString() {
			return JSONObject.fromObject(data).toString();
		}
		
	}
	
}

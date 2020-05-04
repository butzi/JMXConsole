package com.jabubo.jmx.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import net.sf.json.JSONObject;

import org.hitchhackers.tools.jmx.util.parser.Param;
import org.hitchhackers.tools.jmx.util.parser.ParameterParser;
import org.hitchhackers.tools.jmx.util.parser.ParsedCommandLine;

/**
 * command line client that lists information about attributes and operations 
 * of a JMX enabled application.
 * 
 * @author butzi
 */
public class Browse extends CommandBase {

	private String objectNameToQuery = "";
	
	public Browse() {
		super();
		addFormatter(CommandBase.OutputType.TEXT, TextFormatter.class);
		addFormatter(CommandBase.OutputType.JSON, JsonFormatter.class);
	}

	@Override
	public String getUsageHeader() {
		return "This command displays all available MBeans in the Java VM.<br/><br/>" +
				"If <i>object</i> is specified, the attributes and operations of the specified " +
				"MBean object are displayed (you can find this value if you connect to the target VM with the JConsole " +
				"and take a look at the <i>MBean Name</i> displayed on the <i>Info</i> tab).<br/><br/>" +
				"Examples:<br/>" +
				"browse [...] object=java.lang:type=Memory";
	}

	@Override
	protected void initParams(ParameterParser parser) {
		parser.addParam(
			new Param("object").
				setDescription("name of the object which should be inspected").
				setShortName("o").
				setMightBeUnnamed(true)
		);
	}

	@Override
	public void processParams(ParsedCommandLine commandLine) {
		if (commandLine.hasOption("object")) {
			objectNameToQuery = commandLine.getOptionValue("object");
		}
	}	
	
	@Override
	public String run() throws Exception {
		Formatter formatter = (Formatter) getFormatter();
		
		ObjectName objectName = null;
		if (objectNameToQuery != "") {
			System.out.println("displaying information about " + objectNameToQuery);
			objectName = ObjectName.getInstance(objectNameToQuery);
				
			MBeanInfo beanInfo = getConnection().getMBeanInfo(objectName);
			
			// get attributes
			MBeanAttributeInfo[] attributes = beanInfo.getAttributes();
			Arrays.sort(attributes, new Comparator<MBeanAttributeInfo>() {
				public int compare(MBeanAttributeInfo o1, MBeanAttributeInfo o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			
			for (MBeanAttributeInfo beanAttributeInfo : attributes) {
				formatter.addAttribute(beanAttributeInfo);			
			}
			
			// and operations
			MBeanOperationInfo[] operations = beanInfo.getOperations();
			Arrays.sort(operations, new Comparator<MBeanOperationInfo>() {

				public int compare(MBeanOperationInfo o1, MBeanOperationInfo o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			
			for (MBeanOperationInfo beanOperationInfo : operations) {
				formatter.addOperation(beanOperationInfo);
			}
		} else {
			Set<ObjectInstance> beans = getConnection().queryMBeans(null, null);
			TreeSet<ObjectInstance> treeSet = new TreeSet<ObjectInstance>(new Comparator<ObjectInstance>() {

				public int compare(ObjectInstance o1, ObjectInstance o2) {
					return o1.getObjectName().getCanonicalName().compareTo(o2.getObjectName().getCanonicalName());
				}
			});
			treeSet.addAll(beans);
			for (ObjectInstance objectInstance : treeSet) {
				formatter.addObject(objectInstance);
			}			
		}
				
		return formatter.asString();
	}	

	interface Formatter extends OutputFormatter {
		void addObject(ObjectInstance objectInstance);
		void addOperation(MBeanOperationInfo operationInfo);
		void addAttribute(MBeanAttributeInfo attributeInfo);
	}
	
	@SuppressWarnings(value="unchecked")
	static class JsonFormatter implements Formatter {

		final ArrayList<Map> objects = new ArrayList<Map>();
		final ArrayList<Map> attributes = new ArrayList<Map>();
		final ArrayList<Map> operations = new ArrayList<Map>();
		
		public void addAttribute(MBeanAttributeInfo attributeInfo) {
			Map<String,String> map = new HashMap<String, String>();
			map.put("name", attributeInfo.getName());
			attributes.add(map);
		}

		public void addObject(ObjectInstance objectInstance) {
			Map<String, String> objectMap = new HashMap<String, String>();
			objectMap.put("name", objectInstance.getObjectName().toString());
			objects.add(objectMap);
		}

		@SuppressWarnings(value="unchecked")
		public void addOperation(MBeanOperationInfo operationInfo) {
			Map operationMap = new HashMap();
			
			operationMap.put("name", operationInfo.getName());
			
			String returnType = operationInfo.getReturnType();
			operationMap.put("return_type", returnType);

			ArrayList<Map<String, String>> params = new ArrayList<Map<String,String>>();
			MBeanParameterInfo[] signature = operationInfo.getSignature();
			for (MBeanParameterInfo beanParameterInfo : signature) {
				final Map<String, String> param = new HashMap<String, String>();
				param.put("name", beanParameterInfo.getName());
				param.put("type", beanParameterInfo.getType());
				params.add(param);			
			}
			operationMap.put("params", params);
			operations.add(operationMap);
		}

		public String asString() {
			// let's put the three collections into a map
			final Map<String, ArrayList<Map>> result = 
				new HashMap<String, ArrayList<Map>>();
			result.put("objects", objects);
			result.put("attributes", attributes);
			result.put("operations", operations);
			
			JSONObject jsonObject = JSONObject.fromObject(result);
			return jsonObject.toString();
		}
		
	}
	
	static class TextFormatter implements Formatter {
		
		private StringBuilder sb;

		public TextFormatter() {
			this.sb = new StringBuilder();
		}

		public void addObject(ObjectInstance objectInstance) {
			sb.append(objectInstance.getObjectName());
			sb.append("\n");
		}

		public void addAttribute(MBeanAttributeInfo attributeInfo) {
			sb.append("[A] ");
			sb.append(attributeInfo.getName());
			sb.append("\n");
		}

		public void addOperation(MBeanOperationInfo operationInfo) {
			sb.append("[O] ");
			
			String returnType = operationInfo.getReturnType();
			sb.append(returnType);
			sb.append(" ");
			
			sb.append(operationInfo.getName());
			
			sb.append("(");
			
			MBeanParameterInfo[] signature = operationInfo.getSignature();
			boolean firstParam = true;
			for (MBeanParameterInfo beanParameterInfo : signature) {
				if (firstParam) {
					firstParam = false;
				} else {
					sb.append(", ");
				}
				sb.append(beanParameterInfo.getType());
				sb.append(" ");
				sb.append(beanParameterInfo.getName());
			}
			sb.append(")");
			sb.append("\n");
			
		}

		public String asString() {
			return sb.toString();
		}
		
	}
	
	
}

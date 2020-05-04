package com.jabubo.jmx.commands;

import java.util.ArrayList;
import java.util.Collection;

import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.hitchhackers.tools.jmx.commands.dto.OperationObject;
import org.hitchhackers.tools.jmx.util.TypeConversionHelper;
import org.hitchhackers.tools.jmx.util.parser.Param;
import org.hitchhackers.tools.jmx.util.parser.ParameterParser;
import org.hitchhackers.tools.jmx.util.parser.ParsedCommandLine;

/**
 * command that invokes JMX operations
 * 
 * @author butzi
 */
public class InvokeOperation extends CommandBase {

	private Collection<OperationObject> operationNames = new ArrayList<OperationObject>();
	private Collection<String> params = new ArrayList<String>();
	
	// TODO add JSON formatter here
	
	@Override
	public String getUsageHeader() {
		return 
			"This tool invokes a JMX operation and returns it's result\n\n" + 
			"The operation to be executed is specified as the name of the object on which the operation " +
			"should be invoked, followed by a slash and the name of the operation, e.g.\n" +
			"  java.lang:type=Memory/gc\n" +
			"If the operation accepts parameters, these may be specified behind the operation name (multiple parameters should be separated by spaces)";
	}

	@Override
	protected void initParams(ParameterParser parser) {
		parser.addParam(
			new Param("object")
				.setDescription("the object on which the operation should be invoked")
				.setMightBeUnnamed(true)
				.setRequired(true)
				.setShortName("o")
		);
		
		parser.addParam(
			new Param("operation")
				.setDescription("the operation that should be invoked")
				.setMightBeUnnamed(true)
				.setRequired(true)
				.setShortName("op")
		);
		
		parser.addParam(
			new Param("param")
				.setDescription("parameters for the operation")
				.setMightBeUnnamed(true)
				.setMultiParam(true)
		);
	}

	@Override
	public void processParams(ParsedCommandLine commandLine) {
		String objectName = commandLine.getParamValue("object");
		String operationName = commandLine.getParamValue("operation");
		
		try {
			OperationObject operationObject = new OperationObject(ObjectName.getInstance(objectName), operationName);
			operationNames.add(operationObject);
		} catch (MalformedObjectNameException e) {
			throw new IllegalArgumentException("invalid object/operation name combination; object : '" + objectName + "', operation : '" + operationName + "'");
		}
	
		if (commandLine.hasOption("param")) {
			String[] unnamedParams = commandLine.getOptionValues("param"); //commandLine.getArgs();
			for (String unnamedParam : unnamedParams) {
				params.add(unnamedParam);
			}
		}
	}

	@Override
	public String run() throws Exception {
		String resultString = "";
		for (OperationObject operationObject : operationNames) {
//			try {
				// let's see if we have this operation
				MBeanInfo beanInfo = getConnection().getMBeanInfo(operationObject.getObjectName());
				MBeanOperationInfo[] operations = beanInfo.getOperations();
				MBeanOperationInfo myOperationInfo = null;
				for (MBeanOperationInfo beanOperationInfo : operations) {
					if (beanOperationInfo.getName().equals(operationObject.getOperationName())) {
						myOperationInfo = beanOperationInfo;
						break;
					}
				}
				
				if (myOperationInfo == null)
					throw new IllegalArgumentException("did not find an operation with name " + operationObject.getOperationName());

				// prepare the parameters
				MBeanParameterInfo[] signature = myOperationInfo.getSignature();
				String[] paramsArray = params.toArray(new String[params.size()]);
				if (signature.length != paramsArray.length)
					throw new IllegalArgumentException("expected " + signature.length + " parameters, but found " + paramsArray.length);
				int idx = 0;
				Object[] convertedParams = new Object[signature.length];
				String[] paramTypes = new String[signature.length];
				for (MBeanParameterInfo paramInfo : signature) {
					try {
						convertedParams[idx] = TypeConversionHelper.getObjectFromString(
								paramInfo.getType(), paramsArray[idx]
						);
						paramTypes[idx] = paramInfo.getType();
						idx++;
					} catch (IllegalArgumentException e) {
						System.err.println("[error] could not convert value '" + paramsArray[idx] + "' into a valid value object for parameter '" + paramInfo.getName() + "'; sorry.");
					}
				}
				
				Object result = getConnection().invoke(
						operationObject.getObjectName(), 
						operationObject.getOperationName(), 
						convertedParams,
						paramTypes
				);
				System.out.println("[ok] method '" + operationObject.getOperationName() + "' has been invoked successfully.");
				if (result != null) {
					resultString = result.toString();
				}
			// TODO make sure the other operations do not catch throwables either!
//			} catch (Throwable t) {
//				System.err.println("[error] There occurred an error while invoking '" + operationObject.getOperationName() + "' on '" + operationObject.getObjectName() + "'");
//				t.printStackTrace(System.err);
//			}
		}
		return resultString;
	}
	
}

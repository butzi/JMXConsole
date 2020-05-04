package com.jabubo.jmx.util;

import java.io.IOException;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.ReflectionException;

public class TypeConversionHelper {

	public static final Object getObjectFromString(String targetTypeName, String string)
	throws InstanceNotFoundException, AttributeNotFoundException,
	InvalidAttributeValueException, MBeanException,
	ReflectionException, IOException {
		
		Object result = null;
		
		if (targetTypeName.equals("boolean")) {
			result = (string.equalsIgnoreCase("true") ? true : false);
		} else if (targetTypeName.equals("int") || (targetTypeName.equals("java.lang.Integer"))) {
			result = Integer.valueOf(string);
		} else if (targetTypeName.equals("long") || (targetTypeName.equals("java.lang.Long"))) {
			result = Long.valueOf(string).longValue();
		} else if (targetTypeName.equals("double") || (targetTypeName.equals("java.lang.Double"))) {
			result = Double.valueOf(string).doubleValue();
		} else if (targetTypeName.equals("java.lang.String")) {				
			result = string;
		}
		
		if (result == null)
			throw new IllegalArgumentException("Tried to convert the value '" + string + "' into a '" + targetTypeName + "', but found no appropriate method to convert it. Please ask your friendly developer to change this.");
	
		return result;
	}
	
}

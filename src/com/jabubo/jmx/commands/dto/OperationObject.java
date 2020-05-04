package com.jabubo.jmx.commands.dto;

import javax.management.ObjectName;

/**
 * This object describes an JMX operation that should be performed remotely.
 * The object description consists of an object name against which the operation should be
 * executed and the name of the operation.
 * 
 * @author butzi
 */
public class OperationObject {

	private ObjectName objectName;
	private String operationName;

	public OperationObject(ObjectName objectName, String operationName) {
		this.objectName = objectName;
		this.operationName = operationName;
	}

	public ObjectName getObjectName() {
		return objectName;
	}

	public void setObjectName(ObjectName objectName) {
		this.objectName = objectName;
	}

	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

}

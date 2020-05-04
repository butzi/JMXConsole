package com.jabubo.jmx.connection.pool;

public interface MonitorableGenericKeyedObjectPoolMBean {

	int getIdleCount();
	
	int getActiveCount();
	
}

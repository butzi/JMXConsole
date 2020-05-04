package com.jabubo.jmx.connection.pool;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.commons.pool.KeyedPoolableObjectFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.apache.log4j.Logger;

public class MonitorableGenericKeyedObjectPool extends GenericKeyedObjectPool
		implements MonitorableGenericKeyedObjectPoolMBean {
	
	Logger LOGGER = Logger.getLogger(MonitorableGenericKeyedObjectPoolMBean.class);

	public MonitorableGenericKeyedObjectPool(KeyedPoolableObjectFactory factory) {
		super(factory);
		registerJMX();
	}

	private void registerJMX() {
		// register at JMX
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

		ObjectName name;
		try {
			name = new ObjectName("org.hitchhackers.tools.jmx.connection.pool:type=ConnectionPool");
			mbs.registerMBean(this, name);
		} catch (Exception e) {
			LOGGER.warn("could not register connection pool JMX MBean : ", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public int getActiveCount() {
		return super.getNumActive();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getIdleCount() {
		return super.getNumIdle();
	}

}

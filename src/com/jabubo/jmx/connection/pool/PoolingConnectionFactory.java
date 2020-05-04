package com.jabubo.jmx.connection.pool;

import java.io.IOException;
import java.util.HashMap;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.hitchhackers.tools.jmx.connection.AbstractJMXConnectionFactory;

/**
 * JMXConnectionFactory that uses Apache's "commons-pool" library for
 * pooling the JMX connection access to the client machines.
 * 
 * @author butzi
 */
public class PoolingConnectionFactory extends AbstractJMXConnectionFactory {

	private GenericKeyedObjectPool genericKeyedObjectPool;

	public PoolingConnectionFactory() {
		super();
		genericKeyedObjectPool = new MonitorableGenericKeyedObjectPool(new FactoryForNewInstances());
		genericKeyedObjectPool.setWhenExhaustedAction(GenericKeyedObjectPool.WHEN_EXHAUSTED_GROW);
	}

	@Override
	public MBeanServerConnection getConnection(JMXServiceURL url, HashMap<String, String[]> environment)
			throws IOException {
		try {
			JMXConnector connector = JMXConnectorFactory.connect(url, environment);
			
			// Retrieve an MBeanServerConnection that represent the MBeanServer the remote
			// connector server is bound to
			return connector.getMBeanServerConnection();
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	public void returnConnection(JMXServiceURL url, MBeanServerConnection mbeanServerConnection) {
		try {
			genericKeyedObjectPool.returnObject(url, mbeanServerConnection);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	class FactoryForNewInstances extends BaseKeyedPoolableObjectFactory {

		@Override
		public Object makeObject(Object key) throws Exception {
			JMXServiceURL url = (JMXServiceURL) key;
			JMXConnector connector = JMXConnectorFactory.connect(url);			
			
			// Retrieve an MBeanServerConnection that represent the MBeanServer the remote
			// connector server is bound to
			MBeanServerConnection beanServerConnection = connector.getMBeanServerConnection();
			return beanServerConnection;
		}
		
	}
	
}

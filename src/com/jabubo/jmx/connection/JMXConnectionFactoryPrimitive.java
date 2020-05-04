package com.jabubo.jmx.connection;

import java.io.IOException;
import java.util.HashMap;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class JMXConnectionFactoryPrimitive extends AbstractJMXConnectionFactory {

	@Override
	public MBeanServerConnection getConnection(JMXServiceURL url, HashMap<String, String[]> environment) throws IOException {
		JMXConnector connector = JMXConnectorFactory.connect(url, environment);
		
		// Retrieve an MBeanServerConnection that represent the MBeanServer the remote
		// connector server is bound to
		return connector.getMBeanServerConnection();
	}

	public void returnConnection(JMXServiceURL url, MBeanServerConnection mbeanServerConnection) {
	}

}

package com.jabubo.jmx.connection;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXServiceURL;

import org.hitchhackers.tools.jmx.util.parser.ParsedCommandLine;

public interface JMXConnectionFactory {
	
	JMXServiceURL buildURLFromCommandLine(ParsedCommandLine commandLine) throws MalformedURLException;
	
	HashMap<String, String[]> getEnvironment(ParsedCommandLine commandLine);
	
	JMXServiceURL assembleURL(String namingHost, int namingPort) throws MalformedURLException, IOException;
	
	JMXServiceURL assembleURL(String namingHost, int namingPort,
			String serverProtocol, String jndiPath) throws MalformedURLException;

	MBeanServerConnection getConnection(JMXServiceURL url, HashMap<String, String[]> environment) throws IOException;
	
	void returnConnection(JMXServiceURL url, MBeanServerConnection mbeanServerConnection);

}

package com.jabubo.jmx.connection;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;

import org.hitchhackers.tools.jmx.util.parser.ParsedCommandLine;

public abstract class AbstractJMXConnectionFactory implements JMXConnectionFactory {

	public JMXServiceURL buildURLFromCommandLine(ParsedCommandLine commandLine) throws MalformedURLException {
		JMXServiceURL jmxURL = null;
		if (commandLine.hasOption("URL")) {
			jmxURL = new JMXServiceURL(commandLine.getOptionValue("URL"));
		} else {
			/* ... or some fragments in order to build our own URL
			 * in this case, we'll need at least a host and a port to connect to,
			 * and optionally a protocol and a jndipath, but these can be defaulted
			 */
			if ((!commandLine.hasOption("host")) || (!commandLine.hasOption("port"))) {
				throw new IllegalArgumentException("not enough params to establish a connection - at least 'host' and 'port' are required.");
			}

			try {
				int port = Integer.valueOf(commandLine.getOptionValue("port"));
				
				jmxURL = assembleURL(
						commandLine.getOptionValue("host"), 
						port,
						commandLine.getOptionValue("protocol"), 
						commandLine.getOptionValue("jndipath")
				);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("invalid port '" + commandLine.getOptionValue("port") + "'");
			}
		}
		
		return jmxURL;
	}
	
	public HashMap<String, String[]> getEnvironment(ParsedCommandLine commandLine) {
		HashMap<String, String[]>   environment = new HashMap();
		
		if (commandLine.hasOption("user") && commandLine.hasOption("password")) {
		    String[]  credentials = new String[] {commandLine.getOptionValue("user"), commandLine.getOptionValue("password")};
		    environment.put (JMXConnector.CREDENTIALS, credentials);
		}
		
		return environment;
	}
	
	public JMXServiceURL assembleURL(String namingHost, int namingPort) throws MalformedURLException  {
		return assembleURL(namingHost, namingPort, null, null);
	}

	public JMXServiceURL assembleURL(String namingHost,
			int namingPort, String serverProtocol, String jndiPath) throws MalformedURLException {
		//The RMI server's host: this is actually ignored by JSR 160
		// since this information is stored in the RMI stub.
		String serverHost = "host";

		if (serverProtocol == null) {
			serverProtocol = "rmi";
		}

		if (jndiPath == null) {
			jndiPath = "/jmxrmi";
		}

		// The address of the connector server
		JMXServiceURL url = new JMXServiceURL("service:jmx:" + serverProtocol
				+ "://" + serverHost + "/jndi/rmi://" + namingHost + ":"
				+ namingPort + jndiPath);
		
		return url;
	}
	
	public abstract MBeanServerConnection getConnection(JMXServiceURL url, HashMap<String, String[]> environment) throws IOException;

}

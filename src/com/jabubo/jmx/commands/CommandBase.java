package com.jabubo.jmx.commands;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXServiceURL;

import org.hitchhackers.tools.jmx.HelpRequiredException;
import org.hitchhackers.tools.jmx.connection.JMXConnectionFactory;
import org.hitchhackers.tools.jmx.connection.pool.PoolingConnectionFactory;
import org.hitchhackers.tools.jmx.util.parser.Param;
import org.hitchhackers.tools.jmx.util.parser.ParameterParser;
import org.hitchhackers.tools.jmx.util.parser.ParsedCommandLine;
import org.hitchhackers.tools.jmx.util.parser.formatter.HelpFormatter;

/**
 * base class for commands that should be executed via JMX
 * 
 * @author butzi
 */
abstract public class CommandBase {

	// TODO add interval
	// TODO add command/script file
	
	private MBeanServerConnection connection;

	private ParameterParser parser = new ParameterParser();

	private String commandName;

	private JMXServiceURL url;
	
	private HashMap<String, String[]> environment;

	private OutputType outputType = OutputType.TEXT;
	
	private Map<OutputType, Class<? extends OutputFormatter>> formatterByType = 
		new HashMap<OutputType, Class<? extends OutputFormatter>>();

	static JMXConnectionFactory connectionFactory = new PoolingConnectionFactory();

	public static final String USAGE_VERSION = "0.1.6";
	public static final String USAGE_TITLE = "JMXConsoleTools v" + USAGE_VERSION;
	private static final String USAGE_SYNTAX_LINE_START = "./jmx_console.sh";
	
	protected static final String USAGE_CONNECTION_SYNTAX =
		"    (url=<url>|host=<hostname> port=<hostport> [protocol=<protocol>] [jndipath=<jndipath>])";
	protected static final String USAGE_CONNECTION_DETAILS = 
		"Connection parameters:\n" +
		"  You need to either specify a complete JMX Service URL (with url=<url>)\n" +
		"  or the following parameters:\n" +
		"    host\tname/IP of the host to connect to\n" +
		"    port\tport on which a JMX RMI service is running\n" +
		"  Optionally you can specify:\n" +
		"    protocol\tthe protocol to use (defaults to 'rmi')\n" +
		"    jndipath\tthe jndiPath on which the JMX service is bound (defaults to '/jmxrmi')\n" +
		"  In most cases, it should be sufficient to pass something like this:\n" +
		"    host=myserver.domain port=4711\n" +
		"  where <myserver.domain> is the host you want to connect to and <4711> is the port on which\n" +
		"  your application exposes its JMX/RMI interface.";

	
	public CommandBase() {
		super();
		initGlobalParams(parser);
		initParams(parser);
	}
	
	protected void addFormatter(OutputType outputType, Class<? extends OutputFormatter> formatter) {
		formatterByType.put(outputType, formatter);
	}
	
	protected OutputFormatter getFormatter() throws InstantiationException, IllegalAccessException {
		Class<? extends OutputFormatter> result = formatterByType.get(getOutputType());
		if (result == null) {
			result = formatterByType.get(OutputType.TEXT);
		}
		if (result == null) {
			throw new IllegalArgumentException("no output formatter defined for type '" + getOutputType() + "'");			
		}
		OutputFormatter outputFormatter = result.newInstance();
		return outputFormatter;
	}

	public MBeanServerConnection getConnection() {
		return connection;
	}
	
	public OutputType getOutputType() {
		return outputType;
	}

	private void setOutputType(OutputType outputType) {
		// TODO we could validate if we've got an appropriate formatter here
		this.outputType = outputType;
	}
	
	public void init(String[] args) throws IOException {
		ParsedCommandLine commandLine = parser.parse(args);
		// abort intialization if the user wants to see help only
		if (commandLine.hasOption("help")) {
			throw new HelpRequiredException();
		}
		
		// decide which formatting to use
		if (commandLine.hasOption("output_type")) {
			// TODO maybe we should format this exception a bit
			OutputType outputType = 
				CommandBase.OutputType.valueOf(commandLine.getParamValue("output_type"));
			setOutputType(outputType);
		}
		
		// establish the connection
		url = connectionFactory.buildURLFromCommandLine(commandLine);
		
		environment = connectionFactory.getEnvironment(commandLine);
		
		// ...and give the command a chance to init
		processParams(commandLine);
	}
	
	public String doRun() throws Exception {
		connection = connectionFactory.getConnection(url, environment);
                try {
                    String result = run();
                    return result;
                } finally {
                    connectionFactory.returnConnection(url, connection);
                }
	}
	
	// TODO this is ugly...
	
	public String getSyntaxLine() {
		HelpFormatter helpFormatter = new HelpFormatter();
		return helpFormatter.getSyntaxLine(parser);
	}

	public String getSyntaxLineWeb() {
		HelpFormatter helpFormatter = new HelpFormatter();
		helpFormatter.setFormatForWeb(true);
		return helpFormatter.getSyntaxLine(parser);
	}
	
	public String getParameterDetails() {
		HelpFormatter helpFormatter = new HelpFormatter();
		return helpFormatter.getParameterDetails(parser);
	}
	
	public String getParameterDetailsWeb() {
		HelpFormatter helpFormatter = new HelpFormatter();
		helpFormatter.setFormatForWeb(true);
		return helpFormatter.getParameterDetails(parser);
	}
	
	public String getUsageHeaderWeb() {
		return getUsageHeader();
	}
	
	public String getUsageHeaderConsole() {
		return getUsageHeader()
			.replaceAll("<br/>", "\n")
			.replaceAll("<i>", "'")
			.replaceAll("</i>", "'");
	}
	
	
	public void printUsage() {
		System.out.println();
		System.out.println(USAGE_TITLE + " (" + commandName + ")");
		System.out.println();
		
		String commandHeader = getUsageHeaderConsole();
		if (! commandHeader.equals("")) {
			System.out.println(commandHeader);
			System.out.println();
		}
		
		String syntaxLine = getSyntaxLine();
		System.out.println("Usage:");
		System.out.println("  " + USAGE_SYNTAX_LINE_START + " " + commandName + " " + syntaxLine);
		System.out.println();
		System.out.println("Parameters:");
		String parameterDetails = getParameterDetails();
		System.out.println(parameterDetails);
	}

	protected void initGlobalParams(ParameterParser parser) {
		parser.addParam(
				new Param("host").
				setDescription("the host against which to connect").
				setRequired(true).
				setShortName("h")
		);
		
		parser.addParam(
				new Param("port").
				setDescription("the port against which to connect").
				setRequired(true).
				setShortName("p")
		);
		
		parser.addParam(
				new Param("user").
				setDescription("the user to connect with").
				setRequired(false).
				setShortName("U")
		);
		
		parser.addParam(
				new Param("password").
				setDescription("the password to connect with").
				setRequired(false).
				setShortName("P")
		);
		
		parser.addParam(
				new Param("url").
				setDescription("the JMX URL against which to connect (overrides host/port)").
				setShortName("u")	
		);
		
		parser.addParam(
				new Param("debug")
				.setDescription("turns on debugging mode")
				.setShortName("d")
				.setHasNoValue(true)
		);
		
		parser.addParam(
				new Param("help")
				.setDescription("shows the help screen")
				.setShortName("s")
				.setHasNoValue(true)
		);
		
		parser.addParam(
			new Param("output_type").
				setDescription("in which format should the output be returned? (one of JSON or TEXT)").
				setShortName("t")
		);
		
		// TODO re-vive other jmx connection options
		// TODO add jmx security stuff (username, password - more?)
//		options.setKnownParams(new String[] 
//		    { "host", "port", "url", "protocol", "jndipath", "debug", "help" }
//		);
	}
	
	// helper methods for descendant classes
	public String getCommandName() {
		return commandName;
	}

	public void setCommandName(String commandName) {
		this.commandName = commandName;
	}	
	
	// these should be overridden in descendent classes
	abstract protected void initParams(ParameterParser parser);
	
	abstract public String run() throws Exception;

	abstract public void processParams(ParsedCommandLine commandLine);

	public String getUsageHeader() {
		return "";
	}
	
	public enum OutputType {
		
		TEXT('t'),
		JSON('j');
		
		private final char id;
		
		private OutputType(char c) {
		  this.id = c;
		}
		
	}

}

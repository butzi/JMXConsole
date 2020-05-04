package com.jabubo.jmx;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hitchhackers.tools.jmx.commands.Browse;
import org.hitchhackers.tools.jmx.commands.CommandBase;
import org.hitchhackers.tools.jmx.commands.GetThreadInfo;
import org.hitchhackers.tools.jmx.commands.InvokeOperation;
import org.hitchhackers.tools.jmx.commands.ModifyAttribute;
import org.hitchhackers.tools.jmx.commands.ReadAttributes;
import org.hitchhackers.tools.jmx.connection.JMXConnectionFactory;
import org.hitchhackers.tools.jmx.connection.JMXConnectionFactoryPrimitive;

/**
 * This class is responsible for executing JMX commands.
 * 
 * In order to execute a command, it constructs the correct {@link com.jabubo.jmx.CommandBase} descendant,
 * parses and validates parameters, establishes a JMX connection and executes the command.
 * 
 * @author butzi
 */
@SuppressWarnings("unchecked")
public class CommandProcessor {

	private static final Logger LOGGER = Logger.getLogger(CommandProcessor.class);
	
	static Map<String, Class> commandsByName = new HashMap<String, Class>();
	static JMXConnectionFactory connectionFactory = new JMXConnectionFactoryPrimitive();
	
	static {
		// TODO this should be springified
 		commandsByName.put("browse", Browse.class);
		commandsByName.put("get_attribute", ReadAttributes.class);
		commandsByName.put("invoke", InvokeOperation.class);
		commandsByName.put("set_attribute", ModifyAttribute.class);
		commandsByName.put("thread_info", GetThreadInfo.class);
		
		LOGGER.debug("command initialization complete - known commands are:");
		for (Iterator<Map.Entry<String, Class>> iterator = commandsByName.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<String, Class> next = iterator.next();
			LOGGER.debug("  " + next.getKey());
		}
	}

	static public String[] getCommandNames() {
		return commandsByName.keySet().toArray(new String[commandsByName.size()]);
	}
	
	private CommandBase command;
	
	public CommandProcessor(String commandName) {
		// build a new command object
		Class commandClass = commandsByName.get(commandName);
		if (commandClass == null) {
			throw new IllegalArgumentException("invalid command name '" + commandName + "'");
		}
		command = instantiateCommand(commandClass);
		command.setCommandName(commandName);
	}
	
	public void init(String[] args) throws IOException{
		command.init(args);
	}
	
	private CommandBase instantiateCommand(Class clazz) {
		try {
			Object newInstance = clazz.newInstance();
			if (newInstance instanceof CommandBase) {
				CommandBase command = (CommandBase) newInstance;
				return command;
			} else {
				throw new IllegalArgumentException("invalid class " + clazz.getCanonicalName());
			}
		} catch (InstantiationException e) {
			throw new IllegalArgumentException("could not instantiate class " + clazz.getCanonicalName(), e);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("could not access class " + clazz.getCanonicalName(), e);
		}	
	}
	
	public void printUsage() {
		command.printUsage();
	}
	
	public String execute() throws Exception {
		return command.doRun();
	}

	public CommandBase getCommand() {
		return command;
	}
	
}

package com.jabubo.jmx.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hitchhackers.tools.jmx.CommandProcessor;
import org.hitchhackers.tools.jmx.HelpRequiredException;
import org.hitchhackers.tools.jmx.commands.CommandBase;

public class JMXConsoleServlet extends HttpServlet {
	
	Logger LOGGER = Logger.getLogger(JMXConsoleServlet.class);

	private static final long serialVersionUID = 1174181406172089550L;
	// TODO setup embedded servlet container
	// TODO add support for different output formats (json?)
	
	private void displayUsage(HttpServletRequest req, HttpServletResponse resp, String reason) throws ServletException, IOException {
		req.setAttribute("version", CommandBase.USAGE_VERSION);
		req.setAttribute("error_message", reason);
		
		// get the list of all available commands
		String[] commandNames = CommandProcessor.getCommandNames();
		ArrayList<CommandBase> commands = new ArrayList<CommandBase>();
		for (String commandName : commandNames) {
			CommandProcessor commandProcessor = new CommandProcessor(commandName);
			CommandBase command = commandProcessor.getCommand();
			
			commands.add(command);
		}
		req.setAttribute("commands", commands);
		
		getServletContext().getRequestDispatcher("/usage.jsp").forward(req, resp);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		// extract the operation name from the URL
		Pattern pattern = Pattern.compile("/(.+)");
		if ((req.getPathInfo() == null) || (req.getPathInfo().equals(""))) {
			displayUsage(req, resp, null);
			return;	
		}
		Matcher matcher = pattern.matcher(req.getPathInfo());
		String commandName = null;
		if (matcher.matches()) {
			commandName = matcher.group(1);
			req.setAttribute("command_name", commandName);
		} else {
			displayUsage(req, resp, "Please specify a command to execute");
			return;
		}

		// convert the parameters passed to the servlet into a string array
		ArrayList<String> args = new ArrayList<String>();
		Enumeration<String> parameterNames = req.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String paramName = (String) parameterNames.nextElement();
			args.add(paramName + "=" + req.getParameter(paramName));
		}
		
		// and execute the command
		try {
			CommandProcessor processor = new CommandProcessor(commandName);
			processor.init(
				args.toArray(new String[args.size()])
			);
			String result = processor.execute();
			resp.getOutputStream().println(result);
		} catch (Exception e) {
			String errorString = null;
			if (!(e instanceof HelpRequiredException)) {
				LOGGER.error("could not execute command : ", e);
				errorString = e.getMessage();
			}
			displayUsage(req, resp, errorString);
		}
	}

	
}

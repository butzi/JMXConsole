package com.jabubo.jmx.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hitchhackers.tools.jmx.CommandProcessor;

public class JMXBrowserServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2670862399876672043L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		if (
			(req.getParameter("host") == null) ||
			(req.getParameter("port") == null)
		   ) {
			resp.getOutputStream().println("Please specify host and port!");
			return;
		}
		
		CommandProcessor commandProcessor = new CommandProcessor("browse");
		commandProcessor.init(new String[] {
			"host=" + req.getParameter("host"),
			"port=" + req.getParameter("port")
		});
//		String result = commandProcessor.execute();
		
	}
	
	
}

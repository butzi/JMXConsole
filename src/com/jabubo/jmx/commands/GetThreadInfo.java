package com.jabubo.jmx.commands;

import java.io.IOException;
import java.lang.management.ThreadInfo;
import java.util.Arrays;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeDataSupport;

import org.hitchhackers.tools.jmx.util.parser.Param;
import org.hitchhackers.tools.jmx.util.parser.ParameterParser;
import org.hitchhackers.tools.jmx.util.parser.ParsedCommandLine;

/**
 * This command queries thread information via JMX. 
 * 
 * @author butzi
 */
public class GetThreadInfo extends CommandBase {

	private boolean queryCPUInfo = false;
	private ObjectName threadingObject;
	
	public GetThreadInfo() throws MalformedObjectNameException, NullPointerException {
		super();
		
		threadingObject = ObjectName.getInstance("java.lang:type=Threading");		
	}
	
	@Override
	public String getUsageHeader() {
		return 
			"This tool retrieves thread information (current stacktrace, waits/blocks) via JMX and prints them.\n" +
			"If you turn on the 'cpu_info' option, each thread's CPU consumption is queried and displayed in nanoseconds.\n" +
			"NOTE: Querying the CPU consumption takes two more separate JMX calls for each listed thread - this might slow down\n" +
			"your query if the application you connect against has got many threads.";
	}
	
	@Override
	protected void initParams(ParameterParser parser) {
		parser.addParam(
			new Param("cpu_info")
				.setDescription("indicates if CPU usage information should be queried as well")
				.setShortName("c")
				.setHasNoValue(true)
		);
	}

	@Override
	public void processParams(ParsedCommandLine commandLine) {
		queryCPUInfo = commandLine.hasOption("cpu_info");
	}

	@Override
	public String run() throws Exception {
		JMXRetriever retriever = new JMXRetriever();
		String result = retriever.getThreadInformation(getConnection());
		return result;
	}

	class JMXRetriever {	
		
		public String getThreadInformation(MBeanServerConnection theConnection)
				throws MBeanException, AttributeNotFoundException,
				InstanceNotFoundException, ReflectionException, IOException {
			StringBuffer theResult = new StringBuffer();
			// retrieve all thread IDs
			// TODO this should be easier by calling dumpThreads
			long[] threadIDs = (long[]) theConnection.getAttribute(threadingObject, "AllThreadIds");
	
			Arrays.sort(threadIDs);
			
			for (long threadID : threadIDs) {
				// separate JMX call for obtaining thread detail information
				CompositeDataSupport result = (CompositeDataSupport) theConnection.invoke(
						threadingObject, 
						"getThreadInfo", 
						new Object[] { threadID, 100 }, 
						new String[] { "long", "int" } );
				
				// it's possible that the thread does not exist anymore at this moment
				if (result != null) {	
					ThreadInfo threadInfo = ThreadInfo.from(result);
	
					// assemble the output for printing
					StringBuilder sb = new StringBuilder();
					printThreadInfo(theConnection, threadInfo, sb);
					if (queryCPUInfo) {
						getAndPrintThreadCPUInfo(theConnection, threadingObject, threadID, sb);
					}
					printStackTrace(threadInfo.getStackTrace(), sb);
					
					theResult.append(sb.toString());
					theResult.append("\n");
				}
			}
			return theResult.toString();
		}
	
		private void getAndPrintThreadCPUInfo(MBeanServerConnection theConnection, ObjectName threadingObject, long threadID, StringBuilder sb) throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
			Long cpuTime = (Long) theConnection.invoke(
					threadingObject, 
					"getThreadCpuTime", 
					new Object[] { threadID }, 
					new String[] { "long" }
			);
			Long userTime = (Long) theConnection.invoke(
					threadingObject,
					"getThreadUserTime",
					new Object[] { threadID },
					new String[] { "long" }
			);
			sb.append("CPU(User/Total): ");
			sb.append(userTime);
			sb.append("/");
			sb.append(cpuTime);
			sb.append("\n");
		}
	
		private void printThreadInfo(MBeanServerConnection theConnection, ThreadInfo threadInfo, StringBuilder sb) {
			sb.append("Thread");
			sb.append("#");
			sb.append(threadInfo.getThreadId());
			sb.append(" \"");
			sb.append(threadInfo.getThreadName());
			sb.append("\" ");
			sb.append(threadInfo.getThreadState());
			if (threadInfo.getLockName() != null && ! "".equals(threadInfo.getLockName())) {
				sb.append(" on ");
				sb.append(threadInfo.getLockName());
				if (threadInfo.getLockOwnerId() != -1) {
					sb.append("  (owned by: Thread#");
					sb.append(threadInfo.getLockOwnerId());
									
					// get the name of the thread that is blocking
					try {
						CompositeDataSupport result = (CompositeDataSupport) theConnection.invoke(
								threadingObject, 
								"getThreadInfo", 
								new Object[] { threadInfo.getLockOwnerId(), 100 }, 
								new String[] { "long", "int" } );
						
						// it's possible that the thread does not exist anymore at this moment
						if (result != null) {	
							ThreadInfo blockingThread = ThreadInfo.from(result);
							sb.append(" \"");
							sb.append(blockingThread.getThreadName());
							sb.append("\"");
						}
					} catch (Exception e) {
						System.err.println("could not obtain information about blocking thread with ID " + threadInfo.getLockOwnerId());
					} 
					sb.append(")");
				}
			}
			sb.append("\n");
			
			sb.append("blocked/waiting: ");
			sb.append(threadInfo.getBlockedCount());
			sb.append("/");
			sb.append(threadInfo.getWaitedCount());
			sb.append("\n");
		}
		
		private void printStackTrace(StackTraceElement[] stackTraceElements, StringBuilder sb) {
			for (StackTraceElement stackTraceElement : stackTraceElements) {
				sb.append("  ");
				sb.append(stackTraceElement.getClassName());
				sb.append(".");
				sb.append(stackTraceElement.getMethodName());
				sb.append("(");
				if (stackTraceElement.isNativeMethod()) {
					sb.append("Native Method");  
				} else {
					sb.append(stackTraceElement.getFileName());
					sb.append(":");
					sb.append(stackTraceElement.getLineNumber());
				}
				sb.append(")\n");
			}
		}
	}
	
}
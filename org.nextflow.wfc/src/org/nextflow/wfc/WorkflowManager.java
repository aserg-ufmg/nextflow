/*
 * Copyright (C) 2013 Rógel Garcia
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS 
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.nextflow.wfc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;

import org.nextflow.wfc.driver.WorkflowManagerDriver;

/**
 * Starting point to Workflow Manager Engine communication.
 * 
 * 
 * @author rogel
 *
 */
public class WorkflowManager {

	private static Map<String, WorkflowManagerDriver> drivers = new HashMap<String, WorkflowManagerDriver>();
	
	static {
		//load drivers
		ServiceLoader<WorkflowManagerDriver> serviceDrivers  = ServiceLoader.load(WorkflowManagerDriver.class);
		for (Iterator<WorkflowManagerDriver> iterator = serviceDrivers.iterator(); iterator.hasNext();) {
			//WorkflowManagerDriver drv = (WorkflowManagerDriver) 
			iterator.next();
		}
	}
	
	public static void registerDriver(WorkflowManagerDriver driver){
		String protocol = driver.getProtocol();
		drivers.put(protocol, driver);
	}
	
	/**
	 * Creates a connection to the workflow engine.<BR>
	 * 
	 * @wapi WMConnect
	 * @param url
	 * @return
	 */
	public static Session connect(String url){
		return connect(url, null, null, null);
	}
	
	/**
	 * Creates a connection to the workflow engine.<BR>
	 * 
	 * @wapi WMConnect
	 * @param url
	 * @return
	 */
	public static Session connect(String url, Properties properties){
		return connect(url, null, null, properties);
	}
	
	/**
	 * Creates a connection to the workflow engine.<BR>
	 * 
	 * @wapi WMConnect 
	 * @param url
	 * @param username
	 * @param password
	 * @return
	 */
	public static Session connect(String url, String username, String password, Properties properties){
		String protocol = extractProtocol(url);
		WorkflowManagerDriver driver = getSuitableDriver(protocol);
		String uri = url.substring(url.indexOf(protocol) + protocol.length() + 1);
		return driver.connect(uri, username, password, properties);
	}

	private static WorkflowManagerDriver getSuitableDriver(String protocol) {
		WorkflowManagerDriver driver = drivers.get(protocol);
		if(driver == null){
			throw new WorkflowException("No suitable drivers found for protocol: "+protocol);
		}
		return driver;
	}

	private static String extractProtocol(String url) {
		try {
			int protocolStart = url.indexOf(':') + 1;
			int protocolEnd = url.indexOf(':', protocolStart);
			return url.substring(protocolStart, protocolEnd);
		} catch (IndexOutOfBoundsException e) {
			throw new WorkflowException("Invalid url "+url, e);
		}
	}
}

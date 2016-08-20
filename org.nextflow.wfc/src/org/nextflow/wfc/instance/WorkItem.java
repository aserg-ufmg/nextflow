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
package org.nextflow.wfc.instance;

import java.util.Map;

import org.nextflow.wfc.WorkflowObject;

/**
 * @wapi 
 * 	WorkItem interface (section 8.2.9)
 * @author rogel
 *
 */
public interface WorkItem extends WorkflowObject {
	
	String getId();
	
	ActivityInstance getActivityInstance();

	Map<String, Object> complete();
	
	/**
	 * @wapi
	 *   Extension<BR>
	 *   The complete method with parameters was not defined in wapi<BR>
	 *   This is one way to pass parameters from the application to the workflow<BR>
	 * @param attributes
	 */
	Map<String, Object> complete(Map<String, Object> parameters);
	
	void abort();
	
	/**
	 * Returns the parameters passed by the application
	 * @return
	 */
	Map<String, Object> getParameters();
	
	/**
	 * Get the parameter value
	 * @param key
	 * @return
	 */
	Object getParameter(String key);
	
	/**
	 * Set the parameter value
	 * @param key
	 * @param value
	 */
	void setParameter(String key, Object value);
	
	/**
	 * Returns the results of the execution of this work item.
	 * The values of this map should only be changed within calls of ApplicationToolAgent.executeActivity(..)
	 * @return
	 */
	Map<String, Object> getResults();
	
	/**
	 * Searches for a value with key in the WorkItem parameters,
	 * if not found search the WorkItem.activityInstance.processInstance attributes,
	 * if not found return null  
	 * @param key
	 * @return
	 */
	Object findValue(String key);
}

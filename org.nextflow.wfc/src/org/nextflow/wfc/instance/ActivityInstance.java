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

import org.nextflow.wfc.WorkflowObject;
import org.nextflow.wfc.model.ActivityDefinition;

/**
 * @wapi
 *  WMTActivityInst<BR>
 *  Activity Instance (section 8.2.8) 
 * @author rogel
 */
public interface ActivityInstance extends WorkflowObject {

	String getId();
	
	ProcessInstance getProcessInstace();
	
	ActivityDefinition getActivityDefinition();
	
	/**
	 * Returns the work item referent to this activity instance
	 * @return
	 */
	WorkItem getWorkItem();
	
	ActivityState getState();
	
//	Map<String, Object> getAttributes();
//
//	Object getAttribute(String key);
//	
//	/**
//	 * Searches for a value with key in the ActivityInstance attributes,
//	 * if not found search the ActivityInstance.processInstance attributes,
//	 * if not found return null  
//	 * @param key
//	 * @return
//	 */
//	Object findAttribute(String key);
	
	/**
	 * @wapi
	 *   Not defined. It can be used to cancel Activity Instances that doesn't have work itens.<BR>
	 *   Note that this method can have different semantics of the workItem abort() method.
	 */
	void cancel();
	
	/**
	 * @wapi
	 *   Not defined. It can be used to complete Activity Instances that doesn't have work itens.<BR>
	 *   Note that this method can have different semantics of the workItem complete() method.<BR>
	 */
	void complete();
}

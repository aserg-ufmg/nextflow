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
package org.nextflow.wfc.model;

import java.util.Collection;

import org.nextflow.wfc.WorkflowObject;
import org.nextflow.wfc.instance.ProcessInstance;



/**
 * Represents a definition of a process
 * 
 * @wapi 
 *  WMTProcDef<BR>
 *  ProcessDefinition interface (section 8.2.5)
 *  ProcessModel interface (section 8.3.3.3)
 * @author rogel
 */
public interface ProcessDefinition extends WorkflowObject {

	String getId();

	String getName();

	/**
	 * @wapi 
	 *  ProcessModel interface (section 8.3.3.3)
	 * @return
	 */
	Collection<ActivityDefinition> getActivityDefinitions();
	
	/**
	 * @wapi
	 * 
	 * @param activityDefinitionId
	 * @return
	 */
	ActivityDefinition getActivityDefinition(String activityDefinitionId);
	
	/**
	 * @wapi 
	 *  ProcessModel interface (section 8.3.3.3)
	 * @return
	 */
	Collection<TransitionDefinition> getTransitionDefinitions();
	
	/**
	 * Get all instances of this process
	 * @return
	 */
	Collection<ProcessInstance> getProcessInstances();
}

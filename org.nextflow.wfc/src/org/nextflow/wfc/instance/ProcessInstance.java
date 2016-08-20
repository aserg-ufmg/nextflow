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

import java.util.Collection;
import java.util.Map;

import org.nextflow.wfc.WorkflowObject;
import org.nextflow.wfc.model.ActivityDefinition;
import org.nextflow.wfc.model.ProcessDefinition;

/**
 * @wapi
 *   WMTPProcInst <BR>
 *   Process Instance (section 8.2.6)
 *   Server (section 8.2.3)
 * @author rogel
 */
public interface ProcessInstance extends WorkflowObject {

	String getId();
	
	/**
	 * Start this process instance
	 * 
	 * @wapi WMStartProcess 
	 */
	void start();
	
	
	/**
	 * Returns the process definition of this instance
	 * @return
	 */
	ProcessDefinition getProcessDefinition();

	/**
	 * Return the activities for this instance
	 * @return
	 */
	Collection<ActivityInstance> getActivityInstances();
	
	/**
	 * Return the activities for this instance
	 * @return
	 */
	Collection<ActivityInstance> getActivityInstances(ActivityDefinition activityDefinition);
	
	/**
	 * Return the activities for this instance
	 * @return
	 */
	ActivityInstance getActivityInstance(ActivityDefinition activityDefinition);
	
	/**
	 * Returns the activity instance associated with the id
	 * @param activityInstanceId
	 * @return
	 */
	ActivityInstance getActivityInstance(String activityInstanceId);
	
	/**
	 * Query the activity instances by the activity definition id
	 * @param activityDefinitionId
	 * @return
	 */
	Collection<ActivityInstance> getActivityInstancesByDefinitionId(String activityDefinitionId);
	
	/**
	 * Query the activity instances by the activity definition name
	 * @param activityDefinitionId
	 * @return
	 */
	Collection<ActivityInstance> getActivityInstancesByDefinitionName(String activityDefinitionName);
	
	
	/**
	 * Query the activity instances by the activity definition id
	 * @param activityDefinitionId
	 * @return
	 */
	ActivityInstance getActivityInstanceByDefinitionId(String activityDefinitionId);
	
	/**
	 * Query the activity instances by the activity definition name
	 * @param activityDefinitionId
	 * @return
	 */
	ActivityInstance getActivityInstanceByDefinitionName(String activityDefinitionName);
	
	/**
	 * @wapi
	 *   The WAPI defines methods for retrieving internal states of the process, but this solution is workflow specific.<BR>
	 *   The API should define its own possible states and the driver maps its internal states do the specification states.
	 * @return
	 */
	ProcessState getState();
	
	/**
	 * @wapi 
	 *    section 8.2.2
	 * @return
	 */
	Map<String, Object> getAttributes();
	
	/**
	 * @wapi 
	 *    section 8.2.2
	 * @return
	 */
	Object getAttribute(String key);
	
	/**
	 * @wapi 
	 *    section 8.2.2
	 * @return
	 */
	void setAttribute(String key, Object value);
	
	ActivityDefinition getActivityDefinition(String activityDefinitionId);
}

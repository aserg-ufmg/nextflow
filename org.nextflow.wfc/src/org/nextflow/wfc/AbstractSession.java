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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.nextflow.wfc.instance.ActivityInstance;
import org.nextflow.wfc.instance.ProcessInstance;
import org.nextflow.wfc.model.ActivityDefinition;
import org.nextflow.wfc.model.ProcessDefinition;

/**
 * Helper class for Session implementors
 * @author rogel
 *
 */
public abstract class AbstractSession implements Session {
	
	List<ApplicationToolAgent> applicationAgents = new ArrayList<ApplicationToolAgent>();

	

	@Override
	public Collection<ProcessInstance> getProcessInstancesByDefinitionId(String processDefinitionId) {
		Collection<ProcessInstance> processInstances = getProcessInstances();
		List<ProcessInstance> result = new ArrayList<ProcessInstance>();
		for (ProcessInstance processInstance : processInstances) {
			if(processInstance.getProcessDefinition().getId().equals(processDefinitionId)){
				result.add(processInstance);
			}
		}
		return result;
	}

	
	@Override
	public ProcessDefinition getProcessDefinition(String id) {
		List<String> allIds = new ArrayList<String>();
		Iterator<ProcessDefinition> processDefinitions = getProcessDefinitions().iterator();
		while(processDefinitions.hasNext()){
			ProcessDefinition pd = processDefinitions.next();
			allIds.add(pd.getId());
			if(pd.getId().equals(id)){
				return pd;
			}
		}
		throw new WorkflowException("Process definition with id \""+id+"\" not found. Available definitions: "+allIds);
	}
	
	@Override
	public ProcessInstance getProcessInstance(String processInstanceID) {
		Collection<ProcessInstance> processInstances = getProcessInstances();
		for (ProcessInstance processInstance : processInstances) {
			if(processInstance.getId().equals(processInstanceID)){
				return processInstance;
			}
		}
		return null;
	}
	
	@Override
	public void registerAgent(ApplicationToolAgent agent) {
		applicationAgents.add(agent);
	}
	
	public Collection<ApplicationToolAgent> getAgents() {
		return Collections.unmodifiableCollection(applicationAgents);
	}
	
	/**
	 * Invoke the application interfaces registered with this session<BR>
	 * Driver implementors must call this method whenever an ActivityInstance is executed
	 * @param processInstance
	 * @param activityInstance
	 */
	public void invokeApplication(ProcessInstance processInstance, ActivityInstance activityInstance) {
		for (ApplicationToolAgent agents : applicationAgents) {
			agents.executeActivity(processInstance, activityInstance, activityInstance.getWorkItem());
		}
	}
	

	public boolean invokeApplicationCondition(ProcessInstance processInstance, ActivityInstance from, ActivityDefinition target) {
		for (ApplicationToolAgent agents : applicationAgents) {
			if(agents.executePath(processInstance, from, target)){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public ProcessInstance createProcessInstance(String definitionId) {
		return createProcessInstance(getProcessDefinition(definitionId));
	}
}

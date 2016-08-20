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
package org.nextflow.driver.jbpm.toolagent;

import java.util.Collection;

import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;
import org.nextflow.driver.jbpm.JbpmSession;
import org.nextflow.driver.jbpm.instance.JbpmProcessInstance;
import org.nextflow.wfc.instance.ActivityInstance;

public class ToolAgentWorkItemHandler implements WorkItemHandler {

	private JbpmSession session;

	public ToolAgentWorkItemHandler(JbpmSession jbpmSession) {
		this.session = jbpmSession;
	}
	
	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		doWorkItem(workItem);
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		doWorkItem(workItem);
	}
	
	private void doWorkItem(WorkItem workItem) {
		JbpmProcessInstance processInstance = session.getProcessInstance(String.valueOf(workItem.getProcessInstanceId()));
		Collection<ActivityInstance> activityInstances = processInstance.getActivityInstances();
		ActivityInstance activityInstance = null; 
		for (ActivityInstance ai : activityInstances) {
			if(ai.getWorkItem() != null && ai.getWorkItem().getId().equals(String.valueOf(workItem.getId()))){
				activityInstance = ai;
				break;
			}
		}
		session.invokeApplication(processInstance, activityInstance);
	}
	
}

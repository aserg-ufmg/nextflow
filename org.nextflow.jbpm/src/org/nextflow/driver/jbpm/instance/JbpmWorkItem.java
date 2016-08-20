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
package org.nextflow.driver.jbpm.instance;

import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.drools.runtime.process.WorkItemManager;
import org.jbpm.workflow.instance.node.WorkItemNodeInstance;
import org.nextflow.driver.jbpm.JbpmSession;
import org.nextflow.wfc.instance.AbstractWorkItem;
import org.nextflow.wfc.instance.ActivityInstance;

/**
 * @wapi
 *   In jBPM wapi driver some attributes of the WorkItem are mapped with different names in this interface.<BR>
 *   <pre>
 *     -----------------------------------------------------------------------------------
 *     | in jBPM                    | in jBPM wapi driver                                |
 *     -----------------------------------------------------------------------------------
 *     | workItem.getParameters()   | workItem.getActivityInstance().getAttributes()     |
 *     | workItem.getResults()      | workItem.getParameters()                           |
 *     -----------------------------------------------------------------------------------
 *   </pre>  
 * @author rogel
 */
public class JbpmWorkItem extends AbstractWorkItem {

	private JbpmActivityInstance activityInstance;
	private WorkItemNodeInstance nodeInstance;
	private org.drools.process.instance.WorkItem workItem;
	private JbpmSession session;

	public JbpmWorkItem(JbpmActivityInstance jbpmActivityInstance, WorkItemNodeInstance nodeInstance) {
		this.activityInstance = jbpmActivityInstance;
		this.nodeInstance = nodeInstance;
		this.workItem = nodeInstance.getWorkItem();
		this.session = jbpmActivityInstance.getSession();
	}
	
	@Override
	public String getId() {
		if(workItem == null){
			return null;
		}
		return String.valueOf(workItem.getId());
	}
	
	@Override
	public ActivityInstance getActivityInstance() {
		return activityInstance;
	}

	public org.drools.process.instance.WorkItem getInternalWorkItem() {
		return workItem;
	}

	/**
	 * jBPM internal
	 * @return
	 */
	public JbpmSession getSession() {
		return session;
	}
	
	/**
	 * jBPM internal
	 * @return
	 */
	public WorkItemNodeInstance getNodeInstance() {
		return nodeInstance;
	}

	/**
	 * In jBPM driver the parameters are the org.drools.process.instance.WorkItem.getResults()
	 */
	@Override
	public Map<String, Object> getParameters() {
		return workItem.getParameters();
	}
	
	@Override
	public Map<String, Object> complete(Map<String, Object> parameters) {
		activityInstance.setActive(true);
		Set<Entry<String, Object>> entrySet = parameters.entrySet();
		for (Entry<String, Object> entry : entrySet) {
			workItem.getParameters().put(entry.getKey(), entry.getValue());
		}
		session.invokeApplication(activityInstance.getProcessInstace(), activityInstance);
		activityInstance.setActive(false);
		getWorkItemManager().completeWorkItem(workItem.getId(), workItem.getResults());
		return getResults();
	}
	
	@Override
	public void abort() {
		getWorkItemManager().abortWorkItem(workItem.getId());
	}
	
	private WorkItemManager getWorkItemManager() {
		return getSession().getKSession().getWorkItemManager();
	}

	@Override
	public String toString() {
		return "JbpmWorkItem ["+activityInstance.getActivityDefinition().getName()+", "+activityInstance.getState()+", id="+getId()+", activityInstanceId=" + activityInstance.getId() + ", workItemInternal=" + workItem + "]";
	}

	@Override
	public Map<String, Object> getResults() {
		return workItem.getResults();
	}

}

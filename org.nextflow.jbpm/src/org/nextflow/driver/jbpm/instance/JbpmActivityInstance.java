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

import org.jbpm.workflow.instance.NodeInstance;
import org.jbpm.workflow.instance.node.JoinInstance;
import org.jbpm.workflow.instance.node.WorkItemNodeInstance;
import org.nextflow.driver.jbpm.JbpmSession;
import org.nextflow.wfc.instance.AbstractActivityInstance;
import org.nextflow.wfc.instance.ActivityState;
import org.nextflow.wfc.instance.ProcessInstance;
import org.nextflow.wfc.model.ActivityDefinition;

public class JbpmActivityInstance extends AbstractActivityInstance {

	private JbpmProcessInstance processInstance;
	private NodeInstance nodeInstance;
	private String activityInstanceId;
	private ActivityDefinition activityDefinition;
	private JbpmSession session;
	
	private JbpmWorkItem workItem;
	private boolean active;

	public JbpmActivityInstance(JbpmProcessInstance process, NodeInstance nodeInstance) {
		this.processInstance = process;
		this.nodeInstance = nodeInstance;
		this.activityInstanceId = String.valueOf(nodeInstance.getId());
		this.activityDefinition = process.getProcessDefinition().getActivityDefinition(String.valueOf(nodeInstance.getNodeId()));
		this.session = process.getSession();
	}
	
	/**
	 * jBPM internal
	 * @return
	 */
	public JbpmSession getSession() {
		return session;
	}
	
	@Override
	public String getId() {
		return activityInstanceId;
	}

	@Override
	public ActivityDefinition getActivityDefinition() {
		return activityDefinition;
	}

	@Override
	public ProcessInstance getProcessInstace() {
		return processInstance;
	}
	
	/**
	 * jBPM internal
	 * @return
	 */
	public NodeInstance getNodeInstance() {
		return nodeInstance;
	}
	
	@Override
	public JbpmWorkItem getWorkItem() {
		if(getNodeInstance() instanceof WorkItemNodeInstance && workItem == null){
			WorkItemNodeInstance workItemNodeInstance = (WorkItemNodeInstance)getNodeInstance();
			if(workItemNodeInstance.getWorkItem() != null){
				workItem = getSession().getObjectFactory().createWorkItem(this, workItemNodeInstance);
			}
		}
		return workItem;
	}
	

	@Override
	public String toString() {
		return "JbpmActivityInstance [id=" + activityInstanceId + ", processInstanceId="+processInstance.getId()+", activityDefinition="+activityDefinition.getId()+" ("+activityDefinition.getName()+"), state="+getState()+", workItemInternalState="+getInternalState()+"]";
	}

	private String getInternalState() {
		if(getNodeInstance() instanceof WorkItemNodeInstance){
			WorkItemNodeInstance wini = (WorkItemNodeInstance)getNodeInstance();
			if(wini.getWorkItem() != null){
				switch (wini.getWorkItem().getState()) {
				case org.drools.runtime.process.WorkItem.PENDING: 	 return "PENDING";
				case org.drools.runtime.process.WorkItem.ACTIVE: 	 return "ACTIVE";
				case org.drools.runtime.process.WorkItem.COMPLETED: return "COMPLETED";
				case org.drools.runtime.process.WorkItem.ABORTED:   return "ABORTED";
				default: return "UNKNOWN("+wini.getWorkItem().getState()+")";
				}
			}
		} else {
			return "NOT WORKING ITEM NODE";
		}
		return "NO WORK ITEM";
	}

	@Override
	public ActivityState getState() {
		if(active){
			return ActivityState.ACTIVE;
		}
		if(getNodeInstance() instanceof WorkItemNodeInstance){
			WorkItemNodeInstance wini = (WorkItemNodeInstance)getNodeInstance();
			if(wini.getWorkItem() == null){
				return ActivityState.NEW;
			}
			switch (wini.getWorkItem().getState()) {
			case org.drools.runtime.process.WorkItem.PENDING: 	 return ActivityState.PENDING;
			case org.drools.runtime.process.WorkItem.ACTIVE: 	 return ActivityState.ACTIVE;
			case org.drools.runtime.process.WorkItem.COMPLETED: return ActivityState.COMPLETED;
			case org.drools.runtime.process.WorkItem.ABORTED:   return ActivityState.ABORTED;
			default: return ActivityState.UNKNOWN;
			}
		} else {
			return ActivityState.ACTIVE;
		}
	}

	/**
	 * jBPM internal<BR>
	 * Force this activity to be in a ACTIVE state.<BR>
	 * The ACTIVE state indicates that this activity is to be completed.<BR>
	 * It gives a chance for the interface 3 implementation of wapi API (ApplicationToolAgent) to be caled before the 
	 * work item is actually completed. 
	 * @param b
	 */
	public void setActive(boolean b) {
		this.active = b;
	}
	
	/**
	 * jBPM internal<BR>
	 * Tels if this activity is in a forced ACTIVE state.
	 * @return
	 */
	public boolean isActive() {
		return active;
	}

//	@Override
//	public Map<String, Object> getAttributes() {
//		if(getWorkItem() == null){
//			return Collections.unmodifiableMap(new HashMap<String, Object>());
//		}
//		return Collections.unmodifiableMap(getWorkItem().getInternalWorkItem().getParameters());
//	}

	/**
	 * If this activity represents a work item, then the work item is completed.<BR>
	 * Else, if this activity represents a Join, the the join is completed<BR>
	 * Else, nothing is done.
	 */
	@Override
	public void complete() {
		if(nodeInstance instanceof JoinInstance){
			((JoinInstance)nodeInstance).triggerCompleted();
		} else if(getWorkItem() != null){
			getWorkItem().complete();
		}
	}
	
	/**
	 * Call this activity cancel() method
	 */
	@Override
	public void cancel() {
		nodeInstance.cancel();
	}

}

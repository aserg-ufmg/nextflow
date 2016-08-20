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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.drools.runtime.process.NodeInstance;
import org.jbpm.process.instance.ProcessInstance;
import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.jbpm.workflow.instance.node.WorkItemNodeInstance;
import org.nextflow.driver.jbpm.JbpmSession;
import org.nextflow.driver.jbpm.model.JbpmProcessDefinition;
import org.nextflow.wfc.WorkflowException;
import org.nextflow.wfc.instance.AbstractProcessInstance;
import org.nextflow.wfc.instance.ActivityInstance;
import org.nextflow.wfc.instance.ProcessState;
import org.nextflow.wfc.model.ProcessDefinition;

/**
 * @wapi
 *   In jBPM wapi driver some attributes of the ProcessInstance are mapped with different names in this interface.<BR>
 *   <pre>
 *     -------------------------------------------------------------------------
 *     | in jBPM                          | in jBPM wapi driver                |
 *     -------------------------------------------------------------------------
 *     | processInstance.getVariables()   | processInstance.getAttributes()    |
 *     -------------------------------------------------------------------------
 *   </pre>  
 * @author rogel
 */
public class JbpmProcessInstance extends AbstractProcessInstance {

	private String processInstanceId;
	private WorkflowProcessInstance pi;
	private JbpmProcessDefinition processDefinition;
	private JbpmSession session;
	
	private Map<String, Object> initialValues = new HashMap<String, Object>();

	public JbpmProcessInstance(JbpmProcessDefinition jbpmProcessDefinition) {
		this.processDefinition = jbpmProcessDefinition;
		this.session = jbpmProcessDefinition.getSession();
	}
	
	public JbpmProcessInstance(JbpmProcessDefinition jbpmProcessDefinition, WorkflowProcessInstance pi) {
		this(jbpmProcessDefinition);
		setInternalProcessInstance(pi);
	}

	private void setInternalProcessInstance(WorkflowProcessInstance pi) {
		if(this.pi != null){
			throw new IllegalStateException("The internal jbpm process instance was already defined for this process instance");
		}
		this.pi = pi;
		this.processInstanceId = String.valueOf(pi.getId());
	}

	@Override
	public void start() {
		if(this.pi == null){
			setInternalProcessInstance((WorkflowProcessInstance) this.processDefinition.getSession().startProcess(this.processDefinition.getId(), initialValues));
		} else {
			throw new WorkflowException("Process already started");
		}
	}

	@Override
	public ProcessDefinition getProcessDefinition() {
		return processDefinition;
	}

	@Override
	public Collection<ActivityInstance> getActivityInstances() {
		Collection<ActivityInstance> result = new ArrayList<ActivityInstance>();
		Collection<NodeInstance> nodeInstances = pi.getNodeInstances();
		for (NodeInstance nodeInstance : nodeInstances) {
			if(nodeInstance instanceof WorkItemNodeInstance){
				result.add(session.getObjectFactory().createActivityInstance(this, (WorkItemNodeInstance)nodeInstance));
			} else {
				result.add(session.getObjectFactory().createActivityInstance(this, (org.jbpm.workflow.instance.NodeInstance)nodeInstance));
			}
		}
		return Collections.unmodifiableCollection(result);
	}

	@Override
	public String toString() {
		return "JbpmProcessInstance [id=" + processInstanceId + ", processDefinitionId=" + processDefinition.getId() + ", state="+getState()+"]";
	}

	@Override
	public String getId() {
		return processInstanceId;
	}
	
	@Override
	public ProcessState getState() {
		if(pi != null){
			switch (pi.getState()) {
			case ProcessInstance.STATE_ACTIVE: 
				return ProcessState.ACTIVE;
			case ProcessInstance.STATE_COMPLETED: 
				return ProcessState.COMPLETED;
			case ProcessInstance.STATE_SUSPENDED: 
				return ProcessState.SUSPENDED;
			case ProcessInstance.STATE_ABORTED: 
				return ProcessState.ABORTED;
			case ProcessInstance.STATE_PENDING: 
				return ProcessState.PENDING;
			default:
				return ProcessState.UNKNOWN;
			}
		}
		return ProcessState.PENDING;
	}
	
	public WorkflowProcessInstance getInternalProcessInstance() {
		return pi;
	}
	
	public JbpmSession getSession() {
		return session;
	}
	
	ProcessInstanceVariableMap processInstanceVariableMap = new ProcessInstanceVariableMap();
	
	@Override
	public Map<String, Object> getAttributes() {
		return processInstanceVariableMap;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((processDefinition == null) ? 0 : processDefinition
						.hashCode());
		result = prime
				* result
				+ ((processInstanceId == null) ? 0 : processInstanceId
						.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JbpmProcessInstance other = (JbpmProcessInstance) obj;
		if (processDefinition == null) {
			if (other.processDefinition != null)
				return false;
		} else if (!processDefinition.equals(other.processDefinition))
			return false;
		if (processInstanceId == null || other.processInstanceId == null) {
				return false;
		} else if (!processInstanceId.equals(other.processInstanceId))
			return false;
		return true;
	}

	class ProcessInstanceVariableMap extends AbstractMap<String, Object> {
		@Override
		public Object put(String key, Object value) {
			if(pi == null){
				return initialValues.put(key, value);
			}
			Object oldValue = pi.getVariable(key);
			pi.setVariable(key, value);
			return oldValue;
		}

		@Override
		public Set<java.util.Map.Entry<String, Object>> entrySet() {
			Map<String, Object> variables;
			if(pi == null){
				variables = initialValues;
			} else {
				variables = ((RuleFlowProcessInstance)pi).getVariables();
			}
			return variables.entrySet();
		}
	}
}


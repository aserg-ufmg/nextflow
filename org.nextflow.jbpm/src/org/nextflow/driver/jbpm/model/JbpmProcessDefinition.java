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
package org.nextflow.driver.jbpm.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.drools.definition.process.Node;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.nextflow.driver.jbpm.JbpmSession;
import org.nextflow.driver.jbpm.instance.JbpmProcessInstance;
import org.nextflow.wfc.instance.ProcessInstance;
import org.nextflow.wfc.model.AbstractProcessDefinition;
import org.nextflow.wfc.model.ActivityDefinition;
import org.nextflow.wfc.model.TransitionDefinition;

/**
 * 
 * @author rogel
 */
public class JbpmProcessDefinition extends AbstractProcessDefinition {

	private RuleFlowProcess process;
	private JbpmSession session;
	private String id;

	public JbpmProcessDefinition(JbpmSession jbpmSession, RuleFlowProcess process) {
		this.session = jbpmSession;
		this.process = process;
		this.id = process.getId();
		
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return process.getName();
	}
	
	@Override
	public Collection<ActivityDefinition> getActivityDefinitions() {
		Node[] nodes = process.getNodes();
		List<ActivityDefinition> result = new ArrayList<ActivityDefinition>();
		for (Node node : nodes) {
			ActivityDefinition ad = createActivityDefinition(node);
			result.add(ad);
		}
		return Collections.unmodifiableCollection(result);
	}

	/**
	 * jBPM internal
	 * @param node
	 * @return
	 */
	public JbpmActivityDefinition createActivityDefinition(Node node) {
		return getSession().getObjectFactory().createActivityDefinition(this, node);
	}

	
	@Override
	public Collection<TransitionDefinition> getTransitionDefinitions() {
		Collection<ActivityDefinition> activityDefinitions = getActivityDefinitions();
		List<TransitionDefinition> result = new ArrayList<TransitionDefinition>();
		for (ActivityDefinition activityDefinition : activityDefinitions) {
			result.addAll(activityDefinition.getTransitions());
		}
		return Collections.unmodifiableCollection(result);
	}


	@Override
	public Collection<ProcessInstance> getProcessInstances() {
		StatefulKnowledgeSession kSession = getSession().getKSession();
		Collection<ProcessInstance> result = new ArrayList<ProcessInstance>();
		Collection<org.drools.runtime.process.ProcessInstance> processInstances = kSession.getProcessInstances();
		for (org.drools.runtime.process.ProcessInstance processInstance : processInstances) {
			if(processInstance.getProcessId().equals(this.getId())){
				result.add(getSession().getObjectFactory().createProcessInstace((JbpmProcessDefinition) processInstance));
			}
		}
		return Collections.unmodifiableCollection(result);
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
	public JbpmProcessInstance newInstance() {
		return getSession().getObjectFactory().createProcessInstace(this);
	}
	
	@Override
	public String toString() {
		return "JbpmProcessDefinition [id=" + getId() + ", name="+ getName() + ", package="+ process.getPackageName() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		JbpmProcessDefinition other = (JbpmProcessDefinition) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}


}

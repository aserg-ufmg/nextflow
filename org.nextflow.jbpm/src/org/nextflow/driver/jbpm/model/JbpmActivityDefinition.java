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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.definition.process.Connection;
import org.drools.definition.process.Node;
import org.jbpm.workflow.core.node.ActionNode;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.HumanTaskNode;
import org.jbpm.workflow.core.node.Join;
import org.jbpm.workflow.core.node.Split;
import org.jbpm.workflow.core.node.StartNode;
import org.jbpm.workflow.core.node.WorkItemNode;
import org.nextflow.driver.jbpm.JbpmSession;
import org.nextflow.wfc.model.ActivityDefinition;
import org.nextflow.wfc.model.ActivityType;
import org.nextflow.wfc.model.ProcessDefinition;
import org.nextflow.wfc.model.TransitionDefinition;

public class JbpmActivityDefinition implements ActivityDefinition {

	private Node node;
	private JbpmProcessDefinition definition;
	private String id;

	static final Map<Class<? extends Node>, ActivityType> activityTypes = new HashMap<Class<? extends Node>, ActivityType>();
	
	static {
		activityTypes.put(StartNode.class, ActivityType.START);
		activityTypes.put(EndNode.class, ActivityType.END);
		activityTypes.put(ActionNode.class, ActivityType.TASK);
		activityTypes.put(HumanTaskNode.class, ActivityType.HUMAN_TASK);
		activityTypes.put(Join.class, ActivityType.JOIN);
		activityTypes.put(Split.class, ActivityType.SPLIT);
	}

	public JbpmActivityDefinition(JbpmProcessDefinition jbpmProcessDefinition, Node node) {
		this.definition = jbpmProcessDefinition;
		this.node = node;
		this.id = String.valueOf(node.getId()); 
	}
	
	/**
	 * jBPM internal
	 * @return
	 */
	public JbpmSession getSession() {
		return definition.getSession();
	}
	
	@Override
	public String getId(){
		return id;
	}
	
	@Override
	public String getName() {
		return node.getName();
	}

	@Override
	public ActivityType getType() {
		return activityTypes.get(node.getClass());
	}
	
	/**
	 * jBPM internal
	 * @return
	 */
	public boolean isWorkNode(){
		return node instanceof WorkItemNode;
	}
	
	/**
	 * jBPM internal
	 * @return
	 */
	public WorkItemNode getWorkItemNode(){
		if(isWorkNode()){
			return (WorkItemNode) node;
		}
		return null;
	}
	
	/**
	 * jBPM internal
	 * @return
	 */
	public String getWorkNodeName(){
		if(isWorkNode()){
			return getWorkItemNode().getWork().getName();
		}
		return null;
	}
	
	@Override
	public Collection<TransitionDefinition> getTransitions() {
		//TODO CACHE
		Collection<List<Connection>> values = node.getOutgoingConnections().values();
		List<TransitionDefinition> result = new ArrayList<TransitionDefinition>();
		for (List<Connection> list : values) {
			for (Connection connection : list) {
				result.add(getSession().getObjectFactory().createTransationDefinition(definition, connection));
			}
		}
		return result;
	}
	

	@Override
	public ProcessDefinition getProcessDefinition() {
		return definition;
	}
	
	
	@Override
	public String toString() {
		return "JbpmActivityDefinition [id=" + getId() + ", name="
		+ getName() + ", type=" + getType() + "]";
	}

	/**
	 * jBPM
	 * @return
	 */
	public Node getNode() {
		return node;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((definition == null) ? 0 : definition.hashCode());
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
		JbpmActivityDefinition other = (JbpmActivityDefinition) obj;
		if (definition == null) {
			if (other.definition != null)
				return false;
		} else if (!definition.equals(other.definition))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
}

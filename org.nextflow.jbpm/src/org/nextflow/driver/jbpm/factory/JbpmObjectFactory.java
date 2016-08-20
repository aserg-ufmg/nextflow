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
package org.nextflow.driver.jbpm.factory;

import org.drools.definition.process.Connection;
import org.drools.definition.process.Node;
import org.drools.definition.process.Process;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.workflow.instance.NodeInstance;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.jbpm.workflow.instance.node.WorkItemNodeInstance;
import org.nextflow.driver.jbpm.JbpmActivityCallback;
import org.nextflow.driver.jbpm.JbpmActivityCallbackImpl;
import org.nextflow.driver.jbpm.JbpmSession;
import org.nextflow.driver.jbpm.instance.JbpmActivityInstance;
import org.nextflow.driver.jbpm.instance.JbpmProcessInstance;
import org.nextflow.driver.jbpm.instance.JbpmWorkItem;
import org.nextflow.driver.jbpm.model.JbpmActivityDefinition;
import org.nextflow.driver.jbpm.model.JbpmProcessDefinition;
import org.nextflow.driver.jbpm.model.JbpmTransitionDefinition;

public class JbpmObjectFactory implements IJbpmObjectFactory {
	
	JbpmSession jbpmSession;
	
	public void setJbpmSession(JbpmSession jbpmSession) {
		this.jbpmSession = jbpmSession;
	}

	@Override
	public JbpmProcessDefinition createProcessDefinition(Process process) {
		return new JbpmProcessDefinition(jbpmSession, (RuleFlowProcess)process);
	}

	@Override
	public JbpmProcessInstance createProcessInstace(JbpmProcessDefinition jbpmProcessDefinition) {
		return new JbpmProcessInstance(jbpmProcessDefinition);
	}

	@Override
	public JbpmProcessInstance createProcessInstace(WorkflowProcessInstance processInstance) {
		return new JbpmProcessInstance(jbpmSession.getProcessDefinition(processInstance.getProcessId()), processInstance);
	}

	@Override
	public JbpmActivityDefinition createActivityDefinition(JbpmProcessDefinition jbpmProcessDefinition, Node node) {
		return new JbpmActivityDefinition(jbpmProcessDefinition, node);
	}

	@Override
	public JbpmTransitionDefinition createTransationDefinition(JbpmProcessDefinition jbpmProcessDefinition, Connection connection) {
		return new JbpmTransitionDefinition(jbpmProcessDefinition, connection);
	}

	@Override
	public JbpmActivityInstance createActivityInstance(JbpmProcessInstance process, NodeInstance nodeInstance) {
		return new JbpmActivityInstance(process, nodeInstance);
	}

	@Override
	public JbpmWorkItem createWorkItem(JbpmActivityInstance jbpmActivityInstance, WorkItemNodeInstance nodeInstance) {
		return new JbpmWorkItem(jbpmActivityInstance, nodeInstance);
	}

	@Override
	public JbpmActivityCallback createCallback(JbpmSession jbpmSession) {
		return new JbpmActivityCallbackImpl(jbpmSession);
	}

}

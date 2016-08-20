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
import org.jbpm.workflow.instance.NodeInstance;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.jbpm.workflow.instance.node.WorkItemNodeInstance;
import org.nextflow.driver.jbpm.JbpmActivityCallback;
import org.nextflow.driver.jbpm.JbpmSession;
import org.nextflow.driver.jbpm.instance.JbpmActivityInstance;
import org.nextflow.driver.jbpm.instance.JbpmProcessInstance;
import org.nextflow.driver.jbpm.instance.JbpmWorkItem;
import org.nextflow.driver.jbpm.model.JbpmActivityDefinition;
import org.nextflow.driver.jbpm.model.JbpmProcessDefinition;
import org.nextflow.driver.jbpm.model.JbpmTransitionDefinition;

/**
 * Factory for the jBPM driver objects
 * @author rogel
 */
public interface IJbpmObjectFactory {
	
	void setJbpmSession(JbpmSession jbpmSession);

	JbpmProcessDefinition createProcessDefinition(Process process);

	JbpmProcessInstance createProcessInstace(JbpmProcessDefinition jbpmProcessDefinition);

	JbpmProcessInstance createProcessInstace(WorkflowProcessInstance processInstance);

	JbpmActivityDefinition createActivityDefinition(JbpmProcessDefinition jbpmProcessDefinition, Node node);

	JbpmTransitionDefinition createTransationDefinition(JbpmProcessDefinition definition, Connection connection);

	JbpmActivityInstance createActivityInstance(JbpmProcessInstance process, NodeInstance nodeInstance);

	JbpmWorkItem createWorkItem(JbpmActivityInstance jbpmActivityInstance, WorkItemNodeInstance nodeInstance);

	JbpmActivityCallback createCallback(JbpmSession jbpmSession);

}

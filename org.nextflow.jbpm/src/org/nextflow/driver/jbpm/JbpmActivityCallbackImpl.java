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
package org.nextflow.driver.jbpm;

import org.drools.definition.process.Node;
import org.drools.definition.process.NodeContainer;
import org.drools.runtime.process.ProcessContext;
import org.jbpm.workflow.core.node.Split;
import org.jbpm.workflow.instance.NodeInstance;
import org.jbpm.workflow.instance.node.SplitInstance;
import org.nextflow.driver.jbpm.instance.JbpmProcessInstance;
import org.nextflow.wfc.instance.ActivityInstance;
import org.nextflow.wfc.model.ActivityDefinition;

public class JbpmActivityCallbackImpl implements JbpmActivityCallback {

	private JbpmSession session;

	public JbpmActivityCallbackImpl(JbpmSession jbpmSession) {
		this.session = jbpmSession;
	}

	@Override
	public void invokeApplication(ProcessContext kcontext) {
		NodeInstance nodeInstance = (NodeInstance) kcontext.getNodeInstance();
		JbpmProcessInstance processInstance = session.getProcessInstance(String.valueOf(kcontext.getProcessInstance().getId()));
		ActivityInstance activityInstance = null;
		if(nodeInstance != null){ //is it possible to not happen?
			activityInstance = session.getObjectFactory().createActivityInstance(processInstance, nodeInstance);
		}
		session.invokeApplication(processInstance, activityInstance);
	}
	
	@Override
	public boolean invokeApplicationCondition(ProcessContext kcontext, String targetRef) {
		JbpmProcessInstance processInstance = session.getProcessInstance(String.valueOf(kcontext.getProcessInstance().getId()));
		NodeInstance nodeInstance = (NodeInstance) kcontext.getNodeInstance();
		ActivityInstance source = null;
		if(nodeInstance != null){ //is it possible to not happen?
			source = session.getObjectFactory().createActivityInstance(processInstance, nodeInstance);
		}

		if(nodeInstance instanceof SplitInstance){
			SplitInstance splitInstance = (SplitInstance) nodeInstance;
			Split split = (Split) splitInstance.getNode();
			NodeContainer nodeContainer = split.getNodeContainer();
			Node targetNode = getTargetNode(targetRef, nodeContainer);
			ActivityDefinition targetActivityDefinition = processInstance.getProcessDefinition().getActivityDefinition(String.valueOf(targetNode.getId()));
			return session.invokeApplicationCondition(processInstance, source, targetActivityDefinition);
		} else {
			throw new IllegalArgumentException("SplitInstance expected");
		}
	}
	
	public Node getTargetNode(String targetRef, NodeContainer nodeContainer){
		//copied from org.jbpm.bpmn2.xml.ProcessHandler
		Node target = null;
		try {
			// remove starting _
			targetRef = targetRef.substring(1);
	        // remove ids of parent nodes
			targetRef = targetRef.substring(targetRef.lastIndexOf("-") + 1);
			target = nodeContainer.getNode(new Integer(targetRef));
		} catch (NumberFormatException e) {
		    // try looking for a node with same "UniqueId" (in metadata)
            for (Node node: nodeContainer.getNodes()) {
                if (targetRef.equals(node.getMetaData().get("UniqueId"))) {
                    target = node;
                    break;
                }
            }
            if (target == null) {
                throw new IllegalArgumentException("Could not find target node for connection:" + targetRef);
            }
		}
		return target;
	}

	@Override
	public String toString() {
		return "JbpmActivityCallbackImpl@"+hashCode();
	}


}

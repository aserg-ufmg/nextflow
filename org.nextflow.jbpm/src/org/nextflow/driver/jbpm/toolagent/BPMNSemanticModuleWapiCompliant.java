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

import java.util.ArrayList;
import java.util.List;

import org.drools.xml.ExtensibleXmlParser;
import org.jbpm.bpmn2.core.SequenceFlow;
import org.jbpm.bpmn2.xml.BPMNSemanticModule;
import org.jbpm.bpmn2.xml.ScriptTaskHandler;
import org.jbpm.bpmn2.xml.SequenceFlowHandler;
import org.jbpm.bpmn2.xml.UserTaskHandler;
import org.jbpm.workflow.core.DroolsAction;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.impl.DroolsConsequenceAction;
import org.jbpm.workflow.core.node.ActionNode;
import org.jbpm.workflow.core.node.WorkItemNode;
import org.nextflow.driver.jbpm.JbpmActivityCallback;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * @see BPMNSemanticModule 
 * @author rogel
 */
public class BPMNSemanticModuleWapiCompliant extends BPMNSemanticModule {

	public BPMNSemanticModuleWapiCompliant() {
		super();
		
		addHandler("userTask", new UserTaskHandler(){
			@Override
			protected void handleNode(Node node, Element element, String uri, String localName, ExtensibleXmlParser parser) throws SAXException {
				super.handleNode(node, element, uri, localName, parser);
				WorkItemNode workItemNode = (WorkItemNode) node;
				String[] actionTypes = workItemNode.getActionTypes();
				for (String type : actionTypes) {
					List<DroolsAction> actions = workItemNode.getActions(type);
					if(actions == null){
						workItemNode.setActions(type, actions = new ArrayList<DroolsAction>());
					}
					actions.add(new DroolsConsequenceAction("java", getConsequenceCall()));
				}
			}
		});
		addHandler("scriptTask", new ScriptTaskHandler(){
			protected void handleNode(Node node, org.w3c.dom.Element element, String uri, String localName, org.drools.xml.ExtensibleXmlParser parser) throws org.xml.sax.SAXException {
				super.handleNode(node, element, uri, localName, parser);
				DroolsConsequenceAction action = (DroolsConsequenceAction) ((ActionNode)node).getAction();
				action.setConsequence(action.getConsequence()+getConsequenceCall());
			};
		});
		
		addHandler("sequenceFlow", new SequenceFlowHandler(){
			@Override
			public Object end(String uri, String localName, ExtensibleXmlParser parser) throws SAXException {
				SequenceFlow sequenceFlow = (SequenceFlow) super.end(uri, localName, parser);
				if(sequenceFlow.getExpression() == null || sequenceFlow.getExpression().isEmpty()){
					sequenceFlow.setLanguage("java");
					sequenceFlow.setExpression(getConditionalConsequenceCall(sequenceFlow.getTargetRef()));
				}
				return sequenceFlow;
			}
		});
	}

	public String getConditionalConsequenceCall(String toRef){
		String jbpmActivityCallback = JbpmActivityCallback.class.getName();
		StringBuilder builder = new StringBuilder();
		builder.append(";"+jbpmActivityCallback+" _jbpmCallback = ("+jbpmActivityCallback+") kcontext.getVariable(\"jbpmCallback\"); ");
		builder.append("if(_jbpmCallback != null){return _jbpmCallback.invokeApplicationCondition(kcontext, \""+toRef+"\");} else {System.err.println(\"Warning: jbpmCallback variable not found in context (no wapi interface 3 provided) (returning false to path id "+toRef+")\"); return false;} ");
		return builder.toString();
	}
	
	public String getConsequenceCall(){
		String jbpmActivityCallback = JbpmActivityCallback.class.getName();
		StringBuilder builder = new StringBuilder();
		builder.append(";"+jbpmActivityCallback+" _jbpmCallback = ("+jbpmActivityCallback+") kcontext.getVariable(\"jbpmCallback\"); ");
		builder.append("if(_jbpmCallback != null){_jbpmCallback.invokeApplication(kcontext);} else {System.err.println(\"Warning: jbpmCallback variable not found in context (no wapi interface 3 provided)\");} ");
		return builder.toString();
	}
}

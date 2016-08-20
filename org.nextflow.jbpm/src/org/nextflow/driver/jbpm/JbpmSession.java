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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.drools.KnowledgeBase;
import org.drools.definition.KnowledgePackage;
import org.drools.definition.process.Node;
import org.drools.definition.process.Process;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.process.core.Work;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.workflow.core.node.WorkItemNode;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.nextflow.driver.jbpm.factory.IJbpmObjectFactory;
import org.nextflow.driver.jbpm.factory.IKSessionFactory;
import org.nextflow.driver.jbpm.factory.JbpmObjectFactory;
import org.nextflow.driver.jbpm.factory.KSessionFactory;
import org.nextflow.driver.jbpm.instance.JbpmProcessInstance;
import org.nextflow.driver.jbpm.model.JbpmProcessDefinition;
import org.nextflow.wfc.AbstractSession;
import org.nextflow.wfc.instance.ProcessInstance;
import org.nextflow.wfc.model.ProcessDefinition;

public class JbpmSession extends AbstractSession {
	
	public static final String JBPM_CALLBACK = "jbpmCallback";

	private KnowledgeBase kBase;
	
	private StatefulKnowledgeSession kSession = null;
	
	private IKSessionFactory kSessionFactory;
	
	private IJbpmObjectFactory objectFactory;

	private Properties properties;

	public JbpmSession(KnowledgeBase kBase, Properties properties) {
		this.kBase = kBase;
		this.properties = properties;
		init();
	}
	
	JbpmActivityCallback jbpmCallback;
	
	private JbpmActivityCallback getJbpmCallback() {
		if(jbpmCallback == null){
			jbpmCallback = getObjectFactory().createCallback(this);
		}
		return jbpmCallback ;
	}

	protected void init() {
		kSessionFactory = new KSessionFactory();
		objectFactory = new JbpmObjectFactory();
		objectFactory.setJbpmSession(this);
	}
	
	/**
	 * jBPM internal
	 * @return
	 */
	public IJbpmObjectFactory getObjectFactory() {
		return objectFactory;
	}
	
	/**
	 * jBPM driver internal 
	 * @return
	 */
	public StatefulKnowledgeSession getKSession(){
		if(kSession == null){
			kSession = kSessionFactory.buildKSession(this, kBase);
			if(JbpmSessionPropertiesUtil.isUseConsoleLogger(properties)){
				KnowledgeRuntimeLoggerFactory.newConsoleLogger(kSession);
			}
		}
		return kSession;
	}

	/**
	 * jBPM internal
	 * @return
	 */
	public List<Work> getWorks(){
		List<Work> works = new ArrayList<Work>();
		Collection<Process> processes = kBase.getProcesses();
		for (Process process : processes) {
			Node[] nodes = ((RuleFlowProcess)process).getNodes();
			for (Node node : nodes) {
				if(node instanceof WorkItemNode){
					works.add(((WorkItemNode)node).getWork());
				}
			}
		}
		return works;
	}
	
	@Override
	public void close() {
		if(kSession != null){
			kSession.dispose();
		}
	}

	@Override
	public Collection<ProcessDefinition> getProcessDefinitions() {
		List<ProcessDefinition> result = new ArrayList<ProcessDefinition>();
		Collection<Process> processes = kBase.getProcesses();
		for (Process process : processes) {
			ProcessDefinition pd = createProcessDefinition(process);
			result.add(pd);
		}
		return result;
	}
	
	@Override
	public Collection<ProcessInstance> getProcessInstances() {
		Collection<ProcessInstance> result = new ArrayList<ProcessInstance>();
		Collection<org.drools.runtime.process.ProcessInstance> processInstances = getNativeProcessInstances();
		for (org.drools.runtime.process.ProcessInstance processInstance : processInstances) {
			result.add(objectFactory.createProcessInstace((WorkflowProcessInstance) processInstance));
		}
		return result;
	}
	
	@Override
	public JbpmProcessDefinition getProcessDefinition(String id) {
		return (JbpmProcessDefinition) super.getProcessDefinition(id);
	}

	private Collection<org.drools.runtime.process.ProcessInstance> getNativeProcessInstances() {
		StatefulKnowledgeSession kSession = getKSession();
		Collection<org.drools.runtime.process.ProcessInstance> processInstances = kSession.getProcessInstances();
		return processInstances;
	}

	private JbpmProcessDefinition createProcessDefinition(Process process) {
		return objectFactory.createProcessDefinition(process);
	}

	@Override
	public ProcessInstance createProcessInstance(ProcessDefinition definition) {
		return ((JbpmProcessDefinition)definition).newInstance();
	}
	
	public JbpmProcessInstance getProcessInstance(String processInstanceID) {
		return (JbpmProcessInstance) super.getProcessInstance(processInstanceID);
	};

	/**
	 * jBPM internal
	 * @return
	 */
	Collection<KnowledgePackage> getKnowledgePackages() {
		return kBase.getKnowledgePackages();
	}

	/**
	 * jBPM internal
	 * @return
	 */
	Collection<Process> getProcesses() {
		return kBase.getProcesses();
	}

	@Override
	public String toString() {
		return "JbpmSession [kBase=" + kBase + ", kSession=" + kSession
				+ ",\n  knowledgePackages=" + toStringKb(getKnowledgePackages())
				+ ",\n  processes=" + toStringProcesses(getProcesses()) + "\n]";
	}

	private String toStringProcesses(Collection<Process> processes) {
		String names = "";
		for (Process process : processes) {
			names += "; ["+process.getId()+":"+process.getName()+":"+process.getPackageName()+":"+process.getType()+"]";
		}
		return names.substring(2);
	}

	private String toStringKb(Collection<KnowledgePackage> knowledgePackages) {
		String names = "";
		for (KnowledgePackage knowledgePackage : knowledgePackages) {
			names += "; " +knowledgePackage.getName() ;
		}
		return names.substring(2);
	}

	/**
	 * jBPM internal
	 * @param id
	 * @return
	 */
	public org.drools.runtime.process.ProcessInstance startProcess(String id, Map<String, Object> initialValues) {
		if(initialValues == null){
			initialValues = new HashMap<String, Object>();
		}
		initialValues.put(JBPM_CALLBACK, getJbpmCallback());
		org.drools.runtime.process.ProcessInstance process = this.getKSession().startProcess(id, initialValues);
		return process;
	}


}

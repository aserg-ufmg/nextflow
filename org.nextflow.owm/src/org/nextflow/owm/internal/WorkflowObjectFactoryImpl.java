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
package org.nextflow.owm.internal;


import org.nextflow.owm.Configuration;
import org.nextflow.owm.WorkflowObjectFactory;
import org.nextflow.owm.WorkflowRepository;
import org.nextflow.owm.internal.proxy.ProxyContextObject;
import org.nextflow.owm.internal.proxy.ProxyWorkflowProcess;
import org.nextflow.owm.mapping.WorkflowProcess;
import org.nextflow.wfc.Session;
import org.nextflow.wfc.model.ProcessDefinition;

public class WorkflowObjectFactoryImpl implements WorkflowObjectFactory {

	Configuration configuration;
	Mapper mapper;
	
	public WorkflowObjectFactoryImpl(Configuration configuration) {
		this.configuration = configuration;
		this.mapper = configuration.getMapper();
		configuration.getSession().registerAgent(new CallbackToolAgent(configuration));
	}
	
	public Configuration getConfiguration() {
		return configuration;
	}
	

	@Override
	public void start(Object workflowProcess) {
		((WorkflowProcess)workflowProcess).start();
	}
	
	@Override
	public <E> E start(Class<E> workflowInterfaceClass) {
		E newInstance = newInstance(workflowInterfaceClass);
		start(newInstance);
		return newInstance;
	}

	@Override
	public <E> E newInstance(Class<E> workflowInterfaceClass) {
		ProcessDefinition processDefinition = getProcessDefinitionForInterface(workflowInterfaceClass);
		
		E proxy = ProxyWorkflowProcess.createFor(workflowInterfaceClass, processDefinition, configuration);
		return proxy;
	}

	protected <E> ProcessDefinition getProcessDefinitionForInterface(Class<E> workflowInterfaceClass) {
		mapper.checkWorkflowInterface(workflowInterfaceClass);
		ProcessDefinition processDefinition = mapper.getDefinitionForClass(workflowInterfaceClass, configuration);
		mapper.checkInterfaceMethods(processDefinition, workflowInterfaceClass);
		return processDefinition;
	}

	@Override
	public <E> E start(Class<E> workflowInterfaceClass, Object context) {
		E newInstance = newInstance(workflowInterfaceClass, context);
		start(newInstance);
		return newInstance;
	}

	@Override
	public <E> E newInstance(Class<E> workflowInterfaceClass, Object context) {
		E newInstance = newInstance(workflowInterfaceClass);
		if(context != null){
			ProxyContextObject.setValuesOfObjectForProcessInstance(context, ((WorkflowProcess)newInstance).getProcessInstance());
		}
		return newInstance;
	}

	@Override
	public WorkflowRepository getRepository() {
		return new WorkflowRepositoryImpl(this);
	}

	@Override
	public Session getSession() {
		return configuration.getSession();
	}


	
}

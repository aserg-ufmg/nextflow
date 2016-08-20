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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.nextflow.owm.Configuration;
import org.nextflow.owm.WorkflowRepository;
import org.nextflow.owm.internal.proxy.ProxyWorkflowProcess;
import org.nextflow.wfc.instance.ProcessInstance;
import org.nextflow.wfc.model.ProcessDefinition;

class WorkflowRepositoryImpl implements WorkflowRepository {

	private WorkflowObjectFactoryImpl workflowObjectFactory;
	private Mapper mapper;
	private Configuration configuration;

	public WorkflowRepositoryImpl(WorkflowObjectFactoryImpl workflowObjectFactoryImpl) {
		this.workflowObjectFactory = workflowObjectFactoryImpl;
		this.mapper = workflowObjectFactoryImpl.mapper;
		this.configuration = workflowObjectFactory.configuration;
	}

	@Override
	public <E> List<E> getRunningProcesses(Class<E> workflowInterfaceClass) {
		ProcessDefinition processDefinition = workflowObjectFactory.getProcessDefinitionForInterface(workflowInterfaceClass);
		Collection<ProcessInstance> processInstances = configuration.getSession().getProcessInstancesByDefinitionId(processDefinition.getId());
		List<E> result = new ArrayList<E>();
		for (ProcessInstance processInstance : processInstances) {
			result.add(ProxyWorkflowProcess.createFor(workflowInterfaceClass, processInstance, configuration));
		}
		return result;
	}

	@Override
	public <E> E getRunningProcess(Class<E> workflowInterfaceClass, String id) {
		ProcessInstance processInstance = configuration.getSession().getProcessInstance(id);
		return ProxyWorkflowProcess.createFor(workflowInterfaceClass, processInstance, configuration);
	}

	
}

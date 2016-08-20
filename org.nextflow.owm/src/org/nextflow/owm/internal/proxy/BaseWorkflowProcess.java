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
package org.nextflow.owm.internal.proxy;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nextflow.owm.Configuration;
import org.nextflow.owm.internal.Mapper;
import org.nextflow.owm.mapping.WorkflowProcess;
import org.nextflow.wfc.Session;
import org.nextflow.wfc.instance.ActivityInstance;
import org.nextflow.wfc.instance.ProcessInstance;
import org.nextflow.wfc.model.ProcessDefinition;

public class BaseWorkflowProcess implements WorkflowProcess {
	
	protected Session session;
	protected ProcessInstance processInstance;
	protected Mapper mapper;
	private Configuration configuration;

	public BaseWorkflowProcess(Configuration configuration, ProcessDefinition definition) {
		this.session = configuration.getSession();
		this.processInstance = session.createProcessInstance(definition);
		this.mapper = configuration.getMapper();
		this.configuration = configuration;
	}

	@Override
	public String getId() {
		return processInstance.getId();
	}
	
	@Override
	public void start() {
		processInstance.start();
	}

	public ProcessInstance getProcessInstance() {
		return processInstance;
	}
	
	public void setProcessInstance(ProcessInstance processInstance) {
		this.processInstance = processInstance;
	}
	

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Enum<E>> List<E> getAvailableTasks(Class<E> e) {
		List<String> availableTasks = getAvailableTasksNameIds();
		List<E> result = new ArrayList<E>();
		Enum<E>[] enumConstants = e.getEnumConstants();
		for (Enum<E> enum1 : enumConstants) {
			if(availableTasks.contains(mapper.formatTaskName(enum1.name()))){
				result.add((E) enum1);
			}
		}
		return result;
	}
	
	@Override
	public List<String> getAvailableTasksNameIds() {
		List<String> result = new ArrayList<String>();
		Collection<ActivityInstance> activityInstances = processInstance.getActivityInstances();
		for (ActivityInstance activityInstance : activityInstances) {
			result.add(mapper.formatTaskName(activityInstance.getActivityDefinition().getName()));
		}
		return result;
	}
	

	@Override
	public List<String> getAvailableTasks() {
		List<String> result = new ArrayList<String>();
		Collection<ActivityInstance> activityInstances = processInstance.getActivityInstances();
		for (ActivityInstance activityInstance : activityInstances) {
			result.add(activityInstance.getActivityDefinition().getName());
		}
		return result;
	}
	
	@Override
	public boolean isTaskAvailable(Object taskName) {
		if(taskName == null){
			throw new InvalidParameterException("taskName cannot be null");
		}
		Collection<ActivityInstance> activityInstances = processInstance.getActivityInstances();
		String task = mapper.formatTaskName(taskName.toString());
		for (ActivityInstance activityInstance : activityInstances) {
			String name = mapper.formatTaskName(activityInstance.getActivityDefinition().getName());
			if(name.equals(task)){
				return true;
			}
		}
		return false;
	}

	
	Map<String, Object> cachedContextObject = new HashMap<String, Object>();
	
	@SuppressWarnings("unchecked")
	@Override
	public <E> E getContextObject(Class<E> type) {
		E obj;
		if((obj = (E) cachedContextObject.get(type.getName())) == null){
			cachedContextObject.put(type.getName(), obj = mapper.getContextObject(type, getProcessInstance(), configuration));
		}
		return obj; 
	}



}

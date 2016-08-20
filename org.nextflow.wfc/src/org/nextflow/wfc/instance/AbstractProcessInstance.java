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
package org.nextflow.wfc.instance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.nextflow.wfc.WorkflowException;
import org.nextflow.wfc.model.ActivityDefinition;

/**
 * Helper class for ProcessInstance implementors
 * @author rogel
 *
 */
public abstract class AbstractProcessInstance implements ProcessInstance{

	@Override
	public Collection<ActivityInstance> getActivityInstancesByDefinitionId(String activityDefinitionId) {
		Collection<ActivityInstance> result = new ArrayList<ActivityInstance>();
		Collection<ActivityInstance> activityInstances = getActivityInstances();
		for (ActivityInstance activityInstance : activityInstances) {
			if(activityInstance.getActivityDefinition().getId().equals(activityDefinitionId)){
				result.add(activityInstance);
			}
		}
		return Collections.unmodifiableCollection(result);
	}
	

	@Override
	public Collection<ActivityInstance> getActivityInstancesByDefinitionName(String activityDefinitionName) {
		Collection<ActivityInstance> result = new ArrayList<ActivityInstance>();
		Collection<ActivityInstance> activityInstances = getActivityInstances();
		for (ActivityInstance activityInstance : activityInstances) {
			if(activityInstance.getActivityDefinition().getName().equals(activityDefinitionName)){
				result.add(activityInstance);
			}
		}
		return Collections.unmodifiableCollection(result);
	}

	@Override
	public ActivityInstance getActivityInstance(String activityInstanceId) {
		Collection<ActivityInstance> activityInstances = getActivityInstances();
		for (ActivityInstance activityInstance : activityInstances) {
			if(activityInstance.getId().equals(activityInstanceId)){
				return activityInstance;
			}
		}
		return null;
	}
	
	@Override
	public Collection<ActivityInstance> getActivityInstances(ActivityDefinition activityDefinition) {
		return getActivityInstancesByDefinitionId(activityDefinition.getId());
	}
	
	@Override
	public ActivityInstance getActivityInstance(ActivityDefinition activityDefinition) {
		return getActivityInstanceByDefinitionId(activityDefinition.getId());
	}
	
	@Override
	public ActivityInstance getActivityInstanceByDefinitionId(String activityDefinitionId) {
		Collection<ActivityInstance> activityInstances = getActivityInstancesByDefinitionId(activityDefinitionId);
		if(activityInstances.size() > 1){
			throw new WorkflowException("More that one activity instance found for "+activityDefinitionId+" in process "+this);
		}
		if(activityInstances.size() == 0){
			return null;
		}
		return activityInstances.iterator().next();
	}
	
	@Override
	public ActivityInstance getActivityInstanceByDefinitionName(String activityDefinitionName) {
		Collection<ActivityInstance> activityInstances = getActivityInstancesByDefinitionName(activityDefinitionName);
		if(activityInstances.size() > 1){
			throw new WorkflowException("More that one activity instance found for "+activityDefinitionName+" in process "+this);
		}
		if(activityInstances.size() == 0){
			return null;
		}
		return activityInstances.iterator().next();
	}
	
	@Override
	public Object getAttribute(String key) {
		return getAttributes().get(key);
	}
	
	@Override
	public void setAttribute(String key, Object value) {
		getAttributes().put(key, value);
	}
	
	@Override
	public ActivityDefinition getActivityDefinition(String activityDefinitionId) {
		return getProcessDefinition().getActivityDefinition(activityDefinitionId);
	}
}

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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nextflow.owm.Configuration;
import org.nextflow.owm.WorkflowMappingException;
import org.nextflow.owm.internal.proxy.ProxyWorkflowProcess;
import org.nextflow.owm.mapping.Callback;
import org.nextflow.owm.mapping.ContextObject;
import org.nextflow.owm.mapping.Process;
import org.nextflow.owm.mapping.WorkflowProcess;
import org.nextflow.wfc.instance.ActivityInstance;
import org.nextflow.wfc.instance.ActivityState;
import org.nextflow.wfc.instance.ProcessInstance;
import org.nextflow.wfc.model.ActivityDefinition;
import org.nextflow.wfc.model.ActivityType;
import org.nextflow.wfc.model.ProcessDefinition;

public class Mapper {
	
	public String formatTaskName(String name) {
		return name.toLowerCase().replace("_", "").replace(" ", "");
	}

	public Collection<ActivityInstance> getActivityForMethod(Method method, ProcessInstance processInstance) {
		ActivityDefinition activityDefinition = getDefinitionForMethod(method, processInstance.getProcessDefinition());
		Collection<ActivityInstance> activityInstances = processInstance.getActivityInstancesByDefinitionId(activityDefinition.getId());
		return activityInstances;
	}
	
	public ActivityDefinition getDefinitionForMethod(Method method, ProcessDefinition processDefinition) {
		return getDefinitionForMethod(method, processDefinition.getActivityDefinitions());
	}
	
	public ActivityDefinition getDefinitionForMethod(Method method, Collection<ActivityDefinition> activityDefinitions) {
		String activityName = formatTaskName(method.getName());
		return findActivity(activityDefinitions, activityName); 
	}

	private ActivityDefinition findActivity(Collection<ActivityDefinition> activityDefinitions, String activityName) {
		if(activityName.startsWith("abort")){
			//special handler
			activityName = activityName.substring("abort".length());
		}
		ActivityDefinition result = null;
		for (ActivityDefinition activityDefinition : activityDefinitions) {
			String taskName = formatTaskName(activityDefinition.getName());
			if(taskName.equals(activityName)){
				if(result != null){
					throw new WorkflowMappingException("More than one activity was found with name \""+activityName+"\" for process \""+activityDefinition.getProcessDefinition().getId()+"\". Workflow mappings does not allows duplicate activity names.");
				}
				result = activityDefinition;
			}
		}
		return result;
	}

	/**
	 * Checks if the class is an interface and has an annotation @Process
	 * @param <E>
	 * @param workflowInterfaceClass
	 */
	public <E> void checkWorkflowInterface(Class<E> workflowInterfaceClass) {
		if(!workflowInterfaceClass.isInterface()){
			throw new WorkflowMappingException("Only interfaces can be used as workflow instances. Found "+workflowInterfaceClass);
		}
		if(workflowInterfaceClass.getAnnotation(Process.class) == null){
			throw new WorkflowMappingException("Workflow interfaces must be annotated with @Process. Annotation not found in "+workflowInterfaceClass);
		}
	}

	public void checkDefinitionForMethod(Method method, ActivityDefinition activityDefinition) {
		if(activityDefinition.getType() != ActivityType.HUMAN_TASK){
			throw new WorkflowMappingException("The process interfaces can only have methods for humam tasks. Invalid method "+method.getName()+"() in "+method.getDeclaringClass());
		}
	}

	/**
	 * Check if this interface defines valid methods
	 * @param processDefinition
	 * @param workflowInterface
	 */
	public void checkInterfaceMethods(ProcessDefinition processDefinition, Class<?> workflowInterface) {
		Collection<ActivityDefinition> activityDefinitions = processDefinition.getActivityDefinitions();
		Method[] methods = workflowInterface.getMethods();
		for (Method method : methods) {
			ActivityDefinition activityDefinition = getDefinitionForMethod(method, activityDefinitions);
			if(activityDefinition == null){
				if(isContextGetter(method)){
					//context getter
					continue;
				}
				if(!method.getDeclaringClass().equals(WorkflowProcess.class)){
					throw new WorkflowMappingException("No activity definition found for method "+method.getName()+"() of "+method.getDeclaringClass()+" in process definition with id "+processDefinition.getId());
				}
			} else {
				if(!method.getDeclaringClass().equals(WorkflowProcess.class)){
					checkDefinitionForMethod(method, activityDefinition);
				}
			}
		}
	}

	public boolean isContextGetter(Method method) {
		return isGetter(method) && (method.isAnnotationPresent(ContextObject.class) || !method.getReturnType().getName().startsWith("java"));
	}

	private boolean isGetter(Method method) {
		return method.getName().startsWith("get") && method.getParameterTypes().length == 0;
	}

	public void checkCallbackClass(Class<?> callback) {
		if(callback.isInterface() ||  Modifier.isAbstract(callback.getModifiers())){
			throw new WorkflowMappingException("Callback class must be a concrete class. Error in "+callback);
		}
		if(callback.getAnnotation(Process.class) == null){
			throw new WorkflowMappingException("Callback classes must be annotaded with @Process. Annotation not found in "+callback);
		}
		
	}

	public List<CallbackMapping> getCallbackMappings(Class<?> callback, Configuration configuration) {
		List<CallbackMapping> result = new ArrayList<CallbackMapping>();
		ProcessDefinition processDefinition = getDefinitionForClass(callback, configuration);
		Method[] methods = callback.getMethods();
		for (Method method : methods) {
			if(method.getDeclaringClass().equals(Object.class)){
				continue;
			}
			CallbackMapping callbackMapping = getCallbackMapping(method, processDefinition);
			result.add(callbackMapping);
		}
		return result;
	}

	public CallbackMapping getCallbackMapping(Method method, ProcessDefinition definitionForClass) {
		Callback annotation = method.getAnnotation(Callback.class);
		if(annotation == null){
			return getCallbackMappingWithoutAnnotation(method, definitionForClass);
		} else {
			return getCallbackMappginWithAnnotation(method, definitionForClass, annotation);
		}
	}

	private CallbackMapping getCallbackMappginWithAnnotation(Method method, ProcessDefinition definitionForClass, Callback callback) {
		String activityName = callback.activity();
		if(activityName.startsWith("execute")){ //TODO parameterize
//			activityName = activityName.substring("execute".length());
		}
		CallbackMapping callbackMapping = new CallbackMapping();
		callbackMapping.setMethod(method);
		callbackMapping.setActivityState(callback.state());
		callbackMapping.setActivityDefinition(findDefinition(activityName, definitionForClass, method));
		return callbackMapping;
	}

	private CallbackMapping getCallbackMappingWithoutAnnotation(Method method, ProcessDefinition definitionForClass) {
		String activityName = method.getName();
		if(activityName.startsWith("execute")){ //TODO parameterize
			//activityName = activityName.substring("execute".length());
		}
		CallbackMapping callbackMapping = new CallbackMapping();
		callbackMapping.setMethod(method);
		callbackMapping.setActivityState(ActivityState.ACTIVE);
		callbackMapping.setActivityDefinition(findDefinition(activityName, definitionForClass, method));
		return callbackMapping;
	}

	private ActivityDefinition findDefinition(String activityName, ProcessDefinition processDefinition, Method method) {
		activityName = formatTaskName(activityName);
		ActivityDefinition activity = findActivity(processDefinition.getActivityDefinitions(), activityName);
		if(activity == null){
			throw new WorkflowMappingException("No activity definition found for method "+method.getName()+"() of "+method.getDeclaringClass()+" with name \""+activityName+"\" in process definition \""+processDefinition.getId()+"\".");
		}
		return activity;
	}

	String getDefinitionID(Class<?> workflowInterfaceClass) {
		return workflowInterfaceClass.getAnnotation(Process.class).value();
	}

	public <E> ProcessDefinition getDefinitionForClass(Class<E> workflowInterfaceClass, Configuration configuration) {
		String definitionID = getDefinitionID(workflowInterfaceClass);
		ProcessDefinition processDefinition = configuration.getSession().getProcessDefinition(definitionID);
		return processDefinition;
	}

	public Map<Class<?>, List<CallbackMapping>> getCallbackMapping(Configuration configuration, ProcessInstance processInstance) {
		Map<Class<?>, List<CallbackMapping>> result = new HashMap<Class<?>, List<CallbackMapping>>();
		Map<Class<?>, List<CallbackMapping>> callbackMappings = configuration.getCallbackMappings();
		Set<Class<?>> keySet = callbackMappings.keySet();
		for (Class<?> class1 : keySet) {
			String definitionID = getDefinitionID(class1);
			if(processInstance.getProcessDefinition().getId().equals(definitionID)){
				result.put(class1, callbackMappings.get(class1));
			}
		}
		return result;
	}

	public <E> E getContextObject(Class<E> type, ProcessInstance processInstance2, Configuration configuration) {
		if(type.isInterface() && type.isAnnotationPresent(Process.class)){
			return ProxyWorkflowProcess.createFor(type, processInstance2, configuration);
		}
		
//		try {
			E newInstance = org.nextflow.owm.internal.proxy.ProxyContextObject.createFor(type, processInstance2);
//			Map<String, Object> attributes = processInstance2.getAttributes();
//			PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(type).getPropertyDescriptors();
//			for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
//				if(propertyDescriptor.getName().equals("class")){
//					continue;
//				}
//				if(propertyDescriptor.getWriteMethod() == null){
//					throw new WorkflowMappingException(type+" does not have a setter for property "+propertyDescriptor.getName());
//				}
//				if(attributes.containsKey(propertyDescriptor.getName())){
//					try {
//						propertyDescriptor.getWriteMethod().invoke(newInstance, attributes.get(propertyDescriptor.getName()));
//					} catch (Exception e) {
//						throw new WorkflowMappingException("Cannot set value for property "+propertyDescriptor.getName(), e);
//					}
//				}
//			}
			return newInstance;
//		} catch (IntrospectionException e) {
//			throw new WorkflowMappingException(e);
//		}
	}

	public boolean isContextField(Field field, ProcessInstance processInstance2) {
		return (field.isAnnotationPresent(ContextObject.class) || !field.getType().getName().startsWith("java"));
	}

	public boolean isAbort(Method method) {
		return method.getName().startsWith("abort"); //TODO REFACTOR
	}

}

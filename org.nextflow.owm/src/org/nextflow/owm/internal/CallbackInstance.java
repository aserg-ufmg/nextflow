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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.nextflow.owm.CannotExecuteActivityException;
import org.nextflow.owm.Configuration;
import org.nextflow.owm.WorkflowMappingException;
import org.nextflow.owm.mapping.Callback;
import org.nextflow.wfc.instance.ActivityInstance;
import org.nextflow.wfc.instance.ProcessInstance;
import org.nextflow.wfc.model.ActivityDefinition;

public class CallbackInstance {

	private ProcessInstance processInstance;
	private Map<Object, List<CallbackMapping>> objectMapping;
	private Configuration configuration;
	
	LocalVariableTableParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

	public CallbackInstance(Configuration configuration, ProcessInstance processInstance) {
		this.configuration = configuration;
		this.processInstance = processInstance;
		this.objectMapping = createObjectMapping(configuration, processInstance);
	}

	private Map<Object, List<CallbackMapping>> createObjectMapping(Configuration configuration, ProcessInstance processInstance2) {
		Map<Object, List<CallbackMapping>> result = new HashMap<Object, List<CallbackMapping>>();
		Map<Class<?>, List<CallbackMapping>> callbackMapping = configuration.getMapper().getCallbackMapping(configuration, processInstance);
		for (Class<?> clazz : callbackMapping.keySet()) {
			try {
				Object callbackObject = clazz.newInstance();
				injectDependencies(callbackObject, processInstance2);
				result.put(callbackObject, callbackMapping.get(clazz));
			} catch (Exception e) {
				throw new WorkflowMappingException("Cannot instantiate callback class "+clazz, e);
			}
		}
		return result;
	}

	private void injectDependencies(Object callbackObject, ProcessInstance processInstance2) {
		Class<? extends Object> clazz = callbackObject.getClass();
		while(!clazz.equals(Object.class)){
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				if(configuration.getMapper().isContextField(field, processInstance2)){
					Object contextObject = configuration.getMapper().getContextObject(field.getType(), processInstance2, configuration);
					field.setAccessible(true);
					try {
						field.set(callbackObject, contextObject);
					} catch (Exception e) {
						throw new WorkflowMappingException("Cannot inject context object field ("+field.getName()+") in callback of "+callbackObject);
					}
				}
			}
			clazz = clazz.getSuperclass();
		}
	}

	public void invoke(ActivityInstance activityInstance) {
		Set<Entry<Object, List<CallbackMapping>>> entrySet = objectMapping.entrySet();
		for (Entry<Object, List<CallbackMapping>> entry : entrySet) {
			Object callback = entry.getKey();
			List<CallbackMapping> mappings = entry.getValue();
			for (CallbackMapping callbackMapping : mappings) {
				boolean stateMatches = callbackMapping.getActivityState().equals(activityInstance.getState());
				boolean definitionMatches = callbackMapping.getActivityDefinition().equals(activityInstance.getActivityDefinition());
				if(stateMatches && definitionMatches){
					try {
						Method method = callbackMapping.getMethod();
						Object[] values = getMethodParameters(activityInstance, method);
						//TODO THE METHOD RETURN SHOULD BE ASSOCIATED TO TASK RESULTS
						method.invoke(callback, values);
					} catch (InvocationTargetException e) {
						if(e.getTargetException() instanceof RuntimeException){
							throw (RuntimeException) e.getTargetException();
						} else {
							throw new CannotExecuteActivityException(e.getTargetException());
						}
					} catch (Exception e) {
						throw new WorkflowMappingException("Cannot execute callback "+callbackMapping.getMethod(), e);
					}
				}
			}
		}
	}

	private Object[] getMethodParameters(ActivityInstance activityInstance, Method method) {
		String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
		Object[] values = new Object[parameterNames.length];
		for (int i = 0; i < parameterNames.length; i++) {
			String parameter = parameterNames[i];
			values[i] = getParameterValue(activityInstance, parameter);
		}
		return values;
	}

	private Object getParameterValue(ActivityInstance activityInstance, String parameter) {
		//TODO CREATE A FIND VALUE FOR ACTIVITY INSTANCE
		if(activityInstance.getWorkItem() != null){
			return activityInstance.getWorkItem().getParameter(parameter);
		}
		return activityInstance.getProcessInstace().getAttribute(parameter);
	}

	public boolean executePath(ActivityInstance from, ActivityDefinition target) {
		//TODO CACHE THE RESULT
		boolean foundPath = false;
		Set<Entry<Object, List<CallbackMapping>>> entrySet = objectMapping.entrySet();
		for (Entry<Object, List<CallbackMapping>> entry : entrySet) {
			Object callback = entry.getKey();
			List<CallbackMapping> mappings = entry.getValue();
			for (CallbackMapping callbackMapping : mappings) {
				boolean stateMatches = callbackMapping.getActivityState().equals(from.getState());
				boolean definitionMatches = callbackMapping.getActivityDefinition().equals(from.getActivityDefinition());
				if(stateMatches && definitionMatches){
					Class<?> returnType = callbackMapping.getMethod().getReturnType();
					if(!(returnType.equals(String.class) || returnType.isEnum())){
						throw new WorkflowMappingException("A "+Callback.SplitXOR.class.getSimpleName()+" method must return a String or Enum. Error in "+callbackMapping.getMethod());
					}
					try {
						foundPath = true;
						
						Object result = callbackMapping.getMethod().invoke(callback);
						if(result == null){
							throw new WorkflowMappingException("A "+Callback.SplitXOR.class.getSimpleName()+" method returned null. "+callbackMapping.getMethod());
						}
						String pathresult = result.toString();
						pathresult = configuration.getMapper().formatTaskName(pathresult);
						String targetName = configuration.getMapper().formatTaskName(target.getName());
						if(pathresult.equals(targetName)){
							return true;
						}
					} catch (InvocationTargetException e) {
						throw new WorkflowMappingException("Exception in execution of callback "+callbackMapping.getMethod(), e.getTargetException());
					} catch (Exception e) {
						throw new WorkflowMappingException("Cannot execute callback "+callbackMapping.getMethod(), e);
					}
				}
			}
		}
		if(!foundPath){
			System.err.println("Warning: No callback methods found for SPLIT activity "+from.getActivityDefinition());
		}
		return false;
	}

}



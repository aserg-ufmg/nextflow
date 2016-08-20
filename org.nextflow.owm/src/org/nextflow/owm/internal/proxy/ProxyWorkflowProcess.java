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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;

import org.nextflow.owm.CannotExecuteActivityException;
import org.nextflow.owm.Configuration;
import org.nextflow.owm.internal.LocalVariableTableParameterNameDiscoverer;
import org.nextflow.owm.mapping.WorkflowProcess;
import org.nextflow.wfc.instance.ActivityInstance;
import org.nextflow.wfc.instance.ProcessInstance;
import org.nextflow.wfc.model.ProcessDefinition;

public class ProxyWorkflowProcess extends BaseWorkflowProcess implements InvocationHandler {
	
	LocalVariableTableParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

	public ProxyWorkflowProcess(Configuration configuration, ProcessDefinition definition) {
		super(configuration, definition);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if(method.getDeclaringClass().equals(WorkflowProcess.class) || method.getDeclaringClass().equals(Object.class)){
			return method.invoke(this, args);
		}
		if(mapper.isContextGetter(method)){
			return getContextObject(method.getReturnType());
		}
		boolean isAbort = mapper.isAbort(method);
		Collection<ActivityInstance> activities = mapper.getActivityForMethod(method, getProcessInstance());
		if(activities == null || activities.size() == 0){
			throw new CannotExecuteActivityException("Cannot execute activity \""+getActivityName(method)+"\". No activity instances found in process instance. ");
		} else {
			
			//TODO if there are multiple activity instances it is a loop, how to handle it?
			if(activities.size() > 1){
				System.err.println("Loop detected. All instances of the activity will be executed. Activity: "+getActivityName(method));
			}
			for (ActivityInstance activityInstance : activities) {
				if(activityInstance.getWorkItem() != null){
					if(isAbort){
						activityInstance.getWorkItem().abort();
						continue;
					}
					//System.out.println(method.getDeclaringClass());
					String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
					for (int i = 0; i < parameterNames.length; i++) {
						activityInstance.getWorkItem().setParameter(parameterNames[i], args[i]);
					}
					activityInstance.getWorkItem().complete();
				} else {
					if(isAbort){
						activityInstance.cancel();
						continue;
					}
					activityInstance.complete();
				}
			}
		}
		//TODO return the results from the execution of the task
		return null;
	}

	private String getActivityName(Method method) {
		return (mapper.getDefinitionForMethod(method, getProcessInstance().getProcessDefinition()).getName());
	}

	@SuppressWarnings("unchecked")
	public static <E> E createFor(Class<E> workflowInterface, ProcessDefinition definition, Configuration configuration){
		Object proxyInstance = Proxy.newProxyInstance(workflowInterface.getClassLoader(), 
				new Class[]{workflowInterface, WorkflowProcess.class}, 
				new ProxyWorkflowProcess(configuration, definition));
		return (E) proxyInstance;
	}
	
	@SuppressWarnings("unchecked")
	public static <E> E createFor(Class<E> workflowInterface, ProcessInstance instance, Configuration configuration){
		ProxyWorkflowProcess h = new ProxyWorkflowProcess(configuration, instance.getProcessDefinition());
		h.setProcessInstance(instance);
		Object proxyInstance = Proxy.newProxyInstance(workflowInterface.getClassLoader(), 
				new Class[]{workflowInterface, WorkflowProcess.class}, 
				h);
		return (E) proxyInstance;
	}

}

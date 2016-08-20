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

import java.lang.reflect.Method;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.nextflow.owm.WorkflowMappingException;
import org.nextflow.owm.mapping.WorkflowProcessCallback;
import org.nextflow.wfc.instance.ProcessInstance;

public class ProxyContextObject extends BaseContextObject implements MethodInterceptor {

	public ProxyContextObject(ProcessInstance instance) {
		super(instance);
	}
	
	private void setDefaultValues(Object object) {
		//read all getters for default values
		try {
			object = object.getClass().getSuperclass().newInstance();
		} catch (Exception e1) {
			System.err.println("Cannot set default values for context object "+object);
		}
		setValuesOfObjectForProcessInstance(object, processInstance);
	}

	public static void setValuesOfObjectForProcessInstance(Object object, ProcessInstance processInstance) {
		Method[] methods = object.getClass().getMethods();
		for (Method method : methods) {
			if(isGetter(method)){
				String attributeName = uncapitalize(method.getName().startsWith("is")? method.getName().substring(2) : method.getName().substring(3));
				if(attributeName.equals("class")){
					continue;
				}
				try {
					Object value = method.invoke(object);
					if(value != null && processInstance.getAttribute(attributeName) == null){
						//System.out.println("Setting default value of "+attributeName);
						processInstance.setAttribute(attributeName, value);
					}
				} catch (Exception e) {
					System.err.println("Error setting default value for process instance attribute "+attributeName);
				}
			}
		}
	}

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		if(method.getDeclaringClass().equals(WorkflowProcessCallback.class)){
			return method.invoke(this, args);
		}
		if(method.getName().length() > 3){
			String attributeName = method.getName().startsWith("is")? method.getName().substring(2) : method.getName().substring(3);
			attributeName = uncapitalize(attributeName);
			if(isSetter(method)){
				processInstance.setAttribute(attributeName, args[0]);
				return null;
			} else if(isGetter(method)){
				Object attribute = processInstance.getAttribute(attributeName);
				return attribute;
			}
		}
		return proxy.invokeSuper(obj, args);
	}

	private static String uncapitalize(String attributeName) {
		return Character.toLowerCase(attributeName.charAt(0)) + attributeName.substring(1);
	}

	private static boolean isGetter(Method method) {
		return (method.getName().startsWith("is") || method.getName().startsWith("get")) && method.getParameterTypes().length == 0;
	}

	private boolean isSetter(Method method) {
		return (method.getName().startsWith("set")) && method.getParameterTypes().length == 1;
	}

	@SuppressWarnings("unchecked")
	public static <E> E createFor(Class<E> clazz, ProcessInstance instance) {
		try {
			org.nextflow.owm.internal.proxy.ProxyContextObject contextObject = new org.nextflow.owm.internal.proxy.ProxyContextObject(instance);
			Enhancer e = new Enhancer();
			e.setInterfaces(new Class[]{WorkflowProcessCallback.class});
			e.setSuperclass(clazz);
			e.setCallback(contextObject);
			E bean = (E) e.create();
			contextObject.setDefaultValues(bean);
			return bean;
		} catch (Exception e) {
			throw new WorkflowMappingException("cannot instantiate "+clazz, e);
		}
	}

	


}

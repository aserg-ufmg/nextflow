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
package org.nextflow.owm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nextflow.owm.internal.CallbackMapping;
import org.nextflow.owm.internal.Mapper;
import org.nextflow.wfc.Session;
import org.nextflow.wfc.WorkflowManager;

public class Configuration {
	
	List<Class<?>> callbackClasses = new ArrayList<Class<?>>();
	
	Map<Class<?>, List<CallbackMapping>> callbackMappings = new HashMap<Class<?>, List<CallbackMapping>>();

	Mapper mapper = new Mapper();
	
	Session session;
	
	public Configuration(String url) {
		this.session = WorkflowManager.connect(url);
	}
	
	public Configuration(Session session) {
		this.session = session;
	}
	
	public Session getSession() {
		return session;
	}
	
	public Mapper getMapper() {
		return mapper;
	}

	public void addCallbackClass(Class<?> callback) {
		mapper.checkCallbackClass(callback);
		callbackClasses.add(callback);
		callbackMappings.put(callback, mapper.getCallbackMappings(callback, this));
	}
	
	public List<Class<?>> getCallbackClasses() {
		return callbackClasses;
	}
	
	public Map<Class<?>, List<CallbackMapping>> getCallbackMappings() {
		return callbackMappings;
	}
	
	public WorkflowObjectFactory createFactory(){
		return WorkflowMapping.createFactory(this);
	}
}

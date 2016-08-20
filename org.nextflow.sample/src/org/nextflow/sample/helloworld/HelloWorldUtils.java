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
package org.nextflow.sample.helloworld;

import org.nextflow.owm.Configuration;
import org.nextflow.owm.WorkflowObjectFactory;
import org.nextflow.sample.helloworld.automatic.HelloWorldAutomaticCallback;
import org.nextflow.sample.helloworld.base.HelloWorldCallback;
import org.nextflow.sample.helloworld.data.HelloWorldDataCallback;

public class HelloWorldUtils {
	
	private static WorkflowObjectFactory factory;
	private static String url = "jwfc:jbpm:helloWorld.bpmn, helloWorldAutomatic.bpmn, helloWorldData.bpmn";

	public static WorkflowObjectFactory getFactory() {
		return getFactory(url, HelloWorldCallback.class, HelloWorldAutomaticCallback.class, HelloWorldDataCallback.class);
	}
	
	public static WorkflowObjectFactory getFactory(String url, Class<?>...callbacks) {
		if(factory == null){
			logInfo(url, callbacks);
			factory = createFactory(url, callbacks);
			System.err.println("Factory initialized!");
		}
		return factory;
	}

	private static WorkflowObjectFactory createFactory(String url, Class<?>... callbacks) {
		Configuration configuration = new Configuration(url);
		for (Class<?> class1 : callbacks) {
			configuration.addCallbackClass(class1);
		}
		return configuration.createFactory();
	}

	private static void logInfo(String url, Class<?>... callbacks) {
		System.err.println("Initializing Workflow Object Factory...");
		System.err.println("  + URL: "+url);
		for (Class<?> class1 : callbacks) {
			System.err.println("  + Callback: "+class1.getName());
		}
	}
}

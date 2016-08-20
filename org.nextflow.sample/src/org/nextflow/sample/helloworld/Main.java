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

import org.nextflow.owm.WorkflowObjectFactory;
import org.nextflow.sample.helloworld.automatic.HelloWorldAutomatic;
import org.nextflow.sample.helloworld.base.HelloWorld;
import org.nextflow.sample.helloworld.data.HelloWorldData;

public class Main {

	public static void main(String[] args) throws InterruptedException {
		WorkflowObjectFactory factory = HelloWorldUtils.getFactory();
		delay();
		
		System.out.println("\n** EXECUTING HELLO WORLD PROCESS");
		delay();
		HelloWorld helloWorld = factory.start(HelloWorld.class);
		helloWorld.helloTask();
		
		delay();
		
		System.out.println("\n** EXECUTING HELLO WORLD AUTOMATIC PROCESS");
		delay();
		factory.start(HelloWorldAutomatic.class);
		
		delay();
		System.out.println("\n** EXECUTING HELLO WORLD DATA PROCESS");
		delay();
		HelloWorldData helloWorldData = factory.start(HelloWorldData.class);
		helloWorldData.taskWithParameters("This is the data parameter");
		System.out.println("Resulting data from process: "+helloWorldData.getData().getProcessData());
	}

	static void delay() throws InterruptedException {
		Thread.sleep(1000);
	}
}

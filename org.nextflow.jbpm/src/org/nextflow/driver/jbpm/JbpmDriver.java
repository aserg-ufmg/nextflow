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
package org.nextflow.driver.jbpm;

import java.util.Properties;

import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.nextflow.driver.jbpm.toolagent.BPMN2ProcessProviderWapiCompliant;
import org.nextflow.wfc.Session;
import org.nextflow.wfc.WorkflowManager;
import org.nextflow.wfc.driver.WorkflowManagerDriver;

public class JbpmDriver implements WorkflowManagerDriver {
	
	static {
		WorkflowManager.registerDriver(new JbpmDriver());
		
		//this will install the Interface 3 modules to the jBPM engine
		//TODO it will affect all instances not only the created by the driver (refactor)
		BPMN2ProcessProviderWapiCompliant.install();
	}

	@Override
	public String getProtocol() {
		return "jbpm";
	}

	@Override
	public Session connect(String uri, String username, String password, Properties properties) {
		String[] paths = uri.split(",");
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		for (String path : paths) {
			kbuilder.add(ResourceFactory.newClassPathResource(path.trim()), ResourceType.BPMN2);
		}
		return newSession(kbuilder, properties);
	}

	public static JbpmSession newSession(KnowledgeBuilder kbuilder, Properties properties) {
		KnowledgeBase newKnowledgeBase = kbuilder.newKnowledgeBase();
		return new JbpmSession(newKnowledgeBase, properties);
	}

}

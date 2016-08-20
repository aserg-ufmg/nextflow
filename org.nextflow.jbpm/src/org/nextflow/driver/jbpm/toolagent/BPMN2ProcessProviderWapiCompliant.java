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
package org.nextflow.driver.jbpm.toolagent;

import org.drools.compiler.BPMN2ProcessFactory;
import org.drools.compiler.BPMN2ProcessProvider;
import org.drools.compiler.PackageBuilder;
import org.drools.compiler.PackageBuilderConfiguration;
import org.jbpm.bpmn2.xml.BPMNDISemanticModule;
import org.jbpm.bpmn2.xml.BPMNExtensionsSemanticModule;
import org.jbpm.bpmn2.xml.BPMNSemanticModule;
import org.jbpm.bpmn2.xpath.XPATHProcessDialect;
import org.jbpm.process.builder.dialect.ProcessDialectRegistry;

public class BPMN2ProcessProviderWapiCompliant implements BPMN2ProcessProvider {
	
	static {
		//copied from BPMN2ProcessProviderImpl
        ProcessDialectRegistry.setDialect("XPath", new XPATHProcessDialect());
	}
	
	public static void install(){
		BPMN2ProcessFactory.setBPMN2ProcessProvider(new BPMN2ProcessProviderWapiCompliant());
	}

	@Override
	public void configurePackageBuilder(PackageBuilder packageBuilder) {
		//copied from BPMN2ProcessProviderImpl
		
        PackageBuilderConfiguration conf = packageBuilder.getPackageBuilderConfiguration();
        if (conf.getSemanticModules().getSemanticModule(BPMNSemanticModule.BPMN2_URI) == null) {
        	//overwrite default BPMN semantic module to provide WAPI Interface 3 callback
        	conf.addSemanticModule(new BPMNSemanticModuleWapiCompliant());
        	conf.addSemanticModule(new BPMNDISemanticModule());
        	conf.addSemanticModule(new BPMNExtensionsSemanticModule());
        }
	}
}

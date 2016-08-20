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

import org.nextflow.owm.Configuration;
import org.nextflow.wfc.ApplicationToolAgent;
import org.nextflow.wfc.instance.ActivityInstance;
import org.nextflow.wfc.instance.ProcessInstance;
import org.nextflow.wfc.instance.WorkItem;
import org.nextflow.wfc.model.ActivityDefinition;

public class CallbackToolAgent implements ApplicationToolAgent {
	
	private static final String WOM_INTERNAL_TOOL_AGENT = "wom.internal.tool.agent";
	private Configuration configuration;

	public CallbackToolAgent(Configuration configuration) {
		this.configuration = configuration;
	}

	@Override
	public void executeActivity(ProcessInstance processInstance, ActivityInstance activityInstance, WorkItem workItem) {
		CallbackInstance callback = installCallbacks(processInstance);
		callback.invoke(activityInstance);
	}

	@Override
	public boolean executePath(ProcessInstance processInstance, ActivityInstance from, ActivityDefinition target) {
		CallbackInstance callback = installCallbacks(processInstance);
		return callback.executePath(from, target);
	}

	private CallbackInstance installCallbacks(ProcessInstance processInstance) {
		CallbackInstance ci = (CallbackInstance) processInstance.getAttribute(WOM_INTERNAL_TOOL_AGENT);
		if(ci == null){
			ci = new CallbackInstance(configuration, processInstance);
			processInstance.setAttribute(WOM_INTERNAL_TOOL_AGENT, ci);
		}
		return ci;
	}

	@Override
	public String getName() {
		return "Workflow Mapping Tool Agent";
	}

}

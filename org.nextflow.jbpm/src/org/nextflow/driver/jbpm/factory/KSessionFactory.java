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
package org.nextflow.driver.jbpm.factory;

import java.util.List;

import org.drools.KnowledgeBase;
import org.drools.process.core.Work;
import org.drools.runtime.StatefulKnowledgeSession;
import org.nextflow.driver.jbpm.JbpmSession;
import org.nextflow.driver.jbpm.toolagent.ToolAgentWorkItemHandler;

public class KSessionFactory implements IKSessionFactory {

	public StatefulKnowledgeSession buildKSession(JbpmSession jbpmSession, KnowledgeBase kBase) {
		final StatefulKnowledgeSession kSession = kBase.newStatefulKnowledgeSession();
		
		//all human tasks will be configured for delayed execution (inversion of control)
		//the WAPI sets methods for changing the state of a work item 
		//(work item is an activity instance with a user assigned)
		List<Work> works = jbpmSession.getWorks();
		for (Work work : works) {
			kSession.getWorkItemManager().registerWorkItemHandler(work.getName(), new ToolAgentWorkItemHandler(jbpmSession));
		}
		
		return kSession;
	}
}

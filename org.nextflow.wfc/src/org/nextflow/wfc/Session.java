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
package org.nextflow.wfc;

import java.util.Collection;

import org.nextflow.wfc.instance.ProcessInstance;
import org.nextflow.wfc.model.ProcessDefinition;


/**
 * Represents a session to the workflow manager engine.<BR>
 * 
 * A session is obtained with WorkflowManager.connect(..)<BR>
 * 
 * 
 * @wapi 
 *  WMTPSessionHandle<BR>
 *  Server (section 8.2.3)
 * @see WorkflowManager.connect
 * @author rogel
 */
public interface Session {

	/**
	 * Returns the process definitions defined in this session.
	 * 
	 * @wapi WMOpenProcessDefinitionsList
	 * @return
	 */
	Collection<ProcessDefinition> getProcessDefinitions();
	
	/**
	 * Returns the process definition associated with the id.
	 * 
	 * @wapi WMOpenProcessDefinitionsList
	 * @return
	 */
	ProcessDefinition getProcessDefinition(String id);
	
	/**
	 * @wapi
	 * @return
	 */
	Collection<ProcessInstance> getProcessInstances();
	
	/**
	 * @wapi
	 * @return
	 */
	Collection<ProcessInstance> getProcessInstancesByDefinitionId(String processDefinitionId);
	
	/**
	 * @wapi
	 * @param processInstanceID the process instance id
	 * @return
	 */
	ProcessInstance getProcessInstance(String processInstanceID);
	
	/**
	 * @wapi WMCreateProcessInstance
	 */
	ProcessInstance createProcessInstance(ProcessDefinition definition);
	
	/**
	 * @wapi WMCreateProcessInstance
	 */
	ProcessInstance createProcessInstance(String definitionId);
	
	/**
	 * @wapi WMDisconnect
	 */
	void close();
	
	/**
	 * @wapi Interface 3
	 * @param agent
	 */
	void registerAgent(ApplicationToolAgent agent);
	
	/**
	 * @wapi Interface 3
	 * @return The registered Agents with this session
	 */
	Collection<ApplicationToolAgent> getAgents();
}

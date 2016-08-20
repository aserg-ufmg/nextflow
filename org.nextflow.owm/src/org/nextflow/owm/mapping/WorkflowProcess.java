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
package org.nextflow.owm.mapping;

import java.util.List;

import org.nextflow.wfc.instance.ProcessInstance;

public interface WorkflowProcess {
	
	/**
	 * Returns this process instance id
	 * @return
	 */
	String getId();
	
	/**
	 * Returns the internal WAPI Process Instance this object represents
	 * @return
	 */
	ProcessInstance getProcessInstance();

	/**
	 * Starts this process
	 */
	void start();
	
	/**
	 * Verify if there is a task with the specified name available for execution.<BR>
	 * The parameter can be of type String or an Enum.<BR>
	 * The String or Enum will be compared to the task name.
	 * @param taskName
	 * @return true if there is an activity instance with the specified name
	 */
	boolean isTaskAvailable(Object taskName);
	
	/**
	 * Returns a list of the available tasks names identifiers for this process.
	 * The names of tasks will be in contracted form (lowercase, without spaces and underscores)
	 * @return
	 */
	List<String> getAvailableTasksNameIds();
	
	/**
	 * Returns a list of the available tasks names as appears in the definition.
	 * @return
	 */
	List<String> getAvailableTasks();
	
	/**
	 * Retuns a list of the available tasks for this process with the Enum type
	 * @return
	 */
	<E extends Enum<E>> List<E> getAvailableTasks(Class<E> e);
	
	/**
	 * Instantiate this object and populates with process attributes.<BR>
	 * Each attribute of the parameter class must have a setter.
	 * @param <E>
	 * @param type
	 * @return
	 */
	<E> E getContextObject(Class<E> type);
}

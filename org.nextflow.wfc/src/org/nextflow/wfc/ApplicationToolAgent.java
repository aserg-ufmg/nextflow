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

import org.nextflow.wfc.instance.ActivityInstance;
import org.nextflow.wfc.instance.ProcessInstance;
import org.nextflow.wfc.instance.WorkItem;
import org.nextflow.wfc.model.ActivityDefinition;

/**
 * This interface is to be implemented by applications that need to provide application functionality to workflows.<BR>
 * Objects of this interface should be registered in the Session before any call to workflow process instances.<BR>
 * 
 * @wapi
 *   This interface has the Tool Agent methods as defined in the WAPI Interface 3.
 * @author rogel
 */
public interface ApplicationToolAgent {

	String getName();

	/**
	 * @wapi
	 *  WMTAInvokeApplication (section 6.8.2) (name was changed to facilitate implementors undestanding)
	 *  This method signature is changed from the defined in WMTAInvokeApplication.<BR>
	 *  The parameter ActivityInstance was introduced as there is the possibility of having a activity instance without work item (non human tasks).<BR>
	 *  The WorkItem parameter is actually the same as activityInstance.getWorkItem().
	 */
	void executeActivity(ProcessInstance processInstance, ActivityInstance activityInstance, WorkItem workItem);

	/**
	 * @wapi
	 *  This method is not defined in wapi, but is required if the application should choose a path in the workflow<BR>
	 *  Used in XOR or OR splits
	 * @param processInstance
	 * @param from
	 * @param target 
	 * @return
	 */
	boolean executePath(ProcessInstance processInstance, ActivityInstance from, ActivityDefinition target);
}

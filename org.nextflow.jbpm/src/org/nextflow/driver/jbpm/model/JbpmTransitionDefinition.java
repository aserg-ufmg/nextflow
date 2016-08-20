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
package org.nextflow.driver.jbpm.model;

import org.drools.definition.process.Connection;
import org.nextflow.wfc.model.ActivityDefinition;
import org.nextflow.wfc.model.ProcessDefinition;
import org.nextflow.wfc.model.TransitionDefinition;

public class JbpmTransitionDefinition implements TransitionDefinition {

	private Connection connection;
	private JbpmProcessDefinition definition;
	private JbpmActivityDefinition from;
	private JbpmActivityDefinition to;

	public JbpmTransitionDefinition(JbpmProcessDefinition definition, Connection connection) {
		this.connection = connection;
		this.definition = definition;
		this.from = definition.createActivityDefinition(connection.getFrom());
		this.to = definition.createActivityDefinition(connection.getTo());
	}
	
	@Override
	public ActivityDefinition getFrom() {
		return from;
	}
	
	@Override
	public ActivityDefinition getTo() {
		return to;
	}

	@Override
	public String toString() {
		return getFrom().getId()+" -> "+getTo().getId();
	}

	@Override
	public ProcessDefinition getProcessDefinition() {
		return definition;
	}
	
	/**
	 * jBPM
	 * @return
	 */
	public Connection getConnection() {
		return connection;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((definition == null) ? 0 : definition.hashCode());
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JbpmTransitionDefinition other = (JbpmTransitionDefinition) obj;
		if (definition == null) {
			if (other.definition != null)
				return false;
		} else if (!definition.equals(other.definition))
			return false;
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		if (to == null) {
			if (other.to != null)
				return false;
		} else if (!to.equals(other.to))
			return false;
		return true;
	}
	
	
}

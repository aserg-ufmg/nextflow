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

public class JbpmSessionPropertiesUtil {

	public static final String USE_CONSOLE_LOGGER = "jbpm.log.consolelogger";

	public static boolean isUseConsoleLogger(Properties properties) {
		return isTrue(getProperty(properties, USE_CONSOLE_LOGGER));
	}

	private static boolean isTrue(Object value) {
		return value != null && (Boolean.TRUE.equals(value) || value.toString().equalsIgnoreCase("true") || value.equals("1") || value.toString().equalsIgnoreCase("yes"));
	}

	private static Object getProperty(Properties properties, String prop) {
		return properties != null? properties.get(prop) : null;
	}
	
	
}

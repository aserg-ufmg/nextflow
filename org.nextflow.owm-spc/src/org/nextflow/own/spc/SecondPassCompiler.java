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
package org.nextflow.own.spc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.annotation.processing.AbstractProcessor;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;


public class SecondPassCompiler {

	public static void main(String[] args) throws FileNotFoundException {
		try {
			System.out.println("Running...");
			String project = args[0];
			String fileName = args[1];
			String output = args[2];
			if (fileName.contains("ParameterNames")) {
				return;
			}
			System.out.println("Executing second pass compile for project" + project);
			File file = new File(fileName);
			//		System.out.println(file.exists());
			//		System.out.println(output);
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String linha = "";
			boolean containsProcess = false; 
			boolean containsInterface = false; 
			while((linha = reader.readLine()) != null){
				if(linha.contains("public class")){
					break;
				}
				if(linha.contains("interface")){
					containsInterface = true;
				}
				if(linha.contains("@Process")){
					containsProcess = true;
				}
			}
			if(!containsProcess || !containsInterface){
				return;
			}
			
			
			Map<String, List<String>> methodParameters = getParameterNames(file, null, true);
			String parameterNamesPath = file.getAbsolutePath();
			parameterNamesPath = parameterNamesPath.substring(0, parameterNamesPath.length() - 5);
			parameterNamesPath = parameterNamesPath + "ParameterNames.java";
			String className = parameterNamesPath.substring(project.length() + 1, parameterNamesPath.length() - 5).replace('/', '.').replace('\\', '.');
			System.out.println(parameterNamesPath);
			PrintWriter out = new PrintWriter(new FileOutputStream(parameterNamesPath));
			out.println("package " + className.substring(0, className.lastIndexOf('.')) + ";");
			out.println();
			out.println("import java.lang.reflect.Method;");
			out.println("import org.nextflow.owm.mapping.ParameterNamesProvider;");
			out.println();
			out.println("/* Auto Generated */");
			out.println("public class " + className.substring(className.lastIndexOf('.') + 1) + " implements ParameterNamesProvider {");
			out.println();
			out.println("    @Override");
			out.println("    public String[] getParameterNamesFor(Method method) {");
			Set<Entry<String, List<String>>> entrySet = methodParameters.entrySet();
			for (Entry<String, List<String>> entry : entrySet) {
				String methodName = entry.getKey();
				out.println("        if(method.getName().equals(\"" + methodName + "\")){");
				out.println("            return new String[]{" + convertToStringArray(entry.getValue()) + "};");
				out.println("        }");
			}
			out.println("        return null;");
			out.println("    }");
			out.println("}");
			out.flush();
			System.out.println(methodParameters);
		} catch (Throwable e) {
		}
		
		
//		System.out.println(new String(outSt.toByteArray()));
	}

	private static String convertToStringArray(List<String> value) {
		StringBuilder res = new StringBuilder();
		for (Iterator iterator = value.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			res.append("\""+string+"\"");
			if(iterator.hasNext()){
				res.append(", ");
			}
		}
		return res.toString();
	}

	private static Map<String, List<String>> getParameterNames(File file, String output, boolean useProcessor) {
		//Get an instance of java compiler
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

		//Get a new instance of the standard file manager implementation
		StandardJavaFileManager fileManager = compiler.
		        getStandardFileManager(null, null, null);
		        
		// Get the list of java file objects, in this case we have only 
		// one file, TestClass.java
		Iterable<? extends JavaFileObject> compilationUnits1 = 
		        fileManager.getJavaFileObjectsFromFiles(Arrays.asList(file));
		
		// Create the compilation task
		CompilationTask task = compiler.getTask(null, fileManager, null,
		                                        output!= null? Arrays.asList("-d", output): null, null, compilationUnits1);
		
		// Create a list to hold annotation processors
		LinkedList<AbstractProcessor> processors = new LinkedList<AbstractProcessor>();

		// Add an annotation processor to the list
		CodeAnalyzerProcessor processor = new CodeAnalyzerProcessor();
		if(useProcessor){
			processors.add(processor);
			// Set the annotation processor to the compiler task
			task.setProcessors(processors);
		}
		                                        
		// Perform the compilation task.
		task.call();
		Map<String, List<String>> methodParameters = processor.visitor.methodParameters;
		return methodParameters;
	}
}

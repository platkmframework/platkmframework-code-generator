/*******************************************************************************
 * Copyright(c) 2023 the original author Eduardo Iglesias Taylor.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	 https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 * 	Eduardo Iglesias Taylor - initial API and implementation
 *******************************************************************************/
package org.platkmframework.codegenerator.core;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
 
  
public final class TemplateProcessorEngine {
	 
	VelocityEngine ve = null;
	   
	public TemplateProcessorEngine() {}
         
	 public  void init(Properties p){ 
		 ve = new VelocityEngine(); 
	     ve.init(p); 
	}
     
    /**
     * 
     * @param templateFileName
     * @param contextMap
     * @return
     * @throws GenerationException
     * @throws VelocityFileGenerationInitException 
     */
	public String generate(String templateFileName, Map<String, Object> contextMap){
	   Template t  =  ve.getTemplate( templateFileName ); 
	   VelocityContext context = new VelocityContext();
	
	   if(contextMap!=null && contextMap.size()>0){
	       Iterator<String>  contextMapIter = contextMap.keySet().iterator();
	       while (contextMapIter.hasNext()) {
	           String key = (String) contextMapIter.next();
	           context.put(key, contextMap.get(key));
	       }
	   }
	 
	   StringWriter writer = new StringWriter();
	   t.merge( context, writer ); 
	    
	   return writer.toString(); 
   
	}	

}

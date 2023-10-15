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
package org.platkmframework.codegenerator.core.processor;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.platkmframework.codegenerator.core.Templates;

public abstract class GeneratorProcessorBase {

    protected static Map<String, Templates> defaultTemplateMap;

    protected Connection con = null; 
    Function<String, String> notifier;
    Function<String, String> confirm;
      
    public GeneratorProcessorBase() { 
    }
 
    protected List<String> getExcludedTables(String excludedTables) {
        if(StringUtils.isBlank(excludedTables)) return new ArrayList<>();
        return  Arrays.asList(excludedTables.split(","));
    } 

    public static synchronized Map<String, Templates> getDefaultTemplateMap() {
        if(defaultTemplateMap == null){
            initDefaultTemplate();
        }
        return defaultTemplateMap;
    }  
    
    
    private static void initDefaultTemplate() {
        defaultTemplateMap = new HashMap<>();
		
        Templates templates = new Templates();
        templates.setKey("entity");
        templates.setName("Entity");
        templates.setPostfix(".java"); 
        templates.setRewrite(Boolean.TRUE);
        templates.setActive(Boolean.TRUE);
        templates.setTemplatePath("/templates/backend/domain/entity/entity.vm");
        defaultTemplateMap.put("entity", templates);

        templates = new Templates();
        templates.setKey("dao");
        templates.setName("DAO");
        templates.setPostfix("DAO.java"); 
        templates.setRewrite(Boolean.TRUE);
        templates.setActive(Boolean.TRUE);
        templates.setTemplatePath("/templates/backend/domain/dao/dao.vm");
        defaultTemplateMap.put("dao", templates);

        templates = new Templates();
        templates.setKey("service");
        templates.setName("Service");
        templates.setPostfix("ServiceImpl.java"); 
        templates.setRewrite(Boolean.TRUE);
        templates.setActive(Boolean.TRUE);
        templates.setTemplatePath("/templates/backend/domain/service/service.vm");
        defaultTemplateMap.put("service", templates);	

        templates = new Templates();
        templates.setKey("serviceInterface");
        templates.setName("Service Interface");
        templates.setPostfix("Service.java"); 
        templates.setRewrite(Boolean.TRUE);
        templates.setActive(Boolean.TRUE);
        templates.setTemplatePath("/templates/backend/domain/service/serviceInterface.vm");
        defaultTemplateMap.put("serviceInterface", templates);	

        templates = new Templates();
        templates.setKey("vo");
        templates.setName("Value Object");
        templates.setPostfix("VO.java"); 
        templates.setRewrite(Boolean.TRUE);
        templates.setActive(Boolean.TRUE);
        templates.setTemplatePath("/templates/backend/vo/vo.vm");
        defaultTemplateMap.put("vo", templates);

        templates = new Templates();
        templates.setKey("controller");
        templates.setName("Controller");
        templates.setPostfix("Controller.java"); 
        templates.setRewrite(Boolean.TRUE);
        templates.setActive(Boolean.TRUE);
        templates.setTemplatePath("/templates/backend/controller/controller.vm");
        defaultTemplateMap.put("controller", templates);
    }
     
     
    public void setNotifier(Function<String, String> notifier) {
        this.notifier = notifier;
    }
    
    protected void sendMessage(String msg){
        if(notifier != null)
            notifier.apply(msg);
    }
    
    public void setConfirm(Function<String, String> confirm) {
        this.confirm = confirm;
    }
    
    protected String confirmProcess(String msg){
        if(confirm != null)
           return confirm.apply(msg);
        else return "";
    }
}

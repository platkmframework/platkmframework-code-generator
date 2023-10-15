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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.velocity.runtime.RuntimeConstants; 
import org.platkmframework.codegenerator.core.TemplateProcessorEngine;
import org.platkmframework.codegenerator.core.Templates;
import org.platkmframework.codegenerator.util.Util;
import org.platkmframework.databasereader.core.DatabaseReader;
import org.platkmframework.databasereader.model.Table;
import org.platkmframework.jpa.database.mapping.DatabaseMapper;
import org.platkmframework.jpa.database.mapping.postgresql.PostgreSQLDatabaseMapper;
import org.platkmframework.jpa.database.mapping.sqlserver.SQLServerDatabaseMapper;

public class GeneratorProcessorDbToCrudPatterns extends GeneratorProcessorBase{
  
    String databaseMapperName; 
    String url; 
    String driver; 	
    String user; 	
    String password; 	
    String excludedTables;  
    List<Templates> customtemplates;
    List<String> parameters;
    Map<String, Templates> templates;
    String projectRootPath;

    
    
    public GeneratorProcessorDbToCrudPatterns(String projectRootPath, String databaseMapperName, 
                                                String url, 
                                                String driver, 
                                                String user, 
                                                String password, 
                                                String excludedTables, 
                                                Map<String, Templates> templates, 
                                                List<Templates> customtemplates
                                                ) {

        super(); 
        this.url = url;
        this.databaseMapperName = databaseMapperName;
        this.driver = driver;
        this.user = user;
        this.password = password;
        this.excludedTables = excludedTables; 
        this.customtemplates = customtemplates;  
        this.templates = templates;
        this.projectRootPath = projectRootPath;
    }

    public void process() {
        try {
            if( !CodeGenerationConstant.C_DATABASE_MAPPER_MSSQLSERVER.equalsIgnoreCase(databaseMapperName) && 
                            !CodeGenerationConstant.C_DATABASE_MAPPER_POSTGRESQL.equalsIgnoreCase(databaseMapperName)) {
 
                sendMessage("Debe seleccionar como nombre de base de datos uno de estos valores->  " + CodeGenerationConstant.C_DATABASE_MAPPER_MSSQLSERVER + " o " + CodeGenerationConstant.C_DATABASE_MAPPER_POSTGRESQL);

            }else {
                
                Class.forName(driver).getDeclaredConstructor().newInstance();
                con = DriverManager.getConnection(url, user, password);

                DatabaseReader databaseReader = new DatabaseReader(con, getExcludedTables(excludedTables));
                List<Table> list = databaseReader.readTables(null, null, null, new String[] {"TABLE"});

                if(list != null && !list.isEmpty() ){ 

                    TemplateProcessorEngine templateProcessorEngine = new TemplateProcessorEngine();

                    Properties prop = new Properties();
                    prop.setProperty(RuntimeConstants.RESOURCE_LOADER, "class,file");
                    prop.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.Log4JLogChute");
                    prop.setProperty("runtime.log.logsystem.log4j.logger", "VELLOGGER");
                    prop.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
                    prop.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogSystem");
                    templateProcessorEngine.init(prop);

                    DatabaseMapper databaseMapper;
                    if( databaseMapperName.trim().equalsIgnoreCase(CodeGenerationConstant.C_DATABASE_MAPPER_MSSQLSERVER)) {
                            databaseMapper = new SQLServerDatabaseMapper();
                    }else {
                            databaseMapper = new PostgreSQLDatabaseMapper();
                    } 
                    Util util = new Util(databaseMapper);
                    Map<String, Object> contextMap = new HashMap<>();
                    contextMap.put("util", util);
                    
                    templates.forEach((k,t)->{
                        contextMap.put( t.getKey()+"Package", t.getPackageName());
                    });

                    String[] paramValue;
                    if(parameters != null && !parameters.isEmpty()) {
                        for (String param : parameters) {
                            paramValue = param.split("=");
                            contextMap.put(paramValue[0], paramValue[1]);
                        }
                    } 
                    for (Table table : list){
                        contextMap.put("table", table);
                        
                        if(templates != null && !templates.isEmpty()){
                            templates.forEach((k,t)->{ 
                                if(t.getActive()){
                                    File file = new File(projectRootPath + File.separator + t.getProjectSourceFolders() + File.separator + t.getPackageName().replace(".", File.separator) + File.separator + util.fixToEntityName(table.getName()) + t.getPostfix());
                                    if(!file.exists() || t.getRewrite()){
                                        String result = templateProcessorEngine.generate(t.getTemplatePath(), contextMap);  
                                        sendMessage(result);  
                                        try {
                                            FileUtils.write(file, result, "UTF-8");
                                        } catch (IOException ex) {  
                                                sendMessage("error ->"  + ex.getMessage()); 
                                        }
                                    }
                                }  
                            }); 
                        }

                        if(customtemplates != null && !customtemplates.isEmpty()){
                            customtemplates.stream().forEach((t)->{

                                if(t.getActive()){ 
                                    File file = new File(projectRootPath + File.separator + t.getProjectSourceFolders() + File.separator + t.getPackageName().replace(".", File.separator) + File.separator + util.fixToEntityName(table.getName()) + t.getPostfix());
                                    if(!file.exists() || t.getRewrite()){
                                        String result = templateProcessorEngine.generate(t.getTemplatePath(), contextMap);  
                                        sendMessage(result); 
                                        try {
                                            FileUtils.write(file, result, "UTF-8");
                                        } catch (IOException ex) { 
                                                sendMessage("error ->"  + ex.getMessage()); 
                                        }
                                    }
                                }  
                            }); 
                        } 
                    } 
                }
            } 

        } catch ( ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException | SQLException ex) {
            sendMessage("error ->"  + ex.getMessage()); 
        } 
    } 

}

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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties; 
import javax.persistence.Entity;

import org.apache.velocity.runtime.RuntimeConstants; 
import org.platkmframework.codegenerator.core.TemplateProcessorEngine;
import org.platkmframework.codegenerator.util.Util;
import org.platkmframework.databasereader.core.DatabaseReader;
import org.platkmframework.databasereader.model.Column;
import org.platkmframework.databasereader.model.Table;
import org.platkmframework.jpa.database.mapping.DatabaseMapper;
import org.platkmframework.jpa.database.mapping.postgresql.PostgreSQLDatabaseMapper;
import org.platkmframework.jpa.database.mapping.sqlserver.SQLServerDatabaseMapper;

public class GeneratorProcessorEntityToDb extends GeneratorProcessorBase {
	 
    private final Map<String, String> templateMap;

    private Util util;

    String databaseType; 
    String url; 
    String serverURL;
    String schema;
    String driver; 	
    String user; 	
    String password; 
    String excludedTables; 
    String operation; 
    String projectClassesPathFolder;
	
    public GeneratorProcessorEntityToDb(String projectRootPath,
                                        String projectClassesPathFolder,
                                        String databaseType, 
                                        String url, 
                                        String driver,
                                        String user, 
                                        String password,
                                        String operation, 
                                        String serverURL, 
                                        String schema, 
                                        String excludedTables ) {

        super();
        this.projectClassesPathFolder = projectClassesPathFolder;
        this.databaseType = databaseType;
        this.url = url;
        this.driver = driver;
        this.user = user;
        this.password = password;
        this.operation = operation;
        this.schema = schema;
        this.serverURL = serverURL;
        this.excludedTables = excludedTables;

        templateMap = new HashMap<>();
        templateMap.put("create", "/templates/db/create.vm");
        templateMap.put("upate", "/templates/db/update.vm"); 
    }


    public void process(){

        try {
            if (DbOperation.valueOf(operation) != null){

                if( !CodeGenerationConstant.C_DATABASE_MAPPER_MSSQLSERVER.equalsIgnoreCase(databaseType) && 
                                !CodeGenerationConstant.C_DATABASE_MAPPER_POSTGRESQL.equalsIgnoreCase(databaseType)) {
                    sendMessage("Debe seleccionar como nombre de base de datos uno de estos valores->  " + CodeGenerationConstant.C_DATABASE_MAPPER_MSSQLSERVER + " o " + CodeGenerationConstant.C_DATABASE_MAPPER_POSTGRESQL);
                }else {
                    TemplateProcessorEngine templateProcessorEngine = new TemplateProcessorEngine();

                    Properties prop = new Properties();
                    prop.setProperty(RuntimeConstants.RESOURCE_LOADER, "class,file");
                    prop.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.Log4JLogChute");
                    prop.setProperty("runtime.log.logsystem.log4j.logger", "VELLOGGER");
                    prop.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
                    prop.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogSystem");
                    templateProcessorEngine.init(prop);

                    DatabaseMapper databaseMapper;
                    if( databaseType.trim().equalsIgnoreCase(CodeGenerationConstant.C_DATABASE_MAPPER_MSSQLSERVER)) {
                            databaseMapper = new SQLServerDatabaseMapper();
                    }else {
                            databaseMapper = new PostgreSQLDatabaseMapper();
                    }

                    util = new Util(databaseMapper);
                    Map<String, Object> contextMap = new HashMap<>();
                    contextMap.put("util", util);
                    contextMap.put("databaseType", databaseType);
                    contextMap.put("url", url);
                    contextMap.put("driver", driver);
                    contextMap.put("user", user);
                    contextMap.put("password", password); 
                    contextMap.put("operation", operation); 
                    contextMap.put("schema", schema);

                    if (DbOperation.valueOf(operation).name().equals(DbOperation.create.name())){

                        processCreateScripts(contextMap, templateProcessorEngine);

                    }else if (DbOperation.valueOf(operation).name().equals(DbOperation.validate.name()) || 
                                    DbOperation.valueOf(operation).name().equals(DbOperation.update.name())){

                        processValidateUpdateScripts(contextMap, templateProcessorEngine);
                    }
                }
            }else{
                if(notifier != null)
                    notifier.apply("No se reconoce la operación con nombre -> " + operation + " las operaciones váidas son -> "
                                                            + DbOperation.create.name() + ", "
                                                            + DbOperation.update.name() + ", "
                                                            + DbOperation.validate.name());
            } 

        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException
                        | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                        | SecurityException | SQLException | IOException e) { 

            sendMessage("error -> " +  e.getMessage());
        }finally {
            try {
                    if(con != null && !con.isClosed()) con.close();
            } catch (SQLException e) { 
                sendMessage("error -> " +  e.getMessage()); 
            }
        }
    }


    private void processValidateUpdateScripts(Map<String, Object> contextMap, TemplateProcessorEngine templateProcessorEngine) throws SQLException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException, IOException {
 
 
        sendMessage("Validando...");
        List<String> scripts = new ArrayList<>();
        Collection<Class<?>> entities = getEntities();
        if(entities != null &&  !entities.isEmpty() ){
                
            Class.forName(driver).getDeclaredConstructor().newInstance();
            con = DriverManager.getConnection(url, user, password);
            con.setAutoCommit(true);

            DatabaseReader databaseReader = new DatabaseReader(con, getExcludedTables(excludedTables));
            List<Table> tables = databaseReader.readTables(null, null, null, new String[] {"TABLE"});
            if(tables != null && !tables.isEmpty()) { 
                Table table;
                String entityTableName;
                String fieldColumnName;
                Column column;
                for (Class<?> classEntity : entities){
                    entityTableName = util.getEntityTableName(classEntity);
                    table = tables.stream().filter((t)->(t.getName().equalsIgnoreCase(util.getEntityTableName(classEntity)))).findFirst().orElse(null);
                    if(table == null){

                        sendMessage("No se encontró en la base de datos la tabla " + entityTableName + " que está asociada a la entidad " + classEntity.getSimpleName());

                        contextMap.put("entityClass", classEntity);
                        scripts.add(templateProcessorEngine.generate("/templates/db/" + databaseType + "/create.vm", contextMap)); 

                    }else {
                        List<Field> entityColumns = util.getFieldsWithColumnsTag(classEntity);
                        if(!entityColumns.isEmpty()) {
                                if(!table.getColumn().isEmpty()){
                                    for (Field field : entityColumns) {
                                        fieldColumnName = util.getFieldColumnName(field);
                                        column = table.getColumn().stream().filter((c)->(c.getName().equalsIgnoreCase(util.getFieldColumnName(field)))).findFirst().orElse(null);
                                        if(column == null) {
                                                sendMessage("No se han reflejado la columna " + fieldColumnName + " de la entidad " + classEntity.getSimpleName() + " en la tabla " +  entityTableName);
                                                contextMap.put("field", field);
                                                contextMap.put("tableName", entityTableName);
                                                scripts.add(templateProcessorEngine.generate("/templates/db/" + databaseType + "/create_column.vm", contextMap));
                                        }
                                    }
                                }else{
                                    sendMessage("No se ha reflejado las columnas de la entidad " + classEntity.getSimpleName() + " en la tabla " +  entityTableName);
                                }
                        }else{
                            sendMessage("No se encontraron attributos asociadas a la entidad " + classEntity.getSimpleName());
                        }
                    }
                }

                if(scripts.isEmpty()){
                    sendMessage("No se encontraron inconsistencias");
                }else {
                    if(DbOperation.update.name().equalsIgnoreCase(operation)) {
                        sendMessage("Listado de inconsistencias");
                        StringBuilder stringB = new StringBuilder();
                        for (String script : scripts) {
                            sendMessage(script);
                            stringB.append(script);
                        } 
                        if("s".equalsIgnoreCase(confirmProcess("Existen actualizaciones pendientes, desea ejecutarlas? (s,n)"))) {
                            con.createStatement().execute(stringB.toString()); 
                        }
                        sendMessage("Proceso finalizado");
                    }
                }
            }else {
                sendMessage("No se encontraron Tablas en la base de datos");
            }
        }else {
            sendMessage("No se encontraron Entidades para validar");
        }
    }

    private void processCreateScripts(Map<String, Object> contextMap, TemplateProcessorEngine templateProcessorEngine
                                                            ) throws InstantiationException, IllegalAccessException, ClassNotFoundException,
                                                                             IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, SQLException, IOException {

        Collection<Class<?>> entities = getEntities();
        if(entities != null && !entities.isEmpty() ){

            con = DriverManager.getConnection(serverURL, user, password);
            con.setAutoCommit(true);
            DatabaseReader databaseReader = new DatabaseReader(con, getExcludedTables(excludedTables));

            if(databaseReader.schemaExists(schema)){  
                if("s".equalsIgnoreCase(confirmProcess("La base de datos no está vacía. Desea borrar las tablas existentes? (s,n)"))){
                   String result = templateProcessorEngine.generate("/templates/db/" + databaseType + "/drop.vm", contextMap); 
                    sendMessage(result);
                    con.createStatement().execute(result); 
                    processCreateScriptsByEntities(contextMap, entities, templateProcessorEngine);
                }
            }else { 
                processCreateScriptsByEntities(contextMap, entities, templateProcessorEngine);
            }
        }else {
                sendMessage("No se encontraron Entidades para generar los scripts de creación");
        }
    }


    private void processCreateScriptsByEntities(Map<String, Object> contextMap, Collection<Class<?>> entities, TemplateProcessorEngine templateProcessorEngine) throws SQLException, IOException{

        String result = templateProcessorEngine.generate("/templates/db/" + databaseType + "/create_database.vm", contextMap); 
        sendMessage(result);
        con.createStatement().execute(result);
        con.commit();
        con.close();

        con = DriverManager.getConnection(url, user, password);
        con.setAutoCommit(true);

        StringBuilder bf = new StringBuilder();
        for (Class<?> class1 : entities){
            contextMap.put("entityClass", class1);
            result = templateProcessorEngine.generate("/templates/db/" + databaseType + "/create.vm", contextMap); 
            sendMessage(result); 
            bf.append(result);
        }
 
        if("s".equalsIgnoreCase(confirmProcess("A continuación se ejecutarán los scripts en la base de datos. Desea continuar (s,n)"))){
            con.createStatement().execute(bf.toString());
            con.commit();
            sendMessage("Proceso realizado satisfactoriamente");
        }
    }
    
    protected Collection<Class<?>>  getEntities(){
    
        Collection<Class<?>> entities = new ArrayList<>();
        try {
             
            File file = new File(projectClassesPathFolder);
            URL url1 = file.toURI().toURL();
            URL[] urls = new URL[]{url1};
            ClassLoader cl = new URLClassLoader(urls);
            
            loadEntityClass(file, cl, entities); 
            return entities;
            
        } catch (MalformedURLException | ClassNotFoundException ex) {
            sendMessage("Error ->" + ex.getMessage());
        }  
        
        return entities;
    }
    
    private void loadEntityClass(File file, ClassLoader cl, Collection<Class<?>> entities) throws ClassNotFoundException{
        if(file.isDirectory()){
            File[]  files = file.listFiles();
            if(files != null){
                for (File file1 : files) {
                    loadEntityClass(file1, cl, entities); 
                }
            }
        }else{
            if(file.getName().endsWith(".class")){
                String fileName = file.getAbsolutePath().replace(projectClassesPathFolder + File.separator,"");
                Class<?> cls = cl.loadClass(fileName.substring(0, fileName.lastIndexOf(".")).replace('/', '.').replace('\\', '.'));
                if(cls.isAnnotationPresent(Entity.class)){
                    entities.add(cls);
                }
            } 
        }
    }
    
    protected List<Class<?>> orderedEntities(List<Class<?>> entities, Util util ){
     
        List<Class<?>> processed = new ArrayList<>(); 
        
        for (Class<?> cTable : entities) {
            processOrderedEntities(processed, cTable);
        }
        
        return processed;
    }
    
    protected void processOrderedEntities(List<Class<?>> processed,Class<?> cTable){
        List<Field> fkFields = util.getFieldsWithJoinColumnTag(cTable);
       
        if(fkFields.isEmpty()){
            processed.add(cTable);
        }else{
            for (Field field : fkFields) {
                cTable =  processed.stream().filter((t)->(
                         t.getAnnotation(javax.persistence.Table.class).name()
                                 .equalsIgnoreCase(field.getAnnotation(javax.persistence.JoinColumn.class).table())
                        )).findAny().orElse(null);

                if(cTable != null){
                    processOrderedEntities(processed, cTable);
                }
            }
            processed.add(cTable);
        } 
    }
 
}

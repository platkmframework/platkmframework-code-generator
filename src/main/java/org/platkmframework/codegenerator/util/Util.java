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
package org.platkmframework.codegenerator.util;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;

import org.apache.commons.lang3.StringUtils;
import org.platkmframework.databasereader.model.Column;
import org.platkmframework.databasereader.model.FkContraint;
import org.platkmframework.databasereader.model.ImportedKey;
import org.platkmframework.databasereader.model.Table;
import org.platkmframework.jpa.database.mapping.BasicJavaValueParser;
import org.platkmframework.jpa.database.mapping.DatabaseMapper;
import org.platkmframework.util.reflection.ReflectionUtil;

public class Util {

	DatabaseMapper databaseMapper;
	Connection connection;
	
	public Util(DatabaseMapper databaseMapper) {
		super();
		this.databaseMapper = databaseMapper;
	}
	
	public String getEntityTableName(Class<?> class1) {
		if(class1.isAnnotationPresent(javax.persistence.Table.class)) {
			return class1.getAnnotation(javax.persistence.Table.class).name();
		}else return "";
	}
	
	public List<Field> getFieldsWithColumnsTag(Class<?> class1){
		List<Field> fieldsReturn = new ArrayList<>();
		List<Field> fields = ReflectionUtil.getAllFieldHeritage(class1);
		for (Field field : fields) {
			if(field.isAnnotationPresent(javax.persistence.Column.class)){
				fieldsReturn.add(field);
			} 
		}
		return fieldsReturn;
	}
	
    public List<Field> getFieldsWithJoinColumnTag(Class<?> cTable) {
        List<Field> fieldsReturn = new ArrayList<>();
        List<Field> fields = ReflectionUtil.getAllFieldHeritage(cTable);
        for (Field field : fields) {
                if(field.isAnnotationPresent(javax.persistence.Column.class) && field.isAnnotationPresent(javax.persistence.JoinColumn.class)){
                        fieldsReturn.add(field);
                } 
        }
        return fieldsReturn;        
    }
    
	public boolean isPkField(Field field) {
		return field.isAnnotationPresent(Id.class);
	}

	public boolean isGeneratedValue(Field field) {
		return field.isAnnotationPresent(GeneratedValue.class);
	}
	
	public boolean isForeignKey(Field field) {
		return field.isAnnotationPresent(JoinColumn.class);
	}
	
	public JoinColumn getJoinColumnInfo(Field field) {
		return field.getAnnotation(JoinColumn.class);
	}
	
	public boolean isNullable(Field field) {
		return field.getAnnotation(javax.persistence.Column.class).nullable();
	}
	
	public String getFieldColumnName(Field field) {
		return field.getAnnotation(javax.persistence.Column.class).name();
	}
	
	public boolean isColumnDefinition(Field field) {
		return StringUtils.isNoneBlank(field.getAnnotation(javax.persistence.Column.class).columnDefinition());
	}
	
	public String getColumnDefinition(Field field) {
	    return field.getAnnotation(javax.persistence.Column.class).columnDefinition();
	}
         
	public String getDbType(Field field) {
            String dbDataType =  databaseMapper.getDbDataType(field.getType());
            javax.persistence.Column fieldcolumn = field.getAnnotation(javax.persistence.Column.class);
            if(field.getType().getName().equals(java.sql.Date.class.getName()) ||
                            field.getType().getName().equals(java.util.Date.class.getName()) ||
                            field.getType().getName().startsWith("java.time")) {
                    return dbDataType.replace("${precision}", "").replace("${scale}", "").replace("${length}", "");
            }else {
                    return dbDataType.replace("${precision}", String.valueOf(fieldcolumn.precision())).
                                    replace("${scale}", String.valueOf(fieldcolumn.scale())).
                                    		replace("${length}", String.valueOf(fieldcolumn.length()));
            }
	}
	
	/**public String getDecimalAndScale(javax.persistence.Column fieldcolumn) {
		if(fieldcolumn.precision() == 0) return "";
		return " (" + fieldcolumn.scale() + (fieldcolumn.scale() == 0? "": "," + fieldcolumn.scale()) + ") ";
	}*/
	
        /**
    * 
    * @param table
    * @return 
    */
   public String getPKJavaType(Table table) {

        if(table.getPkContraint() == null ||
                table.getPkContraint().getListField() == null ||
                table.getPkContraint().getListField().isEmpty()) return "java.lang.Object";

        String columnName = table.getPkContraint().getListField().get(0);
        Column column = table.getColumn().stream().filter((c)-> c.getName().equals(columnName)).findFirst().orElse(null);


        BasicJavaValueParser<?> basicJavaValueParser = databaseMapper.getDefaultJavaValueParserBySqlType(column.getJavaSqlType());
        return basicJavaValueParser.getJavaType().getName();
   }
	
    public String getAttributeForMethodName(String columnName) {
        return firstUpperCase(fixToEntityName(columnName));
    }
	
    public String getColumnJavaType(Column column) { 
        BasicJavaValueParser<?> basicJavaValueParser = databaseMapper.getDefaultJavaValueParserBySqlType(column.getJavaSqlType());
        Class<?> class1 = basicJavaValueParser.getJavaType();
        if(class1.isArray()){
            return class1.getTypeName();
        }else{
            return class1.getName();
        } 
    }
	
    public Boolean isPkPresent(Table table) {

            if(table.getPkContraint() == null ||
                    table.getPkContraint().getListField() == null ||
                    table.getPkContraint().getListField().isEmpty()) return Boolean.FALSE;

            String columnName = table.getPkContraint().getListField().get(0);
            Column column = table.getColumn().stream().filter((c)-> c.getName().equals(columnName)).findFirst().orElse(null);
            return column != null;	
    }
	
    public Column getPkColumn(Table table) {

            if(table.getPkContraint() == null ||
                    table.getPkContraint().getListField() == null ||
                    table.getPkContraint().getListField().isEmpty()) return null;

            String columnName = table.getPkContraint().getListField().get(0);
            return table.getColumn().stream().filter((c)-> c.getName().equals(columnName)).findFirst().orElse(null);
    }

    public boolean isFkColumn(Table table, Column column){
        
        return table.getFkContraint().stream().filter((fk)-> (
                fk.getImportedKey().stream().filter((ik)->(ik.getFkColumnName().equalsIgnoreCase(column.getName()))).findAny().isPresent()
                
                )).findAny().isPresent();
    }	
    
    
    public ImportedKey getFkColumnInfo(Table table, Column column){
        
        ImportedKey importedKey;
        for (FkContraint fkContraint : table.getFkContraint()) {
            importedKey = fkContraint.getImportedKey().stream().filter(
                     
                    (ik)->(ik.getFkColumnName().equalsIgnoreCase(column.getName()))
            
            ).findAny().orElse(null);
            
            if(importedKey != null) return importedKey;
        }
        return null;  
    }            
    /**
     * description: first upper case
     * @param value: value
     * @return result string
     */
    public  String firstUpperCase(String value) {
        if (value == null || "".equals(value)) {
            return value;
        }
        if (value.length() == 1) {
            return value.toUpperCase();
        }
        return (value.substring(0, 1)).toUpperCase() + value.substring(1);
    } 
	 
	 
	/**
	 * description: fix to entity name
	 * @param name name
	 * @return fixed name
	 */
    public String fixToEntityName(String name) {
        return fixName(name, null, true);
    }
    
    /**
     * description: fix to attribute name
     * @param name: name
     * @return fixed name
     */
    public String fixToAttributeName(String name) {
        return fixName(name, null, false);
    }
    
    /**
     * description: fix name
     * @param name: name
     * @param extraCharacters: extra characters
     * @param firstUpperCase: whether first should be upper case
     * @return name fixed
     */
    public String fixName(String name, String extraCharacters,  boolean firstUpperCase) {
        String result = "";
        String pattern ="abcdefghijklmnopqrstuvwxyz";
        String patternNumber ="1234567890";
        
        if(StringUtils.isNotEmpty(extraCharacters)){
            pattern+=extraCharacters;
        }
        
        if(StringUtils.isNotEmpty(name) && !"".equals(name.trim()) ){
           String auxName = name.trim();
           
           //todo en minuscula
           auxName = auxName.toLowerCase();
           boolean primerCaractOk = false;
           boolean nextUpperCase  = false;
           for (int i = 0; i < auxName.length(); i++) {
                char  varChar = auxName.charAt(i);
                if(pattern.contains(String.valueOf(varChar))){
                    //es un caracter alfa numerico
                    if(!primerCaractOk){
                        if(firstUpperCase){
                             varChar = Character.toUpperCase(varChar);
                        }else{
                            varChar = Character.toLowerCase(varChar);
                        }
                        result  = result + String.valueOf(varChar);
                        primerCaractOk = true;
                        nextUpperCase = false;
                    }else{
                        if(nextUpperCase){
                            varChar = Character.toUpperCase(varChar);
                            result  = result + String.valueOf(varChar);
                            nextUpperCase = false;
                        }else{
                            result  = result + String.valueOf(varChar);
                        }
                    }
                }else{
                    if(i>0 && patternNumber.contains(String.valueOf(varChar)))
                    {
                       //es un numero y no es el primer caracter, se deja adicionar
                       result  = result + String.valueOf(varChar);
                    }else
                    {
                       //no es alfa numerico y se marca que el proximo va en mayuscula
                       nextUpperCase = true;
                    }
                    
                } 
            }
            
           if(StringUtils.isEmpty(result)){
               result = "No Name Found";
           }
           
        }
        
        return result;
     }

	public DatabaseMapper getDatabaseMapper() {
		return databaseMapper;
	}

	public void setDatabaseMapper(DatabaseMapper databaseMapper) {
		this.databaseMapper = databaseMapper;
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}



}

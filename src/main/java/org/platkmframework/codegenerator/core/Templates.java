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


public class Templates {
	
    private String key;
    private String name;
    private String projectSourceFolders;  
    private String packageName;
    private Boolean rewrite;
    private String postfix = null;
    private Boolean active;
    private String templatePath;
	
    public String getName() {
            return name;
    }
    public void setName(String name) {
            this.name = name;
    }
  
    public Boolean getRewrite() {
            return rewrite;
    }
    public void setRewrite(Boolean rewrite) {
            this.rewrite = rewrite;
    }
    public String getPostfix() {
            return postfix;
    }
    public void setPostfix(String postfix) {
            this.postfix = postfix;
    }  

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    } 

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    } 
    public String getProjectSourceFolders() {
        return projectSourceFolders;
    }

    public void setProjectSourceFolders(String projectSourceFolders) {
        this.projectSourceFolders = projectSourceFolders;
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }
	
}

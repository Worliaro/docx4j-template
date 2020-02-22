/*
 * Copyright (c) 2018, hiwepy (https://github.com/hiwepy).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.docx4j.template;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * 模板处理接口
 * @author <a href="https://github.com/hiwepy">hiwepy</a>
 */
public interface WordprocessingMLTemplate {
	
	/**
	 * @param template ：模板文件
	 * @param variables ：变量
	 * @return {@link WordprocessingMLPackage} 对象
	 * @throws Exception ：异常对象
	 */
	default WordprocessingMLPackage process(File template, Map<String, Object> variables) throws Exception{
		return this.process(new FileInputStream(template), variables);
	}
	
	/**
	 * @param template ：模板文件流
	 * @param variables ：变量
	 * @return {@link WordprocessingMLPackage} 对象
	 * @throws Exception ：异常对象
	 */
	default WordprocessingMLPackage process(InputStream template, Map<String, Object> variables) throws Exception{
		return WordprocessingMLPackage.load(template);
	}
	
	/**
	 * @param template ：模板内容/路径
	 * @param variables ：变量
	 * @return {@link WordprocessingMLPackage} 对象
	 * @throws Exception ：异常对象
	 */
	default WordprocessingMLPackage process(String template, Map<String, Object> variables) throws Exception{
		return this.process(new FileInputStream(template), variables);
	}
	
	
}

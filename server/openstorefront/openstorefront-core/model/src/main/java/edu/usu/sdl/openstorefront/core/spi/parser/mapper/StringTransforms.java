/*
 * Copyright 2016 Space Dynamics Laboratory - Utah State University Research Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.usu.sdl.openstorefront.core.spi.parser.mapper;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author dshurtleff
 */
public enum StringTransforms
	implements DataTransform<String>
{
	UPPERCASE("Uppercase"){
		
		@Override
		public String transform(String input)
		{
			if (StringUtils.isNotBlank(input)) {
				return input.toUpperCase();
			} else {
				return input;
			}
		}
		
	},
	
	LOWERCASE("Lowercase"){
		
		@Override
		public String transform(String input)
		{
			if (StringUtils.isNotBlank(input)) {
				return input.toLowerCase();
			} else {
				return input;
			}
		}
		
	},	
	
	TRIM("Trim"){
		
		@Override
		public String transform(String input)
		{
			if (StringUtils.isNotBlank(input)) {
				return input.trim();
			} else {
				return input;
			}
		}
		
	},
	
	BOLD("Bold"){
		
		@Override
		public String transform(String input)
		{
			if (StringUtils.isNotBlank(input)) {
				return "<b>" + input + "</b>";
			} else {
				return input;
			}
		}
		
	},	
	
	REMOVEUNDERSCORE("Remove Underscore") {
		@Override
		public String transform(String input)
		{
			if (StringUtils.isNotBlank(input)) {
				return input.replace("_", "");
			} else {
				return input;
			}			
		}
	},
	
	TRANSLATEESCAPES("Translate escaped characters"){
		
		@Override
		public String transform(String input)
		{
			if (StringUtils.isNotBlank(input)) {
				input = input.replace("x0020", " ");
				input = input.replace("x0028", "(");
				input = input.replace("x0029", ")");
				input = input.replace("x002F", "/");
				input = input.replace("x005E", "^");
				input = input.replace("x007C", "|");
				input = input.replace("x007B", "{");
				input = input.replace("x007D", "}");
				input = input.replace("x007E", "~");
				input = input.replace("x0060", "`");
				input = input.replace("x0025", "%");
				input = input.replace("x0026", "&");
				input = input.replace("x0023", "#");
				input = input.replace("x0040", "@");
				return input;
			} else {
				return input;
			}
		}
		
	},
	
	TRANSLATEHTMLESCAPES("Translate HTML escaped characters"){
		
		@Override
		public String transform(String input)
		{
			if (StringUtils.isNotBlank(input)) {
				input = input.replace("&gt;", ">");
				input = input.replace("&amp;#58;", "://");
				input = input.replace("&amp;", "&");
				return input;
			} else {
				return input;
			}
		}
		
	},	

	SPLITLASTUNDERSCORE("Split Last Underscore"){
		
		@Override
		public String transform(String input)
		{
			return StringTransforms.splitLast(input, "_");
		}
		
	},
	
	SPLITLASTDASH("Split Last Dash"){
		
		@Override
		public String transform(String input)
		{
			return StringTransforms.splitLast(input, "-");
		}
		
	},

	SPLITLASTSPACE("Split Last Space"){
		
		@Override
		public String transform(String input)
		{
			return StringTransforms.splitLast(input, " ");
		}
		
	},
	
	SPLITLASTDOT("Split Last Dot"){
		
		@Override
		public String transform(String input)
		{
			return StringTransforms.splitLast(input, "\\.");
		}
		
	},	
	
	SPLITLASTCOMMA("Split Last Comma"){
		
		@Override
		public String transform(String input)
		{
			return StringTransforms.splitLast(input, ",");
		}
		
	};	
	
	private static String splitLast(String input, String splitChar) {
			if (StringUtils.isNotBlank(input)) {
				String data[] = input.split(splitChar);
				return data[data.length-1];
			} else {
				return input;
			}		
	}
	
	private String description;

	private StringTransforms(String description)
	{
		this.description = description;
	}

	public String getDescription()
	{
		return description;
	}
	
}

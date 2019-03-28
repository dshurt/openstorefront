/*
 * Copyright 2018 Space Dynamics Laboratory - Utah State University Research Foundation.
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
package edu.usu.sdl.spoon.aerospace.importer.model;

import java.util.ArrayList;
import java.util.List;
import org.simpleframework.xml.ElementList;

/**
 *
 * @author dshurtleff
 */
public class ProductFamily
{

	@ElementList(name = "classification", type = Classification.class, inline = true, required = false)
	private List<Classification> classification = new ArrayList<>();

	public ProductFamily()
	{
	}

	public List<Classification> getClassification()
	{
		return classification;
	}

	public void setClassification(List<Classification> classification)
	{
		this.classification = classification;
	}

}

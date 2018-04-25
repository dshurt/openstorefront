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
package edu.usu.sdl.openstorefront.core.model;

import edu.usu.sdl.openstorefront.core.view.RequiredForComponent;
import edu.usu.sdl.openstorefront.validation.ValidationResult;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author dshurtleff
 */
public class ComponentFormSet
{

	private ComponentAll primary;
	private List<ComponentAll> children = new ArrayList<>();

	public ComponentFormSet()
	{
	}

	public ValidationResult validate(boolean checkRequiredForComponent)
	{
		ValidationResult result = new ValidationResult();

		List<ComponentAll> toCheck = new ArrayList<>();
		if (primary != null) {
			toCheck.add(primary);
		}
		toCheck.addAll(children);

		for (ComponentAll componentAll : toCheck) {
			result.merge(componentAll.validate());

			if (checkRequiredForComponent) {
				RequiredForComponent requiredForComponent = new RequiredForComponent();
				requiredForComponent.setComponent(componentAll.getComponent());
				requiredForComponent.setAttributes(componentAll.getAttributes());
				result.merge(requiredForComponent.checkForComplete());
			}
		}

		return result;
	}

	public ComponentAll getPrimary()
	{
		return primary;
	}

	public void setPrimary(ComponentAll primary)
	{
		this.primary = primary;
	}

	public List<ComponentAll> getChildren()
	{
		return children;
	}

	public void setChildren(List<ComponentAll> children)
	{
		this.children = children;
	}

}

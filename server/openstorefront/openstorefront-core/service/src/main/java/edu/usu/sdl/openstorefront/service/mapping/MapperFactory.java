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
package edu.usu.sdl.openstorefront.service.mapping;

import edu.usu.sdl.openstorefront.core.entity.SubmissionFormField;
import edu.usu.sdl.openstorefront.core.entity.SubmissionFormFieldMappingType;
import edu.usu.sdl.openstorefront.core.entity.SubmissionFormTemplate;
import edu.usu.sdl.openstorefront.core.entity.UserSubmissionField;
import edu.usu.sdl.openstorefront.core.model.ComponentAll;
import edu.usu.sdl.openstorefront.core.model.ComponentFormSet;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author dshurtleff
 */
public class MapperFactory
{

	public BaseMapper getMapperForField(String mappingType)
	{
		BaseMapper mapper = null;
		switch (mappingType) {
			case SubmissionFormFieldMappingType.COMPONENT:
				mapper = new ComponentFieldMapper();
				break;
			case SubmissionFormFieldMappingType.COMPLEX:
				mapper = new ComplexMapper();
				break;
			case SubmissionFormFieldMappingType.SUBMISSION:
				mapper = new SubmissionMapper();
				break;
			case SubmissionFormFieldMappingType.NONE:
				mapper = new BaseMapper()
				{

					@Override
					public List<ComponentAll> mapField(ComponentAll componentAll, SubmissionFormField submissionField)
					{
						//noop; for static content
						return new ArrayList<>();
					}

					@Override
					public List<ComponentAll> mapField(ComponentAll componentAll, SubmissionFormField submissionField, UserSubmissionField userSubmissionField)
					{
						//noop; for static content
						return new ArrayList<>();
					}

					@Override
					public UserSubmissionFieldMedia mapComponentToSubmission(SubmissionFormField submissionField, ComponentFormSet componentFormSet, SubmissionFormTemplate template) throws MappingException
					{
						//noop; for static content
						return null;
					}

				};
				break;
			default:
				throw new UnsupportedOperationException(mappingType + " not supported");
		}
		return mapper;
	}

}

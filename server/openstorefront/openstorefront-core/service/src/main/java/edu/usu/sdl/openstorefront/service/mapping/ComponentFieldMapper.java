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

import edu.usu.sdl.openstorefront.common.util.Convert;
import edu.usu.sdl.openstorefront.common.util.StringProcessor;
import edu.usu.sdl.openstorefront.core.entity.Component;
import edu.usu.sdl.openstorefront.core.entity.ComponentMedia;
import edu.usu.sdl.openstorefront.core.entity.MediaType;
import edu.usu.sdl.openstorefront.core.entity.SubmissionFormField;
import edu.usu.sdl.openstorefront.core.entity.SubmissionFormTemplate;
import edu.usu.sdl.openstorefront.core.entity.UserSubmissionField;
import edu.usu.sdl.openstorefront.core.entity.UserSubmissionMedia;
import edu.usu.sdl.openstorefront.core.model.ComponentAll;
import edu.usu.sdl.openstorefront.core.model.ComponentFormSet;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.beanutils.BeanUtils;

/**
 * This mapper handles field to field mapping basically direct value to value.
 *
 * @author dshurtleff
 */
public class ComponentFieldMapper
		extends BaseMapper
{

	private static final Logger LOG = Logger.getLogger(ComponentFieldMapper.class.getName());

	private static final String STUB_TEST = "TEST-";

	@Override
	public List<ComponentAll> mapField(ComponentAll componentAll, SubmissionFormField submissionField, UserSubmissionField userSubmissionField)
			throws MappingException
	{
		List<ComponentAll> childComponents = new ArrayList<>();

		String value = STUB_TEST + StringProcessor.uniqueId();
		if (userSubmissionField != null) {
			value = userSubmissionField.getRawValue();
		}

		try {
			if (Component.FIELD_RELEASE_DATE.equals(submissionField.getFieldName())) {
				componentAll.getComponent().setReleaseDate(Convert.toDate(value));
			} else {
				BeanUtils.setProperty(componentAll.getComponent(), submissionField.getFieldName(), value);
			}
		} catch (IllegalAccessException | InvocationTargetException ex) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, null, ex);
			}

			MappingException mappingException = new MappingException("Field cannot be mapped.");
			mappingException.setFieldLabel(submissionField.getLabel());
			mappingException.setFieldName(submissionField.getFieldName());
			mappingException.setMappingType(submissionField.getMappingType());
			mappingException.setRawValue(value);
			throw mappingException;
		}
		//handle any media (inline media)
		if (userSubmissionField != null && submissionField.getFieldId() != null) {
			UserSubmissionMedia exampleMedia = new UserSubmissionMedia();
			exampleMedia.setTemplateFieldId(submissionField.getFieldId());
			List<UserSubmissionMedia> mediaRecords = exampleMedia.findByExample();

			componentAll.getMedia().addAll(createInlineMedia(mediaRecords));
		}

		return childComponents;
	}

	private List<ComponentMedia> createInlineMedia(List<UserSubmissionMedia> submissionMedia)
	{
		List<ComponentMedia> componentMediaList = new ArrayList<>();

		if (submissionMedia != null) {
			for (UserSubmissionMedia userSubmissionMedia : submissionMedia) {
				ComponentMedia componentMedia = new ComponentMedia();
				componentMedia.setMediaTypeCode(MediaType.typeFromMimeType(userSubmissionMedia.getFile().getMimeType()).getCode());
				componentMedia.setFile(userSubmissionMedia.getFile().copy());
				componentMedia.setUsedInline(Boolean.TRUE);
				componentMedia.setHideInDisplay(Boolean.TRUE);
				componentMediaList.add(componentMedia);
			}
		}

		return componentMediaList;
	}

	@Override
	public UserSubmissionFieldMedia mapComponentToSubmission(SubmissionFormField submissionField, ComponentFormSet componentFormSet, SubmissionFormTemplate template) throws MappingException
	{
		UserSubmissionFieldMedia userSubmissionFieldMedia = new UserSubmissionFieldMedia();

		UserSubmissionField userSubmissionField = new UserSubmissionField();
		userSubmissionFieldMedia.setUserSubmissionField(userSubmissionField);

		try {
			userSubmissionField.setTemplateFieldId(submissionField.getFieldId());
			userSubmissionField.setRawValue(BeanUtils.getProperty(componentFormSet.getPrimary().getComponent(), submissionField.getFieldName()));

		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, null, ex);
			}

			MappingException mappingException = new MappingException("Component Field cannot be mapped.");
			mappingException.setFieldLabel(submissionField.getLabel());
			mappingException.setFieldName(submissionField.getFieldName());
			mappingException.setMappingType(submissionField.getMappingType());
			throw mappingException;
		}

		return userSubmissionFieldMedia;
	}

}

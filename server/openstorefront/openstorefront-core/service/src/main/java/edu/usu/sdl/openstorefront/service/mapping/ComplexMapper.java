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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.usu.sdl.openstorefront.common.util.StringProcessor;
import edu.usu.sdl.openstorefront.core.api.ServiceProxyFactory;
import edu.usu.sdl.openstorefront.core.entity.AttributeType;
import edu.usu.sdl.openstorefront.core.entity.ComponentAttribute;
import edu.usu.sdl.openstorefront.core.entity.ComponentContact;
import edu.usu.sdl.openstorefront.core.entity.ComponentExternalDependency;
import edu.usu.sdl.openstorefront.core.entity.ComponentMedia;
import edu.usu.sdl.openstorefront.core.entity.ComponentRelationship;
import edu.usu.sdl.openstorefront.core.entity.ComponentResource;
import edu.usu.sdl.openstorefront.core.entity.ComponentTag;
import edu.usu.sdl.openstorefront.core.entity.SubmissionFormField;
import edu.usu.sdl.openstorefront.core.entity.SubmissionFormFieldType;
import edu.usu.sdl.openstorefront.core.entity.SubmissionFormTemplate;
import edu.usu.sdl.openstorefront.core.entity.UserSubmissionField;
import edu.usu.sdl.openstorefront.core.entity.UserSubmissionMedia;
import edu.usu.sdl.openstorefront.core.model.ComponentAll;
import edu.usu.sdl.openstorefront.core.model.ComponentFormSet;
import edu.usu.sdl.openstorefront.core.view.ComponentAttributeView;
import edu.usu.sdl.openstorefront.core.view.ComponentContactView;
import edu.usu.sdl.openstorefront.core.view.ComponentExternalDependencyView;
import edu.usu.sdl.openstorefront.core.view.ComponentMediaView;
import edu.usu.sdl.openstorefront.core.view.ComponentRelationshipView;
import edu.usu.sdl.openstorefront.core.view.ComponentResourceView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This maps complex object back into entities; use for contacts, media etc.
 *
 * @author dshurtleff
 */
public class ComplexMapper
		extends BaseMapper
{

	private static final Logger LOG = Logger.getLogger(ComplexMapper.class.getName());
	private ObjectMapper objectMapper;

	public ComplexMapper()
	{
		super();
		objectMapper = StringProcessor.defaultObjectMapper();
	}

	public ComplexMapper(ObjectMapper objectMapper)
	{
		this.objectMapper = objectMapper;
	}

	@Override
	public List<ComponentAll> mapField(ComponentAll componentAll, SubmissionFormField submissionField, UserSubmissionField userSubmissionField)
			throws MappingException
	{
		List<ComponentAll> childComponents = new ArrayList<>();

		if (userSubmissionField != null) {

			String fieldType = submissionField.getFieldType();
			try {
				switch (fieldType) {

					case SubmissionFormFieldType.ATTRIBUTE:
					case SubmissionFormFieldType.ATTRIBUTE_SINGLE:
					case SubmissionFormFieldType.ATTRIBUTE_MULTI:
					case SubmissionFormFieldType.ATTRIBUTE_SUGGESTED:
					case SubmissionFormFieldType.ATTRIBUTE_RADIO:
					case SubmissionFormFieldType.ATTRIBUTE_REQUIRED:
					case SubmissionFormFieldType.ATTRIBUTE_MULTI_CHECKBOX:
						addAttribute(componentAll, userSubmissionField);
						break;

					case SubmissionFormFieldType.CONTACT:
					case SubmissionFormFieldType.CONTACT_MULTI:
						addContact(componentAll, userSubmissionField);
						break;

					case SubmissionFormFieldType.EXT_DEPENDENCY:
					case SubmissionFormFieldType.EXT_DEPENDENCY_MULTI:
						addDependency(componentAll, userSubmissionField);
						break;

					case SubmissionFormFieldType.MEDIA:
					case SubmissionFormFieldType.MEDIA_MULTI:
						addMedia(componentAll, userSubmissionField);
						break;

					case SubmissionFormFieldType.RESOURCE:
					case SubmissionFormFieldType.RESOURCE_SIMPLE:
					case SubmissionFormFieldType.RESOURCE_MULTI:
						addResource(componentAll, userSubmissionField);
						break;

					case SubmissionFormFieldType.TAG:
					case SubmissionFormFieldType.TAG_MULTI:
						addTag(componentAll, userSubmissionField);
						break;

					case SubmissionFormFieldType.RELATIONSHIPS:
					case SubmissionFormFieldType.RELATIONSHIPS_MULTI:
						addRelationships(componentAll, userSubmissionField);
						break;

					default:
						throw new UnsupportedOperationException(fieldType + " not supported");
				}
			} catch (IOException ioe) {
				String value = userSubmissionField.getRawValue();

				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "Field Mapping exception", ioe);
				}
				MappingException mappingException = new MappingException("Unable to mapping form field to entry.");
				mappingException.setFieldLabel(submissionField.getLabel());
				mappingException.setFieldType(fieldType);
				mappingException.setMappingType(submissionField.getMappingType());
				mappingException.setRawValue(value);
				throw mappingException;
			}
		}

		return childComponents;
	}

	private void addAttribute(ComponentAll componentAll, UserSubmissionField userSubmissionField) throws IOException
	{
		if (userSubmissionField != null) {
			List<ComponentAttribute> componentAttributes = objectMapper.readValue(userSubmissionField.getRawValue(), new TypeReference<List<ComponentAttribute>>()
			{
			});
			componentAll.getAttributes().addAll(componentAttributes);
		}
	}

	private void addContact(ComponentAll componentAll, UserSubmissionField userSubmissionField) throws IOException
	{
		if (userSubmissionField != null) {
			List<ComponentContact> contacts = objectMapper.readValue(userSubmissionField.getRawValue(), new TypeReference<List<ComponentContact>>()
			{
			});
			//clear ids
			for (ComponentContact contact : contacts) {
				contact.setComponentContactId(null);
			}

			componentAll.getContacts().addAll(contacts);
		}
	}

	private void addDependency(ComponentAll componentAll, UserSubmissionField userSubmissionField) throws IOException
	{
		if (userSubmissionField != null) {
			List<ComponentExternalDependency> dependencies = objectMapper.readValue(userSubmissionField.getRawValue(), new TypeReference<List<ComponentExternalDependency>>()
			{
			});
			//clear ids
			for (ComponentExternalDependency dependency : dependencies) {
				dependency.setDependencyId(null);
			}
			componentAll.getExternalDependencies().addAll(dependencies);
		}
	}

	private void addMedia(ComponentAll componentAll, UserSubmissionField userSubmissionField) throws IOException
	{
		if (userSubmissionField != null) {

			List<ComponentMedia> mediaRecords = objectMapper.readValue(userSubmissionField.getRawValue(), new TypeReference<List<ComponentMedia>>()
			{
			});
			//clear ids
			for (ComponentMedia media : mediaRecords) {
				media.setComponentMediaId(null);
			}
			mapMedia(userSubmissionField, mediaRecords);

			componentAll.getMedia().addAll(mediaRecords);
		}

	}

	private void mapMedia(UserSubmissionField userSubmissionField, List<ComponentMedia> mediaRecords)
	{
		Map<String, UserSubmissionMedia> mediaMap = new HashMap<>();

		UserSubmissionMedia fieldMediaExample = new UserSubmissionMedia();
		fieldMediaExample.setTemplateFieldId(userSubmissionField.getTemplateFieldId());
		List<UserSubmissionMedia> fieldMedia = fieldMediaExample.findByExample();

		for (UserSubmissionMedia userSubmissionMedia : fieldMedia) {
			mediaMap.put(userSubmissionMedia.getFile().getMediaFileId(), userSubmissionMedia);
		}

		for (ComponentMedia media : mediaRecords) {
			if (media.getFile() != null) {
				UserSubmissionMedia userSubmissionMedia = mediaMap.get(media.getFile().getMediaFileId());
				if (userSubmissionMedia != null) {
					media.setFile(userSubmissionMedia.getFile());
					media.setLink(null);
				} else {
					media.setFile(null);
				}
			}
		}
	}

	private void addResource(ComponentAll componentAll, UserSubmissionField userSubmissionField) throws IOException
	{
		if (userSubmissionField != null) {

			List<ComponentResource> resourceRecords = objectMapper.readValue(userSubmissionField.getRawValue(), new TypeReference<List<ComponentResource>>()
			{
			});
			//clear ids
			for (ComponentResource resource : resourceRecords) {
				resource.setResourceId(null);
			}
			mapResource(userSubmissionField, resourceRecords);

			componentAll.getResources().addAll(resourceRecords);

		}

	}

	private void mapResource(UserSubmissionField userSubmissionField, List<ComponentResource> resources)
	{
		Map<String, UserSubmissionMedia> mediaMap = new HashMap<>();

		UserSubmissionMedia fieldMediaExample = new UserSubmissionMedia();
		fieldMediaExample.setTemplateFieldId(userSubmissionField.getTemplateFieldId());
		List<UserSubmissionMedia> fieldMedia = fieldMediaExample.findByExample();

		for (UserSubmissionMedia userSubmissionMedia : fieldMedia) {
			mediaMap.put(userSubmissionMedia.getFile().getMediaFileId(), userSubmissionMedia);
		}

		for (ComponentResource resource : resources) {
			if (resource.getFile() != null) {
				UserSubmissionMedia userSubmissionMedia = mediaMap.get(resource.getFile().getMediaFileId());
				if (userSubmissionMedia != null) {
					resource.setFile(userSubmissionMedia.getFile());
					resource.setLink(null);
				} else {
					resource.setFile(null);
				}
			}
		}
	}

	private void addTag(ComponentAll componentAll, UserSubmissionField userSubmissionField) throws IOException
	{
		if (userSubmissionField != null) {
			List<ComponentTag> tags = objectMapper.readValue(userSubmissionField.getRawValue(), new TypeReference<List<ComponentTag>>()
			{
			});
			//clear ids
			for (ComponentTag tag : tags) {
				tag.setTagId(null);
			}

			componentAll.getTags().addAll(tags);
		}
	}

	private void addRelationships(ComponentAll componentAll, UserSubmissionField userSubmissionField) throws IOException
	{
		if (userSubmissionField != null) {
			List<ComponentRelationship> relationships = objectMapper.readValue(userSubmissionField.getRawValue(), new TypeReference<List<ComponentRelationship>>()
			{
			});
			//clear ids
			for (ComponentRelationship relationship : relationships) {
				relationship.setComponentRelationshipId(null);
			}

			componentAll.getRelationships().addAll(relationships);
		}
	}

	@Override
	public UserSubmissionFieldMedia mapComponentToSubmission(SubmissionFormField submissionField, ComponentFormSet componentFormSet, SubmissionFormTemplate template) throws MappingException
	{
		UserSubmissionFieldMedia userSubmissionFieldMedia = new UserSubmissionFieldMedia();

		UserSubmissionField userSubmissionField = new UserSubmissionField();
		userSubmissionFieldMedia.setUserSubmissionField(userSubmissionField);

		userSubmissionField.setTemplateFieldId(submissionField.getFieldId());

		String fieldType = submissionField.getFieldType();
		try {
			switch (fieldType) {

				//this is a single attribute form
				case SubmissionFormFieldType.ATTRIBUTE:
					mapAttributes(userSubmissionField, componentFormSet);
					break;

				case SubmissionFormFieldType.ATTRIBUTE_REQUIRED:
					mapRequiredAttributes(userSubmissionField, componentFormSet, template);
					break;

				case SubmissionFormFieldType.ATTRIBUTE_SUGGESTED:
					mapOptionalAttributes(userSubmissionField, componentFormSet, template);
					break;

				//this includes optional minus any attributes are matched by other fields
				case SubmissionFormFieldType.ATTRIBUTE_MULTI:
					mapOptionalAttributes(userSubmissionField, componentFormSet, template);
					break;

				case SubmissionFormFieldType.ATTRIBUTE_SINGLE:
				case SubmissionFormFieldType.ATTRIBUTE_RADIO:
				case SubmissionFormFieldType.ATTRIBUTE_MULTI_CHECKBOX:
					mapSingleAttributes(userSubmissionField, componentFormSet, submissionField);
					break;

				case SubmissionFormFieldType.CONTACT:
				case SubmissionFormFieldType.CONTACT_MULTI:
					mapContacts(userSubmissionField, componentFormSet);
					break;

				case SubmissionFormFieldType.EXT_DEPENDENCY:
				case SubmissionFormFieldType.EXT_DEPENDENCY_MULTI:
					mapDependencies(userSubmissionField, componentFormSet);
					break;

				case SubmissionFormFieldType.MEDIA:
				case SubmissionFormFieldType.MEDIA_MULTI:
					userSubmissionFieldMedia.getMedia().addAll(mapMediaForForm(userSubmissionField, componentFormSet));
					break;

				case SubmissionFormFieldType.RESOURCE:
				case SubmissionFormFieldType.RESOURCE_SIMPLE:
				case SubmissionFormFieldType.RESOURCE_MULTI:
					userSubmissionFieldMedia.getMedia().addAll(mapResourcesForForm(userSubmissionField, componentFormSet));
					break;

				case SubmissionFormFieldType.TAG:
				case SubmissionFormFieldType.TAG_MULTI:
					mapTags(userSubmissionField, componentFormSet);
					break;

				case SubmissionFormFieldType.RELATIONSHIPS:
				case SubmissionFormFieldType.RELATIONSHIPS_MULTI:
					mapRelationships(userSubmissionField, componentFormSet);
					break;

				default:
					throw new UnsupportedOperationException(fieldType + " not supported");
			}
		} catch (IOException ioe) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "Field Mapping exception", ioe);
			}
			MappingException mappingException = new MappingException("Unable to mapping entry to form field");
			mappingException.setFieldLabel(submissionField.getLabel());
			mappingException.setFieldType(fieldType);
			mappingException.setMappingType(submissionField.getMappingType());
			throw mappingException;
		}

		return userSubmissionFieldMedia;
	}

	private void mapRequiredAttributes(UserSubmissionField userSubmissionField, ComponentFormSet componentFormSet, SubmissionFormTemplate template) throws JsonProcessingException
	{
		mapRestrictedAttributes(userSubmissionField, componentFormSet, template, true);
	}

	private void mapRestrictedAttributes(UserSubmissionField userSubmissionField, ComponentFormSet componentFormSet, SubmissionFormTemplate template, boolean required) throws JsonProcessingException
	{
		List<AttributeType> attributeTypes;
		if (required) {
			attributeTypes = ServiceProxyFactory.getServiceProxy().getAttributeService().findRequiredAttributes(componentFormSet.getPrimary().getComponent().getComponentType(), true, template.getSubmissionTemplateId());
		} else {
			attributeTypes = ServiceProxyFactory.getServiceProxy().getAttributeService().findOptionalAttributes(componentFormSet.getPrimary().getComponent().getComponentType(), true, template.getSubmissionTemplateId());
		}

		// get the attributes whose type matches any of the above attribute types.
		List<ComponentAttribute> completeList = componentFormSet.getPrimary().getAttributes();
		List<ComponentAttribute> reducedList = new ArrayList<>();

		for (ComponentAttribute componentAttribute : completeList) {
			for (AttributeType attributeType : attributeTypes) {
				if (attributeType.getAttributeType().equals(componentAttribute.getComponentAttributePk().getAttributeType())) {
					reducedList.add(componentAttribute);
				}
			}
		}
		// the preferredUnit gets set to a AttributeUnitView in rawValue
		// the preferredUnit should be a string not an object in rawValue
		// the front end will need to handle both cases (object and string) of preferredUnit
		String value = objectMapper.writeValueAsString(ComponentAttributeView.toViewList(reducedList)); // can't go backwards preferredUnit is a typeof String
		userSubmissionField.setRawValue(value);

	}

	private void mapSingleAttributes(UserSubmissionField userSubmissionField, ComponentFormSet componentFormSet, SubmissionFormField submissionField) throws JsonProcessingException
	{
		// get the attributes that match the type in submissionField and pass only those.
		List<ComponentAttribute> completeList = componentFormSet.getPrimary().getAttributes();
		List<ComponentAttribute> reducedList = new ArrayList<>();

		for (ComponentAttribute componentAttribute : completeList) {
			if (componentAttribute.getComponentAttributePk().getAttributeType().equals(submissionField.getAttributeType())) {
				reducedList.add(componentAttribute);
			}
		}

		String value = objectMapper.writeValueAsString(ComponentAttributeView.toViewList(reducedList));
		userSubmissionField.setRawValue(value);
	}

	private void mapOptionalAttributes(UserSubmissionField userSubmissionField, ComponentFormSet componentFormSet, SubmissionFormTemplate template) throws JsonProcessingException
	{
		mapRestrictedAttributes(userSubmissionField, componentFormSet, template, false);
	}

	private void mapAttributes(UserSubmissionField userSubmissionField, ComponentFormSet componentFormSet) throws JsonProcessingException
	{
		//This grab all?; Note this is currently used
		String value = objectMapper.writeValueAsString(ComponentAttributeView.toViewList(componentFormSet.getPrimary().getAttributes()));
		userSubmissionField.setRawValue(value);
	}

	private void mapContacts(UserSubmissionField userSubmissionField, ComponentFormSet componentFormSet) throws JsonProcessingException
	{
		List<ComponentContactView> views = ComponentContactView.toViewList(componentFormSet.getPrimary().getContacts());

		//remove ids that causes issue with change requests.
		for (ComponentContactView view : views) {
			view.setComponentContactId(null);
		}

		String value = objectMapper.writeValueAsString(views);
		userSubmissionField.setRawValue(value);
	}

	private void mapDependencies(UserSubmissionField userSubmissionField, ComponentFormSet componentFormSet) throws JsonProcessingException
	{
		List<ComponentExternalDependencyView> views = ComponentExternalDependencyView.toViewList(componentFormSet.getPrimary().getExternalDependencies());

		//remove ids that causes issue with change requests.
		for (ComponentExternalDependencyView view : views) {
			view.setDependencyId(null);
		}

		String value = objectMapper.writeValueAsString(views);
		userSubmissionField.setRawValue(value);
	}

	private List<UserSubmissionMedia> mapMediaForForm(UserSubmissionField userSubmissionField, ComponentFormSet componentFormSet) throws JsonProcessingException
	{
		List<UserSubmissionMedia> userSubmissionMediaRecords = new ArrayList<>();

		for (ComponentMedia media : componentFormSet.getPrimary().getMedia()) {
			if (media.getFile() != null) {
				UserSubmissionMedia userSubmissionMedia = new UserSubmissionMedia();
				userSubmissionMedia.setTemplateFieldId(userSubmissionField.getTemplateFieldId());
				userSubmissionMedia.setFile(media.getFile());
				userSubmissionMediaRecords.add(userSubmissionMedia);

				media.setFile(userSubmissionMedia.getFile());
			}
		}

		List<ComponentMediaView> views = ComponentMediaView.toViewList(componentFormSet.getPrimary().getMedia());
		//remove ids that causes issue with change requests.
		for (ComponentMediaView view : views) {
			view.setComponentMediaId(null);
		}

		String value = objectMapper.writeValueAsString(views);
		userSubmissionField.setRawValue(value);

		return userSubmissionMediaRecords;
	}

	private List<UserSubmissionMedia> mapResourcesForForm(UserSubmissionField userSubmissionField, ComponentFormSet componentFormSet) throws JsonProcessingException
	{
		List<UserSubmissionMedia> userSubmissionMediaRecords = new ArrayList<>();

		for (ComponentResource resource : componentFormSet.getPrimary().getResources()) {
			if (resource.getFile() != null) {
				UserSubmissionMedia userSubmissionMedia = new UserSubmissionMedia();
				userSubmissionMedia.setTemplateFieldId(userSubmissionField.getTemplateFieldId());
				userSubmissionMedia.setFile(resource.getFile());
				userSubmissionMediaRecords.add(userSubmissionMedia);

				resource.setFile(userSubmissionMedia.getFile());
			}
		}
		List<ComponentResourceView> views = ComponentResourceView.toViewList(componentFormSet.getPrimary().getResources());
		for (ComponentResourceView view : views) {
			view.setResourceId(null);
		}

		String value = objectMapper.writeValueAsString(views);
		userSubmissionField.setRawValue(value);

		return userSubmissionMediaRecords;
	}

	private void mapTags(UserSubmissionField userSubmissionField, ComponentFormSet componentFormSet) throws JsonProcessingException
	{
		List<ComponentTag> existTags = componentFormSet.getPrimary().getTags();

		//avoid change the original
		List<ComponentTag> copyTags = new ArrayList<>();
		for (ComponentTag tag : existTags) {
			ComponentTag newTag = new ComponentTag();
			newTag.setText(tag.getText());
			copyTags.add(tag);
		}

		String value = objectMapper.writeValueAsString(copyTags);
		userSubmissionField.setRawValue(value);
	}

	private void mapRelationships(UserSubmissionField userSubmissionField, ComponentFormSet componentFormSet) throws JsonProcessingException
	{
		List<ComponentRelationshipView> views = ComponentRelationshipView.toViewList(componentFormSet.getPrimary().getRelationships());
		for (ComponentRelationshipView view : views) {
			view.setRelationshipId(null);
		}

		String value = objectMapper.writeValueAsString(views);
		userSubmissionField.setRawValue(value);
	}

}

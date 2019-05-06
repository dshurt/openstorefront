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
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.usu.sdl.openstorefront.common.util.StringProcessor;
import edu.usu.sdl.openstorefront.core.api.ContactService;
import edu.usu.sdl.openstorefront.core.api.LookupService;
import edu.usu.sdl.openstorefront.core.api.PersistenceService;
import edu.usu.sdl.openstorefront.core.api.Service;
import edu.usu.sdl.openstorefront.core.api.ServiceProxyFactory;
import edu.usu.sdl.openstorefront.core.entity.ApprovalStatus;
import edu.usu.sdl.openstorefront.core.entity.Component;
import edu.usu.sdl.openstorefront.core.entity.ComponentContact;
import edu.usu.sdl.openstorefront.core.entity.ComponentType;
import edu.usu.sdl.openstorefront.core.entity.Contact;
import edu.usu.sdl.openstorefront.core.entity.SubmissionFormField;
import edu.usu.sdl.openstorefront.core.entity.SubmissionFormFieldMappingType;
import edu.usu.sdl.openstorefront.core.entity.SubmissionFormFieldType;
import edu.usu.sdl.openstorefront.core.entity.SubmissionFormSection;
import edu.usu.sdl.openstorefront.core.entity.SubmissionFormTemplate;
import edu.usu.sdl.openstorefront.core.entity.UserSubmission;
import edu.usu.sdl.openstorefront.core.entity.UserSubmissionField;
import edu.usu.sdl.openstorefront.core.model.ComponentAll;
import edu.usu.sdl.openstorefront.core.model.ComponentFormSet;
import edu.usu.sdl.openstorefront.core.model.UserSubmissionAll;
import edu.usu.sdl.openstorefront.validation.ValidationResult;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 * @author dshurtleff
 */
public class MappingControllerTest
{

	private static final String TEST = "TEST";

	public MappingControllerTest()
	{
	}

	/**
	 * Test of verifyTemplate method, of class MappingController.
	 */
	@Test
	public void testVerifyTemplateWithSectionsInvalid()
	{
		setupMockService();

		SubmissionFormTemplate template = new SubmissionFormTemplate();
		template.setSections(new ArrayList<>());
		template.getSections().add(new SubmissionFormSection());

		MapperFactory mockMapperFactory = getMockMapperFactory();
		MappingController instance = new MappingController(mockMapperFactory);
		ValidationResult result = instance.verifyTemplate(template, TEST);
		assertFalse(result.valid());
	}

	private Service setupMockService()
	{
		Service mockService = Mockito.mock(Service.class);

		PersistenceService persistenceService = Mockito.mock(PersistenceService.class);
		Mockito.when(mockService.getPersistenceService()).thenReturn(persistenceService);
		ComponentType testComponentType = new ComponentType();
		testComponentType.setComponentType(TEST);
		testComponentType.setLabel(TEST);
		Mockito.when(persistenceService.findById(ComponentType.class, TEST)).thenReturn(testComponentType);

		LookupService lookupService = Mockito.mock(LookupService.class);
		Mockito.when(mockService.getLookupService()).thenReturn(lookupService);

		ApprovalStatus approvalStatus = new ApprovalStatus();
		Mockito.when(lookupService.findLookup(ApprovalStatus.class, null)).thenReturn(approvalStatus.systemValues());

		ServiceProxyFactory.setTestService(mockService);
		return mockService;
	}

	private MapperFactory getMockMapperFactory()
	{
		MapperFactory mockMapperFactory = new MapperFactory()
		{
			@Override
			public BaseMapper getMapperForField(String mappingType)
			{
				return new BaseMapper()
				{
					@Override
					public List<ComponentAll> mapField(ComponentAll componentAll, SubmissionFormField submissionField, UserSubmissionField userSubmissionField)
					{
						throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
					}

					@Override
					public UserSubmissionFieldMedia mapComponentToSubmission(SubmissionFormField submissionField, ComponentFormSet componentFormSet, SubmissionFormTemplate template) throws MappingException
					{
						throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
					}

				};
			}

		};
		return mockMapperFactory;
	}

	@Test(expected = NullPointerException.class)
	public void testVerifyTemplateNullTemplate()
	{
		MappingController instance = new MappingController();
		instance.verifyTemplate(null, "Test");
	}

	@Test
	public void testVerifyTemplateWithoutSectionsInvalid()
	{
		setupMockService();

		SubmissionFormTemplate template = new SubmissionFormTemplate();

		MapperFactory mockMapperFactory = getMockMapperFactory();
		MappingController instance = new MappingController(mockMapperFactory);
		ValidationResult result = instance.verifyTemplate(template, TEST);
		assertFalse(result.valid());
	}

	@Test
	public void testVerifyTemplateWithFieldsInvalid()
	{
		setupMockService();

		SubmissionFormTemplate template = new SubmissionFormTemplate();
		template.setSections(new ArrayList<>());
		SubmissionFormSection section = new SubmissionFormSection();
		section.setFields(new ArrayList<>());

		SubmissionFormField submissionFormField = new SubmissionFormField();
		submissionFormField.setMappingType(SubmissionFormFieldMappingType.COMPONENT);
		section.getFields().add(submissionFormField);

		template.getSections().add(new SubmissionFormSection());

		MapperFactory mockMapperFactory = getMockMapperFactory();
		MappingController instance = new MappingController(mockMapperFactory);
		ValidationResult result = instance.verifyTemplate(template, TEST);
		assertFalse(result.valid());
	}

	@Test
	public void testVerifyTemplateWithFieldsValid()
	{
		setupMockService();

		SubmissionFormTemplate template = new SubmissionFormTemplate();
		template.setSections(new ArrayList<>());
		SubmissionFormSection section = new SubmissionFormSection();
		section.setFields(new ArrayList<>());

		SubmissionFormField submissionFormField = new SubmissionFormField();
		submissionFormField.setMappingType(SubmissionFormFieldMappingType.COMPONENT);
		submissionFormField.setFieldName(Component.FIELD_NAME);

		section.getFields().add(submissionFormField);

		submissionFormField = new SubmissionFormField();
		submissionFormField.setMappingType(SubmissionFormFieldMappingType.COMPONENT);
		submissionFormField.setFieldName(Component.FIELD_ORGANIZATION);
		section.getFields().add(submissionFormField);

		submissionFormField = new SubmissionFormField();
		submissionFormField.setMappingType(SubmissionFormFieldMappingType.COMPONENT);
		submissionFormField.setFieldName(Component.FIELD_DESCRIPTION);
		section.getFields().add(submissionFormField);

		template.getSections().add(section);

		MappingController instance = new MappingController();
		ValidationResult result = instance.verifyTemplate(template, TEST);
		assertTrue(result.valid());
	}

	@Test
	public void testUserSubmissionMapToEntryValid() throws MappingException
	{
		setupMockService();

		SubmissionFormTemplate template = new SubmissionFormTemplate();
		template.setSections(new ArrayList<>());
		SubmissionFormSection section = new SubmissionFormSection();
		section.setFields(new ArrayList<>());

		SubmissionFormField submissionFormField = new SubmissionFormField();
		submissionFormField.setMappingType(SubmissionFormFieldMappingType.COMPONENT);
		submissionFormField.setFieldName(Component.FIELD_NAME);
		submissionFormField.setFieldId("NAME");
		section.getFields().add(submissionFormField);

		submissionFormField = new SubmissionFormField();
		submissionFormField.setMappingType(SubmissionFormFieldMappingType.COMPONENT);
		submissionFormField.setFieldName(Component.FIELD_ORGANIZATION);
		submissionFormField.setFieldId("ORG");
		section.getFields().add(submissionFormField);

		submissionFormField = new SubmissionFormField();
		submissionFormField.setMappingType(SubmissionFormFieldMappingType.COMPONENT);
		submissionFormField.setFieldName(Component.FIELD_DESCRIPTION);
		submissionFormField.setFieldId("DESCR");
		section.getFields().add(submissionFormField);

		template.getSections().add(section);

		MappingController instance = new MappingController();

		UserSubmission userSubmission = new UserSubmission();
		userSubmission.setComponentType(TEST);
		userSubmission.setFields(new ArrayList<>());

		UserSubmissionField field = new UserSubmissionField();
		field.setTemplateFieldId("NAME");
		field.setRawValue("Apple");
		userSubmission.getFields().add(field);

		field = new UserSubmissionField();
		field.setTemplateFieldId("ORG");
		field.setRawValue("Org-Test");
		userSubmission.getFields().add(field);

		field = new UserSubmissionField();
		field.setTemplateFieldId("DESCR");
		field.setRawValue("Org-Test");
		userSubmission.getFields().add(field);

		ComponentFormSet componentFormSet = instance.mapUserSubmissionToEntry(template, userSubmission);
		assertTrue(componentFormSet.getPrimary().getComponent().getName().equals("Apple"));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testUserSubmissionMapToEntryInvalidForm() throws MappingException
	{
		setupMockService();

		SubmissionFormTemplate template = new SubmissionFormTemplate();
		template.setSections(new ArrayList<>());
		SubmissionFormSection section = new SubmissionFormSection();
		section.setFields(new ArrayList<>());

		SubmissionFormField submissionFormField = new SubmissionFormField();
		submissionFormField.setMappingType(SubmissionFormFieldMappingType.COMPONENT);
		submissionFormField.setFieldName(Component.FIELD_NAME);
		submissionFormField.setFieldId("NAME");
		section.getFields().add(submissionFormField);

		submissionFormField = new SubmissionFormField();
		submissionFormField.setMappingType(SubmissionFormFieldMappingType.COMPONENT);
		submissionFormField.setFieldName(Component.FIELD_ORGANIZATION);
		submissionFormField.setFieldId("ORG");
		section.getFields().add(submissionFormField);

		submissionFormField = new SubmissionFormField();
		submissionFormField.setMappingType(SubmissionFormFieldMappingType.COMPLEX);
		submissionFormField.setFieldType("BADFIELDTYPE");
		submissionFormField.setFieldId("DESCR");
		section.getFields().add(submissionFormField);

		template.getSections().add(section);

		MappingController instance = new MappingController();

		UserSubmission userSubmission = new UserSubmission();
		userSubmission.setComponentType(TEST);
		userSubmission.setFields(new ArrayList<>());

		UserSubmissionField field = new UserSubmissionField();
		field.setTemplateFieldId("NAME");
		field.setRawValue("Apple");
		userSubmission.getFields().add(field);

		field = new UserSubmissionField();
		field.setTemplateFieldId("ORG");
		field.setRawValue("Org-Test");
		userSubmission.getFields().add(field);

		field = new UserSubmissionField();
		field.setTemplateFieldId("DESCR");
		field.setRawValue("Org-Test");
		userSubmission.getFields().add(field);

		ComponentFormSet componentFormSet = instance.mapUserSubmissionToEntry(template, userSubmission);
		System.out.println(componentFormSet);

	}

	@Test
	public void testUserSubmissionMapToEntryValidComplex() throws MappingException, JsonProcessingException
	{
		setupMockService();

		SubmissionFormTemplate template = new SubmissionFormTemplate();
		template.setSections(new ArrayList<>());
		SubmissionFormSection section = new SubmissionFormSection();
		section.setFields(new ArrayList<>());

		SubmissionFormField submissionFormField = new SubmissionFormField();
		submissionFormField.setMappingType(SubmissionFormFieldMappingType.COMPONENT);
		submissionFormField.setFieldName(Component.FIELD_NAME);
		submissionFormField.setFieldId("NAME");
		section.getFields().add(submissionFormField);

		submissionFormField = new SubmissionFormField();
		submissionFormField.setMappingType(SubmissionFormFieldMappingType.COMPONENT);
		submissionFormField.setFieldName(Component.FIELD_ORGANIZATION);
		submissionFormField.setFieldId("ORG");
		section.getFields().add(submissionFormField);

		submissionFormField = new SubmissionFormField();
		submissionFormField.setMappingType(SubmissionFormFieldMappingType.COMPONENT);
		submissionFormField.setFieldName(Component.FIELD_DESCRIPTION);
		submissionFormField.setFieldId("DESCR");
		section.getFields().add(submissionFormField);

		submissionFormField = new SubmissionFormField();
		submissionFormField.setMappingType(SubmissionFormFieldMappingType.COMPLEX);
		submissionFormField.setFieldType(SubmissionFormFieldType.CONTACT);
		submissionFormField.setFieldId("CONTACT");
		section.getFields().add(submissionFormField);

		template.getSections().add(section);

		MappingController instance = new MappingController();

		UserSubmission userSubmission = new UserSubmission();
		userSubmission.setComponentType(TEST);
		userSubmission.setFields(new ArrayList<>());

		UserSubmissionField field = new UserSubmissionField();
		field.setTemplateFieldId("NAME");
		field.setRawValue("Apple");
		userSubmission.getFields().add(field);

		field = new UserSubmissionField();
		field.setTemplateFieldId("ORG");
		field.setRawValue("Org-Test");
		userSubmission.getFields().add(field);

		field = new UserSubmissionField();
		field.setTemplateFieldId("DESCR");
		field.setRawValue("Org-Test");
		userSubmission.getFields().add(field);

		field = new UserSubmissionField();
		field.setTemplateFieldId("CONTACT");

		ComponentContact contact = new ComponentContact();
		contact.setFirstName("Bob");
		contact.setLastName("test");
		contact.setEmail("bob@test.com");

		List<ComponentContact> allContacts = new ArrayList<>();
		allContacts.add(contact);
		ObjectMapper objectMapper = StringProcessor.defaultObjectMapper();
		String contactJson = objectMapper.writeValueAsString(allContacts);

		field.setRawValue(contactJson);
		userSubmission.getFields().add(field);

		ComponentFormSet componentFormSet = instance.mapUserSubmissionToEntry(template, userSubmission);
		assertTrue(componentFormSet.getPrimary().getContacts().get(0).getFirstName().equals("Bob"));
	}

	@Test
	public void testEntryToUserSubmissionMapValid() throws MappingException
	{
		Service mockService = setupMockService();

		ContactService contactService = Mockito.mock(ContactService.class);
		Mockito.when(mockService.getContactService()).thenReturn(contactService);
		Contact contact = new Contact();
		contact.setFirstName("Bob");
		contact.setLastName("test");
		contact.setEmail("bob@test.com");
		Mockito.when(contactService.findContact(Mockito.anyString())).thenReturn(contact);

		MappingController instance = new MappingController();

		SubmissionFormTemplate template = new SubmissionFormTemplate();
		template.setSections(new ArrayList<>());
		SubmissionFormSection section = new SubmissionFormSection();
		section.setFields(new ArrayList<>());

		SubmissionFormField submissionFormField = new SubmissionFormField();
		submissionFormField.setMappingType(SubmissionFormFieldMappingType.COMPONENT);
		submissionFormField.setFieldName(Component.FIELD_NAME);
		submissionFormField.setFieldId("NAME");
		section.getFields().add(submissionFormField);

		submissionFormField = new SubmissionFormField();
		submissionFormField.setMappingType(SubmissionFormFieldMappingType.COMPONENT);
		submissionFormField.setFieldName(Component.FIELD_ORGANIZATION);
		submissionFormField.setFieldId("ORG");
		section.getFields().add(submissionFormField);

		submissionFormField = new SubmissionFormField();
		submissionFormField.setMappingType(SubmissionFormFieldMappingType.COMPONENT);
		submissionFormField.setFieldName(Component.FIELD_DESCRIPTION);
		submissionFormField.setFieldId("DESCR");
		section.getFields().add(submissionFormField);

		submissionFormField = new SubmissionFormField();
		submissionFormField.setMappingType(SubmissionFormFieldMappingType.COMPLEX);
		submissionFormField.setFieldType(SubmissionFormFieldType.CONTACT);
		submissionFormField.setFieldId("CONTACT");
		section.getFields().add(submissionFormField);

		template.getSections().add(section);

		ComponentFormSet componentFormSet = new ComponentFormSet();
		ComponentAll componentAll = new ComponentAll();
		Component component = new Component();
		component.setComponentId("1");
		component.setComponentType(TEST);
		component.setName("Apple");
		component.setOrganization("Org");
		component.setDescription("description");
		componentAll.setComponent(component);

		ComponentContact componentContact = new ComponentContact();
		componentContact.setFirstName("Bob");
		componentContact.setLastName("test");
		componentContact.setEmail("bob@test.com");
		componentAll.getContacts().add(componentContact);
		componentFormSet.setPrimary(componentAll);

		UserSubmissionAll userSubmissionAll = instance.mapEntriesToUserSubmission(template, componentFormSet);

		assertTrue(userSubmissionAll.getUserSubmission().getFields().size() == 4);
		assertTrue(userSubmissionAll.getUserSubmission().getFields().get(0).getRawValue().equals("Apple"));

	}

	@Test(expected = MappingException.class)
	public void testEntryToUserSubmissionMapInValid() throws MappingException
	{
		setupMockService();
		MappingController instance = new MappingController();

		SubmissionFormTemplate template = new SubmissionFormTemplate();
		template.setSections(new ArrayList<>());
		SubmissionFormSection section = new SubmissionFormSection();
		section.setFields(new ArrayList<>());

		SubmissionFormField submissionFormField = new SubmissionFormField();
		submissionFormField.setMappingType(SubmissionFormFieldMappingType.COMPONENT);
		submissionFormField.setFieldName(Component.FIELD_NAME);
		submissionFormField.setFieldId("NAME");
		section.getFields().add(submissionFormField);

		submissionFormField = new SubmissionFormField();
		submissionFormField.setMappingType(SubmissionFormFieldMappingType.COMPONENT);
		submissionFormField.setFieldName(Component.FIELD_ORGANIZATION);
		submissionFormField.setFieldId("ORG");
		section.getFields().add(submissionFormField);

		submissionFormField = new SubmissionFormField();
		submissionFormField.setMappingType(SubmissionFormFieldMappingType.COMPONENT);
		submissionFormField.setFieldName("badfield");
		submissionFormField.setFieldId("DESCR");
		section.getFields().add(submissionFormField);

		submissionFormField = new SubmissionFormField();
		submissionFormField.setMappingType(SubmissionFormFieldMappingType.COMPLEX);
		submissionFormField.setFieldType(SubmissionFormFieldType.CONTACT);
		submissionFormField.setFieldId("CONTACT");
		section.getFields().add(submissionFormField);

		template.getSections().add(section);

		ComponentFormSet componentFormSet = new ComponentFormSet();
		ComponentAll componentAll = new ComponentAll();
		Component component = new Component();
		component.setComponentId("1");
		component.setComponentType(TEST);
		component.setName("Apple");
		component.setOrganization("Org");
		component.setDescription("description");
		componentAll.setComponent(component);

		ComponentContact componentContact = new ComponentContact();
		componentContact.setFirstName("Bob");
		componentContact.setLastName("test");
		componentContact.setEmail("bob@test.com");
		componentAll.getContacts().add(componentContact);
		componentFormSet.setPrimary(componentAll);

		UserSubmissionAll userSubmissionAll = instance.mapEntriesToUserSubmission(template, componentFormSet);

	}

}

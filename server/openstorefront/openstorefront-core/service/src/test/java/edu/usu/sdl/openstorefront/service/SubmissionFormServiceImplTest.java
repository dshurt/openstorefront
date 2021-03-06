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
package edu.usu.sdl.openstorefront.service;

import edu.usu.sdl.openstorefront.common.manager.FileSystemManager;
import edu.usu.sdl.openstorefront.common.util.OpenStorefrontConstant;
import edu.usu.sdl.openstorefront.common.util.StringProcessor;
import edu.usu.sdl.openstorefront.core.api.ComponentService;
import edu.usu.sdl.openstorefront.core.api.PersistenceService;
import edu.usu.sdl.openstorefront.core.api.SecurityService;
import edu.usu.sdl.openstorefront.core.api.Service;
import edu.usu.sdl.openstorefront.core.api.ServiceProxyFactory;
import edu.usu.sdl.openstorefront.core.api.WorkPlanService;
import edu.usu.sdl.openstorefront.core.api.query.QueryByExample;
import edu.usu.sdl.openstorefront.core.entity.BaseEntity;
import edu.usu.sdl.openstorefront.core.entity.ComponentType;
import edu.usu.sdl.openstorefront.core.entity.MediaFile;
import edu.usu.sdl.openstorefront.core.entity.SubmissionFormTemplate;
import edu.usu.sdl.openstorefront.core.entity.SubmissionTemplateStatus;
import edu.usu.sdl.openstorefront.core.entity.UserProfile;
import edu.usu.sdl.openstorefront.core.entity.UserSubmission;
import edu.usu.sdl.openstorefront.core.entity.UserSubmissionMedia;
import edu.usu.sdl.openstorefront.core.util.MediaFileType;
import edu.usu.sdl.openstorefront.security.UserContext;
import edu.usu.sdl.openstorefront.service.mapping.MappingController;
import edu.usu.sdl.openstorefront.validation.RuleResult;
import edu.usu.sdl.openstorefront.validation.ValidationResult;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 * @author dshurtleff
 */
public class SubmissionFormServiceImplTest
{

	private static final Logger LOG = Logger.getLogger(PersistUseCase.class.getName());

	private static final String URL = "memory:test";
	private static String testDir;

	public SubmissionFormServiceImplTest()
	{
	}

	@BeforeClass
	public static void setUpClass()
	{
		if (FileSystemManager.SYSTEM_TEMP_DIR.endsWith(File.separator)) {
			testDir = FileSystemManager.SYSTEM_TEMP_DIR + "osf-" + StringProcessor.uniqueId();
		} else {
			testDir = FileSystemManager.SYSTEM_TEMP_DIR + File.separator + "osf-" + StringProcessor.uniqueId();
		}

		FileSystemManager testFileSystemManager = FileSystemManager.getInstance();
		testFileSystemManager.setBaseDirectory(testDir);
	}

	@AfterClass
	public static void tearDownClass()
	{
		LOG.info("Clean up temp data folder");
		File file = new File(testDir);
		if (file.exists()) {
			try {
				FileUtils.deleteDirectory(file);
			} catch (IOException ex) {
				LOG.log(Level.WARNING, "Unable to delete temp directory", ex);
			}
		}
	}

	/**
	 * Test of saveSubmissionFormTemplate method, of class
	 * SubmissionFormServiceImpl.
	 */
	@Test
	public void testSaveSubmissionFormTemplateInvalid()
	{
		System.out.println("testSaveSubmissionFormTemplateInvalid");
		handleSaveSubmissionTemplateTest(SubmissionTemplateStatus.INCOMPLETE, true);
	}

	@Test
	public void testSaveSubmissionFormTemplateValid()
	{
		System.out.println("testSaveSubmissionFormTemplateValid");
		handleSaveSubmissionTemplateTest(SubmissionTemplateStatus.PENDING_VERIFICATION, false);
	}

	private void handleSaveSubmissionTemplateTest(String expected, boolean addError)
	{
		Service mockService = Mockito.mock(Service.class);
		SecurityService mockSecurityService = Mockito.mock(SecurityService.class);
		Mockito.when(mockService.getSecurityService()).thenReturn(mockSecurityService);

		UserContext userContext = new UserContext();
		userContext.setGuest(true);
		userContext.setUserProfile(new UserProfile());
		userContext.getUserProfile().setActiveStatus(UserProfile.INACTIVE_STATUS);
		userContext.getUserProfile().setFirstName("Guest");
		userContext.getUserProfile().setUsername(OpenStorefrontConstant.ANONYMOUS_USER);
		Mockito.when(mockSecurityService.getGuestContext()).thenReturn(userContext);

		ServiceProxyFactory.setTestService(mockService);

		MappingController mappingController = Mockito.mock(MappingController.class);
		ValidationResult validationResult = new ValidationResult();
		if (addError) {
			RuleResult ruleResult = new RuleResult();
			ruleResult.setFieldName("Test");
			ruleResult.setMessage("Fail");
			validationResult.getRuleResults().add(ruleResult);
		}

		Mockito.when(mappingController.verifyTemplate(Mockito.any(), Mockito.any())).thenReturn(validationResult);

		SubmissionFormTemplate template = new SubmissionFormTemplate();
		template.setSubmissionTemplateId("1");
		template.setName("Test");
		template.setDescription("This is a test");

		PersistenceService persistenceService = Mockito.mock(PersistenceService.class);
		Mockito.when(persistenceService.persist(Mockito.any())).thenReturn(template);
		Mockito.when(persistenceService.unwrapProxyObject(Mockito.any())).thenReturn(template);

		ComponentService mockComponentService = Mockito.mock(ComponentService.class);

		List<ComponentType> componentTypes = new ArrayList<>();
		ComponentType componentType = new ComponentType();
		componentType.setComponentType("TEST");
		componentTypes.add(componentType);
		Mockito.when(mockComponentService.getAllComponentTypes()).thenReturn(componentTypes);

		SubmissionFormServiceImpl instance = new SubmissionFormServiceImpl(persistenceService)
		{
			@Override
			public ComponentService getComponentService()
			{
				return mockComponentService;
			}
		};

		instance.setMappingController(mappingController);
		String expResult = expected;
		SubmissionFormTemplate result = instance.saveSubmissionFormTemplate(template);

		assertEquals(expResult, result.getTemplateStatus());
	}

	/**
	 * Test of deleteSubmissionFormTemplate method, of class
	 * SubmissionFormServiceImpl.
	 */
	@Test
	public void testDeleteSubmissionFormTemplate()
	{
		System.out.println("deleteSubmissionFormTemplate - no resources");

		String templateId = "1";

		SubmissionFormTemplate template = new SubmissionFormTemplate();
		template.setName("Test");
		template.setDescription("This is a test");

		PersistenceService persistenceService = Mockito.mock(PersistenceService.class);
		Mockito.when(persistenceService.findById(SubmissionFormTemplate.class, "1")).thenReturn(template);
		Mockito.when(persistenceService.queryByExample(Mockito.any(BaseEntity.class))).thenReturn(new ArrayList<>());

		Service testService = Mockito.mock(Service.class);
		Mockito.when(testService.getPersistenceService()).thenReturn(persistenceService);
		ServiceProxyFactory.setTestService(testService);

		SubmissionFormServiceImpl instance = new SubmissionFormServiceImpl(persistenceService);
		instance.deleteSubmissionFormTemplate(templateId);

		//Expected to not throw an error
	}

	@Test
	public void testDeleteUserSubmissionNoMedia() throws IOException
	{
		PersistenceService persistenceService = Mockito.mock(PersistenceService.class);
		WorkPlanService workPlanService = Mockito.mock(WorkPlanService.class);

		Service testService = Mockito.mock(Service.class);

		Mockito.when(testService.getWorkPlanService()).thenReturn(workPlanService);

		Mockito.when(testService.getPersistenceService()).thenReturn(persistenceService);
		Mockito.when(persistenceService.queryByExample(Mockito.any(QueryByExample.class))).thenReturn(new ArrayList<>());
		ServiceProxyFactory.setTestService(testService);

		UserSubmission userSubmission = new UserSubmission();
		Mockito.when(persistenceService.findById(UserSubmission.class, "1")).thenReturn(userSubmission);

		SubmissionFormServiceImpl instance = Mockito.spy(SubmissionFormServiceImpl.class);
		instance.setPersistenceService(persistenceService);
		Mockito.when(instance.getWorkPlanService()).thenReturn(workPlanService);
		//Mockito.when(instance.deleteUserSubmission("1"))

		instance.deleteUserSubmission("1");

	}

	@Test
	public void testDeleteUserSubmissionMedia() throws IOException
	{
		PersistenceService persistenceService = Mockito.mock(PersistenceService.class);

		UserSubmissionMedia media = new UserSubmissionMedia();
		MediaFile mediaFile = new MediaFile();
		mediaFile.setFileType(MediaFileType.MEDIA);
		mediaFile.setFileName("testmedia.txt");
		media.setFile(mediaFile);

		Path path = mediaFile.path();
		Files.createFile(path);

		Mockito.when(persistenceService.findById(UserSubmissionMedia.class, "1")).thenReturn(media);

		SubmissionFormServiceImpl instance = new SubmissionFormServiceImpl(persistenceService);
		instance.deleteUserSubmissionMedia("1");

		path = mediaFile.path();
		assertTrue("Media should be deleted", !path.toFile().exists());

	}

}

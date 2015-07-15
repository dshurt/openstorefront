/*
 * Copyright 2015 Space Dynamics Laboratory - Utah State University Research Foundation.
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
package edu.usu.sdl.openstorefront.storage.model;

import edu.usu.sdl.openstorefront.doc.ConsumeField;
import edu.usu.sdl.openstorefront.doc.DataType;
import edu.usu.sdl.openstorefront.doc.ValidValueType;
import edu.usu.sdl.openstorefront.util.OpenStorefrontConstant;
import edu.usu.sdl.openstorefront.util.PK;
import edu.usu.sdl.openstorefront.validation.Sanitize;
import edu.usu.sdl.openstorefront.validation.TextSanitizer;
import java.util.List;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author dshurtleff
 */
public class Alert
		extends StandardEntity<Alert>
{

	@PK(generated = true)
	@NotNull
	private String alertId;

	@NotNull
	@ConsumeField
	@Size(min = 1, max = OpenStorefrontConstant.FIELD_SIZE_CODE)
	@ValidValueType(value = {}, lookupClass = AlertType.class)
	private String alertType;

	@NotNull
	@Size(min = 1, max = OpenStorefrontConstant.FIELD_SIZE_GENERAL_TEXT)
	@Sanitize(TextSanitizer.class)
	@ConsumeField
	private String name;

	@DataType(EmailAddress.class)
	@ConsumeField
	@OneToMany(orphanRemoval = true)
	private List<EmailAddress> emailAddresses;

	@ConsumeField
	@OneToOne(orphanRemoval = true)
	private UserDataAlertOption userDataAlertOption;

	@ConsumeField
	@OneToOne(orphanRemoval = true)
	private SystemErrorAlertOption systemErrorAlertOption;

	public Alert()
	{
	}

	public String getAlertId()
	{
		return alertId;
	}

	public void setAlertId(String alertId)
	{
		this.alertId = alertId;
	}

	public String getAlertType()
	{
		return alertType;
	}

	public void setAlertType(String alertType)
	{
		this.alertType = alertType;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public List<EmailAddress> getEmailAddresses()
	{
		return emailAddresses;
	}

	public void setEmailAddresses(List<EmailAddress> emailAddresses)
	{
		this.emailAddresses = emailAddresses;
	}

	public UserDataAlertOption getUserDataAlertOption()
	{
		return userDataAlertOption;
	}

	public void setUserDataAlertOption(UserDataAlertOption userDataAlertOption)
	{
		this.userDataAlertOption = userDataAlertOption;
	}

	public SystemErrorAlertOption getSystemErrorAlertOption()
	{
		return systemErrorAlertOption;
	}

	public void setSystemErrorAlertOption(SystemErrorAlertOption systemErrorAlertOption)
	{
		this.systemErrorAlertOption = systemErrorAlertOption;
	}

}
/*
 * Copyright 2017 Space Dynamics Laboratory - Utah State University Research Foundation.
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
package edu.usu.sdl.openstorefront.report.output;

import edu.usu.sdl.openstorefront.common.manager.PropertiesManager;
import edu.usu.sdl.openstorefront.common.util.Convert;
import edu.usu.sdl.openstorefront.core.entity.Report;
import edu.usu.sdl.openstorefront.core.entity.ReportOutput;
import edu.usu.sdl.openstorefront.core.entity.ReportType;
import edu.usu.sdl.openstorefront.core.util.TranslateUtil;
import edu.usu.sdl.openstorefront.report.BaseReport;
import edu.usu.sdl.openstorefront.report.generator.BaseGenerator;
import edu.usu.sdl.openstorefront.report.model.BaseReportModel;
import edu.usu.sdl.openstorefront.security.UserContext;
import edu.usu.sdl.openstorefront.service.manager.MailManager;
import javax.mail.Message;
import org.codemonkey.simplejavamail.email.Email;

/**
 * Normal view/save report
 *
 * @author dshurtleff
 */
public class ViewOutput
		extends BaseOutput
{
	
	public ViewOutput(ReportOutput reportOutput, Report report, BaseReport reportGenerator, UserContext userContext)
	{
		super(reportOutput, report, reportGenerator, userContext);
	}

	@Override
	protected BaseGenerator init()
	{
		BaseGenerator generator = getBaseGenerator();
		return generator;
	}

	@Override
	protected void finishOutput(BaseReportModel reportModel)
	{
		// if the user indicated they wanted to be notified on report completion, send the email.
		if (Convert.toBoolean(reportOutput.getReportTransmissionOption().getReportNotify())) {
			String applicationTitle = PropertiesManager.getInstance().getValue(PropertiesManager.KEY_APPLICATION_TITLE, "Openstorefront");
			String message = reportGenerator.reportSummmaryDefault(reportModel);

			Email email = MailManager.newEmail();
			email.setSubject(applicationTitle + " - " + TranslateUtil.translate(ReportType.class, report.getReportType()) + " Report");
			email.setTextHTML(message);

			email.addRecipient("", userContext.getUserProfile().getEmail(), Message.RecipientType.TO);
			MailManager.send(email, true);
		}
	}

}

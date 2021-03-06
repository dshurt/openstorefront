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
package edu.usu.sdl.openstorefront.report;

import edu.usu.sdl.openstorefront.common.util.TimeUtil;
import edu.usu.sdl.openstorefront.core.entity.ComponentComment;
import edu.usu.sdl.openstorefront.core.entity.ComponentCommentType;
import edu.usu.sdl.openstorefront.core.entity.Evaluation;
import edu.usu.sdl.openstorefront.core.entity.Report;
import edu.usu.sdl.openstorefront.core.entity.ReportFormat;
import edu.usu.sdl.openstorefront.core.entity.ReportTransmissionType;
import edu.usu.sdl.openstorefront.core.entity.UserSubmission;
import edu.usu.sdl.openstorefront.core.entity.WorkPlan;
import edu.usu.sdl.openstorefront.core.entity.WorkPlanLink;
import edu.usu.sdl.openstorefront.core.entity.WorkPlanLinkType;
import edu.usu.sdl.openstorefront.core.entity.WorkPlanStep;
import edu.usu.sdl.openstorefront.report.generator.BaseGenerator;
import edu.usu.sdl.openstorefront.report.generator.CSVGenerator;
import edu.usu.sdl.openstorefront.report.model.BaseReportModel;
import edu.usu.sdl.openstorefront.report.model.WorkPlanStatusLineModel;
import edu.usu.sdl.openstorefront.report.model.WorkPlanStatusReportModel;
import edu.usu.sdl.openstorefront.report.output.ReportWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author dshurtleff
 */
public class WorkPlanStatusReport
		extends BaseReport
{

	public WorkPlanStatusReport(Report report)
	{
		super(report);
	}

	@Override
	protected BaseReportModel gatherData()
	{
		WorkPlanStatusReportModel reportModel = new WorkPlanStatusReportModel();
		reportModel.setTitle("WorkPlan Status");

		WorkPlanLink workPlanLinkExample = new WorkPlanLink();
		workPlanLinkExample.setActiveStatus(WorkPlanLink.ACTIVE_STATUS);
		List<WorkPlanLink> links = workPlanLinkExample.findByExample();

		Map<String, WorkPlan> workPlanMap = new HashMap<>();
		for (WorkPlanLink link : links) {
			WorkPlanStatusLineModel lineModel = new WorkPlanStatusLineModel();
			lineModel.setWorkPlanLinkId(link.getWorkPlanLinkId());

			lineModel.setLinkType(WorkPlanLinkType.typeForWorkLink(link));

			WorkPlan workPlan;
			if (workPlanMap.containsKey(link.getWorkPlanId())) {
				workPlan = workPlanMap.get(link.getWorkPlanId());
			} else {
				workPlan = service.getWorkPlanService().getWorkPlan(link.getWorkPlanId());
				workPlanMap.put(link.getWorkPlanId(), workPlan);
			}
			lineModel.setWorkPlanName(workPlan.getName());

			String linkName = "(Missing Check Link)";
			if (link.getComponentId() != null) {
				linkName = service.getComponentService().getComponentName(link.getComponentId());
				String componentType = service.getComponentService().getComponentTypeForComponent(link.getComponentId());
				lineModel.setComponentType(service.getComponentService().getComponentTypeParentsString(componentType, false));
			} else if (link.getEvaluationId() != null) {
				Evaluation evaluation = new Evaluation();
				evaluation.setEvaluationId(link.getEvaluationId());
				evaluation = evaluation.find();

				linkName = service.getComponentService().getComponentName(evaluation.getComponentId());

				String componentType = service.getComponentService().getComponentTypeForComponent(link.getComponentId());
				lineModel.setComponentType(service.getComponentService().getComponentTypeParentsString(componentType, false));
				if (StringUtils.isBlank(linkName)) {
					linkName = service.getComponentService().getComponentName(evaluation.getOriginComponentId());
					//can't switch type on evals
					//lineModel.setComponentType(service.getComponentService().getComponentTypeParentsString(evaluation.getOriginComponentId(), false));
				}
			} else if (link.getUserSubmissionId() != null) {
				UserSubmission userSubmission = new UserSubmission();
				userSubmission.setUserSubmissionId(link.getUserSubmissionId());
				userSubmission = userSubmission.find();

				linkName = userSubmission.getSubmissionName();
			}
			lineModel.setLinkName(linkName);

			String assignedUser = "Unassigned";
			if (StringUtils.isNotBlank(link.getCurrentUserAssigned())) {
				assignedUser = link.getCurrentUserAssigned();
			}
			lineModel.setCurrentAssignee(assignedUser);

			String assignedGroup = "Unassigned";
			if (StringUtils.isNotBlank(link.getCurrentGroupAssigned())) {
				assignedGroup = link.getCurrentGroupAssigned();
			}
			lineModel.setCurrentAssignedGroup(assignedGroup);

			WorkPlanStep step = workPlan.findWorkPlanStep(link.getCurrentStepId());
			String stepName = "(Invalid step for workplan)";
			if (step != null) {
				stepName = step.getName();
			}
			lineModel.setCurrentStepName(stepName);

			lineModel.setCurrentSubStatus(link.getSubStatus());
			lineModel.setLastUpdateChangeDts(link.getUpdateDts());
			long diff = TimeUtil.currentDate().getTime() - link.getUpdateDts().getTime();
			long daysBetween = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
			lineModel.setDaysSincesLastUpdate(daysBetween);

			long numberOfComments = 0;
			Date lastCommentDate = null;
			if (link.getComponentId() != null) {
				ComponentComment commentExample = new ComponentComment();
				commentExample.setComponentId(link.getComponentId());
				commentExample.setCommentType(ComponentCommentType.SUBMISSION);

				List<ComponentComment> comments = commentExample.findByExample();
				for (ComponentComment comment : comments) {
					if (lastCommentDate == null || comment.getUpdateDts().after(lastCommentDate)) {
						lastCommentDate = comment.getUpdateDts();
					}
				}
				numberOfComments = comments.size();
			}
//			else if (link.getEvaluationId() != null) {
//				//todo
//			}  else if (link.getUserSubmissionId() != null) {
//				//todo
//			}
			lineModel.setNumberOfComments(numberOfComments);
			lineModel.setLastCommentUpdate(lastCommentDate);

			reportModel.getData().add(lineModel);
		}
		reportModel.getData().sort((a, b) -> {
			return a.getLinkName().toLowerCase().compareTo(b.getLinkName().toLowerCase());
		});

		return reportModel;
	}

	@Override
	protected Map getWriterMap()
	{
		Map<String, ReportWriter> writerMap = new HashMap<>();

		String viewCSV = outputKey(ReportTransmissionType.VIEW, ReportFormat.CSV);
		writerMap.put(viewCSV, (generator, reportModel) -> {
			writeCSV(generator, reportModel);
		});

		String emailCSV = outputKey(ReportTransmissionType.EMAIL, ReportFormat.CSV);
		writerMap.put(emailCSV, (generator, reportModel) -> {
			writeCSV(generator, reportModel);
		});

		return writerMap;
	}

	@Override
	public List getSupportedOutputs()
	{
		List<ReportTransmissionType> transmissionTypes = new ArrayList<>();

		ReportTransmissionType view = service.getLookupService().getLookupEnity(ReportTransmissionType.class, ReportTransmissionType.VIEW);
		ReportTransmissionType email = service.getLookupService().getLookupEnity(ReportTransmissionType.class, ReportTransmissionType.EMAIL);
		transmissionTypes.add(view);
		transmissionTypes.add(email);

		return transmissionTypes;
	}

	@Override
	public List getSupportedFormats(String reportTransmissionType)
	{
		List<ReportFormat> formats = new ArrayList<>();

		switch (reportTransmissionType) {
			case ReportTransmissionType.VIEW:
				ReportFormat format = service.getLookupService().getLookupEnity(ReportFormat.class, ReportFormat.CSV);
				formats.add(format);
				break;

			case ReportTransmissionType.EMAIL:
				format = service.getLookupService().getLookupEnity(ReportFormat.class, ReportFormat.CSV);
				formats.add(format);
				break;
		}

		return formats;
	}

	private void writeCSV(BaseGenerator generator, BaseReportModel reportModel)
	{
		CSVGenerator cvsGenerator = (CSVGenerator) generator;

		//write header
		cvsGenerator.addLine(reportModel.getTitle(), sdf.format(reportModel.getCreateTime()));
		cvsGenerator.addLine(
				"Link Name",
				"Link Type",
				"Entry Type",
				"Workplan Name",
				"Current Step Name",
				"Current Assignee",
				"Current Assigned Group",
				"Current SubStatus",
				"Last Step Change Date",
				"Number of Sumission Comments",
				"Last Comment Update",
				"Work Plan Link"
		);

		WorkPlanStatusReportModel workPlanStatusModel = (WorkPlanStatusReportModel) reportModel;

		for (WorkPlanStatusLineModel reportLineModel : workPlanStatusModel.getData()) {
			cvsGenerator.addLine(
					reportLineModel.getLinkName(),
					reportLineModel.getLinkType(),
					reportLineModel.getComponentType(),
					reportLineModel.getWorkPlanName(),
					reportLineModel.getCurrentStepName(),
					reportLineModel.getCurrentAssignee(),
					reportLineModel.getCurrentAssignedGroup(),
					reportLineModel.getCurrentSubStatus(),
					reportLineModel.getLastUpdateChangeDts() != null ? sdf.format(reportLineModel.getLastUpdateChangeDts()) : "",
					reportLineModel.getNumberOfComments(),
					reportLineModel.getLastCommentUpdate() != null ? sdf.format(reportLineModel.getLastCommentUpdate()) : "",
					reportLineModel.getWorkPlanLinkId()
			);
		}
	}

}

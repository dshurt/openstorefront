/*
 * Copyright 2014 Space Dynamics Laboratory - Utah State University Research Foundation.
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
package edu.usu.sdl.openstorefront.web.rest.model;

import edu.usu.sdl.openstorefront.storage.model.ComponentEvaluationSection;
import edu.usu.sdl.openstorefront.storage.model.EvaluationSection;
import edu.usu.sdl.openstorefront.util.TranslateUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author dshurtleff
 */
public class ComponentEvaluationSectionView
{

	public static final String NAME_FIELD = "name";

	private String name;
	private Integer score;
	private Date updateDts;
	private String activeStatus;
	private String evaluationSection;

	public ComponentEvaluationSectionView()
	{
	}

	public static ComponentEvaluationSectionView toView(ComponentEvaluationSection section)
	{
		ComponentEvaluationSectionView view = new ComponentEvaluationSectionView();
		view.setName(TranslateUtil.translate(EvaluationSection.class, section.getComponentEvaluationSectionPk().getEvaluationSection()));
		view.setScore(section.getScore());
		view.setUpdateDts(section.getUpdateDts());
		view.setActiveStatus(section.getActiveStatus());
		view.setEvaluationSection(section.getComponentEvaluationSectionPk().getEvaluationSection());
		return view;
	}

	public static List<ComponentEvaluationSectionView> toViewList(List<ComponentEvaluationSection> sections)
	{
		List<ComponentEvaluationSectionView> viewList = new ArrayList();
		sections.forEach(section -> {
			viewList.add(ComponentEvaluationSectionView.toView(section));
		});
		return viewList;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Integer getScore()
	{
		return score;
	}

	public void setScore(Integer score)
	{
		this.score = score;
	}

	public Date getUpdateDts()
	{
		return updateDts;
	}

	public void setUpdateDts(Date updateDts)
	{
		this.updateDts = updateDts;
	}

	public String getActiveStatus()
	{
		return activeStatus;
	}

	public void setActiveStatus(String activeStatus)
	{
		this.activeStatus = activeStatus;
	}

	public String getEvaluationSection()
	{
		return evaluationSection;
	}

	public void setEvaluationSection(String evaluationSection)
	{
		this.evaluationSection = evaluationSection;
	}

}

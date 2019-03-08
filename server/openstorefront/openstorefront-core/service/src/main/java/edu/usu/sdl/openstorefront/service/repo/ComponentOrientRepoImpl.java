/*
 * Copyright 2019 Space Dynamics Laboratory - Utah State University Research Foundation.
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
package edu.usu.sdl.openstorefront.service.repo;

import com.orientechnologies.orient.core.record.impl.ODocument;
import edu.usu.sdl.openstorefront.core.api.repo.ComponentRepo;
import edu.usu.sdl.openstorefront.core.entity.ApprovalStatus;
import edu.usu.sdl.openstorefront.core.entity.Component;
import edu.usu.sdl.openstorefront.core.entity.ComponentReview;
import edu.usu.sdl.openstorefront.core.model.search.SearchOperation;
import edu.usu.sdl.openstorefront.core.view.ComponentSearchView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author dshurtleff
 */
public class ComponentOrientRepoImpl
		extends BaseOrientRepo
		implements ComponentRepo
{

	@Override
	public Map<String, List<Component>> getComponentByOrganization(Set<String> componentIds)
	{

		Map<String, Object> params = new HashMap<>();
		String componentFilter = "";
		if (componentIds != null && !componentIds.isEmpty()) {
			params = new HashMap<>();
			params.put("idlistParam", componentIds);
			componentFilter = " and componentId in :idlistParam";
		}

		String restrictionQuery = filterEngine.queryComponentRestriction();

		List<ODocument> documents = service.getPersistenceService().query("Select organization, name, name.toLowerCase() as sortname, securityMarkingType, lastActivityDts, approvalState from " + Component.class.getSimpleName()
				+ " where approvalState='" + ApprovalStatus.APPROVED + "' and "
				+ (StringUtils.isNotBlank(restrictionQuery) ? restrictionQuery + " and " : "")
				+ " activeStatus= '" + Component.ACTIVE_STATUS + "' " + componentFilter + " order by sortname", params);

		//group by org
		Map<String, List<Component>> orgMap = new HashMap<>();

		documents.forEach(
				document -> {
					String org = document.field("organization");
					if (StringUtils.isBlank(org)) {
						org = "No Organization Specified";
					}
					if (orgMap.containsKey(org)) {
						orgMap.get(org).add(populateOrganizationComponent(document));
					} else {

						List<Component> records = new ArrayList<>();
						records.add(populateOrganizationComponent(document));
						orgMap.put(org, records);
					}
				});

		return orgMap;
	}

	private Component populateOrganizationComponent(ODocument document)
	{
		Component component = new Component();
		component.setName(document.field("name"));
		component.setLastActivityDts(document.field("lastActivityDts"));
		component.setSecurityMarkingType(document.field("securityMarkingType"));
		component.setApprovalState(document.field("approvalState"));
		return component;
	}

	@Override
	public Map<String, Integer> findAverageUserRatingForComponents()
	{
		Map<String, Integer> componentRatingsMap = new HashMap<>();

		String query = "select componentId, avg(rating) as rating from " + ComponentReview.class.getSimpleName() + " group by componentId ";
		List<ODocument> resultsRatings = service.getPersistenceService().query(query, new HashMap<>());
		resultsRatings.forEach(document -> {
			componentRatingsMap.put(document.field("componentId"), document.field("rating"));
		});

		return componentRatingsMap;
	}

	@Override
	public Map<String, ComponentSearchView> getIntermidateSearchResults(Set<String> componentIds)
	{
		String dataFilterRestriction = getFilterEngine().queryComponentRestriction();
		if (StringUtils.isNotBlank(dataFilterRestriction)) {
			dataFilterRestriction += " and ";
		}

		String query = "select componentId, componentType, name, lastUpdateDts, activeStatus, approvalState from "
				+ Component.class.getSimpleName()
				+ " where "
				+ dataFilterRestriction
				+ " componentId in :idList";

		Map<String, Object> parameterMap = new HashMap<>();
		parameterMap.put("idList", componentIds);
		List<ODocument> results = service.getPersistenceService().query(query, parameterMap);

		Map<String, ComponentSearchView> resultMap = new HashMap<>();
		for (ODocument doc : results) {
			if (Component.ACTIVE_STATUS.equals(doc.field("activeStatus"))
					&& ApprovalStatus.APPROVED.equals(doc.field("approvalState"))) {
				ComponentSearchView view = new ComponentSearchView();
				view.setComponentId(doc.field("componentId"));
				view.setName(doc.field("name"));
				view.setComponentType(doc.field("componentType"));
				view.setLastActivityDts(doc.field("lastUpdateDts"));
				resultMap.put(view.getComponentId(), view);
			}
		}
		return resultMap;
	}

	@Override
	public Map<Integer, List<String>> getAverageRatingForComponents(int maxRating, SearchOperation.NumberOperation numberOperation)
	{
		Map<Integer, List<String>> ratingMap = new HashMap<>();

		String query = "select componentId, avg(rating) as rating from " + ComponentReview.class.getSimpleName() + " where activeStatus='" + ComponentReview.ACTIVE_STATUS + "' group by componentId ";

		List<ODocument> queryResults = service.getPersistenceService().query(query, new HashMap<>());

		for (ODocument oDocument : queryResults) {
			Integer value = oDocument.field("rating");
			String componentId = oDocument.field("componentId");
			if (numberOperation.pass(value, maxRating)) {
				if (ratingMap.containsKey(value)) {
					ratingMap.get(value).add(componentId);
				} else {
					List<String> componentIds = new ArrayList<>();
					componentIds.add(componentId);
					ratingMap.put(value, componentIds);
				}
			}
		}
		return ratingMap;
	}

}

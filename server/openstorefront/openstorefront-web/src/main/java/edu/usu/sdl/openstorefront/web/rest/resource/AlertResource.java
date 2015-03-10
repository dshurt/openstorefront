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
package edu.usu.sdl.openstorefront.web.rest.resource;

import edu.usu.sdl.openstorefront.doc.APIDescription;
import edu.usu.sdl.openstorefront.doc.DataType;
import edu.usu.sdl.openstorefront.doc.RequireAdmin;
import edu.usu.sdl.openstorefront.doc.RequiredParam;
import edu.usu.sdl.openstorefront.storage.model.Alert;
import edu.usu.sdl.openstorefront.validation.ValidationModel;
import edu.usu.sdl.openstorefront.validation.ValidationResult;
import edu.usu.sdl.openstorefront.validation.ValidationUtil;
import edu.usu.sdl.openstorefront.web.rest.model.FilterQueryParams;
import java.net.URI;
import java.util.List;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author dshurtleff
 */
@Path("v1/resource/alerts")
@APIDescription("Alert are triggers setup to watch the  data that user and subscribe to.")
public class AlertResource
		extends BaseResource
{

	@GET
	@RequireAdmin
	@APIDescription("Gets alert subscribion records.")
	@Produces({MediaType.APPLICATION_JSON})
	@DataType(Alert.class)
	public Response getAlerts(@BeanParam FilterQueryParams filterQueryParams)
	{
		ValidationResult validationResult = filterQueryParams.validate();
		if (!validationResult.valid()) {
			return sendSingleEntityResponse(validationResult.toRestError());
		}

		Alert alertExample = new Alert();
		alertExample.setActiveStatus(filterQueryParams.getStatus());
		List<Alert> alerts = service.getPersistenceService().queryByExample(Alert.class, alertExample);
		alerts = filterQueryParams.filter(alerts);

		GenericEntity<List<Alert>> entity = new GenericEntity<List<Alert>>(alerts)
		{
		};
		return sendSingleEntityResponse(entity);
	}

	@GET
	@RequireAdmin
	@APIDescription("Gets an alert subscribion record.")
	@Produces({MediaType.APPLICATION_JSON})
	@DataType(Alert.class)
	@Path("/{id}")
	public Response getAlert(
			@PathParam("id") String alertId)
	{
		Alert alertExample = new Alert();
		alertExample.setAlertId(alertId);
		Alert alert = service.getPersistenceService().queryOneByExample(Alert.class, alertExample);
		return sendSingleEntityResponse(alert);
	}

	@POST
	@RequireAdmin
	@APIDescription("Creates a new Alert")
	@Consumes({MediaType.APPLICATION_JSON})
	public Response postAlert(Alert alert)
	{
		return handleSaveAlert(alert, true);
	}

	@PUT
	@RequireAdmin
	@APIDescription("Updates a Alert")
	@Consumes({MediaType.APPLICATION_JSON})
	@Path("/{id}")
	public Response updateEntityValue(
			@PathParam("id")
			@RequiredParam String alertId,
			Alert alert)
	{

		Alert existing = service.getPersistenceService().findById(Alert.class, alertId);
		if (existing == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		alert.setAlertId(alertId);
		return handleSaveAlert(alert, false);
	}

	private Response handleSaveAlert(Alert alert, boolean post)
	{
		ValidationModel validationModel = new ValidationModel(alert);
		validationModel.setConsumeFieldsOnly(true);
		ValidationResult validationResult = ValidationUtil.validate(validationModel);
		if (validationResult.valid()) {
			service.getAlertService().saveAlert(alert);
		} else {
			return Response.ok(validationResult.toRestError()).build();
		}
		if (post) {
			return Response.created(URI.create("v1/resource/alerts/" + alert.getAlertId())).entity(alert).build();
		} else {
			return Response.ok().build();
		}
	}

	@POST
	@RequireAdmin
	@APIDescription("activates an Alert")
	@Produces({MediaType.APPLICATION_JSON})
	@DataType(Alert.class)
	@Path("/{id}/activate")
	public Response activatesAlert(
			@PathParam("id") String alertId)
	{
		Alert alert = service.getPersistenceService().setStatusOnEntity(Alert.class, alertId, Alert.INACTIVE_STATUS);
		return sendSingleEntityResponse(alert);
	}

	@DELETE
	@RequireAdmin
	@APIDescription("Inactivates an Alert")
	@Path("/{id}")
	public void inactiveAlert(
			@PathParam("id") String alertId)
	{
		service.getPersistenceService().setStatusOnEntity(Alert.class, alertId, Alert.INACTIVE_STATUS);
	}

	@DELETE
	@RequireAdmin
	@APIDescription("Deletes an alert")
	@Path("/{id}/force")
	public void deleteAlert(
			@PathParam("id") String alertId)
	{
		service.getAlertService().deleteAlert(alertId);
	}

}

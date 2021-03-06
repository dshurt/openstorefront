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
package edu.usu.sdl.apiclient.rest.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.usu.sdl.apiclient.AbstractService;
import edu.usu.sdl.apiclient.ClientAPI;
import edu.usu.sdl.openstorefront.core.entity.SecurityRole;
import edu.usu.sdl.openstorefront.core.entity.UserRole;
import edu.usu.sdl.openstorefront.core.view.LookupModel;
import java.util.List;
import javax.ws.rs.core.Response;

/**
 *
 * @author ccummings
 */ 
public class SecurityRoleClient extends AbstractService
{
	String basePath = "api/v1/resource/securityroles";

	public SecurityRoleClient(ClientAPI client)
	{
		super(client);
	}
	
	public SecurityRoleClient()
	{
		this(new ClientAPI(new ObjectMapper()));
	}

	public Response addUserToRole(String rolename, UserRole userRole)
	{
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public Response createRole(SecurityRole securityRole)
	{
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public Response createRole(String rolename, SecurityRole securityRole)
	{
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public Response deleteRole(String rolename, String moveToRole)
	{
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public Response getRole(String rolename)
	{
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public List<SecurityRole> getRoles()
	{
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public List<LookupModel> getRolesLookup()
	{
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public List<UserRole> getUserForRole(String rolename)
	{
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public Response removeUserFromRole(String rolename, String username)
	{
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

}

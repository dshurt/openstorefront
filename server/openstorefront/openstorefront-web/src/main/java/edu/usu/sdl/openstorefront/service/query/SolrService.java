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
package edu.usu.sdl.openstorefront.service.query;

import edu.usu.sdl.openstorefront.service.manager.PropertiesManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;

/**
 *
 * @author dshurtleff
 */
public class SolrService
{
	private static final String CORE_EXAMPLE = "openstorefront";
	
	//Should reuse server to avoid leaks according to docs.
	private static SolrServer exampleServer;
	
	public static void init()
	{
		String url = PropertiesManager.getValue("SOLR_BASE_ADDRESS", "http://localhost:8983/solr/") + CORE_EXAMPLE;		
		exampleServer  = new HttpSolrServer(url, new DefaultHttpClient());		
	}
	
	public static void cleanup()
	{
		if (exampleServer != null)
		{
			exampleServer.shutdown();
		}
	}	
	
	public SolrServer getExampleServer()
	{
		return exampleServer;
	}
	
	
	
	
}

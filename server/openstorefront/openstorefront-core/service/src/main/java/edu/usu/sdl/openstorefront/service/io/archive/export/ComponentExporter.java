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
package edu.usu.sdl.openstorefront.service.io.archive.export;

import edu.usu.sdl.openstorefront.core.entity.Component;
import edu.usu.sdl.openstorefront.service.io.archive.BaseExporter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author dshurtleff
 */
public class ComponentExporter
		extends BaseExporter
{

	@Override
	public int getPriority()
	{
		return 6;
	}

	@Override
	public String getExporterSupportEntity()
	{
		return Component.class.getSimpleName();
	}

	@Override
	public List<BaseExporter> getAllRequiredExports()
	{
		List<BaseExporter> exporters = new ArrayList<>();
				
		exporters.add(new UserLookupTypeExporter());
		exporters.add(new GeneralMediaExporter());		
		exporters.add(new AttributeExporter());		
		
		exporters.add(new EntryTypeExporter());	
		exporters.add(new EntryTemplateExporter());	
		exporters.add(new ComponentExporter());	
		
		exporters.add(new ChecklistQuestionExporter());
		exporters.add(new ChecklistTemplateExporter());
		exporters.add(new SectionTemplateExporter());
		
		exporters.add(new EvaluationTemplateExporter());		
		exporters.add(new EvaluationExporter());
		
		return exporters;
	}

	@Override
	public void exportRecords()
	{
		//look options to see which or all entry to export
		
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void importRecords()
	{
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

}

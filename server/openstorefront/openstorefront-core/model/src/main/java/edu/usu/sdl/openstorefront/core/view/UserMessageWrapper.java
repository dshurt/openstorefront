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
package edu.usu.sdl.openstorefront.core.view;

import edu.usu.sdl.openstorefront.core.entity.UserMessage;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author dshurtleff
 */
public class UserMessageWrapper
{

	private long totalNumber;
	private List<UserMessage> data = new ArrayList<>();

	@SuppressWarnings({"squid:S2637", "squid:S1186"})
	public UserMessageWrapper()
	{
	}

	public long getTotalNumber()
	{
		return totalNumber;
	}

	public void setTotalNumber(long totalNumber)
	{
		this.totalNumber = totalNumber;
	}

	public List<UserMessage> getData()
	{
		return data;
	}

	public void setData(List<UserMessage> data)
	{
		this.data = data;
	}

}

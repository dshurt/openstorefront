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
package edu.usu.sdl.openstorefront.service.manager;

import edu.usu.sdl.apidoc.APIDocActivator;
import edu.usu.sdl.core.CoreActivator;
import edu.usu.sdl.openstorefront.common.OpenstorefrontCommonActivator;
import edu.usu.sdl.openstorefront.common.exception.OpenStorefrontRuntimeException;
import edu.usu.sdl.openstorefront.common.manager.Initializable;
import edu.usu.sdl.openstorefront.common.manager.PropertiesManager;
import edu.usu.sdl.openstorefront.core.model.internal.CoreAPIActivator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.felix.framework.Felix;
import org.apache.felix.framework.util.FelixConstants;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkEvent;

/**
 * Handles the OSGi framework life-cycle
 *
 * @author dshurtleff
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class OsgiManager
		implements Initializable
{

	private static final Logger LOG = Logger.getLogger(OsgiManager.class.getName());

	private static final long MAX_SHUTDOWN_WAIT_TIME = 60000;
	private Felix felix = null;
	private AtomicBoolean started = new AtomicBoolean(false);
	private PropertiesManager propertiesManager;

	private static OsgiManager singleton = null;

	private OsgiManager(PropertiesManager propManager)
	{
		this.propertiesManager = propManager;
	}

	public static OsgiManager getInstance()
	{
		return getInstance(PropertiesManager.getInstance());
	}

	// Created second getInstance with parameter due to @Inject requires CDI support
	// This is temporary until decision has been made on using @Inject
	public static OsgiManager getInstance(PropertiesManager propertiesManager)
	{
		if (singleton == null) {
			singleton = new OsgiManager(propertiesManager);
		}
		return singleton;
	}

	@SuppressWarnings("unchecked")
	private void init()
	{
		Map configMap = new HashMap();

		List list = new ArrayList();
		list.add(new CoreActivator());
		list.add(new OpenstorefrontCommonActivator());
		list.add(new CoreAPIActivator());
		list.add(new APIDocActivator());
		configMap.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, list);

		//We are managing the plugins in the application
		configMap.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);

		//org.osgi.framework.system.packages.extra
		//org.osgi.framework.bootdelegation
		String moduleVersion = propertiesManager.getModuleVersion();
		configMap.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA,
				"edu.usu.sdl.openstorefront.core.annotation; version=" + moduleVersion + ", "
				+ "edu.usu.sdl.openstorefront.core.api; version=" + moduleVersion + ", "
				+ "edu.usu.sdl.openstorefront.core.api.query; version=" + moduleVersion + ", "
				+ "edu.usu.sdl.openstorefront.core.api.model; version=" + moduleVersion + ", "
				+ "edu.usu.sdl.openstorefront.core.entity; version=" + moduleVersion + ", "
				+ "edu.usu.sdl.openstorefront.core.model; version=" + moduleVersion + ", "
				+ "edu.usu.sdl.openstorefront.core.model.search; version=" + moduleVersion + ", "
				+ "edu.usu.sdl.openstorefront.core.sort; version=" + moduleVersion + ", "
				+ "edu.usu.sdl.openstorefront.core.spi; version=" + moduleVersion + ", "
				+ "edu.usu.sdl.openstorefront.core.spi.parser; version=" + moduleVersion + ", "
				+ "edu.usu.sdl.openstorefront.core.spi.parser.mapper; version=" + moduleVersion + ", "
				+ "edu.usu.sdl.openstorefront.core.spi.parser.reader; version=" + moduleVersion + ", "
				+ "edu.usu.sdl.openstorefront.core.util; version=" + moduleVersion + ", "
				+ "edu.usu.sdl.openstorefront.core.view; version=" + moduleVersion + ", "
				+ "edu.usu.sdl.openstorefront.core.view.statistic; version=" + moduleVersion + ", "
				+ "edu.usu.sdl.openstorefront.common.util; version=" + moduleVersion + ", "
				+ "edu.usu.sdl.openstorefront.common.exception; version=" + moduleVersion + ", "
				+ "edu.usu.sdl.openstorefront.common.manager; version=" + moduleVersion + ", "
				+ "edu.usu.sdl.openstorefront.validation; version=" + moduleVersion + ", "
				+ "org.simpleframework.xml; version=2.7.1, "
				+ "org.simpleframework.xml.core; version=2.7.1, "
				+ "edu.usu.sdl.openstorefront.security; version=" + moduleVersion
		);
		try {
			felix = new Felix(configMap);
			felix.start();

			LOG.log(Level.INFO, () -> "Started Felix Version: " + felix.getVersion());

		} catch (BundleException ex) {
			throw new OpenStorefrontRuntimeException("Unable to load module system", ex);
		}

	}

	private void cleanup()
	{
		if (felix != null) {

			try {
				felix.stop();
				FrameworkEvent frameworkEvent = felix.waitForStop(MAX_SHUTDOWN_WAIT_TIME);
				LOG.log(Level.INFO, () -> "Framework Shutdown Event: " + frameworkEvent.getType());
			} catch (BundleException | InterruptedException ex) {
				Logger.getLogger(OsgiManager.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	public Felix getFelix()
	{
		return felix;
	}

	public void setPropertyManager(PropertiesManager propertiesManager)
	{
		this.propertiesManager = propertiesManager;
	}

	@Override
	public void initialize()
	{
		init();
		started.set(true);
	}

	@Override
	public void shutdown()
	{
		cleanup();
		started.set(false);
	}

	@Override
	public boolean isStarted()
	{
		return started.get();
	}

}

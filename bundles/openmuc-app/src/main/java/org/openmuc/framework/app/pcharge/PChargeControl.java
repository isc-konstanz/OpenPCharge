/*
 * Copyright 2016-20 ISC Konstanz
 *
 * This file is part of OpenPCharge.
 * For more information visit https://github.com/isc-konstanz/OpenPCharge.
 *
 * OpenPCharge is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenPCharge is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenPCharge.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openmuc.framework.app.pcharge;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import org.ini4j.Ini;
import org.ini4j.IniPreferences;
import org.openmuc.framework.app.pcharge.port.ChargePort;
import org.openmuc.framework.dataaccess.DataAccessService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = {})
public final class PChargeControl extends Thread {
	private static final Logger logger = LoggerFactory.getLogger(PChargeControl.class);

	private final static String CONFIG = "org.openmuc.framework.app.pcharge.config";
	private final static int SLEEP_INTERVAL = 60000;

	@Reference
	private DataAccessService dataAccessService;

	private final Map<String, ChargePort> ports = new HashMap<String, ChargePort>();

	private volatile boolean deactivateFlag;

	private Ini configs = null;

    @Activate
    private void activate() {
		logger.info("Activating P-CHARGE Control");

		String fileName = System.getProperty(CONFIG);
		if (fileName == null) {
			fileName = "conf" + File.separator + "p-charge.conf";
		}
		try {
			configs = new Ini(new File(fileName));
			start();
			
		} catch (IOException e) {
			logger.error("Error while reading P-CHARGE control configuration: {}", e.getMessage());
		}
	}

    @Deactivate
    private void deactivate() {
		logger.info("Deactivating P-CHARGE Control");
		deactivateFlag = true;

		interrupt();
		try {
			this.join();
		} catch (InterruptedException e) {
		}
	}

	@Override
	public void run() {
		logger.info("Starting P-CHARGE Control");
		setName("P-CHARGE Control");
		
		if (deactivateFlag) {
			logger.info("P-CHARGE Control thread interrupted and will stop");
			return;
		}
		
		Preferences prefs = new IniPreferences(configs);
		for (String id: configs.keySet()) {
			ChargePort port;
			try {
				port = new ChargePort(dataAccessService, id, prefs.node(id));
				ports.put(id, port);
				
			} catch (PChargeConfigException e) {
				logger.warn("Error while configuring {}: {}", id, e.getMessage());
			}
		}
		
		while (!deactivateFlag) {
			try {
				// TODO: implement channel flag checks, ports and optimization verifications
				
				Thread.sleep(SLEEP_INTERVAL);
			} catch (InterruptedException e) {
			}
		}
	}
}

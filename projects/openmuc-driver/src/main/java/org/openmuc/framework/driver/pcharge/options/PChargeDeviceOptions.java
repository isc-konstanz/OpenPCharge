/*
 * Copyright 2016-17 ISC Konstanz
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
package org.openmuc.framework.driver.pcharge.options;

import org.openmuc.framework.config.options.DeviceOptions;
import org.openmuc.framework.config.options.Option;
import org.openmuc.framework.config.options.OptionCollection;
import org.openmuc.framework.data.ValueType;

public class PChargeDeviceOptions extends DeviceOptions {

	private static final String DESCRIPTION = "A device represents a port which is connected to the car.";

	//public static final String CHARGE_PORT = "chargePort";
	
	public static final String TCP_PORT = "tcpPort";

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	protected void configureAddress(OptionCollection address) {		
		address.disable();
	}

	@Override
	protected void configureSettings(OptionCollection settings) {		
		settings.add(tcpPort());
	}

	@Override
	protected void configureScanSettings(OptionCollection scanSettings) {
		scanSettings.disable();
	}

	
	
	private Option tcpPort(){
		Option tcpPort = new Option (TCP_PORT, "TCP Port", ValueType.INTEGER);
		tcpPort.setDescription("TCP Port represents the server port and needs to be entered in the EWS-Box settings");
		tcpPort.setMandatory(true);
		
		return tcpPort;
	
	}

}

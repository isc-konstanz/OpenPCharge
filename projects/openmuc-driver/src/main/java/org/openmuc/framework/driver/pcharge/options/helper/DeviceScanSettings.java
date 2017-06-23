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
package org.openmuc.framework.driver.pcharge.options.helper;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.options.Parameters;
import org.openmuc.framework.driver.pcharge.options.PChargeDeviceOptions;

public class DeviceScanSettings {

	private final Parameters scanSettings;

	public DeviceScanSettings(String settings) throws ArgumentSyntaxException {
		
		PChargeDeviceOptions options = new PChargeDeviceOptions();
		this.scanSettings = options.parseScanSettings(settings);
	}

	public int getTcpPort() {
		
		//TODO implement option
		
		return -1;
	}

}

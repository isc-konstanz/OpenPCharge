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
package org.openmuc.framework.driver.pcharge;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.DriverInfo;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.config.ScanInterruptedException;
import org.openmuc.framework.driver.pcharge.options.PChargeDevicePreferences;
import org.openmuc.framework.driver.pcharge.options.PChargeDriverInfo;
import org.openmuc.framework.driver.spi.Connection;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.DriverDeviceScanListener;
import org.openmuc.framework.driver.spi.DriverService;
import org.osgi.service.component.annotations.Component;


@Component
public class PChargeDriver implements DriverService {
//	private final static Logger logger = LoggerFactory.getLogger(PChargeDriver.class);
    private final PChargeDriverInfo info = PChargeDriverInfo.getInfo();

	@Override
	public DriverInfo getInfo() {
		return info;
	}

	@Override
	public void scanForDevices(String settingsStr, DriverDeviceScanListener listener)
			throws UnsupportedOperationException, ArgumentSyntaxException, ScanException, ScanInterruptedException {

		throw new UnsupportedOperationException();
	}

	@Override
	public void interruptDeviceScan() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Connection connect(String addressStr, String settingsStr) throws ArgumentSyntaxException, ConnectionException {
		try {
			PChargeDevicePreferences settings = info.getDevicePreferences(settingsStr);
			
			return new PChargeDevice(settings.getPort());

		} catch (IllegalArgumentException e) {
			throw new ArgumentSyntaxException("Unable to configure EWS-Box device: " + e.getMessage());
		}
	}
}

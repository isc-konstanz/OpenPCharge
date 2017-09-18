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
import org.openmuc.framework.config.options.ChannelOptions;
import org.openmuc.framework.config.options.DeviceOptions;
import org.openmuc.framework.driver.pcharge.options.PChargeChannelOptions;
import org.openmuc.framework.driver.pcharge.options.PChargeDeviceOptions;
import org.openmuc.framework.driver.pcharge.options.helper.DeviceSettings;
import org.openmuc.framework.driver.spi.Connection;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.DriverDeviceScanListener;
import org.openmuc.framework.driver.spi.DriverService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component
public class PChargeDriver implements DriverService {

	private final static Logger logger = LoggerFactory.getLogger(PChargeDriver.class);

	private final static String ID = "pcharge";
	private final static String NAME = "P-CHARGE";
	private final static String DESCRIPTION = "The description";
	private final static DeviceOptions DEVICE_OPTIONS = new PChargeDeviceOptions();
	private final static ChannelOptions CHANNEL_OPTIONS = new PChargeChannelOptions();
	private final static DriverInfo DRIVER_INFO = new DriverInfo(ID, NAME, DESCRIPTION, DEVICE_OPTIONS, CHANNEL_OPTIONS);

	@Override
	public DriverInfo getInfo() {
		return DRIVER_INFO;
	}

	@Override
	public void scanForDevices(String settingsStr, DriverDeviceScanListener listener)
			throws UnsupportedOperationException, ArgumentSyntaxException, ScanException, ScanInterruptedException {

		throw new UnsupportedOperationException();
	}

	@Override
	public void interruptDeviceScan() throws UnsupportedOperationException {
	}

	@Override
	public Connection connect(String deviceAddressStr, String settingsStr) throws ArgumentSyntaxException, ConnectionException {

		DeviceSettings settings = new DeviceSettings(settingsStr);
		
		try {
			return new PChargeConnection(settings);

		} catch (IllegalArgumentException e) {
			throw new ArgumentSyntaxException("Unable to configure device: " + e.getMessage());
			
		}
	}
}

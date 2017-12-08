/*
 * Copyright 2011-16 Fraunhofer ISE
 *
 * This file is part of OpenMUC.
 * For more information visit http://www.openmuc.org
 *
 * OpenMUC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenMUC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenMUC.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openmuc.framework.driver.pcharge.options;

import java.util.HashMap;
import java.util.Map;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.DriverInfo;
import org.openmuc.framework.config.options.Preferences;
import org.openmuc.framework.driver.spi.ChannelRecordContainer;
import org.openmuc.framework.driver.spi.ChannelValueContainer;

public class PChargeDriverInfo extends DriverInfo {

	private final static PChargeDriverInfo info = new PChargeDriverInfo();

	private final Map<String, PChargeChannelPreferences> channels = new HashMap<String, PChargeChannelPreferences>();

	private PChargeDriverInfo() {
		super(PChargeDriverInfo.class.getResourceAsStream("options.xml"));
	}

	public static PChargeDriverInfo getInfo() {
		return info;
	}

	public PChargeDevicePreferences getDevicePreferences(String addressStr) throws ArgumentSyntaxException {
		Preferences address = parseDeviceAddress(addressStr);
		
		return new PChargeDevicePreferences(address);
	}

	public PChargeChannelPreferences getChannelPreferences(ChannelValueContainer container) throws ArgumentSyntaxException {
		String address = container.getChannelAddress();
		String settings = container.getChannelSettings();
		
		return new PChargeChannelPreferences(address, settings, parseChannelSettings(settings));
	}

	public PChargeChannelPreferences getChannelPreferences(ChannelRecordContainer container) throws ArgumentSyntaxException {
		String id = container.getChannel().getId();
		String address = container.getChannelAddress();
		String settings = container.getChannelSettings();
		if (channels.containsKey(id)) {
			PChargeChannelPreferences prefs = channels.get(id);
			if (prefs.equals(address, settings)) {
				return prefs;
			}
		}
		return new PChargeChannelPreferences(address, settings, parseChannelSettings(settings));
	}

}

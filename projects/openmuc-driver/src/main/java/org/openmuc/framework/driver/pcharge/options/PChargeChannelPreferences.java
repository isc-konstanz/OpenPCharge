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

import org.openmuc.framework.config.options.Preferences;

public class PChargeChannelPreferences {

	public static final String PORT_CHARGE_KEY = "chargePort";

	private final String addressStr;
	private final String settingsStr;
	private final Preferences settings;

	public PChargeChannelPreferences(String addressStr, String settingsStr, Preferences settings) {
		this.addressStr = addressStr;
		this.settingsStr = settingsStr;
		this.settings = settings;
	}

	public boolean equals(String addressStr, String settingsStr) {
		return this.addressStr.equals(addressStr) && this.settingsStr.equals(settingsStr);
	}

	public PChargePortKey getKey() {
		if (addressStr != null && !addressStr.isEmpty()) {
			return PChargePortKey.valueOf(addressStr.toUpperCase());
		}
		return null;
	}

	public Integer getChargePort() {
		if (settings.contains(PORT_CHARGE_KEY)) {
			return settings.getInteger(PORT_CHARGE_KEY);
		}
		return null;
	}

}

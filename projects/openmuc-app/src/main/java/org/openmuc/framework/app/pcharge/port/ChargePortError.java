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
package org.openmuc.framework.app.pcharge.port;

public enum ChargePortError {

	CABLE_CURRENT("Maximum cable current insufficient"),
	LOCKING("Locking plug failed"),
	UNLOCKING("Unlocking plug failed"),
	RELAIS_ON("Switching relais on failed"),
	RELAIS_OFF("Switching relais off failed"),
	CONFIG_INVALID("Configuration is invalid"),
	VENTING("Turning on ventilation failed"),
	PORT_BUSY("Port is busy");

	private final String message;

	private ChargePortError(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return message;
	}

}

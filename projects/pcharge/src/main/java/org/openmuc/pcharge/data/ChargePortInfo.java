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
package org.openmuc.pcharge.data;

import java.util.Arrays;


/**
 * A ChargePort represents a status reading of a charging station port.
 * ChargePort is immutable. It contains a user, a timestamp, a status
 * and additional information about the charging station port.
 */
public class ChargePortInfo {
	
	private final byte[] message;
	
	public ChargePortInfo(byte[] message) throws IllegalArgumentException {
		if (message == null || message.length < 40) {
			throw new IllegalArgumentException("Message array inconsistent with the Charge Port Status");
		}
		this.message = message;
	}

	public boolean isOptimized(int port) {
		return (message[5] == 1 ? true : false);
	}

	public ChargePortStatus getStatus(int port) {
		return ChargePortStatus.newStatus(message[0]);
	}

	public ChargeCompleteStatus getCompleteStatus(int port) {
		return ChargeCompleteStatus.getStatus(message[6]);
	}

	public int getCurrent(int port) {
		return message[1];
	}

	public int getCurrentLimit(int port) {
		return message[2];
	}

	public int getDurationCharging(int port) {
		return byteArrayToInt(Arrays.copyOfRange(message, 7, 11));
	}

	public int getEnergyCharging(int port) {
		return byteArrayToInt(Arrays.copyOfRange(message, 11, 15));
	}

	public int getEnergyTotal(int port) {
		return byteArrayToInt(Arrays.copyOfRange(message, 23, 27));
	}

	public boolean isCharging(int port) {
		ChargePortStatus status = getStatus(port);
		if (status == ChargePortStatus.CHARGING) {
			return true;
		}
		return false;
	}

	public boolean isConnected(int port) {
		ChargePortStatus status = getStatus(port);
		if (status == ChargePortStatus.CHARGING ||
				status == ChargePortStatus.CHARGING_ABORTED ||
				status == ChargePortStatus.CHARGING_COMPLETE ||
				status == ChargePortStatus.CHARGING_OPTIMIZED ||
				status == ChargePortStatus.CHARGING_PAUSE ||
				status == ChargePortStatus.CHARGING_PAUSE_OPTIMIZED ||
				status == ChargePortStatus.WAIT_FOR_START) {
			return true;
		}
		return false;
	}

	private int byteArrayToInt(byte[] b) {
	    return   b[3] & 0xFF |
	            (b[2] & 0xFF) << 8 |
	            (b[1] & 0xFF) << 16 |
	            (b[0] & 0xFF) << 24;
	}
}

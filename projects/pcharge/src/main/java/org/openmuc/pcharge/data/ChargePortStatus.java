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


public enum ChargePortStatus {

	NOT_CONNECTED(0x00),
	WAIT_FOR_CABLE(0x10),
	WAIT_FOR_START(0x30),
	CHARGING(0x40),
	CHARGING_OPTIMIZED(0x41),
	CHARGING_PAUSE(0x42),
	CHARGING_PAUSE_OPTIMIZED(0x43),
	CHARGING_COMPLETE(0x50),
	CHARGING_ABORTED(0x60),
	ERROR_CABLE_CURRENT(0xA0),
	ERROR_LOCKING(0xA1),
	ERROR_UNLOCKING(0xA2),
	ERROR_RELAIS_ON(0xA3),
	ERROR_RELAIS_OFF(0xA4),
	ERROR_CONFIG_INVALID(0xA5),
	ERROR_VENTING(0xA6),
	PORT_BUSY(0xA9),
	UNKNOWN(-1);

    private final int code;

    private ChargePortStatus(int code) {
        this.code = code;
    }

    public byte getCode() {
        return (byte) code;
    }
	
	public static ChargePortStatus newStatus(byte b) {
		switch(b) {
			case (byte) 0X00:
				return NOT_CONNECTED;
			case (byte) 0x10:
				return WAIT_FOR_CABLE;
			case (byte) 0x30:
				return WAIT_FOR_START;
			case (byte) 0x40:
				return CHARGING;
			case (byte) 0x41:
				return CHARGING_OPTIMIZED;
			case (byte) 0x42:
				return CHARGING_PAUSE;
			case (byte) 0x43:
				return CHARGING_PAUSE_OPTIMIZED;
			case (byte) 0x50:
				return CHARGING_COMPLETE;
			case (byte) 0x60:
				return CHARGING_ABORTED;
			case (byte) 0xA0:
				return ERROR_CABLE_CURRENT;
			case (byte) 0xA1:
				return ERROR_LOCKING;
			case (byte) 0xA2:
				return ERROR_UNLOCKING;
			case (byte) 0xA3:
				return ERROR_RELAIS_OFF;
			case (byte) 0xA4:
				return ERROR_RELAIS_ON;
			case (byte) 0xA5:
				return ERROR_CONFIG_INVALID;
			case (byte) 0xA6:
				return ERROR_VENTING;
			case (byte) 0xA9:
				return PORT_BUSY;
			default:
				return UNKNOWN;
		}
	}
}

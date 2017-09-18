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


public enum ChargeCompleteStatus {

	OK_COMPLETE(0),
	OK_STOP(1),
	OK_CABLE_PULLED(3),
	ERROR_CABLE_LOST(4),
	ERROR_CIRCUIT_BREAKER(5),
	ERROR_METER(6),
	ERROR_TIMEOUT(7),
	ERROR_VENTING(8),
	ERROR_PMW(9),
	UNKNOWN_STATUS(-1);

    private final int code;

    private ChargeCompleteStatus(int code) {
        this.code = code;
    }

    public byte getCode() {
        return (byte) code;
    }
	
	public static ChargeCompleteStatus newStatus(int b) {
		switch(b) {
			case 0:
				return OK_COMPLETE;
			case 1:
				return OK_STOP;
			case 3:
				return OK_CABLE_PULLED;
			case 4:
				return ERROR_CABLE_LOST;
			case 5:
				return ERROR_CIRCUIT_BREAKER;
			case 6:
				return ERROR_METER;
			case 7:
				return ERROR_TIMEOUT;
			case 8:
				return ERROR_VENTING;
			case 9:
				return ERROR_PMW;
			default:
				return UNKNOWN_STATUS;
		}
	}
}

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

	WAITING,
	OK_STOP,
	OK_CABLE_PULLED,
	ERROR_CABLE_LOST,
	ERROR_CIRCUIT_BREAKER,
	ERROR_METER,
	ERROR_TIMEOUT,
	ERROR_VENTING,
	ERROR_PMW,
	UNKNOWN_STATUS;
	
	public byte getByte() {
		switch(this) {
			case WAITING:
				return 0;
			case OK_STOP:
				return 1;
			case OK_CABLE_PULLED:
				return 3;
			case ERROR_CABLE_LOST:
				return 4;
			case ERROR_CIRCUIT_BREAKER:
				return 5;
			case ERROR_METER:
				return 6;
			case ERROR_TIMEOUT:
				return 7;
			case ERROR_VENTING:
				return 8;
			case ERROR_PMW:
				return 9;
			default:
				return -1;
		}
	}
	
	public static ChargeCompleteStatus getStatus(byte b) {
		switch(b) {
			case 0:
				return WAITING;
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

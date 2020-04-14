/*
 * Copyright 2016-20 ISC Konstanz
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

public enum ChargeAuthorizationStatus {

	SERVER_REQUESTED_START(0x01),
	START_AUTHORIZED(0x02),
	UNKNOWN(-1);
	
	private final int code;
	
	private ChargeAuthorizationStatus (int code) {
		this.code = code;
	}
	
	 public byte getCode() {
	     return (byte) code;
	 }
		
	public static ChargeAuthorizationStatus newStatus(byte b) {
		switch(b) {
		case (byte) 0x01:
			return SERVER_REQUESTED_START;
		case (byte) 0x02:
			return START_AUTHORIZED;
		default:
			return UNKNOWN;
		}
	}
		
}

/*
 * Copyright 2016-18 ISC Konstanz
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


public class ChargePortEvent {
	
	private final byte message;

	public ChargePortEvent(byte message) {
		this.message = message;
	}

	public boolean hasPortEvent(int port) {
		switch(port) {
		case 1:
			return isBitSet(message, 2);
		case 2:
			return isBitSet(message, 1);
		default:
			return false;
		}
	}

	public boolean hasRfidEvent() {
		return isBitSet(message, 0);
	}

	private static Boolean isBitSet(byte b, int bit) {
	    return (b & (1 << bit)) != 0;
	}
}

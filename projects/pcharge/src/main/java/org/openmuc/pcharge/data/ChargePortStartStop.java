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


public enum ChargePortStartStop {

	STOP (1),
	START_REQUEST (2),
	START (3),
	OPTIMIZED_ACTIVATE (4),
	OPTIMIZED_DEACTIVATE (5),
	INTERRUPT_CHARGING (6), 	//Charging can be interrupted with this command. It's still locked but can be unlocked 
								//by sending this command a second time. Charging can be started again by sending the "START" command. 
	LOCK_MANUALLY(7);

    private final int code;

    private ChargePortStartStop(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
	
    
	public static ChargePortStartStop newStatus(int b) {
		switch(b) {
			case 1:
				return STOP;
			case 2:
				return START_REQUEST;
			case 3:
				return START;
			case 4:
				return OPTIMIZED_ACTIVATE;
			case 5:
				return OPTIMIZED_DEACTIVATE;
			case 6:
				return INTERRUPT_CHARGING;
			case 7:
				return LOCK_MANUALLY;
			default:
				throw new IllegalArgumentException("Invalid charge port status: " + b);
		}
	}
}

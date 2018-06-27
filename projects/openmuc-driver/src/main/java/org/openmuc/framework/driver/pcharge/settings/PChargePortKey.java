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
package org.openmuc.framework.driver.pcharge.settings;

public enum PChargePortKey {

	STATUS,
	CABLE_VOLTAGE,
	CABLE_CURRENT,
	CHARGING_CURRENT,
	VENTILATION_REQUEST, 
	CHARGING_AUTHORIZATION_STATUS,
	CHARGING_OPTIMIZED,
	CHARGING_COMPLETE_STATUS,
	CHARGING_DURATION,
	CHARGING_ENERGY,
	CHARGING_DURATION_LAST,
	CHARGING_ENERGY_TOTAL,
	CHARGING_CYCLE_COUNTER,
	LOCKED,
	CONTACTOR,
	RCD,
	PWM_MINIMUM,
	PWM_MAXIMUM,
	BUTTON_1_START,
	BUTTON_2_STOP,
	BUTTON_3_OPTIMIZED,
	BUTTON_4_SPARE,
	LED_RED_ERROR,
	LED_GREEN_READY,
	LED_ORANGE_OPTIMIZED,
	LED_SPARE,
	RFID_LOGIN,
	RFID_GROUP,
	RFID_CARD,
	RFID_USER,
	EVENT_PORT,
	EVENT_RFID,
	UNKNOWN;
}

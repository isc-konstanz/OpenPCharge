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
package org.openmuc.framework.driver.pcharge;

import org.openmuc.framework.driver.Channel;
import org.openmuc.framework.options.Address;
import org.openmuc.framework.options.Setting;

public class PChargeChannel extends Channel {

	public enum PChargeKey {
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

	@Address(id = "key",
			name = "Key",
			description = "The unique key, identifying the type of information that should be read of the charge port.",
			valueSelection = "STATUS:Status," +
							"CABLE_VOLTAGE:Cable Voltage," +
							"CABLE_CURRENT:Cable Current Limit," +
							"CHARGING_CURRENT:Charging Current," +
							"CHARGING_AUTHORIZATION_STATUS:Charging Authorization Status," +
							"CHARGING_OPTIMIZED:Charging Optimization," +
							"CHARGING_COMPLETE_STATUS:Charging Complete Status," +
							"CHARGING_DURATION:Charging Duration," +
							"CHARGING_DURATION_LAST:Charging Duration of the last Charge Period," +
							"CHARGING_ENERGY:Charging Energy Consumption," +
							"CHARGING_ENERGY_TOTAL:Total Charging Energy Consumption," +
							"CHARGING_CYCLE_COUNTER:Total Charging Cycles," +
							"VENTILATION_REQUEST:Ventilation request," +
							"LOCKED:Plug Lock," +
							"CONTACTOR:Contactor state," +
							"RCD:RCD state," +
							"PWM_MINIMUM:Minimum Voltage of PWM," +
							"PWM_MAXIMUM:Maximum Voltage of PWM," +
							"BUTTON_1_START:Start Button," +
							"BUTTON_2_STOP:Stop Button," +
							"BUTTON_3_OPTIMIZED:Optimized Charging Button," +
							"BUTTON_4_SPARE:Spare Button," +
							"LED_RED_ERROR:Red Error LED," +
							"LED_GREEN_READY:Green Ready LED," +
							"LED_ORANGE_OPTIMIZED:Orange Optimized Charging Active LED," +
							"LED_SPARE:Spare LED," +
							"RFID_LOGIN:RFID Login," +
							"RFID_GROUP:RFID Groups," +
							"RFID_CARD:RFID Card," +
							"RFID_USER:RFID User," +
							"EVENT_PORT:RFID Port Event," +
							"EVENT_RFID:RFID Event")
	private String key;

	@Setting(id = "chargePort",
			name = "Charge Port",
			description = "The charge port, information should be read for.",
			valueSelection = "1:Port 1," +
							"2:Port 2")
	private int chargePort;

	public PChargeKey getKey() {
		return PChargeKey.valueOf(key);
	}

	public int getChargePort() {
		return chargePort;
	}

}

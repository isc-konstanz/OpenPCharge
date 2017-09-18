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
package org.openmuc.framework.driver.pcharge.options;

import org.openmuc.framework.config.options.ChannelOptions;
import org.openmuc.framework.config.options.Option;
import org.openmuc.framework.config.options.OptionCollection;
import org.openmuc.framework.config.options.OptionSelection;
import org.openmuc.framework.data.ValueType;
import org.openmuc.framework.driver.pcharge.options.helper.ChannelAddressKey;

public class PChargeChannelOptions extends ChannelOptions {

	private static final String DESCRIPTION = "A channel represents a register which contains all information about a device.";
	public static final String CHARGE_PORT = "chargePort";
	public static final String KEY = "key";
	
	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	protected void configureAddress(OptionCollection address) {	
		address.add(chargePort());
		address.add(key());	
	}

	@Override
	protected void configureScanSettings(OptionCollection scanSettings) {
		scanSettings.disable();
	}
	
	private Option chargePort(){
		Option chargePort = new Option(CHARGE_PORT, "Charge Port", ValueType.INTEGER);
		chargePort.setDescription("Select the charge port, the channel should record");
		chargePort.setMandatory(true);
		
		OptionSelection selection = new OptionSelection(ValueType.INTEGER);
		selection.addInteger(1, "Port 1");
		selection.addInteger(2, "Port 2");
		chargePort.setValueSelection(selection);
		
		return chargePort;
	}
	
	private Option key() {
		
		Option key = new Option(KEY, "Key", ValueType.STRING);
		key.setMandatory(true);
		key.setDescription("Select the channel address key, the channel should record");
		
		OptionSelection selection = new OptionSelection(ValueType.STRING);
		selection.addString(ChannelAddressKey.STATUS.name(), "Status");
		selection.addString(ChannelAddressKey.CURRENT_CABLE.name(), "Maximum current of the detected cable");
		selection.addString(ChannelAddressKey.CURRENT_LIMIT.name(), "Limit of current");
		selection.addString(ChannelAddressKey.VENTILATION_REQUEST.name(), "Ventialtion is necessary");
		selection.addString(ChannelAddressKey.STATUS_AUTHORIZATION.name(), "Charge authorization status");
		selection.addString(ChannelAddressKey.OPTIMIZED_CHARGING.name(), "Optimized charging is selected");
		selection.addString(ChannelAddressKey.STATUS_COMPLETE.name(), "Charge complete status");
		selection.addString(ChannelAddressKey.DURATION_CHARGING.name(), "Duration of charging");
		selection.addString(ChannelAddressKey.ENERGY_CHARGING.name(), "Energy consumption of present charging process");
		selection.addString(ChannelAddressKey.DURATION_LAST_CHARGING.name(), "Duration of the last charging process");
		selection.addString(ChannelAddressKey.ENERGY_TOTAL.name(), "Total Energy");
		selection.addString(ChannelAddressKey.COUNTER_CHARGING_CYCLES.name(), "Counter to count charging cycles");
		selection.addString(ChannelAddressKey.LOCKED.name(), "The Plug is Locked");
		selection.addString(ChannelAddressKey.CONTACTOR.name(), "Contacot is aktive");
		selection.addString(ChannelAddressKey.RCD.name(), "RCD is aktive");
		selection.addString(ChannelAddressKey.PWM_MINIMUM.name(), "Minimum Voltage of PWM");
		selection.addString(ChannelAddressKey.PWM_MAXIMUM.name(), "Maximum Voltage of PWM");
		selection.addString(ChannelAddressKey.BUTTON_1_START.name(), "Start button");
		selection.addString(ChannelAddressKey.BUTTON_2_STOP.name(), "Stop button");
		selection.addString(ChannelAddressKey.BUTTON_3_OPTIMIZED.name(), "Optimized charging selection button");
		selection.addString(ChannelAddressKey.BUTTON_4_SPARE.name(), "Spare button");
		selection.addString(ChannelAddressKey.LED_RED_ERROR.name(), "Red LED ERROR");
		selection.addString(ChannelAddressKey.LED_GREEN_READY.name(), "Green LED READY");
		selection.addString(ChannelAddressKey.LED_ORANGE_OPTIMIZED.name(), "Orange LED Optimized charging aktivated");
		selection.addString(ChannelAddressKey.LED_SPARE.name(), "Spare LED");
		selection.addString(ChannelAddressKey.RFID_LOGGED_IN.name(), "RFID logged in");
		selection.addString(ChannelAddressKey.RFID_GROUP.name(), "RFID number of group");
		selection.addString(ChannelAddressKey.RFID_CARD.name(), "RFID number of card");
		selection.addString(ChannelAddressKey.RFID_USER.name(), "RFID username");
		selection.addString(ChannelAddressKey.EVENT_PORT.name(), "Port event");
		selection.addString(ChannelAddressKey.EVENT_RFID.name(), "RFID event");
		key.setValueSelection(selection);
		
		return key;
	}

}

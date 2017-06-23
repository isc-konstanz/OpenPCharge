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

import org.openmuc.framework.config.options.DeviceOptions;
import org.openmuc.framework.config.options.Option;
import org.openmuc.framework.config.options.OptionCollection;
import org.openmuc.framework.config.options.OptionSelection;
import org.openmuc.framework.data.IntValue;
import org.openmuc.framework.data.ValueType;

public class PChargeDeviceOptions extends DeviceOptions {

	private static final String DESCRIPTION = "The description of what a Device represents for this driver.";

	public static final String SKELETON_KEY = "skeleton";
	public static final int SKELETON_DEFAULT = 1;

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	protected void configureAddress(OptionCollection address) {
		// TODO Implement the device address options

		// If no address options are necessary, they may be disabled
		address.disable();
	}

	@Override
	protected void configureSettings(OptionCollection settings) {
		// TODO Implement the device settings options
		
		// The default Syntax of "skeleton1:1,skeleton2:2,..." can be changed
		settings.setSyntax(";", "=");
		
		settings.add(skeleton());
	}

	@Override
	protected void configureScanSettings(OptionCollection scanSettings) {
		// TODO Implement the device scan settings options

	}

	private Option skeleton() {
		
		Option skeleton = new Option(SKELETON_KEY, "Skeleton", ValueType.INTEGER);
		skeleton.setDescription("The description of the skeleton option.");
		skeleton.setMandatory(false);
		
		OptionSelection selection = new OptionSelection(ValueType.INTEGER);
		selection.addInteger(1, "One");
		selection.addInteger(2, "Two");
		selection.addInteger(3, "Three");
		selection.addInteger(4, "Four");
		selection.addInteger(5, "Five");
		skeleton.setValueSelection(selection);
		skeleton.setValueDefault(new IntValue(SKELETON_DEFAULT));
		
		return skeleton;
	}

}

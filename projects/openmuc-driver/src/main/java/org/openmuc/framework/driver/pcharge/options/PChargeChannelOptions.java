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
import org.openmuc.framework.config.options.OptionCollection;

public class PChargeChannelOptions extends ChannelOptions {

	private static final String DESCRIPTION = "The description of what a Channel represents for this driver.";

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	protected void configureAddress(OptionCollection address) {
		// TODO Implement the channel address options
		
	}

	@Override
	protected void configureScanSettings(OptionCollection scanSettings) {
		// TODO Implement the channel scans settings options
		
	}

}

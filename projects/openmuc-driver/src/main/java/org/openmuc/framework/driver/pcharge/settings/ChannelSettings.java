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

import org.openmuc.framework.config.PreferenceType;
import org.openmuc.framework.config.Preferences;

public class ChannelSettings extends Preferences {

    public static final PreferenceType TYPE = PreferenceType.SETTINGS_CHANNEL;

    @Option
    private int chargePort;

    @Override
    public PreferenceType getPreferenceType() {
        return TYPE;
    }

    public int getPort() {
    	return chargePort;
    }

}

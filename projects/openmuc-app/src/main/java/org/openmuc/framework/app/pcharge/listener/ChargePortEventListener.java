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
package org.openmuc.framework.app.pcharge.listener;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.RecordListener;
import org.openmuc.framework.driver.pcharge.options.helper.ChannelAddress;


public class ChargePortEventListener implements RecordListener {
	
	private final int port;
	private final PChargeListenerCallbacks callbacks;
	
	public ChargePortEventListener(PChargeListenerCallbacks callbacks, Channel event) throws ArgumentSyntaxException {
		this.callbacks = callbacks;
		ChannelAddress address = new ChannelAddress(event.getChannelAddress());
		
		port = address.getChargePort();
	}
	
	@Override
	public void newRecord(Record record) {
		if (isValid(record)) {
			if (record.getValue().asBoolean()){
				callbacks.onChargePortEvent(port);
			}
		}
	}
	
	private boolean isValid(Record record) {
		if (record != null && record.getFlag() == Flag.VALID) {
			return true;
		}
		return false;
	}

}


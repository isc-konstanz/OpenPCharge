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
package org.openmuc.framework.app.pcharge.port;

import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.dataaccess.RecordListener;
import org.openmuc.pcharge.data.ChargePortStatus;

public class ChargePortStatusListener implements RecordListener {

	private final ChargePortListenerCallbacks callbacks;

	private final int startIntervalMin;
	private volatile long startTimeLast = 0;
	private volatile ChargePortStatus statusLast = null;

	public ChargePortStatusListener(ChargePortListenerCallbacks callbacks, int interval) {
		this.callbacks = callbacks;
		this.startIntervalMin = interval;
	}

	@Override
	public void newRecord(Record record) {
		if (record != null && record.getFlag() == Flag.VALID) {
			ChargePortStatus status = ChargePortStatus.newStatus(record.getValue().asByte());
			
			if (status == ChargePortStatus.WAIT_FOR_START) {
				if (statusLast == ChargePortStatus.NOT_CONNECTED ||
						System.currentTimeMillis() - startTimeLast >= startIntervalMin) {
					
					callbacks.onChargingStartRequest();
				}
			}
			else if (statusLast != status) {
				switch (status) {
				case CHARGING_PAUSE:
				case CHARGING_PAUSE_OPTIMIZED:
					callbacks.onChargingPaused();
					break;
				case CHARGING_COMPLETE:
					callbacks.onChargingComplete();
					break;
				case CHARGING_ABORTED:
					callbacks.onChargingAborted();
					break;
				case ERROR_CABLE_CURRENT:
					callbacks.onError(ChargePortError.CABLE_CURRENT);
					break;
				case ERROR_LOCKING:
					callbacks.onError(ChargePortError.LOCKING);
					break;
				case ERROR_UNLOCKING:
					callbacks.onError(ChargePortError.UNLOCKING);
					break;
				case ERROR_RELAIS_ON:
					callbacks.onError(ChargePortError.RELAIS_ON);
					break;
				case ERROR_RELAIS_OFF:
					callbacks.onError(ChargePortError.RELAIS_OFF);
					break;
				case ERROR_CONFIG_INVALID:
					callbacks.onError(ChargePortError.CONFIG_INVALID);
					break;
				case ERROR_VENTING:
					callbacks.onError(ChargePortError.VENTING);
					break;
				case PORT_BUSY:
					callbacks.onError(ChargePortError.PORT_BUSY);
					break;
				default:
					break;
				}
			}
			statusLast = status;
		}
	}

}

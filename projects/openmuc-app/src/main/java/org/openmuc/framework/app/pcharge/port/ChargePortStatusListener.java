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
package org.openmuc.framework.app.pcharge.port;

import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.RecordListener;
import org.openmuc.pcharge.data.ChargeCompleteStatus;
import org.openmuc.pcharge.data.ChargePortStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChargePortStatusListener implements RecordListener {
	private static final Logger logger = LoggerFactory.getLogger(ChargePortStatusListener.class);

	private final ChargePortListenerCallbacks callbacks;

	private final String id;
	private final Channel statusComplete;

	public ChargePortStatusListener(ChargePortListenerCallbacks callbacks, String id, Channel statusComplete) {
		this.callbacks = callbacks;
		
		this.id = id;
		this.statusComplete = statusComplete;
	}

	@Override
	public void newRecord(Record record) {
		if (isValid(record)) {
			ChargePortStatus status = ChargePortStatus.newStatus(record.getValue().asByte());

			ChargeCompleteStatus completeStatus = ChargeCompleteStatus.UNKNOWN_STATUS;
			if(statusComplete != null) {
				Record completeStatusRecord = statusComplete.getLatestRecord();
				if (isValid(completeStatusRecord)) {
					completeStatus = ChargeCompleteStatus.newStatus(completeStatusRecord.getValue().asByte());
				}
			}
			
			switch (status) {
			case WAIT_FOR_START:
				logger.debug("Electric vehicle detected at \"{}\", requesting to start charging", id);
				
				callbacks.onWaitForStart();
				break;
			
			case CHARGING_PAUSE:
			case CHARGING_PAUSE_OPTIMIZED:
				logChargingAbort("Paused");
				
				callbacks.onChargingPaused();
				break;
			case CHARGING_COMPLETE:
				switch(completeStatus) {
				case OK_COMPLETE:
					logger.debug("Electric vehicle at \"{}\" completed charging successfully", id);
					break;
				case OK_STOP:
					logger.debug("Electric vehicle at \"{}\" aborted charging: "
							+ "Stopped by user", id);
					
					callbacks.onChargingStopped();
					break;
				default:
					break;
				}
			case CHARGING_ABORTED:
				switch(completeStatus) {
				case OK_STOP:
					logChargingAbort("Stopped by user");
					
					callbacks.onChargingStopped();
					break;
				case OK_CABLE_PULLED:
					logChargingAbort("Cable got removed by user");
					break;
				case ERROR_CABLE_LOST:
					logChargingAbort("Lost contact to cable");
					break;
				case ERROR_CIRCUIT_BREAKER:
					logChargingAbort("Circuit breaker fault detected");
					break;
				case ERROR_METER:
					logChargingAbort("Current meter fault detected");
					break;
				case ERROR_TIMEOUT:
					logChargingAbort("Server timeout");
					
					callbacks.onTimeout();
					break;
				case ERROR_VENTING:
					logChargingAbort("Venting not supported");
					break;
				case ERROR_PMW:
					logChargingAbort("PMW-signal unstable. Charging interrupted to avoid damage");
					break;
				default:
					break;
				}
			case ERROR_CABLE_CURRENT:
				logErrorWarning("Cable current");
				break;
			case ERROR_LOCKING:
				logErrorWarning("Locking");
				break;
			case ERROR_UNLOCKING:
				logErrorWarning("Unlocking");
				break;
			case ERROR_RELAIS_ON:
				logErrorWarning("Switchign relais on");
				break;
			case ERROR_RELAIS_OFF:
				logErrorWarning("Switchign relais off");
				break;
			case ERROR_CONFIG_INVALID:
				logErrorWarning("Confiuration invalid");
				break;
			case ERROR_VENTING:
				logErrorWarning("Ventilation");
				break;
			case PORT_BUSY:
				logErrorWarning("Busy");
				break;
			default:
				break;
			}
		}
	}

	private void logChargingAbort(String message) {
		logger.warn("Electric vehicle at \"{}\" aborted charging: {}", id, message);
	}

	private void logErrorWarning(String message) {
		logger.warn("Error detected at \"{}\": {}", id, message);
	}

	private boolean isValid(Record record) {
		if (record != null && record.getFlag() == Flag.VALID) {
			return true;
		}
		return false;
	}

}

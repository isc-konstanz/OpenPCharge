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
import org.openmuc.pcharge.data.ChargeAuthorizationStatus;
import org.openmuc.pcharge.data.ChargeCompleteStatus;
import org.openmuc.pcharge.data.ChargePortStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ChargePortStatusListener implements RecordListener {
	private static final Logger logger = LoggerFactory.getLogger(ChargePortStatusListener.class);
	
	private final int port;
	private final Channel statusComplete;
	
	private final PChargeListenerCallbacks callbacks;
	
	public ChargePortStatusListener(PChargeListenerCallbacks callbacks, Channel status, Channel statusComplete) throws ArgumentSyntaxException {
		this.callbacks = callbacks;
		this.statusComplete = statusComplete;

		ChannelAddress address = new ChannelAddress(status.getChannelAddress());
		port = address.getChargePort();
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
				logger.debug("Electric vehicle detected on port {}, requesting to start charging", port);
				
				callbacks.onWaitForStart(port);
				break;
			
			case CHARGING_PAUSE:
			case CHARGING_PAUSE_OPTIMIZED:
				logger.debug("Electric vehicle on port {} aborted charging: "
						+ "Paused", port);
				
				callbacks.onChargingPaused(port);
				break;
			case CHARGING_COMPLETE:
				switch(completeStatus) {
				case OK_COMPLETE:
					logger.debug("Electric vehicle on port {} completed charging successfully", port);
					break;
				case OK_STOP:
					logger.debug("Electric vehicle on port {} aborted charging: "
							+ "Stopped by user", port);
					
					callbacks.onChargingStopped(port);
					break;
				default:
					break;
				}
			case CHARGING_ABORTED:
				switch(completeStatus) {
				case OK_STOP:
					logger.debug("Electric vehicle on port {} aborted charging: "
							+ "Stopped by user", port);
					
					callbacks.onChargingStopped(port);
					break;
				case OK_CABLE_PULLED:
					logger.debug("Electric vehicle on port {} aborted charging: "
							+ "Cable got removed by user", port);
					break;
				case ERROR_CABLE_LOST:
					logger.warn("Electric vehicle on port {} aborted charging: "
							+ "Lost contact to cable", port);
					break;
				case ERROR_CIRCUIT_BREAKER:
					logger.warn("Electric vehicle on port {} aborted charging: "
							+ "Circuit breaker fault detected", port);
					break;
				case ERROR_METER:
					logger.warn("Electric vehicle on port {} aborted charging: "
							+ "Current meter fault detected", port);
					break;
				case ERROR_TIMEOUT:
					logger.warn("Electric vehicle on port {} aborted charging: "
							+ "Server timeout", port);
					
					callbacks.onTimeout(port);
					break;
				case ERROR_VENTING:
					logger.warn("Electric vehicle on port {} aborted charging: "
							+ "Venting not supported", port);
					break;
				case ERROR_PMW:
					logger.warn("Electric vehicle on port {} aborted charging: "
							+ "PMW-signal unstable. Charging interrupted to avoid damage", port);
					break;
				default:
					break;
				}
			case ERROR_CABLE_CURRENT:
				logger.warn("Error detected on port {}: "
						+ "Cable current", port);
				break;
			case ERROR_LOCKING:
				logger.warn("Error detected on port {}: "
						+ "Locking", port);
				break;
			case ERROR_UNLOCKING:
				logger.warn("Error detected on port {}: "
						+ "Unlocking", port);
				break;
			case ERROR_RELAIS_ON:
				logger.warn("Error detected on port {}: "
						+ "Switchign relais on", port);
				break;
			case ERROR_RELAIS_OFF:
				logger.warn("Error detected on port {}: "
						+ "Switchign relais off", port);
				break;
			case ERROR_CONFIG_INVALID:
				logger.warn("Error detected on port {}: "
						+ "Confiuration invalid", port);
				break;
			case ERROR_VENTING:
				logger.warn("Error detected on port {}: "
						+ "Ventilation", port);
				break;
			case PORT_BUSY:
				logger.warn("Error detected on port {}: "
						+ "Busy", port);
				break;
			default:
				break;
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


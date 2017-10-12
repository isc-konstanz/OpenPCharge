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
package org.openmuc.framework.app.pcharge;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.openmuc.framework.app.pcharge.listener.ChargePortEventListener;
import org.openmuc.framework.app.pcharge.listener.ChargePortStatusListener;
import org.openmuc.framework.app.pcharge.listener.PChargeListenerCallbacks;
import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.IntValue;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.DataAccessService;
import org.openmuc.framework.dataaccess.ReadRecordContainer;
import org.openmuc.pcharge.data.ChargeAuthorizationStatus;
import org.openmuc.pcharge.data.ChargePortStartStop;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component(service = {})
public class PChargeControl extends Thread implements PChargeListenerCallbacks {
	private static final Logger logger = LoggerFactory.getLogger(PChargeControl.class);
	
	private static final int SLEEP_INTERVAL = 5000;
	
	private static final int CURRENT_LIMIT = 16;

	private volatile boolean deactivatedSignal;

	private DataAccessService dataAccessService;

	private Channel chargePortEvent;
	private Channel chargePortStatus;
	private Channel chargePortCompleteStatus;
	private Channel chargePortAuthorizationStatus;
	private Channel chargePortCurrentLimit;


	@Activate
	protected void activate(ComponentContext context) {
		logger.info("Activating P-CHARGE Control");
		setName("OpenMUC P-CHARGE Control App");
		start();
	}

	@Deactivate
	protected void deactivate(ComponentContext context) {
		logger.info("Deactivating P-CHARGE Control");
		deactivatedSignal = true;

		interrupt();
		try {
			this.join();
		} catch (InterruptedException e) {
		}
	}

	@Reference
	protected void bindDataAccessService(DataAccessService dataAccessService) {
		this.dataAccessService = dataAccessService;
	}

	protected void unbindDataAccessService(DataAccessService dataAccessService) {
		this.dataAccessService = null;
	}

	@Override
	public void run() {

		logger.info("P-CHARGE Control started running...");

		if (deactivatedSignal) {
			logger.info("P-CHARGE Control thread interrupted: will stop");
			return;
		}

		chargePortEvent = dataAccessService.getChannel("port1_event");
		chargePortStatus = dataAccessService.getChannel("port1_status");
		chargePortCompleteStatus = dataAccessService.getChannel("port1_status_complete");
		chargePortAuthorizationStatus = dataAccessService.getChannel("port1_status_auth");
		chargePortCurrentLimit = dataAccessService.getChannel("port1_current_limit");
		
		if(chargePortEvent != null && chargePortCompleteStatus != null && chargePortAuthorizationStatus != null && 
				chargePortCurrentLimit != null && chargePortStatus != null) {
			
			try {
				ChargePortEventListener eventListener = new ChargePortEventListener(this, chargePortEvent);
				chargePortEvent.addListener(eventListener);

				ChargePortStatusListener statusListener = new ChargePortStatusListener(this, chargePortStatus, chargePortCompleteStatus);
				chargePortStatus.addListener(statusListener);

				while (!deactivatedSignal) {
					
					try {
						Thread.sleep(SLEEP_INTERVAL);
					} catch (InterruptedException e) {
					}
				}
				
			} catch (ArgumentSyntaxException e) {
				logger.warn("Unable to create a charge port status listener");
			}
		}
		else {
			logger.error("Necessary channels are not configured");
		}
	}

	private void startCharging(int port) {
		long time = System.currentTimeMillis();
		
		List<Record> records = new LinkedList<Record>();
		records.add(new Record(new IntValue(ChargePortStartStop.OPTIMIZED_AKTIVATE.getCode()), time));
		records.add(new Record(new IntValue(ChargePortStartStop.START.getCode()), time));
		
		chargePortStatus.write(records);
		chargePortCurrentLimit.write(new IntValue(CURRENT_LIMIT));
	}

	private void restartCharging(int port) {
		long time = System.currentTimeMillis();
		
		List<Record> records = new LinkedList<Record>();
		records.add(new Record(new IntValue(ChargePortStartStop.INTERRUPT_CHARGING.getCode()), time));
		records.add(new Record(new IntValue(ChargePortStartStop.OPTIMIZED_AKTIVATE.getCode()), time));
		records.add(new Record(new IntValue(ChargePortStartStop.START.getCode()), time));
		
		chargePortStatus.write(records);
		try {
			Thread.sleep(SLEEP_INTERVAL);
			
		} catch (InterruptedException e) {
		}
		chargePortCurrentLimit.write(new IntValue(CURRENT_LIMIT));
	}

	@Override
	public void onWaitForStart(int port) {
		logger.debug("Listener recognized start charging signal for port {}", port);
		startCharging(port);
	}

	@Override
	public void onChargingPaused(int port) {
		logger.warn("Charging paused for port {}", port);
		restartCharging(port);
	}

	@Override
	public void onChargingStopped(int port) {
		logger.warn("Charging stopped for port {}", port);

		ChargeAuthorizationStatus authStatus = ChargeAuthorizationStatus.UNKNOWN;
		if(authStatus != null) {
			Record authStatusRecord = chargePortAuthorizationStatus.getLatestRecord();
			if (authStatusRecord != null && authStatusRecord.getFlag() == Flag.VALID) {
				authStatus = ChargeAuthorizationStatus.newStatus(authStatusRecord.getValue().asByte());
				
				if (authStatus == ChargeAuthorizationStatus.SERVER_REQUESTED_START) {
					restartCharging(port);
				}
			}
		}
	}

	@Override
	public void onTimeout(int port) {
		// TODO Check behavior on timeout
		logger.warn("Timeout: port {}", port);
		restartCharging(port);
	}

	@Override
	public void onChargePortEvent(int port) {
		logger.debug("Charge port event detected on port {}", port);

		List<ReadRecordContainer> recordContainers = new ArrayList<ReadRecordContainer>();
		recordContainers.add(chargePortStatus.getReadContainer());
		recordContainers.add(chargePortCompleteStatus.getReadContainer());
		
		dataAccessService.read(recordContainers);
	}
}
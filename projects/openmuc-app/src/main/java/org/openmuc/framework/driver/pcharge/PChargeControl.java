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
package org.openmuc.framework.driver.pcharge;

import org.openmuc.framework.data.IntValue;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.StringValue;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.DataAccessService;
import org.openmuc.framework.dataaccess.RecordListener;
import org.openmuc.pcharge.data.ChargePortStatus;
import org.openmuc.pcharge.data.CmdId;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component(service = {})
public class PChargeControl extends Thread {
	private static final Logger logger = LoggerFactory.getLogger(PChargeControl.class);
	
	private static final int SLEEP_INTERVAL = 5000;

	private volatile boolean deactivatedSignal;

	// With the dataAccessService you can access to your measured and control data of your devices.
	private DataAccessService dataAccessService;

	private Channel chargePortEvent;
	private Channel chargePortStatus;
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

		logger.info("Demo App started running...");

		if (deactivatedSignal) {
			logger.info("DemoApp thread interrupted: will stop");
			return;
		}

		chargePortEvent = dataAccessService.getChannel("chargePortEvent");
		chargePortStatus = dataAccessService.getChannel("chargePortStatus");
		chargePortCurrentLimit = dataAccessService.getChannel("chargePortCurrentLimit");
		
		chargePortEvent.addListener(new RecordListener() {
			@Override
			public void newRecord(Record record) {
				if (record.getValue() != null) {
					if (record.getValue().asBoolean()) {
						handleChargePortEvent(1);
					}
				}
			}
		});

		while (!deactivatedSignal) {
			
			try {
				Thread.sleep(SLEEP_INTERVAL);
			} catch (InterruptedException e) {
			}
		}
	}
	
	private void handleChargePortEvent(int port) {

		// Get latest status event and e.g. start 
		Record record = chargePortStatus.read();
		if (record != null && record.getValue() != null) {
			ChargePortStatus status = ChargePortStatus.newStatus(record.getValue().asByte());
			if (status == ChargePortStatus.WAIT_FOR_START) {
				
				// TODO: continue example
				chargePortCurrentLimit.write(new IntValue(16));
				chargePortStatus.write(new StringValue(CmdId.STARTSTOP.getId()));
			}
		}
	}
}


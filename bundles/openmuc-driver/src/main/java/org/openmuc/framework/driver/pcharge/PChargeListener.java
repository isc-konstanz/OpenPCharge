/*
 * Copyright 2016-20 ISC Konstanz
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openmuc.framework.data.BooleanValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.driver.spi.ChannelRecordContainer;
import org.openmuc.framework.driver.spi.RecordsReceivedListener;
import org.openmuc.pcharge.PChargeException;
import org.openmuc.pcharge.PChargeMessage;
import org.openmuc.pcharge.PChargeSocket;
import org.openmuc.pcharge.data.ChargePortEvent;
import org.openmuc.pcharge.data.MsgId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PChargeListener implements Runnable {
	private final static Logger logger = LoggerFactory.getLogger(PChargeListener.class);

	private static final int SLEEP_INTERVAL = 100;

	private final List<PChargeChannel> channels;

	private final RecordsReceivedListener listener;
	private final PChargeSocket connection;

	private volatile boolean running = false;
	private volatile boolean listening = false;
	private volatile boolean pauseFlag = false;

	public PChargeListener(List<PChargeChannel> channels, RecordsReceivedListener listener, PChargeSocket connection) {
		this.channels = channels;
		this.listener = listener;
		this.connection = connection;
		
		this.running = true;
	}

	public void start() {
		logger.debug("Signal P-CHARGE info listener to continue");
		
		listening = true;
	}

	public void stop() {
		logger.debug("Signal P-CHARGE info listener to pause");
		
		pauseFlag = true;
		while (pauseFlag) {
			try {
				Thread.sleep(SLEEP_INTERVAL);
			} catch (InterruptedException e) {
			}
		}
	}

	public void terminate() {
		logger.debug("Signal P-CHARGE info listener to terminate");
		
		running = false;
		Thread.currentThread().interrupt();
		try {
			Thread.sleep(SLEEP_INTERVAL);
		} catch (InterruptedException e) {
		}
	}

	@Override
	public void run() {
		
		while (running) {
			if (pauseFlag) {
				listening = false;
				pauseFlag = false;
			}
			else if (listening) {
				try {
					PChargeMessage message = null;
					synchronized(connection) {
						message = connection.read();
					}
					
					if (message != null && message.getMsgId() == MsgId.INFO) {
						handleInfoMessage(message);
					}
				} catch (IOException | PChargeException e) {
					logger.warn("Error while listening at P-CHARGE connection: {}", e.getMessage());
				}
			}
			
			try {
				Thread.sleep(SLEEP_INTERVAL);
			} catch (InterruptedException e) {
			}
		}
	}

	public void handleInfoMessage(PChargeMessage message) {
		List<ChannelRecordContainer> containers = new ArrayList<ChannelRecordContainer>();
		
		switch(message.getCmdId()) {
		case INFO:
			ChargePortEvent event = new ChargePortEvent(message.getMessage()[0]);
			
			long samplingTime = System.currentTimeMillis();

			for (PChargeChannel channel : channels) {
				int port = channel.getChargePort();
				
				Value value = null;
				switch (channel.getKey()) {
				case EVENT_PORT:
					boolean portEvent = event.hasPortEvent(port);
					if (portEvent) {
						value = new BooleanValue(portEvent);
					
						logger.debug("Event at port: {}", port);
					}
					break;
				case EVENT_RFID:
					boolean rfidEvent = event.hasRfidEvent();
					if (rfidEvent) {
						value = new BooleanValue(rfidEvent);
						
						logger.debug("RFID Event recognized");
					}
					break;
				default:
					logger.warn("Unable to listen for Charge Port Key: {}", channel.getKey().name());
					break;
				}
				
				if (value != null) {
					channel.setRecord(new Record(value, samplingTime, Flag.VALID));
					containers.add(channel);
				}
			}
			listener.newRecords(containers);
			break;
		default:
			logger.warn("Unknown Command ID: {}", message.getCmdString());
			break;
		}
	}
}

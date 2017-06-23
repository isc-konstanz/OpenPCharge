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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.ChannelScanInfo;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.driver.pcharge.options.helper.ChannelAddress;
import org.openmuc.framework.driver.pcharge.options.helper.DeviceSettings;
import org.openmuc.framework.driver.spi.ChannelRecordContainer;
import org.openmuc.framework.driver.spi.ChannelValueContainer;
import org.openmuc.framework.driver.spi.Connection;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.RecordsReceivedListener;
import org.openmuc.pcharge.PChargeException;
import org.openmuc.pcharge.PChargeMessage;
import org.openmuc.pcharge.PChargeSocket;
import org.openmuc.pcharge.data.ChargePortInfo;
import org.openmuc.pcharge.data.CmdId;
import org.openmuc.pcharge.data.MsgId;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component
public class PChargeConnection implements Connection {
	private final static Logger logger = LoggerFactory.getLogger(PChargeConnection.class);

	private final ExecutorService executor;
	private final PChargeSocket connection;

	private PChargeListener listener = null;

	public PChargeConnection(DeviceSettings settings) throws ConnectionException {
		
		int port = settings.getTcpPort();
		
		logger.info("Opening P-CHARGE TCP connection at port {}", port);
		try {
			connection = new PChargeSocket(port);
			
		} catch (IOException e) {
			throw new ConnectionException("Unable to open P-CHARGE connection: " + e.getMessage());
		}
		
		executor = Executors.newFixedThreadPool(1);
	}

	@Override
	public List<ChannelScanInfo> scanForChannels(String settings)
			throws UnsupportedOperationException, ArgumentSyntaxException, ScanException, ConnectionException {
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object read(List<ChannelRecordContainer> containers, Object containerListHandle, String samplingGroup)
			throws UnsupportedOperationException, ConnectionException {

		long samplingTime = System.currentTimeMillis();
		try {
			if (listener != null) {
				listener.stop();
			}
			synchronized(connection) {
				connection.write(CmdId.READ_CHARGEPORT_STATUS);

				PChargeMessage message = connection.read();
				if (message.getMsgId() == MsgId.INFO) {
					switch(message.getCmdId()) {
						case READ_CHARGEPORT_STATUS:
							ChargePortInfo chargePort = new ChargePortInfo(message.getMessage());
							
							for (ChannelRecordContainer container : containers) {
								try {
									ChannelAddress address = new ChannelAddress(container.getChannelAddress());
									
									int port = address.getChargePort();
									
									// TODO check address info to retrieve specific channels, not just the exemplary energy
									
									Value value = new DoubleValue(chargePort.getEnergyTotal(port));
									container.setRecord(new Record(value, samplingTime, Flag.VALID));
									
									logger.debug("Received total energy charged value for port {}: {}", port, value);
									
								} catch (ArgumentSyntaxException e) {
									logger.warn("Unable to configure channel address \"{}\": {}", container.getChannelAddress(), e);
									container.setRecord(new Record(null, samplingTime, Flag.DRIVER_ERROR_READ_FAILURE));
								}
							}
							break;
						default:
							logger.warn("Received unknown command id: {}", message.getCmdString());
							break;
					}
				}
			}
			if (listener != null) {
				listener.start();
			}
			
		} catch (PChargeException e) {
			logger.debug("Error while reading P-CHARGE connection: {}", e.getMessage());
			
		} catch (IOException e) {
			throw new ConnectionException("P-CHARGE connection failed: " + e.getMessage());
		}
		
		return null;
	}

	@Override
	public void startListening(List<ChannelRecordContainer> containers, RecordsReceivedListener listener)
			throws UnsupportedOperationException, ConnectionException {

		if (this.listener != null) {
			this.listener.terminate();
		}
		this.listener = new PChargeListener(containers, listener, connection);
		
		executor.execute(this.listener);
	}

	@Override
	public Object write(List<ChannelValueContainer> containers, Object containerListHandle)
			throws UnsupportedOperationException, ConnectionException {
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void disconnect() {
		logger.info("Closing P-CHARGE connection");

		connection.close();
        executor.shutdown();
	}
}

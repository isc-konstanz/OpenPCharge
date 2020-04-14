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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.openmuc.framework.data.BooleanValue;
import org.openmuc.framework.data.ByteValue;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.IntValue;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.TypeConversionException;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.driver.Device;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.RecordsReceivedListener;
import org.openmuc.framework.options.Address;
import org.openmuc.framework.options.AddressSyntax;
import org.openmuc.pcharge.PChargeException;
import org.openmuc.pcharge.PChargeMessage;
import org.openmuc.pcharge.PChargeSocket;
import org.openmuc.pcharge.data.ChargeAuthorizationStatus;
import org.openmuc.pcharge.data.ChargeCompleteStatus;
import org.openmuc.pcharge.data.ChargePortInfo;
import org.openmuc.pcharge.data.ChargePortStartStop;
import org.openmuc.pcharge.data.ChargePortStatus;
import org.openmuc.pcharge.data.CmdId;
import org.openmuc.pcharge.data.MsgId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AddressSyntax(separator = ",", assignmentOperator = ":", keyValuePairs = true)
public class PChargeConnection extends Device<PChargeChannel> {
	private final static Logger logger = LoggerFactory.getLogger(PChargeConnection.class);

	private static final int CURRENT_LIMIT = 32;

	@Address(id = "tcpPort",
			name = "TCP port",
			description = "The TCP Port to listen on, as configured in the EWS-Box configurations.",
			mandatory = false)
	private int port;

	private ExecutorService executor;

	private PChargeSocket connection;
	private PChargeListener listener;

    @Override
    protected void onCreate() {
		executor = Executors.newFixedThreadPool(1);
    }

    @Override
    protected void onConnect() throws ConnectionException {
		logger.info("Opening P-CHARGE TCP connection at port {}", port);
		try {
			connection = new PChargeSocket(port);
			
		} catch (IOException e) {
			throw new ConnectionException("Unable to open P-CHARGE connection: " + e.getMessage());
		}
    }

    @Override
    public void onDisconnect() {
		logger.info("Closing P-CHARGE connection");

		connection.close();
    }

    @Override
    public void onDestroy() {
    	executor.shutdown();
    }

	@Override
	public void onStartListening(List<PChargeChannel> channels, RecordsReceivedListener listener)
			throws UnsupportedOperationException, ConnectionException {
		
		if (this.listener != null) {
			this.listener.terminate();
		}
		this.listener = new PChargeListener(channels, listener, connection);
		executor.execute(this.listener);
	}

	@Override
	protected Object onRead(List<PChargeChannel> channels, Object containerListHandle, String samplingGroup)
            throws UnsupportedOperationException, ConnectionException {

		long samplingTime = System.currentTimeMillis();

		if (listener != null) {
			listener.stop();
		}
		try {
			PChargeMessage message = null;
			synchronized(connection) {
				try {
					connection.write(CmdId.READ_CHARGEPORT_STATUS);
	
					message = connection.read();
					if (message != null && message.getMsgId() == MsgId.INFO) {
						// If an info message got received first, read again for the correct answer.
						// TODO: Check if the info message may be necessary for future versions with RFID events
						message = connection.read();
					}
				} catch (IOException e) {
					connection.close();
					throw new ConnectionException("Connection to EWS-Box failed: " + e.getMessage());
				}
			}
			
			if (message != null && message.getMsgId() == MsgId.ANSWER) {
				switch(message.getCmdId()) {
					case READ_CHARGEPORT_STATUS:
						ChargePortInfo chargePort = new ChargePortInfo(message.getMessage());
						
						for (PChargeChannel channel : channels) {
							int port = channel.getChargePort();
							
							Value value = null;
							switch(channel.getKey()) {
							case STATUS:
								ChargePortStatus status = chargePort.getStatus(port);

								value = new ByteValue(status.getCode());
								logger.debug("Read Status of Port {}: {}", port, value);
								break;
							case CABLE_CURRENT:
								int current = chargePort.getCableCurrent(port);
								
								value = new IntValue(current);
								logger.debug("Read Cable Current of Port {}: {} A", port, value);
								break;
							case CHARGING_CURRENT:
								int currentLimit = chargePort.getCurrentLimit(port);
								
								value = new IntValue(currentLimit);
								logger.debug("Read Charging Current of Port {}: {} A", port, value);
								break;
							case VENTILATION_REQUEST:
								boolean ventilationRequested = chargePort.ventilationIsRequested(port);
								
								value = new BooleanValue(ventilationRequested);
								logger.debug("Read Ventilation Request of Port {}: {}", port,value);
								break;
							case CHARGING_AUTHORIZATION_STATUS:
								ChargeAuthorizationStatus authorizationStatus = chargePort.getChargeAuthorizationStatus(port);
								
								value = new ByteValue(authorizationStatus.getCode());
								logger.debug("Read Charging Authorization Status of Port {}: {}", port, value);
								break;
							case CHARGING_OPTIMIZED:
								boolean isOptimized = chargePort.isOptimized(port);
								
								value = new BooleanValue(isOptimized);
								logger.debug("Read Charging Optimization of Port {}: {}", port,value);
								break;
							case CHARGING_COMPLETE_STATUS:
								ChargeCompleteStatus completeStatus = chargePort.getCompleteStatus(port);
								
								value = new ByteValue(completeStatus.getCode());
								logger.debug("Read Charging Complete Status of Port {}: {}", port, value);
								break;
							case CHARGING_DURATION:
								int chargingDuration = chargePort.getDurationCharging(port);
								
								value = new IntValue(chargingDuration);
								logger.debug("Read Charging Duration of Port {}: {} seconds", port, value);
								break;
							case CHARGING_ENERGY:
								double chargingEnergy = ((double) chargePort.getEnergyCharging(port));
								
								value = new DoubleValue(chargingEnergy);
								logger.debug("Read Charging Energy Consumption of Port {}: {} Wh", port, value);
								break;
							case CHARGING_DURATION_LAST:
								int chargingDurationLast = chargePort.getLastChargingTime(port);
								
								value = new IntValue(chargingDurationLast);
								logger.debug("Read Charging Duration of last Charging Period of Port {}: {} seconds", port, value);
								break;
							case CHARGING_ENERGY_TOTAL:
								double chargingEnergyTotal = ((double) chargePort.getEnergyTotal(port))/1000; //change unit from Wh to kWh
								
								value = new DoubleValue(chargingEnergyTotal);
								logger.debug("Read Total Energy Consumption of Port {}: {} kWh", port, value);
								break;
							case CHARGING_CYCLE_COUNTER:
								int chargingCyclesCounter = chargePort.getChargingCycleCounter(port);
								
								value = new IntValue(chargingCyclesCounter);
								logger.debug("Read Charging Cycles of Port {}: {}", port, value);
								break;
							case LOCKED:
								boolean isLocked = chargePort.isLocked(port);
								
								value = new BooleanValue(isLocked);
								logger.debug("Read Plug Lock State of Port {}: {}", port, value);
								break;
							case CONTACTOR:
								boolean contactorState = chargePort.contactorIsAktive(port);
								
								value = new BooleanValue(contactorState);
								logger.debug("Read Contactor State of Port {}: {}", port, value);
								break;
							case RCD:
								boolean rcdState = chargePort.contactorIsAktive(port);
								
								value = new BooleanValue(rcdState);
								logger.debug("Read RCD State of Port {}: {}", port, value);
								break;
							case PWM_MINIMUM:
								int pwmMinimum = chargePort.getPwmMinimum(port);
								
								value = new IntValue(pwmMinimum);
								logger.debug("Read Minimum PWM Voltage of Port {}: {} V/100", port, value);
								break;
							case PWM_MAXIMUM:
								int pwmMaximum = chargePort.getPwmMaximum(port);
								
								value = (new IntValue(pwmMaximum));
								logger.debug("Read Maximim PWM Voltage of Port {}: {} V/100", port, value);
								break;
							case CABLE_VOLTAGE:
								int voltage = chargePort.getVoltage(port);
								
								value = new IntValue(voltage);
								logger.debug("Read Cable Voltage of Port {}: {} V/100", port, value);
								break;
							case BUTTON_1_START:
								boolean button1Start = chargePort.button1Start(port);
								
								value = new BooleanValue(button1Start);
								logger.debug("Read Start Button State of Port {}: {}", port, value);
								break;
							case BUTTON_2_STOP:
								boolean button2Stop = chargePort.button2Stop(port);
								
								value = new BooleanValue(button2Stop);
								logger.debug("Read Stop Button State of Port {}: {}", port, value);
								break;
							case BUTTON_3_OPTIMIZED:
								boolean button3Optimized = chargePort.button3OptimizedCharging(port);
								
								value = new BooleanValue(button3Optimized);
								logger.debug("Read Optimized Charging Button State of Port {}: {}", port, value);
								break;
							case BUTTON_4_SPARE:
								boolean button4Spare = chargePort.button4Spare(port);
								
								value = new BooleanValue(button4Spare);
								logger.debug("Read Spare Button State of Port {}: {}", port, value);
								break;
							case LED_RED_ERROR:
								boolean ledRedError = chargePort.ledRedError(port);
								
								value = new BooleanValue(ledRedError);
								logger.debug("Read Red Error LED State of Port {}: {}", port, value);
								break;
							case LED_GREEN_READY:
								boolean ledGreenReady = chargePort.ledGreenReady(port);
								
								value = new BooleanValue(ledGreenReady);
								logger.debug("Read Green Ready LED State of Port {}: {}", port, value);
								break;
							case LED_ORANGE_OPTIMIZED:
								boolean ledOrangeOptimized = chargePort.ledOrangeOptimizedCharging(port);
								
								value = new BooleanValue(ledOrangeOptimized);
								logger.debug("Read Orange Optimized Charging LED State of Port {}: {}", port, value);
								break;
							case LED_SPARE:
								boolean ledSpare = chargePort.ledSpare(port);
								
								value = new BooleanValue(ledSpare);
								logger.debug("Read Spare LED State of Port {}: {}", port, value);
								break;
							case RFID_LOGIN:
								boolean rfidLoggedIn = chargePort.rfidLoggedIn(port);
								
								value = new BooleanValue(rfidLoggedIn);
								logger.debug("Read RFID Login State of Port {}: {}", port, value);
								break;
							case RFID_GROUP:
								int rfidNumberOfGroup = chargePort.getRfidNumberOfGroup(port);
								
								value = new IntValue(rfidNumberOfGroup);
								logger.debug("Read RFID Group Number of Port {}: {}", port, value);
								break;
							case RFID_CARD:
								int rfidNumberOfCard = chargePort.getRfidNumberOfCard(port);
								
								value = new IntValue(rfidNumberOfCard);
								logger.debug("Read RFID Card Number of Port {}: {}", port, value);
								break;
							case RFID_USER:
								int rfidUser = chargePort.getRfidUser(port);
								
								value = new IntValue(rfidUser);
								logger.debug("Read RFID User of Port {}: {} ", port, value);
								break;
							default:
								logger.warn("Unknown Charge Port Key for channel: {}" + channel.getId());
								channel.setFlag(Flag.DRIVER_ERROR_CHANNEL_ADDRESS_SYNTAX_INVALID);
							}
							channel.setRecord(new Record(value, samplingTime, Flag.VALID));
						}
						break;
					
					default:
						logger.warn("Unknown Command ID: {}", message.getCmdString());
						break;
				}
			}
		} catch (PChargeException e) {
			logger.debug("Error while reading P-CHARGE connection: {}", e.getMessage());
		}
		if (listener != null) {
			listener.start();
		}
		return null;
	}

	@Override
    protected Object onWrite(List<PChargeChannel> channels, Object containerListHandle)
            throws UnsupportedOperationException, ConnectionException {
		
		if (listener != null) {
			listener.stop();
		}
		
		PChargeMessage messageInfo = null;
		try {
			for (PChargeChannel channel : channels) {
				int port = channel.getChargePort();
				Value value = channel.getValue();
				
				synchronized(connection) {
					switch(channel.getKey()) {
					case CHARGING_CURRENT:
						logger.debug("Set Current Limit of Port {}: {}A", port, value);
						try {
							int limit = value.asInt();
							
							if (limit > 0 && limit < CURRENT_LIMIT) {
								Integer[] msg = new Integer[2];
								msg[0] = port;
								msg[1] = limit;

								connection.write(CmdId.CURRENT_LIMIT, msg);
								PChargeMessage message = connection.read();
								if (message != null) {
									if (message.getMsgId() == MsgId.INFO) {
										// If an info message got received first, read again for the correct answer.
										messageInfo = message.copy();
										message = connection.read();
									}
									
									if (message.getMsgId() == MsgId.ANSWER && message.getCmdId() == CmdId.CURRENT_LIMIT) {
										channel.setFlag(Flag.VALID);
									}
									else {
										logger.warn("Unknown error while setting current limit of port {} to {}A", port, value);
									}
								}
							}
							else {
								logger.warn("Current limit is out of range. It has to be between 0 and {}", CURRENT_LIMIT);
							}
						}
						catch (TypeConversionException e) {
							logger.warn("Passed Current Limit is not an Integer: {}", value.toString());
							channel.setFlag(Flag.DRIVER_ERROR_CHANNEL_VALUE_TYPE_CONVERSION_EXCEPTION);
						}
						break;
					case STATUS:
						logger.debug("Set Status of Port {}: {}", port, value);
						try {
							ChargePortStartStop startStop = ChargePortStartStop.newStatus(value.asInt());
							
							Integer[] msg = new Integer[2];
							msg [0] = port;
							msg [1] = startStop.getCode();
							
							connection.write(CmdId.STARTSTOP, msg);
							PChargeMessage message = connection.read();
							if (message != null) {
								if (message.getMsgId() == MsgId.INFO) {
									message = connection.read();
								}
								
								if (message.getMsgId() == MsgId.ANSWER && message.getCmdId() == CmdId.STARTSTOP) {
									channel.setFlag(Flag.VALID);
								}
								else {
									logger.warn("Unknown error while setting status of port {}: {}", port, startStop.name());
								}
							}
						} catch (TypeConversionException | IllegalArgumentException e) {
							logger.warn("Type conversion failed: {}", e.getMessage());
							channel.setFlag(Flag.DRIVER_ERROR_CHANNEL_VALUE_TYPE_CONVERSION_EXCEPTION);
						}
						break;
					default:
						logger.warn("Unknown Charge Port Key for channel: {}" + channel.getId());
						break;
					}
				}
			}
		} catch (PChargeException e) {
			logger.debug("Error while reading P-CHARGE connection: {}", e.getMessage());
			
		} catch (IOException e) {
			throw new ConnectionException("Connection to EWS-Box failed: " + e.getMessage());
		}
		if (listener != null) {
			if (messageInfo != null) {
				listener.handleInfoMessage(messageInfo);
			}
			listener.start();
		}
		return null;
	}

}

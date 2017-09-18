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
import org.openmuc.framework.data.BooleanValue;
import org.openmuc.framework.data.ByteValue;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.IntValue;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.TypeConversionException;
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
import org.openmuc.pcharge.data.ChargeAuthorizationStatus;
import org.openmuc.pcharge.data.ChargeCompleteStatus;
import org.openmuc.pcharge.data.ChargePortInfo;
import org.openmuc.pcharge.data.ChargePortStartStop;
import org.openmuc.pcharge.data.ChargePortStatus;
import org.openmuc.pcharge.data.CmdId;
import org.openmuc.pcharge.data.MsgId;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component
public class PChargeConnection implements Connection {
	private final static Logger logger = LoggerFactory.getLogger(PChargeConnection.class);
	private static final int CURRENT_LIMIT = 32;

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
						message = connection.read();
					}
				} catch (IOException e) {
					connection.close();
					throw new ConnectionException("P-CHARGE connection failed: " + e.getMessage());
				}
			}
			
			if (message != null && message.getMsgId() == MsgId.ANSWER) {
				switch(message.getCmdId()) {
					case READ_CHARGEPORT_STATUS:
						ChargePortInfo chargePort = new ChargePortInfo(message.getMessage());
						
						for (ChannelRecordContainer container : containers) {
							try {
								ChannelAddress address = new ChannelAddress(container.getChannelAddress());
								
								int port = address.getChargePort();
								
								Value value = null;
								switch(address.getKey()) {
								case STATUS:
									ChargePortStatus status = chargePort.getStatus(port);

									value = new ByteValue(status.getCode());
									logger.debug("Status of Port {}: {}", port, value);
									break;
								case CURRENT_CABLE:
									int current = chargePort.getCableCurrent(port);
									
									value = new IntValue(current);
									logger.debug("Current of Port {}: {} A", port, value);
									break;
								case CURRENT_LIMIT:
									int currentLimit = chargePort.getCurrentLimit(port);
									
									value = new IntValue(currentLimit);
									logger.debug("Current limit of Port {}: {} A", port, value);
									break;
								case VENTILATION_REQUEST:
									boolean ventilationRequested = chargePort.ventilationIsRequested(port);
									
									value = new BooleanValue(ventilationRequested);
									logger.debug("Ventialtion ist requested for port {}: {}", port,value);
									break;
								case STATUS_AUTHORIZATION:
									ChargeAuthorizationStatus authorizationStatus = chargePort.getChargeAuthorizationStatus(port);
									
									value = new ByteValue(authorizationStatus.getCode());
									logger.debug("Authorization status of Port {}: {}", port, value);
									break;
								case OPTIMIZED_CHARGING:
									boolean isOptimized = chargePort.isOptimized(port);
									
									value = new BooleanValue(isOptimized);
									logger.debug("Ventialtion ist requested for port {}: {}", port,value);
									break;
								case STATUS_COMPLETE:
									ChargeCompleteStatus completeStatus = chargePort.getCompleteStatus(port);
									
									value = new ByteValue(completeStatus.getCode());
									logger.debug("Complete status of Port {}: {}", port, value);
									break;
								case DURATION_CHARGING:
									int durationCharging = chargePort.getDurationCharging(port);
									
									value = new IntValue(durationCharging);
									logger.debug("Duration of present charging process of Port {}: {} seconds", port, value);
									break;
								case ENERGY_CHARGING:
									double energyCharging = ((double) chargePort.getEnergyCharging(port));
									
									value = new DoubleValue(energyCharging);
									logger.debug("Energy of present charing process of Port {}: {} Wh", port, value);
									break;
								case DURATION_LAST_CHARGING:
									int lastChargingTime = chargePort.getLastChargingTime(port);
									
									value = new IntValue(lastChargingTime);
									logger.debug("Duration of last charging process of Port {}: {} seconds", port, value);
									break;
								case ENERGY_TOTAL:
									double energyTotal = ((double) chargePort.getEnergyTotal(port))/1000; //change unit from Wh to kWh
									
									value = new DoubleValue(energyTotal);
									logger.debug("Total Energy consumption of Port {}: {} kWh", port, value);
									break;
								case COUNTER_CHARGING_CYCLES:
									int chargingCyclesCounter = chargePort.getChargingCycleCounter(port);
									
									value = new IntValue(chargingCyclesCounter);
									logger.debug("Amount of charging cycles of Port {}: {}", port, value);
									break;
								case LOCKED:
									boolean isLocked = chargePort.isLocked(port);
									
									value = new BooleanValue(isLocked);
									logger.debug("The plug is locked on port {}: {}", port,value);
									break;
								case CONTACTOR:
									boolean contactorIsAktive = chargePort.contactorIsAktive(port);
									
									value = new BooleanValue(contactorIsAktive);
									logger.debug("Contacor of port {} is aktive: {}", port,value);
									break;
								case RCD:
									boolean rcdIsAktive = chargePort.contactorIsAktive(port);
									
									value = new BooleanValue(rcdIsAktive);
									logger.debug("RCD of port {} is aktive: {}", port,value);
									break;
								case PWM_MINIMUM:
									int pwmMinimum = chargePort.getPwmMinimum(port);
									
									value = new IntValue(pwmMinimum);
									logger.debug("Minimum Voltage of PWM of Port {}: {} V/100", port, value);
									break;
								case PWM_MAXIMUM:
									int pwmMaximum = chargePort.getPwmMaximum(port);
									
									value = (new IntValue(pwmMaximum));
									logger.debug("Maximim Voltage of PWM of Port {}: {} V/100", port, value);
									break;
								case VOLTAGE:
									int voltage = chargePort.getVoltage(port);
									
									value = new IntValue(voltage);
									logger.debug("Voltage of the cable of Port {}: {} V/100", port, value);
									break;
								case BUTTON_1_START:
									boolean button1Start = chargePort.button1Start(port);
									
									value = new BooleanValue(button1Start);
									logger.debug("Button 1 START of port {} is pressed: {}", port,value);
									break;
								case BUTTON_2_STOP:
									boolean button2Stop = chargePort.button2Stop(port);
									
									value = new BooleanValue(button2Stop);
									logger.debug("Button 2 STOP of port {} is pressed: {}", port,value);
									break;
								case BUTTON_3_OPTIMIZED:
									boolean button3Optimized = chargePort.button3OptimizedCharging(port);
									
									value = new BooleanValue(button3Optimized);
									logger.debug("Button  of port {} is pressed: {}", port,value);
									break;
								case BUTTON_4_SPARE:
									boolean button4Spare = chargePort.button4Spare(port);
									
									value = new BooleanValue(button4Spare);
									logger.debug("Button 4 SPARE of port {} is pressed: {}", port,value);
									break;
								case LED_RED_ERROR:
									boolean ledRedError = chargePort.ledRedError(port);
									
									value = new BooleanValue(ledRedError);
									logger.debug("Red LED ERROR of port {} is on: {}", port,value);
									break;
								case LED_GREEN_READY:
									boolean ledGreenReady = chargePort.ledGreenReady(port);
									
									value = new BooleanValue(ledGreenReady);
									logger.debug("Green LED READY of port {} is on: {}", port,value);
									break;
								case LED_ORANGE_OPTIMIZED:
									boolean ledOrangeOptimized = chargePort.ledOrangeOptimizedCharging(port);
									
									value = new BooleanValue(ledOrangeOptimized);
									logger.debug("Orange LED OPTIMIZED CHARGING of port {} is on: {}", port,value);
									break;
								case LED_SPARE:
									boolean ledSpare = chargePort.ledSpare(port);
									
									value = new BooleanValue(ledSpare);
									logger.debug("LED SPARE of port {} is on: {}", port,value);
									break;
								case RFID_LOGGED_IN:
									boolean rfidLoggedIn = chargePort.rfidLoggedIn(port);
									
									value = new BooleanValue(rfidLoggedIn);
									logger.debug("RFID is logged in on port {} the following data is valid: {}", port,value);
									break;
								case RFID_GROUP:
									int rfidNumberOfGroup = chargePort.getRfidNumberOfGroup(port);
									
									value = new IntValue(rfidNumberOfGroup);
									logger.debug("The RFID Number of the Group of the registered card on port {} is: {} ", port, value);
									break;
								case RFID_CARD:
									int rfidNumberOfCard = chargePort.getRfidNumberOfCard(port);
									
									value = new IntValue(rfidNumberOfCard);
									logger.debug("The RFID Number of the Card on port {} is: {} ", port, value);
									break;
								case RFID_USER:
									int rfidUser = chargePort.getRfidUser(port);
									
									value = new IntValue(rfidUser);
									logger.debug("The RFID User on port {} is: {} ", port, value);
									break;
								default: 
									throw new ArgumentSyntaxException("Unknown channel address key: " + address.getKey().name());
								}
								container.setRecord(new Record(value, samplingTime, Flag.VALID));
								
							}
							catch (ArgumentSyntaxException e) {
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
		} catch (PChargeException e) {
			logger.debug("Error while reading P-CHARGE connection: {}", e.getMessage());
		}
		if (listener != null) {
			listener.start();
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

		if (listener != null) {
			listener.stop();
		}
		try {
			for (ChannelValueContainer container : containers) {
				try {
					synchronized(connection) {
						ChannelAddress address = new ChannelAddress(container.getChannelAddress());
	
						int port = address.getChargePort();
						Value value = container.getValue();
						
						switch(address.getKey()) {
						case CURRENT_LIMIT:
							logger.debug("Current limit of port {}: {}A", port, value);
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
											message = connection.read();
										}
										
										if (message.getMsgId() == MsgId.ANSWER && message.getCmdId() == CmdId.CURRENT_LIMIT) {
											container.setFlag(Flag.VALID);
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
								logger.warn("The passed current limit is not an Integer: {}", value.toString());
								container.setFlag(Flag.DRIVER_ERROR_CHANNEL_VALUE_TYPE_CONVERSION_EXCEPTION);
							}
							break;
						case STATUS:
							logger.debug("Status of port {}: {}", port, value);
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
										container.setFlag(Flag.VALID);
									}
									else {
										logger.warn("Unknown error while setting status of port {}: {}", port, startStop.name());
									}
								}
							}
							catch (TypeConversionException | IllegalArgumentException e) {
								logger.warn("Type conversion failed: {}", e.getMessage());
								container.setFlag(Flag.DRIVER_ERROR_CHANNEL_VALUE_TYPE_CONVERSION_EXCEPTION);
							}
							
							break;
						default:
							logger.warn("No proper Key");
							break;
						}
					}
				}
				catch (ArgumentSyntaxException e) {
					logger.warn("Unable to configure channel address \"{}\": {}", container.getChannelAddress(), e);
				}
			}
		} catch (PChargeException e) {
			logger.debug("Error while reading P-CHARGE write response: {}", e.getMessage());
			
		} catch (IOException e) {
			connection.close();
			throw new ConnectionException("P-CHARGE connection failed: " + e.getMessage());
		}
		if (listener != null) {
			listener.start();
		}
		return null;
	}

	@Override
	public void disconnect() {
		logger.info("Closing P-CHARGE connection");

		connection.close();
        executor.shutdown();
	}
}

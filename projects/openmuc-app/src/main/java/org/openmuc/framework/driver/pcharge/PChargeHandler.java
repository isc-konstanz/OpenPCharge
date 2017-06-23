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

public class PChargeHandler {

//	public PChargeHandler(int tcpPort) {
//		this.tcpPort = tcpPort;
//		
//		executor = Executors.newFixedThreadPool(2);
//	}
//
//	public void init() {
//		if (connection == null) {
//			logger.info("Opening P-Charge TCP connection");
//			running = true;
//			
//			try {
//				connection = new PChargeSocket(this, tcpPort);
//				executor.execute(connection);
//				
//			} catch (IOException e) {
//				logger.warn("Error while initiating P-Charge connection");
//			}
//		}
//	}
//
//	public void close() {
//		logger.info("Closing P-Charge TCP connection");
//		running = false;
//		
//		if (connection != null) {
//			connection.terminate();
//			connection = null;
//		}
//        executor.shutdown();
//	}
//
//	public HashMap<String, Float> read(int port) {
//		HashMap<String, Float> result = null;
//		
//		if (ports.containsKey(port)) {
//			ChargePortInfo status = ports.get(port);
//			
//			result = new HashMap<String, Float>(1);
//			result.put("energyIn", Float.valueOf(status.getEnergyTotal()));
//			if (status.isCharging()) {
//				result.put("status", 2f);
//			}
//			else if (status.isConnected()) {
//				result.put("status", 1f);
//			}
//			else {
//				result.put("status", 0f);
//			}
//		}
//		return result;
//	}
//
//	public void startCharging(int port) throws IOException {
//		if (logger.isTraceEnabled()) {
//			logger.trace("Requesting to signal charge port {} to start charging", port);
//		}
//
//		Integer[] msg = new Integer[2];
//		msg[0] = port;
//		msg[1] = 3;
//		
////		writeCmd(MsgId.STARTSTOP, msg);
//	}
//
//	public void setCurrentLimit(int port, int limit) throws IOException {
//		if (logger.isTraceEnabled()) {
//			logger.trace("Requesting to set current limit of charge port {}: {}A", port, limit);
//		}
//		
//		Integer[] msg = new Integer[2];
//		msg[0] = port;
//		msg[1] = limit;
//		
////		writeCmd(MsgId.CURRENT_LIMIT, msg);
//	}
//
//	public void requestChargePortStatus() throws IOException {
//		if (logger.isTraceEnabled()) {
//			logger.trace("Requesting to read current charge port status");
//		}
//		
////		writeCmd(MsgId.READ_CHARGEPORT_STATUS);
//	}
//
//	@Override
//	public void onConnectionAccepted(String address) {
//		logger.debug("Connected to EWS-Box TCP client {}", address);
//		
//		Thread scheduler = new Thread("P-Charge read scheduler") {
//			@Override
//		    public void run(){
//				try {
//					while(!isInterrupted()) {
//						long start = System.currentTimeMillis();
//						connection.requestChargePortStatus();
//						
//						long sleep = SLEEP_INTERVAL - (System.currentTimeMillis() - start);
//						if (sleep > 0) {
//							try {
//								Thread.sleep(sleep);
//							} catch (InterruptedException e) {
//							}
//						}
//					}
//				} catch (IOException e) {
//					logger.debug("Error requesting new charge port status: {}", e.getMessage());
//					if (connection != null) {
//						connection.terminate();
//					}
//				}
//		    }
//		};
//		executor.execute(scheduler);
//	}
//
//	@Override
//	public void onConnectionClosed() {
//		logger.debug("Connection to EWS-Box TCP client closed");
//		if (running) {
//			try {
//				connection = new PChargeSocket(this, tcpPort);
//				executor.execute(connection);
//				
//			} catch (IOException e) {
//				logger.warn("Error while initiating P-Charge connection");
//			}
//		}
//	}
//
//	@Override
//	public void onReceivedChargePortData(ChargePortInfo port) {
//		ports.put(port.getPort(), port);
//		
//		// TODO Automatic start only temporary solution
//		if (port != null) {
//			switch(port.getStatus()) {
//				case WAIT_FOR_START:
//					logger.debug("Electric vehicle detected on port {}, requesting to start charging", port.getPort());
//					
//					try {
//						connection.startCharging(port.getPort());
//						 
//			        } catch (IOException e) {
//			            logger.warn("Error while trying to start charging at port {}: {}", port.getPort(), e.getMessage());
//			        }
//					break;
//				case CHARGING_COMPLETE:
//					if (port.getCompleteStatus() == ChargeCompleteStatus.OK_STOP) {
//						logger.debug("Electric vehicle on port {} completed charging successfully", port.getPort());
//					}
//					break;
//				case CHARGING_ABORTED:
//					switch(port.getCompleteStatus()) {
//						case OK_CABLE_PULLED:
//							logger.debug("Electric vehicle on port {} aborted charging: "
//									+ "Cable got removed by user", port.getPort());
//							break;
//						case ERROR_CABLE_LOST:
//							logger.warn("Electric vehicle on port {} aborted charging: "
//									+ "Lost contact to cable", port.getPort());
//							break;
//						case ERROR_CIRCUIT_BREAKER:
//							logger.warn("Electric vehicle on port {} aborted charging: "
//									+ "Circuit breaker fault detected", port.getPort());
//							break;
//						case ERROR_METER:
//							logger.warn("Electric vehicle on port {} aborted charging: "
//									+ "Current meter fault detected", port.getPort());
//							break;
//						case ERROR_TIMEOUT:
//							logger.warn("Electric vehicle on port {} aborted charging: "
//									+ "Server timeout", port.getPort());
//							
//							// TODO Check for 
//							logger.debug("Attempt to continue charging on port {}", port.getPort());
//							try {
//								connection.startCharging(port.getPort());
//								 
//					        } catch (IOException e) {
//					            logger.warn("Error while trying to start charging at port {}: {}", port.getPort(), e.getMessage());
//					        }
//							break;
//						case ERROR_VENTING:
//							logger.warn("Electric vehicle on port {} aborted charging: "
//									+ "Venting not supported", port.getPort());
//							break;
//						case ERROR_PMW:
//							logger.warn("Electric vehicle on port {} aborted charging: "
//									+ "PMW-signal unstable. Charging interrupted to avoid damage", port.getPort());
//							break;
//						default:
//							break;
//					}
//				case ERROR_CABLE_CURRENT:
//					logger.warn("Error detected on port {}: "
//							+ "Cable current", port.getPort());
//					break;
//				case ERROR_LOCKING:
//					logger.warn("Error detected on port {}: "
//							+ "Locking", port.getPort());
//					break;
//				case ERROR_UNLOCKING:
//					logger.warn("Error detected on port {}: "
//							+ "Unlocking", port.getPort());
//					break;
//				case ERROR_RELAIS_ON:
//					logger.warn("Error detected on port {}: "
//							+ "Switchign relais on", port.getPort());
//					break;
//				case ERROR_RELAIS_OFF:
//					logger.warn("Error detected on port {}: "
//							+ "Switchign relais off", port.getPort());
//					break;
//				case ERROR_CONFIG_INVALID:
//					logger.warn("Error detected on port {}: "
//							+ "Confiuration invalid", port.getPort());
//					break;
//				case ERROR_VENTING:
//					logger.warn("Error detected on port {}: "
//							+ "Ventilation", port.getPort());
//					break;
//				case PORT_BUSY:
//					logger.warn("Error detected on port {}: "
//							+ "Busy", port.getPort());
//					break;
//				default:
//					break;
//			}
//		}
//	}
//
//	@Override
//	public void onReceivedChargingStart(int port) {
//		logger.debug("Electric vehicle successfully started charging at port {}", port);
//
//		// TODO Automatic current limit only temporary solution
//		try {
//			connection.setCurrentLimit(port, 16);
//			
//		} catch (IOException e) {
//			logger.warn("Error while trying to set current limit of charge port {}: {}", port, e.getMessage());
//		}
//	}
//
//	@Override
//	public void onReceivedInfoData(int port) {
//		logger.debug("Received notification about an event on port {}", port);
//		try {
//			connection.requestChargePortStatus();
//			
//		} catch (IOException e) {
//			logger.warn("Error requesting new charge port status: {}", e.getMessage());
//		}
//	}
}

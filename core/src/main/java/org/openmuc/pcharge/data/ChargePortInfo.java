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
package org.openmuc.pcharge.data;

import java.util.Arrays;


/**
 * A ChargePort represents a status reading of a charging station port.
 * ChargePort is immutable. It contains a user, a timestamp, a status
 * and additional information about the charging station port.
 */
public class ChargePortInfo {

	private static final int OFFSET = 54;
	
	private final byte[] message;
	
	public ChargePortInfo(byte[] message) throws IllegalArgumentException {
		if (message == null || message.length < 40) {
			throw new IllegalArgumentException("Message array inconsistent with the Charge Port Status");
		}
		this.message = message;
	}

	public ChargePortStatus getStatus(int port) {
		return ChargePortStatus.newStatus(message[0+offset(port)]);
	}
	
	public int getCableCurrent(int port) { //current in Ampere
		return message[1+offset(port)];
	}

	public int getCurrentLimit(int port) { //current in Ampere
		return message[2+offset(port)];
	}
	
	public boolean ventilationIsRequested (int port) { //Flags of the car
		if (message[3+offset(port)] == 0x01) {
			return true;
		}
		return false;
	}
	
	public ChargeAuthorizationStatus getChargeAuthorizationStatus (int port) {
		return ChargeAuthorizationStatus.newStatus(message[4+offset(port)]);
	}
	
	public boolean isOptimized(int port) {
		return (message[5+offset(port)] == 1 ? true : false);
	}
	
	public ChargeCompleteStatus getCompleteStatus(int port) {
		return ChargeCompleteStatus.newStatus(message[6+offset(port)]);
	}

	public int getDurationCharging(int port) { // time in seconds
		int offset = offset(port);
		return byteArrayToInt(Arrays.copyOfRange(message, 7+offset, 11+offset));
	}

	public int getEnergyCharging(int port) { //energy in Wh
		int offset = offset(port);
		return byteArrayToInt(Arrays.copyOfRange(message, 11+offset, 15+offset));
	}
	
	public int getLastChargingTime(int port) { // time in seconds
		int offset = offset(port);
		return byteArrayToInt(Arrays.copyOfRange(message, 15+offset, 19+offset));
	}
	
	public int getLastChargingEnergy(int port) { //energy in Wh
		int offset = offset(port);
		return byteArrayToInt(Arrays.copyOfRange(message, 19+offset, 23+offset));
	}

	public int getEnergyTotal(int port) { //energy in Wh
		int offset = offset(port);
		return byteArrayToInt(Arrays.copyOfRange(message, 23+offset, 27+offset));
	}
	
	public int getChargingCycleCounter (int port) {
		int offset = offset(port);
		return byteArrayToInt(Arrays.copyOfRange(message, 27+offset, 31+offset));
	}
	
	public boolean isLocked (int port) {
		if (message[31+offset(port)] == 0x01) {
			return true;
		}
		return false;
	}
	
	public boolean contactorIsAktive (int port) {
		if (message[32+offset(port)] == 0x01){
			return true;
		}
		return false;
	}
	
	public boolean rcdIsAktive (int port) {
		if (message [33+offset(port)] == 0x01) {
			return true;
		}
		return false;
	}
	
	public int getPwmMinimum (int port) { // PWM in 1/100 V
		int offset = offset(port);
		return byteArrayToInt(Arrays.copyOfRange(message, 34+offset, 36+offset));
	}
	
	public int getPwmMaximum(int port) { //PWM in 1/100 V
		int offset = offset(port);
		return byteArrayToInt(Arrays.copyOfRange(message, 36+offset, 38+offset));
	}
	
	public int getVoltage(int port) { // Voltage in 1/100 V
		int offset = offset(port);
		return byteArrayToInt(Arrays.copyOfRange(message, 38+offset, 40+offset));
	}
	
	public boolean button1Start (int port) { //Start
		if ((message[40+offset(port)] & 0x01) == 1) {
			return true;
		}
		return false;
	}
	
	public boolean button2Stop (int port) { // Stop
		if ((message[40+offset(port)] & 0x02) == 1) {
			return true;
		}
		return false;
	}
	public boolean button3OptimizedCharging (int port) { // Optimized Charging
		if ((message[40+offset(port)] & 0x04) == 1) {
			return true;
		}
		return false;
	}
	public boolean button4Spare (int port) { //Spare
		if ((message[40+offset(port)] & 0x08) == 1) {
			return true;
		}
		return false;
	}
	
	public boolean ledRedError (int port) { // Error
		if ((message[40+offset(port)] & 0x10) == 1) {
			return true;
		}
		return false;
	}
	
	public boolean ledGreenReady (int port) { // On/Ready
		if ((message[40+offset(port)] & 0x20) == 1) {
			return true;
		}
		return false;
	}
	
	public boolean ledOrangeOptimizedCharging (int port) { //Optimized charging
		if ((message[40+offset(port)] & 0x40) == 1) {
			return true;
		}
		return false;
	}
	
	public boolean ledSpare (int port) { //Spare
		if ((message[40+offset(port)] & 0x80) == 1) {
			return true;
		}
		return false;
	}
	
	public boolean rfidLoggedIn (int port) {
		if ((message[41+offset(port)] & 0x01) == 0x01) {
			return true;
		}
		return false;
	}
	
	public int getRfidNumberOfGroup (int port) {
		int offset = offset(port);
		return byteArrayToInt(Arrays.copyOfRange(message, 42+offset, 44+offset));
	}
	
	public int getRfidNumberOfCard (int port) {
		int offset = offset(port);
		return byteArrayToInt(Arrays.copyOfRange(message, 44+offset, 46+offset));
	}
	
	public int getRfidUser (int port) {
		int offset = offset(port);
		return byteArrayToInt(Arrays.copyOfRange(message, 46+offset, 54+offset));
	}
	
	public boolean isCharging(int port) {
		ChargePortStatus status = getStatus(port);
		if (status == ChargePortStatus.CHARGING) {
			return true;
		}
		return false;
	}

	public boolean isConnected(int port) {
		ChargePortStatus status = getStatus(port);
		if (status == ChargePortStatus.CHARGING ||
			status == ChargePortStatus.CHARGING_ABORTED ||
			status == ChargePortStatus.CHARGING_COMPLETE ||
			status == ChargePortStatus.CHARGING_OPTIMIZED ||
			status == ChargePortStatus.CHARGING_PAUSE ||
			status == ChargePortStatus.CHARGING_PAUSE_OPTIMIZED ||
			status == ChargePortStatus.WAIT_FOR_START) {
			return true;
		}
		return false;
	}
	
	private int offset (int port) {
		if (port == 2) {
			return OFFSET;
		}
		return 0;
	}

	private int byteArrayToInt(byte[] b) {
	    return   b[3] & 0xFF |
	            (b[2] & 0xFF) << 8 |
	            (b[1] & 0xFF) << 16 |
	            (b[0] & 0xFF) << 24;
	}
}

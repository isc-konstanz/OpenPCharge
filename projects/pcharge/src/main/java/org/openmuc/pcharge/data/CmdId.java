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
package org.openmuc.pcharge.data;


public enum CmdId {

	READ_CHARGEPORT_STATUS("rc"),
	READ_CHARGEPORT_ADDITIONAL("rd"),
	CURRENT_LIMIT("st"),
	STARTSTOP("sl"),
	RFID_CARD_READ("rf"),
	RFID_CARD_WRITE("sf"),
	RFID_CARD_ACCEPT("ma"),
	RFID_READER_CMD("mc"),
	INFO("if"),
	UNKNOWN("UNKNOWN");

    private final String id;

    private CmdId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
    	return id;
    }
    
    public static String parseCmdId(byte b1, byte b2) {

		StringBuilder id = new StringBuilder();
		id.append((char) Integer.parseInt(Integer.toHexString(b1 & 0xff), 16));
		id.append((char) Integer.parseInt(Integer.toHexString(b2 & 0xff), 16));
    	
		return id.toString();
    }

	public static CmdId newCmdId(byte b1, byte b2) {
		
		switch(parseCmdId(b1, b2)) {
		case "rc":
			return READ_CHARGEPORT_STATUS;
		case "rd":
			return READ_CHARGEPORT_ADDITIONAL;
		case "st":
			return CURRENT_LIMIT;
		case "sl":
			return STARTSTOP;
		case "rf":
			return RFID_CARD_READ;
		case "sf":
			return RFID_CARD_WRITE;
		case "ma":
			return RFID_CARD_ACCEPT;
		case "mc":
			return RFID_READER_CMD;
		case "if":
			return INFO;
		default:
			return UNKNOWN;
		}
	}
}

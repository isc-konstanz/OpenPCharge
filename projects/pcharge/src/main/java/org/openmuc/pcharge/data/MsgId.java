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


public enum MsgId {

	ANSWER(0x41),
	COMMAND(0x43),
	DEBUG(0x44),
	INFO(0x49),
	UNKNOWN(-1);

    private final int code;

    private MsgId(int code) {
        this.code = code;
    }

    public byte getCode() {
        return (byte) code;
    }

	public static MsgId newMsgId(byte b) {
		
		switch(b) {
		case (byte) 0x41:
			return ANSWER;
		case (byte) 0x43:
			return COMMAND;
		case (byte) 0x44:
			return DEBUG;
		case (byte) 0x49:
			return INFO;
		default:
			return UNKNOWN;
		}
	}
}

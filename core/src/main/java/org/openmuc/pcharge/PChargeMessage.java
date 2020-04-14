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
package org.openmuc.pcharge;

import java.util.Arrays;

import org.openmuc.pcharge.data.CmdId;
import org.openmuc.pcharge.data.MsgId;

public class PChargeMessage {
	
	private final MsgId msgId;
	private final byte[] message;


	public PChargeMessage(MsgId messageId, byte[] message) {

		this.msgId = messageId;
		this.message = message;
	}

	public MsgId getMsgId() {
		return msgId;
	}

	public CmdId getCmdId() {
		return CmdId.newCmdId(message[0], message[1]);
	}

	public String getCmdString() {
		return CmdId.parseCmdId(message[0], message[1]);
	}

	public byte[] getMessage(boolean hasCmdId) {
		if (hasCmdId) {
			return Arrays.copyOfRange(message, 2, message.length);
		}
		else return message;
	}

	public byte[] getMessage() {
		return getMessage(true);
	}

	public PChargeMessage copy() {
		return new PChargeMessage(msgId, message);
	}

	/**
	 * Returns the Block Check Character of a given byte array.
	 * 
	 * @param block
	 *            the byte array of which the BCC will be calculated.
	 * @return The calculated BCC.
	 */
	public static byte getBCC(byte[] block) {
		byte bcc = 0x00;
		
        for (int i = 0; i < block.length; i++) {
            bcc ^= block[i];
        }
		return bcc;
    }

	public static PChargeMessage parse(byte[] response) throws PChargeException {
		
		if (response[0] == 0x02) {
			int endian = response[1]*256 + response[2];
			if (response[4+endian] == getBCC(Arrays.copyOfRange(response, 1, 4+endian))) {
				
				MsgId msgId = MsgId.newMsgId(response[3]);
				return new PChargeMessage(msgId, Arrays.copyOfRange(response, 4, 4+endian));
			}
			else throw new PChargeException("Received invalid Block Check Character (BCC)");
		}
		else throw new PChargeException("Received message in invalid format");
	}
}

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
package org.openmuc.pcharge;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

import org.openmuc.pcharge.data.CmdId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PChargeSocket {
	private static final Logger logger = LoggerFactory.getLogger(PChargeSocket.class);

	private static final int INPUT_BUFFER_LENGTH = 1024;
	private static final int SLEEP_INTERVAL = 50;
	private static final int TIMEOUT = 250;

	private ServerSocket serverSocket;
   	private Socket clientSocket;
   	
	private DataOutputStream os;
	private DataInputStream is;
	
	
	public PChargeSocket(Integer tcpPort) throws IOException {
		if (tcpPort == null) {
			throw new IllegalArgumentException("tcpPort may not be NULL");
		}
		
		logger.debug("Accepting connection to a client");
	    serverSocket = new ServerSocket(tcpPort);
		clientSocket = serverSocket.accept();
		
		os = new DataOutputStream(clientSocket.getOutputStream());
		is = new DataInputStream(clientSocket.getInputStream());
	}

	public void close() {
		try {
			if (serverSocket != null) {
				serverSocket.close();
				serverSocket = null;
			}
			if (clientSocket != null) {
				clientSocket.close();
				clientSocket = null;
			}
		} catch (IOException e) {
			logger.warn("Error while trying to close the connection");
		}
	}

	public synchronized PChargeMessage read() throws IOException, PChargeException  {
		
		PChargeMessage msg = null;
		
		long start = System.currentTimeMillis();
		synchronized(is) {
			byte[] buffer = new byte[INPUT_BUFFER_LENGTH];
			while (System.currentTimeMillis() - start < TIMEOUT) {
				if (is.available() > 0) {
					int numBytesRead = is.read(buffer);
					if (numBytesRead > 0) {
						msg = PChargeMessage.parse(Arrays.copyOfRange(buffer, 0, numBytesRead));

				        if (msg != null && logger.isTraceEnabled()) {
				            StringBuilder hex = new StringBuilder();
					        StringBuilder ascii = new StringBuilder();

					        ascii.append("<STX><ENDIAN>");
					        ascii.append(msg.getMsgId().getSymbol());
					        
					        byte[] byteMsg = msg.getMessage(false);
					        for (int i=0; i<byteMsg.length; i++) {
				    			String h = Integer.toHexString(byteMsg[i] & 0xff);
				    			if (h.length() < 2) {
				    	    		hex.append("0");
				            	}
					    		hex.append(h);
						        ascii.append((char) Integer.parseInt(h, 16));
					        }
					        ascii.append("<BCC>");

				            logger.trace("Server <--	{} (HEX: {})", ascii.toString(), hex.toString());
						}
				        break;
					}
				}
				try {
					Thread.sleep(SLEEP_INTERVAL);
				} catch (InterruptedException e) {
				}
			}
			if (msg == null) {
				logger.debug("Read timed out after {}s", TIMEOUT/1000);
			}
		}
		return msg;
	}

	public synchronized void write(CmdId cmdId, Integer[] message) throws IOException {
        if (clientSocket == null) {
			throw new IllegalStateException("Connection is not open.");
		}
        
		byte[] request = buildCmd(cmdId, message);
        if (logger.isTraceEnabled()) {
            StringBuilder hex = new StringBuilder();
	        StringBuilder ascii = new StringBuilder();

	        ascii.append("<STX><ENDIAN>C");
	        ascii.append(cmdId);
	        if (message != null) {
		        for (int p : message) {
			        ascii.append(Integer.toHexString(p));
		        }
	        }
	        ascii.append("<BCC>");
	        
            for (byte b : request) {
            	String h = Integer.toHexString(b & 0xFF);
            	if (h.length() < 2) {
    	    		hex.append("0");
            	}
	    		hex.append(h);
            }
            logger.trace("Server -->	{} (HEX: {})", ascii.toString(), hex.toString());
        }
		
		os.write(request);
	}

	public synchronized void write(CmdId cmdId) throws IOException {
		write(cmdId, null);
	}

    /**
     * Converts a Command ID and additional parameters to a byte command,
     * which will be recognized and responded by the P-CHARGE system.
     *
     * @param msgid
     * 			the CmdId, specifying the request, send to the client.
     * @param parameters
     * 			a list of integer, which add to the request.
     * @return the message as a byte array.
     */
	private byte[] buildCmd(CmdId cmdId, Integer[] message) {
		byte[] cmd;
        if (message == null) {
        	message = new Integer[0];
        }
    	cmd = new byte[7+message.length];

        cmd[0] = 0x02;							// STX
        int endian = 2 + message.length;		// ENDIAN
        cmd[1] = 0x00;
        while (endian > 256) {
            endian -= 256;
            cmd[1] += 0x01;
        }
        cmd[2] = (byte) endian;
        cmd[3] = 0x43;							// C
        String cmdIdString = cmdId.toString(); 	// CmdId
        cmd[4] = (byte) Integer.parseInt(Integer.toHexString(cmdIdString.charAt(0)), 16);
        cmd[5] = (byte) Integer.parseInt(Integer.toHexString(cmdIdString.charAt(1)), 16);
        
        int i = 6;
        for (int p : message) {
        	cmd[i] = (byte) p;
        	i++;
        }
        cmd[cmd.length-1] = PChargeMessage.getBCC(Arrays.copyOfRange(cmd, 1, cmd.length-1));
        
        return cmd;
    }

}